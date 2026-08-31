package io.recruitcrm.microservice.timesheet.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Getter
@Setter
@AllArgsConstructor
public class APIErrorResponse extends APINormalResponse<Object> {

	protected List<APIResponseKeyError> errors;

	public APIErrorResponse() {
		super(new APIResponseKeyMeta(APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
		this.errors = null;
	}

	public APIErrorResponse(Throwable exception) {
		super(new APIResponseKeyMeta(APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
		this.errors = List.of(new APIResponseKeyError(exception));
	}

	public APIErrorResponse(Throwable exception, HttpStatus httpStatus) {
		super(new APIResponseKeyMeta(APIResponseType.ERROR, httpStatus));
		this.errors = List.of(new APIResponseKeyError(exception));
	}

	public APIErrorResponse(List<APIResponseKeyError> errors, HttpStatus httpStatus) {
		super(new APIResponseKeyMeta(APIResponseType.ERROR, httpStatus));
		this.errors = errors;
	}

	public APIErrorResponse(String data, String message, APIResponseType responseType, HttpStatus httpStatus) {
		super(data, new APIResponseKeyMeta(message, responseType, httpStatus));
		this.errors = List.of();
	}

}