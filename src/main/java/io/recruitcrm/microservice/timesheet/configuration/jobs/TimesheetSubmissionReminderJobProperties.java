package io.recruitcrm.microservice.timesheet.configuration.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the Lambda-triggered timesheet submission reminder job
 * ({@code x-api-key} header validated by {@link LambdaJobsApiKeyInterceptor}).
 */
@ConfigurationProperties(prefix = "spring.aws.lambda.email-reminder")
public record TimesheetSubmissionReminderJobProperties(
		@DefaultValue("/v1/jobs/timesheet-submission-reminders") String protectedPath,
		@DefaultValue("") String validationKey) {
}
