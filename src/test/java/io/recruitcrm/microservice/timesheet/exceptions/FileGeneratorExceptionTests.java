package io.recruitcrm.microservice.timesheet.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.FileGeneratorExceptionTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test cases for FileGeneratorException following mandatory rule patterns. Tests all
 * public constructors with 100% branch coverage using factory-based test data and
 * BDD-style assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileGeneratorException Tests")
class FileGeneratorExceptionTests {

	// ==================== Constructor with Message Tests ====================

	@Test
	@DisplayName("Constructor with message should create exception with correct message")
	void testConstructorWithMessageCreatesExceptionWithCorrectMessage() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithMessage();

		// Then
		assertThat(exception.getMessage()).isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedErrorMessage());
		assertThat(exception.getCause()).isNull();
	}

	@Test
	@DisplayName("Constructor with detailed message should create exception with correct message")
	void testConstructorWithDetailedMessageCreatesExceptionWithCorrectMessage() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithDetailedMessage();

		// Then
		assertThat(exception.getMessage())
			.isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedDetailedErrorMessage());
		assertThat(exception.getCause()).isNull();
	}

	@Test
	@DisplayName("Constructor with null message should create exception with null message")
	void testConstructorWithNullMessageCreatesExceptionWithNullMessage() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithNullMessage();

		// Then
		assertThat(exception.getMessage()).isNull();
		assertThat(exception.getCause()).isNull();
	}

	@Test
	@DisplayName("Constructor with empty message should create exception with empty message")
	void testConstructorWithEmptyMessageCreatesExceptionWithEmptyMessage() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithEmptyMessage();

		// Then
		assertThat(exception.getMessage()).isEmpty();
		assertThat(exception.getCause()).isNull();
	}

	// ==================== Constructor with Message and Cause Tests ====================

	@Test
	@DisplayName("Constructor with message and cause should create exception with correct message and cause")
	void testConstructorWithMessageAndCauseCreatesExceptionWithCorrectMessageAndCause() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithMessageAndCause();

		// Then
		assertThat(exception.getMessage()).isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedErrorMessage());
		assertThat(exception.getCause()).isNotNull();
		assertThat(exception.getCause().getMessage())
			.isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedCauseMessage());
	}

	@Test
	@DisplayName("Constructor with detailed message and cause should create exception with correct message and cause")
	void testConstructorWithDetailedMessageAndCauseCreatesExceptionWithCorrectMessageAndCause() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory
			.createExceptionWithDetailedMessageAndCause();

		// Then
		assertThat(exception.getMessage())
			.isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedDetailedErrorMessage());
		assertThat(exception.getCause()).isNotNull();
		assertThat(exception.getCause().getMessage())
			.isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedCauseMessage());
	}

	@Test
	@DisplayName("Constructor with null message and cause should create exception with null message and correct cause")
	void testConstructorWithNullMessageAndCauseCreatesExceptionWithNullMessageAndCorrectCause() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory
			.createExceptionWithNullMessageAndCause();

		// Then
		assertThat(exception.getMessage()).isNull();
		assertThat(exception.getCause()).isNotNull();
		assertThat(exception.getCause().getMessage())
			.isEqualTo(FileGeneratorExceptionTestDataFactory.getExpectedCauseMessage());
	}

	// ==================== Exception Inheritance Tests ====================

	@Test
	@DisplayName("FileGeneratorException should extend RuntimeException")
	void testFileGeneratorExceptionExtendsRuntimeException() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithMessage();

		// Then
		assertThat(exception).isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("FileGeneratorException should be instance of Exception")
	void testFileGeneratorExceptionIsInstanceOfException() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithMessage();

		// Then
		assertThat(exception).isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("FileGeneratorException should be instance of Throwable")
	void testFileGeneratorExceptionIsInstanceOfThrowable() {
		// Given & When
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithMessage();

		// Then
		assertThat(exception).isInstanceOf(Throwable.class);
	}

	// ==================== Cause Chain Tests ====================

	@Test
	@DisplayName("Exception with nested cause should maintain cause chain")
	void testExceptionWithNestedCauseMaintainsCauseChain() {
		// Given
		RuntimeException nestedCause = FileGeneratorExceptionTestDataFactory.createNestedTestCause();
		FileGeneratorException exception = new FileGeneratorException(
				FileGeneratorExceptionTestDataFactory.getExpectedErrorMessage(), nestedCause);

		// When & Then
		assertThat(exception.getCause()).isEqualTo(nestedCause);
		assertThat(exception.getCause().getCause()).isNotNull();
		assertThat(exception.getCause().getCause().getMessage()).isEqualTo("Root cause exception");
	}

	@Test
	@DisplayName("Exception cause should be accessible via getCause method")
	void testExceptionCauseIsAccessibleViaGetCauseMethod() {
		// Given
		RuntimeException testCause = FileGeneratorExceptionTestDataFactory.createTestCause();
		FileGeneratorException exception = new FileGeneratorException(
				FileGeneratorExceptionTestDataFactory.getExpectedErrorMessage(), testCause);

		// When
		Throwable retrievedCause = exception.getCause();

		// Then
		assertThat(retrievedCause).isEqualTo(testCause).isInstanceOf(RuntimeException.class);
	}

	// ==================== toString() Tests ====================

	@Test
	@DisplayName("Exception toString should include class name and message")
	void testExceptionToStringIncludesClassNameAndMessage() {
		// Given
		FileGeneratorException exception = FileGeneratorExceptionTestDataFactory.createExceptionWithMessage();

		// When
		String stringRepresentation = exception.toString();

		// Then
		assertThat(stringRepresentation).contains("FileGeneratorException")
			.contains(FileGeneratorExceptionTestDataFactory.getExpectedErrorMessage());
	}

}
