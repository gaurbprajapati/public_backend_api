/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimesheetSettingMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimeLogMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.List;

/**
 * Duration-based rule evaluator for timesheets with total hours logging.
 *
 * This evaluator handles timesheets where time logs contain total hours worked, allowing
 * for duration-based calculations and rule evaluation.
 */
@Component
public class DurationBasedRuleEvaluator extends BaseRuleEvaluator {

	@Autowired
	public DurationBasedRuleEvaluator(IRuleFactory ruleFactory,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		super(ruleFactory, logger);
	}

	/**
	 * Override to normalize duration-based time logs.
	 *
	 * For duration-based time logs, we convert the workTime duration into workStartTime
	 * and workEndTime using midnight (00:00) as the anchor point. This allows the rule
	 * engine to work with time ranges while preserving the original duration information.
	 */
	@Override
	protected List<List<TimeLog>> prepareWeeklyTimeLogs(
			List<io.recruitcrm.contract_staffing.entity.model.TimeLog> timeLogs) {
		return TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs.stream().filter((timeLog) -> {
			// For duration-based time logs, we need workTime to be present
			if (timeLog.getWorkTime() == null) {
				this.logger.logWarn(
						MessageFormat.format("Skipping timelog with ID {0} due to null work time", timeLog.getId()));
				return false;
			}
			return true;
		}).map(this::normalizeDurationBasedTimeLog).toList());
	}

	/**
	 * Override to normalize duration-based time logs with custom week start day.
	 */
	@Override
	protected List<List<TimeLog>> prepareWeeklyTimeLogs(
			List<io.recruitcrm.contract_staffing.entity.model.TimeLog> timeLogs, WorkDay weekStartDay) {
		return TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs.stream().filter((timeLog) -> {
			// For duration-based time logs, we need workTime to be present
			if (timeLog.getWorkTime() == null) {
				this.logger.logWarn(
						MessageFormat.format("Skipping timelog with ID {0} due to null work time", timeLog.getId()));
				return false;
			}
			return true;
		}).map(this::normalizeDurationBasedTimeLog).toList(), weekStartDay);
	}

	/**
	 * Normalizes a duration-based time log by converting workTime to normalized work
	 * start and end times. Uses the existing TimeHelper methods to normalize durations
	 * using midnight (00:00) as the anchor point. The original workStartTime and
	 * workEndTime fields remain unchanged, while normalized values are stored in the new
	 * normalizedWorkStartTime and normalizedWorkEndTime fields.
	 * @param timeLog the original time log with duration
	 * @return normalized time log with normalized start and end times
	 */
	private TimeLog normalizeDurationBasedTimeLog(io.recruitcrm.contract_staffing.entity.model.TimeLog timeLog) {
		TimeLog normalizedTimeLog = RuleEngineTimeLogMapper.INSTANCE.toTimeLog(timeLog);

		// If workStartTime and workEndTime are already set, use them as normalized values
		if (timeLog.getWorkStartTime() != null && timeLog.getWorkEndTime() != null) {
			normalizedTimeLog.setNormalizedWorkStartTime(normalizedTimeLog.getWorkStartTime());
			normalizedTimeLog.setNormalizedWorkEndTime(normalizedTimeLog.getWorkEndTime());
			return normalizedTimeLog;
		}

		// For duration-based time logs, normalize using existing TimeHelper methods
		if (normalizedTimeLog.getWorkTime() != null) {
			// Use TimeHelper to normalize duration to LocalTime using midnight as anchor
			LocalTime startTime = LocalTime.MIDNIGHT; // 00:00
			LocalTime endTime = TimeHelper.normaliseDurationToLocalTime(normalizedTimeLog.getWorkTime());

			// Set the normalized fields instead of the original fields
			normalizedTimeLog.setNormalizedWorkStartTime(startTime);
			normalizedTimeLog.setNormalizedWorkEndTime(endTime);

			this.logger.logDebug(MessageFormat.format(
					"Normalized duration-based timelog ID {0}: {1} duration -> normalized {2} to {3}", timeLog.getId(),
					normalizedTimeLog.getWorkTime(), startTime, endTime));
		}

		return normalizedTimeLog;
	}

	@Override
	protected boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet) {
		// Check if the rule is a weekly overtime rule type
		boolean isWeeklyOvertimeRuleType = rule.getRuleType() == RuleType.DURATION_BASED_WEEKLY_OVERTIME;

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
		return RuleType.DURATION_BASED_REGULAR_HOURS;
	}

	@Override
	protected RuleType getBreakRuleType() {
		return RuleType.DURATION_BASED_BREAK;
	}

	@Override
	protected RuleType getDefaultPayRuleType() {
		return RuleType.DURATION_BASED_DEFAULT_PAY;
	}

}
