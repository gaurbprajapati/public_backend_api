package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;
import io.recruitcrm.microservice.timesheet.repositories.extra_fields.IExtraFieldsRepository;
import io.recruitcrm.microservice.timesheet.testdata.CustomColumnTypeServiceTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

/**
 * Unit test cases for CustomColumnTypeService following mandatory rule patterns. Tests
 * all public methods with 100% branch coverage using factory-based test data and
 * BDD-style assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomColumnTypeService Tests")
class CustomColumnTypeServiceTests {

	@Mock
	private IExtraFieldsRepository extraFieldsRepository;

	@Mock
	private AuthHolder authHolder;

	@InjectMocks
	private CustomColumnTypeService customColumnTypeService;

	@BeforeEach
	void setUp() {
		lenient().when(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
			.thenReturn(CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	// ==================== getFieldTypes() Tests ====================

	@Test
	@DisplayName("Get field types with cached data should return cached results without repository call")
	void testGetFieldTypesWithCachedDataReturnsCachedResults() {
		// Given
		List<Integer> columnIds = CustomColumnTypeServiceTestDataFactory.createColumnIdList();
		Map<Integer, ExtraFieldDefinitionDto> extraFields = CustomColumnTypeServiceTestDataFactory
			.createExtraFieldDefinitionsMap();

		given(this.extraFieldsRepository.getExtraFieldDefinitions(columnIds, EntityType.CANDIDATE,
				CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);

		// When - First call to populate cache
		Map<Integer, String> firstResult = this.customColumnTypeService.getFieldTypes(columnIds);

		// When - Second call should use cache
		Map<Integer, String> secondResult = this.customColumnTypeService.getFieldTypes(columnIds);

		// Then
		Map<Integer, String> expectedResult = CustomColumnTypeServiceTestDataFactory.createExpectedFieldTypesMap();
		assertThat(firstResult).isEqualTo(expectedResult);
		assertThat(secondResult).isEqualTo(expectedResult);

		// Verify repository was called only once (first time)
		then(this.extraFieldsRepository).should()
			.getExtraFieldDefinitions(columnIds, EntityType.CANDIDATE,
					CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Get field types with missing fields should fetch from repository and default to text")
	void testGetFieldTypesWithMissingFieldsFetchesFromRepositoryAndDefaultsToText() {
		// Given
		List<Integer> columnIds = CustomColumnTypeServiceTestDataFactory.createColumnIdListWithMissing();
		Map<Integer, ExtraFieldDefinitionDto> extraFields = CustomColumnTypeServiceTestDataFactory
			.createExtraFieldDefinitionsMapWithMissing();

		given(this.extraFieldsRepository.getExtraFieldDefinitions(columnIds, EntityType.CANDIDATE,
				CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);

		// When
		Map<Integer, String> result = this.customColumnTypeService.getFieldTypes(columnIds);

		// Then
		assertThat(result).hasSize(2)
			.containsEntry(CustomColumnTypeServiceTestDataFactory.COLUMN_ID_1,
					CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT)
			.containsEntry(CustomColumnTypeServiceTestDataFactory.COLUMN_ID_15,
					CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT); // Default
		// for
		// missing

		then(this.extraFieldsRepository).should()
			.getExtraFieldDefinitions(columnIds, EntityType.CANDIDATE,
					CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Get field types with empty list should return empty map without repository call")
	void testGetFieldTypesWithEmptyListReturnsEmptyMapWithoutRepositoryCall() {
		// Given
		List<Integer> emptyColumnIds = CustomColumnTypeServiceTestDataFactory.createEmptyColumnIdList();

		// When
		Map<Integer, String> result = this.customColumnTypeService.getFieldTypes(emptyColumnIds);

		// Then
		assertThat(result).isEmpty();

		// Verify no repository call was made
		then(this.extraFieldsRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field types with no missing fields should not call repository")
	void testGetFieldTypesWithNoMissingFieldsDoesNotCallRepository() {
		// Given
		List<Integer> columnIds = CustomColumnTypeServiceTestDataFactory.createSingleColumnIdList();
		Map<Integer, ExtraFieldDefinitionDto> extraFields = Map.of(CustomColumnTypeServiceTestDataFactory.COLUMN_ID_1,
				CustomColumnTypeServiceTestDataFactory.createTextFieldDefinition());

		// First call to populate cache
		given(this.extraFieldsRepository.getExtraFieldDefinitions(columnIds, EntityType.CANDIDATE,
				CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);
		this.customColumnTypeService.getFieldTypes(columnIds);

		// When - Second call with same column IDs
		Map<Integer, String> result = this.customColumnTypeService.getFieldTypes(columnIds);

		// Then
		assertThat(result).hasSize(1)
			.containsEntry(CustomColumnTypeServiceTestDataFactory.COLUMN_ID_1,
					CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT);

		// Verify repository was called only once (during first call)
		then(this.extraFieldsRepository).should()
			.getExtraFieldDefinitions(columnIds, EntityType.CANDIDATE,
					CustomColumnTypeServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	// ==================== convertValue() Tests ====================

	@Test
	@DisplayName("Convert value with null input should return empty string")
	void testConvertValueWithNullInputReturnsEmptyString() {
		// Given
		Object nullValue = CustomColumnTypeServiceTestDataFactory.createNullValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT;

		// When
		String result = this.customColumnTypeService.convertValue(nullValue, fieldType);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Convert value with empty string should return empty string")
	void testConvertValueWithEmptyStringReturnsEmptyString() {
		// Given
		String emptyValue = CustomColumnTypeServiceTestDataFactory.createEmptyStringValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT;

		// When
		String result = this.customColumnTypeService.convertValue(emptyValue, fieldType);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Convert value with whitespace string should return empty string")
	void testConvertValueWithWhitespaceStringReturnsEmptyString() {
		// Given
		String whitespaceValue = CustomColumnTypeServiceTestDataFactory.createWhitespaceStringValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT;

		// When
		String result = this.customColumnTypeService.convertValue(whitespaceValue, fieldType);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Convert value with date field type should format as MM/dd/yyyy")
	void testConvertValueWithDateFieldTypeFormatsAsMmDdYyyy() {
		// Given
		String timestamp = CustomColumnTypeServiceTestDataFactory.createValidUnixTimestamp();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_DATE;

		// When
		String result = this.customColumnTypeService.convertValue(timestamp, fieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedDateFormat());
	}

	@Test
	@DisplayName("Convert value with date field type and invalid timestamp should return original value")
	void testConvertValueWithDateFieldTypeAndInvalidTimestampReturnsOriginalValue() {
		// Given
		String invalidTimestamp = CustomColumnTypeServiceTestDataFactory.createInvalidTimestamp();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_DATE;

		// When
		String result = this.customColumnTypeService.convertValue(invalidTimestamp, fieldType);

		// Then
		assertThat(result).isEqualTo(invalidTimestamp);
	}

	@Test
	@DisplayName("Convert value with datetime field type should format as MM/dd/yyyy HH:mm:ss")
	void testConvertValueWithDatetimeFieldTypeFormatsAsMmDdYyyyHhMmSs() {
		// Given
		String timestamp = CustomColumnTypeServiceTestDataFactory.createValidUnixTimestamp();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_DATETIME;

		// When
		String result = this.customColumnTypeService.convertValue(timestamp, fieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedDateTimeFormat());
	}

	@Test
	@DisplayName("Convert value with date_time field type should format as MM/dd/yyyy HH:mm:ss")
	void testConvertValueWithDateTimeFieldTypeFormatsAsMmDdYyyyHhMmSs() {
		// Given
		String timestamp = CustomColumnTypeServiceTestDataFactory.createValidUnixTimestamp();
		String fieldType = "date_time";

		// When
		String result = this.customColumnTypeService.convertValue(timestamp, fieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedDateTimeFormat());
	}

	@Test
	@DisplayName("Convert value with timestamp field type should format as MM/dd/yyyy HH:mm:ss")
	void testConvertValueWithTimestampFieldTypeFormatsAsMmDdYyyyHhMmSs() {
		// Given
		String timestamp = CustomColumnTypeServiceTestDataFactory.createValidUnixTimestamp();
		String fieldType = "timestamp";

		// When
		String result = this.customColumnTypeService.convertValue(timestamp, fieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedDateTimeFormat());
	}

	@Test
	@DisplayName("Convert value with datetime field type and invalid timestamp should return original value")
	void testConvertValueWithDatetimeFieldTypeAndInvalidTimestampReturnsOriginalValue() {
		// Given
		String invalidTimestamp = CustomColumnTypeServiceTestDataFactory.createInvalidTimestamp();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_DATETIME;

		// When
		String result = this.customColumnTypeService.convertValue(invalidTimestamp, fieldType);

		// Then
		assertThat(result).isEqualTo(invalidTimestamp);
	}

	@Test
	@DisplayName("Convert value with checkbox field type and true value should return true")
	void testConvertValueWithCheckboxFieldTypeAndTrueValueReturnsTrue() {
		// Given
		String checkboxValue = CustomColumnTypeServiceTestDataFactory.createCheckboxTrueValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_CHECKBOX;

		// When
		String result = this.customColumnTypeService.convertValue(checkboxValue, fieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedCheckboxTrue());
	}

	@Test
	@DisplayName("Convert value with checkbox field type and false value should return false")
	void testConvertValueWithCheckboxFieldTypeAndFalseValueReturnsFalse() {
		// Given
		String checkboxValue = CustomColumnTypeServiceTestDataFactory.createCheckboxFalseValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_CHECKBOX;

		// When
		String result = this.customColumnTypeService.convertValue(checkboxValue, fieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedCheckboxFalse());
	}

	@Test
	@DisplayName("Convert value with checkbox field type and invalid value should return original value")
	void testConvertValueWithCheckboxFieldTypeAndInvalidValueReturnsOriginalValue() {
		// Given
		String invalidCheckboxValue = CustomColumnTypeServiceTestDataFactory.createCheckboxInvalidValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_CHECKBOX;

		// When
		String result = this.customColumnTypeService.convertValue(invalidCheckboxValue, fieldType);

		// Then
		assertThat(result).isEqualTo(invalidCheckboxValue);
	}

	@Test
	@DisplayName("Convert value with text field type should return original value")
	void testConvertValueWithTextFieldTypeReturnsOriginalValue() {
		// Given
		String textValue = CustomColumnTypeServiceTestDataFactory.createRegularTextValue();
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_TEXT;

		// When
		String result = this.customColumnTypeService.convertValue(textValue, fieldType);

		// Then
		assertThat(result).isEqualTo(textValue);
	}

	@Test
	@DisplayName("Convert value with number field type should return original value")
	void testConvertValueWithNumberFieldTypeReturnsOriginalValue() {
		// Given
		String numberValue = "123.45";
		String fieldType = CustomColumnTypeServiceTestDataFactory.FIELD_TYPE_NUMBER;

		// When
		String result = this.customColumnTypeService.convertValue(numberValue, fieldType);

		// Then
		assertThat(result).isEqualTo(numberValue);
	}

	@Test
	@DisplayName("Convert value with unknown field type should return original value")
	void testConvertValueWithUnknownFieldTypeReturnsOriginalValue() {
		// Given
		String value = CustomColumnTypeServiceTestDataFactory.createRegularTextValue();
		String unknownFieldType = "unknown_type";

		// When
		String result = this.customColumnTypeService.convertValue(value, unknownFieldType);

		// Then
		assertThat(result).isEqualTo(value);
	}

	@Test
	@DisplayName("Convert value with uppercase field type should handle case insensitivity")
	void testConvertValueWithUppercaseFieldTypeHandlesCaseInsensitivity() {
		// Given
		String checkboxValue = CustomColumnTypeServiceTestDataFactory.createCheckboxTrueValue();
		String uppercaseFieldType = "CHECKBOX";

		// When
		String result = this.customColumnTypeService.convertValue(checkboxValue, uppercaseFieldType);

		// Then
		assertThat(result).isEqualTo(CustomColumnTypeServiceTestDataFactory.getExpectedCheckboxTrue());
	}

}
