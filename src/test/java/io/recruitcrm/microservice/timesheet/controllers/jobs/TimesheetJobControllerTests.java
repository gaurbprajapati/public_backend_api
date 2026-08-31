package io.recruitcrm.microservice.timesheet.controllers.jobs;

import io.recruitcrm.microservice.timesheet.dto.jobs.KafkaEventLogCleanupAcceptedDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobAcceptedBodyDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.jobs.TimesheetJobAsyncRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TimesheetJobControllerTests {

	@Mock
	private TimesheetJobAsyncRunner timesheetJobAsyncRunner;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetJobController controller;

	@Test
	@DisplayName("POST submission reminders returns 202 and enqueues async work when window is valid")
	void fetchTimesheetIdsForSubmissionWindowValidRequestReturnsAccepted() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		TimesheetSubmissionReminderJobRequestBodyDto body = new TimesheetSubmissionReminderJobRequestBodyDto(from, to);
		TimesheetSubmissionReminderJobAcceptedBodyDto accepted = new TimesheetSubmissionReminderJobAcceptedBodyDto(true,
				from, to);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<TimesheetSubmissionReminderJobAcceptedBodyDto>> expected = new ResponseEntity<>(
				new APINormalResponse<>(accepted), HttpStatus.ACCEPTED);
		Mockito.doNothing().when(this.timesheetJobAsyncRunner).processSubmissionReminderWindow(from, to);
		Mockito.when(this.apiResponder.respond(accepted,
				"Timesheet submission reminder job accepted; IDs are resolved and notified asynchronously",
				APIResponseType.SUCCESS, HttpStatus.ACCEPTED))
			.thenReturn(expected);

		ResponseEntity<?> response = this.controller.fetchTimesheetIdsForSubmissionWindow(body);

		assertThat(response).isEqualTo(expected);
		Mockito.verify(this.timesheetJobAsyncRunner).processSubmissionReminderWindow(from, to);
	}

	@Test
	@DisplayName("POST reimbursement submission reminders returns 202 and enqueues async work when window is valid")
	void fetchTimesheetIdsForReimbursementSubmissionWindowValidRequestReturnsAccepted() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		TimesheetSubmissionReminderJobRequestBodyDto body = new TimesheetSubmissionReminderJobRequestBodyDto(from, to);
		TimesheetSubmissionReminderJobAcceptedBodyDto accepted = new TimesheetSubmissionReminderJobAcceptedBodyDto(true,
				from, to);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<TimesheetSubmissionReminderJobAcceptedBodyDto>> expected = new ResponseEntity<>(
				new APINormalResponse<>(accepted), HttpStatus.ACCEPTED);
		Mockito.doNothing().when(this.timesheetJobAsyncRunner).processReimbursementSubmissionReminderWindow(from, to);
		Mockito.when(this.apiResponder.respond(accepted,
				"Reimbursement submission reminder job accepted; IDs are resolved and notified asynchronously",
				APIResponseType.SUCCESS, HttpStatus.ACCEPTED))
			.thenReturn(expected);

		ResponseEntity<?> response = this.controller.fetchTimesheetIdsForReimbursementSubmissionWindow(body);

		assertThat(response).isEqualTo(expected);
		Mockito.verify(this.timesheetJobAsyncRunner).processReimbursementSubmissionReminderWindow(from, to);
	}

	@Test
	@DisplayName("DELETE kafka-event-logs/cleanup returns 202 and enqueues async cleanup")
	void deleteOldKafkaEventLogsValidKeyReturnsAccepted() {
		KafkaEventLogCleanupAcceptedDto accepted = new KafkaEventLogCleanupAcceptedDto(true);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<KafkaEventLogCleanupAcceptedDto>> expected = new ResponseEntity<>(
				new APINormalResponse<>(accepted), HttpStatus.ACCEPTED);
		Mockito.doNothing().when(this.timesheetJobAsyncRunner).processKafkaEventLogCleanup();
		Mockito.when(this.apiResponder.respond(accepted,
				"Kafka event log cleanup accepted; records older than 30 days will be deleted asynchronously",
				APIResponseType.SUCCESS, HttpStatus.ACCEPTED))
			.thenReturn(expected);

		ResponseEntity<?> response = this.controller.deleteOldKafkaEventLogs();

		assertThat(response).isEqualTo(expected);
		Mockito.verify(this.timesheetJobAsyncRunner).processKafkaEventLogCleanup();
	}

}
