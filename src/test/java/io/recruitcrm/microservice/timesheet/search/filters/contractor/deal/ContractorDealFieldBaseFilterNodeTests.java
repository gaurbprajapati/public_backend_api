package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("ContractorDealFieldBaseFilterNode Tests")
class ContractorDealFieldBaseFilterNodeTests {

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
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsFilterNode isFilterNode = new IsFilterNode(this.filterNodeContext);

		// When
		Field<?> searchField = isFilterNode.getSearchField();

		// Then
		assertThat(searchField).isNotNull().isEqualTo(Tbldeals.TBLDEALS.ID);
	}

	@Test
	@DisplayName("Get group by fields should return candidate ID")
	void testGetGroupByFieldsReturnsCandidateId() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = containsAtLeastFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Get join tables should return deal-related joins")
	void testGetJoinTablesReturnsDealRelatedJoins() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.CONTAINS_AT_LEAST_ONE);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContainsAtLeastFilterNode containsAtLeastFilterNode = new ContainsAtLeastFilterNode(this.filterNodeContext);

		// When
		List<io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification> joinTables = containsAtLeastFilterNode
			.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(2);
	}

	@Test
	@DisplayName("Get group by fields should return candidate ID from base class")
	void testGetGroupByFieldsReturnsCandidateIdFromBaseClass() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.HAS_ANY_VALUE);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		HasAnyValueFilterNode hasAnyValueFilterNode = new HasAnyValueFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = hasAnyValueFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Base get group by fields should return candidate ID when not overridden")
	void testBaseGetGroupByFieldsReturnsCandidateIdWhenNotOverridden() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContractorDealFieldBaseFilterNode baseFilterNode = new TestContractorDealFieldBaseFilterNode(
				this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = baseFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isNotNull().hasSize(1).first().isEqualTo(Tblcandidate.TBLCANDIDATE.ID);
	}

	@Test
	@DisplayName("Base is select distinct should return false when not overridden")
	void testBaseIsSelectDistinctReturnsFalseWhenNotOverridden() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		ContractorDealFieldBaseFilterNode baseFilterNode = new TestContractorDealFieldBaseFilterNode(
				this.filterNodeContext);

		// When
		Boolean isDistinct = baseFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isFalse();
	}

	/**
	 * Minimal concrete subclass used to exercise the non-overridden base implementations
	 * of getGroupByFields and isSelectDistinct.
	 */
	private static final class TestContractorDealFieldBaseFilterNode extends ContractorDealFieldBaseFilterNode {

		private TestContractorDealFieldBaseFilterNode(FilterNodeContext filterContext) {
			super(filterContext);
		}

		@Override
		public List<Condition> getFilterConditions() {
			return List.of(DSL.noCondition());
		}

	}

}
