package io.recruitcrm.microservice.timesheet.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupORNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupORNode;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.SearchFieldConstants;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.search.dto.GroupFilterListDto;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("FilterSearchASTBuilder Tests")
class FilterSearchASTBuilderTests {

	private static final int SINGLE_SUBGROUP_COUNT = 1;

	private static final int MULTIPLE_FILTERS_COUNT = 2;

	private static final int MULTIPLE_SUBGROUPS_COUNT = 2;

	private FilterSearchASTBuilder filterSearchASTBuilder;

	private final Integer accountId = 1;

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterSearchASTBuilder = new FilterSearchASTBuilder(this.accountId, this.gmtDifference);
	}

	@Test
	@DisplayName("buildFilterASTTree should create GroupANDNode for AND groupJoinOperator")
	void testBuildFilterASTTreeForAND() {
		FilterSearchListDto filterSearchListDto = createFilterSearchListDto("AND", "OR");

		GroupConjointNode result = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);

		assertThat(result).isNotNull().isInstanceOf(GroupANDNode.class);
	}

	@Test
	@DisplayName("buildFilterASTTree should create GroupORNode for OR groupJoinOperator")
	void testBuildFilterASTTreeForOR() {
		FilterSearchListDto filterSearchListDto = createFilterSearchListDto("OR", "AND");

		GroupConjointNode result = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);

		assertThat(result).isNotNull().isInstanceOf(GroupORNode.class);
	}

	@Test
	@DisplayName("buildFilterASTTree should create SubGroupANDNode for AND groupFilterJoinOperator")
	void testBuildFilterASTTreeWithSubGroupAND() {
		FilterSearchListDto filterSearchListDto = createFilterSearchListDto("AND", "AND");

		GroupConjointNode result = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);

		assertThat(result).isNotNull();
		assertThat(result.getSubGroups()).isNotEmpty();
		assertThat(result.getSubGroups().get(0)).isInstanceOf(SubGroupANDNode.class);
	}

	@Test
	@DisplayName("buildFilterASTTree should create SubGroupORNode for OR groupFilterJoinOperator")
	void testBuildFilterASTTreeWithSubGroupOR() {
		FilterSearchListDto filterSearchListDto = createFilterSearchListDto("AND", "OR");

		GroupConjointNode result = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);

		assertThat(result).isNotNull();
		assertThat(result.getSubGroups()).isNotEmpty();
		assertThat(result.getSubGroups().get(0)).isInstanceOf(SubGroupORNode.class);
	}

	@Test
	@DisplayName("buildFilterASTTree should add multiple filters to subGroup")
	void testBuildFilterASTTreeWithMultipleFilters() {
		FilterSearchListDto filterSearchListDto = new FilterSearchListDto();
		filterSearchListDto.setGroupJoinOperator("AND");

		GroupFilterListDto groupFilterListDto = new GroupFilterListDto();
		groupFilterListDto.setGroupFilterJoinOperator("OR");

		FilterDto filterDto1 = createFilterDto(SearchFieldConstants.FIELD_ADDED_ON, FilterTypes.IS, "TODAY");
		FilterDto filterDto2 = createFilterDto(SearchFieldConstants.FIELD_TIMESHEET_PERIOD, FilterTypes.IS,
				"THIS_MONTH");

		groupFilterListDto.setFilters(List.of(filterDto1, filterDto2));
		filterSearchListDto.setGroupFilterList(List.of(groupFilterListDto));

		GroupConjointNode result = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);

		assertThat(result).isNotNull();
		assertThat(result.getSubGroups()).hasSize(SINGLE_SUBGROUP_COUNT);
		SubGroupConjointNode subGroup = result.getSubGroups().get(0);
		assertThat(subGroup.getFilters()).hasSize(MULTIPLE_FILTERS_COUNT);
	}

	@Test
	@DisplayName("buildFilterASTTree should handle multiple groupFilterLists")
	void testBuildFilterASTTreeWithMultipleGroupFilterLists() {
		FilterSearchListDto filterSearchListDto = new FilterSearchListDto();
		filterSearchListDto.setGroupJoinOperator("AND");

		GroupFilterListDto groupFilterListDto1 = new GroupFilterListDto();
		groupFilterListDto1.setGroupFilterJoinOperator("OR");
		groupFilterListDto1
			.setFilters(List.of(createFilterDto(SearchFieldConstants.FIELD_ADDED_ON, FilterTypes.IS, "TODAY")));

		GroupFilterListDto groupFilterListDto2 = new GroupFilterListDto();
		groupFilterListDto2.setGroupFilterJoinOperator("AND");
		groupFilterListDto2
			.setFilters(List.of(createFilterDto(SearchFieldConstants.FIELD_COMPANY_NAME, FilterTypes.IS, "1,2,3")));

		filterSearchListDto.setGroupFilterList(List.of(groupFilterListDto1, groupFilterListDto2));

		GroupConjointNode result = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);

		assertThat(result).isNotNull();
		assertThat(result.getSubGroups()).hasSize(MULTIPLE_SUBGROUPS_COUNT);
	}

	private FilterSearchListDto createFilterSearchListDto(String groupJoinOperator, String groupFilterJoinOperator) {
		FilterSearchListDto filterSearchListDto = new FilterSearchListDto();
		filterSearchListDto.setGroupJoinOperator(groupJoinOperator);

		GroupFilterListDto groupFilterListDto = new GroupFilterListDto();
		groupFilterListDto.setGroupFilterJoinOperator(groupFilterJoinOperator);

		FilterDto filterDto = createFilterDto(SearchFieldConstants.FIELD_ADDED_ON, FilterTypes.IS, "TODAY");
		groupFilterListDto.setFilters(List.of(filterDto));
		filterSearchListDto.setGroupFilterList(List.of(groupFilterListDto));

		return filterSearchListDto;
	}

	private FilterDto createFilterDto(String dbField, FilterTypes filterType, String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(dbField);
		filterDto.setFilterType(filterType);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");
		return filterDto;
	}

}
