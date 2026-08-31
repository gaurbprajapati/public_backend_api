package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test data factory for TimesheetExportRepository tests
 */
public final class TimesheetExportRepositoryTestDataFactory {

	private TimesheetExportRepositoryTestDataFactory() {
	}

	// Test constants
	public static final Integer DEFAULT_ACCOUNT_ID = 1;

	public static final Integer TEST_TIMESHEET_ID = 100;

	public static final Integer TEST_CANDIDATE_ID = 200;

	public static final Integer TEST_JOB_ID = 300;

	public static final Integer TEST_PERIOD_START = 1640995200; // 2022-01-01 00:00:00 UTC

	public static final Integer TEST_PERIOD_END = 1641081600; // 2022-01-02 00:00:00 UTC

	public static final String TEST_FIELD_NAME = "candidatename";

	public static final String TEST_FIELD_ALIAS = "candidate_name";

	public static final String TEST_PERIOD_DISPLAY = "01 January - 02 January";

	public static final String TEST_TIMESHEET_PERIOD = "01/01/2022 - 01/02/2022";

	/**
	 * Creates a basic DynamicExportRequestBodyDto for testing
	 */
	public static DynamicExportRequestBodyDto createBasicExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	/**
	 * Creates an export request with timesheet IDs
	 */
	public static DynamicExportRequestBodyDto createExportRequestWithTimesheetIds() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.timesheetIds(List.of(TEST_TIMESHEET_ID))
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	/**
	 * Creates an export request with filters
	 */
	public static DynamicExportRequestBodyDto createExportRequestWithFilters() {
		Map<String, Object> filters = new HashMap<>();
		filters.put("candidateId", TEST_CANDIDATE_ID);
		filters.put("periodStartAfter", TEST_PERIOD_START);
		filters.put("periodEndBefore", TEST_PERIOD_END);

		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.filters(filters)
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	/**
	 * Creates an export request for period grouping
	 */
	public static DynamicExportRequestBodyDto createExportRequestForPeriodGrouping() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(true)
			.build();
	}

