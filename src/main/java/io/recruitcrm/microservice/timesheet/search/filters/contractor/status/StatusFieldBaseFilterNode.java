package io.recruitcrm.microservice.timesheet.search.filters.contractor.status;

import java.time.Instant;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;

public abstract class StatusFieldBaseFilterNode extends ContractorGroupBaseFilterNode {

	private static final String INNER_ACJ_ALIAS = "innerAcj";

	private static final String INNER_ACJ2_ALIAS = "innerAcj2";

	private static final String INNER_ACJ3_ALIAS = "innerAcj3";

	private static final String JOB_TYPE_CONTRACT = "contract";

	private static final String JOB_TYPE_CONTRACT_TO_PERMANENT = "contracttopermanent";

	protected static final Tblassignjobcandidate ASSIGN_JOB_CANDIDATE = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

	protected StatusFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public final Field<?> getSearchField() {
		return ContractorGroupBaseFilterNode.CANDIDATE.ID;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Status filter needs joins to check for active timesheet settings
		// Join: cst_timesheet_setting_association_t, cst_timesheet_setting_t, tbljob
		return this.getJobJoins();
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		// Group by candidate ID to aggregate timesheet settings per contractor
		return List.of(ContractorGroupBaseFilterNode.CANDIDATE.ID);
	}

