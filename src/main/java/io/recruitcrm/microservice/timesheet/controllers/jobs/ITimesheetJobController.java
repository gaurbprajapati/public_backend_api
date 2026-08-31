package io.recruitcrm.microservice.timesheet.controllers.jobs;

import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller contract for Lambda-triggered timesheet jobs. The {@code x-api-key} header
 * is validated by
 * {@link io.recruitcrm.microservice.timesheet.configuration.jobs.LambdaJobsApiKeyInterceptor}.
 */
public interface ITimesheetJobController {

	/**
	 * Lambda-triggered job: accepts the window and processes asynchronously (DB lookup
	 * and reminder Kafka publish). HTTP response is {@code 202 Accepted} with
	 * {@code accepted/from/to} only; matching timesheet IDs are not returned on the HTTP
	 * response.
	 * @param requestBody inclusive UTC {@code from} / {@code to} window
	 * @return wrapped {@code 202} acceptance payload
	 */
	ResponseEntity<?> fetchTimesheetIdsForSubmissionWindow(
			@Valid @RequestBody TimesheetSubmissionReminderJobRequestBodyDto requestBody);

	/**
	 * Lambda-triggered job: accepts the window and processes asynchronously (DB lookup
	 * and reimbursement reminder Kafka publish). HTTP response is {@code 202 Accepted}
	 * with {@code accepted/from/to} only; matching timesheet IDs are not returned on the
	 * HTTP response.
	 * @param requestBody inclusive UTC {@code from} / exclusive {@code to} window on
	 * reimbursement status history {@code created_on}
	 * @return wrapped {@code 202} acceptance payload
	 */
	ResponseEntity<?> fetchTimesheetIdsForReimbursementSubmissionWindow(
			@Valid @RequestBody TimesheetSubmissionReminderJobRequestBodyDto requestBody);

	/**
	 * Lambda-triggered cleanup job: enqueues asynchronous deletion of all rows in
	 * {@code cst_timesheet_kafka_event_log_t} that are older than 30 days. Uses a
	 * dedicated {@code x-api-key} bound to
	 * {@code spring.aws.lambda.kafka-event-log-cleanup.validation-key}. HTTP response is
	 * {@code 202 Accepted}; the actual deletion runs off the request thread.
	 * @return wrapped {@code 202} acceptance payload
	 */
	ResponseEntity<?> deleteOldKafkaEventLogs();

}
