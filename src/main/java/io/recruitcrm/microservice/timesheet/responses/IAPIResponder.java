package io.recruitcrm.microservice.timesheet.responses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface IAPIResponder {

	<T> ResponseEntity<APINormalResponse<T>> respond(APINormalResponse<T> response);

	<T> ResponseEntity<APINormalResponse<T>> respond(T data);

	<T> ResponseEntity<APINormalResponse<T>> respond(T data, String message);

	<T> ResponseEntity<APINormalResponse<T>> respond(T data, String message, APIResponseType responseType,
			HttpStatus status);

	<T> ResponseEntity<APINormalResponse<T>> respond(T data, APIResponseType responseType, HttpStatus status);

	ResponseEntity<APIErrorResponse> respondWithError(APIErrorResponse response);

	ResponseEntity<APIErrorResponse> respondWithError(Throwable exception);

	ResponseEntity<APIErrorResponse> respondWithError(Throwable exception, APIResponseType responseType,
			HttpStatus status);

}
