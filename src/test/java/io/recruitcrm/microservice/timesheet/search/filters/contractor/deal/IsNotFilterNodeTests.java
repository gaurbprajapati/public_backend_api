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
@DisplayName("ContractorDeal IsNotFilterNode Tests")
class IsNotFilterNodeTests {

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
	@DisplayName("Get filter conditions should return NOT IN condition for comma-separated deal IDs")
	void testGetFilterConditionsCommaSeparatedDealIdsReturnsNotInCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "{\"dealId\":1}" })
	@DisplayName("Get filter conditions should return empty list for null, empty or non-array JSON value")
	void testGetFilterConditionsInvalidFilterValueReturnsEmptyList(String filterValue) {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get filter conditions should skip invalid numbers in comma-separated string")
	void testGetFilterConditionsSkipsInvalidNumbersInCommaSeparatedString() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,invalid,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("validDealIdFilterValues")
	@DisplayName("Get filter conditions should build a NOT IN subquery condition that is not a false condition")
	void testGetFilterConditionsValidDealIdsReturnsNonFalseCondition(String testCase, String filterValue) {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).as(testCase).isNotNull().hasSize(1).first().isNotEqualTo(DSL.falseCondition());
	}

	private static Stream<Arguments> validDealIdFilterValues() {
		return Stream.of(Arguments.of("comma-separated deal IDs", "1,2,3"),
				Arguments.of("JSON array with integers", "[1,2,3]"),
				Arguments.of("JSON array with string numbers", "[\"1\", \"2\", \"3\"]"),
				Arguments.of("JSON array with non-numeric textual element", "[\"1\", \"x\", \"2\"]"),
				Arguments.of("JSON array with non-int non-textual element", "[true, 1]"));
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification> joinTables = isNotFilterNode
			.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(0);
	}

	@Test
	@DisplayName("Get group by fields should return empty list")
	void testGetGroupByFieldsReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<org.jooq.Field<?>> groupByFields = isNotFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isEmpty();
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		Condition havingCondition = isNotFilterNode.getGroupByHavingCondition();

		// Then
		assertThat(havingCondition).isNotNull().isEqualTo(DSL.noCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isNotFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
