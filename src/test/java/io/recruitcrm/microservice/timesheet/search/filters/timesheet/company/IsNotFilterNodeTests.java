package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
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
@DisplayName("Company IsNotFilterNode Tests")
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
		filterDto.setDbField("companyName");
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
	@DisplayName("Get filter conditions should return condition for JSON array with single integer")
	void testGetFilterConditionsJsonArrayWithSingleIntegerReturnsCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("[123]");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "[123,456]", "invalid" })
	@DisplayName("Get filter conditions should return empty list for invalid or null filter value")
	void testGetFilterConditionsInvalidFilterValueReturnsEmptyList(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		assertThat(conditions).isEmpty();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "[\"123\"]", "\"123\"", "01", "+5" })
	@DisplayName("Get filter conditions should return condition for textual array, textual scalar and non-JSON integer")
	void testGetFilterConditionsTextualAndNonJsonIntegerReturnsCondition(String filterValue) {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
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

}
