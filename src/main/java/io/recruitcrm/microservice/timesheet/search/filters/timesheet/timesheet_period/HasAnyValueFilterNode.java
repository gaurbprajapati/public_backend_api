package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet period HAS_ANY_VALUE filter - returns all timesheets (no
 * period filtering, only account_id filter is applied). Since all timesheets have
 * period_start and period_end, this filter matches all timesheets.
 */
public class HasAnyValueFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	public HasAnyValueFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Return empty condition list - matches all timesheets (only account_id filter
		// will
		// be applied)
		return List.of();
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
