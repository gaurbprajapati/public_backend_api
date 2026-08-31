package io.recruitcrm.microservice.timesheet.testdata;

import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Test data factory for JooqFieldResolver tests. Provides test data for field resolution
 * scenarios including valid expressions, invalid expressions, and edge cases.
 */
public final class JooqFieldResolverTestDataFactory {

	private JooqFieldResolverTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Test constants for field expressions
	 */
	public static final String VALID_TIMESHEET_EXPRESSION = "TS.ID";

	public static final String VALID_CANDIDATE_EXPRESSION = "C.FIRSTNAME";

	public static final String VALID_JOB_EXPRESSION = "J.TITLE";

	public static final String INVALID_EXPRESSION_NO_DOT = "INVALID";

	public static final String INVALID_EXPRESSION_TOO_MANY_PARTS = "TS.FIELD.EXTRA";

	public static final String INVALID_TABLE_EXPRESSION = "UNKNOWN.FIELD";

	public static final String INVALID_FIELD_EXPRESSION = "TS.NONEXISTENT";

	public static final String EMPTY_EXPRESSION = "";

	public static final String NULL_EXPRESSION = null;

	/**
	 * Test constants for aliases
	 */
	public static final String TEST_ALIAS = "testAlias";

	public static final String TIMESHEET_ID_ALIAS = "timesheetId";

	public static final String CANDIDATE_NAME_ALIAS = "candidateName";

	public static final String JOB_TITLE_ALIAS = "jobTitle";

	/**
	 * Test constants for table names
	 */
	public static final String TIMESHEET_TABLE = "TS";

	public static final String CANDIDATE_TABLE = "C";

	public static final String JOB_TABLE = "J";

	public static final String UNKNOWN_TABLE = "UNKNOWN";

	/**
	 * Test constants for field names
	 */
	public static final String ID_FIELD = "ID";

	public static final String FIRSTNAME_FIELD = "FIRSTNAME";

	public static final String TITLE_FIELD = "TITLE";

	public static final String NONEXISTENT_FIELD = "NONEXISTENT";

	/**
	 * Creates a valid timesheet field expression
	 */
	public static String createValidTimesheetExpression() {
		return VALID_TIMESHEET_EXPRESSION;
	}

	/**
	 * Creates a valid candidate field expression
	 */
	public static String createValidCandidateExpression() {
		return VALID_CANDIDATE_EXPRESSION;
	}

	/**
	 * Creates a valid job field expression
	 */
	public static String createValidJobExpression() {
		return VALID_JOB_EXPRESSION;
	}

	/**
	 * Creates an invalid expression without dot
	 */
	public static String createInvalidExpressionNoDot() {
		return INVALID_EXPRESSION_NO_DOT;
	}

	/**
	 * Creates an invalid expression with too many parts
	 */
	public static String createInvalidExpressionTooManyParts() {
		return INVALID_EXPRESSION_TOO_MANY_PARTS;
	}

	/**
	 * Creates an expression with unknown table
	 */
	public static String createUnknownTableExpression() {
		return INVALID_TABLE_EXPRESSION;
	}

	/**
	 * Creates an expression with nonexistent field
	 */
	public static String createNonexistentFieldExpression() {
		return INVALID_FIELD_EXPRESSION;
	}

	/**
	 * Creates an empty expression
	 */
	public static String createEmptyExpression() {
		return EMPTY_EXPRESSION;
	}

	/**
	 * Creates a test alias
	 */
	public static String createTestAlias() {
		return TEST_ALIAS;
	}

	/**
	 * Creates a timesheet ID alias
	 */
	public static String createTimesheetIdAlias() {
		return TIMESHEET_ID_ALIAS;
	}

	/**
	 * Creates a candidate name alias
	 */
	public static String createCandidateNameAlias() {
		return CANDIDATE_NAME_ALIAS;
	}

	/**
	 * Creates a job title alias
	 */
	public static String createJobTitleAlias() {
		return JOB_TITLE_ALIAS;
	}

	/**
	 * Creates an expected empty field for invalid expressions
	 */
	public static Field<?> createExpectedEmptyField(String alias) {
		return DSL.val("").as(alias);
	}

	/**
	 * Success messages for tests
	 */
	public static final class Messages {

		public static final String FIELD_RESOLVED_SUCCESSFULLY = "Field resolved successfully";

		public static final String FIELD_CACHED_SUCCESSFULLY = "Field cached successfully";

		public static final String EXPRESSION_CAN_BE_RESOLVED = "Expression can be resolved";

		public static final String EXPRESSION_CANNOT_BE_RESOLVED = "Expression cannot be resolved";

		public static final String INVALID_EXPRESSION_HANDLED = "Invalid expression handled gracefully";

	}

}
