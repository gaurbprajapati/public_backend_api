package io.recruitcrm.microservice.timesheet.responses;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class HttpStatusSerializer extends StdSerializer<HttpStatus> {

	private static final long serialVersionUID = 1L;

	public HttpStatusSerializer() {
		super(HttpStatus.class);
	}

	@Override
	public void serialize(HttpStatus httpStatus, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {
		jsonGenerator.writeNumber(httpStatus.value());
	}

}
