package io.recruitcrm.microservice.timesheet.testdata;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.search.dto.GroupFilterListDto;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.services.search.TimesheetSearchService}
 * tests.
 */
public final class TimesheetSearchTestDataFactory {

	private static final Integer DEFAULT_ACCOUNT_ID = Integer.valueOf(1);

	private static final String DEFAULT_GMT_DIFFERENCE = "+05:30";

	private static final int DEFAULT_PAGE_NUMBER = 0;

	private static final int DEFAULT_PAGE_SIZE = 10;

	private TimesheetSearchTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Default account id used in search tests.
	 */
	public static Integer getDefaultAccountId() {
		return TimesheetSearchTestDataFactory.DEFAULT_ACCOUNT_ID;
	}

	/**
	 * Default GMT offset string used in search tests.
	 */
	public static String getDefaultGmtDifference() {
		return TimesheetSearchTestDataFactory.DEFAULT_GMT_DIFFERENCE;
	}

	/**
	 * Default first page with page size 10.
	 */
	public static Pageable createDefaultPageable() {
		return PageRequest.of(TimesheetSearchTestDataFactory.DEFAULT_PAGE_NUMBER,
				TimesheetSearchTestDataFactory.DEFAULT_PAGE_SIZE);
	}

	/**
	 * Builds a minimal valid {@link FilterSearchListDto} for timesheet filter search
	 * (added_on TODAY).
	 */
	public static FilterSearchListDto createFilterSearchListDto() {
		FilterSearchListDto filterSearchListDto = new FilterSearchListDto();
		filterSearchListDto.setGroupJoinOperator("AND");
		GroupFilterListDto groupFilterListDto = new GroupFilterListDto();
		groupFilterListDto.setGroupFilterJoinOperator("AND");
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");
		groupFilterListDto.setFilters(List.of(filterDto));
		filterSearchListDto.setGroupFilterList(List.of(groupFilterListDto));
		return filterSearchListDto;
	}

}
