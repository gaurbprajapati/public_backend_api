/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimeLogMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimesheetSettingMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Range-based rule evaluator for timesheets with start/end time logging.
 *
 * This evaluator handles timesheets where time logs contain start and end times, allowing
 * for range-based calculations and rule evaluation.
 */
@Component
public class RangeBasedRuleEvaluator extends BaseRuleEvaluator {

	private final ITimeLogIntervalRepository timeLogIntervalRepository;

	@Autowired
	public RangeBasedRuleEvaluator(IRuleFactory ruleFactory,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger,
			ITimeLogIntervalRepository timeLogIntervalRepository) {
		super(ruleFactory, logger);
		this.timeLogIntervalRepository = timeLogIntervalRepository;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
		validateTimesheet(timesheet);
		Integer workLogType = timesheet.getTimesheetSetting().getWorkLogType();

		// Check if workLogType is START_AND_END_TIME (2)
		if (workLogType != null && workLogType == WorkLogType.START_AND_END_TIME.getTypeId()) {
			return evaluateRulesWithIntervals(timesheet);
		}

		// Default behavior for other work log types
		return super.evaluateRules(timesheet);
	}

	/**
	 * Evaluates rules for timesheets with multiple time log intervals per day. This
	 * method handles the new feature where each day can have up to 10 time log intervals.
	 *
	 * The process: 1. Fetch PKs of all time logs from the timesheet 2. Fetch all
	 * TimeLogInterval records from cst_time_log_interval_t 3. Convert intervals to
	 * expanded time log DTOs 4. Each interval is evaluated separately by the rule engine
	 * 5. Results are merged per day
	 * @param timesheet the timesheet to evaluate
	 * @return WeeklyRuleEvaluatorResult containing evaluation results
	 */
	private WeeklyRuleEvaluatorResult evaluateRulesWithIntervals(Timesheet timesheet) {
		List<TimeLog> timeLogs = timesheet.getTimeLogs();

		// Fetch expanded time logs from intervals
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> expandedTimeLogs = fetchTimeLogsFromIntervals(
				timeLogs);

		this.logger
			.logDebug(MessageFormat.format("Expanded {0} time logs to {1} interval-based time logs for timesheet {2}",
					timeLogs.size(), expandedTimeLogs.size(), timesheet.getId()));

		// Evaluate using the expanded time logs
		return evaluateWithExpandedTimeLogs(timesheet, expandedTimeLogs);
	}

	/**
	 * Fetches time logs expanded from TimeLogInterval table. Each time log that has
	 * intervals in cst_time_log_interval_t will be expanded into multiple time log DTOs,
	 * one per interval.
	 * @param timeLogs the original time logs from the timesheet
	 * @return list of expanded time log DTOs with interval-based start/end times
	 */
	private List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> fetchTimeLogsFromIntervals(
			List<TimeLog> timeLogs) {

		List<Integer> timeLogIds = timeLogs.stream().map(TimeLog::getId).toList();

		Map<Integer, List<TimeLogIntervalDto>> intervalsByTimeLogId = this.timeLogIntervalRepository
			.findIntervalsByTimeLogIds(timeLogIds);

		// Step 3: Convert intervals to expanded time log DTOs
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> expandedTimeLogs = new ArrayList<>();

		for (TimeLog timeLog : timeLogs) {
			List<TimeLogIntervalDto> intervals = intervalsByTimeLogId.get(timeLog.getId());

			if (intervals != null && !intervals.isEmpty()) {
				for (TimeLogIntervalDto interval : intervals) {
					io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog mappedTimeLog = createTimeLogFromInterval(
							timeLog, interval);

					if (mappedTimeLog != null) {
						expandedTimeLogs.add(mappedTimeLog);
					}
				}
			}
			else {
				// No intervals - use the original time log
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog mappedTimeLog = RuleEngineTimeLogMapper.INSTANCE
					.toTimeLog(timeLog);
				Duration duration = TimeHelper.calculateTimeLogDuration(mappedTimeLog);
				if (!duration.isZero()) {
					expandedTimeLogs.add(mappedTimeLog);
				}
				else {
					this.logger.logWarn(MessageFormat.format("Skipping timelog with ID {0} due to invalid time data",
							timeLog.getId()));
				}
			}
		}

		return expandedTimeLogs;
	}

