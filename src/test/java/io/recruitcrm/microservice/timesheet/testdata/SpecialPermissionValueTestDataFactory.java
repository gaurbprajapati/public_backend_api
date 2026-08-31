package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for SpecialPermissionValue tests.
 */
public final class SpecialPermissionValueTestDataFactory {

	public static final String VALID_STRING_OWNED_ONLY_MIXED_CASE = "oWnEd oNlY";

	public static final String INVALID_STRING_PERMISSION_VALUE = "Not A Permission";

	public static final int VALID_INTEGER_EVERYTHING = 5;

	public static final int VALID_INTEGER_SHARED_LEVEL = 3;

	public static final int INVALID_INTEGER_PERMISSION_VALUE = 42;

	private SpecialPermissionValueTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
