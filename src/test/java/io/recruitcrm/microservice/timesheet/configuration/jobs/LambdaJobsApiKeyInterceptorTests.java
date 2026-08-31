package io.recruitcrm.microservice.timesheet.configuration.jobs;

import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LambdaJobsApiKeyInterceptorTests {

	private static final String REMINDER_PATH = "/v1/jobs/timesheet-submission-reminders";

	private static final String REIMBURSEMENT_REMINDER_PATH = "/v1/jobs/reimbursement-submission-reminders";

	private static final String CLEANUP_PATH = "/v1/jobs/kafka-event-logs/cleanup";

	private static final Object HANDLER = new Object();

	private final HttpServletResponse response = new MockHttpServletResponse();

	// ---- POST /v1/jobs/timesheet-submission-reminders ----

	@Test
	@DisplayName("POST submission reminders rejects when server validation key is not configured")
	void postSubmissionReminderRejectsWhenValidationKeyNotConfigured() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.postSubmissionReminderRequest(null);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Email reminder validation key is not configured");
	}

	@Test
	@DisplayName("POST submission reminders rejects when client header is null")
	void postSubmissionReminderRejectsWhenHeaderNull() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.postSubmissionReminderRequest(null);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Missing or blank x-api-key header");
	}

	@Test
	@DisplayName("POST submission reminders rejects when client header is blank")
	void postSubmissionReminderRejectsWhenHeaderBlank() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.postSubmissionReminderRequest("   ");
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Missing or blank x-api-key header");
	}

	@Test
	@DisplayName("POST submission reminders rejects when key does not match")
	void postSubmissionReminderRejectsWhenKeyMismatch() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.postSubmissionReminderRequest("wrong");
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Invalid API key");
	}

	@Test
	@DisplayName("POST submission reminders succeeds when key matches")
	void postSubmissionReminderSucceedsWhenKeyMatches() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.postSubmissionReminderRequest("secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	@Test
	@DisplayName("GET submission reminders path rejects when x-api-key is missing")
	void getOnSubmissionReminderPathRejectsWhenHeaderMissing() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", REMINDER_PATH);
		request.setRequestURI(REMINDER_PATH);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Missing or blank x-api-key header");
	}

	@Test
	@DisplayName("GET submission reminders path succeeds when x-api-key matches")
	void getOnSubmissionReminderPathSucceedsWhenKeyMatches() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", REMINDER_PATH);
		request.setRequestURI(REMINDER_PATH);
		request.addHeader("x-api-key", "secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	@Test
	@DisplayName("POST submission reminders with servlet context path normalizes URI")
	void postSubmissionReminderNormalizesContextPath() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", REMINDER_PATH);
		request.setContextPath("/timesheet-api");
		request.setRequestURI("/timesheet-api" + REMINDER_PATH);
		request.addHeader("x-api-key", "secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	// ---- POST /v1/jobs/reimbursement-submission-reminders ----

	@Test
	@DisplayName("POST reimbursement submission reminders rejects when server validation key is not configured")
	void postReimbursementSubmissionReminderRejectsWhenValidationKeyNotConfigured() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("", "", "cleanup-secret");
		MockHttpServletRequest request = this.postReimbursementSubmissionReminderRequest(null);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Reimbursement email reminder validation key is not configured");
	}

	@Test
	@DisplayName("POST reimbursement submission reminders succeeds when key matches")
	void postReimbursementSubmissionReminderSucceedsWhenKeyMatches() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "reimbursement-secret",
				"cleanup-secret");
		MockHttpServletRequest request = this.postReimbursementSubmissionReminderRequest("reimbursement-secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	// ---- DELETE /v1/jobs/kafka-event-logs/cleanup ----

	@Test
	@DisplayName("DELETE kafka cleanup rejects when server validation key is not configured")
	void deleteKafkaCleanupRejectsWhenValidationKeyNotConfigured() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "reimbursement-secret", "");
		MockHttpServletRequest request = this.deleteKafkaCleanupRequest(null);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Kafka event log cleanup validation key is not configured");
	}

	@Test
	@DisplayName("DELETE kafka cleanup rejects when client header is null")
	void deleteKafkaCleanupRejectsWhenHeaderNull() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.deleteKafkaCleanupRequest(null);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Missing or blank x-api-key header");
	}

	@Test
	@DisplayName("DELETE kafka cleanup rejects when client header is blank")
	void deleteKafkaCleanupRejectsWhenHeaderBlank() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.deleteKafkaCleanupRequest("   ");
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Missing or blank x-api-key header");
	}

	@Test
	@DisplayName("DELETE kafka cleanup rejects when key does not match")
	void deleteKafkaCleanupRejectsWhenKeyMismatch() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.deleteKafkaCleanupRequest("wrong");
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Invalid API key");
	}

	@Test
	@DisplayName("DELETE kafka cleanup succeeds when key matches")
	void deleteKafkaCleanupSucceedsWhenKeyMatches() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = this.deleteKafkaCleanupRequest("cleanup-secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	@Test
	@DisplayName("POST kafka cleanup path rejects when x-api-key is missing")
	void postOnKafkaCleanupPathRejectsWhenHeaderMissing() {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", CLEANUP_PATH);
		request.setRequestURI(CLEANUP_PATH);
		assertThatThrownBy(() -> interceptor.preHandle(request, this.response, HANDLER))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage("Missing or blank x-api-key header");
	}

	@Test
	@DisplayName("DELETE kafka cleanup with servlet context path normalizes URI")
	void deleteKafkaCleanupNormalizesContextPath() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("DELETE", CLEANUP_PATH);
		request.setContextPath("/timesheet-api");
		request.setRequestURI("/timesheet-api" + CLEANUP_PATH);
		request.addHeader("x-api-key", "cleanup-secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	@Test
	@DisplayName("preHandle returns true when context path is blank and URI is used as-is")
	void preHandleWithBlankContextPathUsesRequestUri() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", REMINDER_PATH);
		request.setContextPath("   ");
		request.setRequestURI(REMINDER_PATH);
		request.addHeader("x-api-key", "secret");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	@Test
	@DisplayName("preHandle returns true for unrelated path branch")
	void unrelatedPathReturnsTrue() throws Exception {
		LambdaJobsApiKeyInterceptor interceptor = this.buildInterceptor("secret", "secret", "cleanup-secret");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/other");
		request.setRequestURI("/other");
		assertThat(interceptor.preHandle(request, this.response, HANDLER)).isTrue();
	}

	private LambdaJobsApiKeyInterceptor buildInterceptor(String reminderKey, String reimbursementReminderKey,
			String cleanupKey) {
		TimesheetSubmissionReminderJobProperties jobProps = new TimesheetSubmissionReminderJobProperties(REMINDER_PATH,
				reminderKey);
		ReimbursementSubmissionReminderJobProperties reimbursementJobProps = new ReimbursementSubmissionReminderJobProperties(
				REIMBURSEMENT_REMINDER_PATH, reimbursementReminderKey);
		KafkaEventLogCleanupJobProperties cleanupProps = new KafkaEventLogCleanupJobProperties(CLEANUP_PATH,
				cleanupKey);
		return new LambdaJobsApiKeyInterceptor(jobProps, reimbursementJobProps, cleanupProps);
	}

	private MockHttpServletRequest postReimbursementSubmissionReminderRequest(String apiKey) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", REIMBURSEMENT_REMINDER_PATH);
		request.setRequestURI(REIMBURSEMENT_REMINDER_PATH);
		if (apiKey != null) {
			request.addHeader("x-api-key", apiKey);
		}
		return request;
	}

	private MockHttpServletRequest postSubmissionReminderRequest(String apiKey) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", REMINDER_PATH);
		request.setRequestURI(REMINDER_PATH);
		if (apiKey != null) {
			request.addHeader("x-api-key", apiKey);
		}
		return request;
	}

	private MockHttpServletRequest deleteKafkaCleanupRequest(String apiKey) {
		MockHttpServletRequest request = new MockHttpServletRequest("DELETE", CLEANUP_PATH);
		request.setRequestURI(CLEANUP_PATH);
		if (apiKey != null) {
			request.addHeader("x-api-key", apiKey);
		}
		return request;
	}

}
