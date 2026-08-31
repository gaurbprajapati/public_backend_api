/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;

import java.util.List;

/**
 * Main interface for the rule engine that orchestrates rule evaluation.
 */
public interface IRuleEngine {

	/**
	 * Evaluates all applicable rules for a given timesheet.
	 * @param timesheet the timesheet containing time logs and rule configuration
	 * @return WeeklyRuleEvaluatorResult containing weekly evaluation results and monetary
	 * data
	 * @throws IllegalArgumentException if timesheet is null or invalid
	 * @throws IllegalStateException if rule configuration is invalid
	 */
	WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet);

	/**
	 * Evaluates rules on-demand using pre-built time log DTOs. This bypasses DB-based
	 * time log fetching and uses provided time logs directly.
	 * @param timesheet the timesheet containing rule configuration and settings
	 * @param timeLogs the pre-built time log DTOs to evaluate
	 * @return WeeklyRuleEvaluatorResult containing weekly evaluation results
	 */
	WeeklyRuleEvaluatorResult evaluateRulesOnDemand(Timesheet timesheet, List<TimeLog> timeLogs);

	/**
	 * Validates that all rules are properly configured and the system is ready.
	 * @throws IllegalArgumentException if any rule configuration is invalid
	 * @throws IllegalStateException if the rule engine is not properly initialized
	 */
	void validateRules();

}
