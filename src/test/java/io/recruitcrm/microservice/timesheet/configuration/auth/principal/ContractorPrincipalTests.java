/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.entity.model.Candidate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ContractorPrincipal class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorPrincipalTests {

	private static final Integer DEFAULT_CANDIDATE_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final String DEFAULT_EMAIL = "contractor@test.com";

	private static final String DEFAULT_FIRST_NAME = "John";

	private static final String DEFAULT_LAST_NAME = "Doe";

	private static final String DEFAULT_CONTACT_NUMBER = "1234567890";

	// ========== Helper Methods ==========

	private static Candidate createDefaultCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(DEFAULT_CANDIDATE_ID);
		candidate.setAccountId(DEFAULT_ACCOUNT_ID);
		candidate.setEmailId(DEFAULT_EMAIL);
		candidate.setFirstName(DEFAULT_FIRST_NAME);
		candidate.setLastName(DEFAULT_LAST_NAME);
		candidate.setContactNumber(DEFAULT_CONTACT_NUMBER);
		return candidate;
	}

	// ========== Constructor Tests ==========

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Should create ContractorPrincipal with valid candidate")
		void testConstructorValidCandidateCreatesContractorPrincipal() {
			// Given
			Candidate candidate = createDefaultCandidate();

			// When
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// Then
			assertThat(principal).isNotNull();
			assertThat(principal.getCandidate()).isEqualTo(candidate);
		}

		@Test
		@DisplayName("Should throw IllegalArgumentException when candidate is null")
		void testConstructorNullCandidateThrowsIllegalArgumentException() {
			// Given
			Candidate candidate = null;

			// When & Then
			assertThatThrownBy(() -> new ContractorPrincipal(candidate)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Candidate cannot be null");
		}

	}

	// ========== getPrincipalType Tests ==========

	@Nested
	@DisplayName("getPrincipalType Tests")
	class GetPrincipalTypeTests {

		@Test
		@DisplayName("Should return CONTRACTOR principal type")
		void testGetPrincipalTypeReturnsContractor() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			PrincipalType result = principal.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.CONTRACTOR);
		}

	}

	// ========== getUniqueIdentifier Tests ==========

	@Nested
	@DisplayName("getUniqueIdentifier Tests")
	class GetUniqueIdentifierTests {

		@Test
		@DisplayName("Should return candidate ID as unique identifier")
		void testGetUniqueIdentifierReturnsCandidateId() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			Integer result = principal.getUniqueIdentifier();

			// Then
			assertThat(result).isEqualTo(DEFAULT_CANDIDATE_ID);
		}

	}

	// ========== getOrganizationIdentifier Tests ==========

	@Nested
	@DisplayName("getOrganizationIdentifier Tests")
	class GetOrganizationIdentifierTests {

		@Test
		@DisplayName("Should return account ID as organization identifier")
		void testGetOrganizationIdentifierReturnsAccountId() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			Integer result = principal.getOrganizationIdentifier();

			// Then
			assertThat(result).isEqualTo(DEFAULT_ACCOUNT_ID);
		}

	}

	// ========== getEmail Tests ==========

	@Nested
	@DisplayName("getEmail Tests")
	class GetEmailTests {

		@Test
		@DisplayName("Should return candidate email")
		void testGetEmailReturnsCandidateEmail() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			String result = principal.getEmail();

			// Then
			assertThat(result).isEqualTo(DEFAULT_EMAIL);
		}

	}

	// ========== getDisplayName Tests ==========

	@Nested
	@DisplayName("getDisplayName Tests")
	class GetDisplayNameTests {

		@Test
		@DisplayName("Should return full name when both first and last names are present")
		void testGetDisplayNameBothNamesReturnsFullName() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("John Doe");
		}

		@Test
		@DisplayName("Should return first name only when last name is null")
		void testGetDisplayNameNullLastNameReturnsFirstName() {
			// Given
			Candidate candidate = createDefaultCandidate();
			candidate.setLastName(null);
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("John");
		}

		@Test
		@DisplayName("Should return last name only when first name is null")
		void testGetDisplayNameNullFirstNameReturnsLastName() {
			// Given
			Candidate candidate = createDefaultCandidate();
			candidate.setFirstName(null);
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("Doe");
		}

		@Test
		@DisplayName("Should return empty string when both names are null")
		void testGetDisplayNameBothNullReturnsEmptyString() {
			// Given
			Candidate candidate = createDefaultCandidate();
			candidate.setFirstName(null);
			candidate.setLastName(null);
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			String result = principal.getDisplayName();

			// Then
			assertThat(result).isEmpty();
		}

	}

	// ========== getFullName Tests ==========

	@Nested
	@DisplayName("getFullName Tests")
	class GetFullNameTests {

		@Test
		@DisplayName("Should match getDisplayName for contractor full name")
		void testGetFullNameMatchesDisplayName() {
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			assertThat(principal.getFullName()).isEqualTo(principal.getDisplayName());
		}

	}

	// ========== getRoleIdentifier Tests ==========

	@Nested
	@DisplayName("getRoleIdentifier Tests")
	class GetRoleIdentifierTests {

		@Test
		@DisplayName("Should return virtual contractor role ID")
		void testGetRoleIdentifierReturnsVirtualRoleId() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			Integer result = principal.getRoleIdentifier();

			// Then
			assertThat(result).isEqualTo(-1);
		}

	}

	// ========== getCandidate Tests ==========

	@Nested
	@DisplayName("getCandidate Tests")
	class GetCandidateTests {

		@Test
		@DisplayName("Should return the underlying candidate entity")
		void testGetCandidateReturnsCandidate() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			Candidate result = principal.getCandidate();

			// Then
			assertThat(result).isEqualTo(candidate);
		}

	}

	// ========== getCandidateId Tests ==========

	@Nested
	@DisplayName("getCandidateId Tests")
	class GetCandidateIdTests {

		@Test
		@DisplayName("Should return candidate ID")
		void testGetCandidateIdReturnsCandidateId() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			Integer result = principal.getCandidateId();

			// Then
			assertThat(result).isEqualTo(DEFAULT_CANDIDATE_ID);
		}

	}

	// ========== getContactNumber Tests ==========

	@Nested
	@DisplayName("getContactNumber Tests")
	class GetContactNumberTests {

		@Test
		@DisplayName("Should return contact number")
		void testGetContactNumberReturnsContactNumber() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			String result = principal.getContactNumber();

			// Then
			assertThat(result).isEqualTo(DEFAULT_CONTACT_NUMBER);
		}

	}

	// ========== AuthPrincipal Default Method Tests ==========

	@Nested
	@DisplayName("AuthPrincipal Default Method Tests")
	class AuthPrincipalDefaultMethodTests {

		@Test
		@DisplayName("isSystemUser should return false for contractor")
		void testIsSystemUserReturnsFalse() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			boolean result = principal.isSystemUser();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("isContractor should return true for contractor")
		void testIsContractorReturnsTrue() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			boolean result = principal.isContractor();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("isContact should return false for contractor")
		void testIsContactReturnsFalse() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal principal = new ContractorPrincipal(candidate);

			// When
			boolean result = principal.isContact();

			// Then
			assertThat(result).isFalse();
		}

	}

}
