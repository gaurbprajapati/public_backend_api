package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for APIResponseKeyMeta class. Tests all constructors, field management, and
 * auto-generation of UUID/timestamp.
 */
class APIResponseKeyMetaTests {

	@Test
	@DisplayName("Should create APIResponseKeyMeta with default constructor")
	void shouldCreateAPIResponseKeyMetaWithDefaultConstructor() {
		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta();

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isNull();
		assertThat(meta.getRequestUuid()).isNotNull();
		assertThat(meta.getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(meta.getTimestamp()).isNotNull();
		assertThat(meta.getStatus()).isEqualTo(HttpStatus.OK);
		// Verify UUID format
		assertThat(meta.getRequestUuid())
			.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
		// Verify timestamp is recent (within last few seconds)
		assertThat(meta.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
		assertThat(meta.getTimestamp()).isAfter(LocalDateTime.now().minusSeconds(5));
	}

	@Test
	@DisplayName("Should create APIResponseKeyMeta with message only constructor")
	void shouldCreateAPIResponseKeyMetaWithMessageOnlyConstructor() {
		// Given
		String message = "Test message";

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(message);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isEqualTo(message);
		assertThat(meta.getRequestUuid()).isNotNull();
		assertThat(meta.getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(meta.getTimestamp()).isNotNull();
		assertThat(meta.getStatus()).isEqualTo(HttpStatus.OK);
		// Verify UUID format
		assertThat(meta.getRequestUuid())
			.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
	}

	@Test
	@DisplayName("Should create APIResponseKeyMeta with message and response type constructor")
	void shouldCreateAPIResponseKeyMetaWithMessageAndResponseTypeConstructor() {
		// Given
		String message = "Info message";
		APIResponseType responseType = APIResponseType.INFO;

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(message, responseType);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isEqualTo(message);
		assertThat(meta.getRequestUuid()).isNotNull();
		assertThat(meta.getResponseType()).isEqualTo(responseType);
		assertThat(meta.getTimestamp()).isNotNull();
		assertThat(meta.getStatus()).isEqualTo(HttpStatus.OK);
	}

	@Test
	@DisplayName("Should create APIResponseKeyMeta with response type and status constructor")
	void shouldCreateAPIResponseKeyMetaWithResponseTypeAndStatusConstructor() {
		// Given
		APIResponseType responseType = APIResponseType.ERROR;
		HttpStatus status = HttpStatus.BAD_REQUEST;

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(responseType, status);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isNull();
		assertThat(meta.getRequestUuid()).isNotNull();
		assertThat(meta.getResponseType()).isEqualTo(responseType);
		assertThat(meta.getTimestamp()).isNotNull();
		assertThat(meta.getStatus()).isEqualTo(status);
	}

	@Test
	@DisplayName("Should create APIResponseKeyMeta with message, response type and status constructor")
	void shouldCreateAPIResponseKeyMetaWithMessageResponseTypeAndStatusConstructor() {
		// Given
		String message = "Warning message";
		APIResponseType responseType = APIResponseType.WARNING;
		HttpStatus status = HttpStatus.ACCEPTED;

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(message, responseType, status);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isEqualTo(message);
		assertThat(meta.getRequestUuid()).isNotNull();
		assertThat(meta.getResponseType()).isEqualTo(responseType);
		assertThat(meta.getTimestamp()).isNotNull();
		assertThat(meta.getStatus()).isEqualTo(status);
	}

	@Test
	@DisplayName("Should create APIResponseKeyMeta with all args constructor")
	void shouldCreateAPIResponseKeyMetaWithAllArgsConstructor() {
		// Given
		String message = "Custom message";
		String requestUuid = "custom-uuid-123";
		APIResponseType responseType = APIResponseType.SUCCESS;
		LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
		HttpStatus status = HttpStatus.CREATED;

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(message, requestUuid, responseType, timestamp, status);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isEqualTo(message);
		assertThat(meta.getRequestUuid()).isEqualTo(requestUuid);
		assertThat(meta.getResponseType()).isEqualTo(responseType);
		assertThat(meta.getTimestamp()).isEqualTo(timestamp);
		assertThat(meta.getStatus()).isEqualTo(status);
	}

	@Test
	@DisplayName("Should handle null message properly")
	void shouldHandleNullMessageProperly() {
		// Given
		String nullMessage = null;
		APIResponseType responseType = APIResponseType.SUCCESS;

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(nullMessage, responseType);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isNull();
		assertThat(meta.getResponseType()).isEqualTo(responseType);
		assertThat(meta.getRequestUuid()).isNotNull();
		assertThat(meta.getTimestamp()).isNotNull();
	}

	@Test
	@DisplayName("Should generate unique UUIDs for different instances")
	void shouldGenerateUniqueUUIDsForDifferentInstances() {
		// When
		APIResponseKeyMeta meta1 = new APIResponseKeyMeta();
		APIResponseKeyMeta meta2 = new APIResponseKeyMeta();
		APIResponseKeyMeta meta3 = new APIResponseKeyMeta("Message");

		// Then
		assertThat(meta1.getRequestUuid()).isNotNull();
		assertThat(meta2.getRequestUuid()).isNotNull();
		assertThat(meta3.getRequestUuid()).isNotNull();
		assertThat(meta1.getRequestUuid()).isNotEqualTo(meta2.getRequestUuid());
		assertThat(meta1.getRequestUuid()).isNotEqualTo(meta3.getRequestUuid());
		assertThat(meta2.getRequestUuid()).isNotEqualTo(meta3.getRequestUuid());
	}

	@Test
	@DisplayName("Should generate current timestamps for different instances")
	void shouldGenerateCurrentTimestampsForDifferentInstances() {
		// When
		APIResponseKeyMeta meta1 = new APIResponseKeyMeta();
		APIResponseKeyMeta meta2 = new APIResponseKeyMeta("Message");

		// Then
		assertThat(meta1.getTimestamp()).isNotNull();
		assertThat(meta2.getTimestamp()).isNotNull();
		assertThat(meta1.getTimestamp()).isBeforeOrEqualTo(meta2.getTimestamp());
		// Both should be recent
		LocalDateTime now = LocalDateTime.now();
		assertThat(meta1.getTimestamp()).isBefore(now.plusSeconds(1));
		assertThat(meta2.getTimestamp()).isBefore(now.plusSeconds(1));
	}

	@Test
	@DisplayName("Should allow setting and getting message via setters")
	void shouldAllowSettingAndGettingMessageViaSetters() {
		// Given
		APIResponseKeyMeta meta = new APIResponseKeyMeta();
		String newMessage = "Updated message";

		// When
		meta.setMessage(newMessage);

		// Then
		assertThat(meta.getMessage()).isEqualTo(newMessage);
	}

	@Test
	@DisplayName("Should allow setting and getting requestUuid via setters")
	void shouldAllowSettingAndGettingRequestUuidViaSetters() {
		// Given
		APIResponseKeyMeta meta = new APIResponseKeyMeta();
		String newUuid = "custom-uuid-456";

		// When
		meta.setRequestUuid(newUuid);

		// Then
		assertThat(meta.getRequestUuid()).isEqualTo(newUuid);
	}

	@Test
	@DisplayName("Should allow setting and getting responseType via setters")
	void shouldAllowSettingAndGettingResponseTypeViaSetters() {
		// Given
		APIResponseKeyMeta meta = new APIResponseKeyMeta();
		APIResponseType newResponseType = APIResponseType.WARNING;

		// When
		meta.setResponseType(newResponseType);

		// Then
		assertThat(meta.getResponseType()).isEqualTo(newResponseType);
	}

	@Test
	@DisplayName("Should allow setting and getting timestamp via setters")
	void shouldAllowSettingAndGettingTimestampViaSetters() {
		// Given
		APIResponseKeyMeta meta = new APIResponseKeyMeta();
		LocalDateTime newTimestamp = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

		// When
		meta.setTimestamp(newTimestamp);

		// Then
		assertThat(meta.getTimestamp()).isEqualTo(newTimestamp);
	}

	@Test
	@DisplayName("Should allow setting and getting status via setters")
	void shouldAllowSettingAndGettingStatusViaSetters() {
		// Given
		APIResponseKeyMeta meta = new APIResponseKeyMeta();
		HttpStatus newStatus = HttpStatus.NOT_FOUND;

		// When
		meta.setStatus(newStatus);

		// Then
		assertThat(meta.getStatus()).isEqualTo(newStatus);
	}

	@Test
	@DisplayName("Should handle all API response types properly")
	void shouldHandleAllAPIResponseTypesProperly() {
		// Given & When
		APIResponseKeyMeta successMeta = new APIResponseKeyMeta(APIResponseType.SUCCESS, HttpStatus.OK);
		APIResponseKeyMeta errorMeta = new APIResponseKeyMeta(APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
		APIResponseKeyMeta infoMeta = new APIResponseKeyMeta(APIResponseType.INFO, HttpStatus.ACCEPTED);
		APIResponseKeyMeta warningMeta = new APIResponseKeyMeta(APIResponseType.WARNING, HttpStatus.PARTIAL_CONTENT);

		// Then
		assertThat(successMeta.getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(errorMeta.getResponseType()).isEqualTo(APIResponseType.ERROR);
		assertThat(infoMeta.getResponseType()).isEqualTo(APIResponseType.INFO);
		assertThat(warningMeta.getResponseType()).isEqualTo(APIResponseType.WARNING);
	}

	@Test
	@DisplayName("Should handle all HTTP status codes properly")
	void shouldHandleAllHTTPStatusCodesProperly() {
		// Given & When
		APIResponseKeyMeta okMeta = new APIResponseKeyMeta(APIResponseType.SUCCESS, HttpStatus.OK);
		APIResponseKeyMeta createdMeta = new APIResponseKeyMeta(APIResponseType.SUCCESS, HttpStatus.CREATED);
		APIResponseKeyMeta badRequestMeta = new APIResponseKeyMeta(APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
		APIResponseKeyMeta notFoundMeta = new APIResponseKeyMeta(APIResponseType.ERROR, HttpStatus.NOT_FOUND);
		APIResponseKeyMeta serverErrorMeta = new APIResponseKeyMeta(APIResponseType.ERROR,
				HttpStatus.INTERNAL_SERVER_ERROR);

		// Then
		assertThat(okMeta.getStatus()).isEqualTo(HttpStatus.OK);
		assertThat(createdMeta.getStatus()).isEqualTo(HttpStatus.CREATED);
		assertThat(badRequestMeta.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(notFoundMeta.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(serverErrorMeta.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@DisplayName("Should support equals and hashCode from Lombok")
	void shouldSupportEqualsAndHashCodeFromLombok() {
		// Given
		String message = "Test message";
		String uuid = "test-uuid";
		APIResponseType responseType = APIResponseType.SUCCESS;
		LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
		HttpStatus status = HttpStatus.OK;

		APIResponseKeyMeta meta1 = new APIResponseKeyMeta(message, uuid, responseType, timestamp, status);
		APIResponseKeyMeta meta2 = new APIResponseKeyMeta(message, uuid, responseType, timestamp, status);

		// When & Then
		assertThat(meta1).isEqualTo(meta2).hasSameHashCodeAs(meta2);
		assertThat(meta1.equals(meta1)).isTrue();
	}

	@Test
	@DisplayName("Should support toString from Lombok")
	void shouldSupportToStringFromLombok() {
		// Given
		String message = "Test message";
		APIResponseKeyMeta meta = new APIResponseKeyMeta(message);

		// When
		String toString = meta.toString();

		// Then
		assertThat(toString).isNotNull().contains("APIResponseKeyMeta").contains(message).contains("SUCCESS");
	}

	@Test
	@DisplayName("Should handle empty string message properly")
	void shouldHandleEmptyStringMessageProperly() {
		// Given
		String emptyMessage = "";

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(emptyMessage);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isEqualTo(emptyMessage);
		assertThat(meta.getMessage()).isEmpty();
	}

	@Test
	@DisplayName("Should handle very long message properly")
	void shouldHandleVeryLongMessageProperly() {
		// Given
		String longMessage = "A".repeat(1000);

		// When
		APIResponseKeyMeta meta = new APIResponseKeyMeta(longMessage);

		// Then
		assertThat(meta).isNotNull();
		assertThat(meta.getMessage()).isEqualTo(longMessage);
		assertThat(meta.getMessage()).hasSize(1000);
	}

}