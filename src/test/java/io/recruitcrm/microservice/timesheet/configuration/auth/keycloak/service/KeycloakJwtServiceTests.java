/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for KeycloakJwtService class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakJwtServiceTests {

	private static final String DEFAULT_ISSUER_URI = "http://localhost:8080/realms/test";

	private static final String DEFAULT_SUBJECT = "user-123";

	private static final String DEFAULT_EMAIL = "test@example.com";

	private static final String DEFAULT_PREFERRED_USERNAME = "testuser";

	@Mock
	private Jwt jwt;

	@Mock
	private JwtDecoder jwtDecoder;

	private KeycloakJwtService keycloakJwtService;

	@BeforeEach
	void setUp() {
		this.keycloakJwtService = new KeycloakJwtService();
		ReflectionTestUtils.setField(this.keycloakJwtService, "issuerUri", DEFAULT_ISSUER_URI);
	}

	@Nested
	@DisplayName("isKeycloakEnabled Tests")
	class IsKeycloakEnabledTests {

		@Test
		@DisplayName("Should return false when keycloakEnabled is false")
		void testIsKeycloakEnabledDisabledReturnsFalse() {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "keycloakEnabled", false);

			// When
			boolean result = KeycloakJwtServiceTests.this.keycloakJwtService.isKeycloakEnabled();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return true when keycloakEnabled is true")
		void testIsKeycloakEnabledEnabledReturnsTrue() {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "keycloakEnabled", true);

			// When
			boolean result = KeycloakJwtServiceTests.this.keycloakJwtService.isKeycloakEnabled();

			// Then
			assertThat(result).isTrue();
		}

	}

	@Nested
	@DisplayName("getIssuerUri Tests")
	class GetIssuerUriTests {

		@Test
		@DisplayName("Should return configured issuer URI")
		void testGetIssuerUriReturnsConfiguredUri() {
			// When
			String result = KeycloakJwtServiceTests.this.keycloakJwtService.getIssuerUri();

			// Then
			assertThat(result).isEqualTo(DEFAULT_ISSUER_URI);
		}

	}

	@Nested
	@DisplayName("getJwtDecoder Tests")
	class GetJwtDecoderTests {

		private static Stream<Arguments> getJwtDecoderTestCases() {
			return Stream.of(
					Arguments.of(null, null, "Should throw IllegalStateException when no configuration provided"),
					Arguments.of("", null,
							"Should throw IllegalStateException when jwkSetUri is empty and publicKey is null"),
					Arguments.of("", "",
							"Should throw IllegalStateException when both jwkSetUri and publicKey are empty"));
		}

		@ParameterizedTest(name = "{2}")
		@MethodSource("getJwtDecoderTestCases")
		@DisplayName("Should throw IllegalStateException for invalid configurations")
		void testGetJwtDecoderInvalidConfigurationsThrowIllegalStateException(String jwkSetUri, String publicKey,
				String testDescription) {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "jwkSetUri", jwkSetUri);
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "keycloakPublicKey",
					publicKey);

			// When & Then
			assertThatThrownBy(() -> KeycloakJwtServiceTests.this.keycloakJwtService.getJwtDecoder())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Keycloak JWT decoder configuration missing. Provide either public-key or jwk-set-uri.");
		}

		@Test
		@DisplayName("Should build decoder using jwk-set-uri when configured")
		void testGetJwtDecoderWithJwkSetUriReturnsDecoder() {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "jwkSetUri",
					"http://localhost:8080/realms/test/protocol/openid-connect/certs");

			// When
			JwtDecoder result = KeycloakJwtServiceTests.this.keycloakJwtService.getJwtDecoder();

			// Then
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Should build decoder using public key when configured")
		void testGetJwtDecoderWithPublicKeyReturnsDecoder() throws Exception {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "keycloakPublicKey",
					generatePublicKeyPem());

			// When
			JwtDecoder result = KeycloakJwtServiceTests.this.keycloakJwtService.getJwtDecoder();

			// Then
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Should return cached decoder on subsequent calls")
		void testGetJwtDecoderCachedReturnsSameDecoder() {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "jwkSetUri",
					"http://localhost:8080/realms/test/protocol/openid-connect/certs");
			JwtDecoder first = KeycloakJwtServiceTests.this.keycloakJwtService.getJwtDecoder();

			// When
			JwtDecoder second = KeycloakJwtServiceTests.this.keycloakJwtService.getJwtDecoder();

			// Then
			assertThat(second).isSameAs(first);
		}

		@Test
		@DisplayName("Should throw IllegalStateException when public key is invalid")
		void testGetJwtDecoderInvalidPublicKeyThrowsIllegalStateException() {
			// Given
			String invalidKey = "-----BEGIN PUBLIC KEY-----\n"
					+ Base64.getEncoder().encodeToString("not-a-valid-key".getBytes()) + "\n-----END PUBLIC KEY-----";
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "keycloakPublicKey",
					invalidKey);

			// When & Then
			assertThatThrownBy(() -> KeycloakJwtServiceTests.this.keycloakJwtService.getJwtDecoder())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Failed to parse Keycloak public key");
		}

		private String generatePublicKeyPem() throws Exception {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
			return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
		}

	}

	@Nested
	@DisplayName("validateToken Tests")
	class ValidateTokenTests {

		@Test
		@DisplayName("Should return decoded JWT when token is valid")
		void testValidateTokenValidTokenReturnsJwt() {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "jwtDecoder",
					KeycloakJwtServiceTests.this.jwtDecoder);
			given(KeycloakJwtServiceTests.this.jwtDecoder.decode("valid-token"))
				.willReturn(KeycloakJwtServiceTests.this.jwt);

			// When
			Jwt result = KeycloakJwtServiceTests.this.keycloakJwtService.validateToken("valid-token");

			// Then
			assertThat(result).isEqualTo(KeycloakJwtServiceTests.this.jwt);
		}

		@Test
		@DisplayName("Should rethrow JwtException when token validation fails")
		void testValidateTokenInvalidTokenThrowsJwtException() {
			// Given
			ReflectionTestUtils.setField(KeycloakJwtServiceTests.this.keycloakJwtService, "jwtDecoder",
					KeycloakJwtServiceTests.this.jwtDecoder);
			given(KeycloakJwtServiceTests.this.jwtDecoder.decode("invalid-token"))
				.willThrow(new JwtException("malformed"));

			// When & Then
			assertThatThrownBy(() -> KeycloakJwtServiceTests.this.keycloakJwtService.validateToken("invalid-token"))
				.isInstanceOf(JwtException.class)
				.hasMessageContaining("Keycloak token validation failed: malformed");
		}

	}

	@Nested
	@DisplayName("extractUserId Tests")
	class ExtractUserIdTests {

		@Test
		@DisplayName("Should return subject from JWT")
		void testExtractUserIdValidJwtReturnsSubject() {
			// Given
			given(KeycloakJwtServiceTests.this.jwt.getSubject()).willReturn(DEFAULT_SUBJECT);

			// When
			String result = KeycloakJwtServiceTests.this.keycloakJwtService
				.extractUserId(KeycloakJwtServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(DEFAULT_SUBJECT);
		}

	}

	@Nested
	@DisplayName("extractEmail Tests")
	class ExtractEmailTests {

		@Test
		@DisplayName("Should return email from JWT")
		void testExtractEmailValidJwtReturnsEmail() {
			// Given
			given(KeycloakJwtServiceTests.this.jwt.getClaimAsString("email")).willReturn(DEFAULT_EMAIL);

			// When
			String result = KeycloakJwtServiceTests.this.keycloakJwtService
				.extractEmail(KeycloakJwtServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(DEFAULT_EMAIL);
		}

	}

	@Nested
	@DisplayName("extractPreferredUsername Tests")
	class ExtractPreferredUsernameTests {

		@Test
		@DisplayName("Should return preferred_username from JWT")
		void testExtractPreferredUsernameValidJwtReturnsPreferredUsername() {
			// Given
			given(KeycloakJwtServiceTests.this.jwt.getClaimAsString("preferred_username"))
				.willReturn(DEFAULT_PREFERRED_USERNAME);

			// When
			String result = KeycloakJwtServiceTests.this.keycloakJwtService
				.extractPreferredUsername(KeycloakJwtServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(DEFAULT_PREFERRED_USERNAME);
		}

	}

	@Nested
	@DisplayName("extractRealmRoles Tests")
	class ExtractRealmRolesTests {

		@Test
		@DisplayName("Should return roles when realm_access claim exists with roles")
		void testExtractRealmRolesValidRealmAccessReturnsRoles() {
			// Given
			List<String> roles = List.of("user", "admin");
			Map<String, Object> realmAccess = new HashMap<>();
			realmAccess.put("roles", roles);
			given(KeycloakJwtServiceTests.this.jwt.getClaimAsMap("realm_access")).willReturn(realmAccess);

			// When
			List<String> result = KeycloakJwtServiceTests.this.keycloakJwtService
				.extractRealmRoles(KeycloakJwtServiceTests.this.jwt);

			// Then
			assertThat(result).containsExactly("user", "admin");
		}

		@Test
		@DisplayName("Should return empty list when realm_access is null")
		void testExtractRealmRolesNullRealmAccessReturnsEmptyList() {
			// Given
			given(KeycloakJwtServiceTests.this.jwt.getClaimAsMap("realm_access")).willReturn(null);

			// When
			List<String> result = KeycloakJwtServiceTests.this.keycloakJwtService
				.extractRealmRoles(KeycloakJwtServiceTests.this.jwt);

			// Then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return empty list when roles key does not exist")
		void testExtractRealmRolesNoRolesKeyReturnsEmptyList() {
			// Given
			Map<String, Object> realmAccess = new HashMap<>();
			given(KeycloakJwtServiceTests.this.jwt.getClaimAsMap("realm_access")).willReturn(realmAccess);

			// When
			List<String> result = KeycloakJwtServiceTests.this.keycloakJwtService
				.extractRealmRoles(KeycloakJwtServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(Collections.emptyList());
		}

	}

}
