package io.recruitcrm.microservice.timesheet.controllers.jobs;

import io.recruitcrm.microservice.timesheet.dto.jobs.KafkaEventLogCleanupAcceptedDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobAcceptedBodyDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.jobs.TimesheetJobAsyncRunner;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/jobs")
public class TimesheetJobController implements ITimesheetJobController {

	private final TimesheetJobAsyncRunner timesheetJobAsyncRunner;

	private final APIResponder apiResponder;

	public TimesheetJobController(TimesheetJobAsyncRunner timesheetJobAsyncRunner, APIResponder apiResponder) {
		this.timesheetJobAsyncRunner = timesheetJobAsyncRunner;
		this.apiResponder = apiResponder;
	}

	/** {@inheritDoc} */
	@Override
	@PostMapping("/timesheet-submission-reminders")
	public ResponseEntity<?> fetchTimesheetIdsForSubmissionWindow(
			@Valid @RequestBody TimesheetSubmissionReminderJobRequestBodyDto requestBody) {
		this.timesheetJobAsyncRunner.processSubmissionReminderWindow(requestBody.from(), requestBody.to());
		TimesheetSubmissionReminderJobAcceptedBodyDto accepted = new TimesheetSubmissionReminderJobAcceptedBodyDto(true,
				requestBody.from(), requestBody.to());
		return this.apiResponder.respond(accepted,
				"Timesheet submission reminder job accepted; IDs are resolved and notified asynchronously",
				APIResponseType.SUCCESS, HttpStatus.ACCEPTED);
	}

	/** {@inheritDoc} */
	@Override
	@PostMapping("/reimbursement-submission-reminders")
	public ResponseEntity<?> fetchTimesheetIdsForReimbursementSubmissionWindow(
			@Valid @RequestBody TimesheetSubmissionReminderJobRequestBodyDto requestBody) {
		this.timesheetJobAsyncRunner.processReimbursementSubmissionReminderWindow(requestBody.from(), requestBody.to());
		TimesheetSubmissionReminderJobAcceptedBodyDto accepted = new TimesheetSubmissionReminderJobAcceptedBodyDto(true,
				requestBody.from(), requestBody.to());
		return this.apiResponder.respond(accepted,
				"Reimbursement submission reminder job accepted; IDs are resolved and notified asynchronously",
				APIResponseType.SUCCESS, HttpStatus.ACCEPTED);
	}

	/** {@inheritDoc} */
	@Override
	@DeleteMapping("/kafka-event-logs/cleanup")
	public ResponseEntity<?> deleteOldKafkaEventLogs() {
		this.timesheetJobAsyncRunner.processKafkaEventLogCleanup();
		return this.apiResponder.respond(new KafkaEventLogCleanupAcceptedDto(true),
				"Kafka event log cleanup accepted; records older than 30 days will be deleted asynchronously",
				APIResponseType.SUCCESS, HttpStatus.ACCEPTED);
	}

}
