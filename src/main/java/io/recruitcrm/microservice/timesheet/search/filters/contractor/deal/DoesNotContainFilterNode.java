package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

/**
 * Filter node for "does not contain" logic - matches contractors that do NOT have any of
 * the specified deals. Uses NOT EXISTS subquery to ensure no matching deals exist. This
 * is the opposite of "contains at least one".
 */
public class DoesNotContainFilterNode extends DistinctDealFilterNode {

	public DoesNotContainFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> dealIds = this.parseIntegerFilterValue();

		if (dealIds.isEmpty()) {
			return DealFilterConditions.emptyDealIdsMatchesAll();
		}

		// Use NOT EXISTS to ensure contractor does NOT have ANY of the specified deals
		Condition dealCondition = DSL.notExists(DSL.selectOne()
			.from(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES)
			.innerJoin(ContractorDealFieldBaseFilterNode.DEALS)
			.on(ContractorDealFieldBaseFilterNode.DEALS.ID.eq(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
			.where(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID
				.eq(ContractorGroupBaseFilterNode.CANDIDATE.ID))
			.and(this.getAccountDealCondition())
			.and(ContractorDealFieldBaseFilterNode.DEALS.ID.in(dealIds)));

		return List.of(dealCondition.and(this.getActiveContractorAssignmentCondition()));
	}

}
