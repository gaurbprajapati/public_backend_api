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
 * Rule for calculating before shift time
 */
public class BeforeShiftRule extends BaseRule {

	private Float basePayRatePerHour;

	private Float baseBillRatePerHour;

	private Float rulePayRateMultiplier;

	private Float ruleBillRateMultiplier;

	private ChargeMethodType ruleChargeMethod;

	private Float rulePayRatePerHour;

	private Float ruleBillRatePerHour;

	public BeforeShiftRule(Logger logger) {
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
		Duration beforeShiftDuration = TimeHelper
			.convertRangeSetToDuration(ruleEvaluationContext.getTimeRangesToEvaluate());

		// Calculate amounts based on charge method type
		BigDecimal payableAmount = TimeHelper.calculatePayAmount(beforeShiftDuration, this.ruleChargeMethod,
				this.basePayRatePerHour, // base pay rate from timesheet settings
				this.rulePayRateMultiplier, this.rulePayRatePerHour);

		BigDecimal billableAmount = TimeHelper.calculateBillAmount(beforeShiftDuration, this.ruleChargeMethod,
				this.baseBillRatePerHour, // base bill rate from timesheet settings
				this.ruleBillRateMultiplier, this.ruleBillRatePerHour);

		// Use BaseRule utility method to create complete result
		RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext,
				ruleEvaluationContext.getTimeRangesToEvaluate(), payableAmount, billableAmount);

		logEvaluationComplete(result);
		return result;
	}

	@Override
	public String getName() {
		return "Range-Based Before Shift Rule";
	}

	@Override
	protected RuleType getDefaultRuleType() {
		return RuleType.RANGE_BASED_BEFORE_SHIFT;
	}

}
