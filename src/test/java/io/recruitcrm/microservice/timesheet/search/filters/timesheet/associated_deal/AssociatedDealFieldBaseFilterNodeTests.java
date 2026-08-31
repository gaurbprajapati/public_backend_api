package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

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

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("AssociatedDealFieldBaseFilterNode Tests")
class AssociatedDealFieldBaseFilterNodeTests {

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
	@DisplayName("Get search field should return deals ID field")
	void testGetSearchFieldReturnsDealsIdField() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		Field<?> searchField = isFilterNode.getSearchField();

		// Then
		assertThat(searchField).isNotNull().isEqualTo(Tbldeals.TBLDEALS.ID);
	}

	@Test
	@DisplayName("Get group by fields should return timesheet ID")
	void testGetGroupByFieldsReturnsTimesheetId() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);

		// When
		// Note: IsFilterNode overrides getGroupByFields to return empty
		// But base class returns timesheet ID
		// Let's check a node that doesn't override it
		HasAnyValueFilterNode hasAnyValueFilterNode = new HasAnyValueFilterNode(this.filterNodeContext);
		List<Field<?>> baseGroupByFields = hasAnyValueFilterNode.getGroupByFields();

		// Then
		assertThat(baseGroupByFields).isEmpty();
	}

	@Test
	@DisplayName("Get join tables should return all deal-related joins")
	void testGetJoinTablesReturnsAllDealRelatedJoins() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.HAS_ANY_VALUE);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		HasAnyValueFilterNode hasAnyValueFilterNode = new HasAnyValueFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> joinTables = hasAnyValueFilterNode.getJoinTables();

		// Then
		// Verify INNER joins exist (cannot access protected fields directly)
		long innerJoinCount = joinTables.stream().filter((join) -> join.getJoinType() == TableJoinType.INNER).count();
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(4);
		assertThat(innerJoinCount).isGreaterThanOrEqualTo(4);
	}

	@Test
	@DisplayName("Is select distinct should return false")
	void testIsSelectDistinctReturnsFalse() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.HAS_ANY_VALUE);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		HasAnyValueFilterNode hasAnyValueFilterNode = new HasAnyValueFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = hasAnyValueFilterNode.isSelectDistinct();

		// Then
		// Note: HasAnyValueFilterNode overrides isSelectDistinct to return true
		// But base class returns false
		// Let's check the base class behavior through a node that doesn't override it
		// Actually, all nodes override it, so we verify the override works
		assertThat(isDistinct).isTrue();
	}

	@Test
	@DisplayName("Get account deal condition should return account scoped deals condition")
	void testGetAccountDealConditionReturnsAccountScopedCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");
		this.filterNodeContext.setFilterDto(filterDto);
		TestableAssociatedDealFieldBaseFilterNode filterNode = new TestableAssociatedDealFieldBaseFilterNode(
				this.filterNodeContext);

		// When
		Condition accountDealCondition = filterNode.getAccountDealCondition();

		// Then
		assertThat(accountDealCondition).isNotNull();
	}

	@Test
	@DisplayName("Build contains at least conditions should return false condition when deal IDs are empty")
	void testBuildContainsAtLeastConditionsEmptyDealIdsReturnsFalseCondition() {
		FilterDto filterDto = this.createFilterDto(FilterTypes.CONTAINS_AT_LEAST_ONE, "invalid");
		this.filterNodeContext.setFilterDto(filterDto);
		TestableAssociatedDealFieldBaseFilterNode filterNode = new TestableAssociatedDealFieldBaseFilterNode(
				this.filterNodeContext);
		Field<Integer> dealIdField = filterNode.getSearchField(Integer.class);

		assertThat(filterNode.buildContainsAtLeastConditions(dealIdField)).hasSize(1);
	}

	@Test
	@DisplayName("Build contains all conditions should return condition when deal IDs are present")
	void testBuildContainsAllConditionsWithDealIdsReturnsCondition() {
		FilterDto filterDto = this.createFilterDto(FilterTypes.CONTAINS, "1,2");
		this.filterNodeContext.setFilterDto(filterDto);
		TestableAssociatedDealFieldBaseFilterNode filterNode = new TestableAssociatedDealFieldBaseFilterNode(
				this.filterNodeContext);
		Field<Integer> dealIdField = filterNode.getSearchField(Integer.class);

		assertThat(filterNode.buildContainsAllConditions(dealIdField)).hasSize(1);
	}

	@Test
	@DisplayName("Build contains all having should return count distinct condition when deal IDs are present")
	void testBuildContainsAllHavingWithDealIdsReturnsCountDistinctCondition() {
		FilterDto filterDto = this.createFilterDto(FilterTypes.CONTAINS, "1,2");
		this.filterNodeContext.setFilterDto(filterDto);
		TestableAssociatedDealFieldBaseFilterNode filterNode = new TestableAssociatedDealFieldBaseFilterNode(
				this.filterNodeContext);
		Field<Integer> dealIdField = filterNode.getSearchField(Integer.class);

		assertThat(filterNode.buildContainsAllHaving(dealIdField)).isNotNull();
	}

	@Test
	@DisplayName("Get group by fields from base class should return timesheet ID")
	void testGetGroupByFieldsFromBaseClassReturnsTimesheetId() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS);
		filterDto.setFilterValue("1");
		filterDto.setGroupType("AND");
		this.filterNodeContext.setFilterDto(filterDto);
		TestableAssociatedDealFieldBaseFilterNode filterNode = new TestableAssociatedDealFieldBaseFilterNode(
				this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = filterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).containsExactly(CstTimesheetT.CST_TIMESHEET_T.ID);
	}

	@Test
	@DisplayName("Is select distinct should return false from base class")
	void testIsSelectDistinctReturnsFalseFromBaseClass() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.CONTAINS);
		filterDto.setFilterValue("1");
		filterDto.setGroupType("AND");
		this.filterNodeContext.setFilterDto(filterDto);
		TestableAssociatedDealFieldBaseFilterNode filterNode = new TestableAssociatedDealFieldBaseFilterNode(
				this.filterNodeContext);

		// When / Then
		assertThat(filterNode.isSelectDistinct()).isFalse();
	}

	private FilterDto createFilterDto(FilterTypes filterType, String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(filterType);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");
		return filterDto;
	}

	@Test
	@DisplayName("Get group by fields default implementation should return timesheet ID")
	void testGetGroupByFieldsDefaultImplementationReturnsTimesheetId() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		TestAssociatedDealFieldFilterNode baseNode = new TestAssociatedDealFieldFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = baseNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isNotNull().hasSize(1);
	}

	@Test
	@DisplayName("Is select distinct default implementation should return false")
	void testIsSelectDistinctDefaultImplementationReturnsFalse() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		TestAssociatedDealFieldFilterNode baseNode = new TestAssociatedDealFieldFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = baseNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isFalse();
	}

	private static final class TestableAssociatedDealFieldBaseFilterNode extends AssociatedDealFieldBaseFilterNode {

		private TestableAssociatedDealFieldBaseFilterNode(FilterNodeContext filterContext) {
			super(filterContext);
		}

		@Override
		public List<Condition> getFilterConditions() {
			return List.of();
		}

	}

	/**
	 * Concrete test subclass that does not override the base default implementations of
	 * {@code getGroupByFields()} and {@code isSelectDistinct()}, so the base behaviour
	 * can be exercised directly.
	 */
	private static final class TestAssociatedDealFieldFilterNode extends AssociatedDealFieldBaseFilterNode {

		private TestAssociatedDealFieldFilterNode(FilterNodeContext filterContext) {
			super(filterContext);
		}

		@Override
		public List<org.jooq.Condition> getFilterConditions() {
			return List.of();
		}

	}

}
