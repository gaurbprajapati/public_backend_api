package io.recruitcrm.microservice.timesheet.search.filters.timesheet.company_name;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.jooq.Condition;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.IsFilterNode;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("CompanyName IsFilterNode Tests")
class CompanyNameIsFilterNodeTests {

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

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(strings = { "1,2,3", "[1,2,3]", "[\"1\", \"2\", \"3\"]" })
	@DisplayName("getFilterConditions should return IN condition for valid array filter value")
	void testGetFilterConditionsForValidArrayFilterValue(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyname");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "" })
	@DisplayName("getFilterConditions should return condition with empty/null filter value")
	void testGetFilterConditionsForEmptyOrNullFilterValue(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyname");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

	@ParameterizedTest(name = "filterValue={0} / dbField={1}")
	@ValueSource(strings = { "1, 2, 3", "1,invalid,3" })
	@DisplayName("getFilterConditions should handle various comma-separated formats")
	void testGetFilterConditionsForCommaSeparatedVariants(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("companyname");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).doesNotContainNull();
	}

	@Test
	@DisplayName("getFilterConditions should work with company_name dbField")
	void testGetFilterConditionsForCompanyNameWithUnderscore() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("company_name");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT);
	}

}
