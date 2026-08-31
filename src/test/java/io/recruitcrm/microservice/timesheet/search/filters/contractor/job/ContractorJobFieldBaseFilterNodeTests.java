package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;

import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("ContractorJobFieldBaseFilterNode Tests")
class ContractorJobFieldBaseFilterNodeTests {

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = Integer.valueOf(1);

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
		this.filterNodeContext.setFilterDto(this.createFilterDto("1"));
	}

	@Test
	@DisplayName("Get search field should return job ID field")
	void testGetSearchFieldReturnsJobIdField() {
		TestableContractorJobFieldBaseFilterNode filterNode = new TestableContractorJobFieldBaseFilterNode(
				this.filterNodeContext);

		Field<?> searchField = filterNode.getSearchField();

		assertThat(searchField).isNotNull().isEqualTo(Tbljob.TBLJOB.ID);
	}

	@Test
	@DisplayName("Get join tables should return job and timesheet setting joins")
	void testGetJoinTablesReturnsJobAndTimesheetSettingJoins() {
		TestableContractorJobFieldBaseFilterNode filterNode = new TestableContractorJobFieldBaseFilterNode(
				this.filterNodeContext);

		List<TableJoinSpecification> joinTables = filterNode.getJoinTables();

		assertThat(joinTables).isNotNull().hasSize(4);
	}

	@Test
	@DisplayName("Get common filter condition should include latest timesheet setting condition")
	void testGetCommonFilterConditionReturnsLatestTimesheetSettingCondition() {
		TestableContractorJobFieldBaseFilterNode filterNode = new TestableContractorJobFieldBaseFilterNode(
				this.filterNodeContext);

		List<Condition> commonConditions = filterNode.getCommonFilterCondition();

		assertThat(commonConditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Get active date range condition should return non-null condition")
	void testGetActiveDateRangeConditionReturnsCondition() {
		TestableContractorJobFieldBaseFilterNode filterNode = new TestableContractorJobFieldBaseFilterNode(
				this.filterNodeContext);

		Condition dateRangeCondition = filterNode.getActiveDateRangeCondition();

		assertThat(dateRangeCondition).isNotNull();
	}

	@Test
	@DisplayName("Is select distinct should return true from base class")
	void testIsSelectDistinctReturnsTrueFromBaseClass() {
		ContractorJobFieldBaseFilterNode baseFilterNode = this.createBaseFilterNodeTestDouble();
		Boolean isDistinct = baseFilterNode.isSelectDistinct();
		assertThat(isDistinct).isTrue();
	}

	@Test
	@DisplayName("Get active date range condition should return date range condition")
	void testGetActiveDateRangeConditionReturnsDateRangeCondition() throws Exception {
		ContainsAtLeastFilterNode containsAtLeastFilterNode = this.createContainsAtLeastNode("1,2,3");
		Method method = ContractorJobFieldBaseFilterNode.class.getDeclaredMethod("getActiveDateRangeCondition");
		method.setAccessible(true);
		Condition dateRangeCondition = (Condition) method.invoke(containsAtLeastFilterNode);
		assertThat(dateRangeCondition).isNotNull();
		assertThat(dateRangeCondition.toString()).isNotBlank();
	}

	private FilterDto createFilterDto(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");
		return filterDto;
	}

	private ContainsAtLeastFilterNode createContainsAtLeastNode(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("contractor");
		this.filterNodeContext.setFilterDto(filterDto);
		return new ContainsAtLeastFilterNode(this.filterNodeContext);
	}

	private ContractorJobFieldBaseFilterNode createBaseFilterNodeTestDouble() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("jobName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1");
		filterDto.setGroupType("contractor");
		this.filterNodeContext.setFilterDto(filterDto);
		return new TestableContractorJobFieldBaseFilterNode(this.filterNodeContext);
	}

	private static final class TestableContractorJobFieldBaseFilterNode extends ContractorJobFieldBaseFilterNode {

		private TestableContractorJobFieldBaseFilterNode(FilterNodeContext filterNodeContext) {
			super(filterNodeContext);
		}

		@Override
		public List<Condition> getFilterConditions() {
			return List.of();
		}

	}

}
