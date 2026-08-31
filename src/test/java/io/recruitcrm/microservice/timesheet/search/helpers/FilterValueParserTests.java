package io.recruitcrm.microservice.timesheet.search.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("FilterValueParser Tests")
class FilterValueParserTests {

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "\t" })
	@DisplayName("parseIntegerList should return empty list for null, blank, or whitespace values")
	void testParseIntegerListNullOrBlankReturnsEmptyList(String filterValue) {
		assertThat(FilterValueParser.parseIntegerList(filterValue)).isEmpty();
	}

	@Test
	@DisplayName("parseIntegerList should parse comma-separated integers")
	void testParseIntegerListCommaSeparated() {
		assertThat(FilterValueParser.parseIntegerList("1,2,3")).containsExactly(1, 2, 3);
		assertThat(FilterValueParser.parseIntegerList("1, 2 , 3")).containsExactly(1, 2, 3);
	}

	@Test
	@DisplayName("parseIntegerList should parse JSON array of integers")
	void testParseIntegerListJsonArrayOfIntegers() {
		assertThat(FilterValueParser.parseIntegerList("[1,2,3]")).containsExactly(1, 2, 3);
		assertThat(FilterValueParser.parseIntegerList("[1]")).containsExactly(1);
	}

	@Test
	@DisplayName("parseIntegerList should skip non-integer non-textual JSON array elements")
	void testParseIntegerListJsonArraySkipsNonScalarElements() {
		assertThat(FilterValueParser.parseIntegerList("[true,null,1]")).containsExactly(1);
		assertThat(FilterValueParser.parseIntegerList("[1.5]")).isEmpty();
	}

	@Test
	@DisplayName("parseIntegerList should fall back to comma-separated when JSON parsing fails")
	void testParseIntegerListMalformedJsonFallsBackToCommaSeparated() {
		assertThat(FilterValueParser.parseIntegerList("[1,2")).containsExactly(2);
	}

	@Test
	@DisplayName("parseIntegerList should parse JSON array of string integers")
	void testParseIntegerListJsonArrayOfStrings() {
		assertThat(FilterValueParser.parseIntegerList("[\"1\",\"2\",\"3\"]")).containsExactly(1, 2, 3);
	}

	@Test
	@DisplayName("parseIntegerList should skip invalid entries in comma-separated values")
	void testParseIntegerListCommaSeparatedSkipsInvalid() {
		assertThat(FilterValueParser.parseIntegerList("1,invalid,3")).containsExactly(1, 3);
	}

	@Test
	@DisplayName("parseIntegerList should skip empty segments in comma-separated values")
	void testParseIntegerListCommaSeparatedSkipsEmptySegments() {
		assertThat(FilterValueParser.parseIntegerList("1,,2")).containsExactly(1, 2);
		assertThat(FilterValueParser.parseIntegerList(",1")).containsExactly(1);
		assertThat(FilterValueParser.parseIntegerList("1,")).containsExactly(1);
	}

	@Test
	@DisplayName("parseIntegerList should skip invalid textual entries in JSON array")
	void testParseIntegerListJsonArraySkipsInvalidTextual() {
		assertThat(FilterValueParser.parseIntegerList("[1,\"invalid\",3]")).containsExactly(1, 3);
		assertThat(FilterValueParser.parseIntegerList("[\"abc\"]")).isEmpty();
	}

	@Test
	@DisplayName("parseIntegerList should fall back to comma-separated when JSON is not an array")
	void testParseIntegerListNonArrayJsonFallsBackToCommaSeparated() {
		assertThat(FilterValueParser.parseIntegerList("{\"id\":1}")).isEmpty();
		assertThat(FilterValueParser.parseIntegerList("not-json,4,5")).containsExactly(4, 5);
	}

	@ParameterizedTest
	@MethodSource("provideEmptyResultInputs")
	@DisplayName("parseIntegerList should return empty list when no valid integers are found")
	void testParseIntegerListNoValidIntegersReturnsEmptyList(String filterValue, List<Integer> expected) {
		assertThat(FilterValueParser.parseIntegerList(filterValue)).isEqualTo(expected);
	}

	@Test
	@DisplayName("parseIntegerList should return empty list for JSON array with unsupported element types")
	void testParseIntegerListJsonArrayWithObjectElementReturnsEmpty() {
		assertThat(FilterValueParser.parseIntegerList("[{}]")).isEmpty();
	}

	static Stream<Arguments> provideEmptyResultInputs() {
		return Stream.of(Arguments.of("invalid", List.of()), Arguments.of(",", List.of()),
				Arguments.of("[]", List.of()));
	}

}
