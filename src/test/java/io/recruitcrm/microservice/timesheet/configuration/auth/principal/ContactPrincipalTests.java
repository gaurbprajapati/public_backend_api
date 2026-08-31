/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.entity.model.Contact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ContactPrincipal class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContactPrincipalTests {

	private static final Integer DEFAULT_CONTACT_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final Integer DEFAULT_COMPANY_ID = 50;

	private static final String DEFAULT_EMAIL = "contact@example.com";

	private static final String DEFAULT_FIRST_NAME = "John";

	private static final String DEFAULT_LAST_NAME = "Doe";

	private static final String DEFAULT_CONTACT_NUMBER = "+1234567890";

	private static final String DEFAULT_DESIGNATION = "Manager";

	private static final Integer CONTACT_VIRTUAL_ROLE_ID = -2;

	private static Contact createDefaultContact() {
		Contact contact = new Contact();
		contact.setId(DEFAULT_CONTACT_ID);
		contact.setAccountId(DEFAULT_ACCOUNT_ID);
		contact.setCompanyId(DEFAULT_COMPANY_ID);
		contact.setEmail(DEFAULT_EMAIL);
		contact.setFirstName(DEFAULT_FIRST_NAME);
		contact.setLastName(DEFAULT_LAST_NAME);
		contact.setContactNumber(DEFAULT_CONTACT_NUMBER);
		contact.setDesignation(DEFAULT_DESIGNATION);
		return contact;
	}

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Should create ContactPrincipal when contact is valid")
		void testConstructorValidContactCreatesContactPrincipal() {
			// Given
			Contact contact = createDefaultContact();

			// When
			ContactPrincipal principal = new ContactPrincipal(contact);

			// Then
			assertThat(principal).isNotNull();
			assertThat(principal.getContact()).isEqualTo(contact);
		}

		@Test
		@DisplayName("Should throw IllegalArgumentException when contact is null")
		void testConstructorNullContactThrowsIllegalArgumentException() {
			// When & Then
			assertThatThrownBy(() -> new ContactPrincipal(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Contact cannot be null");
		}

	}

	@Nested
	@DisplayName("getPrincipalType Tests")
	class GetPrincipalTypeTests {

		@Test
		@DisplayName("Should return CONTACT principal type")
		void testGetPrincipalTypeReturnsContact() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			PrincipalType result = principal.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.CONTACT);
		}

	}

	@Nested
	@DisplayName("getUniqueIdentifier Tests")
	class GetUniqueIdentifierTests {

		@Test
		@DisplayName("Should return contact ID")
		void testGetUniqueIdentifierReturnsContactId() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			Integer result = principal.getUniqueIdentifier();

			// Then
			assertThat(result).isEqualTo(DEFAULT_CONTACT_ID);
		}

	}

	@Nested
	@DisplayName("getOrganizationIdentifier Tests")
	class GetOrganizationIdentifierTests {

		@Test
		@DisplayName("Should return account ID")
		void testGetOrganizationIdentifierReturnsAccountId() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			Integer result = principal.getOrganizationIdentifier();

			// Then
			assertThat(result).isEqualTo(DEFAULT_ACCOUNT_ID);
		}

	}

	@Nested
	@DisplayName("getEmail Tests")
	class GetEmailTests {

		@Test
		@DisplayName("Should return email")
		void testGetEmailReturnsEmail() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getEmail();

			// Then
			assertThat(result).isEqualTo(DEFAULT_EMAIL);
		}

		@Test
		@DisplayName("Should return JWT email when JWT email is present")
		void testGetEmailWithJwtEmailReturnsJwtEmail() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact, "jwt@example.com");

			// When
			String result = principal.getEmail();

			// Then
			assertThat(result).isEqualTo("jwt@example.com");
		}

		@Test
		@DisplayName("Should fall back to contact email when JWT email is empty")
		void testGetEmailWithEmptyJwtEmailReturnsContactEmail() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact, "");

			// When
			String result = principal.getEmail();

			// Then
			assertThat(result).isEqualTo(DEFAULT_EMAIL);
		}

		@Test
		@DisplayName("Should fall back to contact email when JWT email is null")
		void testGetEmailWithNullJwtEmailReturnsContactEmail() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact, null);

			// When
			String result = principal.getEmail();

			// Then
			assertThat(result).isEqualTo(DEFAULT_EMAIL);
		}

	}

	@Nested
	@DisplayName("getDisplayName Tests")
	class GetDisplayNameTests {

		@Test
		@DisplayName("Should return full name when both first and last names exist")
		void testGetDisplayNameBothNamesExistReturnsFullName() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEqualTo(DEFAULT_FIRST_NAME + " " + DEFAULT_LAST_NAME);
		}

		@Test
		@DisplayName("Should return first name when last name is null")
		void testGetDisplayNameNullLastNameReturnsFirstName() {
			// Given
			Contact contact = createDefaultContact();
			contact.setLastName(null);
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEqualTo(DEFAULT_FIRST_NAME);
		}

		@Test
		@DisplayName("Should return last name when first name is null")
		void testGetDisplayNameNullFirstNameReturnsLastName() {
			// Given
			Contact contact = createDefaultContact();
			contact.setFirstName(null);
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEqualTo(DEFAULT_LAST_NAME);
		}

		@Test
		@DisplayName("Should return empty string when both names are null")
		void testGetDisplayNameBothNamesNullReturnsEmptyString() {
			// Given
			Contact contact = createDefaultContact();
			contact.setFirstName(null);
			contact.setLastName(null);
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEmpty();
		}

	}

	@Nested
	@DisplayName("getFullName Tests")
	class GetFullNameTests {

		@Test
		@DisplayName("Should match getDisplayName for contact full name")
		void testGetFullNameMatchesDisplayName() {
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			assertThat(principal.getFullName()).isEqualTo(principal.getDisplayName());
		}

	}

	@Nested
	@DisplayName("getRoleIdentifier Tests")
	class GetRoleIdentifierTests {

		@Test
		@DisplayName("Should return virtual role ID for contacts")
		void testGetRoleIdentifierReturnsVirtualRoleId() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			Integer result = principal.getRoleIdentifier();

			// Then
			assertThat(result).isEqualTo(CONTACT_VIRTUAL_ROLE_ID);
		}

	}

	@Nested
	@DisplayName("getContact Tests")
	class GetContactTests {

		@Test
		@DisplayName("Should return underlying contact entity")
		void testGetContactReturnsContact() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			Contact result = principal.getContact();

			// Then
			assertThat(result).isEqualTo(contact);
		}

	}

	@Nested
	@DisplayName("getContactId Tests")
	class GetContactIdTests {

		@Test
		@DisplayName("Should return contact ID")
		void testGetContactIdReturnsContactId() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			Integer result = principal.getContactId();

			// Then
			assertThat(result).isEqualTo(DEFAULT_CONTACT_ID);
		}

	}

	@Nested
	@DisplayName("getCompanyId Tests")
	class GetCompanyIdTests {

		@Test
		@DisplayName("Should return company ID")
		void testGetCompanyIdReturnsCompanyId() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			Integer result = principal.getCompanyId();

			// Then
			assertThat(result).isEqualTo(DEFAULT_COMPANY_ID);
		}

	}

	@Nested
	@DisplayName("getContactNumber Tests")
	class GetContactNumberTests {

		@Test
		@DisplayName("Should return contact number")
		void testGetContactNumberReturnsContactNumber() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getContactNumber();

			// Then
			assertThat(result).isEqualTo(DEFAULT_CONTACT_NUMBER);
		}

	}

	@Nested
	@DisplayName("getDesignation Tests")
	class GetDesignationTests {

		@Test
		@DisplayName("Should return designation")
		void testGetDesignationReturnsDesignation() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			String result = principal.getDesignation();

			// Then
			assertThat(result).isEqualTo(DEFAULT_DESIGNATION);
		}

	}

	@Nested
	@DisplayName("AuthPrincipal Interface Default Method Tests")
	class AuthPrincipalDefaultMethodTests {

		@Test
		@DisplayName("Should return false for isSystemUser")
		void testIsSystemUserReturnsFalse() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			boolean result = principal.isSystemUser();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false for isContractor")
		void testIsContractorReturnsFalse() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			boolean result = principal.isContractor();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return true for isContact")
		void testIsContactReturnsTrue() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal principal = new ContactPrincipal(contact);

			// When
			boolean result = principal.isContact();

			// Then
			assertThat(result).isTrue();
		}

	}

}
