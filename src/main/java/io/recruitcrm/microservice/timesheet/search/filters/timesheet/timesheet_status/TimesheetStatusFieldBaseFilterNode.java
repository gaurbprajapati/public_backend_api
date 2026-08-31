package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import java.util.List;

import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;

public abstract class TimesheetStatusFieldBaseFilterNode extends TimesheetGroupBaseFilterNode {

	protected static final CstTimesheetApprovalT TIMESHEET_APPROVAL = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;

	protected TimesheetStatusFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	/**
	 * Returns the timesheet approval status type ID field for filtering
	 * @return TIMESHEET_APPROVAL_STATUS_TYPE_ID field from cst_timesheet_approval_t table
	 */
	protected final Field<Integer> getStatusIdField() {
		return TIMESHEET_APPROVAL.TIMESHEET_APPROVAL_STATUS_TYPE_ID;
	}

	@Override
	public final Field<?> getSearchField() {
		return TIMESHEET_APPROVAL.TIMESHEET_APPROVAL_STATUS_TYPE_ID;
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		List<TableJoinSpecification> joins = this.getMinimalJoins();

		// Join timesheet approval table to get the latest approval status
		// We need to join to get the latest approval for each timesheet
		// Using LEFT JOIN because a timesheet might not have an approval record yet
		// Create a subquery to get the max ID (latest approval) for each timesheet
		org.jooq.Select<? extends org.jooq.Record1<Integer>> latestApprovalSubquery = DSL
			.select(DSL.max(TIMESHEET_APPROVAL.ID))
			.from(TIMESHEET_APPROVAL)
			.where(TIMESHEET_APPROVAL.TIMESHEET_ID.eq(TS.ID));

		// Join condition: match timesheet_id and ensure we get the latest approval (max
		// ID)
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TIMESHEET_APPROVAL,
				TIMESHEET_APPROVAL.TIMESHEET_ID.eq(TS.ID).and(TIMESHEET_APPROVAL.ID.eq(latestApprovalSubquery))));

		return joins;
	}

}
