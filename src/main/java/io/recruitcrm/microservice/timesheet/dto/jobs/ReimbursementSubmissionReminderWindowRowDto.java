package io.recruitcrm.microservice.timesheet.dto.jobs;

import java.util.Objects;

public record ReimbursementSubmissionReminderWindowRowDto(Integer reimbursementId, Integer timesheetId) {

	public ReimbursementSubmissionReminderWindowRowDto {
		Objects.requireNonNull(reimbursementId, "reimbursementId");
		Objects.requireNonNull(timesheetId, "timesheetId");
	}

}
