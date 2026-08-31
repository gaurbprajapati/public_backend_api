package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("AssociatedDeal ContainsFilterNode Tests")
class ContainsFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private static final String DB_FIELD = "associatedDeal";

	private static final String GROUP_TYPE = "AND";

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = Integer.valueOf(1);

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
	}

	@ParameterizedTest
	@MethodSource("validFilterValues")
	@DisplayName("Get filter conditions should return in condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnInCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest
	@MethodSource("invalidOrEmptyFilterValues")
	@DisplayName("Get filter conditions should return false condition for invalid or empty filter values")
	void testGetFilterConditionsInvalidOrEmptyFilterValuesReturnFalseCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
		assertThat(conditions.getFirst().toString()).contains("and false");
	}

	@Test
	@DisplayName("Get group by fields should return timesheet ID field")
	void testGetGroupByFieldsReturnsTimesheetIdField() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2,3");
		List<Field<?>> groupByFields = containsFilterNode.getGroupByFields();
		assertThat(groupByFields).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get group by having condition should return false condition for empty filter value")
	void testGetGroupByHavingConditionWithEmptyFilterValueReturnsFalseCondition() {
		ContainsFilterNode containsFilterNode = this.createNode("");
		Condition havingCondition = containsFilterNode.getGroupByHavingCondition();
		assertThat(havingCondition).isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get group by having condition should return count distinct condition for valid filter value")
	void testGetGroupByHavingConditionWithValidFilterValueReturnsCountDistinctCondition() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2");
		Condition havingCondition = containsFilterNode.getGroupByHavingCondition();
		assertThat(havingCondition).isNotNull().isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Is select distinct should return false")
	void testIsSelectDistinctReturnsFalse() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2,3");
		assertThat(containsFilterNode.isSelectDistinct()).isFalse();
	}

	private List<Condition> getFilterConditionsForValue(String filterValue) {
		ContainsFilterNode containsFilterNode = this.createNode(filterValue);
		return containsFilterNode.getFilterConditions();
	}

	private ContainsFilterNode createNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(DB_FIELD);
		filterDto.setFilterType(FilterTypes.CONTAINS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType(GROUP_TYPE);
		this.filterNodeContext.setFilterDto(filterDto);
		return new ContainsFilterNode(this.filterNodeContext);
	}

	private static Stream<Arguments> validFilterValues() {
		return Stream.of(Arguments.of("1,2,3"), Arguments.of("[1,2,3]"), Arguments.of("[\"1\", \" 2 \", \"x\", \"3\"]"),
				Arguments.of("[true, 1]"));
	}

	private static Stream<Arguments> invalidOrEmptyFilterValues() {
		return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "), Arguments.of("abc,xyz"),
				Arguments.of("{\"id\":1}"));
	}

}
