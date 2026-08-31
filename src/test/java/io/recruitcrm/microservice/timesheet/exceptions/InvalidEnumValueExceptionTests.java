package io.recruitcrm.microservice.timesheet.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvalidEnumValueException Tests")
class InvalidEnumValueExceptionTests {

	@Test
	@DisplayName("Constructor with enum name and value should build descriptive message")
	void testConstructorWithEnumNameAndValueBuildsDescriptiveMessage() {
		// Given
		InvalidEnumValueException exception = new InvalidEnumValueException("Status", "FOO");

		// When and Then
		assertThat(exception).isInstanceOf(ValidationErrorException.class);
		assertThat(exception.getMessage()).isEqualTo("Invalid Status value: FOO");
	}

	@Test
	@DisplayName("Constructor with cause should preserve message and cause")
	void testConstructorWithCausePreservesMessageAndCause() {
		// Given
		Throwable cause = new IllegalArgumentException("root");
		InvalidEnumValueException exception = new InvalidEnumValueException("Status", 42, cause);

		// When and Then
		assertThat(exception.getMessage()).isEqualTo("Invalid Status value: 42");
		assertThat(exception.getCause()).isSameAs(cause);
	}

}
