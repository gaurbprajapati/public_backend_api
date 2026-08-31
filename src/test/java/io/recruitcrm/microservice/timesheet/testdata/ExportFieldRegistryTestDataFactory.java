package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldConfig;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.FieldDefinitionJson;
import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test data factory for ExportFieldRegistry unit tests. Provides factory methods for
 * creating test data following the mandatory rule patterns.
 */
public final class ExportFieldRegistryTestDataFactory {

	// ==================== Constants ====================

	/**
	 * Success message for field registry operations
	 */
	public static final String REGISTRY_SUCCESS = "Field registry operation successful";

	/**
	 * Error message for field registry operations
	 */
	public static final String REGISTRY_FAILED = "Field registry operation failed";

	/**
	 * Test account ID
	 */
	public static final Integer TEST_ACCOUNT_ID = 12345;

	/**
	 * Field name constants
	 */
	public static final String TIMESHEET_ID_FIELD = "timesheetId";

	public static final String CANDIDATE_NAME_FIELD = "candidatename";

	public static final String DEAL_NAME_FIELD = "dealName";

	public static final String CUSTOM_COLUMN_1_FIELD = "custcolumn1";

	public static final String CUSTOM_COLUMN_10_FIELD = "custcolumn10";

	public static final String CUSTOM_COLUMN_151_FIELD = "custcolumn151";

	public static final String INVALID_FIELD = "invalidField";

	public static final String INVALID_CUSTOM_COLUMN_FIELD = "custcolumnabc";

	public static final String LAST_NOTE_CREATED_ON_FIELD = "last_note_created_on";

	/**
	 * Display name constants
	 */
	public static final String TIMESHEET_ID_DISPLAY = "Timesheet ID";

	public static final String CANDIDATE_NAME_DISPLAY = "Candidate Name";

	public static final String DEAL_NAME_DISPLAY = "Deal Name";

	public static final String CUSTOM_COLUMN_1_DISPLAY = "First Name";

	public static final String CUSTOM_COLUMN_10_DISPLAY = "Custom Column 10";

	public static final String LAST_NOTE_CREATED_ON_DISPLAY = "Last Note Added";

	/**
	 * JOOQ expression constants
	 */
	public static final String TIMESHEET_ID_EXPRESSION = "DSL.concat(DSL.val(\"TS-\"), C.SRNO, DSL.val(\"-\"), TS.ID)";

	public static final String CANDIDATE_NAME_EXPRESSION = "DSL.when(C.LASTNAME.isNull().or(C.LASTNAME.eq(\"\")), C.FIRSTNAME).otherwise(DSL.concat(C.FIRSTNAME, DSL.val(\" \"), C.LASTNAME))";

	public static final String DEAL_NAME_EXPRESSION = "DSL.field(\"deals.deal_name\", String.class)";

	public static final String LAST_NOTE_CREATED_ON_EXPRESSION = "\"DSL.function(\\\"FROM_UNIXTIME\\\", String.class, CLA.NOTE_CREATED_ON, DSL.val(\\\"%b %d %Y %H:%i:%s UTC\\\"))\", ";

	/**
	 * Java type constants
	 */
	public static final String INTEGER_TYPE = "Integer";

	public static final String STRING_TYPE = "String";

	public static final String BIGDECIMAL_TYPE = "BigDecimal";

	public static final String BOOLEAN_TYPE = "Boolean";

	public static final String DATE_TYPE = "Date";

	public static final String UNKNOWN_TYPE = "UnknownType";

	private ExportFieldRegistryTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Field Definition JSON Objects ====================

