package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealcandidates;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealjobs;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

public abstract class AssociatedDealFieldBaseFilterNode extends TimesheetGroupBaseFilterNode {

	protected static final Tbldeals DEALS = Tbldeals.TBLDEALS;

	protected static final Tbldealcandidates DEAL_CANDIDATES = Tbldealcandidates.TBLDEALCANDIDATES;

	protected static final Tbldealjobs DEAL_JOBS = Tbldealjobs.TBLDEALJOBS;

	protected AssociatedDealFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public final Field<?> getSearchField() {
		return DEALS.ID;
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		// Group by timesheet ID for exact match counting
		return List.of(TimesheetGroupBaseFilterNode.TS.ID);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		List<TableJoinSpecification> joins = this.getMinimalJoins();

		// Add association join (needed for contractor and job IDs)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC,
				TimesheetGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.ID)));

		// Add deal candidate join (on contractor)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, DEAL_CANDIDATES,
				DEAL_CANDIDATES.CANDIDATEID.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID)));

		// Add deal job join (on job and matching deal)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, DEAL_JOBS,
				DEAL_JOBS.JOBID.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID)
					.and(DEAL_JOBS.DEALID.eq(DEAL_CANDIDATES.DEALID))));

		// Add deals table join
		joins.add(new TableJoinSpecification(TableJoinType.INNER, DEALS, DEALS.ID.eq(DEAL_CANDIDATES.DEALID)));

		return joins;
	}

	@Override
	public Boolean isSelectDistinct() {
		// Using GROUP BY instead of DISTINCT
		return false;
	}

	protected Condition getAccountDealCondition() {
		return DEALS.ACCOUNTID.eq(this.filterNodeContext.getAccountId());
	}

	protected List<Condition> buildContainsAtLeastConditions(Field<Integer> dealIdField) {
		return DealFilterConditions.containsAtLeast(this.parseIntegerFilterValue(), this.getAccountDealCondition(),
				dealIdField, null);
	}

	protected List<Condition> buildContainsAllConditions(Field<Integer> dealIdField) {
		return DealFilterConditions.containsAtLeast(this.parseIntegerFilterValue(), this.getAccountDealCondition(),
				dealIdField, null);
	}

	protected Condition buildContainsAllHaving(Field<Integer> dealIdField) {
		return DealFilterConditions.containsAllHaving(this.parseIntegerFilterValue(), dealIdField);
	}

}
