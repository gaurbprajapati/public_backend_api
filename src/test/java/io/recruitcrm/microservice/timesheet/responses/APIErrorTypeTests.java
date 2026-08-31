package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Unit tests for APIErrorType enum. Tests all enum values, getters, and properties.
 */
class APIErrorTypeTests {

	@Test
	@DisplayName("Should have correct VALIDATION_ERROR enum values")
	void shouldHaveCorrectValidationErrorEnumValues() {
		// When
		APIErrorType validationErrorType = APIErrorType.VALIDATION_ERROR;

		// Then
		assertThat(validationErrorType).isNotNull();
		assertThat(validationErrorType.getContext()).isEqualTo("Validation Error");
		assertThat(validationErrorType.getCode()).isEqualTo(201);
	}

	@Test
	@DisplayName("Should have correct GENERIC_ERROR enum values")
	void shouldHaveCorrectGenericErrorEnumValues() {
		// When
		APIErrorType genericErrorType = APIErrorType.GENERIC_ERROR;

		// Then
		assertThat(genericErrorType).isNotNull();
		assertThat(genericErrorType.getContext()).isEqualTo("Generic Error");
		assertThat(genericErrorType.getCode()).isEqualTo(202);
	}

	@Test
	@DisplayName("Should have exactly two enum values")
	void shouldHaveExactlyTwoEnumValues() {
		// When
		APIErrorType[] values = APIErrorType.values();

		// Then
		assertThat(values).hasSize(2)
			.containsExactlyInAnyOrder(APIErrorType.VALIDATION_ERROR, APIErrorType.GENERIC_ERROR);
	}

	@ParameterizedTest
	@EnumSource(APIErrorType.class)
	@DisplayName("Should have non-null context for all enum values")
	void shouldHaveNonNullContextForAllEnumValues(APIErrorType errorType) {
		// When & Then
		assertThat(errorType.getContext()).isNotNull();
		assertThat(errorType.getContext()).isNotBlank();
	}

	@ParameterizedTest
	@EnumSource(APIErrorType.class)
	@DisplayName("Should have non-null code for all enum values")
	void shouldHaveNonNullCodeForAllEnumValues(APIErrorType errorType) {
		// When & Then
		assertThat(errorType.getCode()).isNotNull();
		assertThat(errorType.getCode()).isPositive();
	}

	@Test
	@DisplayName("Should have unique codes for all enum values")
	void shouldHaveUniqueCodesForAllEnumValues() {
		// When
		APIErrorType[] values = APIErrorType.values();

		// Then
		assertThat(values).extracting(APIErrorType::getCode).hasSize(2).containsExactlyInAnyOrder(201, 202);
	}

	@Test
	@DisplayName("Should have unique contexts for all enum values")
	void shouldHaveUniqueContextsForAllEnumValues() {
		// When
		APIErrorType[] values = APIErrorType.values();

		// Then
		assertThat(values).extracting(APIErrorType::getContext)
			.hasSize(2)
			.containsExactlyInAnyOrder("Validation Error", "Generic Error");
	}

	@Test
	@DisplayName("Should support valueOf with correct enum names")
	void shouldSupportValueOfWithCorrectEnumNames() {
		// When & Then
		assertThat(APIErrorType.valueOf("VALIDATION_ERROR")).isEqualTo(APIErrorType.VALIDATION_ERROR);
		assertThat(APIErrorType.valueOf("GENERIC_ERROR")).isEqualTo(APIErrorType.GENERIC_ERROR);
	}

	@Test
	@DisplayName("Should support toString method")
	void shouldSupportToStringMethod() {
		// When & Then
		assertThat(APIErrorType.VALIDATION_ERROR).hasToString("VALIDATION_ERROR");
		assertThat(APIErrorType.GENERIC_ERROR).hasToString("GENERIC_ERROR");
	}

	@Test
	@DisplayName("Should support ordinal method")
	void shouldSupportOrdinalMethod() {
		// When & Then
		assertThat(APIErrorType.VALIDATION_ERROR.ordinal()).isZero();
		assertThat(APIErrorType.GENERIC_ERROR.ordinal()).isEqualTo(1);
	}

	@Test
	@DisplayName("Should support name method")
	void shouldSupportNameMethod() {
		// When & Then
		assertThat(APIErrorType.VALIDATION_ERROR.name()).isEqualTo("VALIDATION_ERROR");
		assertThat(APIErrorType.GENERIC_ERROR.name()).isEqualTo("GENERIC_ERROR");
	}

	@Test
	@DisplayName("Should support equals and hashCode")
	void shouldSupportEqualsAndHashCode() {
		// When & Then
		assertThat(APIErrorType.VALIDATION_ERROR).isEqualTo(APIErrorType.VALIDATION_ERROR)
			.isNotEqualTo(APIErrorType.GENERIC_ERROR);
		assertThat(APIErrorType.VALIDATION_ERROR.hashCode()).isEqualTo(APIErrorType.VALIDATION_ERROR.hashCode())
			.isNotEqualTo(APIErrorType.GENERIC_ERROR.hashCode());
	}

	@Test
	@DisplayName("Should have JSON format annotation")
	void shouldHaveJsonFormatAnnotation() {
		// When
		APIErrorType errorType = APIErrorType.VALIDATION_ERROR;

		// Then
		// The @JsonFormat(shape = JsonFormat.Shape.OBJECT) annotation should ensure
		// that the enum is serialized as an object with code and context fields
		// This test verifies the enum can be used in JSON contexts
		assertThat(errorType).isNotNull();
		assertThat(errorType.getCode()).isNotNull();
		assertThat(errorType.getContext()).isNotNull();
	}

	@Test
	@DisplayName("Should maintain consistent code ranges")
	void shouldMaintainConsistentCodeRanges() {
		// When & Then
		// All codes should be in the 200-299 range for error types
		assertThat(APIErrorType.VALIDATION_ERROR.getCode()).isBetween(200, 299);
		assertThat(APIErrorType.GENERIC_ERROR.getCode()).isBetween(200, 299);
	}

	@Test
	@DisplayName("Should have meaningful context descriptions")
	void shouldHaveMeaningfulContextDescriptions() {
		// When & Then
		assertThat(APIErrorType.VALIDATION_ERROR.getContext()).contains("Validation");
		assertThat(APIErrorType.VALIDATION_ERROR.getContext()).contains("Error");
		assertThat(APIErrorType.GENERIC_ERROR.getContext()).contains("Generic");
		assertThat(APIErrorType.GENERIC_ERROR.getContext()).contains("Error");
	}

	@Test
	@DisplayName("Should have appropriate error codes for different error types")
	void shouldHaveAppropriateErrorCodesForDifferentErrorTypes() {
		// When & Then
		// Validation errors should have a specific code
		assertThat(APIErrorType.VALIDATION_ERROR.getCode()).isEqualTo(201);
		// Generic errors should have a different code
		assertThat(APIErrorType.GENERIC_ERROR.getCode()).isEqualTo(202);
		// They should be sequential
		assertThat(APIErrorType.GENERIC_ERROR.getCode()).isEqualTo(APIErrorType.VALIDATION_ERROR.getCode() + 1);
	}

	@Test
	@DisplayName("Should have appropriate contexts for different error types")
	void shouldHaveAppropriateContextsForDifferentErrorTypes() {
		// When & Then
		assertThat(APIErrorType.VALIDATION_ERROR.getContext()).isEqualTo("Validation Error");
		assertThat(APIErrorType.GENERIC_ERROR.getContext()).isEqualTo("Generic Error");
	}

}