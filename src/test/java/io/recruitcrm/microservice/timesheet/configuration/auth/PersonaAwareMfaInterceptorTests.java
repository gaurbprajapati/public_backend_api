/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.MultiFactorAuthenticationJwtInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Unit tests for PersonaAwareMfaInterceptor class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class PersonaAwareMfaInterceptorTests {

	@Mock
	private MultiFactorAuthenticationJwtInterceptor mfaInterceptor;

	@Mock
	private AuthHolder authHolder;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Object handler;

	@Mock
	private AuthPrincipal unifiedPrincipal;

	@InjectMocks
	private PersonaAwareMfaInterceptor personaAwareMfaInterceptor;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("PreHandle should delegate to MFA interceptor when no unified principal")
	void testPreHandleNoUnifiedPrincipalDelegatesToMfaInterceptor() throws Exception {
		// Given
		given(this.authHolder.hasUnifiedPrincipal()).willReturn(false);
		given(this.mfaInterceptor.preHandle(this.request, this.response, this.handler)).willReturn(true);

		// When
		boolean result = this.personaAwareMfaInterceptor.preHandle(this.request, this.response, this.handler);

		// Then
		assertThat(result).isTrue();
		then(this.authHolder).should().hasUnifiedPrincipal();
		then(this.mfaInterceptor).should().preHandle(this.request, this.response, this.handler);
	}

	@Test
	@DisplayName("PreHandle should delegate to MFA interceptor when principal type is USER")
	void testPreHandleUserPrincipalTypeDelegatesToMfaInterceptor() throws Exception {
		// Given
		given(this.authHolder.hasUnifiedPrincipal()).willReturn(true);
		given(this.authHolder.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.mfaInterceptor.preHandle(this.request, this.response, this.handler)).willReturn(true);

		// When
		boolean result = this.personaAwareMfaInterceptor.preHandle(this.request, this.response, this.handler);

		// Then
		assertThat(result).isTrue();
		then(this.authHolder).should().hasUnifiedPrincipal();
		then(this.authHolder).should().getPrincipalType();
		then(this.mfaInterceptor).should().preHandle(this.request, this.response, this.handler);
	}

	@Test
	@DisplayName("PreHandle should skip MFA when principal type is CONTRACTOR")
	void testPreHandleContractorPrincipalTypeSkipsMfa() throws Exception {
		// Given
		given(this.authHolder.hasUnifiedPrincipal()).willReturn(true);
		given(this.authHolder.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		// When
		boolean result = this.personaAwareMfaInterceptor.preHandle(this.request, this.response, this.handler);

		// Then
		assertThat(result).isTrue();
		then(this.authHolder).should().hasUnifiedPrincipal();
		then(this.authHolder).should().getPrincipalType();
		then(this.mfaInterceptor).should(org.mockito.Mockito.never())
			.preHandle(this.request, this.response, this.handler);
	}

	@Test
	@DisplayName("PreHandle should skip MFA when principal type is CONTACT")
	void testPreHandleContactPrincipalTypeSkipsMfa() throws Exception {
		// Given
		given(this.authHolder.hasUnifiedPrincipal()).willReturn(true);
		given(this.authHolder.getPrincipalType()).willReturn(PrincipalType.CONTACT);

		// When
		boolean result = this.personaAwareMfaInterceptor.preHandle(this.request, this.response, this.handler);

		// Then
		assertThat(result).isTrue();
		then(this.authHolder).should().hasUnifiedPrincipal();
		then(this.authHolder).should().getPrincipalType();
		then(this.mfaInterceptor).should(org.mockito.Mockito.never())
			.preHandle(this.request, this.response, this.handler);
	}

}
