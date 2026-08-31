package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("TimesheetPeriod IsFilterNode Tests")
class TimesheetPeriodIsFilterNodeTests {

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
	@ValueSource(strings = { "TODAY", "THIS_MONTH", "YESTERDAY", "THIS_WEEK", "LAST_WEEK", "LAST_MONTH", "THIS_QUARTER",
			"LAST_QUARTER", "THIS_YEAR", "LAST_YEAR", "ALL_TIME", "LAST_30", "LAST_60", "LAST_90", "LAST_365",
			"today" })
	@DisplayName("getFilterConditions should return period overlap condition for valid filter value")
	void testGetFilterConditionsForValidFilterValue(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheet_period");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "   " })
	@DisplayName("getFilterConditions should return false condition for blank or null filter value")
	void testGetFilterConditionsBlankOrNullFilterValueReturnsFalseCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheet_period");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull()
			.hasSize(SINGLE_CONDITION_COUNT)
			.first()
			.isEqualTo(org.jooq.impl.DSL.falseCondition());
	}

	@Test
	@DisplayName("getFilterConditions should use default GMT difference when null")
	void testGetFilterConditionsWithNullGmtDifference() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheet_period");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference(null);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("isSelectDistinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheet_period");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		Boolean isDistinct = isFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

	@Test
	@DisplayName("Constructor should throw IllegalArgumentException for invalid filter value")
	void testConstructorThrowsIllegalArgumentExceptionForInvalidFilterValue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheet_period");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("INVALID_VALUE");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);

		assertThatThrownBy(() -> new IsFilterNode(this.filterNodeContext)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Invalid value type: INVALID_VALUE");
	}

}
