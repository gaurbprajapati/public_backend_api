package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for "has any value" logic - matches timesheets for jobs that have been
 * assigned to deals. No filter value is required, just checks that deals exist for the
 * job-contractor combination.
 */
public class HasAnyValueFilterNode extends AssociatedDealFieldBaseFilterNode {

	public HasAnyValueFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Only check account_id - no deal ID filtering needed
		// The joins will ensure we only get timesheets where deals exist
		return List.of(this.getAccountDealCondition());
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		// No GROUP BY needed - just check that deals exist
		return List.of();
	}

	@Override
	public Condition getGroupByHavingCondition() {
		// No HAVING clause needed
		return DSL.noCondition();
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs when a timesheet has multiple
		// deals
		return true;
	}

}
