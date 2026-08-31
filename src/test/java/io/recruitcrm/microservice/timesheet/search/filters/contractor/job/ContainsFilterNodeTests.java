package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("ContractorJob ContainsFilterNode Tests")
class ContainsFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private static final int EXPECTED_JOIN_COUNT = 4;

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
	@MethodSource("validFilterValues")
	@DisplayName("Get filter conditions should return job condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnJobCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull()
			.isNotEmpty()
			.hasSize(SINGLE_CONDITION_COUNT)
			.first()
			.isNotEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest
	@MethodSource("invalidOrEmptyFilterValues")
	@DisplayName("Get filter conditions should return false condition for invalid or empty filter values")
	void testGetFilterConditionsInvalidOrEmptyFilterValuesReturnFalseCondition(String filterValue) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotNull()
			.isNotEmpty()
			.hasSize(SINGLE_CONDITION_COUNT)
			.first()
			.isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get group by fields should return candidate ID field")
	void testGetGroupByFieldsReturnsCandidateIdField() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2,3");
		List<Field<?>> groupByFields = containsFilterNode.getGroupByFields();
		assertThat(groupByFields).isNotNull()
			.isNotEmpty()
			.hasSize(SINGLE_CONDITION_COUNT)
			.first()
			.isEqualTo(Tblcandidate.TBLCANDIDATE.ID);
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
	@DisplayName("Get join tables should return four inner joins")
	void testGetJoinTablesReturnsFourInnerJoins() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2,3");
		List<TableJoinSpecification> joinTables = containsFilterNode.getJoinTables();
		assertThat(joinTables).isNotNull()
			.isNotEmpty()
			.hasSize(EXPECTED_JOIN_COUNT)
			.allMatch((join) -> join.getJoinType().equals(TableJoinType.INNER))
			.extracting((join) -> join.getJoinTable().getName())
			.containsExactlyInAnyOrder("tblassignjobcandidate", "tbljob", "cst_timesheet_setting_association_t",
					"cst_timesheet_setting_t");
	}

	@Test
	@DisplayName("Is select distinct should return false")
	void testIsSelectDistinctReturnsFalse() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2,3");
		assertThat(containsFilterNode.isSelectDistinct()).isFalse();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		ContainsFilterNode containsFilterNode = this.createNode("1,2,3");
		List<Condition> commonConditions = containsFilterNode.getCommonFilterCondition();
		assertThat(commonConditions).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("edgeCaseFilterValuesWithExpectedJobIds")
	@DisplayName("Get filter conditions should handle non-textual JSON nodes and empty comma segments")
	void testGetFilterConditionsHandlesEdgeCaseFilterValues(String filterValue, List<Integer> expectedJobIds) {
		List<Condition> conditions = this.getFilterConditionsForValue(filterValue);
		assertThat(conditions).isNotEmpty().hasSize(SINGLE_CONDITION_COUNT).first().satisfies((condition) -> {
			String conditionSql = condition.toString();
			assertThat(conditionSql).isNotEqualTo(DSL.falseCondition().toString());
			assertThat(expectedJobIds).isNotEmpty().allMatch((jobId) -> conditionSql.contains(String.valueOf(jobId)));
		});
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
		return Stream.of(Arguments.of("1,2,3"), Arguments.of("[1,2,3]"),
				Arguments.of("[\"1\", \" 2 \", \"x\", \"3\"]"));
	}

	private static Stream<Arguments> invalidOrEmptyFilterValues() {
		return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "), Arguments.of("abc,xyz"),
				Arguments.of("{\"id\":1}"), Arguments.of("[]"), Arguments.of("[true]"));
	}

	private static Stream<Arguments> edgeCaseFilterValuesWithExpectedJobIds() {
		return Stream.of(Arguments.of("[true, 1]", List.of(Integer.valueOf(1))),
				Arguments.of("1,,2", List.of(Integer.valueOf(1), Integer.valueOf(2))));
	}

}
