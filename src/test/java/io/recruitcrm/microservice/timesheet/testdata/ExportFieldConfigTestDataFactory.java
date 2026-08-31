package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldConfig;
import io.recruitcrm.microservice.timesheet.dto.export.FieldDefinitionJson;

import java.util.ArrayList;
import java.util.List;

/**
 * Test data factory for ExportFieldConfig unit tests. Provides factory methods for
 * creating test data objects with realistic values for comprehensive test coverage.
 */
public final class ExportFieldConfigTestDataFactory {

	private ExportFieldConfigTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Test Constants ====================

	public static final String TIMESHEET_FIELD_NAME = "timesheet_id";

	public static final String TIMESHEET_FIELD_DISPLAY = "Timesheet ID";

	public static final String TIMESHEET_FIELD_EXPRESSION = "TS.ID";

	public static final String CANDIDATE_FIELD_NAME = "candidate_name";

	public static final String CANDIDATE_FIELD_DISPLAY = "Candidate Name";

	public static final String CANDIDATE_FIELD_EXPRESSION = "C.FIRSTNAME";

	public static final String JOB_FIELD_NAME = "job_title";

	public static final String JOB_FIELD_DISPLAY = "Job Title";

	public static final String JOB_FIELD_EXPRESSION = "J.TITLE";

	public static final String COMPANY_FIELD_NAME = "company_name";

	public static final String COMPANY_FIELD_DISPLAY = "Company Name";

	public static final String COMPANY_FIELD_EXPRESSION = "CO.NAME";

	public static final String APPROVAL_FIELD_NAME = "approval_status";

	public static final String APPROVAL_FIELD_DISPLAY = "Approval Status";

	public static final String APPROVAL_FIELD_EXPRESSION = "TA.STATUS";

	// ==================== Factory Methods ====================

	/**
	 * Creates a complete ExportFieldConfig with all field types populated
	 */
	public static ExportFieldConfig createCompleteExportFieldConfig() {
		ExportFieldConfig config = new ExportFieldConfig();
		config.setTimesheetFields(createTimesheetFieldsList());
		config.setCandidateFields(createCandidateFieldsList());
		config.setJobFields(createJobFieldsList());
		config.setCompanyFields(createCompanyFieldsList());
		config.setApprovalFields(createApprovalFieldsList());
		return config;
	}

	/**
	 * Creates an empty ExportFieldConfig with all null fields
	 */
	public static ExportFieldConfig createEmptyExportFieldConfig() {
		return new ExportFieldConfig();
	}

	/**
	 * Creates an ExportFieldConfig with only timesheet fields
	 */
	public static ExportFieldConfig createTimesheetOnlyConfig() {
		ExportFieldConfig config = new ExportFieldConfig();
		config.setTimesheetFields(createTimesheetFieldsList());
		return config;
	}

	/**
	 * Creates an ExportFieldConfig with empty lists instead of null
	 */
	public static ExportFieldConfig createConfigWithEmptyLists() {
		ExportFieldConfig config = new ExportFieldConfig();
		config.setTimesheetFields(new ArrayList<>());
		config.setCandidateFields(new ArrayList<>());
		config.setJobFields(new ArrayList<>());
		config.setCompanyFields(new ArrayList<>());
		config.setApprovalFields(new ArrayList<>());
		return config;
	}

	/**
	 * Creates an ExportFieldConfig with mixed null and populated fields
	 */
	public static ExportFieldConfig createMixedConfig() {
		ExportFieldConfig config = new ExportFieldConfig();
		config.setTimesheetFields(createTimesheetFieldsList());
		config.setCandidateFields(null);
		config.setJobFields(createJobFieldsList());
		config.setCompanyFields(null);
		config.setApprovalFields(createApprovalFieldsList());
		return config;
	}

	// ==================== Field Definition Lists ====================

	/**
	 * Creates a list of timesheet field definitions
	 */
	public static List<FieldDefinitionJson> createTimesheetFieldsList() {
		List<FieldDefinitionJson> fields = new ArrayList<>();
		fields.add(createTimesheetFieldDefinition());
		return fields;
	}

	/**
	 * Creates a list of candidate field definitions
	 */
	public static List<FieldDefinitionJson> createCandidateFieldsList() {
		List<FieldDefinitionJson> fields = new ArrayList<>();
		fields.add(createCandidateFieldDefinition());
		return fields;
	}

	/**
	 * Creates a list of job field definitions
	 */
	public static List<FieldDefinitionJson> createJobFieldsList() {
		List<FieldDefinitionJson> fields = new ArrayList<>();
		fields.add(createJobFieldDefinition());
		return fields;
	}

	/**
	 * Creates a list of company field definitions
	 */
	public static List<FieldDefinitionJson> createCompanyFieldsList() {
		List<FieldDefinitionJson> fields = new ArrayList<>();
		fields.add(createCompanyFieldDefinition());
		return fields;
	}

