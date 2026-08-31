package io.recruitcrm.microservice.timesheet.configuration.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the Lambda-triggered Kafka event log cleanup job ({@code x-api-key}
 * header validated by {@link LambdaJobsApiKeyInterceptor}). Bound from
 * {@code spring.aws.lambda.kafka-event-log-cleanup.validation-key}.
 */
@ConfigurationProperties(prefix = "spring.aws.lambda.kafka-event-log-cleanup")
public record KafkaEventLogCleanupJobProperties(@DefaultValue("/v1/jobs/kafka-event-logs/cleanup") String protectedPath,
		@DefaultValue("") String validationKey) {
}
