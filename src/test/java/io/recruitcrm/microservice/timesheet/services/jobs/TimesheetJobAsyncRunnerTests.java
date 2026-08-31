package io.recruitcrm.microservice.timesheet.services.jobs;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dto.jobs.KafkaEventLogCleanupResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimesheetJobAsyncRunnerTests {

	@Mock
	private ITimesheetJobService timesheetJobService;

	@Mock
	private Logger logger;

	@InjectMocks
	private TimesheetJobAsyncRunner runner;

	@Test
	@DisplayName("processSubmissionReminderWindow delegates to service")
	void processDelegatesToService() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		this.runner.processSubmissionReminderWindow(from, to);
		Mockito.verify(this.timesheetJobService).findTimesheetIdsForSubmissionReminderWindow(from, to);
	}

	@Test
	@DisplayName("processSubmissionReminderWindow swallows service failures after handling")
	void processSwallowsRuntimeExceptionFromService() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		Mockito.doThrow(new IllegalStateException("boom"))
			.when(this.timesheetJobService)
			.findTimesheetIdsForSubmissionReminderWindow(from, to);
		this.runner.processSubmissionReminderWindow(from, to);
		Mockito.verify(this.timesheetJobService).findTimesheetIdsForSubmissionReminderWindow(from, to);
	}

	@Test
	@DisplayName("processReimbursementSubmissionReminderWindow delegates to service")
	void processReimbursementDelegatesToService() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		this.runner.processReimbursementSubmissionReminderWindow(from, to);
		Mockito.verify(this.timesheetJobService).findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);
	}

	@Test
	@DisplayName("processReimbursementSubmissionReminderWindow swallows service failures after handling")
	void processReimbursementSwallowsRuntimeExceptionFromService() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		Mockito.doThrow(new IllegalStateException("boom"))
			.when(this.timesheetJobService)
			.findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);
		this.runner.processReimbursementSubmissionReminderWindow(from, to);
		Mockito.verify(this.timesheetJobService).findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);
	}

	@Test
	@DisplayName("processKafkaEventLogCleanup delegates to service and logs deleted count")
	void processKafkaEventLogCleanupDelegatesToService() {
		Mockito.when(this.timesheetJobService.deleteOldKafkaEventLogs())
			.thenReturn(new KafkaEventLogCleanupResponseDto(15));
		this.runner.processKafkaEventLogCleanup();
		Mockito.verify(this.timesheetJobService).deleteOldKafkaEventLogs();
		verify(this.logger).logInfo(contains("Kafka event log cleanup async job finished; deletedCount=15"));
	}

	@Test
	@DisplayName("processKafkaEventLogCleanup swallows service failures")
	void processKafkaEventLogCleanupSwallowsRuntimeException() {
		Mockito.doThrow(new IllegalStateException("db error")).when(this.timesheetJobService).deleteOldKafkaEventLogs();
		this.runner.processKafkaEventLogCleanup();
		Mockito.verify(this.timesheetJobService).deleteOldKafkaEventLogs();
	}

}
