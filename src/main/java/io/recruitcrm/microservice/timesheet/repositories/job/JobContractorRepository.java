/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.job;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateQueryResultDto;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JobContractorRepository {

	private final DSLContext auroraDbDSLContext;

	public JobContractorRepository(DSLContext auroraDbDSLContext) {
		this.auroraDbDSLContext = auroraDbDSLContext;
	}

	/**
	 * Get timesheet enabled assigned candidates for given job IDs. This query joins
	 * candidate with job assignments and timesheet setting associations to ensure only
	 * candidates with timesheet enabled are returned. Results are scoped to the
	 * authenticated account so a caller cannot read another account's candidates by
	 * supplying its job IDs.
	 * @param jobIds List of job IDs to filter candidates
	 * @return List of candidate query results with timesheet enabled
	 */
	public List<TimesheetEnabledAssignedCandidateQueryResultDto> getTimesheetEnabledAssignedCandidates(
			List<Integer> jobIds, Integer accountId) {
		// Table aliases
		var candidate = Tblcandidate.TBLCANDIDATE;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		// Separate alias for subquery to avoid table reference conflict
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");

		// Build JOOQ query with aliases matching DTO field names
		// Note: Using select instead of selectDistinct to allow contractors to appear
		var query = this.auroraDbDSLContext
			.select(candidate.ID.as("id"), candidate.SRNO.as("srno"), candidate.FIRSTNAME.as("firstName"),
					candidate.LASTNAME.as("lastName"), candidate.EMAILID.as("emailId"), candidate.SLUG.as("slug"),
					candidate.OWNERID.as("ownerId"), candidate.POSITION.as("position"),
					candidate.LASTORGANISATION.as("lastOrganisation"), candidate.CONTACTNUMBER.as("contactNumber"),
					candidate.PROFILEPIC.as("profilePic"), assignJobCandidate.JOBID.as("jobId"),
					tsSetting.JOB_START_DATE.as("jobStartDate"), tsSetting.JOB_END_DATE.as("jobEndDate"),
					tsSetting.TIMESHEET_FREQUENCY.as("timesheetFrequency"),
					tsSetting.TIMESHEET_START_DAY.as("timesheetStartDay"))
			.from(candidate)
			.innerJoin(assignJobCandidate)
			.on(assignJobCandidate.CANDIDATEID.eq(candidate.ID))
			.innerJoin(tsSettingAssoc)
			.on(tsSettingAssoc.JOB_ID.eq(assignJobCandidate.JOBID)
				.and(tsSettingAssoc.CONTRACTOR_ID.eq(assignJobCandidate.CANDIDATEID)))
			.innerJoin(tsSetting)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.where(assignJobCandidate.JOBID.in(jobIds)
				.and(assignJobCandidate.ACCOUNTID.eq(accountId))
				.and(tsSetting.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
					.from(tsSettingInner)
					.where(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssoc.ID)))));

		return query.fetch().into(TimesheetEnabledAssignedCandidateQueryResultDto.class);
	}

	/**
	 * Returns the subset of the given job IDs that belong to the supplied account. Used
	 * to authorize access before returning candidate data so a caller cannot read jobs
	 * that do not exist or belong to another account.
	 * @param jobIds List of job IDs to validate
	 * @param accountId Account ID derived from the authenticated principal
	 * @return Job IDs from {@code jobIds} owned by {@code accountId} (empty if none)
	 */
	public List<Integer> getOwnedJobIds(List<Integer> jobIds, Integer accountId) {
		var job = Tbljob.TBLJOB;
		return this.auroraDbDSLContext.select(job.ID)
			.from(job)
			.where(job.ID.in(jobIds).and(job.ACCOUNTID.eq(accountId)))
			.fetch(job.ID);
	}

}
