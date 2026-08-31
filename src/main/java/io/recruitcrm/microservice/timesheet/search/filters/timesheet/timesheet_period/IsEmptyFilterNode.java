package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet period IS_EMPTY filter - returns no timesheets (false
 * condition). This filter is used when the user wants to explicitly filter out all
 * timesheets.
 */
public class IsEmptyFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	public IsEmptyFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Return false condition (matches nothing - no timesheets fetched)
		return List.of(DSL.falseCondition());
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
