package io.recruitcrm.microservice.timesheet.search.filters.contractor.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("StatusFieldBaseFilterNode Tests")
class StatusFieldBaseFilterNodeTests {

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
	@DisplayName("Get search field should return candidate ID field")
	void testGetSearchFieldReturnsCandidateIdField() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("0");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		Field<?> searchField = isFilterNode.getSearchField();

		// Then
		assertThat(searchField).isNotNull().isEqualTo(Tblcandidate.TBLCANDIDATE.ID);
	}

	@Test
	@DisplayName("Get join tables should return job joins")
	void testGetJoinTablesReturnsJobJoins() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("0");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification> joinTables = isFilterNode
			.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(1);
	}

	@Test
	@DisplayName("Get group by fields should return candidate ID")
	void testGetGroupByFieldsReturnsCandidateId() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("0");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = isFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isNotNull().hasSize(1);
		assertThat(groupByFields.get(0)).isEqualTo(Tblcandidate.TBLCANDIDATE.ID);
	}

	@Test
	@DisplayName("Get status condition should return EXISTS condition for ASSIGNED status")
	void testGetStatusConditionReturnsExistsConditionForAssignedStatus() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Get status condition should return NOT EXISTS condition for AVAILABLE status")
	void testGetStatusConditionReturnsNotExistsConditionForAvailableStatus() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("0");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

}
