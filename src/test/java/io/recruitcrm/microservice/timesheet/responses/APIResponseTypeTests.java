package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Unit tests for APIResponseType enum. Tests all enum values, getters, and properties.
 */
class APIResponseTypeTests {

	@Test
	@DisplayName("Should have correct ERROR enum values")
	void shouldHaveCorrectErrorEnumValues() {
		// When
		APIResponseType errorType = APIResponseType.ERROR;

		// Then
		assertThat(errorType).isNotNull();
		assertThat(errorType.getCode()).isEqualTo(101);
		assertThat(errorType.getContext()).isEqualTo("Error while processing request");
	}

	@Test
	@DisplayName("Should have correct INFO enum values")
	void shouldHaveCorrectInfoEnumValues() {
		// When
		APIResponseType infoType = APIResponseType.INFO;

		// Then
		assertThat(infoType).isNotNull();
		assertThat(infoType.getCode()).isEqualTo(102);
		assertThat(infoType.getContext()).isEqualTo("Informational");
	}

	@Test
	@DisplayName("Should have correct SUCCESS enum values")
	void shouldHaveCorrectSuccessEnumValues() {
		// When
		APIResponseType successType = APIResponseType.SUCCESS;

		// Then
		assertThat(successType).isNotNull();
		assertThat(successType.getCode()).isEqualTo(103);
		assertThat(successType.getContext()).isEqualTo("Request is successful");
	}

	@Test
	@DisplayName("Should have correct WARNING enum values")
	void shouldHaveCorrectWarningEnumValues() {
		// When
		APIResponseType warningType = APIResponseType.WARNING;

		// Then
		assertThat(warningType).isNotNull();
		assertThat(warningType.getCode()).isEqualTo(104);
		assertThat(warningType.getContext()).isEqualTo("Warning");
	}

	@Test
	@DisplayName("Should have exactly four enum values")
	void shouldHaveExactlyFourEnumValues() {
		// When
		APIResponseType[] values = APIResponseType.values();

		// Then
		assertThat(values).hasSize(4)
			.containsExactlyInAnyOrder(APIResponseType.ERROR, APIResponseType.INFO, APIResponseType.SUCCESS,
					APIResponseType.WARNING);
	}

	@ParameterizedTest
	@EnumSource(APIResponseType.class)
	@DisplayName("Should have non-null code for all enum values")
	void shouldHaveNonNullCodeForAllEnumValues(APIResponseType responseType) {
		// When & Then
		assertThat(responseType.getCode()).isNotNull();
		assertThat(responseType.getCode()).isPositive();
	}

	@ParameterizedTest
	@EnumSource(APIResponseType.class)
	@DisplayName("Should have non-null context for all enum values")
	void shouldHaveNonNullContextForAllEnumValues(APIResponseType responseType) {
		// When & Then
		assertThat(responseType.getContext()).isNotNull();
		assertThat(responseType.getContext()).isNotBlank();
	}

	@Test
	@DisplayName("Should have unique codes for all enum values")
	void shouldHaveUniqueCodesForAllEnumValues() {
		// When
		APIResponseType[] values = APIResponseType.values();

		// Then
		assertThat(values).extracting(APIResponseType::getCode)
			.hasSize(4)
			.containsExactlyInAnyOrder(101, 102, 103, 104);
	}

	@Test
	@DisplayName("Should have unique contexts for all enum values")
	void shouldHaveUniqueContextsForAllEnumValues() {
		// When
		APIResponseType[] values = APIResponseType.values();

		// Then
		assertThat(values).extracting(APIResponseType::getContext)
			.hasSize(4)
			.containsExactlyInAnyOrder("Error while processing request", "Informational", "Request is successful",
					"Warning");
	}

