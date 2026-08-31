/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.microservice.timesheet.rule_engine.rules.DurationBasedRuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RangeBasedRuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for rule engine components.
 *
 * This configuration class defines the Spring beans and dependency injection setup for
 * the rule engine system. It ensures that all components are properly instantiated and
 * wired together according to the system's architectural design.
 *
 * The configuration class provides several key benefits: - Centralized bean definition
 * and dependency management - Clear separation of configuration from business logic -
 * Explicit control over bean creation and wiring - Support for different evaluation
 * strategies - Easy extensibility for new components
 *
 * Key components configured: - RuleEvaluator: The main orchestrator that selects and
 * applies evaluation strategies - DurationBasedRuleEvaluationStrategy: Strategy for
 * timesheets with total hours logging - RangeBasedRuleEvaluationStrategy: Strategy for
 * timesheets with start/end time logging
 *
 * The configuration ensures that: - All strategies are properly registered and available
 * - Dependencies are correctly injected - Components are instantiated in the correct
 * order - The system is ready for rule evaluation operations
 *
 * Usage: This configuration is automatically loaded by Spring's component scanning and
 * ensures that all rule engine components are properly configured and available for
 * dependency injection throughout the application.
 */
@Configuration
public class RuleEngineConfiguration {

	/**
	 * Creates the main rule evaluator with all available strategies.
	 *
	 * This bean creates the central RuleEvaluator component that orchestrates the rule
	 * evaluation process. The evaluator receives all available evaluation strategies and
	 * automatically selects the appropriate one based on the timesheet's work log type.
	 *
	 * The RuleEvaluator performs several key functions: - Strategy selection based on
	 * work log type - Delegation to the appropriate evaluation strategy - Result
	 * aggregation and formatting - Error handling and validation
	 *
	 * The evaluator supports both range-based and duration-based evaluation strategies,
	 * ensuring that the most appropriate evaluation method is used for each timesheet
	 * configuration.
	 * @param evaluationStrategies list of all available rule evaluation strategies
	 * @return the configured RuleEvaluator ready for rule evaluation operations
	 */
	@Bean
	public RuleEvaluator ruleEvaluator(List<RuleEvaluationStrategy> evaluationStrategies) {
		/*
		 * Create the main rule evaluator with all available strategies. The evaluator
		 * will automatically select the appropriate strategy based on the timesheet's
		 * work log type during evaluation.
		 */
		return new RuleEvaluator(evaluationStrategies);
	}

	/**
	 * Creates the duration-based rule evaluation strategy.
	 *
	 * This bean creates the DurationBasedRuleEvaluationStrategy that handles timesheets
	 * where time logs contain total hours worked rather than specific start and end
	 * times. This strategy is suitable for simplified rule evaluation based on total
	 * duration.
	 *
	 * The duration-based strategy: - Works with total hours/duration data - Provides
	 * simplified calculation approach - Handles weekly and daily hour limit rules -
	 * Supports efficient bulk hour calculations
	 *
	 * This strategy is automatically selected by the RuleEvaluator when the work log type
	 * is WORK_HOUR, ensuring optimal processing for timesheets with duration-based
	 * logging.
	 * @param durationBasedRuleEvaluator the specialized evaluator for duration-based
	 * processing
	 * @return the configured DurationBasedRuleEvaluationStrategy ready for use
	 */
	@Bean
	public DurationBasedRuleEvaluationStrategy durationBasedRuleEvaluationStrategy(
			DurationBasedRuleEvaluator durationBasedRuleEvaluator) {
		/*
		 * Create the duration-based strategy with its specialized evaluator. This
		 * strategy handles timesheets with total hours logging and provides simplified
		 * rule evaluation based on duration.
		 */
		return new DurationBasedRuleEvaluationStrategy(durationBasedRuleEvaluator);
	}

	/**
	 * Creates the range-based rule evaluation strategy.
	 *
	 * This bean creates the RangeBasedRuleEvaluationStrategy that handles timesheets
	 * where time logs contain start and end times, enabling precise time range
	 * calculations and complex rule evaluation scenarios.
	 *
	 * The range-based strategy: - Works with precise start/end time data - Enables
	 * complex time range calculations and overlaps - Supports shift-based rules
	 * (before/after shift) - Allows detailed overtime calculations based on time ranges -
	 * Provides granular control over rule application
	 *
	 * This strategy is automatically selected by the RuleEvaluator when the work log type
	 * is START_AND_END_TIME, ensuring optimal processing for timesheets with detailed
	 * time logging.
	 * @param rangeBasedRuleEvaluator the specialized evaluator for range-based processing
	 * @return the configured RangeBasedRuleEvaluationStrategy ready for use
	 */
	@Bean
	public RangeBasedRuleEvaluationStrategy rangeBasedRuleEvaluationStrategy(
			RangeBasedRuleEvaluator rangeBasedRuleEvaluator) {
		/*
		 * Create the range-based strategy with its specialized evaluator. This strategy
		 * handles timesheets with start/end time logging and provides sophisticated rule
		 * evaluation based on time ranges.
		 */
		return new RangeBasedRuleEvaluationStrategy(rangeBasedRuleEvaluator);
	}

}