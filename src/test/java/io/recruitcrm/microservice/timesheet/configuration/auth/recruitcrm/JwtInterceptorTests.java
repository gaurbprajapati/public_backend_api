/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for JwtInterceptor class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class JwtInterceptorTests {

	@Mock
	private IJwtInterceptorHelper interceptorHelper;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private AuthHolder authHolder;

	private JwtInterceptor jwtInterceptor;

	@BeforeEach
	void setUp() {
		this.jwtInterceptor = new JwtInterceptor(this.interceptorHelper, this.authHolder);
	}

	// ========== preHandle Tests ==========

	@Nested
	@DisplayName("preHandle Tests")
	class PreHandleTests {

		@Test
		@DisplayName("Should return true immediately when principal already present")
		void testPreHandlePrincipalAlreadyPresentReturnsTrue() throws Exception {
			// Given
			Object handler = new Object();

			given(JwtInterceptorTests.this.authHolder.hasPrincipal()).willReturn(true);

			// When
			boolean result = JwtInterceptorTests.this.jwtInterceptor.preHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler);

			// Then
			assertThat(result).isTrue();
			then(JwtInterceptorTests.this.interceptorHelper).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("Should return true when valid bearer token is present")
		void testPreHandleValidBearerTokenReturnsTrue() throws Exception {
			// Given
			String authHeader = "Bearer validToken123";
			Object handler = new Object();

			given(JwtInterceptorTests.this.request.getHeader("Authorization")).willReturn(authHeader);
			given(JwtInterceptorTests.this.interceptorHelper.handleWithBearerToken(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, authHeader))
				.willReturn(true);

			// When
			boolean result = JwtInterceptorTests.this.jwtInterceptor.preHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler);

			// Then
			assertThat(result).isTrue();
			then(JwtInterceptorTests.this.interceptorHelper).should()
				.handleWithBearerToken(JwtInterceptorTests.this.request, JwtInterceptorTests.this.response, authHeader);
		}

		@Test
		@DisplayName("Should return false when bearer token validation fails")
		void testPreHandleInvalidBearerTokenReturnsFalse() throws Exception {
			// Given
			String authHeader = "Bearer invalidToken";
			Object handler = new Object();

			given(JwtInterceptorTests.this.request.getHeader("Authorization")).willReturn(authHeader);
			given(JwtInterceptorTests.this.interceptorHelper.handleWithBearerToken(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, authHeader))
				.willReturn(false);

			// When
			boolean result = JwtInterceptorTests.this.jwtInterceptor.preHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false and send error when Authorization header is null")
		void testPreHandleNullAuthHeaderReturnsFalse() throws Exception {
			// Given
			Object handler = new Object();

			given(JwtInterceptorTests.this.request.getHeader("Authorization")).willReturn(null);

			// When
			boolean result = JwtInterceptorTests.this.jwtInterceptor.preHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler);

			// Then
			assertThat(result).isFalse();
			then(JwtInterceptorTests.this.interceptorHelper).should()
				.sendErrorResponse(JwtInterceptorTests.this.response, "Missing bearer token in header");
		}

		@Test
		@DisplayName("Should return false and send error when Authorization header does not start with Bearer")
		void testPreHandleNonBearerAuthHeaderReturnsFalse() throws Exception {
			// Given
			String authHeader = "Basic someCredentials";
			Object handler = new Object();

			given(JwtInterceptorTests.this.request.getHeader("Authorization")).willReturn(authHeader);

			// When
			boolean result = JwtInterceptorTests.this.jwtInterceptor.preHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler);

			// Then
			assertThat(result).isFalse();
			then(JwtInterceptorTests.this.interceptorHelper).should()
				.sendErrorResponse(JwtInterceptorTests.this.response, "Missing bearer token in header");
		}

	}

	// ========== postHandle Tests ==========

	@Nested
	@DisplayName("postHandle Tests")
	class PostHandleTests {

		@Test
		@DisplayName("Should complete without exception")
		void testPostHandleCompletesSuccessfully() {
			// Given
			Object handler = new Object();
			ModelAndView modelAndView = mock(ModelAndView.class);

			// When & Then - Should not throw exception
			assertThatCode(() -> JwtInterceptorTests.this.jwtInterceptor.postHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler, modelAndView))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Should complete without exception when modelAndView is null")
		void testPostHandleNullModelAndViewCompletesSuccessfully() {
			// Given
			Object handler = new Object();

			// When & Then - Should not throw exception
			assertThatCode(() -> JwtInterceptorTests.this.jwtInterceptor.postHandle(JwtInterceptorTests.this.request,
					JwtInterceptorTests.this.response, handler, null))
				.doesNotThrowAnyException();
		}

	}

	// ========== afterCompletion Tests ==========

	@Nested
	@DisplayName("afterCompletion Tests")
	class AfterCompletionTests {

		@Test
		@DisplayName("Should complete without exception")
		void testAfterCompletionCompletesSuccessfully() {
			// Given
			Object handler = new Object();

			// When & Then - Should not throw exception
			assertThatCode(() -> JwtInterceptorTests.this.jwtInterceptor
				.afterCompletion(JwtInterceptorTests.this.request, JwtInterceptorTests.this.response, handler, null))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Should complete without exception when exception is present")
		void testAfterCompletionWithExceptionCompletesSuccessfully() {
			// Given
			Object handler = new Object();
			Exception exception = new RuntimeException("Test exception");

			// When & Then - Should not throw exception
			assertThatCode(
					() -> JwtInterceptorTests.this.jwtInterceptor.afterCompletion(JwtInterceptorTests.this.request,
							JwtInterceptorTests.this.response, handler, exception))
				.doesNotThrowAnyException();
		}

	}

}
