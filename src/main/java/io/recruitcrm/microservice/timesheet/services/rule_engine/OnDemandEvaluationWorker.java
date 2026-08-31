/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.rule_engine;

import io.recruitcrm.aws.aurora.annotation.ReaderRouteGlobalConsistency;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimeLogOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.TimeLogRuleEvaluationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyRuleResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.mapper.RuleEngineMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleEngine;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Worker bean for on-demand rule engine evaluation. Each call to
 * {@link #evaluateSingleTimesheet} runs in its own transaction, allowing parallel
 * execution from separate threads.
 */
@Service
public class OnDemandEvaluationWorker {

	private static final Set<RuleType> OVERTIME_RULE_TYPES = EnumSet.of(RuleType.RANGE_BASED_AFTER_SHIFT,
			RuleType.RANGE_BASED_BEFORE_SHIFT, RuleType.RANGE_BASED_DAILY_OVERTIME,
			RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE, RuleType.DURATION_BASED_DAILY_OVERTIME,
			RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final IRuleEngine ruleEngine;

	private final RuleEngineMapper ruleEngineMapper;

	@Autowired
	public OnDemandEvaluationWorker(TimesheetJpaRepository timesheetJpaRepository, IRuleEngine ruleEngine,
			RuleEngineMapper ruleEngineMapper) {
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.ruleEngine = ruleEngine;
		this.ruleEngineMapper = ruleEngineMapper;
	}

	/**
	 * Evaluates rules for a single timesheet on-demand. Each invocation starts its own
	 * transaction so it can safely run on a worker thread.
	 * @param timesheetId the timesheet ID
	 * @param timesheetTimeLogs the time logs for this timesheet
	 * @param accountId the account ID for DB lookup
	 * @param includeWeeklyOvertime when true, populates weeklyOvertimeResults in the
	 * response (only for single-timesheet requests)
	 * @return simplified overtime result per timeLogId
	 */
	@Transactional
	@ReaderRouteGlobalConsistency
	public OnDemandTimesheetOvertimeDto evaluateSingleTimesheet(Integer timesheetId,
			List<BulkTimeLogRequestBodyDto> timesheetTimeLogs, Integer accountId, boolean includeWeeklyOvertime) {

		Optional<Timesheet> timesheetOptional = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId,
				accountId);

		if (timesheetOptional.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet not found with ID: " + timesheetId);
		}

		Timesheet timesheet = timesheetOptional.get();
		Integer workLogType = timesheet.getTimesheetSetting().getWorkLogType();

		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> timeLogs = convertBulkTimeLogsToDtos(
				timesheetTimeLogs, workLogType);

		if (timeLogs.isEmpty()) {
			return OnDemandTimesheetOvertimeDto.builder().timesheetId(timesheetId).timeLogs(List.of()).build();
		}

		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRulesOnDemand(timesheet, timeLogs);
		RuleEngineResponseBodyDto fullResponse = this.ruleEngineMapper.toRuleEngineResponseBodyDto(result);

		return extractOvertimeFromResponse(timesheetId, fullResponse, includeWeeklyOvertime);
	}

	private List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> convertBulkTimeLogsToDtos(
			List<BulkTimeLogRequestBodyDto> bulkTimeLogs, Integer workLogType) {

		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = new ArrayList<>();
		boolean isRangeBased = workLogType != null && workLogType == WorkLogType.START_AND_END_TIME.getTypeId();

		for (BulkTimeLogRequestBodyDto bulkTimeLog : bulkTimeLogs) {
			if (isRangeBased) {
				collectRangeBasedTimeLogs(bulkTimeLog, result);
			}
			else {
				collectDurationBasedTimeLog(bulkTimeLog, result);
			}
		}

		return result;
	}

	private void collectRangeBasedTimeLogs(BulkTimeLogRequestBodyDto bulkTimeLog,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result) {

		List<WorkTimeDetailDto> details = bulkTimeLog.getWorkTimeDetails();
		if (details == null || details.isEmpty()) {
			return;
		}

		int timeLogId = bulkTimeLog.getId();
		LocalDate date = LocalDate.ofEpochDay(bulkTimeLog.getDate() / 86400L);
		WorkDayType dayType = WorkDayType.fromId(bulkTimeLog.getDayTypeId());

		// Sort intervals chronologically by work start time so the rule engine sees the
		// same ordering as the persisted flow (see
		// TimeLogIntervalRepository.findIntervalsByTimeLogIds which orders by
		// WORK_START_TIME). Without this, rules that mutate the shared per-day
		// occupiedTimeRanges state (e.g. BeforeShift, AfterShift, DailyOvertime
		// allocator) produce different results for the same set of intervals when the
		// caller passes them out of order.
		List<WorkTimeDetailDto> sortedDetails = sortByWorkStartTime(details);

		for (WorkTimeDetailDto detail : sortedDetails) {
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = createRangeBasedTimeLog(timeLogId,
					date, dayType, detail);
			if (timeLog != null) {
				result.add(timeLog);
			}
		}
	}

	private void collectDurationBasedTimeLog(BulkTimeLogRequestBodyDto bulkTimeLog,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result) {

		if (bulkTimeLog.getWorkTime() == null || bulkTimeLog.getWorkTime() <= 0) {
			return;
		}

		int timeLogId = bulkTimeLog.getId();
		LocalDate date = LocalDate.ofEpochDay(bulkTimeLog.getDate() / 86400L);
		WorkDayType dayType = WorkDayType.fromId(bulkTimeLog.getDayTypeId());

		result.add(createDurationBasedTimeLog(timeLogId, date, dayType, bulkTimeLog.getWorkTime(),
				bulkTimeLog.getBreakTime()));
	}

	private List<WorkTimeDetailDto> sortByWorkStartTime(List<WorkTimeDetailDto> details) {
		List<WorkTimeDetailDto> sorted = new ArrayList<>(details);
		sorted.sort(Comparator.comparing((d) -> (d != null) ? d.getWorkStartTime() : null,
				Comparator.nullsFirst(Comparator.naturalOrder())));
		return sorted;
	}

	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog createRangeBasedTimeLog(int timeLogId,
			LocalDate date, WorkDayType dayType, WorkTimeDetailDto detail) {

		if (detail.getWorkStartTime() == null || detail.getWorkEndTime() == null) {
			return null;
		}

		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		timeLog.setId(timeLogId);
		timeLog.setDate(date);
		timeLog.setDayType(dayType);

		LocalTime startTime = LocalTime.ofSecondOfDay(detail.getWorkStartTime());
		LocalTime endTime = LocalTime.ofSecondOfDay(detail.getWorkEndTime());
		timeLog.setWorkStartTime(startTime);
		timeLog.setWorkEndTime(endTime);
		timeLog.setNormalizedWorkStartTime(startTime);
		timeLog.setNormalizedWorkEndTime(endTime);

		Duration workDuration = TimeHelper.calculateDuration(startTime, endTime);
		timeLog.setWorkTime(workDuration);

		if (detail.getBreakIntervals() != null && !detail.getBreakIntervals().isEmpty()) {
			List<TimeLogBreakInterval> breakIntervals = new ArrayList<>();
			Duration totalBreakDuration = Duration.ZERO;

			for (BreakIntervalDto breakDto : detail.getBreakIntervals()) {
				if (breakDto.getBreakStartTime() != null && breakDto.getBreakEndTime() != null) {
					TimeLogBreakInterval breakInterval = new TimeLogBreakInterval();
					breakInterval.setBreakStartTime(LocalTime.ofSecondOfDay(breakDto.getBreakStartTime()));
					breakInterval.setBreakEndTime(LocalTime.ofSecondOfDay(breakDto.getBreakEndTime()));
					breakIntervals.add(breakInterval);
					totalBreakDuration = totalBreakDuration
						.plus(Duration.between(breakInterval.getBreakStartTime(), breakInterval.getBreakEndTime()));
				}
			}

			timeLog.setBreakIntervals(breakIntervals);
			timeLog.setBreakTime(totalBreakDuration);
		}
		else {
			timeLog.setBreakTime(Duration.ZERO);
		}

		if (!workDuration.isZero()) {
			return timeLog;
		}
		return null;
	}

	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog createDurationBasedTimeLog(int timeLogId,
			LocalDate date, WorkDayType dayType, Integer workTimeSeconds, Integer breakTimeSeconds) {

		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		timeLog.setId(timeLogId);
		timeLog.setDate(date);
		timeLog.setDayType(dayType);

		Duration duration = Duration.ofSeconds(workTimeSeconds);
		timeLog.setWorkTime(duration);

		// Propagate break time so daily/weekly overtime calculations can correctly
		// subtract unpaid break (mirrors RuleEngineTimeLogMapper behavior used by the
		// non on-demand evaluation flow). Without this, breakTime stayed null and
		// effective work hours were overstated, causing spurious overtime.
		if (breakTimeSeconds != null && breakTimeSeconds > 0) {
			timeLog.setBreakTime(Duration.ofSeconds(breakTimeSeconds));
		}
		else {
			timeLog.setBreakTime(Duration.ZERO);
		}

		LocalTime startTime = LocalTime.MIDNIGHT;
		LocalTime endTime = TimeHelper.normaliseDurationToLocalTime(duration);
		timeLog.setNormalizedWorkStartTime(startTime);
		timeLog.setNormalizedWorkEndTime(endTime);

		return timeLog;
	}

	private OnDemandTimesheetOvertimeDto extractOvertimeFromResponse(Integer timesheetId,
			RuleEngineResponseBodyDto fullResponse, boolean includeWeeklyOvertime) {

		List<OnDemandTimeLogOvertimeDto> timeLogOvertimes = new ArrayList<>();

		if (fullResponse.getWeeklyResults() == null) {
			return OnDemandTimesheetOvertimeDto.builder().timesheetId(timesheetId).timeLogs(timeLogOvertimes).build();
		}

		List<Long> weeklyOvertimeResults = (includeWeeklyOvertime) ? new ArrayList<>() : null;

		for (WeeklyRuleResultResponseBodyDto weeklyResult : fullResponse.getWeeklyResults()) {
			if (weeklyResult == null) {
				continue;
			}
			collectOvertimesFromWeeklyResult(weeklyResult, timeLogOvertimes);
			if (includeWeeklyOvertime) {
				weeklyOvertimeResults.add(extractWeeklyOvertimeSeconds(weeklyResult));
			}
		}

		return OnDemandTimesheetOvertimeDto.builder()
			.timesheetId(timesheetId)
			.timeLogs(timeLogOvertimes)
			.weeklyOvertimeResults(weeklyOvertimeResults)
			.build();
	}

	private long extractWeeklyOvertimeSeconds(WeeklyRuleResultResponseBodyDto weeklyResult) {
		if (weeklyResult.getWeeklyOvertimeResult() == null) {
			return 0L;
		}
		Long seconds = weeklyResult.getWeeklyOvertimeResult().getWeeklyOvertimeHoursInSeconds();
		return (seconds != null) ? seconds : 0L;
	}

	private void collectOvertimesFromWeeklyResult(WeeklyRuleResultResponseBodyDto weeklyResult,
			List<OnDemandTimeLogOvertimeDto> timeLogOvertimes) {

		if (weeklyResult.getTimeLogRuleEvaluations() == null) {
			return;
		}

		for (TimeLogRuleEvaluationResponseBodyDto timeLogEval : weeklyResult.getTimeLogRuleEvaluations()) {
			timeLogOvertimes.add(OnDemandTimeLogOvertimeDto.builder()
				.timeLogId(timeLogEval.getTimeLogId())
				.overtimeInSeconds(calculateOvertimeSeconds(timeLogEval))
				.build());
		}
	}

	private long calculateOvertimeSeconds(TimeLogRuleEvaluationResponseBodyDto timeLogEval) {
		if (timeLogEval.getRuleEvaluationResults() == null) {
			return 0L;
		}

		long overtime = 0L;
		for (RuleEvaluationResultResponseBodyDto ruleResult : timeLogEval.getRuleEvaluationResults()) {
			if (isOvertimeRuleWithDuration(ruleResult)) {
				overtime += ruleResult.getEvaluatedDurationInSeconds();
			}
		}
		return overtime;
	}

	private boolean isOvertimeRuleWithDuration(RuleEvaluationResultResponseBodyDto ruleResult) {
		return ruleResult.getRuleType() != null && OVERTIME_RULE_TYPES.contains(ruleResult.getRuleType())
				&& ruleResult.getEvaluatedDurationInSeconds() != null;
	}

}
