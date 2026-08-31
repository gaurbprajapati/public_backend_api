package io.recruitcrm.microservice.timesheet.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Central Kafka producer helper for the timesheet service. Uses string serializers for
 * keys and values (serialize JSON payloads to {@link String} yourself, for example with
 * {@link com.fasterxml.jackson.databind.ObjectMapper}).
 * <p>
 * Fan-out: publish to a topic once; multiple downstream services consume with distinct
 * {@code group.id} values. Consumer-side idempotency is handled by each consumer using
 * the {@code eventId} in the payload.
 */
@Component
public class KafkaProducerHelper {

	/**
	 * Writes immediately to Logback (console / file). RecruitCRM {@link Logger} buffers
	 * until HTTP {@code LoggerFilter} flushes, which does not run on Kafka threads—mirror
	 * here so operators see Kafka activity on the server console.
	 */
	private static final org.slf4j.Logger CONSOLE_LOG = LoggerFactory.getLogger(KafkaProducerHelper.class);

	/**
	 * Grep-friendly marker for server log search (e.g. {@code KAFKA_TS_NOTIFICATION}).
	 */
	private static final String LOG_TAG_KAFKA_NOTIFICATION = "KAFKA_TS_NOTIFICATION";

	private static final String LOG_EVENT_ID = " eventId=";

	private static final String LOG_TOPIC = " topic=";

	private static final String LOG_KEY = " key=";

	private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

	private final Logger logger;

	private final ObjectMapper objectMapper;

	private final String notificationTimesheetTopic;

