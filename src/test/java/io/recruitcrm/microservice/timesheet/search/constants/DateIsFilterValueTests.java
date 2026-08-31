package io.recruitcrm.microservice.timesheet.search.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("DateIsFilterValue Tests")
class DateIsFilterValueTests {

	@Test
	@DisplayName("Get label should return the wire label for the enum constant")
	void testGetLabelReturnsWireLabel() {
		// Given and When and Then
		assertThat(DateIsFilterValue.TODAY.getLabel()).isEqualTo("today");
		assertThat(DateIsFilterValue.LAST_365.getLabel()).isEqualTo("last_365");
	}

	@Test
	@DisplayName("From value should return null for null input")
	void testFromValueNullInputReturnsNull() {
		// Given and When
		DateIsFilterValue result = DateIsFilterValue.fromValue(null);

		// Then
		assertThat(result).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "today", "TODAY", "this_week" })
	@DisplayName("From value should match label case-insensitively")
	void testFromValueMatchesLabelCaseInsensitively(String value) {
		// Given and When
		DateIsFilterValue result = DateIsFilterValue.fromValue(value);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("From value should resolve to expected constant")
	void testFromValueResolvesToExpectedConstant() {
		// Given and When
		DateIsFilterValue result = DateIsFilterValue.fromValue("last_month");

		// Then
		assertThat(result).isEqualTo(DateIsFilterValue.LAST_MONTH);
	}

	@Test
	@DisplayName("From value should throw for invalid value")
	void testFromValueInvalidValueThrowsException() {
		// Given and When and Then
		assertThatThrownBy(() -> DateIsFilterValue.fromValue("never")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Invalid value type");
	}

}
