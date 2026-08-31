/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualRuleTests {

	@Test
	@DisplayName("Constructor - with valid parameters")
	void testConstructorWithValidParameters() {
		// Arrange & Act
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY, WorkDay.TUESDAY));

		// Assert
		assertThat(virtualRule).satisfies((rule) -> assertThat(rule.ruleName()).isEqualTo("Test Rule"))
			.satisfies((rule) -> assertThat(rule.ruleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS))
			.satisfies((rule) -> assertThat(rule.workDays()).containsExactly(WorkDay.MONDAY, WorkDay.TUESDAY));
	}

	@Test
	@DisplayName("Constructor - with null workDays")
	void testConstructorWithNullWorkDays() {
		// Arrange & Act
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, null);

		// Assert
		assertThat(virtualRule).satisfies((rule) -> assertThat(rule.ruleName()).isEqualTo("Test Rule"))
			.satisfies((rule) -> assertThat(rule.ruleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS))
			.satisfies((rule) -> assertThat(rule.workDays()).isNull());
	}

	@Test
	@DisplayName("Constructor - with empty workDays")
	void testConstructorWithEmptyWorkDays() {
		// Arrange & Act
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, List.of());

		// Assert
		assertThat(virtualRule).satisfies((rule) -> assertThat(rule.ruleName()).isEqualTo("Test Rule"))
			.satisfies((rule) -> assertThat(rule.ruleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS))
			.satisfies((rule) -> assertThat(rule.workDays()).isEmpty());
	}

	@Test
	@DisplayName("getRuleName - returns rule name")
	void testGetRuleNameReturnsRuleName() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		String result = virtualRule.getRuleName();

		// Assert
		assertThat(result).isEqualTo("Test Rule");
	}

	@Test
	@DisplayName("getRuleType - returns rule type")
	void testGetRuleTypeReturnsRuleType() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		RuleType result = virtualRule.getRuleType();

		// Assert
		assertThat(result).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("getWorkDays - returns work days")
	void testGetWorkDaysReturnsWorkDays() {
		// Arrange
		List<WorkDay> workDays = List.of(WorkDay.MONDAY, WorkDay.TUESDAY);
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, workDays);

		// Act
		List<WorkDay> result = virtualRule.getWorkDays();

		// Assert
		assertThat(result).isEqualTo(workDays);
	}

	@Test
	@DisplayName("isApplicableOnDay - with matching work day")
	void testIsApplicableOnDayWithMatchingWorkDay() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY, WorkDay.TUESDAY));

		// Act
		boolean result = virtualRule.isApplicableOnDay(WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isApplicableOnDay - with non-matching work day")
	void testIsApplicableOnDayWithNonMatchingWorkDay() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY, WorkDay.TUESDAY));

		// Act
		boolean result = virtualRule.isApplicableOnDay(WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isApplicableOnDay - with null work days")
	void testIsApplicableOnDayWithNullWorkDays() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, null);

		// Act
		boolean result = virtualRule.isApplicableOnDay(WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isApplicableOnDay - with empty work days")
	void testIsApplicableOnDayWithEmptyWorkDays() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, List.of());

		// Act
		boolean result = virtualRule.isApplicableOnDay(WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isRegularHoursRule - with range-based regular hours")
	void testIsRegularHoursRuleWithRangeBasedRegularHours() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isRegularHoursRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isRegularHoursRule - with duration-based regular hours")
	void testIsRegularHoursRuleWithDurationBasedRegularHours() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.DURATION_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isRegularHoursRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isRegularHoursRule - with non-regular hours rule")
	void testIsRegularHoursRuleWithNonRegularHoursRule() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_DAILY_OVERTIME,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isRegularHoursRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isBreakRule - with range-based break")
	void testIsBreakRuleWithRangeBasedBreak() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_BREAK, List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isBreakRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isBreakRule - with duration-based break")
	void testIsBreakRuleWithDurationBasedBreak() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.DURATION_BASED_BREAK, List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isBreakRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isBreakRule - with non-break rule")
	void testIsBreakRuleWithNonBreakRule() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isBreakRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isDailyOvertimeRule - with range-based daily overtime")
	void testIsDailyOvertimeRuleWithRangeBasedDailyOvertime() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_DAILY_OVERTIME,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isDailyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isDailyOvertimeRule - with duration-based daily overtime")
	void testIsDailyOvertimeRuleWithDurationBasedDailyOvertime() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.DURATION_BASED_DAILY_OVERTIME,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isDailyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isDailyOvertimeRule - with non-daily overtime rule")
	void testIsDailyOvertimeRuleWithNonDailyOvertimeRule() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isDailyOvertimeRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isWeeklyOvertimeRule - with range-based weekly overtime")
	void testIsWeeklyOvertimeRuleWithRangeBasedWeeklyOvertime() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_WEEKLY_OVERTIME,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isWeeklyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWeeklyOvertimeRule - with duration-based weekly overtime")
	void testIsWeeklyOvertimeRuleWithDurationBasedWeeklyOvertime() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.DURATION_BASED_WEEKLY_OVERTIME,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isWeeklyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWeeklyOvertimeRule - with non-weekly overtime rule")
	void testIsWeeklyOvertimeRuleWithNonWeeklyOvertimeRule() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		boolean result = virtualRule.isWeeklyOvertimeRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("createRegularHoursRule - creates regular hours rule")
	void testCreateRegularHoursRuleCreatesRegularHoursRule() {
		// Arrange
		List<WorkDay> workDays = List.of(WorkDay.MONDAY, WorkDay.TUESDAY);

		// Act
		VirtualRule result = VirtualRule.createRegularHoursRule(RuleType.RANGE_BASED_REGULAR_HOURS, workDays);

		// Assert
		assertThat(result).satisfies((rule) -> assertThat(rule.ruleName()).isEqualTo("Regular Hours"))
			.satisfies((rule) -> assertThat(rule.ruleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS))
			.satisfies((rule) -> assertThat(rule.workDays()).isEqualTo(workDays));
	}

	@Test
	@DisplayName("createBreakRule - creates break rule")
	void testCreateBreakRuleCreatesBreakRule() {
		// Arrange
		List<WorkDay> workDays = List.of(WorkDay.MONDAY, WorkDay.TUESDAY);

		// Act
		VirtualRule result = VirtualRule.createBreakRule(RuleType.RANGE_BASED_BREAK, workDays);

		// Assert
		assertThat(result).satisfies((rule) -> assertThat(rule.ruleName()).isEqualTo("Break"))
			.satisfies((rule) -> assertThat(rule.ruleType()).isEqualTo(RuleType.RANGE_BASED_BREAK))
			.satisfies((rule) -> assertThat(rule.workDays()).isEqualTo(workDays));
	}

	@Test
	@DisplayName("equals and hashCode - with same values")
	void testEqualsAndHashCodeWithSameValues() {
		// Arrange
		VirtualRule rule1 = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, List.of(WorkDay.MONDAY));
		VirtualRule rule2 = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS, List.of(WorkDay.MONDAY));

		// Act & Assert
		assertThat(rule1).isEqualTo(rule2).hasSameHashCodeAs(rule2);
	}

	@Test
	@DisplayName("equals and hashCode - with different values")
	void testEqualsAndHashCodeWithDifferentValues() {
		// Arrange
		VirtualRule rule1 = new VirtualRule("Test Rule 1", RuleType.RANGE_BASED_REGULAR_HOURS, List.of(WorkDay.MONDAY));
		VirtualRule rule2 = new VirtualRule("Test Rule 2", RuleType.RANGE_BASED_REGULAR_HOURS, List.of(WorkDay.MONDAY));

		// Act & Assert
		assertThat(rule1).isNotEqualTo(rule2).doesNotHaveSameHashCodeAs(rule2);
	}

	@Test
	@DisplayName("toString - contains all fields")
	void testToStringContainsAllFields() {
		// Arrange
		VirtualRule virtualRule = new VirtualRule("Test Rule", RuleType.RANGE_BASED_REGULAR_HOURS,
				List.of(WorkDay.MONDAY));

		// Act
		String result = virtualRule.toString();

		// Assert
		assertThat(result).contains("Test Rule").contains("RANGE_BASED_REGULAR_HOURS").contains("MONDAY");
	}

}