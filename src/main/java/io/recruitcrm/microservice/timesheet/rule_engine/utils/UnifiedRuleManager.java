/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.VirtualRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified rule manager that combines custom rules and virtual rules into a single
 * evaluation sequence based on precedence configuration.
 *
 * This class provides a unified approach to rule evaluation by treating both custom rules
 * and system rules (like Regular Hours) as evaluatable rules in the same precedence
 * order.
 */
public final class UnifiedRuleManager {

	private UnifiedRuleManager() {
		// Utility class - prevent instantiation
	}

	/**
	 * Creates a unified list of evaluatable rules from custom rules and virtual rules.
	 * The rules are sorted according to the precedence configuration.
	 * @param customRules the list of custom rules
	 * @param virtualRules the list of virtual rules (e.g., Regular Hours)
	 * @return unified and sorted list of evaluatable rules
	 */
	public static List<IEvaluatableRule> createUnifiedRuleList(List<CustomRule> customRules,
			List<VirtualRule> virtualRules) {
		List<IEvaluatableRule> allRules = new ArrayList<>();

		// Add custom rules
		if (customRules != null) {
			allRules.addAll(customRules);
		}

		// Add virtual rules
		if (virtualRules != null) {
			allRules.addAll(virtualRules);
		}

		// Sort according to precedence
		return sortRulesByPrecedence(allRules);
	}

	/**
	 * Creates a unified list of evaluatable rules from custom rules and a single virtual
	 * rule.
	 * @param customRules the list of custom rules
	 * @param virtualRule the virtual rule to include
	 * @return unified and sorted list of evaluatable rules
	 */
	public static List<IEvaluatableRule> createUnifiedRuleList(List<CustomRule> customRules, VirtualRule virtualRule) {
		List<VirtualRule> virtualRules = new ArrayList<>();
		if (virtualRule != null) {
			virtualRules.add(virtualRule);
		}
		return createUnifiedRuleList(customRules, virtualRules);
	}

	/**
	 * Creates a unified list of evaluatable rules from custom rules, Regular Hours, and
	 * Break virtual rules.
	 * @param customRules the list of custom rules
	 * @param regularHoursRuleType the rule type for Regular Hours (range-based or
	 * duration-based)
	 * @param breakRuleType the rule type for Break (range-based or duration-based)
	 * @param timesheet the timesheet containing settings
	 * @return unified list of evaluatable rules
	 */
	public static List<IEvaluatableRule> createUnifiedRuleListWithSystemRules(List<CustomRule> customRules,
			RuleType regularHoursRuleType, RuleType breakRuleType,
			io.recruitcrm.contract_staffing.entity.model.Timesheet timesheet) {

		List<VirtualRule> virtualRules = new ArrayList<>();

		// Add Regular Hours virtual rule - always calculates full work time minus break
		// time
		VirtualRule regularHoursRule = createRegularHoursVirtualRule(regularHoursRuleType, timesheet);
		if (regularHoursRule != null) {
			virtualRules.add(regularHoursRule);
		}

		// Always add Break virtual rule - always calculates break time ranges
		// The calculateBreakTime flag controls amounts in rule evaluation:
		// When calculateBreakTime is TRUE: break rule gets calculated amounts
		// When calculateBreakTime is FALSE: break rule gets zero amounts but shows ranges
		VirtualRule breakRule = createBreakVirtualRule(breakRuleType, timesheet);
		if (breakRule != null) {
			virtualRules.add(breakRule);
		}

		return createUnifiedRuleList(customRules, virtualRules);
	}

	/**
	 * Sorts evaluatable rules according to precedence configuration.
	 * @param rules the list of rules to sort
	 * @return sorted list of rules
	 */
	public static List<IEvaluatableRule> sortRulesByPrecedence(List<IEvaluatableRule> rules) {
		if (rules == null || rules.isEmpty()) {
			return new ArrayList<>();
		}

		// Separate rules by type
		List<IEvaluatableRule> rangeBasedRules = new ArrayList<>();
		List<IEvaluatableRule> durationBasedRules = new ArrayList<>();

		for (IEvaluatableRule rule : rules) {
			if (RulePrecedenceConfig.isRangeBasedRule(rule.getRuleType())) {
				rangeBasedRules.add(rule);
			}
			else if (RulePrecedenceConfig.isDurationBasedRule(rule.getRuleType())) {
				durationBasedRules.add(rule);
			}
		}

		// Sort each type according to precedence
		List<IEvaluatableRule> sortedRules = new ArrayList<>();

		if (!rangeBasedRules.isEmpty()) {
			sortedRules.addAll(sortRangeBasedRules(rangeBasedRules));
		}
		else if (!durationBasedRules.isEmpty()) {
			sortedRules.addAll(sortDurationBasedRules(durationBasedRules));
		}

		// Validate the sorted rules follow precedence order
		if (!RulePrecedenceConfig.validateRulePrecedenceOrder(sortedRules)) {
			throw new IllegalStateException("Rules are not in correct precedence order after sorting");
		}

		return sortedRules;
	}

