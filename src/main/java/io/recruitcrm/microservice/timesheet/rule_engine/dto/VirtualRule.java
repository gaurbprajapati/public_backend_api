/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;

import java.util.List;

/**
 * Virtual rule representing system rules that need to be evaluated in precedence order.
 *
 * This class allows system rules like Regular Hours to be treated as regular rules in the
 * evaluation sequence, providing a unified approach to rule precedence.
 */
public record VirtualRule(String ruleName, RuleType ruleType, List<WorkDay> workDays) implements IEvaluatableRule {

	/**
	 * Creates a virtual rule for Regular Hours.
	 * @param ruleType the rule type (range-based or duration-based)
	 * @param workDays the work days this rule applies to
	 * @return a virtual rule for Regular Hours
	 */
	public static VirtualRule createRegularHoursRule(RuleType ruleType, List<WorkDay> workDays) {
		return new VirtualRule("Regular Hours", ruleType, workDays);
	}

	/**
	 * Creates a virtual rule for Break.
	 * @param ruleType the rule type (range-based or duration-based)
	 * @param workDays the work days this rule applies to
	 * @return a virtual rule for Break
	 */
	public static VirtualRule createBreakRule(RuleType ruleType, List<WorkDay> workDays) {
		return new VirtualRule("Break", ruleType, workDays);
	}

	@Override
	public String getRuleName() {
		return this.ruleName;
	}

	@Override
	public RuleType getRuleType() {
		return this.ruleType;
	}

	@Override
	public List<WorkDay> getWorkDays() {
		return this.workDays;
	}

	@Override
	public boolean isApplicableOnDay(WorkDay timeLogDay) {
		// If workDays is null or empty, rule applies to all days
		if (this.workDays == null || this.workDays.isEmpty()) {
			return true;
		}

		// Check if the rule's workDays contains this day
		return this.workDays.contains(timeLogDay);
	}

	/**
	 * Checks if this is a Regular Hours rule.
	 * @return true if this is a Regular Hours rule, false otherwise
	 */
	@Override
	public boolean isRegularHoursRule() {
		return this.ruleType == RuleType.RANGE_BASED_REGULAR_HOURS
				|| this.ruleType == RuleType.DURATION_BASED_REGULAR_HOURS;
	}

	/**
	 * Checks if this is a Break rule.
	 * @return true if this is a Break rule, false otherwise
	 */
	@Override
	public boolean isBreakRule() {
		return this.ruleType == RuleType.RANGE_BASED_BREAK || this.ruleType == RuleType.DURATION_BASED_BREAK;
	}

	/**
	 * Checks if this is a Daily Overtime rule.
	 * @return true if this is a Daily Overtime rule, false otherwise
	 */
	@Override
	public boolean isDailyOvertimeRule() {
		return this.ruleType == RuleType.RANGE_BASED_DAILY_OVERTIME
				|| this.ruleType == RuleType.DURATION_BASED_DAILY_OVERTIME;
	}

	/**
	 * Checks if this is a Weekly Overtime rule.
	 * @return true if this is a Weekly Overtime rule, false otherwise
	 */
	@Override
	public boolean isWeeklyOvertimeRule() {
		return this.ruleType == RuleType.RANGE_BASED_WEEKLY_OVERTIME
				|| this.ruleType == RuleType.DURATION_BASED_WEEKLY_OVERTIME;
	}
}