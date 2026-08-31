package io.recruitcrm.microservice.timesheet.helpers.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.exceptions.InvalidEnumValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractorStatus Tests")
class ContractorStatusTests {

	private static Integer expectedValue(ContractorStatus status) {
		return switch (status) {
			case AVAILABLE -> Integer.valueOf(0);
			case ASSIGNED -> Integer.valueOf(1);
		};
	}

	@Test
	@DisplayName("values should contain exactly the two defined contractor statuses")
	void testValuesContainsExactlyTwoContractorStatuses() {
		// When
		ContractorStatus[] values = ContractorStatus.values();

		// Then
		assertThat(values).hasSize(2).containsExactlyInAnyOrder(ContractorStatus.AVAILABLE, ContractorStatus.ASSIGNED);
	}

	@ParameterizedTest
	@EnumSource(ContractorStatus.class)
	@DisplayName("getValue should return the configured value for each enum constant")
	void testGetValueReturnsConfiguredValueForEachEnum(ContractorStatus status) {
		// Given
		Integer expected = expectedValue(status);

		// When
		Integer result = status.getValue();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("getValue should return zero for AVAILABLE")
	void testGetValueForAvailableReturnsZero() {
		// When
		Integer result = ContractorStatus.AVAILABLE.getValue();

		// Then
		assertThat(result).isZero();
	}

	@ParameterizedTest
	@EnumSource(ContractorStatus.class)
	@DisplayName("fromValue should resolve each known value to the matching enum")
	void testFromValueWithValidValuesReturnsMatchingEnum(ContractorStatus expected) {
		// Given
		Integer value = expected.getValue();

		// When
		ContractorStatus result = ContractorStatus.fromValue(value);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(ints = { -1, 2, 99 })
	@DisplayName("fromValue should throw when value is unknown")
	void testFromValueWithUnknownValueThrowsInvalidEnumValueException(int invalidValue) {
		// Given
		Integer invalidValueInteger = Integer.valueOf(invalidValue);

		// When & Then
		assertThatThrownBy(() -> ContractorStatus.fromValue(invalidValueInteger))
			.isInstanceOf(InvalidEnumValueException.class)
			.hasMessageContaining("Invalid ContractorStatus value:")
			.hasMessageContaining(String.valueOf(invalidValue));
	}

	@ParameterizedTest
	@NullSource
	@DisplayName("fromValue should throw when value is null")
	void testFromValueWithNullThrowsInvalidEnumValueException(Integer value) {
		// When & Then
		assertThatThrownBy(() -> ContractorStatus.fromValue(value)).isInstanceOf(InvalidEnumValueException.class)
			.hasMessageContaining("Invalid ContractorStatus value:")
			.hasMessageContaining("null");
	}

}
