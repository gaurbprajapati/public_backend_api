/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule;

/**
 * Factory interface for creating rule instances and time range resolvers.
 *
 * This interface implements the Factory pattern to provide a centralized mechanism for
 * creating rule instances and their associated time range resolvers. The factory pattern
 * is used here to:
 *
 * 1. Encapsulate the creation logic for different rule types 2. Provide a unified
 * interface for rule instantiation 3. Enable dynamic rule creation based on configuration
 * 4. Support the extensibility of the rule system 5. Ensure proper initialization of rule
 * dependencies
 *
 * The factory supports two types of objects: - IRule: The actual rule implementations
 * that perform business logic - ICustomRuleTimeRangeResolver: Components that determine
 * which time ranges should be evaluated by specific rules
 *
 * This separation of concerns allows for flexible rule evaluation where time range
 * resolution and rule evaluation can be independently configured and optimized.
 *
 * Usage: The factory is typically used by evaluation strategies to create rule instances
 * dynamically based on the timesheet configuration. Each rule type has its own
 * implementation and corresponding time range resolver.
 */
public interface IRuleFactory {

	/**
	 * Creates a rule instance for the given rule type.
	 *
	 * This method instantiates the appropriate rule implementation based on the provided
	 * rule type. The factory maintains a mapping between rule types and their concrete
	 * implementations, ensuring that the correct rule class is instantiated for each rule
	 * type.
	 *
	 * The created rule instance will be ready to evaluate timesheet data according to its
	 * specific business logic. Rules can be either range-based (operating on specific
	 * time ranges) or duration-based (operating on total hours worked).
	 * @param ruleType the type of rule to create (e.g., DAILY_OVERTIME, WEEKLY_OVERTIME)
	 * @param logger the logger instance to be used by the rule
	 * @return the rule instance ready for evaluation
	 * @throws IllegalArgumentException if the rule type is not supported or invalid
	 * @throws IllegalStateException if the rule cannot be instantiated due to
	 * configuration issues
	 */
	IRule createRule(RuleType ruleType, Logger logger);

	/**
	 * Creates a time range resolver for the given rule type.
	 *
	 * This method instantiates the appropriate time range resolver for the specified rule
	 * type. Time range resolvers are responsible for determining which specific time
	 * ranges within a time log should be evaluated by a particular rule.
	 *
	 * The resolver works in conjunction with the rule to: 1. Analyze the time log data 2.
	 * Identify relevant time ranges based on rule criteria 3. Return a set of time ranges
	 * that should be evaluated 4. Handle edge cases and overlapping time periods
	 *
	 * Different rule types may require different resolution strategies. For example,
	 * overtime rules might need to resolve time ranges beyond regular hours, while
	 * shift-based rules might focus on specific time periods around shift boundaries.
	 * @param ruleType the type of rule to create resolver for
	 * @return the time range resolver instance ready for use
	 * @throws IllegalArgumentException if the rule type is not supported or invalid
	 * @throws IllegalStateException if the resolver cannot be instantiated due to
	 * configuration issues
	 */
	ICustomRuleTimeRangeResolver createTimeRangeResolver(RuleType ruleType);

}