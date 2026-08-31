/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.IsNotFilterNode}.
 */
public final class IsNotFilterNodeTestDataFactory {

	public static final String DB_FIELD_TIMESHEET_STATUS = "timesheetStatus";

	public static final String GROUP_TYPE_AND = "AND";

	public static final Integer DEFAULT_ACCOUNT_ID = 1;

	public static final String DEFAULT_GMT_DIFFERENCE = "+05:30";

	/**
	 * Single-condition result from {@code getFilterConditions} when a status is parsed.
	 */
	public static final int SINGLE_CONDITION_COUNT = 1;

	/**
	 * Invalid JSON integer token (leading zeros); JSON parsing fails, then the filter
	 * falls back to {@link Integer#parseInt(String)} on the raw filter string (yielding
	 * {@code 7}).
	 */
	public static final String FILTER_VALUE_LEADING_ZERO_REJECTED_BY_JSON = "007";

	/**
	 * JSON integral number too large for a 32-bit int; the inner JSON branch does not
	 * return a value, then {@link Integer#parseInt(String)} on the full filter string
	 * throws.
	 */
	public static final String FILTER_VALUE_JSON_LONG_NUMBER_NOT_INT = "21474836470";

	private IsNotFilterNodeTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static FilterDto createTimesheetStatusIsNotFilterDto(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(DB_FIELD_TIMESHEET_STATUS);
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType(GROUP_TYPE_AND);
		return filterDto;
	}

}
