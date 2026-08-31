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
 * Response body DTO for weekly overtime summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyOvertimeSummaryResponseBodyDto {

	@NotNull(message = "Weekly overtime hours cannot be null")
	@PositiveOrZero(message = "Weekly overtime hours must be zero or positive")
	private Long weeklyOvertimeHoursInSeconds;

	@PositiveOrZero(message = "Weekly overtime hours in approximate hours must be zero or positive")
	private Float weeklyOvertimeHoursInApproximateHours;

	@PositiveOrZero(message = "Weekly overtime bill amount must be zero or positive")
	private BigDecimal weeklyOvertimeBillAmount;

	@PositiveOrZero(message = "Weekly overtime pay amount must be zero or positive")
	private BigDecimal weeklyOvertimePayAmount;

	private boolean hasWeeklyOvertime;

	private String weeklyOvertimeRuleName;

	private boolean virtualRule;

}