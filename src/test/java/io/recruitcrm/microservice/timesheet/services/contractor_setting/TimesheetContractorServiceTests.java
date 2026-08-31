package io.recruitcrm.microservice.timesheet.services.contractor_setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.assigned_candidate.AssignCandidateJobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.ContractorTimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.repositories.contractor_setting.TimesheetContractorSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.testdata.ContractorTestDataFactory;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimesheetContractorServiceTests {

	private static final String FIELD_PORTAL_ACCESS_CONTROL_SERVICE = "portalAccessControlService";

	private static final String METHOD_DETERMINE_SEVEN_DAY_SLOT_CURRENT_START = "determineSevenDaySlotCurrentStart";

	private static final String METHOD_CALCULATE_LAST_DAY_OF_MONTH = "calculateLastDayOfMonth";

	private static final String METHOD_CALCULATE_FREE_SLOTS = "calculateFreeSlots";

	@InjectMocks
	private TimesheetContractorService timesheetContractorService;

	@Mock
	private TimesheetSettingRepository timesheetSettingRepository;

	@Mock
	private AssignCandidateJobJpaRepository assignCandidateJobJpaRepository;

	@Mock
	private TimesheetContractorSettingRepository timesheetContractorSettingRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlService;

	@Spy
	private FreeSlotCalculator freeSlotCalculator = new FreeSlotCalculator();

	@BeforeEach
	void setUp() {
		// Given - Setup auth mock for all test methods
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ContractorTestDataFactory.getDefaultAccountId());
		// Default principal for getFreeSlots/getBulkFreeSlots tests; individual tests may
		// override
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal defaultUserPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		given(this.auth.getUnifiedPrincipal()).willReturn(defaultUserPrincipal);
	}

	@Test
	@DisplayName("Get contractor timesheet settings should return success response")
	void testGetContractorTimesheetSettingsValidRequestReturnsSuccess() {
		// Given
		GetContractorListRequestBodyDto request = ContractorTestDataFactory.createGetContractorListRequest();
		AssignCandidateJob assignment = ContractorTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = ContractorTestDataFactory.createTimesheetSetting();

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultAccountId()))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId()))
			.willReturn(Optional.of(timesheetSetting));

		// When
		Map<Integer, ContractorTimesheetSettingResponseBodyDto> result = this.timesheetContractorService
			.getContractorTimesheetSettings(request);

		// Then
		assertThat(result).isNotNull().hasSize(1).containsKey(ContractorTestDataFactory.getDefaultContractorId());
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
					ContractorTestDataFactory.getDefaultContractorId(),
					ContractorTestDataFactory.getDefaultAccountId());
		then(this.timesheetSettingRepository).should()
			.findByJobIdContractorId(request.getJobId(), ContractorTestDataFactory.getDefaultContractorId());
	}

	@Test
	@DisplayName("Get contractor timesheet settings should throw ResourceNotFoundException when assignment not found")
	void testGetContractorTimesheetSettingsAssignmentNotFoundThrowsResourceNotFoundException() {
		// Given
		GetContractorListRequestBodyDto request = ContractorTestDataFactory.createGetContractorListRequest();

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultAccountId()))
			.willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getContractorTimesheetSettings(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetSetting not found for Job Id: " + request.getJobId() + " candidate Id : "
					+ ContractorTestDataFactory.getDefaultContractorId());

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
					ContractorTestDataFactory.getDefaultContractorId(),
					ContractorTestDataFactory.getDefaultAccountId());
	}

	@Test
	@DisplayName("Get contractor timesheet settings should throw ResourceNotFoundException when timesheet setting not found")
	void testGetContractorTimesheetSettingsTimesheetSettingNotFoundThrowsResourceNotFoundException() {
		// Given
		GetContractorListRequestBodyDto request = ContractorTestDataFactory.createGetContractorListRequest();
		AssignCandidateJob assignment = ContractorTestDataFactory.createAssignCandidateJob();

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultAccountId()))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId()))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getContractorTimesheetSettings(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetSetting not found for Job ID: " + request.getJobId()
					+ " and Contractor ID: " + ContractorTestDataFactory.getDefaultContractorId());

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
					ContractorTestDataFactory.getDefaultContractorId(),
					ContractorTestDataFactory.getDefaultAccountId());
		then(this.timesheetSettingRepository).should()
			.findByJobIdContractorId(request.getJobId(), ContractorTestDataFactory.getDefaultContractorId());
	}

	@Test
	@DisplayName("Get free slots should return success response")
	void testGetFreeSlotsValidRequestReturnsSuccess() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				timesheetFrequencyId);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle empty contractor list")
	void testGetFreeSlotsEmptyContractorListReturnsSuccess() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWithEmptyContractors();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				timesheetFrequencyId);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle multiple contractors")
	void testGetFreeSlotsMultipleContractorsReturnsSuccess() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWithMultipleContractors();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory.createOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				timesheetFrequencyId);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get contractor timesheet settings should handle multiple contractors")
	void testGetContractorTimesheetSettingsMultipleContractorsReturnsSuccess() {
		// Given
		GetContractorListRequestBodyDto request = ContractorTestDataFactory
			.createGetContractorListRequestWithMultipleContractors();
		AssignCandidateJob assignment1 = ContractorTestDataFactory.createAssignCandidateJob();
		AssignCandidateJob assignment2 = ContractorTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting1 = ContractorTestDataFactory.createTimesheetSetting();
		TimesheetSetting timesheetSetting2 = ContractorTestDataFactory.createTimesheetSetting();

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultAccountId()))
			.willReturn(assignment1);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
				ContractorTestDataFactory.getDefaultSecondaryContractorId(),
				ContractorTestDataFactory.getDefaultAccountId()))
			.willReturn(assignment2);
		given(this.timesheetSettingRepository.findByJobIdContractorId(request.getJobId(),
				ContractorTestDataFactory.getDefaultContractorId()))
			.willReturn(Optional.of(timesheetSetting1));
		given(this.timesheetSettingRepository.findByJobIdContractorId(request.getJobId(),
				ContractorTestDataFactory.getDefaultSecondaryContractorId()))
			.willReturn(Optional.of(timesheetSetting2));

		// When
		Map<Integer, ContractorTimesheetSettingResponseBodyDto> result = this.timesheetContractorService
			.getContractorTimesheetSettings(request);

		// Then
		assertThat(result).isNotNull()
			.hasSize(2)
			.containsKey(ContractorTestDataFactory.getDefaultContractorId())
			.containsKey(ContractorTestDataFactory.getDefaultSecondaryContractorId());
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
					ContractorTestDataFactory.getDefaultContractorId(),
					ContractorTestDataFactory.getDefaultAccountId());
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdAndAccountId(request.getJobId(),
					ContractorTestDataFactory.getDefaultSecondaryContractorId(),
					ContractorTestDataFactory.getDefaultAccountId());
	}

	// ===== Private Method Coverage Tests =====

	@Test
	@DisplayName("Get free slots should handle weekly frequency with no occupied slots")
	void testGetFreeSlotsWeeklyFrequencyNoOccupiedSlotsReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWeekly();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle biweekly frequency with no occupied slots")
	void testGetFreeSlotssBiweeklyFrequencyNoOccupiedSlotsReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestBiweekly();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getBiweeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with no occupied slots")
	void testGetFreeSlotsMonthlyFrequencyNoOccupiedSlotsReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestMonthly();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle overlapping occupied slots")
	void testGetFreeSlotsOverlappingOccupiedSlotsReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWithOverlappingSlots();
		List<OccupiedSlotsQueryResultDto> overlappingOccupiedSlots = ContractorTestDataFactory
			.createOverlappingOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(overlappingOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle slots with gaps")
	void testGetFreeSlotsWithGapsHandlesPartialSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWeekly();
		List<OccupiedSlotsQueryResultDto> slotsWithGaps = ContractorTestDataFactory.createOccupiedSlotsWithGaps();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(slotsWithGaps);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle slots matching boundaries")
	void testGetFreeSlotsMatchingBoundariesHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWeekly();
		List<OccupiedSlotsQueryResultDto> matchingBoundarySlots = ContractorTestDataFactory
			.createOccupiedSlotsMatchingBoundaries();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(matchingBoundarySlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle Sunday as timesheet start day")
	void testGetFreeSlotsSundayStartDayReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = new EmptySlotRequestBodyDto();
		request.setStartDate(ContractorTestDataFactory.getTestStartDate());
		request.setEndDate(ContractorTestDataFactory.getTestEndDate());
		request.setContractorIds(Arrays.asList(ContractorTestDataFactory.getDefaultContractorId()));
		request.setTimesheetStartDay(ContractorTestDataFactory.getTimesheetStartDaySunday());
		request.setJobId(ContractorTestDataFactory.getDefaultJobId());
		request.setTimesheetFrequencyId(ContractorTestDataFactory.getDefaultTimesheetFrequencyId());

		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for Monthly Ending Partial Slot Coverage =====

	@Test
	@DisplayName("Get free slots should handle monthly frequency with ending partial slot when gap is >= 1 month")
	void testGetFreeSlotsMonthlyFrequencyEndingPartialSlotWithLargeGapReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestMonthlyForEndingPartialSlot();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyEndingPartialSlot();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with ending partial slot when gap is < 1 month")
	void testGetFreeSlotsMonthlyFrequencyEndingPartialSlotWithSmallGapReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForEndingPartialSlotSmallGap();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyEndingPartialSlotSmallGap();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for Monthly Starting Partial Slot Coverage =====

	@Test
	@DisplayName("Get free slots should handle monthly frequency with starting partial slot when gap is >= 1 month")
	void testGetFreeSlotsMonthlyFrequencyStartingPartialSlotWithLargeGapReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForStartingPartialSlot();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyStartingPartialSlot();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with starting partial slot when gap is < 1 month")
	void testGetFreeSlotsMonthlyFrequencyStartingPartialSlotWithSmallGapReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForStartingPartialSlotSmallGap();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyStartingPartialSlotSmallGap();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for Monthly Frequency with Different Boundary Conditions =====

	@Test
	@DisplayName("Get free slots should handle monthly frequency with partial slot that requires month adjustment")
	void testGetFreeSlotsMonthlyFrequencyPartialSlotRequiresMonthAdjustmentReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForPartialSlotWithMonthAdjustment();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyPartialSlotWithMonthAdjustment();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with complex boundary conditions")
	void testGetFreeSlotsMonthlyFrequencyComplexBoundaryConditionsReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForComplexBoundaryConditions();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyComplexBoundaryConditions();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for Empty Free Slots and Empty Occupied Slots Coverage =====

	@Test
	@DisplayName("Get free slots should handle weekly frequency with empty free slots and empty occupied slots")
	void testGetFreeSlotsWeeklyFrequencyEmptySlotsHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestWeeklyForEmptySlots();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle biweekly frequency with empty free slots and empty occupied slots")
	void testGetFreeSlotsBiweeklyFrequencyEmptySlotsHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestBiweeklyForEmptySlots();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getBiweeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with empty free slots and empty occupied slots")
	void testGetFreeSlotsMonthlyFrequencyEmptySlotsHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestMonthlyForEmptySlots();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle weekly frequency with empty slots and endMatchedSlots true")
	void testGetFreeSlotsWeeklyFrequencyEmptySlotsWithEndMatchedSlotsTrueHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestWeeklyForEmptySlotsWithEndMatched();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle biweekly frequency with empty slots and endMatchedSlots true")
	void testGetFreeSlotsBiweeklyFrequencyEmptySlotsWithEndMatchedSlotsTrueHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestBiweeklyForEmptySlotsWithEndMatched();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getBiweeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with empty slots and endMatchedSlots true")
	void testGetFreeSlotsMonthlyFrequencyEmptySlotsWithEndMatchedSlotsTrueHandlesCorrectly() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForEmptySlotsWithEndMatched();
		List<OccupiedSlotsQueryResultDto> emptyOccupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(emptyOccupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for calculateLastDayOfMonth Method Coverage =====

	@Test
	@DisplayName("Get free slots should handle monthly frequency with 31-day months")
	void testGetFreeSlotsMonthlyFrequencyWith31DayMonthsReturnsSlots() {
		// Given - Test January (month 1) which has 31 days
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestMonthlyFor31DayMonth();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthly31DayMonth();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with 30-day months")
	void testGetFreeSlotsMonthlyFrequencyWith30DayMonthsReturnsSlots() {
		// Given - Test April (month 4) which has 30 days
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestMonthlyFor30DayMonth();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthly30DayMonth();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with February leap year")
	void testGetFreeSlotsMonthlyFrequencyWithFebruaryLeapYearReturnsSlots() {
		// Given - Test February 2024 (leap year) which has 29 days
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequestMonthlyForFebruaryLeapYear();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyFebruaryLeapYear();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with February non-leap year")
	void testGetFreeSlotsMonthlyFrequencyWithFebruaryNonLeapYearReturnsSlots() {
		// Given - Test February 2023 (non-leap year) which has 28 days
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForFebruaryNonLeapYear();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyFebruaryNonLeapYear();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should reject February century year dates that overflow the epoch range")
	void testGetFreeSlotsMonthlyFrequencyWithFebruaryCenturyYearThrowsValidationErrorException() {
		// Given - February 2100 does not fit in a 32-bit epoch; the cast overflows to a
		// pre-1970 date, which the epoch-year validation must reject
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForFebruaryCenturyYear();

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId()))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException.class)
			.hasMessageContaining("Invalid value for Year in Job Start Date");

		then(this.timesheetContractorSettingRepository).should(never())
			.findTimesheetsWithinDateRangeAndContractors(anyInt(), anyInt(), anyList(), anyInt());
	}

	@Test
	@DisplayName("Get free slots should handle monthly frequency with February century leap year")
	void testGetFreeSlotsMonthlyFrequencyWithFebruaryCenturyLeapYearReturnsSlots() {
		// Given - Test February 2000 (century leap year) which has 29 days
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestMonthlyForFebruaryCenturyLeapYear();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForMonthlyFebruaryCenturyLeapYear();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getMonthlyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for calculateWeeklyBiweeklyPartialSlotEndDate Branch Coverage =====

	@Test
	@DisplayName("Get free slots should handle weekly frequency with currentDate not after startDateTime")
	void testGetFreeSlotsWeeklyFrequencyCurrentDateNotAfterStartDateTimeReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestWeeklyForCurrentDateNotAfterStartDateTime();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForCurrentDateNotAfterStartDateTime();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// ===== Tests for calculateWeeklyBiweeklyEndingPartialSlot Branch Coverage =====

	@Test
	@DisplayName("Get free slots should handle weekly frequency with ending partial slot when isValidPartialSlot returns true")
	void testGetFreeSlotsWeeklyFrequencyEndingPartialSlotValidSlotReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestWeeklyForEndingPartialSlotValidSlot();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForEndingPartialSlotValidSlot();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots should handle biweekly frequency with ending partial slot when isValidPartialSlot returns true")
	void testGetFreeSlotsBiweeklyFrequencyEndingPartialSlotValidSlotReturnsSlots() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createEmptySlotRequestBiweeklyForEndingPartialSlotValidSlot();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createOccupiedSlotsForEndingPartialSlotValidSlot();

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				ContractorTestDataFactory.getBiweeklyFrequencyId());

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	// Note: The determineSevenDaySlotCurrentStart method is private and should be tested
	// indirectly through public methods that call it, not directly.
	// Private method testing should be done through public API integration tests.

	// ===== Tests for persona-based routing in getFreeSlots =====

	@Test
	@DisplayName("Get free slots for USER persona should return success")
	void testGetFreeSlotsForUserPersonaReturnsSuccess() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				timesheetFrequencyId);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots for CONTRACTOR persona should return success")
	void testGetFreeSlotsForContractorPersonaReturnsSuccess() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal contractorPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTRACTOR);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				timesheetFrequencyId);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots for CONTACT persona should return success with valid permissions")
	void testGetFreeSlotsForContactPersonaValidPermissionsReturnsSuccess() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		Integer clientId = 300;
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(1);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		// Mock portalAccessControlService - need to add this as a dependency
		io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlServiceMock = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService.class);
		org.springframework.test.util.ReflectionTestUtils.setField(this.timesheetContractorService,
				FIELD_PORTAL_ACCESS_CONTROL_SERVICE, portalAccessControlServiceMock);

		given(portalAccessControlServiceMock.validatePortalAccessControl(request.getJobId(), clientId))
			.willReturn(permissions);
		given(this.timesheetContractorSettingRepository.findTimesheetsWithinDateRangeAndContractors(
				request.getStartDate(), request.getEndDate(), request.getContractorIds(), request.getJobId()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getFreeSlots(request,
				timesheetFrequencyId);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsWithinDateRangeAndContractors(request.getStartDate(), request.getEndDate(),
					request.getContractorIds(), request.getJobId());
	}

	@Test
	@DisplayName("Get free slots for CONTACT persona should throw UnauthorizedAccessException when canCreate is null")
	void testGetFreeSlotsForContactPersonaCanCreateNullThrowsUnauthorizedAccessException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		Integer clientId = 300;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(null);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlServiceMock = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService.class);
		org.springframework.test.util.ReflectionTestUtils.setField(this.timesheetContractorService,
				FIELD_PORTAL_ACCESS_CONTROL_SERVICE, portalAccessControlServiceMock);

		given(portalAccessControlServiceMock.validatePortalAccessControl(request.getJobId(), clientId))
			.willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for create timesheet");

		then(this.auth).should().getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Get free slots for CONTACT persona should throw UnauthorizedAccessException when canCreate is not 1")
	void testGetFreeSlotsForContactPersonaCanCreateNotOneThrowsUnauthorizedAccessException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		Integer clientId = 300;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(0);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlServiceMock = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService.class);
		org.springframework.test.util.ReflectionTestUtils.setField(this.timesheetContractorService,
				FIELD_PORTAL_ACCESS_CONTROL_SERVICE, portalAccessControlServiceMock);

		given(portalAccessControlServiceMock.validatePortalAccessControl(request.getJobId(), clientId))
			.willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for create timesheet");

		then(this.auth).should().getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Get free slots for CONTACT persona should propagate ResourceNotFoundException from portal access control when job not found")
	void testGetFreeSlotsForContactPersonaJobNotFoundThrowsResourceNotFoundException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		Integer clientId = 300;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlServiceMock = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService.class);
		org.springframework.test.util.ReflectionTestUtils.setField(this.timesheetContractorService,
				FIELD_PORTAL_ACCESS_CONTROL_SERVICE, portalAccessControlServiceMock);

		given(portalAccessControlServiceMock.validatePortalAccessControl(request.getJobId(), clientId))
			.willThrow(new ResourceNotFoundException("Job", request.getJobId()));

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job");

		then(this.auth).should().getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Get free slots for CONTACT persona should propagate ValidationErrorException from portal access control when portal not enabled")
	void testGetFreeSlotsForContactPersonaPortalNotEnabledThrowsValidationErrorException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		Integer clientId = 300;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlServiceMock = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService.class);
		org.springframework.test.util.ReflectionTestUtils.setField(this.timesheetContractorService,
				FIELD_PORTAL_ACCESS_CONTROL_SERVICE, portalAccessControlServiceMock);

		given(portalAccessControlServiceMock.validatePortalAccessControl(request.getJobId(), clientId))
			.willThrow(new io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException(
					"Portal is not enabled for this job"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException.class)
			.hasMessageContaining("Portal is not enabled for this job");

		then(this.auth).should().getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Get free slots for CONTACT persona should propagate UnauthorizedAccessException from portal access control when client ID mismatch")
	void testGetFreeSlotsForContactPersonaClientIdMismatchThrowsUnauthorizedAccessException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		Integer clientId = 300;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlServiceMock = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService.class);
		org.springframework.test.util.ReflectionTestUtils.setField(this.timesheetContractorService,
				FIELD_PORTAL_ACCESS_CONTROL_SERVICE, portalAccessControlServiceMock);

		given(portalAccessControlServiceMock.validatePortalAccessControl(request.getJobId(), clientId))
			.willThrow(new io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException(
					"Client ID does not match job contact"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining("Client ID does not match job contact");

		then(this.auth).should().getUnifiedPrincipal();
	}

	// ===== getBulkFreeSlots Tests =====

	@Test
	@DisplayName("Get bulk free slots should return success response for USER persona")
	void testGetBulkFreeSlotsUserPersonaReturnsSuccess() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal userPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		TimesheetSetting timesheetSetting = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();
		List<io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto> contractorJobPairs = request
			.getContractorJobPairs();

		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs))
			.willReturn(Arrays.asList(timesheetSetting));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should return success response for CONTRACTOR persona")
	void testGetBulkFreeSlotsContractorPersonaReturnsSuccess() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal contractorPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createContractorPrincipal();
		TimesheetSetting timesheetSetting = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();
		List<io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto> contractorJobPairs = request
			.getContractorJobPairs();

		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs))
			.willReturn(Arrays.asList(timesheetSetting));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should return success response for CONTACT persona with valid permissions")
	void testGetBulkFreeSlotsContactPersonaWithValidPermissionsReturnsSuccess() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(ContractorTestDataFactory.getDefaultContactId());
		TimesheetSetting timesheetSetting = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto(
				1, 1, 1);
		List<io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto> contractorJobPairs = request
			.getContractorJobPairs();
		Integer jobId = ContractorTestDataFactory.getDefaultJobId();

		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.portalAccessControlService.validatePortalAccessControl(jobId,
				ContractorTestDataFactory.getDefaultContactId()))
			.willReturn(permissions);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs))
			.willReturn(Arrays.asList(timesheetSetting));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should()
			.validatePortalAccessControl(jobId, ContractorTestDataFactory.getDefaultContactId());
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should throw UnauthorizedAccessException for CONTACT persona with invalid permissions")
	void testGetBulkFreeSlotsContactPersonaWithInvalidPermissionsThrowsUnauthorizedAccessException() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		Integer contactId = 900;
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto(
				0, 1, 1);

		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.portalAccessControlService.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId(),
				contactId))
			.willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getBulkFreeSlots(request))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining(
					"Unauthorized access for create timesheet for job: " + ContractorTestDataFactory.getDefaultJobId());

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should()
			.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId(), contactId);
		then(this.timesheetSettingRepository).should(org.mockito.Mockito.never())
			.findLatestTimesheetSettingsForContractorJobPairs(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("Get bulk free slots should throw UnauthorizedAccessException for CONTACT persona when canCreate is null")
	void testGetBulkFreeSlotsContactPersonaWithNullCanCreateThrowsUnauthorizedAccessException() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		Integer contactId = 901;
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto(
				null, 1, 1);

		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.portalAccessControlService.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId(),
				contactId))
			.willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getBulkFreeSlots(request))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining(
					"Unauthorized access for create timesheet for job: " + ContractorTestDataFactory.getDefaultJobId());

		then(this.timesheetSettingRepository).should(never())
			.findLatestTimesheetSettingsForContractorJobPairs(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("Get bulk free slots should return empty list when settings list is empty")
	void testGetBulkFreeSlotsEmptySettingsListReturnsEmptyList() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal userPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		List<io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto> contractorJobPairs = request
			.getContractorJobPairs();

		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs))
			.willReturn(Arrays.asList());

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);
		then(this.timesheetContractorSettingRepository).should(never())
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should return empty list when settings have inconsistent frequency")
	void testGetBulkFreeSlotsInconsistentFrequencyReturnsEmptyList() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequestWithMultiplePairs();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal userPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		TimesheetSetting setting1 = ContractorTestDataFactory.createTimesheetSetting();
		TimesheetSetting setting2 = ContractorTestDataFactory.createTimesheetSetting();
		setting2.setTimesheetFrequency(ContractorTestDataFactory.getDefaultTimesheetFrequencyId() + 1);
		List<io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto> contractorJobPairs = request
			.getContractorJobPairs();

		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs))
			.willReturn(Arrays.asList(setting1, setting2));

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);
		then(this.timesheetContractorSettingRepository).should(never())
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should return empty list when settings have inconsistent start day")
	void testGetBulkFreeSlotsInconsistentStartDayReturnsEmptyList() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequestWithMultiplePairs();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal userPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		TimesheetSetting setting1 = ContractorTestDataFactory.createTimesheetSetting();
		TimesheetSetting setting2 = ContractorTestDataFactory.createTimesheetSetting();
		setting2.setTimesheetStartDay(ContractorTestDataFactory.getDefaultTimesheetStartDay() + 1);
		List<io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto> contractorJobPairs = request
			.getContractorJobPairs();

		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs))
			.willReturn(Arrays.asList(setting1, setting2));

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);
		then(this.timesheetContractorSettingRepository).should(never())
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should return success response with consistent settings")
	void testGetBulkFreeSlotsConsistentSettingsReturnsSuccess() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequestWithMultiplePairs();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal userPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		TimesheetSetting setting1 = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		TimesheetSetting setting2 = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultSecondaryContractorId(),
				ContractorTestDataFactory.getDefaultJobId());
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();

		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs()))
			.willReturn(Arrays.asList(setting1, setting2));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs());
		then(this.timesheetContractorSettingRepository).should(times(1))
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Get bulk free slots should not include ending partial slot when a job extends beyond the window")
	void testGetBulkFreeSlotsNoEndingPartialWhenJobExtendsBeyondWindow() {
		// Given - Contractor 1 job: Jul 1, 2025 -> Jul 31, 2025; Contractor 2 job:
		// Jun 24, 2025 -> Jul 27, 2026. Both weekly, week starts Tuesday(2).
		// Window = [maxJobStartDate=Jul 1 2025, minJobEndDate=Jul 31 2025].
		Integer weeklyFrequency = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.WEEKLY
			.getId();
		Integer tuesday = 2;
		Integer jul1Start = (int) LocalDateTime.of(2025, 7, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer jul31End = (int) LocalDateTime.of(2025, 7, 31, 23, 59, 59).toEpochSecond(ZoneOffset.UTC);

		TimesheetSetting setting1 = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		setting1.setTimesheetFrequency(weeklyFrequency);
		setting1.setTimesheetStartDay(tuesday);
		setting1.setJobStartDate(jul1Start);
		setting1.setJobEndDate(jul31End);

		TimesheetSetting setting2 = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultSecondaryContractorId(),
				ContractorTestDataFactory.getDefaultJobId());
		setting2.setTimesheetFrequency(weeklyFrequency);
		setting2.setTimesheetStartDay(tuesday);
		setting2.setJobStartDate((int) LocalDateTime.of(2025, 6, 24, 0, 0).toEpochSecond(ZoneOffset.UTC));
		setting2.setJobEndDate((int) LocalDateTime.of(2026, 7, 27, 23, 59, 59).toEpochSecond(ZoneOffset.UTC));

		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto();
		request.setContractorJobPairs(Arrays.asList(
				new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(
						ContractorTestDataFactory.getDefaultContractorId(),
						ContractorTestDataFactory.getDefaultJobId()),
				new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(
						ContractorTestDataFactory.getDefaultSecondaryContractorId(),
						ContractorTestDataFactory.getDefaultJobId())));
		request.setMaxJobStartDate(jul1Start);
		request.setMinJobEndDate(jul31End);

		given(this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs()))
			.willReturn(Arrays.asList(setting1, setting2));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(ContractorTestDataFactory.createEmptyOccupiedSlotsQueryResults());

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then - only the 4 full weeks; the ending partial Jul 29-31 is not common
		// because contractor 2's job continues beyond Jul 31 (its slot there is the
		// full week Jul 29 - Aug 4).
		assertThat(result).hasSize(4);
		assertThat(result.get(0).getStartDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 1, 0, 0).toEpochSecond(ZoneOffset.UTC));
		assertThat(result.get(3).getEndDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 28, 23, 59, 59).toEpochSecond(ZoneOffset.UTC));
	}

	@Test
	@DisplayName("Get bulk free slots for a single pair should include both partial slots like /free-slots")
	void testGetBulkFreeSlotsSinglePairIncludesPartialsLikeFreeSlots() {
		// Given - single contractor, job Jul 1, 2025 -> Jul 31, 2025, weekly, week
		// starts Monday(1). The stored setting snapshot has job dates that differ from
		// the request window (stale snapshot / different timestamp source) - partials
		// must still be produced because a single pair's window IS its job range.
		Integer weeklyFrequency = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.WEEKLY
			.getId();
		Integer monday = 1;
		Integer jul1Start = (int) LocalDateTime.of(2025, 7, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer jul31End = (int) LocalDateTime.of(2025, 7, 31, 23, 59, 59).toEpochSecond(ZoneOffset.UTC);

		TimesheetSetting setting = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		setting.setTimesheetFrequency(weeklyFrequency);
		setting.setTimesheetStartDay(monday);
		// Stale snapshot dates that do NOT match the request window
		setting.setJobStartDate((int) LocalDateTime.of(2025, 5, 15, 0, 0).toEpochSecond(ZoneOffset.UTC));
		setting.setJobEndDate((int) LocalDateTime.of(2025, 12, 31, 0, 0).toEpochSecond(ZoneOffset.UTC));

		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto();
		request.setContractorJobPairs(Arrays
			.asList(new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(
					ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId())));
		request.setMaxJobStartDate(jul1Start);
		request.setMinJobEndDate(jul31End);

		given(this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs()))
			.willReturn(Arrays.asList(setting));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(ContractorTestDataFactory.createEmptyOccupiedSlotsQueryResults());

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then - starting partial Jul 1-6, full weeks Jul 7-13, 14-20, 21-27, and
		// ending partial Jul 28-31: identical to what /free-slots returns.
		assertThat(result).hasSize(5);
		assertThat(result.get(0).getStartDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 1, 0, 0).toEpochSecond(ZoneOffset.UTC));
		assertThat(result.get(0).getEndDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 6, 23, 59, 59).toEpochSecond(ZoneOffset.UTC));
		assertThat(result.get(4).getStartDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 28, 0, 0).toEpochSecond(ZoneOffset.UTC));
		assertThat(result.get(4).getEndDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 31, 23, 59, 59).toEpochSecond(ZoneOffset.UTC));
	}

	@Test
	@DisplayName("Get bulk free slots should include ending partial slot when all jobs end on the window end")
	void testGetBulkFreeSlotsIncludesEndingPartialWhenAllJobsEndOnWindowEnd() {
		// Given - both jobs: Jul 1, 2025 -> Jul 31, 2025, weekly, week starts Tuesday(2)
		Integer weeklyFrequency = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.WEEKLY
			.getId();
		Integer tuesday = 2;
		Integer jul1Start = (int) LocalDateTime.of(2025, 7, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer jul31End = (int) LocalDateTime.of(2025, 7, 31, 23, 59, 59).toEpochSecond(ZoneOffset.UTC);

		TimesheetSetting setting1 = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		setting1.setTimesheetFrequency(weeklyFrequency);
		setting1.setTimesheetStartDay(tuesday);
		setting1.setJobStartDate(jul1Start);
		setting1.setJobEndDate(jul31End);

		TimesheetSetting setting2 = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultSecondaryContractorId(),
				ContractorTestDataFactory.getDefaultJobId());
		setting2.setTimesheetFrequency(weeklyFrequency);
		setting2.setTimesheetStartDay(tuesday);
		setting2.setJobStartDate(jul1Start);
		setting2.setJobEndDate(jul31End);

		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto();
		request.setContractorJobPairs(Arrays.asList(
				new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(
						ContractorTestDataFactory.getDefaultContractorId(),
						ContractorTestDataFactory.getDefaultJobId()),
				new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto(
						ContractorTestDataFactory.getDefaultSecondaryContractorId(),
						ContractorTestDataFactory.getDefaultJobId())));
		request.setMaxJobStartDate(jul1Start);
		request.setMinJobEndDate(jul31End);

		given(this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs()))
			.willReturn(Arrays.asList(setting1, setting2));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(ContractorTestDataFactory.createEmptyOccupiedSlotsQueryResults());

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then - 4 full weeks plus the ending partial Jul 29-31, which is valid for
		// both contractors because both jobs actually end on Jul 31.
		assertThat(result).hasSize(5);
		assertThat(result.get(4).getStartDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 29, 0, 0).toEpochSecond(ZoneOffset.UTC));
		assertThat(result.get(4).getEndDate())
			.isEqualTo((int) LocalDateTime.of(2025, 7, 31, 23, 59, 59).toEpochSecond(ZoneOffset.UTC));
	}

	@Test
	@DisplayName("Get bulk free slots should validate permissions for all unique job IDs for CONTACT persona")
	void testGetBulkFreeSlotsContactPersonaValidatesAllUniqueJobIds() {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequestWithDifferentJobs();
		Integer contactId = 901;
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal contactPrincipal = org.mockito.Mockito
			.mock(io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal.class);
		TimesheetSetting setting1 = ContractorTestDataFactory.createTimesheetSetting();
		TimesheetSetting setting2 = ContractorTestDataFactory.createTimesheetSetting();
		List<OccupiedSlotsQueryResultDto> occupiedSlots = ContractorTestDataFactory
			.createEmptyOccupiedSlotsQueryResults();
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto(
				1, 1, 1);

		given(contactPrincipal.getPrincipalType())
			.willReturn(io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.portalAccessControlService.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId(),
				contactId))
			.willReturn(permissions);
		given(this.portalAccessControlService
			.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId() + 1, contactId))
			.willReturn(permissions);
		given(this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs()))
			.willReturn(Arrays.asList(setting1, setting2));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(occupiedSlots);

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should()
			.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId(), contactId);
		then(this.portalAccessControlService).should()
			.validatePortalAccessControl(ContractorTestDataFactory.getDefaultJobId() + 1, contactId);
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs());
		then(this.timesheetContractorSettingRepository).should(times(1))
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("Find breakpoint should return global end for biweekly ending partial slot with matching day and sufficient gap")
	void testFindBreakpointBiweeklyEndingFalseWithMatchingDayReturnsGlobalEnd() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.BIWEEKLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 4, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 18, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 1;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.FALSE);

		// Then
		assertThat(result).isEqualTo(globalEnd);
	}

	@Test
	@DisplayName("Find breakpoint should return zero for biweekly ending partial slot when day does not match")
	void testFindBreakpointBiweeklyEndingFalseWithNonMatchingDayReturnsZero() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.BIWEEKLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 5, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 18, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 2;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.FALSE);

		// Then
		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Find breakpoint should return zero for biweekly front slot when matching day has insufficient gap")
	void testFindBreakpointBiweeklyEndingFalseWithMatchingDayAndInsufficientGapReturnsZero() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.BIWEEKLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 11, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 18, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 1;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.FALSE);

		// Then
		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Find breakpoint should return next day epoch for monthly start day when ending partial slot is false")
	void testFindBreakpointMonthlyEndingFalseWithNextDayMatchReturnsNextDayEpoch() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.MONTHLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 10, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 14, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 15;
		Integer expected = (int) LocalDateTime.of(2026, 5, 15, 0, 0).toEpochSecond(ZoneOffset.UTC);

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.FALSE);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Find breakpoint should return zero for monthly frequency when no matching day exists in range")
	void testFindBreakpointMonthlyEndingTrueWithoutMatchingDayReturnsZero() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.MONTHLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 5, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 31;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.TRUE);

		// Then
		assertThat(result).isZero();
	}

	@Test
	@DisplayName("determineSevenDaySlotCurrentStart should return first start day when adjusted start is before first start day")
	void testDetermineSevenDaySlotCurrentStartBeforeFirstStartReturnsFirstStart() {
		// Given
		int adjustedStart = 1000;
		int firstStartDayEpoch = 2000;
		io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext context = new io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext(
				adjustedStart, LocalDateTime.ofEpochSecond(adjustedStart, 0, ZoneOffset.UTC), 1, firstStartDayEpoch, 2);

		// When
		Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(this.timesheetContractorService,
				METHOD_DETERMINE_SEVEN_DAY_SLOT_CURRENT_START, context, 5000);

		// Then
		assertThat(result).isEqualTo(firstStartDayEpoch);
	}

	@Test
	@DisplayName("determineSevenDaySlotCurrentStart should return partial slot end when adjusted start is after first start day")
	void testDetermineSevenDaySlotCurrentStartAfterFirstStartReturnsPartialSlotEnd() {
		// Given
		LocalDateTime adjustedDateTime = LocalDateTime.of(2026, 5, 4, 12, 0);
		int adjustedStart = (int) adjustedDateTime.toEpochSecond(ZoneOffset.UTC);
		int firstStartDayEpoch = adjustedStart - 10;
		io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext context = new io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext(
				adjustedStart, adjustedDateTime, 1, firstStartDayEpoch, 2);
		int end = (int) adjustedDateTime.plusDays(3).toEpochSecond(ZoneOffset.UTC);

		// When
		Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(this.timesheetContractorService,
				METHOD_DETERMINE_SEVEN_DAY_SLOT_CURRENT_START, context, end);

		// Then
		assertThat(result).isGreaterThan(adjustedStart);
	}

	@Test
	@DisplayName("Find breakpoint should return global end for weekly frequency when ending partial slot is false")
	void testFindBreakpointWeeklyEndingFalseReturnsGlobalEnd() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.WEEKLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 4, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 6, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 7;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.FALSE);

		// Then
		assertThat(result).isEqualTo(globalEnd);
	}

	@Test
	@DisplayName("Find breakpoint should return zero for weekly frequency when matching day is absent")
	void testFindBreakpointWeeklyEndingTrueWithoutMatchingDayReturnsZero() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.WEEKLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 4, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 5, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 7;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.TRUE);

		// Then
		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Find breakpoint should return zero for biweekly frequency when second matching day is absent")
	void testFindBreakpointBiweeklyEndingTrueWithoutSecondMatchingDayReturnsZero() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.BIWEEKLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 4, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 6, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 1;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.TRUE);

		// Then
		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Find breakpoint should resolve monthly last-day marker before searching")
	void testFindBreakpointMonthlyLastDayMarkerReturnsMonthEndBreakpoint() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.MONTHLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2024, 2, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2024, 3, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 100;
		Integer expected = (int) LocalDateTime.of(2024, 2, 28, 23, 59, 59).toEpochSecond(ZoneOffset.UTC);

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.TRUE);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Find breakpoint should return global end for monthly front slot when next day does not match")
	void testFindBreakpointMonthlyEndingFalseWithoutNextDayMatchReturnsGlobalEnd() {
		// Given
		Integer frequencyType = io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum.MONTHLY
			.getId();
		Integer globalStart = (int) LocalDateTime.of(2026, 5, 10, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer globalEnd = (int) LocalDateTime.of(2026, 5, 14, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 20;

		// When
		Integer result = this.timesheetContractorService.findBreakpoint(frequencyType, globalStart, globalEnd,
				timesheetStartDay, Boolean.FALSE);

		// Then
		assertThat(result).isEqualTo(globalEnd);
	}

	@Test
	@DisplayName("determineSevenDaySlotCurrentStart should skip partial slot when next start equals adjusted start")
	void testDetermineSevenDaySlotCurrentStartNextStartEqualsAdjustedStartReturnsAdjustedStart() {
		// Given
		LocalDateTime adjustedDateTime = LocalDateTime.of(2026, 5, 4, 0, 0);
		int adjustedStart = (int) adjustedDateTime.toEpochSecond(ZoneOffset.UTC);
		io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext context = new io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext(
				adjustedStart, adjustedDateTime, 1, adjustedStart - 1, 1);

		// When
		Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(this.timesheetContractorService,
				METHOD_DETERMINE_SEVEN_DAY_SLOT_CURRENT_START, context, adjustedStart + 1000);

		// Then
		assertThat(result).isEqualTo(adjustedStart);
	}

	@Test
	@DisplayName("initializeSevenDaySlotContext should adjust start when next second rolls to next day")
	void testInitializeSevenDaySlotContextStartPlusOneSecondRollsDayAdjustsStart() {
		// Given
		int start = (int) LocalDateTime.of(2026, 5, 4, 23, 59, 59).toEpochSecond(ZoneOffset.UTC);
		Integer timesheetStartDay = 2;

		// When
		io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext context = org.springframework.test.util.ReflectionTestUtils
			.invokeMethod(this.timesheetContractorService, "initializeSevenDaySlotContext", start, timesheetStartDay);

		// Then
		assertThat(context.adjustedStart()).isEqualTo(start + 1);
		assertThat(context.adjustedStartDayOfWeek()).isEqualTo(timesheetStartDay);
	}

	@Test
	@DisplayName("calculateLastDayOfMonth should return thirty one for all long months")
	void testCalculateLastDayOfMonthLongMonthsReturnThirtyOne() {
		// Given
		List<Integer> longMonths = List.of(1, 3, 5, 7, 8, 10, 12);

		// When and Then
		for (Integer month : longMonths) {
			Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
					this.timesheetContractorService, METHOD_CALCULATE_LAST_DAY_OF_MONTH, month, Integer.valueOf(2026));
			assertThat(result).isEqualTo(Integer.valueOf(31));
		}
	}

	@Test
	@DisplayName("calculateLastDayOfMonth should return twenty eight for century years not divisible by four hundred")
	void testCalculateLastDayOfMonthCenturyNonLeapYearReturnsTwentyEight() {
		// When
		Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(this.timesheetContractorService,
				METHOD_CALCULATE_LAST_DAY_OF_MONTH, Integer.valueOf(2), Integer.valueOf(1900));

		// Then
		assertThat(result).isEqualTo(Integer.valueOf(28));
	}

	@Test
	@DisplayName("calculateLastDayOfMonth should return twenty nine for February in leap years")
	void testCalculateLastDayOfMonthFebruaryLeapYearReturnsTwentyNine() {
		// Given
		List<Integer> leapYears = List.of(2024, 2000);

		// When and Then
		for (Integer year : leapYears) {
			Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
					this.timesheetContractorService, METHOD_CALCULATE_LAST_DAY_OF_MONTH, Integer.valueOf(2), year);
			assertThat(result).isEqualTo(Integer.valueOf(29));
		}
	}

	@Test
	@DisplayName("calculateLastDayOfMonth should return twenty eight for February in non-leap years")
	void testCalculateLastDayOfMonthFebruaryNonLeapYearReturnsTwentyEight() {
		// When
		Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(this.timesheetContractorService,
				METHOD_CALCULATE_LAST_DAY_OF_MONTH, Integer.valueOf(2), Integer.valueOf(2023));

		// Then
		assertThat(result).isEqualTo(Integer.valueOf(28));
	}

	@Test
	@DisplayName("calculateLastDayOfMonth should return thirty for short months")
	void testCalculateLastDayOfMonthShortMonthsReturnThirty() {
		// Given
		List<Integer> shortMonths = List.of(4, 6, 9, 11);

		// When and Then
		for (Integer month : shortMonths) {
			Integer result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
					this.timesheetContractorService, METHOD_CALCULATE_LAST_DAY_OF_MONTH, month, Integer.valueOf(2026));
			assertThat(result).isEqualTo(Integer.valueOf(30));
		}
	}

	@Test
	@DisplayName("calculateFreeSlots should skip between-slot split when merged boundaries touch")
	void testCalculateFreeSlotsAdjacentMergedIntervalsSkipsBetweenGap() {
		// Given
		List<List<Integer>> mergedOccupiedSlots = ContractorTestDataFactory
			.createAdjacentOccupiedSlotsForMergedGapTest();
		int globalStart = ContractorTestDataFactory.getAdjacentMergedGapTestGlobalStart();
		int globalEnd = ContractorTestDataFactory.getAdjacentMergedGapTestGlobalEnd();

		// When
		@SuppressWarnings("unchecked")
		List<TimeSlotsResultBodyDto> result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
				this.timesheetContractorService, METHOD_CALCULATE_FREE_SLOTS, mergedOccupiedSlots, globalStart,
				globalEnd, Integer.valueOf(6 * 24 * 60 * 60), ContractorTestDataFactory.getTimesheetStartDayMonday(),
				ContractorTestDataFactory.getWeeklyFrequencyId());

		// Then
		assertThat(result).isNotEmpty();
	}

	@Test
	@DisplayName("initializeSevenDaySlotContext should adjust start when year changes but day of year stays equal")
	void testInitializeSevenDaySlotContextYearChangeSameDayOfYearAdjustsStart() {
		// Given
		int start = 1000;
		LocalDateTime startDateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0);
		LocalDateTime startPlusOneSecond = LocalDateTime.of(2026, 6, 15, 12, 0, 1);
		Integer timesheetStartDay = ContractorTestDataFactory.getTimesheetStartDayMonday();

		// When
		io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext context;
		try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class,
				Mockito.CALLS_REAL_METHODS)) {
			mockedLocalDateTime.when(() -> LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC))
				.thenReturn(startDateTime);
			mockedLocalDateTime.when(() -> LocalDateTime.ofEpochSecond(start + 1, 0, ZoneOffset.UTC))
				.thenReturn(startPlusOneSecond);
			context = org.springframework.test.util.ReflectionTestUtils.invokeMethod(this.timesheetContractorService,
					"initializeSevenDaySlotContext", start, timesheetStartDay);
		}

		// Then
		assertThat(context.adjustedStart()).isEqualTo(start + 1);
	}

	@Test
	@DisplayName("Get free slots should throw UnauthorizedAccessException when principal is null")
	void testGetFreeSlotsNullPrincipalThrowsUnauthorizedAccessException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();

		given(this.auth.getUnifiedPrincipal()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining("Unknown persona type");

		then(this.timesheetContractorSettingRepository).should(never())
			.findTimesheetsWithinDateRangeAndContractors(anyInt(), anyInt(), anyList(), anyInt());
	}

	@Test
	@DisplayName("Get free slots should throw UnauthorizedAccessException when principal type is null")
	void testGetFreeSlotsNullPrincipalTypeThrowsUnauthorizedAccessException() {
		// Given
		EmptySlotRequestBodyDto request = ContractorTestDataFactory.createEmptySlotRequest();
		Integer timesheetFrequencyId = ContractorTestDataFactory.getDefaultTimesheetFrequencyId();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal principal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal.class);

		given(principal.getPrincipalType()).willReturn(null);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.getFreeSlots(request, timesheetFrequencyId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining("Unknown persona type");

		then(this.timesheetContractorSettingRepository).should(never())
			.findTimesheetsWithinDateRangeAndContractors(anyInt(), anyInt(), anyList(), anyInt());
	}

	@Test
	@DisplayName("Validate free slots date range should throw ValidationErrorException when span exceeds 10 years")
	void testValidateFreeSlotsDateRangeSpanExceedsTenYearsThrowsValidationErrorException() {
		// Given
		Integer startDate = (int) LocalDateTime.of(2025, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
		Integer endDate = (int) LocalDateTime.of(2036, 1, 2, 0, 0).toEpochSecond(ZoneOffset.UTC);

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.validateFreeSlotsDateRange(startDate, endDate))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException.class)
			.hasMessageContaining("Job Start Date and Job End Date must not exceed a span of 10 years");
	}

	@Test
	@DisplayName("Validate epoch year should throw ValidationErrorException when year is before 1970")
	void testValidateEpochYearBeforeNineteenSeventyThrowsValidationErrorException() {
		// Given - a negative epoch resolves to 1969
		Integer epochSeconds = -1;

		// When & Then
		assertThatThrownBy(() -> this.timesheetContractorService.validateEpochYear(epochSeconds, "Job Start Date"))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException.class)
			.hasMessageContaining("Invalid value for Year in Job Start Date");
	}

	@Test
	@DisplayName("Get bulk free slots should handle settings with null job dates")
	void testGetBulkFreeSlotsNullJobDatesOnSettingReturnsSuccess() {
		// Given - the stored setting snapshot has no job dates; the null dates must
		// flow through the partial-slot decision without failing
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto request = ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal userPrincipal = io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory
			.createUserPrincipal();
		TimesheetSetting timesheetSetting = ContractorTestDataFactory.createTimesheetSettingWithAssociation(
				ContractorTestDataFactory.getDefaultContractorId(), ContractorTestDataFactory.getDefaultJobId());
		timesheetSetting.setJobStartDate(null);
		timesheetSetting.setJobEndDate(null);

		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(request.getContractorJobPairs()))
			.willReturn(Arrays.asList(timesheetSetting));
		given(this.timesheetContractorSettingRepository.findTimesheetsForContractorJobPairs(anyInt(), anyInt(),
				anyList()))
			.willReturn(ContractorTestDataFactory.createEmptyOccupiedSlotsQueryResults());

		// When
		List<TimeSlotsResultBodyDto> result = this.timesheetContractorService.getBulkFreeSlots(request);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetContractorSettingRepository).should()
			.findTimesheetsForContractorJobPairs(anyInt(), anyInt(), anyList());
	}

}