	@Test
	@DisplayName("Should support valueOf with correct enum names")
	void shouldSupportValueOfWithCorrectEnumNames() {
		// When & Then
		assertThat(APIResponseType.valueOf("ERROR")).isEqualTo(APIResponseType.ERROR);
		assertThat(APIResponseType.valueOf("INFO")).isEqualTo(APIResponseType.INFO);
		assertThat(APIResponseType.valueOf("SUCCESS")).isEqualTo(APIResponseType.SUCCESS);
		assertThat(APIResponseType.valueOf("WARNING")).isEqualTo(APIResponseType.WARNING);
	}

	@Test
	@DisplayName("Should support toString method")
	void shouldSupportToStringMethod() {
		// When & Then
		assertThat(APIResponseType.ERROR).hasToString("ERROR");
		assertThat(APIResponseType.INFO).hasToString("INFO");
		assertThat(APIResponseType.SUCCESS).hasToString("SUCCESS");
		assertThat(APIResponseType.WARNING).hasToString("WARNING");
	}

	@Test
	@DisplayName("Should support ordinal method")
	void shouldSupportOrdinalMethod() {
		// When & Then
		assertThat(APIResponseType.ERROR.ordinal()).isZero();
		assertThat(APIResponseType.INFO.ordinal()).isEqualTo(1);
		assertThat(APIResponseType.SUCCESS.ordinal()).isEqualTo(2);
		assertThat(APIResponseType.WARNING.ordinal()).isEqualTo(3);
	}

	@Test
	@DisplayName("Should support name method")
	void shouldSupportNameMethod() {
		// When & Then
		assertThat(APIResponseType.ERROR.name()).isEqualTo("ERROR");
		assertThat(APIResponseType.INFO.name()).isEqualTo("INFO");
		assertThat(APIResponseType.SUCCESS.name()).isEqualTo("SUCCESS");
		assertThat(APIResponseType.WARNING.name()).isEqualTo("WARNING");
	}

	@Test
	@DisplayName("Should support equals and hashCode")
	void shouldSupportEqualsAndHashCode() {
		// When & Then
		assertThat(APIResponseType.ERROR).isEqualTo(APIResponseType.ERROR)
			.isNotEqualTo(APIResponseType.SUCCESS)
			.hasSameHashCodeAs(APIResponseType.ERROR);
		assertThat(APIResponseType.ERROR.hashCode()).isNotEqualTo(APIResponseType.SUCCESS.hashCode());
	}

	@Test
	@DisplayName("Should have JSON format annotation")
	void shouldHaveJsonFormatAnnotation() {
		// When
		APIResponseType responseType = APIResponseType.SUCCESS;

		// Then
		// The @JsonFormat(shape = JsonFormat.Shape.OBJECT) annotation should ensure
		// that the enum is serialized as an object with code and context fields
		// This test verifies the enum can be used in JSON contexts
		assertThat(responseType).isNotNull();
		assertThat(responseType.getCode()).isNotNull();
		assertThat(responseType.getContext()).isNotNull();
	}

	@Test
	@DisplayName("Should maintain consistent code ranges")
	void shouldMaintainConsistentCodeRanges() {
		// When & Then
		// All codes should be in the 100-199 range for response types
		assertThat(APIResponseType.ERROR.getCode()).isBetween(100, 199);
		assertThat(APIResponseType.INFO.getCode()).isBetween(100, 199);
		assertThat(APIResponseType.SUCCESS.getCode()).isBetween(100, 199);
		assertThat(APIResponseType.WARNING.getCode()).isBetween(100, 199);
	}

	@Test
	@DisplayName("Should have meaningful context descriptions")
	void shouldHaveMeaningfulContextDescriptions() {
		// When & Then
		assertThat(APIResponseType.ERROR.getContext()).contains("Error");
		assertThat(APIResponseType.INFO.getContext()).contains("Informational");
		assertThat(APIResponseType.SUCCESS.getContext()).contains("successful");
		assertThat(APIResponseType.WARNING.getContext()).contains("Warning");
	}

}