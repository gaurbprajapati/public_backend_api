package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
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
@DisplayName("Company HasAnyValueFilterNode Tests")
class HasAnyValueFilterNodeTests {

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
	@DisplayName("Get filter conditions should return IS NOT NULL condition")
	void testGetFilterConditionsReturnsIsNotNullCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.HAS_ANY_VALUE);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		HasAnyValueFilterNode hasAnyValueFilterNode = new HasAnyValueFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = hasAnyValueFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.HAS_ANY_VALUE);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		HasAnyValueFilterNode hasAnyValueFilterNode = new HasAnyValueFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = hasAnyValueFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
