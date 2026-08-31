package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("AssociatedDeal ContainsAtLeastFilterNode Tests")
class ContainsAtLeastFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = 1;

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(
			strings = { "1,2,3", "[1,2,3]", "", "1,invalid,3", "1", "[\"1\",\"2\"]", "[\"1\",\"abc\"]", "[true, 1]" })
	@DisplayName("Get filter conditions should return condition for filter value")
	void testGetFilterConditionsReturnsCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get group by fields should return empty list")
	void testGetGroupByFieldsReturnsEmptyList() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		List<Field<?>> groupByFields = containsAtLeastFilterNode.getGroupByFields();

		assertThat(groupByFields).isEmpty();
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		Condition havingCondition = containsAtLeastFilterNode.getGroupByHavingCondition();

		assertThat(havingCondition).isNotNull().isEqualTo(DSL.noCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		Boolean isDistinct = containsAtLeastFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

}
