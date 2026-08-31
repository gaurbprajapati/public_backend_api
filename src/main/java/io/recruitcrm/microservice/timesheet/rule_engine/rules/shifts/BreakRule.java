/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.BaseRule;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Rule for calculating break time based on shifts
 */
public class BreakRule extends BaseRule {

	private Float basePayRatePerHour;

	private Float baseBillRatePerHour;

	private Float rulePayRateMultiplier;

	private Float ruleBillRateMultiplier;

	private ChargeMethodType ruleChargeMethod;

	private Float rulePayRatePerHour;

	private Float ruleBillRatePerHour;

	public BreakRule(Logger logger) {
		super(logger);
	}

	@Override
	protected void initRatesAndMethods(RuleEvaluationContext ctx) {
		this.basePayRatePerHour = ctx.getTimesheetSetting().getPayRate();
		this.baseBillRatePerHour = ctx.getTimesheetSetting().getBillRate();
		// Break rules always use 1.0f multiplier (no custom rule)
		this.rulePayRateMultiplier = 1.0f;
		this.ruleBillRateMultiplier = 1.0f;
		this.ruleChargeMethod = ChargeMethodType.MULTIPLIER;
		this.rulePayRatePerHour = ctx.getTimesheetSetting().getPayRate();
		this.ruleBillRatePerHour = ctx.getTimesheetSetting().getBillRate();
	}

	@Override
	public RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext) {
		// Use BaseRule validation and logging
		validateContext(ruleEvaluationContext);
		logEvaluationStart(ruleEvaluationContext);

		this.initRatesAndMethods(ruleEvaluationContext);

		// Get break time from the time ranges to evaluate
		Duration breakDuration = TimeHelper.convertRangeSetToDuration(ruleEvaluationContext.getTimeRangesToEvaluate());

		// If no break time (empty ranges), return zero amounts
		if (breakDuration.isZero()) {
			RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext,
					ruleEvaluationContext.getTimeRangesToEvaluate(), BigDecimal.ZERO, BigDecimal.ZERO);
			logEvaluationComplete(result);
			return result;
		}

		// Check if break time calculation is enabled
		boolean calculateBreakTime = Boolean.TRUE
			.equals(ruleEvaluationContext.getTimesheetSettingDto().getCalculateBreakTime());

		if (!calculateBreakTime) {
			// When calculateBreakTime is FALSE, return zero amounts but keep the time
			// ranges
			RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext,
					ruleEvaluationContext.getTimeRangesToEvaluate(), BigDecimal.ZERO, BigDecimal.ZERO);
			logEvaluationComplete(result);
			return result;
		}

		// Calculate amounts for break time when calculateBreakTime is TRUE
		// Break time uses the same multipliers and rates as regular hours
		BigDecimal payableAmount = TimeHelper.calculatePayAmount(breakDuration, this.ruleChargeMethod,
				this.basePayRatePerHour, this.rulePayRateMultiplier, this.rulePayRatePerHour);

		BigDecimal billableAmount = TimeHelper.calculateBillAmount(breakDuration, this.ruleChargeMethod,
				this.baseBillRatePerHour, this.ruleBillRateMultiplier, this.ruleBillRatePerHour);

		// Use BaseRule utility method to create complete result
		RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext,
				ruleEvaluationContext.getTimeRangesToEvaluate(), payableAmount, billableAmount);

		logEvaluationComplete(result);
		return result;
	}

	@Override
	public String getName() {
		return "Range-Based Break Rule";
	}

	@Override
	protected RuleType getDefaultRuleType() {
		return RuleType.RANGE_BASED_BREAK;
	}

}