	/**
	 * Creates a time log DTO from a time log entity and an interval. Overrides the
	 * start/end times with interval times and recalculates duration.
	 * @param timeLog the parent time log entity
	 * @param interval the interval to use for times
	 * @return the mapped time log DTO, or null if invalid
	 */
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog createTimeLogFromInterval(TimeLog timeLog,
			TimeLogIntervalDto interval) {

		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog mappedTimeLog = RuleEngineTimeLogMapper.INSTANCE
			.toTimeLog(timeLog);

		// Override start/end times with interval times
		LocalTime intervalStartTime = null;
		LocalTime intervalEndTime = null;

		if (interval.getWorkStartTime() != null) {
			intervalStartTime = LocalTime.ofSecondOfDay(interval.getWorkStartTime());
			mappedTimeLog.setWorkStartTime(intervalStartTime);
			// Also set normalized times so getEffectiveStartTime/EndTime works correctly
			mappedTimeLog.setNormalizedWorkStartTime(intervalStartTime);
		}
		if (interval.getWorkEndTime() != null) {
			intervalEndTime = LocalTime.ofSecondOfDay(interval.getWorkEndTime());
			mappedTimeLog.setWorkEndTime(intervalEndTime);
			// Also set normalized times so getEffectiveStartTime/EndTime works correctly
			mappedTimeLog.setNormalizedWorkEndTime(intervalEndTime);
		}

		// Calculate work time from interval start/end times
		Duration intervalDuration = TimeHelper.calculateDuration(intervalStartTime, intervalEndTime);
		mappedTimeLog.setWorkTime(intervalDuration);

		// Parse and set break intervals from JSON
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval> breakIntervals = parseBreakIntervalsFromJson(
				interval.getBreakInterval(), timeLog.getId());
		if (breakIntervals != null && !breakIntervals.isEmpty()) {
			mappedTimeLog.setBreakIntervals(breakIntervals);

			Duration totalBreakDuration = calculateTotalBreakDuration(breakIntervals);
			mappedTimeLog.setBreakTime(totalBreakDuration);
		}
		else {
			mappedTimeLog.setBreakTime(Duration.ZERO);
		}

		if (!intervalDuration.isZero()) {
			return mappedTimeLog;
		}
		else {
			this.logger.logWarn(MessageFormat.format("Skipping interval for timeLog ID {0} due to invalid time data",
					timeLog.getId()));
			return null;
		}
	}

	/**
	 * Parses break intervals from JSON string stored in
	 * cst_time_log_interval_t.break_interval column. Expected JSON format: [{"id": 1,
	 * "breakStartTime": 3600, "breakEndTime": 4200}, ...]
	 * @param breakIntervalJson the JSON string containing break intervals
	 * @param timeLogId the parent time log ID for logging
	 * @return list of TimeLogBreakInterval DTOs, or empty list if JSON is null/invalid
	 */
	private List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval> parseBreakIntervalsFromJson(
			String breakIntervalJson, Integer timeLogId) {
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval> breakIntervals = new ArrayList<>();

		if (breakIntervalJson == null || breakIntervalJson.isBlank()) {
			return breakIntervals;
		}

		try {
			com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode jsonArray = objectMapper.readTree(breakIntervalJson);

			if (jsonArray.isArray()) {
				for (com.fasterxml.jackson.databind.JsonNode node : jsonArray) {
					Integer id = (node.has("id") && !node.get("id").isNull()) ? node.get("id").asInt() : null;
					Integer breakStartTimeSeconds = (node.has("breakStartTime") && !node.get("breakStartTime").isNull())
							? node.get("breakStartTime").asInt() : null;
					Integer breakEndTimeSeconds = (node.has("breakEndTime") && !node.get("breakEndTime").isNull())
							? node.get("breakEndTime").asInt() : null;

					// Convert seconds to LocalTime
					LocalTime breakStartTime = (breakStartTimeSeconds != null)
							? LocalTime.ofSecondOfDay(breakStartTimeSeconds) : null;
					LocalTime breakEndTime = (breakEndTimeSeconds != null)
							? LocalTime.ofSecondOfDay(breakEndTimeSeconds) : null;

					// Validate break interval times
					if (breakStartTime != null && breakEndTime != null && breakStartTime.isBefore(breakEndTime)) {
						io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval breakInterval = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval();
						breakInterval.setId(id);
						breakInterval.setTimeLogId(timeLogId);
						breakInterval.setBreakStartTime(breakStartTime);
						breakInterval.setBreakEndTime(breakEndTime);
						breakIntervals.add(breakInterval);
					}
					else {
						this.logger.logWarn(MessageFormat.format(
								"Invalid break interval for timeLog ID {0}: startTime={1}, endTime={2}", timeLogId,
								breakStartTime, breakEndTime));
					}
				}
			}
		}
		catch (Exception ex) {
			this.logger.logWarn(MessageFormat.format("Failed to parse break intervals JSON for timeLog ID {0}: {1}",
					timeLogId, ex.getMessage()));
		}

		return breakIntervals;
	}

