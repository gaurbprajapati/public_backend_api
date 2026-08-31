package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.exceptions.FileGeneratorException;

/**
 * Test data factory for FileGeneratorException unit tests. Provides factory methods for
 * creating test data and constants for comprehensive test coverage.
 */
public final class FileGeneratorExceptionTestDataFactory {

	private FileGeneratorExceptionTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Test Constants ====================

	public static final String TEST_ERROR_MESSAGE = "File generation failed";

	public static final String TEST_DETAILED_ERROR_MESSAGE = "Failed to generate CSV file due to invalid data format";

	public static final String TEST_CAUSE_MESSAGE = "Underlying IO exception occurred";

	// ==================== Factory Methods ====================

	/**
	 * Creates a FileGeneratorException with message only
	 */
	public static FileGeneratorException createExceptionWithMessage() {
		return new FileGeneratorException(TEST_ERROR_MESSAGE);
	}

	/**
	 * Creates a FileGeneratorException with detailed message
	 */
	public static FileGeneratorException createExceptionWithDetailedMessage() {
		return new FileGeneratorException(TEST_DETAILED_ERROR_MESSAGE);
	}

	/**
	 * Creates a FileGeneratorException with message and cause
	 */
	public static FileGeneratorException createExceptionWithMessageAndCause() {
		Throwable cause = createTestCause();
		return new FileGeneratorException(TEST_ERROR_MESSAGE, cause);
	}

	/**
	 * Creates a FileGeneratorException with detailed message and cause
	 */
	public static FileGeneratorException createExceptionWithDetailedMessageAndCause() {
		Throwable cause = createTestCause();
		return new FileGeneratorException(TEST_DETAILED_ERROR_MESSAGE, cause);
	}

	/**
	 * Creates a test cause exception
	 */
	public static RuntimeException createTestCause() {
		return new RuntimeException(TEST_CAUSE_MESSAGE);
	}

	/**
	 * Creates a test cause with nested exception
	 */
	public static RuntimeException createNestedTestCause() {
		RuntimeException rootCause = new RuntimeException("Root cause exception");
		return new RuntimeException(TEST_CAUSE_MESSAGE, rootCause);
	}

	/**
	 * Creates a FileGeneratorException with null message
	 */
	public static FileGeneratorException createExceptionWithNullMessage() {
		return new FileGeneratorException(null);
	}

	/**
	 * Creates a FileGeneratorException with empty message
	 */
	public static FileGeneratorException createExceptionWithEmptyMessage() {
		return new FileGeneratorException("");
	}

	/**
	 * Creates a FileGeneratorException with null message and cause
	 */
	public static FileGeneratorException createExceptionWithNullMessageAndCause() {
		Throwable cause = createTestCause();
		return new FileGeneratorException(null, cause);
	}

	// ==================== Expected Results ====================

	/**
	 * Gets the expected error message for basic exception
	 */
	public static String getExpectedErrorMessage() {
		return TEST_ERROR_MESSAGE;
	}

	/**
	 * Gets the expected detailed error message
	 */
	public static String getExpectedDetailedErrorMessage() {
		return TEST_DETAILED_ERROR_MESSAGE;
	}

	/**
	 * Gets the expected cause message
	 */
	public static String getExpectedCauseMessage() {
		return TEST_CAUSE_MESSAGE;
	}

}
