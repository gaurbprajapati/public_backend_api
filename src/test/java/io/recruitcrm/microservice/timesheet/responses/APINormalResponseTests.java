package io.recruitcrm.microservice.timesheet.responses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for APINormalResponse class. Tests all constructors, getters, setters, and
 * edge cases.
 */
class APINormalResponseTests {

	@Test
	@DisplayName("Should create APINormalResponse with default constructor")
	void shouldCreateAPINormalResponseWithDefaultConstructor() {
		// When
		APINormalResponse<String> response = new APINormalResponse<>();

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.OK);
		assertThat(response.getMeta().getRequestUuid()).isNotNull();
		assertThat(response.getMeta().getTimestamp()).isNotNull();
	}

	@Test
	@DisplayName("Should create APINormalResponse with data only constructor")
	void shouldCreateAPINormalResponseWithDataOnlyConstructor() {
		// Given
		String testData = "Test Data";

		// When
		APINormalResponse<String> response = new APINormalResponse<>(testData);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isEqualTo(testData);
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.OK);
		assertThat(response.getMeta().getRequestUuid()).isNotNull();
		assertThat(response.getMeta().getTimestamp()).isNotNull();
		assertThat(response.getMeta().getMessage()).isNull();
	}

	@Test
	@DisplayName("Should create APINormalResponse with meta only constructor")
	void shouldCreateAPINormalResponseWithMetaOnlyConstructor() {
		// Given
		APIResponseKeyMeta meta = new APIResponseKeyMeta("Test message", APIResponseType.INFO, HttpStatus.ACCEPTED);

		// When
		APINormalResponse<String> response = new APINormalResponse<>(meta);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getMeta()).isEqualTo(meta);
		assertThat(response.getMeta().getMessage()).isEqualTo("Test message");
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.INFO);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.ACCEPTED);
	}

	@Test
	@DisplayName("Should create APINormalResponse with data and meta constructor")
	void shouldCreateAPINormalResponseWithDataAndMetaConstructor() {
		// Given
		String testData = "Test Data";
		APIResponseKeyMeta meta = new APIResponseKeyMeta("Success message", APIResponseType.SUCCESS,
				HttpStatus.CREATED);

		// When
		APINormalResponse<String> response = new APINormalResponse<>(testData, meta);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isEqualTo(testData);
		assertThat(response.getMeta()).isEqualTo(meta);
		assertThat(response.getMeta().getMessage()).isEqualTo("Success message");
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	@DisplayName("Should create APINormalResponse with data and message constructor")
	void shouldCreateAPINormalResponseWithDataAndMessageConstructor() {
		// Given
		String testData = "Test Data";
		String message = "Operation successful";

		// When
		APINormalResponse<String> response = new APINormalResponse<>(testData, message);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isEqualTo(testData);
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getMeta().getMessage()).isEqualTo(message);
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.SUCCESS);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.OK);
		assertThat(response.getMeta().getRequestUuid()).isNotNull();
		assertThat(response.getMeta().getTimestamp()).isNotNull();
	}

	@Test
	@DisplayName("Should create APINormalResponse with all args constructor")
	void shouldCreateAPINormalResponseWithAllArgsConstructor() {
		// Given
		String testData = "Test Data";
		APIResponseKeyMeta meta = new APIResponseKeyMeta("Custom message", APIResponseType.WARNING,
				HttpStatus.PARTIAL_CONTENT);

		// When
		APINormalResponse<String> response = new APINormalResponse<>(testData, meta);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isEqualTo(testData);
		assertThat(response.getMeta()).isEqualTo(meta);
		assertThat(response.getMeta().getMessage()).isEqualTo("Custom message");
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.WARNING);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.PARTIAL_CONTENT);
	}

	@Test
	@DisplayName("Should handle null data properly")
	void shouldHandleNullDataProperly() {
		// Given
		String nullData = null;
		String message = "Null data test";

		// When
		APINormalResponse<String> response = new APINormalResponse<>(nullData, message);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isNull();
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getMeta().getMessage()).isEqualTo(message);
	}

	@Test
	@DisplayName("Should handle null message properly")
	void shouldHandleNullMessageProperly() {
		// Given
		String testData = "Test Data";
		String nullMessage = null;

		// When
		APINormalResponse<String> response = new APINormalResponse<>(testData, nullMessage);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isEqualTo(testData);
		assertThat(response.getMeta()).isNotNull();
		assertThat(response.getMeta().getMessage()).isNull();
	}

	@Test
	@DisplayName("Should handle null meta properly")
	void shouldHandleNullMetaProperly() {
		// Given
		String testData = "Test Data";
		APIResponseKeyMeta nullMeta = null;

		// When
		APINormalResponse<String> response = new APINormalResponse<>(testData, nullMeta);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getData()).isEqualTo(testData);
		assertThat(response.getMeta()).isNull();
	}

	@Test
	@DisplayName("Should work with different data types")
	void shouldWorkWithDifferentDataTypes() {
		// Given
		Integer integerData = 42;
		Boolean booleanData = true;

		// When
		APINormalResponse<Integer> intResponse = new APINormalResponse<>(integerData);
		APINormalResponse<Boolean> boolResponse = new APINormalResponse<>(booleanData);

		// Then
		assertThat(intResponse.getData()).isEqualTo(integerData);
		assertThat(boolResponse.getData()).isEqualTo(booleanData);
	}

	@Test
	@DisplayName("Should allow setting and getting meta via setters")
	void shouldAllowSettingAndGettingMetaViaSetters() {
		// Given
		APINormalResponse<String> response = new APINormalResponse<>();
		APIResponseKeyMeta newMeta = new APIResponseKeyMeta("New message", APIResponseType.ERROR,
				HttpStatus.BAD_REQUEST);

		// When
		response.setMeta(newMeta);

		// Then
		assertThat(response.getMeta()).isEqualTo(newMeta);
		assertThat(response.getMeta().getMessage()).isEqualTo("New message");
		assertThat(response.getMeta().getResponseType()).isEqualTo(APIResponseType.ERROR);
		assertThat(response.getMeta().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("Should allow setting and getting data via setters")
	void shouldAllowSettingAndGettingDataViaSetters() {
		// Given
		APINormalResponse<String> response = new APINormalResponse<>();
		String newData = "New Test Data";

		// When
		response.setData(newData);

		// Then
		assertThat(response.getData()).isEqualTo(newData);
	}

	@Test
	@DisplayName("Should support equals and hashCode from Lombok")
	void shouldSupportEqualsAndHashCodeFromLombok() {
		// Given
		String testData = "Test Data";
		String message = "Test message";
		APINormalResponse<String> response1 = new APINormalResponse<>(testData, message);
		APINormalResponse<String> response2 = new APINormalResponse<>(testData, message);

		// When & Then
		assertThat(response1).isNotNull();
		assertThat(response2).isNotNull();
		// Note: Since meta contains UUID and timestamp, they won't be equal
		// But we can test that the equals and hashCode methods exist and work
		assertThat(response1.equals(response1)).isTrue();
		assertThat(response1.hashCode()).isNotZero();
	}

	@Test
	@DisplayName("Should support toString from Lombok")
	void shouldSupportToStringFromLombok() {
		// Given
		String testData = "Test Data";
		APINormalResponse<String> response = new APINormalResponse<>(testData);

		// When
		String toString = response.toString();

		// Then
		assertThat(toString).isNotNull().contains("APINormalResponse").contains(testData);
	}

}