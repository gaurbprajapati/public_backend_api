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
 * Strategy interface for different rule evaluation approaches.
 *
 * This interface implements the Strategy pattern to provide different evaluation
 * strategies for timesheet rule processing. The strategy pattern allows the rule engine
 * to dynamically select the most appropriate evaluation approach based on the work log
 * type and timesheet configuration.
 *
 * The system supports two main evaluation strategies: 1. Range-based evaluation: For
 * timesheets with start/end time logging 2. Duration-based evaluation: For timesheets
 * with total hours logging
 *
 * Each strategy implements its own evaluation logic while maintaining a consistent
 * interface. This allows for: - Flexible evaluation approaches based on data format -
 * Easy addition of new evaluation strategies - Consistent result format regardless of
 * strategy used - Optimized performance for different data structures
 *
 * The strategy selection is automatic based on the work log type, ensuring that the most
 * appropriate evaluation method is used for each timesheet configuration.
 *
 * Key responsibilities: - Implement specific evaluation logic for different work log
 * types - Handle the complete evaluation workflow for a timesheet - Ensure consistent
 * result format across all strategies - Provide strategy-specific optimizations and
 * validations
 */
public interface RuleEvaluationStrategy {

	/**
	 * Evaluates rules for a given timesheet using this strategy.
	 *
	 * This method implements the core evaluation logic for a specific strategy. The
	 * evaluation process typically involves:
	 *
	 * 1. Validating the timesheet data and configuration 2. Processing time logs
	 * according to the strategy's approach 3. Applying configured rules in the correct
	 * order 4. Resolving time ranges for each rule evaluation 5. Calculating monetary
	 * amounts (pay and bill) 6. Aggregating results from all rule evaluations 7. Handling
	 * weekly overtime calculations if applicable
	 *
	 * The strategy determines how time logs are processed: - Range-based strategies work
	 * with start/end times and time ranges - Duration-based strategies work with total
	 * hours and duration calculations
	 *
	 * Each strategy may have different optimizations and handling for edge cases specific
	 * to its evaluation approach.
	 * @param timesheet the timesheet containing time logs and rule configuration
	 * @return WeeklyRuleEvaluatorResult containing weekly evaluation results and monetary
	 * data
	 * @throws IllegalArgumentException if timesheet data is invalid for this strategy
	 * @throws IllegalStateException if rule configuration is incompatible with this
	 * strategy
	 */
	WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet);

	/**
	 * Checks if this strategy can handle the given work log type.
	 *
	 * This method determines whether this strategy is appropriate for processing
	 * timesheets with the specified work log type. The work log type indicates how time
	 * data is structured and logged:
	 *
	 * - START_AND_END_TIME: Time logs contain start and end times (suitable for
	 * range-based strategies) - WORK_HOUR: Time logs contain total hours worked (suitable
	 * for duration-based strategies)
	 *
	 * The rule engine uses this method to automatically select the appropriate strategy
	 * for each timesheet, ensuring optimal processing based on the data format.
	 * @param workLogType the work log type identifier to check compatibility
	 * @return true if this strategy can effectively process the work log type, false if
	 * the strategy is not suitable for this data format
	 */
	boolean canHandle(Integer workLogType);

	/**
	 * Evaluates rules using pre-built time log DTOs (for on-demand evaluation). This
	 * bypasses DB-based time log fetching and uses provided time logs directly.
	 * @param timesheet the timesheet containing rule configuration and settings
	 * @param timeLogs the pre-built time log DTOs to evaluate
	 * @return WeeklyRuleEvaluatorResult containing weekly evaluation results
	 */
	WeeklyRuleEvaluatorResult evaluateRulesWithTimeLogs(Timesheet timesheet, List<TimeLog> timeLogs);

}