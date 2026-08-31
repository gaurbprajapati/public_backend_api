package io.recruitcrm.microservice.timesheet.dto.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies {@link IntegerListDeserializer} via {@link TimesheetSearchRequestBodyDto} and
 * direct deserializer invocation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IntegerListDeserializer Test")
class IntegerListDeserializerTests {

	private ObjectMapper objectMapper;

	@Mock
	private DeserializationContext deserializationContext;

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Single JSON number deserializes to a one-element timesheetIds list")
	void testSingleIntegerDeserializesToSingletonList() throws Exception {
		// Given
		String json = "{\"timesheetIds\":38916}";

		// When
		TimesheetSearchRequestBodyDto dto = this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class);

		// Then
		assertThat(dto.getTimesheetIds()).containsExactly(38916);
	}

	@Test
	@DisplayName("JSON array deserializes to timesheetIds list")
	void testArrayDeserializesToList() throws Exception {
		// Given
		String json = "{\"timesheetIds\":[23,45]}";

		// When
		TimesheetSearchRequestBodyDto dto = this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class);

		// Then
		assertThat(dto.getTimesheetIds()).containsExactly(23, 45);
	}

	@Test
	@DisplayName("Single-element JSON array deserializes to one-element timesheetIds list")
	void testSingleElementArrayDeserializesToSingletonList() throws Exception {
		// Given
		String json = "{\"timesheetIds\":[99]}";

		// When
		TimesheetSearchRequestBodyDto dto = this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class);

		// Then
		assertThat(dto.getTimesheetIds()).containsExactly(99);
	}

	@Test
	@DisplayName("Explicit null timesheetIds stays null")
	void testNullTimesheetIdsStaysNull() throws Exception {
		// Given
		String json = "{\"timesheetIds\":null}";

		// When
		TimesheetSearchRequestBodyDto dto = this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class);

		// Then
		assertThat(dto.getTimesheetIds()).isNull();
	}

	@Test
	@DisplayName("Empty JSON array deserializes to empty timesheetIds list")
	void testEmptyArrayDeserializesToEmptyList() throws Exception {
		// Given
		String json = "{\"timesheetIds\":[]}";

		// When
		TimesheetSearchRequestBodyDto dto = this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class);

		// Then
		assertThat(dto.getTimesheetIds()).isEmpty();
	}

	@Test
	@DisplayName("isReimbursement boolean deserializes from JSON payload")
	void testIsReimbursementBooleanDeserializesFromJson() throws Exception {
		// Given
		String json = "{\"isReimbursement\":true}";

		// When
		TimesheetSearchRequestBodyDto dto = this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class);

		// Then
		assertThat(dto.getIsReimbursement()).isTrue();
		assertThat(dto.getIsSubmitted()).isNull();
	}

	@Test
	@DisplayName("Constructor should initialize deserializer with list type")
	void testConstructorInitializesDeserializer() {
		// When
		IntegerListDeserializer deserializer = new IntegerListDeserializer();

		// Then
		assertThat(deserializer.handledType()).isEqualTo(List.class);
	}

	@Test
	@DisplayName("Single integer token deserializes to one-element list via direct deserializer")
	void testSingleIntegerTokenDeserializesToSingletonList() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("42");
		parser.nextToken();

		// When
		List<Integer> result = deserializer.deserialize(parser, this.deserializationContext);

		// Then
		assertThat(parser.getCurrentToken()).isEqualTo(JsonToken.VALUE_NUMBER_INT);
		assertThat(result).containsExactly(42);
	}

	@Test
	@DisplayName("JSON array token deserializes to list via direct deserializer")
	void testArrayTokenDeserializesToList() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("[23,45]");
		parser.nextToken();

		// When
		List<Integer> result = deserializer.deserialize(parser, this.deserializationContext);

		// Then
		assertThat(parser.getCurrentToken()).isEqualTo(JsonToken.END_ARRAY);
		assertThat(result).containsExactly(23, 45);
	}

	@Test
	@DisplayName("Single-element JSON array token deserializes via direct deserializer")
	void testSingleElementArrayTokenDeserializesToSingletonList() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("[7]");
		parser.nextToken();

		// When
		List<Integer> result = deserializer.deserialize(parser, this.deserializationContext);

		// Then
		assertThat(parser.getCurrentToken()).isEqualTo(JsonToken.END_ARRAY);
		assertThat(result).containsExactly(7);
	}

	@Test
	@DisplayName("Empty JSON array token deserializes to empty list via direct deserializer")
	void testEmptyArrayTokenDeserializesToEmptyList() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("[]");
		parser.nextToken();

		// When
		List<Integer> result = deserializer.deserialize(parser, this.deserializationContext);

		// Then
		assertThat(parser.getCurrentToken()).isEqualTo(JsonToken.END_ARRAY);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Non-array non-integer token deserializes to empty list")
	void testNonArrayNonIntegerTokenDeserializesToEmptyList() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("true");
		parser.nextToken();

		// When
		List<Integer> result = deserializer.deserialize(parser, this.deserializationContext);

		// Then
		assertThat(parser.getCurrentToken()).isEqualTo(JsonToken.VALUE_TRUE);
		assertThat(result).isEmpty();
	}

	@ParameterizedTest(name = "Invalid timesheetIds JSON throws InvalidFormatException: {0}")
	@MethodSource("invalidTimesheetIdsJsonProvider")
	@DisplayName("Invalid timesheetIds values throw InvalidFormatException")
	void testInvalidTimesheetIdsThrowInvalidFormatException(String json) {
		// When & Then
		assertThatThrownBy(() -> this.objectMapper.readValue(json, TimesheetSearchRequestBodyDto.class))
			.isInstanceOf(InvalidFormatException.class)
			.hasMessageContaining("out of range of int");
	}

	private static Stream<String> invalidTimesheetIdsJsonProvider() {
		return Stream.of("{\"timesheetIds\":[39891111111111]}", "{\"timesheetIds\":39891111111111}",
				"{\"timesheetIds\":[-2147483649]}", "{\"timesheetIds\":-2147483649}");
	}

	@Test
	@DisplayName("Out of range integer in validate email request throws InvalidFormatException")
	void testOutOfRangeIntegerInValidateEmailRequestThrowsInvalidFormatException() {
		// Given
		String json = "{\"timesheetIds\":[39891111111111],\"entity_type_id\":1}";

		// When & Then
		assertThatThrownBy(() -> this.objectMapper.readValue(json, ValidateTimesheetEmailRequestBodyDto.class))
			.isInstanceOf(InvalidFormatException.class)
			.hasMessageContaining("out of range of int");
	}

	@Test
	@DisplayName("Out of range integer token throws InvalidFormatException via direct deserializer")
	void testOutOfRangeIntegerTokenThrowsInvalidFormatException() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("[39891111111111]");
		parser.nextToken();
		parser.nextToken();

		// When & Then
		assertThatThrownBy(() -> deserializer.deserialize(parser, this.deserializationContext))
			.isInstanceOf(InvalidFormatException.class)
			.hasMessageContaining("out of range of int");
	}

	@Test
	@DisplayName("Out of range single integer token throws InvalidFormatException via direct deserializer")
	void testOutOfRangeSingleIntegerTokenThrowsInvalidFormatException() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("39891111111111");
		parser.nextToken();

		// When & Then
		assertThatThrownBy(() -> deserializer.deserialize(parser, this.deserializationContext))
			.isInstanceOf(InvalidFormatException.class)
			.hasMessageContaining("out of range of int");
	}

	@Test
	@DisplayName("Below minimum integer token throws InvalidFormatException via direct deserializer")
	void testBelowMinimumIntegerTokenThrowsInvalidFormatException() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory().createParser("[-2147483649]");
		parser.nextToken();
		parser.nextToken();

		// When & Then
		assertThatThrownBy(() -> deserializer.deserialize(parser, this.deserializationContext))
			.isInstanceOf(InvalidFormatException.class)
			.hasMessageContaining("out of range of int");
	}

	@Test
	@DisplayName("Integer boundary values deserialize correctly via direct deserializer")
	void testIntegerBoundaryValuesDeserializeCorrectlyViaDirectDeserializer() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser minParser = this.objectMapper.getFactory().createParser(String.valueOf(Integer.MIN_VALUE));
		minParser.nextToken();
		JsonParser maxParser = this.objectMapper.getFactory().createParser(String.valueOf(Integer.MAX_VALUE));
		maxParser.nextToken();

		// When
		List<Integer> minResult = deserializer.deserialize(minParser, this.deserializationContext);
		List<Integer> maxResult = deserializer.deserialize(maxParser, this.deserializationContext);

		// Then
		assertThat(minResult).containsExactly(Integer.MIN_VALUE);
		assertThat(maxResult).containsExactly(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("Integer boundary values in array deserialize correctly via direct deserializer")
	void testIntegerBoundaryValuesInArrayDeserializeCorrectlyViaDirectDeserializer() throws Exception {
		// Given
		IntegerListDeserializer deserializer = new IntegerListDeserializer();
		JsonParser parser = this.objectMapper.getFactory()
			.createParser("[" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + "]");
		parser.nextToken();

		// When
		List<Integer> result = deserializer.deserialize(parser, this.deserializationContext);

		// Then
		assertThat(result).containsExactly(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

}
