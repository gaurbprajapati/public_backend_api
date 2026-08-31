package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

/**
 * Filter node for "does not contain" logic - matches timesheets that do NOT have any of
 * the specified deals. Uses NOT EXISTS subquery to ensure no matching deals exist. This
 * is the opposite of "contains at least one".
 */
public class DoesNotContainFilterNode extends DistinctDealFilterNode {

	public DoesNotContainFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		List<TableJoinSpecification> joins = this.getMinimalJoins();

		// Add association join (needed for contractor and job IDs for NOT EXISTS
		// subquery)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC,
				TimesheetGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.ID)));

		// No deal-related joins needed - we use NOT EXISTS subquery to check for matching
		// deals
		return joins;
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> dealIds = this.parseIntegerFilterValue();

		if (dealIds.isEmpty()) {
			return DealFilterConditions.emptyDealIdsMatchesAll();
		}

		// Use NOT EXISTS to ensure timesheet does NOT have ANY of the specified deals
		return List.of(DSL.notExists(DSL.selectOne()
			.from(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES)
			.innerJoin(AssociatedDealFieldBaseFilterNode.DEAL_JOBS)
			.on(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.DEALID
				.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
			.innerJoin(AssociatedDealFieldBaseFilterNode.DEALS)
			.on(AssociatedDealFieldBaseFilterNode.DEALS.ID.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
			.where(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID
				.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID))
			.and(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.JOBID
				.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID))
			.and(this.getAccountDealCondition())
			.and(AssociatedDealFieldBaseFilterNode.DEALS.ID.in(dealIds))));
	}

}