	/**
	 * Creates a timesheet ID field definition JSON
	 */
	public static FieldDefinitionJson createTimesheetIdFieldJson() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(TIMESHEET_ID_FIELD);
		field.setDisplayName(TIMESHEET_ID_DISPLAY);
		field.setJooqExpression(TIMESHEET_ID_EXPRESSION);
		field.setJavaType(STRING_TYPE);
		field.setRequiredEntities(List.of("ts", "tss", "tsa", "c"));
		field.setNullable(false);
		field.setEnabled(true);
		return field;
	}

	/**
	 * Creates a candidate name field definition JSON
	 */
	public static FieldDefinitionJson createCandidateNameFieldJson() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(CANDIDATE_NAME_FIELD);
		field.setDisplayName(CANDIDATE_NAME_DISPLAY);
		field.setJooqExpression(CANDIDATE_NAME_EXPRESSION);
		field.setJavaType(STRING_TYPE);
		field.setRequiredEntities(List.of("ts", "tss", "tsa", "c"));
		field.setNullable(true);
		field.setEnabled(true);
		return field;
	}

	/**
	 * Creates a deal name field definition JSON
	 */
	public static FieldDefinitionJson createDealNameFieldJson() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(DEAL_NAME_FIELD);
		field.setDisplayName(DEAL_NAME_DISPLAY);
		field.setJooqExpression(DEAL_NAME_EXPRESSION);
		field.setJavaType(STRING_TYPE);
		field.setRequiredEntities(List.of("ts", "tss", "tsa", "c", "j", "deals"));
		field.setNullable(true);
		field.setEnabled(true);
		return field;
	}

	/**
	 * Creates a disabled field definition JSON
	 */
	public static FieldDefinitionJson createDisabledFieldJson() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName("disabledField");
		field.setDisplayName("Disabled Field");
		field.setJooqExpression("DISABLED.FIELD");
		field.setJavaType(STRING_TYPE);
		field.setRequiredEntities(List.of("disabled"));
		field.setNullable(true);
		field.setEnabled(false);
		return field;
	}

	// ==================== Export Field Config ====================

	/**
	 * Creates a complete export field config with multiple fields
	 */
	public static ExportFieldConfig createCompleteExportFieldConfig() {
		ExportFieldConfig config = new ExportFieldConfig();
		config.setTimesheetFields(List.of(createTimesheetIdFieldJson()));
		config.setCandidateFields(List.of(createCandidateNameFieldJson()));
		config.setJobFields(List.of(createDealNameFieldJson()));
		config.setCompanyFields(List.of(createDisabledFieldJson()));
		config.setApprovalFields(List.of());
		return config;
	}

	/**
	 * Creates an empty export field config
	 */
	public static ExportFieldConfig createEmptyExportFieldConfig() {
		ExportFieldConfig config = new ExportFieldConfig();
		config.setTimesheetFields(List.of());
		config.setCandidateFields(List.of());
		config.setJobFields(List.of());
		config.setCompanyFields(List.of());
		config.setApprovalFields(List.of());
		return config;
	}

	// ==================== Field Name Lists ====================

	/**
	 * Creates a list of valid field names
	 */
	public static List<String> createValidFieldNamesList() {
		return List.of(TIMESHEET_ID_FIELD, CANDIDATE_NAME_FIELD, DEAL_NAME_FIELD);
	}

	/**
	 * Creates a list of field names including custom columns
	 */
	public static List<String> createFieldNamesWithCustomColumns() {
		return List.of(TIMESHEET_ID_FIELD, CUSTOM_COLUMN_1_FIELD, CUSTOM_COLUMN_10_FIELD);
	}

	/**
	 * Creates a list of invalid field names
	 */
	public static List<String> createInvalidFieldNamesList() {
		return List.of(INVALID_FIELD, CUSTOM_COLUMN_151_FIELD, INVALID_CUSTOM_COLUMN_FIELD);
	}

	/**
	 * Creates a mixed list of valid and invalid field names
	 */
	public static List<String> createMixedFieldNamesList() {
		return List.of(TIMESHEET_ID_FIELD, INVALID_FIELD, CUSTOM_COLUMN_1_FIELD);
	}

	/**
	 * Creates an empty field names list
	 */
	public static List<String> createEmptyFieldNamesList() {
		return List.of();
	}

	// ==================== Export Field Definitions ====================

	/**
	 * Creates a timesheet ID export field definition
	 */
	public static ExportFieldDefinition createTimesheetIdFieldDefinition() {
		Field<?> jooqField = DSL.field("TS.ID").as(TIMESHEET_ID_FIELD);
		return ExportFieldDefinition.builder()
			.frontendName(TIMESHEET_ID_FIELD)
			.jooqField(jooqField)
			.displayName(TIMESHEET_ID_DISPLAY)
			.javaType(String.class)
			.requiredEntities(Set.of("ts", "tss", "tsa", "c"))
			.nullable(false)
			.enabled(true)
			.build();
	}

	/**
	 * Creates a candidate name export field definition
	 */
	public static ExportFieldDefinition createCandidateNameFieldDefinition() {
		Field<?> jooqField = DSL.field("C.FIRSTNAME").as(CANDIDATE_NAME_FIELD);
		return ExportFieldDefinition.builder()
			.frontendName(CANDIDATE_NAME_FIELD)
			.jooqField(jooqField)
			.displayName(CANDIDATE_NAME_DISPLAY)
			.javaType(String.class)
			.requiredEntities(Set.of("ts", "tss", "tsa", "c"))
			.nullable(true)
			.enabled(true)
			.build();
	}

	/**
	 * Creates a custom column export field definition
	 */
	public static ExportFieldDefinition createCustomColumnFieldDefinition() {
		Field<?> jooqField = DSL.field("CCF.CUSTOMCOLUMN1").as(CUSTOM_COLUMN_1_FIELD);
		return ExportFieldDefinition.builder()
			.frontendName(CUSTOM_COLUMN_1_FIELD)
			.jooqField(jooqField)
			.displayName(CUSTOM_COLUMN_1_DISPLAY)
			.javaType(String.class)
			.requiredEntities(Set.of("c", "ccf"))
			.nullable(true)
			.enabled(true)
			.build();
	}

	// ==================== Extra Field Definitions ====================

	/**
	 * Creates extra field definitions map for custom columns
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createExtraFieldDefinitionsMap() {
		Map<Integer, ExtraFieldDefinitionDto> map = new HashMap<>();
		map.put(1, new ExtraFieldDefinitionDto(1, CUSTOM_COLUMN_1_DISPLAY, "text", 5, TEST_ACCOUNT_ID));
		return map;
	}

	/**
	 * Creates empty extra field definitions map
	 */
	public static Map<Integer, ExtraFieldDefinitionDto> createEmptyExtraFieldDefinitionsMap() {
		return new HashMap<>();
	}

	// ==================== Required Entities ====================

	/**
	 * Creates expected required entities set
	 */
	public static Set<String> createExpectedRequiredEntities() {
		Set<String> entities = new HashSet<>();
		entities.add("ts");
		entities.add("tss");
		entities.add("tsa");
		entities.add("c");
		entities.add("j");
		entities.add("deals");
		return entities;
	}

	/**
	 * Creates expected required entities set for custom columns
	 */
	public static Set<String> createExpectedRequiredEntitiesWithCustomColumns() {
		Set<String> entities = new HashSet<>();
		entities.add("ts");
		entities.add("tss");
		entities.add("tsa");
		entities.add("c");
		entities.add("ccf");
		return entities;
	}

	// ==================== Java Type Test Data ====================

	/**
	 * Creates test data for all supported Java types
	 */
	public static Map<String, Class<?>> createJavaTypeTestData() {
		Map<String, Class<?>> typeMap = new HashMap<>();
		typeMap.put(STRING_TYPE, String.class);
		typeMap.put(INTEGER_TYPE, Integer.class);
		typeMap.put(BIGDECIMAL_TYPE, BigDecimal.class);
		typeMap.put(BOOLEAN_TYPE, Boolean.class);
		typeMap.put(DATE_TYPE, java.util.Date.class);
		typeMap.put(UNKNOWN_TYPE, Object.class);
		return typeMap;
	}

	// ==================== Custom Column Test Data ====================

	/**
	 * Creates valid custom column field names
	 */
	public static List<String> createValidCustomColumnFields() {
		return List.of("custcolumn1", "custcolumn50", "custcolumn150");
	}

	/**
	 * Creates invalid custom column field names
	 */
	public static List<String> createInvalidCustomColumnFields() {
		return List.of("custcolumn0", "custcolumn151", "custcolumnabc", "custcolumn-1");
	}

}
