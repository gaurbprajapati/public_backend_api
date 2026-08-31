/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.portals.IContractorTimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ContractorTimesheetService class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorTimesheetServiceTests {

	@Mock
	private IContractorTimesheetRepository contractorTimesheetRepository;

	@Mock
	private AuthHolder auth;

	private ContractorTimesheetService contractorTimesheetService;

	private static final Integer DEFAULT_CONTRACTOR_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	@BeforeEach
	void setUp() {
		this.contractorTimesheetService = new ContractorTimesheetService(this.contractorTimesheetRepository, this.auth);
	}

	// ========== isTimesheetEnabled Tests ==========

	@Nested
	@DisplayName("isTimesheetEnabled Tests")
	class IsTimesheetEnabledTests {

		@Test
		@DisplayName("Should return null when contractor has no contract jobs")
		void testIsTimesheetEnabledNoContractJobsReturnsNull() {
			// Given
			given(ContractorTimesheetServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(ContractorTimesheetServiceTests.this.contractorTimesheetRepository
				.countContractJobsForContractor(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID)).willReturn(0L);

			// When
			Integer result = ContractorTimesheetServiceTests.this.contractorTimesheetService
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID);

			// Then
			assertThat(result).isNull();
			then(ContractorTimesheetServiceTests.this.contractorTimesheetRepository).should()
				.countContractJobsForContractor(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should return 1 when timesheet is enabled for contractor")
		void testIsTimesheetEnabledTimesheetEnabledReturnsOne() {
			// Given
			given(ContractorTimesheetServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(ContractorTimesheetServiceTests.this.contractorTimesheetRepository
				.countContractJobsForContractor(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID)).willReturn(5L);
			given(ContractorTimesheetServiceTests.this.contractorTimesheetRepository
				.countTimesheetEnabledForContractor(eq(DEFAULT_CONTRACTOR_ID), anyLong(), eq(DEFAULT_ACCOUNT_ID)))
				.willReturn(1L);

			// When
			Integer result = ContractorTimesheetServiceTests.this.contractorTimesheetService
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID);

			// Then
			assertThat(result).isEqualTo(1);
			then(ContractorTimesheetServiceTests.this.contractorTimesheetRepository).should()
				.countContractJobsForContractor(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID);
			then(ContractorTimesheetServiceTests.this.contractorTimesheetRepository).should()
				.countTimesheetEnabledForContractor(eq(DEFAULT_CONTRACTOR_ID), anyLong(), eq(DEFAULT_ACCOUNT_ID));
		}

		@Test
		@DisplayName("Should return 0 when timesheet is not enabled for contractor")
		void testIsTimesheetEnabledTimesheetDisabledReturnsZero() {
			// Given
			given(ContractorTimesheetServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(ContractorTimesheetServiceTests.this.contractorTimesheetRepository
				.countContractJobsForContractor(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID)).willReturn(3L);
			given(ContractorTimesheetServiceTests.this.contractorTimesheetRepository
				.countTimesheetEnabledForContractor(eq(DEFAULT_CONTRACTOR_ID), anyLong(), eq(DEFAULT_ACCOUNT_ID)))
				.willReturn(0L);

			// When
			Integer result = ContractorTimesheetServiceTests.this.contractorTimesheetService
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID);

			// Then
			assertThat(result).isZero();
		}

	}

}
