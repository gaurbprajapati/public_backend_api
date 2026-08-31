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

import io.recruitcrm.microservice.timesheet.helpers.GenericHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Unit tests for {@link SecurityConfiguration}. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfiguration Tests")
class SecurityConfigurationTests {

	private static final String SWAGGER_USERNAME = "swagger_user";

	private static final String SWAGGER_PASSWORD = "cinnamon-radiance-grub1";

	private static final String CUSTOM_ORIGIN = "https://custom.example.com";

	private static final String CUSTOM_ORIGIN_PATTERN = "https://custom.example.com:*";

	private CorsProperties corsProperties;

	private SecurityConfiguration securityConfiguration;

	@BeforeEach
	void setUp() {
		this.corsProperties = new CorsProperties();
		this.corsProperties.setAllowedOrigins(new ArrayList<>(List.of(CUSTOM_ORIGIN)));
		this.corsProperties.setAllowedOriginPatterns(new ArrayList<>(List.of(CUSTOM_ORIGIN_PATTERN)));

		this.securityConfiguration = new SecurityConfiguration(this.corsProperties);
		ReflectionTestUtils.setField(this.securityConfiguration, "swaggerUsername", SWAGGER_USERNAME);
		ReflectionTestUtils.setField(this.securityConfiguration, "swaggerPassword", SWAGGER_PASSWORD);
	}

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Should assign corsProperties field")
		void testConstructorSetsCorsProperties() {
			CorsProperties testProps = new CorsProperties();
			SecurityConfiguration config = new SecurityConfiguration(testProps);
			assertThat(config.corsProperties).isSameAs(testProps);
		}

	}

	@Nested
	@DisplayName("securityFilterChain Tests")
	class SecurityFilterChainTests {

		@Test
		@DisplayName("Should configure CORS, stateless sessions, CSRF, authorization, and HTTP basic")
		void testSecurityFilterChainConfiguresHttpSecurityAndBuildsChain() throws Exception {
			HttpSecurity http = mock(HttpSecurity.class, org.mockito.Answers.RETURNS_SELF);
			DefaultSecurityFilterChain expectedChain = mock(DefaultSecurityFilterChain.class);
			given(http.build()).willReturn(expectedChain);

			SecurityFilterChain result = SecurityConfigurationTests.this.securityConfiguration
				.securityFilterChain(http);

			assertThat(result).isSameAs(expectedChain);
			then(http).should().cors(any());
			then(http).should().sessionManagement(any());
			then(http).should().csrf(any());
			then(http).should().authorizeHttpRequests(any());
			then(http).should().httpBasic(any());
			then(http).should().build();
		}

	}

	@Nested
	@DisplayName("corsConfigurationSource Tests")
	class CorsConfigurationSourceTests {

		@Test
		@DisplayName("Should create non-null CorsConfigurationSource")
		void testCorsConfigurationSourceBeanCreation() {
			CorsConfigurationSource corsConfigurationSource = SecurityConfigurationTests.this.securityConfiguration
				.corsConfigurationSource();
			assertThat(corsConfigurationSource).isNotNull();
		}

		@Test
		@DisplayName("Should combine configured origins with defaults when origins are non-empty")
		void testAllowedOriginsCombinesConfiguredAndDefaultOrigins() {
			CorsConfigurationSource corsConfigurationSource = SecurityConfigurationTests.this.securityConfiguration
				.corsConfigurationSource();
			MockHttpServletRequest request = new MockHttpServletRequest();
			List<String> expected = GenericHelper.combineAndRemoveDuplicates(
					SecurityConfigurationTests.this.corsProperties.getAllowedOrigins(),
					SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);

			assertThat(
					Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request)).getAllowedOrigins())
				.containsExactlyElementsOf(expected);
		}

		@Test
		@DisplayName("Should combine configured origin patterns with defaults when patterns are non-empty")
		void testAllowedOriginPatternsCombinesConfiguredAndDefaultPatterns() {
			CorsConfigurationSource corsConfigurationSource = SecurityConfigurationTests.this.securityConfiguration
				.corsConfigurationSource();
			MockHttpServletRequest request = new MockHttpServletRequest();
			List<String> expected = GenericHelper.combineAndRemoveDuplicates(
					SecurityConfigurationTests.this.corsProperties.getAllowedOriginPatterns(),
					SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);

			assertThat(Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request))
				.getAllowedOriginPatterns()).containsExactlyElementsOf(expected);
		}

		@Test
		@DisplayName("Should allow all HTTP methods")
		void testAllowedMethods() {
			CorsConfiguration corsConfig = getCorsConfiguration();
			assertThat(corsConfig.getAllowedMethods()).containsExactly("*");
		}

		@Test
		@DisplayName("Should allow all headers")
		void testAllowedHeaders() {
			CorsConfiguration corsConfig = getCorsConfiguration();
			assertThat(corsConfig.getAllowedHeaders()).containsExactly("*");
		}

		@Test
		@DisplayName("Should allow credentials")
		void testCorsAllowsCredentials() {
			CorsConfiguration corsConfig = getCorsConfiguration();
			assertThat(corsConfig.getAllowCredentials()).isTrue();
		}

		@Test
		@DisplayName("Should expose Content-Disposition header")
		void testCorsExposedHeaders() {
			CorsConfiguration corsConfig = getCorsConfiguration();
			assertThat(corsConfig.getExposedHeaders()).contains("Content-Disposition");
		}

		@Test
		@DisplayName("Should set max age to zero")
		void testCorsMaxAge() {
			CorsConfiguration corsConfig = getCorsConfiguration();
			assertThat(corsConfig.getMaxAge()).isZero();
		}

		@Test
		@DisplayName("Should register CORS configuration for all paths")
		void testCorsConfigurationRegisteredForAllPaths() {
			CorsConfigurationSource corsConfigurationSource = SecurityConfigurationTests.this.securityConfiguration
				.corsConfigurationSource();
			MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/v1/timesheets");
			MockHttpServletRequest rootRequest = new MockHttpServletRequest("GET", "/");

			assertThat(corsConfigurationSource.getCorsConfiguration(apiRequest)).isNotNull();
			assertThat(corsConfigurationSource.getCorsConfiguration(rootRequest)).isNotNull();
		}

		@Test
		@DisplayName("Should use default origins and patterns when corsProperties lists are empty")
		void testAllowedOriginsWhenCorsPropertiesIsEmpty() {
			CorsProperties emptyCorsProperties = new CorsProperties();
			emptyCorsProperties.setAllowedOrigins(new ArrayList<>());
			emptyCorsProperties.setAllowedOriginPatterns(new ArrayList<>());

			SecurityConfiguration config = new SecurityConfiguration(emptyCorsProperties);
			MockHttpServletRequest request = new MockHttpServletRequest();
			CorsConfigurationSource corsConfigurationSource = config.corsConfigurationSource();

			assertThat(
					Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request)).getAllowedOrigins())
				.isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);
			assertThat(Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request))
				.getAllowedOriginPatterns()).isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);
		}

		@Test
		@DisplayName("Should use configured origins and patterns when both lists are non-empty")
		void testAllowedOriginsWhenCorsPropertiesIsNotEmpty() {
			CorsProperties populatedCorsProperties = new CorsProperties();
			populatedCorsProperties.setAllowedOrigins(new ArrayList<>(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS));
			populatedCorsProperties
				.setAllowedOriginPatterns(new ArrayList<>(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS));

			SecurityConfiguration config = new SecurityConfiguration(populatedCorsProperties);
			MockHttpServletRequest request = new MockHttpServletRequest();
			CorsConfigurationSource corsConfigurationSource = config.corsConfigurationSource();

			assertThat(
					Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request)).getAllowedOrigins())
				.isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);
			assertThat(Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request))
				.getAllowedOriginPatterns()).isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);
		}

		@Test
		@DisplayName("Should use default origin patterns when configured origins are non-empty and patterns are empty")
		void testCorsOriginsNonEmptyPatternsEmptyUsesDefaultPatterns() {
			CorsProperties partialCorsProperties = new CorsProperties();
			partialCorsProperties.setAllowedOrigins(new ArrayList<>(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS));
			partialCorsProperties.setAllowedOriginPatterns(new ArrayList<>());

			SecurityConfiguration config = new SecurityConfiguration(partialCorsProperties);
			MockHttpServletRequest request = new MockHttpServletRequest();
			CorsConfiguration corsConfig = config.corsConfigurationSource().getCorsConfiguration(request);

			assertThat(Objects.requireNonNull(corsConfig).getAllowedOrigins())
				.isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);
			assertThat(corsConfig.getAllowedOriginPatterns())
				.isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);
		}

		@Test
		@DisplayName("Should use default origins when configured patterns are non-empty and origins are empty")
		void testCorsOriginsEmptyPatternsNonEmptyUsesDefaultOrigins() {
			CorsProperties partialCorsProperties = new CorsProperties();
			partialCorsProperties.setAllowedOrigins(new ArrayList<>());
			partialCorsProperties
				.setAllowedOriginPatterns(new ArrayList<>(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS));

			SecurityConfiguration config = new SecurityConfiguration(partialCorsProperties);
			MockHttpServletRequest request = new MockHttpServletRequest();
			CorsConfiguration corsConfig = config.corsConfigurationSource().getCorsConfiguration(request);

			assertThat(Objects.requireNonNull(corsConfig).getAllowedOrigins())
				.isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);
			assertThat(corsConfig.getAllowedOriginPatterns())
				.isEqualTo(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);
		}

		private CorsConfiguration getCorsConfiguration() {
			CorsConfigurationSource corsConfigurationSource = SecurityConfigurationTests.this.securityConfiguration
				.corsConfigurationSource();
			MockHttpServletRequest request = new MockHttpServletRequest();
			return Objects.requireNonNull(corsConfigurationSource.getCorsConfiguration(request));
		}

	}

	@Nested
	@DisplayName("userDetailsService Tests")
	class UserDetailsServiceTests {

		@Test
		@DisplayName("Should create non-null InMemoryUserDetailsManager")
		void testUserDetailsServiceBeanCreation() {
			InMemoryUserDetailsManager userDetailsManager = SecurityConfigurationTests.this.securityConfiguration
				.userDetailsService();
			assertThat(userDetailsManager).isNotNull();
		}

		@Test
		@DisplayName("Should not contain unknown users")
		void testUserDetailsServiceUnknownUserDoesNotExist() {
			InMemoryUserDetailsManager userDetailsManager = SecurityConfigurationTests.this.securityConfiguration
				.userDetailsService();
			assertThat(userDetailsManager.userExists("unknown_nonexistent_user_xyz")).isFalse();
		}

		@Test
		@DisplayName("Should contain the configured swagger user")
		void testUserDetailsServiceContainsRegisteredUser() {
			InMemoryUserDetailsManager userDetailsManager = SecurityConfigurationTests.this.securityConfiguration
				.userDetailsService();
			UserDetails swaggerUser = userDetailsManager.loadUserByUsername(SWAGGER_USERNAME);
			assertThat(swaggerUser).isNotNull();
			assertThat(swaggerUser.getUsername()).isEqualTo(SWAGGER_USERNAME);
		}

		@Test
		@DisplayName("Should store encoded password that differs from raw password")
		void testSwaggerUserHasEncodedPassword() {
			InMemoryUserDetailsManager userDetailsManager = SecurityConfigurationTests.this.securityConfiguration
				.userDetailsService();
			UserDetails swaggerUser = userDetailsManager.loadUserByUsername(SWAGGER_USERNAME);
			assertThat(swaggerUser.getPassword()).isNotBlank();
			assertThat(swaggerUser.getPassword()).isNotEqualTo(SWAGGER_PASSWORD);
		}

		@Test
		@DisplayName("Should assign SWAGGER role authority")
		void testSwaggerUserHasSwaggerRole() {
			InMemoryUserDetailsManager userDetailsManager = SecurityConfigurationTests.this.securityConfiguration
				.userDetailsService();
			UserDetails swaggerUser = userDetailsManager.loadUserByUsername(SWAGGER_USERNAME);
			assertThat(swaggerUser.getAuthorities()).isNotEmpty();
			assertThat(swaggerUser.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_SWAGGER");
		}

		@Test
		@DisplayName("Should keep swagger user account enabled and non-expired")
		void testSwaggerUserAccountIsActive() {
			InMemoryUserDetailsManager userDetailsManager = SecurityConfigurationTests.this.securityConfiguration
				.userDetailsService();
			UserDetails swaggerUser = userDetailsManager.loadUserByUsername(SWAGGER_USERNAME);
			assertThat(swaggerUser.isEnabled()).isTrue();
			assertThat(swaggerUser.isAccountNonExpired()).isTrue();
			assertThat(swaggerUser.isAccountNonLocked()).isTrue();
			assertThat(swaggerUser.isCredentialsNonExpired()).isTrue();
		}

	}

	@Nested
	@DisplayName("Default CORS Constants Tests")
	class DefaultConstantsTests {

		@Test
		@DisplayName("DEFAULT_ALLOWED_ORIGINS should be populated")
		void testDefaultAllowedOriginsIsPopulated() {
			assertThat(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS).isNotEmpty()
				.contains("https://discord.com", "https://github.com", "https://recruitcrm.io");
		}

		@Test
		@DisplayName("DEFAULT_ALLOWED_ORIGIN_PATTERNS should be populated")
		void testDefaultAllowedOriginPatternsIsPopulated() {
			assertThat(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS).isNotEmpty()
				.contains("*.facebook.com", "*.google.com", "*.recruitcrm.io");
		}

	}

}
