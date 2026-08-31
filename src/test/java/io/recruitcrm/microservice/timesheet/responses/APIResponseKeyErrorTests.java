package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for APIResponseKeyError class. Tests all constructors, error types, and
 * exception handling.
 */
class APIResponseKeyErrorTests {

	@Test
	@DisplayName("Should create APIResponseKeyError with default constructor")
	void shouldCreateAPIResponseKeyErrorWithDefaultConstructor() {
		// When
		APIResponseKeyError error = new APIResponseKeyError();

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isNull();
		assertThat(error.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should create APIResponseKeyError with throwable constructor")
	void shouldCreateAPIResponseKeyErrorWithThrowableConstructor() {
		// Given
		String errorMessage = "Test exception message";
		Exception testException = new RuntimeException(errorMessage);

		// When
		APIResponseKeyError error = new APIResponseKeyError(testException);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo(errorMessage);
		assertThat(error.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should create APIResponseKeyError with all args constructor")
	void shouldCreateAPIResponseKeyErrorWithAllArgsConstructor() {
		// Given
		String message = "Custom error message";
		APIErrorType errorType = APIErrorType.VALIDATION_ERROR;

		// When
		APIResponseKeyError error = new APIResponseKeyError(message, errorType);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo(message);
		assertThat(error.getErrorType()).isEqualTo(errorType);
	}

	@Test
	@DisplayName("Should throw NullPointerException for null throwable")
	void shouldThrowNullPointerExceptionForNullThrowable() {
		// Given
		Throwable nullThrowable = null;

		// When & Then
		assertThatThrownBy(() -> new APIResponseKeyError(nullThrowable)).isInstanceOf(NullPointerException.class)
			.hasMessage("Cannot invoke \"java.lang.Throwable.getMessage()\" because \"exception\" is null");
	}

	@Test
	@DisplayName("Should handle throwable with null message properly")
	void shouldHandleThrowableWithNullMessageProperly() {
		// Given
		Exception exceptionWithNullMessage = new RuntimeException((String) null);

		// When
		APIResponseKeyError error = new APIResponseKeyError(exceptionWithNullMessage);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isNull();
		assertThat(error.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should handle null message properly")
	void shouldHandleNullMessageProperly() {
		// Given
		String nullMessage = null;
		APIErrorType errorType = APIErrorType.VALIDATION_ERROR;

		// When
		APIResponseKeyError error = new APIResponseKeyError(nullMessage, errorType);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isNull();
		assertThat(error.getErrorType()).isEqualTo(errorType);
	}

	@Test
	@DisplayName("Should handle null error type properly")
	void shouldHandleNullErrorTypeProperly() {
		// Given
		String message = "Test message";
		APIErrorType nullErrorType = null;

		// When
		APIResponseKeyError error = new APIResponseKeyError(message, nullErrorType);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo(message);
		assertThat(error.getErrorType()).isNull();
	}

	@Test
	@DisplayName("Should handle different exception types properly")
	void shouldHandleDifferentExceptionTypesProperly() {
		// Given
		IllegalArgumentException illegalArgException = new IllegalArgumentException("Invalid argument");
		NullPointerException nullPointerException = new NullPointerException("Null pointer error");
		RuntimeException runtimeException = new RuntimeException("Runtime error");
		Exception genericException = new Exception("Generic exception");

		// When
		APIResponseKeyError error1 = new APIResponseKeyError(illegalArgException);
		APIResponseKeyError error2 = new APIResponseKeyError(nullPointerException);
		APIResponseKeyError error3 = new APIResponseKeyError(runtimeException);
		APIResponseKeyError error4 = new APIResponseKeyError(genericException);

		// Then
		assertThat(error1.getMessage()).isEqualTo("Invalid argument");
		assertThat(error1.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(error2.getMessage()).isEqualTo("Null pointer error");
		assertThat(error2.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(error3.getMessage()).isEqualTo("Runtime error");
		assertThat(error3.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(error4.getMessage()).isEqualTo("Generic exception");
		assertThat(error4.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should handle both error types properly")
	void shouldHandleBothErrorTypesProperly() {
		// Given
		String message1 = "Validation error message";
		String message2 = "Generic error message";

		// When
		APIResponseKeyError validationError = new APIResponseKeyError(message1, APIErrorType.VALIDATION_ERROR);
		APIResponseKeyError genericError = new APIResponseKeyError(message2, APIErrorType.GENERIC_ERROR);

		// Then
		assertThat(validationError.getMessage()).isEqualTo(message1);
		assertThat(validationError.getErrorType()).isEqualTo(APIErrorType.VALIDATION_ERROR);
		assertThat(genericError.getMessage()).isEqualTo(message2);
		assertThat(genericError.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should allow setting and getting message via setters")
	void shouldAllowSettingAndGettingMessageViaSetters() {
		// Given
		APIResponseKeyError error = new APIResponseKeyError();
		String newMessage = "Updated error message";

		// When
		error.setMessage(newMessage);

		// Then
		assertThat(error.getMessage()).isEqualTo(newMessage);
	}

	@Test
	@DisplayName("Should allow setting and getting errorType via setters")
	void shouldAllowSettingAndGettingErrorTypeViaSetters() {
		// Given
		APIResponseKeyError error = new APIResponseKeyError();
		APIErrorType newErrorType = APIErrorType.VALIDATION_ERROR;

		// When
		error.setErrorType(newErrorType);

		// Then
		assertThat(error.getErrorType()).isEqualTo(newErrorType);
	}

	@Test
	@DisplayName("Should support equals and hashCode from Lombok")
	void shouldSupportEqualsAndHashCodeFromLombok() {
		// Given
		String message = "Test error";
		APIErrorType errorType = APIErrorType.VALIDATION_ERROR;
		APIResponseKeyError error1 = new APIResponseKeyError(message, errorType);
		APIResponseKeyError error2 = new APIResponseKeyError(message, errorType);

		// When & Then
		assertThat(error1).isEqualTo(error2).hasSameHashCodeAs(error2).isEqualTo(error1);
	}

	@Test
	@DisplayName("Should support toString from Lombok")
	void shouldSupportToStringFromLombok() {
		// Given
		String message = "Test error message";
		APIErrorType errorType = APIErrorType.VALIDATION_ERROR;
		APIResponseKeyError error = new APIResponseKeyError(message, errorType);

		// When
		String toString = error.toString();

		// Then
		assertThat(toString).isNotNull().contains("APIResponseKeyError").contains(message).contains("VALIDATION_ERROR");
	}

	@Test
	@DisplayName("Should handle empty string message properly")
	void shouldHandleEmptyStringMessageProperly() {
		// Given
		String emptyMessage = "";
		APIErrorType errorType = APIErrorType.GENERIC_ERROR;

		// When
		APIResponseKeyError error = new APIResponseKeyError(emptyMessage, errorType);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo(emptyMessage);
		assertThat(error.getMessage()).isEmpty();
		assertThat(error.getErrorType()).isEqualTo(errorType);
	}

	@Test
	@DisplayName("Should handle very long error message properly")
	void shouldHandleVeryLongErrorMessageProperly() {
		// Given
		String longMessage = "Error: " + "X".repeat(1000);
		APIErrorType errorType = APIErrorType.GENERIC_ERROR;

		// When
		APIResponseKeyError error = new APIResponseKeyError(longMessage, errorType);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo(longMessage);
		assertThat(error.getMessage()).hasSize(1007); // "Error: " + 1000 X's
		assertThat(error.getErrorType()).isEqualTo(errorType);
	}

	@Test
	@DisplayName("Should handle exception with cause properly")
	void shouldHandleExceptionWithCauseProperly() {
		// Given
		Exception rootCause = new IllegalArgumentException("Root cause");
		Exception wrappedException = new RuntimeException("Wrapper exception", rootCause);

		// When
		APIResponseKeyError error = new APIResponseKeyError(wrappedException);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo("Wrapper exception");
		assertThat(error.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should handle custom exception types properly")
	void shouldHandleCustomExceptionTypesProperly() {
		// Given
		class CustomValidationException extends Exception {

			CustomValidationException(String message) {
				super(message);
			}

		}

		CustomValidationException customException = new CustomValidationException("Custom validation failed");

		// When
		APIResponseKeyError error = new APIResponseKeyError(customException);

		// Then
		assertThat(error).isNotNull();
		assertThat(error.getMessage()).isEqualTo("Custom validation failed");
		assertThat(error.getErrorType()).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

}