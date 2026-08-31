/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.contractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ContractorJobRepository class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorJobRepositoryTests {

	@Mock
	private EntityManager entityManager;

	private ContractorJobRepository contractorJobRepository;

	private static final Integer DEFAULT_CONTRACTOR_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	// ========== Helper Methods ==========

	private static List<ContractorJobResponseBodyDto> createContractorJobList() {
		return Arrays.asList(new ContractorJobResponseBodyDto(1, "Job 1", "Company 1"),
				new ContractorJobResponseBodyDto(2, "Job 2", null));
	}

	private static final String EXPECTED_JPQL = """
			SELECT new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto(
			    j.id,
			    j.name,
			    c.companyName
			)
			FROM AssignCandidateJob acj
			JOIN acj.job j
			LEFT JOIN j.company c
			JOIN TimesheetSettingAssociation tsa
			    ON acj.candidateId = tsa.contractorId
			    AND acj.jobId = tsa.jobId
			WHERE tsa.contractorId = :contractorId
			  AND j.jobType IN ('contract', 'contracttopermanent')
			  AND j.accountId = :accountId
			""";

	@BeforeEach
	void setUp() {
		this.contractorJobRepository = new ContractorJobRepository(this.entityManager);
	}

	// ========== findJobsByContractorId Tests ==========

	@Nested
	@DisplayName("findJobsByContractorId Tests")
	class FindJobsByContractorIdTests {

		@Test
		@DisplayName("Should return jobs when found for contractor")
		@SuppressWarnings("unchecked")
		void testFindJobsByContractorIdValidParametersReturnsJobs() {
			// Given
			List<ContractorJobResponseBodyDto> expectedJobs = createContractorJobList();
			TypedQuery<ContractorJobResponseBodyDto> mockQuery = mock(TypedQuery.class);
			String expectedJpql = EXPECTED_JPQL;

			given(ContractorJobRepositoryTests.this.entityManager.createQuery(expectedJpql,
					ContractorJobResponseBodyDto.class))
				.willReturn(mockQuery);
			given(mockQuery.setParameter("contractorId", DEFAULT_CONTRACTOR_ID)).willReturn(mockQuery);
			given(mockQuery.setParameter("accountId", DEFAULT_ACCOUNT_ID)).willReturn(mockQuery);
			given(mockQuery.getResultList()).willReturn(expectedJobs);

			// When
			List<ContractorJobResponseBodyDto> result = ContractorJobRepositoryTests.this.contractorJobRepository
				.findJobsByContractorId(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expectedJobs);
			// A job whose company is absent must still be returned, with a null
			// companyName
			assertThat(result).extracting(ContractorJobResponseBodyDto::getCompanyName)
				.containsExactly("Company 1", null);
			then(ContractorJobRepositoryTests.this.entityManager).should()
				.createQuery(expectedJpql, ContractorJobResponseBodyDto.class);
			then(mockQuery).should().setParameter("contractorId", DEFAULT_CONTRACTOR_ID);
			then(mockQuery).should().setParameter("accountId", DEFAULT_ACCOUNT_ID);
			then(mockQuery).should().getResultList();
		}

		@Test
		@DisplayName("Should return empty list when no jobs found")
		@SuppressWarnings("unchecked")
		void testFindJobsByContractorIdNoJobsReturnsEmptyList() {
			// Given
			TypedQuery<ContractorJobResponseBodyDto> mockQuery = mock(TypedQuery.class);
			String expectedJpql = EXPECTED_JPQL;

			given(ContractorJobRepositoryTests.this.entityManager.createQuery(expectedJpql,
					ContractorJobResponseBodyDto.class))
				.willReturn(mockQuery);
			given(mockQuery.setParameter("contractorId", DEFAULT_CONTRACTOR_ID)).willReturn(mockQuery);
			given(mockQuery.setParameter("accountId", DEFAULT_ACCOUNT_ID)).willReturn(mockQuery);
			given(mockQuery.getResultList()).willReturn(Collections.emptyList());

			// When
			List<ContractorJobResponseBodyDto> result = ContractorJobRepositoryTests.this.contractorJobRepository
				.findJobsByContractorId(DEFAULT_CONTRACTOR_ID, DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEmpty();
		}

	}

}
