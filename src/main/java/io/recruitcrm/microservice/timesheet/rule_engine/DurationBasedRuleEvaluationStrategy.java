/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.DurationBasedRuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy implementation for duration-based rule evaluation.
 */
@Component
public class DurationBasedRuleEvaluationStrategy implements RuleEvaluationStrategy {

	private final DurationBasedRuleEvaluator durationBasedRuleEvaluator;

	@Autowired
	public DurationBasedRuleEvaluationStrategy(DurationBasedRuleEvaluator durationBasedRuleEvaluator) {
		this.durationBasedRuleEvaluator = durationBasedRuleEvaluator;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
		return this.durationBasedRuleEvaluator.evaluateRules(timesheet);
	}

	@Override
	public boolean canHandle(Integer workLogType) {
		return WorkLogType.WORK_HOUR.getTypeId() == workLogType;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRulesWithTimeLogs(Timesheet timesheet, List<TimeLog> timeLogs) {
		return this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(timesheet, timeLogs);
	}

}