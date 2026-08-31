package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test data factory for TimesheetExportService tests. Provides test data for export
 * scenarios including request DTOs, response DTOs, grouped data, and various export
 * configurations.
 */
public final class TimesheetExportServiceTestDataFactory {

	private TimesheetExportServiceTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static final Integer TEST_ACCOUNT_ID = 1;

	public static final Integer TEST_USER_ID = 100;

	public static final Integer TEST_USER_ID_2 = 200;

	public static final Integer TEST_TIMESHEET_ID = 789;

	public static final Integer TEST_TIMESHEET_ID_2 = 790;

	public static final String TIMESHEET_ID_FIELD = "timesheetId";

	public static final String TIMESHEET_FIELD = "timesheet";

	public static final String CANDIDATE_NAME_FIELD = "candidatename";

	public static final String CONTRACTOR_FIELD = "contractor";

	public static final String TIMESHEET_PERIOD_FIELD = "timesheetPeriod";

	public static final String WORK_DAYS_FIELD = "workDays";

	public static final String RESOURCE_URL_FIELD = "resource_url";

	public static final String OWNER_ID_FIELD = "ownerid";

	public static final String CREATED_BY_FIELD = "createdby";

	public static final String UPDATED_BY_FIELD = "updatedby";

	public static final String CUST_COLUMN_FIELD = "custcolumn1";

	public static final String CUST_COLUMN_FIELD_INVALID = "custcolumnabc";

	public static final String CUST_COLUMN_FIELD_OUT_OF_RANGE = "custcolumn0";

	public static final String WORK_HOURS_FIELD = "workHours";

	public static final String OVERTIME_HOURS_FIELD = "overtimeHours";

	public static final String EFFECTIVE_WORK_HOURS_FIELD = "effectiveWorkHours";

	public static final String TOTAL_OVERTIME_FIELD = "totalOvertime";

	public static final String TOTAL_WORK_TIME_FIELD = "totalWorkTime";

	public static final String TOTAL_REGULAR_HOURS_FIELD = "totalRegularHours";

	public static final String TOTAL_TIME_FIELD = "totalTime";

	public static final String TEST_WORK_DAYS_JSON = "[{\"workDayId\":1},{\"workDayId\":2}]";

	public static final String TEST_WORK_DAYS_CONVERTED = "Monday, Tuesday";

	public static final String TEST_RESOURCE_URL_SLUG = "test-candidate-slug";

	public static final String TEST_USER_NAME = "Test User";

	public static final String TEST_PERIOD_DISPLAY_NAME = "1 January - 7 January";

	public static final String TEST_SUGGESTED_FILENAME = "1 January - 7 January";

	public static final String TEST_DATE_COL_MON = "Monday, 06 Jan 2025";

	public static final String TEST_DATE_COL_TUE = "Tuesday, 07 Jan 2025";

	public static final String TEST_WORK_HOURS_COL_MON = "Monday, 06 Jan 2025, Work Hours";

	public static final String TEST_WORK_HOURS_COL_TUE = "Tuesday, 07 Jan 2025, Work Hours";

	public static final String TEST_OVERTIME_COL_MON = "Monday, 06 Jan 2025, Overtime Hours";

	public static final String TEST_OVERTIME_COL_TUE = "Tuesday, 07 Jan 2025, Overtime Hours";

	public static final String TEST_PERIOD_WITH_SPECIAL_CHARS = "1 Jan<>:2025 - 7 Jan\"2025";

