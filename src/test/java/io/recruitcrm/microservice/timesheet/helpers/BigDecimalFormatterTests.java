package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.microservice.timesheet.helpers.constants.DecimalPrecisionConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BigDecimalFormatter utility class.
 */
class BigDecimalFormatterTests {

	@Test
	@DisplayName("Format monetary amount - Success")
	void formatMonetaryAmountSuccess() {
		// Arrange
		BigDecimal input = new BigDecimal("123.456789");
		BigDecimal expected = new BigDecimal("123.46");

		// Act
		BigDecimal result = BigDecimalFormatter.formatMonetaryAmount(input);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format monetary amount - Null input")
	void formatMonetaryAmountNullInput() {
		// Act
		BigDecimal result = BigDecimalFormatter.formatMonetaryAmount(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Format monetary amount - Zero value")
	void formatMonetaryAmountZeroValue() {
		// Arrange
		BigDecimal input = BigDecimal.ZERO;
		BigDecimal expected = BigDecimal.ZERO.setScale(DecimalPrecisionConstants.MONETARY_DECIMAL_SCALE,
				DecimalPrecisionConstants.MONETARY_ROUNDING_MODE);

		// Act
		BigDecimal result = BigDecimalFormatter.formatMonetaryAmount(input);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format percentage - Success")
	void formatPercentageSuccess() {
		// Arrange
		BigDecimal input = new BigDecimal("12.3456789");
		BigDecimal expected = new BigDecimal("12.3457");

		// Act
		BigDecimal result = BigDecimalFormatter.formatPercentage(input);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format percentage - Null input")
	void formatPercentageNullInput() {
		// Act
		BigDecimal result = BigDecimalFormatter.formatPercentage(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Format percentage - Zero value")
	void formatPercentageZeroValue() {
		// Arrange
		BigDecimal input = BigDecimal.ZERO;
		BigDecimal expected = BigDecimal.ZERO.setScale(DecimalPrecisionConstants.PERCENTAGE_DECIMAL_SCALE,
				DecimalPrecisionConstants.PERCENTAGE_ROUNDING_MODE);

		// Act
		BigDecimal result = BigDecimalFormatter.formatPercentage(input);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format general decimal - Success")
	void formatGeneralDecimalSuccess() {
		// Arrange
		BigDecimal input = new BigDecimal("123.456789");
		BigDecimal expected = new BigDecimal("123.457");

		// Act
		BigDecimal result = BigDecimalFormatter.formatGeneralDecimal(input);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format general decimal - Null input")
	void formatGeneralDecimalNullInput() {
		// Act
		BigDecimal result = BigDecimalFormatter.formatGeneralDecimal(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Format general decimal - Zero value")
	void formatGeneralDecimalZeroValue() {
		// Arrange
		BigDecimal input = BigDecimal.ZERO;
		BigDecimal expected = BigDecimal.ZERO.setScale(DecimalPrecisionConstants.GENERAL_DECIMAL_SCALE,
				DecimalPrecisionConstants.GENERAL_ROUNDING_MODE);

		// Act
		BigDecimal result = BigDecimalFormatter.formatGeneralDecimal(input);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format with custom precision - Success")
	void formatWithCustomPrecisionSuccess() {
		// Arrange
		BigDecimal input = new BigDecimal("123.456789");
		int scale = 3;
		RoundingMode roundingMode = RoundingMode.DOWN;
		BigDecimal expected = new BigDecimal("123.456");

		// Act
		BigDecimal result = BigDecimalFormatter.formatWithCustomPrecision(input, scale, roundingMode);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format with custom precision - Null input")
	void formatWithCustomPrecisionNullInput() {
		// Act
		BigDecimal result = BigDecimalFormatter.formatWithCustomPrecision(null, 2, RoundingMode.HALF_UP);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Format with custom precision - Different rounding modes")
	void formatWithCustomPrecisionDifferentRoundingModes() {
		// Arrange
		BigDecimal input = new BigDecimal("123.456");
		int scale = 2;

		// Act & Assert - HALF_UP
		BigDecimal resultHalfUp = BigDecimalFormatter.formatWithCustomPrecision(input, scale, RoundingMode.HALF_UP);
		assertThat(resultHalfUp).isEqualTo(new BigDecimal("123.46"));

		// Act & Assert - DOWN
		BigDecimal resultDown = BigDecimalFormatter.formatWithCustomPrecision(input, scale, RoundingMode.DOWN);
		assertThat(resultDown).isEqualTo(new BigDecimal("123.45"));

		// Act & Assert - UP
		BigDecimal resultUp = BigDecimalFormatter.formatWithCustomPrecision(input, scale, RoundingMode.UP);
		assertThat(resultUp).isEqualTo(new BigDecimal("123.46"));
	}

	@Test
	@DisplayName("Format with custom scale - Success")
	void formatWithCustomScaleSuccess() {
		// Arrange
		BigDecimal input = new BigDecimal("123.456789");
		int scale = 4;
		BigDecimal expected = new BigDecimal("123.4568");

		// Act
		BigDecimal result = BigDecimalFormatter.formatWithCustomScale(input, scale);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format with custom scale - Null input")
	void formatWithCustomScaleNullInput() {
		// Act
		BigDecimal result = BigDecimalFormatter.formatWithCustomScale(null, 2);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Format with custom scale - Zero value")
	void formatWithCustomScaleZeroValue() {
		// Arrange
		BigDecimal input = BigDecimal.ZERO;
		int scale = 3;
		BigDecimal expected = BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);

		// Act
		BigDecimal result = BigDecimalFormatter.formatWithCustomScale(input, scale);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Format with custom scale - Uses HALF_UP rounding")
	void formatWithCustomScaleUsesHalfUpRounding() {
		// Arrange
		BigDecimal input = new BigDecimal("123.456");
		int scale = 2;
		BigDecimal expected = new BigDecimal("123.46");

		// Act
		BigDecimal result = BigDecimalFormatter.formatWithCustomScale(input, scale);

		// Assert
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Private constructor throws UnsupportedOperationException")
	void privateConstructorThrowsUnsupportedOperationException() throws NoSuchMethodException {
		// Arrange
		Constructor<BigDecimalFormatter> constructor = BigDecimalFormatter.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		// Act & Assert
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

}