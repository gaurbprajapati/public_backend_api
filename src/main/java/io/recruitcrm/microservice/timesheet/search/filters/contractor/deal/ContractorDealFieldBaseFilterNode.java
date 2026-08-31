package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealcandidates;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

public abstract class ContractorDealFieldBaseFilterNode extends ContractorGroupBaseFilterNode {

	protected static final Tbldeals DEALS = Tbldeals.TBLDEALS;

	protected static final Tbldealcandidates DEAL_CANDIDATES = Tbldealcandidates.TBLDEALCANDIDATES;

	protected ContractorDealFieldBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public final Field<?> getSearchField() {
		return DEALS.ID;
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		// Group by contractor ID for exact match counting
		return List.of(ContractorGroupBaseFilterNode.CANDIDATE.ID);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		List<TableJoinSpecification> joins = this.getMinimalJoins();

		// Add deal candidate join (on contractor)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, DEAL_CANDIDATES,
				DEAL_CANDIDATES.CANDIDATEID.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)));

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
				dealIdField, this.getActiveContractorAssignmentCondition());
	}

	protected List<Condition> buildContainsAllConditions(Field<Integer> dealIdField) {
		return DealFilterConditions.containsAtLeast(this.parseIntegerFilterValue(), this.getAccountDealCondition(),
				dealIdField, this.getActiveContractorAssignmentCondition());
	}

	protected Condition buildContainsAllHaving(Field<Integer> dealIdField) {
		return DealFilterConditions.containsAllHaving(this.parseIntegerFilterValue(), dealIdField);
	}

}
