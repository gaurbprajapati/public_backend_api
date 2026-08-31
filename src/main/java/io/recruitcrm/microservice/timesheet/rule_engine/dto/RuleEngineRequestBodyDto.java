/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rule engine evaluation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEngineRequestBodyDto {

	@NotNull(message = "Timesheet ID cannot be null")
	@Positive(message = "Timesheet ID must be a positive number")
	private Integer timesheetId;

	/**
	 * Flag to control refreshing stored total bill data and pay data in the Timesheet
	 * model. When true, will refresh
	 * io.recruitcrm.contract_staffing.entity.model.Timesheet#totalBillData and
	 * io.recruitcrm.contract_staffing.entity.model.Timesheet#totalPayData
	 */
	@Builder.Default
	private Boolean refreshStoredData = false;

}