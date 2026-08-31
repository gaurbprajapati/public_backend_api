/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.timesheet;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to hold permission-related data for timesheets fetched in bulk This allows us to
 * get all candidate and job data for multiple timesheets in a single query
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetPermissionDataDto {

	/**
	 * Timesheet ID
	 */
	private Integer timesheetId;

	/**
	 * Timesheet setting ID
	 */
	private Integer timesheetSettingId;

	/**
	 * Candidate ID
	 */
	private Integer candidateId;

	/**
	 * Job ID
	 */
	private Integer jobId;

	/**
	 * Full candidate entity with all required data for permission checking
	 */
	private Candidate candidate;

	/**
	 * Full job entity with all required data for permission checking
	 */
	private Job job;

}