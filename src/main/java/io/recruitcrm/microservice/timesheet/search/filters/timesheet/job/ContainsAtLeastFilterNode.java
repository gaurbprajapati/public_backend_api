package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for job CONTAINS_AT_LEAST_ONE filter - matches timesheets assigned to at
 * least one of the specified job IDs, or unassigned timesheets (job_id IS NULL).
 */
public class ContainsAtLeastFilterNode extends JobFieldBaseFilterNode {

	private Field<Integer> jobIdField;

	public ContainsAtLeastFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.jobIdField = this.getJobIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> jobIds = this.parseIntegerFilterValue();

		if (jobIds.isEmpty()) {
			// If no filter values, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Return timesheets assigned to any of the specified jobs OR unassigned
		return List.of(this.jobIdField.in(jobIds).or(this.jobIdField.isNull()));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
