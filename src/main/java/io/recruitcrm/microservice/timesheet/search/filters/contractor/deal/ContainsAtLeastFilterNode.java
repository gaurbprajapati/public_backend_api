package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for "contains at least" logic - matches contractors that have at least one
 * of the specified deals. Uses simple WHERE IN condition without GROUP BY/HAVING.
 */
public class ContainsAtLeastFilterNode extends DistinctDealFilterNode {

	private final Field<Integer> dealIdField;

	public ContainsAtLeastFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.dealIdField = this.getSearchField(Integer.class);
	}

	@Override
	public List<Condition> getFilterConditions() {
		return this.buildContainsAtLeastConditions(this.dealIdField);
	}

}
