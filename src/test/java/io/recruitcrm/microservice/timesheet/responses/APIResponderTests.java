package io.recruitcrm.microservice.timesheet.responses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class APIResponderTests {

	private APIResponder apiResponder;

	@BeforeEach
	void setUp() {
		this.apiResponder = new APIResponder();
	}

	@Test
	@DisplayName("Should create APIResponder instance")
	void shouldCreateAPIResponderInstance() {
		assertThat(this.apiResponder).isNotNull();
	}

	@Test
	@DisplayName("Respond by using API Normal Response")
	void respondByUsingAPINormalResponse() {
		// Arrange
		String responseData = "TIMESHEETS";
		APINormalResponse<String> apiNormalResponse = new APINormalResponse<>(responseData);

		// Act
		ResponseEntity<APINormalResponse<String>> response = this.apiResponder.respond(apiNormalResponse);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isNull();
		assertThat(response.getBody().getData()).isEqualTo(responseData);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond by using data")
	void respondByUsingData() {
		// Arrange
		String responseData = "TIMESHEETS";

		// Act
		ResponseEntity<APINormalResponse<String>> response = this.apiResponder.respond(responseData);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isNull();
		assertThat(response.getBody().getData()).isEqualTo(responseData);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond by using data and message")
	void respondByUsingDataAndMessage() {
		// Arrange
		String responseData = "TIMESHEETS";
		String message = "Timesheets fetched successfully";

		// Act
		ResponseEntity<APINormalResponse<String>> response = this.apiResponder.respond(responseData, message);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();
		assertThat(response.getBody().getData()).isEqualTo(responseData);
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo(message);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond by using data, message, APIResponseType and HttpStatus")
	void respondByUsingDataMessageAPIResponseTypeAndHttpStatus() {
		// Arrange
		String responseData = "TIMESHEETS";
		String message = "Timesheets fetched successfully";
		APIResponseType apiResponseType = APIResponseType.SUCCESS;
		HttpStatus httpStatus = HttpStatus.OK;

		// Act
		ResponseEntity<APINormalResponse<String>> response = this.apiResponder.respond(responseData, message,
				apiResponseType, httpStatus);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();
		assertThat(response.getBody().getData()).isEqualTo(responseData);
		assertThat(response.getBody().getMeta().getMessage()).isEqualTo(message);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond by using data, APIResponseType and HttpStatus")
	void respondByUsingDataAPIResponseTypeAndHttpStatus() {
		// Arrange
		String responseData = "TIMESHEETS";
		APIResponseType apiResponseType = APIResponseType.SUCCESS;
		HttpStatus httpStatus = HttpStatus.OK;

		// Act
		ResponseEntity<APINormalResponse<String>> response = this.apiResponder.respond(responseData, apiResponseType,
				httpStatus);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();
		assertThat(response.getBody().getMeta().getMessage()).isNull();
		assertThat(response.getBody().getData()).isEqualTo(responseData);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond with error by using APIErrorResponse")
	void respondWithErrorByUsingAPIErrorResponse() {
		// Arrange
		String exceptionMessage = "Error occurred";
		APIErrorResponse apiErrorResponse = new APIErrorResponse(new Exception(exceptionMessage));
		List<APIResponseKeyError> errors = List.of(new APIResponseKeyError(new Exception(exceptionMessage)));

		// Act
		ResponseEntity<APIErrorResponse> response = this.apiResponder.respondWithError(apiErrorResponse);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getMeta().getMessage()).isNull();
		assertThat(response.getBody().getMeta().getMessage()).isNull();
		assertThat(response.getBody().getErrors()).containsExactlyElementsOf(errors);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond with error by using Throwable")
	void respondWithErrorByUsingThrowable() {
		// Arrange
		String exceptionMessage = "Error occurred";
		List<APIResponseKeyError> errors = List.of(new APIResponseKeyError(new Exception(exceptionMessage)));

		// Act
		ResponseEntity<APIErrorResponse> response = this.apiResponder.respondWithError(new Exception(exceptionMessage));

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getMeta().getMessage()).isNull();
		assertThat(response.getBody().getMeta().getMessage()).isNull();
		assertThat(response.getBody().getErrors()).containsExactlyElementsOf(errors);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

	@Test
	@DisplayName("Respond with error by using Throwable, APIResponseType and HttpStatus")
	void respondWithErrorByUsingThrowableAPIResponseTypeAndHttpStatus() {
		// Arrange
		String exceptionMessage = "Error occurred";
		List<APIResponseKeyError> errors = List.of(new APIResponseKeyError(new Exception(exceptionMessage)));
		APIResponseType apiResponseType = APIResponseType.ERROR;
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

		// Act
		ResponseEntity<APIErrorResponse> response = this.apiResponder.respondWithError(new Exception(exceptionMessage),
				apiResponseType, httpStatus);

		// Assert
		assertThat(Objects.requireNonNull(response.getBody()).getMeta().getMessage()).isNull();
		assertThat(response.getBody().getMeta().getMessage()).isNull();
		assertThat(response.getBody().getErrors()).containsExactlyElementsOf(errors);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().getMeta().requestUuid).isNotNull();
	}

}
