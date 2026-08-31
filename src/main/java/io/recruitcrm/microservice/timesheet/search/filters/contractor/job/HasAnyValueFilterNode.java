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
 * Filter node for "has any value" logic - matches contractors where timesheet is enabled
 * AND current date lies within the job duration. This means we return all actively
 * working contractors.
 */
public class HasAnyValueFilterNode extends ContractorJobFieldBaseFilterNode {

	public HasAnyValueFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		Long currentEpoch = Instant.now().getEpochSecond();
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		// Check if contractor has at least one active timesheet setting
		// A contractor is actively working when:
		// - Contractor is assigned to job (via assignJobCandidate)
		// - Timesheet is enabled (via tsSettingAssoc)
		// - Latest timesheet setting is used
		// - Today's date is between job start and end date
		// - Job type is contract/contracttopermanent
		Condition activeTimesheetCondition = DSL.exists(DSL.selectOne()
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
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_START_DATE.le(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_END_DATE.ge(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))))));

		return List.of(activeTimesheetCondition);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// No joins needed - we use EXISTS subquery instead
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
