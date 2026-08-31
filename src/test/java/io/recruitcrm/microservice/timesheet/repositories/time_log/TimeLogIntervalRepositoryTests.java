package io.recruitcrm.microservice.timesheet.repositories.time_log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogIntervalT;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogIntervalJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.DeleteUsingStep;
import org.jooq.JSON;
import org.jooq.Record6;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test cases for TimeLogIntervalRepository with comprehensive coverage. Tests all
 * repository methods following BDD-style testing patterns and strict checkstyle
 * compliance.
 */
@ExtendWith(MockitoExtension.class)
class TimeLogIntervalRepositoryTests {

	private static final String PARAM_TIME_LOG_IDS = "timeLogIds";

	private TimeLogIntervalRepository repository;

	@Mock
	private TimeLogIntervalJpaRepository timeLogIntervalJpaRepository;

	@Mock
	private DSLContext auroraDbDSLContext;

	@Mock
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		this.timeLogIntervalJpaRepository = mock(TimeLogIntervalJpaRepository.class);
		this.auroraDbDSLContext = mock(DSLContext.class);
		this.entityManager = mock(EntityManager.class);
		this.repository = new TimeLogIntervalRepository(this.timeLogIntervalJpaRepository, this.auroraDbDSLContext);
		ReflectionTestUtils.setField(this.repository, "entityManager", this.entityManager);
	}

	// ===== Test helper methods (must be before inner types) =====

	private List<TimeLogInterval> createTimeLogIntervalList() {
		TimeLogInterval interval = new TimeLogInterval();
		interval.setTimeLogId(1);
		interval.setWorkStartTime(540);
		interval.setWorkEndTime(1020);
		interval.setRangeBasedRemark("Test remark");
		return Arrays.asList(interval);
	}

	// ===== Tests for deleteByTimeLogIntervalIdIn (JOOQ batch delete) =====

	@Nested
	@DisplayName("deleteByTimeLogIntervalIdIn method tests")
	class DeleteByTimeLogIntervalIdInTests {

		@Test
		@DisplayName("Should execute JOOQ delete when valid time log IDs provided")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testDeleteByTimeLogIntervalIdInValidIdsExecutesJooqDelete() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
			DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
			given(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext
				.deleteFrom(CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T)).willReturn(mockDeleteUsingStep);
			given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
			given(mockDeleteConditionStep.execute()).willReturn(3);

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIntervalIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).should()
				.deleteFrom(CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T);
			then(mockDeleteUsingStep).should().where(any(org.jooq.Condition.class));
			then(mockDeleteConditionStep).should().execute();
		}

		@Test
		@DisplayName("Should return early when time log IDs list is null")
		void testDeleteByTimeLogIntervalIdInNullIdsReturnsEarly() {
			// Given
			List<Integer> timeLogIds = null;

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIntervalIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("Should return early when time log IDs list is empty")
		void testDeleteByTimeLogIntervalIdInEmptyIdsReturnsEarly() {
			// Given
			List<Integer> timeLogIds = Collections.emptyList();

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIntervalIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("Should propagate exception when JOOQ delete fails")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testDeleteByTimeLogIntervalIdInJooqDeleteFailsPropagatesException() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
			DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
			given(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext
				.deleteFrom(CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T)).willReturn(mockDeleteUsingStep);
			given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
			willThrow(new RuntimeException("Database connection failed")).given(mockDeleteConditionStep).execute();

			// When & Then
			assertThatThrownBy(
					() -> TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIntervalIdIn(timeLogIds))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Database connection failed");

			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).should()
				.deleteFrom(CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T);
		}

		@Test
		@DisplayName("Should handle single time log ID correctly")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testDeleteByTimeLogIntervalIdInSingleIdExecutesJooqDelete() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1);
			DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
			DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
			given(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext
				.deleteFrom(CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T)).willReturn(mockDeleteUsingStep);
			given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
			given(mockDeleteConditionStep.execute()).willReturn(1);

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIntervalIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).should()
				.deleteFrom(CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T);
			then(mockDeleteConditionStep).should().execute();
		}

	}

	// ===== Tests for saveTimeLogIntervals =====

	@Nested
	@DisplayName("saveTimeLogIntervals method tests")
	class SaveTimeLogIntervalsTests {

		@Test
		@DisplayName("Should delegate to JPA repository when saving intervals")
		void testSaveTimeLogIntervalsValidListDelegatesToJpa() {
			// Given
			List<TimeLogInterval> intervals = TimeLogIntervalRepositoryTests.this.createTimeLogIntervalList();
			TimeLogInterval interval1 = new TimeLogInterval();
			interval1.setId(1);
			interval1.setTimeLogId(1);
			interval1.setWorkStartTime(540);
			interval1.setWorkEndTime(1020);
			interval1.setRangeBasedRemark("Test remark 1");

			TimeLogInterval interval2 = new TimeLogInterval();
			interval2.setId(2);
			interval2.setTimeLogId(2);
			interval2.setWorkStartTime(600);
			interval2.setWorkEndTime(1080);
			interval2.setRangeBasedRemark("Test remark 2");
			List<TimeLogInterval> savedIntervals = Arrays.asList(interval1, interval2);
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.saveAll(intervals))
				.willReturn(savedIntervals);

			// When
			List<TimeLogInterval> result = TimeLogIntervalRepositoryTests.this.repository
				.saveTimeLogIntervals(intervals);

			// Then
			assertThat(result).isEqualTo(savedIntervals);
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should().saveAll(intervals);
		}

		@Test
		@DisplayName("Should handle empty list correctly")
		void testSaveTimeLogIntervalsEmptyListReturnsEmpty() {
			// Given
			List<TimeLogInterval> emptyList = Collections.emptyList();
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.saveAll(emptyList))
				.willReturn(Collections.emptyList());

			// When
			List<TimeLogInterval> result = TimeLogIntervalRepositoryTests.this.repository
				.saveTimeLogIntervals(emptyList);

			// Then
			assertThat(result).isEmpty();
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should().saveAll(emptyList);
		}

		@Test
		@DisplayName("Should propagate DataAccessException from JPA repository")
		void testSaveTimeLogIntervalsDataAccessExceptionPropagated() {
			// Given
			List<TimeLogInterval> intervals = TimeLogIntervalRepositoryTests.this.createTimeLogIntervalList();
			willThrow(new DataIntegrityViolationException("Constraint violation"))
				.given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository)
				.saveAll(intervals);

			// When & Then
			assertThatThrownBy(() -> TimeLogIntervalRepositoryTests.this.repository.saveTimeLogIntervals(intervals))
				.isInstanceOf(DataAccessException.class)
				.hasMessageContaining("Constraint violation");
		}

	}

	// ===== Tests for findByTimeLogIdIn =====

	@Nested
	@DisplayName("findByTimeLogIdIn method tests")
	class FindByTimeLogIdInTests {

		@Test
		@DisplayName("Should delegate to JPA repository with valid IDs")
		void testFindByTimeLogIdInValidIdsDelegatesToJpa() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			List<TimeLogInterval> expectedIntervals = TimeLogIntervalRepositoryTests.this.createTimeLogIntervalList();
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.findByTimeLogIdIn(timeLogIds))
				.willReturn(expectedIntervals);

			// When
			List<TimeLogInterval> result = TimeLogIntervalRepositoryTests.this.repository.findByTimeLogIdIn(timeLogIds);

			// Then
			assertThat(result).isEqualTo(expectedIntervals);
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should()
				.findByTimeLogIdIn(timeLogIds);
		}

		@Test
		@DisplayName("Should return empty list when no intervals found")
		void testFindByTimeLogIdInNoIntervalsFoundReturnsEmpty() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(999, 1000);
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.findByTimeLogIdIn(timeLogIds))
				.willReturn(Collections.emptyList());

			// When
			List<TimeLogInterval> result = TimeLogIntervalRepositoryTests.this.repository.findByTimeLogIdIn(timeLogIds);

			// Then
			assertThat(result).isEmpty();
		}

	}

	// ===== Tests for findByTimeLogId =====

	@Nested
	@DisplayName("findByTimeLogId method tests")
	class FindByTimeLogIdTests {

		@Test
		@DisplayName("Should delegate to JPA repository with valid ID")
		void testFindByTimeLogIdValidIdDelegatesToJpa() {
			// Given
			Integer timeLogId = 1;
			List<TimeLogInterval> expectedIntervals = TimeLogIntervalRepositoryTests.this.createTimeLogIntervalList();
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.findByTimeLogId(timeLogId))
				.willReturn(expectedIntervals);

			// When
			List<TimeLogInterval> result = TimeLogIntervalRepositoryTests.this.repository.findByTimeLogId(timeLogId);

			// Then
			assertThat(result).isEqualTo(expectedIntervals);
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should().findByTimeLogId(timeLogId);
		}

	}

	// ===== Tests for findIntervalIdsByTimeLogIdIn =====

	@Nested
	@DisplayName("findIntervalIdsByTimeLogIdIn method tests")
	class FindIntervalIdsByTimeLogIdInTests {

		@Test
		@DisplayName("Should execute native SQL query with valid IDs")
		void testFindIntervalIdsByTimeLogIdInValidIdsExecutesNativeSql() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(PARAM_TIME_LOG_IDS, timeLogIds)).willReturn(mockQuery);
			given(mockQuery.getResultList()).willReturn(Arrays.asList(10, 20, 30));

			// When
			List<Integer> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalIdsByTimeLogIdIn(timeLogIds);

			// Then
			assertThat(result).containsExactly(10, 20, 30);
			then(TimeLogIntervalRepositoryTests.this.entityManager).should().createNativeQuery(anyString());
			then(mockQuery).should().setParameter(PARAM_TIME_LOG_IDS, timeLogIds);
		}

		@Test
		@DisplayName("Should return empty list when time log IDs is null")
		void testFindIntervalIdsByTimeLogIdInNullIdsReturnsEmpty() {
			// Given
			List<Integer> timeLogIds = null;

			// When
			List<Integer> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalIdsByTimeLogIdIn(timeLogIds);

			// Then
			assertThat(result).isEmpty();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return empty list when time log IDs is empty")
		void testFindIntervalIdsByTimeLogIdInEmptyIdsReturnsEmpty() {
			// Given
			List<Integer> timeLogIds = Collections.emptyList();

			// When
			List<Integer> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalIdsByTimeLogIdIn(timeLogIds);

			// Then
			assertThat(result).isEmpty();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

	}

	// ===== Tests for deleteAll =====

	@Nested
	@DisplayName("deleteAll method tests")
	class DeleteAllTests {

		@Test
		@DisplayName("Should delegate to JPA repository when deleting intervals")
		void testDeleteAllValidIntervalsDelegatesToJpa() {
			// Given
			List<TimeLogInterval> intervals = TimeLogIntervalRepositoryTests.this.createTimeLogIntervalList();
			willDoNothing().given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository)
				.deleteAll(intervals);

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteAll(intervals);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should().deleteAll(intervals);
		}

		@Test
		@DisplayName("Should return early when intervals list is null")
		void testDeleteAllNullIntervalsReturnsEarly() {
			// Given
			List<TimeLogInterval> intervals = null;

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteAll(intervals);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should(never())
				.deleteAll(any(List.class));
		}

		@Test
		@DisplayName("Should return early when intervals list is empty")
		void testDeleteAllEmptyIntervalsReturnsEarly() {
			// Given
			List<TimeLogInterval> intervals = Collections.emptyList();

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteAll(intervals);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should(never())
				.deleteAll(any(List.class));
		}

	}

	// ===== Tests for deleteByTimeLogIdIn (JPA-based) =====

	@Nested
	@DisplayName("deleteByTimeLogIdIn method tests")
	class DeleteByTimeLogIdInTests {

		@Test
		@DisplayName("Should fetch and delete intervals when valid IDs provided")
		void testDeleteByTimeLogIdInValidIdsFetchesAndDeletes() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			List<TimeLogInterval> intervalsToDelete = TimeLogIntervalRepositoryTests.this.createTimeLogIntervalList();
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.findByTimeLogIdIn(timeLogIds))
				.willReturn(intervalsToDelete);
			willDoNothing().given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository)
				.deleteAll(intervalsToDelete);

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should()
				.findByTimeLogIdIn(timeLogIds);
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should()
				.deleteAll(intervalsToDelete);
		}

		@Test
		@DisplayName("Should return early when time log IDs is null")
		void testDeleteByTimeLogIdInNullIdsReturnsEarly() {
			// Given
			List<Integer> timeLogIds = null;

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should(never())
				.findByTimeLogIdIn(any());
		}

		@Test
		@DisplayName("Should return early when time log IDs is empty")
		void testDeleteByTimeLogIdInEmptyIdsReturnsEarly() {
			// Given
			List<Integer> timeLogIds = Collections.emptyList();

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should(never())
				.findByTimeLogIdIn(any());
		}

		@Test
		@DisplayName("Should not delete when no intervals found")
		void testDeleteByTimeLogIdInNoIntervalsFoundSkipsDelete() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			given(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository.findByTimeLogIdIn(timeLogIds))
				.willReturn(Collections.emptyList());

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByTimeLogIdIn(timeLogIds);

			// Then
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should()
				.findByTimeLogIdIn(timeLogIds);
			then(TimeLogIntervalRepositoryTests.this.timeLogIntervalJpaRepository).should(never())
				.deleteAll(any(List.class));
		}

	}

	// ===== Tests for deleteByIdIn (Native SQL batch delete) =====

	@Nested
	@DisplayName("deleteByIdIn method tests")
	class DeleteByIdInTests {

		@Test
		@DisplayName("Should execute native SQL delete when valid IDs provided")
		void testDeleteByIdInValidIdsExecutesNativeSqlDelete() {
			// Given
			List<Integer> ids = Arrays.asList(1, 2, 3);
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(eq("ids"), any(List.class))).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(3);

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByIdIn(ids);

			// Then
			then(TimeLogIntervalRepositoryTests.this.entityManager).should().createNativeQuery(anyString());
			then(mockQuery).should().executeUpdate();
		}

		@Test
		@DisplayName("Should return early when IDs list is null")
		void testDeleteByIdInNullIdsReturnsEarly() {
			// Given
			List<Integer> ids = null;

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByIdIn(ids);

			// Then
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return early when IDs list is empty")
		void testDeleteByIdInEmptyIdsReturnsEarly() {
			// Given
			List<Integer> ids = Collections.emptyList();

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByIdIn(ids);

			// Then
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

	}

	// ===== Tests for batchInsert =====

	@Nested
	@DisplayName("batchInsert method tests")
	class BatchInsertTests {

		@Test
		@DisplayName("Should execute native SQL insert when valid values provided")
		void testBatchInsertValidValuesExecutesNativeSqlInsert() {
			// Given
			List<Object[]> values = Arrays.asList(new Object[] { 1, 100, 200, "remark1", "[]" },
					new Object[] { 2, 300, 400, "remark2", "[]" });
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(any(Integer.class), any())).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(2);

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchInsert(values);

			// Then
			assertThat(result).isEqualTo(2);
			then(TimeLogIntervalRepositoryTests.this.entityManager).should().createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return 0 when values list is null")
		void testBatchInsertNullValuesReturnsZero() {
			// Given
			List<Object[]> values = null;

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchInsert(values);

			// Then
			assertThat(result).isZero();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return 0 when values list is empty")
		void testBatchInsertEmptyValuesReturnsZero() {
			// Given
			List<Object[]> values = Collections.emptyList();

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchInsert(values);

			// Then
			assertThat(result).isZero();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

	}

	// ===== Tests for batchUpsert =====

	@Nested
	@DisplayName("batchUpsert method tests")
	class BatchUpsertTests {

		@Test
		@DisplayName("Should execute native SQL upsert when valid values provided")
		void testBatchUpsertValidValuesExecutesNativeSqlUpsert() {
			// Given
			List<TimeLogIntervalUpsertDto> values = Arrays.asList(
					new TimeLogIntervalUpsertDto(1, 1, 100, 200, "remark1", "[]"),
					new TimeLogIntervalUpsertDto(null, 2, 300, 400, "remark2", "[]"));
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(any(Integer.class), any())).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(2);

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchUpsert(values);

			// Then
			assertThat(result).isEqualTo(2);
			then(TimeLogIntervalRepositoryTests.this.entityManager).should().createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return 0 when values list is null")
		void testBatchUpsertNullValuesReturnsZero() {
			// Given
			List<TimeLogIntervalUpsertDto> values = null;

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchUpsert(values);

			// Then
			assertThat(result).isZero();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return 0 when values list is empty")
		void testBatchUpsertEmptyValuesReturnsZero() {
			// Given
			List<TimeLogIntervalUpsertDto> values = Collections.emptyList();

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchUpsert(values);

			// Then
			assertThat(result).isZero();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

	}

	// ===== Tests for findTimeLogIdsWithExistingIntervals =====

	@Nested
	@DisplayName("findTimeLogIdsWithExistingIntervals method tests")
	class FindTimeLogIdsWithExistingIntervalsTests {

		@Test
		@DisplayName("Should return empty list when time log IDs is null")
		void testFindTimeLogIdsWithExistingIntervalsNullIdsReturnsEmpty() {
			// Given
			List<Integer> timeLogIds = null;

			// When
			List<Integer> result = TimeLogIntervalRepositoryTests.this.repository
				.findTimeLogIdsWithExistingIntervals(timeLogIds);

			// Then
			assertThat(result).isEmpty();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should return empty list when time log IDs is empty")
		void testFindTimeLogIdsWithExistingIntervalsEmptyIdsReturnsEmpty() {
			// Given
			List<Integer> timeLogIds = Collections.emptyList();

			// When
			List<Integer> result = TimeLogIntervalRepositoryTests.this.repository
				.findTimeLogIdsWithExistingIntervals(timeLogIds);

			// Then
			assertThat(result).isEmpty();
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(never()).createNativeQuery(anyString());
		}

		@Test
		@DisplayName("Should execute native SQL and return distinct time log IDs that have intervals")
		void testFindTimeLogIdsWithExistingIntervalsValidIdsExecutesNativeSql() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(PARAM_TIME_LOG_IDS, timeLogIds)).willReturn(mockQuery);
			given(mockQuery.getResultList()).willReturn(Arrays.asList(1, 3));

			// When
			List<Integer> result = TimeLogIntervalRepositoryTests.this.repository
				.findTimeLogIdsWithExistingIntervals(timeLogIds);

			// Then
			assertThat(result).containsExactly(1, 3);
			then(TimeLogIntervalRepositoryTests.this.entityManager).should().createNativeQuery(anyString());
			then(mockQuery).should().setParameter(PARAM_TIME_LOG_IDS, timeLogIds);
		}

	}

	// ===== Tests for findIntervalsByTimeLogIds =====

	@Nested
	@DisplayName("findIntervalsByTimeLogIds method tests")
	class FindIntervalsByTimeLogIdsTests {

		@Test
		@DisplayName("Should return empty map when time log IDs is null")
		void testFindIntervalsByTimeLogIdsNullIdsReturnsEmptyMap() {
			// Given
			List<Integer> timeLogIds = null;

			// When
			java.util.Map<Integer, List<io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto>> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalsByTimeLogIds(timeLogIds);

			// Then
			assertThat(result).isNotNull().isEmpty();
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("Should return empty map when time log IDs is empty")
		void testFindIntervalsByTimeLogIdsEmptyIdsReturnsEmptyMap() {
			// Given
			List<Integer> timeLogIds = Collections.emptyList();

			// When
			java.util.Map<Integer, List<io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto>> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalsByTimeLogIds(timeLogIds);

			// Then
			assertThat(result).isNotNull().isEmpty();
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("Should fetch intervals via JOOQ and group by time log ID with break_interval null")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testFindIntervalsByTimeLogIdsValidIdsReturnsGroupedMapBreakIntervalNull() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(10);
			CstTimeLogIntervalT table = CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T;
			Record6<Integer, Integer, Integer, Integer, String, JSON> mockRecord = mock(Record6.class);
			org.jooq.Result<Record6<Integer, Integer, Integer, Integer, String, JSON>> mockResult = mock(
					org.jooq.Result.class);

			given(mockRecord.get(table.ID)).willReturn(100);
			given(mockRecord.get(table.TIME_LOG_ID)).willReturn(10);
			given(mockRecord.get(table.WORK_START_TIME)).willReturn(32400);
			given(mockRecord.get(table.WORK_END_TIME)).willReturn(61200);
			given(mockRecord.get(table.RANGE_BASED_REMARK)).willReturn("Remark");
			given(mockRecord.get(table.BREAK_INTERVAL)).willReturn(null);

			given(mockResult.iterator()).willReturn(java.util.Collections.singletonList(mockRecord).iterator());

			org.jooq.SelectSelectStep selectStep = mock(org.jooq.SelectSelectStep.class);
			org.jooq.SelectJoinStep joinStep = mock(org.jooq.SelectJoinStep.class);
			org.jooq.SelectConditionStep conditionStep = mock(org.jooq.SelectConditionStep.class);
			org.jooq.SelectSeekStep2 seekStep = mock(org.jooq.SelectSeekStep2.class);

			given(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext.select(table.ID, table.TIME_LOG_ID,
					table.WORK_START_TIME, table.WORK_END_TIME, table.RANGE_BASED_REMARK, table.BREAK_INTERVAL))
				.willReturn(selectStep);
			given(selectStep.from(table)).willReturn(joinStep);
			given(joinStep.where(any(org.jooq.Condition.class))).willReturn(conditionStep);
			given(conditionStep.orderBy(any(), any())).willReturn(seekStep);
			given(seekStep.fetch()).willReturn(mockResult);

			// When
			java.util.Map<Integer, List<io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto>> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalsByTimeLogIds(timeLogIds);

			// Then
			assertThat(result).isNotNull().hasSize(1).containsKey(10);
			assertThat(result.get(10)).hasSize(1);
			assertThat(result.get(10).get(0).getId()).isEqualTo(100);
			assertThat(result.get(10).get(0).getTimeLogId()).isEqualTo(10);
			assertThat(result.get(10).get(0).getWorkStartTime()).isEqualTo(32400);
			assertThat(result.get(10).get(0).getWorkEndTime()).isEqualTo(61200);
			assertThat(result.get(10).get(0).getRangeBasedRemark()).isEqualTo("Remark");
			assertThat(result.get(10).get(0).getBreakInterval()).isNull();
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).should()
				.select(table.ID, table.TIME_LOG_ID, table.WORK_START_TIME, table.WORK_END_TIME,
						table.RANGE_BASED_REMARK, table.BREAK_INTERVAL);
		}

		@Test
		@DisplayName("Should map break_interval to string when non-null")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testFindIntervalsByTimeLogIdsBreakIntervalNonNullMappedToString() {
			// Given
			List<Integer> timeLogIds = Arrays.asList(20);
			CstTimeLogIntervalT table = CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T;
			Record6<Integer, Integer, Integer, Integer, String, JSON> mockRecord = mock(Record6.class);
			org.jooq.Result<Record6<Integer, Integer, Integer, Integer, String, JSON>> mockResult = mock(
					org.jooq.Result.class);
			JSON breakIntervalJsonMock = mock(JSON.class);
			given(breakIntervalJsonMock.toString()).willReturn("{\"start\":540,\"end\":600}");

			given(mockRecord.get(table.ID)).willReturn(200);
			given(mockRecord.get(table.TIME_LOG_ID)).willReturn(20);
			given(mockRecord.get(table.WORK_START_TIME)).willReturn(540);
			given(mockRecord.get(table.WORK_END_TIME)).willReturn(600);
			given(mockRecord.get(table.RANGE_BASED_REMARK)).willReturn(null);
			given(mockRecord.get(table.BREAK_INTERVAL)).willReturn(breakIntervalJsonMock);

			given(mockResult.iterator()).willReturn(java.util.Collections.singletonList(mockRecord).iterator());

			org.jooq.SelectSelectStep selectStep = mock(org.jooq.SelectSelectStep.class);
			org.jooq.SelectJoinStep joinStep = mock(org.jooq.SelectJoinStep.class);
			org.jooq.SelectConditionStep conditionStep = mock(org.jooq.SelectConditionStep.class);
			org.jooq.SelectSeekStep2 seekStep = mock(org.jooq.SelectSeekStep2.class);

			given(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext.select(table.ID, table.TIME_LOG_ID,
					table.WORK_START_TIME, table.WORK_END_TIME, table.RANGE_BASED_REMARK, table.BREAK_INTERVAL))
				.willReturn(selectStep);
			given(selectStep.from(table)).willReturn(joinStep);
			given(joinStep.where(any(org.jooq.Condition.class))).willReturn(conditionStep);
			given(conditionStep.orderBy(any(), any())).willReturn(seekStep);
			given(seekStep.fetch()).willReturn(mockResult);

			// When
			java.util.Map<Integer, List<io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto>> result = TimeLogIntervalRepositoryTests.this.repository
				.findIntervalsByTimeLogIds(timeLogIds);

			// Then
			assertThat(result).isNotNull().hasSize(1).containsKey(20);
			assertThat(result.get(20).get(0).getBreakInterval()).isEqualTo("{\"start\":540,\"end\":600}");
			then(TimeLogIntervalRepositoryTests.this.auroraDbDSLContext).should()
				.select(table.ID, table.TIME_LOG_ID, table.WORK_START_TIME, table.WORK_END_TIME,
						table.RANGE_BASED_REMARK, table.BREAK_INTERVAL);
		}

	}

	// ===== Additional batch coverage for deleteByIdIn (multiple batches) =====

	@Nested
	@DisplayName("deleteByIdIn batch loop tests")
	class DeleteByIdInBatchLoopTests {

		@Test
		@DisplayName("Should process multiple batches when IDs exceed DELETE_BATCH_SIZE")
		void testDeleteByIdInMultipleBatchesExecutesMultipleQueries() {
			// Given - 2500 IDs = 2 batches (2000 + 500)
			List<Integer> ids = new java.util.ArrayList<>();
			for (int i = 1; i <= 2500; i++) {
				ids.add(i);
			}
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(eq("ids"), any(List.class))).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(2000).willReturn(500);

			// When
			TimeLogIntervalRepositoryTests.this.repository.deleteByIdIn(ids);

			// Then - 2 batches
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(times(2)).createNativeQuery(anyString());
			then(mockQuery).should(times(2)).executeUpdate();
		}

	}

	// ===== Additional batch coverage for batchInsert (multiple batches) =====

	@Nested
	@DisplayName("batchInsert batch loop tests")
	class BatchInsertBatchLoopTests {

		@Test
		@DisplayName("Should process multiple batches when values exceed BATCH_SIZE")
		void testBatchInsertMultipleBatchesReturnsTotalInserted() {
			// Given - 2500 rows = 2 batches (2000 + 500)
			List<Object[]> values = new java.util.ArrayList<>();
			for (int i = 0; i < 2500; i++) {
				values.add(new Object[] { i, 100, 200, "r", "[]" });
			}
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(any(Integer.class), any())).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(2000).willReturn(500);

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchInsert(values);

			// Then
			assertThat(result).isEqualTo(2500);
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(times(2)).createNativeQuery(anyString());
		}

	}

	// ===== Additional batch coverage for batchUpsert (multiple batches) =====

	@Nested
	@DisplayName("batchUpsert batch loop tests")
	class BatchUpsertBatchLoopTests {

		@Test
		@DisplayName("Should process multiple batches when values exceed BATCH_SIZE")
		void testBatchUpsertMultipleBatchesReturnsTotalAffected() {
			// Given - 2500 dtos = 2 batches (2000 + 500)
			List<TimeLogIntervalUpsertDto> values = new java.util.ArrayList<>();
			for (int i = 0; i < 2500; i++) {
				values.add(new TimeLogIntervalUpsertDto(i, i, 100, 200, "r", "[]"));
			}
			Query mockQuery = mock(Query.class);
			given(TimeLogIntervalRepositoryTests.this.entityManager.createNativeQuery(anyString()))
				.willReturn(mockQuery);
			given(mockQuery.setParameter(any(Integer.class), any())).willReturn(mockQuery);
			given(mockQuery.executeUpdate()).willReturn(2000).willReturn(500);

			// When
			int result = TimeLogIntervalRepositoryTests.this.repository.batchUpsert(values);

			// Then
			assertThat(result).isEqualTo(2500);
			then(TimeLogIntervalRepositoryTests.this.entityManager).should(times(2)).createNativeQuery(anyString());
		}

	}

}
