package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import java.util.List;

import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

public abstract class CompanyFieldBaseFilterNode extends TimesheetGroupBaseFilterNode {

	protected CompanyFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	/**
	 * Returns the company ID field for filtering
	 * @return ID field from tblcompany table
	 */
	protected final Field<Integer> getCompanyIdField() {
		return TimesheetGroupBaseFilterNode.COMPANY.ID;
	}

	@Override
	public final Field<?> getSearchField() {
		return TimesheetGroupBaseFilterNode.COMPANY.ID;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Company ID is accessed via job.COMPANYID -> company.ID
		// We need joins: cst_timesheet_setting_t, cst_timesheet_setting_association_t,
		// tbljob, tblcompany
		return this.getCompanyJoins();
	}

}
