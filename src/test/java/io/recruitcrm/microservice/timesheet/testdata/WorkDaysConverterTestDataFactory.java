package io.recruitcrm.microservice.timesheet.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test data factory for WorkDaysConverter tests. Provides test data for work days JSON
 * conversion scenarios.
 */
public final class WorkDaysConverterTestDataFactory {

	private WorkDaysConverterTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Test constants for work days JSON
	 */
	public static final String VALID_SINGLE_DAY_JSON = "[{\"workDayId\":1}]";

	public static final String VALID_MULTIPLE_DAYS_JSON = "[{\"workDayId\":1},{\"workDayId\":2},{\"workDayId\":3}]";

	public static final String VALID_ALL_DAYS_JSON = "[{\"workDayId\":1},{\"workDayId\":2},{\"workDayId\":3},{\"workDayId\":4},{\"workDayId\":5},{\"workDayId\":6},{\"workDayId\":7}]";

	public static final String VALID_WEEKEND_DAYS_JSON = "[{\"workDayId\":6},{\"workDayId\":7}]";

	public static final String VALID_STRING_DAY_IDS_JSON = "[{\"workDayId\":\"1\"},{\"workDayId\":\"2\"}]";

	public static final String INVALID_JSON = "invalid json";

	public static final String EMPTY_JSON = "";

	public static final String NULL_JSON = null;

	public static final String WHITESPACE_JSON = "   ";

	public static final String EMPTY_ARRAY_JSON = "[]";

	public static final String NULL_WORK_DAY_ID_JSON = "[{\"workDayId\":null}]";

	public static final String MISSING_WORK_DAY_ID_JSON = "[{\"otherField\":\"value\"}]";

	public static final String INVALID_DAY_ID_JSON = "[{\"workDayId\":99}]";

	public static final String MIXED_VALID_INVALID_JSON = "[{\"workDayId\":1},{\"workDayId\":99},{\"workDayId\":2}]";

	/**
	 * Test constants for expected results
	 */
	public static final String EXPECTED_SINGLE_DAY = "Monday";

	public static final String EXPECTED_MULTIPLE_DAYS = "Monday, Tuesday, Wednesday";

	public static final String EXPECTED_ALL_DAYS = "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday";

	public static final String EXPECTED_WEEKEND_DAYS = "Saturday, Sunday";

	public static final String EXPECTED_STRING_DAY_IDS = "Monday, Tuesday";

	public static final String EXPECTED_EMPTY_RESULT = "";

	public static final String EXPECTED_MIXED_VALID_INVALID = "Monday, Tuesday";

	/**
	 * Test constants for individual day IDs
	 */
	public static final Integer MONDAY_ID = 1;

	public static final Integer TUESDAY_ID = 2;

	public static final Integer WEDNESDAY_ID = 3;

	public static final Integer THURSDAY_ID = 4;

	public static final Integer FRIDAY_ID = 5;

	public static final Integer SATURDAY_ID = 6;

	public static final Integer SUNDAY_ID = 7;

	public static final Integer INVALID_DAY_ID = 99;

	public static final String STRING_MONDAY_ID = "1";

	public static final String STRING_TUESDAY_ID = "2";

	/**
	 * Test constants for day names
	 */
	public static final String MONDAY_NAME = "Monday";

	public static final String TUESDAY_NAME = "Tuesday";

	public static final String WEDNESDAY_NAME = "Wednesday";

	public static final String THURSDAY_NAME = "Thursday";

	public static final String FRIDAY_NAME = "Friday";

	public static final String SATURDAY_NAME = "Saturday";

	public static final String SUNDAY_NAME = "Sunday";

	/**
	 * Creates valid single day JSON
	 */
	public static String createValidSingleDayJson() {
		return VALID_SINGLE_DAY_JSON;
	}

	/**
	 * Creates valid multiple days JSON
	 */
	public static String createValidMultipleDaysJson() {
		return VALID_MULTIPLE_DAYS_JSON;
	}

	/**
	 * Creates valid all days JSON
	 */
	public static String createValidAllDaysJson() {
		return VALID_ALL_DAYS_JSON;
	}

	/**
	 * Creates valid weekend days JSON
	 */
	public static String createValidWeekendDaysJson() {
		return VALID_WEEKEND_DAYS_JSON;
	}

	/**
	 * Creates valid JSON with string day IDs
	 */
	public static String createValidStringDayIdsJson() {
		return VALID_STRING_DAY_IDS_JSON;
	}

	/**
	 * Creates invalid JSON
	 */
	public static String createInvalidJson() {
		return INVALID_JSON;
	}

	/**
	 * Creates empty JSON
	 */
	public static String createEmptyJson() {
		return EMPTY_JSON;
	}

	/**
	 * Creates whitespace JSON
	 */
	public static String createWhitespaceJson() {
		return WHITESPACE_JSON;
	}

	/**
	 * Creates empty array JSON
	 */
	public static String createEmptyArrayJson() {
		return EMPTY_ARRAY_JSON;
	}

	/**
	 * Creates JSON with null work day ID
	 */
	public static String createNullWorkDayIdJson() {
		return NULL_WORK_DAY_ID_JSON;
	}

	/**
	 * Creates JSON with missing work day ID
	 */
	public static String createMissingWorkDayIdJson() {
		return MISSING_WORK_DAY_ID_JSON;
	}

	/**
	 * Creates JSON with invalid day ID
	 */
	public static String createInvalidDayIdJson() {
		return INVALID_DAY_ID_JSON;
	}

	/**
	 * Creates JSON with mixed valid and invalid day IDs
	 */
	public static String createMixedValidInvalidJson() {
		return MIXED_VALID_INVALID_JSON;
	}

	/**
	 * Creates ObjectMapper for testing
	 */
	public static ObjectMapper createObjectMapper() {
		return new ObjectMapper();
	}

	/**
	 * Success messages for tests
	 */
	public static final class Messages {

		public static final String WORK_DAYS_CONVERTED_SUCCESSFULLY = "Work days converted successfully";

		public static final String INVALID_JSON_HANDLED_GRACEFULLY = "Invalid JSON handled gracefully";

		public static final String NULL_INPUT_HANDLED_GRACEFULLY = "Null input handled gracefully";

		public static final String EMPTY_INPUT_HANDLED_GRACEFULLY = "Empty input handled gracefully";

		public static final String INVALID_DAY_ID_FILTERED_OUT = "Invalid day ID filtered out";

	}

}
