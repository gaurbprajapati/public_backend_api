package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import java.util.List;

import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

/**
 * Base for contractor-deal filter nodes that select DISTINCT contractor rows without any
 * GROUP BY/HAVING clause.
 */
public abstract class DistinctDealFilterNode extends ContractorDealFieldBaseFilterNode {

	protected DistinctDealFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		return DealFilterConditions.noGroupByFields();
	}

	@Override
	public Boolean isSelectDistinct() {
		return DealFilterConditions.selectDistinctTrue();
	}

}
