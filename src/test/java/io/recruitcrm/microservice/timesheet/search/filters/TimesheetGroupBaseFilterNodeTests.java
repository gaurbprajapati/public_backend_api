package io.recruitcrm.microservice.timesheet.search.filters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsLessThanFilterNode;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetGroupBaseFilterNodeTestDataFactory;

@DisplayName("TimesheetGroupBaseFilterNode Tests")
class TimesheetGroupBaseFilterNodeTests {

	private FilterNodeContext filterNodeContext;

	@BeforeEach
	void setUp() {
		this.filterNodeContext = TimesheetGroupBaseFilterNodeTestDataFactory.createFilterNodeContext();
	}

	@Test
	@DisplayName("Get base table should return CST_TIMESHEET_T table")
	void testGetBaseTableReturnsCstTimesheetT() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		Table<?> result = filterNode.getBaseTable();

		// Then
		assertThat(result).isNotNull().isEqualTo(CstTimesheetT.CST_TIMESHEET_T);
	}

	@Test
	@DisplayName("Get select fields should return timesheet ID field")
	void testGetSelectFieldsReturnsTimesheetIdField() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> result = filterNode.getSelectFields();

		// Then
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0)).isEqualTo(CstTimesheetT.CST_TIMESHEET_T.ID);
	}

	@Test
	@DisplayName("Get account ID filter condition should match setting account_id equals context account id")
	void testGetAccountIdFilterConditionMatchesAccountIdFromContext() {
		// Given
		Integer expectedAccountId = 42;
		this.filterNodeContext.setAccountId(expectedAccountId);
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);
		Condition expected = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.ACCOUNT_ID.eq(expectedAccountId);

		// When
		Condition result = filterNode.getAccountIdFilterCondition();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Get join tables default implementation should return all joins")
	void testGetJoinTablesDefaultReturnsAllJoins() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> defaultJoins = filterNode.getJoinTables();
		List<TableJoinSpecification> allJoins = filterNode.exposeAllJoins();

		// Then
		assertThat(defaultJoins).hasSameSizeAs(allJoins).containsExactlyElementsOf(allJoins);
	}

	@Test
	@DisplayName("Get all joins should return nine left join specifications")
	void testGetAllJoinsReturnsNineLeftJoinSpecifications() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeAllJoins();

		// Then
		assertThat(result).hasSize(9);
		assertThat(result).extracting(TableJoinSpecification::getJoinType).containsOnly(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get minimal joins should return single timesheet setting join")
	void testGetMinimalJoinsReturnsSingleTimesheetSettingJoin() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeMinimalJoins();

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJoinType()).isEqualTo(TableJoinType.LEFT);
		assertThat(result.get(0).getJoinTable()).isEqualTo(CstTimesheetSettingT.CST_TIMESHEET_SETTING_T);
	}

	@Test
	@DisplayName("Get timesheet setting joins should match minimal joins")
	void testGetTimesheetSettingJoinsMatchesMinimalJoins() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> settingJoins = filterNode.exposeTimesheetSettingJoins();
		List<TableJoinSpecification> minimalJoins = filterNode.exposeMinimalJoins();

		// Then
		assertThat(settingJoins).containsExactlyElementsOf(minimalJoins);
	}

	@Test
	@DisplayName("Get contractor joins should return three join specifications")
	void testGetContractorJoinsReturnsThreeJoinSpecifications() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeContractorJoins();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result).extracting(TableJoinSpecification::getJoinType).containsOnly(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get job joins should return three join specifications")
	void testGetJobJoinsReturnsThreeJoinSpecifications() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeJobJoins();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result).extracting(TableJoinSpecification::getJoinType).containsOnly(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get company joins should return four join specifications")
	void testGetCompanyJoinsReturnsFourJoinSpecifications() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeCompanyJoins();

		// Then
		assertThat(result).hasSize(4);
		assertThat(result).extracting(TableJoinSpecification::getJoinType).containsOnly(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get currency joins should return three join specifications")
	void testGetCurrencyJoinsReturnsThreeJoinSpecifications() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeCurrencyJoins();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result).extracting(TableJoinSpecification::getJoinType).containsOnly(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get invoice joins should return three join specifications")
	void testGetInvoiceJoinsReturnsThreeJoinSpecifications() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.exposeInvoiceJoins();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result).extracting(TableJoinSpecification::getJoinType).containsOnly(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get join tables should return minimal joins for timesheet period field")
	void testGetJoinTablesReturnsMinimalJoinsForTimesheetPeriodField() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> result = filterNode.getJoinTables();

		// Then
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0).getJoinType()).isEqualTo(TableJoinType.LEFT);
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		List<Condition> result = filterNode.getCommonFilterCondition();

		// Then
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Get group by fields should return empty list")
	void testGetGroupByFieldsReturnsEmptyList() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> result = filterNode.getGroupByFields();

		// Then
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		Condition result = filterNode.getGroupByHavingCondition();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get search field should return period start field")
	void testGetSearchFieldReturnsPeriodStartField() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		Field<?> result = filterNode.getSearchField();

		// Then
		assertThat(result).isNotNull().isEqualTo(CstTimesheetT.CST_TIMESHEET_T.PERIOD_START);
	}

	@Test
	@DisplayName("Get search field with class should return typed field")
	void testGetSearchFieldWithClassReturnsTypedField() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		Field<Integer> result = filterNode.getSearchField(Integer.class);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get coerced search field with class should return typed field")
	void testGetCoercedSearchFieldWithClassReturnsTypedField() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		Field<Integer> result = filterNode.getCoercedSearchField(Integer.class);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get sub group comparison operator should return IN")
	void testGetSubGroupComparisonOperatorReturnsIn() {
		// Given
		FilterDto filterDto = TimesheetGroupBaseFilterNodeTestDataFactory.createTimesheetPeriodFilterDto();
		this.filterNodeContext.setFilterDto(filterDto);
		IsLessThanFilterNode filterNode = new IsLessThanFilterNode(this.filterNodeContext);

		// When
		SubGroupComparisonOperator result = filterNode.getSubGroupComparisonOperator();

		// Then
		assertThat(result).isNotNull().isEqualTo(SubGroupComparisonOperator.IN);
	}

	@Test
	@DisplayName("Get CTE query should build select for concrete timesheet group node")
	void testGetCteQueryBuildsSelectQueryForConcreteNode() {
		// Given
		TestTimesheetGroupBaseFilterNode filterNode = new TestTimesheetGroupBaseFilterNode(this.filterNodeContext);

		// When
		SelectQuery<?> query = filterNode.getCteQuery();

		// Then
		assertThat(query).isNotNull();
	}

	/**
	 * Concrete implementation used to exercise {@link TimesheetGroupBaseFilterNode}
	 * protected join helpers and default
	 * {@link TimesheetGroupBaseFilterNode#getJoinTables()}.
	 */
	private static final class TestTimesheetGroupBaseFilterNode extends TimesheetGroupBaseFilterNode {

		private TestTimesheetGroupBaseFilterNode(FilterNodeContext filterNodeContext) {
			super(filterNodeContext);
		}

		@Override
		public List<Condition> getFilterConditions() {
			return List.of();
		}

		@Override
		public Field<?> getSearchField() {
			return TimesheetGroupBaseFilterNode.TS.ID;
		}

		List<TableJoinSpecification> exposeAllJoins() {
			return this.getAllJoins();
		}

		List<TableJoinSpecification> exposeMinimalJoins() {
			return this.getMinimalJoins();
		}

		List<TableJoinSpecification> exposeTimesheetSettingJoins() {
			return this.getTimesheetSettingJoins();
		}

		List<TableJoinSpecification> exposeContractorJoins() {
			return this.getContractorJoins();
		}

		List<TableJoinSpecification> exposeJobJoins() {
			return this.getJobJoins();
		}

		List<TableJoinSpecification> exposeCompanyJoins() {
			return this.getCompanyJoins();
		}

		List<TableJoinSpecification> exposeCurrencyJoins() {
			return this.getCurrencyJoins();
		}

		List<TableJoinSpecification> exposeInvoiceJoins() {
			return this.getInvoiceJoins();
		}

	}

}
