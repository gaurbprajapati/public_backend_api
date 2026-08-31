package io.recruitcrm.microservice.timesheet.search.filters.contractor.status;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

public class IsFilterNode extends StatusFieldBaseFilterNode {

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		String filterValue = this.filterNodeContext.getFilterDto().getFilterValue();
		if (filterValue == null || filterValue.trim().isEmpty()) {
			// If no value provided, return no condition (matches nothing)
			return List.of();
		}

		// Parse filterValue as integer (0 for AVAILABLE, 1 for ASSIGNED)
		Integer statusValue;
		try {
			statusValue = Integer.parseInt(filterValue.trim());
		}
		catch (NumberFormatException ex) {
			// Invalid status value, return no condition (matches nothing)
			return List.of();
		}

		// Validate status value (must be 0 or 1)
		if (statusValue != 0 && statusValue != 1) {
			return List.of();
		}

		// For IS filter, use the status condition
		List<Condition> conditions = new ArrayList<>();
		conditions.add(this.getStatusCondition(statusValue));
		return conditions;
	}

}
