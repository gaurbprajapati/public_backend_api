package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
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
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("Job IsFilterNode Tests")
class IsFilterNodeTests {

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
	@DisplayName("Get filter conditions should return condition for valid integer string")
	void testGetFilterConditionsValidIntegerStringReturnsCondition() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("123");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should return condition for JSON array with single integer")
	void testGetFilterConditionsJsonArrayWithSingleIntegerReturnsCondition() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("[123]");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "[123,456]", "invalid" })
	@DisplayName("Get filter conditions should return false condition for invalid filter value")
	void testGetFilterConditionsInvalidFilterValueReturnsFalseCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("123");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		Boolean isDistinct = isFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "123", "[123]", "[\"123\"]", "\"123\"", "+5" })
	@DisplayName("Get filter conditions should return match condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnMatchCondition(String filterValue) {
		// Given
		IsFilterNode isFilterNode = this.createNode(filterValue);

		// When
		List<Condition> conditions = isFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		// Given
		IsFilterNode isFilterNode = this.createNode("123");

		// When
		List<TableJoinSpecification> joinTables = isFilterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		// Given
		IsFilterNode isFilterNode = this.createNode("123");

		// When
		List<Condition> commonConditions = isFilterNode.getCommonFilterCondition();

		// Then
		assertThat(commonConditions).isEmpty();
	}

	private IsFilterNode createNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");
		this.filterNodeContext.setFilterDto(filterDto);
		return new IsFilterNode(this.filterNodeContext);
	}

}
