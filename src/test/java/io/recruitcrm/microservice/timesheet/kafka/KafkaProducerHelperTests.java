package io.recruitcrm.microservice.timesheet.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dto.kafka.ReminderNotificationEventType;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class KafkaProducerHelperTests {

	@Mock
	private Logger logger;

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	@SuppressWarnings("unchecked")
	private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider = mock(ObjectProvider.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	private KafkaProducerHelper helperWithKafka;

	@BeforeEach
	void setUp() {
		lenient().when(this.kafkaTemplateProvider.getIfAvailable()).thenReturn(this.kafkaTemplate);
		this.helperWithKafka = new KafkaProducerHelper(this.kafkaTemplateProvider, this.logger, this.objectMapper,
				"dev-reminder-notification");
	}

	private KafkaProducerHelper newHelperWithoutKafka(final Logger logger, final ObjectMapper mapper) {
		@SuppressWarnings("unchecked")
		ObjectProvider<KafkaTemplate<String, String>> emptyProvider = mock(ObjectProvider.class);
		given(emptyProvider.getIfAvailable()).willReturn(null);
		return new KafkaProducerHelper(emptyProvider, logger, mapper, "dev-reminder-notification");
	}

	@Test
	void isKafkaConfiguredReturnsTrueWhenTemplatePresent() {
		assertThat(this.helperWithKafka.isKafkaConfigured()).isTrue();
	}

	@Test
	void isKafkaConfiguredReturnsFalseWhenTemplateAbsent() {
		assertThat(newHelperWithoutKafka(this.logger, this.objectMapper).isKafkaConfigured()).isFalse();
	}

	@Test
	void sendRequiresNonNullTopic() {
		assertThatThrownBy(() -> this.helperWithKafka.send(null, "v")).isInstanceOf(NullPointerException.class);
	}

	@Test
	void sendRequiresNonNullValue() {
		assertThatThrownBy(() -> this.helperWithKafka.send("t", null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void sendReturnsFailedFutureWhenKafkaNotConfigured() {
		CompletableFuture<SendResult<String, String>> future = newHelperWithoutKafka(this.logger, this.objectMapper)
			.send("topic", "payload");
		assertThatThrownBy(future::get).isInstanceOf(ExecutionException.class)
			.cause()
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("not configured");
		then(this.logger).should().logWarn("Kafka producer is not configured; message was not sent. topic=topic");
	}

	@Test
	void sendWithNullKeyUsesSendWithoutKey() throws Exception {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send("topic", "payload")).willReturn(CompletableFuture.completedFuture(sendResult));

		SendResult<String, String> result = this.helperWithKafka.send("topic", "payload").get();

		assertThat(result).isSameAs(sendResult);
	}

	@Test
	void sendWithKeyUsesSendWithKey() throws Exception {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send("topic", "k", "payload"))
			.willReturn(CompletableFuture.completedFuture(sendResult));

		SendResult<String, String> result = this.helperWithKafka.send("topic", "k", "payload").get();

		assertThat(result).isSameAs(sendResult);
	}

	@Test
	void sendLogsErrorWhenSendCompletesExceptionally() {
		final RuntimeException failure = new RuntimeException("broker down");
		given(this.kafkaTemplate.send(anyString(), anyString())).willReturn(CompletableFuture.failedFuture(failure));

		CompletableFuture<SendResult<String, String>> future = this.helperWithKafka.send("topic", "payload");

		assertThatThrownBy(future::join).hasCause(failure);
		then(this.logger).should()
			.logError(
					"KAFKA_TS_NOTIFICATION phase=kafka_send_failed topic=topic key=(null) Kafka send failed for topic topic: broker down");
	}

	@Test
	void sendNotificationTimesheetReminderFailsWhenTopicBlank() {
		@SuppressWarnings("unchecked")
		ObjectProvider<KafkaTemplate<String, String>> provider = mock(ObjectProvider.class);
		KafkaProducerHelper helper = new KafkaProducerHelper(provider, this.logger, this.objectMapper, "   ");
		CompletableFuture<SendResult<String, String>> future = helper.sendNotificationTimesheetReminder("x");
		assertThatThrownBy(future::get).isInstanceOf(ExecutionException.class)
			.cause()
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("notification-timesheet");
		then(this.logger).should().logWarn("app.kafka.topic.notification-timesheet is not set; message was not sent");
	}

	@Test
	void sendNotificationTimesheetReminderDelegatesToSend() throws Exception {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send("dev-reminder-notification", "payload"))
			.willReturn(CompletableFuture.completedFuture(sendResult));

		SendResult<String, String> result = this.helperWithKafka.sendNotificationTimesheetReminder("payload").get();

		assertThat(result).isSameAs(sendResult);
	}

	@Test
	void sendTimesheetReminderNotificationSerializesPayloadAndUsesTimesheetIdAsKey() throws Exception {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send(eq("dev-reminder-notification"), eq("42"), anyString()))
			.willReturn(CompletableFuture.completedFuture(sendResult));

		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				UUID.randomUUID().toString(), new ArrayList<>(List.of(42)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED, 7, 2, 3,
				ReminderNotificationEventType.REALTIME, false, true, true, null, null);

		SendResult<String, String> result = this.helperWithKafka.sendTimesheetReminderNotification(payload).get();

		assertThat(result).isSameAs(sendResult);
		then(this.logger).should()
			.logInfo(contains("eventName=" + TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED));
	}

	@Test
	void sendLogsSuccessWithMetadataWhenSendCompletes() {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		RecordMetadata metadata = mock(RecordMetadata.class);
		given(metadata.partition()).willReturn(2);
		given(metadata.offset()).willReturn(99L);
		given(metadata.timestamp()).willReturn(1234567890L);
		given(sendResult.getRecordMetadata()).willReturn(metadata);
		given(this.kafkaTemplate.send("topic", "payload")).willReturn(CompletableFuture.completedFuture(sendResult));

		this.helperWithKafka.send("topic", "payload").join();

		then(this.logger).should()
			.logInfo(contains("phase=kafka_send_success topic=topic key=(null) partition=2 offset=99"));
		then(this.logger).should().logInfo(contains("phase=kafka_event_data topic=topic key=(null) eventData=payload"));
	}

	@Test
	void sendLogsMetadataUnavailableWhenSendResultHasNullMetadata() {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(sendResult.getRecordMetadata()).willReturn(null);
		given(this.kafkaTemplate.send("topic", "payload")).willReturn(CompletableFuture.completedFuture(sendResult));

		this.helperWithKafka.send("topic", "payload").join();

		then(this.logger).should()
			.logInfo(contains(
					"phase=kafka_send_success topic=topic key=(null) partition=n/a offset=n/a (metadata unavailable)"));
	}

	@Test
	void sendNotificationTimesheetReminderWithKeyDelegatesToSend() throws Exception {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send("dev-reminder-notification", "message-key", "payload"))
			.willReturn(CompletableFuture.completedFuture(sendResult));

		SendResult<String, String> result = this.helperWithKafka
			.sendNotificationTimesheetReminder("message-key", "payload")
			.get();

		assertThat(result).isSameAs(sendResult);
	}

	@Test
	void sendTimesheetReminderNotificationRequiresNonNullPayload() {
		assertThatThrownBy(() -> this.helperWithKafka.sendTimesheetReminderNotification(null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void sendTimesheetReminderNotificationReturnsFailedFutureWhenSerializationFails() throws Exception {
		ObjectMapper failingMapper = mock(ObjectMapper.class);
		given(failingMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
			.willThrow(new JsonProcessingException("fail") {
			});
		KafkaProducerHelper helper = new KafkaProducerHelper(this.kafkaTemplateProvider, this.logger, failingMapper,
				"dev-reminder-notification");
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				UUID.randomUUID().toString(), new ArrayList<>(List.of(1)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED, 1, 2, 3,
				ReminderNotificationEventType.REALTIME, false, true, true, null, null);

		CompletableFuture<SendResult<String, String>> future = helper.sendTimesheetReminderNotification(payload);

		assertThatThrownBy(future::get).isInstanceOf(ExecutionException.class)
			.cause()
			.isInstanceOf(JsonProcessingException.class);
		then(this.logger).should().logError(contains("phase=timesheet_payload_serialize_failed"));
	}

}
