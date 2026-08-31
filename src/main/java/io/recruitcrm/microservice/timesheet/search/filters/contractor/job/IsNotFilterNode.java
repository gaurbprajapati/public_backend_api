package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import java.time.Instant;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;

/**
 * Filter node for job IS_NOT filter - matches contractors that are NOT assigned to
 * EXACTLY the specified jobs. Uses subquery approach similar to contractor deal IS_NOT
 * filter. This is the negation of the IS filter.
 */
public class IsNotFilterNode extends ContractorJobFieldBaseFilterNode {

	private static final String JOB_TYPE_CONTRACT = "contract";

	private static final String JOB_TYPE_CONTRACT_TO_PERMANENT = "contracttopermanent";

	public IsNotFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> jobIds = this.parseIntegerFilterValue();

		if (jobIds.isEmpty()) {
			// If no filter values, return no condition (matches all)
			return List.of();
		}

		int expectedCount = jobIds.size();
		Long currentEpoch = Instant.now().getEpochSecond();

		// Step 1: Find contractor-job pairs that have all the specified jobs
		// A contractor-job pair is considered active when:
		// - Contractor is assigned to job (via assignJobCandidate)
		// - Timesheet is enabled (via tsSettingAssoc)
		// - Latest timesheet setting is used
		// - Today's date is between job start and end date
		// - Job type is contract/contracttopermanent
		Field<Integer> candidateIdField = ContractorGroupBaseFilterNode.CANDIDATE.ID;
		Field<Integer> jobIdField = ContractorGroupBaseFilterNode.JOB.ID.as("job_id");

		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		Select<?> contractorJobPairsWithJobs = DSL.select(candidateIdField.as("contractor_id"), jobIdField)
			.from(ContractorGroupBaseFilterNode.CANDIDATE)
			.innerJoin(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE)
			.on(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.CANDIDATEID
				.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
			.innerJoin(ContractorGroupBaseFilterNode.JOB)
			.on(ContractorGroupBaseFilterNode.JOB.ID.eq(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.JOBID))
			.innerJoin(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC)
			.on(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
				.and(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
					.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)))
			.innerJoin(ContractorGroupBaseFilterNode.TS_SETTING)
			.on(ContractorGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
				.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.ID))
			.where(ContractorGroupBaseFilterNode.CANDIDATE.ACCOUNTID.eq(this.filterNodeContext.getAccountId()))
			.and(ContractorGroupBaseFilterNode.JOB.ACCOUNTID.eq(this.filterNodeContext.getAccountId()))
			.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
			.and(ContractorGroupBaseFilterNode.JOB.ID.in(jobIds))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_START_DATE.le(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_END_DATE.ge(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)))))
			.groupBy(candidateIdField)
			.having(DSL.countDistinct(jobIdField).eq(expectedCount));

		// Step 2: Filter out contractor-job pairs that have jobs outside the specified
		// list
		// Using NOT EXISTS to ensure no jobs outside the filter list
		Table<?> pairTable = contractorJobPairsWithJobs.asTable("pair");
		Field<Integer> pairCandidateIdField = pairTable.field("contractor_id", Integer.class);

		// Create aliases for the NOT EXISTS subquery
		var candidateAlias = ContractorGroupBaseFilterNode.CANDIDATE.as("c");
		var assignJobCandidateAlias = ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.as("acj");
		var jobAlias = ContractorGroupBaseFilterNode.JOB.as("j");
		var tsSettingAssocAlias = ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.as("tsa");
		var tsSettingAlias = ContractorGroupBaseFilterNode.TS_SETTING.as("ts");

		Select<? extends Record1<Integer>> matchingContractorIds = DSL.select(pairCandidateIdField)
			.from(pairTable)
			.where(DSL.notExists(DSL.selectOne()
				.from(candidateAlias)
				.innerJoin(assignJobCandidateAlias)
				.on(assignJobCandidateAlias.CANDIDATEID.eq(candidateAlias.ID))
				.innerJoin(jobAlias)
				.on(jobAlias.ID.eq(assignJobCandidateAlias.JOBID))
				.innerJoin(tsSettingAssocAlias)
				.on(tsSettingAssocAlias.JOB_ID.eq(jobAlias.ID)
					.and(tsSettingAssocAlias.CONTRACTOR_ID.eq(candidateAlias.ID)))
				.innerJoin(tsSettingAlias)
				.on(tsSettingAlias.ASSOCIATION_ID.eq(tsSettingAssocAlias.ID))
				.where(pairCandidateIdField.eq(candidateAlias.ID))
				.and(candidateAlias.ACCOUNTID.eq(this.filterNodeContext.getAccountId()))
				.and(jobAlias.ACCOUNTID.eq(this.filterNodeContext.getAccountId()))
				.and(jobAlias.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
				.and(jobAlias.ID.notIn(jobIds))
				.and(tsSettingAlias.JOB_START_DATE.le(currentEpoch.intValue()))
				.and(tsSettingAlias.JOB_END_DATE.ge(currentEpoch.intValue()))
				.and(tsSettingAlias.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
					.from(tsSettingInner)
					.innerJoin(tsSettingAssocInner)
					.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
					.where(tsSettingAssocInner.JOB_ID.eq(jobAlias.ID)
						.and(tsSettingAssocInner.CONTRACTOR_ID.eq(candidateAlias.ID)))))));

		// Step 3: Ensure contractors are actively assigned to jobs with timesheet
		// settings enabled
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
			.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT)));

		// Step 4: Filter contractors where ID is NOT IN the matching contractor IDs
		// AND contractor is actively assigned to a job with timesheet settings enabled
		return List
			.of(ContractorGroupBaseFilterNode.CANDIDATE.ID.notIn(matchingContractorIds).and(activeAssignmentCondition));
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// No joins needed - we use subqueries instead
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

}
