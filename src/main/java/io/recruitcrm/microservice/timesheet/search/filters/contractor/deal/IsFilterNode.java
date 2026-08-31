package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

/**
 * Filter node for exact match logic - matches contractors that have exactly the specified
 * deals (all and only those deals). Uses subquery approach similar to timesheet IS
 * filter.
 */
public class IsFilterNode extends DistinctDealFilterNode {

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		// No deal-related joins needed - we filter by contractor ID using subquery
		return this.getMinimalJoins();
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> dealIds = this.parseIntegerFilterValue();

		if (dealIds.isEmpty()) {
			return DealFilterConditions.emptyDealIdsMatchesNothing();
		}

		int expectedCount = dealIds.size();

		// Step 1: Find contractors that have all the specified deals
		// SELECT candidate_id FROM tbldealcandidates WHERE dealid IN (dealIds) GROUP BY
		// candidate_id HAVING count(*) = expectedCount
		Field<Integer> candidateIdField = ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID;
		Select<?> contractorsWithDeals = DSL.select(candidateIdField)
			.from(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES)
			.innerJoin(ContractorDealFieldBaseFilterNode.DEALS)
			.on(ContractorDealFieldBaseFilterNode.DEALS.ID.eq(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
			.where(this.getAccountDealCondition())
			.and(ContractorDealFieldBaseFilterNode.DEALS.ID.in(dealIds))
			.groupBy(candidateIdField)
			.having(DSL.count().eq(expectedCount));

		// Step 2: Filter out contractors that have deals outside the specified list
		// Using NOT EXISTS to ensure no deals outside the filter list
		// Create table alias for the subquery
		Table<?> dealTable = contractorsWithDeals.asTable("deal");
		Field<Integer> dealCandidateIdField = dealTable.field(candidateIdField.getName(), Integer.class);

		Select<? extends Record1<Integer>> matchingContractorIds = DSL.select(dealCandidateIdField)
			.from(dealTable)
			.where(DSL.notExists(DSL.selectOne()
				.from(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES)
				.innerJoin(ContractorDealFieldBaseFilterNode.DEALS)
				.on(ContractorDealFieldBaseFilterNode.DEALS.ID
					.eq(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
				.where(dealCandidateIdField.eq(ContractorDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID))
				.and(this.getAccountDealCondition())
				.and(ContractorDealFieldBaseFilterNode.DEALS.ID.notIn(dealIds))));

		return List.of(ContractorGroupBaseFilterNode.CANDIDATE.ID.in(matchingContractorIds)
			.and(this.getActiveContractorAssignmentCondition()));
	}

}
