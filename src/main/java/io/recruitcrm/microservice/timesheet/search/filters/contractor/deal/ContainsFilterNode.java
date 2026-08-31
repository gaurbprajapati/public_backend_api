package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.contractor.ContractorGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

/**
 * Filter node for "contains" logic - matches contractors that have ALL of the specified
 * deals (but can have additional deals as well). Uses GROUP BY with HAVING COUNT to
 * ensure all specified deals are present.
 */
public class ContainsFilterNode extends ContractorDealFieldBaseFilterNode {

	private final Field<Integer> dealIdField;

	public ContainsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.dealIdField = this.getSearchField(Integer.class);
	}

	@Override
	public List<Condition> getFilterConditions() {
		return this.buildContainsAllConditions(this.dealIdField);
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		return List.of(ContractorGroupBaseFilterNode.CANDIDATE.ID);
	}

	@Override
	public Condition getGroupByHavingCondition() {
		return this.buildContainsAllHaving(this.dealIdField);
	}

	@Override
	public Boolean isSelectDistinct() {
		return DealFilterConditions.selectDistinctFalse();
	}

}
