package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

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
@DisplayName("TimesheetPeriod IsLessThanFilterNode Tests")
class IsLessThanFilterNodeTests {

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
	@DisplayName("Get filter conditions should return overlap condition for valid days value")
	void testGetFilterConditionsValidDaysValueReturnsOverlapCondition() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue("10");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		List<Condition> conditions = filterNode.getFilterConditions();

		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT);
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "-5", "invalid" })
	@DisplayName("Get filter conditions should return false condition for invalid filter value")
	void testGetFilterConditionsInvalidFilterValueReturnsFalseCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		List<Condition> conditions = filterNode.getFilterConditions();

		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "10", "\"10\"", "3000000000", "+10" })
	@DisplayName("Get filter conditions should parse all valid value variants and return single condition")
	void testGetFilterConditionsValidParseVariantsReturnsSingleCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		List<Condition> conditions = filterNode.getFilterConditions();

		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Get filter conditions should apply GMT offset correctly")
	void testGetFilterConditionsAppliesGmtOffsetCorrectly() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue("10");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference("+05:30");
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		List<Condition> conditions = filterNode.getFilterConditions();

		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Get filter conditions should use default GMT offset when null")
	void testGetFilterConditionsNullGmtDifferenceUsesDefaultOffset() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue("10");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference(null);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		List<Condition> conditions = filterNode.getFilterConditions();

		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Get filter conditions should handle zero days value")
	void testGetFilterConditionsZeroDaysValueHandlesCorrectly() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue("0");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		List<Condition> conditions = filterNode.getFilterConditions();

		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue("10");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		Boolean isDistinct = filterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

}