	/**
	 * Sorts range-based rules according to precedence.
	 * @param rules the list of range-based rules
	 * @return sorted list of range-based rules
	 */
	private static List<IEvaluatableRule> sortRangeBasedRules(List<IEvaluatableRule> rules) {
		List<IEvaluatableRule> sortedRules = new ArrayList<>(rules);

		// Sort by precedence using RulePrecedenceConfig
		sortedRules.sort((a, b) -> {
			int aPrecedence = RulePrecedenceConfig.getRangeBasedPrecedence(a.getRuleType());
			int bPrecedence = RulePrecedenceConfig.getRangeBasedPrecedence(b.getRuleType());

			// First sort by precedence
			int precedenceComparison = Integer.compare(aPrecedence, bPrecedence);
			if (precedenceComparison != 0) {
				return precedenceComparison;
			}

			// For rules with same precedence (e.g., multiple daily overtime rules), sort
			// by threshold
			if (a.isDailyOvertimeRule() && b.isDailyOvertimeRule()) {
				long aThreshold = getDailyThreshold(a);
				long bThreshold = getDailyThreshold(b);
				return Long.compare(aThreshold, bThreshold);
			}

			return 0;
		});

		return sortedRules;
	}

	/**
	 * Sorts duration-based rules according to precedence.
	 * @param rules the list of duration-based rules
	 * @return sorted list of duration-based rules
	 */
	private static List<IEvaluatableRule> sortDurationBasedRules(List<IEvaluatableRule> rules) {
		List<IEvaluatableRule> sortedRules = new ArrayList<>(rules);

		// Sort by precedence using RulePrecedenceConfig
		sortedRules.sort((a, b) -> {
			int aPrecedence = RulePrecedenceConfig.getDurationBasedPrecedence(a.getRuleType());
			int bPrecedence = RulePrecedenceConfig.getDurationBasedPrecedence(b.getRuleType());

			// First sort by precedence
			int precedenceComparison = Integer.compare(aPrecedence, bPrecedence);
			if (precedenceComparison != 0) {
				return precedenceComparison;
			}

			// For rules with same precedence (e.g., multiple daily overtime rules), sort
			// by threshold
			if (a.isDailyOvertimeRule() && b.isDailyOvertimeRule()) {
				long aThreshold = getDailyThreshold(a);
				long bThreshold = getDailyThreshold(b);
				return Long.compare(aThreshold, bThreshold);
			}

			return 0;
		});

		return sortedRules;
	}

	/**
	 * Gets the daily threshold for a rule.
	 * @param rule the rule to get threshold for
	 * @return the daily threshold in hours, or 0 if not available
	 */
	private static long getDailyThreshold(IEvaluatableRule rule) {
		if (rule instanceof CustomRule customRule && customRule.getDailyThreshold() != null) {
			return customRule.getDailyThreshold().toHours();
		}
		return 0;
	}

	/**
	 * Creates a virtual rule for Regular Hours based on timesheet settings.
	 * @param ruleType the rule type (range-based or duration-based)
	 * @param timesheet the timesheet containing settings
	 * @return a virtual rule for Regular Hours, or null if not applicable
	 */
	public static VirtualRule createRegularHoursVirtualRule(RuleType ruleType,
			io.recruitcrm.contract_staffing.entity.model.Timesheet timesheet) {

		// Get work days from timesheet settings
		List<WorkDay> workDays = getWorkDaysFromTimesheet(timesheet);

		if (workDays.isEmpty()) {
			return null; // No work days configured
		}

		return VirtualRule.createRegularHoursRule(ruleType, workDays);
	}

	/**
	 * Creates a virtual rule for Break based on timesheet settings. Break rules are
	 * always created and always calculate break time ranges. The calculateBreakTime flag
	 * controls whether amounts are applied during rule evaluation.
	 *
	 * Break rules are applicable on all days regardless of timesheet work day settings.
	 * @param ruleType the rule type (range-based or duration-based)
	 * @param timesheet the timesheet containing settings
	 * @return a virtual rule for Break, or null if not applicable
	 */
	public static VirtualRule createBreakVirtualRule(RuleType ruleType,
			io.recruitcrm.contract_staffing.entity.model.Timesheet timesheet) {

		// Break rules are applicable on all days, so we pass an empty list
		// which will make the rule applicable on all days according to
		// VirtualRule.isApplicableOnDay()
		return VirtualRule.createBreakRule(ruleType, new ArrayList<>());
	}

	/**
	 * Extracts work days from timesheet settings.
	 * @param timesheet the timesheet containing settings
	 * @return list of work days
	 */
	private static List<WorkDay> getWorkDaysFromTimesheet(
			io.recruitcrm.contract_staffing.entity.model.Timesheet timesheet) {
		List<WorkDay> workDays = new ArrayList<>();

		if (timesheet.getTimesheetSetting() != null && timesheet.getTimesheetSetting().getTemplateWorkDay() != null) {

			// Convert entity TemplateWorkDay to DTO TemplateWorkDay using the mapper
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay> templateWorkDays = io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineTimesheetSettingMapper.INSTANCE
				.toTemplateWorkDayList(timesheet.getTimesheetSetting().getTemplateWorkDay());

			workDays = templateWorkDays.stream().map(TemplateWorkDay::getWorkDayType).toList();
		}

		return workDays;
	}

}