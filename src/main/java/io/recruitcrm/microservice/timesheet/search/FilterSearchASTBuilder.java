package io.recruitcrm.microservice.timesheet.search;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.NodeFactory;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.filters.FilterNode;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.search.dto.GroupFilterListDto;

public class FilterSearchASTBuilder {

	private final Integer accountId;

	private final String gmtDifference;

	public FilterSearchASTBuilder(Integer accountId, String gmtDifference) {
		this.accountId = accountId;
		this.gmtDifference = gmtDifference;
	}

	public GroupConjointNode buildFilterASTTree(FilterSearchListDto filterSearchListDto) {
		NodeFactory nodeFactory = this.createNodeFactory();
		// Create Group node
		GroupConjointNode root = nodeFactory.createGroupConjointNode(filterSearchListDto.getGroupJoinOperator());
		for (GroupFilterListDto groupFilterListDto : filterSearchListDto.getGroupFilterList()) {
			// Create Sub Group node
			SubGroupConjointNode subGroupNode = nodeFactory
				.createSubGroupConjointNode(groupFilterListDto.getGroupFilterJoinOperator());
			for (FilterDto filterDto : groupFilterListDto.getFilters()) {
				// Create filter node
				FilterNode filterNode = nodeFactory.createFilterNode(filterDto);
				subGroupNode.addChild(filterNode);
			}
			root.addChild(subGroupNode);
		}
		return root;
	}

	private NodeFactory createNodeFactory() {
		return new NodeFactory(this.accountId, this.gmtDifference);
	}

}