	/**
	 * Creates a list of approval field definitions
	 */
	public static List<FieldDefinitionJson> createApprovalFieldsList() {
		List<FieldDefinitionJson> fields = new ArrayList<>();
		fields.add(createApprovalFieldDefinition());
		return fields;
	}

	// ==================== Individual Field Definitions ====================

	/**
	 * Creates a timesheet field definition
	 */
	public static FieldDefinitionJson createTimesheetFieldDefinition() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(TIMESHEET_FIELD_NAME);
		field.setDisplayName(TIMESHEET_FIELD_DISPLAY);
		field.setJooqExpression(TIMESHEET_FIELD_EXPRESSION);
		field.setRequiredEntities(List.of("ts"));
		field.setEnabled(true);
		field.setNullable(false);
		field.setJavaType("Integer");
		field.setDynamicColumns(false);
		field.setRequiresPostProcessing(false);
		field.setDescription("Unique identifier for timesheet");
		return field;
	}

	/**
	 * Creates a candidate field definition
	 */
	public static FieldDefinitionJson createCandidateFieldDefinition() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(CANDIDATE_FIELD_NAME);
		field.setDisplayName(CANDIDATE_FIELD_DISPLAY);
		field.setJooqExpression(CANDIDATE_FIELD_EXPRESSION);
		field.setRequiredEntities(List.of("c"));
		field.setEnabled(true);
		field.setNullable(true);
		field.setJavaType("String");
		field.setDynamicColumns(false);
		field.setRequiresPostProcessing(false);
		field.setDescription("Full name of the candidate");
		return field;
	}

	/**
	 * Creates a job field definition
	 */
	public static FieldDefinitionJson createJobFieldDefinition() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(JOB_FIELD_NAME);
		field.setDisplayName(JOB_FIELD_DISPLAY);
		field.setJooqExpression(JOB_FIELD_EXPRESSION);
		field.setRequiredEntities(List.of("j"));
		field.setEnabled(true);
		field.setNullable(true);
		field.setJavaType("String");
		field.setDynamicColumns(false);
		field.setRequiresPostProcessing(false);
		field.setDescription("Title of the job position");
		return field;
	}

	/**
	 * Creates a company field definition
	 */
	public static FieldDefinitionJson createCompanyFieldDefinition() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(COMPANY_FIELD_NAME);
		field.setDisplayName(COMPANY_FIELD_DISPLAY);
		field.setJooqExpression(COMPANY_FIELD_EXPRESSION);
		field.setRequiredEntities(List.of("co"));
		field.setEnabled(true);
		field.setNullable(true);
		field.setJavaType("String");
		field.setDynamicColumns(false);
		field.setRequiresPostProcessing(false);
		field.setDescription("Name of the company");
		return field;
	}

	/**
	 * Creates an approval field definition
	 */
	public static FieldDefinitionJson createApprovalFieldDefinition() {
		FieldDefinitionJson field = new FieldDefinitionJson();
		field.setFrontendName(APPROVAL_FIELD_NAME);
		field.setDisplayName(APPROVAL_FIELD_DISPLAY);
		field.setJooqExpression(APPROVAL_FIELD_EXPRESSION);
		field.setRequiredEntities(List.of("ta"));
		field.setEnabled(true);
		field.setNullable(true);
		field.setJavaType("String");
		field.setDynamicColumns(false);
		field.setRequiresPostProcessing(false);
		field.setDescription("Current approval status");
		return field;
	}

	// ==================== Expected Results ====================

	/**
	 * Creates the expected total count of fields in complete config
	 */
	public static int getExpectedCompleteConfigFieldCount() {
		return 5; // 1 each from timesheet, candidate, job, company, approval
	}

	/**
	 * Creates the expected total count of fields in mixed config
	 */
	public static int getExpectedMixedConfigFieldCount() {
		return 3; // timesheet, job, approval (candidate and company are null)
	}

	/**
	 * Creates expected all fields list for complete config
	 */
	public static List<FieldDefinitionJson> createExpectedAllFieldsList() {
		List<FieldDefinitionJson> allFields = new ArrayList<>();
		allFields.addAll(createTimesheetFieldsList());
		allFields.addAll(createCandidateFieldsList());
		allFields.addAll(createJobFieldsList());
		allFields.addAll(createCompanyFieldsList());
		allFields.addAll(createApprovalFieldsList());
		return allFields;
	}

	/**
	 * Creates expected all fields list for mixed config
	 */
	public static List<FieldDefinitionJson> createExpectedMixedAllFieldsList() {
		List<FieldDefinitionJson> allFields = new ArrayList<>();
		allFields.addAll(createTimesheetFieldsList());
		allFields.addAll(createJobFieldsList());
		allFields.addAll(createApprovalFieldsList());
		return allFields;
	}

}
