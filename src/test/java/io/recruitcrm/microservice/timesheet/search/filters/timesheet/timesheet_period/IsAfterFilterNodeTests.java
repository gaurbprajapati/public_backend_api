package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("TimesheetPeriod IsAfterFilterNode Tests")
class IsAfterFilterNodeTests {

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
	@MethodSource("provideValidFilterValues")
	@DisplayName("Get filter conditions should return condition for valid filter value")
	void testGetFilterConditionsValidFilterValueReturnsCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	static Stream<String> provideValidFilterValues() {
		// epoch int (JSON number), JSON textual, and plus-prefixed (invalid JSON, parsed
		// as long string) - all within int range so the downstream toIntExact succeeds
		return Stream.of("1764527400", "\"1764527400\"", "+1764527400");
	}

	@Test
	@DisplayName("Parse filter value should return long for JSON number beyond integer range")
	void testParseFilterValueJsonLongBeyondIntegerRangeReturnsLong() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("9999999999");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		Method parseFilterValue = IsAfterFilterNode.class.getDeclaredMethod("parseFilterValue");
		parseFilterValue.setAccessible(true);
		Object result = parseFilterValue.invoke(isAfterFilterNode);

		assertThat(result).isEqualTo(Long.valueOf(9999999999L));
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "invalid", "true", "[1,2]" })
	@DisplayName("Get filter conditions should return false condition for invalid filter value")
	void testGetFilterConditionsInvalidFilterValueReturnsFalseCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get filter conditions should handle GMT difference")
	void testGetFilterConditionsHandlesGmtDifference() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1764527400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference("+10:00");
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should use default GMT difference when null")
	void testGetFilterConditionsUsesDefaultGmtDifferenceWhenNull() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1764527400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference(null);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1764527400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		Boolean isDistinct = isAfterFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

}
