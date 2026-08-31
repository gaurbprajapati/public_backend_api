package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet status IS filter - matches timesheets with the specified
 * approval status ID. Status values: OPEN(1), SUBMITTED(2), REJECTED(3), APPROVED(4).
 */
public class IsFilterNode extends TimesheetStatusFieldBaseFilterNode {

	private Field<Integer> statusIdField;

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.statusIdField = this.getStatusIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		Integer statusId = this.parseFilterValue();

		if (statusId == null) {
			// If no status ID provided, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Return timesheets with the specified status ID
		return List.of(this.statusIdField.eq(statusId));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

	/**
	 * Parses filterValue as a single status ID. Can be: 1. Single integer: "1" (OPEN),
	 * "2" (SUBMITTED), "3" (REJECTED), "4" (APPROVED) 2. JSON number: 1, 2, 3, or 4 3.
	 * JSON array with single element: "[1]"
	 * @return Status ID or null if invalid
	 */
	private Integer parseFilterValue() {
		String filterValue = this.filterNodeContext.getFilterDto().getFilterValue();
		if (filterValue == null || filterValue.trim().isEmpty()) {
			return null;
		}

		try {
			// Try parsing as JSON first
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(filterValue);

			// Handle JSON array with single element (e.g., "[1]")
			if (jsonNode.isArray()) {
				if (jsonNode.size() == 1) {
					com.fasterxml.jackson.databind.JsonNode firstElement = jsonNode.get(0);
					if (firstElement.isInt()) {
						return firstElement.asInt();
					}
					else if (firstElement.isTextual()) {
						return Integer.parseInt(firstElement.asText().trim());
					}
				}
				// If array has more than one element, return null (IS filter expects
				// single
				// value)
				return null;
			}

			// Handle JSON number
			if (jsonNode.isInt()) {
				return jsonNode.asInt();
			}
			else if (jsonNode.isTextual()) {
				return Integer.parseInt(jsonNode.asText().trim());
			}
		}
		catch (Exception ex) {
			// Not JSON, try parsing as integer string
		}

		// Parse as integer string
		try {
			return Integer.parseInt(filterValue.trim());
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

}
