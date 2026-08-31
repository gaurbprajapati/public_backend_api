package io.recruitcrm.microservice.timesheet.exceptions;

public class ExternalServiceException extends RuntimeException {

	public ExternalServiceException(String message) {
		super(message);
	}

	public ExternalServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExternalServiceException(String serviceName, String operation, String details) {
		super("External service '" + serviceName + "' " + operation + " failed: " + details);
	}

	public ExternalServiceException(String serviceName, String operation, String details, Throwable cause) {
		super("External service '" + serviceName + "' " + operation + " failed: " + details, cause);
	}

}