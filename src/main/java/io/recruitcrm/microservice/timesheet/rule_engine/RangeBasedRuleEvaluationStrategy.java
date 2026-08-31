/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RangeBasedRuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy implementation for range-based rule evaluation.
 */
@Component
public class RangeBasedRuleEvaluationStrategy implements RuleEvaluationStrategy {

	private final RangeBasedRuleEvaluator rangeBasedRuleEvaluator;

	@Autowired
	public RangeBasedRuleEvaluationStrategy(RangeBasedRuleEvaluator rangeBasedRuleEvaluator) {
		this.rangeBasedRuleEvaluator = rangeBasedRuleEvaluator;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
		return this.rangeBasedRuleEvaluator.evaluateRules(timesheet);
	}

	@Override
	public boolean canHandle(Integer workLogType) {
		return WorkLogType.START_AND_END_TIME.getTypeId() == workLogType;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRulesWithTimeLogs(Timesheet timesheet, List<TimeLog> timeLogs) {
		return this.rangeBasedRuleEvaluator.evaluatePreBuiltTimeLogs(timesheet, timeLogs);
	}

}