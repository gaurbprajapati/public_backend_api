package io.recruitcrm.microservice.timesheet.dto.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidSubmissionReminderWindowValidator Tests")
class ValidSubmissionReminderWindowValidatorTests {

	private ValidSubmissionReminderWindowValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		this.validator = new ValidSubmissionReminderWindowValidator();
	}

	@Test
	@DisplayName("isValid should return true when value is null")
	void testIsValidNullValueReturnsTrue() {
		// Given
		TimesheetSubmissionReminderJobRequestBodyDto value = null;

		// When
		boolean result = this.validator.isValid(value, this.context);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isValid should return true when from is strictly before to")
	void testIsValidStrictlyIncreasingWindowReturnsTrue() {
		// Given
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		TimesheetSubmissionReminderJobRequestBodyDto value = new TimesheetSubmissionReminderJobRequestBodyDto(from, to);

		// When
		boolean result = this.validator.isValid(value, this.context);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isValid should return false when from equals to")
	void testIsValidEqualWindowReturnsFalse() {
		// Given
		Instant instant = Instant.parse("2026-01-01T00:00:00Z");
		TimesheetSubmissionReminderJobRequestBodyDto value = new TimesheetSubmissionReminderJobRequestBodyDto(instant,
				instant);

		// When
		boolean result = this.validator.isValid(value, this.context);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isValid should return false when from is after to")
	void testIsValidFromAfterToReturnsFalse() {
		// Given
		Instant from = Instant.parse("2026-02-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-01T00:00:00Z");
		TimesheetSubmissionReminderJobRequestBodyDto value = new TimesheetSubmissionReminderJobRequestBodyDto(from, to);

		// When
		boolean result = this.validator.isValid(value, this.context);

		// Then
		assertThat(result).isFalse();
	}

}
