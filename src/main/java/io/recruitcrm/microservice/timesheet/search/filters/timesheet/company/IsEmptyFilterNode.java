package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for company IS_EMPTY filter - matches timesheets where the company doesn't
 * exist (company.ID IS NULL). This includes: 1. Unassigned timesheets (job_id IS NULL in
 * timesheet_setting_association, which means company_id is also NULL) 2. Timesheets with
 * deleted companies (job_id exists but company is deleted from tblcompany) This is the
 * negation of HasAnyValueFilterNode.
 */
public class IsEmptyFilterNode extends CompanyFieldBaseFilterNode {

	public IsEmptyFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Check if company.ID is NULL (company doesn't exist in tblcompany)
		// This catches both unassigned timesheets and timesheets with deleted companies
		// Since company table is LEFT JOINed, deleted companies will have company.ID =
		// NULL
		return List.of(TimesheetGroupBaseFilterNode.COMPANY.ID.isNull());
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
