package io.recruitcrm.microservice.timesheet.configuration;

import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ExternalServiceException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.apache.catalina.connector.ClientAbortException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("SentryConfiguration Tests")
class SentryConfigurationTests {

	private SentryConfiguration sentryConfiguration;

	private SentryOptions.BeforeSendCallback callback;

	@BeforeEach
	void setUp() {
		this.sentryConfiguration = new SentryConfiguration();
		this.callback = this.sentryConfiguration.beforeSendCallback();
	}

	@Test
	@DisplayName("beforeSendCallback bean is non-null")
	void testBeforeSendCallbackBeanIsNonNull() {
		assertThat(this.sentryConfiguration.beforeSendCallback()).isNotNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ResourceNotFoundException entity id constructor - drops event")
	void testBeforeSendCallbackDropsResourceNotFoundExceptionEntityId() {
		SentryEvent event = new SentryEvent(new ResourceNotFoundException("Timesheet", 3391));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ResourceNotFoundException message constructor - drops event")
	void testBeforeSendCallbackDropsResourceNotFoundExceptionMessageOnly() {
		SentryEvent event = new SentryEvent(new ResourceNotFoundException("Custom not found message"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ResourceNotFoundException with zero id - drops event")
	void testBeforeSendCallbackDropsResourceNotFoundExceptionZeroId() {
		SentryEvent event = new SentryEvent(new ResourceNotFoundException("Timesheet", 0));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ValidationErrorException - drops event")
	void testBeforeSendCallbackDropsValidationErrorException() {
		SentryEvent event = new SentryEvent(new ValidationErrorException("Invalid input"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ValidationErrorException with cause - drops event")
	void testBeforeSendCallbackDropsValidationErrorExceptionWithCause() {
		ValidationErrorException exception = new ValidationErrorException("Invalid", new IllegalStateException("x"));
		SentryEvent event = new SentryEvent(exception);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - UnauthorizedAccessException - drops event")
	void testBeforeSendCallbackDropsUnauthorizedAccessException() {
		SentryEvent event = new SentryEvent(new UnauthorizedAccessException("Unauthorized"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - UnauthorizedAccessException default constructor - drops event")
	void testBeforeSendCallbackDropsUnauthorizedAccessExceptionDefaultConstructor() {
		SentryEvent event = new SentryEvent(new UnauthorizedAccessException());

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ConflictException - drops event")
	void testBeforeSendCallbackDropsConflictException() {
		SentryEvent event = new SentryEvent(
				new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_NOT_APPROVED_FOR_FLAGS));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - wrapper with ConflictException cause - forwards event")
	void testBeforeSendCallbackForwardsWhenConflictExceptionIsOnlyCause() {
		ConflictException cause = new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_NOT_APPROVED_FOR_FLAGS);
		RuntimeException wrapper = new RuntimeException("wrapped", cause);
		SentryEvent event = new SentryEvent(wrapper);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - HttpRequestMethodNotSupportedException - drops event")
	void testBeforeSendCallbackDropsHttpRequestMethodNotSupportedException() {
		SentryEvent event = new SentryEvent(new HttpRequestMethodNotSupportedException("GET", List.of("POST")));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - HttpRequestMethodNotSupportedException method only - drops event")
	void testBeforeSendCallbackDropsHttpRequestMethodNotSupportedExceptionMethodOnly() {
		SentryEvent event = new SentryEvent(new HttpRequestMethodNotSupportedException("DELETE"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - HttpRequestMethodNotSupportedException with empty supported list - drops event")
	void testBeforeSendCallbackDropsHttpRequestMethodNotSupportedExceptionEmptySupportedList() {
		SentryEvent event = new SentryEvent(new HttpRequestMethodNotSupportedException("GET", List.of()));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - HttpRequestMethodNotSupportedException multiple allowed - drops event")
	void testBeforeSendCallbackDropsHttpRequestMethodNotSupportedExceptionMultipleAllowed() {
		SentryEvent event = new SentryEvent(
				new HttpRequestMethodNotSupportedException("PUT", List.of("GET", "POST", "PATCH")));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - MethodArgumentNotValidException - drops event")
	void testBeforeSendCallbackDropsMethodArgumentNotValidException() {
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		SentryEvent event = new SentryEvent(exception);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - NoResourceFoundException - drops event")
	void testBeforeSendCallbackDropsNoResourceFoundException() {
		NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET,
				"/v1/timesheets/50376/time-logssw");
		SentryEvent event = new SentryEvent(exception);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - MethodArgumentTypeMismatchException - drops event")
	void testBeforeSendCallbackDropsMethodArgumentTypeMismatchException() {
		MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
		SentryEvent event = new SentryEvent(exception);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - HttpMessageNotReadableException - drops event")
	void testBeforeSendCallbackDropsHttpMessageNotReadableException() {
		MockHttpInputMessage httpInputMessage = new MockHttpInputMessage(new byte[0]);
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
				"Required request body is missing: public org.springframework.http.ResponseEntity<?> "
						+ "io.recruitcrm.microservice.timesheet.controllers.timesheet_logs.TimesheetLogsController"
						+ ".getContractorBulkTimeLogs(io.recruitcrm.microservice.timesheet.dto.time_log.bulk"
						+ ".BulkTimeLogRequestBodyDto)",
				null, httpInputMessage);
		SentryEvent event = new SentryEvent(exception);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - wrapper with HttpMessageNotReadableException cause - forwards event")
	void testBeforeSendCallbackForwardsWhenHttpMessageNotReadableIsOnlyCause() {
		MockHttpInputMessage httpInputMessage = new MockHttpInputMessage(new byte[0]);
		HttpMessageNotReadableException cause = new HttpMessageNotReadableException("Invalid request body", null,
				httpInputMessage);
		RuntimeException wrapper = new RuntimeException("wrapped", cause);
		SentryEvent event = new SentryEvent(wrapper);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - wrapper with ResourceNotFoundException cause - forwards event")
	void testBeforeSendCallbackForwardsWhenResourceNotFoundIsOnlyCause() {
		ResourceNotFoundException cause = new ResourceNotFoundException("Timesheet", 1);
		RuntimeException wrapper = new RuntimeException("wrapped", cause);
		SentryEvent event = new SentryEvent(wrapper);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - wrapper with ValidationErrorException cause - forwards event")
	void testBeforeSendCallbackForwardsWhenValidationErrorIsOnlyCause() {
		ValidationErrorException cause = new ValidationErrorException("bad");
		RuntimeException wrapper = new RuntimeException("wrapped", cause);
		SentryEvent event = new SentryEvent(wrapper);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - wrapper with HttpRequestMethodNotSupportedException cause - forwards event")
	void testBeforeSendCallbackForwardsWhenMethodNotSupportedIsOnlyCause() {
		HttpRequestMethodNotSupportedException cause = new HttpRequestMethodNotSupportedException("TRACE",
				List.of("POST"));
		RuntimeException wrapper = new RuntimeException("wrapped", cause);
		SentryEvent event = new SentryEvent(wrapper);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - consecutive filtered events both dropped")
	void testBeforeSendCallbackDropsConsecutiveFilteredEvents() {
		SentryEvent first = new SentryEvent(new ResourceNotFoundException("X", 1));
		SentryEvent second = new SentryEvent(new HttpRequestMethodNotSupportedException("GET", List.of("POST")));

		assertThat(this.callback.execute(first, new Hint())).isNull();
		assertThat(this.callback.execute(second, new Hint())).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - other exception - forwards event")
	void testBeforeSendCallbackForwardsOtherException() {
		SentryEvent event = new SentryEvent(new ExternalServiceException("Service down"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - ExternalServiceException with cause - forwards event")
	void testBeforeSendCallbackForwardsExternalServiceExceptionWithCause() {
		IOException cause = new IOException("connection reset");
		SentryEvent event = new SentryEvent(new ExternalServiceException("Billing failed", cause));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - ExternalServiceException four-arg constructor with cause - forwards event")
	void testBeforeSendCallbackForwardsExternalServiceExceptionFourArgWithCause() {
		IOException cause = new IOException("upstream");
		SentryEvent event = new SentryEvent(new ExternalServiceException("Payments", "charge", "refused", cause));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - AssertionError - forwards event")
	void testBeforeSendCallbackForwardsAssertionError() {
		SentryEvent event = new SentryEvent(new AssertionError("invariant failed"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - TimeoutException - forwards event")
	void testBeforeSendCallbackForwardsTimeoutException() {
		SentryEvent event = new SentryEvent(new TimeoutException("deadline exceeded"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - SecurityException - forwards event")
	void testBeforeSendCallbackForwardsSecurityException() {
		SentryEvent event = new SentryEvent(new SecurityException("denied"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - IllegalStateException - forwards event")
	void testBeforeSendCallbackForwardsIllegalStateException() {
		SentryEvent event = new SentryEvent(new IllegalStateException("invalid state"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - NullPointerException - forwards event")
	void testBeforeSendCallbackForwardsNullPointerException() {
		SentryEvent event = new SentryEvent(new NullPointerException("npe"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - RuntimeException - forwards event")
	void testBeforeSendCallbackForwardsRuntimeException() {
		SentryEvent event = new SentryEvent(new RuntimeException("Unexpected error"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - null throwable - forwards event")
	void testBeforeSendCallbackForwardsNullThrowable() {
		SentryEvent event = new SentryEvent();

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

	@Test
	@DisplayName("beforeSendCallback - AsyncRequestNotUsableException - drops event")
	void testBeforeSendCallbackDropsAsyncRequestNotUsableException() {
		IOException cause = new IOException("Broken pipe");
		AsyncRequestNotUsableException exception = new AsyncRequestNotUsableException(
				"ServletOutputStream failed to write: java.io.IOException: Broken pipe", cause);
		SentryEvent event = new SentryEvent(exception);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - ClientAbortException - drops event")
	void testBeforeSendCallbackDropsClientAbortException() {
		SentryEvent event = new SentryEvent(new ClientAbortException("Broken pipe"));

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("beforeSendCallback - wrapper with AsyncRequestNotUsableException cause - forwards event")
	void testBeforeSendCallbackForwardsWhenAsyncRequestNotUsableIsCause() {
		AsyncRequestNotUsableException cause = new AsyncRequestNotUsableException("Response not usable",
				new IOException("Broken pipe"));
		RuntimeException wrapper = new RuntimeException("wrapped", cause);
		SentryEvent event = new SentryEvent(wrapper);

		SentryEvent result = this.callback.execute(event, new Hint());

		assertThat(result).isSameAs(event);
	}

}
