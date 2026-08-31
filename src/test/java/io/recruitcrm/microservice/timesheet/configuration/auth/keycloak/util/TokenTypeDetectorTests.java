/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.KeycloakJwtService;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.util.TokenTypeDetector.TokenType;
import java.util.Base64;
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

/**
 * Unit tests for TokenTypeDetector class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class TokenTypeDetectorTests {

	private static final String DEFAULT_ISSUER_URI = "http://localhost:8080/realms/test";

	@Mock
	private KeycloakJwtService keycloakJwtService;

	private ObjectMapper objectMapper;

	private TokenTypeDetector tokenTypeDetector;

	private static String createJwtToken(String payload) {
		String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"RS256\"}".getBytes());
		String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
		String signature = Base64.getUrlEncoder().encodeToString("signature".getBytes());
		return header + "." + encodedPayload + "." + signature;
	}

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
		this.tokenTypeDetector = new TokenTypeDetector(this.objectMapper, this.keycloakJwtService);
	}

	@Nested
	@DisplayName("detectTokenType Tests")
	class DetectTokenTypeTests {

		@Test
		@DisplayName("Should return UNKNOWN when token has insufficient parts")
		void testDetectTokenTypeInsufficientPartsReturnsUnknown() {
			// Given
			String invalidToken = "single-part-token";

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(invalidToken);

			// Then
			assertThat(result).isEqualTo(TokenType.UNKNOWN);
		}

		@Test
		@DisplayName("Should return KEYCLOAK when issuer contains /realms/ pattern")
		void testDetectTokenTypeRealmsPatternReturnsKeycloak() {
			// Given
			String payload = "{\"iss\":\"http://localhost:8080/realms/test\"}";
			String token = createJwtToken(payload);

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.KEYCLOAK);
		}

		@Test
		@DisplayName("Should return KEYCLOAK when issuer matches configured issuer URI")
		void testDetectTokenTypeMatchingIssuerUriReturnsKeycloak() {
			// Given - issuer doesn't contain /realms/ but matches configured URI
			String nonRealmsIssuer = "http://auth.example.com/auth";
			String payload = "{\"iss\":\"" + nonRealmsIssuer + "\"}";
			String token = createJwtToken(payload);

			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(true);
			given(TokenTypeDetectorTests.this.keycloakJwtService.getIssuerUri()).willReturn(nonRealmsIssuer);

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.KEYCLOAK);
		}

		private static Stream<Arguments> keycloakClaimTestCases() {
			return Stream.of(
					Arguments.of("{\"iss\":\"http://other-issuer.com\",\"realm_access\":{\"roles\":[\"user\"]}}",
							"Should return KEYCLOAK when realm_access claim exists"),
					Arguments.of("{\"iss\":\"http://other-issuer.com\",\"preferred_username\":\"testuser\"}",
							"Should return KEYCLOAK when preferred_username claim exists"),
					Arguments.of("{\"iss\":\"http://other-issuer.com\",\"resource_access\":{}}",
							"Should return KEYCLOAK when resource_access claim exists"));
		}

		@ParameterizedTest(name = "{1}")
		@MethodSource("keycloakClaimTestCases")
		@DisplayName("Should return KEYCLOAK when Keycloak-specific claims exist")
		void testDetectTokenTypeKeycloakClaimsReturnKeycloak(String payload, String testDescription) {
			// Given
			String token = createJwtToken(payload);
			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.KEYCLOAK);
		}

		@Test
		@DisplayName("Should return LEGACY when no Keycloak indicators found")
		void testDetectTokenTypeNoKeycloakIndicatorsReturnsLegacy() {
			// Given
			String payload = "{\"iss\":\"http://other-issuer.com\",\"sub\":\"user-123\"}";
			String token = createJwtToken(payload);

			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.LEGACY);
		}

		@Test
		@DisplayName("Should return UNKNOWN when exception occurs during parsing")
		void testDetectTokenTypeParsingExceptionReturnsUnknown() {
			// Given
			String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"RS256\"}".getBytes());
			String invalidPayload = Base64.getUrlEncoder().encodeToString("invalid-json".getBytes());
			String token = header + "." + invalidPayload + ".signature";

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.UNKNOWN);
		}

		@Test
		@DisplayName("Should return LEGACY when issuer does not contain /realms/ and keycloak disabled")
		void testDetectTokenTypeNonRealmsIssuerKeycloakDisabledReturnsLegacy() {
			// Given
			String payload = "{\"iss\":\"http://legacy-issuer.com\"}";
			String token = createJwtToken(payload);

			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.LEGACY);
		}

		@Test
		@DisplayName("Should return LEGACY when issuer does not match configured issuer URI")
		void testDetectTokenTypeNonMatchingIssuerUriReturnsLegacy() {
			// Given
			String payload = "{\"iss\":\"http://different-issuer.com\"}";
			String token = createJwtToken(payload);

			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(true);
			given(TokenTypeDetectorTests.this.keycloakJwtService.getIssuerUri()).willReturn(DEFAULT_ISSUER_URI);

			// When
			TokenType result = TokenTypeDetectorTests.this.tokenTypeDetector.detectTokenType(token);

			// Then
			assertThat(result).isEqualTo(TokenType.LEGACY);
		}

	}

	@Nested
	@DisplayName("isKeycloakToken Tests")
	class IsKeycloakTokenTests {

		@Test
		@DisplayName("Should return true when token is Keycloak token")
		void testIsKeycloakTokenKeycloakTokenReturnsTrue() {
			// Given
			String payload = "{\"iss\":\"http://localhost:8080/realms/test\"}";
			String token = createJwtToken(payload);

			// When
			boolean result = TokenTypeDetectorTests.this.tokenTypeDetector.isKeycloakToken(token);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when token is legacy token")
		void testIsKeycloakTokenLegacyTokenReturnsFalse() {
			// Given
			String payload = "{\"iss\":\"http://other-issuer.com\"}";
			String token = createJwtToken(payload);

			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);

			// When
			boolean result = TokenTypeDetectorTests.this.tokenTypeDetector.isKeycloakToken(token);

			// Then
			assertThat(result).isFalse();
		}

	}

	@Nested
	@DisplayName("isLegacyToken Tests")
	class IsLegacyTokenTests {

		@Test
		@DisplayName("Should return true when token is legacy token")
		void testIsLegacyTokenLegacyTokenReturnsTrue() {
			// Given
			String payload = "{\"iss\":\"http://other-issuer.com\"}";
			String token = createJwtToken(payload);

			given(TokenTypeDetectorTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);

			// When
			boolean result = TokenTypeDetectorTests.this.tokenTypeDetector.isLegacyToken(token);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when token is Keycloak token")
		void testIsLegacyTokenKeycloakTokenReturnsFalse() {
			// Given
			String payload = "{\"iss\":\"http://localhost:8080/realms/test\"}";
			String token = createJwtToken(payload);

			// When
			boolean result = TokenTypeDetectorTests.this.tokenTypeDetector.isLegacyToken(token);

			// Then
			assertThat(result).isFalse();
		}

	}

}
