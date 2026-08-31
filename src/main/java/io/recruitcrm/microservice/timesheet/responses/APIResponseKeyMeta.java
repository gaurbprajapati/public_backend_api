package io.recruitcrm.microservice.timesheet.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
public class APIResponseKeyMeta {

	protected String message;

	protected String requestUuid;

	protected APIResponseType responseType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "UTC")
	protected LocalDateTime timestamp;

	@JsonSerialize(using = HttpStatusSerializer.class)
	@JsonDeserialize(using = HttpStatusDeserializer.class)
	protected HttpStatus status;

	public APIResponseKeyMeta(String message) {
		this(message, UUID.randomUUID().toString(), APIResponseType.SUCCESS, LocalDateTime.now(), HttpStatus.OK);
	}

	public APIResponseKeyMeta() {
		this(null, UUID.randomUUID().toString(), APIResponseType.SUCCESS, LocalDateTime.now(), HttpStatus.OK);
	}

	public APIResponseKeyMeta(String message, APIResponseType responseType) {
		this(message, UUID.randomUUID().toString(), responseType, LocalDateTime.now(), HttpStatus.OK);
	}

	public APIResponseKeyMeta(APIResponseType responseType, HttpStatus status) {
		this(null, UUID.randomUUID().toString(), responseType, LocalDateTime.now(), status);
	}

	public APIResponseKeyMeta(String message, APIResponseType responseType, HttpStatus status) {
		this(message, UUID.randomUUID().toString(), responseType, LocalDateTime.now(), status);
	}

}
