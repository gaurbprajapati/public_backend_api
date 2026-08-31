/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.contractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.contractor.ContractorJobRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ContractorJobService class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorJobServiceTests {

	@Mock
	private ContractorJobRepository contractorJobRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private UserPrincipal userPrincipal;

	private ContractorJobService contractorJobService;

	private static final Integer DEFAULT_CONTRACTOR_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	// ========== Helper Methods ==========

	private static Candidate createDefaultCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(DEFAULT_CONTRACTOR_ID);
		candidate.setAccountId(DEFAULT_ACCOUNT_ID);
		candidate.setEmailId("contractor@test.com");
		candidate.setFirstName("Test");
		candidate.setLastName("Contractor");
		return candidate;
	}

	private static List<ContractorJobResponseBodyDto> createContractorJobList() {
		return Arrays.asList(new ContractorJobResponseBodyDto(1, "Job 1", "Company 1"),
				new ContractorJobResponseBodyDto(2, "Job 2", null));
	}

	@BeforeEach
	void setUp() {
		this.contractorJobService = new ContractorJobService(this.contractorJobRepository, this.auth);
	}

	// ========== getContractorJobs Tests ==========

	@Nested
	@DisplayName("getContractorJobs Tests")
	class GetContractorJobsTests {

		@Test
		@DisplayName("Should return contractor jobs when principal is contractor")
		void testGetContractorJobsContractorPrincipalReturnsJobs() {
			// Given
			Candidate candidate = createDefaultCandidate();
			ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);
			List<ContractorJobResponseBodyDto> expectedJobs = createContractorJobList();

			given(ContractorJobServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(ContractorJobServiceTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ContractorJobServiceTests.this.contractorJobRepository.findJobsByContractorId(DEFAULT_CONTRACTOR_ID,
					DEFAULT_ACCOUNT_ID))
				.willReturn(expectedJobs);

			// When
			List<ContractorJobResponseBodyDto> result = ContractorJobServiceTests.this.contractorJobService
				.getContractorJobs();

			// Then
			assertThat(result).isEqualTo(expectedJobs);
			then(ContractorJobServiceTests.this.contractorJobRepository).should()
				.findJobsByContractorId(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when principal is not contractor")
		void testGetContractorJobsNonContractorPrincipalThrowsUnauthorizedException() {
			// Given
			given(ContractorJobServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(ContractorJobServiceTests.this.auth.getUnifiedPrincipal())
				.willReturn(ContractorJobServiceTests.this.userPrincipal);
			given(ContractorJobServiceTests.this.userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);

			// When & Then
			assertThatThrownBy(() -> ContractorJobServiceTests.this.contractorJobService.getContractorJobs())
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage("Unauthorized access");
		}

	}

}
