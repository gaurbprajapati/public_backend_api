/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response body DTO for money data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyDataResponseBodyDto {

	@PositiveOrZero(message = "Pay amount must be zero or positive")
	private BigDecimal payAmount;

	@PositiveOrZero(message = "Bill amount must be zero or positive")
	private BigDecimal billAmount;

}