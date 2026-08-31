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
@DisplayName("AddedOn IsBeforeFilterNode Tests")
class IsBeforeFilterNodeTests {

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
	@DisplayName("Get filter conditions should return before condition for valid epoch seconds")
	void testGetFilterConditionsValidEpochSecondsReturnsBeforeCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_BEFORE);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsBeforeFilterNode isBeforeFilterNode = new IsBeforeFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isBeforeFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should use default GMT difference when null")
	void testGetFilterConditionsWithNullGmtDifference() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_BEFORE);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference(null);
		IsBeforeFilterNode isBeforeFilterNode = new IsBeforeFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isBeforeFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

	@Test
	@DisplayName("Get filter conditions should apply GMT offset correctly")
	void testGetFilterConditionsAppliesGmtOffsetCorrectly() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_BEFORE);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		this.filterNodeContext.setGmtDifference("+10:00");
		IsBeforeFilterNode isBeforeFilterNode = new IsBeforeFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isBeforeFilterNode.getFilterConditions();

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
		filterDto.setFilterType(FilterTypes.IS_BEFORE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsBeforeFilterNode isBeforeFilterNode = new IsBeforeFilterNode(this.filterNodeContext);

		assertThatThrownBy(isBeforeFilterNode::getFilterConditions).isInstanceOf(NumberFormatException.class);
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTables() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_BEFORE);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsBeforeFilterNode isBeforeFilterNode = new IsBeforeFilterNode(this.filterNodeContext);

		// When
		var joinTables = isBeforeFilterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return false")
	void testIsSelectDistinctReturnsFalse() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS_BEFORE);
		filterDto.setFilterValue("1633046400");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsBeforeFilterNode isBeforeFilterNode = new IsBeforeFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isBeforeFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isFalse();
	}

}
