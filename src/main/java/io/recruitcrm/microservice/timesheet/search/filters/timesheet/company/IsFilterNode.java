package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for company IS filter - matches timesheets assigned to jobs with the
 * specified company ID, or unassigned timesheets (where job_id IS NULL in
 * timesheet_setting_association, which means company_id is also NULL).
 */
public class IsFilterNode extends CompanyFieldBaseFilterNode {

	private Field<Integer> companyIdField;

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.companyIdField = this.getCompanyIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		Integer companyId = this.parseFilterValue();

		if (companyId == null) {
			// If no company ID provided, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Return timesheets assigned to jobs with this company OR unassigned (job_id IS
		// NULL means company_id is also NULL)
		return List
			.of(this.companyIdField.eq(companyId).or(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.isNull()));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

	/**
	 * Parses filterValue as a single company ID. Can be: 1. Single integer: "123" 2. JSON
	 * number: 123 3. JSON array with single element: "[123]"
	 * @return Company ID or null if invalid
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

			// Handle JSON array with single element (e.g., "[123]")
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
