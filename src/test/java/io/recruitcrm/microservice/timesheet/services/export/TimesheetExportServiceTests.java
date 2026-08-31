package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportResult;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import io.recruitcrm.microservice.timesheet.dto.export.TimesheetTotalsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.repositories.export.ITimesheetExportRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementExportTestDataFactory;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetExportServiceTestDataFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for TimesheetExportService covering all public methods and private method
 * branches through comprehensive BDD-style testing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetExportService Tests")
class TimesheetExportServiceTests {

	@InjectMocks
	private TimesheetExportService timesheetExportService;

	@Mock
	private ITimesheetExportRepository timesheetExportRepository;

	@Mock
	private ITimeLogRepository timeLogRepository;

	@Mock
	private ExportFieldRegistry fieldRegistry;

	@Mock
	private ITimesheetFileGeneratorService timesheetFileGeneratorService;

	@Mock
	private IReimbursementExportService reimbursementExportService;

	@Mock
	private AuthHolder authHolder;

	@Mock
	private WorkDaysConverter workDaysConverter;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CustomColumnTypeService customColumnTypeService;

	@BeforeEach
	void setUp() {
		// Given
		ReflectionTestUtils.setField(this.timesheetExportService, "applicationEnv", "test");
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	// ===== Validation Tests =====

	@Test
	@DisplayName("Should throw ResourceNotFoundException when timesheetId is missing from timesheetFields")
	void testExportDataWithFilenameMissingTimesheetIdThrowsResourceNotFoundException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createExportRequestMissingTimesheetId();

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.TIMESHEET_ID_MANDATORY);
	}

	@Test
	@DisplayName("Should throw ResourceNotFoundException when candidatename is missing from candidateFields")
	void testExportDataWithFilenameMissingCandidateNameThrowsResourceNotFoundException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createExportRequestMissingCandidateName();

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.CANDIDATE_NAME_MANDATORY);
	}

	@Test
	@DisplayName("Should throw ResourceNotFoundException when timesheetFields is null")
	void testExportDataWithFilenameNullTimesheetFieldsThrowsResourceNotFoundException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createExportRequestNullTimesheetFields();

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.TIMESHEET_ID_MANDATORY);
	}

	@Test
	@DisplayName("Should throw ResourceNotFoundException when candidateFields is null")
	void testExportDataWithFilenameNullCandidateFieldsThrowsResourceNotFoundException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createExportRequestNullCandidateFields();

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.CANDIDATE_NAME_MANDATORY);
	}

	@Test
	@DisplayName("Should throw ResourceNotFoundException when timesheetPeriod is missing from timesheetFields")
	void testExportDataWithFilenameMissingTimesheetPeriodThrowsResourceNotFoundException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createExportRequestMissingTimesheetPeriod();

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.TIMESHEET_PERIOD_MANDATORY);
	}

	// ===== Regular Export Tests =====

	@Test
	@DisplayName("Should return regular export result when exportEachDay is false")
	void testExportDataWithFilenameRegularExportReturnsResult() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getResource()).isEqualTo(resource);
		assertThat(result.getSuggestedFilename()).isEqualTo("export_data");
		assertThat(result.getRecordCount()).isEqualTo(1L);
		assertThat(result.isPeriodGrouped()).isFalse();

		then(this.fieldRegistry).should().validateFields(request.getSelectedFields());
		then(this.timesheetExportRepository).should()
			.getExportData(request, TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Should throw ResourceNotFoundException when regular export returns empty data")
	void testExportDataWithFilenameRegularExportEmptyDataThrowsException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(new ArrayList<>());

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.NO_DATA_FOUND);
	}

	@Test
	@DisplayName("Should use 24-hour format when userId is null")
	void testExportDataWithFilenameUserIdNullUses24HourFormat() {
		// Given
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(null);
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.userRepository).should(never()).getTimeFormatTypeByUserId(any());
	}

	@Test
	@DisplayName("Should default to 24-hour format when getTimeFormatTypeByUserId throws")
	void testExportDataWithFilenameTimeFormatThrowsUses24HourFormat() {
		// Given
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		willThrow(new RuntimeException("User not found")).given(this.userRepository)
			.getTimeFormatTypeByUserId(TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should use 12-hour format when getTimeFormatTypeByUserId returns 0 for period grouped export")
	void testExportDataWithFilenameTimeFormatZeroUses12HourFormat() {
		// Given
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		given(this.userRepository.getTimeFormatTypeByUserId(TimesheetExportServiceTestDataFactory.TEST_USER_ID))
			.willReturn(0);
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedExportResponseWithTimeColumns());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdListForTwo();
		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapForTwoTimesheets();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
		then(this.userRepository).should()
			.getTimeFormatTypeByUserId(TimesheetExportServiceTestDataFactory.TEST_USER_ID);
	}

	@Test
	@DisplayName("Should enhance regular export with all three aggregate totals when selected")
	void testExportDataWithFilenameRegularExportWithAggregateTotals() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createAggregateTotalsExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalOvertime = new HashMap<>();
		totalOvertime.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "8.00");
		Map<Integer, String> totalWork = new HashMap<>();
		totalWork.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "32.00");
		Map<Integer, String> totalHours = new HashMap<>();
		totalHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "40.00");
		Map<Integer, String> totalRegularHours = new HashMap<>();
		totalRegularHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "24.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(totalHours, totalWork,
				totalOvertime, totalRegularHours);

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetExportRepository).should()
			.getTimesheetTotals(timesheetIds, TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Should populate Total Regular Hours and enforce canonical totals column ordering")
	void testExportEnforcesCanonicalTotalsOrderingWithRegularHours() {
		// Given a request selecting all four total columns in a scrambled order
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_OVERTIME_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_TIME_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();

		// Row whose columnOrder has the total columns scattered (overtime first, work/
		// regular/total later)
		DynamicExportResponseBodyDto row = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		row.setData(data);
		row.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_OVERTIME_FIELD,
				TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_TIME_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(row);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalOvertime = new HashMap<>();
		totalOvertime.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "8.00");
		Map<Integer, String> totalWork = new HashMap<>();
		totalWork.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "32.00");
		Map<Integer, String> totalHours = new HashMap<>();
		totalHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "40.00");
		Map<Integer, String> totalRegularHours = new HashMap<>();
		totalRegularHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "24.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(totalHours, totalWork,
				totalOvertime, totalRegularHours);

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		this.timesheetExportService.exportDataWithFilename(request);

		// Then - Total Regular Hours value is populated from total_regular_hour
		assertThat(row.getData()).containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD,
				"24.00");

		// And the totals appear as a contiguous block in canonical order (Work Hours ->
		// Regular Hours -> Overtime Hours -> Total Hours), anchored at the earliest total
		// position; surrounding columns keep their relative order
		assertThat(row.getColumnOrder()).containsExactly(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_OVERTIME_FIELD,
				TimesheetExportServiceTestDataFactory.TOTAL_TIME_FIELD,
				TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD);
	}

	@Test
	@DisplayName("Should handle aggregate totals with empty values defaulting to 0.00")
	void testExportDataWithFilenameAggregateTotalsEmptyValuesDefaultToZero() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createAggregateTotalsExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> emptyTotalOvertime = new HashMap<>();
		emptyTotalOvertime.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "");
		Map<Integer, String> emptyTotalWork = new HashMap<>();
		emptyTotalWork.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "");
		Map<Integer, String> emptyTotalHours = new HashMap<>();
		emptyTotalHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "");
		Map<Integer, String> emptyTotalRegularHours = new HashMap<>();
		emptyTotalRegularHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "");
		TimesheetTotalsQueryResultDto emptyTotalsResult = new TimesheetTotalsQueryResultDto(emptyTotalHours,
				emptyTotalWork, emptyTotalOvertime, emptyTotalRegularHours);

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(emptyTotalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry("totalOvertime", "0.00")
			.containsEntry("totalWorkTime", "0.00")
			.containsEntry("totalTime", "0.00");
	}

	@Test
	@DisplayName("Should skip aggregate population when timesheetId is null in data")
	void testExportDataWithFilenameAggregateTotalsNullTimesheetIdSkipsPopulation() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createAggregateTotalsExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNoTimesheetId());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetExportRepository).should(never()).getTimesheetTotals(anyList(), any());
	}

	// ===== Period Grouped Export Tests =====

	@Test
	@DisplayName("Should return period grouped export result when exportEachDay is true")
	void testExportDataWithFilenamePeriodGroupedExportReturnsResult() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportRequestNoTimeFields();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportResponseList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getSuggestedFilename())
			.isEqualTo(TimesheetExportServiceTestDataFactory.TEST_SUGGESTED_FILENAME);
		assertThat(result.isPeriodGrouped()).isTrue();
	}

	@Test
	@DisplayName("Should throw ResourceNotFoundException when period grouped export returns empty data")
	void testExportDataWithFilenamePeriodGroupedExportEmptyDataThrowsException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportRequestNoTimeFields();

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(new ArrayList<>());

		// When & Then
		assertThatThrownBy(() -> this.timesheetExportService.exportDataWithFilename(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(TimesheetExportServiceTestDataFactory.Messages.NO_DATA_FOUND);
	}

	@Test
	@DisplayName("Should sanitize filename with special characters from period display name")
	void testExportDataWithFilenameSanitizesSpecialCharactersInFilename() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportRequestNoTimeFields();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedExportResponseWithSpecialChars());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getSuggestedFilename()).doesNotContain("<").doesNotContain(">").doesNotContain("\"");
	}

	// ===== Work Hours Expansion Tests =====

	@Test
	@DisplayName("Should expand work hours into separate date columns for period grouped export")
	void testExportDataWithFilenameExpandsWorkHoursIntoColumns() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportResponseWithTimeColumns();
		groupedData.add(period);
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdListForTwo();
		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapForTwoTimesheets();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should()
			.getStructuredTimeLogsForTimesheets(timesheetIds, TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Should skip work hours expansion when period has empty timesheet IDs")
	void testExportDataWithFilenameWorkHoursSkipsEmptyTimesheetIds() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedExportResponseWithEmptyTimesheets());
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedExportResponseWithNullTimesheetIds());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should(never()).getStructuredTimeLogsForTimesheets(anyList(), any());
	}

	@Test
	@DisplayName("Should expand break intervals into columns for period grouped export when breakIntervals selected")
	void testExportDataWithFilenameExpandsBreakIntervalsIntoColumns() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "breakIntervals"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedExportResponseWithTimeColumns());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdListForTwo();
		Map<Integer, Map<String, String>> breakIntervalsMap = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapForTwoTimesheets();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredBreakIntervalsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(breakIntervalsMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should()
			.getStructuredBreakIntervalsForTimesheets(timesheetIds,
					TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Should expand time log remarks into columns for period grouped export when timeLogRemarks selected")
	void testExportDataWithFilenameExpandsRemarksIntoColumns() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "timeLogRemarks"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedExportResponseWithTimeColumns());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdListForTwo();
		Map<Integer, Map<String, String>> remarksMap = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapForTwoTimesheets();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredRemarksForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(remarksMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should()
			.getStructuredRemarksForTimesheets(timesheetIds, TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Should add empty work hours columns for timesheets without time log data and handle null column order")
	void testExportDataWithFilenameWorkHoursAddsEmptyColumnsForMissingData() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		DynamicExportResponseBodyDto tsWithData = TimesheetExportServiceTestDataFactory.createBasicExportResponse();
		DynamicExportResponseBodyDto tsWithoutMatch = TimesheetExportServiceTestDataFactory
			.createExportResponseWithNullColumnOrder();
		timesheets.add(tsWithData);
		timesheets.add(tsWithoutMatch);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		Map<Integer, Map<String, String>> overtimeData = new HashMap<>();
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should handle work hours with non-comma date keys in time log data")
	void testExportDataWithFilenameWorkHoursHandlesNonCommaDateKeys() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		Map<Integer, Map<String, String>> timeLogDataNonComma = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapWithNonCommaKey();
		Map<Integer, Map<String, String>> overtimeData = new HashMap<>();
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogDataNonComma);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	// ===== Effective Work Hours Tests =====

	@Test
	@DisplayName("Should expand effective work hours into separate date columns")
	void testExportDataWithFilenameExpandsEffectiveWorkHours() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createTimeBasedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportResponseWithTimeColumns();
		groupedData.add(period);
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdListForTwo();
		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapForTwoTimesheets();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		Map<Integer, Map<String, String>> effectiveData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timeLogRepository.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(effectiveData);
		TimesheetTotalsQueryResultDto emptyTotals = new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(),
				new HashMap<>(), new HashMap<>());
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(emptyTotals);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should()
			.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
					TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	@Test
	@DisplayName("Should handle effective work hours with empty columns for unmatched timesheets")
	void testExportDataWithFilenameEffectiveWorkHoursEmptyForUnmatchedTimesheets() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.EFFECTIVE_WORK_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		DynamicExportResponseBodyDto tsNoMatch = new DynamicExportResponseBodyDto();
		Map<String, Object> noMatchData = new HashMap<>();
		noMatchData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD, Integer.valueOf(999));
		tsNoMatch.setData(noMatchData);
		tsNoMatch.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(tsNoMatch);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		List<Integer> timesheetIds = List.of(Integer.valueOf(999));
		Map<Integer, Map<String, String>> effectiveData = new HashMap<>();
		Map<String, String> data999 = new HashMap<>();
		data999.put(TimesheetExportServiceTestDataFactory.TEST_DATE_COL_MON, "5.0");
		effectiveData.put(Integer.valueOf(999), data999);
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(effectiveData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	// ===== Post-Processing Tests =====

	@Test
	@DisplayName("Should skip post-processing when no post-processing fields are selected")
	void testExportDataWithFilenameSkipsPostProcessingWhenNoFieldsSelected() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.workDaysConverter).should(never()).convertWorkDaysToNames(any());
		then(this.userRepository).should(never()).getUserDetailsMap(anySet());
	}

	@Test
	@DisplayName("Should apply all post-processing fields including workDays, resource_url, user fields, and custom columns")
	void testExportDataWithFilenameAppliesAllPostProcessing() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPostProcessingExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithPostProcessingFields());
		Map<Integer, UserDetailsQueryResultDto> userDetailsMap = TimesheetExportServiceTestDataFactory
			.createUserDetailsMap();
		Map<Integer, String> fieldTypes = TimesheetExportServiceTestDataFactory.createCustomColumnFieldTypesMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.workDaysConverter.convertWorkDaysToNames(TimesheetExportServiceTestDataFactory.TEST_WORK_DAYS_JSON))
			.willReturn(TimesheetExportServiceTestDataFactory.TEST_WORK_DAYS_CONVERTED);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(userDetailsMap);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypes);
		given(this.customColumnTypeService.convertValue(any(), eq("text"))).willReturn("Converted");
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.WORK_DAYS_FIELD,
					TimesheetExportServiceTestDataFactory.TEST_WORK_DAYS_CONVERTED)
			.hasEntrySatisfying(TimesheetExportServiceTestDataFactory.RESOURCE_URL_FIELD,
					(value) -> assertThat(value.toString()).contains("https://test.recruitcrm.io/v1/candidate/"));

		then(this.workDaysConverter).should()
			.convertWorkDaysToNames(TimesheetExportServiceTestDataFactory.TEST_WORK_DAYS_JSON);
		then(this.userRepository).should(atLeastOnce()).getUserDetailsMap(anySet());
		then(this.customColumnTypeService).should().getFieldTypes(anyList());
	}

	@Test
	@DisplayName("Should handle null workDays value and null/empty resource_url gracefully")
	void testExportDataWithFilenameHandlesNullPostProcessingValues() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPostProcessingExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNullFields());
		Map<Integer, String> fieldTypes = TimesheetExportServiceTestDataFactory.createCustomColumnFieldTypesMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypes);
		given(this.customColumnTypeService.convertValue(any(), eq("text"))).willReturn("Converted");
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.workDaysConverter).should(never()).convertWorkDaysToNames(any());
	}

	@Test
	@DisplayName("Should handle empty resource_url slug value gracefully")
	void testExportDataWithFilenameHandlesEmptyResourceUrlSlug() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.RESOURCE_URL_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithEmptyResourceUrl());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.RESOURCE_URL_FIELD,
				"   ");
	}

	@Test
	@DisplayName("Should build localhost URL when application environment is local")
	void testExportDataWithFilenameBuildsLocalhostUrlForLocalEnv() {
		// Given
		ReflectionTestUtils.setField(this.timesheetExportService, "applicationEnv", "local");
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.RESOURCE_URL_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.RESOURCE_URL_FIELD, "my-slug");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).hasEntrySatisfying(
				TimesheetExportServiceTestDataFactory.RESOURCE_URL_FIELD,
				(value) -> assertThat(value.toString()).startsWith("http://localhost:9000/v1/candidate/my-slug"));
	}

	// ===== User Field Processing Tests =====

	@Test
	@DisplayName("Should transform user IDs to names for ownerid, createdby, and updatedby fields")
	void testExportDataWithFilenameTransformsUserFieldsToNames() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPostProcessingExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithPostProcessingFields());
		Map<Integer, UserDetailsQueryResultDto> userDetailsMap = TimesheetExportServiceTestDataFactory
			.createUserDetailsMap();
		Map<Integer, String> fieldTypes = TimesheetExportServiceTestDataFactory.createCustomColumnFieldTypesMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.workDaysConverter.convertWorkDaysToNames(TimesheetExportServiceTestDataFactory.TEST_WORK_DAYS_JSON))
			.willReturn(TimesheetExportServiceTestDataFactory.TEST_WORK_DAYS_CONVERTED);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(userDetailsMap);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypes);
		given(this.customColumnTypeService.convertValue(any(), eq("text"))).willReturn("Converted");
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_NAME);
	}

	@Test
	@DisplayName("Should return Unknown User when user details have null name")
	void testExportDataWithFilenameReturnsUnknownUserForNullName() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		Map<Integer, UserDetailsQueryResultDto> nullNameMap = TimesheetExportServiceTestDataFactory
			.createUserDetailsMapWithNullName();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(nullNameMap);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				"Unknown User");
	}

	@Test
	@DisplayName("Should handle user repository exception gracefully and return empty map")
	void testExportDataWithFilenameHandlesUserRepositoryException() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.userRepository.getUserDetailsMap(anySet())).willThrow(new RuntimeException("DB error"));
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				"Unknown User");
	}

	@Test
	@DisplayName("Should handle invalid user ID strings and zero/negative user IDs in data")
	void testExportDataWithFilenameHandlesInvalidUserIds() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPostProcessingExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNullFields());
		Map<Integer, String> fieldTypes = TimesheetExportServiceTestDataFactory.createCustomColumnFieldTypesMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypes);
		given(this.customColumnTypeService.convertValue(any(), eq("text"))).willReturn("Converted");
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.CREATED_BY_FIELD,
				"invalid_id");
	}

	@Test
	@DisplayName("Should use cached user details for subsequent rows and skip database call")
	void testExportDataWithFilenameCachesUserDetailsAcrossRows() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		DynamicExportResponseBodyDto row1 = new DynamicExportResponseBodyDto();
		Map<String, Object> data1 = new HashMap<>();
		data1.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data1.put(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		row1.setData(data1);
		row1.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		exportData.add(row1);

		DynamicExportResponseBodyDto row2 = new DynamicExportResponseBodyDto();
		Map<String, Object> data2 = new HashMap<>();
		data2.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID_2);
		data2.put(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		row2.setData(data2);
		row2.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		exportData.add(row2);

		Map<Integer, UserDetailsQueryResultDto> userDetailsMap = TimesheetExportServiceTestDataFactory
			.createUserDetailsMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(userDetailsMap);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_NAME);
		assertThat(exportData.get(1).getData()).containsEntry(TimesheetExportServiceTestDataFactory.OWNER_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_NAME);
	}

	// ===== Custom Column Processing Tests =====

	@Test
	@DisplayName("Should process custom columns and handle invalid column numbers gracefully")
	void testExportDataWithFilenameProcessesCustomColumnsWithInvalidNumbers() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createCustomColumnExportRequest();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD, "Raw Value");
		data.put(TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD_INVALID, "Invalid Col");
		data.put(TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD_OUT_OF_RANGE, "Out of Range");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);

		Map<Integer, String> fieldTypes = TimesheetExportServiceTestDataFactory.createCustomColumnFieldTypesMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypes);
		given(this.customColumnTypeService.convertValue("Raw Value", "text")).willReturn("Converted");
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.customColumnTypeService).should().getFieldTypes(anyList());
	}

	@Test
	@DisplayName("Should skip custom column conversion when field type is null for column")
	void testExportDataWithFilenameSkipsConversionForNullFieldType() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "custcolumn50"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put("custcolumn50", "Some Value");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);

		Map<Integer, String> fieldTypesWithNull = new HashMap<>();
		fieldTypesWithNull.put(Integer.valueOf(50), null);
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypesWithNull);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry("custcolumn50", "Some Value");
		then(this.customColumnTypeService).should(never()).convertValue(any(), any());
	}

	@Test
	@DisplayName("Should skip custom column processing when timesheets data is empty")
	void testExportDataWithFilenameSkipsCustomColumnProcessingForEmptyData() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		Map<Integer, String> fieldTypes = TimesheetExportServiceTestDataFactory.createCustomColumnFieldTypesMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.customColumnTypeService.getFieldTypes(anyList())).willReturn(fieldTypes);
		given(this.customColumnTypeService.convertValue(any(), eq("text"))).willReturn("Converted");
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	// ===== Column Ordering Tests =====

	@Test
	@DisplayName("Should skip column ordering when no time-based fields are selected")
	void testExportDataWithFilenameSkipsColumnOrderingForNoTimeFields() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportRequestNoTimeFields();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportResponseList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should apply column ordering with all time fields and handle date sorting")
	void testExportDataWithFilenameAppliesColumnOrderingWithAllTimeFields() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createTimeBasedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();

		DynamicExportResponseBodyDto ts = new DynamicExportResponseBodyDto();
		Map<String, Object> tsData = new HashMap<>();
		tsData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		tsData.put(TimesheetExportServiceTestDataFactory.WORK_HOURS_FIELD, "40.00");
		tsData.put(TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD, "8.00");
		ts.setData(tsData);
		ts.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
				"contractor", "timesheetPeriod", TimesheetExportServiceTestDataFactory.TEST_WORK_HOURS_COL_MON,
				TimesheetExportServiceTestDataFactory.TEST_OVERTIME_COL_MON,
				TimesheetExportServiceTestDataFactory.TEST_DATE_COL_MON, "totalOvertime", "totalWorkTime")));
		timesheets.add(ts);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		Map<Integer, Map<String, String>> effectiveData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timeLogRepository.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(effectiveData);
		TimesheetTotalsQueryResultDto emptyTotals = new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(),
				new HashMap<>(), new HashMap<>());
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(emptyTotals);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
	}

	@Test
	@DisplayName("Should handle null column order in data during column reordering")
	void testExportDataWithFilenameHandlesNullColumnOrderDuringReordering() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNullColumnOrder());
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	// ===== Date Comparison Edge Case Tests =====

	@Test
	@DisplayName("Should fall back to string comparison when date parsing fails in column sorting")
	void testExportDataWithFilenameFallsBackToStringComparisonForInvalidDates() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();

		DynamicExportResponseBodyDto ts = new DynamicExportResponseBodyDto();
		Map<String, Object> tsData = new HashMap<>();
		tsData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		tsData.put(TimesheetExportServiceTestDataFactory.WORK_HOURS_FIELD, "8.00");
		ts.setData(tsData);
		ts.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(ts);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		Map<Integer, Map<String, String>> timeLogData = new HashMap<>();
		Map<String, String> invalidDateData = new HashMap<>();
		invalidDateData.put("Invalid, Not A Date", "8.00");
		invalidDateData.put("Also Invalid, No Date", "7.00");
		timeLogData.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, invalidDateData);

		Map<Integer, Map<String, String>> overtimeData = new HashMap<>();
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should handle extractUnixTimestamp with no comma in date column")
	void testExportDataWithFilenameHandlesDateColumnsWithoutComma() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createTimeBasedExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();

		DynamicExportResponseBodyDto ts = new DynamicExportResponseBodyDto();
		Map<String, Object> tsData = new HashMap<>();
		tsData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		ts.setData(tsData);
		ts.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_WORK_HOURS_COL_MON,
				TimesheetExportServiceTestDataFactory.TEST_OVERTIME_COL_MON,
				TimesheetExportServiceTestDataFactory.TEST_DATE_COL_MON)));
		timesheets.add(ts);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		Map<Integer, Map<String, String>> effectiveData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timeLogRepository.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(effectiveData);
		TimesheetTotalsQueryResultDto emptyTotals2 = new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(),
				new HashMap<>(), new HashMap<>());
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(emptyTotals2);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
	}

	// ===== Overtime Hours Expansion Tests =====

	@Test
	@DisplayName("Should expand overtime hours into separate date columns for period grouped export")
	void testExportDataWithFilenameExpandsOvertimeHoursIntoColumns() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		DynamicExportResponseBodyDto ts = TimesheetExportServiceTestDataFactory.createBasicExportResponse();
		ts.getData().put(TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD, "8.00");
		timesheets.add(ts);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should()
			.getStructuredOvertimeHoursForTimesheets(timesheetIds,
					TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	// ===== Reimbursement Export Tests =====

	@Test
	@DisplayName("Should skip reimbursement processing when includeReimbursements is false (default)")
	void testExportSkipsReimbursementsWhenFlagIsFalse() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getResource()).isEqualTo(resource);
		then(this.reimbursementExportService).should(never()).buildReimbursementExportRows(anyList(), any(), any());
	}

	@Test
	@DisplayName("Should include reimbursements in Excel when includeReimbursements is true")
	void testExportIncludesReimbursementSheetForExcel() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.EXCEL)
			.exportEachDay(false)
			.maxRecords(1000)
			.includeReimbursements(true)
			.build();

		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<DynamicExportResponseBodyDto> contextData = new ArrayList<>();
		contextData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<ReimbursementExportRowDto> reimbursements = ReimbursementExportTestDataFactory
			.createReimbursementExportRows();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getExportData(any(DynamicExportRequestBodyDto.class),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID)))
			.willReturn(contextData);
		given(this.reimbursementExportService.buildReimbursementExportRows(anyList(),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID), any()))
			.willReturn(reimbursements);
		given(this.timesheetFileGeneratorService.generateExcelWithReimbursements(exportData, request, reimbursements))
			.willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.reimbursementExportService).should()
			.buildReimbursementExportRows(anyList(), eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID), any());
	}

	@Test
	@DisplayName("Should generate CSV ZIP with reimbursements when includeReimbursements is true and format is CSV")
	void testExportGeneratesCsvReimbursementsZip() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.includeReimbursements(true)
			.build();

		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<DynamicExportResponseBodyDto> contextData = new ArrayList<>();
		contextData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<ReimbursementExportRowDto> reimbursements = ReimbursementExportTestDataFactory
			.createReimbursementExportRows();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(any(DynamicExportRequestBodyDto.class),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID)))
			.willReturn(exportData);
		given(this.reimbursementExportService.buildReimbursementExportRows(anyList(),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID), any()))
			.willReturn(reimbursements);
		given(this.timesheetFileGeneratorService.generateCsvWithReimbursementsZip(exportData, request, reimbursements))
			.willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetFileGeneratorService).should()
			.generateCsvWithReimbursementsZip(exportData, request, reimbursements);
	}

	@Test
	@DisplayName("Should add empty overtime columns when timesheet has no overtime data")
	void testExportDataWithFilenameAddsEmptyOvertimeColumnsForNoData() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		DynamicExportResponseBodyDto tsWithData = TimesheetExportServiceTestDataFactory.createBasicExportResponse();
		tsWithData.getData().put(TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD, "8.00");
		timesheets.add(tsWithData);
		DynamicExportResponseBodyDto tsNoData = new DynamicExportResponseBodyDto();
		Map<String, Object> noDataMap = new HashMap<>();
		noDataMap.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD, Integer.valueOf(999));
		noDataMap.put(TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD, "0.00");
		tsNoData.setData(noDataMap);
		tsNoData.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(tsNoData);
		period.setTimesheetsInPeriod(timesheets);
		groupedData.add(period);

		List<Integer> timesheetIds = List.of(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID,
				Integer.valueOf(999));
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should use 24-hour format when getTimeFormatTypeByUserId returns 1 (non-zero)")
	void testExportDataWithFilenameTimeFormatNonZeroUses24HourFormat() {
		// Given
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		given(this.userRepository.getTimeFormatTypeByUserId(TimesheetExportServiceTestDataFactory.TEST_USER_ID))
			.willReturn(1);
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.userRepository).should()
			.getTimeFormatTypeByUserId(TimesheetExportServiceTestDataFactory.TEST_USER_ID);
	}

	@Test
	@DisplayName("Should generate grouped Excel with reimbursements when grouped export and includeReimbursements true")
	void testExportGeneratesGroupedExcelWithReimbursements() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.EXCEL)
			.exportEachDay(true)
			.maxRecords(1000)
			.includeReimbursements(true)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportResponseList();
		List<DynamicExportResponseBodyDto> contextData = new ArrayList<>();
		contextData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<ReimbursementExportRowDto> reimbursements = ReimbursementExportTestDataFactory
			.createReimbursementExportRows();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetExportRepository.getExportData(any(DynamicExportRequestBodyDto.class),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID)))
			.willReturn(contextData);
		given(this.reimbursementExportService.buildReimbursementExportRows(anyList(),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID), any()))
			.willReturn(reimbursements);
		given(this.timesheetFileGeneratorService.generateGroupedExcelWithReimbursements(groupedData, request,
				reimbursements))
			.willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
		then(this.timesheetFileGeneratorService).should()
			.generateGroupedExcelWithReimbursements(groupedData, request, reimbursements);
	}

	@Test
	@DisplayName("Should generate grouped CSV ZIP with reimbursements when grouped export and includeReimbursements true")
	void testExportGeneratesGroupedCsvWithReimbursements() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.includeReimbursements(true)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetExportServiceTestDataFactory
			.createPeriodGroupedExportResponseList();
		List<DynamicExportResponseBodyDto> contextData = new ArrayList<>();
		contextData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<ReimbursementExportRowDto> reimbursements = ReimbursementExportTestDataFactory
			.createReimbursementExportRows();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetExportRepository.getExportData(any(DynamicExportRequestBodyDto.class),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID)))
			.willReturn(contextData);
		given(this.reimbursementExportService.buildReimbursementExportRows(anyList(),
				eq(TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID), any()))
			.willReturn(reimbursements);
		given(this.timesheetFileGeneratorService.generateGroupedCsvWithReimbursementsZip(groupedData, request,
				reimbursements))
			.willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
		then(this.timesheetFileGeneratorService).should()
			.generateGroupedCsvWithReimbursementsZip(groupedData, request, reimbursements);
	}

	@Test
	@DisplayName("Should reorder all time field columns with full precedence when all time fields selected")
	void testExportDataWithFilenameReordersAllTimeFieldColumnsWithFullPrecedence() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createAllTimeFieldsExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(TimesheetExportServiceTestDataFactory.createPeriodGroupedResponseWithAllTimeColumns());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> workHoursMap = TimesheetExportServiceTestDataFactory
			.createSingleTimesheetDateColumnMap();
		Map<Integer, Map<String, String>> overtimeMap = TimesheetExportServiceTestDataFactory
			.createSingleTimesheetDateColumnMap();
		Map<Integer, Map<String, String>> effectiveMap = TimesheetExportServiceTestDataFactory
			.createSingleTimesheetDateColumnMap();
		Map<Integer, Map<String, String>> breakIntervalsMap = TimesheetExportServiceTestDataFactory
			.createSingleTimesheetDateColumnMap();
		Map<Integer, Map<String, String>> remarksMap = TimesheetExportServiceTestDataFactory
			.createSingleTimesheetDateColumnMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(workHoursMap);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeMap);
		given(this.timeLogRepository.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(effectiveMap);
		given(this.timeLogRepository.getStructuredBreakIntervalsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(breakIntervalsMap);
		given(this.timeLogRepository.getStructuredRemarksForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(remarksMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
		then(this.timeLogRepository).should()
			.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
					TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
		then(this.timeLogRepository).should()
			.getStructuredBreakIntervalsForTimesheets(timesheetIds,
					TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
		then(this.timeLogRepository).should()
			.getStructuredRemarksForTimesheets(timesheetIds, TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID);
	}

	// ===== Additional Coverage Tests =====

	@Test
	@DisplayName("Should skip totals column reordering when row columnOrder is null")
	void testExportDataWithFilenameSkipsTotalsReorderingForNullColumnOrder() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createBasicExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNullColumnOrder());
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getColumnOrder()).isNull();
	}

	@Test
	@DisplayName("Should add empty work hours columns for a timesheet entirely absent from the time log map")
	void testExportDataWithFilenameAddsEmptyWorkHoursColumnsForTimesheetMissingFromMap() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createPeriodGroupedExportRequest();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());

		DynamicExportResponseBodyDto tsMissing = new DynamicExportResponseBodyDto();
		Map<String, Object> missingData = new HashMap<>();
		missingData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD, Integer.valueOf(999));
		tsMissing.setData(missingData);
		tsMissing.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(tsMissing);
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = List.of(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID,
				Integer.valueOf(999));
		Map<Integer, Map<String, String>> timeLogData = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(timeLogData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(tsMissing.getData()).containsEntry(TimesheetExportServiceTestDataFactory.TEST_WORK_HOURS_COL_MON, "")
			.containsEntry(TimesheetExportServiceTestDataFactory.TEST_WORK_HOURS_COL_TUE, "");
	}

	@Test
	@DisplayName("Should add empty break intervals columns for a timesheet entirely absent from the map")
	void testExportDataWithFilenameAddsEmptyBreakIntervalsColumnsForTimesheetMissingFromMap() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "breakIntervals"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());

		DynamicExportResponseBodyDto tsMissing = new DynamicExportResponseBodyDto();
		Map<String, Object> missingData = new HashMap<>();
		missingData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD, Integer.valueOf(999));
		tsMissing.setData(missingData);
		tsMissing.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(tsMissing);
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = List.of(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID,
				Integer.valueOf(999));
		Map<Integer, Map<String, String>> breakIntervalsMap = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredBreakIntervalsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(breakIntervalsMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(tsMissing.getData()).containsEntry(TimesheetExportServiceTestDataFactory.TEST_BREAK_COL_MON, "")
			.containsEntry("Tuesday, 07 Jan 2025, Break Intervals", "");
	}

	@Test
	@DisplayName("Should handle break intervals with non-comma date keys")
	void testExportDataWithFilenameBreakIntervalsHandlesNonCommaDateKeys() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "breakIntervals"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> breakIntervalsMap = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapWithNonCommaKey();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredBreakIntervalsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(breakIntervalsMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should add empty remarks columns for a timesheet entirely absent from the map")
	void testExportDataWithFilenameAddsEmptyRemarksColumnsForTimesheetMissingFromMap() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "timeLogRemarks"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());

		DynamicExportResponseBodyDto tsMissing = new DynamicExportResponseBodyDto();
		Map<String, Object> missingData = new HashMap<>();
		missingData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD, Integer.valueOf(999));
		tsMissing.setData(missingData);
		tsMissing.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(tsMissing);
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = List.of(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID,
				Integer.valueOf(999));
		Map<Integer, Map<String, String>> remarksMap = TimesheetExportServiceTestDataFactory.createTimeLogDataMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredRemarksForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(remarksMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(tsMissing.getData()).containsEntry(TimesheetExportServiceTestDataFactory.TEST_REMARKS_COL_MON, "")
			.containsEntry("Tuesday, 07 Jan 2025, Remarks", "");
	}

	@Test
	@DisplayName("Should handle remarks with non-comma date keys")
	void testExportDataWithFilenameRemarksHandlesNonCommaDateKeys() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD, "timeLogRemarks"))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> remarksMap = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapWithNonCommaKey();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredRemarksForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(remarksMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should fill matched overtime columns and blank unmatched ones across timesheets with different date sets")
	void testExportDataWithFilenameOvertimeHoursHandlesPartiallyMatchingDateSets() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();

		DynamicExportResponseBodyDto ts1 = TimesheetExportServiceTestDataFactory.createBasicExportResponse();
		timesheets.add(ts1);

		DynamicExportResponseBodyDto ts2 = new DynamicExportResponseBodyDto();
		Map<String, Object> ts2Data = new HashMap<>();
		ts2Data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID_2);
		ts2.setData(ts2Data);
		ts2.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(ts2);

		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdListForTwo();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapForTwoTimesheets();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(ts2.getData()).containsEntry(TimesheetExportServiceTestDataFactory.TEST_OVERTIME_COL_MON,
				"6.00 hours");
		assertThat(ts2.getData()).containsEntry("Tuesday, 07 Jan 2025, Overtime Hours", "");
	}

	@Test
	@DisplayName("Should handle overtime hours with non-comma date keys")
	void testExportDataWithFilenameOvertimeHoursHandlesNonCommaDateKeys() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.OVERTIME_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> overtimeData = TimesheetExportServiceTestDataFactory
			.createTimeLogDataMapWithNonCommaKey();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(overtimeData);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should skip custom column type lookup when all selected custom columns have invalid IDs")
	void testExportDataWithFilenameSkipsCustomColumnLookupWhenAllColumnIdsInvalid() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD_INVALID,
					TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD_OUT_OF_RANGE))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD_INVALID, "Invalid Col");
		data.put(TimesheetExportServiceTestDataFactory.CUST_COLUMN_FIELD_OUT_OF_RANGE, "Out of Range");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		then(this.customColumnTypeService).should(never()).getFieldTypes(anyList());
	}

	@Test
	@DisplayName("Should expand all time fields skipping null-timesheetId rows within a populated period")
	void testExportDataWithFilenameExpandsAllTimeFieldsWithNullTimesheetIdRow() {
		// Given a period containing one real timesheet row and one row whose data has no
		// timesheet id, with every time-based field selected so each expansion loop must
		// walk the null-id row (exercising the timesheetId != null false branch).
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory.createAllTimeFieldsExportRequest();

		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();

		DynamicExportResponseBodyDto realRow = new DynamicExportResponseBodyDto();
		Map<String, Object> realData = new HashMap<>();
		realData.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		realRow.setData(realData);
		realRow.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		timesheets.add(realRow);

		DynamicExportResponseBodyDto nullIdRow = new DynamicExportResponseBodyDto();
		Map<String, Object> nullIdData = new HashMap<>();
		nullIdData.put(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD, "No Timesheet Candidate");
		nullIdRow.setData(nullIdData);
		nullIdRow.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD)));
		timesheets.add(nullIdRow);

		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		Map<Integer, Map<String, String>> dateColumnMap = TimesheetExportServiceTestDataFactory
			.createSingleTimesheetDateColumnMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timeLogRepository.getStructuredTimeLogsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(dateColumnMap);
		given(this.timeLogRepository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(dateColumnMap);
		given(this.timeLogRepository.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(dateColumnMap);
		given(this.timeLogRepository.getStructuredBreakIntervalsForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(dateColumnMap);
		given(this.timeLogRepository.getStructuredRemarksForTimesheets(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(dateColumnMap);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then the null-id row received empty time columns for every field type
		assertThat(result).isNotNull();
		assertThat(nullIdRow.getData()).containsEntry(TimesheetExportServiceTestDataFactory.TEST_WORK_HOURS_COL_MON, "")
			.containsEntry(TimesheetExportServiceTestDataFactory.TEST_OVERTIME_COL_MON, "")
			.containsEntry(TimesheetExportServiceTestDataFactory.TEST_BREAK_COL_MON, "")
			.containsEntry(TimesheetExportServiceTestDataFactory.TEST_REMARKS_COL_MON, "");
	}

	@Test
	@DisplayName("Should populate only Total Work Hours when it is the sole selected total column")
	void testExportDataWithFilenameOnlyTotalWorkTimeSelected() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalWork = new HashMap<>();
		totalWork.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "32.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(new HashMap<>(), totalWork,
				new HashMap<>(), new HashMap<>());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD, "32.00");
	}

	@Test
	@DisplayName("Should populate only Total Regular Hours when it is the sole selected total column")
	void testExportDataWithFilenameOnlyTotalRegularHoursSelected() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalRegular = new HashMap<>();
		totalRegular.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "24.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(),
				new HashMap<>(), totalRegular);

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD, "24.00");
	}

	@Test
	@DisplayName("Should populate only Total Hours when it is the sole selected total column")
	void testExportDataWithFilenameOnlyTotalTimeSelected() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_TIME_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalHours = new HashMap<>();
		totalHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "40.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(totalHours, new HashMap<>(),
				new HashMap<>(), new HashMap<>());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_TIME_FIELD,
				"40.00");
	}

	@Test
	@DisplayName("Should default Total Regular Hours to 0.00 when the fetched value is empty")
	void testExportDataWithFilenameTotalRegularHoursEmptyValueDefaultsToZero() {
		// Given only totalRegularHours selected with an empty stored value so the
		// value.isEmpty() ? "0.00" branch in processAggregatePart is exercised.
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> emptyRegular = new HashMap<>();
		emptyRegular.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(),
				new HashMap<>(), emptyRegular);

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_REGULAR_HOURS_FIELD, "0.00");
	}

	@Test
	@DisplayName("Should populate only Total Overtime Hours when it is the sole selected total column")
	void testExportDataWithFilenameOnlyTotalOvertimeSelected() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_OVERTIME_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalOvertime = new HashMap<>();
		totalOvertime.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "8.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(),
				totalOvertime, new HashMap<>());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_OVERTIME_FIELD, "8.00");
	}

	@Test
	@DisplayName("Should skip aggregate population for null-timesheetId row while populating the real row (flat export)")
	void testExportDataWithFilenameAggregateSkipsNullIdRowInFlatExport() {
		// Given a flat export containing a real row (so totals are fetched) plus a row
		// with
		// no timesheet id (exercising the timesheetId != null false branch in
		// enhanceWithAggregateTotals).
		DynamicExportRequestBodyDto request = TimesheetExportServiceTestDataFactory
			.createAggregateTotalsExportRequest();
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		exportData.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNoTimesheetId());
		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalOvertime = new HashMap<>();
		totalOvertime.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "8.00");
		Map<Integer, String> totalWork = new HashMap<>();
		totalWork.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "32.00");
		Map<Integer, String> totalHours = new HashMap<>();
		totalHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "40.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(totalHours, totalWork,
				totalOvertime, new HashMap<>());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then the real row is populated; the null-id row is left without total fields
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD, "32.00");
		assertThat(exportData.get(1).getData())
			.doesNotContainKey(TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD);
	}

	@Test
	@DisplayName("Should skip aggregate population for null-timesheetId row in grouped export")
	void testExportDataWithFilenameAggregateSkipsNullIdRowInGroupedExport() {
		// Given a grouped export whose period has a real row plus a null-id row while
		// total
		// columns are selected (exercising the timesheetId != null false branch in
		// enhanceGroupedDataWithAggregateTotalsBatch).
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD,
					TimesheetExportServiceTestDataFactory.TOTAL_TIME_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();

		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName("Test Period");
		period.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(TimesheetExportServiceTestDataFactory.createBasicExportResponse());
		timesheets.add(TimesheetExportServiceTestDataFactory.createExportResponseWithNoTimesheetId());
		period.setTimesheetsInPeriod(timesheets);
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();
		groupedData.add(period);

		List<Integer> timesheetIds = TimesheetExportServiceTestDataFactory.createTimesheetIdList();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		Map<Integer, String> totalWork = new HashMap<>();
		totalWork.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "32.00");
		Map<Integer, String> totalHours = new HashMap<>();
		totalHours.put(TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID, "40.00");
		TimesheetTotalsQueryResultDto totalsResult = new TimesheetTotalsQueryResultDto(totalHours, totalWork,
				new HashMap<>(), new HashMap<>());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportDataGroupedByPeriods(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(groupedData);
		given(this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(totalsResult);
		given(this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isPeriodGrouped()).isTrue();
		assertThat(timesheets.get(0).getData())
			.containsEntry(TimesheetExportServiceTestDataFactory.TOTAL_WORK_TIME_FIELD, "32.00");
	}

	@Test
	@DisplayName("Should transform only createdby user field when it is the sole selected user field")
	void testExportDataWithFilenameOnlyCreatedByUserFieldSelected() {
		// Given a request selecting only createdby (owner short-circuit false branch in
		// determinePostProcessingRequirements, collectUserIds and transformUserFields).
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.CREATED_BY_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.CREATED_BY_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		Map<Integer, UserDetailsQueryResultDto> userDetailsMap = TimesheetExportServiceTestDataFactory
			.createUserDetailsMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(userDetailsMap);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.CREATED_BY_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_NAME);
	}

	@Test
	@DisplayName("Should transform only updatedby user field when it is the sole selected user field")
	void testExportDataWithFilenameOnlyUpdatedByUserFieldSelected() {
		// Given a request selecting only updatedby (owner and createdby short-circuit
		// false
		// branches).
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD,
					TimesheetExportServiceTestDataFactory.TIMESHEET_PERIOD_FIELD,
					TimesheetExportServiceTestDataFactory.UPDATED_BY_FIELD))
			.candidateFields(List.of(TimesheetExportServiceTestDataFactory.CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TimesheetExportServiceTestDataFactory.TIMESHEET_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_TIMESHEET_ID);
		data.put(TimesheetExportServiceTestDataFactory.UPDATED_BY_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_ID);
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TimesheetExportServiceTestDataFactory.TIMESHEET_ID_FIELD)));
		List<DynamicExportResponseBodyDto> exportData = new ArrayList<>();
		exportData.add(response);
		Map<Integer, UserDetailsQueryResultDto> userDetailsMap = TimesheetExportServiceTestDataFactory
			.createUserDetailsMap();
		ByteArrayResource resource = new ByteArrayResource("test".getBytes());

		willDoNothing().given(this.fieldRegistry).validateFields(request.getSelectedFields());
		given(this.timesheetExportRepository.getExportData(request,
				TimesheetExportServiceTestDataFactory.TEST_ACCOUNT_ID))
			.willReturn(exportData);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(userDetailsMap);
		given(this.timesheetFileGeneratorService.generateFile(exportData, request)).willReturn(resource);

		// When
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(exportData.get(0).getData()).containsEntry(TimesheetExportServiceTestDataFactory.UPDATED_BY_FIELD,
				TimesheetExportServiceTestDataFactory.TEST_USER_NAME);
	}

}
