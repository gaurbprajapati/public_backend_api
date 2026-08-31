package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet status DOES_NOT_CONTAIN filter - matches timesheets that do
 * NOT have any of the specified status IDs. This is the opposite of "contains at least
 * one". Includes timesheets without status (status IS NULL).
 */
public class DoesNotContainFilterNode extends TimesheetStatusFieldBaseFilterNode {

	private Field<Integer> statusIdField;

	public DoesNotContainFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.statusIdField = this.getStatusIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> statusIds = this.parseIntegerFilterValue();

		if (statusIds.isEmpty()) {
			// If no filter values, return no condition (matches all)
			return List.of();
		}

		// Return timesheets NOT having any of the specified status IDs
		// This includes timesheets without status (status IS NULL) OR status not in the
		// list
		return List.of(this.statusIdField.isNull().or(this.statusIdField.notIn(statusIds)));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
