package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode}
 * and related filter-node tests.
 */
public final class TimesheetGroupBaseFilterNodeTestDataFactory {

	public static final Integer DEFAULT_ACCOUNT_ID = 1;

	public static final String DEFAULT_GMT_DIFFERENCE = "+05:30";

	private TimesheetGroupBaseFilterNodeTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static FilterNodeContext createFilterNodeContext() {
		FilterNodeContext filterNodeContext = new FilterNodeContext();
		filterNodeContext.setAccountId(DEFAULT_ACCOUNT_ID);
		filterNodeContext.setGmtDifference(DEFAULT_GMT_DIFFERENCE);
		return filterNodeContext;
	}

	public static FilterDto createTimesheetPeriodFilterDto() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("timesheetPeriod");
		filterDto.setFilterType(FilterTypes.IS_LESS_THAN);
		filterDto.setFilterValue("10");
		filterDto.setGroupType("AND");
		return filterDto;
	}

}
