/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.search.dto.GroupFilterListDto;
import java.util.List;

/**
 * Test data for
 * {@link io.recruitcrm.microservice.timesheet.search.cte.TimesheetFilterSearchCteProvider}
 * unit tests.
 */
public final class TimesheetFilterSearchCteProviderTestDataFactory {

	private TimesheetFilterSearchCteProviderTestDataFactory() {
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

	/**
	 * One group, one {@code added_on} IS TODAY filter (timesheet scope).
	 */
	public static FilterSearchListDto createFilterSearchListSingleAddedOnToday() {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator("AND");
		GroupFilterListDto group = new GroupFilterListDto();
		group.setGroupFilterJoinOperator("OR");
		FilterDto filter = new FilterDto();
		filter.setDbField("added_on");
		filter.setFilterType(FilterTypes.IS);
		filter.setFilterValue("TODAY");
		filter.setGroupType("AND");
		group.setFilters(List.of(filter));
		list.setGroupFilterList(List.of(group));
		return list;
	}

	/**
	 * Two filters in one subgroup (exercises multi-filter subgroup SQL).
	 */
	public static FilterSearchListDto createFilterSearchListAddedOnAndTimesheetPeriod() {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator("AND");
		GroupFilterListDto group = new GroupFilterListDto();
		group.setGroupFilterJoinOperator("AND");
		FilterDto addedOn = new FilterDto();
		addedOn.setDbField("added_on");
		addedOn.setFilterType(FilterTypes.IS);
		addedOn.setFilterValue("TODAY");
		addedOn.setGroupType("AND");
		FilterDto period = new FilterDto();
		period.setDbField("timesheetPeriod");
		period.setFilterType(FilterTypes.IS);
		period.setFilterValue("THIS_MONTH");
		period.setGroupType("AND");
		group.setFilters(List.of(addedOn, period));
		list.setGroupFilterList(List.of(group));
		return list;
	}

	/**
	 * Two top-level groups; join operators are configurable (drives {@code GroupANDNode}
	 * vs {@code GroupORNode}).
	 */
	public static FilterSearchListDto createFilterSearchListTwoGroups(String rootGroupJoinOperator,
			String firstSubGroupJoin, String secondSubGroupJoin) {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator(rootGroupJoinOperator);
		GroupFilterListDto group1 = new GroupFilterListDto();
		group1.setGroupFilterJoinOperator(firstSubGroupJoin);
		FilterDto f1 = new FilterDto();
		f1.setDbField("added_on");
		f1.setFilterType(FilterTypes.IS);
		f1.setFilterValue("TODAY");
		f1.setGroupType("AND");
		group1.setFilters(List.of(f1));
		GroupFilterListDto group2 = new GroupFilterListDto();
		group2.setGroupFilterJoinOperator(secondSubGroupJoin);
		FilterDto f2 = new FilterDto();
		f2.setDbField("added_on");
		f2.setFilterType(FilterTypes.IS);
		f2.setFilterValue("YESTERDAY");
		f2.setGroupType("AND");
		group2.setFilters(List.of(f2));
		list.setGroupFilterList(List.of(group1, group2));
		return list;
	}

	public static FilterSearchListDto createFilterSearchListAddedOnIsBetween() {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator("AND");
		GroupFilterListDto group = new GroupFilterListDto();
		group.setGroupFilterJoinOperator("OR");
		FilterDto filter = new FilterDto();
		filter.setDbField("added_on");
		filter.setFilterType(FilterTypes.IS_BETWEEN);
		filter.setFilterValue("{\"start\":\"1633046400\",\"end\":\"1635724800\"}");
		filter.setGroupType("AND");
		group.setFilters(List.of(filter));
		list.setGroupFilterList(List.of(group));
		return list;
	}

	public static FilterSearchListDto createFilterSearchListAssociatedDeal() {
		FilterSearchListDto list = new FilterSearchListDto();
		list.setGroupJoinOperator("AND");
		GroupFilterListDto group = new GroupFilterListDto();
		group.setGroupFilterJoinOperator("OR");
		FilterDto filter = new FilterDto();
		filter.setDbField("associatedDeal");
		filter.setFilterType(FilterTypes.IS);
		filter.setFilterValue("1,2,3");
		filter.setGroupType("AND");
		group.setFilters(List.of(filter));
		list.setGroupFilterList(List.of(group));
		return list;
	}

	/**
	 * Invalid root join string defaults to AND in {@code NodeFactory}.
	 */
	public static FilterSearchListDto createFilterSearchListInvalidRootJoinOperator() {
		FilterSearchListDto list = createFilterSearchListSingleAddedOnToday();
		list.setGroupJoinOperator("NOT_A_REAL_OPERATOR");
		return list;
	}

}
