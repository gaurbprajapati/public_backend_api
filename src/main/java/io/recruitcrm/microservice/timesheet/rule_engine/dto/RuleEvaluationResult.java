/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import com.google.common.collect.RangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Result of a single rule evaluation Contains the outcome of applying a specific rule to
 * time ranges
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResult {

	/**
	 * The time ranges that were evaluated by this rule
	 */
	@NotNull(message = "Time range cannot be null")
	private RangeSet<LocalTime> timeRange;

	/**
	 * The type of rule that was evaluated
	 */
	@NotNull(message = "Rule type cannot be null")
	private RuleType ruleType;

	/**
	 * Name of the rule that was evaluated
	 */
	private String ruleName;

	/**
	 * Weekly overtime hours calculated (if applicable)
	 */
	private Duration weeklyOvertimeHours;

	/**
	 * Bill amount for the evaluated time ranges
	 */
	@PositiveOrZero(message = "Bill amount must be zero or positive")
	private BigDecimal billAmount;

	/**
	 * Pay amount for the evaluated time ranges
	 */
	@PositiveOrZero(message = "Pay amount must be zero or positive")
	private BigDecimal payAmount;

	/**
	 * Pay rate multiplier of the rule that produced this result (null for virtual rules
	 * and FIXED_RATE charge-method rules)
	 */
	private Float payRateMultiplier;

	/**
	 * Bill rate multiplier of the rule that produced this result (null for virtual rules
	 * and FIXED_RATE charge-method rules)
	 */
	private Float billRateMultiplier;

	/**
	 * Duration of the evaluated time ranges
	 */
	private Duration evaluatedDuration;

	/**
	 * Whether the rule evaluation was successful
	 */
	@Builder.Default
	private boolean successful = true;

	/**
	 * Error message if the evaluation failed
	 */
	private String errorMessage;

	/**
	 * Additional metadata about the evaluation
	 */
	private String metadata;

	/**
	 * The date when this rule was evaluated
	 */
	private java.time.LocalDate evaluationDate;

	/**
	 * The index of the rule in the evaluation sequence
	 */
	private Integer ruleIndex;

	/**
	 * Indicates whether this is a virtual/system rule (e.g., Regular Hours, Break)
	 */
	@Builder.Default
	private boolean virtualRule = false;

	/**
	 * Calculates the total duration from the time range
	 * @return the total duration, or null if time range is empty
	 */
	public Duration calculateDuration() {
		if (this.timeRange == null || this.timeRange.isEmpty()) {
			return Duration.ZERO;
		}

		return this.timeRange.asRanges()
			.stream()
			.map((range) -> Duration.between(range.lowerEndpoint(), range.upperEndpoint()))
			.reduce(Duration.ZERO, Duration::plus);
	}

	/**
	 * Checks if this result has monetary values
	 * @return true if either bill or pay amount is present
	 */
	public boolean hasMonetaryValues() {
		return (this.billAmount != null && this.billAmount.compareTo(BigDecimal.ZERO) > 0)
				|| (this.payAmount != null && this.payAmount.compareTo(BigDecimal.ZERO) > 0);
	}

	/**
	 * Gets the total monetary value (bill + pay)
	 * @return total monetary value, or zero if no values present
	 */
	public BigDecimal getTotalMonetaryValue() {
		BigDecimal total = BigDecimal.ZERO;

		if (this.billAmount != null) {
			total = total.add(this.billAmount);
		}

		if (this.payAmount != null) {
			total = total.add(this.payAmount);
		}

		return total;
	}

	/**
	 * Creates a copy of this result with updated monetary values
	 * @param newBillAmount new bill amount
	 * @param newPayAmount new pay amount
	 * @return new RuleEvaluationResult with updated values
	 */
	public RuleEvaluationResult withUpdatedMonetaryValues(BigDecimal newBillAmount, BigDecimal newPayAmount) {
		return RuleEvaluationResult.builder()
			.timeRange(this.timeRange)
			.ruleType(this.ruleType)
			.ruleName(this.ruleName)
			.weeklyOvertimeHours(this.weeklyOvertimeHours)
			.billAmount(newBillAmount)
			.payAmount(newPayAmount)
			.payRateMultiplier(this.payRateMultiplier)
			.billRateMultiplier(this.billRateMultiplier)
			.evaluatedDuration(this.evaluatedDuration)
			.successful(this.successful)
			.errorMessage(this.errorMessage)
			.metadata(this.metadata)
			.evaluationDate(this.evaluationDate)
			.ruleIndex(this.ruleIndex)
			.virtualRule(this.virtualRule)
			.build();
	}

	/**
	 * Creates a copy of this result with updated monetary values and weekly overtime
	 * hours
	 * @param newBillAmount new bill amount
	 * @param newPayAmount new pay amount
	 * @param newWeeklyOvertimeHours new weekly overtime hours
	 * @return new RuleEvaluationResult with updated values
	 */
	public RuleEvaluationResult withUpdatedValues(BigDecimal newBillAmount, BigDecimal newPayAmount,
			Duration newWeeklyOvertimeHours) {
		return RuleEvaluationResult.builder()
			.timeRange(this.timeRange)
			.ruleType(this.ruleType)
			.ruleName(this.ruleName)
			.weeklyOvertimeHours(newWeeklyOvertimeHours)
			.billAmount(newBillAmount)
			.payAmount(newPayAmount)
			.payRateMultiplier(this.payRateMultiplier)
			.billRateMultiplier(this.billRateMultiplier)
			.evaluatedDuration(this.evaluatedDuration)
			.successful(this.successful)
			.errorMessage(this.errorMessage)
			.metadata(this.metadata)
			.evaluationDate(this.evaluationDate)
			.ruleIndex(this.ruleIndex)
			.virtualRule(this.virtualRule)
			.build();
	}

	public boolean hasTimeRangeOverlap() {
		return this.timeRange != null && !this.timeRange.isEmpty();
	}

	public BigDecimal getTotalBillAmount() {
		return (this.billAmount != null) ? this.billAmount : BigDecimal.ZERO;
	}

	public BigDecimal getTotalPayAmount() {
		return (this.payAmount != null) ? this.payAmount : BigDecimal.ZERO;
	}

}
