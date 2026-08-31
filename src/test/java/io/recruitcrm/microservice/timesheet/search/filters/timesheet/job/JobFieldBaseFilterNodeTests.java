package io.recruitcrm.microservice.timesheet.search.filters.timesheet.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("JobFieldBaseFilterNode Tests")
class JobFieldBaseFilterNodeTests {

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
	@DisplayName("Get search field should return job ID field")
	void testGetSearchFieldReturnsJobIdField() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("123");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		Field<?> searchField = isFilterNode.getSearchField();

		// Then
		assertThat(searchField).isNotNull()
			.isEqualTo(CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T.JOB_ID);
	}

	@Test
	@DisplayName("Get join tables should return job joins")
	void testGetJoinTablesReturnsJobJoins() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("123");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification> joinTables = isFilterNode
			.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(1);
	}

}
