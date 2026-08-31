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
import io.recruitcrm.microservice.timesheet.rule_engine.rules.BaseRule;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Duration-based Default Pay rule. Pays any worked time that remained unallocated after
 * all other rules (including Weekly Overtime) ran, at the base pay/bill rate × 1.0.
 *
 * Invoked at week end by {@code BaseRuleEvaluator.evaluateDefaultPayRules} with the
 * pre-computed range set in {@code context.timeRangesToEvaluate}. Gated on
 * {@code TimesheetSetting.isUnplannedHoursPayEnabled}.
 */
public class DefaultPayRule extends BaseRule {

	public DefaultPayRule(Logger logger) {
		super(logger);
	}

	@Override
	public RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext) {
		validateContext(ruleEvaluationContext);
		logEvaluationStart(ruleEvaluationContext);

		RangeSet<LocalTime> timeRange = ruleEvaluationContext.getTimeRangesToEvaluate();
		Duration duration = TimeHelper.convertRangeSetToDuration(timeRange);

		float basePayRate = ruleEvaluationContext.getTimesheetSetting().getPayRate();
		float baseBillRate = ruleEvaluationContext.getTimesheetSetting().getBillRate();

		BigDecimal payableAmount = TimeHelper.calculatePayAmount(duration, ChargeMethodType.MULTIPLIER, basePayRate,
				1.0f, basePayRate);
		BigDecimal billableAmount = TimeHelper.calculateBillAmount(duration, ChargeMethodType.MULTIPLIER, baseBillRate,
				1.0f, baseBillRate);

		RuleEvaluationResult result = createCompleteResult(ruleEvaluationContext, timeRange, payableAmount,
				billableAmount);
		logEvaluationComplete(result);
		return result;
	}

	@Override
	public String getName() {
		return "Duration-Based Default Pay Rule";
	}

	@Override
	protected RuleType getDefaultRuleType() {
		return RuleType.DURATION_BASED_DEFAULT_PAY;
	}

}
