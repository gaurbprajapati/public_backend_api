package io.recruitcrm.microservice.timesheet.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum APIResponseType {

	ERROR(101, "Error while processing request"), INFO(102, "Informational"), SUCCESS(103, "Request is successful"),
	WARNING(104, "Warning"),;

	private final String context;

	private final Integer code;

	APIResponseType(Integer code, String context) {
		this.code = code;
		this.context = context;
	}

}
