package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChargeMethodType Tests")
class ChargeMethodTypeTests {

	@Test
	@DisplayName("fromId - with valid MULTIPLIER id")
	void testFromIdWithValidMultiplierId() {
		// Act
		ChargeMethodType result = ChargeMethodType.fromId(1);

		// Assert
		assertThat(result).isEqualTo(ChargeMethodType.MULTIPLIER);
	}

	@Test
	@DisplayName("fromId - with valid FIXED_RATE id")
	void testFromIdWithValidFixedRateId() {
		// Act
		ChargeMethodType result = ChargeMethodType.fromId(2);

		// Assert
		assertThat(result).isEqualTo(ChargeMethodType.FIXED_RATE);
	}

	@Test
	@DisplayName("fromId - with invalid id throws exception")
	void testFromIdWithInvalidIdThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> ChargeMethodType.fromId(999)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid ChargeType id: 999");
	}

	@Test
	@DisplayName("fromId - with negative id throws exception")
	void testFromIdWithNegativeIdThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> ChargeMethodType.fromId(-1)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid ChargeType id: -1");
	}

	@Test
	@DisplayName("fromId - with zero id throws exception")
	void testFromIdWithZeroIdThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> ChargeMethodType.fromId(0)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid ChargeType id: 0");
	}

	@Test
	@DisplayName("Enum values - verify all values exist")
	void testEnumValuesVerifyAllValuesExist() {
		// Act
		ChargeMethodType[] values = ChargeMethodType.values();

		// Assert
		assertThat(values).hasSize(2).contains(ChargeMethodType.MULTIPLIER).contains(ChargeMethodType.FIXED_RATE);
	}

	@Test
	@DisplayName("Enum valueOf - with valid string")
	void testValueOfWithValidString() {
		// Act
		ChargeMethodType result = ChargeMethodType.valueOf("MULTIPLIER");

		// Assert
		assertThat(result).isEqualTo(ChargeMethodType.MULTIPLIER);
	}

	@Test
	@DisplayName("Enum valueOf - with invalid string throws exception")
	void testValueOfWithInvalidStringThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> ChargeMethodType.valueOf("INVALID")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Getter method - verify id values")
	void testGetterMethodVerifyIdValues() {
		// Assert
		assertThat(ChargeMethodType.MULTIPLIER.getId()).isEqualTo(1);
		assertThat(ChargeMethodType.FIXED_RATE.getId()).isEqualTo(2);
	}

}