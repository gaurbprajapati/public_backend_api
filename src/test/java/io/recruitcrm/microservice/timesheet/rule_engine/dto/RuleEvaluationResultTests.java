/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEvaluationResultTests {

	@Test
	@DisplayName("Default constructor - Success")
	void testDefaultConstructorSuccess() {
		// Act
		RuleEvaluationResult result = new RuleEvaluationResult();

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimeRange()).isNull();
		assertThat(result.getRuleType()).isNull();
		assertThat(result.getRuleName()).isNull();
		assertThat(result.getWeeklyOvertimeHours()).isNull();
		assertThat(result.getBillAmount()).isNull();
		assertThat(result.getPayAmount()).isNull();
		assertThat(result.getEvaluatedDuration()).isNull();
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.getErrorMessage()).isNull();
		assertThat(result.getMetadata()).isNull();
		assertThat(result.getEvaluationDate()).isNull();
		assertThat(result.getRuleIndex()).isNull();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("All args constructor - Success")
	void testAllArgsConstructorSuccess() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		RuleType ruleType = RuleType.RANGE_BASED_REGULAR_HOURS;
		String ruleName = "Regular Hours Rule";
		Duration weeklyOvertimeHours = Duration.ofHours(2);
		BigDecimal billAmount = new BigDecimal("100.00");
		BigDecimal payAmount = new BigDecimal("50.00");
		Duration evaluatedDuration = Duration.ofHours(8);
		boolean successful = true;
		String errorMessage = "No errors";
		String metadata = "Test metadata";
		LocalDate evaluationDate = LocalDate.now();
		Integer ruleIndex = 1;
		boolean virtualRule = false;

		Float payRateMultiplier = 1.5f;
		Float billRateMultiplier = 2.0f;

		// Act
		RuleEvaluationResult result = new RuleEvaluationResult(timeRange, ruleType, ruleName, weeklyOvertimeHours,
				billAmount, payAmount, payRateMultiplier, billRateMultiplier, evaluatedDuration, successful,
				errorMessage, metadata, evaluationDate, ruleIndex, virtualRule);

		// Assert
		assertThat(result.getPayRateMultiplier()).isEqualTo(payRateMultiplier);
		assertThat(result.getBillRateMultiplier()).isEqualTo(billRateMultiplier);
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getRuleType()).isEqualTo(ruleType);
		assertThat(result.getRuleName()).isEqualTo(ruleName);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(weeklyOvertimeHours);
		assertThat(result.getBillAmount()).isEqualTo(billAmount);
		assertThat(result.getPayAmount()).isEqualTo(payAmount);
		assertThat(result.getEvaluatedDuration()).isEqualTo(evaluatedDuration);
		assertThat(result.isSuccessful()).isEqualTo(successful);
		assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
		assertThat(result.getMetadata()).isEqualTo(metadata);
		assertThat(result.getEvaluationDate()).isEqualTo(evaluationDate);
		assertThat(result.getRuleIndex()).isEqualTo(ruleIndex);
		assertThat(result.isVirtualRule()).isEqualTo(virtualRule);
	}

	@Test
	@DisplayName("Builder pattern - Success")
	void testBuilderPatternSuccess() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(10, 0), LocalTime.of(18, 0)));
		RuleType ruleType = RuleType.DURATION_BASED_BREAK;
		String ruleName = "Break Rule";

		// Act
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(ruleType)
			.ruleName(ruleName)
			.successful(true)
			.virtualRule(true)
			.build();

		// Assert
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getRuleType()).isEqualTo(ruleType);
		assertThat(result.getRuleName()).isEqualTo(ruleName);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Setters and getters - All fields")
	void testSettersAndGettersAllFields() {
		// Arrange
		RuleEvaluationResult result = new RuleEvaluationResult();
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(8, 0), LocalTime.of(16, 0)));
		RuleType ruleType = RuleType.RANGE_BASED_DAILY_OVERTIME;
		String ruleName = "Overtime Rule";
		Duration weeklyOvertimeHours = Duration.ofHours(5);
		BigDecimal billAmount = new BigDecimal("150.00");
		BigDecimal payAmount = new BigDecimal("75.00");
		Duration evaluatedDuration = Duration.ofHours(10);
		boolean successful = false;
		String errorMessage = "Evaluation failed";
		String metadata = "Error metadata";
		LocalDate evaluationDate = LocalDate.of(2024, 1, 15);
		Integer ruleIndex = 2;
		boolean virtualRule = true;

		// Act
		result.setTimeRange(timeRange);
		result.setRuleType(ruleType);
		result.setRuleName(ruleName);
		result.setWeeklyOvertimeHours(weeklyOvertimeHours);
		result.setBillAmount(billAmount);
		result.setPayAmount(payAmount);
		result.setEvaluatedDuration(evaluatedDuration);
		result.setSuccessful(successful);
		result.setErrorMessage(errorMessage);
		result.setMetadata(metadata);
		result.setEvaluationDate(evaluationDate);
		result.setRuleIndex(ruleIndex);
		result.setVirtualRule(virtualRule);

		// Assert
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getRuleType()).isEqualTo(ruleType);
		assertThat(result.getRuleName()).isEqualTo(ruleName);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(weeklyOvertimeHours);
		assertThat(result.getBillAmount()).isEqualTo(billAmount);
		assertThat(result.getPayAmount()).isEqualTo(payAmount);
		assertThat(result.getEvaluatedDuration()).isEqualTo(evaluatedDuration);
		assertThat(result.isSuccessful()).isEqualTo(successful);
		assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
		assertThat(result.getMetadata()).isEqualTo(metadata);
		assertThat(result.getEvaluationDate()).isEqualTo(evaluationDate);
		assertThat(result.getRuleIndex()).isEqualTo(ruleIndex);
		assertThat(result.isVirtualRule()).isEqualTo(virtualRule);
	}

	@Test
	@DisplayName("Calculate duration - Valid time range")
	void testCalculateDurationValidTimeRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		timeRange.add(Range.closed(LocalTime.of(18, 0), LocalTime.of(20, 0)));

		RuleEvaluationResult result = RuleEvaluationResult.builder().timeRange(timeRange).build();

		// Act
		Duration duration = result.calculateDuration();

		// Assert
		assertThat(duration).isEqualTo(Duration.ofHours(10)); // 8 hours + 2 hours
	}

	@Test
	@DisplayName("Calculate duration - Null time range")
	void testCalculateDurationNullTimeRange() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().timeRange(null).build();

		// Act
		Duration duration = result.calculateDuration();

		// Assert
		assertThat(duration).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("Calculate duration - Empty time range")
	void testCalculateDurationEmptyTimeRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		RuleEvaluationResult result = RuleEvaluationResult.builder().timeRange(timeRange).build();

		// Act
		Duration duration = result.calculateDuration();

		// Assert
		assertThat(duration).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("Has monetary values - Both amounts present")
	void testHasMonetaryValuesBothAmountsPresent() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(new BigDecimal("100.00"))
			.payAmount(new BigDecimal("50.00"))
			.build();

		// Act
		boolean hasMonetaryValues = result.hasMonetaryValues();

		// Assert
		assertThat(hasMonetaryValues).isTrue();
	}

	@Test
	@DisplayName("Has monetary values - Only bill amount")
	void testHasMonetaryValuesOnlyBillAmount() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(new BigDecimal("100.00"))
			.payAmount(BigDecimal.ZERO)
			.build();

		// Act
		boolean hasMonetaryValues = result.hasMonetaryValues();

		// Assert
		assertThat(hasMonetaryValues).isTrue();
	}

	@Test
	@DisplayName("Has monetary values - Only pay amount")
	void testHasMonetaryValuesOnlyPayAmount() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(BigDecimal.ZERO)
			.payAmount(new BigDecimal("50.00"))
			.build();

		// Act
		boolean hasMonetaryValues = result.hasMonetaryValues();

		// Assert
		assertThat(hasMonetaryValues).isTrue();
	}

	@Test
	@DisplayName("Has monetary values - No amounts")
	void testHasMonetaryValuesNoAmounts() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(BigDecimal.ZERO)
			.payAmount(BigDecimal.ZERO)
			.build();

		// Act
		boolean hasMonetaryValues = result.hasMonetaryValues();

		// Assert
		assertThat(hasMonetaryValues).isFalse();
	}

	@Test
	@DisplayName("Has monetary values - Null amounts")
	void testHasMonetaryValuesNullAmounts() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().billAmount(null).payAmount(null).build();

		// Act
		boolean hasMonetaryValues = result.hasMonetaryValues();

		// Assert
		assertThat(hasMonetaryValues).isFalse();
	}

	@Test
	@DisplayName("Get total monetary value - Both amounts present")
	void testGetTotalMonetaryValueBothAmountsPresent() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(new BigDecimal("100.00"))
			.payAmount(new BigDecimal("50.00"))
			.build();

		// Act
		BigDecimal total = result.getTotalMonetaryValue();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("150.00"));
	}

	@Test
	@DisplayName("Get total monetary value - Only bill amount")
	void testGetTotalMonetaryValueOnlyBillAmount() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(new BigDecimal("100.00"))
			.payAmount(null)
			.build();

		// Act
		BigDecimal total = result.getTotalMonetaryValue();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("100.00"));
	}

	@Test
	@DisplayName("Get total monetary value - Only pay amount")
	void testGetTotalMonetaryValueOnlyPayAmount() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder()
			.billAmount(null)
			.payAmount(new BigDecimal("50.00"))
			.build();

		// Act
		BigDecimal total = result.getTotalMonetaryValue();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("50.00"));
	}

	@Test
	@DisplayName("Get total monetary value - No amounts")
	void testGetTotalMonetaryValueNoAmounts() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().billAmount(null).payAmount(null).build();

		// Act
		BigDecimal total = result.getTotalMonetaryValue();

		// Assert
		assertThat(total).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("With updated monetary values - Success")
	void testWithUpdatedMonetaryValuesSuccess() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		RuleType ruleType = RuleType.RANGE_BASED_REGULAR_HOURS;
		String ruleName = "Regular Hours";
		Duration weeklyOvertimeHours = Duration.ofHours(2);
		BigDecimal originalBillAmount = new BigDecimal("100.00");
		BigDecimal originalPayAmount = new BigDecimal("50.00");
		Duration evaluatedDuration = Duration.ofHours(8);
		boolean successful = true;
		String errorMessage = "No errors";
		String metadata = "Test metadata";
		LocalDate evaluationDate = LocalDate.now();
		Integer ruleIndex = 1;
		boolean virtualRule = false;

		RuleEvaluationResult original = RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(ruleType)
			.ruleName(ruleName)
			.weeklyOvertimeHours(weeklyOvertimeHours)
			.billAmount(originalBillAmount)
			.payAmount(originalPayAmount)
			.evaluatedDuration(evaluatedDuration)
			.successful(successful)
			.errorMessage(errorMessage)
			.metadata(metadata)
			.evaluationDate(evaluationDate)
			.ruleIndex(ruleIndex)
			.virtualRule(virtualRule)
			.build();

		BigDecimal newBillAmount = new BigDecimal("150.00");
		BigDecimal newPayAmount = new BigDecimal("75.00");

		// Act
		RuleEvaluationResult updated = original.withUpdatedMonetaryValues(newBillAmount, newPayAmount);

		// Assert
		assertThat(updated.getTimeRange()).isEqualTo(timeRange);
		assertThat(updated.getRuleType()).isEqualTo(ruleType);
		assertThat(updated.getRuleName()).isEqualTo(ruleName);
		assertThat(updated.getWeeklyOvertimeHours()).isEqualTo(weeklyOvertimeHours);
		assertThat(updated.getBillAmount()).isEqualTo(newBillAmount);
		assertThat(updated.getPayAmount()).isEqualTo(newPayAmount);
		assertThat(updated.getEvaluatedDuration()).isEqualTo(evaluatedDuration);
		assertThat(updated.isSuccessful()).isEqualTo(successful);
		assertThat(updated.getErrorMessage()).isEqualTo(errorMessage);
		assertThat(updated.getMetadata()).isEqualTo(metadata);
		assertThat(updated.getEvaluationDate()).isEqualTo(evaluationDate);
		assertThat(updated.getRuleIndex()).isEqualTo(ruleIndex);
		assertThat(updated.isVirtualRule()).isEqualTo(virtualRule);
	}

	@Test
	@DisplayName("With updated values - Success")
	void testWithUpdatedValuesSuccess() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		RuleType ruleType = RuleType.RANGE_BASED_REGULAR_HOURS;
		String ruleName = "Regular Hours";
		Duration originalWeeklyOvertimeHours = Duration.ofHours(2);
		BigDecimal originalBillAmount = new BigDecimal("100.00");
		BigDecimal originalPayAmount = new BigDecimal("50.00");
		Duration evaluatedDuration = Duration.ofHours(8);
		boolean successful = true;
		String errorMessage = "No errors";
		String metadata = "Test metadata";
		LocalDate evaluationDate = LocalDate.now();
		Integer ruleIndex = 1;
		boolean virtualRule = false;

		RuleEvaluationResult original = RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(ruleType)
			.ruleName(ruleName)
			.weeklyOvertimeHours(originalWeeklyOvertimeHours)
			.billAmount(originalBillAmount)
			.payAmount(originalPayAmount)
			.evaluatedDuration(evaluatedDuration)
			.successful(successful)
			.errorMessage(errorMessage)
			.metadata(metadata)
			.evaluationDate(evaluationDate)
			.ruleIndex(ruleIndex)
			.virtualRule(virtualRule)
			.build();

		BigDecimal newBillAmount = new BigDecimal("150.00");
		BigDecimal newPayAmount = new BigDecimal("75.00");
		Duration newWeeklyOvertimeHours = Duration.ofHours(5);

		// Act
		RuleEvaluationResult updated = original.withUpdatedValues(newBillAmount, newPayAmount, newWeeklyOvertimeHours);

		// Assert
		assertThat(updated.getTimeRange()).isEqualTo(timeRange);
		assertThat(updated.getRuleType()).isEqualTo(ruleType);
		assertThat(updated.getRuleName()).isEqualTo(ruleName);
		assertThat(updated.getWeeklyOvertimeHours()).isEqualTo(newWeeklyOvertimeHours);
		assertThat(updated.getBillAmount()).isEqualTo(newBillAmount);
		assertThat(updated.getPayAmount()).isEqualTo(newPayAmount);
		assertThat(updated.getEvaluatedDuration()).isEqualTo(evaluatedDuration);
		assertThat(updated.isSuccessful()).isEqualTo(successful);
		assertThat(updated.getErrorMessage()).isEqualTo(errorMessage);
		assertThat(updated.getMetadata()).isEqualTo(metadata);
		assertThat(updated.getEvaluationDate()).isEqualTo(evaluationDate);
		assertThat(updated.getRuleIndex()).isEqualTo(ruleIndex);
		assertThat(updated.isVirtualRule()).isEqualTo(virtualRule);
	}

	@Test
	@DisplayName("Has time range overlap - Valid time range")
	void testHasTimeRangeOverlapValidTimeRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		RuleEvaluationResult result = RuleEvaluationResult.builder().timeRange(timeRange).build();

		// Act
		boolean hasOverlap = result.hasTimeRangeOverlap();

		// Assert
		assertThat(hasOverlap).isTrue();
	}

	@Test
	@DisplayName("Has time range overlap - Null time range")
	void testHasTimeRangeOverlapNullTimeRange() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().timeRange(null).build();

		// Act
		boolean hasOverlap = result.hasTimeRangeOverlap();

		// Assert
		assertThat(hasOverlap).isFalse();
	}

	@Test
	@DisplayName("Has time range overlap - Empty time range")
	void testHasTimeRangeOverlapEmptyTimeRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		RuleEvaluationResult result = RuleEvaluationResult.builder().timeRange(timeRange).build();

		// Act
		boolean hasOverlap = result.hasTimeRangeOverlap();

		// Assert
		assertThat(hasOverlap).isFalse();
	}

	@Test
	@DisplayName("Get total bill amount - Valid amount")
	void testGetTotalBillAmountValidAmount() {
		// Arrange
		BigDecimal billAmount = new BigDecimal("100.00");
		RuleEvaluationResult result = RuleEvaluationResult.builder().billAmount(billAmount).build();

		// Act
		BigDecimal total = result.getTotalBillAmount();

		// Assert
		assertThat(total).isEqualTo(billAmount);
	}

	@Test
	@DisplayName("Get total bill amount - Null amount")
	void testGetTotalBillAmountNullAmount() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().billAmount(null).build();

		// Act
		BigDecimal total = result.getTotalBillAmount();

		// Assert
		assertThat(total).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Get total pay amount - Valid amount")
	void testGetTotalPayAmountValidAmount() {
		// Arrange
		BigDecimal payAmount = new BigDecimal("50.00");
		RuleEvaluationResult result = RuleEvaluationResult.builder().payAmount(payAmount).build();

		// Act
		BigDecimal total = result.getTotalPayAmount();

		// Assert
		assertThat(total).isEqualTo(payAmount);
	}

	@Test
	@DisplayName("Get total pay amount - Null amount")
	void testGetTotalPayAmountNullAmount() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().payAmount(null).build();

		// Act
		BigDecimal total = result.getTotalPayAmount();

		// Assert
		assertThat(total).isEqualTo(BigDecimal.ZERO);
	}

}