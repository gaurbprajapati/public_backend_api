package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test data factory for ExtraFieldsRepository unit tests. Provides factory methods for
 * creating test data objects with realistic values for comprehensive test coverage.
 */
public final class ExtraFieldsRepositoryTestDataFactory {

	private ExtraFieldsRepositoryTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Test Constants ====================

	public static final Integer TEST_ACCOUNT_ID = 1001;

	public static final Integer TEST_ACCOUNT_ID_2 = 1002;

	public static final EntityType TEST_ENTITY_TYPE = EntityType.CANDIDATE;

	public static final Integer TEST_ENTITY_TYPE_ID = 5;

	public static final Integer COLUMN_ID_1 = 1;

	public static final Integer COLUMN_ID_2 = 5;

	public static final Integer COLUMN_ID_3 = 10;

	public static final Integer COLUMN_ID_4 = 15;

	public static final Integer COLUMN_ID_5 = 20;

	public static final String FIELD_NAME_1 = "Custom Field 1";

	public static final String FIELD_NAME_2 = "Date Field";

	public static final String FIELD_NAME_3 = "Number Field";

	public static final String FIELD_NAME_4 = "Text Field";

	public static final String FIELD_NAME_5 = "Boolean Field";

	public static final String FIELD_TYPE_TEXT = "text";

	public static final String FIELD_TYPE_DATE = "date";

	public static final String FIELD_TYPE_NUMBER = "number";

	public static final String FIELD_TYPE_BOOLEAN = "boolean";

	// ==================== Factory Methods for Column IDs ====================

	/**
	 * Creates a list of column IDs for testing
	 */
	public static List<Integer> createColumnIdList() {
		return Arrays.asList(COLUMN_ID_1, COLUMN_ID_2, COLUMN_ID_3);
	}

	/**
	 * Creates a list of test column IDs for bulk operations
	 */
	public static List<Integer> createTestColumnIdsList() {
		return Arrays.asList(COLUMN_ID_1, COLUMN_ID_2, COLUMN_ID_3);
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
		return new ArrayList<>();
	}

	/**
	 * Creates a large column ID list for bulk testing
	 */
	public static List<Integer> createLargeColumnIdList() {
		return Arrays.asList(COLUMN_ID_1, COLUMN_ID_2, COLUMN_ID_3, COLUMN_ID_4, COLUMN_ID_5);
	}

	/**
	 * Creates column IDs that don't exist in database
	 */
	public static List<Integer> createNonExistentColumnIdList() {
		return Arrays.asList(999, 998, 997);
	}

	/**
	 * Creates mixed existing and non-existing column IDs
	 */
	public static List<Integer> createMixedColumnIdList() {
		return Arrays.asList(COLUMN_ID_1, 999, COLUMN_ID_2);
	}

	// ==================== Factory Methods for ExtraFieldDefinitionDto
	// ====================

