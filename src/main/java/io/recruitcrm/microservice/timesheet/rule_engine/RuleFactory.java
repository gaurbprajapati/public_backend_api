/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedBreakRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedDailyOvertimeRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedDefaultPayRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedRegularHoursRangeRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedSpecificHourRangeRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedWeeklyOvertimeRuleRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedAfterShiftRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedDefaultPayRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedBeforeShiftRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedBreakRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedDailyOvertimeRuleRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedRegularHoursRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedSpecificTimeRangeRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based.RangeBasedWeeklyOvertimeRuleTimeRangeResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Implementation of IRuleFactory that creates rule instances and time range resolvers.
 */
@Component
public class RuleFactory implements IRuleFactory {

	private final Map<RuleType, ICustomRuleTimeRangeResolver> timeRangeResolverMap;

	public RuleFactory(@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.timeRangeResolverMap = initializeTimeRangeResolverMap(logger);
	}

	@Override
	public IRule createRule(RuleType ruleType, @Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		switch (ruleType) {
			// Range-based rules
			case RANGE_BASED_BEFORE_SHIFT:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.BeforeShiftRule(logger);
			case RANGE_BASED_AFTER_SHIFT:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.AfterShiftRule(logger);
			case RANGE_BASED_DAILY_OVERTIME:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.DailyOvertimeRule(logger);
			case RANGE_BASED_WEEKLY_OVERTIME:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.WeeklyOvertimeRule(logger);
			case RANGE_BASED_REGULAR_HOURS:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.RegularHoursRule(logger);
			case RANGE_BASED_SPECIFIC_TIME_RANGE:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.SpecificTimeRangeRule(logger);
			case RANGE_BASED_BREAK:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.BreakRule(logger);
			case RANGE_BASED_DEFAULT_PAY:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts.DefaultPayRule(logger);

			// Duration-based rules
			case DURATION_BASED_REGULAR_HOURS:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly.RegularHoursRule(logger);
			case DURATION_BASED_DAILY_OVERTIME:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly.DailyOvertimeRule(logger);
			case DURATION_BASED_SPECIFIC_HOUR_RANGE:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly.SpecificHoursRangeRule(logger);
			case DURATION_BASED_WEEKLY_OVERTIME:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly.WeeklyOvertimeRule(logger);
			case DURATION_BASED_BREAK:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly.BreakRule(logger);
			case DURATION_BASED_DEFAULT_PAY:
				return new io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly.DefaultPayRule(logger);

			default:
				throw new IllegalArgumentException("Unsupported rule type: " + ruleType);
		}
	}

	@Override
	public ICustomRuleTimeRangeResolver createTimeRangeResolver(RuleType ruleType) {
		ICustomRuleTimeRangeResolver resolver = this.timeRangeResolverMap.get(ruleType);

		if (resolver == null) {
			throw new IllegalArgumentException("Unsupported rule type for time range resolver: " + ruleType);
		}

		return resolver;
	}

	private Map<RuleType, ICustomRuleTimeRangeResolver> initializeTimeRangeResolverMap(Logger logger) {
		Map<RuleType, ICustomRuleTimeRangeResolver> map = new EnumMap<>(RuleType.class);

		// Range-based resolvers
		map.put(RuleType.RANGE_BASED_BEFORE_SHIFT, new RangeBasedBeforeShiftRuleTimeRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_AFTER_SHIFT, new RangeBasedAfterShiftRuleTimeRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_DAILY_OVERTIME, new RangeBasedDailyOvertimeRuleRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_WEEKLY_OVERTIME, new RangeBasedWeeklyOvertimeRuleTimeRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_REGULAR_HOURS, new RangeBasedRegularHoursRuleTimeRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE, new RangeBasedSpecificTimeRangeRuleTimeRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_BREAK, new RangeBasedBreakRuleTimeRangeResolver(logger));
		map.put(RuleType.RANGE_BASED_DEFAULT_PAY, new RangeBasedDefaultPayRuleTimeRangeResolver(logger));

		// Duration-based resolvers
		map.put(RuleType.DURATION_BASED_REGULAR_HOURS, new DurationBasedRegularHoursRangeRuleTimeRangeResolver(logger));
		map.put(RuleType.DURATION_BASED_DAILY_OVERTIME, new DurationBasedDailyOvertimeRuleTimeRangeResolver(logger));
		map.put(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE,
				new DurationBasedSpecificHourRangeRuleTimeRangeResolver(logger));
		map.put(RuleType.DURATION_BASED_WEEKLY_OVERTIME, new DurationBasedWeeklyOvertimeRuleRangeResolver(logger));
		map.put(RuleType.DURATION_BASED_BREAK, new DurationBasedBreakRuleTimeRangeResolver(logger));
		map.put(RuleType.DURATION_BASED_DEFAULT_PAY, new DurationBasedDefaultPayRuleTimeRangeResolver(logger));

		return map;
	}

}