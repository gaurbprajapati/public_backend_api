/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.search.cte;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.testdata.ContractorFilterSearchCteProviderTestDataFactory;
import org.jooq.CommonTableExpression;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ContractorFilterSearchCteProvider}: CTE naming, jOOQ wiring to
 * {@code tblcandidate}, contractor filter AST wiring, and constructor-bound account id.
 */
@DisplayName("ContractorFilterSearchCteProvider Tests")
class ContractorFilterSearchCteProviderTests {

	private ContractorFilterSearchCteProvider cteProvider;

	@BeforeEach
	void setUp() {
		this.cteProvider = new ContractorFilterSearchCteProvider(
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference());
	}

	private static String renderCte(CommonTableExpression<?> cte) {
		return DSL.using(SQLDialect.MYSQL).renderInlined(DSL.with(cte).select(DSL.one())).toLowerCase();
	}

	@Test
	@DisplayName("getCteName returns stable contractor CTE identifier")
	void testGetCteNameReturnsContractorFilterSearchCte() {
		// When
		String name = this.cteProvider.getCteName();

		// Then
		assertThat(name).isEqualTo("contractorFilterSearchCte");
	}

	@Test
	@DisplayName("getCte wraps contractor AST in named CTE whose body references candidate table and id")
	void testGetCteRendersSqlAgainstCandidateTable() {
		// Given
		FilterSearchListDto dto = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListSingleContractorStatus();

		// When
		CommonTableExpression<?> cte = this.cteProvider.getCte(dto,
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference());

		// Then
		assertThat(cte.getName()).isEqualTo("contractorFilterSearchCte");
		String sql = renderCte(cte);
		assertThat(sql).contains(Tblcandidate.TBLCANDIDATE.getName().toLowerCase())
			.contains(Tblcandidate.TBLCANDIDATE.ID.getName().toLowerCase());
	}

	@Test
	@DisplayName("getCte account and GMT method parameters do not change rendered SQL")
	void testGetCteIgnoresMethodLevelAccountAndGmtArguments() {
		// Given
		FilterSearchListDto dto = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListSingleContractorStatus();

		// When
		String sqlDefaultArgs = renderCte(
				this.cteProvider.getCte(dto, ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));
		String sqlOtherArgs = renderCte(
				this.cteProvider.getCte(dto, ContractorFilterSearchCteProviderTestDataFactory.getAlternateAccountId(),
						ContractorFilterSearchCteProviderTestDataFactory.getAlternateGmtDifference()));

		// Then
		assertThat(sqlOtherArgs).isEqualTo(sqlDefaultArgs);
	}

	@Test
	@DisplayName("getCte with status and deal filters in one subgroup combines both predicates")
	void testGetCteStatusAndDealFiltersInOneSubgroup() {
		// Given
		FilterSearchListDto dto = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListStatusAndDealName();

		// When
		String sql = renderCte(
				this.cteProvider.getCte(dto, ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sql).contains("10").contains("20");
	}

	@Test
	@DisplayName("Two top-level groups with AND vs OR root join produce different outer SQL connectives")
	void testGetCteTwoGroupsRootAndVersusOrChangesRenderedSql() {
		// Given
		FilterSearchListDto dtoAnd = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListTwoGroupsStatusOnly("AND", "AND", "AND");
		FilterSearchListDto dtoOr = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListTwoGroupsStatusOnly("OR", "AND", "AND");

		// When
		String sqlAnd = renderCte(
				this.cteProvider.getCte(dtoAnd, ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));
		String sqlOr = renderCte(
				this.cteProvider.getCte(dtoOr, ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
						ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sqlAnd).contains("group0", "group1");
		assertThat(sqlOr).contains("group0", "group1").isNotEqualTo(sqlAnd).contains(" or ");
	}

	@Test
	@DisplayName("Invalid root group join operator falls back to AND semantics via NodeFactory")
	void testGetCteInvalidRootJoinOperatorDefaultsToAndBehaviour() {
		// Given
		FilterSearchListDto invalidRoot = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListInvalidRootJoinOperator();
		FilterSearchListDto explicitAnd = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListSingleContractorStatus();

		// When
		String sqlInvalid = renderCte(this.cteProvider.getCte(invalidRoot,
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));
		String sqlExplicitAnd = renderCte(this.cteProvider.getCte(explicitAnd,
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultAccountId(),
				ContractorFilterSearchCteProviderTestDataFactory.getDefaultGmtDifference()));

		// Then
		assertThat(sqlInvalid).isEqualTo(sqlExplicitAnd);
	}

	@Test
	@DisplayName("Constructor account id is applied in contractor filter SQL; getCte account argument does not override it")
	void testConstructorAccountIdUsedInSqlNotGetCteAccountParameter() {
		// Given
		Integer dedicatedAccount = 515151;
		String dedicatedGmt = "+02:00";
		ContractorFilterSearchCteProvider dedicatedProvider = new ContractorFilterSearchCteProvider(dedicatedAccount,
				dedicatedGmt);
		FilterSearchListDto dto = ContractorFilterSearchCteProviderTestDataFactory
			.createFilterSearchListStatusAndDealName();

		// When
		String sql = renderCte(
				dedicatedProvider.getCte(dto, ContractorFilterSearchCteProviderTestDataFactory.getAlternateAccountId(),
						ContractorFilterSearchCteProviderTestDataFactory.getAlternateGmtDifference()));

		// Then
		assertThat(sql).contains(String.valueOf(dedicatedAccount))
			.doesNotContain(String.valueOf(ContractorFilterSearchCteProviderTestDataFactory.getAlternateAccountId()));
	}

}
