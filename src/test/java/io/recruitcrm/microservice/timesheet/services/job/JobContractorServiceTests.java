/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.job.JobContractorRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link JobContractorService}.
 */
@ExtendWith(MockitoExtension.class)
class JobContractorServiceTests {

	@Mock
	private JobContractorRepository jobContractorRepository;

	@Mock
	private AuthHolder auth;

	private JobContractorService jobContractorService;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final List<Integer> JOB_IDS = List.of(1, 2);

	@BeforeEach
	void setUp() {
		this.jobContractorService = new JobContractorService(this.jobContractorRepository, this.auth);
	}

	private static TimesheetEnabledAssignedCandidateQueryResultDto createQueryResult() {
		return createQueryResultWithNames("Jane", "Doe");
	}

	private static TimesheetEnabledAssignedCandidateQueryResultDto createQueryResultWithNames(String firstName,
			String lastName) {
		TimesheetEnabledAssignedCandidateQueryResultDto result = new TimesheetEnabledAssignedCandidateQueryResultDto();
		result.setId(10);
		result.setSrno(5);
		result.setFirstName(firstName);
		result.setLastName(lastName);
		result.setEmailId("jane@test.com");
		result.setSlug("jane-doe");
		result.setProfilePic("pic.png");
		result.setJobId(1);
		result.setJobStartDate(20250101);
		result.setJobEndDate(20251231);
		result.setTimesheetFrequency(1);
		result.setTimesheetStartDay(1);
		return result;
	}

	@Nested
	@DisplayName("getTimesheetEnabledAssignedCandidates Tests")
	class GetTimesheetEnabledAssignedCandidatesTests {

		@Test
		@DisplayName("Should scope the query to the authenticated account id")
		void testScopesQueryToAuthenticatedAccount() {
			// Given
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
			given(JobContractorServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(JobContractorServiceTests.this.jobContractorRepository.getOwnedJobIds(JOB_IDS, DEFAULT_ACCOUNT_ID))
				.willReturn(JOB_IDS);
			given(JobContractorServiceTests.this.jobContractorRepository.getTimesheetEnabledAssignedCandidates(JOB_IDS,
					DEFAULT_ACCOUNT_ID))
				.willReturn(List.of(createQueryResult()));

			// When
			List<TimesheetEnabledAssignedCandidateResponseBodyDto> result = JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS);

			// Then: the account id from the JWT principal must reach the repository so a
			// caller cannot read another account's candidates with just a job id.
			then(JobContractorServiceTests.this.jobContractorRepository).should()
				.getTimesheetEnabledAssignedCandidates(JOB_IDS, DEFAULT_ACCOUNT_ID);
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getTitle()).isEqualTo("Jane Doe");
			assertThat(result.get(0).getEntityType()).isEqualTo(EntityTypeEnum.CANDIDATE.getIdAsString());
		}

		@Test
		@DisplayName("Should reject contractor persona with ForbiddenAccessException")
		void testRejectsContractorPersona() {
			// Given: a contractor persona
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

			// When / Then: blocked up front, regardless of the supplied job ids
			assertThatThrownBy(() -> JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS)).isInstanceOf(ForbiddenAccessException.class);

			then(JobContractorServiceTests.this.jobContractorRepository).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("Should allow contact/client persona (only contractors are blocked)")
		void testAllowsContactPersona() {
			// Given: a contact/client persona is permitted, like an agency user
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.CONTACT);
			given(JobContractorServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(JobContractorServiceTests.this.jobContractorRepository.getOwnedJobIds(JOB_IDS, DEFAULT_ACCOUNT_ID))
				.willReturn(JOB_IDS);
			given(JobContractorServiceTests.this.jobContractorRepository.getTimesheetEnabledAssignedCandidates(JOB_IDS,
					DEFAULT_ACCOUNT_ID))
				.willReturn(List.of(createQueryResult()));

			// When
			List<TimesheetEnabledAssignedCandidateResponseBodyDto> result = JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS);

