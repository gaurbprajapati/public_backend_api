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
@DisplayName("TimesheetPeriod IsNotBetweenFilterNode Tests")
class IsNotBetweenFilterNodeTests {

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
	@DisplayName("Get filter conditions should return not overlap condition for valid JSON with start and end")
	void testGetFilterConditionsValidJsonWithStartAndEndReturnsNotOverlapCondition() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_NOT_BETWEEN);
		filterDto.setFilterValue("{\"start\": 1764527400, \"end\": 1765996199}");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotBetweenFilterNode isNotBetweenFilterNode = new IsNotBetweenFilterNode(this.filterNodeContext);

		List<Condition> conditions = isNotBetweenFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "invalid json" })
	@DisplayName("Get filter conditions should return false condition for invalid filter value")
	void testGetFilterConditionsInvalidFilterValueReturnsFalseCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_NOT_BETWEEN);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotBetweenFilterNode isNotBetweenFilterNode = new IsNotBetweenFilterNode(this.filterNodeContext);

		List<Condition> conditions = isNotBetweenFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get filter conditions should handle GMT difference")
	void testGetFilterConditionsHandlesGmtDifference() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_NOT_BETWEEN);
		filterDto.setFilterValue("{\"start\": 1764527400, \"end\": 1765996199}");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference("+10:00");
		IsNotBetweenFilterNode isNotBetweenFilterNode = new IsNotBetweenFilterNode(this.filterNodeContext);

		List<Condition> conditions = isNotBetweenFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_NOT_BETWEEN);
		filterDto.setFilterValue("{\"start\": 1764527400, \"end\": 1765996199}");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotBetweenFilterNode isNotBetweenFilterNode = new IsNotBetweenFilterNode(this.filterNodeContext);

		Boolean isDistinct = isNotBetweenFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

}
