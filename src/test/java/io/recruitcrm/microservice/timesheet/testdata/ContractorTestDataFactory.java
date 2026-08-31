package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.ContractorTimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.record.FreeSlotsCalculationContext;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test data factory for Contractor Setting-related test objects. Provides factory methods
 * to create consistent test data across all contractor setting tests.
 */
public final class ContractorTestDataFactory {

	// Private constructor to prevent instantiation
	private ContractorTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	/**
	 * Creates a ContractorSearchRequestBodyDto with default test data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto createContractorSearchRequest() {
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto();
	}

	/**
	 * Creates a ContractorSearchRequestBodyDto with advanced search context
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto createContractorSearchRequestWithAdvancedContext() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto();
		request.setAdvancedSearchContext("test-context");
		return request;
	}

	/**
	 * Creates a ContractorSearchRequestBodyDto with filters
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto createContractorSearchRequestWithFilters() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto();
		request.setAdvancedSearchContext("filter-context");
		return request;
	}

	/**
	 * Creates a GetContractorListRequestBodyDto with default test data
	 */
	public static GetContractorListRequestBodyDto createGetContractorListRequest() {
		return new GetContractorListRequestBodyDto(getDefaultJobId(), Arrays.asList(getDefaultContractorId()));
	}

	/**
	 * Creates a GetContractorListRequestBodyDto with custom contractor IDs
	 */
	public static GetContractorListRequestBodyDto createGetContractorListRequest(List<Integer> contractorIds) {
		return new GetContractorListRequestBodyDto(getDefaultJobId(), contractorIds);
	}

	/**
	 * Creates a GetContractorListRequestBodyDto with empty contractor IDs
	 */
	public static GetContractorListRequestBodyDto createGetContractorListRequestWithEmptyContractors() {
		return new GetContractorListRequestBodyDto(getDefaultJobId(), Arrays.asList());
	}

	/**
	 * Creates a GetContractorListRequestBodyDto with multiple contractor IDs
	 */
	public static GetContractorListRequestBodyDto createGetContractorListRequestWithMultipleContractors() {
		return new GetContractorListRequestBodyDto(getDefaultJobId(),
				Arrays.asList(getDefaultContractorId(), getDefaultSecondaryContractorId()));
	}

	/**
	 * Creates an EmptySlotRequestBodyDto with default test data
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequest() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates an EmptySlotRequestBodyDto with custom contractor IDs
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequest(List<Integer> contractorIds) {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate());
		dto.setContractorIds(contractorIds);
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates an EmptySlotRequestBodyDto with empty contractor IDs
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWithEmptyContractors() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate());
		dto.setContractorIds(Arrays.asList());
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates an EmptySlotRequestBodyDto with multiple contractor IDs
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWithMultipleContractors() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId(), getDefaultSecondaryContractorId()));
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates an EmptySlotRequestBodyDto with custom date range
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWithCustomDateRange() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate() + 604800);
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	// ===== Response DTOs =====

	/**
	 * Creates a ContractorTimesheetSettingResponseBodyDto with default test data
	 */
	public static ContractorTimesheetSettingResponseBodyDto createContractorTimesheetSettingResponse() {
		ContractorTimesheetSettingResponseBodyDto dto = new ContractorTimesheetSettingResponseBodyDto();
		dto.setTimesheetSettingId(getDefaultTimesheetSettingId());
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate());
		dto.setTimesheetStartDate(getDefaultTimesheetStartDate());
		dto.setTimesheetFrequency(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates a Map with single contractor timesheet setting response
	 */
	public static Map<Integer, ContractorTimesheetSettingResponseBodyDto> createContractorTimesheetSettingsResponse() {
		Map<Integer, ContractorTimesheetSettingResponseBodyDto> responseMap = new HashMap<>();
		responseMap.put(getDefaultContractorId(), createContractorTimesheetSettingResponse());
		return responseMap;
	}

	/**
	 * Creates an empty Map for contractor timesheet settings response
	 */
	public static Map<Integer, ContractorTimesheetSettingResponseBodyDto> createEmptyContractorTimesheetSettingsResponse() {
		return new HashMap<>();
	}

	/**
	 * Creates a Map with multiple contractor timesheet setting responses
	 */
	public static Map<Integer, ContractorTimesheetSettingResponseBodyDto> createMultipleContractorTimesheetSettingsResponse() {
		Map<Integer, ContractorTimesheetSettingResponseBodyDto> responseMap = new HashMap<>();
		responseMap.put(getDefaultContractorId(), createContractorTimesheetSettingResponse());
		responseMap.put(getDefaultSecondaryContractorId(), createContractorTimesheetSettingResponse());
		return responseMap;
	}

	/**
	 * Creates a TimeSlotsResultBodyDto with default test data
	 */
	public static TimeSlotsResultBodyDto createSingleTimeSlotsResult() {
		TimeSlotsResultBodyDto dto = new TimeSlotsResultBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate());
		return dto;
	}

	/**
	 * Creates a List with single TimeSlotsResultBodyDto
	 */
	public static List<TimeSlotsResultBodyDto> createTimeSlotsResult() {
		return Arrays.asList(createSingleTimeSlotsResult());
	}

	/**
	 * Creates an empty List of TimeSlotsResultBodyDto
	 */
	public static List<TimeSlotsResultBodyDto> createEmptyTimeSlotsResult() {
		return Arrays.asList();
	}

	/**
	 * Creates a List with multiple TimeSlotsResultBodyDto
	 */
	public static List<TimeSlotsResultBodyDto> createMultipleTimeSlotsResult() {
		return Arrays.asList(createSingleTimeSlotsResult(), createSingleTimeSlotsResult());
	}

	/**
	 * Creates a List with custom date range TimeSlotsResultBodyDto
	 */
	public static List<TimeSlotsResultBodyDto> createCustomDateRangeTimeSlotsResult() {
		TimeSlotsResultBodyDto dto = new TimeSlotsResultBodyDto();
		dto.setStartDate(getDefaultStartDate());
		dto.setEndDate(getDefaultEndDate() + 604800);
		return Arrays.asList(dto);
	}

	// ===== Entity Objects =====

