/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

import com.google.common.collect.RangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.BaseRule;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Rule for calculating weekly overtime hours
 */
public class WeeklyOvertimeRule extends BaseRule {

	private Float basePayRatePerHour;

	private Float baseBillRatePerHour;

	private Float rulePayRateMultiplier;

	private Float ruleBillRateMultiplier;

	private ChargeMethodType ruleChargeMethod;

	private Float rulePayRatePerHour;

	private Float ruleBillRatePerHour;

	public WeeklyOvertimeRule(Logger logger) {
		super(logger);
	}

	@Override
	protected void initRatesAndMethods(RuleEvaluationContext ctx) {
		if (ctx.getCurrentRuleBeingEvaluated() == null) {
			throw new IllegalArgumentException("Current rule being evaluated cannot be null");
		}
		this.basePayRatePerHour = ctx.getTimesheetSetting().getPayRate();
		this.baseBillRatePerHour = ctx.getTimesheetSetting().getBillRate();
		this.rulePayRateMultiplier = ctx.getCurrentRuleBeingEvaluated().getPayRateMultiplier();
		this.ruleBillRateMultiplier = ctx.getCurrentRuleBeingEvaluated().getBillRateMultiplier();
		this.ruleChargeMethod = ctx.getCurrentRuleBeingEvaluated().getChargeMethod();
		this.rulePayRatePerHour = ctx.getCurrentRuleBeingEvaluated().getPayRatePerHour();
		this.ruleBillRatePerHour = ctx.getCurrentRuleBeingEvaluated().getBillRatePerHour();
	}

	@Override
	public RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext) {
		// Use BaseRule validation and logging
		validateContext(ruleEvaluationContext);
		logEvaluationStart(ruleEvaluationContext);

		this.initRatesAndMethods(ruleEvaluationContext);

		Duration totalHoursWorked = getTotalHoursWorkedFromTimeLogList(ruleEvaluationContext.getWeeklyTimeLogs(),
				ruleEvaluationContext);
		Duration weeklyOvertimeThreshold = ruleEvaluationContext.getCurrentRuleBeingEvaluated().getWeeklyThreshold();

		Duration grossWeeklyOvertimeDuration = Duration.ZERO;
		if (weeklyOvertimeThreshold != null && totalHoursWorked.compareTo(weeklyOvertimeThreshold) > 0) {
			grossWeeklyOvertimeDuration = totalHoursWorked.minus(weeklyOvertimeThreshold);
		}

		Duration totalDurationOfCandidateWeeklyOvertimeTimeRanges = getTotalHoursWorkedFromRangeSetList(
				ruleEvaluationContext.getWeeklyOvertimeCandidateTimeRanges());

		Duration netWeeklyOvertimeDuration = TimeHelper.getMinimumDuration(grossWeeklyOvertimeDuration,
				totalDurationOfCandidateWeeklyOvertimeTimeRanges);

		// Calculate amounts based on charge method type
		BigDecimal payableAmount = TimeHelper.calculatePayAmount(netWeeklyOvertimeDuration, this.ruleChargeMethod,
				this.basePayRatePerHour, // base pay rate from
											// timesheet settings
				this.rulePayRateMultiplier, this.rulePayRatePerHour);

		BigDecimal billableAmount = TimeHelper.calculateBillAmount(netWeeklyOvertimeDuration, this.ruleChargeMethod,
				this.baseBillRatePerHour, // base bill rate from
											// timesheet settings
				this.ruleBillRateMultiplier, this.ruleBillRatePerHour);

		// Use BaseRule utility method to create complete result with weekly overtime
		// hours
		// Set timeRange to null for weekly overtime since it's calculated across the
		// entire week
		RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext, null, netWeeklyOvertimeDuration,
				payableAmount, billableAmount);

		logEvaluationComplete(result);
		return result;
	}

	@Override
	public String getName() {
		return "Duration-Based Weekly Overtime Rule";
	}

	@Override
	protected RuleType getDefaultRuleType() {
		return RuleType.DURATION_BASED_WEEKLY_OVERTIME;
	}

	private Duration getTotalHoursWorkedFromTimeLogList(List<TimeLog> timeLogs, RuleEvaluationContext context) {
		if (timeLogs == null || timeLogs.isEmpty()) {
			this.logger.logDebug("TimeLogs list is null or empty, returning zero duration");
			return Duration.ZERO;
		}

		return timeLogs.stream()
			.map((timeLog) -> TimeHelper.calculateWorkTimeRespectingBreakFlag(timeLog,
					context.getTimesheetSettingDto()))
			.reduce(Duration.ZERO, Duration::plus);
	}

	private Duration getTotalHoursWorkedFromRangeSetList(List<RangeSet<LocalTime>> rangeSets) {
		if (rangeSets == null || rangeSets.isEmpty()) {
			this.logger.logDebug("RangeSets list is null or empty, returning zero duration");
			return Duration.ZERO;
		}

		return rangeSets.stream().map(TimeHelper::convertRangeSetToDuration).reduce(Duration.ZERO, Duration::plus);
	}

}
