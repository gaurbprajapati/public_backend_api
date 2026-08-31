/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */
package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import java.util.Map;
import java.util.Set;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadDataRequest {

	private BulkDataLoadingStrategy strategy;

	private Set<Integer> timesheetIds;

	private Set<Integer> candidateIds;

	private Set<Integer> jobIds;

	private Integer accountId;

	private Map<Integer, Candidate> candidatesByTimesheetId;

	private Map<Integer, Job> jobsByTimesheetId;

	private Map<Integer, Candidate> candidatesById;

	private Map<Integer, Job> jobsById;

}