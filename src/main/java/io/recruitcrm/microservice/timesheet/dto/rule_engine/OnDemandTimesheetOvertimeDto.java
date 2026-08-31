/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.rule_engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Simplified overtime results for a single timesheet in on-demand evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnDemandTimesheetOvertimeDto {

	private Integer timesheetId;

	private List<OnDemandTimeLogOvertimeDto> timeLogs;

	/**
	 * Weekly overtime in seconds, one entry per week in chronological order. Populated
	 * only when the request contains a single timesheet; null otherwise.
	 */
	private List<Long> weeklyOvertimeResults;

}
