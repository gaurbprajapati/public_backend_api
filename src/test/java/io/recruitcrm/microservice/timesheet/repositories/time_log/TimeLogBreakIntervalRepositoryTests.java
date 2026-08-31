package io.recruitcrm.microservice.timesheet.repositories.time_log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogBreakIntervalT;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogBreakIntervalJpaRepository;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetLogsTestDataFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.DeleteUsingStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Unit test cases for TimeLogBreakIntervalRepository with 100% coverage. Tests all
 * repository methods following BDD-style testing patterns and strict checkstyle
 * compliance.
 *
 * Repository Method Coverage: 1. save BreakIntervals method - Simple JPA delegation with
 * transaction 2. findBreakIntervalsByTimeLogIdIn method - Custom JPA query method 3.
 * deleteAll method - Simple JPA delegation with transaction
 *
 * Each method is tested for: - Success scenarios with valid data - Empty collection
 * scenarios - Exception handling for database errors - Parameter validation and
 * delegation verification
 */
@ExtendWith(MockitoExtension.class)
class TimeLogBreakIntervalRepositoryTests {

	@InjectMocks
	private TimeLogBreakIntervalRepository repository;

	@Mock
	private TimeLogBreakIntervalJpaRepository timeLogBreakIntervalJpaRepository;

	@Mock
	private DSLContext auroraDbDSLContext;

	@BeforeEach
	void setUp() {
		// Initialize repository with mocked dependencies
		this.repository = new TimeLogBreakIntervalRepository(this.timeLogBreakIntervalJpaRepository,
				this.auroraDbDSLContext);
	}

	/**
	 * Test saveBreakIntervals() method - Success scenarios
	 */

	@Test
	@DisplayName("Save break intervals should delegate to JPA repository with valid list")
	void testSaveBreakIntervalsValidListDelegatesToJpaRepository() {
		// Given
		List<TimeLogBreakInterval> breakIntervals = TimesheetLogsTestDataFactory.createTimeLogBreakIntervalList();
		List<TimeLogBreakInterval> savedBreakIntervals = createSavedTimeLogBreakIntervalList();

		given(this.timeLogBreakIntervalJpaRepository.saveAll(breakIntervals)).willReturn(savedBreakIntervals);

		// When
		List<TimeLogBreakInterval> result = this.repository.saveBreakIntervals(breakIntervals);

		// Then
		assertThat(result).isEqualTo(savedBreakIntervals).hasSize(2);
		then(this.timeLogBreakIntervalJpaRepository).should().saveAll(breakIntervals);
	}

	@Test
	@DisplayName("Save break intervals should handle empty list correctly")
	void testSaveBreakIntervalsEmptyListHandledCorrectly() {
		// Given
		List<TimeLogBreakInterval> emptyBreakIntervals = Collections.emptyList();
		List<TimeLogBreakInterval> emptyResult = Collections.emptyList();

		given(this.timeLogBreakIntervalJpaRepository.saveAll(emptyBreakIntervals)).willReturn(emptyResult);

		// When
		List<TimeLogBreakInterval> result = this.repository.saveBreakIntervals(emptyBreakIntervals);

		// Then
		assertThat(result).isEmpty();
		then(this.timeLogBreakIntervalJpaRepository).should().saveAll(emptyBreakIntervals);
	}

	@Test
	@DisplayName("Save break intervals should propagate DataAccessException from JPA repository")
	void testSaveBreakIntervalsDataAccessExceptionPropagatedFromJpaRepository() {
		// Given
		List<TimeLogBreakInterval> breakIntervals = TimesheetLogsTestDataFactory.createTimeLogBreakIntervalList();

		willThrow(new DataIntegrityViolationException("Database constraint violation"))
			.given(this.timeLogBreakIntervalJpaRepository)
			.saveAll(breakIntervals);

		// When & Then
		assertThatThrownBy(() -> this.repository.saveBreakIntervals(breakIntervals))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database constraint violation");

		then(this.timeLogBreakIntervalJpaRepository).should().saveAll(breakIntervals);
	}

	/**
	 * Test findBreakIntervalsByTimeLogIdIn() method - Custom JPA query scenarios
	 */

