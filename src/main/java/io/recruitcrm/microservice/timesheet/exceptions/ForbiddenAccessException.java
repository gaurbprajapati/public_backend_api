package io.recruitcrm.microservice.timesheet.exceptions;

public class ForbiddenAccessException extends RuntimeException {

	public ForbiddenAccessException(String message) {
		super(message);
	}

}
