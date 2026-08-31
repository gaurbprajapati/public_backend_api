/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration;

import io.recruitcrm.microservice.timesheet.configuration.auth.PersonaAwareMfaInterceptor;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.JwtInterceptor;
import io.recruitcrm.microservice.timesheet.configuration.jobs.KafkaEventLogCleanupJobProperties;
import io.recruitcrm.microservice.timesheet.configuration.jobs.LambdaJobsApiKeyInterceptor;
import io.recruitcrm.microservice.timesheet.configuration.jobs.ReimbursementSubmissionReminderJobProperties;
import io.recruitcrm.microservice.timesheet.configuration.jobs.TimesheetSubmissionReminderJobProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

	private static final String[] BASE_EXCLUDED_PATHS = { "/login", "/v2/flagsmith/webhook", "/v3/api-docs/**",
			"/swagger-ui.html", "/swagger-ui/**" };

	private final JwtInterceptor jwtInterceptor;

	private final PersonaAwareMfaInterceptor personaAwareMfaInterceptor;

	private final LambdaJobsApiKeyInterceptor lambdaJobsApiKeyInterceptor;

	/**
	 * Routes excluded from JWT and MFA: the {@link #BASE_EXCLUDED_PATHS} plus the
	 * {@code x-api-key}-protected Lambda job paths (kept in sync with
	 * {@link LambdaJobsApiKeyInterceptor} by deriving them from the same properties).
	 */
	private final String[] excludedPaths;

	@Autowired
	public WebMvcConfiguration(JwtInterceptor jwtInterceptor, PersonaAwareMfaInterceptor personaAwareMfaInterceptor,
			LambdaJobsApiKeyInterceptor lambdaJobsApiKeyInterceptor,
			TimesheetSubmissionReminderJobProperties reminderJobProperties,
			ReimbursementSubmissionReminderJobProperties reimbursementJobProperties,
			KafkaEventLogCleanupJobProperties cleanupJobProperties) {
		this.jwtInterceptor = jwtInterceptor;
		this.personaAwareMfaInterceptor = personaAwareMfaInterceptor;
		this.lambdaJobsApiKeyInterceptor = lambdaJobsApiKeyInterceptor;
		this.excludedPaths = buildExcludedPaths(reminderJobProperties.protectedPath(),
				reimbursementJobProperties.protectedPath(), cleanupJobProperties.protectedPath());
	}

	private static String[] buildExcludedPaths(String... lambdaJobPaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(BASE_EXCLUDED_PATHS));
		paths.addAll(Arrays.asList(lambdaJobPaths));
		return paths.toArray(new String[0]);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		// Order 0: JWT authentication (sets AuthHolder with principal)
		registry.addInterceptor(this.jwtInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(this.excludedPaths)
			.order(0);

		// Order 1: Persona-aware MFA (only applies to USER personas)
		// Contractors and Contacts skip MFA (they use Keycloak MFA)
		registry.addInterceptor(this.personaAwareMfaInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(this.excludedPaths)
			.order(1);

		// Order 2: Lambda job shared-secret (x-api-key) for dedicated /v1/jobs routes
		// only
		registry.addInterceptor(this.lambdaJobsApiKeyInterceptor).addPathPatterns("/v1/jobs/**").order(2);
	}

}
