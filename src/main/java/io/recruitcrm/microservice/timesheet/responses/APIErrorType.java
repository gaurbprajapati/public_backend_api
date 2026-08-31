package io.recruitcrm.microservice.timesheet.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum APIErrorType {

	VALIDATION_ERROR("Validation Error", 201), GENERIC_ERROR("Generic Error", 202);

	private final String context;

	private final Integer code;

	APIErrorType(String context, Integer code) {
		this.context = context;
		this.code = code;
	}

}