	public KafkaProducerHelper(final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) final Logger logger, final ObjectMapper objectMapper,
			@Value("${app.kafka.topic.notification-timesheet:}") final String notificationTimesheetTopic) {
		this.kafkaTemplateProvider = kafkaTemplateProvider;
		this.logger = logger;
		this.objectMapper = objectMapper;
		this.notificationTimesheetTopic = notificationTimesheetTopic;
	}

	/**
	 * @return {@code true} when Spring has created a {@link KafkaTemplate} (typically
	 * when {@code spring.kafka.bootstrap-servers} is set).
	 */
	public boolean isKafkaConfigured() {
		return this.kafkaTemplateProvider.getIfAvailable() != null;
	}

	/**
	 * Sends a message with no key (Kafka partitioner chooses the partition).
	 */
	public CompletableFuture<SendResult<String, String>> send(final String topic, final String value) {
		return this.send(topic, null, value);
	}

	/**
	 * Sends a message with an optional key (use the same key for ordering per logical
	 * entity).
	 */
	public CompletableFuture<SendResult<String, String>> send(final String topic, final String key,
			final String value) {
		Objects.requireNonNull(topic, "topic");
		Objects.requireNonNull(value, "value");
		final KafkaTemplate<String, String> template = this.kafkaTemplateProvider.getIfAvailable();
		if (template == null) {
			this.kafkaWarn("Kafka producer is not configured; message was not sent. topic=" + topic);
			return CompletableFuture.failedFuture(
					new IllegalStateException("Kafka producer is not configured (set spring.kafka.bootstrap-servers)"));
		}
		this.kafkaInfo(this.buildLogPrefix("phase=kafka_send_start") + LOG_TOPIC + topic + LOG_KEY
				+ this.formatKeyForLog(key) + " valueLength=" + value.length());
		this.kafkaInfo(this.buildLogPrefix("phase=kafka_template_invoke") + LOG_TOPIC + topic + LOG_KEY
				+ this.formatKeyForLog(key));
		final CompletableFuture<SendResult<String, String>> future = (key != null) ? template.send(topic, key, value)
				: template.send(topic, value);
		return future.whenComplete((result, ex) -> {
			if (ex != null) {
				this.kafkaError(this.buildLogPrefix("phase=kafka_send_failed") + LOG_TOPIC + topic + LOG_KEY
						+ this.formatKeyForLog(key) + " Kafka send failed for topic " + topic + ": " + ex.getMessage());
			}
			else {
				this.logKafkaSendSuccess(topic, key, value, result);
			}
		});
	}

	public CompletableFuture<SendResult<String, String>> sendNotificationTimesheetReminder(final String value) {
		return this.sendNotificationTimesheetReminder(null, value);
	}

	public CompletableFuture<SendResult<String, String>> sendNotificationTimesheetReminder(final String key,
			final String value) {
		if (!StringUtils.hasText(this.notificationTimesheetTopic)) {
			this.kafkaWarn("app.kafka.topic.notification-timesheet is not set; message was not sent");
			return CompletableFuture
				.failedFuture(new IllegalStateException("app.kafka.topic.notification-timesheet is not configured"));
		}
		this.kafkaInfo(
				this.buildLogPrefix("phase=notification_topic_routed") + LOG_TOPIC + this.notificationTimesheetTopic
						+ LOG_KEY + this.formatKeyForLog(key) + " valueLength=" + value.length());
		return this.send(this.notificationTimesheetTopic, key, value);
	}

	/**
	 * Publishes a structured reminder payload to
	 * {@code app.kafka.topic.notification-timesheet} (e.g.
	 * {@code {env}-reminder-notification}).
	 * <p>
	 * Consumer-side idempotency should be implemented by each consumer using the
	 * {@code eventId} in the payload.
	 */
	public CompletableFuture<SendResult<String, String>> sendTimesheetReminderNotification(
			final TimesheetReminderNotificationPayloadDto payload) {
		Objects.requireNonNull(payload, "payload");
		final String eventId = payload.eventId();
		this.kafkaInfo(this.buildLogPrefix("phase=timesheet_payload_received") + LOG_EVENT_ID + eventId + " eventName="
				+ payload.eventName() + " timesheetIds=" + payload.timesheetIds() + " reimbursementIds="
				+ payload.reimbursementIds() + " accountId=" + payload.accountId() + " createdByUserTypeId="
				+ payload.createdByUserTypeId());
		try {
			final String json = this.objectMapper.writeValueAsString(payload);
			final String key = String.valueOf(payload.timesheetIds().get(0));
			this.kafkaInfo(this.buildLogPrefix("phase=timesheet_payload_serialized") + LOG_EVENT_ID + eventId
					+ " messageKey=" + key + " jsonLength=" + json.length());
			return this.sendNotificationTimesheetReminder(key, json);
		}
		catch (JsonProcessingException ex) {
			this.kafkaError(this.buildLogPrefix("phase=timesheet_payload_serialize_failed") + LOG_EVENT_ID + eventId
					+ " Failed to serialize TimesheetReminderNotificationPayloadDto: " + ex.getMessage());
			return CompletableFuture.failedFuture(ex);
		}
	}

	private String buildLogPrefix(final String phaseAndDetail) {
		return LOG_TAG_KAFKA_NOTIFICATION + " " + phaseAndDetail;
	}

	private String formatKeyForLog(final String key) {
		return (key != null) ? key : "(null)";
	}

	private void logKafkaSendSuccess(final String topic, final String key, final String value,
			final SendResult<String, String> result) {
		final RecordMetadata meta = (result != null) ? result.getRecordMetadata() : null;
		final String metaSummary;
		if (meta != null) {
			metaSummary = "partition=" + meta.partition() + " offset=" + meta.offset() + " timestamp="
					+ meta.timestamp();
		}
		else {
			metaSummary = "partition=n/a offset=n/a (metadata unavailable)";
		}
		this.kafkaInfo(this.buildLogPrefix("phase=kafka_send_success") + LOG_TOPIC + topic + LOG_KEY
				+ this.formatKeyForLog(key) + " " + metaSummary);
		this.kafkaInfo(this.buildLogPrefix("phase=kafka_event_data") + LOG_TOPIC + topic + LOG_KEY
				+ this.formatKeyForLog(key) + " eventData=" + value);
	}

	private void kafkaInfo(final String message) {
		CONSOLE_LOG.info(message);
		this.logger.logInfo(message);
	}

	private void kafkaWarn(final String message) {
		CONSOLE_LOG.warn(message);
		this.logger.logWarn(message);
	}

	private void kafkaError(final String message) {
		CONSOLE_LOG.error(message);
		this.logger.logError(message);
	}

}
