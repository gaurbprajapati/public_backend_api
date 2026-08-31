/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
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
 * Response body DTO for individual rule evaluation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResultResponseBodyDto {

	@NotNull(message = "Time ranges cannot be null")
	private List<List<LocalTime>> timeRanges;

	@NotNull(message = "Rule type cannot be null")
	private RuleType ruleType;

	private String ruleName;

	@PositiveOrZero(message = "Bill amount must be zero or positive")
	private BigDecimal billAmount;

	@PositiveOrZero(message = "Pay amount must be zero or positive")
	private BigDecimal payAmount;

	@PositiveOrZero(message = "Evaluated duration must be zero or positive")
	private Long evaluatedDurationInSeconds;

	/**
	 * Approximate hours for the evaluated duration (evaluatedDurationInSeconds / 60.0)
	 */
	private Float evaluatedDurationApproximateHours;

	private boolean successful;

	private String errorMessage;

	// Split metadata fields
	private LocalDate evaluationDate;

	private Integer ruleIndex;

	// Indicates whether this is a virtual/system rule (e.g., Regular Hours, Break)
	private boolean virtualRule;

	// Keep original metadata for backward compatibility
	private String metadata;

}