package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for job IS filter - matches timesheets assigned to the specified job ID, or
 * unassigned timesheets (where job_id IS NULL in timesheet_setting_association).
 * Unassigned timesheets are those that were created for a job but the job_id is NULL.
 */
public class IsFilterNode extends JobFieldBaseFilterNode {

	private Field<Integer> jobIdField;

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.jobIdField = this.getJobIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		Integer jobId = this.parseFilterValue();

		if (jobId == null) {
			// If no job ID provided, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Return timesheets assigned to this job OR unassigned (job_id IS NULL)
		// Unassigned timesheets are those that have a record in
		// timesheet_setting_association
		// but job_id is NULL
		return List.of(this.jobIdField.eq(jobId).or(this.jobIdField.isNull()));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

	/**
	 * Parses filterValue as a single job ID. Can be: 1. Single integer: "123" 2. JSON
	 * number: 123 3. JSON array with single element: "[1902882]"
	 * @return Job ID or null if invalid
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

			// Handle JSON array with single element (e.g., "[1902882]")
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
