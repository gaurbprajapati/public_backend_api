package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

/**
 * Filter node for company CONTAINS_AT_LEAST_ONE filter - matches timesheets assigned to
 * jobs with at least one of the specified company IDs, or unassigned timesheets (job_id
 * IS NULL).
 */
public class ContainsAtLeastFilterNode extends CompanyFieldBaseFilterNode {

	private Field<Integer> companyIdField;

	public ContainsAtLeastFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.companyIdField = this.getCompanyIdField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> companyIds = this.parseIntegerFilterValue();

		if (companyIds.isEmpty()) {
			// If no filter values, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Return timesheets assigned to jobs with any of the specified companies OR
		// unassigned
		return List
			.of(this.companyIdField.in(companyIds).or(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID.isNull()));
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
