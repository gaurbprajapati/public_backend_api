package io.recruitcrm.microservice.timesheet.exceptions.access_control;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvalidPermissionLevelException Tests")
class InvalidPermissionLevelExceptionTests {

	@Test
	@DisplayName("Constructor with message should preserve message")
	void testConstructorWithMessagePreservesMessage() {
		// Given
		InvalidPermissionLevelException exception = new InvalidPermissionLevelException("invalid level");

		// When and Then
		assertThat(exception).isInstanceOf(RuntimeException.class);
		assertThat(exception.getMessage()).isEqualTo("invalid level");
	}

	@Test
	@DisplayName("Constructor with message and cause should preserve both")
	void testConstructorWithMessageAndCausePreservesBoth() {
		// Given
		Throwable cause = new IllegalStateException("root");
		InvalidPermissionLevelException exception = new InvalidPermissionLevelException("invalid level", cause);

		// When and Then
		assertThat(exception.getMessage()).isEqualTo("invalid level");
		assertThat(exception.getCause()).isSameAs(cause);
	}

}
