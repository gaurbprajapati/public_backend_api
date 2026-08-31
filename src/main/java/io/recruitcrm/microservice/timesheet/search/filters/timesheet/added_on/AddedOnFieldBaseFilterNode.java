package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import java.util.List;

import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

public abstract class AddedOnFieldBaseFilterNode extends TimesheetGroupBaseFilterNode {

	protected AddedOnFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public final Field<?> getSearchField() {
		return TimesheetGroupBaseFilterNode.TS.ADDED_ON;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// added_on is a field directly on cst_timesheet_t table
		// We only need cst_timesheet_setting_t for the account_id filter
		return this.getMinimalJoins();
	}

}
