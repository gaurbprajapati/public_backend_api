package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("TimesheetPeriod IsEmptyFilterNode Tests")
class IsEmptyFilterNodeTests {

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
	@DisplayName("Get filter conditions should return false condition")
	void testGetFilterConditionsReturnsFalseCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isEmptyFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isEmptyFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
