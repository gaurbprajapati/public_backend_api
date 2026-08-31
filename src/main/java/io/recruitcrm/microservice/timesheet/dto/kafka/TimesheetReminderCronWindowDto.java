package io.recruitcrm.microservice.timesheet.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * UTC bounds for a scheduled (CRON) reminder window (Unix epoch seconds), serialized
 * under {@code cronWindow} on {@link TimesheetReminderNotificationPayloadDto}.
 */
public record TimesheetReminderCronWindowDto(@JsonProperty("startTime") Long startTime,
		@JsonProperty("endTime") Long endTime) {

	public TimesheetReminderCronWindowDto {
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(endTime, "endTime");
	}

}
