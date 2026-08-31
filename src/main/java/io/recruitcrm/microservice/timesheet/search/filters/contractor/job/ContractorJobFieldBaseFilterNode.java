package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import java.time.Instant;
import java.util.ArrayList;
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
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;

public abstract class ContractorJobFieldBaseFilterNode extends ContractorGroupBaseFilterNode {

	protected static final Tblassignjobcandidate ASSIGN_JOB_CANDIDATE = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

	protected ContractorJobFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public final Field<?> getSearchField() {
		return ContractorGroupBaseFilterNode.JOB.ID;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins
		List<TableJoinSpecification> joins = new ArrayList<>();

		// Add assign job candidate join to ensure contractor is actively assigned
		joins.add(new TableJoinSpecification(TableJoinType.INNER, ASSIGN_JOB_CANDIDATE,
				ASSIGN_JOB_CANDIDATE.CANDIDATEID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)));

		// Add job join
		joins.add(new TableJoinSpecification(TableJoinType.INNER, ContractorGroupBaseFilterNode.JOB,
				ContractorGroupBaseFilterNode.JOB.ID.eq(ASSIGN_JOB_CANDIDATE.JOBID)));

		// Add timesheet setting association join (timesheet must be enabled)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, ContractorGroupBaseFilterNode.TS_SETTING_ASSOC,
				ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
						.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))));

		// Add timesheet setting join
		joins.add(new TableJoinSpecification(TableJoinType.INNER, ContractorGroupBaseFilterNode.TS_SETTING,
				ContractorGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.ID)));

		return joins;
	}

	@Override
	public List<Condition> getCommonFilterCondition() {
		// Ensure we use the latest timesheet setting for each job-contractor pair
		// This matches the logic in ContractorRepository.getJobsByContractorIds
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		Condition latestSettingCondition = ContractorGroupBaseFilterNode.TS_SETTING.ID
			.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))));

		List<Condition> conditions = new ArrayList<>(super.getCommonFilterCondition());
		conditions.add(latestSettingCondition);
		return conditions;
	}

	/**
	 * Returns condition to check that today's date is between job start and end date
	 * @return Condition for date range check
	 */
	protected Condition getActiveDateRangeCondition() {
		Long currentEpoch = Instant.now().getEpochSecond();
		return ContractorGroupBaseFilterNode.TS_SETTING.JOB_START_DATE.le(currentEpoch.intValue())
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_END_DATE.ge(currentEpoch.intValue()));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate contractor IDs
		return true;
	}

}
