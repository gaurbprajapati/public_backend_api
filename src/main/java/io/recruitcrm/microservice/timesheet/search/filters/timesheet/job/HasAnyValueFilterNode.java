package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for job HAS_ANY_VALUE filter - matches timesheets that are assigned to any
 * existing job (job.ID IS NOT NULL). No filter value is required, just checks that job
 * exists in tbljob.
 */
public class HasAnyValueFilterNode extends JobFieldBaseFilterNode {

	public HasAnyValueFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Return timesheets assigned to any existing job (job.ID IS NOT NULL)
		// Since job table is LEFT JOINed, only existing jobs will have job.ID != NULL
		return List.of(TimesheetGroupBaseFilterNode.JOB.ID.isNotNull());
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
