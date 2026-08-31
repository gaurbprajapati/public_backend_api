/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response body DTO for timelog rule evaluation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogRuleEvaluationResponseBodyDto {

	@NotNull(message = "Time log ID cannot be null")
	private Integer timeLogId;

	@NotNull(message = "Date cannot be null")
	private LocalDate date;

	private LocalTime startTime;

	private LocalTime endTime;

	/**
	 * Normalized start time for duration-based time logging. This field contains the
	 * calculated start time based on work duration and rule evaluation.
	 */
	private LocalTime normalizedStartTime;

	/**
	 * Normalized end time for duration-based time logging. This field contains the
	 * calculated end time based on work duration and rule evaluation.
	 */
	private LocalTime normalizedEndTime;

	@NotNull(message = "Rule evaluation results cannot be null")
	private List<RuleEvaluationResultResponseBodyDto> ruleEvaluationResults;

	@PositiveOrZero(message = "Total pay amount must be zero or positive")
	private BigDecimal totalPayAmount;

	@PositiveOrZero(message = "Total bill amount must be zero or positive")
	private BigDecimal totalBillAmount;

	/**
	 * Approximate hours for actual work duration (duration between startTime and endTime)
	 */
	private Float actualWorkDurationApproximateHours;

	/**
	 * Approximate hours for normalized work duration (duration between
	 * normalizedStartTime and normalizedEndTime)
	 */
	private Float normalizedWorkDurationApproximateHours;

	/**
	 * Unallocated time ranges that are not covered by any rules
	 */
	private List<List<LocalTime>> unallocatedTimeRanges;

	/**
	 * Approximate hours of unallocated time
	 */
	private Float unallocatedTimeApproximateHours;

}