			// Then: the request is served, not rejected.
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return 404 (ResourceNotFound) when none of the requested jobs belong to the account")
		void testThrowsWhenNoJobsOwnedByAccount() {
			// Given: every requested job id is non-existent or owned by another account
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
			given(JobContractorServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(JobContractorServiceTests.this.jobContractorRepository.getOwnedJobIds(JOB_IDS, DEFAULT_ACCOUNT_ID))
				.willReturn(List.of());

			// When / Then
			assertThatThrownBy(() -> JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS)).isInstanceOf(ResourceNotFoundException.class);

			// The candidate query must never run when nothing is accessible.
			then(JobContractorServiceTests.this.jobContractorRepository).should(never())
				.getTimesheetEnabledAssignedCandidates(JOB_IDS, DEFAULT_ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should query candidates only for the owned subset when some job ids are not accessible")
		void testQueriesOnlyOwnedJobsForPartialOwnership() {
			// Given: only job id 1 of the requested {1, 2} belongs to the account
			List<Integer> ownedJobIds = List.of(1);
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
			given(JobContractorServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(JobContractorServiceTests.this.jobContractorRepository.getOwnedJobIds(JOB_IDS, DEFAULT_ACCOUNT_ID))
				.willReturn(ownedJobIds);
			given(JobContractorServiceTests.this.jobContractorRepository
				.getTimesheetEnabledAssignedCandidates(ownedJobIds, DEFAULT_ACCOUNT_ID))
				.willReturn(List.of(createQueryResult()));

			// When
			List<TimesheetEnabledAssignedCandidateResponseBodyDto> result = JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS);

			// Then: only the owned subset reaches the candidate query.
			then(JobContractorServiceTests.this.jobContractorRepository).should()
				.getTimesheetEnabledAssignedCandidates(ownedJobIds, DEFAULT_ACCOUNT_ID);
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return an empty list (not 404) for an owned job that has no timesheet-enabled candidates")
		void testReturnsEmptyForOwnedJobWithoutCandidates() {
			// Given: the job is owned but has no timesheet-enabled assigned candidates
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
			given(JobContractorServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(JobContractorServiceTests.this.jobContractorRepository.getOwnedJobIds(JOB_IDS, DEFAULT_ACCOUNT_ID))
				.willReturn(JOB_IDS);
			given(JobContractorServiceTests.this.jobContractorRepository.getTimesheetEnabledAssignedCandidates(JOB_IDS,
					DEFAULT_ACCOUNT_ID))
				.willReturn(List.of());

			// When
			List<TimesheetEnabledAssignedCandidateResponseBodyDto> result = JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS);

			// Then: a legitimately empty result stays a clean empty success, not a 404.
			assertThat(result).isEmpty();
		}

		@ParameterizedTest(name = "firstName=[{0}], lastName=[{1}] -> title=[{2}]")
		@DisplayName("Should build candidate title across null/blank first and last name combinations")
		@CsvSource(nullValues = "NULL", value = { "John, NULL, John", // null lastName ->
																		// first name only
				"John, '   ', John", // blank lastName -> first name only
				"NULL, NULL, ''", // both null -> empty title
				"NULL, Smith, ' Smith'" // null firstName with lastName -> leading space +
										// last name
		})
		void testBuildsTitleAcrossNameCombinations(String firstName, String lastName, String expectedTitle) {
			// Given
			given(JobContractorServiceTests.this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
			given(JobContractorServiceTests.this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				.willReturn(DEFAULT_ACCOUNT_ID);
			given(JobContractorServiceTests.this.jobContractorRepository.getOwnedJobIds(JOB_IDS, DEFAULT_ACCOUNT_ID))
				.willReturn(JOB_IDS);
			given(JobContractorServiceTests.this.jobContractorRepository.getTimesheetEnabledAssignedCandidates(JOB_IDS,
					DEFAULT_ACCOUNT_ID))
				.willReturn(List.of(createQueryResultWithNames(firstName, lastName)));

			// When
			List<TimesheetEnabledAssignedCandidateResponseBodyDto> result = JobContractorServiceTests.this.jobContractorService
				.getTimesheetEnabledAssignedCandidates(JOB_IDS);

			// Then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getTitle()).isEqualTo(expectedTitle);
		}

	}

}
