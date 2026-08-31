/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RulePrecedenceConfigTests {

	@Test
	@DisplayName("Get range based precedence - Valid rule types return correct precedence")
	void testGetRangeBasedPrecedenceValidRuleTypesReturnCorrectPrecedence() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_BREAK)).isZero();
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_AFTER_SHIFT)).isEqualTo(1);
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_BEFORE_SHIFT)).isEqualTo(2);
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE)).isEqualTo(3);
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_REGULAR_HOURS)).isEqualTo(4);
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_DAILY_OVERTIME)).isEqualTo(5);
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_WEEKLY_OVERTIME)).isEqualTo(6);
		assertThat(RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.RANGE_BASED_DEFAULT_PAY)).isEqualTo(7);
	}

	@Test
	@DisplayName("Get range based precedence - Invalid rule type returns max value")
	void testGetRangeBasedPrecedenceInvalidRuleTypeReturnsMaxValue() {
		// Act
		int precedence = RulePrecedenceConfig.getRangeBasedPrecedence(RuleType.DURATION_BASED_BREAK);

		// Assert
		assertThat(precedence).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("Get duration based precedence - Valid rule types return correct precedence")
	void testGetDurationBasedPrecedenceValidRuleTypesReturnCorrectPrecedence() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.DURATION_BASED_BREAK)).isZero();
		assertThat(RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE))
			.isEqualTo(1);
		assertThat(RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.DURATION_BASED_REGULAR_HOURS)).isEqualTo(2);
		assertThat(RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.DURATION_BASED_DAILY_OVERTIME))
			.isEqualTo(3);
		assertThat(RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.DURATION_BASED_WEEKLY_OVERTIME))
			.isEqualTo(4);
		assertThat(RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.DURATION_BASED_DEFAULT_PAY)).isEqualTo(5);
	}

	@Test
	@DisplayName("Get duration based precedence - Invalid rule type returns max value")
	void testGetDurationBasedPrecedenceInvalidRuleTypeReturnsMaxValue() {
		// Act
		int precedence = RulePrecedenceConfig.getDurationBasedPrecedence(RuleType.RANGE_BASED_BREAK);

		// Assert
		assertThat(precedence).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("Get range based precedence list - Returns correct list")
	void testGetRangeBasedPrecedenceListReturnsCorrectList() {
		// Act
		List<RuleType> precedenceList = RulePrecedenceConfig.getRangeBasedPrecedenceList();

		// Assert
		assertThat(precedenceList).hasSize(8)
			.containsExactly(RuleType.RANGE_BASED_BREAK, RuleType.RANGE_BASED_AFTER_SHIFT,
					RuleType.RANGE_BASED_BEFORE_SHIFT, RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE,
					RuleType.RANGE_BASED_REGULAR_HOURS, RuleType.RANGE_BASED_DAILY_OVERTIME,
					RuleType.RANGE_BASED_WEEKLY_OVERTIME, RuleType.RANGE_BASED_DEFAULT_PAY);
	}

	@Test
	@DisplayName("Get duration based precedence list - Returns correct list")
	void testGetDurationBasedPrecedenceListReturnsCorrectList() {
		// Act
		List<RuleType> precedenceList = RulePrecedenceConfig.getDurationBasedPrecedenceList();

		// Assert
		assertThat(precedenceList).hasSize(6)
			.containsExactly(RuleType.DURATION_BASED_BREAK, RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE,
					RuleType.DURATION_BASED_REGULAR_HOURS, RuleType.DURATION_BASED_DAILY_OVERTIME,
					RuleType.DURATION_BASED_WEEKLY_OVERTIME, RuleType.DURATION_BASED_DEFAULT_PAY);
	}

	@Test
	@DisplayName("Is range based rule - Range based rules return true")
	void testIsRangeBasedRuleRangeBasedRulesReturnTrue() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_BREAK)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_AFTER_SHIFT)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_BEFORE_SHIFT)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_REGULAR_HOURS)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_DAILY_OVERTIME)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_WEEKLY_OVERTIME)).isTrue();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.RANGE_BASED_DEFAULT_PAY)).isTrue();
	}

	@Test
	@DisplayName("Is range based rule - Duration based rules return false")
	void testIsRangeBasedRuleDurationBasedRulesReturnFalse() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.DURATION_BASED_BREAK)).isFalse();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE)).isFalse();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.DURATION_BASED_REGULAR_HOURS)).isFalse();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.DURATION_BASED_DAILY_OVERTIME)).isFalse();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME)).isFalse();
		assertThat(RulePrecedenceConfig.isRangeBasedRule(RuleType.DURATION_BASED_DEFAULT_PAY)).isFalse();
	}

	@Test
	@DisplayName("Is duration based rule - Duration based rules return true")
	void testIsDurationBasedRuleDurationBasedRulesReturnTrue() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.DURATION_BASED_BREAK)).isTrue();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE)).isTrue();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.DURATION_BASED_REGULAR_HOURS)).isTrue();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.DURATION_BASED_DAILY_OVERTIME)).isTrue();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME)).isTrue();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.DURATION_BASED_DEFAULT_PAY)).isTrue();
	}

	@Test
	@DisplayName("Is duration based rule - Range based rules return false")
	void testIsDurationBasedRuleRangeBasedRulesReturnFalse() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_BREAK)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_AFTER_SHIFT)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_BEFORE_SHIFT)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_REGULAR_HOURS)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_DAILY_OVERTIME)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_WEEKLY_OVERTIME)).isFalse();
		assertThat(RulePrecedenceConfig.isDurationBasedRule(RuleType.RANGE_BASED_DEFAULT_PAY)).isFalse();
	}

	@Test
	@DisplayName("Get precedence - Range based rules return range based precedence")
	void testGetPrecedenceRangeBasedRulesReturnRangeBasedPrecedence() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.getPrecedence(RuleType.RANGE_BASED_BREAK)).isZero();
		assertThat(RulePrecedenceConfig.getPrecedence(RuleType.RANGE_BASED_AFTER_SHIFT)).isEqualTo(1);
		assertThat(RulePrecedenceConfig.getPrecedence(RuleType.RANGE_BASED_BEFORE_SHIFT)).isEqualTo(2);
	}

	@Test
	@DisplayName("Get precedence - Duration based rules return duration based precedence")
	void testGetPrecedenceDurationBasedRulesReturnDurationBasedPrecedence() {
		// Act & Assert
		assertThat(RulePrecedenceConfig.getPrecedence(RuleType.DURATION_BASED_BREAK)).isZero();
		assertThat(RulePrecedenceConfig.getPrecedence(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE)).isEqualTo(1);
		assertThat(RulePrecedenceConfig.getPrecedence(RuleType.DURATION_BASED_REGULAR_HOURS)).isEqualTo(2);
	}

	// Note: The branch in getPrecedence that returns Integer.MAX_VALUE for unknown types
	// cannot be covered in tests because Java enums are final and cannot be mocked or
	// extended.
	// All RuleType values are either range-based or duration-based, so this branch is
	// unreachable
	// in normal code. This is a defensive branch for future-proofing only.

	@Test
	@DisplayName("Validate rule precedence order - Null rules return true")
	void testValidateRulePrecedenceOrderNullRulesReturnTrue() {
		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(null);

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Validate rule precedence order - Empty rules return true")
	void testValidateRulePrecedenceOrderEmptyRulesReturnTrue() {
		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Collections.emptyList());

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Validate rule precedence order - Single rule returns true")
	void testValidateRulePrecedenceOrderSingleRuleReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = mock(IEvaluatableRule.class);
		given(rule.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(List.of(rule));

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Validate rule precedence order - Valid precedence order returns true")
	void testValidateRulePrecedenceOrderValidPrecedenceOrderReturnsTrue() {
		// Arrange
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_AFTER_SHIFT);

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Arrays.asList(rule1, rule2));

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Validate rule precedence order - Invalid precedence order returns false")
	void testValidateRulePrecedenceOrderInvalidPrecedenceOrderReturnsFalse() {
		// Arrange
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_AFTER_SHIFT);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Arrays.asList(rule1, rule2));

		// Assert
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Validate rule precedence order - Daily overtime rules with valid threshold order returns true")
	void testValidateRulePrecedenceOrderDailyOvertimeRulesWithValidThresholdOrderReturnsTrue() {
		// Arrange
		CustomRule rule1 = mock(CustomRule.class);
		CustomRule rule2 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(Duration.ofHours(8));
		given(rule2.getDailyThreshold()).willReturn(Duration.ofHours(10));

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Arrays.asList(rule1, rule2));

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Validate rule precedence order - Daily overtime rules with invalid threshold order returns false")
	void testValidateRulePrecedenceOrderDailyOvertimeRulesWithInvalidThresholdOrderReturnsFalse() {
		// Arrange
		CustomRule rule1 = mock(CustomRule.class);
		CustomRule rule2 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(Duration.ofHours(10));
		given(rule2.getDailyThreshold()).willReturn(Duration.ofHours(8));

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Arrays.asList(rule1, rule2));

		// Assert
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Validate rule precedence order - Daily overtime rules with null threshold returns true")
	void testValidateRulePrecedenceOrderDailyOvertimeRulesWithNullThresholdReturnsTrue() {
		// Arrange
		CustomRule rule1 = mock(CustomRule.class);
		CustomRule rule2 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(null);
		given(rule2.getDailyThreshold()).willReturn(Duration.ofHours(8));

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Arrays.asList(rule1, rule2));

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Validate rule precedence order - Non-daily overtime rules with same precedence returns true")
	void testValidateRulePrecedenceOrderNonDailyOvertimeRulesWithSamePrecedenceReturnsTrue() {
		// Arrange
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(rule1.isDailyOvertimeRule()).willReturn(false);
		given(rule2.isDailyOvertimeRule()).willReturn(false);

		// Act
		boolean isValid = RulePrecedenceConfig.validateRulePrecedenceOrder(Arrays.asList(rule1, rule2));

		// Assert
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Get range based precedence description - Returns formatted description")
	void testGetRangeBasedPrecedenceDescriptionReturnsFormattedDescription() {
		// Act
		String description = RulePrecedenceConfig.getRangeBasedPrecedenceDescription();

		// Assert
		assertThat(description).contains("Range-based rule precedence order:")
			.contains("1. BREAK")
			.contains("2. AFTER SHIFT")
			.contains("3. BEFORE SHIFT")
			.contains("4. SPECIFIC TIME RANGE")
			.contains("5. REGULAR HOURS")
			.contains("6. DAILY OVERTIME")
			.contains("7. WEEKLY OVERTIME");
	}

	@Test
	@DisplayName("Get duration based precedence description - Returns formatted description")
	void testGetDurationBasedPrecedenceDescriptionReturnsFormattedDescription() {
		// Act
		String description = RulePrecedenceConfig.getDurationBasedPrecedenceDescription();

		// Assert
		assertThat(description).contains("Duration-based rule precedence order:")
			.contains("1. BREAK")
			.contains("2. SPECIFIC HOUR RANGE")
			.contains("3. REGULAR HOURS")
			.contains("4. DAILY OVERTIME")
			.contains("5. WEEKLY OVERTIME");
	}

}