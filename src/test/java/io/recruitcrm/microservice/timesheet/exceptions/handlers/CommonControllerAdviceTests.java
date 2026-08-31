package io.recruitcrm.microservice.timesheet.exceptions.handlers;

import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ExternalServiceException;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommonControllerAdvice Tests")
class CommonControllerAdviceTests {

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private CommonControllerAdvice commonControllerAdvice;

	private static final String ERROR_MESSAGE = "Test error message";

	private ResponseEntity<APIErrorResponse> expectedResponse;

	@BeforeEach
	void setUp() {
		APIErrorResponse errorResponse = new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR,
				HttpStatus.BAD_REQUEST);
		this.expectedResponse = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	private HttpMessageNotReadableException createHttpMessageNotReadableException(String message, Throwable cause) {
		MockHttpInputMessage httpInputMessage = new MockHttpInputMessage(new byte[0]);
		return new HttpMessageNotReadableException(message, cause, httpInputMessage);
	}

	@Test
	@DisplayName("Handle MethodArgumentNotValidException uses first field error default message")
	void testHandleMethodArgumentNotValidExceptionUsesFirstFieldErrorMessage() {
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		given(exception.getStackTrace()).willReturn(new StackTraceElement[0]);
		BindingResult bindingResult = mock(BindingResult.class);
		FieldError fieldError = new FieldError("request", "description", "must not be blank");
		given(exception.getBindingResult()).willReturn(bindingResult);
		given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError));

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleMethodArgumentNotValidException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("must not be blank");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle MethodArgumentNotValidException uses default message when no field errors")
	void testHandleMethodArgumentNotValidExceptionUsesFallbackWhenNoFieldErrors() {
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		given(exception.getStackTrace()).willReturn(new StackTraceElement[0]);
		BindingResult bindingResult = mock(BindingResult.class);
		given(exception.getBindingResult()).willReturn(bindingResult);
		given(bindingResult.getFieldErrors()).willReturn(Collections.emptyList());

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleMethodArgumentNotValidException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("Validation failed");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle validation error exception")
	void testHandleValidationErrorException() {
		ValidationErrorException exception = new ValidationErrorException(ERROR_MESSAGE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleValidationErrorException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("Handle validation error exception with cause")
	void testHandleValidationErrorExceptionWithCause() {
		IllegalArgumentException cause = new IllegalArgumentException("root");
		ValidationErrorException exception = new ValidationErrorException(ERROR_MESSAGE, cause);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleValidationErrorException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("Handle validation error exception with blank message")
	void testHandleValidationErrorExceptionBlankMessage() {
		ValidationErrorException exception = new ValidationErrorException("");
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleValidationErrorException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("Handle resource not found exception with message constructor")
	void testHandleNotFoundExceptionWithMessage() {
		ResourceNotFoundException exception = new ResourceNotFoundException(ERROR_MESSAGE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleNotFoundException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("Handle resource not found exception with blank message constructor")
	void testHandleNotFoundExceptionBlankMessage() {
		ResourceNotFoundException exception = new ResourceNotFoundException("");
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleNotFoundException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("Handle resource not found exception with entity and id constructor")
	void testHandleNotFoundExceptionWithEntityAndId() {
		ResourceNotFoundException exception = new ResourceNotFoundException("Timesheet", 62011);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleNotFoundException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("Handle unauthorized access exception with message")
	void testHandleUnauthorizedAccessExceptionWithMessage() {
		UnauthorizedAccessException exception = new UnauthorizedAccessException(ERROR_MESSAGE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.UNAUTHORIZED))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleUnauthorizedAccessException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.UNAUTHORIZED);
	}

	@Test
	@DisplayName("Handle unauthorized access exception with default constructor")
	void testHandleUnauthorizedAccessExceptionDefaultConstructor() {
		UnauthorizedAccessException exception = new UnauthorizedAccessException();
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.UNAUTHORIZED))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleUnauthorizedAccessException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.UNAUTHORIZED);
	}

	@Test
	@DisplayName("Handle conflict exception")
	void testHandleConflictException() {
		ConflictException exception = new ConflictException(
				ExceptionMessageConstants.REIMBURSEMENT_NOT_APPROVED_FOR_FLAGS);
		ResponseEntity<APIErrorResponse> conflictResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.CONFLICT),
				HttpStatus.CONFLICT);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.CONFLICT))
			.willReturn(conflictResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleConflictException(exception);

		assertThat(response).isEqualTo(conflictResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.CONFLICT);
	}

	@Test
	@DisplayName("Handle external service exception")
	void testHandleExternalServiceException() {
		ExternalServiceException exception = new ExternalServiceException("Downstream API unavailable");
		ResponseEntity<APIErrorResponse> serviceUnavailableResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE),
				HttpStatus.SERVICE_UNAVAILABLE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE))
			.willReturn(serviceUnavailableResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleExternalServiceException(exception);

		assertThat(response).isEqualTo(serviceUnavailableResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	@DisplayName("Handle external service exception with service name operation details constructor")
	void testHandleExternalServiceExceptionWithServiceDetails() {
		ExternalServiceException exception = new ExternalServiceException("Billing", "fetchInvoice", "timeout");
		ResponseEntity<APIErrorResponse> serviceUnavailableResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE),
				HttpStatus.SERVICE_UNAVAILABLE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE))
			.willReturn(serviceUnavailableResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleExternalServiceException(exception);

		assertThat(response).isEqualTo(serviceUnavailableResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	@DisplayName("Handle external service exception with four-arg constructor and cause")
	void testHandleExternalServiceExceptionWithFourArgsAndCause() {
		IOException cause = new IOException("upstream reset");
		ExternalServiceException exception = new ExternalServiceException("Payments", "charge", "declined", cause);
		ResponseEntity<APIErrorResponse> serviceUnavailableResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE),
				HttpStatus.SERVICE_UNAVAILABLE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE))
			.willReturn(serviceUnavailableResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleExternalServiceException(exception);

		assertThat(response).isEqualTo(serviceUnavailableResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	@DisplayName("Handle HTTP method not supported when GET used but only POST allowed")
	void testHandleMethodNotAllowedGetVersusPost() {
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("GET",
				List.of("POST"));
		ResponseEntity<APIErrorResponse> methodNotAllowedResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED),
				HttpStatus.METHOD_NOT_ALLOWED);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED))
			.willReturn(methodNotAllowedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleMethodNotAllowed(exception);

		assertThat(response).isEqualTo(methodNotAllowedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	@DisplayName("Handle HTTP method not supported when PATCH used but multiple methods allowed")
	void testHandleMethodNotAllowedPatchVersusGetAndPost() {
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("PATCH",
				List.of("GET", "POST"));
		ResponseEntity<APIErrorResponse> methodNotAllowedResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED),
				HttpStatus.METHOD_NOT_ALLOWED);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED))
			.willReturn(methodNotAllowedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleMethodNotAllowed(exception);

		assertThat(response).isEqualTo(methodNotAllowedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	@DisplayName("Handle HTTP method not supported when HEAD used but POST and PUT allowed")
	void testHandleMethodNotAllowedHeadVersusPostAndPut() {
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("HEAD",
				List.of("POST", "PUT"));
		ResponseEntity<APIErrorResponse> methodNotAllowedResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED),
				HttpStatus.METHOD_NOT_ALLOWED);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED))
			.willReturn(methodNotAllowedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleMethodNotAllowed(exception);

		assertThat(response).isEqualTo(methodNotAllowedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	@DisplayName("Handle HTTP method not supported when OPTIONS used but DELETE allowed")
	void testHandleMethodNotAllowedOptionsVersusDelete() {
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("OPTIONS",
				List.of("DELETE"));
		ResponseEntity<APIErrorResponse> methodNotAllowedResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED),
				HttpStatus.METHOD_NOT_ALLOWED);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED))
			.willReturn(methodNotAllowedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleMethodNotAllowed(exception);

		assertThat(response).isEqualTo(methodNotAllowedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	@DisplayName("Handle HTTP method not supported with empty supported methods list")
	void testHandleMethodNotAllowedWithEmptySupportedMethods() {
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("GET", List.of());
		ResponseEntity<APIErrorResponse> methodNotAllowedResponse = new ResponseEntity<>(
				new APIErrorResponse(ERROR_MESSAGE, null, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED),
				HttpStatus.METHOD_NOT_ALLOWED);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED))
			.willReturn(methodNotAllowedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleMethodNotAllowed(exception);

		assertThat(response).isEqualTo(methodNotAllowedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	@DisplayName("Handle MethodArgumentTypeMismatchException returns 400 with parameter details")
	void testHandleMethodArgumentTypeMismatchReturnsBadRequestWithDetails() {
		MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
		given(exception.getStackTrace()).willReturn(new StackTraceElement[0]);
		given(exception.getName()).willReturn("dealId");
		given(exception.getValue()).willReturn("60811111189013");
		doReturn(Integer.class).when(exception).getRequiredType();

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleMethodArgumentTypeMismatch(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value '60811111189013' for parameter 'dealId'. Expected type: Integer");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle MethodArgumentTypeMismatchException uses null when value is null")
	void testHandleMethodArgumentTypeMismatchUsesNullWhenValueIsNull() {
		MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
		given(exception.getStackTrace()).willReturn(new StackTraceElement[0]);
		given(exception.getName()).willReturn("dealId");
		given(exception.getValue()).willReturn(null);
		doReturn(Integer.class).when(exception).getRequiredType();

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleMethodArgumentTypeMismatch(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'null' for parameter 'dealId'. Expected type: Integer");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle MethodArgumentTypeMismatchException uses unknown when required type is null")
	void testHandleMethodArgumentTypeMismatchUsesUnknownWhenRequiredTypeIsNull() {
		MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
		given(exception.getStackTrace()).willReturn(new StackTraceElement[0]);
		given(exception.getName()).willReturn("dealId");
		given(exception.getValue()).willReturn("invalid");
		given(exception.getRequiredType()).willReturn(null);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleMethodArgumentTypeMismatch(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'invalid' for parameter 'dealId'. Expected type: unknown");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns request body required when body is missing")
	void testHandleHttpMessageNotReadableReturnsRequestBodyRequiredWhenBodyIsMissing() {
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException(
				"Required request body is missing: public org.springframework.http.ResponseEntity<?> "
						+ "io.recruitcrm.microservice.timesheet.controllers.timesheet_logs.TimesheetLogsController"
						+ ".getContractorBulkTimeLogs(io.recruitcrm.microservice.timesheet.dto.time_log.bulk"
						+ ".BulkTimeLogRequestBodyDto)",
				null);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("Request body is required");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns invalid request body for malformed JSON")
	void testHandleHttpMessageNotReadableReturnsInvalidRequestBodyForMalformedJson() {
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException(
				"JSON parse error: Unexpected character ('}' (code 125)): expected a value", null);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("Invalid request body");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns invalid request body when message is null")
	void testHandleHttpMessageNotReadableReturnsInvalidRequestBodyWhenMessageIsNull() {
		HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
		given(exception.getMessage()).willReturn(null);
		given(exception.getCause()).willReturn(null);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("Invalid request body");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns field specific message for invalid amount")
	void testHandleHttpMessageNotReadableReturnsFieldSpecificMessageForInvalidAmount() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null,
				"Cannot deserialize value of type `java.math.BigDecimal` from String \"ee\": not a valid representation",
				"ee", BigDecimal.class);
		invalidFormatException.prependPath(new CreateReimbursementRequestBodyDto(), "amount");
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException(
				"JSON parse error: Cannot deserialize value of type `java.math.BigDecimal` from String \"ee\": not a valid representation",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'ee' for field 'amount'. Expected a valid number");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns boolean message for invalid boolean field")
	void testHandleHttpMessageNotReadableReturnsBooleanMessageForInvalidBooleanField() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null,
				"Cannot deserialize value of type `java.lang.Boolean` from String \"maybe\"", "maybe", Boolean.class);
		invalidFormatException.prependPath(new CreateReimbursementRequestBodyDto(), "isActive");
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException("JSON parse error",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'maybe' for field 'isActive'. Expected a valid boolean");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns type message for unsupported field type")
	void testHandleHttpMessageNotReadableReturnsTypeMessageForUnsupportedFieldType() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null,
				"Cannot deserialize value of type `java.time.LocalDate` from String \"bad-date\"", "bad-date",
				LocalDate.class);
		invalidFormatException.prependPath(new CreateReimbursementRequestBodyDto(), "submittedOn");
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException("JSON parse error",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'bad-date' for field 'submittedOn'. Expected type: LocalDate");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns invalid request body when invalid format has no field path")
	void testHandleHttpMessageNotReadableReturnsInvalidRequestBodyWhenInvalidFormatHasNoFieldPath() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null, "Invalid format", "ee",
				BigDecimal.class);
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException("JSON parse error",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("Invalid request body");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns integer range message for out of range value")
	void testHandleHttpMessageNotReadableReturnsIntegerRangeMessageForOutOfRangeValue() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null,
				"Numeric value (39891111111111) out of range of int (-2147483648 - 2147483647)", 39891111111111L,
				Integer.class);
		invalidFormatException.prependPath(new ValidateTimesheetEmailRequestBodyDto(), "timesheetIds");
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException(
				"JSON parse error: Numeric value (39891111111111) out of range of int (-2147483648 - 2147483647)",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo(
				"Invalid value '39891111111111' for field 'timesheetIds'. Expected a valid integer between -2147483648 and 2147483647");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException resolves invalid format from nested cause")
	void testHandleHttpMessageNotReadableResolvesInvalidFormatFromNestedCause() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null, "Invalid format", "ee",
				BigDecimal.class);
		invalidFormatException.prependPath(new CreateReimbursementRequestBodyDto(), "amount");
		RuntimeException wrapper = new RuntimeException("wrapper", invalidFormatException);
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException("JSON parse error",
				wrapper);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'ee' for field 'amount'. Expected a valid number");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns invalid request body when InvalidFormatException path is null")
	void testHandleHttpMessageNotReadableReturnsInvalidRequestBodyWhenPathIsNull() {
		InvalidFormatException invalidFormatException = mock(InvalidFormatException.class);
		given(invalidFormatException.getPath()).willReturn(null);
		HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
		given(exception.getMessage()).willReturn("JSON parse error");
		given(exception.getCause()).willReturn(invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo("Invalid request body");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException uses 'null' string when InvalidFormatException value is null")
	void testHandleHttpMessageNotReadableUsesNullStringWhenValueIsNull() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null, "Invalid format", null,
				BigDecimal.class);
		invalidFormatException.prependPath(new CreateReimbursementRequestBodyDto(), "amount");
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException("JSON parse error",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'null' for field 'amount'. Expected a valid number");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns integer range message for primitive int target type")
	void testHandleHttpMessageNotReadableReturnsIntegerRangeMessageForPrimitiveIntType() {
		InvalidFormatException invalidFormatException = InvalidFormatException.from(null,
				"Numeric value out of range of int", 99999999999L, int.class);
		invalidFormatException.prependPath(new ValidateTimesheetEmailRequestBodyDto(), "timesheetIds");
		HttpMessageNotReadableException exception = this.createHttpMessageNotReadableException("JSON parse error",
				invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo(
				"Invalid value '99999999999' for field 'timesheetIds'. Expected a valid integer between -2147483648 and 2147483647");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle HttpMessageNotReadableException returns unknown type when InvalidFormatException target type is null")
	void testHandleHttpMessageNotReadableReturnsUnknownTypeWhenTargetTypeIsNull() {
		InvalidFormatException invalidFormatException = mock(InvalidFormatException.class);
		JsonMappingException.Reference ref = mock(JsonMappingException.Reference.class);
		given(invalidFormatException.getPath()).willReturn(List.of(ref));
		given(ref.getFieldName()).willReturn("someField");
		given(invalidFormatException.getValue()).willReturn("badValue");
		given(invalidFormatException.getTargetType()).willReturn(null);
		HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
		given(exception.getMessage()).willReturn("JSON parse error");
		given(exception.getCause()).willReturn(invalidFormatException);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice.handleHttpMessageNotReadable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage())
			.isEqualTo("Invalid value 'badValue' for field 'someField'. Expected type: unknown");
		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle forbidden access exception")
	void testHandleForbiddenAccessException() {
		ForbiddenAccessException exception = new ForbiddenAccessException(ERROR_MESSAGE);
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.FORBIDDEN))
			.willReturn(this.expectedResponse);

		ResponseEntity<APIErrorResponse> response = this.commonControllerAdvice
			.handleForbiddenAccessException(exception);

		assertThat(response).isEqualTo(this.expectedResponse);
		then(this.apiResponder).should().respondWithError(exception, APIResponseType.ERROR, HttpStatus.FORBIDDEN);
	}

	@Test
	@DisplayName("Handle generic exception logs the failure without rethrowing")
	void testHandleExceptionLogsWithoutRethrowing() {
		RuntimeException original = new RuntimeException("boom");

		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(original))
			.isInstanceOf(RuntimeException.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle generic exception resolves method name from a MethodParameter when available")
	void testHandleExceptionResolvesMethodNameFromMethodParameter() throws Exception {
		Method targetMethod = String.class.getMethod("trim");
		MethodParameter methodParameter = new MethodParameter(targetMethod, -1);
		BindingResult bindingResult = mock(BindingResult.class);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(exception))
			.isInstanceOf(MethodArgumentNotValidException.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle generic exception ignores getParameter that does not return a MethodParameter")
	void testHandleExceptionIgnoresNonMethodParameterGetParameter() {
		NonMethodParameterException exception = new NonMethodParameterException();
		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(exception))
			.isInstanceOf(RuntimeException.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle generic exception resolves method name from a controller stack frame")
	void testHandleExceptionResolvesMethodNameFromControllerStackFrame() {
		RuntimeException exception = new RuntimeException("boom");
		StackTraceElement controllerFrame = new StackTraceElement(
				"io.recruitcrm.microservice.timesheet.controllers.SampleController", "fetchData",
				"SampleController.java", 42);
		exception.setStackTrace(new StackTraceElement[] { controllerFrame });

		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(exception))
			.isInstanceOf(RuntimeException.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle generic exception resolves method name from a rule_engine stack frame")
	void testHandleExceptionResolvesMethodNameFromRuleEngineStackFrame() {
		RuntimeException exception = new RuntimeException("boom");
		StackTraceElement ruleEngineFrame = new StackTraceElement(
				"io.recruitcrm.microservice.timesheet.rule_engine.SomeRule", "evaluate", "SomeRule.java", 10);
		exception.setStackTrace(new StackTraceElement[] { ruleEngineFrame });

		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(exception))
			.isInstanceOf(RuntimeException.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle generic exception continues to stack trace scan when MethodParameter has no method (constructor-backed)")
	void testHandleExceptionContinuesToStackTraceWhenMethodParameterHasNullMethod() throws Exception {
		Constructor<?> ctor = String.class.getConstructor(String.class);
		MethodParameter constructorParam = new MethodParameter(ctor, 0);
		BindingResult bindingResult = mock(BindingResult.class);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(constructorParam,
				bindingResult);

		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(exception))
			.isInstanceOf(MethodArgumentNotValidException.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Handle generic exception falls back to unknown when stack trace is null")
	void testHandleExceptionFallsBackToUnknownWhenStackTraceIsNull() {
		Exception exception = mock(Exception.class);
		given(exception.getStackTrace()).willReturn(null);

		assertThatThrownBy(() -> this.commonControllerAdvice.handleException(exception)).isInstanceOf(Exception.class);

		then(this.apiResponder).shouldHaveNoInteractions();
	}

	private static final class NonMethodParameterException extends RuntimeException {

		public String getParameter() {
			return "not-a-method-parameter";
		}

	}

}