	public static DynamicExportRequestBodyDto createBasicExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createExportRequestMissingTimesheetId() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of("contractor"))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createExportRequestMissingCandidateName() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of("email"))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createExportRequestNullTimesheetFields() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(null)
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createExportRequestNullCandidateFields() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD))
			.candidateFields(null)
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	/**
	 * Request with timesheetId and candidatename but missing timesheetPeriod (mandatory).
	 */
	public static DynamicExportRequestBodyDto createExportRequestMissingTimesheetPeriod() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createPeriodGroupedExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(
					List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD, WORK_HOURS_FIELD, OVERTIME_HOURS_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.EXCEL)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createPeriodGroupedExportRequestNoTimeFields() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createTimeBasedExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD, WORK_HOURS_FIELD, OVERTIME_HOURS_FIELD,
					EFFECTIVE_WORK_HOURS_FIELD, TOTAL_OVERTIME_FIELD, TOTAL_WORK_TIME_FIELD, TOTAL_TIME_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createAggregateTotalsExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD, TOTAL_OVERTIME_FIELD,
					TOTAL_WORK_TIME_FIELD, TOTAL_TIME_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createPostProcessingExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD, WORK_DAYS_FIELD, RESOURCE_URL_FIELD,
					OWNER_ID_FIELD, CREATED_BY_FIELD, UPDATED_BY_FIELD, CUST_COLUMN_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportRequestBodyDto createCustomColumnExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, TIMESHEET_PERIOD_FIELD, CUST_COLUMN_FIELD,
					CUST_COLUMN_FIELD_INVALID, CUST_COLUMN_FIELD_OUT_OF_RANGE))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
	}

	public static DynamicExportResponseBodyDto createBasicExportResponse() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		data.put(CANDIDATE_NAME_FIELD, "Test Candidate");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TIMESHEET_ID_FIELD, CANDIDATE_NAME_FIELD)));
		return response;
	}

	public static DynamicExportResponseBodyDto createExportResponseWithPostProcessingFields() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		data.put(WORK_DAYS_FIELD, TEST_WORK_DAYS_JSON);
		data.put(RESOURCE_URL_FIELD, TEST_RESOURCE_URL_SLUG);
		data.put(OWNER_ID_FIELD, TEST_USER_ID);
		data.put(CREATED_BY_FIELD, TEST_USER_ID_2);
		data.put(UPDATED_BY_FIELD, TEST_USER_ID);
		data.put(CUST_COLUMN_FIELD, "Custom Value");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TIMESHEET_ID_FIELD, WORK_DAYS_FIELD, RESOURCE_URL_FIELD,
				OWNER_ID_FIELD, CREATED_BY_FIELD, UPDATED_BY_FIELD, CUST_COLUMN_FIELD)));
		return response;
	}

	public static DynamicExportResponseBodyDto createExportResponseWithNullFields() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		data.put(WORK_DAYS_FIELD, null);
		data.put(RESOURCE_URL_FIELD, null);
		data.put(OWNER_ID_FIELD, null);
		data.put(CREATED_BY_FIELD, "invalid_id");
		data.put(UPDATED_BY_FIELD, Integer.valueOf(0));
		data.put(CUST_COLUMN_FIELD, "Value");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TIMESHEET_ID_FIELD)));
		return response;
	}

	public static DynamicExportResponseBodyDto createExportResponseWithEmptyResourceUrl() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		data.put(RESOURCE_URL_FIELD, "   ");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TIMESHEET_ID_FIELD)));
		return response;
	}

	public static DynamicExportResponseBodyDto createExportResponseWithNoTimesheetId() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(CANDIDATE_NAME_FIELD, "No Timesheet Candidate");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(CANDIDATE_NAME_FIELD)));
		return response;
	}

	public static DynamicExportResponseBodyDto createExportResponseWithTimeColumns() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		data.put(WORK_HOURS_FIELD, "40.00");
		data.put(OVERTIME_HOURS_FIELD, "8.00");
		data.put(EFFECTIVE_WORK_HOURS_FIELD, "48.00");
		response.setData(data);
		response.setColumnOrder(new ArrayList<>(List.of(TIMESHEET_ID_FIELD, CONTRACTOR_FIELD, TIMESHEET_PERIOD_FIELD,
				WORK_HOURS_FIELD, OVERTIME_HOURS_FIELD, EFFECTIVE_WORK_HOURS_FIELD)));
		return response;
	}

	public static DynamicExportResponseBodyDto createExportResponseWithNullColumnOrder() {
		DynamicExportResponseBodyDto response = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		response.setData(data);
		response.setColumnOrder(null);
		return response;
	}

	public static List<DynamicExportResponseBodyDto> createExportResponseList() {
		List<DynamicExportResponseBodyDto> responses = new ArrayList<>();
		responses.add(createBasicExportResponse());
		responses.add(createExportResponseWithPostProcessingFields());
		return responses;
	}

	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponse() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(2);
		response.setTimesheetsInPeriod(createExportResponseList());
		return response;
	}

	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponseWithTimeColumns() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(2);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(createExportResponseWithTimeColumns());
		DynamicExportResponseBodyDto secondResponse = createExportResponseWithTimeColumns();
		secondResponse.getData().put(TIMESHEET_FIELD, TEST_TIMESHEET_ID_2);
		timesheets.add(secondResponse);
		response.setTimesheetsInPeriod(timesheets);
		return response;
	}

	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponseWithSpecialChars() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_WITH_SPECIAL_CHARS);
		response.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(createBasicExportResponse());
		response.setTimesheetsInPeriod(timesheets);
		return response;
	}

	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponseWithEmptyTimesheets() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(0);
		response.setTimesheetsInPeriod(new ArrayList<>());
		return response;
	}

	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedExportResponseWithNullTimesheetIds() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		timesheets.add(createExportResponseWithNoTimesheetId());
		response.setTimesheetsInPeriod(timesheets);
		return response;
	}

	public static List<PeriodGroupedExportResponseBodyDto> createPeriodGroupedExportResponseList() {
		List<PeriodGroupedExportResponseBodyDto> responses = new ArrayList<>();
		responses.add(createPeriodGroupedExportResponse());
		return responses;
	}

	public static Map<Integer, Map<String, String>> createTimeLogDataMap() {
		Map<Integer, Map<String, String>> timeLogMap = new HashMap<>();
		Map<String, String> timeLogData = new HashMap<>();
		timeLogData.put(TEST_DATE_COL_MON, "8.00 hours");
		timeLogData.put(TEST_DATE_COL_TUE, "7.50 hours");
		timeLogMap.put(TEST_TIMESHEET_ID, timeLogData);
		return timeLogMap;
	}

	public static Map<Integer, Map<String, String>> createTimeLogDataMapForTwoTimesheets() {
		Map<Integer, Map<String, String>> timeLogMap = new HashMap<>();
		Map<String, String> timeLogData1 = new HashMap<>();
		timeLogData1.put(TEST_DATE_COL_MON, "8.00 hours");
		timeLogData1.put(TEST_DATE_COL_TUE, "7.50 hours");
		timeLogMap.put(TEST_TIMESHEET_ID, timeLogData1);
		Map<String, String> timeLogData2 = new HashMap<>();
		timeLogData2.put(TEST_DATE_COL_MON, "6.00 hours");
		timeLogMap.put(TEST_TIMESHEET_ID_2, timeLogData2);
		return timeLogMap;
	}

	public static Map<Integer, Map<String, String>> createTimeLogDataMapWithNonCommaKey() {
		Map<Integer, Map<String, String>> timeLogMap = new HashMap<>();
		Map<String, String> timeLogData = new HashMap<>();
		timeLogData.put("Monday", "8.00 hours");
		timeLogMap.put(TEST_TIMESHEET_ID, timeLogData);
		return timeLogMap;
	}

	public static Map<Integer, UserDetailsQueryResultDto> createUserDetailsMap() {
		Map<Integer, UserDetailsQueryResultDto> userMap = new HashMap<>();
		userMap.put(TEST_USER_ID, new UserDetailsQueryResultDto(TEST_USER_NAME, null));
		userMap.put(TEST_USER_ID_2, new UserDetailsQueryResultDto("Second User", null));
		return userMap;
	}

	public static Map<Integer, UserDetailsQueryResultDto> createUserDetailsMapWithNullName() {
		Map<Integer, UserDetailsQueryResultDto> userMap = new HashMap<>();
		userMap.put(TEST_USER_ID, new UserDetailsQueryResultDto(null, null));
		return userMap;
	}

	public static Set<Integer> createUserIdSet() {
		return Set.of(TEST_USER_ID, TEST_USER_ID_2);
	}

	public static List<Integer> createTimesheetIdList() {
		return List.of(TEST_TIMESHEET_ID);
	}

	public static List<Integer> createTimesheetIdListForTwo() {
		return List.of(TEST_TIMESHEET_ID, TEST_TIMESHEET_ID_2);
	}

	public static Map<Integer, String> createCustomColumnFieldTypesMap() {
		Map<Integer, String> fieldTypes = new HashMap<>();
		fieldTypes.put(Integer.valueOf(1), "text");
		return fieldTypes;
	}

	public static final String TEST_BREAK_COL_MON = "Monday, 06 Jan 2025, Break Intervals";

	public static final String TEST_REMARKS_COL_MON = "Monday, 06 Jan 2025, Remarks";

	public static final String TIME_LOG_REMARKS_FIELD = "timeLogRemarks";

	public static final String BREAK_INTERVALS_FIELD = "breakIntervals";

	/**
	 * Request that exports every day with all time-based fields selected so that the full
	 * column ordering precedence (effective, work, break, remarks, overtime) is
	 * exercised.
	 */
	public static DynamicExportRequestBodyDto createAllTimeFieldsExportRequest() {
		return DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TIMESHEET_ID_FIELD, CONTRACTOR_FIELD, TIMESHEET_PERIOD_FIELD, WORK_HOURS_FIELD,
					OVERTIME_HOURS_FIELD, EFFECTIVE_WORK_HOURS_FIELD, BREAK_INTERVALS_FIELD, TIME_LOG_REMARKS_FIELD))
			.candidateFields(List.of(CANDIDATE_NAME_FIELD))
			.fileFormat(FileFormat.CSV)
			.exportEachDay(true)
			.maxRecords(1000)
			.build();
	}

	/**
	 * Period grouped response whose single timesheet already has a fully populated column
	 * order containing date, work-hours, overtime, break and remarks columns for a real
	 * date so the reorder precedence helpers find matching columns.
	 */
	public static PeriodGroupedExportResponseBodyDto createPeriodGroupedResponseWithAllTimeColumns() {
		PeriodGroupedExportResponseBodyDto response = new PeriodGroupedExportResponseBodyDto();
		response.setPeriodDisplayName(TEST_PERIOD_DISPLAY_NAME);
		response.setTimesheetCount(1);
		List<DynamicExportResponseBodyDto> timesheets = new ArrayList<>();
		DynamicExportResponseBodyDto ts = new DynamicExportResponseBodyDto();
		Map<String, Object> data = new HashMap<>();
		data.put(TIMESHEET_FIELD, TEST_TIMESHEET_ID);
		data.put(CONTRACTOR_FIELD, "Test Contractor");
		data.put(TIMESHEET_PERIOD_FIELD, TEST_PERIOD_DISPLAY_NAME);
		data.put(WORK_HOURS_FIELD, "40.00");
		data.put(OVERTIME_HOURS_FIELD, "8.00");
		data.put(EFFECTIVE_WORK_HOURS_FIELD, "48.00");
		ts.setData(data);
		ts.setColumnOrder(new ArrayList<>(List.of(TIMESHEET_ID_FIELD, CONTRACTOR_FIELD, TIMESHEET_PERIOD_FIELD,
				WORK_HOURS_FIELD, OVERTIME_HOURS_FIELD, EFFECTIVE_WORK_HOURS_FIELD, BREAK_INTERVALS_FIELD,
				TIME_LOG_REMARKS_FIELD)));
		timesheets.add(ts);
		response.setTimesheetsInPeriod(timesheets);
		return response;
	}

	/**
	 * Map keyed by date column for the single test timesheet, used for work hours,
	 * overtime, effective, break intervals and remarks expansion.
	 */
	public static Map<Integer, Map<String, String>> createSingleTimesheetDateColumnMap() {
		Map<Integer, Map<String, String>> map = new HashMap<>();
		Map<String, String> dateData = new HashMap<>();
		dateData.put(TEST_DATE_COL_MON, "8.00");
		dateData.put(TEST_DATE_COL_TUE, "7.50");
		map.put(TEST_TIMESHEET_ID, dateData);
		return map;
	}

	public static final class Messages {

		public static final String NO_DATA_FOUND = "No data found for the specified criteria";

		public static final String TIMESHEET_ID_MANDATORY = "timesheetId is a mandatory field for timesheet export";

		public static final String CANDIDATE_NAME_MANDATORY = "candidatename is a mandatory field for timesheet export";

		public static final String TIMESHEET_PERIOD_MANDATORY = "timesheetPeriod is a mandatory field for timesheet export";

		private Messages() {
			throw new UnsupportedOperationException("Utility class");
		}

	}

}
