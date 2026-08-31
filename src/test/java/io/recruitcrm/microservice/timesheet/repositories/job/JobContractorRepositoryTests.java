/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateQueryResultDto;
import io.recruitcrm.microservice.timesheet.testdata.JobContractorRepositoryTestDataFactory;

import java.util.Collections;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectField;
import org.jooq.TableLike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataAccessException;

/**
 * Unit test cases for {@link JobContractorRepository} with 100% line and branch coverage.
 * <p>
 * Repository Method Coverage:
 * <ol>
 * <li>getTimesheetEnabledAssignedCandidates(...) - JOOQ query joining candidate, job
 * assignments and timesheet setting associations, scoped to the authenticated
 * account.</li>
 * <li>getOwnedJobIds(...) - JOOQ query returning the subset of job IDs owned by the
 * supplied account.</li>
 * </ol>
 * The JOOQ {@link DSLContext} is mocked with deep stubs so the query builder chain can be
 * exercised without a real database, following the project repository testing rules.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JobContractorRepository Tests")
class JobContractorRepositoryTests {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private DSLContext auroraDbDSLContext;

	private JobContractorRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new JobContractorRepository(this.auroraDbDSLContext);
	}

	// ===== getTimesheetEnabledAssignedCandidates Tests =====

	@Test
	@DisplayName("Get timesheet enabled assigned candidates should build JOOQ query and return results")
	void testGetTimesheetEnabledAssignedCandidatesValidParametersReturnsResults() {
		// Given
		List<Integer> jobIds = JobContractorRepositoryTestDataFactory.createJobIds();
		List<TimesheetEnabledAssignedCandidateQueryResultDto> expected = JobContractorRepositoryTestDataFactory
			.createAssignedCandidates();

		given(this.auroraDbDSLContext
			.select(any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class))
			.from(any(TableLike.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.where(any(Condition.class))
			.fetch()
			.into(TimesheetEnabledAssignedCandidateQueryResultDto.class)).willReturn(expected);

		// When
		List<TimesheetEnabledAssignedCandidateQueryResultDto> result = this.repository
			.getTimesheetEnabledAssignedCandidates(jobIds, JobContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Get timesheet enabled assigned candidates should return empty list when no candidates found")
	void testGetTimesheetEnabledAssignedCandidatesNoResultsReturnsEmptyList() {
		// Given
		List<Integer> jobIds = JobContractorRepositoryTestDataFactory.createJobIds();

		given(this.auroraDbDSLContext
			.select(any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class))
			.from(any(TableLike.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.where(any(Condition.class))
			.fetch()
			.into(TimesheetEnabledAssignedCandidateQueryResultDto.class)).willReturn(Collections.emptyList());

		// When
		List<TimesheetEnabledAssignedCandidateQueryResultDto> result = this.repository
			.getTimesheetEnabledAssignedCandidates(jobIds, JobContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get timesheet enabled assigned candidates should propagate DataAccessException from query execution")
	void testGetTimesheetEnabledAssignedCandidatesDataAccessExceptionPropagatesException() {
		// Given
		List<Integer> jobIds = JobContractorRepositoryTestDataFactory.createJobIds();

		given(this.auroraDbDSLContext
			.select(any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class))
			.from(any(TableLike.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.innerJoin(any(TableLike.class))
			.on(any(Condition.class))
			.where(any(Condition.class))
			.fetch()).willThrow(new DataAccessException("Database connection failed") {
			});

		// When & Then
		assertThatThrownBy(() -> this.repository.getTimesheetEnabledAssignedCandidates(jobIds,
				JobContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database connection failed");
	}

	// ===== getOwnedJobIds Tests =====

	@Test
	@DisplayName("Get owned job IDs should build JOOQ query and return owned ids")
	void testGetOwnedJobIdsValidParametersReturnsOwnedIds() {
		// Given
		List<Integer> jobIds = JobContractorRepositoryTestDataFactory.createJobIds();
		List<Integer> expected = JobContractorRepositoryTestDataFactory.createOwnedJobIds();

		given(this.auroraDbDSLContext.select(any(SelectField.class))
			.from(any(TableLike.class))
			.where(any(Condition.class))
			.fetch(any(Field.class))).willReturn(expected);

		// When
		List<Integer> result = this.repository.getOwnedJobIds(jobIds,
				JobContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Get owned job IDs should return empty list when no jobs are owned by the account")
	void testGetOwnedJobIdsNoOwnedJobsReturnsEmptyList() {
		// Given
		List<Integer> jobIds = JobContractorRepositoryTestDataFactory.createJobIds();

		given(this.auroraDbDSLContext.select(any(SelectField.class))
			.from(any(TableLike.class))
			.where(any(Condition.class))
			.fetch(any(Field.class))).willReturn(Collections.emptyList());

		// When
		List<Integer> result = this.repository.getOwnedJobIds(jobIds,
				JobContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get owned job IDs should propagate DataAccessException from query execution")
	void testGetOwnedJobIdsDataAccessExceptionPropagatesException() {
		// Given
		List<Integer> jobIds = JobContractorRepositoryTestDataFactory.createJobIds();

		given(this.auroraDbDSLContext.select(any(SelectField.class))
			.from(any(TableLike.class))
			.where(any(Condition.class))
			.fetch(any(Field.class))).willThrow(new DataAccessException("Database connection failed") {
			});

		// When & Then
		assertThatThrownBy(
				() -> this.repository.getOwnedJobIds(jobIds, JobContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database connection failed");
	}

}
