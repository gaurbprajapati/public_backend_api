package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

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

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("Company ContainsAtLeastFilterNode Tests")
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
	@ValueSource(strings = { "1,2,3", "[1,2,3]", "1,invalid,3" })
	@DisplayName("Get filter conditions should return IN condition for valid filter value")
	void testGetFilterConditionsValidFilterValueReturnsInCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "abc,def" })
	@DisplayName("Get filter conditions should return false condition for blank or null filter value")
	void testGetFilterConditionsBlankOrNullFilterValueReturnsFalseCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "[\"1\",\"2\"]", "[\"1\",\"abc\"]", "[true, 1]" })
	@DisplayName("Get filter conditions should return IN condition for JSON array with textual elements")
	void testGetFilterConditionsTextualJsonArrayReturnsInCondition(String filterValue) {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = containsAtLeastFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		Boolean isDistinct = containsAtLeastFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

}
