package io.recruitcrm.microservice.timesheet.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResourceNotFoundException class.
 */
class ResourceNotFoundExceptionTests {

	@Test
	@DisplayName("ResourceNotFoundException - Message constructor")
	void resourceNotFoundExceptionMessageConstructor() {
		// Arrange
		String message = "Timesheet not found";

		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(message);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(message);
	}

	@Test
	@DisplayName("ResourceNotFoundException - Entity and ID constructor")
	void resourceNotFoundExceptionEntityAndIdConstructor() {
		// Arrange
		String entity = "Timesheet";
		Integer id = 123;

		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(entity, id);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Timesheet id 123 not found.");
	}

	@ParameterizedTest
	@DisplayName("ResourceNotFoundException - Edge cases with empty values")
	@CsvSource({ "'', 123, ' id 123 not found.'" })
	void resourceNotFoundExceptionEdgeCases(String entity, Integer id, String expectedMessage) {
		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(entity, id);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(expectedMessage);
	}

	@Test
	@DisplayName("ResourceNotFoundException - Null entity and null ID")
	void resourceNotFoundExceptionNullEntityAndNullId() {
		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(null, null);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("null id null not found.");
	}

	@Test
	@DisplayName("ResourceNotFoundException - Null entity with valid ID")
	void resourceNotFoundExceptionNullEntityWithValidId() {
		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(null, 123);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("null id 123 not found.");
	}

	@Test
	@DisplayName("ResourceNotFoundException - Valid entity with null ID")
	void resourceNotFoundExceptionValidEntityWithNullId() {
		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException("Timesheet", null);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Timesheet id null not found.");
	}

	@ParameterizedTest
	@DisplayName("ResourceNotFoundException - Null and empty message constructor")
	@NullAndEmptySource
	@ValueSource(strings = { "", "   " })
	void resourceNotFoundExceptionNullAndEmptyMessageConstructor(String message) {
		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(message);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo(message);
	}

	@ParameterizedTest
	@DisplayName("ResourceNotFoundException - Various ID values")
	@CsvSource({ "Timesheet, 0, 'Timesheet id 0 not found.'", "Timesheet, -1, 'Timesheet id -1 not found.'",
			"Timesheet, 2147483647, 'Timesheet id 2147483647 not found.'",
			"Timesheet, 999999999, 'Timesheet id 999999999 not found.'", "Timesheet, 1, 'Timesheet id 1 not found.'" })
	void resourceNotFoundExceptionVariousIdValues(String entity, Integer id, String expectedMessage) {
		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(entity, id);

		// Assert
		assertThat(exception).isNotNull().satisfies((ex) -> {
			assertThat(ex.getMessage()).isEqualTo(expectedMessage);
			assertThat(ex.getMessage()).contains(entity);
			assertThat(ex.getMessage()).contains(id.toString());
		});
	}

	@Test
	@DisplayName("ResourceNotFoundException - Different entity types")
	void resourceNotFoundExceptionDifferentEntityTypes() {
		// Test with different entity types
		ResourceNotFoundException timesheetException = new ResourceNotFoundException("Timesheet", 123);
		ResourceNotFoundException candidateException = new ResourceNotFoundException("Candidate", 456);
		ResourceNotFoundException jobException = new ResourceNotFoundException("Job", 789);

		// Assert
		assertThat(timesheetException.getMessage()).isEqualTo("Timesheet id 123 not found.");
		assertThat(candidateException.getMessage()).isEqualTo("Candidate id 456 not found.");
		assertThat(jobException.getMessage()).isEqualTo("Job id 789 not found.");
	}

	@Test
	@DisplayName("ResourceNotFoundException - Entity with spaces")
	void resourceNotFoundExceptionEntityWithSpaces() {
		// Arrange
		String entity = "Time Sheet";
		Integer id = 123;

		// Act
		ResourceNotFoundException exception = new ResourceNotFoundException(entity, id);

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Time Sheet id 123 not found.");
	}

}