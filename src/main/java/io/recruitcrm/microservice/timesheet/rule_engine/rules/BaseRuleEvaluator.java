/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimesheetSettingMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimeLogMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.UnifiedRuleManager;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for rule evaluators providing common functionality.
 *
 * This class implements the Template Method pattern, allowing subclasses to customize
 * specific parts of the evaluation process while sharing common logic.
 */
public abstract class BaseRuleEvaluator implements IRuleEvaluator {

	protected final IRuleFactory ruleFactory;

	protected final Logger logger;

	protected BaseRuleEvaluator(IRuleFactory ruleFactory, Logger logger) {
		this.ruleFactory = ruleFactory;
		this.logger = logger;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
		validateTimesheet(timesheet);

		List<TimeLog> timeLogs = timesheet.getTimeLogs();

		// Note: When workLogType is START_AND_END_TIME (2), RangeBasedRuleEvaluator
		// overrides this method and fetches time logs from TimeLogInterval table instead.
		// This base implementation handles the default case using
		// timesheet.getTimeLogs().

		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> weeklyTimeLogs = prepareWeeklyTimeLogs(
				timeLogs, timesheet);

		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		for (List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog : weeklyTimeLogs) {
			if (weeklyTimeLog.isEmpty()) {
				continue; // Skip empty weeks
			}

			// Get week start and end dates
			LocalDate weekStartDate = weeklyTimeLog.get(0).getDate();
			LocalDate weekEndDate = weeklyTimeLog.get(weeklyTimeLog.size() - 1).getDate();

			// Create a separate result for this week
			RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
			weekResult.setTimesheet(timesheet);

			// Evaluate this week's time logs
			evaluateWeeklyTimeLogs(weeklyTimeLog, timesheet, weekResult);

			// Post-process the week result
			postProcessResult(weekResult);

			// Add the week result to the weekly results
			weeklyResult.addWeeklyResult(weekStartDate, weekEndDate, weekResult);
		}

		return weeklyResult;
	}

