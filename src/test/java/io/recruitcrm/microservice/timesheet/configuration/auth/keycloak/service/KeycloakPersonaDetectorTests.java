/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for KeycloakPersonaDetector class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakPersonaDetectorTests {

	@Mock
	private Jwt jwt;

	@InjectMocks
	private KeycloakPersonaDetector keycloakPersonaDetector;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Nested
	@DisplayName("detectPersonaType Tests")
	class DetectPersonaTypeTests {

		@Test
		@DisplayName("Should throw IllegalArgumentException when JWT is null")
		void testDetectPersonaTypeNullJwtThrowsIllegalArgumentException() {
			// When & Then
			assertThatThrownBy(() -> KeycloakPersonaDetectorTests.this.keycloakPersonaDetector.detectPersonaType(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("JWT cannot be null");
		}

		@Test
		@DisplayName("Should return USER when entityType is null")
		void testDetectPersonaTypeNullEntityTypeReturnsUser() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(null);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should return CONTRACTOR when entityType is 3 and entityId is valid")
		void testDetectPersonaTypeContractorEntityTypeValidEntityIdReturnsContractor() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(3);
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(1);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.CONTRACTOR);
		}

		@Test
		@DisplayName("Should return USER when entityType is 3 but entityId is null")
		void testDetectPersonaTypeContractorEntityTypeNullEntityIdReturnsUser() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(3);
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(null);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should return USER when entityType is 3 but entityId is zero")
		void testDetectPersonaTypeContractorEntityTypeZeroEntityIdReturnsUser() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(3);
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(0);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should return CONTACT when entityType is 1 and entityId is valid")
		void testDetectPersonaTypeContactEntityTypeValidEntityIdReturnsContact() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(1);
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(1);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.CONTACT);
		}

		@Test
		@DisplayName("Should return USER when entityType is 1 but entityId is null")
		void testDetectPersonaTypeContactEntityTypeNullEntityIdReturnsUser() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(1);
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(null);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should return USER when entityType is 1 but entityId is zero")
		void testDetectPersonaTypeContactEntityTypeZeroEntityIdReturnsUser() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(1);
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(0);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should return USER when entityType is other value")
		void testDetectPersonaTypeOtherEntityTypeReturnsUser() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(99);

			// When
			PrincipalType result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.detectPersonaType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

	}

	@Nested
	@DisplayName("extractEntityType Tests")
	class ExtractEntityTypeTests {

		@Test
		@DisplayName("Should return null when claim is null")
		void testExtractEntityTypeNullClaimReturnsNull() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(null);

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return Integer when claim is Integer")
		void testExtractEntityTypeIntegerClaimReturnsInteger() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(3);

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(3);
		}

		@Test
		@DisplayName("Should return intValue when claim is Number")
		void testExtractEntityTypeNumberClaimReturnsIntValue() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(Long.valueOf(3L));

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(3);
		}

		@Test
		@DisplayName("Should return parsed Integer when claim is String")
		void testExtractEntityTypeStringClaimReturnsParsedInteger() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn("3");

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(3);
		}

		@Test
		@DisplayName("Should return null when claim is other type")
		void testExtractEntityTypeOtherTypeClaimReturnsNull() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn(new Object());

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return null when parsing String throws exception")
		void testExtractEntityTypeInvalidStringClaimReturnsNull() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityType")).willReturn("invalid");

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityType(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isNull();
		}

	}

	@Nested
	@DisplayName("extractEntityId Tests")
	class ExtractEntityIdTests {

		@Test
		@DisplayName("Should return null when claim is null")
		void testExtractEntityIdNullClaimReturnsNull() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(null);

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityId(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return Integer when claim is Integer")
		void testExtractEntityIdIntegerClaimReturnsInteger() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(1);

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityId(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("Should return intValue when claim is Number")
		void testExtractEntityIdNumberClaimReturnsIntValue() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(Long.valueOf(1L));

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityId(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("Should return parsed Integer when claim is String")
		void testExtractEntityIdStringClaimReturnsParsedInteger() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn("1");

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityId(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("Should return null when claim is other type")
		void testExtractEntityIdOtherTypeClaimReturnsNull() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn(new Object());

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityId(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return null when parsing String throws exception")
		void testExtractEntityIdInvalidStringClaimReturnsNull() {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaim("entityId")).willReturn("invalid");

			// When
			Integer result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEntityId(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isNull();
		}

	}

	@Nested
	@DisplayName("extractEmail Tests")
	class ExtractEmailTests {

		private static Stream<Arguments> extractEmailTestCases() {
			return Stream.of(
					Arguments.of("test@example.com", null, "test@example.com",
							"Should return email when email claim exists"),
					Arguments.of(null, "username", null,
							"Should return null when email claim is null and preferred_username does not contain @"),
					Arguments.of("", "username", null, "Should return null when email claim is empty"),
					Arguments.of(null, "test@example.com", "test@example.com",
							"Should return preferred_username when email is null and preferred_username contains @"),
					Arguments.of(null, null, null,
							"Should return null when both email and preferred_username are null"));
		}

		@ParameterizedTest(name = "{3}")
		@MethodSource("extractEmailTestCases")
		@DisplayName("Should handle various email and preferred_username combinations")
		void testExtractEmailVariousCombinations(String emailClaim, String preferredUsernameClaim,
				String expectedResult, String testDescription) {
			// Given
			given(KeycloakPersonaDetectorTests.this.jwt.getClaimAsString("email")).willReturn(emailClaim);
			if (preferredUsernameClaim != null || emailClaim == null || emailClaim.isEmpty()) {
				given(KeycloakPersonaDetectorTests.this.jwt.getClaimAsString("preferred_username"))
					.willReturn(preferredUsernameClaim);
			}

			// When
			String result = KeycloakPersonaDetectorTests.this.keycloakPersonaDetector
				.extractEmail(KeycloakPersonaDetectorTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(expectedResult);
		}

	}

}
