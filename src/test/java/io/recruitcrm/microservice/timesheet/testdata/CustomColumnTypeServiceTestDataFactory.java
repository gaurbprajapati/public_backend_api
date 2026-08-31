package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test data factory for CustomColumnTypeService unit tests. Provides factory methods for
 * creating test data following the mandatory rule patterns.
 */
public final class CustomColumnTypeServiceTestDataFactory {

	// ==================== Constants ====================

	/**
	 * Success message for field type operations
	 */
	public static final String FIELD_TYPE_SUCCESS = "Field types retrieved successfully";

	/**
	 * Error message for field type operations
	 */
	public static final String FIELD_TYPE_FAILED = "Field type retrieval failed";

	/**
	 * Test account ID
	 */
	public static final Integer TEST_ACCOUNT_ID = 12345;

	/**
	 * Test column IDs
	 */
	public static final Integer COLUMN_ID_1 = 1;

	public static final Integer COLUMN_ID_5 = 5;

	public static final Integer COLUMN_ID_10 = 10;

	public static final Integer COLUMN_ID_15 = 15;

	/**
	 * Field type constants
	 */
	public static final String FIELD_TYPE_TEXT = "text";

	public static final String FIELD_TYPE_DATE = "date";

	public static final String FIELD_TYPE_DATETIME = "datetime";

	public static final String FIELD_TYPE_CHECKBOX = "checkbox";

	public static final String FIELD_TYPE_NUMBER = "number";

	private CustomColumnTypeServiceTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Column ID Lists ====================

	/**
	 * Creates a list of column IDs for testing
	 */
	public static List<Integer> createColumnIdList() {
		return List.of(COLUMN_ID_1, COLUMN_ID_5, COLUMN_ID_10);
	}

	/**
	 * Creates a list of column IDs with missing entries
	 */
	public static List<Integer> createColumnIdListWithMissing() {
		return List.of(COLUMN_ID_1, COLUMN_ID_15);
	}

	/**
	 * Creates a single column ID list
	 */
	public static List<Integer> createSingleColumnIdList() {
		return List.of(COLUMN_ID_1);
	}

	/**
	 * Creates an empty column ID list
	 */
	public static List<Integer> createEmptyColumnIdList() {
		return List.of();
	}

	// ==================== Extra Field Definitions ====================

	/**
	 * Creates a text field definition
	 */
	public static ExtraFieldDefinitionDto createTextFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_1, "First Name", FIELD_TYPE_TEXT, 5, TEST_ACCOUNT_ID);
	}

	/**
	 * Creates a date field definition
	 */
	public static ExtraFieldDefinitionDto createDateFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_5, "Birth Date", FIELD_TYPE_DATE, 5, TEST_ACCOUNT_ID);
	}

	/**
	 * Creates a datetime field definition
	 */
	public static ExtraFieldDefinitionDto createDateTimeFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_10, "Last Login", FIELD_TYPE_DATETIME, 5, TEST_ACCOUNT_ID);
	}

	/**
	 * Creates a checkbox field definition
	 */
	public static ExtraFieldDefinitionDto createCheckboxFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_15, "Is Active", FIELD_TYPE_CHECKBOX, 5, TEST_ACCOUNT_ID);
	}

	// ==================== Field Type Maps ====================

	/**
	 * Creates a map of extra field definitions
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createExtraFieldDefinitionsMap() {
		Map<Integer, ExtraFieldDefinitionDto> map = new HashMap<>();
		map.put(COLUMN_ID_1, createTextFieldDefinition());
		map.put(COLUMN_ID_5, createDateFieldDefinition());
		map.put(COLUMN_ID_10, createDateTimeFieldDefinition());
		return map;
	}

	/**
	 * Creates a map of extra field definitions with missing entries
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createExtraFieldDefinitionsMapWithMissing() {
		Map<Integer, ExtraFieldDefinitionDto> map = new HashMap<>();
		map.put(COLUMN_ID_1, createTextFieldDefinition());
		// COLUMN_ID_15 is missing
		return map;
	}

	/**
	 * Creates an empty extra field definitions map
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createEmptyExtraFieldDefinitionsMap() {
		return new HashMap<>();
	}

	/**
	 * Creates expected field types map
	 */
	public static Map<Integer, String> createExpectedFieldTypesMap() {
		Map<Integer, String> map = new HashMap<>();
		map.put(COLUMN_ID_1, FIELD_TYPE_TEXT);
		map.put(COLUMN_ID_5, FIELD_TYPE_DATE);
		map.put(COLUMN_ID_10, FIELD_TYPE_DATETIME);
		return map;
	}

	// ==================== Value Conversion Test Data ====================

	/**
	 * Creates a valid Unix timestamp string (represents 2023-01-01 00:00:00 UTC)
	 */
	public static String createValidUnixTimestamp() {
		return "1672531200";
	}

	/**
	 * Creates an invalid timestamp string
	 */
	public static String createInvalidTimestamp() {
		return "invalid_timestamp";
	}

	/**
	 * Creates checkbox true value
	 */
	public static String createCheckboxTrueValue() {
		return "1";
	}

	/**
	 * Creates checkbox false value
	 */
	public static String createCheckboxFalseValue() {
		return "0";
	}

	/**
	 * Creates checkbox invalid value
	 */
	public static String createCheckboxInvalidValue() {
		return "maybe";
	}

	/**
	 * Creates null value for testing
	 */
	public static Object createNullValue() {
		return null;
	}

	/**
	 * Creates empty string value
	 */
	public static String createEmptyStringValue() {
		return "";
	}

	/**
	 * Creates whitespace string value
	 */
	public static String createWhitespaceStringValue() {
		return "   ";
	}

	/**
	 * Creates regular text value
	 */
	public static String createRegularTextValue() {
		return "Sample Text";
	}

	// ==================== Expected Conversion Results ====================

	/**
	 * Expected date format result for valid timestamp
	 */
	public static String getExpectedDateFormat() {
		return "01/01/2023";
	}

	/**
	 * Expected datetime format result for valid timestamp
	 */
	public static String getExpectedDateTimeFormat() {
		return "01/01/2023 00:00:00";
	}

	/**
	 * Expected checkbox true result
	 */
	public static String getExpectedCheckboxTrue() {
		return "true";
	}

	/**
	 * Expected checkbox false result
	 */
	public static String getExpectedCheckboxFalse() {
		return "false";
	}

}
