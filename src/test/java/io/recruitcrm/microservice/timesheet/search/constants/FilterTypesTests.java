package io.recruitcrm.microservice.timesheet.search.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("FilterTypes Tests")
class FilterTypesTests {

	@Test
	@DisplayName("Get value should return the wire value for the enum constant")
	void testGetValueReturnsWireValue() {
		// Given and When and Then
		assertThat(FilterTypes.IS_LESS_THAN.getValue()).isEqualTo("is_lt");
		assertThat(FilterTypes.CONTAINS_AT_LEAST_ONE.getValue()).isEqualTo("contains_at_least_one");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	@DisplayName("From value should return null for null, empty or blank input")
	void testFromValueNullOrBlankInputReturnsNull(String value) {
		// Given and When
		FilterTypes result = FilterTypes.fromValue(value);

		// Then
		assertThat(result).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "is_lt", "IS_LT", "  is_mt  " })
	@DisplayName("From value should match wire value case-insensitively and trim input")
	void testFromValueMatchesWireValueCaseInsensitively(String value) {
		// Given and When
		FilterTypes result = FilterTypes.fromValue(value);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("From value should match wire value for less than filter")
	void testFromValueMatchesLessThanWireValue() {
		// Given and When
		FilterTypes result = FilterTypes.fromValue("is_lt");

		// Then
		assertThat(result).isEqualTo(FilterTypes.IS_LESS_THAN);
	}

	@Test
	@DisplayName("From value should fall back to uppercase enum name for backward compatibility")
	void testFromValueFallsBackToEnumName() {
		// Given and When
		FilterTypes result = FilterTypes.fromValue("is_less_than");

		// Then
		assertThat(result).isEqualTo(FilterTypes.IS_LESS_THAN);
	}

	@Test
	@DisplayName("From value should throw for unknown value")
	void testFromValueUnknownValueThrowsException() {
		// Given and When and Then
		assertThatThrownBy(() -> FilterTypes.fromValue("not_a_filter")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unknown filter type")
			.hasMessageContaining("is_lt");
	}

}