	/**
	 * Calculates total break duration from a list of break intervals.
	 * @param breakIntervals the list of break intervals
	 * @return the total duration of all break intervals
	 */
	private Duration calculateTotalBreakDuration(
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval> breakIntervals) {
		if (breakIntervals == null || breakIntervals.isEmpty()) {
			return Duration.ZERO;
		}

		Duration totalDuration = Duration.ZERO;
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval interval : breakIntervals) {
			if (interval.getBreakStartTime() != null && interval.getBreakEndTime() != null) {
				Duration intervalDuration = Duration.between(interval.getBreakStartTime(), interval.getBreakEndTime());
				if (!intervalDuration.isNegative()) {
					totalDuration = totalDuration.plus(intervalDuration);
				}
			}
		}
		return totalDuration;
	}

	/**
	 * Evaluates rules using pre-expanded time logs (from intervals). This bypasses the
	 * normal time log preparation since time logs are already expanded.
	 * @param timesheet the timesheet context
	 * @param expandedTimeLogs the expanded time logs to evaluate
	 * @return WeeklyRuleEvaluatorResult containing evaluation results
	 */
	private WeeklyRuleEvaluatorResult evaluateWithExpandedTimeLogs(Timesheet timesheet,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> expandedTimeLogs) {

		validateTimesheet(timesheet);

		// Get week start day from settings
		Integer timesheetStartDay = timesheet.getTimesheetSetting().getTimesheetStartDay();

		// Split expanded time logs on weekly basis
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> weeklyTimeLogs;
		if (timesheetStartDay == null || !this.shouldInterpretTimesheetStartDayAsWeekday(timesheet)) {
			weeklyTimeLogs = TimeHelper.splitTimeLogsOnWeeklyBasis(expandedTimeLogs);
		}
		else {
			io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay weekStartDay = io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay
				.getWorkDayType(timesheetStartDay);
			weeklyTimeLogs = TimeHelper.splitTimeLogsOnWeeklyBasis(expandedTimeLogs, weekStartDay);
		}

		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		for (List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog : weeklyTimeLogs) {
			if (weeklyTimeLog.isEmpty()) {
				continue;
			}

			// Get week start and end dates
			LocalDate weekStartDate = weeklyTimeLog.get(0).getDate();
			LocalDate weekEndDate = weeklyTimeLog.get(weeklyTimeLog.size() - 1).getDate();

			// Create a separate result for this week
			RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
			weekResult.setTimesheet(timesheet);

			evaluateWeeklyTimeLogs(weeklyTimeLog, timesheet, weekResult);

			// Post-process the week result
			postProcessResult(weekResult);

			// Add the week result to the weekly results
			weeklyResult.addWeeklyResult(weekStartDate, weekEndDate, weekResult);
		}

		return weeklyResult;
	}

	@Override
	protected boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet) {
		// Check if the rule is a weekly overtime rule type
		boolean isWeeklyOvertimeRuleType = rule.getRuleType() == RuleType.RANGE_BASED_WEEKLY_OVERTIME;

		// If it's not a weekly overtime rule type, return false
		if (!isWeeklyOvertimeRuleType) {
			return false;
		}

		// Check timesheet frequency - exclude weekly overtime rules for monthly frequency
		TimesheetFrequency frequency = RuleEngineTimesheetSettingMapper.INSTANCE
			.toTimesheetSetting(timesheet.getTimesheetSetting())
			.getTimesheetFrequency();
		// If frequency is null or not monthly, allow weekly overtime rules
		return frequency == null || frequency != TimesheetFrequency.MONTHLY;
	}

	@Override
	protected RuleType getRegularHoursRuleType() {
		return RuleType.RANGE_BASED_REGULAR_HOURS;
	}

	@Override
	protected RuleType getBreakRuleType() {
		return RuleType.RANGE_BASED_BREAK;
	}

	@Override
	protected RuleType getDefaultPayRuleType() {
		return RuleType.RANGE_BASED_DEFAULT_PAY;
	}

}
