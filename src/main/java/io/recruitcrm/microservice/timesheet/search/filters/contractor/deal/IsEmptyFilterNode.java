package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;

/**
 * Filter node for "is empty" logic - shows contractors that are NOT assigned to any deal.
 * Uses LEFT JOINs and checks for NULL deals.
 */
public class IsEmptyFilterNode extends DistinctDealFilterNode {

	public IsEmptyFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		List<TableJoinSpecification> joins = this.getMinimalJoins();

		// Use LEFT JOINs for deal-related tables to include contractors without deals
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES,
				ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID
					.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID)));

		joins.add(new TableJoinSpecification(TableJoinType.LEFT, ContractorDealFieldBaseFilterNode.DEALS,
				ContractorDealFieldBaseFilterNode.DEALS.ID.eq(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID)
					.and(this.getAccountDealCondition())));

		return joins;
	}

	@Override
	public List<Condition> getFilterConditions() {
		Condition dealCondition = ContractorDealFieldBaseFilterNode.DEALS.ID.isNull();

		return List.of(dealCondition.and(this.getActiveContractorAssignmentCondition()));
	}

}
