package io.recruitcrm.microservice.timesheet.exceptions;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String entity, Integer id) {
		super(entity + " id " + id + " not found.");
	}

}
