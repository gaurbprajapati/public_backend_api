package io.recruitcrm.microservice.timesheet.testdata;

import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.HashMap;
import java.util.Map;

/**
 * Test data factory for AdvancedJooqExpressionParser unit tests. Provides factory methods
 * for creating test data following the mandatory rule patterns.
 */
public final class AdvancedJooqExpressionParserTestDataFactory {

	// ==================== Constants ====================

	/**
	 * Success message for parsing operations
	 */
	public static final String PARSING_SUCCESS = "Expression parsed successfully";

	/**
	 * Error message for parsing operations
	 */
	public static final String PARSING_FAILED = "Expression parsing failed";

	public static final String CANDIDATE_LAST_NOTE_DATE_EXPRESSION = "FROM_UNIXTIME(CLA.NOTE_CREATED_ON, \"%b %d %Y\")";

	public static final String LAST_NOTE_CREATED_ON_ALIAS = "last_note_created_on";

	private AdvancedJooqExpressionParserTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Simple Field Expressions ====================

	/**
	 * Creates a simple table.field expression
	 */
	public static String createSimpleFieldExpression() {
		return "TS.ID";
	}

	/**
	 * Creates a candidate field expression
	 */
	public static String createCandidateFieldExpression() {
		return "C.FIRSTNAME";
	}

	/**
	 * Creates an invalid field expression
	 */
	public static String createInvalidFieldExpression() {
		return "INVALID_TABLE.INVALID_FIELD";
	}

	// ==================== DSL.val() Expressions ====================

	/**
	 * Creates a DSL.val() string expression
	 */
	public static String createDslValueStringExpression() {
		return "DSL.val(\"Test Value\")";
	}

	/**
	 * Creates a DSL.val() empty string expression
	 */
	public static String createDslValueEmptyStringExpression() {
		return "DSL.val(\"\")";
	}

	/**
	 * Creates a DSL.val() integer expression
	 */
	public static String createDslValueIntegerExpression() {
		return "DSL.val(123)";
	}

	/**
	 * Creates a DSL.val() decimal expression
	 */
	public static String createDslValueDecimalExpression() {
		return "DSL.val(123.45)";
	}

	/**
	 * Creates an invalid DSL.val() expression
	 */
	public static String createInvalidDslValueExpression() {
		return "DSL.val(invalid";
	}

	// ==================== Candidate Expressions ====================

	/**
	 * Creates a candidate name expression with DSL.when
	 */
	public static String createCandidateNameExpression() {
		return "DSL.when(C.LASTNAME.isNull().or(C.LASTNAME.eq(\"\")), C.FIRSTNAME).otherwise(DSL.concat(C.FIRSTNAME, DSL.val(\" \"), C.LASTNAME))";
	}

	/**
	 * Creates a candidate age expression with TIMESTAMPDIFF
	 */
	public static String createCandidateAgeExpression() {
		return "DSL.when(C.CANDIDATEDOB.ne(1).and(C.CANDIDATEDOB.ne(0)), TIMESTAMPDIFF(YEAR, FROM_UNIXTIME(C.CANDIDATEDOB), NOW())).otherwise(0)";
	}

	/**
	 * Creates a salary type case expression
	 */
	public static String createSalaryTypeCaseExpression() {
		return "DSL.case_(C.SALARYTYPE).when(\"1\", \"Monthly Salary\").when(\"2\", \"Annual Salary\").otherwise(\"\")";
	}

	/**
	 * Creates a candidate date field expression
	 */
	public static String createCandidateDateFieldExpression() {
		return "FROM_UNIXTIME(C.CANDIDATEDOB, \"%b %d %Y\")";
	}

	/**
	 * Creates a candidate opt-out expression
	 */
	public static String createCandidateOptOutExpression() {
		return "DSL.case_(C.EMAIL_OPT_OUT).when(0, \"No\").otherwise(\"Yes\")";
	}

	// ==================== Timesheet Expressions ====================

	/**
	 * Creates a timesheet period expression
	 */
	public static String createTimesheetPeriodExpression() {
		return "DSL.concat(FROM_UNIXTIME(TS.PERIOD_START, \"%m/%d/%Y\"), DSL.val(\" - \"), FROM_UNIXTIME(TS.PERIOD_END, \"%m/%d/%Y\"))";
	}

