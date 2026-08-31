package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import java.time.Instant;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;

/**
 * Filter node for "contains" logic - matches contractors that are actively working in ALL
 * of the specified jobs (but can have additional jobs as well). Uses GROUP BY with HAVING
 * COUNT to ensure all specified jobs are present. Actively working means timesheet
 * enabled and current date lies within the job duration.
 */
public class ContainsFilterNode extends ContractorJobFieldBaseFilterNode {

	private static final String JOB_TYPE_CONTRACT = "contract";

	private static final String JOB_TYPE_CONTRACT_TO_PERMANENT = "contracttopermanent";

	private Field<Integer> jobIdField;

	public ContainsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.jobIdField = this.getSearchField(Integer.class);
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> jobIds = this.parseIntegerFilterValue();

		if (jobIds.isEmpty()) {
			// If no filter values, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		Long currentEpoch = Instant.now().getEpochSecond();
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		// Filter condition: job ID must be in the specified list
		// We'll use GROUP BY with HAVING to ensure all jobs are present
		// A contractor is actively working when:
		// - Contractor is assigned to job (via assignJobCandidate)
		// - Timesheet is enabled (via tsSettingAssoc)
		// - Latest timesheet setting is used
		// - Today's date is between job start and end date
		// - Job type is contract/contracttopermanent
		Condition jobCondition = ContractorGroupBaseFilterNode.JOB.ACCOUNTID.eq(this.filterNodeContext.getAccountId())
			.and(ContractorGroupBaseFilterNode.JOB.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
			.and(this.jobIdField.in(jobIds))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_START_DATE.le(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.JOB_END_DATE.ge(currentEpoch.intValue()))
			.and(ContractorGroupBaseFilterNode.TS_SETTING.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(tsSettingAssocInner.CONTRACTOR_ID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)))));

		return List.of(jobCondition);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Need joins to access job and timesheet setting tables for filtering
		// This matches the pattern used in ContractorJobFieldBaseFilterNode
		List<TableJoinSpecification> joins = new java.util.ArrayList<>();

		// Add assign job candidate join to ensure contractor is actively assigned
		joins.add(new TableJoinSpecification(io.recruitcrm.microservice.timesheet.search.constants.TableJoinType.INNER,
				ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE,
				ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.CANDIDATEID
					.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)));

		// Add job join
		joins.add(new TableJoinSpecification(io.recruitcrm.microservice.timesheet.search.constants.TableJoinType.INNER,
				ContractorGroupBaseFilterNode.JOB,
				ContractorGroupBaseFilterNode.JOB.ID.eq(ContractorJobFieldBaseFilterNode.ASSIGN_JOB_CANDIDATE.JOBID)));

		// Add timesheet setting association join (timesheet must be enabled)
		joins.add(new TableJoinSpecification(io.recruitcrm.microservice.timesheet.search.constants.TableJoinType.INNER,
				ContractorGroupBaseFilterNode.TS_SETTING_ASSOC,
				ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.eq(ContractorGroupBaseFilterNode.JOB.ID)
					.and(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID
						.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))));

		// Add timesheet setting join
		joins.add(new TableJoinSpecification(io.recruitcrm.microservice.timesheet.search.constants.TableJoinType.INNER,
				ContractorGroupBaseFilterNode.TS_SETTING, ContractorGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(ContractorGroupBaseFilterNode.TS_SETTING_ASSOC.ID)));

		return joins;
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		// Group by contractor ID to count distinct jobs per contractor
		return List.of(ContractorGroupBaseFilterNode.CANDIDATE.ID);
	}

	@Override
	public Condition getGroupByHavingCondition() {
		List<Integer> jobIds = this.parseIntegerFilterValue();

		if (jobIds.isEmpty()) {
			// If no filter values, return false condition (matches nothing)
			return DSL.falseCondition();
		}

		// HAVING clause: count of distinct matching jobs must equal the number of jobs
		// in filter This ensures the contractor has ALL the specified jobs
		return DSL.countDistinct(this.jobIdField).eq(jobIds.size());
	}

	@Override
	public List<Condition> getCommonFilterCondition() {
		// Don't add the latest timesheet setting condition here since we're handling
		// it in getFilterConditions
		// Return empty list to avoid duplicate conditions
		return List.of();
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use GROUP BY instead of DISTINCT
		return false;
	}

}
