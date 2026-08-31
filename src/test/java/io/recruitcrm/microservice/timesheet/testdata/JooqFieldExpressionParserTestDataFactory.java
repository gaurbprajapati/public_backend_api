package io.recruitcrm.microservice.timesheet.testdata;

import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Test data factory for JooqFieldExpressionParser tests
 */
public final class JooqFieldExpressionParserTestDataFactory {

	private JooqFieldExpressionParserTestDataFactory() {
	}

	// Test constants
	public static final String VALID_EXPRESSION = "CANDIDATE.ID";

	public static final String COMPLEX_EXPRESSION = "DSL.concat(CANDIDATE.FIRSTNAME, DSL.val(' '), CANDIDATE.LASTNAME)";

	public static final String INVALID_EXPRESSION = "INVALID.EXPRESSION";

	public static final String NULL_EXPRESSION = null;

	public static final String EMPTY_EXPRESSION = "";

	public static final String TEST_ALIAS = "test_alias";

	public static final String CANDIDATE_NAME_ALIAS = "candidate_name";

	public static final String TIMESHEET_ID_ALIAS = "timesheet_id";

	/**
	 * Creates a valid JOOQ field for testing successful parsing
	 */
	public static Field<Object> createValidJooqField() {
		return DSL.field("CANDIDATE.ID").as(TEST_ALIAS);
	}

	/**
	 * Creates a complex JOOQ field for testing advanced expressions
	 */
	public static Field<String> createComplexJooqField() {
		return DSL.concat(DSL.field("CANDIDATE.FIRSTNAME"), DSL.val(" "), DSL.field("CANDIDATE.LASTNAME"))
			.as(CANDIDATE_NAME_ALIAS);
	}

	/**
	 * Creates a fallback JOOQ field (empty string with alias) for testing exception
	 * scenarios
	 */
	public static Field<String> createFallbackJooqField() {
		return DSL.val("").as(TEST_ALIAS);
	}

	/**
	 * Creates a fallback JOOQ field with custom alias
	 */
	public static Field<String> createFallbackJooqField(String alias) {
		return DSL.val("").as(alias);
	}

	/**
	 * Creates a timesheet ID field for testing
	 */
	public static Field<Object> createTimesheetIdField() {
		return DSL.field("TIMESHEET.ID").as(TIMESHEET_ID_ALIAS);
	}

}