	/**
	 * Returns the condition to check contractor status based on timesheet settings.
	 * AVAILABLE: Contractor assigned to contract/contracttopermanent job with timesheet
	 * enabled (exists in tblassignjobcandidate AND cst_timesheet_setting_association_t),
	 * BUT current date is NOT between job_start_date and job_end_date for ANY job
	 * ASSIGNED: Contractor assigned to contract/contracttopermanent job with timesheet
	 * enabled (exists in tblassignjobcandidate AND cst_timesheet_setting_association_t),
	 * AND current date IS between job_start_date and job_end_date for AT LEAST ONE job
	 * @param statusValue 0 for AVAILABLE, 1 for ASSIGNED
	 * @return Condition for status filter
	 */
	protected Condition getStatusCondition(Integer statusValue) {
		Long currentEpoch = Instant.now().getEpochSecond();
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		if (statusValue == 1) {
			// ASSIGNED: contractor must be:
			// 1. Actively assigned to job (exists in tblassignjobcandidate)
			// 2. Timesheet enabled (exists in cst_timesheet_setting_association_t)
			// 3. Latest timesheet setting is used
			// 4. Job type is contract/contracttopermanent
			// 5. Current date is between start/end date for at least one job
			return DSL.exists(DSL.selectOne()
				.from(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC)
				.innerJoin(ContractorGroupBaseFilterNode.TS_SETTING)
				.on(ContractorGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.ID))
				.innerJoin(ContractorGroupBaseFilterNode.JOB)
				.on(ContractorGroupBaseFilterNode.JOB.ID.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID))
				.innerJoin(ASSIGN_JOB_CANDIDATE)
				.on(ASSIGN_JOB_CANDIDATE.CANDIDATEID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)
					.and(ASSIGN_JOB_CANDIDATE.JOBID.eq(ContractorGroupBaseFilterNode.JOB.ID)))
				.where(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
					.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
				.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
				.and(ContractorGroupBaseFilterNode.TS_SETTING.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId()))
				.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_START_DATE.le(currentEpoch.intValue()))
				.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_END_DATE.ge(currentEpoch.intValue()))
				.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
					.from(tsSettingInner)
					.innerJoin(tsSettingAssocInner)
					.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
					.innerJoin(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ_ALIAS))
					.on(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ_ALIAS).JOBID.eq(tsSettingAssocInner.JOB_ID)
						.and(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ_ALIAS).CANDIDATEID
							.eq(tsSettingAssocInner.CONTRACTOR_ID)))
					.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
						.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
						.and(tsSettingInner.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId()))
						.and(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ_ALIAS).ACCOUNTID
							.eq(this.filterNodeContext.getAccountId())))
					.groupBy(tsSettingAssocInner.JOB_ID))));
		}
		else {
			// AVAILABLE: contractor must be:
			// 1. Actively assigned to job (exists in tblassignjobcandidate)
			// 2. Timesheet enabled (exists in cst_timesheet_setting_association_t)
			// 3. Latest timesheet setting is used
			// 4. Job type is contract/contracttopermanent
			// 5. Current date is NOT between start/end date for any job
			Condition timesheetEnabledExists = DSL.exists(DSL.selectOne()
				.from(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC)
				.innerJoin(ContractorGroupBaseFilterNode.TS_SETTING)
				.on(ContractorGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.ID))
				.innerJoin(ContractorGroupBaseFilterNode.JOB)
				.on(ContractorGroupBaseFilterNode.JOB.ID.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID))
				.innerJoin(ASSIGN_JOB_CANDIDATE)
				.on(ASSIGN_JOB_CANDIDATE.CANDIDATEID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)
					.and(ASSIGN_JOB_CANDIDATE.JOBID.eq(ContractorGroupBaseFilterNode.JOB.ID)))
				.where(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
					.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
				.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
				.and(ContractorGroupBaseFilterNode.TS_SETTING.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId()))
				.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
					.from(tsSettingInner)
					.innerJoin(tsSettingAssocInner)
					.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
					.innerJoin(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ2_ALIAS))
					.on(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ2_ALIAS).JOBID.eq(tsSettingAssocInner.JOB_ID)
						.and(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ2_ALIAS).CANDIDATEID
							.eq(tsSettingAssocInner.CONTRACTOR_ID)))
					.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
						.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
						.and(tsSettingInner.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId()))
						.and(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ2_ALIAS).ACCOUNTID
							.eq(this.filterNodeContext.getAccountId())))
					.groupBy(tsSettingAssocInner.JOB_ID))));

			var tsSettingAssocAlias = ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.as("tsa3");
			var tsSettingAlias = ContractorGroupBaseFilterNode.TS_SETTING.as("ts3");
			var jobAlias = ContractorGroupBaseFilterNode.JOB.as("j3");
			var assignJobCandidateAlias = ASSIGN_JOB_CANDIDATE.as("acj3");

			Condition activeJobNotExists = DSL.notExists(DSL.selectOne()
				.from(tsSettingAssocAlias)
				.innerJoin(tsSettingAlias)
				.on(tsSettingAlias.ASSOCIATION_ID.eq(tsSettingAssocAlias.ID))
				.innerJoin(jobAlias)
				.on(jobAlias.ID.eq(tsSettingAssocAlias.JOB_ID))
				.innerJoin(assignJobCandidateAlias)
				.on(assignJobCandidateAlias.CANDIDATEID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)
					.and(assignJobCandidateAlias.JOBID.eq(jobAlias.ID)))
				.where(tsSettingAssocAlias.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
				.and(jobAlias.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
				.and(tsSettingAlias.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId()))
				.and(tsSettingAlias.JOB_START_DATE.le(currentEpoch.intValue()))
				.and(tsSettingAlias.JOB_END_DATE.ge(currentEpoch.intValue()))
				.and(tsSettingAlias.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
					.from(tsSettingInner)
					.innerJoin(tsSettingAssocInner)
					.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
					.innerJoin(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ3_ALIAS))
					.on(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ3_ALIAS).JOBID.eq(tsSettingAssocInner.JOB_ID)
						.and(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ3_ALIAS).CANDIDATEID
							.eq(tsSettingAssocInner.CONTRACTOR_ID)))
					.where(tsSettingAssocInner.JOB_ID.eq(jobAlias.ID)
						.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
						.and(tsSettingInner.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId()))
						.and(ASSIGN_JOB_CANDIDATE.as(INNER_ACJ3_ALIAS).ACCOUNTID
							.eq(this.filterNodeContext.getAccountId())))
					.groupBy(tsSettingAssocInner.JOB_ID))));

			return timesheetEnabledExists.and(activeJobNotExists);
		}
	}

}
