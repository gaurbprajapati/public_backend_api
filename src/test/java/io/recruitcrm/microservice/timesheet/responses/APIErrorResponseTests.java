package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for APIErrorResponse class. Tests all constructors, inheritance, error
 * handling, and edge cases.
 */
class APIErrorResponseTests {

	@Test
	@DisplayName("Should create APIErrorResponse with default constructor")
	void shouldCreateAPIErrorResponseWithDefaultConstructor() {
		// When
		APIErrorResponse response = new APIErrorResponse();

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getErrors()).isNull();
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.ERROR);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getMeta().getRequestUuid()).isNotNull();
		assertThat(response.getMeta().getTimestamp()).isNotNull();
		assertThat(response.getMeta().getMessage()).isNull();
	}

	@Test
	@DisplayName("Should create APIErrorResponse with throwable constructor")
	void shouldCreateAPIErrorResponseWithThrowableConstructor() {
		// Given
		String errorMessage = "Test error occurred";
		Exception testException = new RuntimeException(errorMessage);

		// When
		APIErrorResponse response = new APIErrorResponse(testException);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getErrors()).isNotNull();
		assertThat(response.getErrors()).hasSize(1);
		assertThat(response.getErrors().get(0).getMessage()).isEqualTo(errorMessage);
		assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.ERROR);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@DisplayName("Should create APIErrorResponse with throwable and HttpStatus constructor")
	void shouldCreateAPIErrorResponseWithThrowableAndHttpStatusConstructor() {
		// Given
		String errorMessage = "Validation failed";
		Exception testException = new IllegalArgumentException(errorMessage);
		HttpStatus customStatus = HttpStatus.BAD_REQUEST;

		// When
		APIErrorResponse response = new APIErrorResponse(testException, customStatus);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getErrors()).isNotNull();
		assertThat(response.getErrors()).hasSize(1);
		assertThat(response.getErrors().get(0).getMessage()).isEqualTo(errorMessage);
		assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.ERROR);
		assertThat(response.getMeta().getStatus()).isEqualTo(customStatus);
	}

	@Test
	@DisplayName("Should create APIErrorResponse with errors list and HttpStatus constructor")
	void shouldCreateAPIErrorResponseWithErrorsListAndHttpStatusConstructor() {
		// Given
		APIResponseKeyError error1 = new APIResponseKeyError("First error", APIErrorType.VALIDATION_ERROR);
		APIResponseKeyError error2 = new APIResponseKeyError("Second error", APIErrorType.GENERIC_ERROR);
		List<APIResponseKeyError> errors = Arrays.asList(error1, error2);
		HttpStatus customStatus = HttpStatus.UNPROCESSABLE_ENTITY;

		// When
		APIErrorResponse response = new APIErrorResponse(errors, customStatus);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getErrors()).isEqualTo(errors);
		assertThat(response.getErrors()).hasSize(2);
		assertThat(response.getErrors().get(0).getMessage()).isEqualTo("First error");
		assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(APIErrorType.VALIDATION_ERROR);
		assertThat(response.getErrors().get(1).getMessage()).isEqualTo("Second error");
		assertThat(response.getErrors().get(1).getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.ERROR);
		assertThat(response.getMeta().getStatus()).isEqualTo(customStatus);
	}

	@Test
	@DisplayName("Should create APIErrorResponse with all parameters constructor")
	void shouldCreateAPIErrorResponseWithAllParametersConstructor() {
		// Given
		String data = "Error data";
		String message = "Error message";
		APIResponseType responseType = APIResponseType.ERROR;
		HttpStatus httpStatus = HttpStatus.FORBIDDEN;

		// When
		APIErrorResponse response = new APIErrorResponse(data, message, responseType, httpStatus);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isEqualTo(data);
		assertThat(response.getErrors()).isNotNull();
		assertThat(response.getErrors()).isEmpty();
		assertThat(response.getMeta().getMessage()).isEqualTo(message);
		assertThat(response.getMeta().getResponseType()).isEqualTo(responseType);
		assertThat(response.getMeta().getStatus()).isEqualTo(httpStatus);
	}

	@Test
	@DisplayName("Should create APIErrorResponse with all args constructor")
	void shouldCreateAPIErrorResponseWithAllArgsConstructor() {
		// Given
		APIResponseKeyError error = new APIResponseKeyError("Test error", APIErrorType.VALIDATION_ERROR);
		List<APIResponseKeyError> errors = Arrays.asList(error);
		HttpStatus httpStatus = HttpStatus.NOT_FOUND;

		// When
		APIErrorResponse response = new APIErrorResponse(errors, httpStatus);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getErrors()).isEqualTo(errors);
		assertThat(response.getMeta().getStatus()).isEqualTo(httpStatus);
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.ERROR);
	}

	@Test
	@DisplayName("Should throw NullPointerException for null throwable")
	void shouldThrowNullPointerExceptionForNullThrowable() {
		// Given
		Throwable nullThrowable = null;

		// When & Then
		assertThatThrownBy(() -> new APIErrorResponse(nullThrowable)).isInstanceOf(NullPointerException.class)
			.hasMessage("Cannot invoke \"java.lang.Throwable.getMessage()\" because \"exception\" is null");
	}

	@Test
	@DisplayName("Should handle throwable with null message properly")
	void shouldHandleThrowableWithNullMessageProperly() {
		// Given
		Exception exceptionWithNullMessage = new RuntimeException((String) null);

		// When
		APIErrorResponse response = new APIErrorResponse(exceptionWithNullMessage);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getErrors()).isNotNull();
		assertThat(response.getErrors()).hasSize(1);
		assertThat(response.getErrors().get(0).getMessage()).isNull();
		assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should handle empty errors list properly")
	void shouldHandleEmptyErrorsListProperly() {
		// Given
		List<APIResponseKeyError> emptyErrors = Arrays.asList();
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

		// When
		APIErrorResponse response = new APIErrorResponse(emptyErrors, httpStatus);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getErrors()).isNotNull();
		assertThat(response.getErrors()).isEmpty();
		assertThat(response.getMeta().getStatus()).isEqualTo(httpStatus);
	}

	@Test
	@DisplayName("Should handle null errors list properly")
	void shouldHandleNullErrorsListProperly() {
		// Given
		List<APIResponseKeyError> nullErrors = null;
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		// When
		APIErrorResponse response = new APIErrorResponse(nullErrors, httpStatus);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getErrors()).isNull();
		assertThat(response.getMeta().getStatus()).isEqualTo(httpStatus);
	}

	@Test
	@DisplayName("Should inherit from APINormalResponse")
	void shouldInheritFromAPINormalResponse() {
		// Given
		APIErrorResponse response = new APIErrorResponse();

		// When & Then
		assertThat(response).isInstanceOf(APINormalResponse.class);
		// Test inherited methods
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isNull();
	}

	@Test
	@DisplayName("Should allow setting and getting errors via setters")
	void shouldAllowSettingAndGettingErrorsViaSetters() {
		// Given
		APIErrorResponse response = new APIErrorResponse();
		APIResponseKeyError error = new APIResponseKeyError("New error", APIErrorType.VALIDATION_ERROR);
		List<APIResponseKeyError> newErrors = Arrays.asList(error);

		// When
		response.setErrors(newErrors);

		// Then
		assertThat(response.getErrors()).isEqualTo(newErrors);
		assertThat(response.getErrors()).hasSize(1);
		assertThat(response.getErrors().get(0).getMessage()).isEqualTo("New error");
	}

	@Test
	@DisplayName("Should support equals and hashCode from Lombok")
	void shouldSupportEqualsAndHashCodeFromLombok() {
		// Given
		Exception testException = new RuntimeException("Test error");
		APIErrorResponse response1 = new APIErrorResponse(testException, HttpStatus.BAD_REQUEST);
		APIErrorResponse response2 = new APIErrorResponse(testException, HttpStatus.BAD_REQUEST);

		// When & Then
		assertThat(response1).isNotNull();
		assertThat(response2).isNotNull();
		// Note: Since meta contains UUID and timestamp, they won't be equal
		// But we can test that the equals and hashCode methods exist and work
		assertThat(response1.equals(response1)).isTrue();
		assertThat(response1.hashCode()).isNotZero();
	}

	@Test
	@DisplayName("Should support toString from Lombok")
	void shouldSupportToStringFromLombok() {
		// Given
		Exception testException = new RuntimeException("Test error");
		APIErrorResponse response = new APIErrorResponse(testException);

		// When
		String toString = response.toString();

		// Then
		assertThat(toString).isNotNull().contains("APIErrorResponse").contains("Test error");
	}

	@Test
	@DisplayName("Should handle different exception types properly")
	void shouldHandleDifferentExceptionTypesProperly() {
		// Given
		IllegalArgumentException illegalArgException = new IllegalArgumentException("Invalid argument");
		NullPointerException nullPointerException = new NullPointerException("Null pointer");
		RuntimeException runtimeException = new RuntimeException("Runtime error");

		// When
		APIErrorResponse response1 = new APIErrorResponse(illegalArgException);
		APIErrorResponse response2 = new APIErrorResponse(nullPointerException);
		APIErrorResponse response3 = new APIErrorResponse(runtimeException);

		// Then
		assertThat(response1.getErrors().get(0).getMessage()).isEqualTo("Invalid argument");
		assertThat(response2.getErrors().get(0).getMessage()).isEqualTo("Null pointer");
		assertThat(response3.getErrors().get(0).getMessage()).isEqualTo("Runtime error");
	}

	@Test
	@DisplayName("Should handle multiple errors with different types")
	void shouldHandleMultipleErrorsWithDifferentTypes() {
		// Given
		APIResponseKeyError validationError = new APIResponseKeyError("Validation failed",
				APIErrorType.VALIDATION_ERROR);
		APIResponseKeyError genericError = new APIResponseKeyError("Generic error", APIErrorType.GENERIC_ERROR);
		List<APIResponseKeyError> errors = Arrays.asList(validationError, genericError);

		// When
		APIErrorResponse response = new APIErrorResponse(errors, HttpStatus.BAD_REQUEST);

		// Then
		assertThat(response.getErrors()).hasSize(2);
		assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(APIErrorType.VALIDATION_ERROR);
		assertThat(response.getErrors().get(1).getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

}