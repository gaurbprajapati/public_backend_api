/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response body DTO for rule engine evaluation results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleEngineResponseBodyDto {

	@NotNull(message = "Timesheet ID cannot be null")
	@Positive(message = "Timesheet ID must be a positive number")
	private Integer timesheetId;

	@NotNull(message = "Weekly results cannot be null")
	private List<WeeklyRuleResultResponseBodyDto> weeklyResults;

	@NotNull(message = "Total evaluation summary cannot be null")
	private RuleEvaluationSummaryResponseBodyDto evaluationSummary;

}