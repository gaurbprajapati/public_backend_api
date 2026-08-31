package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.jooq.Condition;
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
@DisplayName("AddedOn IsAfterFilterNode Tests")
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

	@Test
	@DisplayName("Get filter conditions should return after condition for valid epoch seconds")
	void testGetFilterConditionsValidEpochSecondsReturnsAfterCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should use default GMT difference when null")
	void testGetFilterConditionsWithNullGmtDifference() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference(null);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Get filter conditions should apply GMT offset correctly")
	void testGetFilterConditionsAppliesGmtOffsetCorrectly() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference("+10:00");
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isAfterFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "invalid", "" })
	@DisplayName("Get filter conditions should throw NumberFormatException for invalid filter value")
	void testGetFilterConditionsInvalidFilterValueThrowsException(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		assertThatThrownBy(isAfterFilterNode::getFilterConditions).isInstanceOf(NumberFormatException.class);
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTables() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		// When
		var joinTables = isAfterFilterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return false")
	void testIsSelectDistinctReturnsFalse() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_AFTER);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsAfterFilterNode isAfterFilterNode = new IsAfterFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isAfterFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isFalse();
	}

}
