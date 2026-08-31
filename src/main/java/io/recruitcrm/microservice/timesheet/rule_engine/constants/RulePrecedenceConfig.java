/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration class for rule precedence in time logging.
 *
 * This class defines the static precedence order for both range-based and duration-based
 * time logging rules. The precedence order can be easily modified by changing the order
 * of rule types in the respective lists.
 *
 * Range-based precedence order: 1. Break 2. After Shift 3. Before Shift 4. Specific Time
 * Range 5. Regular Hours 6. Daily Overtime (sorted by increasing threshold) 7. Weekly
 * Overtime
 *
 * Duration-based precedence order: 1. Break 2. Specific Time Range 3. Regular Hours 4.
 * Daily Overtime (sorted by increasing threshold) 5. Weekly Overtime
 *
 * Note: Break rules are evaluated first in both logging methods to ensure they claim
 * their time ranges before other rules. The calculateBreakTime setting controls whether
 * break rules calculate pay amounts, but break time ranges are always identified and
 * occupied to prevent other rules from claiming those periods.
 */
public final class RulePrecedenceConfig {

	private RulePrecedenceConfig() {
		/*
		 * Utility class - prevent instantiation
		 */
	}

	/**
	 * Range-based rule precedence order. Rules are evaluated in this exact order.
	 * Multiple Daily Overtime rules will be sorted by their increasing threshold.
	 */
	private static final List<RuleType> RANGE_BASED_PRECEDENCE = Arrays.asList(
			/* 1. Break */
			RuleType.RANGE_BASED_BREAK,
			/* 2. After Shift */
			RuleType.RANGE_BASED_AFTER_SHIFT,
			/* 3. Before Shift */
			RuleType.RANGE_BASED_BEFORE_SHIFT,
			/* 4. Specific Time Range */
			RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE,
			/* 5. Regular Hours */
			RuleType.RANGE_BASED_REGULAR_HOURS,
			/* 6. Daily Overtime (sorted by threshold) */
			RuleType.RANGE_BASED_DAILY_OVERTIME,
			/* 7. Weekly Overtime */
			RuleType.RANGE_BASED_WEEKLY_OVERTIME,
			/* 8. Default Pay (week-end sweep: pays any unallocated worked time at 1x) */
			RuleType.RANGE_BASED_DEFAULT_PAY);

	/**
	 * Duration-based rule precedence order. Rules are evaluated in this exact order.
	 * Multiple Daily Overtime rules will be sorted by their increasing threshold.
	 */
	private static final List<RuleType> DURATION_BASED_PRECEDENCE = Arrays.asList(
			/* 1. Break */
			RuleType.DURATION_BASED_BREAK,
			/* 2. Specific Time Range */
			RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE,
			/* 3. Regular Hours */
			RuleType.DURATION_BASED_REGULAR_HOURS,
			/* 4. Daily Overtime (sorted by threshold) */
			RuleType.DURATION_BASED_DAILY_OVERTIME,
			/* 5. Weekly Overtime */
			RuleType.DURATION_BASED_WEEKLY_OVERTIME,
			/* 6. Default Pay (week-end sweep: pays any unallocated worked time at 1x) */
			RuleType.DURATION_BASED_DEFAULT_PAY);

	/**
	 * Map of rule types to their precedence order for range-based rules. Lower values
	 * indicate higher precedence (evaluated first).
	 */
	private static final Map<RuleType, Integer> RANGE_BASED_PRECEDENCE_MAP = createPrecedenceMap(
			RANGE_BASED_PRECEDENCE);

	/**
	 * Map of rule types to their precedence order for duration-based rules. Lower values
	 * indicate higher precedence (evaluated first).
	 */
	private static final Map<RuleType, Integer> DURATION_BASED_PRECEDENCE_MAP = createPrecedenceMap(
			DURATION_BASED_PRECEDENCE);

	/**
	 * Creates a precedence map from a list of rule types.
	 * @param precedenceList the list of rule types in precedence order
	 * @return a map of rule type to precedence value (lower = higher precedence)
	 */
	private static Map<RuleType, Integer> createPrecedenceMap(List<RuleType> precedenceList) {
		return precedenceList.stream().collect(Collectors.toMap((ruleType) -> ruleType, precedenceList::indexOf));
	}

	/**
	 * Gets the precedence value for a range-based rule type.
	 * @param ruleType the rule type
	 * @return the precedence value (lower = higher precedence), or Integer.MAX_VALUE if
	 * not found
	 */
	public static int getRangeBasedPrecedence(RuleType ruleType) {
		return RANGE_BASED_PRECEDENCE_MAP.getOrDefault(ruleType, Integer.MAX_VALUE);
	}

	/**
	 * Gets the precedence value for a duration-based rule type.
	 * @param ruleType the rule type
	 * @return the precedence value (lower = higher precedence), or Integer.MAX_VALUE if
	 * not found
	 */
	public static int getDurationBasedPrecedence(RuleType ruleType) {
		return DURATION_BASED_PRECEDENCE_MAP.getOrDefault(ruleType, Integer.MAX_VALUE);
	}