	/**
	 * Creates an AssignCandidateJob entity with default test data
	 */
	public static AssignCandidateJob createAssignCandidateJob() {
		AssignCandidateJob assignment = new AssignCandidateJob();
		assignment.setId(getDefaultAssignmentId());
		assignment.setJobId(getDefaultJobId());
		assignment.setCandidateId(getDefaultContractorId());
		assignment.setAccountId(getDefaultAccountId());
		return assignment;
	}

	/**
	 * Creates an AssignCandidateJob entity with custom parameters
	 */
	public static AssignCandidateJob createAssignCandidateJob(Integer jobId, Integer candidateId, Integer accountId) {
		AssignCandidateJob assignment = new AssignCandidateJob();
		assignment.setId(getDefaultAssignmentId());
		assignment.setJobId(jobId);
		assignment.setCandidateId(candidateId);
		assignment.setAccountId(accountId);
		return assignment;
	}

	/**
	 * Creates a TimesheetSetting entity with default test data
	 */
	public static TimesheetSetting createTimesheetSetting() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setJobStartDate(getDefaultStartDate());
		setting.setJobEndDate(getDefaultEndDate());
		setting.setTimesheetStartDay(getDefaultTimesheetStartDate());
		setting.setTimesheetFrequency(getDefaultTimesheetFrequencyId());
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	/**
	 * Creates a TimesheetSetting entity with custom parameters
	 */
	public static TimesheetSetting createTimesheetSetting(Integer jobId, Integer candidateId) {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId() + jobId + candidateId); // Use
																				// parameters
																				// to make
																				// unique
		setting.setJobStartDate(getDefaultStartDate());
		setting.setJobEndDate(getDefaultEndDate());
		setting.setTimesheetStartDay(getDefaultTimesheetStartDate() + (candidateId % 7)); // Vary
																							// timesheet
																							// start
																							// day
		setting.setTimesheetFrequency(getDefaultTimesheetFrequencyId() + (jobId % 3)); // Vary
																						// frequency
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	/**
	 * Creates a TimesheetSetting with association for bulk free slots tests. The
	 * association links the setting to a specific contractor-job pair.
	 */
	public static TimesheetSetting createTimesheetSettingWithAssociation(Integer contractorId, Integer jobId) {
		TimesheetSetting setting = createTimesheetSetting();
		TimesheetSettingAssociation association = new TimesheetSettingAssociation();
		association.setContractorId(contractorId);
		association.setJobId(jobId);
		setting.setAssociation(association);
		return setting;
	}

	/**
	 * Creates an OccupiedSlotsQueryResultDto with default test data
	 */
	public static OccupiedSlotsQueryResultDto createOccupiedSlotsQueryResult() {
		OccupiedSlotsQueryResultDto dto = new OccupiedSlotsQueryResultDto();
		dto.setPeriodStart(getDefaultStartDate());
		dto.setPeriodEnd(getDefaultEndDate());
		return dto;
	}

	/**
	 * Creates a List with single OccupiedSlotsQueryResultDto
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsQueryResults() {
		return Arrays.asList(createOccupiedSlotsQueryResult());
	}

	/**
	 * Creates an empty List of OccupiedSlotsQueryResultDto
	 */
	public static List<OccupiedSlotsQueryResultDto> createEmptyOccupiedSlotsQueryResults() {
		return Collections.emptyList();
	}

	// ===== API Response Entities =====

