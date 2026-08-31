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
 * Filter node for "is empty" logic - matches contractors where timesheet is enabled but
 * current date does NOT lie within the job duration. This is the negation of "has any
 * value" filter.
 */
public class IsEmptyFilterNode extends ContractorJobFieldBaseFilterNode {

	public IsEmptyFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		Long currentEpoch = Instant.now().getEpochSecond();
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		// Check if contractor has timesheet enabled (regardless of date range)
		// A contractor has timesheet enabled when:
		// - Contractor is assigned to job (via assignJobCandidate)
		// - Timesheet is enabled (via tsSettingAssoc)
		// - Latest timesheet setting is used
		// - Job type is contract/contracttopermanent
		Condition timesheetEnabledCondition = DSL.exists(DSL.selectOne()
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
			.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))))));

		// Check that contractor does NOT have any active timesheet settings
		// (date range condition is NOT satisfied)
		// This is the negation of HasAnyValueFilterNode
		var assignJobCandidateAlias = ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.as("acj2");
		var jobAlias = ContractorGroupBaseFilterNode.JOB.as("j2");
		var tsSettingAssocAlias = ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.as("tsa2");
		var tsSettingAlias = ContractorGroupBaseFilterNode.TS_SETTING.as("ts2");

		Condition notActiveTimesheetCondition = DSL.notExists(DSL.selectOne()
			.from(assignJobCandidateAlias)
			.innerJoin(jobAlias)
			.on(jobAlias.ID.eq(assignJobCandidateAlias.JOBID))
			.innerJoin(tsSettingAssocAlias)
			.on(tsSettingAssocAlias.JOB_ID.eq(jobAlias.ID)
				.and(tsSettingAssocAlias.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)))
			.innerJoin(tsSettingAlias)
			.on(tsSettingAlias.ASSOCIATION_ID.eq(tsSettingAssocAlias.ID))
			.where(assignJobCandidateAlias.CANDIDATEID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
			.and(jobAlias.ACCOUNTID.eq(this.filterNodeContext.getAccountId()))
			.and(jobAlias.JOB_TYPE.in("contract", "contracttopermanent"))
			.and(tsSettingAlias.JOB_START_DATE.le(currentEpoch.intValue()))
			.and(tsSettingAlias.JOB_END_DATE.ge(currentEpoch.intValue()))
			.and(tsSettingAlias.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(jobAlias.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))))));

		return List.of(timesheetEnabledCondition.and(notActiveTimesheetCondition));
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// No joins needed - we use EXISTS/NOT EXISTS subqueries instead
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