	/**
	 * Gets the range-based precedence list.
	 * @return the list of rule types in precedence order
	 */
	public static List<RuleType> getRangeBasedPrecedenceList() {
		return List.copyOf(RANGE_BASED_PRECEDENCE);
	}

	/**
	 * Gets the duration-based precedence list.
	 * @return the list of rule types in precedence order
	 */
	public static List<RuleType> getDurationBasedPrecedenceList() {
		return List.copyOf(DURATION_BASED_PRECEDENCE);
	}

	/**
	 * Checks if a rule type is a range-based rule.
	 * @param ruleType the rule type to check
	 * @return true if it's a range-based rule, false otherwise
	 */
	public static boolean isRangeBasedRule(RuleType ruleType) {
		return RANGE_BASED_PRECEDENCE_MAP.containsKey(ruleType);
	}

	/**
	 * Checks if a rule type is a duration-based rule.
	 * @param ruleType the rule type to check
	 * @return true if it's a duration-based rule, false otherwise
	 */
	public static boolean isDurationBasedRule(RuleType ruleType) {
		return DURATION_BASED_PRECEDENCE_MAP.containsKey(ruleType);
	}

	/**
	 * Gets the precedence value for any rule type (range-based or duration-based).
	 * @param ruleType the rule type
	 * @return the precedence value (lower = higher precedence), or Integer.MAX_VALUE if
	 * not found
	 */
	public static int getPrecedence(RuleType ruleType) {
		if (isRangeBasedRule(ruleType)) {
			return getRangeBasedPrecedence(ruleType);
		}
		else if (isDurationBasedRule(ruleType)) {
			return getDurationBasedPrecedence(ruleType);
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * Validates that a list of rules follows the correct precedence order.
	 * @param rules the list of rules to validate
	 * @return true if the rules are in correct precedence order, false otherwise
	 */
	public static boolean validateRulePrecedenceOrder(List<IEvaluatableRule> rules) {
		if (rules == null || rules.size() <= 1) {
			return true;
		}

		for (int i = 0; i < rules.size() - 1; i++) {
			IEvaluatableRule currentRule = rules.get(i);
			IEvaluatableRule nextRule = rules.get(i + 1);

			int currentPrecedence = getPrecedence(currentRule.getRuleType());
			int nextPrecedence = getPrecedence(nextRule.getRuleType());

			/*
			 * If current rule has higher precedence value (lower priority), it should
			 * come after
			 */
			if (currentPrecedence > nextPrecedence) {
				return false;
			}

			/*
			 * For rules with same precedence (e.g., multiple daily overtime rules),
			 * validate that they are sorted by threshold
			 */
			if (currentPrecedence == nextPrecedence && currentRule.isDailyOvertimeRule()
					&& nextRule.isDailyOvertimeRule()) {

				long currentThreshold = getDailyThreshold(currentRule);
				long nextThreshold = getDailyThreshold(nextRule);

				if (currentThreshold > nextThreshold) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Gets a human-readable description of the precedence order for range-based rules.
	 * @return formatted string describing the precedence order
	 */
	public static String getRangeBasedPrecedenceDescription() {
		List<RuleType> precedenceList = getRangeBasedPrecedenceList();
		StringBuilder description = new StringBuilder("Range-based rule precedence order:");

		for (int i = 0; i < precedenceList.size(); i++) {
			RuleType ruleType = precedenceList.get(i);
			description.append(String.format("%n%d. %s", i + 1, formatRuleTypeName(ruleType)));
		}

		return description.toString();
	}

	/**
	 * Gets a human-readable description of the precedence order for duration-based rules.
	 * @return formatted string describing the precedence order
	 */
	public static String getDurationBasedPrecedenceDescription() {
		List<RuleType> precedenceList = getDurationBasedPrecedenceList();
		StringBuilder description = new StringBuilder("Duration-based rule precedence order:");

		for (int i = 0; i < precedenceList.size(); i++) {
			RuleType ruleType = precedenceList.get(i);
			description.append(String.format("%n%d. %s", i + 1, formatRuleTypeName(ruleType)));
		}

		return description.toString();
	}

	/**
	 * Gets the daily threshold for a rule (helper method for validation).
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
	 * Formats a rule type name for human-readable output.
	 * @param ruleType the rule type to format
	 * @return formatted rule type name
	 */
	private static String formatRuleTypeName(RuleType ruleType) {
		String name = ruleType.name();

		/*
		 * Remove the prefix and convert to readable format
		 */
		if (name.startsWith("RANGE_BASED_")) {
			name = name.substring("RANGE_BASED_".length());
		}
		else if (name.startsWith("DURATION_BASED_")) {
			name = name.substring("DURATION_BASED_".length());
		}

		/*
		 * Convert underscores to spaces and capitalize
		 */
		return name.replace("_", " ")
			.toLowerCase(Locale.ROOT)
			.chars()
			.mapToObj((ch) -> (ch == ' ') ? " " : String.valueOf((char) Character.toUpperCase(ch)))
			.collect(Collectors.joining());
	}

}