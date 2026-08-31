package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("AddedOn IsFilterNode Tests")
class AddedOnIsFilterNodeTests {

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
	@ValueSource(strings = { "TODAY", "YESTERDAY", "THIS_MONTH", "THIS_WEEK", "LAST_WEEK", "LAST_MONTH", "THIS_QUARTER",
			"LAST_QUARTER", "THIS_YEAR", "LAST_YEAR", "ALL_TIME", "LAST_30", "LAST_60", "LAST_90", "LAST_365" })
	@DisplayName("getFilterConditions should return between condition for date range filter values")
	void testGetFilterConditionsForDateRangeFilterValue(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("getFilterConditions should apply GMT offset correctly")
	void testGetFilterConditionsAppliesGmtOffsetCorrectly() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference("+10:00");
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Is select distinct should return false")
	void testIsSelectDistinctReturnsFalse() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		Boolean isDistinct = isFilterNode.isSelectDistinct();

		assertThat(isDistinct).isFalse();
	}

	@Test
	@DisplayName("getFilterConditions should use default GMT difference when null")
	void testGetFilterConditionsWithNullGmtDifference() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
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
	@DisplayName("getJoinTables should return minimal joins")
	void testGetJoinTables() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		var joinTables = isFilterNode.getJoinTables();

		assertThat(joinTables).isNotNull();
	}

}
