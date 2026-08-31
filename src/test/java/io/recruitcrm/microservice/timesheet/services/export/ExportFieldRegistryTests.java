package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;
import io.recruitcrm.microservice.timesheet.repositories.extra_fields.IExtraFieldsRepository;
import io.recruitcrm.microservice.timesheet.testdata.ExportFieldRegistryTestDataFactory;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unit test cases for ExportFieldRegistry following mandatory rule patterns. Tests all
 * public methods with 100% branch coverage using factory-based test data and BDD-style
 * assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExportFieldRegistry Tests")
class ExportFieldRegistryTests {

	@Mock
	private JooqFieldExpressionParser expressionParser;

	@Mock
	private AdvancedJooqExpressionParser advancedParser;

	@Mock
	private IExtraFieldsRepository extraFieldsRepository;

	@Mock
	private AuthHolder authHolder;

	@InjectMocks
	private ExportFieldRegistry exportFieldRegistry;

	@BeforeEach
	void setUp() {
		// No common setup needed - each test will configure its own mocks
	}

	// ==================== getFieldDefinitions() Tests ====================

	@Test
	@DisplayName("Get field definitions with valid field names should return field definitions")
	void testGetFieldDefinitionsWithValidFieldNamesReturnsFieldDefinitions() {
		// Given
		List<String> fieldNames = ExportFieldRegistryTestDataFactory.createValidFieldNamesList();
		@SuppressWarnings("unchecked")
		Field<Object> mockTimesheetField = (Field<Object>) DSL.field("TS.ID")
			.as(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCandidateField = (Field<Object>) DSL.field("C.FIRSTNAME")
			.as(ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockDealField = (Field<Object>) DSL.field("D.DEALNAME")
			.as(ExportFieldRegistryTestDataFactory.DEAL_NAME_FIELD);

		doReturn(mockTimesheetField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION,
					ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		doReturn(mockCandidateField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_EXPRESSION,
					ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_FIELD);
		doReturn(mockDealField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.DEAL_NAME_EXPRESSION,
					ExportFieldRegistryTestDataFactory.DEAL_NAME_FIELD);

		// When
		List<ExportFieldDefinition> result = this.exportFieldRegistry.getFieldDefinitions(fieldNames);

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		assertThat(result.get(1).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_FIELD);
		assertThat(result.get(2).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.DEAL_NAME_FIELD);

		then(this.expressionParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION,
					ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		then(this.expressionParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_EXPRESSION,
					ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_FIELD);
		then(this.expressionParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.DEAL_NAME_EXPRESSION,
					ExportFieldRegistryTestDataFactory.DEAL_NAME_FIELD);
	}

	@Test
	@DisplayName("Get field definitions with custom column fields should return custom field definitions")
	void testGetFieldDefinitionsWithCustomColumnFieldsReturnsCustomFieldDefinitions() {
		// Given
		List<String> fieldNames = ExportFieldRegistryTestDataFactory.createFieldNamesWithCustomColumns();
		@SuppressWarnings("unchecked")
		Field<Object> mockTimesheetField = (Field<Object>) DSL.field("TS.ID")
			.as(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCustomField1 = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN1")
			.as(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCustomField10 = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN10")
			.as(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD);

		Map<Integer, ExtraFieldDefinitionDto> extraFields = ExportFieldRegistryTestDataFactory
			.createExtraFieldDefinitionsMap();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID);

		doReturn(mockTimesheetField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION,
					ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		doReturn(mockCustomField1).when(this.advancedParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		doReturn(mockCustomField10).when(this.advancedParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD);
		given(this.extraFieldsRepository.getExtraFieldDefinitions(List.of(1), EntityType.CANDIDATE,
				ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);
		given(this.extraFieldsRepository.getExtraFieldDefinitions(List.of(10), EntityType.CANDIDATE,
				ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(ExportFieldRegistryTestDataFactory.createEmptyExtraFieldDefinitionsMap());

		// When
		List<ExportFieldDefinition> result = this.exportFieldRegistry.getFieldDefinitions(fieldNames);

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		assertThat(result.get(1).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		assertThat(result.get(1).getDisplayName())
			.isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_DISPLAY);
		assertThat(result.get(2).getFrontendName())
			.isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD);
		assertThat(result.get(2).getDisplayName())
			.isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_DISPLAY);

		then(this.advancedParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		then(this.advancedParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD);
	}

	@Test
	@DisplayName("Get field definitions with empty list should return empty list")
	void testGetFieldDefinitionsWithEmptyListReturnsEmptyList() {
		// Given
		List<String> emptyFieldNames = ExportFieldRegistryTestDataFactory.createEmptyFieldNamesList();

		// When
		List<ExportFieldDefinition> result = this.exportFieldRegistry.getFieldDefinitions(emptyFieldNames);

		// Then
		assertThat(result).isEmpty();

		// Verify no parser interactions
		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field definitions with invalid field names should filter out null definitions")
	void testGetFieldDefinitionsWithInvalidFieldNamesFiltersOutNullDefinitions() {
		// Given
		List<String> mixedFieldNames = ExportFieldRegistryTestDataFactory.createMixedFieldNamesList();
		@SuppressWarnings("unchecked")
		Field<Object> mockTimesheetField = (Field<Object>) DSL.field("TS.ID")
			.as(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCustomField1 = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN1")
			.as(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);

		Map<Integer, ExtraFieldDefinitionDto> extraFields = ExportFieldRegistryTestDataFactory
			.createExtraFieldDefinitionsMap();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID);

		doReturn(mockTimesheetField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION,
					ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		doReturn(mockCustomField1).when(this.advancedParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		given(this.extraFieldsRepository.getExtraFieldDefinitions(List.of(1), EntityType.CANDIDATE,
				ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);

		// When
		List<ExportFieldDefinition> result = this.exportFieldRegistry.getFieldDefinitions(mixedFieldNames);

		// Then - Should only return valid field definitions (timesheet and custom column
		// 1)
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		assertThat(result.get(1).getFrontendName()).isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
	}

	// ==================== getFieldDefinition() Tests ====================

	@Test
	@DisplayName("Get field definition with valid field name should return field definition")
	void testGetFieldDefinitionWithValidFieldNameReturnsFieldDefinition() {
		// Given
		String fieldName = ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("TS.ID").as(fieldName);

		doReturn(mockField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(fieldName);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getFrontendName()).isEqualTo(fieldName);
		assertThat(result.getDisplayName()).isEqualTo(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_DISPLAY);

		then(this.expressionParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);
	}

	@Test
	@DisplayName("Get field definition with last note created on should return candidate last activity definition")
	void testGetFieldDefinitionWithLastNoteCreatedOnReturnsCandidateLastActivityDefinition() {
		// Given
		String fieldName = ExportFieldRegistryTestDataFactory.LAST_NOTE_CREATED_ON_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("cla.note_created_on").as(fieldName);

		doReturn(mockField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.LAST_NOTE_CREATED_ON_EXPRESSION, fieldName);

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(fieldName);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getFrontendName()).isEqualTo(fieldName);
		assertThat(result.getDisplayName()).isEqualTo(ExportFieldRegistryTestDataFactory.LAST_NOTE_CREATED_ON_DISPLAY);
		assertThat(result.getJavaType()).isEqualTo(Integer.class);
		assertThat(result.getRequiredEntities()).containsExactlyInAnyOrder("ts", "tss", "tsa", "c", "cla");

		then(this.expressionParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.LAST_NOTE_CREATED_ON_EXPRESSION, fieldName);
	}

	@Test
	@DisplayName("Get field definition with invalid field name should return null")
	void testGetFieldDefinitionWithInvalidFieldNameReturnsNull() {
		// Given
		String invalidFieldName = ExportFieldRegistryTestDataFactory.INVALID_FIELD;

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(invalidFieldName);

		// Then
		assertThat(result).isNull();

		// Verify no parser interactions
		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field definition with disabled field should return null")
	void testGetFieldDefinitionWithDisabledFieldReturnsNull() {
		// Given
		String disabledFieldName = "disabledField";
		String disabledFieldJson = """
				{
				  "timesheet_fields": [
				    {
				      "frontendName": "disabledField",
				      "jooqExpression": "DISABLED.FIELD",
				      "displayName": "Disabled Field",
				      "javaType": "String",
				      "nullable": true,
				      "enabled": false,
				      "requiredEntities": ["ts"]
				    }
				  ]
				}
				""";

		try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
				(mock, context) -> given(mock.getInputStream())
					.willReturn(new ByteArrayInputStream(disabledFieldJson.getBytes(StandardCharsets.UTF_8))))) {

			// When
			ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(disabledFieldName);

			// Then - field exists in config but is disabled, so it must not be built
			assertThat(result).isNull();
			assertThat(mocked.constructed()).hasSize(1);
		}

		// Verify no parser interactions
		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field definition twice with same name should reuse cached definition")
	void testGetFieldDefinitionWithCachedFieldReusesCachedDefinition() {
		// Given
		String fieldName = ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("TS.ID").as(fieldName);

		doReturn(mockField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);

		// When - resolved twice
		ExportFieldDefinition firstCall = this.exportFieldRegistry.getFieldDefinition(fieldName);
		ExportFieldDefinition secondCall = this.exportFieldRegistry.getFieldDefinition(fieldName);

		// Then - second call must be served from the cache, not re-parsed
		assertThat(firstCall).isSameAs(secondCall);
		then(this.expressionParser).should(times(1))
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);
	}

	@Test
	@DisplayName("Get field definition when JSON config fails to load should fall back to empty config")
	void testGetFieldDefinitionWhenJsonConfigLoadFailsReturnsNull() {
		// Given
		try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
				(mock, context) -> given(mock.getInputStream()).willThrow(new IOException("resource unavailable")))) {

			// When
			ExportFieldDefinition result = this.exportFieldRegistry
				.getFieldDefinition(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);

			// Then - IOException is caught, fallback empty config is used, field can't be
			// found
			assertThat(result).isNull();
			assertThat(mocked.constructed()).hasSize(1);
		}

		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field definitions with BigDecimal, Boolean, Date and unknown java types should map correctly")
	void testGetFieldDefinitionsWithAllSupportedJavaTypesMapsCorrectly() {
		// Given
		String multiTypeFieldsJson = """
				{
				  "timesheet_fields": [
				    {
				      "frontendName": "rateAmount",
				      "jooqExpression": "TS.RATE_AMOUNT",
				      "displayName": "Rate Amount",
				      "javaType": "BigDecimal",
				      "nullable": true,
				      "enabled": true,
				      "requiredEntities": ["ts"]
				    },
				    {
				      "frontendName": "isActiveFlag",
				      "jooqExpression": "TS.IS_ACTIVE",
				      "displayName": "Is Active",
				      "javaType": "Boolean",
				      "nullable": true,
				      "enabled": true,
				      "requiredEntities": ["ts"]
				    },
				    {
				      "frontendName": "startDateField",
				      "jooqExpression": "TS.START_DATE",
				      "displayName": "Start Date",
				      "javaType": "Date",
				      "nullable": true,
				      "enabled": true,
				      "requiredEntities": ["ts"]
				    },
				    {
				      "frontendName": "legacyField",
				      "jooqExpression": "TS.LEGACY",
				      "displayName": "Legacy Field",
				      "javaType": "UnknownType",
				      "nullable": true,
				      "enabled": true,
				      "requiredEntities": ["ts"]
				    }
				  ]
				}
				""";

		@SuppressWarnings("unchecked")
		Field<Object> mockRateField = (Field<Object>) DSL.field("TS.RATE_AMOUNT").as("rateAmount");
		@SuppressWarnings("unchecked")
		Field<Object> mockActiveField = (Field<Object>) DSL.field("TS.IS_ACTIVE").as("isActiveFlag");
		@SuppressWarnings("unchecked")
		Field<Object> mockStartDateField = (Field<Object>) DSL.field("TS.START_DATE").as("startDateField");
		@SuppressWarnings("unchecked")
		Field<Object> mockLegacyField = (Field<Object>) DSL.field("TS.LEGACY").as("legacyField");

		doReturn(mockRateField).when(this.expressionParser).parseExpression("TS.RATE_AMOUNT", "rateAmount");
		doReturn(mockActiveField).when(this.expressionParser).parseExpression("TS.IS_ACTIVE", "isActiveFlag");
		doReturn(mockStartDateField).when(this.expressionParser).parseExpression("TS.START_DATE", "startDateField");
		doReturn(mockLegacyField).when(this.expressionParser).parseExpression("TS.LEGACY", "legacyField");

		try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
				(mock, context) -> given(mock.getInputStream())
					.willReturn(new ByteArrayInputStream(multiTypeFieldsJson.getBytes(StandardCharsets.UTF_8))))) {

			// When
			ExportFieldDefinition rateAmount = this.exportFieldRegistry.getFieldDefinition("rateAmount");
			ExportFieldDefinition isActiveFlag = this.exportFieldRegistry.getFieldDefinition("isActiveFlag");
			ExportFieldDefinition startDateField = this.exportFieldRegistry.getFieldDefinition("startDateField");
			ExportFieldDefinition legacyField = this.exportFieldRegistry.getFieldDefinition("legacyField");

			// Then
			assertThat(rateAmount.getJavaType()).isEqualTo(BigDecimal.class);
			assertThat(isActiveFlag.getJavaType()).isEqualTo(Boolean.class);
			assertThat(startDateField.getJavaType()).isEqualTo(java.util.Date.class);
			assertThat(legacyField.getJavaType()).isEqualTo(Object.class);
			assertThat(mocked.constructed()).hasSize(1);
		}
	}

	@Test
	@DisplayName("Get field definition with custom column below minimum range should return null")
	void testGetFieldDefinitionWithCustomColumnBelowMinimumRangeReturnsNull() {
		// Given
		String belowRangeCustomColumn = "custcolumn0";

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(belowRangeCustomColumn);

		// Then
		assertThat(result).isNull();

		// Verify no parser interactions
		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field definition with non-numeric custom column suffix should return null")
	void testGetFieldDefinitionWithNonNumericCustomColumnSuffixReturnsNull() {
		// Given
		String nonNumericCustomColumn = ExportFieldRegistryTestDataFactory.INVALID_CUSTOM_COLUMN_FIELD;

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(nonNumericCustomColumn);

		// Then
		assertThat(result).isNull();

		// Verify no parser interactions
		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Get field definition with custom column should return custom field definition")
	void testGetFieldDefinitionWithCustomColumnReturnsCustomFieldDefinition() {
		// Given
		String customColumnName = ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN1").as(customColumnName);
		Map<Integer, ExtraFieldDefinitionDto> extraFields = ExportFieldRegistryTestDataFactory
			.createExtraFieldDefinitionsMap();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID);

		doReturn(mockField).when(this.advancedParser).parseExpression(customColumnName, customColumnName);
		given(this.extraFieldsRepository.getExtraFieldDefinitions(List.of(1), EntityType.CANDIDATE,
				ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(customColumnName);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getFrontendName()).isEqualTo(customColumnName);
		assertThat(result.getDisplayName()).isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_DISPLAY);
		assertThat(result.getRequiredEntities()).containsExactlyInAnyOrder("c", "ccf");

		then(this.advancedParser).should().parseExpression(customColumnName, customColumnName);
		then(this.extraFieldsRepository).should()
			.getExtraFieldDefinitions(List.of(1), EntityType.CANDIDATE,
					ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Get field definition with invalid custom column should return null")
	void testGetFieldDefinitionWithInvalidCustomColumnReturnsNull() {
		// Given
		String invalidCustomColumn = ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_151_FIELD;

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(invalidCustomColumn);

		// Then
		assertThat(result).isNull();

		// Verify no parser interactions
		then(this.expressionParser).shouldHaveNoInteractions();
		then(this.advancedParser).shouldHaveNoInteractions();
	}

	// ==================== validateFields() Tests ====================

	@Test
	@DisplayName("Validate fields with valid field names should not throw exception")
	void testValidateFieldsWithValidFieldNamesDoesNotThrowException() {
		// Given
		List<String> validFieldNames = ExportFieldRegistryTestDataFactory.createValidFieldNamesList();

		// When/Then - Should not throw any exception
		this.exportFieldRegistry.validateFields(validFieldNames);
	}

	@Test
	@DisplayName("Validate fields with valid custom columns should not throw exception")
	void testValidateFieldsWithValidCustomColumnsDoesNotThrowException() {
		// Given
		List<String> validCustomColumns = ExportFieldRegistryTestDataFactory.createValidCustomColumnFields();

		// When/Then - Should not throw any exception
		this.exportFieldRegistry.validateFields(validCustomColumns);
	}

	@Test
	@DisplayName("Validate fields with invalid field names should throw ValidationErrorException")
	void testValidateFieldsWithInvalidFieldNamesThrowsValidationErrorException() {
		// Given
		List<String> invalidFieldNames = ExportFieldRegistryTestDataFactory.createInvalidFieldNamesList();

		// When/Then
		assertThatThrownBy(() -> this.exportFieldRegistry.validateFields(invalidFieldNames))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Invalid export fields:");
	}

	@Test
	@DisplayName("Validate fields with invalid custom columns should throw ValidationErrorException")
	void testValidateFieldsWithInvalidCustomColumnsThrowsValidationErrorException() {
		// Given
		List<String> invalidCustomColumns = ExportFieldRegistryTestDataFactory.createInvalidCustomColumnFields();

		// When/Then
		assertThatThrownBy(() -> this.exportFieldRegistry.validateFields(invalidCustomColumns))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Invalid export fields:");
	}

	@Test
	@DisplayName("Validate fields with empty list should not throw exception")
	void testValidateFieldsWithEmptyListDoesNotThrowException() {
		// Given
		List<String> emptyFieldNames = ExportFieldRegistryTestDataFactory.createEmptyFieldNamesList();

		// When/Then - Should not throw any exception
		this.exportFieldRegistry.validateFields(emptyFieldNames);
	}

	// ==================== getRequiredEntities() Tests ====================

	@Test
	@DisplayName("Get required entities with valid field names should return required entities")
	void testGetRequiredEntitiesWithValidFieldNamesReturnsRequiredEntities() {
		// Given
		List<String> fieldNames = ExportFieldRegistryTestDataFactory.createValidFieldNamesList();
		@SuppressWarnings("unchecked")
		Field<Object> mockTimesheetField = (Field<Object>) DSL.field("TS.ID")
			.as(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCandidateField = (Field<Object>) DSL.field("C.FIRSTNAME")
			.as(ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockDealField = (Field<Object>) DSL.field("D.DEALNAME")
			.as(ExportFieldRegistryTestDataFactory.DEAL_NAME_FIELD);

		doReturn(mockTimesheetField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION,
					ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		doReturn(mockCandidateField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_EXPRESSION,
					ExportFieldRegistryTestDataFactory.CANDIDATE_NAME_FIELD);
		doReturn(mockDealField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.DEAL_NAME_EXPRESSION,
					ExportFieldRegistryTestDataFactory.DEAL_NAME_FIELD);

		// When
		Set<String> result = this.exportFieldRegistry.getRequiredEntities(fieldNames);

		// Then
		Set<String> expectedEntities = ExportFieldRegistryTestDataFactory.createExpectedRequiredEntities();
		assertThat(result).isEqualTo(expectedEntities);
	}

	@Test
	@DisplayName("Get required entities with custom columns should include custom column entities")
	void testGetRequiredEntitiesWithCustomColumnsIncludesCustomColumnEntities() {
		// Given
		List<String> fieldNames = ExportFieldRegistryTestDataFactory.createFieldNamesWithCustomColumns();
		@SuppressWarnings("unchecked")
		Field<Object> mockTimesheetField = (Field<Object>) DSL.field("TS.ID")
			.as(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCustomField1 = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN1")
			.as(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		@SuppressWarnings("unchecked")
		Field<Object> mockCustomField10 = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN10")
			.as(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD);

		doReturn(mockTimesheetField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION,
					ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD);
		doReturn(mockCustomField1).when(this.advancedParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD);
		doReturn(mockCustomField10).when(this.advancedParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD,
					ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_10_FIELD);
		given(this.extraFieldsRepository.getExtraFieldDefinitions(anyList(), any(EntityType.class), anyInt()))
			.willReturn(ExportFieldRegistryTestDataFactory.createExtraFieldDefinitionsMap())
			.willReturn(ExportFieldRegistryTestDataFactory.createEmptyExtraFieldDefinitionsMap());

		// When
		Set<String> result = this.exportFieldRegistry.getRequiredEntities(fieldNames);

		// Then
		Set<String> expectedEntities = ExportFieldRegistryTestDataFactory
			.createExpectedRequiredEntitiesWithCustomColumns();
		assertThat(result).isEqualTo(expectedEntities);
	}

	@Test
	@DisplayName("Get required entities with empty list should return empty set")
	void testGetRequiredEntitiesWithEmptyListReturnsEmptySet() {
		// Given
		List<String> emptyFieldNames = ExportFieldRegistryTestDataFactory.createEmptyFieldNamesList();

		// When
		Set<String> result = this.exportFieldRegistry.getRequiredEntities(emptyFieldNames);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get field definition called twice should return cached definition on second call")
	void testGetFieldDefinitionCalledTwiceReturnsCachedDefinition() {
		// Given
		String fieldName = ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("TS.ID").as(fieldName);

		doReturn(mockField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);

		// When - first call loads and caches, second call hits the cache branch
		ExportFieldDefinition firstResult = this.exportFieldRegistry.getFieldDefinition(fieldName);
		ExportFieldDefinition secondResult = this.exportFieldRegistry.getFieldDefinition(fieldName);

		// Then
		assertThat(firstResult).isNotNull();
		assertThat(secondResult).isSameAs(firstResult);

		// Expression should be parsed only once because the second call uses the cache
		then(this.expressionParser).should()
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);
	}

	@Test
	@DisplayName("Get field definition with integer typed field should parse Integer java type")
	void testGetFieldDefinitionWithIntegerFieldParsesIntegerJavaType() {
		// Given
		String fieldName = "timesheet";
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("TS.ID").as(fieldName);

		doReturn(mockField).when(this.expressionParser).parseExpression("TS.ID", fieldName);

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(fieldName);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getJavaType()).isEqualTo(Integer.class);
	}

	@Test
	@DisplayName("Get field definition with big decimal typed field should parse BigDecimal java type")
	void testGetFieldDefinitionWithBigDecimalFieldParsesBigDecimalJavaType() {
		// Given
		String fieldName = "currentsalary";
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("C.CURRENTSALARY").as(fieldName);

		doReturn(mockField).when(this.expressionParser).parseExpression("C.CURRENTSALARY", fieldName);

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(fieldName);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getJavaType()).isEqualTo(java.math.BigDecimal.class);
	}

	@Test
	@DisplayName("Get field definition with non numeric custom column should return null via number format exception")
	void testGetFieldDefinitionWithNonNumericCustomColumnReturnsNull() {
		// Given - "custcolumnabc" starts with custcolumn but parsing the index throws
		String invalidCustomColumn = ExportFieldRegistryTestDataFactory.INVALID_CUSTOM_COLUMN_FIELD;

		// When
		ExportFieldDefinition result = this.exportFieldRegistry.getFieldDefinition(invalidCustomColumn);

		// Then
		assertThat(result).isNull();

		// Advanced parser must not be reached because index parsing fails first
		then(this.advancedParser).shouldHaveNoInteractions();
		then(this.extraFieldsRepository).shouldHaveNoInteractions();
	}

	// ==================== getDisplayName() Tests ====================

	@Test
	@DisplayName("Get display name with valid field should return display name")
	void testGetDisplayNameWithValidFieldReturnsDisplayName() {
		// Given
		String fieldName = ExportFieldRegistryTestDataFactory.TIMESHEET_ID_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("TS.ID").as(fieldName);

		doReturn(mockField).when(this.expressionParser)
			.parseExpression(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_EXPRESSION, fieldName);

		// When
		String result = this.exportFieldRegistry.getDisplayName(fieldName);

		// Then
		assertThat(result).isEqualTo(ExportFieldRegistryTestDataFactory.TIMESHEET_ID_DISPLAY);
	}

	@Test
	@DisplayName("Get display name with invalid field should return field name")
	void testGetDisplayNameWithInvalidFieldReturnsFieldName() {
		// Given
		String invalidFieldName = ExportFieldRegistryTestDataFactory.INVALID_FIELD;

		// When
		String result = this.exportFieldRegistry.getDisplayName(invalidFieldName);

		// Then
		assertThat(result).isEqualTo(invalidFieldName);
	}

	@Test
	@DisplayName("Get display name with custom column should return custom display name")
	void testGetDisplayNameWithCustomColumnReturnsCustomDisplayName() {
		// Given
		String customColumnName = ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_FIELD;
		@SuppressWarnings("unchecked")
		Field<Object> mockField = (Field<Object>) DSL.field("CCF.CUSTOMCOLUMN1").as(customColumnName);
		Map<Integer, ExtraFieldDefinitionDto> extraFields = ExportFieldRegistryTestDataFactory
			.createExtraFieldDefinitionsMap();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID);

		doReturn(mockField).when(this.advancedParser).parseExpression(customColumnName, customColumnName);
		given(this.extraFieldsRepository.getExtraFieldDefinitions(List.of(1), EntityType.CANDIDATE,
				ExportFieldRegistryTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(extraFields);

		// When
		String result = this.exportFieldRegistry.getDisplayName(customColumnName);

		// Then
		assertThat(result).isEqualTo(ExportFieldRegistryTestDataFactory.CUSTOM_COLUMN_1_DISPLAY);
	}

}