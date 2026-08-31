package io.recruitcrm.microservice.timesheet.services.jobs;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dao.kafka_consumer.TimesheetKafkaEventLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.jobs.KafkaEventLogCleanupResponseDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.ReimbursementSubmissionReminderWindowRowDto;
import io.recruitcrm.microservice.timesheet.dto.jobs.TimesheetSubmissionReminderJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.ITimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement.ITimesheetReimbursementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TimesheetJobServiceTests {

	@Mock
	private ITimesheetApprovalRepository timesheetApprovalRepository;

	@Mock
	private ITimesheetReimbursementRepository timesheetReimbursementRepository;

	@Mock
	private KafkaProducerHelper kafkaProducerHelper;

	@Mock
	private TimesheetKafkaEventLogJpaRepository timesheetKafkaEventLogJpaRepository;

	@Mock
	private Logger logger;

	@InjectMocks
	private TimesheetJobService service;

	@Test
	@DisplayName("findTimesheetIdsForSubmissionReminderWindow does not publish when no ids")
	void findDoesNotPublishKafkaWhenEmpty() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		given(this.timesheetApprovalRepository.findTimesheetIdsWhereTransitionedToSubmittedInWindow(
				from.getEpochSecond(), to.getEpochSecond(), ApprovalStatusEnum.SUBMITTED.getId(),
				ApprovalStatusEnum.OPEN.getId(), ApprovalStatusEnum.REJECTED.getId()))
			.willReturn(List.of());

		TimesheetSubmissionReminderJobResponseBodyDto result = this.service
			.findTimesheetIdsForSubmissionReminderWindow(from, to);

		assertThat(result.timesheetIds()).isEmpty();
		then(this.kafkaProducerHelper).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("findTimesheetIdsForSubmissionReminderWindow publishes when ids exist")
	void findPublishesKafkaWhenNonEmpty() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		given(this.timesheetApprovalRepository.findTimesheetIdsWhereTransitionedToSubmittedInWindow(
				from.getEpochSecond(), to.getEpochSecond(), ApprovalStatusEnum.SUBMITTED.getId(),
				ApprovalStatusEnum.OPEN.getId(), ApprovalStatusEnum.REJECTED.getId()))
			.willReturn(List.of(10, 20));
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = org.mockito.Mockito.mock(SendResult.class);
		given(this.kafkaProducerHelper
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class)))
			.willReturn(CompletableFuture.completedFuture(sendResult));

		TimesheetSubmissionReminderJobResponseBodyDto result = this.service
			.findTimesheetIdsForSubmissionReminderWindow(from, to);

		assertThat(result.timesheetIds()).containsExactly(Integer.valueOf(10), Integer.valueOf(20));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
		then(this.logger).should().logDebug(contains("Submission reminder cron event published successfully"));
	}

	@Test
	@DisplayName("deleteOldKafkaEventLogs returns count of deleted rows")
	void deleteOldKafkaEventLogsReturnDeletedCount() {
		given(this.timesheetKafkaEventLogJpaRepository.deleteAllByCreatedOnBefore(anyInt())).willReturn(42);

		KafkaEventLogCleanupResponseDto result = this.service.deleteOldKafkaEventLogs();

		assertThat(result.deletedCount()).isEqualTo(Integer.valueOf(42));
		then(this.logger).should().logInfo(contains("Kafka event log cleanup deleted 42 row(s)"));
	}

	@Test
	@DisplayName("deleteOldKafkaEventLogs returns zero when nothing to delete")
	void deleteOldKafkaEventLogsReturnsZeroWhenNothingDeleted() {
		given(this.timesheetKafkaEventLogJpaRepository.deleteAllByCreatedOnBefore(anyInt())).willReturn(0);

		KafkaEventLogCleanupResponseDto result = this.service.deleteOldKafkaEventLogs();

		assertThat(result.deletedCount()).isZero();
	}

	@Test
	@DisplayName("findTimesheetIdsForSubmissionReminderWindow completes when async Kafka publish fails")
	void findCompletesWhenAsyncKafkaPublishFails() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		given(this.timesheetApprovalRepository.findTimesheetIdsWhereTransitionedToSubmittedInWindow(
				from.getEpochSecond(), to.getEpochSecond(), ApprovalStatusEnum.SUBMITTED.getId(),
				ApprovalStatusEnum.OPEN.getId(), ApprovalStatusEnum.REJECTED.getId()))
			.willReturn(List.of(10));
		given(this.kafkaProducerHelper
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class)))
			.willReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

		TimesheetSubmissionReminderJobResponseBodyDto result = this.service
			.findTimesheetIdsForSubmissionReminderWindow(from, to);

		assertThat(result.timesheetIds()).containsExactly(Integer.valueOf(10));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
		then(this.logger).should().logWarn(contains("Timesheet submission reminder Kafka publish failed (async)"));
	}

	@Test
	@DisplayName("findTimesheetIdsForReimbursementSubmissionReminderWindow does not publish when no ids")
	void findReimbursementDoesNotPublishKafkaWhenEmpty() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		given(this.timesheetReimbursementRepository.findReimbursementsWhereTransitionedToSubmittedInWindow(
				from.getEpochSecond(), to.getEpochSecond(), ReimbursementConstants.STATUS_SUBMITTED,
				ReimbursementConstants.STATUS_REJECTED, ReimbursementConstants.STATUS_APPROVED))
			.willReturn(List.of());

		TimesheetSubmissionReminderJobResponseBodyDto result = this.service
			.findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);

		assertThat(result.timesheetIds()).isEmpty();
		then(this.kafkaProducerHelper).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("findTimesheetIdsForReimbursementSubmissionReminderWindow publishes when ids exist")
	void findReimbursementPublishesKafkaWhenNonEmpty() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		given(this.timesheetReimbursementRepository.findReimbursementsWhereTransitionedToSubmittedInWindow(
				from.getEpochSecond(), to.getEpochSecond(), ReimbursementConstants.STATUS_SUBMITTED,
				ReimbursementConstants.STATUS_REJECTED, ReimbursementConstants.STATUS_APPROVED))
			.willReturn(List.of(new ReimbursementSubmissionReminderWindowRowDto(101, 10),
					new ReimbursementSubmissionReminderWindowRowDto(202, 20)));
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = org.mockito.Mockito.mock(SendResult.class);
		given(this.kafkaProducerHelper
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class)))
			.willReturn(CompletableFuture.completedFuture(sendResult));

		TimesheetSubmissionReminderJobResponseBodyDto result = this.service
			.findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);

		assertThat(result.timesheetIds()).containsExactly(Integer.valueOf(10), Integer.valueOf(20));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(argThat((payload) -> payload.timesheetIds().containsAll(List.of(10, 20))
					&& payload.reimbursementIds().containsAll(List.of(101, 202))));
		then(this.logger).should()
			.logDebug(contains("Reimbursement submission reminder cron event published successfully"));
	}

	@Test
	@DisplayName("findTimesheetIdsForReimbursementSubmissionReminderWindow completes when async Kafka publish fails")
	void findReimbursementCompletesWhenAsyncKafkaPublishFails() {
		Instant from = Instant.parse("2026-01-01T00:00:00Z");
		Instant to = Instant.parse("2026-01-02T00:00:00Z");
		given(this.timesheetReimbursementRepository.findReimbursementsWhereTransitionedToSubmittedInWindow(
				from.getEpochSecond(), to.getEpochSecond(), ReimbursementConstants.STATUS_SUBMITTED,
				ReimbursementConstants.STATUS_REJECTED, ReimbursementConstants.STATUS_APPROVED))
			.willReturn(List.of(new ReimbursementSubmissionReminderWindowRowDto(101, 10)));
		given(this.kafkaProducerHelper
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class)))
			.willReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

		TimesheetSubmissionReminderJobResponseBodyDto result = this.service
			.findTimesheetIdsForReimbursementSubmissionReminderWindow(from, to);

		assertThat(result.timesheetIds()).containsExactly(Integer.valueOf(10));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
		then(this.logger).should().logWarn(contains("Reimbursement submission reminder Kafka publish failed (async)"));
	}

}
