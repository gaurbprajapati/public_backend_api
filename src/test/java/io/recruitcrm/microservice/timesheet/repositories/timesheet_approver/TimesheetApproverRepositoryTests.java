/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.timesheet_approver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApproverJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for TimesheetApproverRepository class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class TimesheetApproverRepositoryTests {

	private static final Integer DEFAULT_USER_TYPE_ID = 1;

	private static final Integer DEFAULT_TIMESHEET_SETTING_ID = 100;

	private static final Integer DEFAULT_ENTITY_ID = 50;

	private static final Integer DEFAULT_JOB_ID = 10;

	private static final Integer DEFAULT_ACCOUNT_ID = 1;

	@Mock
	private EntityManager entityManager;

	@Mock
	private TimesheetApproverJpaRepository timesheetApproverJpaRepository;

	@Mock
	private TypedQuery<TimesheetApprover> typedQuery;

	@Mock
	private TypedQuery<Integer> intTypedQuery;

	@Mock
	private Query nativeQuery;

	private TimesheetApproverRepository timesheetApproverRepository;

	private static TimesheetApprover createDefaultTimesheetApprover() {
		TimesheetApprover approver = new TimesheetApprover();
		approver.setId(1);
		approver.setUserTypeId(DEFAULT_USER_TYPE_ID);
		approver.setTimesheetSettingId(DEFAULT_TIMESHEET_SETTING_ID);
		approver.setEntityId(DEFAULT_ENTITY_ID);
		return approver;
	}

	private static List<TimesheetApprover> createTimesheetApproverList() {
		List<TimesheetApprover> approvers = new ArrayList<>();
		approvers.add(createDefaultTimesheetApprover());
		return approvers;
	}

	@BeforeEach
	void setUp() {
		this.timesheetApproverRepository = new TimesheetApproverRepository(this.entityManager,
				this.timesheetApproverJpaRepository);
	}

	@Nested
	@DisplayName("createTimesheetApprover Tests")
	class CreateTimesheetApproverTests {

		@Test
		@DisplayName("Should create and save timesheet approver")
		void testCreateTimesheetApproverValidParametersCreatesApprover() {
			// Given
			TimesheetApprover savedApprover = createDefaultTimesheetApprover();
			given(TimesheetApproverRepositoryTests.this.timesheetApproverJpaRepository
				.save(any(TimesheetApprover.class))).willReturn(savedApprover);

			// When
			TimesheetApprover result = TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.createTimesheetApprover(DEFAULT_USER_TYPE_ID, DEFAULT_TIMESHEET_SETTING_ID, DEFAULT_ENTITY_ID);

			// Then
			assertThat(result).isEqualTo(savedApprover);
			then(TimesheetApproverRepositoryTests.this.timesheetApproverJpaRepository).should()
				.save(any(TimesheetApprover.class));
		}

	}

	@Nested
	@DisplayName("findByTimesheetSettingId Tests")
	class FindByTimesheetSettingIdTests {

		@Test
		@DisplayName("Should return approvers when found")
		void testFindByTimesheetSettingIdApproversFoundReturnsApprovers() {
			// Given
			List<TimesheetApprover> approvers = createTimesheetApproverList();
			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString(),
					eq(TimesheetApprover.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.typedQuery);
			given(TimesheetApproverRepositoryTests.this.typedQuery.setParameter("timesheetSettingId",
					DEFAULT_TIMESHEET_SETTING_ID))
				.willReturn(TimesheetApproverRepositoryTests.this.typedQuery);
			given(TimesheetApproverRepositoryTests.this.typedQuery.getResultList()).willReturn(approvers);

			// When
			List<TimesheetApprover> result = TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.findByTimesheetSettingId(DEFAULT_TIMESHEET_SETTING_ID);

			// Then
			assertThat(result).isEqualTo(approvers).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when no approvers found")
		void testFindByTimesheetSettingIdNoApproversFoundReturnsEmptyList() {
			// Given
			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString(),
					eq(TimesheetApprover.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.typedQuery);
			given(TimesheetApproverRepositoryTests.this.typedQuery.setParameter("timesheetSettingId",
					DEFAULT_TIMESHEET_SETTING_ID))
				.willReturn(TimesheetApproverRepositoryTests.this.typedQuery);
			given(TimesheetApproverRepositoryTests.this.typedQuery.getResultList()).willReturn(new ArrayList<>());

			// When
			List<TimesheetApprover> result = TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.findByTimesheetSettingId(DEFAULT_TIMESHEET_SETTING_ID);

			// Then
			assertThat(result).isEmpty();
		}

	}

	@Nested
	@DisplayName("deleteTimesheetApprovers Tests")
	class DeleteTimesheetApproversTests {

		@Test
		@DisplayName("Should execute delete query with correct parameters")
		void testDeleteTimesheetApproversValidParametersExecutesDeleteQuery() {
			// Given
			List<Integer> contractorIds = List.of(1, 2, 3);
			List<Integer> agencyIds = List.of(10, 20);
			Query mockQuery = mock(Query.class);

			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString())).willReturn(mockQuery);
			given(mockQuery.setParameter("accountId", DEFAULT_ACCOUNT_ID)).willReturn(mockQuery);
			given(mockQuery.setParameter("jobId", DEFAULT_JOB_ID)).willReturn(mockQuery);
			given(mockQuery.setParameter("contractorIds", contractorIds)).willReturn(mockQuery);
			given(mockQuery.setParameter("agencyIds", agencyIds)).willReturn(mockQuery);
			given(mockQuery.setParameter("userTypeId", UserTypeEnum.AGENCY_RECRUITER.getId())).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(1);

			// When
			TimesheetApproverRepositoryTests.this.timesheetApproverRepository.deleteTimesheetApprovers(DEFAULT_JOB_ID,
					contractorIds, agencyIds, DEFAULT_ACCOUNT_ID);

			// Then
			then(mockQuery).should().executeUpdate();
		}

	}

	@Nested
	@DisplayName("addTimesheetApprovers Tests")
	class AddTimesheetApproversTests {

		@Test
		@DisplayName("Should add approvers when timesheet settings exist")
		void testAddTimesheetApproversTimesheetSettingsExistAddsApprovers() {
			// Given
			List<Integer> contractorIds = List.of(1, 2);
			List<Integer> agencyIds = List.of(10, 20);
			List<Integer> timesheetSettingIds = List.of(100, 200);

			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString(), eq(Integer.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.setParameter("accountId", DEFAULT_ACCOUNT_ID))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.setParameter("jobId", DEFAULT_JOB_ID))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.setParameter("contractorIds", contractorIds))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.getResultList()).willReturn(timesheetSettingIds);

			given(TimesheetApproverRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.setParameter("userTypeId",
					UserTypeEnum.AGENCY_RECRUITER.getId()))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.setParameter(eq("tsId"), any(Integer.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.setParameter(eq("agencyId"), any(Integer.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.executeUpdate()).willReturn(1);

			// When
			TimesheetApproverRepositoryTests.this.timesheetApproverRepository.addTimesheetApprovers(DEFAULT_JOB_ID,
					contractorIds, agencyIds, DEFAULT_ACCOUNT_ID);

			// Then - verify that executeUpdate was called (2 tsIds * 2 agencyIds = 4
			// times)
			then(TimesheetApproverRepositoryTests.this.nativeQuery).should(org.mockito.Mockito.times(4))
				.executeUpdate();
		}

	}

	@Nested
	@DisplayName("deleteTimesheetApproversForClients Tests")
	class DeleteTimesheetApproversForClientsTests {

		@Test
		@DisplayName("Should execute delete query for clients")
		void testDeleteTimesheetApproversForClientsExecutesDeleteQuery() {
			// Given
			List<Integer> contractorIds = List.of(1, 2, 3);
			List<Integer> clientIds = List.of(10, 20);
			Query mockQuery = mock(Query.class);

			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString())).willReturn(mockQuery);
			given(mockQuery.setParameter("accountId", DEFAULT_ACCOUNT_ID)).willReturn(mockQuery);
			given(mockQuery.setParameter("jobId", DEFAULT_JOB_ID)).willReturn(mockQuery);
			given(mockQuery.setParameter("contractorIds", contractorIds)).willReturn(mockQuery);
			given(mockQuery.setParameter("clientIds", clientIds)).willReturn(mockQuery);
			given(mockQuery.setParameter("userTypeId", UserTypeEnum.COMPANY_CONTACT.getId())).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(1);

			// When
			TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.deleteTimesheetApproversForClients(DEFAULT_JOB_ID, contractorIds, clientIds, DEFAULT_ACCOUNT_ID);

			// Then
			then(mockQuery).should().executeUpdate();
		}

	}

	@Nested
	@DisplayName("addTimesheetApproversForClients Tests")
	class AddTimesheetApproversForClientsTests {

		@Test
		@DisplayName("Should add approvers for clients when timesheet settings exist")
		void testAddTimesheetApproversForClientsTimesheetSettingsExistAddsApprovers() {
			// Given
			List<Integer> contractorIds = List.of(1, 2);
			List<Integer> clientIds = List.of(10, 20);
			List<Integer> timesheetSettingIds = List.of(100, 200);

			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString(), eq(Integer.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.setParameter("accountId", DEFAULT_ACCOUNT_ID))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.setParameter("jobId", DEFAULT_JOB_ID))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.setParameter("contractorIds", contractorIds))
				.willReturn(TimesheetApproverRepositoryTests.this.intTypedQuery);
			given(TimesheetApproverRepositoryTests.this.intTypedQuery.getResultList()).willReturn(timesheetSettingIds);

			given(TimesheetApproverRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.setParameter("userTypeId",
					UserTypeEnum.COMPANY_CONTACT.getId()))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.setParameter(eq("tsId"), any(Integer.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.setParameter(eq("clientId"), any(Integer.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.nativeQuery);
			given(TimesheetApproverRepositoryTests.this.nativeQuery.executeUpdate()).willReturn(1);

			// When
			TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.addTimesheetApproversForClients(DEFAULT_JOB_ID, contractorIds, clientIds, DEFAULT_ACCOUNT_ID);

			// Then - verify that executeUpdate was called (2 tsIds * 2 clientIds = 4
			// times)
			then(TimesheetApproverRepositoryTests.this.nativeQuery).should(org.mockito.Mockito.times(4))
				.executeUpdate();
		}

	}

	@Nested
	@DisplayName("createTimesheetApproverInBulk Tests")
	class CreateTimesheetApproverInBulkTests {

		@Test
		@DisplayName("Should save all approvers in bulk")
		void testCreateTimesheetApproverInBulkValidListSavesAllApprovers() {
			// Given
			List<TimesheetApprover> approvers = createTimesheetApproverList();

			// When
			TimesheetApproverRepositoryTests.this.timesheetApproverRepository.createTimesheetApproverInBulk(approvers);

			// Then
			then(TimesheetApproverRepositoryTests.this.timesheetApproverJpaRepository).should().saveAll(approvers);
		}

	}

	@Nested
	@DisplayName("findByTimesheetSettingIds Tests")
	class FindByTimesheetSettingIdsTests {

		@Test
		@DisplayName("Should return empty list when input is null")
		void testFindByTimesheetSettingIdsNullInputReturnsEmptyList() {
			// When
			List<TimesheetApprover> result = TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.findByTimesheetSettingIds(null);

			// Then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return empty list when input is empty")
		void testFindByTimesheetSettingIdsEmptyInputReturnsEmptyList() {
			// When
			List<TimesheetApprover> result = TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.findByTimesheetSettingIds(new ArrayList<>());

			// Then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return approvers when timesheet setting IDs are valid")
		void testFindByTimesheetSettingIdsValidIdsReturnsApprovers() {
			// Given
			List<Integer> timesheetSettingIds = List.of(100, 200);
			List<TimesheetApprover> approvers = createTimesheetApproverList();

			given(TimesheetApproverRepositoryTests.this.entityManager.createQuery(anyString(),
					eq(TimesheetApprover.class)))
				.willReturn(TimesheetApproverRepositoryTests.this.typedQuery);
			given(TimesheetApproverRepositoryTests.this.typedQuery.setParameter("timesheetSettingIds",
					timesheetSettingIds))
				.willReturn(TimesheetApproverRepositoryTests.this.typedQuery);
			given(TimesheetApproverRepositoryTests.this.typedQuery.getResultList()).willReturn(approvers);

			// When
			List<TimesheetApprover> result = TimesheetApproverRepositoryTests.this.timesheetApproverRepository
				.findByTimesheetSettingIds(timesheetSettingIds);

			// Then
			assertThat(result).isEqualTo(approvers);
		}

	}

}
