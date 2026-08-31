package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for job IS_EMPTY filter - matches timesheets where the job doesn't exist
 * (job.ID IS NULL). This includes: 1. Unassigned timesheets (job_id IS NULL in
 * timesheet_setting_association) 2. Timesheets with deleted jobs (job_id exists but job
 * is deleted from tbljob) This is the negation of HasAnyValueFilterNode.
 */
public class IsEmptyFilterNode extends JobFieldBaseFilterNode {

	public IsEmptyFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Check if job.ID is NULL (job doesn't exist in tbljob)
		// This catches both unassigned timesheets and timesheets with deleted jobs
		// Since job table is LEFT JOINed, deleted jobs will have job.ID = NULL
		return List.of(TimesheetGroupBaseFilterNode.JOB.ID.isNull());
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
