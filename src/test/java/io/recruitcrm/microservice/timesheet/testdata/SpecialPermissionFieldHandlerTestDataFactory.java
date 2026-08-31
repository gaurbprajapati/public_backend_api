package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for SpecialPermissionFieldHandler tests.
 */
public final class SpecialPermissionFieldHandlerTestDataFactory {

	public static final String BUILT_IN_FIELD_LOWER_CASE = "recruiterperformancereportaccess";

	public static final String BUILT_IN_FIELD_MIXED_CASE = "RecruiterPerformanceReportAccess";

	public static final String NON_SPECIAL_FIELD = "normalFieldAccess";

	public static final String REGISTRABLE_FIELD_MIXED_CASE = "MyCustomReportAccess";

	public static final int VALID_INTEGER_PERMISSION_VALUE = 5;

	public static final int INVALID_INTEGER_PERMISSION_VALUE = 99;

	public static final String VALID_STRING_PERMISSION_VALUE = "Owned Only";

	public static final String INVALID_STRING_PERMISSION_VALUE = "Unknown Permission";

	private SpecialPermissionFieldHandlerTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
