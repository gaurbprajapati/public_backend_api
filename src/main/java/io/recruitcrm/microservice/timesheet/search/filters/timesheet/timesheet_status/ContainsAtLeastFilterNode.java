package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet status CONTAINS_AT_LEAST_ONE filter - matches timesheets with
 * at least one of the specified status IDs. Status values: OPEN(1), SUBMITTED(2),
 * REJECTED(3), APPROVED(4).
 */
public class ContainsAtLeastFilterNode extends TimesheetStatusFieldBaseFilterNode {

	private Field<Integer> statusIdField;

	public ContainsAtLeastFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.statusIdField = this.getStatusIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> statusIds = this.parseIntegerFilterValue();

		if (statusIds.isEmpty()) {
			// If no filter values, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Return timesheets with any of the specified status IDs
		return List.of(this.statusIdField.in(statusIds));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
