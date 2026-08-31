package io.recruitcrm.microservice;

import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.configuration.JooqORMConfiguration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.text.MessageFormat;

@SpringBootApplication
@EntityScan(basePackages = { "io.recruitcrm.microservice", "io.recruitcrm.s3.model" })
@EnableJpaRepositories(basePackages = { "io.recruitcrm.microservice", "io.recruitcrm.s3.repository" })
@OpenAPIDefinition(
		info = @Info(title = "Timesheet Microservice API", version = "1.2",
				description = "API documentation for the Timesheet Microservice"),
		security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class TimesheetApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimesheetApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(JooqORMConfiguration jooqORMConfiguration,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		return (args) -> {
			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			logger.logInfo(MessageFormat.format("GenerationTool.generate() method executed in {0} ms", duration));
		};
	}

}
