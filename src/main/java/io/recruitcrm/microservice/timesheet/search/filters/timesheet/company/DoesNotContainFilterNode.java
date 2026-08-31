package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for company DOES_NOT_CONTAIN filter - matches timesheets that are NOT
 * assigned to jobs with any of the specified company IDs. This is the opposite of
 * "contains at least one". Includes unassigned timesheets (job_id IS NULL).
 */
public class DoesNotContainFilterNode extends CompanyFieldBaseFilterNode {

	private Field<Integer> companyIdField;

	public DoesNotContainFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.companyIdField = this.getCompanyIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> companyIds = this.parseIntegerFilterValue();

		if (companyIds.isEmpty()) {
			// If no filter values, return no condition (matches all)
			return List.of();
		}

		// Return timesheets NOT assigned to jobs with any of the specified companies
		// This includes unassigned timesheets (job_id IS NULL) OR companies not in the
		// list
		return List.of(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.isNull()
			.or(this.companyIdField.notIn(companyIds)));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
