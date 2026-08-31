package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("TimesheetStatusFieldBaseFilterNode Tests")
class TimesheetStatusFieldBaseFilterNodeTests {

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
	@DisplayName("Get search field should return status ID field")
	void testGetSearchFieldReturnsStatusIdField() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetStatus");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		Field<?> searchField = isFilterNode.getSearchField();

		// Then
		assertThat(searchField).isNotNull()
			.isEqualTo(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.TIMESHEET_APPROVAL_STATUS_TYPE_ID);
	}

	@Test
	@DisplayName("Get join tables should return joins with timesheet approval table")
	void testGetJoinTablesReturnsJoinsWithTimesheetApprovalTable() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetStatus");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> joinTables = isFilterNode.getJoinTables();

		// Then
		// Verify LEFT join exists for timesheet approval
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(2);
		assertThat(joinTables.stream().anyMatch((join) -> join.getJoinType() == TableJoinType.LEFT)).isTrue();
	}

}
