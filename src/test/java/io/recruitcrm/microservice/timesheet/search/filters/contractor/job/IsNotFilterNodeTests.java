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

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractorJob IsNotFilterNode Tests")
class IsNotFilterNodeTests {

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
	@ValueSource(strings = { "1,2,3", "[1,2,3]", "[\"1\", \" 2 \", \"abc\", \"3\"]", "[true, 1]", "1,,2" })
	@DisplayName("Get filter conditions should return negated condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnNegatedCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest
	@MethodSource("invalidOrEmptyFilterValues")
	@DisplayName("Get filter conditions should return empty list for invalid or empty filter values")
	void testGetFilterConditionsInvalidOrEmptyFilterValuesReturnEmptyList(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		IsNotFilterNode isNotFilterNode = this.createNode("1,2,3");
		List<TableJoinSpecification> joinTables = isNotFilterNode.getJoinTables();
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		IsNotFilterNode isNotFilterNode = this.createNode("1,2,3");
		List<Condition> commonConditions = isNotFilterNode.getCommonFilterCondition();
		assertThat(commonConditions).isEmpty();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		IsNotFilterNode isNotFilterNode = this.createNode("1,2,3");
		assertThat(isNotFilterNode.isSelectDistinct()).isTrue();
	}

	private List<Condition> getFilterConditionsForValue(String filterValue) {
		IsNotFilterNode isNotFilterNode = this.createNode(filterValue);
		return isNotFilterNode.getFilterConditions();
	}

	private IsNotFilterNode createNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(DB_FIELD);
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType(GROUP_TYPE);
		this.filterNodeContext.setFilterDto(filterDto);
		return new IsNotFilterNode(this.filterNodeContext);
	}

	private static Stream<Arguments> invalidOrEmptyFilterValues() {
		return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "), Arguments.of("abc,xyz"),
				Arguments.of("{\"id\":1}"), Arguments.of("[]"), Arguments.of("[true]"));
	}

}
