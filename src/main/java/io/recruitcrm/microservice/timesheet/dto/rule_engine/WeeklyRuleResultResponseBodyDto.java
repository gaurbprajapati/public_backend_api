/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Response body DTO for weekly rule evaluation results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyRuleResultResponseBodyDto {

	@NotNull(message = "Week start date cannot be null")
	private LocalDate weekStartDate;

	@NotNull(message = "Week end date cannot be null")
	private LocalDate weekEndDate;

	@NotNull(message = "Time log rule evaluation results cannot be null")
	private List<TimeLogRuleEvaluationResponseBodyDto> timeLogRuleEvaluations;

	@NotNull(message = "Weekly money data cannot be null")
	private MoneyDataResponseBodyDto weeklyMoneyData;

	@NotNull(message = "Weekly overtime summary cannot be null")
	private WeeklyOvertimeSummaryResponseBodyDto weeklyOvertimeResult;

}