	/**
	 * Creates a job duration expression
	 */
	public static String createJobDurationExpression() {
		return "DSL.concat(FROM_UNIXTIME(TSS.JOB_START_DATE, \"%m/%d/%Y\"), DSL.val(\" - \"), FROM_UNIXTIME(TSS.JOB_END_DATE, \"%m/%d/%Y\"))";
	}

	/**
	 * Creates a timesheet frequency case expression
	 */
	public static String createTimesheetFrequencyExpression() {
		return "DSL.case_(TSS.TIMESHEET_FREQUENCY).when(1, \"Custom\").when(2, \"Weekly\").otherwise(\"Unknown\")";
	}

	/**
	 * Creates a timesheet status case expression
	 */
	public static String createTimesheetStatusExpression() {
		return "DSL.case_(TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID).when(1, \"OPEN\").when(2, \"SUBMITTED\").otherwise(\"UNKNOWN\")";
	}

	/**
	 * Creates a formatted timesheet ID expression
	 */
	public static String createFormattedTimesheetIdExpression() {
		return "DSL.concat(DSL.val(\"TS-\"), C.SRNO, DSL.val(\"-\"), TS.ID)";
	}

	/**
	 * Creates a timesheet date field expression
	 */
	public static String createTimesheetDateFieldExpression() {
		return "FROM_UNIXTIME(TS.ADDED_ON, \"%m/%d/%Y\")";
	}

	// ==================== User Expressions ====================

	/**
	 * Creates a user full name expression
	 */
	public static String createUserFullNameExpression() {
		return "DSL.concat(TU.FIRST_NAME, DSL.val(\" \"), TU.LAST_NAME)";
	}

	/**
	 * Creates a timesheet added by expression
	 */
	public static String createTimesheetAddedByExpression() {
		return "DSL.concat(TU_ADDED.FIRSTNAME, DSL.val(\" \"), TU_ADDED.LASTNAME)";
	}

	/**
	 * Creates a timesheet updated by expression
	 */
	public static String createTimesheetUpdatedByExpression() {
		return "DSL.concat(TU_UPDATED.FIRSTNAME, DSL.val(\" \"), TU_UPDATED.LASTNAME)";
	}

	// ==================== Invoice Expressions ====================

	/**
	 * Creates a pay status case expression
	 */
	public static String createPayStatusCaseExpression() {
		return "PAY_STATUS_CASE";
	}

	/**
	 * Creates a bill status case expression
	 */
	public static String createBillStatusCaseExpression() {
		return "BILL_STATUS_CASE";
	}

	/**
	 * Creates a payout paid on expression
	 */
	public static String createPayoutPaidOnExpression() {
		return "PAYOUT_PAID_ON_EXPR";
	}

	/**
	 * Creates an invoice created on expression
	 */
	public static String createInvoiceCreatedOnExpression() {
		return "INVOICE_CREATED_ON_EXPR";
	}

	/**
	 * Creates an invoice number expression
	 */
	public static String createInvoiceNumberExpression() {
		return "INVOICE_NUMBER_EXPR";
	}

	// ==================== Special Expressions ====================

	/**
	 * Creates an approvers subquery expression
	 */
	public static String createApproversSubqueryExpression() {
		return "SUBQUERY_GROUP_CONCAT_APPROVERS";
	}

	/**
	 * Creates a deal name field expression
	 */
	public static String createDealNameFieldExpression() {
		return "DSL.field(deals.deal_name, String.class)";
	}

	/**
	 * Creates a date format function expression
	 */
	public static String createDateFormatFunctionExpression() {
		return "DSL.function(DATE_FORMAT, String.class, TS.ID, DSL.val(\"%d %b %Y\"))";
	}

	/**
	 * Creates a custom column field expression
	 */
	public static String createCustomColumnFieldExpression() {
		return "customcolumn1";
	}

	/**
	 * Creates a valid cust-prefixed custom column expression (matches CUST_COLUMN_PREFIX
	 * = "custcolumn")
	 */
	public static String createValidCustColumnExpression() {
		return "custcolumn1";
	}

	/**
	 * Creates an invalid custom column expression
	 */
	public static String createInvalidCustomColumnExpression() {
		return "customcolumnabc";
	}

	/**
	 * Creates a custom column with high number
	 */
	public static String createHighNumberCustomColumnExpression() {
		return "customcolumn999";
	}

