/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for PrincipalEntityExtractor class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class PrincipalEntityExtractorTests {

	private static final Integer DEFAULT_CANDIDATE_ID = 1;

	private static final Integer DEFAULT_CONTACT_ID = 2;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	@Mock
	private AuthPrincipal mockAuthPrincipal;

	private PrincipalEntityExtractor principalEntityExtractor;

	private static Candidate createDefaultCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(DEFAULT_CANDIDATE_ID);
		candidate.setAccountId(DEFAULT_ACCOUNT_ID);
		candidate.setFirstName("Test");
		candidate.setLastName("Contractor");
		return candidate;
	}

	private static Contact createDefaultContact() {
		Contact contact = new Contact();
		contact.setId(DEFAULT_CONTACT_ID);
		contact.setAccountId(DEFAULT_ACCOUNT_ID);
		contact.setFirstName("Test");
		contact.setLastName("Contact");
		contact.setEmail("contact@example.com");
		return contact;
	}

	@BeforeEach
	void setUp() {
		this.principalEntityExtractor = new PrincipalEntityExtractor();
	}

	@Test
	@DisplayName("resolveUserTypeId should return agency recruiter user type id for USER")
	void testResolveUserTypeIdUserReturnsAgencyRecruiterId() {
		// When
		Integer result = this.principalEntityExtractor.resolveUserTypeId(PrincipalType.USER);

		// Then
		assertThat(result).isEqualTo(UserTypeEnum.AGENCY_RECRUITER.getId());
	}

	@Test
	@DisplayName("resolveUserTypeId should return contractor user type id for CONTRACTOR")
	void testResolveUserTypeIdContractorReturnsContractorId() {
		// When
		Integer result = this.principalEntityExtractor.resolveUserTypeId(PrincipalType.CONTRACTOR);

		// Then
		assertThat(result).isEqualTo(UserTypeEnum.CONTRACTOR.getId());
	}

	@Test
	@DisplayName("resolveUserTypeId should return company contact user type id for CONTACT")
	void testResolveUserTypeIdContactReturnsCompanyContactId() {
		// When
		Integer result = this.principalEntityExtractor.resolveUserTypeId(PrincipalType.CONTACT);

		// Then
		assertThat(result).isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
	}

	@Nested
	@DisplayName("extractEntityTypeFromPrincipal Tests")
	class ExtractEntityTypeFromPrincipalTests {

		@Test
		@DisplayName("Should return null when principal is null")
		void testExtractEntityTypeFromPrincipalNullPrincipalReturnsNull() {
			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityTypeFromPrincipal(null);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return ENTITY_TYPE_CONTRACTOR when principal is CONTRACTOR")
		void testExtractEntityTypeFromPrincipalContractorReturnsContractorType() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityTypeFromPrincipal(contractorPrincipal);

			// Then
			assertThat(result).isEqualTo(EntityAccessValidator.ENTITY_TYPE_CONTRACTOR);
		}

		@Test
		@DisplayName("Should return ENTITY_TYPE_CONTACT when principal is CONTACT")
		void testExtractEntityTypeFromPrincipalContactReturnsContactType() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal contactPrincipal = new ContactPrincipal(contact);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityTypeFromPrincipal(contactPrincipal);

			// Then
			assertThat(result).isEqualTo(EntityAccessValidator.ENTITY_TYPE_CONTACT);
		}

		@Test
		@DisplayName("Should return null when principal is USER type")
		void testExtractEntityTypeFromPrincipalUserReturnsNull() {
			// Given
			given(PrincipalEntityExtractorTests.this.mockAuthPrincipal.getPrincipalType())
				.willReturn(PrincipalType.USER);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityTypeFromPrincipal(PrincipalEntityExtractorTests.this.mockAuthPrincipal);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return null when principalType is CONTRACTOR but not ContractorPrincipal")
		void testContractorTypeButWrongInstance() {
			// Given
			AuthPrincipal fakePrincipal = mock(AuthPrincipal.class);
			given(fakePrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityIdFromPrincipal(fakePrincipal);

			// Then
			assertThat(result).isNull(); // instanceof fails, so branch not taken
		}

		@Test
		@DisplayName("Should return null when principalType is CONTACT but not ContactPrincipal")
		void testContactTypeButWrongInstance() {
			// Given
			AuthPrincipal fakePrincipal = mock(AuthPrincipal.class);
			given(fakePrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityIdFromPrincipal(fakePrincipal);

			// Then
			assertThat(result).isNull();
		}

	}

	@Nested
	@DisplayName("extractEntityIdFromPrincipal Tests")
	class ExtractEntityIdFromPrincipalTests {

		@Test
		@DisplayName("Should return null when principal is null")
		void testExtractEntityIdFromPrincipalNullPrincipalReturnsNull() {
			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityIdFromPrincipal(null);

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return candidate ID when principal is CONTRACTOR")
		void testExtractEntityIdFromPrincipalContractorReturnsCandidateId() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityIdFromPrincipal(contractorPrincipal);

			// Then
			assertThat(result).isEqualTo(DEFAULT_CANDIDATE_ID);
		}

		@Test
		@DisplayName("Should return contact ID when principal is CONTACT")
		void testExtractEntityIdFromPrincipalContactReturnsContactId() {
			// Given
			Contact contact = createDefaultContact();
			ContactPrincipal contactPrincipal = new ContactPrincipal(contact);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityIdFromPrincipal(contactPrincipal);

			// Then
			assertThat(result).isEqualTo(DEFAULT_CONTACT_ID);
		}

		@Test
		@DisplayName("Should return null when principal is USER type")
		void testExtractEntityIdFromPrincipalUserReturnsNull() {
			// Given
			given(PrincipalEntityExtractorTests.this.mockAuthPrincipal.getPrincipalType())
				.willReturn(PrincipalType.USER);

			// When
			Integer result = PrincipalEntityExtractorTests.this.principalEntityExtractor
				.extractEntityIdFromPrincipal(PrincipalEntityExtractorTests.this.mockAuthPrincipal);

			// Then
			assertThat(result).isNull();
		}

	}

}
