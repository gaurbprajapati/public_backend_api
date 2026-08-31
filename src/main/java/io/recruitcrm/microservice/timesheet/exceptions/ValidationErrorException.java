package io.recruitcrm.microservice.timesheet.exceptions;

public class ValidationErrorException extends RuntimeException {

	public ValidationErrorException(String message) {
		super(message);
	}

	public ValidationErrorException(String message, Throwable cause) {
		super(message, cause);
	}

}
