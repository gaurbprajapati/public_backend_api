package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test data factory for TimesheetFileGeneratorService tests
 */
public final class TimesheetFileGeneratorServiceTestDataFactory {

	private TimesheetFileGeneratorServiceTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// Test constants
	public static final String TEST_FIELD_NAME = "candidatename";

	public static final String TEST_DISPLAY_NAME = "Candidate Name";

	public static final String TEST_CANDIDATE_NAME = "John Doe";

	public static final String TEST_TIMESHEET_ID = "TS-123";

	public static final String TEST_PERIOD_DISPLAY_NAME = "Week of Jan 01, 2024";

	public static final String TEST_DATE_COLUMN = "Monday, 01 Jan 2024";

	public static final String TEST_WORK_HOURS_COLUMN = "Monday, 01 Jan 2024, Work Hours";

	public static final String TEST_OVERTIME_COLUMN = "Monday, 01 Jan 2024, Overtime Hours";

	public static final String TEST_CSV_CONTENT = "Candidate Name,Timesheet ID\nJohn Doe,TS-123\n";

	public static final String TEST_SANITIZED_FILENAME = "Week_of_Jan_01__2024";

	public static final String TEST_SANITIZED_SHEET_NAME = "Week of Jan 01, 2024";

	public static final String TEST_INVALID_FILENAME = "Week\\of/Jan*01?2024";

	public static final String TEST_LONG_SHEET_NAME = "This is a very long sheet name that exceeds the Excel limit of 31 characters";

	public static final String TEST_INVALID_SHEET_NAME = "Week[of]Jan*01?2024/\\:";

