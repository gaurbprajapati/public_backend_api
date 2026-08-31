package io.recruitcrm.microservice.timesheet.repositories.timesheet_approval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetApprovalRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.DeleteUsingStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetApprovalRepository Tests")
class TimesheetApprovalRepositoryTests {

	private static final String PARAM_TIMESHEET_IDS = "timesheetIds";

	@Mock
	private TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	@Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
	private DSLContext auroraDbDSLContext;

	@Mock
	private EntityManager entityManager;

	private TimesheetApprovalRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new TimesheetApprovalRepository(this.timesheetApprovalJpaRepository, this.auroraDbDSLContext,
				this.entityManager);
	}

	@Test
	@DisplayName("createTimesheetApproval should build and save entity with given fields")
	void testCreateTimesheetApprovalSavesExpectedEntity() {
		// Given
		ArgumentCaptor<TimesheetApproval> captor = ArgumentCaptor.forClass(TimesheetApproval.class);

		// When
		this.repository.createTimesheetApproval(TimesheetApprovalRepositoryTestDataFactory.DEFAULT_TIMESHEET_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_USER_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_USER_TYPE_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_APPROVAL_STATUS_TYPE_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_REMARK);

		// Then
		then(this.timesheetApprovalJpaRepository).should().save(captor.capture());
		TimesheetApproval saved = captor.getValue();
		assertThat(saved.getTimesheetId()).isEqualTo(TimesheetApprovalRepositoryTestDataFactory.DEFAULT_TIMESHEET_ID);
		assertThat(saved.getEntityId()).isEqualTo(TimesheetApprovalRepositoryTestDataFactory.DEFAULT_USER_ID);
		assertThat(saved.getUserTypeId()).isEqualTo(TimesheetApprovalRepositoryTestDataFactory.DEFAULT_USER_TYPE_ID);
		assertThat(saved.getTimesheetApprovalStatusTypeId())
			.isEqualTo(TimesheetApprovalRepositoryTestDataFactory.DEFAULT_APPROVAL_STATUS_TYPE_ID);
		assertThat(saved.getRemark()).isEqualTo(TimesheetApprovalRepositoryTestDataFactory.DEFAULT_REMARK);
		assertThat(saved.getCreatedOn()).isPositive();
	}

	@Test
	@DisplayName("createBulkTimesheetApprovals should delegate to saveAll")
	void testCreateBulkTimesheetApprovalsDelegatesToSaveAll() {
		// Given
		List<TimesheetApproval> approvals = TimesheetApprovalRepositoryTestDataFactory.createTimesheetApprovalList();

		// When
		this.repository.createBulkTimesheetApprovals(approvals);

		// Then
		then(this.timesheetApprovalJpaRepository).should().saveAll(approvals);
	}

	@Test
	@DisplayName("createBulkTimesheetApprovals should propagate data access exception")
	void testCreateBulkTimesheetApprovalsPropagatesException() {
		// Given
		List<TimesheetApproval> approvals = TimesheetApprovalRepositoryTestDataFactory.createTimesheetApprovalList();
		willThrow(new DataIntegrityViolationException("saveAll failed")).given(this.timesheetApprovalJpaRepository)
			.saveAll(approvals);

		// When and Then
		assertThatThrownBy(() -> this.repository.createBulkTimesheetApprovals(approvals))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("saveAll failed");
	}

	@Test
	@DisplayName("deleteByTimesheetIdIn should return early when IDs are null")
	void testDeleteByTimesheetIdInWithNullIdsReturnsEarly() {
		// Given
		List<Integer> timesheetIds = null;

		// When
		this.repository.deleteByTimesheetIdIn(timesheetIds);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("deleteByTimesheetIdIn should return early when IDs are empty")
	void testDeleteByTimesheetIdInWithEmptyIdsReturnsEarly() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();

		// When
		this.repository.deleteByTimesheetIdIn(timesheetIds);

		// Then
		then(this.auroraDbDSLContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("deleteByTimesheetIdIn should execute JOOQ delete for valid IDs")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteByTimesheetIdInWithValidIdsExecutesDelete() {
		// Given
		List<Integer> timesheetIds = TimesheetApprovalRepositoryTestDataFactory.createTimesheetIds();
		DeleteUsingStep mockDeleteUsingStep = org.mockito.Mockito.mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = org.mockito.Mockito.mock(DeleteConditionStep.class);
		given(this.auroraDbDSLContext.deleteFrom(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T))
			.willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		given(mockDeleteConditionStep.execute()).willReturn(3);

		// When
		this.repository.deleteByTimesheetIdIn(timesheetIds);

		// Then
		then(this.auroraDbDSLContext).should().deleteFrom(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T);
		then(mockDeleteUsingStep).should().where(any(org.jooq.Condition.class));
		then(mockDeleteConditionStep).should().execute();
	}

	@Test
	@DisplayName("findLatestApprovalEntitiesByTimesheetIds should return empty list for null input")
	void testFindLatestApprovalEntitiesByTimesheetIdsWithNullInputReturnsEmpty() {
		// Given
		List<Integer> timesheetIds = null;

		// When
		List<TimesheetApproval> result = this.repository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("findLatestApprovalEntitiesByTimesheetIds should return empty list for empty input")
	void testFindLatestApprovalEntitiesByTimesheetIdsWithEmptyInputReturnsEmpty() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();

		// When
		List<TimesheetApproval> result = this.repository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("findLatestApprovalEntitiesByTimesheetIds should execute JPQL query and return results")
	void testFindLatestApprovalEntitiesByTimesheetIdsWithValidInputReturnsResults() {
		// Given
		List<Integer> timesheetIds = TimesheetApprovalRepositoryTestDataFactory.createTimesheetIds();
		List<TimesheetApproval> expected = TimesheetApprovalRepositoryTestDataFactory.createTimesheetApprovalList();
		TypedQuery<TimesheetApproval> mockQuery = org.mockito.Mockito.mock(TypedQuery.class);
		given(this.entityManager.createQuery(any(String.class),
				org.mockito.ArgumentMatchers.eq(TimesheetApproval.class)))
			.willReturn(mockQuery);
		given(mockQuery.setParameter(PARAM_TIMESHEET_IDS, timesheetIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(expected);

		// When
		List<TimesheetApproval> result = this.repository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds);

		// Then
		assertThat(result).isEqualTo(expected);
		then(this.entityManager).should()
			.createQuery(any(String.class), org.mockito.ArgumentMatchers.eq(TimesheetApproval.class));
		then(mockQuery).should().setParameter(PARAM_TIMESHEET_IDS, timesheetIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("findLatestApprovalEntitiesByTimesheetIds should propagate data access exception")
	void testFindLatestApprovalEntitiesByTimesheetIdsPropagatesException() {
		// Given
		List<Integer> timesheetIds = TimesheetApprovalRepositoryTestDataFactory.createTimesheetIds();
		TypedQuery<TimesheetApproval> mockQuery = org.mockito.Mockito.mock(TypedQuery.class);
		given(this.entityManager.createQuery(any(String.class),
				org.mockito.ArgumentMatchers.eq(TimesheetApproval.class)))
			.willReturn(mockQuery);
		given(mockQuery.setParameter(PARAM_TIMESHEET_IDS, timesheetIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willThrow(new DataIntegrityViolationException("query failed"));

		// When and Then
		assertThatThrownBy(() -> this.repository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("query failed");
	}

	@Test
	@DisplayName("createTimesheetApproval should propagate data access exception")
	void testCreateTimesheetApprovalPropagatesException() {
		// Given
		willThrow(new DataIntegrityViolationException("save failed")).given(this.timesheetApprovalJpaRepository)
			.save(org.mockito.ArgumentMatchers.any(TimesheetApproval.class));

		// When & Then
		assertThatThrownBy(() -> this.repository.createTimesheetApproval(
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_TIMESHEET_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_USER_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_USER_TYPE_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_APPROVAL_STATUS_TYPE_ID,
				TimesheetApprovalRepositoryTestDataFactory.DEFAULT_REMARK))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("save failed");
	}

	@Test
	@DisplayName("findTimesheetIdsWhereTransitionedToSubmittedInWindow should return ids from JOOQ query")
	void testFindTimesheetIdsWhereTransitionedToSubmittedInWindowReturnsIds() {
		// Given
		int fromEpoch = TimesheetApprovalRepositoryTestDataFactory.getDefaultFromEpoch();
		int toEpoch = TimesheetApprovalRepositoryTestDataFactory.getDefaultToEpoch();
		int submittedStatusId = TimesheetApprovalRepositoryTestDataFactory.getSubmittedStatusId();
		int openStatusId = TimesheetApprovalRepositoryTestDataFactory.getOpenStatusId();
		int rejectedStatusId = TimesheetApprovalRepositoryTestDataFactory.getRejectedStatusId();
		List<Integer> expectedIds = List.of(101, 102);
		given(this.auroraDbDSLContext.selectDistinct(org.mockito.ArgumentMatchers.any(org.jooq.SelectField.class))
			.from(org.mockito.ArgumentMatchers.any(org.jooq.TableLike.class))
			.where(org.mockito.ArgumentMatchers.any(org.jooq.Condition.class))
			.and(org.mockito.ArgumentMatchers.any(org.jooq.Condition.class))
			.and(org.mockito.ArgumentMatchers.any(org.jooq.Condition.class))
			.and(org.mockito.ArgumentMatchers.any(org.jooq.Condition.class))
			.and(org.mockito.ArgumentMatchers.any(org.jooq.Condition.class))
			.fetch(org.mockito.ArgumentMatchers.any(org.jooq.Field.class))).willReturn(expectedIds);

		// When
		List<Integer> result = this.repository.findTimesheetIdsWhereTransitionedToSubmittedInWindow(fromEpoch, toEpoch,
				submittedStatusId, openStatusId, rejectedStatusId);

		// Then
		assertThat(result).isEqualTo(expectedIds);
	}

}
