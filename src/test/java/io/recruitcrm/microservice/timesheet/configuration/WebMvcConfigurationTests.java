/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.configuration.auth.PersonaAwareMfaInterceptor;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.JwtInterceptor;
import io.recruitcrm.microservice.timesheet.configuration.jobs.ReimbursementSubmissionReminderJobProperties;
import io.recruitcrm.microservice.timesheet.configuration.jobs.KafkaEventLogCleanupJobProperties;
import io.recruitcrm.microservice.timesheet.configuration.jobs.LambdaJobsApiKeyInterceptor;
import io.recruitcrm.microservice.timesheet.configuration.jobs.TimesheetSubmissionReminderJobProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * Unit tests for WebMvcConfiguration class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class WebMvcConfigurationTests {

	@Mock
	private JwtInterceptor jwtInterceptor;

	@Mock
	private PersonaAwareMfaInterceptor personaAwareMfaInterceptor;

	@Mock
	private LambdaJobsApiKeyInterceptor lambdaJobsApiKeyInterceptor;

	private static final String REMINDER_JOB_PATH = "/v1/jobs/timesheet-submission-reminders";

	private static final String REIMBURSEMENT_REMINDER_JOB_PATH = "/v1/jobs/reimbursement-submission-reminders";

	private static final String CLEANUP_JOB_PATH = "/v1/jobs/kafka-event-logs/cleanup";

	private WebMvcConfiguration webMvcConfiguration;

	@BeforeEach
	void setUp() {
		this.webMvcConfiguration = new WebMvcConfiguration(this.jwtInterceptor, this.personaAwareMfaInterceptor,
				this.lambdaJobsApiKeyInterceptor, new TimesheetSubmissionReminderJobProperties(REMINDER_JOB_PATH, ""),
				new ReimbursementSubmissionReminderJobProperties(REIMBURSEMENT_REMINDER_JOB_PATH, ""),
				new KafkaEventLogCleanupJobProperties(CLEANUP_JOB_PATH, ""));
	}

	@Nested
	@DisplayName("addInterceptors Tests")
	class AddInterceptorsTests {

		@Test
		@DisplayName("Should add JWT, MFA, and Lambda jobs interceptors with correct order and path patterns")
		void testAddInterceptorsConfiguresInterceptorsCorrectly() {
			// Given
			InterceptorRegistry mockRegistry = mock(InterceptorRegistry.class);
			InterceptorRegistration jwtRegistration = mock(InterceptorRegistration.class);
			InterceptorRegistration mfaRegistration = mock(InterceptorRegistration.class);
			InterceptorRegistration lambdaRegistration = mock(InterceptorRegistration.class);

			given(mockRegistry.addInterceptor(WebMvcConfigurationTests.this.jwtInterceptor))
				.willReturn(jwtRegistration);
			given(jwtRegistration.addPathPatterns("/**")).willReturn(jwtRegistration);
			given(jwtRegistration.excludePathPatterns(any(String[].class))).willReturn(jwtRegistration);
			given(jwtRegistration.order(0)).willReturn(jwtRegistration);
			ArgumentCaptor<String[]> jwtExcludeCaptor = ArgumentCaptor.forClass(String[].class);

			given(mockRegistry.addInterceptor(WebMvcConfigurationTests.this.personaAwareMfaInterceptor))
				.willReturn(mfaRegistration);
			given(mfaRegistration.addPathPatterns("/**")).willReturn(mfaRegistration);
			given(mfaRegistration.excludePathPatterns(any(String[].class))).willReturn(mfaRegistration);
			given(mfaRegistration.order(1)).willReturn(mfaRegistration);

			given(mockRegistry.addInterceptor(WebMvcConfigurationTests.this.lambdaJobsApiKeyInterceptor))
				.willReturn(lambdaRegistration);
			given(lambdaRegistration.addPathPatterns("/v1/jobs/**")).willReturn(lambdaRegistration);
			given(lambdaRegistration.order(2)).willReturn(lambdaRegistration);

			// When
			WebMvcConfigurationTests.this.webMvcConfiguration.addInterceptors(mockRegistry);

			// Then
			then(mockRegistry).should().addInterceptor(WebMvcConfigurationTests.this.jwtInterceptor);
			then(mockRegistry).should().addInterceptor(WebMvcConfigurationTests.this.personaAwareMfaInterceptor);
			then(mockRegistry).should().addInterceptor(WebMvcConfigurationTests.this.lambdaJobsApiKeyInterceptor);
			then(jwtRegistration).should().order(0);
			then(mfaRegistration).should().order(1);
			then(lambdaRegistration).should().addPathPatterns("/v1/jobs/**");
			then(lambdaRegistration).should().order(2);

			// JWT must NOT blanket-exclude /v1/jobs/** -- only the dedicated x-api-key
			// Lambda job paths are excluded, so contractor-facing /v1/jobs endpoints
			// still require a valid JWT.
			then(jwtRegistration).should().excludePathPatterns(jwtExcludeCaptor.capture());
			String[] excluded = jwtExcludeCaptor.getValue();
			assertThat(excluded).contains(REMINDER_JOB_PATH, CLEANUP_JOB_PATH, REIMBURSEMENT_REMINDER_JOB_PATH)
				.doesNotContain("/v1/jobs/**", "/v1/jobs/get-timesheet-enabled-assigned-candidates");
		}

	}

}
