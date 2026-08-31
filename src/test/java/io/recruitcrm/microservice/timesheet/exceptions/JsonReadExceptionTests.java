package io.recruitcrm.microservice.timesheet.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JsonReadException class.
 */
class JsonReadExceptionTests {

	@Test
	@DisplayName("JsonReadException - Message and cause constructor")
	void jsonReadExceptionMessageAndCauseConstructor() {
		// Arrange
		String message = "Failed to parse JSON";
		RuntimeException cause = new RuntimeException("JSON parsing error");

		// Act
		JsonReadException exception = new JsonReadException(message, cause);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(message);
		assertThat(exception.getCause()).isEqualTo(cause.getCause());
	}

	@Test
	@DisplayName("JsonReadException - Null message constructor")
	void jsonReadExceptionNullMessageConstructor() {
		// Arrange
		String message = null;
		RuntimeException cause = new RuntimeException("JSON parsing error");

		// Act
		JsonReadException exception = new JsonReadException(message, cause);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isNull();
		assertThat(exception.getCause()).isEqualTo(cause.getCause());
	}

	@Test
	@DisplayName("JsonReadException - Empty message constructor")
	void jsonReadExceptionEmptyMessageConstructor() {
		// Arrange
		String message = "";
		RuntimeException cause = new RuntimeException("JSON parsing error");

		// Act
		JsonReadException exception = new JsonReadException(message, cause);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEmpty();
		assertThat(exception.getCause()).isEqualTo(cause.getCause());
	}

	@Test
	@DisplayName("JsonReadException - Null cause constructor throws NullPointerException")
	void jsonReadExceptionNullCauseConstructor() {
		// Arrange
		String message = "Failed to parse JSON";
		RuntimeException cause = null;

		// Act & Assert - Should throw NullPointerException
		assertThatThrownBy(() -> new JsonReadException(message, cause)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("JsonReadException - Cause with nested exception")
	void jsonReadExceptionCauseWithNestedException() {
		// Arrange
		String message = "Failed to parse JSON";
		Exception nestedException = new IllegalArgumentException("Invalid format");
		RuntimeException cause = new RuntimeException("Wrapper exception", nestedException);

		// Act
		JsonReadException exception = new JsonReadException(message, cause);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(message);
		assertThat(exception.getCause()).isEqualTo(nestedException);
	}

	@Test
	@DisplayName("JsonReadException - Long message constructor")
	void jsonReadExceptionLongMessageConstructor() {
		// Arrange
		String longMessage = "This is a very long JSON parsing error message that contains detailed information about what went wrong during the JSON parsing process";
		RuntimeException cause = new RuntimeException("JSON parsing error");

		// Act
		JsonReadException exception = new JsonReadException(longMessage, cause);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(longMessage);
		assertThat(exception.getCause()).isEqualTo(cause.getCause());
	}

	@Test
	@DisplayName("JsonReadException - Special characters in message")
	void jsonReadExceptionSpecialCharactersInMessage() {
		// Arrange
		String specialMessage = "JSON parsing failed: Invalid character at position 123 (Special chars: @#$%^&*{}[]\"'\\/)";
		RuntimeException cause = new RuntimeException("JSON parsing error");

		// Act
		JsonReadException exception = new JsonReadException(specialMessage, cause);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(specialMessage);
		assertThat(exception.getCause()).isEqualTo(cause.getCause());
	}

	@Test
	@DisplayName("JsonReadException - Different exception types as cause")
	void jsonReadExceptionDifferentExceptionTypesAsCause() {
		// Test with different exception types
		IllegalArgumentException illegalArgException = new IllegalArgumentException("Invalid argument");
		NullPointerException nullPointerException = new NullPointerException("Null pointer");
		ClassCastException classCastException = new ClassCastException("Class cast error");

		JsonReadException exception1 = new JsonReadException("Error 1", illegalArgException);
		JsonReadException exception2 = new JsonReadException("Error 2", nullPointerException);
		JsonReadException exception3 = new JsonReadException("Error 3", classCastException);

		// Assert
		assertThat(exception1.getCause()).isEqualTo(illegalArgException.getCause());
		assertThat(exception2.getCause()).isEqualTo(nullPointerException.getCause());
		assertThat(exception3.getCause()).isEqualTo(classCastException.getCause());
	}

}