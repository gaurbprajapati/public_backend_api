/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.UserRepository;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Unit tests for KeycloakUserMappingService class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakUserMappingServiceTests {

	private static final String DEFAULT_EMAIL = "test@example.com";

	private static final String DEFAULT_KEYCLOAK_USER_ID = "keycloak-user-123";

	@Mock
	private UserRepository userRepository;

	@Mock
	private Jwt jwt;

	private KeycloakUserMappingService keycloakUserMappingService;

	private static User createDefaultUser() {
		User user = new User();
		user.setId(1);
		user.setEmail(DEFAULT_EMAIL);
		return user;
	}

	@BeforeEach
	void setUp() {
		this.keycloakUserMappingService = new KeycloakUserMappingService(this.userRepository);
	}

	@Nested
	@DisplayName("mapKeycloakUserToLocalUser Tests")
	class MapKeycloakUserToLocalUserTests {

		@Test
		@DisplayName("Should return user when email found in database")
		void testMapKeycloakUserToLocalUserValidEmailReturnsUser() {
			// Given
			User user = createDefaultUser();
			given(KeycloakUserMappingServiceTests.this.jwt.getClaimAsString("email")).willReturn(DEFAULT_EMAIL);
			given(KeycloakUserMappingServiceTests.this.userRepository.findByEmail(DEFAULT_EMAIL))
				.willReturn(Optional.of(user));

			// When
			User result = KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserToLocalUser(KeycloakUserMappingServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(user);
			assertThat(result.getEmail()).isEqualTo(DEFAULT_EMAIL);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when email is null")
		void testMapKeycloakUserToLocalUserNullEmailThrowsUnauthorizedAccessException() {
			// Given
			given(KeycloakUserMappingServiceTests.this.jwt.getClaimAsString("email")).willReturn(null);

			// When & Then
			assertThatThrownBy(() -> KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserToLocalUser(KeycloakUserMappingServiceTests.this.jwt))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when email is empty")
		void testMapKeycloakUserToLocalUserEmptyEmailThrowsUnauthorizedAccessException() {
			// Given
			given(KeycloakUserMappingServiceTests.this.jwt.getClaimAsString("email")).willReturn("");

			// When & Then
			assertThatThrownBy(() -> KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserToLocalUser(KeycloakUserMappingServiceTests.this.jwt))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when user not found")
		void testMapKeycloakUserToLocalUserUserNotFoundThrowsUsernameNotFoundException() {
			// Given
			given(KeycloakUserMappingServiceTests.this.jwt.getClaimAsString("email")).willReturn(DEFAULT_EMAIL);
			given(KeycloakUserMappingServiceTests.this.userRepository.findByEmail(DEFAULT_EMAIL))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserToLocalUser(KeycloakUserMappingServiceTests.this.jwt))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("User not found");
		}

	}

	@Nested
	@DisplayName("mapKeycloakUserByKeycloakId Tests")
	class MapKeycloakUserByKeycloakIdTests {

		@Test
		@DisplayName("Should throw UnsupportedOperationException when subject is valid")
		void testMapKeycloakUserByKeycloakIdValidSubjectThrowsUnsupportedOperationException() {
			// Given
			given(KeycloakUserMappingServiceTests.this.jwt.getSubject()).willReturn(DEFAULT_KEYCLOAK_USER_ID);

			// When & Then
			assertThatThrownBy(() -> KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserByKeycloakId(KeycloakUserMappingServiceTests.this.jwt))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessage("Keycloak ID mapping not implemented. Use email-based mapping instead.");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when subject is null")
		void testMapKeycloakUserByKeycloakIdNullSubjectThrowsUnauthorizedAccessException() {
			// Given
			given(KeycloakUserMappingServiceTests.this.jwt.getSubject()).willReturn(null);

			// When & Then
			assertThatThrownBy(() -> KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserByKeycloakId(KeycloakUserMappingServiceTests.this.jwt))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when subject is empty")
		void testMapKeycloakUserByKeycloakIdEmptySubjectThrowsUnauthorizedAccessException() {
			// Given
			given(KeycloakUserMappingServiceTests.this.jwt.getSubject()).willReturn("");

			// When & Then
			assertThatThrownBy(() -> KeycloakUserMappingServiceTests.this.keycloakUserMappingService
				.mapKeycloakUserByKeycloakId(KeycloakUserMappingServiceTests.this.jwt))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

	}

}
