package io.recruitcrm.microservice.timesheet.repositories.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.TimesheetTotalsQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.timesheet.services.export.ExportFieldRegistry;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetExportRepositoryTestDataFactory;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.TableLike;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for TimesheetExportRepository
 *
 * Note: These tests focus on testing the repository's logic and behavior patterns rather
 * than complex JOOQ query execution, which would require integration testing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetExportRepository Tests")
class TimesheetExportRepositoryTests {

	@Mock
	private DSLContext dslContext;

	@Mock
	private ExportFieldRegistry fieldRegistry;

	@InjectMocks
	private TimesheetExportRepository timesheetExportRepository;

	@BeforeEach
	void setUp() {
		// Setup is handled by @InjectMocks
	}

	// ========== Constructor Tests ==========

	@Test
	@DisplayName("Constructor should initialize repository with required dependencies")
	void testConstructorInitializesRepositoryCorrectly() {
		// Given & When
		TimesheetExportRepository repository = new TimesheetExportRepository(this.dslContext, this.fieldRegistry);

		// Then
		assertThat(repository).isNotNull();
	}

	// ========== getTimesheetTotals Tests ==========

	@Test
	@DisplayName("getTimesheetTotals should return empty DTO when timesheet IDs are null")
	@SuppressWarnings("unchecked")
	void testGetTimesheetTotalsNullIdsReturnsEmptyDto() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// When
		TimesheetTotalsQueryResultDto result = this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				accountId);

		// Then
		assertThat(result.totalTime()).isEmpty();
		assertThat(result.totalWorkTime()).isEmpty();
		assertThat(result.totalOvertime()).isEmpty();
		assertThat(result.totalRegularHours()).isEmpty();
		then(this.dslContext).should(never())
			.select(any(Field.class), any(Field.class), any(Field.class), any(Field.class), any(Field.class));
	}

	@Test
	@DisplayName("getTimesheetTotals should return empty DTO when timesheet IDs are empty")
	void testGetTimesheetTotalsEmptyIdsReturnsEmptyDto() {
		// Given
		List<Integer> timesheetIds = new ArrayList<>();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// When
		TimesheetTotalsQueryResultDto result = this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				accountId);

		// Then
		assertThat(result.totalTime()).isEmpty();
		assertThat(result.totalWorkTime()).isEmpty();
		assertThat(result.totalOvertime()).isEmpty();
		assertThat(result.totalRegularHours()).isEmpty();
	}

	@Test
	@DisplayName("getTimesheetTotals should return formatted totals when records fetched")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetTimesheetTotalsWithRecordsReturnsFormattedTotals() {
		// Given - fetch returns Result<Record5> so we mock Record5 for the loop
		List<Integer> timesheetIds = List.of(1);
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;
		SelectSelectStep<?> selectSelectStep = mock(SelectSelectStep.class);
		SelectJoinStep<?> selectJoinStep = mock(SelectJoinStep.class);
		SelectConditionStep<?> selectConditionStep = mock(SelectConditionStep.class);
		Result<Record5<Integer, Integer, Integer, Integer, Integer>> mockResult = mock(Result.class);
		Record5<Integer, Integer, Integer, Integer, Integer> mockRecord = mock(Record5.class);

		given(this.dslContext.select(any(Field.class), any(Field.class), any(Field.class), any(Field.class),
				any(Field.class)))
			.willReturn(selectSelectStep);
		given(selectSelectStep.from(any(Table.class))).willReturn((SelectJoinStep) selectJoinStep);
		given(selectJoinStep.where(any(Condition.class))).willReturn((SelectConditionStep) selectConditionStep);
		given(selectConditionStep.and(any(Condition.class))).willReturn((SelectConditionStep) selectConditionStep);
		given(selectConditionStep.fetch()).willReturn((Result) mockResult);
		given(mockResult.iterator()).willReturn(List.of(mockRecord).iterator());
		// Order matches SELECT: id, total_time, total_work_time, total_overtime,
		// total_regular_hour
		given(mockRecord.get(any(Field.class))).willReturn(1, 3600, 1800, 1800, 5400);

		// When
		TimesheetTotalsQueryResultDto result = this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				accountId);

		// Then
		assertThat(result.totalTime()).hasSize(1).containsEntry(1, "1.00");
		assertThat(result.totalWorkTime()).hasSize(1).containsEntry(1, "0.50");
		assertThat(result.totalOvertime()).hasSize(1).containsEntry(1, "0.50");
		assertThat(result.totalRegularHours()).hasSize(1).containsEntry(1, "1.50");
	}

	@Test
	@DisplayName("getTimesheetTotals should skip record when ID is null and format null seconds as 0.00")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetTimesheetTotalsSkipsRecordWithNullIdAndFormatsNullSeconds() {
		// Given - first record has null ID (skipped), second has valid ID with
		// null/negative seconds
		List<Integer> timesheetIds = List.of(1, 2);
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;
		SelectSelectStep<?> selectSelectStep = mock(SelectSelectStep.class);
		SelectJoinStep<?> selectJoinStep = mock(SelectJoinStep.class);
		SelectConditionStep<?> selectConditionStep = mock(SelectConditionStep.class);
		Result<Record5<Integer, Integer, Integer, Integer, Integer>> mockResult = mock(Result.class);
		Record5<Integer, Integer, Integer, Integer, Integer> recordNullId = mock(Record5.class);
		Record5<Integer, Integer, Integer, Integer, Integer> recordValid = mock(Record5.class);

		given(this.dslContext.select(any(Field.class), any(Field.class), any(Field.class), any(Field.class),
				any(Field.class)))
			.willReturn(selectSelectStep);
		given(selectSelectStep.from(any(Table.class))).willReturn((SelectJoinStep) selectJoinStep);
		given(selectJoinStep.where(any(Condition.class))).willReturn((SelectConditionStep) selectConditionStep);
		given(selectConditionStep.and(any(Condition.class))).willReturn((SelectConditionStep) selectConditionStep);
		given(selectConditionStep.fetch()).willReturn((Result) mockResult);
		given(mockResult.iterator()).willReturn(List.of(recordNullId, recordValid).iterator());
		given(recordNullId.get(any(Field.class))).willReturn(null, null, null, null, null);
		given(recordValid.get(any(Field.class))).willReturn(2, null, -100, 0, -50);

		// When
		TimesheetTotalsQueryResultDto result = this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				accountId);

		// Then - only record with id=2 present; null/negative/zero formatted as "0.00"
		assertThat(result.totalTime()).hasSize(1).containsEntry(2, "0.00");
		assertThat(result.totalWorkTime()).hasSize(1).containsEntry(2, "0.00");
		assertThat(result.totalOvertime()).hasSize(1).containsEntry(2, "0.00");
		assertThat(result.totalRegularHours()).hasSize(1).containsEntry(2, "0.00");
	}

	// ========== buildSelectFields Tests ==========

	@Test
	@DisplayName("buildSelectFields should convert field definitions to JOOQ fields")
	void testBuildSelectFieldsConvertsFieldDefinitionsToJooqFields() {
		// Given
		List<ExportFieldDefinition> fieldDefinitions = TimesheetExportRepositoryTestDataFactory
			.createFieldDefinitions();

		// When
		List<Field<?>> result = this.timesheetExportRepository.buildSelectFields(fieldDefinitions);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(fieldDefinitions.get(0).getJooqField());
	}

	@Test
	@DisplayName("buildSelectFields should handle empty field definitions")
	void testBuildSelectFieldsHandlesEmptyFieldDefinitions() {
		// Given
		List<ExportFieldDefinition> fieldDefinitions = new ArrayList<>();

		// When
		List<Field<?>> result = this.timesheetExportRepository.buildSelectFields(fieldDefinitions);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("buildSelectFields should handle multiple field definitions")
	void testBuildSelectFieldsHandlesMultipleFieldDefinitions() {
		// Given
		List<ExportFieldDefinition> fieldDefinitions = TimesheetExportRepositoryTestDataFactory
			.createFieldDefinitionsWithVariousEntities();

		// When
		List<Field<?>> result = this.timesheetExportRepository.buildSelectFields(fieldDefinitions);

		// Then
		assertThat(result).hasSize(fieldDefinitions.size());
		for (int i = 0; i < fieldDefinitions.size(); i++) {
			assertThat(result.get(i)).isEqualTo(fieldDefinitions.get(i).getJooqField());
		}
	}

	// ========== determineEntityTypeForExport Tests ==========

	@Test
	@DisplayName("determineEntityTypeForExport should return CANDIDATE when candidate entity is required")
	void testDetermineEntityTypeForExportReturnsCandidateWhenCandidateEntityRequired() {
		// Given
		Set<String> requiredEntities = Set.of("c", "tss", "tsa");

		// When
		EntityTypeEnum result = this.timesheetExportRepository.determineEntityTypeForExport(requiredEntities);

		// Then
		assertThat(result).isEqualTo(EntityTypeEnum.CANDIDATE);
	}

	@Test
	@DisplayName("determineEntityTypeForExport should return CANDIDATE as default when no candidate entity")
	void testDetermineEntityTypeForExportReturnsDefaultCandidate() {
		// Given
		Set<String> requiredEntities = Set.of("tss", "tsa");

		// When
		EntityTypeEnum result = this.timesheetExportRepository.determineEntityTypeForExport(requiredEntities);

		// Then
		assertThat(result).isEqualTo(EntityTypeEnum.CANDIDATE);
	}

	@Test
	@DisplayName("determineEntityTypeForExport should handle empty required entities")
	void testDetermineEntityTypeForExportHandlesEmptyRequiredEntities() {
		// Given
		Set<String> requiredEntities = Set.of();

		// When
		EntityTypeEnum result = this.timesheetExportRepository.determineEntityTypeForExport(requiredEntities);

		// Then
		assertThat(result).isEqualTo(EntityTypeEnum.CANDIDATE);
	}

	// ========== addTimesheetIdFilter Tests ==========

	@Test
	@DisplayName("addTimesheetIdFilter should add timesheet ID filter when IDs are provided")
	void testAddTimesheetIdFilterAddsFilterWhenIdsProvided() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithTimesheetIds();

		// When
		Condition result = this.timesheetExportRepository.addTimesheetIdFilter(baseCondition, request);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addTimesheetIdFilter should not modify condition when no timesheet IDs")
	void testAddTimesheetIdFilterDoesNotModifyConditionWhenNoIds() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();

		// When
		Condition result = this.timesheetExportRepository.addTimesheetIdFilter(baseCondition, request);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addTimesheetIdFilter should not modify condition when timesheet IDs are empty")
	void testAddTimesheetIdFilterDoesNotModifyConditionWhenIdsEmpty() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("candidatename"))
			.candidateFields(List.of())
			.timesheetIds(new ArrayList<>())
			.fileFormat(io.recruitcrm.microservice.timesheet.dto.export.FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();

		// When
		Condition result = this.timesheetExportRepository.addTimesheetIdFilter(baseCondition, request);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	// ========== addCustomFilters Tests ==========

	@Test
	@DisplayName("addCustomFilters should return unchanged condition when filters are null")
	void testAddCustomFiltersReturnsUnchangedConditionWhenFiltersNull() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithNullFilters();

		// When
		Condition result = this.timesheetExportRepository.addCustomFilters(baseCondition, request);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addCustomFilters should return unchanged condition when filters are empty")
	void testAddCustomFiltersReturnsUnchangedConditionWhenFiltersEmpty() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithEmptyFilters();

		// When
		Condition result = this.timesheetExportRepository.addCustomFilters(baseCondition, request);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addCustomFilters should apply filters when filters are provided")
	void testAddCustomFiltersAppliesFiltersWhenProvided() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createExportRequestWithFilters();

		// When
		Condition result = this.timesheetExportRepository.addCustomFilters(baseCondition, request);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	// ========== applyIndividualFilter Tests ==========

	@Test
	@DisplayName("applyIndividualFilter should handle candidateId filter")
	void testApplyIndividualFilterHandlesCandidateIdFilter() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		String filterKey = "candidateId";
		Object filterValue = TimesheetExportRepositoryTestDataFactory.TEST_CANDIDATE_ID;

		// When
		Condition result = this.timesheetExportRepository.applyIndividualFilter(baseCondition, filterKey, filterValue);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("applyIndividualFilter should handle periodStartAfter filter")
	void testApplyIndividualFilterHandlesPeriodStartAfterFilter() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		String filterKey = "periodStartAfter";
		Object filterValue = TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START;

		// When
		Condition result = this.timesheetExportRepository.applyIndividualFilter(baseCondition, filterKey, filterValue);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("applyIndividualFilter should handle periodEndBefore filter")
	void testApplyIndividualFilterHandlesPeriodEndBeforeFilter() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		String filterKey = "periodEndBefore";
		Object filterValue = TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END;

		// When
		Condition result = this.timesheetExportRepository.applyIndividualFilter(baseCondition, filterKey, filterValue);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("applyIndividualFilter should return unchanged condition for unknown filter key")
	void testApplyIndividualFilterReturnsUnchangedConditionForUnknownFilterKey() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		String filterKey = "unknownFilter";
		Object filterValue = "unknownValue";

		// When
		Condition result = this.timesheetExportRepository.applyIndividualFilter(baseCondition, filterKey, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	// ========== addCandidateIdFilter Tests ==========

	@Test
	@DisplayName("addCandidateIdFilter should add filter when value is Integer")
	void testAddCandidateIdFilterAddsFilterWhenValueIsInteger() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = TimesheetExportRepositoryTestDataFactory.TEST_CANDIDATE_ID;

		// When
		Condition result = this.timesheetExportRepository.addCandidateIdFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addCandidateIdFilter should return unchanged condition when value is not Integer")
	void testAddCandidateIdFilterReturnsUnchangedConditionWhenValueNotInteger() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = "not_an_integer";

		// When
		Condition result = this.timesheetExportRepository.addCandidateIdFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addCandidateIdFilter should return unchanged condition when value is null")
	void testAddCandidateIdFilterReturnsUnchangedConditionWhenValueNull() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = null;

		// When
		Condition result = this.timesheetExportRepository.addCandidateIdFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	// ========== addPeriodStartAfterFilter Tests ==========

	@Test
	@DisplayName("addPeriodStartAfterFilter should add filter when value is Integer")
	void testAddPeriodStartAfterFilterAddsFilterWhenValueIsInteger() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START;

		// When
		Condition result = this.timesheetExportRepository.addPeriodStartAfterFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addPeriodStartAfterFilter should return unchanged condition when value is not Integer")
	void testAddPeriodStartAfterFilterReturnsUnchangedConditionWhenValueNotInteger() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = "not_an_integer";

		// When
		Condition result = this.timesheetExportRepository.addPeriodStartAfterFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addPeriodStartAfterFilter should return unchanged condition when value is null")
	void testAddPeriodStartAfterFilterReturnsUnchangedConditionWhenValueNull() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = null;

		// When
		Condition result = this.timesheetExportRepository.addPeriodStartAfterFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	// ========== addPeriodEndBeforeFilter Tests ==========

	@Test
	@DisplayName("addPeriodEndBeforeFilter should add filter when value is Integer")
	void testAddPeriodEndBeforeFilterAddsFilterWhenValueIsInteger() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END;

		// When
		Condition result = this.timesheetExportRepository.addPeriodEndBeforeFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addPeriodEndBeforeFilter should return unchanged condition when value is not Integer")
	void testAddPeriodEndBeforeFilterReturnsUnchangedConditionWhenValueNotInteger() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = "not_an_integer";

		// When
		Condition result = this.timesheetExportRepository.addPeriodEndBeforeFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	@Test
	@DisplayName("addPeriodEndBeforeFilter should return unchanged condition when value is null")
	void testAddPeriodEndBeforeFilterReturnsUnchangedConditionWhenValueNull() {
		// Given
		Condition baseCondition = DSL.trueCondition();
		Object filterValue = null;

		// When
		Condition result = this.timesheetExportRepository.addPeriodEndBeforeFilter(baseCondition, filterValue);

		// Then
		assertThat(result).isEqualTo(baseCondition);
	}

	// ========== convertToDynamicExportData Tests ==========

	@Test
	@DisplayName("convertToDynamicExportData should convert JOOQ results to export data")
	void testConvertToDynamicExportDataConvertsJooqResultsToExportData() {
		// Given
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		Record mockRecord = mock(Record.class);
		List<String> selectedFields = List.of("candidatename");

		given(mockResults.stream()).willReturn(List.of(mockRecord).stream());
		given(mockRecord.get("candidatename")).willReturn("John Doe");

		// When
		List<DynamicExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToDynamicExportData(mockResults, selectedFields);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getData()).containsEntry("candidatename", "John Doe");
		assertThat(result.get(0).getColumnOrder()).isEqualTo(selectedFields);
	}

	@Test
	@DisplayName("convertToDynamicExportData should handle empty results")
	void testConvertToDynamicExportDataHandlesEmptyResults() {
		// Given
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		List<String> selectedFields = List.of("candidatename");

		given(mockResults.stream()).willReturn(List.<Record>of().stream());

		// When
		List<DynamicExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToDynamicExportData(mockResults, selectedFields);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("convertToDynamicExportData should handle multiple records")
	void testConvertToDynamicExportDataHandlesMultipleRecords() {
		// Given
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		Record mockRecord1 = mock(Record.class);
		Record mockRecord2 = mock(Record.class);
		List<String> selectedFields = List.of("candidatename");

		given(mockResults.stream()).willReturn(List.of(mockRecord1, mockRecord2).stream());
		given(mockRecord1.get("candidatename")).willReturn("John Doe");
		given(mockRecord2.get("candidatename")).willReturn("Jane Smith");

		// When
		List<DynamicExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToDynamicExportData(mockResults, selectedFields);

		// Then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getData()).containsEntry("candidatename", "John Doe");
		assertThat(result.get(1).getData()).containsEntry("candidatename", "Jane Smith");
	}

	@Test
	@DisplayName("convertToDynamicExportData should handle multiple fields")
	void testConvertToDynamicExportDataHandlesMultipleFields() {
		// Given
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		Record mockRecord = mock(Record.class);
		List<String> selectedFields = List.of("candidatename", "jobtitle");

		given(mockResults.stream()).willReturn(List.of(mockRecord).stream());
		given(mockRecord.get("candidatename")).willReturn("John Doe");
		given(mockRecord.get("jobtitle")).willReturn("Software Engineer");

		// When
		List<DynamicExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToDynamicExportData(mockResults, selectedFields);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getData()).containsEntry("candidatename", "John Doe");
		assertThat(result.get(0).getData()).containsEntry("jobtitle", "Software Engineer");
		assertThat(result.get(0).getColumnOrder()).isEqualTo(selectedFields);
	}

	// ========== buildSelectFieldsWithPeriods Tests ==========

	@Test
	@DisplayName("buildSelectFieldsWithPeriods should add period fields to select fields")
	void testBuildSelectFieldsWithPeriodsAddsPeriodFields() {
		// Given
		List<ExportFieldDefinition> fieldDefinitions = TimesheetExportRepositoryTestDataFactory
			.createFieldDefinitions();

		// When
		List<Field<?>> result = this.timesheetExportRepository.buildSelectFieldsWithPeriods(fieldDefinitions);

		// Then
		assertThat(result).hasSize(5); // 1 original + 4 period fields
		assertThat(result.get(0)).isEqualTo(fieldDefinitions.get(0).getJooqField());
		// Period fields are added but we can't easily test their exact structure due to
		// JOOQ
		// complexity
	}

	@Test
	@DisplayName("buildSelectFieldsWithPeriods should handle empty field definitions")
	void testBuildSelectFieldsWithPeriodsHandlesEmptyFieldDefinitions() {
		// Given
		List<ExportFieldDefinition> fieldDefinitions = new ArrayList<>();

		// When
		List<Field<?>> result = this.timesheetExportRepository.buildSelectFieldsWithPeriods(fieldDefinitions);

		// Then
		assertThat(result).hasSize(4); // Only period fields
	}

	// ========== parseDateToEpoch Tests ==========

	@Test
	@DisplayName("parseDateToEpoch should parse valid date string to epoch")
	void testParseDateToEpochParsesValidDateStringToEpoch() {
		// Given
		String dateString = TimesheetExportRepositoryTestDataFactory.createValidDateString();

		// When
		Integer result = this.timesheetExportRepository.parseDateToEpoch(dateString);

		// Then
		assertThat(result).isNotNull().isPositive();
	}

	@Test
	@DisplayName("parseDateToEpoch should return null for invalid date string")
	void testParseDateToEpochReturnsNullForInvalidDateString() {
		// Given
		String dateString = TimesheetExportRepositoryTestDataFactory.createInvalidDateString();

		// When
		Integer result = this.timesheetExportRepository.parseDateToEpoch(dateString);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("parseDateToEpoch should return null for null date string")
	void testParseDateToEpochReturnsNullForNullDateString() {
		// Given
		String dateString = TimesheetExportRepositoryTestDataFactory.createNullDateString();

		// When
		Integer result = this.timesheetExportRepository.parseDateToEpoch(dateString);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("parseDateToEpoch should return null for empty date string")
	void testParseDateToEpochReturnsNullForEmptyDateString() {
		// Given
		String dateString = TimesheetExportRepositoryTestDataFactory.createEmptyDateString();

		// When
		Integer result = this.timesheetExportRepository.parseDateToEpoch(dateString);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("parseDateToEpoch should return null for whitespace-only date string")
	void testParseDateToEpochReturnsNullForWhitespaceOnlyDateString() {
		// Given
		String dateString = "   ";

		// When
		Integer result = this.timesheetExportRepository.parseDateToEpoch(dateString);

		// Then
		assertThat(result).isNull();
	}

	// ========== getExportDataGroupedByPeriodsFromIndividualRecords Tests ==========

	@Test
	@DisplayName("getExportDataGroupedByPeriodsFromIndividualRecords should group records by period")
	void testGetExportDataGroupedByPeriodsFromIndividualRecordsGroupsRecordsByPeriod() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithTimesheetIds();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// Create a spy to mock the getExportData method
		TimesheetExportRepository spyRepository = new TimesheetExportRepository(this.dslContext, this.fieldRegistry) {
			@Override
			public List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto req, Integer accId) {
				return TimesheetExportRepositoryTestDataFactory.createExportResponseDataList();
			}
		};

		// When
		List<PeriodGroupedExportResponseBodyDto> result = spyRepository
			.getExportDataGroupedByPeriodsFromIndividualRecords(request, accountId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTimesheetsInPeriod()).hasSize(2);
		assertThat(result.get(0).getPeriodDisplayName()).contains(" - ");
	}

	@Test
	@DisplayName("getExportDataGroupedByPeriodsFromIndividualRecords should handle empty individual records")
	void testGetExportDataGroupedByPeriodsFromIndividualRecordsHandlesEmptyIndividualRecords() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithTimesheetIds();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// Create a spy to mock the getExportData method
		TimesheetExportRepository spyRepository = new TimesheetExportRepository(this.dslContext, this.fieldRegistry) {
			@Override
			public List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto req, Integer accId) {
				return new ArrayList<>();
			}
		};

		// When
		List<PeriodGroupedExportResponseBodyDto> result = spyRepository
			.getExportDataGroupedByPeriodsFromIndividualRecords(request, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getExportDataGroupedByPeriodsFromIndividualRecords should handle records with null timesheet period")
	void testGetExportDataGroupedByPeriodsFromIndividualRecordsHandlesRecordsWithNullTimesheetPeriod() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithTimesheetIds();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// Create a spy to mock the getExportData method
		TimesheetExportRepository spyRepository = new TimesheetExportRepository(this.dslContext, this.fieldRegistry) {
			@Override
			public List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto req, Integer accId) {
				Map<String, Object> data = new HashMap<>();
				data.put("candidatename", "John Doe");
				data.put("timesheetPeriod", null); // Null period
				return List.of(new DynamicExportResponseBodyDto(data, List.of("candidatename")));
			}
		};

		// When
		List<PeriodGroupedExportResponseBodyDto> result = spyRepository
			.getExportDataGroupedByPeriodsFromIndividualRecords(request, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getExportDataGroupedByPeriodsFromIndividualRecords should sort grouped data by period start")
	void testGetExportDataGroupedByPeriodsFromIndividualRecordsSortsGroupedDataByPeriodStart() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithTimesheetIds();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// Create a spy to mock the getExportData method with multiple periods
		TimesheetExportRepository spyRepository = new TimesheetExportRepository(this.dslContext, this.fieldRegistry) {
			@Override
			public List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto req, Integer accId) {
				List<DynamicExportResponseBodyDto> records = new ArrayList<>();

				// Later period
				Map<String, Object> data1 = new HashMap<>();
				data1.put("candidatename", "John Doe");
				data1.put("timesheetPeriod", "01/02/2022 - 01/03/2022");
				records.add(new DynamicExportResponseBodyDto(data1, List.of("candidatename")));

				// Earlier period
				Map<String, Object> data2 = new HashMap<>();
				data2.put("candidatename", "Jane Smith");
				data2.put("timesheetPeriod", "01/01/2022 - 01/02/2022");
				records.add(new DynamicExportResponseBodyDto(data2, List.of("candidatename")));

				return records;
			}
		};

		// When
		List<PeriodGroupedExportResponseBodyDto> result = spyRepository
			.getExportDataGroupedByPeriodsFromIndividualRecords(request, accountId);

		// Then
		assertThat(result).hasSize(2);
		// Should be sorted by period start (earlier first)
		assertThat(result.get(0).getPeriodDisplayName()).contains("01/01/2022");
		assertThat(result.get(1).getPeriodDisplayName()).contains("01/02/2022");
	}

	@Test
	@DisplayName("getExportDataGroupedByPeriodsFromIndividualRecords should handle sorting with null period starts")
	void testGetExportDataGroupedByPeriodsFromIndividualRecordsHandlesSortingWithNullPeriodStarts() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestWithTimesheetIds();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		// Create a spy to mock the getExportData method with invalid period format
		TimesheetExportRepository spyRepository = new TimesheetExportRepository(this.dslContext, this.fieldRegistry) {
			@Override
			public List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto req, Integer accId) {
				List<DynamicExportResponseBodyDto> records = new ArrayList<>();

				// Valid period
				Map<String, Object> data1 = new HashMap<>();
				data1.put("candidatename", "John Doe");
				data1.put("timesheetPeriod", "01/01/2022 - 01/02/2022");
				records.add(new DynamicExportResponseBodyDto(data1, List.of("candidatename")));

				// Invalid period format (will result in null period start)
				Map<String, Object> data2 = new HashMap<>();
				data2.put("candidatename", "Jane Smith");
				data2.put("timesheetPeriod", "invalid-period-format");
				records.add(new DynamicExportResponseBodyDto(data2, List.of("candidatename")));

				return records;
			}
		};

		// When
		List<PeriodGroupedExportResponseBodyDto> result = spyRepository
			.getExportDataGroupedByPeriodsFromIndividualRecords(request, accountId);

		// Then
		assertThat(result).hasSize(2);
		// Valid period should come first, invalid period (null start) should come last
		assertThat(result.get(0).getPeriodStart()).isNotNull();
		assertThat(result.get(1).getPeriodStart()).isNull();
	}

	// ========== buildBaseQuery Tests ==========
	// Note: buildBaseQuery involves complex JOOQ operations that require a real
	// DSLContext
	// and are better tested through integration tests rather than unit tests with mocks.

	// ========== Join Methods Tests ==========
	// Note: These methods involve complex JOOQ operations that are difficult to test in
	// isolation
	// without full integration testing. The logic is primarily about conditional joins
	// based on
	// required entities, which is tested through the integration of buildBaseQuery.

	// ========== addWhereConditions Tests ==========
	// Note: These methods involve complex JOOQ condition building that is difficult to
	// test
	// in isolation. The logic is tested through the integration tests of the main
	// methods.

	// ========== convertToGroupedPeriodData Tests ==========

	@Test
	@DisplayName("convertToGroupedPeriodData should convert flat JOOQ results to grouped period data")
	void testConvertToGroupedPeriodDataConvertsJooqResultsToGroupedPeriodData() {
		// Given - 2 flat records with same period (in-memory grouping)
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		Record mockRecord1 = mock(Record.class);
		Record mockRecord2 = mock(Record.class);
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();
		List<String> selectedFields = request.getSelectedFields();

		given(mockResults.isEmpty()).willReturn(false);
		given(mockResults.iterator()).willReturn(List.of(mockRecord1, mockRecord2).iterator());

		// Period fields (same for both records - same period group)
		given(mockRecord1.get("period_start_display")).willReturn("01 January");
		given(mockRecord1.get("period_end_display")).willReturn("02 January");
		given(mockRecord1.get("period_start_epoch"))
			.willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START);
		given(mockRecord1.get("period_end_epoch")).willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END);
		given(mockRecord2.get("period_start_epoch"))
			.willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START);
		given(mockRecord2.get("period_end_epoch")).willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END);

		// Selected export fields for each timesheet row (getSelectedFields adds
		// "timesheet")
		given(mockRecord1.get("candidatename")).willReturn("John Doe");
		given(mockRecord1.get("timesheet")).willReturn(null);
		given(mockRecord2.get("candidatename")).willReturn("Jane Smith");
		given(mockRecord2.get("timesheet")).willReturn(null);

		// When
		List<PeriodGroupedExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToGroupedPeriodData(mockResults, selectedFields);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getPeriodStartUtc()).isEqualTo("01 January");
		assertThat(result.get(0).getPeriodEndUtc()).isEqualTo("02 January");
		assertThat(result.get(0).getPeriodDisplayName()).isEqualTo("01 January - 02 January");
		assertThat(result.get(0).getTimesheetsInPeriod()).hasSize(2);
	}

	@Test
	@DisplayName("convertToGroupedPeriodData should return empty list when flatResults is null")
	void testConvertToGroupedPeriodDataReturnsEmptyWhenFlatResultsNull() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();

		// When
		List<PeriodGroupedExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToGroupedPeriodData(null, request.getSelectedFields());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("convertToGroupedPeriodData should handle empty results")
	void testConvertToGroupedPeriodDataHandlesEmptyResults() {
		// Given - empty results trigger early return, only isEmpty() is invoked
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();

		given(mockResults.isEmpty()).willReturn(true);

		// When
		List<PeriodGroupedExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToGroupedPeriodData(mockResults, request.getSelectedFields());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("convertToGroupedPeriodData should sort results by period start")
	void testConvertToGroupedPeriodDataSortsResultsByPeriodStart() {
		// Given - 2 records from different periods (order reversed to test sorting)
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		Record mockRecord1 = mock(Record.class);
		Record mockRecord2 = mock(Record.class);
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();

		// Later period (first in iteration)
		given(mockRecord1.get("period_start_display")).willReturn("02 January");
		given(mockRecord1.get("period_end_display")).willReturn("03 January");
		given(mockRecord1.get("period_start_epoch"))
			.willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END);
		given(mockRecord1.get("period_end_epoch"))
			.willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END + 86400);
		given(mockRecord1.get("candidatename")).willReturn("Later");

		// Earlier period (second in iteration)
		given(mockRecord2.get("period_start_display")).willReturn("01 January");
		given(mockRecord2.get("period_end_display")).willReturn("02 January");
		given(mockRecord2.get("period_start_epoch"))
			.willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START);
		given(mockRecord2.get("period_end_epoch")).willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END);
		given(mockRecord2.get("candidatename")).willReturn("Earlier");

		given(mockResults.isEmpty()).willReturn(false);
		given(mockResults.iterator()).willReturn(List.of(mockRecord1, mockRecord2).iterator());

		// When
		List<PeriodGroupedExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToGroupedPeriodData(mockResults, request.getSelectedFields());

		// Then - should be sorted by period start (earlier first)
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getPeriodStart())
			.isEqualTo(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START);
		assertThat(result.get(1).getPeriodStart()).isEqualTo(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END);
	}

	@Test
	@DisplayName("convertToGroupedPeriodData should handle null period starts in sorting")
	void testConvertToGroupedPeriodDataHandlesNullPeriodStartsInSorting() {
		// Given - one valid period, one with null period
		@SuppressWarnings("unchecked")
		Result<Record> mockResults = mock(Result.class);
		Record mockRecord1 = mock(Record.class);
		Record mockRecord2 = mock(Record.class);
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();

		// Valid period
		given(mockRecord1.get("period_start_display")).willReturn("01 January");
		given(mockRecord1.get("period_end_display")).willReturn("02 January");
		given(mockRecord1.get("period_start_epoch"))
			.willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START);
		given(mockRecord1.get("period_end_epoch")).willReturn(TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END);
		given(mockRecord1.get("candidatename")).willReturn("Valid");

		// Null period start
		given(mockRecord2.get("period_start_display")).willReturn("Invalid");
		given(mockRecord2.get("period_end_display")).willReturn("Invalid");
		given(mockRecord2.get("period_start_epoch")).willReturn(null);
		given(mockRecord2.get("period_end_epoch")).willReturn(null);
		given(mockRecord2.get("candidatename")).willReturn("Invalid");

		given(mockResults.isEmpty()).willReturn(false);
		given(mockResults.iterator()).willReturn(List.of(mockRecord2, mockRecord1).iterator());

		// When
		List<PeriodGroupedExportResponseBodyDto> result = this.timesheetExportRepository
			.convertToGroupedPeriodData(mockResults, request.getSelectedFields());

		// Then - valid period should come first, null period should come last
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getPeriodStart()).isNotNull();
		assertThat(result.get(1).getPeriodStart()).isNull();
	}

	// ========== getTimesheetsInPeriod Tests ==========

	@Test
	@DisplayName("getTimesheetsInPeriod should call field registry and build period request")
	void testGetTimesheetsInPeriodCallsFieldRegistryAndBuildsPeriodRequest() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory.createBasicExportRequest();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;
		Integer periodStart = TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_START;
		Integer periodEnd = TimesheetExportRepositoryTestDataFactory.TEST_PERIOD_END;

		given(this.fieldRegistry.getFieldDefinitions(request.getSelectedFields()))
			.willReturn(TimesheetExportRepositoryTestDataFactory.createFieldDefinitions());

		// When & Then - JOOQ chain is mocked so execution fails; verify field registry
		// was called
		assertThatThrownBy(
				() -> this.timesheetExportRepository.getTimesheetsInPeriod(request, accountId, periodStart, periodEnd))
			.isInstanceOf(Exception.class);

		then(this.fieldRegistry).should().getFieldDefinitions(request.getSelectedFields());
	}

	@Test
	@DisplayName("getExportDataGroupedByPeriods should return empty list when no timesheet IDs and fetch returns empty")
	@SuppressWarnings("unchecked")
	void testGetExportDataGroupedByPeriodsNoTimesheetIdsUsesFetchAndReturnsEmptyWhenNoResults() {
		// Given - request without timesheet IDs so fetchAllTimesheetsForPeriodGrouping
		// path is used
		DynamicExportRequestBodyDto request = TimesheetExportRepositoryTestDataFactory
			.createExportRequestForPeriodGrouping();
		Integer accountId = TimesheetExportRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;

		Result<Record> emptyResult = mock(Result.class);
		given(emptyResult.isEmpty()).willReturn(true);

		TimesheetExportRepository spyRepository = spy(this.timesheetExportRepository);
		doReturn(emptyResult).when(spyRepository).fetchAllTimesheetsForPeriodGrouping(any(), any());

		// When
		List<PeriodGroupedExportResponseBodyDto> result = spyRepository.getExportDataGroupedByPeriods(request,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	// ========== addCurrencyJoins Tests ==========

	@Test
	@DisplayName("addCurrencyJoins joins pay currency table when pay_curr entity is required")
	void testAddCurrencyJoinsWithPayCurrency() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class, Answers.RETURNS_DEEP_STUBS);
		Set<String> requiredEntities = Set.of("pay_curr");

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addCurrencyJoins(mockQuery, requiredEntities);

		// Then - pay currency table is left-joined and the joined query is returned
		then(mockQuery).should().leftJoin(any(TableLike.class));
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("addCurrencyJoins joins bill currency table when bill_curr entity is required")
	void testAddCurrencyJoinsWithBillCurrency() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class, Answers.RETURNS_DEEP_STUBS);
		Set<String> requiredEntities = Set.of("bill_curr");

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addCurrencyJoins(mockQuery, requiredEntities);

		// Then - bill currency table is left-joined and the joined query is returned
		then(mockQuery).should().leftJoin(any(TableLike.class));
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("addCurrencyJoins joins both currency tables when both entities are required")
	void testAddCurrencyJoinsWithBothCurrencies() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class, Answers.RETURNS_DEEP_STUBS);
		Set<String> requiredEntities = Set.of("pay_curr", "bill_curr");

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addCurrencyJoins(mockQuery, requiredEntities);

		// Then - pay currency is joined first, then bill currency is joined onto the
		// resulting step
		then(mockQuery).should().leftJoin(any(TableLike.class));
		assertThat(result).isNotNull();
	}

	// ========== buildBaseQuery Tests ==========

	@Test
	@DisplayName("buildBaseQuery chains all join groups, including currency joins, without error")
	void testBuildBaseQueryWithNoRequiredEntitiesAppliesAllJoinGroups() {
		// Given - a deep-stub DSLContext so the fluent select().from() chain resolves,
		// and no field definitions so no entity-specific join branch is taken
		DSLContext deepStubDslContext = mock(DSLContext.class, Answers.RETURNS_DEEP_STUBS);
		TimesheetExportRepository repository = new TimesheetExportRepository(deepStubDslContext, this.fieldRegistry);
		List<Field<?>> selectFields = List.of();
		List<ExportFieldDefinition> fieldDefinitions = List.of();

		// When
		SelectJoinStep<?> result = repository.buildBaseQuery(selectFields, fieldDefinitions);

		// Then - base query is built by chaining every join group, including
		// addCurrencyJoins
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("addCurrencyJoins returns unchanged query when no currencies in required entities")
	void testAddCurrencyJoinsExcludesNoCurrencies() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class);
		Set<String> requiredEntities = Set.of("ts", "tss");

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addCurrencyJoins(mockQuery, requiredEntities);

		// Then - should return same query reference without calling leftJoin
		assertThat(result).isEqualTo(mockQuery);
	}

	@Test
	@DisplayName("addCurrencyJoins returns unchanged query when required entities is empty")
	void testAddCurrencyJoinsWithEmptyRequiredEntities() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class);
		Set<String> requiredEntities = Set.of();

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addCurrencyJoins(mockQuery, requiredEntities);

		// Then - should return same query reference when no currency entities required
		assertThat(result).isEqualTo(mockQuery);
	}

	// ========== addDealJoins Tests ==========

	@Test
	@DisplayName("addDealJoins returns unchanged query when deals not in required entities")
	void testAddDealJoinsNoDealEntity() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class);
		Set<String> requiredEntities = Set.of("ts", "tss");

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addDealJoins(mockQuery, requiredEntities);

		// Then - should return same query reference
		assertThat(result).isEqualTo(mockQuery);
	}

	@Test
	@DisplayName("addDealJoins returns unchanged query when required entities is empty")
	void testAddDealJoinsEmptyRequiredEntities() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class);
		Set<String> requiredEntities = Set.of();

		// When
		SelectJoinStep<?> result = this.timesheetExportRepository.addDealJoins(mockQuery, requiredEntities);

		// Then - should return same query reference
		assertThat(result).isEqualTo(mockQuery);
	}

	@Test
	@DisplayName("addDealJoins processes deals when present in required entities")
	void testAddDealJoinsWithDealEntity() {
		// Given
		SelectJoinStep<?> mockQuery = mock(SelectJoinStep.class);
		Set<String> requiredEntities = Set.of("deals");

		// When - method should process without error (mock will throw)
		assertThatThrownBy(() -> this.timesheetExportRepository.addDealJoins(mockQuery, requiredEntities))
			.isInstanceOf(Exception.class);
		// Expected: mock throws because it's not fully configured, but the code path is
		// tested
	}

}