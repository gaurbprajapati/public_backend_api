package io.recruitcrm.microservice.timesheet.dto.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body returned after the Lambda-triggered Kafka event log cleanup job deletes
 * records older than 30 days from {@code cst_timesheet_kafka_event_log_t}.
 */
public record KafkaEventLogCleanupResponseDto(@JsonProperty("deletedCount") int deletedCount) {
}
