package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for "is empty" logic - shows timesheets for jobs that are NOT assigned to
 * any deal. This is the negation of "has any value" filter. Uses NOT EXISTS with account
 * ID check for optimization.
 */
public class IsEmptyFilterNode extends DistinctDealFilterNode {

	public IsEmptyFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Need job joins to access tsSettingAssoc for contractor and job IDs in NOT
		// EXISTS
		// subquery
		// This includes: tsSetting, tsSettingAssoc, job
		return this.getJobJoins();
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Use NOT EXISTS to ensure no deals exist for this job-contractor combination
		// This is the negation of HasAnyValueFilterNode
		// Include account ID check for query optimization
		Condition dealNotExists = DSL.notExists(DSL.selectOne()
			.from(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES)
			.innerJoin(AssociatedDealFieldBaseFilterNode.DEAL_JOBS)
			.on(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.JOBID
				.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID)
				.and(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.DEALID
					.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID)))
			.innerJoin(AssociatedDealFieldBaseFilterNode.DEALS)
			.on(AssociatedDealFieldBaseFilterNode.DEALS.ID.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
			.where(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID
				.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID))
			.and(AssociatedDealFieldBaseFilterNode.DEALS.ACCOUNTID.eq(this.filterNodeContext.getAccountId())));

		return List.of(dealNotExists);
	}

}
