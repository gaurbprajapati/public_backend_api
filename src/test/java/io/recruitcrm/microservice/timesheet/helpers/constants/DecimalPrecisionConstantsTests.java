package io.recruitcrm.microservice.timesheet.helpers.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DecimalPrecisionConstants Tests")
class DecimalPrecisionConstantsTests {

	@Test
	@DisplayName("Constants should expose expected precision and rounding values")
	void testConstantsExposeExpectedValues() {
		// Given and When and Then
		assertThat(DecimalPrecisionConstants.MONETARY_DECIMAL_SCALE).isEqualTo(2);
		assertThat(DecimalPrecisionConstants.MONETARY_ROUNDING_MODE).isEqualTo(RoundingMode.HALF_UP);
		assertThat(DecimalPrecisionConstants.PERCENTAGE_DECIMAL_SCALE).isEqualTo(4);
		assertThat(DecimalPrecisionConstants.PERCENTAGE_ROUNDING_MODE).isEqualTo(RoundingMode.HALF_UP);
		assertThat(DecimalPrecisionConstants.GENERAL_DECIMAL_SCALE).isEqualTo(3);
		assertThat(DecimalPrecisionConstants.GENERAL_ROUNDING_MODE).isEqualTo(RoundingMode.HALF_UP);
	}

	@Test
	@DisplayName("Constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<DecimalPrecisionConstants> constructor = DecimalPrecisionConstants.class.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

}
