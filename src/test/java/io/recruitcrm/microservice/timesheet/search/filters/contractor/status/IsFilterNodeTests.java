package io.recruitcrm.microservice.timesheet.search.filters.contractor.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("ContractorStatus IsFilterNode Tests")
class IsFilterNodeTests {

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
	@ValueSource(strings = { "0", "1", "  0  ", "  1  " })
	@DisplayName("Get filter conditions should return condition for valid status values")
	void testGetFilterConditionsValidStatusValueReturnsCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@ParameterizedTest(name = "filterValue={0}")
	@NullSource
	@ValueSource(strings = { "", "invalid", "2", "-1" })
	@DisplayName("Get filter conditions should return empty list for invalid filter values")
	void testGetFilterConditionsInvalidFilterValueReturnsEmptyList(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		List<Condition> conditions = isFilterNode.getFilterConditions();

		assertThat(conditions).isEmpty();
	}

}