	// ==================== Edge Cases ====================

	/**
	 * Creates a null expression
	 */
	public static String createNullExpression() {
		return null;
	}

	/**
	 * Creates an empty expression
	 */
	public static String createEmptyExpression() {
		return "";
	}

	/**
	 * Creates an expression that throws exception
	 */
	public static String createExceptionThrowingExpression() {
		return "INVALID_EXPRESSION_THAT_THROWS_EXCEPTION";
	}

	// ==================== Test Aliases ====================

	/**
	 * Gets default test alias
	 */
	public static String getDefaultTestAlias() {
		return "test_alias";
	}

	/**
	 * Gets candidate name alias
	 */
	public static String getCandidateNameAlias() {
		return "candidate_name";
	}

	/**
	 * Gets timesheet period alias
	 */
	public static String getTimesheetPeriodAlias() {
		return "timesheet_period";
	}

	/**
	 * Gets formatted timesheet ID alias
	 */
	public static String getFormattedTimesheetIdAlias() {
		return "formatted_timesheet_id";
	}

	// ==================== Expected Results ====================

	/**
	 * Creates expected JOOQ field for simple expressions
	 */
	public static Field<?> createExpectedSimpleField(String alias) {
		return DSL.val("").as(alias);
	}

	/**
	 * Creates expected JOOQ field for DSL.val string
	 */
	public static Field<?> createExpectedDslValueStringField(String alias) {
		return DSL.val("Test Value").as(alias);
	}

	/**
	 * Creates expected JOOQ field for DSL.val empty string
	 */
	public static Field<?> createExpectedDslValueEmptyStringField(String alias) {
		return DSL.val("").as(alias);
	}

	/**
	 * Creates expected JOOQ field for DSL.val integer
	 */
	public static Field<?> createExpectedDslValueIntegerField(String alias) {
		return DSL.val(123).as(alias);
	}

	/**
	 * Creates expected JOOQ field for DSL.val decimal
	 */
	public static Field<?> createExpectedDslValueDecimalField(String alias) {
		return DSL.val(123.45).as(alias);
	}

	/**
	 * Creates expected fallback field for invalid expressions
	 */
	public static Field<?> createExpectedFallbackField(String alias) {
		return DSL.val("").as(alias);
	}

	// ==================== Mock Data ====================

	/**
	 * Creates mock field resolver responses
	 */
	public static Map<String, Boolean> createMockFieldResolverCanResolveResponses() {
		Map<String, Boolean> responses = new HashMap<>();
		responses.put("TS.ID", true);
		responses.put("C.FIRSTNAME", true);
		responses.put("INVALID_TABLE.INVALID_FIELD", false);
		responses.put("DSL.val(\"Test\")", false);
		responses.put("DSL.concat(...)", false);
		return responses;
	}

	/**
	 * Creates mock field resolver field responses
	 */
	public static Map<String, Field<?>> createMockFieldResolverFieldResponses() {
		Map<String, Field<?>> responses = new HashMap<>();
		responses.put("TS.ID:test_alias", DSL.field("ts.id").as("test_alias"));
		responses.put("C.FIRSTNAME:candidate_name", DSL.field("c.firstname").as("candidate_name"));
		return responses;
	}

	// ==================== Test Scenarios ====================

	/**
	 * Creates test data for simple field resolution scenario
	 */
	public static String createSimpleFieldResolutionScenario() {
		return createSimpleFieldExpression();
	}

	/**
	 * Creates test data for DSL value parsing scenario
	 */
	public static String createDslValueParsingScenario() {
		return createDslValueStringExpression();
	}

	/**
	 * Creates test data for complex expression parsing scenario
	 */
	public static String createComplexExpressionParsingScenario() {
		return createCandidateNameExpression();
	}

	/**
	 * Creates test data for exception handling scenario
	 */
	public static String createExceptionHandlingScenario() {
		return createExceptionThrowingExpression();
	}

	/**
	 * Creates test data for fallback scenario
	 */
	public static String createFallbackScenario() {
		return createInvalidFieldExpression();
	}

	// ==================== Branch Coverage Edge Cases ====================

