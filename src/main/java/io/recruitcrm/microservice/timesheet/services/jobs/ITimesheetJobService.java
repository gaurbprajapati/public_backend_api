package io.recruitcrm.microservice.timesheet.services.jobs;

import io.recruitcrm.microservice.timesheet.dto.jobs.KafkaEventLogCleanupResponseDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobResponseBodyDto;
import java.time.Instant;

public interface ITimesheetJobService {

	TimesheetSubmissionReminderJobResponseBodyDto findTimesheetIdsForSubmissionReminderWindow(Instant from, Instant to);

	TimesheetSubmissionReminderJobResponseBodyDto findTimesheetIdsForReimbursementSubmissionReminderWindow(Instant from,
			Instant to);

	/**
	 * Deletes all rows in {@code cst_timesheet_kafka_event_log_t} whose
	 * {@code created_on} Unix timestamp is older than 30 days from now.
	 * @return DTO containing the number of rows deleted
	 */
	KafkaEventLogCleanupResponseDto deleteOldKafkaEventLogs();

}
