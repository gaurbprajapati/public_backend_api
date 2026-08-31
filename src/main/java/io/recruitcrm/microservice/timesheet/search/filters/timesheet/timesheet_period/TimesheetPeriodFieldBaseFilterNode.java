package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.util.List;

import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;

public abstract class TimesheetPeriodFieldBaseFilterNode extends TimesheetGroupBaseFilterNode {

	protected TimesheetPeriodFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	/**
	 * Returns the period_start field for filtering
	 * @return PERIOD_START field
	 */
	protected final Field<Integer> getPeriodStartField() {
		return TimesheetGroupBaseFilterNode.TS.PERIOD_START;
	}

	/**
	 * Returns the period_end field for filtering
	 * @return PERIOD_END field
	 */
	protected final Field<Integer> getPeriodEndField() {
		return TimesheetGroupBaseFilterNode.TS.PERIOD_END;
	}

	@Override
	public final Field<?> getSearchField() {
		// For timesheet period, we use period_start as the primary search field
		// The actual filtering logic uses both period_start and period_end
		return TimesheetGroupBaseFilterNode.TS.PERIOD_START;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// timesheet period fields (period_start, period_end) are directly on
		// cst_timesheet_t table
		// We only need cst_timesheet_setting_t for the account_id filter
		return this.getMinimalJoins();
	}

}
