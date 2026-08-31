/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.timesheet_setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingUserPreference;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBreakInfoDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetSettingTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

/**
 * Unit tests for {@link TimesheetSettingRepository}: JPQL/native delegation, parameter
 * binding, early-return branches, and result mapping.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TimesheetSettingRepositoryTests {

	private static final String JPQL_VALIDATE_CONSISTENCY = "SELECT COUNT(ts) FROM TimesheetSetting ts "
			+ "JOIN ts.association a " + "WHERE a.contractorId IN :contractorIds "
			+ "GROUP BY a.jobId, ts.timesheetStartDay, ts.timesheetFrequency "
			+ "HAVING COUNT(DISTINCT a.contractorId) = :contractorCount";

	private static final String JPQL_FIND_LATEST_BY_JOB_AND_CONTRACTORS = "SELECT ts FROM TimesheetSetting ts "
			+ "JOIN ts.association a " + "WHERE a.jobId = :jobId AND a.contractorId IN :contractorIds "
			+ "AND ts.id IN (SELECT MAX(innerTs.id) FROM TimesheetSetting innerTs " + "JOIN innerTs.association innerA "
			+ "WHERE innerA.jobId = :jobId AND innerA.contractorId IN :contractorIds "
			+ "GROUP BY innerA.contractorId)";

	private static final String JPQL_FIND_BY_JOB_CONTRACTOR = "SELECT t FROM TimesheetSetting t "
			+ "JOIN t.association a " + "WHERE a.jobId = :jobId AND a.contractorId = :contractorId "
			+ "ORDER BY t.id DESC";

	private static final String JPQL_FIND_FIRST_BY_JOB_CONTRACTOR = "SELECT t FROM TimesheetSetting t "
			+ "WHERE t.association.jobId = :jobId AND t.association.contractorId = :contractorId ORDER BY t.id ASC";

	private static final String JPQL_FIND_BY_ID_AND_ACCOUNT = "SELECT t FROM TimesheetSetting t "
			+ "WHERE t.id = :id AND t.accountId = :accountId ORDER BY t.id DESC";

	private static final String JPQL_VALIDATE_TIMESHEETS_EXIST = "SELECT COUNT(tl.id) " + "FROM TimeLog tl "
			+ "JOIN tl.timesheet t " + "JOIN t.timesheetSetting ts " + "JOIN ts.association a "
			+ "WHERE a.jobId = :jobId " + "AND a.contractorId IN :contractorIds " + "AND ts.accountId = :accountId "
			+ "AND tl.date IN :dates";

	private static final String JPQL_FETCH_ENABLED_ASSIGNMENT_IDS = "SELECT ajc.id FROM AssignCandidateJob ajc "
			+ "JOIN TimesheetSettingAssociation tsa ON ajc.jobId = tsa.jobId AND ajc.candidateId = tsa.contractorId "
			+ "WHERE ajc.id IN :assignmentIds AND ajc.accountId = :accountId ";

	private static final String NATIVE_TEMPLATE_WORK_DAY_SQL = "SELECT ts.id as timesheetSettingId, "
			+ "GROUP_CONCAT(JSON_EXTRACT(json_item.value, '$.workDayId') ORDER BY JSON_EXTRACT(json_item.value, '$.workDayId')) as workDayIds "
			+ "FROM cst_timesheet_setting_t ts "
			+ "CROSS JOIN JSON_TABLE(ts.template_work_day, '$[*]' COLUMNS(value JSON PATH '$')) as json_item "
			+ "WHERE ts.id IN :timesheetSettingIds " + "GROUP BY ts.id";

	private static final String JPQL_FIND_USER_PREFERENCE = "SELECT p FROM TimesheetSettingUserPreference p "
			+ "WHERE p.accountId = :accountId AND p.addedBy = :userId ORDER BY p.id DESC";

	private static final String JPQL_UPDATE_UNPLANNED_PAY = "UPDATE TimesheetSetting ts "
			+ "SET ts.isUnplannedHoursPayEnabled = :isUnplannedHoursPayEnabled WHERE ts.association.id IN :associationIds";

	private static final String JPQL_UPDATE_REMARK_MANDATORY = "UPDATE TimesheetSetting ts "
			+ "SET ts.isRemarkMandatory = :isRemarkMandatory WHERE ts.association.id IN :associationIds";

	private static final String JPQL_FIND_WORK_LOG_TYPE_BY_IDS = "SELECT ts.id, ts.workLogType FROM TimesheetSetting ts "
			+ "WHERE ts.id IN :ids";

	private static final String JPQL_FIND_BREAK_INFO_BY_IDS = "SELECT ts.id, ts.calculateBreakTime, ts.breakTimeThreshold "
			+ "FROM TimesheetSetting ts WHERE ts.id IN :ids";

	private static final String JPQL_FIND_LATEST_FOR_PAIRS_ONE = "SELECT ts FROM TimesheetSetting ts "
			+ "JOIN ts.association a " + "WHERE ((a.jobId = :jobId0 AND a.contractorId = :contractorId0)) "
			+ "AND ts.id IN (SELECT MAX(innerTs.id) FROM TimesheetSetting innerTs " + "JOIN innerTs.association innerA "
			+ "WHERE ((innerA.jobId = :jobId0 AND innerA.contractorId = :contractorId0)) "
			+ "GROUP BY innerA.jobId, innerA.contractorId)";

	private static final String JPQL_FIND_LATEST_FOR_PAIRS_TWO = "SELECT ts FROM TimesheetSetting ts "
			+ "JOIN ts.association a "
			+ "WHERE ((a.jobId = :jobId0 AND a.contractorId = :contractorId0) OR (a.jobId = :jobId1 AND a.contractorId = :contractorId1)) "
			+ "AND ts.id IN (SELECT MAX(innerTs.id) FROM TimesheetSetting innerTs " + "JOIN innerTs.association innerA "
			+ "WHERE ((innerA.jobId = :jobId0 AND innerA.contractorId = :contractorId0) OR (innerA.jobId = :jobId1 AND innerA.contractorId = :contractorId1)) "
			+ "GROUP BY innerA.jobId, innerA.contractorId)";

	@Mock
	private EntityManager entityManager;

	private TimesheetSettingRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new TimesheetSettingRepository(this.entityManager);
	}

	@Test
	@DisplayName("Constructor stores EntityManager reference")
	void testConstructorWithEntityManagerStoresReference() {
		// Given
		EntityManager em = mock(EntityManager.class);

		// When
		TimesheetSettingRepository created = new TimesheetSettingRepository(em);

		// Then
		assertThat(created).isNotNull();
	}

	@Test
	@DisplayName("Validate timesheet settings consistency returns true when grouped counts exist")
	void testValidateTimesheetSettingsConsistencyResultsNonEmptyReturnsTrue() {
		// Given
		List<Integer> contractorIds = Arrays.asList(101, 102);
		int contractorCount = 2;
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_CONSISTENCY, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorIds", contractorIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorCount", contractorCount)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(2L));

		// When
		Boolean result = this.repository.validateTimesheetSettingsConsistency(contractorIds, contractorCount);

		// Then
		assertThat(result).isTrue();
		then(this.entityManager).should().createQuery(JPQL_VALIDATE_CONSISTENCY, Long.class);
		then(mockQuery).should().setParameter("contractorIds", contractorIds);
		then(mockQuery).should().setParameter("contractorCount", contractorCount);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheet settings consistency returns false when result list is empty")
	void testValidateTimesheetSettingsConsistencyEmptyListReturnsFalse() {
		// Given
		List<Integer> contractorIds = Collections.emptyList();
		int contractorCount = 0;
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_CONSISTENCY, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorIds", contractorIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorCount", contractorCount)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Boolean result = this.repository.validateTimesheetSettingsConsistency(contractorIds, contractorCount);

		// Then
		assertThat(result).isFalse();
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheet settings consistency propagates DataAccessException from createQuery")
	void testValidateTimesheetSettingsConsistencyCreateQueryThrowsPropagates() {
		// Given
		List<Integer> contractorIds = List.of(1);
		willThrow(new DataAccessException("fail") {
		}).given(this.entityManager).createQuery(JPQL_VALIDATE_CONSISTENCY, Long.class);

		// When & Then
		assertThatThrownBy(() -> this.repository.validateTimesheetSettingsConsistency(contractorIds, 1))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("fail");
	}

	@Test
	@DisplayName("Find latest timesheet settings by job and contractors returns query list")
	void testFindLatestTimesheetSettingsByJobIdAndContractorIdsReturnsList() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = Arrays.asList(10, 20);
		TimesheetSetting setting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_LATEST_BY_JOB_AND_CONTRACTORS, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", jobId)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorIds", contractorIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(setting));

		// When
		List<TimesheetSetting> result = this.repository.findLatestTimesheetSettingsByJobIdAndContractorIds(jobId,
				contractorIds);

		// Then
		assertThat(result).containsExactly(setting);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find latest timesheet settings propagates DataAccessException")
	void testFindLatestTimesheetSettingsByJobIdAndContractorIdsThrows() {
		// Given
		willThrow(new DataAccessException("q") {
		}).given(this.entityManager).createQuery(JPQL_FIND_LATEST_BY_JOB_AND_CONTRACTORS, TimesheetSetting.class);

		// When & Then
		Throwable thrown = catchThrowable(
				() -> this.repository.findLatestTimesheetSettingsByJobIdAndContractorIds(1, List.of(1)));
		assertThat(thrown).isInstanceOf(DataAccessException.class);
	}

	@Test
	@DisplayName("Find by job and contractor returns latest row when present")
	void testFindByJobIdContractorIdWhenPresentReturnsOptional() {
		// Given
		TimesheetSetting setting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_BY_JOB_CONTRACTOR, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", 2)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(setting));

		// When
		Optional<TimesheetSetting> result = this.repository.findByJobIdContractorId(1, 2);

		// Then
		assertThat(result).contains(setting);
		then(mockQuery).should().setMaxResults(1);
	}

	@Test
	@DisplayName("Find by job and contractor returns empty when no rows")
	void testFindByJobIdContractorIdWhenEmptyReturnsEmptyOptional() {
		// Given
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_BY_JOB_CONTRACTOR, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", 2)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Optional<TimesheetSetting> result = this.repository.findByJobIdContractorId(1, 2);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find first by job and contractor returns first element when list non-empty")
	void testFindFirstByJobIdContractorIdReturnsFirst() {
		// Given
		TimesheetSetting older = TimesheetSettingTestDataFactory.createTimesheetSetting();
		older.setId(1);
		TimesheetSetting newer = TimesheetSettingTestDataFactory.createTimesheetSetting();
		newer.setId(2);
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_FIRST_BY_JOB_CONTRACTOR, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", 5)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", 9)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(older, newer));

		// When
		Optional<TimesheetSetting> result = this.repository.findFirstByJobIdContractorId(5, 9);

		// Then
		assertThat(result).contains(older);
	}

	@Test
	@DisplayName("Find first by job and contractor returns empty when no rows")
	void testFindFirstByJobIdContractorIdEmptyReturnsEmpty() {
		// Given
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_FIRST_BY_JOB_CONTRACTOR, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", 2)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Optional<TimesheetSetting> result = this.repository.findFirstByJobIdContractorId(1, 2);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Create timesheet setting persists when id is null and returns same instance")
	void testCreateTimesheetSettingNullIdPersistsAndReturnsSame() {
		// Given
		TimesheetSetting entity = TimesheetSettingTestDataFactory.createTimesheetSettingWithoutId();
		willDoNothing().given(this.entityManager).persist(entity);

		// When
		TimesheetSetting result = this.repository.createTimesheetSetting(entity);

		// Then
		assertThat(result).isSameAs(entity);
		then(this.entityManager).should().persist(entity);
		then(this.entityManager).should(never()).merge(any());
	}

	@Test
	@DisplayName("Create timesheet setting merges when id is non-null")
	void testCreateTimesheetSettingNonNullIdMerges() {
		// Given
		TimesheetSetting entity = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting merged = TimesheetSettingTestDataFactory.createTimesheetSetting();
		merged.setId(entity.getId());
		given(this.entityManager.merge(entity)).willReturn(merged);

		// When
		TimesheetSetting result = this.repository.createTimesheetSetting(entity);

		// Then
		assertThat(result).isSameAs(merged);
		then(this.entityManager).should().merge(entity);
		then(this.entityManager).should(never()).persist(any());
	}

	@Test
	@DisplayName("Create timesheet setting propagates exception on persist")
	void testCreateTimesheetSettingPersistThrows() {
		// Given
		TimesheetSetting entity = TimesheetSettingTestDataFactory.createTimesheetSettingWithoutId();
		willThrow(new DataAccessException("persist") {
		}).given(this.entityManager).persist(entity);

		// When & Then
		assertThatThrownBy(() -> this.repository.createTimesheetSetting(entity))
			.isInstanceOf(DataAccessException.class);
	}

	@Test
	@DisplayName("Create timesheet setting propagates exception on merge")
	void testCreateTimesheetSettingMergeThrows() {
		// Given
		TimesheetSetting entity = TimesheetSettingTestDataFactory.createTimesheetSetting();
		willThrow(new DataAccessException("merge") {
		}).given(this.entityManager).merge(entity);

		// When & Then
		assertThatThrownBy(() -> this.repository.createTimesheetSetting(entity))
			.isInstanceOf(DataAccessException.class);
	}

	@Test
	@DisplayName("Find by id and account returns optional when row exists")
	void testFindByIdAndAccountIdReturnsOptional() {
		// Given
		TimesheetSetting setting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_BY_ID_AND_ACCOUNT, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("id", 10)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 20)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(setting));

		// When
		Optional<TimesheetSetting> result = this.repository.findByIdAndAccountId(10, 20);

		// Then
		assertThat(result).contains(setting);
	}

	@Test
	@DisplayName("Find by id and account returns empty when no row")
	void testFindByIdAndAccountIdEmpty() {
		// Given
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_BY_ID_AND_ACCOUNT, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("id", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 1)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Optional<TimesheetSetting> result = this.repository.findByIdAndAccountId(1, 1);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Validate timesheets exist returns true when count positive")
	void testValidateTimesheetsExistCountPositiveReturnsTrue() {
		// Given
		List<Integer> dates = List.of(20230101);
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_TIMESHEETS_EXIST, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorIds", List.of(2))).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 3)).willReturn(mockQuery);
		given(mockQuery.setParameter("dates", dates)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(3L);

		// When
		Boolean result = this.repository.validateTimesheetsExist(dates, 3, 1, List.of(2));

		// Then
		assertThat(result).isTrue();
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Validate timesheets exist returns false when count is zero")
	void testValidateTimesheetsExistCountZeroReturnsFalse() {
		// Given
		List<Integer> dates = List.of(20230101);
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_TIMESHEETS_EXIST, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorIds", List.of(2))).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 3)).willReturn(mockQuery);
		given(mockQuery.setParameter("dates", dates)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(0L);

		// When
		Boolean result = this.repository.validateTimesheetsExist(dates, 3, 1, List.of(2));

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Fetch enabled assignment ids returns list from query")
	void testFetchEnabledAssignmentIdsReturnsIds() {
		// Given
		List<Integer> assignmentIds = Arrays.asList(100, 200);
		TypedQuery<Integer> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FETCH_ENABLED_ASSIGNMENT_IDS, Integer.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("assignmentIds", assignmentIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(100));

		// When
		List<Integer> result = this.repository.fetchEnabledAssignmentIds(assignmentIds, 1);

		// Then
		assertThat(result).containsExactly(100);
	}

	@Test
	@DisplayName("Find timesheet settings with template work day by ids returns empty when input null")
	void testFindTimesheetSettingsWithTemplateWorkDayByIdsNullReturnsEmpty() {
		// When
		List<TimesheetSettingTemplateWorkDayDto> result = this.repository
			.findTimesheetSettingsWithTemplateWorkDayByIds(null);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should(never()).createNativeQuery(anyString());
	}

	@Test
	@DisplayName("Find timesheet settings with template work day by ids returns empty when input empty")
	void testFindTimesheetSettingsWithTemplateWorkDayByIdsEmptyReturnsEmpty() {
		// When
		List<TimesheetSettingTemplateWorkDayDto> result = this.repository
			.findTimesheetSettingsWithTemplateWorkDayByIds(Collections.emptyList());

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should(never()).createNativeQuery(anyString());
	}

	@Test
	@DisplayName("Find timesheet settings with template work day maps comma-separated ids and skips null token string")
	void testFindTimesheetSettingsWithTemplateWorkDayByIdsMapsRows() {
		// Given
		Query mockQuery = mock(Query.class);
		List<Object[]> rows = new ArrayList<>();
		rows.add(new Object[] { 1, "1, 2 ,3" });
		rows.add(new Object[] { 2, null });
		rows.add(new Object[] { 3, "   " });
		given(this.entityManager.createNativeQuery(NATIVE_TEMPLATE_WORK_DAY_SQL)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetSettingIds", List.of(1, 2, 3))).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(rows);

		// When
		List<TimesheetSettingTemplateWorkDayDto> result = this.repository
			.findTimesheetSettingsWithTemplateWorkDayByIds(List.of(1, 2, 3));

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getTimesheetSettingId()).isEqualTo(1);
		assertThat(result.get(0).getWorkDayIds()).containsExactly(1, 2, 3);
		assertThat(result.get(1).getWorkDayIds()).isEmpty();
		assertThat(result.get(2).getWorkDayIds()).isEmpty();
	}

	@Test
	@DisplayName("Save user preference persists entity built from parameters")
	void testSaveUserPreferenceBuildsEntityAndPersists() {
		// Given
		String json = "{\"k\":1}";
		Integer addedBy = 10;
		Integer addedByUserTypeId = 2;
		Integer accountId = 99;
		ArgumentCaptor<TimesheetSettingUserPreference> captor = ArgumentCaptor
			.forClass(TimesheetSettingUserPreference.class);
		willDoNothing().given(this.entityManager).persist(captor.capture());

		// When
		this.repository.saveUserPreference(json, addedBy, addedByUserTypeId, accountId);

		// Then
		TimesheetSettingUserPreference saved = captor.getValue();
		assertThat(saved.getTimesheetSettingJson()).isEqualTo(json);
		assertThat(saved.getAddedBy()).isEqualTo(addedBy);
		assertThat(saved.getAddedByUserTypeId()).isEqualTo(addedByUserTypeId);
		assertThat(saved.getAccountId()).isEqualTo(accountId);
		then(this.entityManager).should().persist(any(TimesheetSettingUserPreference.class));
	}

	@Test
	@DisplayName("Save user preference allows null JSON string")
	void testSaveUserPreferenceNullJson() {
		// Given
		willDoNothing().given(this.entityManager).persist(any(TimesheetSettingUserPreference.class));

		// When
		this.repository.saveUserPreference(null, 1, 2, 3);

		// Then
		then(this.entityManager).should().persist(any(TimesheetSettingUserPreference.class));
	}

	@Test
	@DisplayName("Find user preference returns optional when present")
	void testFindUserPreferenceByAccountIdAndUserIdReturnsOptional() {
		// Given
		TimesheetSettingUserPreference pref = TimesheetSettingTestDataFactory.createTimesheetSettingUserPreference();
		TypedQuery<TimesheetSettingUserPreference> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_USER_PREFERENCE, TimesheetSettingUserPreference.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", 2)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(pref));

		// When
		Optional<TimesheetSettingUserPreference> result = this.repository.findUserPreferenceByAccountIdAndUserId(1, 2);

		// Then
		assertThat(result).contains(pref);
	}

	@Test
	@DisplayName("Find user preference returns empty when list empty")
	void testFindUserPreferenceByAccountIdAndUserIdEmpty() {
		// Given
		TypedQuery<TimesheetSettingUserPreference> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_USER_PREFERENCE, TimesheetSettingUserPreference.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", 2)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Optional<TimesheetSettingUserPreference> result = this.repository.findUserPreferenceByAccountIdAndUserId(1, 2);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find latest settings for contractor-job pairs returns empty when input null")
	void testFindLatestTimesheetSettingsForContractorJobPairsNullReturnsEmpty() {
		// When
		List<TimesheetSetting> result = this.repository.findLatestTimesheetSettingsForContractorJobPairs(null);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should(never()).createQuery(anyString(), eq(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Find latest settings for contractor-job pairs returns empty when input empty")
	void testFindLatestTimesheetSettingsForContractorJobPairsEmptyReturnsEmpty() {
		// When
		List<TimesheetSetting> result = this.repository
			.findLatestTimesheetSettingsForContractorJobPairs(Collections.emptyList());

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should(never()).createQuery(anyString(), eq(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Find latest settings for contractor-job pairs binds indexed parameters for one pair")
	void testFindLatestTimesheetSettingsForContractorJobPairsOnePair() {
		// Given
		List<ContractorJobPairDto> pairs = TimesheetSettingTestDataFactory.createSingleContractorJobPairDto();
		TimesheetSetting setting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_LATEST_FOR_PAIRS_ONE, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId0", TimesheetSettingTestDataFactory.getDefaultJobId()))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId0", TimesheetSettingTestDataFactory.getDefaultContractorId()))
			.willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(setting));

		// When
		List<TimesheetSetting> result = this.repository.findLatestTimesheetSettingsForContractorJobPairs(pairs);

		// Then
		assertThat(result).containsExactly(setting);
		then(mockQuery).should().setParameter("jobId0", TimesheetSettingTestDataFactory.getDefaultJobId());
		then(mockQuery).should()
			.setParameter("contractorId0", TimesheetSettingTestDataFactory.getDefaultContractorId());
	}

	@Test
	@DisplayName("Find latest settings for contractor-job pairs builds OR clause for multiple pairs")
	void testFindLatestTimesheetSettingsForContractorJobPairsTwoPairs() {
		// Given
		List<ContractorJobPairDto> pairs = TimesheetSettingTestDataFactory.createContractorJobPairDtos();
		TypedQuery<TimesheetSetting> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_LATEST_FOR_PAIRS_TWO, TimesheetSetting.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("jobId0", pairs.get(0).getJobId())).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId0", pairs.get(0).getContractorId())).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId1", pairs.get(1).getJobId())).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId1", pairs.get(1).getContractorId())).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		List<TimesheetSetting> result = this.repository.findLatestTimesheetSettingsForContractorJobPairs(pairs);

		// Then
		assertThat(result).isEmpty();
		then(mockQuery).should().setParameter("jobId1", pairs.get(1).getJobId());
		then(mockQuery).should().setParameter("contractorId1", pairs.get(1).getContractorId());
	}

	@Test
	@DisplayName("Update isRemarkMandatory skips persistence when association ids null")
	void testUpdateIsRemarkMandatoryByAssociationIdsNullNoOp() {
		// When
		this.repository.updateIsRemarkMandatoryByAssociationIds(null, 1);

		// Then
		then(this.entityManager).should(never()).createQuery(anyString());
		then(this.entityManager).should(never()).flush();
	}

	@Test
	@DisplayName("Update isRemarkMandatory skips persistence when association ids empty")
	void testUpdateIsRemarkMandatoryByAssociationIdsEmptyNoOp() {
		// When
		this.repository.updateIsRemarkMandatoryByAssociationIds(Collections.emptyList(), 0);

		// Then
		then(this.entityManager).should(never()).createQuery(anyString());
	}

	@Test
	@DisplayName("Update isRemarkMandatory executes bulk update flush and clear")
	void testUpdateIsRemarkMandatoryByAssociationIdsExecutesUpdate() {
		// Given
		List<Integer> ids = List.of(1, 2);
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createQuery(JPQL_UPDATE_REMARK_MANDATORY)).willReturn(mockQuery);
		given(mockQuery.setParameter("isRemarkMandatory", 1)).willReturn(mockQuery);
		given(mockQuery.setParameter("associationIds", ids)).willReturn(mockQuery);
		given(mockQuery.executeUpdate()).willReturn(2);

		// When
		this.repository.updateIsRemarkMandatoryByAssociationIds(ids, 1);

		// Then
		then(mockQuery).should().executeUpdate();
		then(this.entityManager).should().flush();
		then(this.entityManager).should().clear();
	}

	@Test
	@DisplayName("Update isUnplannedHoursPayEnabled executes when ids non-empty")
	void testUpdateIsUnplannedHoursPayEnabledByAssociationIdsExecutes() {
		// Given
		List<Integer> ids = List.of(5);
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createQuery(JPQL_UPDATE_UNPLANNED_PAY)).willReturn(mockQuery);
		given(mockQuery.setParameter("isUnplannedHoursPayEnabled", 0)).willReturn(mockQuery);
		given(mockQuery.setParameter("associationIds", ids)).willReturn(mockQuery);
		given(mockQuery.executeUpdate()).willReturn(1);

		// When
		this.repository.updateIsUnplannedHoursPayEnabledByAssociationIds(ids, 0);

		// Then
		then(mockQuery).should().executeUpdate();
		then(this.entityManager).should().flush();
		then(this.entityManager).should().clear();
	}

	@Test
	@DisplayName("Update isUnplannedHoursPayEnabled skips when ids null or empty")
	void testUpdateIsUnplannedHoursPayEnabledByAssociationIdsSkipsWhenNullOrEmpty() {
		// When
		this.repository.updateIsUnplannedHoursPayEnabledByAssociationIds(null, 1);
		this.repository.updateIsUnplannedHoursPayEnabledByAssociationIds(Collections.emptyList(), 1);

		// Then
		then(this.entityManager).should(never()).createQuery(anyString());
	}

	@Test
	@DisplayName("Find work log type by id returns empty map when ids null or empty")
	void testFindWorkLogTypeByIdInNullOrEmptyReturnsEmptyMap() {
		// When
		Map<Integer, Integer> nullResult = this.repository.findWorkLogTypeByIdIn(null);
		Map<Integer, Integer> emptyResult = this.repository.findWorkLogTypeByIdIn(Collections.emptyList());

		// Then
		assertThat(nullResult).isEmpty();
		assertThat(emptyResult).isEmpty();
		then(this.entityManager).should(never()).createQuery(JPQL_FIND_WORK_LOG_TYPE_BY_IDS, Object[].class);
	}

	@Test
	@DisplayName("Find work log type by id maps rows and skips incomplete pairs")
	void testFindWorkLogTypeByIdInMapsAndSkipsNullPairs() {
		// Given
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_WORK_LOG_TYPE_BY_IDS, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", List.of(1, 2, 3))).willReturn(mockQuery);
		given(mockQuery.getResultList())
			.willReturn(Arrays.asList(new Object[] { 1, 10 }, new Object[] { null, 5 }, new Object[] { 3, null }));

		// When
		Map<Integer, Integer> result = this.repository.findWorkLogTypeByIdIn(List.of(1, 2, 3));

		// Then
		assertThat(result).containsOnlyKeys(1).containsEntry(1, 10);
	}

	@Test
	@DisplayName("Find break info by id returns empty map when ids null or empty")
	void testFindBreakInfoByIdInNullOrEmptyReturnsEmptyMap() {
		// When
		assertThat(this.repository.findBreakInfoByIdIn(null)).isEmpty();
		assertThat(this.repository.findBreakInfoByIdIn(List.of())).isEmpty();
	}

	@Test
	@DisplayName("Find break info by id maps DTO per id and skips null id rows")
	void testFindBreakInfoByIdInMapsRows() {
		// Given
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_FIND_BREAK_INFO_BY_IDS, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", List.of(7))).willReturn(mockQuery);
		given(mockQuery.getResultList())
			.willReturn(Arrays.asList(new Object[] { 7, true, 30 }, new Object[] { null, false, 1 }));

		// When
		Map<Integer, TimesheetSettingBreakInfoDto> result = this.repository.findBreakInfoByIdIn(List.of(7));

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(7).calculateBreakTime()).isTrue();
		assertThat(result.get(7).breakTimeThreshold()).isEqualTo(30);
	}

}
