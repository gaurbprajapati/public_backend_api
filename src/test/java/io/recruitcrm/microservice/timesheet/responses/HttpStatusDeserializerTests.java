package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Unit tests for HttpStatusDeserializer class.
 */
class HttpStatusDeserializerTests {

	private HttpStatusDeserializer deserializer;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.deserializer = new HttpStatusDeserializer();
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("HttpStatusDeserializer - Constructor")
	void httpStatusDeserializerConstructor() {
		// Act & Assert
		assertThat(this.deserializer).isNotNull().isInstanceOf(HttpStatusDeserializer.class);
	}

	@ParameterizedTest
	@MethodSource("validHttpStatusProvider")
	@DisplayName("Deserialize - Valid HTTP status codes")
	void deserializeValidHttpStatus(String json, HttpStatus expectedStatus) throws IOException {
		// Arrange
		JsonParser jsonParser = this.objectMapper.getFactory().createParser(json);
		jsonParser.nextToken(); // Advance to the first token

		// Act
		HttpStatus result = this.deserializer.deserialize(jsonParser, null);

		// Assert
		assertThat(result).isEqualTo(expectedStatus);
	}

	private static Stream<Arguments> validHttpStatusProvider() {
		return Stream.of(Arguments.of("200", HttpStatus.OK), Arguments.of("400", HttpStatus.BAD_REQUEST),
				Arguments.of("404", HttpStatus.NOT_FOUND));
	}

	@ParameterizedTest
	@MethodSource("additionalHttpStatusProvider")
	@DisplayName("Deserialize - Additional HTTP status codes (with extra assertions)")
	void deserializeAdditionalHttpStatus(String json, HttpStatus expectedStatus) throws IOException {
		// Arrange
		JsonParser jsonParser = this.objectMapper.getFactory().createParser(json);
		jsonParser.nextToken(); // Advance to the first token

		// Act
		HttpStatus result = this.deserializer.deserialize(jsonParser, null);

		// Assert
		assertThat(result).isNotNull()
			.isEqualTo(expectedStatus)
			.extracting(HttpStatus::value)
			.isEqualTo(expectedStatus.value());
	}

	private static Stream<Arguments> additionalHttpStatusProvider() {
		return Stream.of(Arguments.of("500", HttpStatus.INTERNAL_SERVER_ERROR), Arguments.of("201", HttpStatus.CREATED),
				Arguments.of("204", HttpStatus.NO_CONTENT));
	}

