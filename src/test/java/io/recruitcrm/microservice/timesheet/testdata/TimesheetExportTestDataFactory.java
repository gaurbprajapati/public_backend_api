package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportResult;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * Test data factory for TimesheetExport-related test objects.
 */
public final class TimesheetExportTestDataFactory {

	private TimesheetExportTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	public static DynamicExportRequestBodyDto createDynamicExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(Arrays.asList("timesheetId", "contractorName", "timesheetPeriod"))
			.candidateFields(Arrays.asList("candidateName", "candidateEmail"))
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	// Basic CSV request (non-grouped)
	public static DynamicExportRequestBodyDto createBasicExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	// Scenario alias for CSV non-grouped export
	public static DynamicExportRequestBodyDto createCsvExportScenario() {
		return createBasicExportRequest();
	}

	// Excel scenario (non-grouped)
	public static DynamicExportRequestBodyDto createExcelExportScenario() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.EXCEL)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	// Excel request (non-grouped)
	public static DynamicExportRequestBodyDto createExcelExportRequest() {
		return createExcelExportScenario();
	}

	// Grouped CSV scenario (exportEachDay=true)
	public static DynamicExportRequestBodyDto createGroupedExportScenario() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(true)
			.build();
	}

	// Grouped CSV request (exportEachDay=true)
	public static DynamicExportRequestBodyDto createGroupedExportRequest() {
		return createGroupedExportScenario();
	}

	// Request with specific timesheet IDs
	public static DynamicExportRequestBodyDto createTimesheetIdsScenario() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.timesheetIds(List.of(101, 102, 103))
			.fileFormat(FileFormat.EXCEL)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	// Request with filters applied
	public static DynamicExportRequestBodyDto createFilteredExportScenario() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.filters(Map.of("status", "APPROVED", "periodStart", "2024-01-01", "periodEnd", "2024-01-31"))
			.build();
	}

	// Request with maximum records (Excel)
	public static DynamicExportRequestBodyDto createExportRequestWithMaxRecords() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("timesheet", "timesheetPeriod"))
			.candidateFields(List.of("candidatename"))
			.fileFormat(FileFormat.EXCEL)
			.maxRecords(50000)
			.exportEachDay(false)
			.build();
	}

	// ===== Response DTOs =====

	public static ExportResult createExportResult() {
		ExportResult result = new ExportResult();
		result.setResource(new ByteArrayResource("test data".getBytes(StandardCharsets.UTF_8)));
		result.setPeriodGrouped(false);
		return result;
	}

	public static ExportResult createPeriodGroupedExportResult() {
		ExportResult result = new ExportResult();
		result.setResource(new ByteArrayResource("test data".getBytes(StandardCharsets.UTF_8)));
		result.setPeriodGrouped(true);
		return result;
	}

	// Basic result (CSV, non-grouped)
	public static ExportResult createBasicExportResult() {
		return new ExportResult(new ByteArrayResource("basic csv".getBytes(StandardCharsets.UTF_8)), "test.csv", 1L,
				false);
	}

	// Excel result (non-grouped)
	public static ExportResult createExcelExportResult() {
		return new ExportResult(new ByteArrayResource("excel bytes".getBytes(StandardCharsets.UTF_8)), "test.xlsx", 1L,
				false);
	}

	// Grouped CSV result (zip)
	public static ExportResult createGroupedExportResult() {
		return new ExportResult(new ByteArrayResource("grouped zip".getBytes(StandardCharsets.UTF_8)), "test.zip", 5L,
				true);
	}

	// Large Excel-like result (non-grouped)
	public static ExportResult createLargeExportResult() {
		byte[] largeContent = "x".repeat(2048).getBytes(StandardCharsets.UTF_8);
		return new ExportResult(new ByteArrayResource(largeContent), "large.xlsx", 10_000L, false);
	}

	// ===== API Response Entities =====

	public static ResponseEntity<Resource> createExportSuccessResponse() {
		Resource resource = new ByteArrayResource("test data".getBytes(StandardCharsets.UTF_8));
		return ResponseEntity.ok()
			.header("Content-Disposition", "attachment; filename=\"Timesheets_01-01-2024_12-00-00.csv\"")
			.header("Content-Type", "text/csv")
			.header("Content-Encoding", "UTF-8")
			.body(resource);
	}

	// ===== Test Constants =====

	public static final class Messages {

		public static final String EXPORT_SUCCESS = "Export completed successfully";

	}

}