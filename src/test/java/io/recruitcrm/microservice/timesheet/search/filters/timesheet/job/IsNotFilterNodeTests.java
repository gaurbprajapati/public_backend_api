package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("Job IsNotFilterNode Tests")
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
	@DisplayName("Get filter conditions should return condition for valid integer string")
	void testGetFilterConditionsValidIntegerStringReturnsCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("123");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for null filter value")
	void testGetFilterConditionsNullFilterValueReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for empty filter value")
	void testGetFilterConditionsEmptyFilterValueReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("123");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isNotFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "123", "[123]", "[\"123\"]", "\"123\"", "+5" })
	@DisplayName("Get filter conditions should return negated condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnNegatedCondition(String filterValue) {
		// Given
		IsNotFilterNode isNotFilterNode = this.createNode(filterValue);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "abc" })
	@DisplayName("Get filter conditions should return empty list for non-numeric filter value")
	void testGetFilterConditionsNonNumericFilterValueReturnsEmptyList(String filterValue) {
		// Given
		IsNotFilterNode isNotFilterNode = this.createNode(filterValue);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for multi-element array")
	void testGetFilterConditionsMultiElementArrayReturnsEmptyList() {
		// Given
		IsNotFilterNode isNotFilterNode = this.createNode("[1,2]");

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		// Given
		IsNotFilterNode isNotFilterNode = this.createNode("123");

		// When
		List<TableJoinSpecification> joinTables = isNotFilterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		// Given
		IsNotFilterNode isNotFilterNode = this.createNode("123");

		// When
		List<Condition> commonConditions = isNotFilterNode.getCommonFilterCondition();

		// Then
		assertThat(commonConditions).isEmpty();
	}

	private IsNotFilterNode createNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");
		this.filterNodeContext.setFilterDto(filterDto);
		return new IsNotFilterNode(this.filterNodeContext);
	}

}
