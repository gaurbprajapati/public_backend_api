package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for job DOES_NOT_CONTAIN filter - matches timesheets that are NOT assigned
 * to any of the specified job IDs. This is the opposite of "contains at least one".
 * Includes unassigned timesheets (job_id IS NULL).
 */
public class DoesNotContainFilterNode extends JobFieldBaseFilterNode {

	private Field<Integer> jobIdField;

	public DoesNotContainFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.jobIdField = this.getJobIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> jobIds = this.parseIntegerFilterValue();

		if (jobIds.isEmpty()) {
			// If no filter values, return no condition (matches all)
			return List.of();
		}

		// Return timesheets NOT assigned to any of the specified jobs
		// This includes unassigned timesheets (job_id IS NULL) OR jobs not in the list
		return List.of(this.jobIdField.isNull().or(this.jobIdField.notIn(jobIds)));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