	/**
	 * Creates a text field definition
	 */
	public static ExtraFieldDefinitionDto createTextFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_1, FIELD_NAME_1, FIELD_TYPE_TEXT, TEST_ENTITY_TYPE_ID,
				TEST_ACCOUNT_ID);
	}

	/**
	 * Creates a date field definition
	 */
	public static ExtraFieldDefinitionDto createDateFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_2, FIELD_NAME_2, FIELD_TYPE_DATE, TEST_ENTITY_TYPE_ID,
				TEST_ACCOUNT_ID);
	}

	/**
	 * Creates a number field definition
	 */
	public static ExtraFieldDefinitionDto createNumberFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_3, FIELD_NAME_3, FIELD_TYPE_NUMBER, TEST_ENTITY_TYPE_ID,
				TEST_ACCOUNT_ID);
	}

	/**
	 * Creates a boolean field definition
	 */
	public static ExtraFieldDefinitionDto createBooleanFieldDefinition() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_4, FIELD_NAME_4, FIELD_TYPE_BOOLEAN, TEST_ENTITY_TYPE_ID,
				TEST_ACCOUNT_ID);
	}

	/**
	 * Creates field definition for different account
	 */
	public static ExtraFieldDefinitionDto createFieldDefinitionForDifferentAccount() {
		return new ExtraFieldDefinitionDto(COLUMN_ID_5, FIELD_NAME_5, FIELD_TYPE_TEXT, TEST_ENTITY_TYPE_ID,
				TEST_ACCOUNT_ID_2);
	}

	// ==================== Factory Methods for JOOQ Records ====================

	// Note: JOOQ Record mocking is handled in the test class directly
	// These methods provide the expected data structure for assertions

	// ==================== Factory Methods for Column ID Results ====================

	/**
	 * Creates list of existing column IDs
	 */
	public static List<Integer> createExistingColumnIds() {
		return Arrays.asList(COLUMN_ID_1, COLUMN_ID_2, COLUMN_ID_3);
	}

	/**
	 * Creates partial list of existing column IDs
	 */
	public static List<Integer> createPartialExistingColumnIds() {
		return Arrays.asList(COLUMN_ID_1, COLUMN_ID_3);
	}

	/**
	 * Creates empty list of existing column IDs
	 */
	public static List<Integer> createEmptyExistingColumnIds() {
		return new ArrayList<>();
	}

	/**
	 * Creates single existing column ID
	 */
	public static List<Integer> createSingleExistingColumnId() {
		return List.of(COLUMN_ID_1);
	}

	// ==================== Expected Result Factory Methods ====================

	/**
	 * Creates expected map for getExtraFieldDefinitions with multiple fields
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createExpectedFieldDefinitionsMap() {
		return Map.of(COLUMN_ID_1, createTextFieldDefinition(), COLUMN_ID_2, createDateFieldDefinition(), COLUMN_ID_3,
				createNumberFieldDefinition());
	}

	/**
	 * Creates expected map for getExtraFieldDefinitions with single field
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createExpectedSingleFieldDefinitionMap() {
		return Map.of(COLUMN_ID_1, createTextFieldDefinition());
	}

	/**
	 * Creates expected empty map for getExtraFieldDefinitions
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createExpectedEmptyFieldDefinitionsMap() {
		return Map.of();
	}

	/**
	 * Creates expected map for checkExtraFieldsExist with all existing
	 */
	public static Map<Integer, Boolean> createExpectedAllExistingFieldsMap() {
		return Map.of(COLUMN_ID_1, true, COLUMN_ID_2, true, COLUMN_ID_3, true);
	}

	/**
	 * Creates expected map for checkExtraFieldsExist with partial existing
	 */
	public static Map<Integer, Boolean> createExpectedPartialExistingFieldsMap() {
		return Map.of(COLUMN_ID_1, true, 999, false, COLUMN_ID_2, true);
	}

	/**
	 * Creates expected map for checkExtraFieldsExist with none existing
	 */
	public static Map<Integer, Boolean> createExpectedNoneExistingFieldsMap() {
		return Map.of(999, false, 998, false, 997, false);
	}

	/**
	 * Creates expected map for checkExtraFieldsExist with single existing
	 */
	public static Map<Integer, Boolean> createExpectedSingleExistingFieldMap() {
		return Map.of(COLUMN_ID_1, true);
	}

	/**
	 * Creates expected empty map for checkExtraFieldsExist
	 */
	public static Map<Integer, Boolean> createExpectedEmptyExistingFieldsMap() {
		return Map.of();
	}

	// ==================== Getter Methods ====================

	/**
	 * Get test account ID
	 */
	public static Integer getTestAccountId() {
		return TEST_ACCOUNT_ID;
	}

	/**
	 * Get test entity type
	 */
	public static EntityType getTestEntityType() {
		return TEST_ENTITY_TYPE;
	}

	/**
	 * Get test entity type ID
	 */
	public static Integer getTestEntityTypeId() {
		return TEST_ENTITY_TYPE_ID;
	}

}
