package io.recruitcrm.microservice.timesheet.responses;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class HttpStatusDeserializer extends StdDeserializer<HttpStatus> {

	private static final long serialVersionUID = 1L;

	public HttpStatusDeserializer() {
		super(HttpStatus.class);
	}

	@Override
	public HttpStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {
		return HttpStatus.valueOf(jsonParser.getIntValue());
	}

}
