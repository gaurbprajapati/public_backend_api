package io.recruitcrm.microservice.timesheet.configuration.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the Lambda-triggered reimbursement submission reminder job
 * ({@code x-api-key} header validated by {@link LambdaJobsApiKeyInterceptor}).
 */
@ConfigurationProperties(prefix = "spring.aws.lambda.reimbursement-email-reminder")
public record ReimbursementSubmissionReminderJobProperties(
		@DefaultValue("/v1/jobs/reimbursement-submission-reminders") String protectedPath,
		@DefaultValue("") String validationKey) {
}
