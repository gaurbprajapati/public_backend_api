package io.recruitcrm.microservice.timesheet.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorExceptionTests {

	@Test
	@DisplayName("Test ValidationErrorException message")
	void testExceptionMessage() {
		String message = "Test message";
		ValidationErrorException exception = new ValidationErrorException(message);

		assertThat(exception.getMessage()).isEqualTo(message);
	}

}
