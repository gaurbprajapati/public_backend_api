package io.recruitcrm.microservice.timesheet.exceptions;

/**
 * Dedicated exception for file generation errors in the timesheet export system. This
 * exception is thrown when there are issues during CSV or Excel file generation.
 */
public class FileGeneratorException extends RuntimeException {

	/**
	 * Constructs a new FileGeneratorException with the specified detail message.
	 * @param message the detail message explaining the cause of the exception
	 */
	public FileGeneratorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new FileGeneratorException with the specified detail message and
	 * cause.
	 * @param message the detail message explaining the cause of the exception
	 * @param cause the underlying cause of the exception
	 */
	public FileGeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

}
