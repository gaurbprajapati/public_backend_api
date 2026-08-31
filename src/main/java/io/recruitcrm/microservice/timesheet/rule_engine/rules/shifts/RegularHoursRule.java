/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.BaseRule;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Rule for calculating regular hours based on shifts
 */
public class RegularHoursRule extends BaseRule {

	private Float basePayRatePerHour;

	private Float baseBillRatePerHour;

	private Float rulePayRateMultiplier;

	private Float ruleBillRateMultiplier;

	private ChargeMethodType ruleChargeMethod;

	private Float rulePayRatePerHour;

	private Float ruleBillRatePerHour;

	public RegularHoursRule(Logger logger) {
		super(logger);
	}

	@Override
	protected void initRatesAndMethods(RuleEvaluationContext ruleEvaluationContext) {
		this.basePayRatePerHour = ruleEvaluationContext.getTimesheetSetting().getPayRate();
		this.baseBillRatePerHour = ruleEvaluationContext.getTimesheetSetting().getBillRate();
		this.rulePayRateMultiplier = 1.0f;
		this.ruleBillRateMultiplier = 1.0f;
		this.ruleChargeMethod = ChargeMethodType.MULTIPLIER;
		this.rulePayRatePerHour = ruleEvaluationContext.getTimesheetSetting().getPayRate();
		this.ruleBillRatePerHour = ruleEvaluationContext.getTimesheetSetting().getBillRate();
	}

	@Override
	public RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext) {
		// Use BaseRule validation and logging
		validateContext(ruleEvaluationContext);
		logEvaluationStart(ruleEvaluationContext);

		this.initRatesAndMethods(ruleEvaluationContext);

		// Handle day off case
		if (ruleEvaluationContext.getCurrentTimeLogBeingEvaluated().getDayType() == WorkDayType.DAY_OFF) {
			RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext,
					ruleEvaluationContext.getTimeRangesToEvaluate(), BigDecimal.ZERO, BigDecimal.ZERO);
			logEvaluationComplete(result);
			return result;
		}

		Duration regularHoursDuration = TimeHelper
			.convertRangeSetToDuration(ruleEvaluationContext.getTimeRangesToEvaluate());

		// Calculate amounts using helper methods
		BigDecimal payableAmount = TimeHelper.calculatePayAmount(regularHoursDuration, this.ruleChargeMethod,
				this.basePayRatePerHour, this.rulePayRateMultiplier, this.rulePayRatePerHour);

		BigDecimal billableAmount = TimeHelper.calculateBillAmount(regularHoursDuration, this.ruleChargeMethod,
				this.baseBillRatePerHour, this.ruleBillRateMultiplier, this.ruleBillRatePerHour);

		// Use BaseRule utility method to create complete result
		RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext,
				ruleEvaluationContext.getTimeRangesToEvaluate(), payableAmount, billableAmount);

		logEvaluationComplete(result);
		return result;
	}

	@Override
	public String getName() {
		return "Range-Based Regular Hours Rule";
	}

	@Override
	protected RuleType getDefaultRuleType() {
		return RuleType.RANGE_BASED_REGULAR_HOURS;
	}

}