	@ParameterizedTest
	@MethodSource("forbiddenUnauthorizedAndZeroStatusProvider")
	@DisplayName("Deserialize - Forbidden, Unauthorized, and Zero status codes")
	void deserializeForbiddenUnauthorizedAndZeroStatus(String json, boolean expectException, HttpStatus expectedStatus,
			String expectedExceptionMessage) throws IOException {
		JsonParser jsonParser = this.objectMapper.getFactory().createParser(json);
		jsonParser.nextToken(); // Advance to the first token

		if (expectException) {
			assertThatThrownBy(() -> this.deserializer.deserialize(jsonParser, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(expectedExceptionMessage);
		}
		else {
			HttpStatus result = this.deserializer.deserialize(jsonParser, null);
			assertThat(result).isNotNull().isEqualTo(expectedStatus);
		}
	}

	private static Stream<Arguments> forbiddenUnauthorizedAndZeroStatusProvider() {
		return Stream.of(Arguments.of("403", false, HttpStatus.FORBIDDEN, null),
				Arguments.of("401", false, HttpStatus.UNAUTHORIZED, null),
				Arguments.of("0", true, null, "No matching constant for [0]"));
	}

	@ParameterizedTest
	@MethodSource("invalidStatusCodeProvider")
	@DisplayName("Deserialize - Invalid status codes should throw IllegalArgumentException")
	void deserializeInvalidStatusCodes(String json, String expectedExceptionMessage) throws IOException {
		// Arrange
		JsonParser jsonParser = this.objectMapper.getFactory().createParser(json);
		jsonParser.nextToken(); // Advance to the first token

		// Act & Assert - Should throw IllegalArgumentException for invalid status code
		assertThatThrownBy(() -> this.deserializer.deserialize(jsonParser, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(expectedExceptionMessage);
	}

	private static Stream<Arguments> invalidStatusCodeProvider() {
		return Stream.of(Arguments.of("999", "No matching constant for [999]"),
				Arguments.of("-1", "No matching constant for [-1]"), Arguments.of("0", "No matching constant for [0]"),
				Arguments.of("1000", "No matching constant for [1000]"));
	}

	@Test
	@DisplayName("Deserialize - Invalid string value")
	void deserializeInvalidStringValue() throws IOException {
		// Arrange
		String json = "\"invalid\"";
		JsonParser jsonParser = this.objectMapper.getFactory().createParser(json);
		jsonParser.nextToken();

		// Act & Assert
		assertThatThrownBy(() -> this.deserializer.deserialize(jsonParser, null))
			.isInstanceOf(com.fasterxml.jackson.core.JsonParseException.class)
			.hasMessageContaining("Current token (VALUE_STRING) not numeric");
	}

	@Test
	@DisplayName("Deserialize - Boundary status codes")
	void deserializeBoundaryStatusCodes() throws IOException {
		// Test boundary cases
		String[] jsonInputs = { "100", "200", "206", "300", "400", "409", "500", "511" };
		HttpStatus[] expectedStatuses = { HttpStatus.CONTINUE, HttpStatus.OK, HttpStatus.PARTIAL_CONTENT,
				HttpStatus.MULTIPLE_CHOICES, HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT,
				HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.NETWORK_AUTHENTICATION_REQUIRED };

		for (int i = 0; i < jsonInputs.length; i++) {
			// Arrange
			JsonParser jsonParser = this.objectMapper.getFactory().createParser(jsonInputs[i]);
			jsonParser.nextToken();

			// Act
			HttpStatus result = this.deserializer.deserialize(jsonParser, null);

			// Assert
			assertThat(result).isEqualTo(expectedStatuses[i]);
		}
	}

	@Test
	@DisplayName("Deserialize - All 1xx status codes")
	void deserializeAll1xxStatusCodes() throws IOException {
		// Given
		String[] jsonInputs = { "100", "101", "102" };
		HttpStatus[] expectedStatuses = { HttpStatus.CONTINUE, HttpStatus.SWITCHING_PROTOCOLS, HttpStatus.PROCESSING };

		for (int i = 0; i < jsonInputs.length; i++) {
			// Arrange
			JsonParser jsonParser = this.objectMapper.getFactory().createParser(jsonInputs[i]);
			jsonParser.nextToken();

			// Act
			HttpStatus result = this.deserializer.deserialize(jsonParser, null);

			// Assert
			assertThat(result).isEqualTo(expectedStatuses[i]);
		}
	}

	@Test
	@DisplayName("Deserialize - All 3xx status codes")
	void deserializeAll3xxStatusCodes() throws IOException {
		// Given
		String[] jsonInputs = { "300", "301", "302", "303", "304", "307", "308" };
		HttpStatus[] expectedStatuses = { HttpStatus.MULTIPLE_CHOICES, HttpStatus.MOVED_PERMANENTLY, HttpStatus.FOUND,
				HttpStatus.SEE_OTHER, HttpStatus.NOT_MODIFIED, HttpStatus.TEMPORARY_REDIRECT,
				HttpStatus.PERMANENT_REDIRECT };

		for (int i = 0; i < jsonInputs.length; i++) {
			// Arrange
			JsonParser jsonParser = this.objectMapper.getFactory().createParser(jsonInputs[i]);
			jsonParser.nextToken();

			// Act
			HttpStatus result = this.deserializer.deserialize(jsonParser, null);

			// Assert
			assertThat(result).isEqualTo(expectedStatuses[i]);
		}
	}

	@Test
	@DisplayName("Deserialize - Common error status codes")
	void deserializeCommonErrorStatusCodes() throws IOException {
		// Given
		String[] jsonInputs = { "400", "401", "403", "404", "405", "409", "418", "422", "429", "500", "502", "503" };
		HttpStatus[] expectedStatuses = { HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN,
				HttpStatus.NOT_FOUND, HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.CONFLICT, HttpStatus.I_AM_A_TEAPOT,
				HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.TOO_MANY_REQUESTS, HttpStatus.INTERNAL_SERVER_ERROR,
				HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE };

		for (int i = 0; i < jsonInputs.length; i++) {
			// Arrange
			JsonParser jsonParser = this.objectMapper.getFactory().createParser(jsonInputs[i]);
			jsonParser.nextToken();

			// Act
			HttpStatus result = this.deserializer.deserialize(jsonParser, null);

			// Assert
			assertThat(result).isEqualTo(expectedStatuses[i]);
		}
	}

	@Test
	@DisplayName("Deserialize - Leading zeros cause JsonParseException")
	void deserializeLeadingZerosCauseJsonParseException() throws IOException {
		// Arrange
		String json = "0200"; // Leading zeros are not allowed in JSON
		JsonParser jsonParser = this.objectMapper.getFactory().createParser(json);

		// When & Then
		assertThatThrownBy(jsonParser::nextToken).isInstanceOf(com.fasterxml.jackson.core.JsonParseException.class)
			.hasMessageContaining("Leading zeroes not allowed");
	}

}