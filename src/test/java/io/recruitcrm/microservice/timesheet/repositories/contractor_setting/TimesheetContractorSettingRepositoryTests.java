package io.recruitcrm.microservice.timesheet.repositories.contractor_setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetContractorSettingRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetContractorSettingRepository Tests")
class TimesheetContractorSettingRepositoryTests {

	private static final String PARAM_START_DATE = "startDate";

	private static final String PARAM_END_DATE = "endDate";

	private static final String PARAM_CONTRACTOR_IDS = "contractorIds";

	private static final String PARAM_JOB_ID = "jobId";

	private static final String PARAM_CONTRACTOR_ID_0 = "contractorId0";

	private static final String PARAM_JOB_ID_0 = "jobId0";

	private static final String PARAM_CONTRACTOR_ID_1 = "contractorId1";

	private static final String PARAM_JOB_ID_1 = "jobId1";

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private TimesheetContractorSettingRepository repository;

	@Mock
	private TypedQuery<OccupiedSlotsQueryResultDto> typedQuery;

	@BeforeEach
	void setUp() {
		this.repository = new TimesheetContractorSettingRepository(this.entityManager);
	}

	@Test
	@DisplayName("findTimesheetsWithinDateRangeAndContractors should return query results for valid input")
	void testFindTimesheetsWithinDateRangeAndContractorsValidInputReturnsResults() {
		// Given
		Integer startDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE;
		Integer endDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE;
		Integer jobId = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_JOB_ID;
		List<Integer> contractorIds = TimesheetContractorSettingRepositoryTestDataFactory.createContractorIds();
		List<OccupiedSlotsQueryResultDto> expected = TimesheetContractorSettingRepositoryTestDataFactory
			.createOccupiedSlots();
		String expectedJpql = "SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay "
				+ "FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "LEFT JOIN ts.association a "
				+ "WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate AND a.contractorId IN :contractorIds AND a.jobId = :jobId ";
		given(this.entityManager.createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class))
			.willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_START_DATE, startDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_END_DATE, endDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_IDS, contractorIds)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID, jobId)).willReturn(this.typedQuery);
		given(this.typedQuery.getResultList()).willReturn(expected);

		// When
		List<OccupiedSlotsQueryResultDto> result = this.repository
			.findTimesheetsWithinDateRangeAndContractors(startDate, endDate, contractorIds, jobId);

		// Then
		assertThat(result).isEqualTo(expected).hasSize(2);
		then(this.entityManager).should().createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class);
		then(this.typedQuery).should().setParameter(PARAM_START_DATE, startDate);
		then(this.typedQuery).should().setParameter(PARAM_END_DATE, endDate);
		then(this.typedQuery).should().setParameter(PARAM_CONTRACTOR_IDS, contractorIds);
		then(this.typedQuery).should().setParameter(PARAM_JOB_ID, jobId);
		then(this.typedQuery).should().getResultList();
	}

	@Test
	@DisplayName("findTimesheetsWithinDateRangeAndContractors should propagate data access exception")
	void testFindTimesheetsWithinDateRangeAndContractorsExceptionPropagates() {
		// Given
		Integer startDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE;
		Integer endDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE;
		Integer jobId = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_JOB_ID;
		List<Integer> contractorIds = TimesheetContractorSettingRepositoryTestDataFactory.createContractorIds();
		String expectedJpql = "SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay "
				+ "FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "LEFT JOIN ts.association a "
				+ "WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate AND a.contractorId IN :contractorIds AND a.jobId = :jobId ";
		given(this.entityManager.createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class))
			.willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_START_DATE, startDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_END_DATE, endDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_IDS, contractorIds)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID, jobId)).willReturn(this.typedQuery);
		given(this.typedQuery.getResultList()).willThrow(new DataAccessException("query failed") {
		});

		// When and Then
		assertThatThrownBy(() -> this.repository.findTimesheetsWithinDateRangeAndContractors(startDate, endDate,
				contractorIds, jobId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("query failed");
	}

	@Test
	@DisplayName("findTimesheetsForContractorJobPairs should return empty list when pairs are null")
	void testFindTimesheetsForContractorJobPairsNullPairsReturnsEmpty() {
		// Given
		List<ContractorJobPairDto> contractorJobPairs = null;

		// When
		List<OccupiedSlotsQueryResultDto> result = this.repository.findTimesheetsForContractorJobPairs(
				TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE,
				TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE, contractorJobPairs);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("findTimesheetsForContractorJobPairs should return empty list when pairs are empty")
	void testFindTimesheetsForContractorJobPairsEmptyPairsReturnsEmpty() {
		// Given
		List<ContractorJobPairDto> contractorJobPairs = Collections.emptyList();

		// When
		List<OccupiedSlotsQueryResultDto> result = this.repository.findTimesheetsForContractorJobPairs(
				TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE,
				TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE, contractorJobPairs);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("findTimesheetsForContractorJobPairs should build single pair query and return results")
	void testFindTimesheetsForContractorJobPairsSinglePairReturnsResults() {
		// Given
		Integer startDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE;
		Integer endDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE;
		List<ContractorJobPairDto> contractorJobPairs = List
			.of(TimesheetContractorSettingRepositoryTestDataFactory.createContractorJobPair(101, 500));
		String expectedJpql = "SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay "
				+ "FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "LEFT JOIN ts.association a " + "WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate "
				+ "AND ((a.contractorId = :contractorId0 AND a.jobId = :jobId0))";
		List<OccupiedSlotsQueryResultDto> expected = new ArrayList<>();
		expected.add(TimesheetContractorSettingRepositoryTestDataFactory.createOccupiedSlot(1, startDate, endDate, 101,
				500, 1));
		given(this.entityManager.createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class))
			.willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_START_DATE, startDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_END_DATE, endDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_ID_0, 101)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID_0, 500)).willReturn(this.typedQuery);
		given(this.typedQuery.getResultList()).willReturn(expected);

		// When
		List<OccupiedSlotsQueryResultDto> result = this.repository.findTimesheetsForContractorJobPairs(startDate,
				endDate, contractorJobPairs);

		// Then
		assertThat(result).isEqualTo(expected).hasSize(1);
		then(this.entityManager).should().createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class);
		then(this.typedQuery).should().setParameter(PARAM_START_DATE, startDate);
		then(this.typedQuery).should().setParameter(PARAM_END_DATE, endDate);
		then(this.typedQuery).should().setParameter(PARAM_CONTRACTOR_ID_0, 101);
		then(this.typedQuery).should().setParameter(PARAM_JOB_ID_0, 500);
		then(this.typedQuery).should().getResultList();
	}

	@Test
	@DisplayName("findTimesheetsForContractorJobPairs should build multi pair OR query and return results")
	void testFindTimesheetsForContractorJobPairsMultiplePairsReturnsResults() {
		// Given
		Integer startDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE;
		Integer endDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE;
		List<ContractorJobPairDto> contractorJobPairs = TimesheetContractorSettingRepositoryTestDataFactory
			.createContractorJobPairs();
		String expectedJpql = "SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay "
				+ "FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "LEFT JOIN ts.association a " + "WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate "
				+ "AND ((a.contractorId = :contractorId0 AND a.jobId = :jobId0) OR (a.contractorId = :contractorId1 AND a.jobId = :jobId1))";
		List<OccupiedSlotsQueryResultDto> expected = TimesheetContractorSettingRepositoryTestDataFactory
			.createOccupiedSlots();
		given(this.entityManager.createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class))
			.willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_START_DATE, startDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_END_DATE, endDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_ID_0, 101)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID_0, 500)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_ID_1, 102)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID_1, 501)).willReturn(this.typedQuery);
		given(this.typedQuery.getResultList()).willReturn(expected);

		// When
		List<OccupiedSlotsQueryResultDto> result = this.repository.findTimesheetsForContractorJobPairs(startDate,
				endDate, contractorJobPairs);

		// Then
		assertThat(result).isEqualTo(expected).hasSize(2);
		then(this.entityManager).should().createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class);
		then(this.typedQuery).should().setParameter(PARAM_START_DATE, startDate);
		then(this.typedQuery).should().setParameter(PARAM_END_DATE, endDate);
		then(this.typedQuery).should().setParameter(PARAM_CONTRACTOR_ID_0, 101);
		then(this.typedQuery).should().setParameter(PARAM_JOB_ID_0, 500);
		then(this.typedQuery).should().setParameter(PARAM_CONTRACTOR_ID_1, 102);
		then(this.typedQuery).should().setParameter(PARAM_JOB_ID_1, 501);
		then(this.typedQuery).should().getResultList();
	}

	@Test
	@DisplayName("findTimesheetsForContractorJobPairs should propagate data access exception")
	void testFindTimesheetsForContractorJobPairsExceptionPropagates() {
		// Given
		Integer startDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_START_DATE;
		Integer endDate = TimesheetContractorSettingRepositoryTestDataFactory.DEFAULT_END_DATE;
		List<ContractorJobPairDto> contractorJobPairs = TimesheetContractorSettingRepositoryTestDataFactory
			.createContractorJobPairs();
		String expectedJpql = "SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay "
				+ "FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "LEFT JOIN ts.association a " + "WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate "
				+ "AND ((a.contractorId = :contractorId0 AND a.jobId = :jobId0) OR (a.contractorId = :contractorId1 AND a.jobId = :jobId1))";
		given(this.entityManager.createQuery(expectedJpql, OccupiedSlotsQueryResultDto.class))
			.willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_START_DATE, startDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_END_DATE, endDate)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_ID_0, 101)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID_0, 500)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_CONTRACTOR_ID_1, 102)).willReturn(this.typedQuery);
		given(this.typedQuery.setParameter(PARAM_JOB_ID_1, 501)).willReturn(this.typedQuery);
		given(this.typedQuery.getResultList()).willThrow(new DataAccessException("dynamic query failed") {
		});

		// When and Then
		assertThatThrownBy(
				() -> this.repository.findTimesheetsForContractorJobPairs(startDate, endDate, contractorJobPairs))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("dynamic query failed");
	}

}
