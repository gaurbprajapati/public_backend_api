package io.recruitcrm.microservice.timesheet.dto.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body when {@code DELETE /v1/jobs/kafka-event-logs/cleanup} accepts the request
 * for asynchronous processing. The actual row deletion happens in a background thread;
 * the HTTP response is returned immediately.
 */
public record KafkaEventLogCleanupAcceptedDto(@JsonProperty("accepted") boolean accepted) {
}
