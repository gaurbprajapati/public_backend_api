package io.recruitcrm.microservice.timesheet.search.filters.contractor;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.BaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

public abstract class ContractorGroupBaseFilterNode extends BaseFilterNode {

	protected static final Tblcandidate CANDIDATE = Tblcandidate.TBLCANDIDATE;

	protected static final CstTimesheetSettingT TS_SETTING = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;

	protected static final CstTimesheetSettingAssociationT TS_SETTING_ASSOC = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;

	protected static final Tbljob JOB = Tbljob.TBLJOB;

	protected ContractorGroupBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public Table<?> getBaseTable() {
		return CANDIDATE;
	}

	@Override
	public List<Field<?>> getSelectFields() {
		return List.of(CANDIDATE.ID);
	}

	@Override
	public Condition getAccountIdFilterCondition() {
		return CANDIDATE.ACCOUNTID.eq(this.filterNodeContext.getAccountId());
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Default implementation returns minimal joins
		// Subclasses should override this to return only necessary joins
		return this.getMinimalJoins();
	}

	/**
	 * Returns minimal joins (no additional joins needed for basic contractor filters)
	 * @return Empty list as contractor table has account_id directly
	 */
	protected List<TableJoinSpecification> getMinimalJoins() {
		return new ArrayList<>();
	}

	/**
	 * Returns joins needed for timesheet setting fields. Includes:
	 * cst_timesheet_setting_association_t, cst_timesheet_setting_t
	 * @return List with joins up to timesheet_setting table
	 */
	protected List<TableJoinSpecification> getTimesheetSettingJoins() {
		List<TableJoinSpecification> joins = new ArrayList<>();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING_ASSOC,
				TS_SETTING_ASSOC.CONTRACTOR_ID.eq(CANDIDATE.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING,
				TS_SETTING.ASSOCIATION_ID.eq(TS_SETTING_ASSOC.ID)));
		return joins;
	}

	/**
	 * Returns joins needed for job fields. Includes: cst_timesheet_setting_association_t,
	 * cst_timesheet_setting_t, tbljob
	 * @return List with joins up to job table
	 */
	protected List<TableJoinSpecification> getJobJoins() {
		List<TableJoinSpecification> joins = this.getTimesheetSettingJoins();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, JOB, JOB.ID.eq(TS_SETTING_ASSOC.JOB_ID)));
		return joins;
	}

	/**
	 * Ensures contractors are actively assigned to contract jobs with timesheet settings
	 * enabled. Prevents contractors who were unassigned but still have timesheet
	 * associations from matching deal-related filters.
	 * @return EXISTS condition for active contractor-job assignment
	 */
	protected Condition getActiveContractorAssignmentCondition() {
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		return DSL.exists(DSL.selectOne()
			.from(TS_SETTING_ASSOC)
			.innerJoin(JOB)
			.on(JOB.ID.eq(TS_SETTING_ASSOC.JOB_ID))
			.innerJoin(assignJobCandidate)
			.on(assignJobCandidate.CANDIDATEID.eq(CANDIDATE.ID).and(assignJobCandidate.JOBID.eq(JOB.ID)))
			.where(TS_SETTING_ASSOC.CONTRACTOR_ID.eq(CANDIDATE.ID))
			.and(JOB.JOB_TYPE.in("contract", "contracttopermanent")));
	}

}
