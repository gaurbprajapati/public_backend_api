package io.recruitcrm.microservice.timesheet.exceptions;

public class UnauthorizedAccessException extends RuntimeException {

	private static final String UNAUTHORIZED_ACCESS = "Unauthorized";

	public UnauthorizedAccessException() {
		super(UNAUTHORIZED_ACCESS);
	}

	public UnauthorizedAccessException(String message) {
		super(message);
	}

}