	/**
	 * Creates an export request with unknown filter
	 */
	public static DynamicExportRequestBodyDto createExportRequestWithUnknownFilter() {
		Map<String, Object> filters = new HashMap<>();
		filters.put("unknownFilter", "unknownValue");

		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.filters(filters)
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	/**
	 * Creates a list of ExportFieldDefinition for testing
	 */
	public static List<ExportFieldDefinition> createFieldDefinitions() {
		Field<?> jooqField = DSL.field("CANDIDATE.NAME").as(TEST_FIELD_ALIAS);
		Set<String> requiredEntities = Set.of("c", "tss", "tsa");

		ExportFieldDefinition fieldDefinition = ExportFieldDefinition.builder()
			.frontendName(TEST_FIELD_NAME)
			.displayName("Candidate Name")
			.jooqField(jooqField)
			.requiredEntities(requiredEntities)
			.build();

		return List.of(fieldDefinition);
	}

	/**
	 * Creates field definitions with various entity requirements
	 */
	public static List<ExportFieldDefinition> createFieldDefinitionsWithVariousEntities() {
		List<ExportFieldDefinition> definitions = new ArrayList<>();

		// Candidate field
		definitions.add(ExportFieldDefinition.builder()
			.frontendName("candidatename")
			.displayName("Candidate Name")
			.jooqField(DSL.field("CANDIDATE.NAME").as("candidate_name"))
			.requiredEntities(Set.of("c"))
			.build());

		// Job field
		definitions.add(ExportFieldDefinition.builder()
			.frontendName("jobtitle")
			.displayName("Job Title")
			.jooqField(DSL.field("JOB.TITLE").as("job_title"))
			.requiredEntities(Set.of("j"))
			.build());

		// Deal field
		definitions.add(ExportFieldDefinition.builder()
			.frontendName("dealname")
			.displayName("Deal Name")
			.jooqField(DSL.field("deals.deal_name").as("deal_name"))
			.requiredEntities(Set.of("deals"))
			.build());

		// Approval field
		definitions.add(ExportFieldDefinition.builder()
			.frontendName("approvalstatus")
			.displayName("Approval Status")
			.jooqField(DSL.field("TA.STATUS").as("approval_status"))
			.requiredEntities(Set.of("ta", "tu"))
			.build());

		return definitions;
	}

	/**
	 * Creates a mock JOOQ Result for testing
	 */
	public static Result<Record> createMockJooqResult() {
		// This would typically be mocked in tests since Result is complex to create
		return null; // Will be mocked in actual tests
	}

	/**
	 * Creates a DynamicExportResponseBodyDto for testing
	 */
	public static DynamicExportResponseBodyDto createExportResponseData() {
		Map<String, Object> data = new HashMap<>();
		data.put(TEST_FIELD_NAME, "John Doe");
		data.put("timesheetPeriod", TEST_TIMESHEET_PERIOD);

		return new DynamicExportResponseBodyDto(data, List.of(TEST_FIELD_NAME));
	}

	/**
	 * Creates a list of DynamicExportResponseBodyDto for testing
	 */
	public static List<DynamicExportResponseBodyDto> createExportResponseDataList() {
		List<DynamicExportResponseBodyDto> dataList = new ArrayList<>();

		// First record
		Map<String, Object> data1 = new HashMap<>();
		data1.put(TEST_FIELD_NAME, "John Doe");
		data1.put("timesheetPeriod", TEST_TIMESHEET_PERIOD);
		dataList.add(new DynamicExportResponseBodyDto(data1, List.of(TEST_FIELD_NAME)));

		// Second record
		Map<String, Object> data2 = new HashMap<>();
		data2.put(TEST_FIELD_NAME, "Jane Smith");
		data2.put("timesheetPeriod", TEST_TIMESHEET_PERIOD);
		dataList.add(new DynamicExportResponseBodyDto(data2, List.of(TEST_FIELD_NAME)));

		return dataList;
	}

	/**
	 * Creates a PeriodGroupedExportResponseBodyDto for testing
	 */
	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportData() {
		List<DynamicExportResponseBodyDto> timesheets = createExportResponseDataList();

		return new PeriodGroupedExportResponseBodyDto("01 January", "02 January", TEST_PERIOD_DISPLAY, timesheets,
				timesheets.size(), TEST_PERIOD_START);
	}

	/**
	 * Creates a list of PeriodGroupedExportResponseBodyDto for testing
	 */
	public static List<PeriodGroupedExportResponseBodyDto> createPeriodGroupedExportDataList() {
		return List.of(createPeriodGroupedExportData());
	}

	/**
	 * Creates an export request with null filters
	 */
	public static DynamicExportRequestBodyDto createExportRequestWithNullFilters() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.filters(null)
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	/**
	 * Creates an export request with empty filters
	 */
	public static DynamicExportRequestBodyDto createExportRequestWithEmptyFilters() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TEST_FIELD_NAME))
			.candidateFields(List.of())
			.filters(new HashMap<>())
			.fileFormat(FileFormat.CSV)
			.maxRecords(1000)
			.exportEachDay(false)
			.build();
	}

	/**
	 * Creates filter values with different types for testing
	 */
	public static Map<String, Object> createFiltersWithDifferentTypes() {
		Map<String, Object> filters = new HashMap<>();
		filters.put("candidateId", TEST_CANDIDATE_ID); // Integer
		filters.put("invalidCandidateId", "not_an_integer"); // String (invalid)
		filters.put("periodStartAfter", TEST_PERIOD_START); // Integer
		filters.put("invalidPeriodStart", "not_an_integer"); // String (invalid)
		return filters;
	}

	/**
	 * Creates an export request with invalid date string for parsing
	 */
	public static String createInvalidDateString() {
		return "invalid-date-format";
	}

	/**
	 * Creates a valid date string for parsing
	 */
	public static String createValidDateString() {
		return "01/01/2022";
	}

	/**
	 * Creates an empty date string for parsing
	 */
	public static String createEmptyDateString() {
		return "";
	}

	/**
	 * Creates a null date string for parsing
	 */
	public static String createNullDateString() {
		return null;
	}

}
