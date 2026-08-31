/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.testdata.ContractorTestDataFactory;
import io.recruitcrm.microservice.timesheet.testdata.ContractorTimesheetRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

/**
 * Unit tests for {@link ContractorTimesheetRepository}: JPQL delegation, parameter
 * binding (including {@code Long} epoch to {@code int}), and error propagation.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ContractorTimesheetRepositoryTests {

	private static final long AGGREGATION_FLAG_ENABLED = 1L;

	private static final long AGGREGATION_FLAG_DISABLED = 0L;

	private static final long CONTRACT_JOB_COUNT_SAMPLE = 5L;

	private static final long CONTRACT_JOB_COUNT_NONE = 0L;

	@Mock
	private EntityManager entityManager;

	private ContractorTimesheetRepository contractorTimesheetRepository;

	@BeforeEach
	void setUp() {
		this.contractorTimesheetRepository = new ContractorTimesheetRepository(this.entityManager);
	}

	@Test
	@DisplayName("Constructor accepts EntityManager and creates repository instance")
	void testConstructorValidEntityManagerCreatesRepository() {
		// Given
		EntityManager em = mock(EntityManager.class);

		// When
		ContractorTimesheetRepository created = new ContractorTimesheetRepository(em);

		// Then
		assertThat(created).isNotNull();
	}

	@ParameterizedTest(name = "countTimesheetEnabledForContractor returns {0}")
	@ValueSource(longs = { AGGREGATION_FLAG_ENABLED, AGGREGATION_FLAG_DISABLED })
	@DisplayName("Count timesheet enabled for contractor delegates JPQL and returns aggregation flag")
	void testCountTimesheetEnabledForContractorReturnsExpectedValue(long expectedResult) {
		// Given
		Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
		Integer accountId = ContractorTestDataFactory.getDefaultAccountId();
		Long currentEpoch = ContractorTimesheetRepositoryTestDataFactory.getDefaultPortalCurrentEpoch();
		String jpql = ContractorTimesheetRepositoryTestDataFactory.jpqlCountTimesheetEnabledForContractor();
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(jpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("currentEpoch", currentEpoch.intValue())).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(expectedResult);

		// When
		Long result = this.contractorTimesheetRepository.countTimesheetEnabledForContractor(contractorId, currentEpoch,
				accountId);

		// Then
		assertThat(result).isEqualTo(Long.valueOf(expectedResult));
		then(this.entityManager).should().createQuery(jpql, Long.class);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("currentEpoch", currentEpoch.intValue());
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Count timesheet enabled for contractor propagates DataAccessException from getSingleResult")
	void testCountTimesheetEnabledForContractorGetSingleResultThrowsPropagates() {
		// Given
		Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
		Integer accountId = ContractorTestDataFactory.getDefaultAccountId();
		Long currentEpoch = ContractorTimesheetRepositoryTestDataFactory.getDefaultPortalCurrentEpoch();
		String jpql = ContractorTimesheetRepositoryTestDataFactory.jpqlCountTimesheetEnabledForContractor();
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(jpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("currentEpoch", currentEpoch.intValue())).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willThrow(new DataAccessException("read failed") {
		});

		// When & Then
		assertThatThrownBy(() -> this.contractorTimesheetRepository.countTimesheetEnabledForContractor(contractorId,
				currentEpoch, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("read failed");
		then(mockQuery).should().getSingleResult();
	}

	@ParameterizedTest(name = "countTimesheetEnabledForContractorWithoutDateCheck returns {0}")
	@ValueSource(longs = { AGGREGATION_FLAG_ENABLED, AGGREGATION_FLAG_DISABLED })
	@DisplayName("Count timesheet enabled without date check binds parameters and returns flag")
	void testCountTimesheetEnabledForContractorWithoutDateCheckReturnsExpectedValue(long expectedResult) {
		// Given
		Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
		Integer accountId = ContractorTestDataFactory.getDefaultAccountId();
		String jpql = ContractorTimesheetRepositoryTestDataFactory
			.jpqlCountTimesheetEnabledForContractorWithoutDateCheck();
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(jpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(expectedResult);

		// When
		Long result = this.contractorTimesheetRepository
			.countTimesheetEnabledForContractorWithoutDateCheck(contractorId, accountId);

		// Then
		assertThat(result).isEqualTo(Long.valueOf(expectedResult));
		then(this.entityManager).should().createQuery(jpql, Long.class);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Count timesheet enabled without date check propagates DataAccessException from createQuery")
	void testCountTimesheetEnabledForContractorWithoutDateCheckCreateQueryThrowsPropagates() {
		// Given
		Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
		Integer accountId = ContractorTestDataFactory.getDefaultAccountId();
		String jpql = ContractorTimesheetRepositoryTestDataFactory
			.jpqlCountTimesheetEnabledForContractorWithoutDateCheck();
		willThrow(new DataAccessException("timeout") {
		}).given(this.entityManager).createQuery(jpql, Long.class);

		// When & Then
		assertThatThrownBy(() -> this.contractorTimesheetRepository
			.countTimesheetEnabledForContractorWithoutDateCheck(contractorId, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("timeout");
	}

	@ParameterizedTest(name = "countContractJobsForContractor returns {0}")
	@ValueSource(longs = { CONTRACT_JOB_COUNT_SAMPLE, CONTRACT_JOB_COUNT_NONE })
	@DisplayName("Count contract jobs for contractor runs distinct job count query")
	void testCountContractJobsForContractorReturnsExpectedValue(long expectedResult) {
		// Given
		Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
		Integer accountId = ContractorTestDataFactory.getDefaultAccountId();
		String jpql = ContractorTimesheetRepositoryTestDataFactory.jpqlCountContractJobsForContractor();
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(jpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(expectedResult);

		// When
		Long result = this.contractorTimesheetRepository.countContractJobsForContractor(contractorId, accountId);

		// Then
		assertThat(result).isEqualTo(Long.valueOf(expectedResult));
		then(this.entityManager).should().createQuery(jpql, Long.class);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Count contract jobs for contractor propagates DataAccessException from getSingleResult")
	void testCountContractJobsForContractorGetSingleResultThrowsPropagates() {
		// Given
		Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
		Integer accountId = ContractorTestDataFactory.getDefaultAccountId();
		String jpql = ContractorTimesheetRepositoryTestDataFactory.jpqlCountContractJobsForContractor();
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(jpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		willThrow(new DataAccessException("no result") {
		}).given(mockQuery).getSingleResult();

		// When & Then
		assertThatThrownBy(
				() -> this.contractorTimesheetRepository.countContractJobsForContractor(contractorId, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("no result");
	}

}