	@Test
	@DisplayName("Find break intervals by time log IDs should delegate to JPA repository")
	void testFindBreakIntervalsByTimeLogIdInValidTimeLogIdsDelegatesToJpaRepository() {
		// Given
		List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
		List<TimeLogBreakInterval> expectedBreakIntervals = TimesheetLogsTestDataFactory
			.createTimeLogBreakIntervalList();

		given(this.timeLogBreakIntervalJpaRepository.findBreakIntervalsByTimeLogIdIn(timeLogIds))
			.willReturn(expectedBreakIntervals);

		// When
		List<TimeLogBreakInterval> result = this.repository.findBreakIntervalsByTimeLogIdIn(timeLogIds);

		// Then
		assertThat(result).isEqualTo(expectedBreakIntervals).hasSize(1);
		then(this.timeLogBreakIntervalJpaRepository).should().findBreakIntervalsByTimeLogIdIn(timeLogIds);
	}

	@Test
	@DisplayName("Find break intervals by time log IDs should return empty list for empty ID list")
	void testFindBreakIntervalsByTimeLogIdInEmptyTimeLogIdsReturnsEmptyList() {
		// Given
		List<Integer> emptyTimeLogIds = Collections.emptyList();
		List<TimeLogBreakInterval> emptyResult = Collections.emptyList();

		given(this.timeLogBreakIntervalJpaRepository.findBreakIntervalsByTimeLogIdIn(emptyTimeLogIds))
			.willReturn(emptyResult);

		// When
		List<TimeLogBreakInterval> result = this.repository.findBreakIntervalsByTimeLogIdIn(emptyTimeLogIds);

		// Then
		assertThat(result).isEmpty();
		then(this.timeLogBreakIntervalJpaRepository).should().findBreakIntervalsByTimeLogIdIn(emptyTimeLogIds);
	}

	@Test
	@DisplayName("Find break intervals by time log IDs should return empty list when no intervals found")
	void testFindBreakIntervalsByTimeLogIdInNoIntervalsFoundReturnsEmptyList() {
		// Given
		List<Integer> timeLogIds = Arrays.asList(999, 1000);
		List<TimeLogBreakInterval> emptyResult = Collections.emptyList();

		given(this.timeLogBreakIntervalJpaRepository.findBreakIntervalsByTimeLogIdIn(timeLogIds))
			.willReturn(emptyResult);

		// When
		List<TimeLogBreakInterval> result = this.repository.findBreakIntervalsByTimeLogIdIn(timeLogIds);

		// Then
		assertThat(result).isEmpty();
		then(this.timeLogBreakIntervalJpaRepository).should().findBreakIntervalsByTimeLogIdIn(timeLogIds);
	}

	@Test
	@DisplayName("Find break intervals by time log IDs should propagate DataAccessException")
	void testFindBreakIntervalsByTimeLogIdInDataAccessExceptionPropagated() {
		// Given
		List<Integer> timeLogIds = Arrays.asList(1, 2, 3);

		given(this.timeLogBreakIntervalJpaRepository.findBreakIntervalsByTimeLogIdIn(timeLogIds))
			.willThrow(new DataIntegrityViolationException("Database query execution failed"));

		// When & Then
		assertThatThrownBy(() -> this.repository.findBreakIntervalsByTimeLogIdIn(timeLogIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database query execution failed");

		then(this.timeLogBreakIntervalJpaRepository).should().findBreakIntervalsByTimeLogIdIn(timeLogIds);
	}

	/**
	 * Test deleteAll() method - JOOQ batch delete scenarios
	 */

	@Test
	@DisplayName("Delete all break intervals should execute JOOQ batch delete")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteAllValidBreakIntervalsExecutesJooqBatchDelete() {
		// Given
		List<TimeLogBreakInterval> breakIntervals = TimesheetLogsTestDataFactory.createTimeLogBreakIntervalList();
		breakIntervals.get(0).setId(1);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
		given(this.auroraDbDSLContext.deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T))
			.willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		given(mockDeleteConditionStep.execute()).willReturn(1);

		// When
		this.repository.deleteAll(breakIntervals);

		// Then
		then(this.auroraDbDSLContext).should().deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T);
		then(mockDeleteUsingStep).should().where(any(org.jooq.Condition.class));
		then(mockDeleteConditionStep).should().execute();
	}

