package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import java.util.List;

import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

public abstract class JobFieldBaseFilterNode extends TimesheetGroupBaseFilterNode {

	protected JobFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	/**
	 * Returns the job ID field for filtering
	 * @return JOB_ID field from cst_timesheet_setting_association_t table
	 */
	protected final Field<Integer> getJobIdField() {
		return TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID;
	}

	@Override
	public final Field<?> getSearchField() {
		return TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Job ID is in cst_timesheet_setting_association_t table
		// We need joins: cst_timesheet_setting_t, cst_timesheet_setting_association_t
		return this.getJobJoins();
	}

}
