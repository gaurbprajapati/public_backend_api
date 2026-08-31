package io.recruitcrm.microservice.timesheet.dto.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.ExportFieldConfigTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Unit test cases for ExportFieldConfig following mandatory rule patterns. Tests all
 * public methods with 100% branch coverage using factory-based test data and BDD-style
 * assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExportFieldConfig Tests")
class ExportFieldConfigTests {

	// ==================== getAllFields() Tests ====================

	@Test
	@DisplayName("Get all fields with complete config should return all field definitions")
	void testGetAllFieldsWithCompleteConfigReturnsAllFieldDefinitions() {
		// Given
		ExportFieldConfig config = ExportFieldConfigTestDataFactory.createCompleteExportFieldConfig();

		// When
		List<FieldDefinitionJson> result = config.getAllFields();

		// Then
		assertThat(result).hasSize(ExportFieldConfigTestDataFactory.getExpectedCompleteConfigFieldCount())
			.isEqualTo(ExportFieldConfigTestDataFactory.createExpectedAllFieldsList());
	}

	@Test
	@DisplayName("Get all fields with empty config should return empty list")
	void testGetAllFieldsWithEmptyConfigReturnsEmptyList() {
		// Given
		ExportFieldConfig config = ExportFieldConfigTestDataFactory.createEmptyExportFieldConfig();

		// When
		List<FieldDefinitionJson> result = config.getAllFields();

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get all fields with config having empty lists should return empty list")
	void testGetAllFieldsWithConfigHavingEmptyListsReturnsEmptyList() {
		// Given
		ExportFieldConfig config = ExportFieldConfigTestDataFactory.createConfigWithEmptyLists();

		// When
		List<FieldDefinitionJson> result = config.getAllFields();

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get all fields with mixed config should return only non-null field definitions")
	void testGetAllFieldsWithMixedConfigReturnsOnlyNonNullFieldDefinitions() {
		// Given
		ExportFieldConfig config = ExportFieldConfigTestDataFactory.createMixedConfig();

		// When
		List<FieldDefinitionJson> result = config.getAllFields();

		// Then
		assertThat(result).hasSize(ExportFieldConfigTestDataFactory.getExpectedMixedConfigFieldCount())
			.isEqualTo(ExportFieldConfigTestDataFactory.createExpectedMixedAllFieldsList());
	}

	@Test
	@DisplayName("Get all fields with timesheet only config should return only timesheet fields")
	void testGetAllFieldsWithTimesheetOnlyConfigReturnsOnlyTimesheetFields() {
		// Given
		ExportFieldConfig config = ExportFieldConfigTestDataFactory.createTimesheetOnlyConfig();

		// When
		List<FieldDefinitionJson> result = config.getAllFields();

		// Then
		assertThat(result).hasSize(1).isEqualTo(ExportFieldConfigTestDataFactory.createTimesheetFieldsList());
	}

	// ==================== Constructor and Getter/Setter Tests ====================

	@Test
	@DisplayName("Default constructor should create config with null fields")
	void testDefaultConstructorCreatesConfigWithNullFields() {
		// Given & When
		ExportFieldConfig config = new ExportFieldConfig();

		// Then
		assertThat(config.getTimesheetFields()).isNull();
		assertThat(config.getCandidateFields()).isNull();
		assertThat(config.getJobFields()).isNull();
		assertThat(config.getCompanyFields()).isNull();
		assertThat(config.getApprovalFields()).isNull();
	}

	@Test
	@DisplayName("All args constructor should create config with provided fields")
	void testAllArgsConstructorCreatesConfigWithProvidedFields() {
		// Given
		List<FieldDefinitionJson> timesheetFields = ExportFieldConfigTestDataFactory.createTimesheetFieldsList();
		List<FieldDefinitionJson> candidateFields = ExportFieldConfigTestDataFactory.createCandidateFieldsList();
		List<FieldDefinitionJson> jobFields = ExportFieldConfigTestDataFactory.createJobFieldsList();
		List<FieldDefinitionJson> companyFields = ExportFieldConfigTestDataFactory.createCompanyFieldsList();
		List<FieldDefinitionJson> approvalFields = ExportFieldConfigTestDataFactory.createApprovalFieldsList();

		// When
		ExportFieldConfig config = new ExportFieldConfig(timesheetFields, candidateFields, jobFields, companyFields,
				approvalFields);

		// Then
		assertThat(config.getTimesheetFields()).isEqualTo(timesheetFields);
		assertThat(config.getCandidateFields()).isEqualTo(candidateFields);
		assertThat(config.getJobFields()).isEqualTo(jobFields);
		assertThat(config.getCompanyFields()).isEqualTo(companyFields);
		assertThat(config.getApprovalFields()).isEqualTo(approvalFields);
	}

	@Test
	@DisplayName("Setters should update field values correctly")
	void testSettersUpdateFieldValuesCorrectly() {
		// Given
		ExportFieldConfig config = new ExportFieldConfig();
		List<FieldDefinitionJson> timesheetFields = ExportFieldConfigTestDataFactory.createTimesheetFieldsList();
		List<FieldDefinitionJson> candidateFields = ExportFieldConfigTestDataFactory.createCandidateFieldsList();

		// When
		config.setTimesheetFields(timesheetFields);
		config.setCandidateFields(candidateFields);

		// Then
		assertThat(config.getTimesheetFields()).isEqualTo(timesheetFields);
		assertThat(config.getCandidateFields()).isEqualTo(candidateFields);
		assertThat(config.getJobFields()).isNull();
		assertThat(config.getCompanyFields()).isNull();
		assertThat(config.getApprovalFields()).isNull();
	}

}