	/**
	 * Creates a successful ResponseEntity for Long count
	 * @param count The Long count value
	 * @return ResponseEntity with APINormalResponse containing the count
	 */
	public static ResponseEntity<APINormalResponse<Long>> createLongCountSuccessResponse(Long count) {
		APINormalResponse<Long> response = new APINormalResponse<>(count);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Creates a success ResponseEntity for contractor timesheet settings
	 */
	public static ResponseEntity<APINormalResponse<Map<Integer, ContractorTimesheetSettingResponseBodyDto>>> createContractorSettingsSuccessResponse(
			Map<Integer, ContractorTimesheetSettingResponseBodyDto> data) {
		APINormalResponse<Map<Integer, ContractorTimesheetSettingResponseBodyDto>> response = new APINormalResponse<>(
				data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Creates a success ResponseEntity for time slots
	 */
	public static ResponseEntity<APINormalResponse<List<TimeSlotsResultBodyDto>>> createTimeSlotsSuccessResponse(
			List<TimeSlotsResultBodyDto> data) {
		APINormalResponse<List<TimeSlotsResultBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Test IDs and Constants =====

	/**
	 * Default job ID for test data
	 */
	public static Integer getDefaultJobId() {
		return 1;
	}

	/**
	 * Default contractor ID for test data
	 */
	public static Integer getDefaultContractorId() {
		return 123;
	}

	/**
	 * Default secondary contractor ID for test data
	 */
	public static Integer getDefaultSecondaryContractorId() {
		return 456;
	}

	/**
	 * Default contact ID for test data (used in CONTACT persona tests)
	 */
	public static Integer getDefaultContactId() {
		return 1;
	}

	/**
	 * Default timesheet setting ID for test data
	 */
	public static Integer getDefaultTimesheetSettingId() {
		return 1;
	}

	/**
	 * Default start date for test data (epoch timestamp) Uses a shorter date range (1
	 * week) for faster test execution
	 */
	public static Integer getDefaultStartDate() {
		return 1704067200; // 2024-01-01 00:00:00 UTC
	}

	/**
	 * Default end date for test data (epoch timestamp) Uses a shorter date range (1 week)
	 * for faster test execution
	 */
	public static Integer getDefaultEndDate() {
		return 1704671999; // 2024-01-07 23:59:59 UTC (end of 2024-01-07)
	}

	/**
	 * Default timesheet start date for test data
	 */
	public static Integer getDefaultTimesheetStartDate() {
		return 1640995200; // 2022-01-01
	}

	/**
	 * Default timesheet frequency ID for test data
	 */
	public static Integer getDefaultTimesheetFrequencyId() {
		return 1; // Weekly
	}

	/**
	 * Default timesheet start day for test data
	 */
	public static Integer getDefaultTimesheetStartDay() {
		return 1; // Monday
	}

	/**
	 * Default assignment ID for test data
	 */
	public static Integer getDefaultAssignmentId() {
		return 1;
	}

	/**
	 * Default account ID for test data
	 */
	public static Integer getDefaultAccountId() {
		return 1;
	}

	// ===== Additional Test Data Methods for Private Method Testing =====

	/**
	 * Creates timesheet frequency IDs for comprehensive testing
	 */
	public static Integer getWeeklyFrequencyId() {
		return 2;
	}

	public static Integer getBiweeklyFrequencyId() {
		return 3;
	}

	public static Integer getMonthlyFrequencyId() {
		return 4;
	}

	/**
	 * Creates test dates for comprehensive slot testing
	 */
	public static Integer getTestStartDate() {
		return 1640995200; // 2022-01-01 00:00:00 UTC (Saturday)
	}

	public static Integer getTestEndDate() {
		return 1643673600; // 2022-02-01 00:00:00 UTC (Tuesday)
	}

	public static Integer getTestMidDate() {
		return 1642204800; // 2022-01-15 00:00:00 UTC (Saturday)
	}

	public static Integer getLeapYearTestDate() {
		return 1582934400; // 2020-02-29 00:00:00 UTC (leap year)
	}

	public static Integer getLastDayOfMonthTestDate() {
		return 1643587200; // 2022-01-31 00:00:00 UTC (Monday)
	}

	public static Integer getTimesheetStartDayMonday() {
		return 1;
	}

	public static Integer getTimesheetStartDayTuesday() {
		return 2;
	}

	public static Integer getTimesheetStartDayWednesday() {
		return 3;
	}

	public static Integer getTimesheetStartDayThursday() {
		return 4;
	}

	public static Integer getTimesheetStartDayFriday() {
		return 5;
	}

	public static Integer getTimesheetStartDaySaturday() {
		return 6;
	}

	public static Integer getTimesheetStartDaySunday() {
		return 7;
	}

	public static Integer getTimesheetStartDayLastDayOfMonth() {
		return 100;
	}

	/**
	 * Creates EmptySlotRequestBodyDto for weekly frequency testing
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeekly() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getTestStartDate());
		dto.setEndDate(getTestEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setTimesheetStartDay(getTimesheetStartDayMonday());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates EmptySlotRequestBodyDto for biweekly frequency testing
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestBiweekly() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getTestStartDate());
		dto.setEndDate(getTestEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setTimesheetStartDay(getTimesheetStartDayTuesday());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates EmptySlotRequestBodyDto for monthly frequency testing
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthly() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getTestStartDate());
		dto.setEndDate(getTestEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setTimesheetStartDay(getTimesheetStartDayWednesday());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates EmptySlotRequestBodyDto for monthly frequency with last day of month
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyLastDay() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getTestStartDate());
		dto.setEndDate(getTestEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setTimesheetStartDay(getTimesheetStartDayLastDayOfMonth());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates EmptySlotRequestBodyDto with overlapping occupied slots
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWithOverlappingSlots() {
		EmptySlotRequestBodyDto dto = new EmptySlotRequestBodyDto();
		dto.setStartDate(getTestStartDate());
		dto.setEndDate(getTestEndDate());
		dto.setContractorIds(Arrays.asList(getDefaultContractorId(), getDefaultSecondaryContractorId()));
		dto.setTimesheetStartDay(getTimesheetStartDayMonday());
		dto.setJobId(getDefaultJobId());
		dto.setTimesheetFrequencyId(getDefaultTimesheetFrequencyId());
		return dto;
	}

	/**
	 * Creates occupied slots with overlapping periods
	 */
	public static List<OccupiedSlotsQueryResultDto> createOverlappingOccupiedSlotsQueryResults() {
		// First slot: Jan 5-10, 2022
		OccupiedSlotsQueryResultDto slot1 = new OccupiedSlotsQueryResultDto();
		slot1.setPeriodStart(1641340800); // 2022-01-05 00:00:00 UTC
		slot1.setPeriodEnd(1641772800); // 2022-01-10 00:00:00 UTC

		// Second slot: Jan 8-15, 2022 (overlaps with first)
		OccupiedSlotsQueryResultDto slot2 = new OccupiedSlotsQueryResultDto();
		slot2.setPeriodStart(1641600000); // 2022-01-08 00:00:00 UTC
		slot2.setPeriodEnd(1642204800); // 2022-01-15 00:00:00 UTC

		return Arrays.asList(slot1, slot2);
	}

	/**
	 * Creates occupied slots with gaps for partial slot testing
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsWithGaps() {
		// First slot: Jan 10-15, 2022
		OccupiedSlotsQueryResultDto slot1 = new OccupiedSlotsQueryResultDto();
		slot1.setPeriodStart(1641772800); // 2022-01-10 00:00:00 UTC
		slot1.setPeriodEnd(1642204800); // 2022-01-15 00:00:00 UTC

		// Second slot: Jan 25-30, 2022 (gap between slots)
		OccupiedSlotsQueryResultDto slot2 = new OccupiedSlotsQueryResultDto();
		slot2.setPeriodStart(1643068800); // 2022-01-25 00:00:00 UTC
		slot2.setPeriodEnd(1643500800); // 2022-01-30 00:00:00 UTC

		return Arrays.asList(slot1, slot2);
	}

	/**
	 * Creates occupied slots that match start and end dates
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsMatchingBoundaries() {
		// Slot that starts exactly at global start
		OccupiedSlotsQueryResultDto slot1 = new OccupiedSlotsQueryResultDto();
		slot1.setPeriodStart(getTestStartDate());
		slot1.setPeriodEnd(1641340800); // 2022-01-05 00:00:00 UTC

		// Slot that ends exactly at global end
		OccupiedSlotsQueryResultDto slot2 = new OccupiedSlotsQueryResultDto();
		slot2.setPeriodStart(1643500800); // 2022-01-30 00:00:00 UTC
		slot2.setPeriodEnd(getTestEndDate());

		return Arrays.asList(slot1, slot2);
	}

	/**
	 * Creates TimesheetSetting with weekly frequency
	 */
	public static TimesheetSetting createTimesheetSettingWeekly() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setJobStartDate(getTestStartDate());
		setting.setJobEndDate(getTestEndDate());
		setting.setTimesheetStartDay(getTimesheetStartDayMonday());
		setting.setTimesheetFrequency(getWeeklyFrequencyId());
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	/**
	 * Creates TimesheetSetting with biweekly frequency
	 */
	public static TimesheetSetting createTimesheetSettingBiweekly() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setJobStartDate(getTestStartDate());
		setting.setJobEndDate(getTestEndDate());
		setting.setTimesheetStartDay(getTimesheetStartDayTuesday());
		setting.setTimesheetFrequency(getBiweeklyFrequencyId());
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	/**
	 * Creates TimesheetSetting with monthly frequency
	 */
	public static TimesheetSetting createTimesheetSettingMonthly() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setJobStartDate(getTestStartDate());
		setting.setJobEndDate(getTestEndDate());
		setting.setTimesheetStartDay(getTimesheetStartDayWednesday());
		setting.setTimesheetFrequency(getMonthlyFrequencyId());
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	/**
	 * Creates TimesheetSetting with monthly frequency and last day of month
	 */
	public static TimesheetSetting createTimesheetSettingMonthlyLastDay() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setJobStartDate(getTestStartDate());
		setting.setJobEndDate(getTestEndDate());
		setting.setTimesheetStartDay(getTimesheetStartDayLastDayOfMonth());
		setting.setTimesheetFrequency(getMonthlyFrequencyId());
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	/**
	 * Creates TimeSlotsResultBodyDto with custom date range
	 */
	public static TimeSlotsResultBodyDto createTimeSlotsResultWithCustomDates(Integer startDate, Integer endDate) {
		TimeSlotsResultBodyDto dto = new TimeSlotsResultBodyDto();
		dto.setStartDate(startDate);
		dto.setEndDate(endDate);
		return dto;
	}

	// ===== Monthly Ending Partial Slot Test Data =====

	/**
	 * Creates empty slot request for monthly ending partial slot with large gap (>= 1
	 * month)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForEndingPartialSlot() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1677628800); // 2023-03-01 00:00:00 UTC (2 months later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay());
		return request;
	}

	/**
	 * Creates occupied slots that will trigger monthly ending partial slot with large gap
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyEndingPartialSlot() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1672531200); // 2023-01-01 00:00:00 UTC
		slot.setPeriodEnd(1672617599); // 2023-01-01 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly ending partial slot with small gap (< 1
	 * month)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForEndingPartialSlotSmallGap() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1673827200); // 2023-01-16 00:00:00 UTC (15 days later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay());
		return request;
	}

	/**
	 * Creates occupied slots that will trigger monthly ending partial slot with small gap
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyEndingPartialSlotSmallGap() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1672531200); // 2023-01-01 00:00:00 UTC
		slot.setPeriodEnd(1673740799); // 2023-01-14 23:59:59 UTC (14 days - small gap
										// scenario)
		return Arrays.asList(slot);
	}

	// ===== Weekly/Biweekly Ending Partial Slot Test Data =====

	/**
	 * Creates empty slot request for weekly ending partial slot that will have valid
	 * partial slot
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeeklyForEndingPartialSlot() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (Sunday)
		request.setEndDate(1674345600); // 2023-01-22 00:00:00 UTC (3 weeks later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay()); // Monday
		return request;
	}

	/**
	 * Creates occupied slots that will trigger weekly ending partial slot with valid slot
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForWeeklyEndingPartialSlot() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1672531200); // 2023-01-01 00:00:00 UTC
		slot.setPeriodEnd(1673135999); // 2023-01-07 23:59:59 UTC (1 week)
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for biweekly ending partial slot that will have valid
	 * partial slot
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestBiweeklyForEndingPartialSlot() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (Sunday)
		request.setEndDate(1675555200); // 2023-02-05 00:00:00 UTC (35 days later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay()); // Monday
		return request;
	}

	/**
	 * Creates occupied slots that will trigger biweekly ending partial slot with valid
	 * slot
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForBiweeklyEndingPartialSlot() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1672531200); // 2023-01-01 00:00:00 UTC
		slot.setPeriodEnd(1674345599); // 2023-01-21 23:59:59 UTC (3 weeks - biweekly
										// ending partial slot)
		return Arrays.asList(slot);
	}

	// ===== Monthly Starting Partial Slot Test Data =====

	/**
	 * Creates empty slot request for monthly starting partial slot with large gap (>= 1
	 * month)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForStartingPartialSlot() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1677628800); // 2023-03-01 00:00:00 UTC (2 months later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getTimesheetStartDayFriday()); // Different day for
																	// starting partial
																	// slot
		request.setJobId(getDefaultJobId() + 2); // Different job ID
		request.setTimesheetFrequencyId(getMonthlyFrequencyId()); // Set monthly frequency
																	// explicitly
		return request;
	}

	/**
	 * Creates occupied slots that will trigger monthly starting partial slot with large
	 * gap
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyStartingPartialSlot() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1675209600); // 2023-02-01 00:00:00 UTC (1 month after start)
		slot.setPeriodEnd(1675295999); // 2023-02-01 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly starting partial slot with small gap (< 1
	 * month)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForStartingPartialSlotSmallGap() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1673827200); // 2023-01-16 00:00:00 UTC (15 days later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId(), getDefaultSecondaryContractorId())); // Multiple
																												// contractors
																												// for
																												// starting
																												// partial
																												// slot
		request.setTimesheetStartDay(getTimesheetStartDayWednesday()); // Different day
																		// for starting
																		// partial slot
		request.setJobId(getDefaultJobId() + 1); // Different job ID
		request.setTimesheetFrequencyId(getMonthlyFrequencyId()); // Set monthly frequency
																	// explicitly
		return request;
	}

	/**
	 * Creates occupied slots that will trigger monthly starting partial slot with small
	 * gap
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyStartingPartialSlotSmallGap() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1673136000); // 2023-01-08 00:00:00 UTC (7 days after start)
		slot.setPeriodEnd(1673222399); // 2023-01-08 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly partial slot that requires month adjustment
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForPartialSlotWithMonthAdjustment() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1677628800); // 2023-03-01 00:00:00 UTC (2 months later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(6); // 15th day of month
		return request;
	}

	/**
	 * Creates occupied slots that will trigger monthly partial slot with month adjustment
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyPartialSlotWithMonthAdjustment() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1674777600); // 2023-01-27 00:00:00 UTC (after 15th, requiring
		// month adjustment)
		slot.setPeriodEnd(1674863999); // 2023-01-27 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly complex boundary conditions
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForComplexBoundaryConditions() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1677628800); // 2023-03-01 00:00:00 UTC (2 months later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(5); // 5th day of month
		return request;
	}

	/**
	 * Creates occupied slots that will trigger monthly complex boundary conditions
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyComplexBoundaryConditions() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1675555200); // 2023-02-05 00:00:00 UTC (exactly on 5th day)
		slot.setPeriodEnd(1675641599); // 2023-02-05 23:59:59 UTC
		return Arrays.asList(slot);
	}

	// ===== Empty Free Slots and Empty Occupied Slots Test Data =====

	/**
	 * Creates empty slot request for weekly frequency with empty free slots and empty
	 * occupied slots
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeeklyForEmptySlots() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (Sunday)
		request.setEndDate(1673136000); // 2023-01-08 00:00:00 UTC (1 week later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay()); // Monday
		return request;
	}

	/**
	 * Creates empty slot request for biweekly frequency with empty free slots and empty
	 * occupied slots
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestBiweeklyForEmptySlots() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (Sunday)
		request.setEndDate(1673740800); // 2023-01-15 00:00:00 UTC (2 weeks later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay()); // Monday
		return request;
	}

	/**
	 * Creates empty slot request for monthly frequency with empty free slots and empty
	 * occupied slots
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForEmptySlots() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1675209600); // 2023-02-01 00:00:00 UTC (1 month later)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay());
		return request;
	}

	/**
	 * Creates empty slot request for weekly frequency with empty slots and
	 * endMatchedSlots true
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeeklyForEmptySlotsWithEndMatched() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (Sunday)
		request.setEndDate(1673135999); // 2023-01-07 23:59:59 UTC (end of first week)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay()); // Monday
		return request;
	}

	/**
	 * Creates empty slot request for biweekly frequency with empty slots and
	 * endMatchedSlots true
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestBiweeklyForEmptySlotsWithEndMatched() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (Sunday)
		request.setEndDate(1673740799); // 2023-01-14 23:59:59 UTC (end of second week)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay()); // Monday
		return request;
	}

	/**
	 * Creates empty slot request for monthly frequency with empty slots and
	 * endMatchedSlots true
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForEmptySlotsWithEndMatched() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC
		request.setEndDate(1675123199); // 2023-01-31 23:59:59 UTC (end of January)
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getDefaultTimesheetStartDay());
		return request;
	}

	// ===== Test Data for calculateLastDayOfMonth Method Coverage =====

	/**
	 * Creates empty slot request for monthly frequency with 31-day month (January)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyFor31DayMonth() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1672531200); // 2023-01-01 00:00:00 UTC (January)
		request.setEndDate(1675209600); // 2023-02-01 00:00:00 UTC
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(100); // Use 100 to trigger last day calculation
		return request;
	}

	/**
	 * Creates occupied slots for monthly frequency with 31-day month
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthly31DayMonth() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1672531200); // 2023-01-01 00:00:00 UTC
		slot.setPeriodEnd(1675123199); // 2023-01-31 23:59:59 UTC - spans entire 31-day
										// month
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly frequency with 30-day month (April)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyFor30DayMonth() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1680307200); // 2023-04-01 00:00:00 UTC (April)
		request.setEndDate(1682899200); // 2023-05-01 00:00:00 UTC
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(100); // Use 100 to trigger last day calculation
		return request;
	}

	/**
	 * Creates occupied slots for monthly frequency with 30-day month
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthly30DayMonth() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1680307200); // 2023-04-01 00:00:00 UTC
		slot.setPeriodEnd(1680387199); // 2023-04-01 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly frequency with February leap year (2024)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForFebruaryLeapYear() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1706745600); // 2024-02-01 00:00:00 UTC (February 2024 - leap
											// year)
		request.setEndDate(1709251200); // 2024-03-01 00:00:00 UTC
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(100); // Use 100 to trigger last day calculation
		return request;
	}

	/**
	 * Creates occupied slots for monthly frequency with February leap year
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyFebruaryLeapYear() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1706745600); // 2024-02-01 00:00:00 UTC
		slot.setPeriodEnd(1706825599); // 2024-02-01 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly frequency with February non-leap year (2023)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForFebruaryNonLeapYear() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(1675209600); // 2023-02-01 00:00:00 UTC (February 2023 -
											// non-leap year)
		request.setEndDate(1677628800); // 2023-03-01 00:00:00 UTC
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(100); // Use 100 to trigger last day calculation
		return request;
	}

	/**
	 * Creates occupied slots for monthly frequency with February non-leap year
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyFebruaryNonLeapYear() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(1675209600); // 2023-02-01 00:00:00 UTC
		slot.setPeriodEnd(1675289599); // 2023-02-01 23:59:59 UTC
		return Arrays.asList(slot);
	}

	/**
	 * Creates empty slot request for monthly frequency with February century year (2100 -
	 * non-leap)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForFebruaryCenturyYear() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate((int) 4102444800L); // 2100-02-01 00:00:00 UTC (February 2100
													// - century year, not leap)
		request.setEndDate((int) 4104864000L); // 2100-03-01 00:00:00 UTC
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(100); // Use 100 to trigger last day calculation
		return request;
	}

	/**
	 * Creates occupied slots for monthly frequency with February century year
	 */
	/**
	 * Creates empty slot request for monthly frequency with February century leap year
	 * (2000)
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestMonthlyForFebruaryCenturyLeapYear() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(949363200); // 2000-02-01 00:00:00 UTC (February 2000 -
											// century leap year)
		request.setEndDate(951782400); // 2000-03-01 00:00:00 UTC
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(100); // Use 100 to trigger last day calculation
		return request;
	}

	/**
	 * Creates occupied slots for monthly frequency with February century leap year
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForMonthlyFebruaryCenturyLeapYear() {
		OccupiedSlotsQueryResultDto slot = new OccupiedSlotsQueryResultDto();
		slot.setPeriodStart(949363200); // 2000-02-01 00:00:00 UTC
		slot.setPeriodEnd(949443199); // 2000-02-01 23:59:59 UTC
		return Arrays.asList(slot);
	}

	// ===== Test Data for calculateWeeklyBiweeklyPartialSlotEndDate Branch Coverage =====

	/**
	 * Creates an empty slot request for weekly frequency that triggers currentDate not
	 * after startDateTime
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeeklyForCurrentDateNotAfterStartDateTime() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		// Use a start date that will cause the calculated end date to be equal to start
		// date
		request.setStartDate(getTestStartDateForCurrentDateNotAfterStartDateTime());
		request.setEndDate(getTestEndDateForCurrentDateNotAfterStartDateTime());
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getTimesheetStartDayMonday());
		return request;
	}

	/**
	 * Creates an empty slot request for biweekly frequency that triggers currentDate not
	 * after startDateTime
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestBiweeklyForCurrentDateNotAfterStartDateTime() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		// Use a start date that will cause the calculated end date to be equal to start
		// date for biweekly frequency
		request.setStartDate(getTestStartDateForCurrentDateNotAfterStartDateTime());
		request.setEndDate(getTestEndDateForCurrentDateNotAfterStartDateTime());
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getTimesheetStartDayTuesday()); // Different day for
																		// biweekly
		request.setTimesheetFrequencyId(getBiweeklyFrequencyId()); // Set biweekly
																	// frequency
		return request;
	}

	/**
	 * Creates occupied slots for the currentDate not after startDateTime scenario
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForCurrentDateNotAfterStartDateTime() {
		// Create occupied slots that will force the calculation to a point where
		// currentDate equals startDateTime
		// but is still less than upperBound
		OccupiedSlotsQueryResultDto slot1 = new OccupiedSlotsQueryResultDto();
		slot1.setPeriodStart(getTestStartDateForCurrentDateNotAfterStartDateTime() + 86400); // Start
																								// +
																								// 1
																								// day
		slot1.setPeriodEnd(getTestStartDateForCurrentDateNotAfterStartDateTime() + 172800); // Start
																							// +
																							// 2
																							// days
		return Arrays.asList(slot1);
	}

	/**
	 * Gets test start date that will trigger currentDate not after startDateTime branch
	 * This creates a scenario where the target end day calculation results in currentDate
	 * equal to startDateTime
	 */
	public static Integer getTestStartDateForCurrentDateNotAfterStartDateTime() {
		// Use a date that will cause the calculated end date to be equal to start date
		// Start date: 2024-01-07 00:00:00 UTC (Sunday)
		// Timesheet start day: Monday (1)
		// Target end day: Sunday (7)
		// Since start date is already Sunday, the calculation will find the same date
		// This creates a scenario where currentDate equals startDateTime
		return 1704672000; // 2024-01-07 00:00:00 UTC (Sunday)
	}

	/**
	 * Gets test end date for currentDate not after startDateTime scenario
	 */
	public static Integer getTestEndDateForCurrentDateNotAfterStartDateTime() {
		return 1704758400; // 2024-01-08 00:00:00 UTC (Monday)
	}

	/**
	 * Creates occupied slots whose merged boundaries touch without a gap between them.
	 */
	public static List<List<Integer>> createAdjacentOccupiedSlotsForMergedGapTest() {
		return Arrays.asList(Arrays.asList(1640995200, 1641600000), Arrays.asList(1641600000, 1642204800));
	}

	public static int getAdjacentMergedGapTestGlobalStart() {
		return 1640908800;
	}

	public static int getAdjacentMergedGapTestGlobalEnd() {
		return 1643673600;
	}

	public static int getBiweeklyPartialSlotEndDatePlusWeeksStart() {
		return getTestStartDateForCurrentDateNotAfterStartDateTime();
	}

	public static int getBiweeklyPartialSlotEndDatePlusWeeksUpperBound() {
		return (int) java.time.LocalDateTime.of(2024, 1, 25, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static FreeSlotsCalculationContext createBiweeklyPartialSlotEndDatePlusWeeksContext(int start,
			int upperBound) {
		return new FreeSlotsCalculationContext(start, upperBound, getTimesheetStartDayTuesday(),
				getBiweeklyFrequencyId(), List.of(), Boolean.TRUE, Boolean.TRUE);
	}

	public static Integer getBiweeklyPartialSlotEndDatePlusWeeksExpectedEnd() {
		return 1705276800;
	}

	public static int getMonthlyStartingPartialSlotEqualStartDayGlobalStart() {
		return (int) java.time.LocalDateTime.of(2026, 1, 1, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getMonthlyStartingPartialSlotEqualStartDayOccupiedStart() {
		return (int) java.time.LocalDateTime.of(2026, 3, 15, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getMonthlyStartingPartialSlotSubMonthGapGlobalStart() {
		return (int) java.time.LocalDateTime.of(2026, 3, 25, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getMonthlyStartingPartialSlotSubMonthGapSlotStart() {
		return (int) java.time.LocalDateTime.of(2026, 4, 5, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getWeeklyStartingPartialSlotZeroAlignedEndGlobalStart() {
		return (int) java.time.LocalDateTime.of(2026, 5, 1, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getWeeklyStartingPartialSlotZeroAlignedEndUpperBound() {
		return (int) java.time.LocalDateTime.of(2026, 5, 4, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getWeeklyStartingPartialSlotSmallGapZeroAlignedEndGlobalStart() {
		return (int) java.time.LocalDateTime.of(2026, 5, 10, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	public static int getWeeklyStartingPartialSlotSmallGapZeroAlignedEndSlotStart() {
		return (int) java.time.LocalDateTime.of(2026, 5, 12, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC);
	}

	// ===== Test Data for calculateWeeklyBiweeklyEndingPartialSlot Branch Coverage =====

	/**
	 * Creates an empty slot request for weekly frequency that triggers isValidPartialSlot
	 * true case
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeeklyForEndingPartialSlotValidSlot() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		// Use dates that will create a gap > 7 days between occupied slots and global end
		request.setStartDate(getTestStartDateForEndingPartialSlotValidSlot());
		request.setEndDate(getTestEndDateForEndingPartialSlotValidSlot());
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getTimesheetStartDayMonday());
		return request;
	}

	/**
	 * Creates an empty slot request for biweekly frequency that triggers
	 * isValidPartialSlot true case
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestBiweeklyForEndingPartialSlotValidSlot() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		// Use dates that will create a gap > 14 days between occupied slots and global
		// end for biweekly frequency
		request.setStartDate(getTestStartDateForEndingPartialSlotValidSlot());
		request.setEndDate(getTestEndDateForEndingPartialSlotValidSlot());
		request.setContractorIds(Arrays.asList(getDefaultContractorId(), getDefaultSecondaryContractorId())); // Multiple
																												// contractors
																												// for
																												// biweekly
		request.setTimesheetStartDay(getTimesheetStartDayTuesday()); // Different day for
																		// biweekly
		request.setTimesheetFrequencyId(getBiweeklyFrequencyId()); // Set biweekly
																	// frequency
		return request;
	}

	/**
	 * Creates occupied slots for the ending partial slot valid slot scenario
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForEndingPartialSlotValidSlot() {
		// Create occupied slots that end well before the global end, creating a large gap
		OccupiedSlotsQueryResultDto slot1 = new OccupiedSlotsQueryResultDto();
		slot1.setPeriodStart(getTestStartDateForEndingPartialSlotValidSlot());
		slot1.setPeriodEnd(getTestOccupiedSlotEndForEndingPartialSlotValidSlot());
		return Arrays.asList(slot1);
	}

	/**
	 * Gets test start date for ending partial slot valid slot scenario
	 */
	public static Integer getTestStartDateForEndingPartialSlotValidSlot() {
		return 1640995200; // 2022-01-01 00:00:00 UTC (Saturday)
	}

	/**
	 * Gets test end date for ending partial slot valid slot scenario
	 */
	public static Integer getTestEndDateForEndingPartialSlotValidSlot() {
		return 1643673600; // 2022-02-01 00:00:00 UTC (Tuesday)
	}

	/**
	 * Gets test occupied slot end for ending partial slot valid slot scenario
	 */
	public static Integer getTestOccupiedSlotEndForEndingPartialSlotValidSlot() {
		return 1641081600; // 2022-01-02 00:00:00 UTC (Sunday) - creates large gap to
							// global end
	}

	// ===== Test Data for determineSevenDaySlotCurrentStart Branch Coverage =====

	/**
	 * Creates an empty slot request for weekly frequency that triggers adjustedStart >
	 * firstStartDayEpoch
	 */
	public static EmptySlotRequestBodyDto createEmptySlotRequestWeeklyForAdjustedStartAfterFirstStartDay() {
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		// Use a start date that is after the first start day in the week
		request.setStartDate(getTestStartDateForAdjustedStartAfterFirstStartDay());
		request.setEndDate(getTestEndDateForAdjustedStartAfterFirstStartDay());
		request.setContractorIds(Arrays.asList(getDefaultContractorId()));
		request.setTimesheetStartDay(getTimesheetStartDayMonday());
		return request;
	}

	/**
	 * Creates occupied slots for the adjusted start after first start day scenario
	 */
	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlotsForAdjustedStartAfterFirstStartDay() {
		// Create empty occupied slots to trigger the weekly slot calculation
		return new ArrayList<>();
	}

	/**
	 * Gets test start date for adjusted start after first start day scenario
	 */
	public static Integer getTestStartDateForAdjustedStartAfterFirstStartDay() {
		// Use a date that is after the first Monday of the week
		// Start: 2022-01-05 00:00:00 UTC (Wednesday) - after Monday (first start day)
		return 1641340800; // 2022-01-05 00:00:00 UTC (Wednesday)
	}

	/**
	 * Gets test end date for adjusted start after first start day scenario
	 */
	public static Integer getTestEndDateForAdjustedStartAfterFirstStartDay() {
		return 1643673600; // 2022-02-01 00:00:00 UTC (Tuesday)
	}

	/**
	 * Default contractor count for test data
	 */
	public static Long getDefaultContractorCount() {
		return 10L;
	}

	/**
	 * Zero contractor count for test data
	 */
	public static Long getZeroContractorCount() {
		return 0L;
	}

	/**
	 * Large contractor count for test data
	 */
	public static Long getLargeContractorCount() {
		return 1000L;
	}

	/**
	 * Single contractor count for test data
	 */
	public static Long getSingleContractorCount() {
		return 1L;
	}

	/**
	 * Small contractor count for test data
	 */
	public static Long getSmallContractorCount() {
		return 5L;
	}

	// ===== Pagination Request DTOs =====

	/**
	 * Creates a PaginationRequestBodyDto with default test data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto createPaginationRequest() {
		return new io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto(1, 10);
	}

	/**
	 * Creates a PaginationRequestBodyDto with custom page and size
	 */
	public static io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto createPaginationRequest(
			Integer page, Integer size) {
		return new io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto(page, size);
	}

	// ===== Contractor List Response DTOs =====

	/**
	 * Creates a ContractorListResponseBodyDto with default test data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto createContractorListResponse() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto contractor = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto();
		contractor.setId(getDefaultContractorId());
		contractor.setName("Test Contractor");
		contractor.setEmail("test@contractor.com");
		contractor.setPhone("+1234567890");
		contractor.setStatus(1);
		return contractor;
	}

	/**
	 * Creates a List of ContractorListResponseBodyDto with default test data
	 */
	public static List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> createContractorListResponseList() {
		return Arrays.asList(createContractorListResponse(), createSecondContractorListResponse());
	}

	/**
	 * Creates a second ContractorListResponseBodyDto with different data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto createSecondContractorListResponse() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto contractor = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto();
		contractor.setId(getDefaultSecondaryContractorId());
		contractor.setName("Second Test Contractor");
		contractor.setEmail("second@contractor.com");
		contractor.setPhone("+0987654321");
		contractor.setStatus(1);
		return contractor;
	}

	/**
	 * Creates an empty List of ContractorListResponseBodyDto
	 */
	public static List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> createEmptyContractorListResponseList() {
		return Arrays.asList();
	}

	// ===== Contractor Query Result DTOs =====

	/**
	 * Creates a ContractorQueryResultDto with default test data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto createContractorQueryResultDto() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto dto = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto();
		dto.setId(getDefaultContractorId());
		dto.setName("Test Contractor");
		dto.setEmail("test@contractor.com");
		return dto;
	}

	/**
	 * Creates a second ContractorQueryResultDto with different contractor id
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto createSecondContractorQueryResultDto() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto dto = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto();
		dto.setId(getDefaultSecondaryContractorId());
		dto.setName("Second Test Contractor");
		dto.setEmail("second@contractor.com");
		return dto;
	}

	/**
	 * Creates a List of ContractorQueryResultDto with default test data
	 */
	public static List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto> createContractorQueryResultDtoList() {
		return Arrays.asList(createContractorQueryResultDto(), createSecondContractorQueryResultDto());
	}

	/**
	 * Creates a ContractorJobQueryResultDto with active job date range relative to now
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto createActiveContractorJobQueryResultDto(
			Integer contractorId) {
		int currentEpoch = (int) java.time.Instant.now().getEpochSecond();
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto(contractorId,
				getDefaultJobId(), "Active Job", "active-job", "Test Company", "test-company", currentEpoch - 86400,
				currentEpoch + 86400, "Open", Integer.valueOf(1));
	}

	/**
	 * Creates a ContractorJobQueryResultDto with null job dates
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto createContractorJobQueryResultDtoWithNullDates(
			Integer contractorId) {
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto(contractorId,
				getDefaultJobId(), "Null Date Job", "null-date-job", "Test Company", "test-company", null, null, "Open",
				Integer.valueOf(3));
	}

	/**
	 * Creates a ContractorJobQueryResultDto with only null job end date
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto createContractorJobQueryResultDtoWithNullEndDate(
			Integer contractorId) {
		int currentEpoch = (int) java.time.Instant.now().getEpochSecond();
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto(contractorId,
				getDefaultJobId(), "Null End Date Job", "null-end-job", "Test Company", "test-company",
				currentEpoch - 86400, null, "Open", Integer.valueOf(4));
	}

	/**
	 * Creates a ContractorJobQueryResultDto with job end date in the past
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto createExpiredContractorJobQueryResultDto(
			Integer contractorId) {
		int currentEpoch = (int) java.time.Instant.now().getEpochSecond();
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto(contractorId,
				getDefaultJobId(), "Expired Job", "expired-job", "Test Company", "test-company", currentEpoch - 172800,
				currentEpoch - 86400, "Closed", Integer.valueOf(5));
	}

	/**
	 * Creates a ContractorJobQueryResultDto with inactive job date range relative to now
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto createInactiveContractorJobQueryResultDto(
			Integer contractorId) {
		int currentEpoch = (int) java.time.Instant.now().getEpochSecond();
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto(contractorId,
				getDefaultJobId(), "Inactive Job", "inactive-job", "Test Company", "test-company", currentEpoch + 86400,
				currentEpoch + 172800, "Closed", Integer.valueOf(2));
	}

	/**
	 * Creates a ContractorDealQueryResultDto with default test data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto createContractorDealQueryResultDto(
			Integer contractorId) {
		return new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto(contractorId,
				Integer.valueOf(10), "Test Deal", "Deal Owner", Integer.valueOf(100), "test-deal", "Open");
	}

	/**
	 * Creates a ContractorSearchRequestBodyDto without filter (null filterSearchList)
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto createContractorSearchRequestWithoutFilter() {
		return createContractorSearchRequest();
	}

	/**
	 * Creates a ContractorSearchRequestBodyDto with filter search list
	 */
	public static io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto createContractorSearchRequestWithFilterSearchList() {
		io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto();
		request.setFilterSearchList(TimesheetSearchTestDataFactory.createFilterSearchListDto());
		return request;
	}

	// ===== API Response Entities =====

	/**
	 * Creates a successful ResponseEntity for List of ContractorListResponseBodyDto
	 */
	public static ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> createContractorListSuccessResponse(
			List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> contractors) {
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> response = new APINormalResponse<>(
				contractors);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Bulk Empty Slot Request DTOs =====

	/**
	 * Creates a ContractorJobPairDto with default test data
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto createContractorJobPair() {
		return new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(getDefaultContractorId(),
				getDefaultJobId());
	}

	/**
	 * Creates a ContractorJobPairDto with custom parameters
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto createContractorJobPair(
			Integer contractorId, Integer jobId) {
		return new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(contractorId, jobId);
	}

	/**
	 * Creates a BulkEmptySlotRequestBodyDto with default test data Uses a smaller date
	 * range (1 week) for faster test execution
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto createBulkEmptySlotRequest() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto dto = new io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto();
		dto.setContractorJobPairs(Arrays.asList(createContractorJobPair()));
		// Use a smaller date range (1 week) for faster test execution
		Integer startDate = getDefaultStartDate();
		dto.setMaxJobStartDate(startDate);
		dto.setMinJobEndDate(startDate + (7 * 24 * 60 * 60)); // 1 week later
		return dto;
	}

	/**
	 * Creates a BulkEmptySlotRequestBodyDto with multiple contractor-job pairs Uses a
	 * shorter date range (1 week) for faster test execution
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto createBulkEmptySlotRequestWithMultiplePairs() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto dto = new io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto();
		dto.setContractorJobPairs(Arrays.asList(createContractorJobPair(),
				createContractorJobPair(getDefaultSecondaryContractorId(), getDefaultJobId())));
		// Use a smaller date range (1 week) for faster test execution
		Integer startDate = getDefaultStartDate();
		dto.setMaxJobStartDate(startDate);
		dto.setMinJobEndDate(startDate + (7 * 24 * 60 * 60)); // 1 week later
		return dto;
	}

	/**
	 * Creates a BulkEmptySlotRequestBodyDto with different job IDs Uses a shorter date
	 * range (1 week) for faster test execution
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto createBulkEmptySlotRequestWithDifferentJobs() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto dto = new io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto();
		dto.setContractorJobPairs(Arrays.asList(createContractorJobPair(getDefaultContractorId(), getDefaultJobId()),
				createContractorJobPair(getDefaultSecondaryContractorId(), getDefaultJobId() + 1)));
		// Use a smaller date range (1 week) for faster test execution
		Integer startDate = getDefaultStartDate();
		dto.setMaxJobStartDate(startDate);
		dto.setMinJobEndDate(startDate + (7 * 24 * 60 * 60)); // 1 week later
		return dto;
	}

	// ===== Message Constants (Inner Types Must Be Last) =====

	/**
	 * Message constants for test assertions. Note: Inner types must be placed at the end
	 * to comply with InnerTypeLast checkstyle rule.
	 */
	public static final class Messages {

		public static final String CONTRACTOR_TIMESHEET_SETTINGS_FETCHED_SUCCESSFULLY = "Contractor timesheet settings fetched successfully";

		public static final String EMPTY_SLOTS_FETCHED_SUCCESSFULLY = "Empty slots fetched successfully";

		public static final String CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY = "Contractor count fetched successfully";

		public static final String CONTRACTORS_FETCHED_SUCCESSFULLY = "Contractors fetched successfully";

		public static final String COMMON_FREE_SLOTS_FETCHED_SUCCESSFULLY = "Common free slots fetched successfully";

		/**
		 * Private constructor to prevent instantiation.
		 */
		private Messages() {
			// Messages class - prevent instantiation
		}

	}

}