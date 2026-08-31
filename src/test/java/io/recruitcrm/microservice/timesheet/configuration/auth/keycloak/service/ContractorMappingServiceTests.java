/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dao.candidate.CandidateJpaRepository;
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
 * Unit tests for ContractorMappingService class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorMappingServiceTests {

	@Mock
	private CandidateJpaRepository candidateRepository;

	@Mock
	private KeycloakPersonaDetector personaDetector;

	@Mock
	private Logger logger;

	@Mock
	private Jwt jwt;

	private ContractorMappingService contractorMappingService;

	private static final Integer DEFAULT_CONTRACTOR_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final String DEFAULT_EMAIL = "contractor@test.com";

	// ========== Helper Methods ==========

	private static Candidate createDefaultCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(DEFAULT_CONTRACTOR_ID);
		candidate.setAccountId(DEFAULT_ACCOUNT_ID);
		candidate.setEmailId(DEFAULT_EMAIL);
		candidate.setFirstName("Test");
		candidate.setLastName("Contractor");
		return candidate;
	}

	@BeforeEach
	void setUp() {
		this.contractorMappingService = new ContractorMappingService(this.candidateRepository, this.personaDetector,
				this.logger);
	}

	// ========== mapToCandidate(Jwt jwt, Integer accountId) Tests ==========

	@Nested
	@DisplayName("mapToCandidate with accountId Tests")
	class MapToCandidateWithAccountIdTests {

		@Test
		@DisplayName("Should return candidate when found by ID and account ID")
		void testMapToCandidateValidIdAndAccountIdReturnsCandidate() {
			// Given
			Candidate expectedCandidate = createDefaultCandidate();
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_CONTRACTOR_ID);
			given(ContractorMappingServiceTests.this.candidateRepository.findByIdAndAccountId(DEFAULT_CONTRACTOR_ID,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.of(expectedCandidate));

			// When
			Candidate result = ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedCandidate);
			then(ContractorMappingServiceTests.this.candidateRepository).should()
				.findByIdAndAccountId(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when contractor not found by ID")
		void testMapToCandidateContractorNotFoundByIdThrowsUsernameNotFoundException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_CONTRACTOR_ID);
			given(ContractorMappingServiceTests.this.candidateRepository.findByIdAndAccountId(DEFAULT_CONTRACTOR_ID,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Contractor not found with ID: " + DEFAULT_CONTRACTOR_ID);
		}

		@Test
		@DisplayName("Should return candidate when found by email and account ID as fallback")
		void testMapToCandidateValidEmailAndAccountIdReturnsCandidate() {
			// Given
			Candidate expectedCandidate = createDefaultCandidate();
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(null);
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEmail(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_EMAIL);
			given(ContractorMappingServiceTests.this.candidateRepository.findByEmailIdAndAccountId(DEFAULT_EMAIL,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.of(expectedCandidate));

			// When
			Candidate result = ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedCandidate);
			then(ContractorMappingServiceTests.this.candidateRepository).should()
				.findByEmailIdAndAccountId(DEFAULT_EMAIL, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should return candidate when ID is zero and found by email")
		void testMapToCandidateZeroIdFallbackToEmailReturnsCandidate() {
			// Given
			Candidate expectedCandidate = createDefaultCandidate();
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(0);
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEmail(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_EMAIL);
			given(ContractorMappingServiceTests.this.candidateRepository.findByEmailIdAndAccountId(DEFAULT_EMAIL,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.of(expectedCandidate));

			// When
			Candidate result = ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedCandidate);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when contractor not found by email")
		void testMapToCandidateContractorNotFoundByEmailThrowsUsernameNotFoundException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(null);
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEmail(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_EMAIL);
			given(ContractorMappingServiceTests.this.candidateRepository.findByEmailIdAndAccountId(DEFAULT_EMAIL,
					DEFAULT_ACCOUNT_ID))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Contractor not found with email: " + DEFAULT_EMAIL);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when email is null")
		void testMapToCandidateNullEmailThrowsUnauthorizedAccessException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(null);
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEmail(ContractorMappingServiceTests.this.jwt)).willReturn(null);

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when email is empty")
		void testMapToCandidateEmptyEmailThrowsUnauthorizedAccessException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(null);
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEmail(ContractorMappingServiceTests.this.jwt)).willReturn("");

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt, DEFAULT_ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

	}

	// ========== mapToCandidate(Jwt jwt) Tests ==========

	@Nested
	@DisplayName("mapToCandidate without accountId Tests")
	class MapToCandidateWithoutAccountIdTests {

		@Test
		@DisplayName("Should return candidate when found by ID only")
		void testMapToCandidateValidIdReturnsCandidate() {
			// Given
			Candidate expectedCandidate = createDefaultCandidate();
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_CONTRACTOR_ID);
			given(ContractorMappingServiceTests.this.candidateRepository.findById(DEFAULT_CONTRACTOR_ID))
				.willReturn(Optional.of(expectedCandidate));

			// When
			Candidate result = ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt);

			// Then
			assertThat(result).isEqualTo(expectedCandidate);
			then(ContractorMappingServiceTests.this.candidateRepository).should().findById(DEFAULT_CONTRACTOR_ID);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when contractor not found by ID")
		void testMapToCandidateContractorNotFoundByIdThrowsUsernameNotFoundException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(DEFAULT_CONTRACTOR_ID);
			given(ContractorMappingServiceTests.this.candidateRepository.findById(DEFAULT_CONTRACTOR_ID))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt)).isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("Contractor not found with ID: " + DEFAULT_CONTRACTOR_ID);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when entityId is null")
		void testMapToCandidateNullEntityIdThrowsUnauthorizedAccessException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(null);

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt)).isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when entityId is zero")
		void testMapToCandidateZeroEntityIdThrowsUnauthorizedAccessException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(0);

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt)).isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when entityId is negative")
		void testMapToCandidateNegativeEntityIdThrowsUnauthorizedAccessException() {
			// Given
			given(ContractorMappingServiceTests.this.personaDetector
				.extractEntityId(ContractorMappingServiceTests.this.jwt)).willReturn(-1);

			// When & Then
			assertThatThrownBy(() -> ContractorMappingServiceTests.this.contractorMappingService
				.mapToCandidate(ContractorMappingServiceTests.this.jwt)).isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Authentication failed");
		}

	}

}
