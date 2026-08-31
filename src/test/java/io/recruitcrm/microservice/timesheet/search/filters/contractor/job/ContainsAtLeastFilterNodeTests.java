package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("ContractorJob ContainsAtLeastFilterNode Tests")
class ContainsAtLeastFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private static final String DB_FIELD = "jobName";

	private static final String GROUP_TYPE = "contractor";

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
	@ValueSource(strings = { "1,2,3", "[1,2,3]", "[1]", "[\"1\", \" 2 \", \"abc\", \"3\"]", "[1,\"invalid\",3]",
			"1,invalid,3", "[1,2" })
	@DisplayName("Get filter conditions should return EXISTS condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnExistsCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest
	@MethodSource("invalidOrEmptyFilterValues")
	@DisplayName("Get filter conditions should return false condition for invalid or empty filter values")
	void testGetFilterConditionsInvalidOrEmptyFilterValuesReturnFalseCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		Condition havingCondition = containsAtLeastFilterNode.getGroupByHavingCondition();
		assertThat(havingCondition).isNotNull().isEqualTo(DSL.noCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		Boolean isDistinct = containsAtLeastFilterNode.isSelectDistinct();
		assertThat(isDistinct).isTrue();
	}

	@Test
	@DisplayName("Get join tables should return empty list")
	void testGetJoinTablesReturnsEmptyList() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		List<io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification> joinTables = containsAtLeastFilterNode
			.getJoinTables();
		assertThat(joinTables).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		List<Condition> commonConditions = containsAtLeastFilterNode.getCommonFilterCondition();
		assertThat(commonConditions).isEmpty();
	}

	private List<Condition> getFilterConditionsForValue(String filterValue) {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode(filterValue);
		return containsAtLeastFilterNode.getFilterConditions();
	}

	private ContainsAtLeastFilterNode createNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(DB_FIELD);
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType(GROUP_TYPE);
		this.filterNodeContext.setFilterDto(filterDto);
		return new ContainsAtLeastFilterNode(this.filterNodeContext);
	}

	@ParameterizedTest
	@ValueSource(strings = { "[true, 1]", "1,,2" })
	@DisplayName("Get filter conditions should handle non-textual JSON nodes and empty comma segments")
	void testGetFilterConditionsHandlesEdgeCaseFilterValues(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	private static Stream<Arguments> invalidOrEmptyFilterValues() {
		return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "), Arguments.of("abc,xyz"),
				Arguments.of("{\"id\":1}"), Arguments.of("[]"), Arguments.of("[true]"));
	}

}
