package io.recruitcrm.microservice.timesheet.dto.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Response body when {@code POST /v1/jobs/timesheet-submission-reminders} accepts work
 * for asynchronous processing (no {@code timesheetIds} in the HTTP response).
 */
public record TimesheetSubmissionReminderJobAcceptedBodyDto(@JsonProperty("accepted") boolean accepted,
		@JsonProperty("from") Instant from, @JsonProperty("to") Instant to) {

	public TimesheetSubmissionReminderJobAcceptedBodyDto {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
	}

}
