package io.recruitcrm.microservice.timesheet.services.jobs;

import io.recruitcrm.aws.aurora.annotation.ReaderRouteGlobalConsistency;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dao.kafka_consumer.TimesheetKafkaEventLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.jobs.KafkaEventLogCleanupResponseDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.ReimbursementSubmissionReminderWindowRowDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.ReminderNotificationEventType;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderCronWindowDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.ITimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement.ITimesheetReimbursementRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TimesheetJobService implements ITimesheetJobService {

	private static final int KAFKA_EVENT_LOG_RETENTION_DAYS = 30;

	private static final String LOG_REIMBURSEMENT_IDS_SUFFIX = " reimbursementIds=";

	private final ITimesheetApprovalRepository timesheetApprovalRepository;

	private final ITimesheetReimbursementRepository timesheetReimbursementRepository;

	private final KafkaProducerHelper kafkaProducerHelper;

	private final TimesheetKafkaEventLogJpaRepository timesheetKafkaEventLogJpaRepository;

	private final Logger logger;

	public TimesheetJobService(ITimesheetApprovalRepository timesheetApprovalRepository,
			ITimesheetReimbursementRepository timesheetReimbursementRepository, KafkaProducerHelper kafkaProducerHelper,
			TimesheetKafkaEventLogJpaRepository timesheetKafkaEventLogJpaRepository,
			@Qualifier(LoggerConfiguration.ASYNC_CONTEXT_LOGGER) Logger logger) {
		this.timesheetApprovalRepository = timesheetApprovalRepository;
		this.timesheetReimbursementRepository = timesheetReimbursementRepository;
		this.kafkaProducerHelper = kafkaProducerHelper;
		this.timesheetKafkaEventLogJpaRepository = timesheetKafkaEventLogJpaRepository;
		this.logger = logger;
	}

	@Override
	@ReaderRouteGlobalConsistency
	public TimesheetSubmissionReminderJobResponseBodyDto findTimesheetIdsForSubmissionReminderWindow(Instant from,
			Instant to) {
		this.logger.logDebug("Finding timesheet IDs for submission reminder window; from=" + from + " to=" + to);
		long fromEpoch = from.getEpochSecond();
		long toEpoch = to.getEpochSecond();
		List<Integer> ids = this.timesheetApprovalRepository.findTimesheetIdsWhereTransitionedToSubmittedInWindow(
				fromEpoch, toEpoch, ApprovalStatusEnum.SUBMITTED.getId(), ApprovalStatusEnum.OPEN.getId(),
				ApprovalStatusEnum.REJECTED.getId());
		this.logger.logDebug("Timesheet IDs for submission reminder window found; timesheetIds=" + ids);
		if (!ids.isEmpty()) {
			this.logger.logDebug("Publishing submission reminder cron event; timesheetIds=" + ids);
			this.publishSubmissionReminderCronEvent(ids, from, to);
		}
		this.logger.logDebug("Timesheet submission reminder window processed successfully; from=" + from + " to=" + to);
		return new TimesheetSubmissionReminderJobResponseBodyDto(new ArrayList<>(ids));
	}

	@Override
	@ReaderRouteGlobalConsistency
	public TimesheetSubmissionReminderJobResponseBodyDto findTimesheetIdsForReimbursementSubmissionReminderWindow(
			Instant from, Instant to) {
		this.logger
			.logDebug("Finding timesheet IDs for reimbursement submission reminder window; from=" + from + " to=" + to);
		long fromEpoch = from.getEpochSecond();
		long toEpoch = to.getEpochSecond();
		List<ReimbursementSubmissionReminderWindowRowDto> rows = this.timesheetReimbursementRepository
			.findReimbursementsWhereTransitionedToSubmittedInWindow(fromEpoch, toEpoch,
					ReimbursementConstants.STATUS_SUBMITTED, ReimbursementConstants.STATUS_REJECTED,
					ReimbursementConstants.STATUS_APPROVED);
		Set<Integer> distinctTimesheetIds = new LinkedHashSet<>();
		Set<Integer> distinctReimbursementIds = new LinkedHashSet<>();
		for (ReimbursementSubmissionReminderWindowRowDto row : rows) {
			distinctTimesheetIds.add(row.timesheetId());
			distinctReimbursementIds.add(row.reimbursementId());
		}
		List<Integer> timesheetIds = new ArrayList<>(distinctTimesheetIds);
		List<Integer> reimbursementIds = new ArrayList<>(distinctReimbursementIds);
		this.logger.logDebug("Timesheet IDs for reimbursement submission reminder window found; timesheetIds="
				+ timesheetIds + LOG_REIMBURSEMENT_IDS_SUFFIX + reimbursementIds);
		if (!timesheetIds.isEmpty()) {
			this.logger.logDebug("Publishing reimbursement submission reminder cron event; timesheetIds=" + timesheetIds
					+ LOG_REIMBURSEMENT_IDS_SUFFIX + reimbursementIds);
			this.publishReimbursementSubmissionReminderCronEvent(timesheetIds, reimbursementIds, from, to);
		}
		this.logger
			.logDebug("Reimbursement submission reminder window processed successfully; from=" + from + " to=" + to);
		return new TimesheetSubmissionReminderJobResponseBodyDto(new ArrayList<>(timesheetIds));
	}

	@Override
	@WriterRoute
	@Transactional
	public KafkaEventLogCleanupResponseDto deleteOldKafkaEventLogs() {
		int cutoffEpoch = Math
			.toIntExact(Instant.now().minus(KAFKA_EVENT_LOG_RETENTION_DAYS, ChronoUnit.DAYS).getEpochSecond());
		this.logger.logInfo("Deleting Kafka event log rows with createdOn < " + cutoffEpoch + " (older than "
				+ KAFKA_EVENT_LOG_RETENTION_DAYS + " days)");
		int deleted = this.timesheetKafkaEventLogJpaRepository.deleteAllByCreatedOnBefore(cutoffEpoch);
		this.logger.logInfo("Kafka event log cleanup deleted " + deleted + " row(s); cutoffEpoch=" + cutoffEpoch);
		return new KafkaEventLogCleanupResponseDto(deleted);
	}

	private void publishSubmissionReminderCronEvent(List<Integer> timesheetIds, Instant from, Instant to) {
		this.logger.logDebug("Publishing submission reminder cron event; timesheetIds=" + timesheetIds);
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(timesheetIds),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_REMINDER_SUBMITTED, null, null, null,
				ReminderNotificationEventType.CRON, false, true, false, null,
				new TimesheetReminderCronWindowDto(from.getEpochSecond(), to.getEpochSecond()));
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload).whenComplete((result, ex) -> {
			if (ex != null) {
				this.logger.logWarn("Timesheet submission reminder Kafka publish failed (async); timesheetIds="
						+ timesheetIds + ": " + ex.getMessage());
			}
			else {
				this.logger
					.logDebug("Submission reminder cron event published successfully; timesheetIds=" + timesheetIds);
			}
		});
	}

	private void publishReimbursementSubmissionReminderCronEvent(List<Integer> timesheetIds,
			List<Integer> reimbursementIds, Instant from, Instant to) {
		this.logger.logDebug("Publishing reimbursement submission reminder cron event; timesheetIds=" + timesheetIds
				+ LOG_REIMBURSEMENT_IDS_SUFFIX + reimbursementIds);
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(timesheetIds),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_REMINDER_SUBMITTED, null, null, null,
				ReminderNotificationEventType.CRON, false, true, false, null,
				new TimesheetReminderCronWindowDto(from.getEpochSecond(), to.getEpochSecond()),
				new ArrayList<>(reimbursementIds));
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload).whenComplete((result, ex) -> {
			if (ex != null) {
				this.logger.logWarn("Reimbursement submission reminder Kafka publish failed (async); timesheetIds="
						+ timesheetIds + ": " + ex.getMessage());
			}
			else {
				this.logger
					.logDebug("Reimbursement submission reminder cron event published successfully; timesheetIds="
							+ timesheetIds);
			}
		});
	}

}
