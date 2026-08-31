/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.MultiFactorAuthenticationJwtInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Wrapper interceptor that applies MFA only to USER personas. Contractors and Contacts
 * authenticate via Keycloak (which has its own MFA).
 */
@Component
public class PersonaAwareMfaInterceptor implements HandlerInterceptor {

	private final MultiFactorAuthenticationJwtInterceptor mfaInterceptor;

	private final AuthHolder authHolder;

	public PersonaAwareMfaInterceptor(MultiFactorAuthenticationJwtInterceptor mfaInterceptor, AuthHolder authHolder) {
		this.mfaInterceptor = mfaInterceptor;
		this.authHolder = authHolder;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// Check if a unified principal is set
		if (!this.authHolder.hasUnifiedPrincipal()) {
			// No unified principal means legacy authentication or not authenticated yet
			// Let MFA interceptor handle it (will apply to legacy users)
			return this.mfaInterceptor.preHandle(request, response, handler);
		}

		// Get principal type
		PrincipalType principalType = this.authHolder.getPrincipalType();

		// Only apply MFA to USER personas
		// Contractors and Contacts authenticate via Keycloak (which has its own MFA)
		if (principalType == PrincipalType.USER) {
			return this.mfaInterceptor.preHandle(request, response, handler);
		}

		// Skip MFA for contractors and contacts
		return true;
	}

}