	/**
	 * Creates expression missing C.CANDIDATEDOB for candidate age calculation
	 */
	public static String createCandidateAgeExpressionMissingCandidateDob() {
		return "DSL.when(CONDITION).TIMESTAMPDIFF(YEAR, FROM_UNIXTIME(field), NOW()).otherwise(0)";
	}

	/**
	 * Creates expression missing TIMESTAMPDIFF for candidate age calculation
	 */
	public static String createCandidateAgeExpressionMissingTimestampDiff() {
		return "DSL.when(C.CANDIDATEDOB.ne(1)).field().otherwise(0)";
	}

	/**
	 * Creates timesheet period expression missing TS.PERIOD_END
	 */
	public static String createTimesheetPeriodExpressionMissingPeriodEnd() {
		return "DSL.concat(FROM_UNIXTIME(TS.PERIOD_START), DSL.val(\" - \"))";
	}

	/**
	 * Creates job date range expression missing TSS.JOB_END_DATE
	 */
	public static String createJobDateRangeExpressionMissingJobEndDate() {
		return "DSL.concat(FROM_UNIXTIME(TSS.JOB_START_DATE), DSL.val(\" - \"))";
	}

	/**
	 * Creates timesheet frequency expression missing DSL.case_
	 */
	public static String createTimesheetFrequencyExpressionMissingCase() {
		return "TSS.TIMESHEET_FREQUENCY.eq(1).then(\"Custom\")";
	}

	/**
	 * Creates approval status expression missing DSL.case_
	 */
	public static String createApprovalStatusExpressionMissingCase() {
		return "TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(1).then(\"Approved\")";
	}

	/**
	 * Creates formatted timesheet expression missing TS.ID
	 */
	public static String createFormattedTimesheetExpressionMissingTsId() {
		return "DSL.concat(DSL.val(\"TS-\"), C.SRNO, DSL.val(\"-\"))";
	}

	/**
	 * Creates formatted timesheet expression missing DSL.concat
	 */
	public static String createFormattedTimesheetExpressionMissingConcat() {
		return "C.SRNO + TS.ID + \"TS-\"";
	}

	/**
	 * Creates formatted timesheet expression missing TS- prefix
	 */
	public static String createFormattedTimesheetExpressionMissingTsPrefix() {
		return "DSL.concat(C.SRNO, DSL.val(\"-\"), TS.ID)";
	}

	/**
	 * Creates user approved-by expression missing TU.FIRST_NAME (first compound-condition
	 * branch)
	 */
	public static String createUserApprovedByExpressionMissingFirstName() {
		return "DSL.concat(TU.LAST_NAME, DSL.val(\" \"))";
	}

	/**
	 * Creates user approved-by expression missing TU.LAST_NAME (second compound-condition
	 * branch)
	 */
	public static String createUserApprovedByExpressionMissingLastName() {
		return "DSL.concat(TU.FIRST_NAME, DSL.val(\" \"))";
	}

	/**
	 * Creates timesheet added-by expression missing DSL.concat (third compound-condition
	 * branch)
	 */
	public static String createTimesheetAddedByExpressionMissingConcat() {
		return "TU_ADDED.FIRSTNAME TU_ADDED.LASTNAME";
	}

	/**
	 * Creates timesheet added-by expression missing TU_ADDED.LASTNAME (second
	 * compound-condition branch)
	 */
	public static String createTimesheetAddedByExpressionMissingLastName() {
		return "DSL.concat(TU_ADDED.FIRSTNAME, DSL.val(\" \"))";
	}

	/**
	 * Creates timesheet updated-by expression missing TU_UPDATED.LASTNAME (second
	 * compound-condition branch)
	 */
	public static String createTimesheetUpdatedByExpressionMissingLastName() {
		return "DSL.concat(TU_UPDATED.FIRSTNAME, DSL.val(\" \"))";
	}

	/**
	 * Creates DSL.val expression with unclosed string quote for parseDslValue branch
	 * coverage
	 */
	public static String createDslValueUnclosedStringExpression() {
		return "DSL.val(\"unclosed)";
	}

	/**
	 * Creates DSL.val expression without DSL.val prefix for direct parseDslValue
	 * invocation
	 */
	public static String createDslValueWithoutPrefixExpression() {
		return "notDSL.val(test)";
	}

	/**
	 * Creates custom column expression with index zero (below valid range)
	 */
	public static String createZeroIndexCustomColumnExpression() {
		return "custcolumn0";
	}

}
