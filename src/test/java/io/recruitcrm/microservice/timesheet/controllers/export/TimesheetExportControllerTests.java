package io.recruitcrm.microservice.timesheet.controllers.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportResult;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.services.export.ITimesheetExportService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetExportTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit test cases for TimesheetExportController following mandatory rule patterns. Tests
 * all public methods with 100% branch coverage using factory-based test data.
 */
@ExtendWith(MockitoExtension.class)
class TimesheetExportControllerTests {

	@Mock
	private ITimesheetExportService timesheetExportService;

	@InjectMocks
	private TimesheetExportController timesheetExportController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	// ==================== CSV Export Tests ====================

	@Test
	@DisplayName("Export dynamic data with CSV format should return CSV file")
	void testExportDynamicDataCsvFormatReturnsCsvFile() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createCsvExportScenario();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockResult.getResource());
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/csv");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment; filename=");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".csv");
		assertThat(response.getHeaders().getFirst("Content-Encoding")).isEqualTo("UTF-8");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Export dynamic data with CSV format and basic request should generate proper filename")
	void testExportDynamicDataCsvFormatGeneratesProperFilename() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createBasicExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertThat(contentDisposition).contains("Timesheets_")
			.contains(".csv")
			.matches(".*Timesheets_\\d{2}-\\d{2}-\\d{4}_\\d{2}-\\d{2}-\\d{2}\\.csv.*");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== Excel Export Tests ====================

	@Test
	@DisplayName("Export dynamic data with Excel format should return Excel file")
	void testExportDynamicDataExcelFormatReturnsExcelFile() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createExcelExportScenario();
		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockResult.getResource());
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".xlsx");
		assertThat(response.getHeaders().getFirst("Content-Encoding")).isEqualTo("UTF-8");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Export dynamic data with Excel format should generate proper filename")
	void testExportDynamicDataExcelFormatGeneratesProperFilename() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createExcelExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertThat(contentDisposition).contains("Timesheets_")
			.contains(".xlsx")
			.matches(".*Timesheets_\\d{2}-\\d{2}-\\d{4}_\\d{2}-\\d{2}-\\d{2}\\.xlsx.*");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== Grouped Export Tests (ZIP) ====================

	@Test
	@DisplayName("Export dynamic data with CSV grouped export should return ZIP file")
	void testExportDynamicDataCsvGroupedExportReturnsZipFile() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createGroupedExportScenario();
		ExportResult mockResult = TimesheetExportTestDataFactory.createGroupedExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockResult.getResource());
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/zip");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".zip");
		assertThat(response.getHeaders().getFirst("Content-Encoding")).isEqualTo("UTF-8");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Export dynamic data with CSV grouped export should generate ZIP filename")
	void testExportDynamicDataCsvGroupedExportGeneratesZipFilename() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createGroupedExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createGroupedExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertThat(contentDisposition).contains("Timesheets_")
			.contains(".zip")
			.matches(".*Timesheets_\\d{2}-\\d{2}-\\d{4}_\\d{2}-\\d{2}-\\d{2}\\.zip.*");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== MIME Type Logic Tests ====================

	@Test
	@DisplayName("Get MIME type for CSV non-grouped export should return CSV MIME type")
	void testGetMimeTypeForCsvNonGroupedExportReturnsCsvMimeType() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createBasicExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/csv");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Get MIME type for Excel export should return Excel MIME type")
	void testGetMimeTypeForExcelExportReturnsExcelMimeType() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createExcelExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Get MIME type for CSV grouped export should return ZIP MIME type")
	void testGetMimeTypeForCsvGroupedExportReturnsZipMimeType() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createGroupedExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createGroupedExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== File Extension Logic Tests ====================

	@Test
	@DisplayName("Get file extension for CSV non-grouped export should return csv extension")
	void testGetFileExtensionForCsvNonGroupedExportReturnsCsvExtension() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createBasicExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertThat(contentDisposition).contains(".csv").doesNotContain(".zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Get file extension for Excel export should return xlsx extension")
	void testGetFileExtensionForExcelExportReturnsXlsxExtension() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createExcelExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertThat(contentDisposition).contains(".xlsx").doesNotContain(".csv").doesNotContain(".zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Get file extension for CSV grouped export should return zip extension")
	void testGetFileExtensionForCsvGroupedExportReturnsZipExtension() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createGroupedExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createGroupedExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertThat(contentDisposition).contains(".zip").doesNotContain(".csv");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== Edge Case Tests ====================

	@Test
	@DisplayName("Export dynamic data with specific timesheet IDs should process correctly")
	void testExportDynamicDataWithSpecificTimesheetIdsProcessesCorrectly() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createTimesheetIdsScenario();
		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockResult.getResource());
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Export dynamic data with filters should process correctly")
	void testExportDynamicDataWithFiltersProcessesCorrectly() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createFilteredExportScenario();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockResult.getResource());
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/csv");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Export dynamic data with maximum records should process correctly")
	void testExportDynamicDataWithMaximumRecordsProcessesCorrectly() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createExportRequestWithMaxRecords();
		ExportResult mockResult = TimesheetExportTestDataFactory.createLargeExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockResult.getResource());
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== Branch Coverage Tests ====================

	@Test
	@DisplayName("Export dynamic data with Excel grouped export should not return ZIP")
	void testExportDynamicDataExcelGroupedExportShouldNotReturnZip() {
		// Given - Excel format with exportEachDay=true should NOT return ZIP
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(TimesheetExportTestDataFactory.createGroupedExportRequest().getTimesheetFields())
			.candidateFields(TimesheetExportTestDataFactory.createGroupedExportRequest().getCandidateFields())
			.fileFormat(FileFormat.EXCEL) // Excel instead of CSV
			.maxRecords(1000)
			.exportEachDay(true) // Grouped export
			.build();

		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then - Should return Excel MIME type, not ZIP
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".xlsx");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).doesNotContain(".zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	// ==================== Reimbursement Flag Tests ====================

	@Test
	@DisplayName("CSV with includeReimbursements=true should return ZIP response")
	void testCsvWithIncludeReimbursementsTrueReturnsZip() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.includeReimbursements(true)
			.build();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/zip");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Excel with includeReimbursements=true should NOT return ZIP")
	void testExcelWithIncludeReimbursementsTrueDoesNotReturnZip() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.EXCEL)
			.maxRecords(1000)
			.exportEachDay(false)
			.includeReimbursements(true)
			.build();
		ExportResult mockResult = TimesheetExportTestDataFactory.createExcelExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".xlsx");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("CSV with includeReimbursements=false (default) should return CSV not ZIP")
	void testCsvWithIncludeReimbursementsFalseReturnsCsv() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createBasicExportRequest();
		ExportResult mockResult = TimesheetExportTestDataFactory.createBasicExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/csv");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".csv");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("CSV grouped with includeReimbursements=true should return ZIP")
	void testCsvGroupedWithIncludeReimbursementsTrueReturnsZip() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(TimesheetExportTestDataFactory.createGroupedExportRequest().getTimesheetFields())
			.candidateFields(TimesheetExportTestDataFactory.createGroupedExportRequest().getCandidateFields())
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(true)
			.includeReimbursements(true)
			.build();
		ExportResult mockResult = TimesheetExportTestDataFactory.createGroupedExportResult();

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/zip");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

	@Test
	@DisplayName("Export dynamic data with CSV non-period-grouped should not return ZIP")
	void testExportDynamicDataCsvNonPeriodGroupedShouldNotReturnZip() {
		// Given - CSV format with exportEachDay=true but NOT period grouped
		DynamicExportRequestBodyDto request = TimesheetExportTestDataFactory.createGroupedExportRequest();
		ExportResult mockResult = new ExportResult(
				TimesheetExportTestDataFactory.createBasicExportResult().getResource(), "test.csv", 1L, false);

		given(this.timesheetExportService.exportDataWithFilename(request)).willReturn(mockResult);

		// When
		ResponseEntity<Resource> response = this.timesheetExportController.exportDynamicData(request);

		// Then - Should return CSV MIME type, not ZIP
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/csv");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".csv");
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).doesNotContain(".zip");

		then(this.timesheetExportService).should().exportDataWithFilename(request);
	}

}