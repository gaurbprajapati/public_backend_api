package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.stream.Stream;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("Job ContainsAtLeastFilterNode Tests")
class ContainsAtLeastFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private static final String DB_FIELD = "jobName";

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
	@ValueSource(strings = { "1,2,3", "[1,2,3]", "[\"1\", \" 2 \", \"abc\", \"3\"]", "7,8", "[true, 1]" })
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
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		Boolean isDistinct = containsAtLeastFilterNode.isSelectDistinct();
		assertThat(isDistinct).isTrue();
	}

	@Test
	@DisplayName("Get join tables should return expected minimal left joins")
	void testGetJoinTablesReturnsExpectedMinimalLeftJoins() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		List<TableJoinSpecification> joinTables = containsAtLeastFilterNode.getJoinTables();
		assertThat(joinTables).isNotNull()
			.hasSize(3)
			.allSatisfy((join) -> assertThat(join.getJoinType()).isEqualTo(TableJoinType.LEFT))
			.extracting((join) -> join.getJoinTable().getName())
			.containsExactlyInAnyOrder("cst_timesheet_setting_t", "cst_timesheet_setting_association_t", "tbljob");
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createNode("1,2,3");
		List<Condition> commonConditions = containsAtLeastFilterNode.getCommonFilterCondition();
		assertThat(commonConditions).isEmpty();
	}

	@Test
	@DisplayName("Get filter conditions should return condition for JSON array with textual elements")
	void testGetFilterConditionsJsonArrayTextualElementsReturnsCondition() {
		List<Condition> conditions = this.getFilterConditionsForValue("[\"1\", \"2\", \"3\"]");
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get filter conditions should return condition for comma-separated string")
	void testGetFilterConditionsCommaSeparatedStringReturnsCondition() {
		List<Condition> conditions = this.getFilterConditionsForValue("1, 2, 3");
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
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

	private static Stream<Arguments> invalidOrEmptyFilterValues() {
		return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "), Arguments.of("abc,xyz"),
				Arguments.of("{\"id\":1}"));
	}

}
