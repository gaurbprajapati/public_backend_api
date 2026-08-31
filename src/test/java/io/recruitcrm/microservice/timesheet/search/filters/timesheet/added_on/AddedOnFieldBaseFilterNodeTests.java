package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("AddedOnFieldBaseFilterNode Tests")
class AddedOnFieldBaseFilterNodeTests {

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
	@DisplayName("Get search field should return ADDED_ON field")
	void testGetSearchFieldReturnsAddedOnField() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		Field<?> searchField = isFilterNode.getSearchField();

		// Then
		assertThat(searchField).isNotNull().isEqualTo(CstTimesheetT.CST_TIMESHEET_T.ADDED_ON);
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> joinTables = isFilterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull().hasSize(1);
		assertThat(joinTables.get(0).getJoinType()).isEqualTo(TableJoinType.LEFT);
	}

}
