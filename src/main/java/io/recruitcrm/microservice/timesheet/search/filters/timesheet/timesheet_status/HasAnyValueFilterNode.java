package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet status HAS_ANY_VALUE filter - matches timesheets that have
 * any status (status IS NOT NULL). No filter value is required, just checks that status
 * exists.
 */
public class HasAnyValueFilterNode extends TimesheetStatusFieldBaseFilterNode {

	private Field<Integer> statusIdField;

	public HasAnyValueFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.statusIdField = this.getStatusIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Return timesheets with any status (status IS NOT NULL)
		return List.of(this.statusIdField.isNotNull());
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
