/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.SearchFieldConstants;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.search.dto.GroupFilterListDto;
import java.util.List;

/**
 * Test data for
 * {@link io.recruitcrm.microservice.timesheet.search.cte.ContractorFilterSearchCteProvider}
 * unit tests (contractor-scoped filters require
 * {@link SearchFieldConstants#GROUP_TYPE_CONTRACTORS}).
 */
public final class ContractorFilterSearchCteProviderTestDataFactory {

	private ContractorFilterSearchCteProviderTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return 1;
	}

	public static String getDefaultGmtDifference() {
		return "+05:30";
	}

	public static Integer getAlternateAccountId() {
		return 999;
	}

	public static String getAlternateGmtDifference() {
		return "-08:00";
	}

	private static FilterDto contractorStatusIs(String statusValue) {
		FilterDto filter = new FilterDto();
		filter.setDbField(SearchFieldConstants.FIELD_STATUS);
		filter.setFilterType(FilterTypes.IS);
		filter.setFilterValue(statusValue);
		filter.setGroupType(SearchFieldConstants.GROUP_TYPE_CONTRACTORS);
		return filter;
	}

	private static FilterDto contractorDealNameIs() {
		FilterDto filter = new FilterDto();
		filter.setDbField(SearchFieldConstants.FIELD_DEAL_NAME);
		filter.setFilterType(FilterTypes.IS);
		filter.setFilterValue("10,20");
		filter.setGroupType(SearchFieldConstants.GROUP_TYPE_CONTRACTORS);
		return filter;
	}

	/**
	 * One group with a single contractor {@code status} IS filter.
	 */
	public static FilterSearchListDto createFilterSearchListSingleContractorStatus() {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator("AND");
		GroupFilterListDto group = new GroupFilterListDto();
		group.setGroupFilterJoinOperator("OR");
		group.setFilters(List.of(contractorStatusIs("0")));
		list.setGroupFilterList(List.of(group));
		return list;
	}

	/**
	 * One group with contractor {@code status} and {@code dealName} filters.
	 */
	public static FilterSearchListDto createFilterSearchListStatusAndDealName() {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator("AND");
		GroupFilterListDto group = new GroupFilterListDto();
		group.setGroupFilterJoinOperator("AND");
		group.setFilters(List.of(contractorStatusIs("0"), contractorDealNameIs()));
		list.setGroupFilterList(List.of(group));
		return list;
	}

	/**
	 * Two top-level groups, each with one status filter (different values) so SQL differs
	 * only by root AND vs OR connective.
	 */
	public static FilterSearchListDto createFilterSearchListTwoGroupsStatusOnly(String rootJoinOperator,
			String firstSubJoin, String secondSubJoin) {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator(rootJoinOperator);
		GroupFilterListDto group1 = new GroupFilterListDto();
		group1.setGroupFilterJoinOperator(firstSubJoin);
		group1.setFilters(List.of(contractorStatusIs("0")));
		GroupFilterListDto group2 = new GroupFilterListDto();
		group2.setGroupFilterJoinOperator(secondSubJoin);
		group2.setFilters(List.of(contractorStatusIs("1")));
		list.setGroupFilterList(List.of(group1, group2));
		return list;
	}

	public static FilterSearchListDto createFilterSearchListInvalidRootJoinOperator() {
		FilterSearchListDto list = createFilterSearchListSingleContractorStatus();
		list.setGroupJoinOperator("NOT_A_REAL_OPERATOR");
		return list;
	}

}
