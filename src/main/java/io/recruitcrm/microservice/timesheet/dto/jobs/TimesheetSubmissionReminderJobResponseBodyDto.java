package io.recruitcrm.microservice.timesheet.dto.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public record TimesheetSubmissionReminderJobResponseBodyDto(@JsonProperty("timesheetIds") List<Integer> timesheetIds) {

	public TimesheetSubmissionReminderJobResponseBodyDto {
		Objects.requireNonNull(timesheetIds, "timesheetIds");
	}

}
