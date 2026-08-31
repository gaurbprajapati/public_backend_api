package io.recruitcrm.microservice.timesheet.search.cte;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetFilterSearchCteProviderTestDataFactory;
import org.jooq.CommonTableExpression;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimesheetFilterSearchCteProvider}: CTE naming, jOOQ wiring to
 * {@code cst_timesheet_t}, and filter AST combinations (including join-operator
 * behaviour).
 */
@DisplayName("TimesheetFilterSearchCteProvider Tests")
class TimesheetFilterSearchCteProviderTests {

	private TimesheetFilterSearchCteProvider cteProvider;

	@BeforeEach
	void setUp() {
		this.cteProvider = new TimesheetFilterSearchCteProvider(
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference());
	}

	private static String renderCte(CommonTableExpression<?> cte) {
		return DSL.using(SQLDialect.MYSQL).renderInlined(DSL.with(cte).select(DSL.one())).toLowerCase();
	}

	@Test
	@DisplayName("getCteName returns stable CTE identifier used by search queries")
	void testGetCteNameReturnsFilterSearchCte() {
		// When
		String name = this.cteProvider.getCteName();

		// Then
		assertThat(name).isEqualTo("filterSearchCte");
	}

	@Test
	@DisplayName("getCte wraps AST in named CTE whose body references the timesheet table and id column")
	void testGetCteRendersSqlAgainstTimesheetTable() {
		// Given
		FilterSearchListDto dto = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListSingleAddedOnToday();

		// When
		CommonTableExpression<?> cte = this.cteProvider.getCte(dto,
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference());

		// Then
		assertThat(cte.getName()).isEqualTo("filterSearchCte");
		String sql = renderCte(cte);
		assertThat(sql).contains(CstTimesheetT.CST_TIMESHEET_T.getName().toLowerCase())
			.contains(CstTimesheetT.CST_TIMESHEET_T.ID.getName().toLowerCase());
	}

	@Test
	@DisplayName("getCte getCteName and runtime account or GMT arguments do not change rendered SQL")
	void testGetCteIgnoresMethodLevelAccountAndGmtArguments() {
		// Given
		FilterSearchListDto dto = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListSingleAddedOnToday();

		// When
		String sqlDefaultArgs = renderCte(
				this.cteProvider.getCte(dto, TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));
		String sqlOtherArgs = renderCte(
				this.cteProvider.getCte(dto, TimesheetFilterSearchCteProviderTestDataFactory.getAlternateAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getAlternateGmtDifference()));

		// Then
		assertThat(sqlOtherArgs).isEqualTo(sqlDefaultArgs);
	}

	@Test
	@DisplayName("getCte with IS_BETWEEN added_on emits a bounded range predicate")
	void testGetCteWithIsBetweenAddedOnRendersBetweenStylePredicate() {
		// Given
		FilterSearchListDto dto = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListAddedOnIsBetween();

		// When
		String sql = renderCte(
				this.cteProvider.getCte(dto, TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sql).contains("between");
	}

	@Test
	@DisplayName("getCte with associated_deal filter includes deal identifiers in SQL")
	void testGetCteWithAssociatedDealRendersInPredicate() {
		// Given
		FilterSearchListDto dto = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListAssociatedDeal();

		// When
		String sql = renderCte(
				this.cteProvider.getCte(dto, TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sql).contains("1").contains("2").contains("3");
	}

	@Test
	@DisplayName("getCte with multiple filters in one subgroup combines both field predicates")
	void testGetCteWithMultipleFiltersInOneSubgroup() {
		// Given
		FilterSearchListDto dto = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListAddedOnAndTimesheetPeriod();

		// When
		String sql = renderCte(
				this.cteProvider.getCte(dto, TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sql).contains(CstTimesheetT.CST_TIMESHEET_T.ADDED_ON.getName().toLowerCase())
			.contains(CstTimesheetT.CST_TIMESHEET_T.PERIOD_START.getName().toLowerCase());
	}

	@Test
	@DisplayName("Two top-level groups with AND vs OR root join produce different outer SQL connectives")
	void testGetCteTwoGroupsRootAndVersusOrChangesRenderedSql() {
		// Given
		FilterSearchListDto dtoAnd = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListTwoGroups("AND", "AND", "AND");
		FilterSearchListDto dtoOr = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListTwoGroups("OR", "AND", "AND");

		// When
		String sqlAnd = renderCte(
				this.cteProvider.getCte(dtoAnd, TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));
		String sqlOr = renderCte(
				this.cteProvider.getCte(dtoOr, TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sqlAnd).contains("group0", "group1");
		assertThat(sqlOr).contains("group0", "group1").isNotEqualTo(sqlAnd).contains(" or ");
	}

	@Test
	@DisplayName("Invalid root group join operator falls back to AND semantics via NodeFactory")
	void testGetCteInvalidRootJoinOperatorDefaultsToAndBehaviour() {
		// Given
		FilterSearchListDto invalidRoot = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListInvalidRootJoinOperator();
		FilterSearchListDto explicitAnd = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListSingleAddedOnToday();

		// When
		String sqlInvalid = renderCte(this.cteProvider.getCte(invalidRoot,
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));
		String sqlExplicitAnd = renderCte(this.cteProvider.getCte(explicitAnd,
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				TimesheetFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sqlInvalid).isEqualTo(sqlExplicitAnd);
	}

	@Test
	@DisplayName("Constructor account id is applied in filter SQL; getCte account argument does not override it")
	void testConstructorAccountIdUsedInSqlNotGetCteAccountParameter() {
		// Given
		Integer dedicatedAccount = 4242;
		String dedicatedGmt = "+01:00";
		TimesheetFilterSearchCteProvider dedicatedProvider = new TimesheetFilterSearchCteProvider(dedicatedAccount,
				dedicatedGmt);
		FilterSearchListDto dto = TimesheetFilterSearchCteProviderTestDataFactory
			.createFilterSearchListAssociatedDeal();

		// When
		String sql = renderCte(
				dedicatedProvider.getCte(dto, TimesheetFilterSearchCteProviderTestDataFactory.getAlternateAccountId(),
						TimesheetFilterSearchCteProviderTestDataFactory.getAlternateGmtDifference()));

		// Then
		assertThat(sql).contains(String.valueOf(dedicatedAccount))
			.doesNotContain(String.valueOf(TimesheetFilterSearchCteProviderTestDataFactory.getAlternateAccountId()));
	}

}
