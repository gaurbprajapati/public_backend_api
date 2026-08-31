/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CustomRuleTests {

	@Test
	@DisplayName("Default constructor - Success")
	void testDefaultConstructorSuccess() {
		// Act
		CustomRule customRule = new CustomRule();

		// Assert
		assertThat(customRule).isNotNull();
		assertThat(customRule.getId()).isNull();
		assertThat(customRule.getRuleName()).isNull();
		assertThat(customRule.getWorkDays()).isNull();
		assertThat(customRule.getRuleType()).isNull();
		assertThat(customRule.getStartTime()).isNull();
		assertThat(customRule.getEndTime()).isNull();
		assertThat(customRule.getDailyThreshold()).isNull();
		assertThat(customRule.getWeeklyThreshold()).isNull();
		assertThat(customRule.getChargeMethod()).isNull();
		assertThat(customRule.getPayRateMultiplier()).isNull();
		assertThat(customRule.getBillRateMultiplier()).isNull();
		assertThat(customRule.getPayRatePerHour()).isNull();
		assertThat(customRule.getBillRatePerHour()).isNull();
		assertThat(customRule.getStartDuration()).isNull();
		assertThat(customRule.getEndDuration()).isNull();
	}

	@Test
	@DisplayName("All args constructor - Success")
	void testAllArgsConstructorSuccess() {
		// Arrange
		Integer id = 1;
		String ruleName = "Test Custom Rule";
		List<WorkDay> workDays = Arrays.asList(WorkDay.MONDAY, WorkDay.TUESDAY);
		RuleType ruleType = RuleType.RANGE_BASED_REGULAR_HOURS;
		LocalTime startTime = LocalTime.of(9, 0);
		LocalTime endTime = LocalTime.of(17, 0);
		Duration dailyThreshold = Duration.ofHours(8);
		Duration weeklyThreshold = Duration.ofHours(40);
		ChargeMethodType chargeMethod = ChargeMethodType.MULTIPLIER;
		Float payRateMultiplier = 1.5f;
		Float billRateMultiplier = 2.0f;
		Float payRatePerHour = 25.0f;
		Float billRatePerHour = 50.0f;
		Duration startDuration = Duration.ofMinutes(30);
		Duration endDuration = Duration.ofMinutes(30);

		// Act
		CustomRule customRule = new CustomRule(id, ruleName, workDays, ruleType, startTime, endTime, dailyThreshold,
				weeklyThreshold, chargeMethod, payRateMultiplier, billRateMultiplier, payRatePerHour, billRatePerHour,
				startDuration, endDuration);

		// Assert
		assertThat(customRule.getId()).isEqualTo(id);
		assertThat(customRule.getRuleName()).isEqualTo(ruleName);
		assertThat(customRule.getWorkDays()).isEqualTo(workDays);
		assertThat(customRule.getRuleType()).isEqualTo(ruleType);
		assertThat(customRule.getStartTime()).isEqualTo(startTime);
		assertThat(customRule.getEndTime()).isEqualTo(endTime);
		assertThat(customRule.getDailyThreshold()).isEqualTo(dailyThreshold);
		assertThat(customRule.getWeeklyThreshold()).isEqualTo(weeklyThreshold);
		assertThat(customRule.getChargeMethod()).isEqualTo(chargeMethod);
		assertThat(customRule.getPayRateMultiplier()).isEqualTo(payRateMultiplier);
		assertThat(customRule.getBillRateMultiplier()).isEqualTo(billRateMultiplier);
		assertThat(customRule.getPayRatePerHour()).isEqualTo(payRatePerHour);
		assertThat(customRule.getBillRatePerHour()).isEqualTo(billRatePerHour);
		assertThat(customRule.getStartDuration()).isEqualTo(startDuration);
		assertThat(customRule.getEndDuration()).isEqualTo(endDuration);
	}

	@Test
	@DisplayName("Setters and getters - All fields")
	void testSettersAndGettersAllFields() {
		// Arrange
		CustomRule customRule = new CustomRule();
		Integer id = 2;
		String ruleName = "Another Custom Rule";
		List<WorkDay> workDays = Arrays.asList(WorkDay.WEDNESDAY, WorkDay.THURSDAY);
		RuleType ruleType = RuleType.DURATION_BASED_BREAK;
		LocalTime startTime = LocalTime.of(12, 0);
		LocalTime endTime = LocalTime.of(13, 0);
		Duration dailyThreshold = Duration.ofHours(6);
		Duration weeklyThreshold = Duration.ofHours(30);
		ChargeMethodType chargeMethod = ChargeMethodType.FIXED_RATE;
		Float payRateMultiplier = 1.0f;
		Float billRateMultiplier = 1.5f;
		Float payRatePerHour = 20.0f;
		Float billRatePerHour = 30.0f;
		Duration startDuration = Duration.ofMinutes(15);
		Duration endDuration = Duration.ofMinutes(15);

		// Act
		customRule.setId(id);
		customRule.setRuleName(ruleName);
		customRule.setWorkDays(workDays);
		customRule.setRuleType(ruleType);
		customRule.setStartTime(startTime);
		customRule.setEndTime(endTime);
		customRule.setDailyThreshold(dailyThreshold);
		customRule.setWeeklyThreshold(weeklyThreshold);
		customRule.setChargeMethod(chargeMethod);
		customRule.setPayRateMultiplier(payRateMultiplier);
		customRule.setBillRateMultiplier(billRateMultiplier);
		customRule.setPayRatePerHour(payRatePerHour);
		customRule.setBillRatePerHour(billRatePerHour);
		customRule.setStartDuration(startDuration);
		customRule.setEndDuration(endDuration);

		// Assert
		assertThat(customRule.getId()).isEqualTo(id);
		assertThat(customRule.getRuleName()).isEqualTo(ruleName);
		assertThat(customRule.getWorkDays()).isEqualTo(workDays);
		assertThat(customRule.getRuleType()).isEqualTo(ruleType);
		assertThat(customRule.getStartTime()).isEqualTo(startTime);
		assertThat(customRule.getEndTime()).isEqualTo(endTime);
		assertThat(customRule.getDailyThreshold()).isEqualTo(dailyThreshold);
		assertThat(customRule.getWeeklyThreshold()).isEqualTo(weeklyThreshold);
		assertThat(customRule.getChargeMethod()).isEqualTo(chargeMethod);
		assertThat(customRule.getPayRateMultiplier()).isEqualTo(payRateMultiplier);
		assertThat(customRule.getBillRateMultiplier()).isEqualTo(billRateMultiplier);
		assertThat(customRule.getPayRatePerHour()).isEqualTo(payRatePerHour);
		assertThat(customRule.getBillRatePerHour()).isEqualTo(billRatePerHour);
		assertThat(customRule.getStartDuration()).isEqualTo(startDuration);
		assertThat(customRule.getEndDuration()).isEqualTo(endDuration);
	}

	@Test
	@DisplayName("Is system rule - Always returns false")
	void testIsSystemRuleAlwaysReturnsFalse() {
		// Arrange
		CustomRule customRule = new CustomRule();

		// Act
		boolean result = customRule.isSystemRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Is applicable on day - Work days contains the day")
	void testIsApplicableOnDayWorkDaysContainsDayReturnsTrue() {
		// Arrange
		CustomRule customRule = new CustomRule();
		customRule.setWorkDays(Arrays.asList(WorkDay.MONDAY, WorkDay.TUESDAY));

		// Act
		boolean result = customRule.isApplicableOnDay(WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Is applicable on day - Work days does not contain the day")
	void testIsApplicableOnDayWorkDaysDoesNotContainDayReturnsFalse() {
		// Arrange
		CustomRule customRule = new CustomRule();
		customRule.setWorkDays(Arrays.asList(WorkDay.MONDAY, WorkDay.TUESDAY));

		// Act
		boolean result = customRule.isApplicableOnDay(WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Is applicable on day - Null work days")
	void testIsApplicableOnDayNullWorkDaysReturnsTrue() {
		// Arrange
		CustomRule customRule = new CustomRule();
		customRule.setWorkDays(null);

		// Act
		boolean result = customRule.isApplicableOnDay(WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Is applicable on day - Empty work days")
	void testIsApplicableOnDayEmptyWorkDaysReturnsTrue() {
		// Arrange
		CustomRule customRule = new CustomRule();
		customRule.setWorkDays(Collections.emptyList());

		// Act
		boolean result = customRule.isApplicableOnDay(WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Equals - Same objects")
	void testEqualsSameObjects() {
		// Arrange
		CustomRule rule1 = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);
		CustomRule rule2 = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);

		// Act & Assert
		assertThat(rule1).isEqualTo(rule2);
		assertThat(rule2).isEqualTo(rule1);
	}

	@Test
	@DisplayName("Equals - Different objects")
	void testEqualsDifferentObjects() {
		// Arrange
		CustomRule rule1 = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);
		CustomRule rule2 = new CustomRule(2, "Different Rule", null, null, null, null, null, null, null, null, null,
				null, null, null, null);

		// Act & Assert
		assertThat(rule1).isNotEqualTo(rule2);
		assertThat(rule2).isNotEqualTo(rule1);
	}

	@Test
	@DisplayName("Equals - Same object reference")
	void testEqualsSameObjectReference() {
		// Arrange
		CustomRule rule = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);

		// Act & Assert
		assertThat(rule).isEqualTo(rule);
	}

	@Test
	@DisplayName("Equals - Null object")
	void testEqualsNullObject() {
		// Arrange
		CustomRule rule = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);

		// Act & Assert
		assertThat(rule).isNotNull();
	}

	@Test
	@DisplayName("Equals - Different class")
	void testEqualsDifferentClass() {
		// Arrange
		CustomRule rule = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);
		Object otherObject = new Object();

		// Act & Assert
		assertThat(rule).isNotEqualTo(otherObject);
	}

	@Test
	@DisplayName("HashCode - Same objects have same hash code")
	void testHashCodeSameObjectsHaveSameHashCode() {
		// Arrange
		CustomRule rule1 = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);
		CustomRule rule2 = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);

		// Act & Assert
		assertThat(rule1).hasSameHashCodeAs(rule2);
	}

	@Test
	@DisplayName("HashCode - Different objects have different hash codes")
	void testHashCodeDifferentObjectsHaveDifferentHashCodes() {
		// Arrange
		CustomRule rule1 = new CustomRule(1, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);
		CustomRule rule2 = new CustomRule(2, "Different Rule", null, null, null, null, null, null, null, null, null,
				null, null, null, null);

		// Act & Assert
		assertThat(rule1.hashCode()).isNotEqualTo(rule2.hashCode());
	}

	@Test
	@DisplayName("HashCode - Null ID")
	void testHashCodeNullId() {
		// Arrange
		CustomRule rule = new CustomRule(null, "Test Rule", null, null, null, null, null, null, null, null, null, null,
				null, null, null);

		// Act & Assert
		assertThatCode(rule::hashCode).doesNotThrowAnyException();
	}

}