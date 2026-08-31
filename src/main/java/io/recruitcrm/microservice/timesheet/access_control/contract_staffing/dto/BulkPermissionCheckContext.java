/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkPermissionCheckContext {

	/**
	 * Preloaded candidates mapped by timesheet ID (for timesheet-based permission checks)
	 */
	private Map<Integer, Candidate> candidatesByTimesheetId;

	/**
	 * Preloaded jobs mapped by timesheet ID (for timesheet-based permission checks)
	 */
	private Map<Integer, Job> jobsByTimesheetId;

	/**
	 * Preloaded candidates mapped by candidate ID (for direct candidate permission
	 * checks)
	 */
	private Map<Integer, Candidate> candidatesById;

	/**
	 * Preloaded jobs mapped by job ID (for direct job permission checks)
	 */
	private Map<Integer, Job> jobsById;

	/**
	 * Organization identifier for the current user
	 */
	private String organizationIdentifier;

}