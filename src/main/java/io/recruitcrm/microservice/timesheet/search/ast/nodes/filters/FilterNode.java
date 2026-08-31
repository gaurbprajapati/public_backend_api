package io.recruitcrm.microservice.timesheet.search.ast.nodes.filters;

import org.jooq.SelectQuery;

import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.IFilterNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterNode {

	private final IFilterNode filterNodeImpl;

	private final FilterNodeContext filterNodeContext;

	public FilterNode(IFilterNode filterNodeImpl, FilterNodeContext filterNodeContext) {
		this.filterNodeImpl = filterNodeImpl;
		this.filterNodeContext = filterNodeContext;
	}

	public SelectQuery<?> toSQL() {
		return this.filterNodeImpl.getCteQuery();
	}

	public SubGroupComparisonOperator getSubgroupComparisonOperator() {
		return this.filterNodeImpl.getSubGroupComparisonOperator();
	}

}
