/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Main implementation of the rule engine that orchestrates rule evaluation
 */
@Service
public class RuleEngine implements IRuleEngine {

	private final RuleEvaluator ruleEvaluator;

	@Autowired
	public RuleEngine(RuleEvaluator ruleEvaluator) {
		this.ruleEvaluator = ruleEvaluator;
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
		if (timesheet == null) {
			throw new IllegalArgumentException("Timesheet cannot be null");
		}

		if (timesheet.getTimesheetSetting() == null) {
			throw new IllegalArgumentException("Timesheet setting cannot be null");
		}

		return this.ruleEvaluator.evaluateRules(timesheet);
	}

	@Override
	public WeeklyRuleEvaluatorResult evaluateRulesOnDemand(Timesheet timesheet, List<TimeLog> timeLogs) {
		if (timesheet == null) {
			throw new IllegalArgumentException("Timesheet cannot be null");
		}
		if (timesheet.getTimesheetSetting() == null) {
			throw new IllegalArgumentException("Timesheet setting cannot be null");
		}
		if (timeLogs == null || timeLogs.isEmpty()) {
			throw new IllegalArgumentException("Time logs cannot be null or empty");
		}
		return this.ruleEvaluator.evaluateRulesWithTimeLogs(timesheet, timeLogs);
	}

	@Override
	public void validateRules() {
		throw new UnsupportedOperationException("Rule validation not implemented yet");
	}

}