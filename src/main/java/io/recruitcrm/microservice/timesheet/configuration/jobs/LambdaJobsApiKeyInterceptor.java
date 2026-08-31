package io.recruitcrm.microservice.timesheet.configuration.jobs;

import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Validates the {@code x-api-key} header for Lambda-triggered {@code /v1/jobs} endpoints
 * before controller handling.
 */
@Component
@EnableConfigurationProperties({ TimesheetSubmissionReminderJobProperties.class,
		ReimbursementSubmissionReminderJobProperties.class, KafkaEventLogCleanupJobProperties.class })
public class LambdaJobsApiKeyInterceptor implements HandlerInterceptor {

	private static final String X_API_KEY_HEADER = "x-api-key";

	private final TimesheetSubmissionReminderJobProperties jobProperties;

	private final ReimbursementSubmissionReminderJobProperties reimbursementJobProperties;

	private final KafkaEventLogCleanupJobProperties cleanupJobProperties;

	public LambdaJobsApiKeyInterceptor(TimesheetSubmissionReminderJobProperties jobProperties,
			ReimbursementSubmissionReminderJobProperties reimbursementJobProperties,
			KafkaEventLogCleanupJobProperties cleanupJobProperties) {
		this.jobProperties = jobProperties;
		this.reimbursementJobProperties = reimbursementJobProperties;
		this.cleanupJobProperties = cleanupJobProperties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String path = this.normalizedPath(request);
		if (this.jobProperties.protectedPath().equals(path)) {
			this.verifyXApiKey(this.jobProperties.validationKey(), request.getHeader(X_API_KEY_HEADER),
					"Email reminder validation key is not configured");
		}
		else if (this.reimbursementJobProperties.protectedPath().equals(path)) {
			this.verifyXApiKey(this.reimbursementJobProperties.validationKey(), request.getHeader(X_API_KEY_HEADER),
					"Reimbursement email reminder validation key is not configured");
		}
		else if (this.cleanupJobProperties.protectedPath().equals(path)) {
			this.verifyXApiKey(this.cleanupJobProperties.validationKey(), request.getHeader(X_API_KEY_HEADER),
					"Kafka event log cleanup validation key is not configured");
		}
		return true;
	}

	private String normalizedPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (StringUtils.hasText(contextPath) && uri.startsWith(contextPath)) {
			return uri.substring(contextPath.length());
		}
		return uri;
	}

	/**
	 * Constant-time comparison of the configured key and {@code x-api-key}.
	 */
	private void verifyXApiKey(String configuredKey, String providedKey, String notConfiguredMessage) {
		if (!StringUtils.hasText(configuredKey)) {
			throw new UnauthorizedAccessException(notConfiguredMessage);
		}
		if (!StringUtils.hasText(providedKey)) {
			throw new UnauthorizedAccessException("Missing or blank x-api-key header");
		}
		byte[] expected = configuredKey.getBytes(StandardCharsets.UTF_8);
		byte[] actual = providedKey.getBytes(StandardCharsets.UTF_8);
		if (!MessageDigest.isEqual(expected, actual)) {
			throw new UnauthorizedAccessException("Invalid API key");
		}
	}

}
