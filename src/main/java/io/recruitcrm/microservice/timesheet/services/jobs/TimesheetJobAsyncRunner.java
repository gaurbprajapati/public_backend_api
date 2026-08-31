package io.recruitcrm.microservice.timesheet.services.jobs;

import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.configuration.jobs.TimesheetJobAsyncConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Runs {@link ITimesheetJobService} work off the request thread so Lambda callers get an
 * immediate HTTP response.
 */
@Component
public class TimesheetJobAsyncRunner {

	private final ITimesheetJobService timesheetJobService;

	private final Logger logger;

	public TimesheetJobAsyncRunner(ITimesheetJobService timesheetJobService,
			@Qualifier(LoggerConfiguration.ASYNC_CONTEXT_LOGGER) Logger logger) {
		this.timesheetJobService = timesheetJobService;
		this.logger = logger;
	}

	@Async(TimesheetJobAsyncConfiguration.TIMESHEET_JOB_EXECUTOR)
	public void processSubmissionReminderWindow(Instant from, Instant to) {
		try {
			this.logger.logDebug("Processing timesheet submission reminder window; from=" + from + " to=" + to);
			this.timesheetJobService.findTimesheetIdsForSubmissionReminderWindow(from, to);
			this.logger
				.logDebug("Timesheet submission reminder window processed successfully; from=" + from + " to=" + to);
		}
		catch (RuntimeException ex) {
			this.logger.logError(
					"Timesheet submission reminder job failed; from=" + from + " to=" + to + ": " + ex.getMessage());
		}
	}

	@Async(TimesheetJobAsyncConfiguration.TIMESHEET_JOB_EXECUTOR)
	public void processReimbursementSubmissionReminderWindow(Instant from, Instant to) {
		try {
			this.logger.logDebug("Processing reimbursement submission reminder window; from=" + from + " to=" + to);
			this.timesheetJobService.findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);
			this.logger.logDebug(
					"Reimbursement submission reminder window processed successfully; from=" + from + " to=" + to);
		}
		catch (RuntimeException ex) {
			this.logger.logError("Reimbursement submission reminder job failed; from=" + from + " to=" + to + ": "
					+ ex.getMessage());
		}
	}

	@Async(TimesheetJobAsyncConfiguration.TIMESHEET_JOB_EXECUTOR)
	public void processKafkaEventLogCleanup() {
		try {
			this.logger.logInfo("Processing Kafka event log cleanup");
			int deleted = this.timesheetJobService.deleteOldKafkaEventLogs().deletedCount();
			this.logger.logInfo("Kafka event log cleanup async job finished; deletedCount=" + deleted);
		}
		catch (RuntimeException ex) {
			this.logger.logError("Kafka event log cleanup job failed: " + ex.getMessage());
		}
	}

}