	/**
	 * Creates a basic CSV export request
	 */
	public static DynamicExportRequestBodyDto createCsvExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	/**
	 * Creates a basic Excel export request
	 */
	public static DynamicExportRequestBodyDto createExcelExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.fileFormat(FileFormat.EXCEL)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	/**
	 * Creates an export request with time-based columns
	 */
	public static DynamicExportRequestBodyDto createTimeBasedExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME, "workhours"))
			.candidateFields(List.of())
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
	}

	/**
	 * Creates a basic export response with simple data
	 */
	public static DynamicExportResponseBodyDto createBasicExportResponse() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();

		// Initialize data map
		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, TEST_CANDIDATE_NAME);
		data.put("timesheet", TEST_TIMESHEET_ID);
		response.setData(data);

		// Set column order
		List<String> columnOrder = new ArrayList<>();
		columnOrder.add(TEST_FIELD_NAME);
		columnOrder.add("timesheet");
		response.setColumnOrder(columnOrder);

		return response;
	}

	/**
	 * Creates an export response with time-based columns
	 */
	public static DynamicExportResponseBodyDto createTimeBasedExportResponse() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();

		// Initialize data map with all fields
		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, TEST_CANDIDATE_NAME);
		data.put("timesheet", TEST_TIMESHEET_ID);
		data.put(TEST_DATE_COLUMN, "08:00");
		data.put(TEST_WORK_HOURS_COLUMN, "07:30");
		data.put(TEST_OVERTIME_COLUMN, "00:30");
		response.setData(data);

		// Set column order with time columns
		List<String> columnOrder = new ArrayList<>();
		columnOrder.add(TEST_FIELD_NAME);
		columnOrder.add(TEST_DATE_COLUMN);
		columnOrder.add(TEST_WORK_HOURS_COLUMN);
		columnOrder.add(TEST_OVERTIME_COLUMN);
		columnOrder.add("timesheet");
		response.setColumnOrder(columnOrder);

		return response;
	}

	/**
	 * Creates an export response with null column order
	 */
	public static DynamicExportResponseBodyDto createExportResponseWithNullColumnOrder() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();

		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, TEST_CANDIDATE_NAME);
		data.put("timesheet", TEST_TIMESHEET_ID);
		response.setData(data);
		response.setColumnOrder(null);
		return response;
	}

	/**
	 * Creates an export response with empty column order
	 */
	public static DynamicExportResponseBodyDto createExportResponseWithEmptyColumnOrder() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();

		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, TEST_CANDIDATE_NAME);
		data.put("timesheet", TEST_TIMESHEET_ID);
		response.setData(data);
		response.setColumnOrder(new ArrayList<>());
		return response;
	}

	/**
	 * Creates a list of export responses
	 */
	public static List<DynamicExportResponseBodyDto> createExportResponseList() {
		List<DynamicExportResponseBodyDto> responses = new ArrayList<>();
		responses.add(createBasicExportResponse());

		// Add second response with different data
		DynamicExportResponseBodyDto response2 = new DynamicExportResponseBodyDto();

		Map<String, Object> data2 = new HashMap<>();
		data2.put(TEST_FIELD_NAME, "Jane Smith");
		data2.put("timesheet", "TS-456");
		response2.setData(data2);

		List<String> columnOrder = new ArrayList<>();
		columnOrder.add(TEST_FIELD_NAME);
		columnOrder.add("timesheet");
		response2.setColumnOrder(columnOrder);

		responses.add(response2);
		return responses;
	}

	/**
	 * Creates an empty list of export responses
	 */
	public static List<DynamicExportResponseBodyDto> createEmptyExportResponseList() {
		return new ArrayList<>();
	}

	/**
	 * Creates a period grouped export response
	 */
	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponse() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(2);
		response.setTimesheetsInPeriod(createExportResponseList());
		return response;
	}

	/**
	 * Creates a period grouped export response with empty timesheets
	 */
	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponseWithEmptyTimesheets() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(0);
		response.setTimesheetsInPeriod(new ArrayList<>());
		return response;
	}

	/**
	 * Creates a list of period grouped export responses
	 */
	public static List<PeriodGroupedExportResponseBodyDto> createPeriodGroupedExportResponseList() {
		List<PeriodGroupedExportResponseBodyDto> responses = new ArrayList<>();
		responses.add(createPeriodGroupedExportResponse());

		// Add second period
		PeriodGroupedExportResponseBodyDto response2 = new PeriodGroupedExportResponseBodyDto();
		response2.setPeriodDisplayName("Week of Jan 08, 2024");
		response2.setTimesheetCount(1);
		response2.setTimesheetsInPeriod(List.of(createBasicExportResponse()));

		responses.add(response2);
		return responses;
	}

	/**
	 * Creates an export field definition
	 */
	public static ExportFieldDefinition createExportFieldDefinition() {
		return ExportFieldDefinition.builder()
			.frontendName(TEST_FIELD_NAME)
			.displayName(TEST_DISPLAY_NAME)
			.requiredEntities(Set.of("c"))
			.javaType(String.class)
			.nullable(true)
			.enabled(true)
			.build();
	}

	/**
	 * Creates a list of export field definitions
	 */
	public static List<ExportFieldDefinition> createExportFieldDefinitionList() {
		List<ExportFieldDefinition> definitions = new ArrayList<>();
		definitions.add(createExportFieldDefinition());

		ExportFieldDefinition definition2 = ExportFieldDefinition.builder()
			.frontendName("timesheet")
			.displayName("Timesheet ID")
			.requiredEntities(Set.of("ts"))
			.javaType(String.class)
			.nullable(true)
			.enabled(true)
			.build();

		definitions.add(definition2);
		return definitions;
	}

	/**
	 * Creates test data with various value types for Excel cell testing
	 */
	public static DynamicExportResponseBodyDto createExportResponseWithVariousValueTypes() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();

		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, TEST_CANDIDATE_NAME);
		data.put("timesheet", TEST_TIMESHEET_ID);
		data.put("numberField", 123.45);
		data.put("booleanField", true);
		data.put("nullField", null);
		data.put("stringField", "Test String");
		response.setData(data);

		List<String> columnOrder = new ArrayList<>();
		columnOrder.add(TEST_FIELD_NAME);
		columnOrder.add("numberField");
		columnOrder.add("booleanField");
		columnOrder.add("nullField");
		columnOrder.add("stringField");
		columnOrder.add("timesheet");
		response.setColumnOrder(columnOrder);

		return response;
	}

	/**
	 * Creates test filename for sanitization testing
	 */
	public static String createTestFilenameForSanitization() {
		return TEST_INVALID_FILENAME;
	}

	/**
	 * Creates test sheet name for sanitization testing
	 */
	public static String createTestSheetNameForSanitization() {
		return TEST_INVALID_SHEET_NAME;
	}

	/**
	 * Creates a long sheet name for length testing
	 */
	public static String createLongSheetName() {
		return TEST_LONG_SHEET_NAME;
	}

	/**
	 * Creates a null filename for testing
	 */
	public static String createNullFilename() {
		return null;
	}

	/**
	 * Creates an empty filename for testing
	 */
	public static String createEmptyFilename() {
		return "";
	}

	/**
	 * Creates a whitespace-only filename for testing
	 */
	public static String createWhitespaceFilename() {
		return "   ";
	}

	/**
	 * Row with an extra column whose value is {@code null} (exercises CSV null-cell
	 * mapping).
	 */
	public static DynamicExportResponseBodyDto createExportResponseWithNullOptionalColumn() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, TEST_CANDIDATE_NAME);
		data.put("optionalNote", null);
		data.put("timesheet", TEST_TIMESHEET_ID);
		response.setData(data);
		List<String> order = new ArrayList<>();
		order.add(TEST_FIELD_NAME);
		order.add("optionalNote");
		order.add("timesheet");
		response.setColumnOrder(order);
		return response;
	}

	/**
	 * Export row with a single non-registry column key (for dynamic header fallback
	 * tests).
	 */
	public static DynamicExportResponseBodyDto createExportResponseWithSingleDynamicColumn(String columnKey,
			Object cellValue) {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(columnKey, cellValue);
		data.put("timesheet", TEST_TIMESHEET_ID);
		response.setData(data);
		List<String> order = new ArrayList<>();
		order.add(columnKey);
		order.add("timesheet");
		response.setColumnOrder(order);
		return response;
	}

	/**
	 * Grouped export using a custom period label (file/sheet naming).
	 */
	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponse(String periodDisplayName,
			List<DynamicExportResponseBodyDto> rows) {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(periodDisplayName);
		response.setTimesheetCount(rows.size());
		response.setTimesheetsInPeriod(rows);
		return response;
	}

	/**
	 * Period with null display name; {@link PeriodGroupedExportResponseBodyDto} falls
	 * back to UTC range labels for naming.
	 */
	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponseWithUtcFallbackLabel() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(null);
		response.setPeriodStartUtc("2024-01-01");
		response.setPeriodEndUtc("2024-01-07");
		response.setTimesheetCount(1);
		response.setTimesheetsInPeriod(List.of(createBasicExportResponse()));
		return response;
	}

}
