package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company;

import static org.assertj.core.api.Assertions.assertThat;

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
@DisplayName("Company DoesNotContainFilterNode Tests")
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

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "1,2,3", "[1,2,3]", "1,invalid,3", "[\"1\",\"2\"]", "[\"1\",\"abc\"]", "[true, 1]" })
	@DisplayName("Get filter conditions should return NOT IN condition for valid filter value")
	void testGetFilterConditionsValidFilterValueReturnsNotInCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		DoesNotContainFilterNode doesNotContainFilterNode = new DoesNotContainFilterNode(this.filterNodeContext);

		List<Condition> conditions = doesNotContainFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "abc,def" })
	@DisplayName("Get filter conditions should return empty list for blank or null filter value")
	void testGetFilterConditionsBlankOrNullFilterValueReturnsEmptyList(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		DoesNotContainFilterNode doesNotContainFilterNode = new DoesNotContainFilterNode(this.filterNodeContext);

		List<Condition> conditions = doesNotContainFilterNode.getFilterConditions();

		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyName");
		filterDto.setFilterType(FilterTypes.DOES_NOT_CONTAIN);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		DoesNotContainFilterNode doesNotContainFilterNode = new DoesNotContainFilterNode(this.filterNodeContext);

		Boolean isDistinct = doesNotContainFilterNode.isSelectDistinct();

		assertThat(isDistinct).isTrue();
	}

}
