package io.recruitcrm.microservice.timesheet.dto.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Request body for {@code POST /v1/jobs/timesheet-submission-reminders}. Bounds map to
 * {@code [from, to)} on approval {@code created_on} (Unix epoch seconds): {@code from} is
 * inclusive and {@code to} is exclusive; {@code from} must be strictly before {@code to}
 * ({@link ValidSubmissionReminderWindow}).
 */
@ValidSubmissionReminderWindow
public record TimesheetSubmissionReminderJobRequestBodyDto(
		@NotNull(message = "from cannot be null") @JsonProperty("from") Instant from,
		@NotNull(message = "to cannot be null") @JsonProperty("to") Instant to) {

	public TimesheetSubmissionReminderJobRequestBodyDto {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
	}

}
