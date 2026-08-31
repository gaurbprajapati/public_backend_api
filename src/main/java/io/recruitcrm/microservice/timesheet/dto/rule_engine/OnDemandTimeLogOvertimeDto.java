/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified overtime result for a single time log in on-demand evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnDemandTimeLogOvertimeDto {

	private Integer timeLogId;

	private Long overtimeInSeconds;

}
