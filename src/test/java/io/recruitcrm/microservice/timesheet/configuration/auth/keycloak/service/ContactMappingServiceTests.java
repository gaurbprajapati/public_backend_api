/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dao.contact.ContactJpaRepository;
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
 * Unit tests for ContactMappingService class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContactMappingServiceTests {

	@Mock
	private ContactJpaRepository contactRepository;

	@Mock
	private KeycloakPersonaDetector personaDetector;

	@Mock
	private Logger logger;

	@Mock
	private Jwt jwt;

	private ContactMappingService contactMappingService;

	private static final Integer DEFAULT_CONTACT_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final String DEFAULT_EMAIL = "contact@test.com";

	// ========== Helper Methods ==========

	private static Contact createDefaultContact() {
		Contact contact = new Contact();
		contact.setId(DEFAULT_CONTACT_ID);
		contact.setAccountId(DEFAULT_ACCOUNT_ID);
		contact.setEmail(DEFAULT_EMAIL);
		contact.setFirstName("Test");
		contact.setLastName("Contact");
		return contact;
	}

	@BeforeEach
	void setUp() {
		this.contactMappingService = new ContactMappingService(this.contactRepository, this.personaDetector,
				this.logger);
	}

	// ========== mapToContact(Jwt jwt, Integer accountId) Tests ==========

	@Nested
	@DisplayName("mapToContact with accountId Tests")
	class MapToContactWithAccountIdTests {

		@Test
		@DisplayName("Should return contact when found by ID and account ID")
		void testMapToContactValidIdAndAccountIdReturnsContact() {
			// Given
			Contact expectedContact = createDefaultContact();
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_CONTACT_ID);
			given(ContactMappingServiceTests.this.contactRepository.findByIdAndAccountId(DEFAULT_CONTACT_ID,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.of(expectedContact));

			// When
			Contact result = ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedContact);
			then(ContactMappingServiceTests.this.contactRepository).should()
				.findByIdAndAccountId(DEFAULT_CONTACT_ID, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when contact not found by ID")
		void testMapToContactContactNotFoundByIdThrowsUsernameNotFoundException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_CONTACT_ID);
			given(ContactMappingServiceTests.this.contactRepository.findByIdAndAccountId(DEFAULT_CONTACT_ID,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Contact not found with ID: " + DEFAULT_CONTACT_ID);
		}

		@Test
		@DisplayName("Should return contact when found by email and account ID as fallback")
		void testMapToContactValidEmailAndAccountIdReturnsContact() {
			// Given
			Contact expectedContact = createDefaultContact();
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(null);
			given(ContactMappingServiceTests.this.personaDetector.extractEmail(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_EMAIL);
			given(ContactMappingServiceTests.this.contactRepository.findByEmailAndAccountId(DEFAULT_EMAIL,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.of(expectedContact));

			// When
			Contact result = ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedContact);
			then(ContactMappingServiceTests.this.contactRepository).should()
				.findByEmailAndAccountId(DEFAULT_EMAIL, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should return contact when ID is zero and found by email")
		void testMapToContactZeroIdFallbackToEmailReturnsContact() {
			// Given
			Contact expectedContact = createDefaultContact();
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(0);
			given(ContactMappingServiceTests.this.personaDetector.extractEmail(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_EMAIL);
			given(ContactMappingServiceTests.this.contactRepository.findByEmailAndAccountId(DEFAULT_EMAIL,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.of(expectedContact));

			// When
			Contact result = ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedContact);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when contact not found by email")
		void testMapToContactContactNotFoundByEmailThrowsUsernameNotFoundException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(null);
			given(ContactMappingServiceTests.this.personaDetector.extractEmail(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_EMAIL);
			given(ContactMappingServiceTests.this.contactRepository.findByEmailAndAccountId(DEFAULT_EMAIL,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Contact not found with email: " + DEFAULT_EMAIL);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when email is null")
		void testMapToContactNullEmailThrowsUnauthorizedAccessException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(null);
			given(ContactMappingServiceTests.this.personaDetector.extractEmail(ContactMappingServiceTests.this.jwt))
				.willReturn(null);

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when email is empty")
		void testMapToContactEmptyEmailThrowsUnauthorizedAccessException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(null);
			given(ContactMappingServiceTests.this.personaDetector.extractEmail(ContactMappingServiceTests.this.jwt))
				.willReturn("");

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

	}

	// ========== mapToContact(Jwt jwt) Tests ==========

	@Nested
	@DisplayName("mapToContact without accountId Tests")
	class MapToContactWithoutAccountIdTests {

		@Test
		@DisplayName("Should return contact when found by ID only")
		void testMapToContactValidIdReturnsContact() {
			// Given
			Contact expectedContact = createDefaultContact();
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_CONTACT_ID);
			given(ContactMappingServiceTests.this.contactRepository.findById(DEFAULT_CONTACT_ID))
				.willReturn(Optional.of(expectedContact));

			// When
			Contact result = ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(expectedContact);
			then(ContactMappingServiceTests.this.contactRepository).should().findById(DEFAULT_CONTACT_ID);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when contact not found by ID")
		void testMapToContactContactNotFoundByIdThrowsUsernameNotFoundException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(DEFAULT_CONTACT_ID);
			given(ContactMappingServiceTests.this.contactRepository.findById(DEFAULT_CONTACT_ID))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt)).isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Contact not found with ID: " + DEFAULT_CONTACT_ID);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when entityId is null")
		void testMapToContactNullEntityIdThrowsUnauthorizedAccessException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(null);

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt)).isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when entityId is zero")
		void testMapToContactZeroEntityIdThrowsUnauthorizedAccessException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(0);

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt)).isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when entityId is negative")
		void testMapToContactNegativeEntityIdThrowsUnauthorizedAccessException() {
			// Given
			given(ContactMappingServiceTests.this.personaDetector.extractEntityId(ContactMappingServiceTests.this.jwt))
				.willReturn(-1);

			// When & Then
			assertThatThrownBy(() -> ContactMappingServiceTests.this.contactMappingService
				.mapToContact(ContactMappingServiceTests.this.jwt)).isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

	}

}
