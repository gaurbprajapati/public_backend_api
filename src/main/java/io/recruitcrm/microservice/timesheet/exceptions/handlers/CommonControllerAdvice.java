package io.recruitcrm.microservice.timesheet.exceptions.handlers;

import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ExternalServiceException;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

@ControllerAdvice
public class CommonControllerAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonControllerAdvice.class);

	private static final String UNKNOWN = "unknown";

	private final APIResponder apiResponder;

	public CommonControllerAdvice(APIResponder apiResponder) {
		this.apiResponder = apiResponder;
	}

	private void logException(Exception exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		LOGGER.error("exception occurred inside {}() method :: {}", extractControllerMethodName(exception), stackTrace);
	}

	private String extractControllerMethodName(Exception exception) {
		String methodName = methodNameFromParameter(exception);
		if (methodName != null) {
			return methodName;
		}
		StackTraceElement[] frames = exception.getStackTrace();
		if (frames == null) {
			return UNKNOWN;
		}
		for (StackTraceElement frame : frames) {
			String cls = frame.getClassName();
			if (cls.startsWith("io.recruitcrm.microservice.timesheet.controllers.")
					|| cls.startsWith("io.recruitcrm.microservice.timesheet.rule_engine.")) {
				return frame.getMethodName();
			}
		}
		return UNKNOWN;
	}

	private String methodNameFromParameter(Exception exception) {
		try {
			Method getParameter = exception.getClass().getMethod("getParameter");
			if (!MethodParameter.class.isAssignableFrom(getParameter.getReturnType())) {
				return null;
			}
			MethodParameter parameter = (MethodParameter) getParameter.invoke(exception);
			if (parameter != null) {
				Method method = parameter.getMethod();
				if (method != null) {
					return method.getName();
				}
			}
		}
		catch (ReflectiveOperationException ignored) {
			// exception doesn't expose a MethodParameter; fall back to the stack-trace
			// scan
		}
		return null;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<APIErrorResponse> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException exception) {
		logException(exception);
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.findFirst()
			.map((error) -> error.getDefaultMessage())
			.orElse("Validation failed");
		APIErrorResponse response = new APIErrorResponse(null, message, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ValidationErrorException.class)
	public ResponseEntity<APIErrorResponse> handleValidationErrorException(ValidationErrorException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<APIErrorResponse> handleNotFoundException(ResourceNotFoundException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<APIErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<APIErrorResponse> handleConflictException(ConflictException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ForbiddenAccessException.class)
	public ResponseEntity<APIErrorResponse> handleForbiddenAccessException(ForbiddenAccessException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(ExternalServiceException.class)
	public ResponseEntity<APIErrorResponse> handleExternalServiceException(ExternalServiceException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<APIErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
		logException(exception);
		return this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<APIErrorResponse> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException exception) {
		logException(exception);
		String paramName = exception.getName();
		String invalidValue = (exception.getValue() != null) ? exception.getValue().toString() : "null";
		String requiredType = (exception.getRequiredType() != null) ? exception.getRequiredType().getSimpleName()
				: UNKNOWN;
		String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", invalidValue,
				paramName, requiredType);
		APIErrorResponse response = new APIErrorResponse(null, message, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<APIErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
		logException(exception);
		String exceptionMessage = exception.getMessage();
		String message;
		if ((exceptionMessage != null) && exceptionMessage.contains("Required request body is missing")) {
			message = "Request body is required";
		}
		else {
			message = this.resolveInvalidFormatMessage(exception.getCause());
		}
		APIErrorResponse response = new APIErrorResponse(null, message, APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	private String resolveInvalidFormatMessage(Throwable cause) {
		InvalidFormatException invalidFormatException = this.findInvalidFormatException(cause);
		if (invalidFormatException == null) {
			return "Invalid request body";
		}
		List<JsonMappingException.Reference> path = invalidFormatException.getPath();
		if ((path == null) || path.isEmpty()) {
			return "Invalid request body";
		}
		String fieldName = path.get(path.size() - 1).getFieldName();
		Object invalidValue = invalidFormatException.getValue();
		String invalidValueString = (invalidValue != null) ? invalidValue.toString() : "null";
		Class<?> targetType = invalidFormatException.getTargetType();
		if (BigDecimal.class.equals(targetType)) {
			return String.format("Invalid value '%s' for field '%s'. Expected a valid number", invalidValueString,
					fieldName);
		}
		if (Boolean.class.equals(targetType)) {
			return String.format("Invalid value '%s' for field '%s'. Expected a valid boolean", invalidValueString,
					fieldName);
		}
		if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
			return String.format("Invalid value '%s' for field '%s'. Expected a valid integer between %d and %d",
					invalidValueString, fieldName, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		String requiredType = (targetType != null) ? targetType.getSimpleName() : UNKNOWN;
		return String.format("Invalid value '%s' for field '%s'. Expected type: %s", invalidValueString, fieldName,
				requiredType);
	}

	private InvalidFormatException findInvalidFormatException(Throwable cause) {
		Throwable current = cause;
		while (current != null) {
			if (current instanceof InvalidFormatException invalidFormatException) {
				return invalidFormatException;
			}
			current = current.getCause();
		}
		return null;
	}

	@ExceptionHandler(Exception.class)
	public void handleException(Exception exception) throws Exception {
		logException(exception);
		throw exception;
	}

}
