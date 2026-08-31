package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for job IS_NOT filter - matches timesheets assigned to jobs other than the
 * specified job ID. Excludes unassigned timesheets (only shows assigned jobs that are not
 * the specified job).
 */
public class IsNotFilterNode extends JobFieldBaseFilterNode {

	private Field<Integer> jobIdField;

	public IsNotFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.jobIdField = this.getJobIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		Integer jobId = this.parseFilterValue();

		if (jobId == null) {
			// If no job ID provided, return no condition (matches all)
			return List.of();
		}

		// Return timesheets assigned to jobs other than the specified job
		// Exclude unassigned timesheets (job_id IS NOT NULL) and exclude the specified
		// job
		return List.of(this.jobIdField.isNotNull().and(this.jobIdField.ne(jobId)));
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
				// If array has more than one element, return null (IS_NOT filter expects
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