	@Test
	@DisplayName("Delete all break intervals should return early when list is null")
	void testDeleteAllNullBreakIntervalsReturnsEarly() {
		// Given
		List<TimeLogBreakInterval> breakIntervals = null;

		// When
		this.repository.deleteAll(breakIntervals);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete all break intervals should return early when list is empty")
	void testDeleteAllEmptyBreakIntervalsReturnsEarly() {
		// Given
		List<TimeLogBreakInterval> emptyBreakIntervals = Collections.emptyList();

		// When
		this.repository.deleteAll(emptyBreakIntervals);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete all break intervals should filter out null IDs")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteAllBreakIntervalsWithNullIdsFiltersOutNullIds() {
		// Given
		TimeLogBreakInterval interval1 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		interval1.setId(1);
		TimeLogBreakInterval interval2 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		interval2.setId(null);
		List<TimeLogBreakInterval> breakIntervals = Arrays.asList(interval1, interval2);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
		given(this.auroraDbDSLContext.deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T))
			.willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		given(mockDeleteConditionStep.execute()).willReturn(1);

		// When
		this.repository.deleteAll(breakIntervals);

		// Then
		then(this.auroraDbDSLContext).should().deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T);
		then(mockDeleteUsingStep).should().where(any(org.jooq.Condition.class));
		then(mockDeleteConditionStep).should().execute();
	}

	@Test
	@DisplayName("Delete all break intervals should return early when all IDs are null")
	void testDeleteAllBreakIntervalsAllNullIdsReturnsEarly() {
		// Given
		TimeLogBreakInterval interval1 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		interval1.setId(null);
		TimeLogBreakInterval interval2 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		interval2.setId(null);
		List<TimeLogBreakInterval> breakIntervals = Arrays.asList(interval1, interval2);

		// When
		this.repository.deleteAll(breakIntervals);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete all break intervals should propagate exception when JOOQ delete fails")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteAllJooqDeleteFailsPropagatesException() {
		// Given
		List<TimeLogBreakInterval> breakIntervals = TimesheetLogsTestDataFactory.createTimeLogBreakIntervalList();
		breakIntervals.get(0).setId(1);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
		given(this.auroraDbDSLContext.deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T))
			.willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		willThrow(new RuntimeException("Database error")).given(mockDeleteConditionStep).execute();

		// When & Then
		assertThatThrownBy(() -> this.repository.deleteAll(breakIntervals)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Database error");

		then(this.auroraDbDSLContext).should().deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T);
		then(mockDeleteConditionStep).should().execute();
	}

	/**
	 * Test deleteByTimeLogIdIn() method - JOOQ batch delete by time log IDs scenarios
	 */

	@Test
	@DisplayName("Delete by time log IDs should return early when list is null")
	void testDeleteByTimeLogIdInNullListReturnsEarly() {
		// Given
		List<Integer> timeLogIds = null;

		// When
		this.repository.deleteByTimeLogIdIn(timeLogIds);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete by time log IDs should return early when list is empty")
	void testDeleteByTimeLogIdInEmptyListReturnsEarly() {
		// Given
		List<Integer> timeLogIds = Collections.emptyList();

		// When
		this.repository.deleteByTimeLogIdIn(timeLogIds);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete by time log IDs should execute JOOQ batch delete for valid IDs")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteByTimeLogIdInValidIdsExecutesJooqBatchDelete() {
		// Given
		List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);

		given(this.auroraDbDSLContext.deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T))
			.willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		given(mockDeleteConditionStep.execute()).willReturn(3);

		// When
		this.repository.deleteByTimeLogIdIn(timeLogIds);

		// Then
		then(this.auroraDbDSLContext).should().deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T);
		then(mockDeleteUsingStep).should().where(any(org.jooq.Condition.class));
		then(mockDeleteConditionStep).should().execute();
	}

	@Test
	@DisplayName("Delete by time log IDs should propagate exception when JOOQ delete fails")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteByTimeLogIdInJooqDeleteFailsPropagatesException() {
		// Given
		List<Integer> timeLogIds = Arrays.asList(1, 2, 3);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);

		given(this.auroraDbDSLContext.deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T))
			.willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		willThrow(new RuntimeException("Database error while deleting by timeLogIds")).given(mockDeleteConditionStep)
			.execute();

		// When & Then
		assertThatThrownBy(() -> this.repository.deleteByTimeLogIdIn(timeLogIds)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Database error while deleting by timeLogIds");

		then(this.auroraDbDSLContext).should().deleteFrom(CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T);
		then(mockDeleteConditionStep).should().execute();
	}

	/**
	 * Creates a list of saved TimeLogBreakInterval entities for testing. These represent
	 * entities that have been persisted and have IDs assigned.
	 */
	private List<TimeLogBreakInterval> createSavedTimeLogBreakIntervalList() {
		TimeLogBreakInterval interval1 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		interval1.setId(1);

		TimeLogBreakInterval interval2 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		interval2.setId(2);
		interval2.setTimeLogId(2);
		interval2.setBreakStartTime(600); // 10 minutes
		interval2.setBreakEndTime(900); // 15 minutes

		return Arrays.asList(interval1, interval2);
	}

}
