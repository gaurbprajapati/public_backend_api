package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import java.time.Instant;
import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;

/**
 * Filter node for "does not contain" logic - matches contractors that are NOT actively
 * working in any of the specified jobs. This is the negation of "contains at least one".
 * Actively working means timesheet enabled and current date lies within the job duration.
 */
public class DoesNotContainFilterNode extends ContractorJobFieldBaseFilterNode {

	public DoesNotContainFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> jobIds = this.parseIntegerFilterValue();

		if (jobIds.isEmpty()) {
			// If no filter values, return no condition (matches all)
			return List.of();
		}

		Long currentEpoch = Instant.now().getEpochSecond();
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		// Use NOT EXISTS to ensure contractor is NOT actively working in ANY of the
		// specified jobs
		// This is the opposite of "contains at least one" - we check that there's no
		// matching active job
		// A contractor is actively working when:
		// - Contractor is assigned to job (via assignJobCandidate)
		// - Timesheet is enabled (via tsSettingAssoc)
		// - Latest timesheet setting is used
		// - Today's date is between job start and end date
		// - Job type is contract/contracttopermanent
		Condition jobCondition = DSL.notExists(DSL.selectOne()
			.from(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE)
			.innerJoin(ContractorGroupBaseFilterNode.JOB)
			.on(ContractorGroupBaseFilterNode.JOB.ID.eq(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.JOBID))
			.innerJoin(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC)
			.on(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
				.and(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
					.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)))
			.innerJoin(ContractorGroupBaseFilterNode.TS_SETTING)
			.on(ContractorGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
				.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.ID))
			.where(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.CANDIDATEID
				.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
			.and(ContractorGroupBaseFilterNode.JOB.ACCOUNTID.eq(this.filterNodeContext.getAccountId()))
			.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in("contract", "contracttopermanent"))
			.and(ContractorGroupBaseFilterNode.JOB.ID.in(jobIds))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_START_DATE.le(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_END_DATE.ge(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))))));

		// Ensure contractors are actively assigned to jobs with timesheet settings
		// enabled
		// This prevents contractors who were unassigned but still have timesheet
		// associations
		Condition activeAssignmentCondition = DSL.exists(DSL.selectOne()
			.from(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC)
			.innerJoin(ContractorGroupBaseFilterNode.JOB)
			.on(ContractorGroupBaseFilterNode.JOB.ID.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID))
			.innerJoin(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE)
			.on(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.CANDIDATEID
				.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)
				.and(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.JOBID
					.eq(ContractorGroupBaseFilterNode.JOB.ID)))
			.where(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
				.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
			.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in("contract", "contracttopermanent")));

		return List.of(jobCondition.and(activeAssignmentCondition));
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// No joins needed - we use NOT EXISTS subquery instead
		// This prevents the base query from joining with job tables unnecessarily
		return this.getMinimalJoins();
	}

	@Override
	public List<Condition> getCommonFilterCondition() {
		// Don't add the latest timesheet setting condition here since we're using
		// subqueries that handle this logic internally
		// Return empty list to avoid referencing tables that aren't joined
		return List.of();
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate contractor IDs
		return true;
	}

}
