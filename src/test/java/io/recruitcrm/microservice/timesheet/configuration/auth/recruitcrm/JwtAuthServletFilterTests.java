/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthServletFilter Tests")
class JwtAuthServletFilterTests {

	@Mock
	private IJwtInterceptorHelper interceptorHelper;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain chain;

	@Test
	@DisplayName("Continues the chain when Authorization header is absent")
	void testContinuesChainWhenAuthorizationHeaderIsAbsent() throws Exception {
		JwtAuthServletFilter filter = new JwtAuthServletFilter(this.interceptorHelper);
		given(this.request.getHeader("Authorization")).willReturn(null);

		filter.doFilterInternal(this.request, this.response, this.chain);

		then(this.interceptorHelper).shouldHaveNoInteractions();
		then(this.chain).should().doFilter(this.request, this.response);
	}

	@Test
	@DisplayName("Continues the chain when Authorization header does not start with Bearer")
	void testContinuesChainWhenAuthorizationHeaderIsNotBearer() throws Exception {
		JwtAuthServletFilter filter = new JwtAuthServletFilter(this.interceptorHelper);
		given(this.request.getHeader("Authorization")).willReturn("Basic abc123");

		filter.doFilterInternal(this.request, this.response, this.chain);

		then(this.interceptorHelper).shouldHaveNoInteractions();
		then(this.chain).should().doFilter(this.request, this.response);
	}

	@Test
	@DisplayName("Stops the chain when bearer token handling returns null")
	void testStopsChainWhenBearerTokenHandlingReturnsNull() throws Exception {
		JwtAuthServletFilter filter = new JwtAuthServletFilter(this.interceptorHelper);
		given(this.request.getHeader("Authorization")).willReturn("Bearer token123");
		given(this.interceptorHelper.handleWithBearerToken(this.request, this.response, "Bearer token123"))
			.willReturn(null);

		filter.doFilterInternal(this.request, this.response, this.chain);

		then(this.chain).should(never()).doFilter(this.request, this.response);
	}

	@Test
	@DisplayName("Stops the chain when bearer token handling returns false")
	void testStopsChainWhenBearerTokenHandlingReturnsFalse() throws Exception {
		JwtAuthServletFilter filter = new JwtAuthServletFilter(this.interceptorHelper);
		given(this.request.getHeader("Authorization")).willReturn("Bearer token123");
		given(this.interceptorHelper.handleWithBearerToken(this.request, this.response, "Bearer token123"))
			.willReturn(false);

		filter.doFilterInternal(this.request, this.response, this.chain);

		then(this.chain).should(never()).doFilter(this.request, this.response);
	}

	@Test
	@DisplayName("Continues the chain when bearer token handling returns true")
	void testContinuesChainWhenBearerTokenHandlingReturnsTrue() throws Exception {
		JwtAuthServletFilter filter = new JwtAuthServletFilter(this.interceptorHelper);
		given(this.request.getHeader("Authorization")).willReturn("Bearer token123");
		given(this.interceptorHelper.handleWithBearerToken(this.request, this.response, "Bearer token123"))
			.willReturn(true);

		filter.doFilterInternal(this.request, this.response, this.chain);

		then(this.chain).should().doFilter(this.request, this.response);
	}

}
