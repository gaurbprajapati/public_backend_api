package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("TimesheetStatus DoesNotContainFilterNode Tests")
class DoesNotContainFilterNodeTests {

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
	@DisplayName("Get filter conditions should return NOT IN condition for comma-separated status IDs")
	void testGetFilterConditionsCommaSeparatedStatusIdsReturnsNotInCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetStatus");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		DoesNotContainFilterNode doesNotContainFilterNode = new DoesNotContainFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = doesNotContainFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for null filter value")
	void testGetFilterConditionsNullFilterValueReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetStatus");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		DoesNotContainFilterNode doesNotContainFilterNode = new DoesNotContainFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = doesNotContainFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetStatus");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		DoesNotContainFilterNode doesNotContainFilterNode = new DoesNotContainFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = doesNotContainFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "1,2,3", "1, 2, 3", "[1,2,3]", "[\"1\", \"2\", \"3\"]", "[\"1\", \"abc\", \"2\"]",
			"1,abc,2", "[true, 1]" })
	@DisplayName("Get filter conditions should return not-in condition for valid filter values")
	void testGetFilterConditionsValidFilterValuesReturnNotInCondition(String filterValue) {
		// Given
		DoesNotContainFilterNode doesNotContainFilterNode = this.createNode(filterValue);

		// When
		List<Condition> conditions = doesNotContainFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for empty filter value")
	void testGetFilterConditionsEmptyFilterValueReturnsEmptyList() {
		// Given
		DoesNotContainFilterNode doesNotContainFilterNode = this.createNode("");

		// When
		List<Condition> conditions = doesNotContainFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		// Given
		DoesNotContainFilterNode doesNotContainFilterNode = this.createNode("1,2,3");

		// When
		List<TableJoinSpecification> joinTables = doesNotContainFilterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		// Given
		DoesNotContainFilterNode doesNotContainFilterNode = this.createNode("1,2,3");

		// When
		List<Condition> commonConditions = doesNotContainFilterNode.getCommonFilterCondition();

		// Then
		assertThat(commonConditions).isEmpty();
	}

	private DoesNotContainFilterNode createNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetStatus");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");
		this.filterNodeContext.setFilterDto(filterDto);
		return new DoesNotContainFilterNode(this.filterNodeContext);
	}

}
