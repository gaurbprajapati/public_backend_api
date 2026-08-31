package io.recruitcrm.microservice.timesheet.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class CorsProperties {

	@Value("${application.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Value("${application.cors.allowed-origin-patterns}")
	private List<String> allowedOriginPatterns;

}