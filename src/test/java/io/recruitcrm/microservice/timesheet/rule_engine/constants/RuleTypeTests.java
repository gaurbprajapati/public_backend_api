/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleTypeTests {

	@Test
	@DisplayName("From ID - Valid range based before shift ID")
	void testFromIdValidRangeBasedBeforeShiftIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(2);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_BEFORE_SHIFT);
	}

	@Test
	@DisplayName("From ID - Valid range based after shift ID")
	void testFromIdValidRangeBasedAfterShiftIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(1);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_AFTER_SHIFT);
	}

	@Test
	@DisplayName("From ID - Valid range based specific time range ID")
	void testFromIdValidRangeBasedSpecificTimeRangeIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(3);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);
	}

	@Test
	@DisplayName("From ID - Valid range based daily overtime ID")
	void testFromIdValidRangeBasedDailyOvertimeIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(4);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_DAILY_OVERTIME);
	}

	@Test
	@DisplayName("From ID - Valid range based weekly overtime ID")
	void testFromIdValidRangeBasedWeeklyOvertimeIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(5);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
	}

	@Test
	@DisplayName("From ID - Valid duration based specific hour range ID")
	void testFromIdValidDurationBasedSpecificHourRangeIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(6);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
	}

	@Test
	@DisplayName("From ID - Valid duration based daily overtime ID")
	void testFromIdValidDurationBasedDailyOvertimeIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(7);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_DAILY_OVERTIME);
	}

	@Test
	@DisplayName("From ID - Valid duration based weekly overtime ID")
	void testFromIdValidDurationBasedWeeklyOvertimeIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(8);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
	}

	@Test
	@DisplayName("From ID - Valid range based regular hours ID")
	void testFromIdValidRangeBasedRegularHoursIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(11);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("From ID - Valid duration based regular hours ID")
	void testFromIdValidDurationBasedRegularHoursIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(12);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("From ID - Valid range based break ID")
	void testFromIdValidRangeBasedBreakIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(9);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_BREAK);
	}

	@Test
	@DisplayName("From ID - Valid duration based break ID")
	void testFromIdValidDurationBasedBreakIdReturnsRuleType() {
		// Act
		RuleType ruleType = RuleType.fromId(10);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_BREAK);
	}

	@Test
	@DisplayName("From ID - Invalid ID throws IllegalArgumentException")
	void testFromIdInvalidIdThrowsIllegalArgumentException() {
		// Act & Assert
		assertThatThrownBy(() -> RuleType.fromId(999)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid RuleType id: 999");
	}

	@Test
	@DisplayName("From ID - Valid range based default pay ID")
	void testFromIdValidRangeBasedDefaultPayIdReturnsRuleType() {
		RuleType ruleType = RuleType.fromId(13);

		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_DEFAULT_PAY);
	}

	@Test
	@DisplayName("From ID - Valid duration based default pay ID")
	void testFromIdValidDurationBasedDefaultPayIdReturnsRuleType() {
		RuleType ruleType = RuleType.fromId(14);

		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_DEFAULT_PAY);
	}

}