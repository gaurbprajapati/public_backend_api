package io.recruitcrm.microservice.timesheet.responses;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HttpStatusSerializer class.
 */
class HttpStatusSerializerTests {

	private HttpStatusSerializer serializer;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.serializer = new HttpStatusSerializer();
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("HttpStatusSerializer - Constructor")
	void testHttpStatusSerializerConstructor() {
		// Act & Assert
		assertThat(this.serializer).isNotNull();
	}

	@Test
	@DisplayName("Serialize - OK status")
	void testSerializeOkStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.OK;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("200");
	}

	@Test
	@DisplayName("Serialize - Bad Request status")
	void testSerializeBadRequestStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("400");
	}

	@Test
	@DisplayName("Serialize - Not Found status")
	void testSerializeNotFoundStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.NOT_FOUND;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("404");
	}

	@Test
	@DisplayName("Serialize - Internal Server Error status")
	void testSerializeInternalServerErrorStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("500");
	}

	@Test
	@DisplayName("Serialize - Created status")
	void testSerializeCreatedStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.CREATED;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("201");
	}

	@Test
	@DisplayName("Serialize - No Content status")
	void testSerializeNoContentStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.NO_CONTENT;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("204");
	}

	@Test
	@DisplayName("Serialize - Forbidden status")
	void testSerializeForbiddenStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.FORBIDDEN;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("403");
	}

	@Test
	@DisplayName("Serialize - Unauthorized status")
	void testSerializeUnauthorizedStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("401");
	}

	@Test
	@DisplayName("Serialize - Accepted status")
	void testSerializeAcceptedStatus() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.ACCEPTED;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("202");
	}

	@Test
	@DisplayName("Serialize - Multiple statuses")
	void testSerializeMultipleStatuses() throws IOException {
		// Test multiple status codes
		HttpStatus[] statuses = { HttpStatus.OK, HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND,
				HttpStatus.INTERNAL_SERVER_ERROR };

		int[] expectedValues = { 200, 201, 400, 404, 500 };

		for (int i = 0; i < statuses.length; i++) {
			// Arrange
			StringWriter stringWriter = new StringWriter();
			JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
			SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

			// Act
			this.serializer.serialize(statuses[i], jsonGenerator, serializerProvider);
			jsonGenerator.close();

			// Assert
			assertThat(stringWriter.toString()).hasToString(String.valueOf(expectedValues[i]));
		}
	}

	@Test
	@DisplayName("Serialize - Custom status code")
	void testSerializeCustomStatusCode() throws IOException {
		// Arrange
		HttpStatus httpStatus = HttpStatus.valueOf(418); // I'm a teapot
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// Act
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.close();

		// Assert
		assertThat(stringWriter.toString()).hasToString("418");
	}

	@Test
	@DisplayName("Serialize - All 1xx status codes")
	void testSerializeAll1xxStatusCodes() throws IOException {
		// Given
		HttpStatus[] statuses = { HttpStatus.CONTINUE, HttpStatus.SWITCHING_PROTOCOLS, HttpStatus.PROCESSING };
		int[] expectedValues = { 100, 101, 102 };

		// When & Then
		for (int i = 0; i < statuses.length; i++) {
			StringWriter stringWriter = new StringWriter();
			JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
			SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

			this.serializer.serialize(statuses[i], jsonGenerator, serializerProvider);
			jsonGenerator.close();

			assertThat(stringWriter.toString()).hasToString(String.valueOf(expectedValues[i]));
		}
	}

	@Test
	@DisplayName("Serialize - All 3xx status codes")
	void testSerializeAll3xxStatusCodes() throws IOException {
		// Given
		HttpStatus[] statuses = { HttpStatus.MULTIPLE_CHOICES, HttpStatus.MOVED_PERMANENTLY, HttpStatus.FOUND,
				HttpStatus.SEE_OTHER, HttpStatus.NOT_MODIFIED, HttpStatus.TEMPORARY_REDIRECT,
				HttpStatus.PERMANENT_REDIRECT };
		int[] expectedValues = { 300, 301, 302, 303, 304, 307, 308 };

		// When & Then
		for (int i = 0; i < statuses.length; i++) {
			StringWriter stringWriter = new StringWriter();
			JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
			SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

			this.serializer.serialize(statuses[i], jsonGenerator, serializerProvider);
			jsonGenerator.close();

			assertThat(stringWriter.toString()).hasToString(String.valueOf(expectedValues[i]));
		}
	}

	@Test
	@DisplayName("Serialize - All 4xx status codes")
	void testSerializeAll4xxStatusCodes() throws IOException {
		// Given
		HttpStatus[] statuses = { HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED, HttpStatus.PAYMENT_REQUIRED,
				HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND, HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.NOT_ACCEPTABLE,
				HttpStatus.PROXY_AUTHENTICATION_REQUIRED, HttpStatus.REQUEST_TIMEOUT, HttpStatus.CONFLICT,
				HttpStatus.GONE, HttpStatus.LENGTH_REQUIRED, HttpStatus.PRECONDITION_FAILED,
				HttpStatus.PAYLOAD_TOO_LARGE, HttpStatus.URI_TOO_LONG, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, HttpStatus.EXPECTATION_FAILED, HttpStatus.I_AM_A_TEAPOT,
				HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.LOCKED, HttpStatus.FAILED_DEPENDENCY, HttpStatus.TOO_EARLY,
				HttpStatus.UPGRADE_REQUIRED, HttpStatus.PRECONDITION_REQUIRED, HttpStatus.TOO_MANY_REQUESTS,
				HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS };
		int[] expectedValues = { 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416,
				417, 418, 422, 423, 424, 425, 426, 428, 429, 431, 451 };

		// When & Then
		for (int i = 0; i < statuses.length; i++) {
			StringWriter stringWriter = new StringWriter();
			JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
			SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

			this.serializer.serialize(statuses[i], jsonGenerator, serializerProvider);
			jsonGenerator.close();

			assertThat(stringWriter.toString()).hasToString(String.valueOf(expectedValues[i]));
		}
	}

	@Test
	@DisplayName("Serialize - All 5xx status codes")
	void testSerializeAll5xxStatusCodes() throws IOException {
		// Given
		HttpStatus[] statuses = { HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.NOT_IMPLEMENTED, HttpStatus.BAD_GATEWAY,
				HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT, HttpStatus.HTTP_VERSION_NOT_SUPPORTED,
				HttpStatus.VARIANT_ALSO_NEGOTIATES, HttpStatus.INSUFFICIENT_STORAGE, HttpStatus.LOOP_DETECTED,
				HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, HttpStatus.NOT_EXTENDED,
				HttpStatus.NETWORK_AUTHENTICATION_REQUIRED };
		int[] expectedValues = { 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511 };

		// When & Then
		for (int i = 0; i < statuses.length; i++) {
			StringWriter stringWriter = new StringWriter();
			JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
			SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

			this.serializer.serialize(statuses[i], jsonGenerator, serializerProvider);
			jsonGenerator.close();

			assertThat(stringWriter.toString()).hasToString(String.valueOf(expectedValues[i]));
		}
	}

	@Test
	@DisplayName("Serialize - Edge case status codes")
	void testSerializeEdgeCaseStatusCodes() throws IOException {
		// Given
		HttpStatus[] statuses = { HttpStatus.valueOf(100), // Minimum valid HTTP status
				HttpStatus.valueOf(511), // Maximum valid HTTP status in Spring
				HttpStatus.valueOf(418), // I'm a teapot (RFC 2324)
				HttpStatus.valueOf(451) // Unavailable for legal reasons
		};
		int[] expectedValues = { 100, 511, 418, 451 };

		// When & Then
		for (int i = 0; i < statuses.length; i++) {
			StringWriter stringWriter = new StringWriter();
			JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
			SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

			this.serializer.serialize(statuses[i], jsonGenerator, serializerProvider);
			jsonGenerator.close();

			assertThat(stringWriter.toString()).hasToString(String.valueOf(expectedValues[i]));
		}
	}

	@Test
	@DisplayName("Serialize - Ensure JSON generator is properly handled")
	void testSerializeEnsureJsonGeneratorIsProperlyHandled() throws IOException {
		// Given
		HttpStatus httpStatus = HttpStatus.OK;
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(stringWriter);
		SerializerProvider serializerProvider = this.objectMapper.getSerializerProvider();

		// When
		this.serializer.serialize(httpStatus, jsonGenerator, serializerProvider);
		jsonGenerator.flush(); // Ensure data is written

		// Then
		assertThat(stringWriter).hasToString("200");
	}

}