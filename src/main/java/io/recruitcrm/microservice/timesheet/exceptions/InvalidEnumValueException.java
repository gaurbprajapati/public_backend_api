package io.recruitcrm.microservice.timesheet.exceptions;

/**
 * Exception thrown when an invalid enum value is provided. This exception is used for
 * enum value validation errors.
 */
public class InvalidEnumValueException extends ValidationErrorException {

	public InvalidEnumValueException(String enumName, Object invalidValue) {
		super("Invalid " + enumName + " value: " + invalidValue);
	}

	public InvalidEnumValueException(String enumName, Object invalidValue, Throwable cause) {
		super("Invalid " + enumName + " value: " + invalidValue, cause);
	}

}
