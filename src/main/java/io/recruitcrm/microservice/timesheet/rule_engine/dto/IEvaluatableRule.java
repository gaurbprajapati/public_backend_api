/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;

import java.util.List;

/**
 * Unified interface for rules that can be evaluated in the rule engine.
 *
 * This interface provides a common contract for both CustomRule and VirtualRule, allowing
 * them to be treated uniformly in the evaluation sequence.
 */
public interface IEvaluatableRule {

	/**
	 * Gets the name of the rule.
	 * @return the rule name
	 */
	String getRuleName();

	/**
	 * Gets the type of the rule.
	 * @return the rule type
	 */
	RuleType getRuleType();

	/**
	 * Gets the work days this rule applies to.
	 * @return list of work days, or null/empty if applies to all days
	 */
	List<WorkDay> getWorkDays();

	/**
	 * Checks if this rule is applicable on the given work day.
	 * @param timeLogDay the work day to check
	 * @return true if the rule applies on this day, false otherwise
	 */
	boolean isApplicableOnDay(WorkDay timeLogDay);

	/**
	 * Checks if this is a system rule (like Regular Hours).
	 * @return true if this is a system rule, false if it's a custom rule
	 */
	default boolean isSystemRule() {
		// System rules are Regular Hours and Break rules
		return isRegularHoursRule() || isBreakRule();
	}

	/**
	 * Checks if this is a Regular Hours rule.
	 * @return true if this is a Regular Hours rule, false otherwise
	 */
	default boolean isRegularHoursRule() {
		return getRuleType() == RuleType.RANGE_BASED_REGULAR_HOURS
				|| getRuleType() == RuleType.DURATION_BASED_REGULAR_HOURS;
	}

	/**
	 * Checks if this is a Break rule.
	 * @return true if this is a Break rule, false otherwise
	 */
	default boolean isBreakRule() {
		return getRuleType() == RuleType.RANGE_BASED_BREAK || getRuleType() == RuleType.DURATION_BASED_BREAK;
	}

	/**
	 * Checks if this is a Daily Overtime rule.
	 * @return true if this is a Daily Overtime rule, false otherwise
	 */
	default boolean isDailyOvertimeRule() {
		return getRuleType() == RuleType.RANGE_BASED_DAILY_OVERTIME
				|| getRuleType() == RuleType.DURATION_BASED_DAILY_OVERTIME;
	}

	/**
	 * Checks if this is a Weekly Overtime rule.
	 * @return true if this is a Weekly Overtime rule, false otherwise
	 */
	default boolean isWeeklyOvertimeRule() {
		return getRuleType() == RuleType.RANGE_BASED_WEEKLY_OVERTIME
				|| getRuleType() == RuleType.DURATION_BASED_WEEKLY_OVERTIME;
	}

}