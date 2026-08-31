package io.recruitcrm.microservice.timesheet.exceptions;

public class JsonReadException extends RuntimeException {

	public JsonReadException(String message, Throwable cause) {
		super(message, cause.getCause());
	}

}