	/**
	 * Evaluates rules using pre-built time log DTOs (for on-demand evaluation). This
	 * method bypasses DB-based time log fetching and uses provided time logs directly.
	 * Validation is relaxed: only checks timesheet and settings, not
	 * timesheet.getTimeLogs() since time logs come from the request body.
	 * @param timesheet the timesheet context (used for settings, rules, frequency)
	 * @param expandedTimeLogs the pre-built time log DTOs to evaluate
	 * @return WeeklyRuleEvaluatorResult containing evaluation results
	 */
	public WeeklyRuleEvaluatorResult evaluatePreBuiltTimeLogs(Timesheet timesheet,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> expandedTimeLogs) {

		if (timesheet == null) {
			throw new IllegalArgumentException("Timesheet cannot be null");
		}
		if (timesheet.getTimesheetSetting() == null) {
			throw new IllegalArgumentException("Timesheet setting cannot be null");
		}

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

	/**
	 * Validates the timesheet before evaluation.
	 */
	protected void validateTimesheet(Timesheet timesheet) {
		if (timesheet == null) {
			throw new IllegalArgumentException("Timesheet cannot be null");
		}
		if (timesheet.getTimesheetSetting() == null) {
			throw new IllegalArgumentException("Timesheet setting cannot be null");
		}
		if (timesheet.getTimeLogs() == null || timesheet.getTimeLogs().isEmpty()) {
			throw new IllegalArgumentException("Timesheet must contain time logs");
		}
	}

	/**
	 * Prepares weekly time logs for evaluation.
	 */
	protected List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
			List<TimeLog> timeLogs) {
		return TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs.stream().filter((timeLog) -> {
			// Filter out timelogs with invalid time data using the new utility method
			Duration duration = TimeHelper
				.calculateTimeLogDuration(RuleEngineTimeLogMapper.INSTANCE.toTimeLog(timeLog));
			if (duration.isZero()) {
				this.logger.logWarn(
						MessageFormat.format("Skipping timelog with ID {0} due to invalid time data", timeLog.getId()));
				return false;
			}
			return true;
		}).map(RuleEngineTimeLogMapper.INSTANCE::toTimeLog).toList());
	}

	/**
	 * Prepares weekly time logs for evaluation with custom week start day.
	 * @param timeLogs the list of time logs to process
	 * @param weekStartDay the day of the week to start weeks from (e.g.,
	 * WorkDay.WEDNESDAY)
	 * @return list of time log lists, each representing a week
	 */
	protected List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
			List<TimeLog> timeLogs, io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay weekStartDay) {
		return TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs.stream().filter((timeLog) -> {
			// Filter out timelogs with invalid time data using the new utility method
			Duration duration = TimeHelper
				.calculateTimeLogDuration(RuleEngineTimeLogMapper.INSTANCE.toTimeLog(timeLog));
			if (duration.isZero()) {
				this.logger.logWarn(
						MessageFormat.format("Skipping timelog with ID {0} due to invalid time data", timeLog.getId()));
				return false;
			}
			return true;
		}).map(RuleEngineTimeLogMapper.INSTANCE::toTimeLog).toList(), weekStartDay);
	}

	/**
	 * For weekly/biweekly timesheets, {@code timesheetStartDay} is ISO day-of-week
	 * (1=Monday…7=Sunday). For monthly timesheets it is the calendar start day of the
	 * period (1–31), so it must not be passed to
	 * {@link io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay#getWorkDayType(Integer)}.
	 */
	protected boolean shouldInterpretTimesheetStartDayAsWeekday(Timesheet timesheet) {
		Integer frequencyId = timesheet.getTimesheetSetting().getTimesheetFrequency();
		if (frequencyId == null || frequencyId == 0) {
			return true;
		}
		try {
			return TimesheetFrequency.valueOf(frequencyId) != TimesheetFrequency.MONTHLY;
		}
		catch (IllegalArgumentException ex) {
			return true;
		}
	}

	/**
	 * Prepares weekly time logs for evaluation using timesheet start day from settings.
	 *
	 * This method uses the timesheetStartDay from TimesheetSetting to determine the week
	 * start day for splitting time logs (weekly/biweekly only).
	 * @param timeLogs the list of time logs to process
	 * @param timesheet the timesheet containing the settings
	 * @return list of time log lists, each representing a week
	 */
	protected List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
			List<TimeLog> timeLogs, Timesheet timesheet) {

		// Get the timesheet start day from settings
		Integer timesheetStartDay = timesheet.getTimesheetSetting().getTimesheetStartDay();

		// If timesheet start day is not set, or monthly (calendar day, not weekday), use
		// default week boundaries for rule evaluation
		if (timesheetStartDay == null || !this.shouldInterpretTimesheetStartDayAsWeekday(timesheet)) {
			return prepareWeeklyTimeLogs(timeLogs);
		}

		// Convert integer to WorkDay enum
		io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay weekStartDay = io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay
			.getWorkDayType(timesheetStartDay);

		return prepareWeeklyTimeLogs(timeLogs, weekStartDay);
	}

	/**
	 * Evaluates all time logs within a week. Same-day intervals share an EvaluationState
	 * so that occupied ranges accumulate across intervals for the same day (needed for
	 * Daily Overtime to see Regular Hours occupied ranges from earlier intervals).
	 */
	protected void evaluateWeeklyTimeLogs(
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog, Timesheet timesheet,
			RuleEvaluatorResult result) {
		// Share EvaluationState across same-day intervals so occupied ranges
		// accumulate within a day but reset across days
		Map<LocalDate, EvaluationState> dailyStates = new LinkedHashMap<>();
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog : weeklyTimeLog) {
			LocalDate date = timeLog.getDate();
			EvaluationState dailyState = dailyStates.computeIfAbsent(date, (d) -> new EvaluationState());
			evaluateTimeLog(timeLog, weeklyTimeLog, timesheet, result, dailyState);
		}

		// Ensure all time logs appear in the result map even if no rules applied.
		// This is needed so the response mapper includes all intervals for correct
		// startTime/endTime calculation.
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog : weeklyTimeLog) {
			result.getRuleEvaluationResults().putIfAbsent(timeLog, new java.util.ArrayList<>());
		}

		// Evaluate weekly overtime rules once at the end of the week
		evaluateWeeklyOvertimeRules(weeklyTimeLog, timesheet, result);

		// After Weekly OT has claimed its share, sweep any still-unallocated worked time
		// into Default Pay (gated on TimesheetSetting.isUnplannedHoursPayEnabled).
		evaluateDefaultPayRules(weeklyTimeLog, timesheet, result);
	}

	/**
	 * Week-end Default Pay sweep. Pays any worked time that no other rule claimed
	 * (Regular Hours, shift/specific rules, Daily OT, Weekly OT) at the base pay/bill
	 * rate × 1.0.
	 *
	 * Runs only when {@code TimesheetSetting.isUnplannedHoursPayEnabled == 1}.
	 *
	 * Per-day chronological attribution: Weekly OT consumes the latest days first (the
	 * hours that pushed the worker over the weekly threshold). Within a day, Weekly OT
	 * takes the tail and Default Pay takes the head — matching the intuition that
	 * overtime accrues at the end of the day/week.
	 */
	protected void evaluateDefaultPayRules(
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog, Timesheet timesheet,
			RuleEvaluatorResult result) {

		Integer flag = timesheet.getTimesheetSetting().getIsUnplannedHoursPayEnabled();
		if (flag == null || flag != 1) {
			return;
		}

		// 1. Group time logs by calendar date (multi-interval days share a date).
		Map<LocalDate, List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> timeLogsByDate = new LinkedHashMap<>();
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog tl : weeklyTimeLog) {
			timeLogsByDate.computeIfAbsent(tl.getDate(), (d) -> new ArrayList<>()).add(tl);
		}

		// 2. For each date compute free ranges = (union of work intervals) − (union of
		// ranges claimed by already-evaluated daily rules on that date).
		Map<LocalDate, RangeSet<LocalTime>> freeRangesByDate = new LinkedHashMap<>();
		Map<LocalDate, Duration> freeDurationByDate = new LinkedHashMap<>();
		for (Map.Entry<LocalDate, List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> entry : timeLogsByDate
			.entrySet()) {
			RangeSet<LocalTime> freeRanges = computeFreeRangesForDate(entry.getValue(), result);
			freeRangesByDate.put(entry.getKey(), freeRanges);
			freeDurationByDate.put(entry.getKey(), TimeHelper.convertRangeSetToDuration(freeRanges));
		}

		// 3. Read Weekly OT consumed duration (0 when no WOT rule, threshold not crossed,
		// or frequency is monthly).
		Duration wotRemaining = Duration.ZERO;
		if (result.getWeeklyOvertimeRuleEvaluationResult() != null
				&& result.getWeeklyOvertimeRuleEvaluationResult().getWeeklyOvertimeHours() != null) {
			wotRemaining = result.getWeeklyOvertimeRuleEvaluationResult().getWeeklyOvertimeHours();
		}

		// 4. Walk dates latest → earliest, subtract WOT from each date's free duration.
		List<LocalDate> datesDescending = new ArrayList<>(freeRangesByDate.keySet());
		datesDescending.sort(Comparator.reverseOrder());
		Map<LocalDate, Duration> defaultPayDurationByDate = computeDefaultPayDurationByDate(datesDescending,
				freeDurationByDate, wotRemaining);

		// 5. For each date with non-zero Default Pay, carve the EARLIEST portion of its
		// free ranges matching the duration, evaluate via the Default Pay rule, and
		// attach the result to the first time log of the date.
		RuleType defaultPayRuleType = getDefaultPayRuleType();
		for (Map.Entry<LocalDate, Duration> entry : defaultPayDurationByDate.entrySet()) {
			LocalDate date = entry.getKey();
			RangeSet<LocalTime> dpRanges = takeFromStart(freeRangesByDate.get(date), entry.getValue());
			if (dpRanges.isEmpty()) {
				continue;
			}

			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog attachTo = timeLogsByDate.get(date).get(0);

			RuleEvaluationContext context = new RuleEvaluationContext();
			context.setTimesheet(timesheet);
			context.setTimesheetSetting(timesheet.getTimesheetSetting());
			context.setTimesheetSettingDto(
					RuleEngineTimesheetSettingMapper.INSTANCE.toTimesheetSetting(timesheet.getTimesheetSetting()));
			context.setTimeRangesToEvaluate(dpRanges);
			context.setCurrentTimeLogBeingEvaluated(attachTo);
			context.setCurrentRuleBeingEvaluated(null);
			context.setCurrentRuleIndex(-1);

			RuleEvaluationResult dpResult = this.ruleFactory.createRule(defaultPayRuleType, this.logger)
				.evaluate(context);
			if (dpResult != null) {
				dpResult.setTimeRange(dpRanges);
				result.addRuleEvaluationResult(attachTo, dpResult);
				this.logger
					.logDebug(MessageFormat.format("Default Pay evaluated for {0}: duration={1}s, pay={2}, bill={3}",
							date, TimeHelper.convertRangeSetToDuration(dpRanges).getSeconds(),
							dpResult.getTotalPayAmount(), dpResult.getTotalBillAmount()));
			}
		}
	}

	/**
	 * Walks calendar dates in descending order (latest first) and deducts Weekly OT hours
	 * from each date's free (unallocated) duration. Returns a per-date Default Pay
	 * duration map ordered from latest to earliest date.
	 */
	private Map<LocalDate, Duration> computeDefaultPayDurationByDate(List<LocalDate> datesDescending,
			Map<LocalDate, Duration> freeDurationByDate, Duration wotRemaining) {
		Map<LocalDate, Duration> defaultPayDurationByDate = new LinkedHashMap<>();
		for (LocalDate date : datesDescending) {
			Duration dateFree = freeDurationByDate.get(date);
			Duration consumed = TimeHelper.getMinimumDuration(dateFree, wotRemaining);
			if (consumed.isNegative()) {
				consumed = Duration.ZERO;
			}
			Duration defaultPay = dateFree.minus(consumed);
			defaultPayDurationByDate.put(date, defaultPay);
			wotRemaining = wotRemaining.minus(consumed);
			if (wotRemaining.isNegative()) {
				wotRemaining = Duration.ZERO;
			}
		}
		return defaultPayDurationByDate;
	}

	/**
	 * Builds the per-date free-range set = (union of each interval's effective
	 * start..end) minus (union of all rule-result time ranges attached to that date's
	 * time logs). Same shape as the mapper's per-interval unallocated calculation, but
	 * aggregated across all intervals of the date.
	 */
	private RangeSet<LocalTime> computeFreeRangesForDate(
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> dateTimeLogs,
			RuleEvaluatorResult result) {

		RangeSet<LocalTime> workRanges = TreeRangeSet.create();
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog tl : dateTimeLogs) {
			LocalTime start = TimeHelper.getEffectiveStartTime(tl);
			LocalTime end = TimeHelper.getEffectiveEndTime(tl);
			if (start != null && end != null && start.isBefore(end)) {
				workRanges.add(TimeHelper.toRange(start, end));
			}
		}

		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog tl : dateTimeLogs) {
			List<RuleEvaluationResult> ruleResults = result.getRuleEvaluationResults().get(tl);
			if (ruleResults == null) {
				continue;
			}
			for (RuleEvaluationResult rr : ruleResults) {
				if (rr != null && rr.getTimeRange() != null) {
					occupied.addAll(rr.getTimeRange());
				}
			}
		}

		RangeSet<LocalTime> free = TreeRangeSet.create();
		for (com.google.common.collect.Range<LocalTime> workRange : workRanges.asRanges()) {
			free.addAll(TimeHelper.getFreeTimeRanges(workRange, occupied));
		}
		return free;
	}

	/**
	 * Takes the chronologically earliest portion of {@code rangeSet} whose total duration
	 * equals {@code duration}. Guava's {@code asRanges()} iterates in natural (ascending)
	 * order, so the head is picked first; a partial tail range is split when needed.
	 */
	private RangeSet<LocalTime> takeFromStart(RangeSet<LocalTime> rangeSet, Duration duration) {
		RangeSet<LocalTime> taken = TreeRangeSet.create();
		if (rangeSet == null || rangeSet.isEmpty() || duration == null || duration.isZero() || duration.isNegative()) {
			return taken;
		}
		Duration remaining = duration;
		for (com.google.common.collect.Range<LocalTime> range : rangeSet.asRanges()) {
			if (remaining.isZero() || remaining.isNegative()) {
				break;
			}
			Duration rangeDur = Duration.between(range.lowerEndpoint(), range.upperEndpoint());
			if (rangeDur.compareTo(remaining) <= 0) {
				taken.add(range);
				remaining = remaining.minus(rangeDur);
			}
			else {
				LocalTime splitPoint = range.lowerEndpoint().plus(remaining);
				taken.add(com.google.common.collect.Range.closedOpen(range.lowerEndpoint(), splitPoint));
				remaining = Duration.ZERO;
			}
		}
		return taken;
	}

	/**
	 * Evaluates weekly overtime rules once at the end of the week.
	 */
	protected void evaluateWeeklyOvertimeRules(
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog, Timesheet timesheet,
			RuleEvaluatorResult result) {
		List<CustomRule> customRules = RuleEngineTimesheetSettingMapper.INSTANCE
			.toCustomRuleList(timesheet.getTimesheetSetting().getCustomRule());

		// Create unified rule list with both Regular Hours and Break virtual rules
		List<IEvaluatableRule> unifiedRules = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
				getRegularHoursRuleType(), getBreakRuleType(), timesheet);

		// Get all weekly overtime candidate time ranges collected during daily processing
		List<RangeSet<LocalTime>> allWeeklyOvertimeCandidateTimeRanges = result
			.getAllWeeklyOvertimeCandidateTimeRanges();

		// Evaluate only weekly overtime rules
		for (int i = 0; i < unifiedRules.size(); i++) {
			IEvaluatableRule currentRule = unifiedRules.get(i);

			if (isWeeklyOvertimeRule(currentRule, timesheet)) {
				this.logger
					.logDebug(MessageFormat.format("Evaluating weekly overtime rule: {0}", currentRule.getRuleName()));

				// Create context for weekly overtime evaluation
				RuleEvaluationContext context = createWeeklyOvertimeRuleEvaluationContext(currentRule, i, timesheet);
				context.setWeeklyTimeLogs(weeklyTimeLog);
				context.setWeeklyOvertimeCandidateTimeRanges(allWeeklyOvertimeCandidateTimeRanges);

				// Determine rule type based on the current rule
				RuleType ruleType = currentRule.getRuleType();
				RuleEvaluationResult ruleResult = this.ruleFactory.createRule(ruleType, this.logger).evaluate(context);

				if (ruleResult != null) {
					// Set the weekly overtime result directly (no accumulation needed)
					result.setWeeklyOvertimeRuleEvaluationResult(ruleResult);
					this.logger.logDebug(MessageFormat.format(
							"Weekly overtime evaluation complete - Pay=${0}, Bill=${1}, Hours={2}s",
							ruleResult.getTotalPayAmount(), ruleResult.getTotalBillAmount(),
							getDurationInSeconds(ruleResult.getWeeklyOvertimeHours())));
				}
			}
		}
	}

	/**
	 * Evaluates a single time log against all applicable rules. This method processes one
	 * interval (e.g., morning 9-12) and applies all rules (Regular Hours, Break, Daily
	 * OT, Weekly OT) while maintaining shared daily state.
	 * @param timeLog the current time log interval being evaluated
	 * @param weeklyTimeLog all time logs for this week (for context)
	 * @param timesheet the timesheet containing settings and rules
	 * @param result accumulator for all rule evaluation results
	 * @param state shared daily state (occupied ranges, worked hours, break adjustments)
	 */
	protected void evaluateTimeLog(io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog, Timesheet timesheet,
			RuleEvaluatorResult result, EvaluationState state) {

		// Initialize list to collect weekly overtime candidate ranges for this time log
		// These will be evaluated later in evaluateWeeklyOvertimeRules() after all daily
		// processing
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		// Extract custom rules from timesheet settings (e.g., Daily OT at 8h, Weekly OT
		// at 40h)
		// These are user-configured rules with thresholds, multipliers, and applicability
		// days
		List<CustomRule> customRules = RuleEngineTimesheetSettingMapper.INSTANCE
			.toCustomRuleList(timesheet.getTimesheetSetting().getCustomRule());

		// Create a unified rule list combining custom rules + system rules (Regular Hours
		// + Break)
		// Order matters: Regular Hours → Break → Daily OT → Weekly OT (precedence-based)
		// System rules are automatically inserted based on rule type (range-based vs
		// duration-based)
		List<IEvaluatableRule> unifiedRules = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
				getRegularHoursRuleType(), getBreakRuleType(), timesheet);

		// Loop through all rules in precedence order and evaluate each one
		// Each rule gets a chance to "claim" time ranges that haven't been occupied by
		// previous rules
		for (int i = 0; i < unifiedRules.size(); i++) {
			IEvaluatableRule currentRule = unifiedRules.get(i);

			// Check if this rule should run on the current day (e.g., Monday-Friday only)
			// System rules use isRuleApplicableOnDay(), custom rules use
			// isApplicableOnDay()
			boolean isApplicable = (currentRule.isSystemRule()) ? isRuleApplicableOnDay(currentRule, timeLog, timesheet) // For
																															// Regular
																															// Hours/Break
																															// rules
					: currentRule.isApplicableOnDay(TimeHelper.getWorkDayFromDate(timeLog.getDate())); // For
																										// custom
																										// rules

			// Only process rules that are applicable on this day (skip weekend rules on
			// weekdays, etc.)
			if (isApplicable) {

				// Get all time logs for the same date as current time log (for
				// multi-interval support)
				// Example: If current is "Jan 15 morning", this returns ["Jan 15
				// morning", "Jan 15 afternoon"]
				List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> sameDayLogs = TimeHelper
					.getSameDayTimeLogs(timeLog, weeklyTimeLog);

				// Determine if this day has multiple work intervals (morning + afternoon)
				// This affects how we handle Daily OT and Weekly OT calculations
				boolean isMultiInterval = sameDayLogs.size() > 1;

				// SPECIAL HANDLING FOR WEEKLY OVERTIME RULES
				// Weekly OT is evaluated separately after all daily processing is
				// complete
				// During daily processing, we only collect "candidate" time ranges for
				// later evaluation
				if (isWeeklyOvertimeRule(currentRule, timesheet)) {

					if (isMultiInterval && !TimeHelper.isLastIntervalOfDay(timeLog, sameDayLogs)) {
						continue;
					}

					collectWeeklyOvertimeCandidates(timeLog, currentRule, timesheet, state, unifiedRules, weeklyTimeLog,
							sameDayLogs, isMultiInterval, weeklyOvertimeCandidateTimeRanges);
					continue;
				}

				// REGULAR RULE PROCESSING (Regular Hours, Break, Daily OT)
				// Calculate what time ranges this rule wants to claim
				// This calls the appropriate resolver (RegularHoursResolver,
				// BreakResolver, DailyOTResolver)
				RangeSet<LocalTime> timeRanges = resolveTimeRangesForRule(timeLog, currentRule, timesheet, state,
						unifiedRules, weeklyTimeLog);

				// Only proceed if the rule identified some time ranges to claim
				if (!timeRanges.isEmpty()) {

					// SPECIAL HANDLING FOR DAILY OVERTIME WITH MULTI-INTERVALS
					// Daily OT needs to see the entire day's work to calculate correctly
					// For multi-interval days, constrain Daily OT to the merged day
					// boundary
					// For other rules and single intervals, use the individual interval
					// boundary
					io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog constrainTarget = (isDailyOvertimeRuleType(
							currentRule) && isMultiInterval) ? TimeHelper.createMergedTimeLog(timeLog, sameDayLogs) // Daily
																													// OT:
																													// use
																													// merged
																													// day
																													// (9:00-18:00)
									: timeLog; // Other rules: use individual interval
												// (9:00-12:00 or 13:00-18:00)

					// Clip the rule's time ranges to fit within the appropriate boundary
					// This ensures rules don't claim time outside their allowed scope
					RangeSet<LocalTime> constrainedRangeSet = TimeHelper
						.constrainTimeRangeToTimeLogBoundaries(timeRanges, constrainTarget);

					// Only proceed if there are valid ranges after constraining
					if (!constrainedRangeSet.isEmpty()) {

						// UPDATE SHARED DAILY STATE
						// Add the claimed time ranges to occupied ranges (prevents
						// double-counting)
						// Update worked hours total for the day (for Daily OT
						// calculations)
						// Accumulate break threshold adjustments (for accurate Daily OT)
						updateEvaluationState(state, constrainedRangeSet);

						// PROCESS THE RULE EVALUATION
						// Calculate pay/bill amounts, apply multipliers, format results
						// This creates the actual RuleEvaluationResult with dollar
						// amounts and hours
						RuleEvaluationResult ruleResult = processRuleEvaluation(timeLog, currentRule,
								constrainedRangeSet, i, weeklyTimeLog, weeklyOvertimeCandidateTimeRanges, timesheet);

						// If the rule evaluation produced a valid result
						if (ruleResult != null) {
							// Add the result to the final output
							// This associates the rule result with the specific time log
							// for response mapping
							addRuleResultToEvaluatorResult(result, timeLog, currentRule, ruleResult, timesheet);
						}
					}
				}
			}
		}

		// STORE WEEKLY OVERTIME CANDIDATES
		// After processing all rules for this time log, store any weekly OT candidates
		// These will be evaluated later in evaluateWeeklyOvertimeRules() after all daily
		// processing
		if (!weeklyOvertimeCandidateTimeRanges.isEmpty()) {
			result.addWeeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges);
		}
	}

	/**
	 * Collects weekly overtime candidate time ranges for a single day. For multi-interval
	 * days, computes candidates directly from the merged day boundary minus all occupied
	 * ranges, then constrains to actual worked intervals (excluding gaps between
	 * intervals). For single-interval days, delegates to the existing resolver.
	 */
	private void collectWeeklyOvertimeCandidates(io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
			IEvaluatableRule currentRule, Timesheet timesheet, EvaluationState state,
			List<IEvaluatableRule> unifiedRules,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> sameDayLogs, boolean isMultiInterval,
			List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges) {

		if (isMultiInterval) {
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog mergedTimeLog = TimeHelper
				.createMergedTimeLog(timeLog, sameDayLogs);
			LocalTime mergedStart = TimeHelper.getEffectiveStartTime(mergedTimeLog);
			LocalTime mergedEnd = TimeHelper.getEffectiveEndTime(mergedTimeLog);

			if (mergedStart != null && mergedEnd != null && mergedStart.isBefore(mergedEnd)) {
				RangeSet<LocalTime> freeRanges = TimeHelper
					.getFreeTimeRanges(TimeHelper.toRange(mergedStart, mergedEnd), state.occupiedTimeRanges);

				if (!freeRanges.isEmpty()) {
					RangeSet<LocalTime> actualWorkRanges = buildActualWorkRanges(sameDayLogs);
					RangeSet<LocalTime> workedFreeRanges = intersectRangeSets(freeRanges, actualWorkRanges);
					if (!workedFreeRanges.isEmpty()) {
						weeklyOvertimeCandidateTimeRanges.add(workedFreeRanges);
					}
				}
			}
		}
		else {
			RangeSet<LocalTime> timeRanges = resolveTimeRangesForRule(timeLog, currentRule, timesheet, state,
					unifiedRules, weeklyTimeLog);

			if (!timeRanges.isEmpty()) {
				RangeSet<LocalTime> constrainedRangeSet = TimeHelper.constrainTimeRangeToTimeLogBoundaries(timeRanges,
						timeLog);
				if (!constrainedRangeSet.isEmpty()) {
					weeklyOvertimeCandidateTimeRanges.add(constrainedRangeSet);
				}
			}
		}
	}

	/**
	 * Builds a RangeSet of actual worked time ranges from same-day time logs. Used to
	 * exclude gaps between intervals from weekly OT candidates.
	 */
	private RangeSet<LocalTime> buildActualWorkRanges(
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> sameDayLogs) {
		RangeSet<LocalTime> workRanges = TreeRangeSet.create();
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog tl : sameDayLogs) {
			LocalTime start = TimeHelper.getEffectiveStartTime(tl);
			LocalTime end = TimeHelper.getEffectiveEndTime(tl);
			if (start != null && end != null && start.isBefore(end)) {
				workRanges.add(TimeHelper.toRange(start, end));
			}
		}
		return workRanges;
	}

	/**
	 * Returns the intersection of two RangeSets — only ranges present in both sets.
	 */
	private RangeSet<LocalTime> intersectRangeSets(RangeSet<LocalTime> rangeSetA, RangeSet<LocalTime> rangeSetB) {
		RangeSet<LocalTime> result = TreeRangeSet.create();
		for (Range<LocalTime> rangeA : rangeSetA.asRanges()) {
			RangeSet<LocalTime> subView = rangeSetB.subRangeSet(rangeA);
			result.addAll(subView);
		}
		return result;
	}

	/**
	 * Gets the total number of custom rules to evaluate.
	 */
	protected int getTotalRuleCount(Timesheet timesheet) {
		return timesheet.getTimesheetSetting().getCustomRule().size();
	}

	/**
	 * Evaluates a specific rule for a time log.
	 */
	protected RangeSet<LocalTime> resolveTimeRangesForRule(
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			Timesheet timesheet, EvaluationState state, List<IEvaluatableRule> unifiedRules) {
		return resolveTimeRangesForRule(timeLog, currentRule, timesheet, state, unifiedRules, null);
	}

	/**
	 * Evaluates a specific rule for a time log with weekly time log context.
	 */
	protected RangeSet<LocalTime> resolveTimeRangesForRule(
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			Timesheet timesheet, EvaluationState state, List<IEvaluatableRule> unifiedRules,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog) {
		TimeRangeResolverContext context = createTimeRangeResolverContext(timeLog, currentRule, timesheet, state,
				unifiedRules, weeklyTimeLog);

		return this.ruleFactory.createTimeRangeResolver(currentRule.getRuleType()).resolveTimeRange(context);
	}

	/**
	 * Creates the time range resolver context.
	 */
	protected TimeRangeResolverContext createTimeRangeResolverContext(
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			Timesheet timesheet, EvaluationState state, List<IEvaluatableRule> unifiedRules) {
		return createTimeRangeResolverContext(timeLog, currentRule, timesheet, state, unifiedRules, null);
	}

	/**
	 * Creates the time range resolver context with weekly time log for same-day
	 * aggregation.
	 */
	protected TimeRangeResolverContext createTimeRangeResolverContext(
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			Timesheet timesheet, EvaluationState state, List<IEvaluatableRule> unifiedRules,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog) {
		TimeRangeResolverContext context = new TimeRangeResolverContext();
		context.setCurrentTimeLogBeingEvaluated(timeLog);
		context.setCurrentCustomRuleBeingEvaluated((currentRule instanceof CustomRule customRule) ? customRule : null);
		context.setInternalSortedCustomRules(
				unifiedRules.stream().filter(CustomRule.class::isInstance).map(CustomRule.class::cast).toList());
		context.setCurrentTimesheetSetting(
				RuleEngineTimesheetSettingMapper.INSTANCE.toTimesheetSetting(timesheet.getTimesheetSetting()));
		context.setOccupiedTimeRanges(state.occupiedTimeRanges);
		context.setWorkedHoursTillNow(state.workedHoursTillNow);

		// Calculate the correct custom rule index for daily overtime rules
		int customRuleIndex = calculateCustomRuleIndex(currentRule, unifiedRules);
		context.setCurrentRuleIndex(customRuleIndex);

		// Set same-day time logs for daily overtime aggregation
		context.setSameDayTimeLogs(TimeHelper.getSameDayTimeLogs(timeLog, weeklyTimeLog));

		return context;
	}

	/**
	 * Calculates the index of the current rule within the custom rules list. For system
	 * rules (like Regular Hours, Break), returns -1. For custom rules, returns their
	 * index in the custom rules list.
	 * @param currentRule the current rule being evaluated
	 * @param unifiedRules the unified rules list
	 * @return the custom rule index, or -1 for system rules
	 */
	private int calculateCustomRuleIndex(IEvaluatableRule currentRule, List<IEvaluatableRule> unifiedRules) {
		if (currentRule instanceof CustomRule customRule) {
			// Find the index of the current rule within the custom rules list
			List<CustomRule> customRules = unifiedRules.stream()
				.filter(CustomRule.class::isInstance)
				.map(CustomRule.class::cast)
				.toList();

			for (int i = 0; i < customRules.size(); i++) {
				if (customRules.get(i).equals(customRule)) {
					return i;
				}
			}
		}

		return -1; // Not found
	}

	/**
	 * Updates the evaluation state with new time ranges.
	 */
	protected void updateEvaluationState(EvaluationState state, RangeSet<LocalTime> evaluatedRangeSet) {
		state.occupiedTimeRanges.addAll(evaluatedRangeSet);
		state.workedHoursTillNow = state.workedHoursTillNow
			.plus(TimeHelper.convertRangeSetToDuration(evaluatedRangeSet));
	}

	/**
	 * Processes the rule evaluation and returns the result. The evaluatedRangeSet is
	 * expected to already be constrained to the correct boundaries by the caller.
	 */
	protected RuleEvaluationResult processRuleEvaluation(
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			RangeSet<LocalTime> evaluatedRangeSet, int ruleIndex,
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog,
			List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges, Timesheet timesheet) {

		// The evaluatedRangeSet is already constrained by the caller
		if (evaluatedRangeSet == null || evaluatedRangeSet.isEmpty()) {
			this.logger.logDebug(MessageFormat.format("Time range for rule evaluation is empty for timeLog ID: {0}",
					timeLog.getId()));
			return null;
		}

		RuleEvaluationContext context = createRuleEvaluationContext(timeLog, currentRule, evaluatedRangeSet, ruleIndex,
				timesheet);

		if (currentRule != null && isWeeklyOvertimeRule(currentRule, timesheet)) {
			weeklyOvertimeCandidateTimeRanges.add(evaluatedRangeSet);
			context.setWeeklyTimeLogs(weeklyTimeLog);
			context.setWeeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges);
		}

		// Determine rule type based on the current rule
		RuleType ruleType = currentRule.getRuleType();

		RuleEvaluationResult result = this.ruleFactory.createRule(ruleType, this.logger).evaluate(context);

		// Only set timeRange for non-weekly overtime rules, since weekly overtime rules
		// set it to null
		if (result != null && !isWeeklyOvertimeRule(currentRule, timesheet)) {
			result.setTimeRange(evaluatedRangeSet);
		}

		return result;
	}

	/**
	 * Creates the rule evaluation context.
	 */
	protected RuleEvaluationContext createRuleEvaluationContext(
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			RangeSet<LocalTime> evaluatedRangeSet, int ruleIndex, Timesheet timesheet) {
		RuleEvaluationContext context = new RuleEvaluationContext();
		context.setTimesheet(timesheet);
		context.setTimesheetSetting(timesheet.getTimesheetSetting());
		context.setTimesheetSettingDto(
				RuleEngineTimesheetSettingMapper.INSTANCE.toTimesheetSetting(timesheet.getTimesheetSetting()));
		context.setTimeRangesToEvaluate(evaluatedRangeSet);
		context.setCurrentTimeLogBeingEvaluated(timeLog);
		context.setCurrentRuleBeingEvaluated((currentRule instanceof CustomRule customRule) ? customRule : null);
		context.setCurrentRuleIndex(ruleIndex);
		return context;
	}

	/**
	 * Creates the rule evaluation context for weekly overtime rules.
	 */
	protected RuleEvaluationContext createWeeklyOvertimeRuleEvaluationContext(IEvaluatableRule currentRule,
			int ruleIndex, Timesheet timesheet) {
		RuleEvaluationContext context = new RuleEvaluationContext();
		context.setTimesheet(timesheet);
		context.setTimesheetSetting(timesheet.getTimesheetSetting());
		context.setTimesheetSettingDto(
				RuleEngineTimesheetSettingMapper.INSTANCE.toTimesheetSetting(timesheet.getTimesheetSetting()));
		context.setTimeRangesToEvaluate(null); // Weekly overtime doesn't use specific
												// time ranges
		context.setCurrentTimeLogBeingEvaluated(null); // Not evaluating a specific time
														// log
		context.setCurrentRuleBeingEvaluated((currentRule instanceof CustomRule customRule) ? customRule : null);
		context.setCurrentRuleIndex(ruleIndex);
		return context;
	}

	/**
	 * Checks if the rule is a weekly overtime rule.
	 * @param rule the rule to check
	 * @param timesheet the timesheet context to check frequency
	 * @return true if the rule is a weekly overtime rule and should be applied, false
	 * otherwise
	 */
	protected abstract boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet);

	/**
	 * Checks if the rule is a daily overtime rule.
	 *
	 * This method determines whether a rule is a daily overtime rule based on its rule
	 * type. Daily overtime rules should be evaluated after other rules but before regular
	 * hours to ensure proper precedence.
	 * @param rule the rule to check
	 * @return true if the rule is a daily overtime rule, false otherwise
	 */
	protected boolean isDailyOvertimeRule(IEvaluatableRule rule) {
		if (rule == null) {
			return false;
		}
		return rule.isDailyOvertimeRule();
	}

	/**
	 * Gets the rule type for regular hours rule.
	 *
	 * This method should return the appropriate RuleType for regular hours based on the
	 * evaluator type (range-based or duration-based).
	 * @return the RuleType for regular hours
	 */
	protected abstract RuleType getRegularHoursRuleType();

	/**
	 * Gets the rule type for break rule.
	 *
	 * This method should return the appropriate RuleType for break based on the evaluator
	 * type (range-based or duration-based).
	 * @return the RuleType for break
	 */
	protected abstract RuleType getBreakRuleType();

	/**
	 * Gets the rule type for the Default Pay sweep. Subclasses return the range-based or
	 * duration-based variant matching their evaluator type.
	 */
	protected abstract RuleType getDefaultPayRuleType();

	/**
	 * Checks if a rule is applicable on the given time log's date.
	 *
	 * This method validates rule applicability based on the rule type: - Regular Hours
	 * Rule: Uses TimesheetSetting.templateWorkDay to determine applicable days - Custom
	 * Rules: Uses CustomRule.workDays to determine applicable days
	 * @param rule the rule to check
	 * @param timeLog the time log to check applicability for
	 * @param timesheet the timesheet containing settings
	 * @return true if the rule is applicable on this day, false otherwise
	 */
	protected boolean isRuleApplicableOnDay(IEvaluatableRule rule,
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, Timesheet timesheet) {

		// Get the day of week for the time log's date
		io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay timeLogDay = TimeHelper
			.getWorkDayFromLocalDate(timeLog.getDate());

		// Use the unified interface method
		return rule.isApplicableOnDay(timeLogDay);
	}

	/**
	 * Checks if the regular hours rule is applicable on the given work day.
	 *
	 * Regular hours rule applicability is determined by TimesheetSetting.templateWorkDay,
	 * which defines which days are configured as work days.
	 * @param timeLogDay the work day to check
	 * @param timesheet the timesheet containing settings
	 * @return true if regular hours rule applies on this day, false otherwise
	 */
	protected boolean isRegularHoursRuleApplicableOnDay(
			io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay timeLogDay, Timesheet timesheet) {
		// Get template work days from timesheet settings
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay> templateWorkDays = RuleEngineTimesheetSettingMapper.INSTANCE
			.toTimesheetSetting(timesheet.getTimesheetSetting())
			.getTemplateWorkDays();

		// Check if the current day is configured as a work day in template
		for (io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay templateWorkDay : templateWorkDays) {
			if (templateWorkDay.getWorkDayType() == timeLogDay) {
				return true; // This day is configured as a work day
			}
		}

		return false; // This day is not configured as a work day
	}

	/**
	 * Checks if a custom rule is applicable on the given work day.
	 *
	 * Custom rule applicability is determined by CustomRule.workDays, which specifies
	 * which days the rule should be applied on.
	 * @param rule the custom rule to check
	 * @param timeLogDay the work day to check
	 * @return true if custom rule applies on this day, false otherwise
	 */
	protected boolean isCustomRuleApplicableOnDay(CustomRule rule,
			io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay timeLogDay) {
		// If workDays is null or empty, rule applies to all days
		if (rule.getWorkDays() == null || rule.getWorkDays().isEmpty()) {
			return true;
		}

		// Check if the rule's workDays contains this day
		return rule.getWorkDays().contains(timeLogDay);
	}

	/**
	 * Adds the rule result to the evaluator result.
	 */
	protected void addRuleResultToEvaluatorResult(RuleEvaluatorResult result,
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule currentRule,
			RuleEvaluationResult ruleResult, Timesheet timesheet) {
		// Weekly overtime rules are now evaluated once at the end of the week
		// so we only add non-weekly overtime results here
		if (!isWeeklyOvertimeRule(currentRule, timesheet)) {
			result.addRuleEvaluationResult(timeLog, ruleResult);
		}
	}

	/**
	 * Gets duration in seconds, returning 0 if duration is null.
	 */
	private long getDurationInSeconds(Duration duration) {
		return (duration != null) ? duration.toSeconds() : 0;
	}

	/**
	 * Post-processes the result after all evaluations are complete.
	 */
	protected void postProcessResult(RuleEvaluatorResult result) {
		populateMoneyData(result);
	}

	/**
	 * Checks if the given rule is a daily overtime rule type.
	 */
	private boolean isDailyOvertimeRuleType(IEvaluatableRule rule) {
		return rule.getRuleType() == RuleType.RANGE_BASED_DAILY_OVERTIME
				|| rule.getRuleType() == RuleType.DURATION_BASED_DAILY_OVERTIME;
	}

	/**
	 * State class to track evaluation progress.
	 */
	protected static class EvaluationState {

		RangeSet<LocalTime> occupiedTimeRanges = TreeRangeSet.create();

		Duration workedHoursTillNow = Duration.ZERO;

	}

}