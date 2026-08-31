/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.entity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for EntityAccessValidator class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class EntityAccessValidatorTests {

	@Mock
	private AuthHolder authHolder;

	private EntityAccessValidator entityAccessValidator;

	private static final Integer ENTITY_TYPE_CONTACT = 1;

	private static final Integer ENTITY_TYPE_CONTRACTOR = 3;

	private static final Integer DEFAULT_CONTRACTOR_ID = 1;

	private static final Integer DEFAULT_CONTACT_ID = 2;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	// ========== Helper Methods ==========

	private static User createDefaultUser() {
		User user = new User();
		user.setId(1);
		user.setEmail("user@test.com");
		user.setUsername("testuser");
		user.setFirstname("Test");
		user.setLastname("User");
		user.setRoleId(1);
		return user;
	}

	private static Candidate createDefaultCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(DEFAULT_CONTRACTOR_ID);
		candidate.setAccountId(DEFAULT_ACCOUNT_ID);
		candidate.setEmailId("contractor@test.com");
		candidate.setFirstName("Test");
		candidate.setLastName("Contractor");
		return candidate;
	}

	private static Contact createDefaultContact() {
		Contact contact = new Contact();
		contact.setId(DEFAULT_CONTACT_ID);
		contact.setAccountId(DEFAULT_ACCOUNT_ID);
		contact.setEmail("contact@test.com");
		contact.setFirstName("Test");
		contact.setLastName("Contact");
		contact.setCompanyId(1);
		return contact;
	}

	@BeforeEach
	void setUp() {
		this.entityAccessValidator = new EntityAccessValidator(this.authHolder);
	}

	// ========== validateEntityAccess Tests - Null Principal ==========

	@Nested
	@DisplayName("validateEntityAccess - Null Principal Tests")
	class NullPrincipalTests {

		@Test
		@DisplayName("Should allow access when unified principal is null (legacy auth)")
		void testValidateEntityAccessNullPrincipalAllowsAccess() {
			// Given
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(null);

			// When & Then
			assertThatCode(() -> EntityAccessValidatorTests.this.entityAccessValidator.validateEntityAccess(3, 1))
				.doesNotThrowAnyException();
		}

	}

	// ========== validateEntityAccess Tests - System User ==========

	@Nested
	@DisplayName("validateEntityAccess - System User Tests")
	class SystemUserTests {

		@Test
		@DisplayName("Should allow system user to access any entity")
		void testValidateEntityAccessSystemUserAllowsAllAccess() {
			// Given
			User user = createDefaultUser();
			UserPrincipal userPrincipal = new UserPrincipal(user);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(userPrincipal);

			// When & Then
			assertThatCode(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTRACTOR, DEFAULT_CONTRACTOR_ID)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Should allow system user to access contact data")
		void testValidateEntityAccessSystemUserAccessContactData() {
			// Given
			User user = createDefaultUser();
			UserPrincipal userPrincipal = new UserPrincipal(user);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(userPrincipal);

			// When & Then
			assertThatCode(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTACT, DEFAULT_CONTACT_ID)).doesNotThrowAnyException();
		}

	}

	// ========== validateEntityAccess Tests - Contractor ==========

	@Nested
	@DisplayName("validateEntityAccess - Contractor Tests")
	class ContractorTests {

		@Test
		@DisplayName("Should allow contractor to access their own data")
		void testValidateEntityAccessContractorAccessOwnDataAllowed() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			// When & Then
			assertThatCode(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTRACTOR, DEFAULT_CONTRACTOR_ID)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Should throw ValidationErrorException when contractor accesses different contractor data")
		void testValidateEntityAccessContractorAccessOtherContractorDataThrowsException() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			Integer otherContractorId = 999;

			// When & Then
			assertThatThrownBy(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTRACTOR, otherContractorId))
				.isInstanceOf(ValidationErrorException.class)
				.hasMessageContaining("Contractors can only access their own timesheet data");
		}

		@Test
		@DisplayName("Should throw ValidationErrorException when contractor accesses contact data")
		void testValidateEntityAccessContractorAccessContactDataThrowsException() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			// When & Then
			assertThatThrownBy(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTACT, DEFAULT_CONTACT_ID))
				.isInstanceOf(ValidationErrorException.class)
				.hasMessageContaining("Contractors can only access contractor timesheet data");
		}

	}

	// ========== validateEntityAccess Tests - Contact ==========

	@Nested
	@DisplayName("validateEntityAccess - Contact Tests")
	class ContactTests {

		@Test
		@DisplayName("Should allow contact to access their own data")
		void testValidateEntityAccessContactAccessOwnDataAllowed() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal contactPrincipal = new ContactPrincipal(contact);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(contactPrincipal);

			// When & Then
			assertThatCode(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTACT, DEFAULT_CONTACT_ID)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Should throw ValidationErrorException when contact accesses different contact data")
		void testValidateEntityAccessContactAccessOtherContactDataThrowsException() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal contactPrincipal = new ContactPrincipal(contact);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(contactPrincipal);

			Integer otherContactId = 999;

			// When & Then
			assertThatThrownBy(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTACT, otherContactId)).isInstanceOf(ValidationErrorException.class)
				.hasMessageContaining("Contacts can only access their own company's timesheet data");
		}

		@Test
		@DisplayName("Should throw ValidationErrorException when contact accesses contractor data")
		void testValidateEntityAccessContactAccessContractorDataThrowsException() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal contactPrincipal = new ContactPrincipal(contact);
			given(EntityAccessValidatorTests.this.authHolder.getUnifiedPrincipal()).willReturn(contactPrincipal);

			// When & Then
			assertThatThrownBy(() -> EntityAccessValidatorTests.this.entityAccessValidator
				.validateEntityAccess(ENTITY_TYPE_CONTRACTOR, DEFAULT_CONTRACTOR_ID))
				.isInstanceOf(ValidationErrorException.class)
				.hasMessageContaining("Contacts can only access contact timesheet data");
		}

	}

}
