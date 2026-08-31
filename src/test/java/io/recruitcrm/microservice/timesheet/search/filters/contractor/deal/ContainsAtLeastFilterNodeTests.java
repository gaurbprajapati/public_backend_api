package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("ContractorDeal ContainsAtLeastFilterNode Tests")
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

	@Test
	@DisplayName("Get filter conditions should return false condition when no valid deal IDs are parsed")
	void testGetFilterConditionsReturnsFalseConditionWhenDealIdsEmpty() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("invalid");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).hasSize(SINGLE_CONDITION_COUNT);
		assertThat(conditions.get(0).toString()).containsIgnoringCase("false");
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "1,2,3", "1,invalid,3", "1,,2" })
	@DisplayName("Get filter conditions should return condition for deal ID filter values")
	void testGetFilterConditionsReturnsConditionForFilterValue(String filterValue) {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("validDealIdFilterValues")
	@DisplayName("Get filter conditions should build a deal IN condition that is not a false condition for valid values")
	void testGetFilterConditionsValidDealIdsReturnsNonFalseCondition(String testCase, String filterValue) {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).as(testCase)
			.isNotNull()
			.hasSize(SINGLE_CONDITION_COUNT)
			.first()
			.isNotEqualTo(DSL.falseCondition());
	}

	private static Stream<Arguments> validDealIdFilterValues() {
		return Stream.of(Arguments.of("comma-separated deal IDs", "1,2,3"),
				Arguments.of("JSON array with integers", "[1,2,3]"),
				Arguments.of("JSON array with string numbers", "[\"1\", \"2\", \"3\"]"),
				Arguments.of("JSON array with non-numeric textual element", "[\"1\", \"x\", \"2\"]"),
				Arguments.of("JSON array with non-int non-textual element", "[true, 1]"),
				Arguments.of("non-array JSON fallback", "{\"dealId\":1}"));
	}

	@Test
	@DisplayName("Get group by fields should return empty list")
	void testGetGroupByFieldsReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<org.jooq.Field<?>> groupByFields = containsAtLeastFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isEmpty();
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		Condition havingCondition = containsAtLeastFilterNode.getGroupByHavingCondition();

		// Then
		assertThat(havingCondition).isNotNull().isEqualTo(DSL.noCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = containsAtLeastFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
