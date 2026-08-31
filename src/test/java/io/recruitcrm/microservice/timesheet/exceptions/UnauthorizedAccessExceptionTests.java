package io.recruitcrm.microservice.timesheet.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UnauthorizedAccessException class.
 */
class UnauthorizedAccessExceptionTests {

	@Test
	@DisplayName("UnauthorizedAccessException - Default constructor")
	void unauthorizedAccessExceptionDefaultConstructor() {
		// Act
		UnauthorizedAccessException exception = new UnauthorizedAccessException();

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Unauthorized");
	}

	@Test
	@DisplayName("UnauthorizedAccessException - Custom message constructor")
	void unauthorizedAccessExceptionCustomMessageConstructor() {
		// Arrange
		String customMessage = "Access denied to timesheet";

		// Act
		UnauthorizedAccessException exception = new UnauthorizedAccessException(customMessage);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(customMessage);
	}

	@Test
	@DisplayName("UnauthorizedAccessException - Null message constructor")
	void unauthorizedAccessExceptionNullMessageConstructor() {
		// Act
		UnauthorizedAccessException exception = new UnauthorizedAccessException(null);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isNull();
	}

	@Test
	@DisplayName("UnauthorizedAccessException - Empty message constructor")
	void unauthorizedAccessExceptionEmptyMessageConstructor() {
		// Arrange
		String emptyMessage = "";

		// Act
		UnauthorizedAccessException exception = new UnauthorizedAccessException(emptyMessage);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(emptyMessage);
	}

	@Test
	@DisplayName("UnauthorizedAccessException - Long message constructor")
	void unauthorizedAccessExceptionLongMessageConstructor() {
		// Arrange
		String longMessage = "This is a very long unauthorized access message that contains detailed information about why the access was denied";

		// Act
		UnauthorizedAccessException exception = new UnauthorizedAccessException(longMessage);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(longMessage);
	}

	@Test
	@DisplayName("UnauthorizedAccessException - Special characters in message")
	void unauthorizedAccessExceptionSpecialCharactersInMessage() {
		// Arrange
		String specialMessage = "Access denied: User ID 123 cannot access timesheet #456 (Special chars: @#$%^&*)";

		// Act
		UnauthorizedAccessException exception = new UnauthorizedAccessException(specialMessage);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(specialMessage);
	}

}