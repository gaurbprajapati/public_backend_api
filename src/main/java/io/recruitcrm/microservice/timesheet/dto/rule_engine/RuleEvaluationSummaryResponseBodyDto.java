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

/**
 * Response body DTO for rule evaluation summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationSummaryResponseBodyDto {

	@NotNull(message = "Total weeks evaluated cannot be null")
	private Integer totalWeeksEvaluated;

	@NotNull(message = "Total rule evaluations cannot be null")
	private Integer totalRuleEvaluations;

	@PositiveOrZero(message = "Total bill amount must be zero or positive")
	private BigDecimal totalBillAmount;

	@PositiveOrZero(message = "Total pay amount must be zero or positive")
	private BigDecimal totalPayAmount;

}