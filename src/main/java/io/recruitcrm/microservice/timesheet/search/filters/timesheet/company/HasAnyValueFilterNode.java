package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for company HAS_ANY_VALUE filter - matches timesheets that are assigned to
 * jobs with any existing company (company.ID IS NOT NULL). No filter value is required,
 * just checks that company exists in tblcompany.
 */
public class HasAnyValueFilterNode extends CompanyFieldBaseFilterNode {

	public HasAnyValueFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Return timesheets assigned to any existing company (company.ID IS NOT NULL)
		// Since company table is LEFT JOINed, only existing companies will have
		// company.ID != NULL
		return List.of(TimesheetGroupBaseFilterNode.COMPANY.ID.isNotNull());
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
