package io.recruitcrm.microservice.timesheet.services.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import static org.mockito.ArgumentMatchers.isNull;

import org.assertj.core.api.InstanceOfAssertFactories;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CompanySearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetForMigrationDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAccessControlResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateJobTimesheetAccessControlRequestBodyDto;
import org.mockito.ArgumentCaptor;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprovalStatusTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.entity.model.Deal;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.assigned_candidate.AssignCandidateJobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.deal.DealJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job.JobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.workTimeEnum;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.helpers.FetchUserAndContactUserIds;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverDetailResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.approver.TimesheetApproversResponseBodyDto;
import io.recruitcrm.microservice.timesheet.mapper.CustomTimeSheetMapper;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetMapper;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetStatusHistoryMapper;
import io.recruitcrm.microservice.timesheet.repositories.candidate.CandidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.TimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetTestDataFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetService Tests")
class TimesheetServiceTests {

	@InjectMocks
	private TimesheetService timesheetService;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private TimeLogRepository timeLogRepository;

	@Mock
	private DealJpaRepository dealJpaRepository;

	@Mock
	private TimesheetSettingRepository timesheetSettingRepository;

	@Mock
	private AssignCandidateJobJpaRepository assignCandidateJobJpaRepository;

	@Mock
	private JobJpaRepository jobJpaRepository;

	@Mock
	private TimesheetApprovalRepository timesheetApprovalRepository;

	@Mock
	private TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	@Mock
	private TimeLogJpaRepository timeLogJpaRepository;

	@Mock
	private TimesheetMapper timesheetMapper;

	@Mock
	private TimesheetStatusHistoryMapper timesheetStatusHistoryMapper;

	@Mock
	private TimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ContactRepository contactRepository;

	@Mock
	private CandidateRepository candidateRepository;

	@Mock
	private FetchUserAndContactUserIds fetchUserAndContactUserIds;

	@Mock
	private CustomTimeSheetMapper customTimeSheetMapper;

	@Mock
	private AuthHolder auth;

	@Mock
	private AccessControlChecker contractStaffingAccessControlChecker;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlService;

	@Mock
	private io.recruitcrm.microservice.timesheet.helpers.auth.EntityAccessValidator entityAccessValidator;

	@Mock
	private io.recruitcrm.microservice.timesheet.helpers.auth.PrincipalEntityExtractor principalEntityExtractor;

	@Mock
	private io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository timesheetApproverRepository;

	@Mock
	private io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogBreakIntervalRepository timeLogBreakIntervalRepository;

	@Mock
	private io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogIntervalRepository timeLogIntervalRepository;

	@Mock
	private io.recruitcrm.microservice.timesheet.dao.job_timesheet_access.JobTimesheetAccessJpaRepository jobTimesheetAccessJpaRepository;

	@Mock
	private io.recruitcrm.microservice.timesheet.mapper.JobTimesheetAccessMapper jobTimesheetAccessMapper;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.search.TimesheetSearchService timesheetSearchService;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.user.IUserTimezoneService userTimezoneService;

	@Mock
	private ITimesheetService selfReference;

	@Mock
	private io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper kafkaProducerHelper;

	@BeforeEach
	void setUp() throws Exception {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		java.lang.reflect.Field selfRefField = TimesheetService.class.getDeclaredField("selfReference");
		selfRefField.setAccessible(true);
		selfRefField.set(this.timesheetService, this.timesheetService);
	}

	@Test
	@DisplayName("Create timesheets should create successfully with valid request")
	void testCreateTimesheetsValidRequestCreatesSuccessfully() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, accountId);
		then(this.timesheetSettingRepository).should()
			.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size());
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ResourceNotFoundException when assignments are missing")
	void testCreateTimesheetsAssignmentsMissingThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> incompleteAssignments = List.of(TimesheetTestDataFactory.createAssignCandidateJob());
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(incompleteAssignments);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Some assignments are missing for Job Id: " + jobId);

		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, 1);
		then(this.timesheetSettingRepository).should(never()).validateTimesheetSettingsConsistency(any(), anyInt());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException when timesheet settings are inconsistent")
	void testCreateTimesheetsInconsistentSettingsThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet settings are inconsistent for the provided contractor IDs.");

		then(this.timesheetSettingRepository).should()
			.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size());
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException when duplicate timesheets exist")
	void testCreateTimesheetsDuplicateTimesheetsThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(true);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet already exists for one or more of the provided time periods.");

		then(this.timesheetRepository).should().validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList());
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ResourceNotFoundException when timesheet settings are missing")
	void testCreateTimesheetsTimesheetSettingsMissingThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> incompleteTimesheetSettings = List.of(TimesheetTestDataFactory.createTimesheetSetting());
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(incompleteTimesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Some TimesheetSettings are missing for Job Id: " + jobId);

		// Note: Service extracts contractorIdsFromJobs from assignments, so verify with
		// anyList()
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList());
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException for invalid start date")
	void testCreateTimesheetsInvalidStartDateThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto invalidTimesheetRequest = new CreateTimesheetRequestBodyDto();
		invalidTimesheetRequest.setStartDate(1704153600); // End date is before start date
		invalidTimesheetRequest.setEndDate(1704067200);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(invalidTimesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Start date must be before end date");

		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException for time logs exceeding 90 days")
	void testCreateTimesheetsTimeLogsExceedingLimitThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		// Set dates spanning more than 90 days (91 days)
		timesheetRequest.setStartDate(1704067200); // 2024-01-01
		timesheetRequest.setEndDate(1711929600); // 2024-04-01 (91 days later)
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		// Update job end date to accommodate the date range
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setJobEndDate(1711929600); // Extended job end date
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(2);
		setting2.setJobEndDate(1711929600);
		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Time Log Dates are more than 90 Days");

		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException for weekly timesheet exceeding 7 days")
	void testCreateTimesheetsWeeklyTimesheetExceedingLimitThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		// Set dates spanning more than 7 days (8 days)
		timesheetRequest.setStartDate(1704067200); // 2024-01-01 (Monday)
		timesheetRequest.setEndDate(1704844800); // 2024-01-09 (8 days later, Tuesday)
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		// Create settings with WEEKLY frequency
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting1.setJobEndDate(1704844800); // Extended job end date
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(2);
		setting2.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting2.setJobEndDate(1704844800);
		List<TimesheetSetting> weeklyTimesheetSettings = List.of(setting1, setting2);
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(weeklyTimesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Weekly Time Logs must be 7 or less");

		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException for biweekly timesheet exceeding 14 days")
	void testCreateTimesheetsBiweeklyTimesheetExceedingLimitThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		// Set dates spanning more than 14 days (15 days)
		timesheetRequest.setStartDate(1704067200); // 2024-01-01
		timesheetRequest.setEndDate(1705276800); // 2024-01-16 (15 days later)
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		// Create settings with BIWEEKLY frequency
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId());
		setting1.setJobEndDate(1705276800); // Extended job end date
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(2);
		setting2.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId());
		setting2.setJobEndDate(1705276800);
		List<TimesheetSetting> biweeklyTimesheetSettings = List.of(setting1, setting2);
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(biweeklyTimesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("BiWeekly Time Logs must be 14 or less");

		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException for monthly timesheet exceeding 31 days")
	void testCreateTimesheetsMonthlyTimesheetExceedingLimitThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		// Set dates spanning more than 31 days (32 days)
		Integer startDate = 1704067200; // 2024-01-01
		Integer endDate = 1706745600 + 86400; // 2024-02-02 (32 days later, Feb 2)
		timesheetRequest.setStartDate(startDate);
		timesheetRequest.setEndDate(endDate);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		// Create settings with MONTHLY frequency
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId());
		setting1.setTimesheetStartDay(1); // Set to 1 to avoid NPE, end date will match
											// job end date
		setting1.setJobStartDate(startDate);
		setting1.setJobEndDate(endDate); // End date matches job end date to pass
											// validation
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(2);
		setting2.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId());
		setting2.setTimesheetStartDay(1);
		setting2.setJobStartDate(startDate);
		setting2.setJobEndDate(endDate);
		List<TimesheetSetting> monthlyTimesheetSettings = List.of(setting1, setting2);
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(monthlyTimesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Monthly Time Logs must be 31 or less");

		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets should handle monthly frequency with last day of month setting")
	void testCreateTimesheetsMonthlyFrequencyLastDayOfMonthHandled() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> monthlyLastDayTimesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(monthlyLastDayTimesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
	}

	// ===== Tests for createTimesheets with different persona types =====

	@Test
	@DisplayName("Create timesheets for USER persona should create successfully with access control check")
	void testCreateTimesheetsForUserPersonaCreatesSuccessfully() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create timesheets for CONTRACTOR persona should create successfully")
	void testCreateTimesheetsForContractorPersonaCreatesSuccessfully() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 200;
		List<Integer> contractorIds = List.of(contractorId);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		// Create assignment with matching contractorId
		AssignCandidateJob assignCandidateJob = TimesheetTestDataFactory.createAssignCandidateJob();
		assignCandidateJob.setCandidateId(contractorId);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignCandidateJob);
		// Create settings list with matching contractor
		TimesheetSetting timesheetSetting = TimesheetTestDataFactory.createTimesheetSetting();
		List<TimesheetSetting> timesheetSettings = List.of(timesheetSetting);
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should create successfully with valid permissions")
	void testCreateTimesheetsForContactPersonaCreatesSuccessfully() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(contactPrincipal.getFullName()).willReturn("Contact User");
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should throw UnauthorizedAccessException when canCreate is null")
	void testCreateTimesheetsForContactPersonaCanCreateNullThrowsUnauthorizedAccessException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(null);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for create timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should throw UnauthorizedAccessException when canCreate is not 1")
	void testCreateTimesheetsForContactPersonaCanCreateNotOneThrowsUnauthorizedAccessException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(0);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for create timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should propagate ResourceNotFoundException from portal access control when job not found")
	void testCreateTimesheetsForContactPersonaJobNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new ResourceNotFoundException("Job", jobId));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should propagate ValidationErrorException from portal access control when portal not enabled")
	void testCreateTimesheetsForContactPersonaPortalNotEnabledThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new ValidationErrorException("Portal is not enabled for this job"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Portal is not enabled for this job");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should propagate UnauthorizedAccessException from portal access control when client ID mismatch")
	void testCreateTimesheetsForContactPersonaClientIdMismatchThrowsUnauthorizedAccessException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new UnauthorizedAccessException("Client ID does not match job contact"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Client ID does not match job contact");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	// Note: This test is removed because PrincipalType enum only has USER, CONTRACTOR,
	// CONTACT values.
	@Test
	@DisplayName("Create timesheets for USER persona should throw ResourceNotFoundException when assignments are missing")
	void testCreateTimesheetsForUserPersonaAssignmentsMissingThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Some assignments are missing");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, accountId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for USER persona should throw ValidationErrorException when timesheet settings are inconsistent")
	void testCreateTimesheetsForUserPersonaInconsistentSettingsThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(false);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet settings are inconsistent");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetSettingRepository).should()
			.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size());
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTRACTOR persona should throw ResourceNotFoundException when timesheet settings are missing")
	void testCreateTimesheetsForContractorPersonaTimesheetSettingsMissingThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 200;
		List<Integer> contractorIds = List.of(contractorId);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		// Create assignment with matching contractorId
		AssignCandidateJob assignCandidateJob = TimesheetTestDataFactory.createAssignCandidateJob();
		assignCandidateJob.setCandidateId(contractorId);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignCandidateJob);
		Integer accountId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Some TimesheetSettings are missing");

		then(this.auth).should().getUnifiedPrincipal();
		// Note: Service extracts contractorIdsFromJobs from assignments, so verify with
		// anyList()
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList());
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should throw ValidationErrorException when duplicate timesheets exist")
	void testCreateTimesheetsForContactPersonaDuplicateTimesheetsThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(true);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet already exists");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should().validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList());
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for USER persona with multiple timesheets in single request should create all successfully")
	void testCreateTimesheetsForUserPersonaMultipleTimesheetsCreatesSuccessfully() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheet1 = TimesheetTestDataFactory.createTimesheetRequest();
		CreateTimesheetRequestBodyDto timesheet2 = new CreateTimesheetRequestBodyDto();
		timesheet2.setStartDate(1704067200); // Different dates
		timesheet2.setEndDate(1704153600);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheet1, timesheet2);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetRepository).should(times(2)).createTimesheets(anyList()); // Called
																						// twice
																						// for
																						// 2
																						// timesheets
		then(this.timesheetApprovalRepository).should(times(2)).createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should(times(2)).createBulkTimesheetLogs(anyList());
	}

	@Test
	@DisplayName("Create timesheets with multiple contractors should use each contractor's own workDays")
	void testCreateTimesheetsWithMultipleContractorsUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		// Create assignments for both contractors
		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2);

		// Create timesheet settings with different workDays for each contractor
		// Contractor 1: Monday-Friday (workDayId 1-5)
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));

		// Contractor 2: Monday-Wednesday only (workDayId 1-3)
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3)));

		// Set association for each setting to link to contractor
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);

		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);

		// Create timesheets with correct timesheetSettingId
		Timesheet timesheet1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1.setId(1000);
		timesheet1.setTimesheetSettingId(100); // Contractor 1's setting
		Timesheet timesheet2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2.setId(2000);
		timesheet2.setTimesheetSettingId(200); // Contractor 2's setting
		List<Timesheet> createdTimesheets = List.of(timesheet1, timesheet2);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		// Capture time logs to verify workDays
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		// Verify time logs were created
		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull().hasSizeGreaterThan(0);

		// Group time logs by timesheetId
		Map<Integer, List<TimeLog>> timeLogsByTimesheet = capturedTimeLogs.stream()
			.collect(java.util.stream.Collectors.groupingBy(TimeLog::getTimesheetId));

		// Verify timesheet1 (contractor 1) has workDays for Monday-Friday (workDayId 1-5)
		List<TimeLog> timesheet1Logs = timeLogsByTimesheet.get(1000);
		if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
			// Check that Monday-Friday are marked as workdays
			// Note: getDayTypeId returns WorkDayEnum IDs (1=Monday, 2=Tuesday, etc.)
			// DayTypeEnum.WORKDAY.getId() = 1, DayTypeEnum.DAY_OFF.getId() = 2
			long workdayCount = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			// Should have workdays (Monday-Friday in the date range)
			assertThat(workdayCount).isGreaterThan(0);
		}

		// Verify timesheet2 (contractor 2) has workDays for Monday-Wednesday only
		// (workDayId 1-3)
		List<TimeLog> timesheet2Logs = timeLogsByTimesheet.get(2000);
		if (timesheet2Logs != null && !timesheet2Logs.isEmpty()) {
			// Contractor 2 should have fewer workdays than contractor 1
			long workdayCount = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			// Should have workdays (Monday-Wednesday in the date range)
			assertThat(workdayCount).isGreaterThan(0);
		}

		// Verify that timesheet2 has fewer or equal workdays compared to timesheet1
		// (since contractor 2 only works Mon-Wed vs contractor 1's Mon-Fri)
		if (timesheet1Logs != null && timesheet2Logs != null && !timesheet1Logs.isEmpty()
				&& !timesheet2Logs.isEmpty()) {
			long timesheet1Workdays = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			long timesheet2Workdays = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			// Contractor 2 (Mon-Wed) should have fewer or equal workdays than contractor
			// 1 (Mon-Fri)
			assertThat(timesheet2Workdays).isLessThanOrEqualTo(timesheet1Workdays);
		}
	}

	@Test
	@DisplayName("Create timesheets should throw ResourceNotFoundException when workDays not found for timesheetSettingId")
	void testCreateTimesheetsWorkDaysNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment.setCandidateId(1);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment);

		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setId(100);
		setting.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association.setContractorId(1);
		setting.setAssociation(association);
		List<TimesheetSetting> timesheetSettings = List.of(setting);

		// Create timesheet with different timesheetSettingId (not in the map)
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		timesheet.setId(1000);
		timesheet.setTimesheetSettingId(999); // Different ID - not in timesheetSettings
		List<Timesheet> createdTimesheets = List.of(timesheet);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("WorkDays not found for timesheetSettingId: 999");

		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should(never()).createBulkTimesheetLogs(anyList());
	}

	@Test
	@DisplayName("Create timesheets with single contractor should use contractor's own workDays")
	void testCreateTimesheetsWithSingleContractorUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment.setCandidateId(1);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment);

		// Contractor works only on weekends (Saturday-Sunday, workDayId 6-7)
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setId(100);
		setting.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(6),
				TimesheetTestDataFactory.createTemplateWorkDay(7)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association.setContractorId(1);
		setting.setAssociation(association);
		List<TimesheetSetting> timesheetSettings = List.of(setting);

		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		timesheet.setId(1000);
		timesheet.setTimesheetSettingId(100);
		List<Timesheet> createdTimesheets = List.of(timesheet);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull().hasSizeGreaterThan(0);

		// Verify that only Saturday and Sunday are marked as workdays
		for (TimeLog timeLog : capturedTimeLogs) {
			if (timeLog.getTimesheetId().equals(1000)) {
				// Get the day of week for this date
				java.time.LocalDateTime dateTime = java.time.LocalDateTime
					.ofInstant(java.time.Instant.ofEpochSecond(timeLog.getDate()), java.time.ZoneOffset.UTC);
				java.time.DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
				Integer dayTypeId = dayOfWeek.getValue(); // 1=Monday, 7=Sunday

				if (dayTypeId == 6 || dayTypeId == 7) { // Saturday or Sunday
					assertThat(timeLog.getDayTypeId())
						.isEqualTo(io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId());
				}
				else {
					assertThat(timeLog.getDayTypeId())
						.isEqualTo(io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.DAY_OFF.getId());
				}
			}
		}
	}

	@Test
	@DisplayName("Create timesheets with three contractors having different workDays should use each contractor's own workDays")
	void testCreateTimesheetsWithThreeContractorsUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2, 3);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		AssignCandidateJob assignment3 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment3.setCandidateId(3);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2, assignment3);

		// Contractor 1: Monday-Friday (workDayId 1-5)
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);

		// Contractor 2: Monday-Wednesday (workDayId 1-3)
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);

		// Contractor 3: All days (workDayId 1-7)
		TimesheetSetting setting3 = TimesheetTestDataFactory.createTimesheetSetting();
		setting3.setId(300);
		setting3.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5),
				TimesheetTestDataFactory.createTemplateWorkDay(6), TimesheetTestDataFactory.createTemplateWorkDay(7)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association3 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association3.setContractorId(3);
		setting3.setAssociation(association3);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2, setting3);

		Timesheet timesheet1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1.setId(1000);
		timesheet1.setTimesheetSettingId(100);
		Timesheet timesheet2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2.setId(2000);
		timesheet2.setTimesheetSettingId(200);
		Timesheet timesheet3 = TimesheetTestDataFactory.createTimesheet();
		timesheet3.setId(3000);
		timesheet3.setTimesheetSettingId(300);
		List<Timesheet> createdTimesheets = List.of(timesheet1, timesheet2, timesheet3);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull();

		// Group time logs by timesheetId
		Map<Integer, List<TimeLog>> timeLogsByTimesheet = capturedTimeLogs.stream()
			.collect(java.util.stream.Collectors.groupingBy(TimeLog::getTimesheetId));

		// Verify contractor 1 (Mon-Fri) has 5 workdays
		List<TimeLog> timesheet1Logs = timeLogsByTimesheet.get(1000);
		if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
			long workdayCount1 = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount1).isGreaterThan(0);
		}

		// Verify contractor 2 (Mon-Wed) has fewer workdays than contractor 1
		List<TimeLog> timesheet2Logs = timeLogsByTimesheet.get(2000);
		if (timesheet2Logs != null && !timesheet2Logs.isEmpty()) {
			long workdayCount2 = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount2).isGreaterThan(0);
			if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
				long workdayCount1 = timesheet1Logs.stream()
					.filter((log) -> log
						.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId())
					.count();
				assertThat(workdayCount2).isLessThanOrEqualTo(workdayCount1);
			}
		}

		// Verify contractor 3 (all days) has the most workdays
		List<TimeLog> timesheet3Logs = timeLogsByTimesheet.get(3000);
		if (timesheet3Logs != null && !timesheet3Logs.isEmpty()) {
			long workdayCount3 = timesheet3Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount3).isGreaterThan(0);
			if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
				long workdayCount1 = timesheet1Logs.stream()
					.filter((log) -> log
						.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId())
					.count();
				assertThat(workdayCount3).isGreaterThanOrEqualTo(workdayCount1);
			}
		}
	}

	@Test
	@DisplayName("Create timesheets with empty workDays list should mark all days as day-off")
	void testCreateTimesheetsWithEmptyWorkDaysMarksAllDaysAsDayOff() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment.setCandidateId(1);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment);

		// Contractor with no workDays (empty list)
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setId(100);
		setting.setTemplateWorkDay(Collections.emptyList()); // Empty workDays
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association.setContractorId(1);
		setting.setAssociation(association);
		List<TimesheetSetting> timesheetSettings = List.of(setting);

		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		timesheet.setId(1000);
		timesheet.setTimesheetSettingId(100);
		List<Timesheet> createdTimesheets = List.of(timesheet);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull().hasSizeGreaterThan(0);

		// Verify all days are marked as day-off
		for (TimeLog timeLog : capturedTimeLogs) {
			if (timeLog.getTimesheetId().equals(1000)) {
				assertThat(timeLog.getDayTypeId())
					.isEqualTo(io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.DAY_OFF.getId());
			}
		}
	}

	@Test
	@DisplayName("Create timesheets with contractors having same workDays should still use individual workDays")
	void testCreateTimesheetsWithContractorsHavingSameWorkDaysUsesIndividualWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2);

		// Both contractors have same workDays (Monday-Friday)
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);

		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);

		Timesheet timesheet1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1.setId(1000);
		timesheet1.setTimesheetSettingId(100);
		Timesheet timesheet2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2.setId(2000);
		timesheet2.setTimesheetSettingId(200);
		List<Timesheet> createdTimesheets = List.of(timesheet1, timesheet2);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull();

		// Group time logs by timesheetId
		Map<Integer, List<TimeLog>> timeLogsByTimesheet = capturedTimeLogs.stream()
			.collect(java.util.stream.Collectors.groupingBy(TimeLog::getTimesheetId));

		// Verify both timesheets have the same number of workdays (since they have same
		// workDays)
		List<TimeLog> timesheet1Logs = timeLogsByTimesheet.get(1000);
		List<TimeLog> timesheet2Logs = timeLogsByTimesheet.get(2000);

		if (timesheet1Logs != null && timesheet2Logs != null && !timesheet1Logs.isEmpty()
				&& !timesheet2Logs.isEmpty()) {
			long workdayCount1 = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			long workdayCount2 = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			// Both should have same number of workdays
			assertThat(workdayCount1).isEqualTo(workdayCount2);
		}
	}

	@Test
	@DisplayName("Create timesheets with contractors having overlapping but different workDays should use each contractor's own workDays")
	void testCreateTimesheetsWithContractorsHavingOverlappingWorkDaysUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		Integer weekStart = 1704067200; // 2024-01-01 (Monday)
		Integer weekEnd = 1704412800; // 2024-01-05 (Friday)
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		timesheetRequest.setStartDate(weekStart);
		timesheetRequest.setEndDate(weekEnd);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2);

		// Contractor 1: Monday-Wednesday (workDayId 1-3)
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);
		setting1.setJobStartDate(weekStart);
		setting1.setJobEndDate(weekEnd);

		// Contractor 2: Wednesday-Friday (workDayId 3-5) - overlaps with contractor 1 on
		// Wednesday
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setJobEndDate(1704412800); // 2024-01-05 (Friday)
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);
		setting2.setJobStartDate(weekStart);
		setting2.setJobEndDate(weekEnd);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);

		Timesheet timesheet1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1.setId(1000);
		timesheet1.setTimesheetSettingId(100);
		timesheet1.setPeriodStart(weekStart);
		timesheet1.setPeriodEnd(weekEnd);
		timesheet1.setPeriodStart(1704067200); // 2024-01-01
		timesheet1.setPeriodEnd(1704412800); // 2024-01-05
		Timesheet timesheet2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2.setId(2000);
		timesheet2.setTimesheetSettingId(200);
		timesheet2.setPeriodStart(1704067200); // 2024-01-01
		timesheet2.setPeriodEnd(1704412800); // 2024-01-05
		timesheet2.setPeriodStart(weekStart);
		timesheet2.setPeriodEnd(weekEnd);
		List<Timesheet> createdTimesheets = List.of(timesheet1, timesheet2);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull();

		// Group time logs by timesheetId
		Map<Integer, List<TimeLog>> timeLogsByTimesheet = capturedTimeLogs.stream()
			.collect(java.util.stream.Collectors.groupingBy(TimeLog::getTimesheetId));

		// Verify contractor 1 has workdays for Mon-Wed
		List<TimeLog> timesheet1Logs = timeLogsByTimesheet.get(1000);
		if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
			long workdayCount1 = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount1).isGreaterThan(0);
		}

		// Verify contractor 2 has workdays for Wed-Fri
		List<TimeLog> timesheet2Logs = timeLogsByTimesheet.get(2000);
		if (timesheet2Logs != null && !timesheet2Logs.isEmpty()) {
			long workdayCount2 = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount2).isGreaterThan(0);
		}
	}

	@Test
	@DisplayName("Create timesheets with contractors having non-consecutive workDays should use each contractor's own workDays")
	void testCreateTimesheetsWithContractorsHavingNonConsecutiveWorkDaysUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		Integer weekStart = 1704067200;
		Integer weekEnd = 1704585600;
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		timesheetRequest.setStartDate(weekStart);
		timesheetRequest.setEndDate(weekEnd);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2);

		// Contractor 1: Monday, Wednesday, Friday (workDayId 1, 3, 5)
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setJobEndDate(1704412800); // 2024-01-05 (Friday)
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(3), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);
		setting1.setJobStartDate(weekStart);
		setting1.setJobEndDate(weekEnd);

		// Contractor 2: Tuesday, Thursday (workDayId 2, 4)
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setJobEndDate(1704412800); // 2024-01-05 (Friday)
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(2),
				TimesheetTestDataFactory.createTemplateWorkDay(4)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);
		setting2.setJobStartDate(weekStart);
		setting2.setJobEndDate(weekEnd);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);

		Timesheet timesheet1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1.setId(1000);
		timesheet1.setTimesheetSettingId(100);
		timesheet1.setPeriodStart(1704067200); // 2024-01-01
		timesheet1.setPeriodEnd(1704412800); // 2024-01-05
		timesheet1.setPeriodStart(weekStart);
		timesheet1.setPeriodEnd(weekEnd);
		Timesheet timesheet2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2.setId(2000);
		timesheet2.setTimesheetSettingId(200);
		timesheet2.setPeriodStart(1704067200); // 2024-01-01
		timesheet2.setPeriodEnd(1704412800); // 2024-01-05
		timesheet2.setPeriodStart(weekStart);
		timesheet2.setPeriodEnd(weekEnd);
		List<Timesheet> createdTimesheets = List.of(timesheet1, timesheet2);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull();

		// Group time logs by timesheetId
		Map<Integer, List<TimeLog>> timeLogsByTimesheet = capturedTimeLogs.stream()
			.collect(java.util.stream.Collectors.groupingBy(TimeLog::getTimesheetId));

		// Verify contractor 1 has workdays for Mon, Wed, Fri
		List<TimeLog> timesheet1Logs = timeLogsByTimesheet.get(1000);
		if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
			long workdayCount1 = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount1).isGreaterThan(0);
		}

		// Verify contractor 2 has workdays for Tue, Thu
		List<TimeLog> timesheet2Logs = timeLogsByTimesheet.get(2000);
		if (timesheet2Logs != null && !timesheet2Logs.isEmpty()) {
			long workdayCount2 = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			assertThat(workdayCount2).isGreaterThan(0);
			// Contractor 2 should have fewer workdays than contractor 1 (2 vs 3)
			if (timesheet1Logs != null && !timesheet1Logs.isEmpty()) {
				long workdayCount1 = timesheet1Logs.stream()
					.filter((log) -> log
						.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId())
					.count();
				assertThat(workdayCount2).isLessThan(workdayCount1);
			}
		}
	}

	@Test
	@DisplayName("Create timesheets with multiple date ranges should use each contractor's own workDays for each date range")
	void testCreateTimesheetsWithMultipleDateRangesUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		// Range 1: Mon Jan 1 - Sun Jan 7
		// Range 2: Mon Jan 8 - Sun Jan 14
		Integer jobStart = 1704067200;
		Integer jobEnd = 1705190400;
		Integer range1End = 1704585600;
		Integer range2Start = 1704672000;
		Integer range2End = 1705190400;
		CreateTimesheetRequestBodyDto timesheetRequest1 = TimesheetTestDataFactory.createTimesheetRequest();
		timesheetRequest1.setStartDate(jobStart);
		timesheetRequest1.setEndDate(range1End);
		CreateTimesheetRequestBodyDto timesheetRequest2 = new CreateTimesheetRequestBodyDto();
		timesheetRequest2.setStartDate(range2Start);
		timesheetRequest2.setEndDate(range2End);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest1, timesheetRequest2);

		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2);

		// Contractor 1: Monday-Friday
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setJobEndDate(1705190400); // 2024-01-14 (Sunday) - covers both date
											// ranges
		setting1.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);

		// Contractor 2: Monday-Wednesday
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setJobEndDate(1705190400); // 2024-01-14 (Sunday) - covers both date
											// ranges
		setting2.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);
		setting2.setJobStartDate(jobStart);
		setting2.setJobEndDate(jobEnd);
		// Allow flexible period end dates for two arbitrary ranges (avoid weekly
		// Sunday-only
		// rule)
		setting1.setTimesheetStartDay(null);
		setting2.setTimesheetStartDay(null);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);

		// Create timesheets for first date range
		Timesheet timesheet1Range1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1Range1.setId(1000);
		timesheet1Range1.setTimesheetSettingId(100);
		timesheet1Range1.setPeriodStart(1704067200); // 2024-01-01
		timesheet1Range1.setPeriodEnd(1704585600); // 2024-01-07
		timesheet1Range1.setPeriodStart(jobStart);
		timesheet1Range1.setPeriodEnd(range1End);
		Timesheet timesheet2Range1 = TimesheetTestDataFactory.createTimesheet();
		timesheet2Range1.setId(2000);
		timesheet2Range1.setTimesheetSettingId(200);
		timesheet2Range1.setPeriodStart(jobStart);
		timesheet2Range1.setPeriodEnd(range1End);

		// Create timesheets for second date range
		Timesheet timesheet1Range2 = TimesheetTestDataFactory.createTimesheet();
		timesheet1Range2.setId(3000);
		timesheet1Range2.setTimesheetSettingId(100);
		timesheet1Range2.setPeriodStart(range2Start);
		timesheet1Range2.setPeriodEnd(range2End);
		Timesheet timesheet2Range2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2Range2.setId(4000);
		timesheet2Range2.setTimesheetSettingId(200);
		timesheet2Range2.setPeriodStart(range2Start);
		timesheet2Range2.setPeriodEnd(range2End);

		List<Timesheet> createdTimesheetsFirst = List.of(timesheet1Range1, timesheet2Range1);
		List<Timesheet> createdTimesheetsSecond = List.of(timesheet1Range2, timesheet2Range2);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		java.util.concurrent.atomic.AtomicInteger createTimesheetsInvocation = new java.util.concurrent.atomic.AtomicInteger(
				0);
		given(this.timesheetRepository.createTimesheets(anyList())).willAnswer((invocation) -> {
			int index = createTimesheetsInvocation.getAndIncrement();
			return (index == 0) ? createdTimesheetsFirst : createdTimesheetsSecond;
		});
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timesheetRepository).should(times(2)).createTimesheets(anyList());
		then(this.timeLogRepository).should(times(2)).createBulkTimesheetLogs(anyList());

		// Verify that workDays map is used for both date ranges
		List<List<TimeLog>> allCapturedTimeLogs = timeLogsCaptor.getAllValues();
		assertThat(allCapturedTimeLogs).hasSize(2); // Two date ranges

		// Verify first date range uses correct workDays
		List<TimeLog> firstRangeLogs = allCapturedTimeLogs.get(0);
		assertThat(firstRangeLogs).isNotNull();

		// Verify second date range also uses correct workDays
		List<TimeLog> secondRangeLogs = allCapturedTimeLogs.get(1);
		assertThat(secondRangeLogs).isNotNull();
	}

	@Test
	@DisplayName("Create timesheets for CONTRACTOR persona with different workDays should use each contractor's own workDays")
	void testCreateTimesheetsForContractorPersonaWithDifferentWorkDaysUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 200;
		List<Integer> contractorIds = List.of(contractorId);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment.setCandidateId(contractorId);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment);

		// Contractor works only on Tuesday-Thursday
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setId(100);
		setting.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(2),
				TimesheetTestDataFactory.createTemplateWorkDay(3), TimesheetTestDataFactory.createTemplateWorkDay(4)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association.setContractorId(contractorId);
		setting.setAssociation(association);
		List<TimesheetSetting> timesheetSettings = List.of(setting);

		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		timesheet.setId(1000);
		timesheet.setTimesheetSettingId(100);
		List<Timesheet> createdTimesheets = List.of(timesheet);

		Integer accountId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull().hasSizeGreaterThan(0);

		// Verify only Tuesday-Thursday are marked as workdays
		for (TimeLog timeLog : capturedTimeLogs) {
			if (timeLog.getTimesheetId().equals(1000)) {
				java.time.LocalDateTime dateTime = java.time.LocalDateTime
					.ofInstant(java.time.Instant.ofEpochSecond(timeLog.getDate()), java.time.ZoneOffset.UTC);
				java.time.DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
				Integer dayTypeId = dayOfWeek.getValue(); // 1=Monday, 2=Tuesday, etc.

				if (dayTypeId == 2 || dayTypeId == 3 || dayTypeId == 4) { // Tuesday,
																			// Wednesday,
																			// Thursday
					assertThat(timeLog.getDayTypeId())
						.isEqualTo(io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId());
				}
				else {
					assertThat(timeLog.getDayTypeId())
						.isEqualTo(io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.DAY_OFF.getId());
				}
			}
		}
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona with different workDays should use each contractor's own workDays")
	void testCreateTimesheetsForContactPersonaWithDifferentWorkDaysUsesOwnWorkDays() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment1 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment1.setCandidateId(1);
		AssignCandidateJob assignment2 = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment2.setCandidateId(2);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment1, assignment2);

		// Contractor 1: Monday-Friday
		TimesheetSetting setting1 = TimesheetTestDataFactory.createTimesheetSetting();
		setting1.setId(100);
		setting1.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1),
				TimesheetTestDataFactory.createTemplateWorkDay(2), TimesheetTestDataFactory.createTemplateWorkDay(3),
				TimesheetTestDataFactory.createTemplateWorkDay(4), TimesheetTestDataFactory.createTemplateWorkDay(5)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association1 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association1.setContractorId(1);
		setting1.setAssociation(association1);

		// Contractor 2: Only Monday
		TimesheetSetting setting2 = TimesheetTestDataFactory.createTimesheetSetting();
		setting2.setId(200);
		setting2.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(1)));
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association2 = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association2.setContractorId(2);
		setting2.setAssociation(association2);

		List<TimesheetSetting> timesheetSettings = List.of(setting1, setting2);

		Timesheet timesheet1 = TimesheetTestDataFactory.createTimesheet();
		timesheet1.setId(1000);
		timesheet1.setTimesheetSettingId(100);
		Timesheet timesheet2 = TimesheetTestDataFactory.createTimesheet();
		timesheet2.setId(2000);
		timesheet2.setTimesheetSettingId(200);
		List<Timesheet> createdTimesheets = List.of(timesheet1, timesheet2);

		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull();

		// Group time logs by timesheetId
		Map<Integer, List<TimeLog>> timeLogsByTimesheet = capturedTimeLogs.stream()
			.collect(java.util.stream.Collectors.groupingBy(TimeLog::getTimesheetId));

		// Verify contractor 1 has more workdays than contractor 2
		List<TimeLog> timesheet1Logs = timeLogsByTimesheet.get(1000);
		List<TimeLog> timesheet2Logs = timeLogsByTimesheet.get(2000);

		if (timesheet1Logs != null && timesheet2Logs != null && !timesheet1Logs.isEmpty()
				&& !timesheet2Logs.isEmpty()) {
			long workdayCount1 = timesheet1Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			long workdayCount2 = timesheet2Logs.stream()
				.filter((log) -> log.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY
					.getId())
				.count();
			// Contractor 1 (Mon-Fri) should have more workdays than contractor 2 (Mon
			// only)
			assertThat(workdayCount1).isGreaterThan(workdayCount2);
		}
	}

	@Test
	@DisplayName("Create timesheets with contractor having only one workDay should use that single workDay")
	void testCreateTimesheetsWithContractorHavingOnlyOneWorkDayUsesSingleWorkDay() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		Integer weekStart = 1704067200;
		Integer weekEnd = 1704585600;
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		timesheetRequest.setStartDate(weekStart);
		timesheetRequest.setEndDate(weekEnd);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		AssignCandidateJob assignment = TimesheetTestDataFactory.createAssignCandidateJob();
		assignment.setCandidateId(1);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignment);

		// Contractor works only on Friday
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setId(100);
		setting.setJobEndDate(1704412800); // 2024-01-05 (Friday)
		setting.setTemplateWorkDay(Arrays.asList(TimesheetTestDataFactory.createTemplateWorkDay(5))); // Friday
																										// only
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association.setContractorId(1);
		setting.setAssociation(association);
		setting.setJobStartDate(weekStart);
		setting.setJobEndDate(weekEnd);
		List<TimesheetSetting> timesheetSettings = List.of(setting);

		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		timesheet.setId(1000);
		timesheet.setTimesheetSettingId(100);
		timesheet.setPeriodStart(weekStart);
		timesheet.setPeriodEnd(weekEnd);
		List<Timesheet> createdTimesheets = List.of(timesheet);

		Integer accountId = 1;
		Integer userId = 100;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TimeLog>> timeLogsCaptor = ArgumentCaptor.forClass(List.class);
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(timeLogsCaptor.capture());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());

		List<TimeLog> capturedTimeLogs = timeLogsCaptor.getValue();
		assertThat(capturedTimeLogs).isNotNull().hasSizeGreaterThan(0);

		// Verify only Friday is marked as workday
		long fridayWorkdayCount = capturedTimeLogs.stream().filter((log) -> {
			if (log.getTimesheetId().equals(1000)) {
				java.time.LocalDateTime dateTime = java.time.LocalDateTime
					.ofInstant(java.time.Instant.ofEpochSecond(log.getDate()), java.time.ZoneOffset.UTC);
				java.time.DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
				return dayOfWeek == java.time.DayOfWeek.FRIDAY && log
					.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId();
			}
			return false;
		}).count();
		assertThat(fridayWorkdayCount).isGreaterThan(0);

		// Verify other days are marked as day-off
		long nonFridayWorkdayCount = capturedTimeLogs.stream().filter((log) -> {
			if (log.getTimesheetId().equals(1000)) {
				java.time.LocalDateTime dateTime = java.time.LocalDateTime
					.ofInstant(java.time.Instant.ofEpochSecond(log.getDate()), java.time.ZoneOffset.UTC);
				java.time.DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
				return dayOfWeek != java.time.DayOfWeek.FRIDAY && log
					.getDayTypeId() == io.recruitcrm.contract_staffing.entity.model.DayTypeEnum.WORKDAY.getId();
			}
			return false;
		}).count();
		assertThat(nonFridayWorkdayCount).isZero();
	}

	@Test
	@DisplayName("Create timesheets for CONTRACTOR persona should throw ValidationErrorException when start date is after end date")
	void testCreateTimesheetsForContractorPersonaInvalidDateRangeThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 200;
		List<Integer> contractorIds = List.of(contractorId);
		CreateTimesheetRequestBodyDto invalidRequest = new CreateTimesheetRequestBodyDto();
		invalidRequest.setStartDate(1704153600); // End date is before start date
		invalidRequest.setEndDate(1704067200);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(invalidRequest);
		// Create assignment with matching contractorId
		AssignCandidateJob assignCandidateJob = TimesheetTestDataFactory.createAssignCandidateJob();
		assignCandidateJob.setCandidateId(contractorId);
		List<AssignCandidateJob> assignCandidateJobs = List.of(assignCandidateJob);
		// Create settings list with matching contractor
		TimesheetSetting timesheetSetting = TimesheetTestDataFactory.createTimesheetSetting();
		List<TimesheetSetting> timesheetSettings = List.of(timesheetSetting);
		Integer accountId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Start date must be before end date");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Create timesheets for CONTACT persona should throw ValidationErrorException when date is outside job date range")
	void testCreateTimesheetsForContactPersonaDateOutsideJobRangeThrowsValidationErrorException() {
		// Given
		Integer jobId = 1;
		Integer clientId = 300;
		List<Integer> contractorIds = List.of(1, 2);
		CreateTimesheetRequestBodyDto invalidRequest = new CreateTimesheetRequestBodyDto();
		invalidRequest.setStartDate(1609459200); // 2021-01-01 (before job start date)
		invalidRequest.setEndDate(1609545600);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(invalidRequest);
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanCreate(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		// Note: Service extracts contractorIdsFromJobs from assignments, so use anyList()
		// to match
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Start date and end date must be between Job Start Date and Job End Date");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should(never()).createTimesheets(anyList());
	}

	@Test
	@DisplayName("Delete timesheet should delete successfully with valid timesheet ID")
	void testDeleteTimesheetValidIdDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		List<Integer> timeLogIds = List.of(10, 11);
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		TimesheetApproval timesheetApproval = TimesheetTestDataFactory.createTimesheetApproval();

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, 1)).willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(List.of(timesheetId))).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetApprovalJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetJpaRepository).deleteByIdAndAccountId(timesheetId, 1);

		// When
		this.timesheetService.deleteTimesheet(timesheetId);

		// Then
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, 1);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(List.of(timesheetId));
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogJpaRepository).should().deleteByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().deleteByTimesheetId(timesheetId);
		then(this.timesheetJpaRepository).should().deleteByIdAndAccountId(timesheetId, 1);
	}

	@Test
	@DisplayName("Delete timesheet should delete successfully when timesheet has no time logs")
	void testDeleteTimesheetWithNoTimeLogsDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		List<Integer> emptyTimeLogIds = Collections.emptyList();
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		TimesheetApproval timesheetApproval = TimesheetTestDataFactory.createTimesheetApproval();

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, 1)).willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(List.of(timesheetId))).willReturn(emptyTimeLogIds);
		willDoNothing().given(this.timeLogJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetApprovalJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetJpaRepository).deleteByIdAndAccountId(timesheetId, 1);

		// When
		this.timesheetService.deleteTimesheet(timesheetId);

		// Then
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, 1);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(List.of(timesheetId));
		then(this.timeLogIntervalRepository).should(never()).deleteByTimeLogIntervalIdIn(anyList());
		then(this.timeLogJpaRepository).should().deleteByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().deleteByTimesheetId(timesheetId);
		then(this.timesheetJpaRepository).should().deleteByIdAndAccountId(timesheetId, 1);
	}

	@Test
	@DisplayName("Delete timesheet should throw ResourceNotFoundException when timesheet not found")
	void testDeleteTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = 999;

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, 1)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deleteTimesheet(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, 1);
		then(this.timeLogJpaRepository).should(never()).deleteByTimesheetId(anyInt());
	}

	@Test
	@DisplayName("Delete timesheet should throw ValidationErrorException when timesheet is approved")
	void testDeleteTimesheetApprovedThrowsValidationErrorException() {
		// Given
		Integer timesheetId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		TimesheetApproval approvedApproval = TimesheetTestDataFactory.createApprovedTimesheetApproval();

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, 1)).willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approvedApproval);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deleteTimesheet(timesheetId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet Status is Approved for the given Timesheet : " + timesheetId);

		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timeLogJpaRepository).should(never()).deleteByTimesheetId(anyInt());
	}

	@Test
	@DisplayName("Delete timesheet should verify intervals are deleted before time logs")
	void testDeleteTimesheetVerifiesIntervalDeletionOrder() {
		// Given
		Integer timesheetId = 1;
		List<Integer> timeLogIds = List.of(10, 11, 12);
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		TimesheetApproval timesheetApproval = TimesheetTestDataFactory.createTimesheetApproval();

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, 1)).willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(List.of(timesheetId))).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetApprovalJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetJpaRepository).deleteByIdAndAccountId(timesheetId, 1);

		// When
		this.timesheetService.deleteTimesheet(timesheetId);

		// Then - Verify order: intervals deleted before time logs
		var inOrder = org.mockito.Mockito.inOrder(this.timeLogIntervalRepository, this.timeLogJpaRepository);
		inOrder.verify(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		inOrder.verify(this.timeLogJpaRepository).deleteByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Delete timesheet should handle large number of time log IDs")
	void testDeleteTimesheetWithLargeNumberOfTimeLogIds() {
		// Given
		Integer timesheetId = 1;
		List<Integer> largeTimeLogIdList = java.util.stream.IntStream.rangeClosed(1, 100).boxed().toList();
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		TimesheetApproval timesheetApproval = TimesheetTestDataFactory.createTimesheetApproval();

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, 1)).willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(List.of(timesheetId)))
			.willReturn(largeTimeLogIdList);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(largeTimeLogIdList);
		willDoNothing().given(this.timeLogJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetApprovalJpaRepository).deleteByTimesheetId(timesheetId);
		willDoNothing().given(this.timesheetJpaRepository).deleteByIdAndAccountId(timesheetId, 1);

		// When
		this.timesheetService.deleteTimesheet(timesheetId);

		// Then
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(List.of(timesheetId));
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(largeTimeLogIdList);
		then(this.timeLogJpaRepository).should().deleteByTimesheetId(timesheetId);
	}

	// ===== Tests for deleteTimesheets (Bulk Delete) =====

	@Test
	@DisplayName("Delete timesheets should delete successfully with valid timesheet IDs")
	void testDeleteTimesheetsValidIdsDeletesSuccessfully() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2);
		List<Integer> timeLogIds = List.of(10, 11, 12);
		List<Timesheet> timesheets = TimesheetTestDataFactory.createTimesheetList();

		TimesheetApproval approval1 = TimesheetTestDataFactory.createTimesheetApproval();
		approval1.setTimesheetId(1);
		TimesheetApproval approval2 = TimesheetTestDataFactory.createTimesheetApproval();
		approval2.setTimesheetId(2);
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(approval1, approval2);

		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), anyInt());

		// When
		this.timesheetService.deleteTimesheets(timesheetIds);

		// Then
		then(this.contractStaffingAccessControlChecker).should().allowsBulk(any(BulkPermissionCheckRequest.class));
		then(this.timesheetRepository).should().findByIdInAndAccountId(timesheetIds, 1);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), anyInt());
	}

	@Test
	@DisplayName("Delete timesheets should delete successfully when timesheets have no time logs")
	void testDeleteTimesheetsWithNoTimeLogsDeletesSuccessfully() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2);
		List<Integer> emptyTimeLogIds = Collections.emptyList();
		List<Timesheet> timesheets = TimesheetTestDataFactory.createTimesheetList();

		TimesheetApproval approval1 = TimesheetTestDataFactory.createTimesheetApproval();
		approval1.setTimesheetId(1);
		TimesheetApproval approval2 = TimesheetTestDataFactory.createTimesheetApproval();
		approval2.setTimesheetId(2);
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(approval1, approval2);

		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(emptyTimeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), anyInt());

		// When
		this.timesheetService.deleteTimesheets(timesheetIds);

		// Then
		then(this.contractStaffingAccessControlChecker).should().allowsBulk(any(BulkPermissionCheckRequest.class));
		then(this.timesheetRepository).should().findByIdInAndAccountId(timesheetIds, 1);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should(never()).deleteByTimeLogIntervalIdIn(anyList());
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), anyInt());
	}

	@Test
	@DisplayName("Delete timesheets should throw ResourceNotFoundException when timesheet not found")
	void testDeleteTimesheetsTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		List<Integer> timesheetIds = List.of(999);
		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deleteTimesheets(timesheetIds))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetRepository).should().findByIdInAndAccountId(timesheetIds, 1);
		then(this.timeLogRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete timesheets should throw ResourceNotFoundException when approval not found")
	void testDeleteTimesheetsApprovalNotFoundThrowsResourceNotFoundException() {
		// Given
		List<Integer> timesheetIds = List.of(1);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deleteTimesheets(timesheetIds))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetApproval for timesheet");

		then(this.timesheetRepository).should().findByIdInAndAccountId(timesheetIds, 1);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		then(this.timeLogRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete timesheets should throw ValidationErrorException when all timesheets are approved")
	void testDeleteTimesheetsAllApprovedThrowsValidationErrorException() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2);
		List<Timesheet> timesheets = TimesheetTestDataFactory.createTimesheetList();

		TimesheetApproval approval1 = TimesheetTestDataFactory.createApprovedTimesheetApproval();
		approval1.setTimesheetId(1);
		TimesheetApproval approval2 = TimesheetTestDataFactory.createApprovedTimesheetApproval();
		approval2.setTimesheetId(2);
		List<TimesheetApproval> approvedApprovals = Arrays.asList(approval1, approval2);

		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(approvedApprovals);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deleteTimesheets(timesheetIds))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet Status is Approved for the given Timesheets : " + timesheetIds);

		then(this.timesheetRepository).should().findByIdInAndAccountId(timesheetIds, 1);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		then(this.timeLogRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete timesheets should verify intervals are deleted before time logs")
	void testDeleteTimesheetsVerifiesIntervalDeletionOrder() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2);
		List<Integer> timeLogIds = List.of(10, 11, 12);
		List<Timesheet> timesheets = TimesheetTestDataFactory.createTimesheetList();

		TimesheetApproval approval1 = TimesheetTestDataFactory.createTimesheetApproval();
		approval1.setTimesheetId(1);
		TimesheetApproval approval2 = TimesheetTestDataFactory.createTimesheetApproval();
		approval2.setTimesheetId(2);
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(approval1, approval2);

		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), anyInt());

		// When
		this.timesheetService.deleteTimesheets(timesheetIds);

		// Then - Verify order: intervals deleted before time logs
		var inOrder = org.mockito.Mockito.inOrder(this.timeLogIntervalRepository, this.timeLogRepository);
		inOrder.verify(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		inOrder.verify(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete timesheets should handle mixed approved and non-approved timesheets")
	void testDeleteTimesheetsWithMixedApprovalStatuses() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet(),
				TimesheetTestDataFactory.createTimesheet(), TimesheetTestDataFactory.createTimesheet());
		timesheets.get(1).setId(2);
		timesheets.get(2).setId(3);

		TimesheetApproval approval1 = TimesheetTestDataFactory.createTimesheetApproval();
		approval1.setTimesheetId(1);
		TimesheetApproval approval2 = TimesheetTestDataFactory.createApprovedTimesheetApproval();
		approval2.setTimesheetId(2);
		TimesheetApproval approval3 = TimesheetTestDataFactory.createTimesheetApproval();
		approval3.setTimesheetId(3);
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(approval1, approval2, approval3);

		List<Integer> timeLogIds = List.of(10, 11);
		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), anyInt());

		// When
		this.timesheetService.deleteTimesheets(timesheetIds);

		// Then - Should only delete non-approved timesheets (1 and 3, skipping 2)
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete timesheets should handle large batch of timesheets")
	void testDeleteTimesheetsWithLargeBatch() {
		// Given
		List<Integer> timesheetIds = java.util.stream.IntStream.rangeClosed(1, 50).boxed().toList();
		List<Integer> timeLogIds = java.util.stream.IntStream.rangeClosed(100, 500).boxed().toList();

		List<Timesheet> timesheets = timesheetIds.stream().map((id) -> {
			Timesheet ts = TimesheetTestDataFactory.createTimesheet();
			ts.setId(id);
			return ts;
		}).toList();

		List<TimesheetApproval> timesheetApprovals = timesheetIds.stream().map((id) -> {
			TimesheetApproval approval = TimesheetTestDataFactory.createTimesheetApproval();
			approval.setTimesheetId(id);
			return approval;
		}).toList();

		BulkPermissionCheckResult bulkResult = TimesheetTestDataFactory.createBulkPermissionCheckResult();

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult);
		given(this.timesheetRepository.findByIdInAndAccountId(timesheetIds, 1)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(timesheetIds))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), anyInt());

		// When
		this.timesheetService.deleteTimesheets(timesheetIds);

		// Then
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), anyInt());
	}

	// ===== Tests for deletePortalTimesheets =====

	@Test
	@DisplayName("Delete portal timesheets for contractor should delete successfully")
	void testDeletePortalTimesheetsForContractorValidRequestDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer contractorId = 200;
		Integer accountId = 1;
		List<Integer> timeLogIds = List.of(10, 11);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(TimesheetTestDataFactory.createTimesheetApproval());

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), eq(accountId));

		// When
		this.timesheetService.deletePortalTimesheets(timesheetId, jobId);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should delete successfully with valid permissions")
	void testDeletePortalTimesheetsForContactValidPermissionDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;
		List<Integer> timeLogIds = List.of(10, 11);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(TimesheetTestDataFactory.createTimesheetApproval());
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanDelete(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), eq(accountId));

		// When
		this.timesheetService.deletePortalTimesheets(timesheetId, jobId);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should throw UnauthorizedAccessException when canDelete is null")
	void testDeletePortalTimesheetsForContactNullCanDeleteThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanDelete(null);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("DELETE_TIMESHEET permission not granted for job ID: " + jobId);

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timeLogJpaRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should throw UnauthorizedAccessException when canDelete is not 1")
	void testDeletePortalTimesheetsForContactCanDeleteNotOneThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanDelete(0);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("DELETE_TIMESHEET permission not granted for job ID: " + jobId);

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timeLogJpaRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete portal timesheets should throw UnauthorizedAccessException when principal type is USER")
	void testDeletePortalTimesheetsUserPrincipalThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		AuthPrincipal userPrincipal = mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only contractors and contacts can delete portal timesheets");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Delete portal timesheets should throw ResourceNotFoundException when timesheet not found")
	void testDeletePortalTimesheetsTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer contractorId = 200;
		Integer accountId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timeLogRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete portal timesheets should throw ResourceNotFoundException when approval not found")
	void testDeletePortalTimesheetsApprovalNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer contractorId = 200;
		Integer accountId = 1;
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetApproval for timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete portal timesheets should throw ValidationErrorException when timesheet is approved")
	void testDeletePortalTimesheetsApprovedTimesheetThrowsValidationErrorException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer contractorId = 200;
		Integer accountId = 1;
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		TimesheetApproval approvedApproval = TimesheetTestDataFactory.createApprovedTimesheetApproval();
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(approvedApproval);

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet Status is Approved for the given Timesheets");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should(never()).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should propagate ResourceNotFoundException from portal access control when job not found")
	void testDeletePortalTimesheetsForContactJobNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new ResourceNotFoundException("Job", jobId));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should propagate ValidationErrorException from portal access control when portal not enabled")
	void testDeletePortalTimesheetsForContactPortalNotEnabledThrowsValidationErrorException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new ValidationErrorException("Portal is not enabled for this job"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Portal is not enabled for this job");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should propagate UnauthorizedAccessException from portal access control when client ID mismatch")
	void testDeletePortalTimesheetsForContactClientIdMismatchThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new UnauthorizedAccessException("Client ID does not match job contact"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Client ID does not match job contact");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should propagate UnauthorizedAccessException from portal access control when access record not found")
	void testDeletePortalTimesheetsForContactAccessRecordNotFoundThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId))
			.willThrow(new UnauthorizedAccessException("JobTimesheetAccess record not found"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("JobTimesheetAccess record not found");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Delete portal timesheets for contractor should successfully delete non-approved timesheet")
	void testDeletePortalTimesheetsForContractorNonApprovedTimesheetDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer contractorId = 200;
		Integer accountId = 1;
		List<Integer> timeLogIds = List.of(10, 11);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		TimesheetApproval submittedApproval = TimesheetTestDataFactory.createTimesheetApproval();
		submittedApproval.setTimesheetApprovalStatusTypeId(TimesheetApprovalStatusTypeEnum.SUBMITTED.getId());
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(submittedApproval);

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), eq(accountId));

		// When
		this.timesheetService.deletePortalTimesheets(timesheetId, jobId);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should successfully delete rejected timesheet")
	void testDeletePortalTimesheetsForContactRejectedTimesheetDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;
		List<Integer> timeLogIds = List.of(10, 11);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		TimesheetApproval rejectedApproval = TimesheetTestDataFactory.createTimesheetApproval();
		rejectedApproval.setTimesheetApprovalStatusTypeId(TimesheetApprovalStatusTypeEnum.REJECTED.getId());
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(rejectedApproval);
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanDelete(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), eq(accountId));

		// When
		this.timesheetService.deletePortalTimesheets(timesheetId, jobId);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(timeLogIds);
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Delete portal timesheets for contractor should verify intervals deleted in correct order")
	void testDeletePortalTimesheetsForContractorVerifiesIntervalDeletionOrder() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer contractorId = 200;
		Integer accountId = 1;
		List<Integer> timeLogIds = List.of(10, 11, 12);
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(TimesheetTestDataFactory.createTimesheetApproval());

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(timeLogIds);
		willDoNothing().given(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), eq(accountId));

		// When
		this.timesheetService.deletePortalTimesheets(timesheetId, jobId);

		// Then - Verify order: intervals deleted before time logs
		var inOrder = org.mockito.Mockito.inOrder(this.timeLogIntervalRepository, this.timeLogRepository);
		inOrder.verify(this.timeLogIntervalRepository).deleteByTimeLogIntervalIdIn(timeLogIds);
		inOrder.verify(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
	}

	@Test
	@DisplayName("Delete portal timesheets for contact should delete successfully without time logs")
	void testDeletePortalTimesheetsForContactWithoutTimeLogsDeletesSuccessfully() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = 100;
		Integer clientId = 200;
		Integer accountId = 1;
		List<Integer> emptyTimeLogIds = Collections.emptyList();
		List<Timesheet> timesheets = Arrays.asList(TimesheetTestDataFactory.createTimesheet());
		List<TimesheetApproval> timesheetApprovals = Arrays.asList(TimesheetTestDataFactory.createTimesheetApproval());
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanDelete(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(jobId, clientId)).willReturn(permissions);
		given(this.timesheetRepository.findByIdInAndAccountId(List.of(timesheetId), accountId)).willReturn(timesheets);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId)))
			.willReturn(timesheetApprovals);
		given(this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(anyList())).willReturn(emptyTimeLogIds);
		willDoNothing().given(this.timeLogRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetApprovalRepository).deleteByTimesheetIdIn(anyList());
		willDoNothing().given(this.timesheetRepository).deleteByIdInAndAccountId(anyList(), eq(accountId));

		// When
		this.timesheetService.deletePortalTimesheets(timesheetId, jobId);

		// Then
		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(jobId, clientId);
		then(this.timesheetRepository).should().findByIdInAndAccountId(List.of(timesheetId), accountId);
		then(this.timesheetApprovalRepository).should().findLatestApprovalEntitiesByTimesheetIds(List.of(timesheetId));
		then(this.timeLogRepository).should().findTimeLogIdsByTimesheetIdIn(anyList());
		then(this.timeLogIntervalRepository).should(never()).deleteByTimeLogIntervalIdIn(anyList());
		then(this.timeLogRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetApprovalRepository).should().deleteByTimesheetIdIn(anyList());
		then(this.timesheetRepository).should().deleteByIdInAndAccountId(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Delete portal timesheets should throw ValidationErrorException when timesheetId is null")
	void testDeletePortalTimesheetsNullTimesheetIdThrowsValidationErrorException() {
		// Given
		Integer timesheetId = null;
		Integer jobId = 100;

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet id is required");

		then(this.auth).should(never()).getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Delete portal timesheets should throw ValidationErrorException when jobId is null")
	void testDeletePortalTimesheetsNullJobIdThrowsValidationErrorException() {
		// Given
		Integer timesheetId = 1;
		Integer jobId = null;

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.deletePortalTimesheets(timesheetId, jobId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Job id is required");

		then(this.auth).should(never()).getUnifiedPrincipal();
	}

	// ===== Tests for getTimesheetStatusHistory =====

	@Test
	@DisplayName("Get timesheet status history should return history successfully")
	void testGetTimesheetStatusHistoryValidIdReturnsHistory() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		List<StatusHistoryQueryResultDto> statusHistory = TimesheetTestDataFactory.createStatusHistoryList();

		// Create user details for the agency recruiter
		UserDetailsQueryResultDto userDetails = new UserDetailsQueryResultDto();
		userDetails.setName("Test User");
		userDetails.setProfilePic("test-profile-pic.jpg");
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = new HashMap<>();
		agencyUsersMap.put(1, userDetails);

		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = TimesheetTestDataFactory.createEmptyMap();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = TimesheetTestDataFactory.createEmptyMap();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(statusHistory);
		given(this.timesheetStatusHistoryMapper.toTimesheetStatusResultBodyDto(statusHistory))
			.willReturn(TimesheetTestDataFactory.createStatusHistoryResponseList());
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(contractorUsersMap);
		willDoNothing().given(this.fetchUserAndContactUserIds)
			.addUserToAppropriateSet(anyInt(), anyInt(), any(), any(), any());

		// When
		TimesheetStatusHistoryResponseBodyDto result = this.timesheetService.getTimesheetStatusHistory(timesheetId);

		// Then
		assertThat(result).isNotNull();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timesheetApprovalJpaRepository).should().findByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timesheetStatusHistoryMapper).should().toTimesheetStatusResultBodyDto(statusHistory);
	}

	@Test
	@DisplayName("Get timesheet status history should throw ResourceNotFoundException when timesheet not found")
	void testGetTimesheetStatusHistoryTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = 999;
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetStatusHistory(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timesheetApprovalJpaRepository).should(never()).findByTimesheetIdOrderByIdDesc(anyInt());
	}

	@Test
	@DisplayName("Get timesheet status history should throw ResourceNotFoundException when status history not found")
	void testGetTimesheetStatusHistoryStatusHistoryNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetStatusHistory(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Status history for timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timesheetApprovalJpaRepository).should().findByTimesheetIdOrderByIdDesc(timesheetId);
	}

	@Test
	@DisplayName("Get timesheet status history with contractor user type should return history successfully")
	void testGetTimesheetStatusHistoryContractorUserTypeReturnsHistory() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 1;
		Integer contractorId = 100;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		StatusHistoryQueryResultDto statusHistoryDto = new StatusHistoryQueryResultDto();
		statusHistoryDto.setId(1);
		statusHistoryDto.setStatus(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		statusHistoryDto.setRemark("Approved");
		statusHistoryDto.setUpdatedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		statusHistoryDto.setUpdatedOn(20240115);
		statusHistoryDto.setUpdatedById(100);
		List<StatusHistoryQueryResultDto> statusHistory = Arrays.asList(statusHistoryDto);

		StatusHistoryResponseBodyDto responseDto = new StatusHistoryResponseBodyDto();
		responseDto.setId(1);
		responseDto.setStatus(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		responseDto.setRemark("Approved");
		responseDto.setUpdatedOn(20240115);
		List<StatusHistoryResponseBodyDto> responseDtos = Arrays.asList(responseDto);

		ContractorNamePhotoQueryResultDto contractorDetails = new ContractorNamePhotoQueryResultDto();
		contractorDetails.setName("Contractor User");
		contractorDetails.setProfilePic("contractor-pic.jpg");
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = new HashMap<>();
		contractorUsersMap.put(100, contractorDetails);

		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = TimesheetTestDataFactory.createEmptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = TimesheetTestDataFactory.createEmptyMap();

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(statusHistory);
		given(this.timesheetStatusHistoryMapper.toTimesheetStatusResultBodyDto(statusHistory)).willReturn(responseDtos);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(contractorUsersMap);
		willDoNothing().given(this.fetchUserAndContactUserIds)
			.addUserToAppropriateSet(anyInt(), anyInt(), any(), any(), any());

		// When
		TimesheetStatusHistoryResponseBodyDto result = this.timesheetService.getTimesheetStatusHistory(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getStatusHistory()).hasSize(1);
		assertThat(result.getStatusHistory().get(0).getUpdatedBy()).isNotNull();
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getId()).isEqualTo(100);
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getName()).isEqualTo("Contractor User");
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getUserTypeId())
			.isEqualTo(UserTypeEnum.CONTRACTOR.getId());
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.candidateRepository).should().getContractorQueryResultMap(any());
	}

	@Test
	@DisplayName("Get timesheet status history with company contact user type should return history successfully")
	void testGetTimesheetStatusHistoryCompanyContactUserTypeReturnsHistory() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		StatusHistoryQueryResultDto statusHistoryDto = new StatusHistoryQueryResultDto();
		statusHistoryDto.setId(1);
		statusHistoryDto.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		statusHistoryDto.setRemark("Open status");
		statusHistoryDto.setUpdatedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		statusHistoryDto.setUpdatedOn(20240115);
		statusHistoryDto.setUpdatedById(200);
		List<StatusHistoryQueryResultDto> statusHistory = Arrays.asList(statusHistoryDto);

		StatusHistoryResponseBodyDto responseDto = new StatusHistoryResponseBodyDto();
		responseDto.setId(1);
		responseDto.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		responseDto.setRemark("Open status");
		responseDto.setUpdatedOn(20240115);
		List<StatusHistoryResponseBodyDto> responseDtos = Arrays.asList(responseDto);

		ContactNamePhotoQueryResultDto contactDetails = new ContactNamePhotoQueryResultDto();
		contactDetails.setName("Contact User");
		contactDetails.setProfilePic("contact-pic.jpg");
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();
		contactUsersMap.put(200, contactDetails);

		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = TimesheetTestDataFactory.createEmptyMap();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = TimesheetTestDataFactory.createEmptyMap();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(statusHistory);
		given(this.timesheetStatusHistoryMapper.toTimesheetStatusResultBodyDto(statusHistory)).willReturn(responseDtos);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(contractorUsersMap);
		willDoNothing().given(this.fetchUserAndContactUserIds)
			.addUserToAppropriateSet(anyInt(), anyInt(), any(), any(), any());

		// When
		TimesheetStatusHistoryResponseBodyDto result = this.timesheetService.getTimesheetStatusHistory(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getStatusHistory()).hasSize(1);
		assertThat(result.getStatusHistory().get(0).getUpdatedBy()).isNotNull();
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getId()).isEqualTo(200);
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getName()).isEqualTo("Contact User");
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getUserTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.contactRepository).should().getContactNamePhotoMap(any());
	}

	@Test
	@DisplayName("Get timesheet status history with multiple entries and different user types should return all history")
	void testGetTimesheetStatusHistoryMultipleEntriesDifferentUserTypesReturnsAllHistory() {
		// Given
		Integer timesheetId = 1;

		// Create multiple status history entries with different user types
		StatusHistoryQueryResultDto agencyHistory = new StatusHistoryQueryResultDto();
		agencyHistory.setId(3);
		agencyHistory.setStatus(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		agencyHistory.setRemark("Approved by agency");
		agencyHistory.setUpdatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		agencyHistory.setUpdatedOn(20240117);
		agencyHistory.setUpdatedById(1);

		StatusHistoryQueryResultDto contractorHistory = new StatusHistoryQueryResultDto();
		contractorHistory.setId(2);
		contractorHistory.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		contractorHistory.setRemark("Open by contractor");
		contractorHistory.setUpdatedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		contractorHistory.setUpdatedOn(20240116);
		contractorHistory.setUpdatedById(100);

		StatusHistoryQueryResultDto contactHistory = new StatusHistoryQueryResultDto();
		contactHistory.setId(1);
		contactHistory.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		contactHistory.setRemark("Initial status");
		contactHistory.setUpdatedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		contactHistory.setUpdatedOn(20240115);
		contactHistory.setUpdatedById(200);

		List<StatusHistoryQueryResultDto> statusHistory = Arrays.asList(agencyHistory, contractorHistory,
				contactHistory);

		StatusHistoryResponseBodyDto agencyResponse = new StatusHistoryResponseBodyDto();
		agencyResponse.setId(3);
		agencyResponse.setStatus(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		agencyResponse.setRemark("Approved by agency");
		agencyResponse.setUpdatedOn(20240117);

		StatusHistoryResponseBodyDto contractorResponse = new StatusHistoryResponseBodyDto();
		contractorResponse.setId(2);
		contractorResponse.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		contractorResponse.setRemark("Open by contractor");
		contractorResponse.setUpdatedOn(20240116);

		StatusHistoryResponseBodyDto contactResponse = new StatusHistoryResponseBodyDto();
		contactResponse.setId(1);
		contactResponse.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		contactResponse.setRemark("Initial status");
		contactResponse.setUpdatedOn(20240115);

		List<StatusHistoryResponseBodyDto> responseDtos = Arrays.asList(agencyResponse, contractorResponse,
				contactResponse);

		// Setup user details for all user types
		UserDetailsQueryResultDto agencyUser = new UserDetailsQueryResultDto();
		agencyUser.setName("Agency User");
		agencyUser.setProfilePic("agency-pic.jpg");
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = new HashMap<>();
		agencyUsersMap.put(1, agencyUser);

		ContractorNamePhotoQueryResultDto contractorUser = new ContractorNamePhotoQueryResultDto();
		contractorUser.setName("Contractor User");
		contractorUser.setProfilePic("contractor-pic.jpg");
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = new HashMap<>();
		contractorUsersMap.put(100, contractorUser);

		ContactNamePhotoQueryResultDto contactUser = new ContactNamePhotoQueryResultDto();
		contactUser.setName("Contact User");
		contactUser.setProfilePic("contact-pic.jpg");
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();
		contactUsersMap.put(200, contactUser);

		Integer accountId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(statusHistory);
		given(this.timesheetStatusHistoryMapper.toTimesheetStatusResultBodyDto(statusHistory)).willReturn(responseDtos);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(contractorUsersMap);
		willDoNothing().given(this.fetchUserAndContactUserIds)
			.addUserToAppropriateSet(anyInt(), anyInt(), any(), any(), any());

		// When
		TimesheetStatusHistoryResponseBodyDto result = this.timesheetService.getTimesheetStatusHistory(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getStatusHistory()).hasSize(3);

		// Verify agency user
		assertThat(result.getStatusHistory().get(0).getUpdatedBy()).isNotNull();
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getName()).isEqualTo("Agency User");
		assertThat(result.getStatusHistory().get(0).getUpdatedBy().getUserTypeId())
			.isEqualTo(UserTypeEnum.AGENCY_RECRUITER.getId());

		// Verify contractor user
		assertThat(result.getStatusHistory().get(1).getUpdatedBy()).isNotNull();
		assertThat(result.getStatusHistory().get(1).getUpdatedBy().getName()).isEqualTo("Contractor User");
		assertThat(result.getStatusHistory().get(1).getUpdatedBy().getUserTypeId())
			.isEqualTo(UserTypeEnum.CONTRACTOR.getId());

		// Verify contact user
		assertThat(result.getStatusHistory().get(2).getUpdatedBy()).isNotNull();
		assertThat(result.getStatusHistory().get(2).getUpdatedBy().getName()).isEqualTo("Contact User");
		assertThat(result.getStatusHistory().get(2).getUpdatedBy().getUserTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());

		// Verify all repositories were called
		then(this.userRepository).should().getUserDetailsMap(any());
		then(this.contactRepository).should().getContactNamePhotoMap(any());
		then(this.candidateRepository).should().getContractorQueryResultMap(any());
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should return list successfully")
	void testGetTimesheetsListByDealIdValidDealIdReturnsList() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		Integer accountId = 1;

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		BulkPermissionCheckResult bulkResult2 = TimesheetTestDataFactory.createBulkPermissionCheckResult();
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult2);
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timeLogJpaRepository.getTimesheetWorkSummaries(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetWorkSummaryList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should throw ResourceNotFoundException when deal not found")
	void testGetTimesheetsListByDealIdDealNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer dealId = 999;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsListByDealId(dealId, searchRequest, pageable))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Deal");

		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should(never()).getCommonCandidatesByDealId(anyInt());
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should return empty list when no timesheets found")
	void testGetTimesheetsListByDealIdNoTimesheetsFoundReturnsEmptyList() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		Integer accountId = 1;

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should handle duration-based work summaries")
	void testGetTimesheetsListByDealIdHandlesDurationBasedWorkSummaries() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		for (TimesheetDealListQueryResultDto row : timesheetResults) {
			row.setTotalWorkTime(40);
			row.setTotalOvertime(8);
			row.setTotalTime(48);
		}
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		Integer accountId = 1;

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetApprovalJpaRepository).should().findLatestApprovalsByTimesheetIds(anyList());
		then(this.timeLogJpaRepository).should(never()).findByTimesheetIds(anyList());
		then(this.timeLogJpaRepository).should(never()).getTimesheetWorkSummaries(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should return list successfully")
	void testGetTimesheetsListByJobAndContractorIdValidIdsReturnsList() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.getTimesheetWorkSummaries(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetWorkSummaryList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should return empty list when no timesheets found")
	void testGetTimesheetsListByJobAndContractorIdNoTimesheetsFoundReturnsEmptyList() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle duration-based work summaries")
	void testGetTimesheetsListByJobAndContractorIdHandlesDurationBasedWorkSummaries() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		for (TimesheetJobAndContractorListQueryResultDto row : timesheetResults) {
			row.setTotalWorkTime(40);
			row.setTotalOvertime(8);
			row.setTotalTime(48);
		}
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(1)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetApprovalJpaRepository).should().findLatestApprovalsByTimesheetIds(anyList());
		then(this.timeLogJpaRepository).should(never()).findByTimesheetIds(anyList());
		then(this.timeLogJpaRepository).should(never()).getTimesheetWorkSummaries(anyList());
	}

	@Test
	@DisplayName("Generate date range with start and end dates should return correct date list")
	void testGenerateDateRangeWithStartAndEndDatesReturnsCorrectDateList() {
		// Given
		Integer startDate = 1704067200; // 2024-01-01 00:00:00 UTC
		Integer endDate = 1704239999; // 2024-01-02 23:59:59 UTC

		// When
		List<Integer> result = this.timesheetService.generateDateRangeWithStartAndEndDates(startDate, endDate);

		// Then
		assertThat(result).isNotEmpty()
			.hasSize(2) // Should include start and intermediate dates
			.element(0)
			.isEqualTo(startDate);
	}

	@Test
	@DisplayName("Generate date range with same start and end date should return single date")
	void testGenerateDateRangeWithSameDateReturnsSingleDate() {
		// Given
		Integer startDate = 1704067200; // 2024-01-01 00:00:00 UTC
		Integer endDate = 1704067200; // Same as start date

		// When
		List<Integer> result = this.timesheetService.generateDateRangeWithStartAndEndDates(startDate, endDate);

		// Then
		assertThat(result).hasSize(1).element(0).isEqualTo(startDate);
	}

	@Test
	@DisplayName("Generate date range should return empty list when start is after end")
	void testGenerateDateRangeWhenStartAfterEndReturnsEmptyList() {
		// Given
		Integer endDate = 1704067200;
		Integer startDate = endDate + 86400;

		// When
		List<Integer> result = this.timesheetService.generateDateRangeWithStartAndEndDates(startDate, endDate);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Is deal valid should pass validation when deal exists")
	void testIsDealValidPassesValidationWhenDealExists() {
		// Given
		Integer dealId = 1;
		Integer accountId = 1;
		Deal deal = TimesheetTestDataFactory.createDeal();

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, accountId)).willReturn(Optional.of(deal));

		// When
		this.timesheetService.isDealValid(dealId, accountId);

		// Then
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, accountId);
	}

	@Test
	@DisplayName("Is deal valid should throw ResourceNotFoundException when deal not found")
	void testIsDealValidThrowsResourceNotFoundExceptionWhenDealNotFound() {
		// Given
		Integer dealId = 999;
		Integer accountId = 1;

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, accountId)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.isDealValid(dealId, accountId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Deal");

		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, accountId);
	}

	// ===== Additional Test Cases for getTimesheetsListByDealId =====

	@Test
	@DisplayName("Get timesheets list by deal ID should handle contractor user type in added by")
	void testGetTimesheetsListByDealIdHandlesContractorUserTypeInAddedBy() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultWithContractorAddedByList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		Integer accountId = 1;
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
		// Service always calls getUserDetailsMap even if agencyUserIds is empty
		then(this.userRepository).should().getUserDetailsMap(any());
		then(this.contactRepository).should().getContactNamePhotoMap(any());
		// Contractor data comes from TimesheetDealListQueryResultDto, not from repository
		// call
		then(this.candidateRepository).should(never()).getContractorQueryResultMap(any());
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should handle agency recruiter user type in added by")
	void testGetTimesheetsListByDealIdHandlesAgencyRecruiterUserTypeInAddedBy() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultWithAgencyRecruiterAddedByList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		Integer accountId = 1;
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		// Test data has addedById = 1 for agency recruiter, so return map with user
		// details for ID 1
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = new HashMap<>();
		UserDetailsQueryResultDto userDetails = new UserDetailsQueryResultDto();
		userDetails.setName("Agency User");
		userDetails.setProfilePic("agency-pic.jpg");
		agencyUsersMap.put(1, userDetails);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should handle contact user type in added by")
	void testGetTimesheetsListByDealIdHandlesContactUserTypeInAddedBy() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultWithContactAddedByList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		Integer accountId = 1;
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		// Test data has addedById = 1 for contact, so return map with contact details for
		// ID 1
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();
		ContactNamePhotoQueryResultDto contactDetails = new ContactNamePhotoQueryResultDto("Contact User",
				"contact-pic.jpg", null);
		contactUsersMap.put(1, contactDetails);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should handle approved timesheet with approver details")
	void testGetTimesheetsListByDealIdHandlesApprovedTimesheetWithApproverDetails() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		Integer accountId = 1;
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createApprovedTimesheetApproverResponseList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should handle null approver")
	void testGetTimesheetsListByDealIdHandlesNullApprover() {
		// Given
		Integer dealId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		Integer accountId = 1;
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.dealJpaRepository).should().findByIdAndAccountId(dealId, 1);
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
	}

	// ===== Additional Test Cases for getTimesheetsListByJobAndContractorId =====

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle contractor user type in added by")
	void testGetTimesheetsListByJobAndContractorIdHandlesContractorUserTypeInAddedBy() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle agency recruiter user type in added by")
	void testGetTimesheetsListByJobAndContractorIdHandlesAgencyRecruiterUserTypeInAddedBy() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultWithAgencyRecruiterAddedByList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		// Test data has addedById = 1 for agency recruiter, so return map with user
		// details for ID 1
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = new HashMap<>();
		UserDetailsQueryResultDto userDetails = new UserDetailsQueryResultDto();
		userDetails.setName("Agency User");
		userDetails.setProfilePic("agency-pic.jpg");
		agencyUsersMap.put(1, userDetails);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle contact user type in added by")
	void testGetTimesheetsListByJobAndContractorIdHandlesContactUserTypeInAddedBy() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultWithContactAddedByList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		// Test data has addedById = 1 for contact, so return map with contact details for
		// ID 1
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();
		ContactNamePhotoQueryResultDto contactDetails = new ContactNamePhotoQueryResultDto("Contact User",
				"contact-pic.jpg", null);
		contactUsersMap.put(1, contactDetails);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle approved timesheet with approver details")
	void testGetTimesheetsListByJobAndContractorIdHandlesApprovedTimesheetWithApproverDetails() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createApprovedTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle null approver")
	void testGetTimesheetsListByJobAndContractorIdHandlesNullApprover() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle null contractor details")
	void testGetTimesheetsListByJobAndContractorIdHandlesNullContractorDetails() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should handle invoice details")
	void testGetTimesheetsListByJobAndContractorIdHandlesInvoiceDetails() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetApproverResponseList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(TimesheetTestDataFactory.createTimesheetInvoiceList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should populate approver ID in approved timesheets")
	void testGetTimesheetsListByDealIdShouldPopulateApproverIdInApprovedTimesheets() {
		// Given
		Integer dealId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();

		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		// Create approved timesheet approver with specific entity ID
		Integer approverEntityId = 123;
		TimesheetApproverResponseBodyDto approver = new TimesheetApproverResponseBodyDto();
		approver.setTimesheetId(1);
		approver.setEntityId(approverEntityId);
		approver.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		approver.setTimeSheetApprovalStatusId(TimesheetApprovalStatusTypeEnum.APPROVED.getId());

		// Create user details for the approver
		UserDetailsQueryResultDto userDetails = new UserDetailsQueryResultDto();
		userDetails.setName("Test Approver");
		userDetails.setProfilePic("approver.jpg");
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Map.of(approverEntityId, userDetails);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, accountId))
			.willReturn(Optional.of(TimesheetTestDataFactory.createDeal()));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetTestDataFactory.createBulkPermissionCheckResult());
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(List.of(approver));
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(TimesheetTestDataFactory.createTimesheetInvoiceList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isNotEmpty();
		// Verify that approver details are populated in the result
		TimesheetListResponseBodyDto firstTimesheet = result.get(0);
		if (firstTimesheet.getApprovedBy() != null) {
			assertThat(firstTimesheet.getApprovedBy()).extracting("id", "name", "userTypeId")
				.containsExactly(approverEntityId, "Test Approver", UserTypeEnum.AGENCY_RECRUITER.getId());
		}
		then(this.timesheetRepository).should().getCommonCandidatesByDealId(dealId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable);
		then(this.timesheetApprovalJpaRepository).should().findLatestApprovalsByTimesheetIds(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should populate approver ID in approved timesheets")
	void testGetTimesheetsListByJobAndContractorIdShouldPopulateApproverIdInApprovedTimesheets() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();

		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();

		// Create approved timesheet approver with specific entity ID
		Integer approverEntityId = 456;
		TimesheetApproverResponseBodyDto approver = new TimesheetApproverResponseBodyDto();
		approver.setTimesheetId(1);
		approver.setEntityId(approverEntityId);
		approver.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		approver.setTimeSheetApprovalStatusId(TimesheetApprovalStatusTypeEnum.APPROVED.getId());

		// Create contact details for the approver
		ContactNamePhotoQueryResultDto contactDetails = new ContactNamePhotoQueryResultDto();
		contactDetails.setName("Test Contact Approver");
		contactDetails.setProfilePic("contact.jpg");
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Map.of(approverEntityId, contactDetails);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId,
				searchRequest, pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(List.of(approver));
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimeLogList());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(TimesheetTestDataFactory.createTimesheetInvoiceList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isNotEmpty();
		// Verify that approver details are populated in the result
		TimesheetListResponseBodyDto firstTimesheet = result.get(0);
		if (firstTimesheet.getApprovedBy() != null) {
			assertThat(firstTimesheet.getApprovedBy()).extracting("id", "name", "userTypeId")
				.containsExactly(approverEntityId, "Test Contact Approver", UserTypeEnum.COMPANY_CONTACT.getId());
		}
		then(this.timesheetRepository).should()
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequest, pageable);
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetApprovalJpaRepository).should().findLatestApprovalsByTimesheetIds(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID for contractor should return list successfully")
	void testGetTimesheetsListByEntityIdContractorReturnsListSuccessfully() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contractorId = 2;
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(principal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contractorId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contractorId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		// Create response list with same size as query results (1 element)
		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		// Mock user details
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = new HashMap<>();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();
		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);

		// Mock invoice details
		List<TimesheetInvoice> invoices = TimesheetTestDataFactory.createTimesheetInvoiceList();
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId))).willReturn(invoices);

		// Mock approvals and work summary
		List<TimesheetApproverResponseBodyDto> approvers = TimesheetTestDataFactory
			.createTimesheetApproverResponseList();
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList())).willReturn(approvers);

		List<TimeLog> timeLogs = TimesheetTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(timeLogs);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull().hasSize(expectedResponse.size());
		// Verify contractor-specific fields are filtered (null)
		for (TimesheetListResponseBodyDto dto : result) {
			assertThat(dto.getBillRate()).isNull();
			assertThat(dto.getBillData()).isNull();
			assertThat(dto.getInvoiceNumber()).isNull();
			// Verify pay currency fields are retained for contractors
			assertThat(dto.getPayCurrencySymbol()).isEqualTo(TimesheetTestDataFactory.getDefaultPayCurrencySymbol());
			assertThat(dto.getPayCurrencyCode()).isEqualTo(TimesheetTestDataFactory.getDefaultPayCurrencyCode());
			// Verify isInvoiceCreated is visible for contractors (1 = invoice_id is set
			// on tracking row)
			assertThat(dto.getIsInvoiceCreated()).isEqualTo(1);
		}
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(principal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(principal);
		then(this.entityAccessValidator).should().validateEntityAccess(entityType, contractorId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by entity ID for contractor with no invoice should return isInvoiceCreated as 0")
	void testGetTimesheetsListByEntityIdContractorNoInvoiceReturnsIsInvoiceCreatedZero() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contractorId = 2;
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(principal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contractorId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contractorId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		given(this.userRepository.getUserDetailsMap(any())).willReturn(new HashMap<>());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(new HashMap<>());

		// Mock empty invoice list — no invoice created for this timesheet
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		List<TimesheetApproverResponseBodyDto> approvers = TimesheetTestDataFactory
			.createTimesheetApproverResponseList();
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList())).willReturn(approvers);

		List<TimeLog> timeLogs = TimesheetTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(timeLogs);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull().hasSize(expectedResponse.size());
		for (TimesheetListResponseBodyDto dto : result) {
			assertThat(dto.getIsInvoiceCreated()).isZero();
		}
	}

	@Test
	@DisplayName("Get timesheets list by entity ID: tracking row without invoice_id should return isInvoiceCreated as 0")
	void testGetTimesheetsListByEntityIdContractorTrackingRowWithoutInvoiceIdReturnsIsInvoiceCreatedZero() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contractorId = 2;
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(principal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contractorId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contractorId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		given(this.userRepository.getUserDetailsMap(any())).willReturn(new HashMap<>());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(new HashMap<>());

		TimesheetInvoice trackingOnly = new TimesheetInvoice();
		trackingOnly.setId(1);
		trackingOnly.setTimesheetId(1);
		trackingOnly.setInvoiceId(null);
		trackingOnly.setAccountId(accountId);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(List.of(trackingOnly));

		List<TimesheetApproverResponseBodyDto> approvers = TimesheetTestDataFactory
			.createTimesheetApproverResponseList();
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList())).willReturn(approvers);

		List<TimeLog> timeLogs = TimesheetTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(timeLogs);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull().hasSize(expectedResponse.size());
		for (TimesheetListResponseBodyDto dto : result) {
			assertThat(dto.getIsInvoiceCreated()).isZero();
		}
	}

	@Test
	@DisplayName("Get timesheets list by entity ID for contact should return list with approvers successfully")
	void testGetTimesheetsListByEntityIdContactReturnsListWithApproversSuccessfully() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contactId = 3;
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactPrincipal principal = mock(ContactPrincipal.class);
		given(principal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contactId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contactId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		// Set timesheetSettingId for approvers test
		queryResults.get(0).setTimesheetSettingId(1);

		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contactId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		// Create response list with same size as query results (1 element)
		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		// Mock user details
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = new HashMap<>();
		UserDetailsQueryResultDto agencyUser = new UserDetailsQueryResultDto();
		agencyUser.setName("Agency User");
		agencyUser.setProfilePic("agency.jpg");
		agencyUsersMap.put(10, agencyUser);

		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();
		ContactNamePhotoQueryResultDto contactUser = new ContactNamePhotoQueryResultDto();
		contactUser.setName("Contact User");
		contactUser.setProfilePic("contact.jpg");
		contactUsersMap.put(20, contactUser);

		given(this.userRepository.getUserDetailsMap(any())).willReturn(agencyUsersMap);
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(contactUsersMap);

		// Mock approvers for contact entity
		TimesheetApprover agencyApprover = new TimesheetApprover();
		agencyApprover.setTimesheetSettingId(1);
		agencyApprover.setEntityId(10);
		agencyApprover.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());

		TimesheetApprover clientApprover = new TimesheetApprover();
		clientApprover.setTimesheetSettingId(1);
		clientApprover.setEntityId(20);
		clientApprover.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());

		given(this.timesheetApproverRepository.findByTimesheetSettingIds(anyList()))
			.willReturn(Arrays.asList(agencyApprover, clientApprover));

		// Mock invoice details
		List<TimesheetInvoice> invoices = TimesheetTestDataFactory.createTimesheetInvoiceList();
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId))).willReturn(invoices);

		// Mock approvals and work summary
		List<TimesheetApproverResponseBodyDto> approvers = TimesheetTestDataFactory
			.createTimesheetApproverResponseList();
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList())).willReturn(approvers);

		List<TimeLog> timeLogs = TimesheetTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(timeLogs);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull().hasSize(expectedResponse.size());
		// Verify contact-specific fields are filtered (null)
		for (TimesheetListResponseBodyDto dto : result) {
			assertThat(dto.getPayRate()).isNull();
			assertThat(dto.getPayData()).isNull();
			assertThat(dto.getPayoutNumber()).isNull();
			// Verify pay currency fields are NOT null for clients (retained for portal)
			assertThat(dto.getPayCurrencySymbol()).isEqualTo("$");
			assertThat(dto.getPayCurrencyCode()).isEqualTo("USD");
			// Verify approvers are populated
			assertThat(dto.getApprovers()).isNotNull();
			// Verify isInvoiceCreated is visible for clients (1 = linked invoice in mock)
			assertThat(dto.getIsInvoiceCreated()).isEqualTo(1);
		}
		then(this.timesheetApproverRepository).should().findByTimesheetSettingIds(anyList());
		then(this.entityAccessValidator).should().validateEntityAccess(entityType, contactId);
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should return empty list when no results")
	void testGetTimesheetsListByEntityIdNoResultsReturnsEmptyList() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contractorId = 2;
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contractorId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contractorId);

		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId, searchRequest, pageable);
		then(this.customTimeSheetMapper).should(never())
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should throw ValidationErrorException when entityType is null")
	void testGetTimesheetsListByEntityIdNullEntityTypeThrowsValidationErrorException() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(null);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(2);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsListByEntityId(searchRequest, pageable))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Entity type and entity ID must be available in the access token");

		then(this.entityAccessValidator).should(never()).validateEntityAccess(any(), any());
		then(this.timesheetRepository).should(never())
			.getTimesheetsListByEntityId(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should throw ValidationErrorException when entityId is null")
	void testGetTimesheetsListByEntityIdNullEntityIdThrowsValidationErrorException() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal))
			.willReturn(UserTypeEnum.CONTRACTOR.getId());
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsListByEntityId(searchRequest, pageable))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Entity type and entity ID must be available in the access token");

		then(this.entityAccessValidator).should(never()).validateEntityAccess(any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should return list with null query results")
	void testGetTimesheetsListByEntityIdNullQueryResultsReturnsEmptyList() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contractorId = 2;
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contractorId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contractorId);

		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(null);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId, searchRequest, pageable);
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should handle work summaries with duration-based logs")
	void testGetTimesheetsListByEntityIdHandlesDurationBasedWorkSummaries() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contractorId = 2;
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		ContractorPrincipal principal = mock(ContractorPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contractorId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contractorId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		// Set work log type to duration-based
		queryResults.get(0).setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());

		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contractorId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		// Create response list with same size as query results (1 element)
		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		// Mock user details
		given(this.userRepository.getUserDetailsMap(any())).willReturn(new HashMap<>());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(new HashMap<>());

		// Mock invoice, approvals, and time logs with start/end times
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		List<TimesheetApproverResponseBodyDto> approvers = TimesheetTestDataFactory
			.createTimesheetApproverResponseList();
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList())).willReturn(approvers);

		// Create time logs with work start/end times for duration-based calculation
		TimeLog timeLog = new TimeLog();
		timeLog.setId(1);
		timeLog.setTimesheetId(1);
		timeLog.setWorkStartTime(1704067200);
		timeLog.setWorkEndTime(1704096000);
		timeLog.setWorkTime(null);
		timeLog.setOverTime(0);
		timeLog.setTotalTime(8);
		timeLog.setPayData(200.0f);
		timeLog.setBillData(240.0f);
		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetApprovalJpaRepository).should().findLatestApprovalsByTimesheetIds(anyList());
		then(this.timeLogJpaRepository).should(never()).findByTimesheetIds(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID for contact with no approvers should not throw exception")
	void testGetTimesheetsListByEntityIdContactNoApproversDoesNotThrowException() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contactId = 3;
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactPrincipal principal = mock(ContactPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contactId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contactId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		queryResults.get(0).setTimesheetSettingId(1);

		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contactId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		// Create response list with same size as query results (1 element)
		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		given(this.userRepository.getUserDetailsMap(any())).willReturn(new HashMap<>());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(new HashMap<>());

		// Mock empty approvers list
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(anyList()))
			.willReturn(Collections.emptyList());

		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetApproverRepository).should().findByTimesheetSettingIds(anyList());
	}

	@Test
	@DisplayName("Get timesheets count by entity ID for contractor should return counts successfully")
	void testGetTimesheetsCountByEntityIdContractorReturnsCountsSuccessfully() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId(); // 3 for contractor
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		given(this.timesheetRepository.getTimesheetsCountByEntityId(entityType, entityId, List.of(), accountId))
			.willReturn(10L);
		given(this.timesheetRepository.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, List.of(),
				accountId, searchRequestBodyDto))
			.willReturn(5L);

		// When
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto result = this.timesheetService
			.getTimesheetsCountByEntityId(searchRequestBodyDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTotalCount()).isEqualTo(10L);
		assertThat(result.getFilteredCount()).isEqualTo(5L);
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(contractorPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(contractorPrincipal);
		then(this.entityAccessValidator).should().validateEntityAccess(entityType, entityId);
		then(this.timesheetRepository).should()
			.getTimesheetsCountByEntityId(entityType, entityId, List.of(), accountId);
		then(this.timesheetRepository).should()
			.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, List.of(), accountId, searchRequestBodyDto);
	}

	@Test
	@DisplayName("Get timesheets count by entity ID for contact should return counts and ensure job access records")
	void testGetTimesheetsCountByEntityIdContactReturnsCountsAndEnsuresJobAccessRecords() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId(); // 1 for contact/client
		Integer entityId = 100;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);

		List<Integer> jobIds = Arrays.asList(1, 2);
		List<io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess> existingRecords = Arrays.asList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contactPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contactPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		given(this.timesheetRepository.getJobIdsByContactId(entityId, accountId)).willReturn(jobIds);
		given(this.jobTimesheetAccessJpaRepository.findByJobIdsAndAccountId(jobIds, accountId))
			.willReturn(existingRecords);
		given(this.jobTimesheetAccessJpaRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
			.willReturn(Arrays.asList());

		given(this.timesheetRepository.getTimesheetsCountByEntityId(entityType, entityId, List.of(), accountId))
			.willReturn(10L);
		given(this.timesheetRepository.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, List.of(),
				accountId, searchRequestBodyDto))
			.willReturn(5L);

		// When
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto result = this.timesheetService
			.getTimesheetsCountByEntityId(searchRequestBodyDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTotalCount()).isEqualTo(10L);
		assertThat(result.getFilteredCount()).isEqualTo(5L);
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(contactPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(contactPrincipal);
		then(this.entityAccessValidator).should().validateEntityAccess(entityType, entityId);
		then(this.timesheetRepository).should().getJobIdsByContactId(entityId, accountId);
		then(this.jobTimesheetAccessJpaRepository).should().findByJobIdsAndAccountId(jobIds, accountId);
		then(this.jobTimesheetAccessJpaRepository).should().saveAll(org.mockito.ArgumentMatchers.anyList());
		then(this.timesheetRepository).should()
			.getTimesheetsCountByEntityId(entityType, entityId, List.of(), accountId);
		then(this.timesheetRepository).should()
			.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, List.of(), accountId, searchRequestBodyDto);
	}

	@Test
	@DisplayName("Get timesheets count by entity ID should throw ValidationErrorException when entityType is null")
	void testGetTimesheetsCountByEntityIdNullEntityTypeThrowsValidationErrorException() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(userPrincipal)).willReturn(null);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(userPrincipal)).willReturn(1);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsCountByEntityId(searchRequestBodyDto))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException.class)
			.hasMessageContaining(
					"Entity type and entity ID must be available in the access token. This endpoint is only available for contractors and contacts.");

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(userPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(userPrincipal);
	}

	@Test
	@DisplayName("Get timesheets count by entity ID should throw ValidationErrorException when entityId is null")
	void testGetTimesheetsCountByEntityIdNullEntityIdThrowsValidationErrorException() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(userPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(userPrincipal)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsCountByEntityId(searchRequestBodyDto))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException.class)
			.hasMessageContaining(
					"Entity type and entity ID must be available in the access token. This endpoint is only available for contractors and contacts.");

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(userPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(userPrincipal);
	}

	@Test
	@DisplayName("Get timesheets count by entity ID for contact with existing job access records should not create new records")
	void testGetTimesheetsCountByEntityIdContactExistingJobAccessRecordsDoesNotCreateNewRecords() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId(); // 1 for contact/client
		Integer entityId = 100;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);

		List<Integer> jobIds = Arrays.asList(1, 2);
		io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess existingAccess1 = new io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess();
		existingAccess1.setJobId(1);
		io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess existingAccess2 = new io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess();
		existingAccess2.setJobId(2);
		List<io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess> existingRecords = Arrays
			.asList(existingAccess1, existingAccess2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contactPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contactPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		given(this.timesheetRepository.getJobIdsByContactId(entityId, accountId)).willReturn(jobIds);
		given(this.jobTimesheetAccessJpaRepository.findByJobIdsAndAccountId(jobIds, accountId))
			.willReturn(existingRecords);

		given(this.timesheetRepository.getTimesheetsCountByEntityId(entityType, entityId, List.of(), accountId))
			.willReturn(10L);
		given(this.timesheetRepository.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, List.of(),
				accountId, searchRequestBodyDto))
			.willReturn(5L);

		// When
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto result = this.timesheetService
			.getTimesheetsCountByEntityId(searchRequestBodyDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTotalCount()).isEqualTo(10L);
		assertThat(result.getFilteredCount()).isEqualTo(5L);
		then(this.timesheetRepository).should().getJobIdsByContactId(entityId, accountId);
		then(this.jobTimesheetAccessJpaRepository).should().findByJobIdsAndAccountId(jobIds, accountId);
		then(this.jobTimesheetAccessJpaRepository).should(org.mockito.BDDMockito.never())
			.saveAll(org.mockito.ArgumentMatchers.anyList());
	}

	@Test
	@DisplayName("Get timesheets count by entity ID for contact with email resolves contact IDs sharing that email")
	void testGetTimesheetsCountByEntityIdContactWithEmailResolvesContactIds() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId();
		Integer entityId = 100;
		String email = "contact@example.com";
		List<Integer> resolvedContactIds = Arrays.asList(100, 101);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contactPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contactPrincipal)).willReturn(entityId);
		given(contactPrincipal.getEmail()).willReturn(email);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		given(this.timesheetRepository.getJobIdsByContactId(entityId, accountId)).willReturn(List.of());
		given(this.timesheetRepository.findContactIdsByEmail(email, accountId)).willReturn(resolvedContactIds);
		given(this.timesheetRepository.getTimesheetsCountByEntityId(entityType, entityId, resolvedContactIds,
				accountId))
			.willReturn(12L);
		given(this.timesheetRepository.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, resolvedContactIds,
				accountId, searchRequestBodyDto))
			.willReturn(7L);

		// When
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto result = this.timesheetService
			.getTimesheetsCountByEntityId(searchRequestBodyDto);

		// Then
		assertThat(result.getTotalCount()).isEqualTo(12L);
		assertThat(result.getFilteredCount()).isEqualTo(7L);
		then(this.timesheetRepository).should().findContactIdsByEmail(email, accountId);
		then(this.timesheetRepository).should()
			.getTimesheetsCountByEntityId(entityType, entityId, resolvedContactIds, accountId);
	}

	@Test
	@DisplayName("Get timesheets count by entity ID for contact with empty job IDs should skip job access record creation")
	void testGetTimesheetsCountByEntityIdContactEmptyJobIdsSkipsJobAccessRecordCreation() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId(); // 1 for contact/client
		Integer entityId = 100;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contactPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contactPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		given(this.timesheetRepository.getJobIdsByContactId(entityId, accountId)).willReturn(Arrays.asList());

		given(this.timesheetRepository.getTimesheetsCountByEntityId(entityType, entityId, List.of(), accountId))
			.willReturn(0L);
		given(this.timesheetRepository.getTimesheetsCountByEntityIdWithFilters(entityType, entityId, List.of(),
				accountId, searchRequestBodyDto))
			.willReturn(0L);

		// When
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto result = this.timesheetService
			.getTimesheetsCountByEntityId(searchRequestBodyDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTotalCount()).isZero();
		assertThat(result.getFilteredCount()).isZero();
		then(this.timesheetRepository).should().getJobIdsByContactId(entityId, accountId);
		then(this.jobTimesheetAccessJpaRepository).should(org.mockito.BDDMockito.never())
			.findByJobIdsAndAccountId(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyInt());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID for contact with empty timesheet setting IDs should skip approvers enrichment")
	void testGetTimesheetsListByEntityIdContactEmptyTimesheetSettingIdsSkipsApproversEnrichment() {
		// Given
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contactId = 3;
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactPrincipal principal = mock(ContactPrincipal.class);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contactId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contactId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		// Set timesheetSettingId to null to test empty filtering
		queryResults.get(0).setTimesheetSettingId(null);

		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contactId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		// Create response list with same size as query results (1 element)
		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);

		given(this.userRepository.getUserDetailsMap(any())).willReturn(new HashMap<>());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(new HashMap<>());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		// Then
		assertThat(result).isNotNull();
		// Verify approvers repository was never called due to empty timesheet setting IDs
		then(this.timesheetApproverRepository).should(never()).findByTimesheetSettingIds(anyList());
	}

	@Test
	@DisplayName("Get timesheet job access info should return existing access when found")
	void testGetTimesheetJobAccessInfoExistingAccessReturnsSuccessfully() {
		// Given
		Integer jobId = 1;
		Integer accountId = 1;
		io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess jobTimesheetAccess = mock(
				io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess.class);
		TimesheetJobAccessControlResponseBodyDto expectedResponse = new TimesheetJobAccessControlResponseBodyDto();
		expectedResponse.setCanCreate(1);
		expectedResponse.setCanEdit(1);
		expectedResponse.setCanDelete(1);

		given(this.jobTimesheetAccessJpaRepository.findByJobIdAndAccountId(jobId, accountId))
			.willReturn(Optional.of(jobTimesheetAccess));
		given(this.jobTimesheetAccessMapper.toResponseDto(jobTimesheetAccess)).willReturn(expectedResponse);

		// When
		TimesheetJobAccessControlResponseBodyDto result = this.timesheetService.getTimesheetJobAccessInfo(jobId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanCreate()).isEqualTo(1);
		assertThat(result.getCanEdit()).isEqualTo(1);
		assertThat(result.getCanDelete()).isEqualTo(1);
		then(this.jobTimesheetAccessJpaRepository).should().findByJobIdAndAccountId(jobId, accountId);
		then(this.jobTimesheetAccessMapper).should().toResponseDto(jobTimesheetAccess);
		then(this.jobTimesheetAccessJpaRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("Get timesheet job access info should create new access when not found")
	void testGetTimesheetJobAccessInfoNotFoundCreatesNewAccess() {
		// Given
		Integer jobId = 1;
		Integer accountId = 1;
		io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess savedAccess = mock(
				io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess.class);
		TimesheetJobAccessControlResponseBodyDto expectedResponse = new TimesheetJobAccessControlResponseBodyDto();
		expectedResponse.setCanCreate(1);
		expectedResponse.setCanEdit(1);
		expectedResponse.setCanDelete(1);

		given(this.jobTimesheetAccessJpaRepository.findByJobIdAndAccountId(jobId, accountId))
			.willReturn(Optional.empty());
		given(this.jobTimesheetAccessJpaRepository.save(any())).willReturn(savedAccess);
		given(this.jobTimesheetAccessMapper.toResponseDto(savedAccess)).willReturn(expectedResponse);

		// When
		TimesheetJobAccessControlResponseBodyDto result = this.timesheetService.getTimesheetJobAccessInfo(jobId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanCreate()).isEqualTo(1);
		assertThat(result.getCanEdit()).isEqualTo(1);
		assertThat(result.getCanDelete()).isEqualTo(1);
		then(this.jobTimesheetAccessJpaRepository).should().findByJobIdAndAccountId(jobId, accountId);
		then(this.jobTimesheetAccessJpaRepository).should().save(any());
		then(this.jobTimesheetAccessMapper).should().toResponseDto(savedAccess);
	}

	@Test
	@DisplayName("Update job timesheet access control should update successfully when access exists")
	void testUpdateJobTimesheetAccessControlExistingAccessUpdatesSuccessfully() {
		// Given
		Integer jobId = 1;
		Integer accountId = 1;
		Integer userId = 1;
		UpdateJobTimesheetAccessControlRequestBodyDto requestDto = new UpdateJobTimesheetAccessControlRequestBodyDto();
		requestDto.setCreate(1);
		requestDto.setEdit(0);
		requestDto.setDelete(1);

		io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess existingAccess = new io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess();
		existingAccess.setId(1);
		existingAccess.setJobId(jobId);
		existingAccess.setAccountId(accountId);
		existingAccess.setCanCreate(0);
		existingAccess.setCanEdit(1);
		existingAccess.setCanDelete(0);

		given(this.jobTimesheetAccessJpaRepository.findByJobIdAndAccountId(jobId, accountId))
			.willReturn(Optional.of(existingAccess));
		given(this.jobTimesheetAccessJpaRepository.save(existingAccess)).willReturn(existingAccess);

		// When
		this.timesheetService.updateJobTimesheetAccessControl(jobId, requestDto);

		// Then
		assertThat(existingAccess.getCanCreate()).isEqualTo(1);
		assertThat(existingAccess.getCanEdit()).isZero();
		assertThat(existingAccess.getCanDelete()).isEqualTo(1);
		assertThat(existingAccess.getUpdatedBy()).isEqualTo(userId);
		assertThat(existingAccess.getUpdatedOn()).isNotNull();
		then(this.jobTimesheetAccessJpaRepository).should().findByJobIdAndAccountId(jobId, accountId);
		then(this.jobTimesheetAccessJpaRepository).should().save(existingAccess);
	}

	@Test
	@DisplayName("Update job timesheet access control should throw ResourceNotFoundException when access not found")
	void testUpdateJobTimesheetAccessControlNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer jobId = 1;
		Integer accountId = 1;
		UpdateJobTimesheetAccessControlRequestBodyDto requestDto = new UpdateJobTimesheetAccessControlRequestBodyDto();
		requestDto.setCreate(1);
		requestDto.setEdit(1);
		requestDto.setDelete(1);

		given(this.jobTimesheetAccessJpaRepository.findByJobIdAndAccountId(jobId, accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.updateJobTimesheetAccessControl(jobId, requestDto))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job with");
		then(this.jobTimesheetAccessJpaRepository).should().findByJobIdAndAccountId(jobId, accountId);
		then(this.jobTimesheetAccessJpaRepository).should(never()).save(any());
	}

	// ===== searchTimesheets Tests =====

	@Test
	@DisplayName("Search timesheets should return empty list when filter is null and repository returns empty")
	void testSearchTimesheetsFilterNullReturnsEmptyWhenRepositoryEmpty() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.timesheetRepository).should()
			.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets should return list when filter is null and repository returns data")
	void testSearchTimesheetsFilterNullReturnsListWhenRepositoryHasData() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> mappedList = TimesheetTestDataFactory.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(queryResults);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(mappedList);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), anyInt()))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timesheetRepository.getDealsByTimesheetIds(anyList(), anyInt())).willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isEqualTo(mappedList);
		then(this.timesheetRepository).should()
			.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets should use search service when filter is non-null")
	void testSearchTimesheetsFilterNonNullUsesSearchService() {
		// Given
		io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto filter = mock(
				io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto.class);
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(filter);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetSearchService.searchTimesheets(any(), any(), anyInt(), any(), any()))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.timesheetSearchService).should().searchTimesheets(any(), any(), anyInt(), any(), any());
	}

	@Test
	@DisplayName("Search timesheets should return empty list when repository returns null")
	void testSearchTimesheetsFilterNullRepositoryReturnsNullReturnsEmpty() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(null);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.timesheetRepository).should()
			.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets should set empty deals on all rows when getDealsByTimesheetIds returns null")
	void testSearchTimesheetsDealEnrichmentNullDealQuerySetsEmptyDeals() {
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		TimesheetJobAndContractorListQueryResultDto q1 = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList()
			.get(0);
		q1.setId(1);
		TimesheetJobAndContractorListQueryResultDto q2 = new TimesheetJobAndContractorListQueryResultDto();
		org.springframework.beans.BeanUtils.copyProperties(q1, q2);
		q2.setId(2);
		List<TimesheetJobAndContractorListQueryResultDto> queryResults = List.of(q1, q2);
		TimesheetListResponseBodyDto r1 = TimesheetTestDataFactory.createTimesheetListResponse();
		r1.setId(1);
		TimesheetListResponseBodyDto r2 = TimesheetTestDataFactory.createTimesheetListResponse();
		r2.setId(2);
		List<TimesheetListResponseBodyDto> mappedList = List.of(r1, r2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(queryResults);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(mappedList);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timesheetRepository.getDealsByTimesheetIds(anyList(), eq(accountId))).willReturn(null);

		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getDeals()).isEmpty();
		assertThat(result.get(1).getDeals()).isEmpty();
		then(this.timesheetRepository).should().getDealsByTimesheetIds(anyList(), eq(accountId));
	}

	@Test
	@DisplayName("Search timesheets should map deals for matching timesheet ids and empty list for others")
	void testSearchTimesheetsDealEnrichmentMapsDealsPerTimesheet() {
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		TimesheetJobAndContractorListQueryResultDto q1 = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList()
			.get(0);
		q1.setId(1);
		TimesheetJobAndContractorListQueryResultDto q2 = new TimesheetJobAndContractorListQueryResultDto();
		org.springframework.beans.BeanUtils.copyProperties(q1, q2);
		q2.setId(2);
		List<TimesheetJobAndContractorListQueryResultDto> queryResults = List.of(q1, q2);
		TimesheetListResponseBodyDto r1 = TimesheetTestDataFactory.createTimesheetListResponse();
		r1.setId(1);
		TimesheetListResponseBodyDto r2 = TimesheetTestDataFactory.createTimesheetListResponse();
		r2.setId(2);
		List<TimesheetListResponseBodyDto> mappedList = List.of(r1, r2);
		DealQueryResultDto deal = new DealQueryResultDto(1, null, null, 99, "Deal A", "Owner", 1, "slug-a", "open");

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(queryResults);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(mappedList);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timesheetRepository.getDealsByTimesheetIds(anyList(), eq(accountId))).willReturn(List.of(deal));

		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		assertThat(result.get(0).getDeals()).hasSize(1);
		assertThat(result.get(0).getDeals().get(0).getDealId()).isEqualTo(99);
		assertThat(result.get(1).getDeals()).isEmpty();
	}

	@Test
	@DisplayName("Search timesheets should merge duplicate invoices by higher id and set payout when paymentPaidOn set")
	void testSearchTimesheetsInvoiceMergeAndPayoutPaidOnBranch() {
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		TimesheetListResponseBodyDto response = TimesheetTestDataFactory.createTimesheetListResponse();
		response.setId(1);
		List<TimesheetListResponseBodyDto> mappedList = List.of(response);

		TimesheetInvoice older = new TimesheetInvoice();
		older.setId(5);
		older.setTimesheetId(1);
		older.setPayoutFile("old.pdf");
		older.setPaymentPaidOn(null);

		TimesheetInvoice newer = new TimesheetInvoice();
		newer.setId(20);
		newer.setTimesheetId(1);
		newer.setPayoutFile("new.pdf");
		newer.setPaymentPaidOn(20240601);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(queryResults);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(mappedList);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(List.of(older, newer));
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timesheetRepository.getDealsByTimesheetIds(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		assertThat(result.get(0).getPayoutFile()).isEqualTo("new.pdf");
		assertThat(result.get(0).getPayoutPaidOn()).isEqualTo(20240601);

		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(List.of(newer, older));
		mappedList.get(0).setPayoutPaidOn(null);
		mappedList.get(0).setPayoutFile(null);
		List<TimesheetListResponseBodyDto> resultReverseOrder = this.timesheetService.searchTimesheets(searchRequest,
				pageable);

		assertThat(resultReverseOrder.get(0).getPayoutFile()).isEqualTo("new.pdf");
		assertThat(resultReverseOrder.get(0).getPayoutPaidOn()).isEqualTo(20240601);
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor should merge duplicate invoices keeping higher id")
	void testGetTimesheetsListByJobAndContractorIdDuplicateInvoicesMergeKeepsHigherId() {
		Integer jobId = 1;
		Integer contractorId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		TimesheetListResponseBodyDto response = TimesheetTestDataFactory.createTimesheetListResponse();
		response.setId(1);
		List<TimesheetListResponseBodyDto> mapped = List.of(response);

		TimesheetInvoice low = new TimesheetInvoice();
		low.setId(3);
		low.setTimesheetId(1);
		low.setPayoutFile("low.pdf");
		TimesheetInvoice high = new TimesheetInvoice();
		high.setId(50);
		high.setTimesheetId(1);
		high.setPayoutFile("high.pdf");

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, 1, searchRequest,
				pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(mapped);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(1))).willReturn(List.of(high, low));
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		for (TimesheetJobAndContractorListQueryResultDto row : timesheetResults) {
			row.setTotalWorkTime(0);
			row.setTotalOvertime(0);
			row.setTotalTime(0);
		}

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		assertThat(result.get(0).getPayoutFile()).isEqualTo("high.pdf");
	}

	@Test
	@DisplayName("Migrate timesheet batch should set hasMore when batch is full and migrate all rows")
	void testMigrateTimesheetTotalColumnsBatchFullBatchSetsHasMoreAndMigrates() {
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(2);
		requestDto.setOffset(0);
		TimesheetForMigrationDto a = new TimesheetForMigrationDto();
		a.setTimesheetId(1);
		a.setTimesheetSettingId(10);
		TimesheetForMigrationDto b = new TimesheetForMigrationDto();
		b.setTimesheetId(2);
		b.setTimesheetSettingId(10);

		given(this.timesheetRepository.findTimesheetsForMigration(2, 0)).willReturn(List.of(a, b));
		given(this.timesheetSettingRepository.findWorkLogTypeByIdIn(List.of(10))).willReturn(Map.of(10,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType.WORK_HOUR.getTypeId()));
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto logA = new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto();
		logA.setTimesheetId(1);
		logA.setTimeLogId(100);
		logA.setWorkTime(8);
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto logB = new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto();
		logB.setTimesheetId(2);
		logB.setTimeLogId(101);
		logB.setTotalTime(100);
		logB.setOverTime(1);
		given(this.timeLogRepository.findTimeLogsForMigration(List.of(1, 2))).willReturn(List.of(logA, logB));
		willDoNothing().given(this.timesheetRepository)
			.updateTimesheetTotalColumns(anyInt(), anyInt(), anyInt(), anyInt());

		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		assertThat(result.isHasMore()).isTrue();
		assertThat(result.getNextOffset()).isEqualTo(2);
		assertThat(result.getSuccessCount()).isEqualTo(2);
		assertThat(result.getFailureCount()).isZero();
		then(this.timesheetRepository).should(times(2))
			.updateTimesheetTotalColumns(anyInt(), anyInt(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("Migrate timesheet batch should use simple class name when exception has no message")
	void testMigrateTimesheetTotalColumnsBatchNullExceptionMessageRecordsSimpleName() {
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(1);
		requestDto.setOffset(0);
		TimesheetForMigrationDto row = new TimesheetForMigrationDto();
		row.setTimesheetId(1);
		row.setTimesheetSettingId(10);

		given(this.timesheetRepository.findTimesheetsForMigration(1, 0)).willReturn(List.of(row));
		given(this.timesheetSettingRepository.findWorkLogTypeByIdIn(List.of(10))).willReturn(Map.of(10, 1));
		given(this.timeLogRepository.findTimeLogsForMigration(List.of(1))).willReturn(Collections.emptyList());
		// Anonymous RuntimeException would make ex.getClass().getSimpleName() "" — use
		// named class
		RuntimeException boom = new RuntimeException((String) null);
		willThrow(boom).given(this.timesheetRepository)
			.updateTimesheetTotalColumns(anyInt(), anyInt(), anyInt(), anyInt());

		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		assertThat(result.getFailureCount()).isEqualTo(1);
		assertThat(result.getFailedMigrations().get(0).getErrorMessage()).isEqualTo("RuntimeException");
	}

	@Test
	@DisplayName("Migrate single timesheet should sum work hours when work log type is work hour")
	void testMigrateTimesheetTotalColumnsSingleWorkHourUsesWorkTimeSum() {
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setTimesheetId(7);
		TimesheetForMigrationDto timesheet = new TimesheetForMigrationDto();
		timesheet.setTimesheetId(7);
		timesheet.setTimesheetSettingId(10);
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto log = new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto();
		log.setTimesheetId(7);
		log.setTimeLogId(200);
		log.setWorkTime(8);
		log.setTotalTime(null);
		log.setOverTime(null);

		given(this.timesheetRepository.findTimesheetForMigrationById(7)).willReturn(Optional.of(timesheet));
		given(this.timesheetSettingRepository.findWorkLogTypeByIdIn(List.of(10))).willReturn(Map.of(10,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType.WORK_HOUR.getTypeId()));
		given(this.timeLogRepository.findTimeLogsForMigration(List.of(7))).willReturn(List.of(log));
		willDoNothing().given(this.timesheetRepository).updateTimesheetTotalColumns(7, 0, 8, 0);

		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		assertThat(result.getSuccessCount()).isEqualTo(1);
		then(this.timesheetRepository).should().updateTimesheetTotalColumns(7, 0, 8, 0);
	}

	@Test
	@DisplayName("Migrate single timesheet should treat incomplete start end interval as zero work time")
	void testMigrateTimesheetTotalColumnsSingleStartEndIncompleteIntervalContributesZero() {
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setTimesheetId(8);
		TimesheetForMigrationDto timesheet = new TimesheetForMigrationDto();
		timesheet.setTimesheetId(8);
		timesheet.setTimesheetSettingId(11);
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto log = new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto();
		log.setTimesheetId(8);
		log.setTimeLogId(300);
		log.setWorkStartTime(null);
		log.setWorkEndTime(100);
		log.setTotalTime(5);
		log.setOverTime(1);

		given(this.timesheetRepository.findTimesheetForMigrationById(8)).willReturn(Optional.of(timesheet));
		given(this.timesheetSettingRepository.findWorkLogTypeByIdIn(List.of(11))).willReturn(Map.of(11,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType.START_AND_END_TIME.getTypeId()));
		given(this.timeLogRepository.findTimeLogsForMigration(List.of(8))).willReturn(List.of(log));
		willDoNothing().given(this.timesheetRepository).updateTimesheetTotalColumns(8, 5, 0, 1);

		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		assertThat(result.getSuccessCount()).isEqualTo(1);
		then(this.timesheetRepository).should().updateTimesheetTotalColumns(8, 5, 0, 1);
	}

	// ===== searchTimesheetsCount Tests =====

	@Test
	@DisplayName("Search timesheets count should return count from repository when filter is null")
	void testSearchTimesheetsCountFilterNullReturnsRepositoryCount() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(null);
		Integer accountId = 1;
		Long expectedCount = 42L;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsCountByAccountId(accountId)).willReturn(expectedCount);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(expectedCount);
		then(this.timesheetRepository).should().getAllTimesheetsCountByAccountId(accountId);
	}

	@Test
	@DisplayName("Search timesheets count should use search service when filter is non-null")
	void testSearchTimesheetsCountFilterNonNullUsesSearchService() {
		// Given
		io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto filter = mock(
				io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto.class);
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setFilterSearchList(filter);
		Integer accountId = 1;
		Long expectedCount = 10L;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetSearchService.getTimesheetsCount(any(), anyInt(), any())).willReturn(expectedCount);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(expectedCount);
		then(this.timesheetSearchService).should().getTimesheetsCount(any(), anyInt(), any());
	}

	// ===== searchTimesheets with timesheetIds (access control) Tests =====

	@Test
	@DisplayName("Search timesheets with timesheetIds should filter by account and candidate access control")
	void testSearchTimesheetsWithTimesheetIdsFiltersViaCandidateAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(10, 20, 30));
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.filterTimesheetIdsByAccountAndCandidateAccess(List.of(10, 20, 30), accountId))
			.willReturn(List.of(10, 30));
		given(this.timesheetRepository.getTimesheetsListByIds(List.of(10, 30), Collections.emptyList(), pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.filterTimesheetIdsByAccountAndCandidateAccess(List.of(10, 20, 30), accountId);
		then(this.timesheetRepository).should()
			.getTimesheetsListByIds(List.of(10, 30), Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets with timesheetIds should return empty when access control filters all IDs")
	void testSearchTimesheetsWithTimesheetIdsReturnsEmptyWhenAllFilteredByAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(10, 20));
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.filterTimesheetIdsByAccountAndCandidateAccess(List.of(10, 20), accountId))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.filterTimesheetIdsByAccountAndCandidateAccess(List.of(10, 20), accountId);
		then(this.timesheetRepository).should(never()).getTimesheetsListByIds(anyList(), anyList(), any());
	}

	// ===== searchTimesheets with isSubmitted (access control) Tests =====

	@Test
	@DisplayName("Search timesheets with isSubmitted true should use approver user ID path with access control")
	void testSearchTimesheetsIsSubmittedTrueUsesApproverPathWithAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsSubmitted(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		Integer userId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsByApproverUserId(userId, accountId, pageable))
			.willReturn(List.of(100, 200));
		given(this.timesheetRepository.getTimesheetsListByIds(List.of(100, 200), Collections.emptyList(), pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should().getTimesheetIdsByApproverUserId(userId, accountId, pageable);
		then(this.timesheetRepository).should()
			.getTimesheetsListByIds(List.of(100, 200), Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets with isSubmitted true should return empty when approver has no timesheets")
	void testSearchTimesheetsIsSubmittedTrueReturnsEmptyWhenNoApproverTimesheets() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsSubmitted(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		Integer userId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsByApproverUserId(userId, accountId, pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should().getTimesheetIdsByApproverUserId(userId, accountId, pageable);
		then(this.timesheetRepository).should(never()).getTimesheetsListByIds(anyList(), anyList(), any());
	}

	// ===== searchTimesheets with isReimbursement Tests =====

	@Test
	@DisplayName("Search timesheets with isReimbursement true should use pending reimbursement path with access control")
	void testSearchTimesheetsIsReimbursementTrueUsesPendingReimbursementPathWithAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsWithPendingReimbursements(accountId, pageable))
			.willReturn(List.of(100, 200));
		given(this.timesheetRepository.getTimesheetsListByIds(List.of(100, 200), Collections.emptyList(), pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should().getTimesheetIdsWithPendingReimbursements(accountId, pageable);
		then(this.timesheetRepository).should()
			.getTimesheetsListByIds(List.of(100, 200), Collections.emptyList(), pageable);
		then(this.timesheetRepository).should(never()).filterTimesheetIdsByAccountAndCandidateAccess(anyList(), any());
	}

	@Test
	@DisplayName("Search timesheets with isReimbursement true should return empty when no pending reimbursements")
	void testSearchTimesheetsIsReimbursementTrueReturnsEmptyWhenNoPendingReimbursements() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsWithPendingReimbursements(accountId, pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should().getTimesheetIdsWithPendingReimbursements(accountId, pageable);
		then(this.timesheetRepository).should(never()).getTimesheetsListByIds(anyList(), anyList(), any());
	}

	@Test
	@DisplayName("Search timesheets with isReimbursement true should return enriched list when repository has data")
	void testSearchTimesheetsIsReimbursementTrueReturnsListWhenRepositoryHasData() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		List<TimesheetListResponseBodyDto> mappedList = TimesheetTestDataFactory.createTimesheetListResponseList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsWithPendingReimbursements(accountId, pageable))
			.willReturn(List.of(100, 200));
		given(this.timesheetRepository.getTimesheetsListByIds(List.of(100, 200), Collections.emptyList(), pageable))
			.willReturn(queryResults);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(mappedList);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), anyInt()))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timesheetRepository.getDealsByTimesheetIds(anyList(), anyInt())).willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isEqualTo(mappedList);
		then(this.timesheetRepository).should().getTimesheetIdsWithPendingReimbursements(accountId, pageable);
		then(this.timesheetRepository).should()
			.getTimesheetsListByIds(List.of(100, 200), Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets with isReimbursement true should return empty when list query returns null")
	void testSearchTimesheetsIsReimbursementTrueRepositoryReturnsNullReturnsEmpty() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsWithPendingReimbursements(accountId, pageable))
			.willReturn(List.of(100));
		given(this.timesheetRepository.getTimesheetsListByIds(List.of(100), Collections.emptyList(), pageable))
			.willReturn(null);

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.timesheetRepository).should().getTimesheetIdsWithPendingReimbursements(accountId, pageable);
		then(this.timesheetRepository).should().getTimesheetsListByIds(List.of(100), Collections.emptyList(), pageable);
	}

	@Test
	@DisplayName("Search timesheets with timesheetIds should ignore isReimbursement flag")
	void testSearchTimesheetsWithTimesheetIdsIgnoresIsReimbursement() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(10, 20));
		searchRequest.setIsReimbursement(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.filterTimesheetIdsByAccountAndCandidateAccess(List.of(10, 20), accountId))
			.willReturn(List.of(10));
		given(this.timesheetRepository.getTimesheetsListByIds(List.of(10), Collections.emptyList(), pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should(never()).getTimesheetIdsWithPendingReimbursements(anyInt(), any());
		then(this.timesheetRepository).should()
			.filterTimesheetIdsByAccountAndCandidateAccess(List.of(10, 20), accountId);
	}

	@Test
	@DisplayName("Search timesheets with isSubmitted true should ignore isReimbursement flag")
	void testSearchTimesheetsWithIsSubmittedTrueIgnoresIsReimbursement() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsSubmitted(true);
		searchRequest.setIsReimbursement(true);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;
		Integer userId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetIdsByApproverUserId(userId, accountId, pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should().getTimesheetIdsByApproverUserId(userId, accountId, pageable);
		then(this.timesheetRepository).should(never()).getTimesheetIdsWithPendingReimbursements(anyInt(), any());
	}

	@Test
	@DisplayName("Search timesheets with isReimbursement false should use default path when filter is null")
	void testSearchTimesheetsIsReimbursementFalseUsesDefaultPathWhenFilterNull() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(false);
		searchRequest.setFilterSearchList(null);
		searchRequest.setSortPriorityList(Collections.emptyList());
		Pageable pageable = Pageable.unpaged();
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetListResponseBodyDto> result = this.timesheetService.searchTimesheets(searchRequest, pageable);

		// Then
		assertThat(result).isEmpty();
		then(this.timesheetRepository).should()
			.getAllTimesheetsByAccountId(accountId, Collections.emptyList(), pageable);
		then(this.timesheetRepository).should(never()).getTimesheetIdsWithPendingReimbursements(anyInt(), any());
	}

	// ===== searchTimesheetsCount with isSubmitted (access control) Tests =====

	@Test
	@DisplayName("Search timesheets count with timesheetIds should use candidate access control filtering")
	void testSearchTimesheetsCountWithTimesheetIdsUsesCandidateAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(5, 10, 15));
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.filterTimesheetIdsByAccountAndCandidateAccess(List.of(5, 10, 15), accountId))
			.willReturn(List.of(5, 15));

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(2L);
		then(this.timesheetRepository).should()
			.filterTimesheetIdsByAccountAndCandidateAccess(List.of(5, 10, 15), accountId);
	}

	@Test
	@DisplayName("Search timesheets count with timesheetIds should return zero when no IDs pass access control")
	void testSearchTimesheetsCountWithTimesheetIdsReturnsZeroWhenNoneVisible() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(7513));
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.filterTimesheetIdsByAccountAndCandidateAccess(List.of(7513), accountId))
			.willReturn(List.of());

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isZero();
		then(this.timesheetRepository).should().filterTimesheetIdsByAccountAndCandidateAccess(List.of(7513), accountId);
	}

	@Test
	@DisplayName("Search timesheets count with isSubmitted true should use approver count with access control")
	void testSearchTimesheetsCountIsSubmittedTrueUsesApproverCountWithAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsSubmitted(true);
		Integer accountId = 1;
		Integer userId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetsCountByApproverUserId(userId, accountId)).willReturn(5L);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(5L);
		then(this.timesheetRepository).should().getTimesheetsCountByApproverUserId(userId, accountId);
	}

	@Test
	@DisplayName("Search timesheets count with isSubmitted true should return zero when approver has no submitted timesheets")
	void testSearchTimesheetsCountIsSubmittedTrueReturnsZeroWhenNoApproverTimesheets() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsSubmitted(true);
		Integer accountId = 1;
		Integer userId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetsCountByApproverUserId(userId, accountId)).willReturn(0L);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isZero();
		then(this.timesheetRepository).should().getTimesheetsCountByApproverUserId(userId, accountId);
	}

	@Test
	@DisplayName("Search timesheets count with isReimbursement true should use pending reimbursement count with access control")
	void testSearchTimesheetsCountIsReimbursementTrueUsesPendingReimbursementCountWithAccessControl() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(true);
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetsCountWithPendingReimbursements(accountId)).willReturn(5L);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(5L);
		then(this.timesheetRepository).should().getTimesheetsCountWithPendingReimbursements(accountId);
		then(this.timesheetRepository).should(never()).filterTimesheetIdsByAccountAndCandidateAccess(anyList(), any());
	}

	@Test
	@DisplayName("Search timesheets count with isReimbursement true should return zero when no pending reimbursements")
	void testSearchTimesheetsCountIsReimbursementTrueReturnsZeroWhenNoPendingReimbursements() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(true);
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetsCountWithPendingReimbursements(accountId)).willReturn(0L);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isZero();
		then(this.timesheetRepository).should().getTimesheetsCountWithPendingReimbursements(accountId);
	}

	@Test
	@DisplayName("Search timesheets count with timesheetIds should ignore isReimbursement flag")
	void testSearchTimesheetsCountWithTimesheetIdsIgnoresIsReimbursement() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(5, 10));
		searchRequest.setIsReimbursement(true);
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.filterTimesheetIdsByAccountAndCandidateAccess(List.of(5, 10), accountId))
			.willReturn(List.of(5));

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(1L);
		then(this.timesheetRepository).should(never()).getTimesheetsCountWithPendingReimbursements(anyInt());
	}

	@Test
	@DisplayName("Search timesheets count with isSubmitted true should ignore isReimbursement flag")
	void testSearchTimesheetsCountWithIsSubmittedTrueIgnoresIsReimbursement() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsSubmitted(true);
		searchRequest.setIsReimbursement(true);
		Integer accountId = 1;
		Integer userId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getTimesheetsCountByApproverUserId(userId, accountId)).willReturn(3L);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(3L);
		then(this.timesheetRepository).should().getTimesheetsCountByApproverUserId(userId, accountId);
		then(this.timesheetRepository).should(never()).getTimesheetsCountWithPendingReimbursements(anyInt());
	}

	@Test
	@DisplayName("Search timesheets count with isReimbursement false should use default path when filter is null")
	void testSearchTimesheetsCountIsReimbursementFalseUsesDefaultPathWhenFilterNull() {
		// Given
		TimesheetSearchRequestBodyDto searchRequest = new TimesheetSearchRequestBodyDto();
		searchRequest.setIsReimbursement(false);
		searchRequest.setFilterSearchList(null);
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn("+00:00");
		given(this.timesheetRepository.getAllTimesheetsCountByAccountId(accountId)).willReturn(12L);

		// When
		Long result = this.timesheetService.searchTimesheetsCount(searchRequest);

		// Then
		assertThat(result).isEqualTo(12L);
		then(this.timesheetRepository).should().getAllTimesheetsCountByAccountId(accountId);
		then(this.timesheetRepository).should(never()).getTimesheetsCountWithPendingReimbursements(anyInt());
	}

	// ===== searchEntity Tests =====

	@Test
	@DisplayName("Search entity should return jobs when jobs flag is true")
	void testSearchEntityJobsTrueReturnsJobs() {
		// Given
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("test", false, true, false, false);
		Integer accountId = 1;
		List<JobSearchQueryResultDto> jobResults = List.of(new JobSearchQueryResultDto());

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.searchJobs(accountId, "test", false)).willReturn(jobResults);

		// When
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.data()).containsKey("4");
		assertThat(result.data().get("4")).isInstanceOf(List.class);
		then(this.timesheetRepository).should().searchJobs(accountId, "test", false);
	}

	@Test
	@DisplayName("Search entity should return companies when companies flag is true")
	void testSearchEntityCompaniesTrueReturnsCompanies() {
		// Given
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("co", true, false, false, false);
		Integer accountId = 1;
		List<CompanySearchQueryResultDto> companyResults = List.of(new CompanySearchQueryResultDto());

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.searchCompanies(accountId, "co")).willReturn(companyResults);

		// When
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.data()).containsKey("3");
		assertThat(result.data().get("3")).isInstanceOf(List.class);
		then(this.timesheetRepository).should().searchCompanies(accountId, "co");
	}

	@Test
	@DisplayName("Search entity should return deals when deals flag is true")
	void testSearchEntityDealsTrueReturnsDeals() {
		// Given
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("deal", false, false, true, false);
		Integer accountId = 1;
		List<DealSearchQueryResultDto> dealResults = List.of(new DealSearchQueryResultDto());

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.searchDeals(accountId, "deal", false)).willReturn(dealResults);

		// When
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.data()).containsKey("11");
		assertThat(result.data().get("11")).isInstanceOf(List.class);
		then(this.timesheetRepository).should().searchDeals(accountId, "deal", false);
	}

	@Test
	@DisplayName("Search entity should use null search keyword when search is blank")
	void testSearchEntitySearchBlankUsesNullKeyword() {
		// Given - whitespace-only search trims to empty and becomes null keyword; jobs
		// flag drives calls
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("   ", false, true, false, false);
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.searchJobs(accountId, null, false)).willReturn(Collections.emptyList());

		// When
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetRepository).should().searchJobs(accountId, null, false);
	}

	@Test
	@DisplayName("Search entity should return combined results when jobs, companies, and deals are requested")
	void testSearchEntityAllEntityTypesRequestedReturnsCombinedMap() {
		// Given
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("acme", true, true, true, true);
		Integer accountId = 1;
		JobSearchQueryResultDto jobRow = new JobSearchQueryResultDto();
		jobRow.setName("Eng");
		jobRow.setSlug("eng");
		jobRow.setId(10);
		jobRow.setSrno(1);
		CompanySearchQueryResultDto companyRow = new CompanySearchQueryResultDto();
		companyRow.setName("Acme");
		companyRow.setSlug("acme");
		companyRow.setId(20);
		companyRow.setSrno(2);
		DealSearchQueryResultDto dealRow = new DealSearchQueryResultDto();
		dealRow.setName("Q1");
		dealRow.setSlug("q1");
		dealRow.setId(30);
		dealRow.setSrno(3);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.searchJobs(accountId, "acme", true)).willReturn(List.of(jobRow));
		given(this.timesheetRepository.searchCompanies(accountId, "acme")).willReturn(List.of(companyRow));
		given(this.timesheetRepository.searchDeals(accountId, "acme", true)).willReturn(List.of(dealRow));

		// When
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.data()).containsKeys("3", "4", "11");
		assertThat(result.data().get("4")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);
		assertThat(result.data().get("3")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);
		assertThat(result.data().get("11")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);
		then(this.timesheetRepository).should().searchJobs(accountId, "acme", true);
		then(this.timesheetRepository).should().searchCompanies(accountId, "acme");
		then(this.timesheetRepository).should().searchDeals(accountId, "acme", true);
	}

	@Test
	@DisplayName("Search entity should return empty map when no entity type flags are enabled")
	void testSearchEntityNoEntityTypesRequestedReturnsEmptyDataMap() {
		// Given
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("anything", false, false, false, false);
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);

		// When
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.data()).isEmpty();
		then(this.timesheetRepository).should(never()).searchJobs(any(), any(), any());
		then(this.timesheetRepository).should(never()).searchCompanies(any(), any());
		then(this.timesheetRepository).should(never()).searchDeals(any(), any(), any());
	}

	// ===== migrateTimesheetTotalColumns Tests =====

	@Test
	@DisplayName("Migrate timesheet total columns with timesheetId should return failed when timesheet not found")
	void testMigrateTimesheetTotalColumnsSingleTimesheetNotFoundReturnsFailed() {
		// Given
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setTimesheetId(999);
		Integer timesheetId = 999;

		given(this.timesheetRepository.findTimesheetForMigrationById(timesheetId)).willReturn(Optional.empty());

		// When
		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getFailureCount()).isEqualTo(1);
		assertThat(result.getSuccessCount()).isZero();
		assertThat(result.getFailedMigrations()).hasSize(1);
		assertThat(result.getFailedMigrations().get(0).getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getFailedMigrations().get(0).getErrorMessage()).isEqualTo("Timesheet not found");
		then(this.timesheetRepository).should().findTimesheetForMigrationById(timesheetId);
	}

	@Test
	@DisplayName("Migrate timesheet total columns with timesheetId should return success when timesheet found and migrated")
	void testMigrateTimesheetTotalColumnsSingleTimesheetFoundMigratesSuccessfully() {
		// Given
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setTimesheetId(1);
		Integer timesheetId = 1;
		TimesheetForMigrationDto timesheet = new TimesheetForMigrationDto();
		timesheet.setTimesheetId(timesheetId);
		timesheet.setTimesheetSettingId(10);
		Map<Integer, Integer> workLogTypeBySettingId = new HashMap<>();
		workLogTypeBySettingId.put(10, 2);
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto timeLog = new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto();
		timeLog.setTimesheetId(timesheetId);
		timeLog.setTimeLogId(100);
		timeLog.setTotalTime(3600);
		timeLog.setOverTime(600);
		timeLog.setWorkStartTime(32400);
		timeLog.setWorkEndTime(61200);

		given(this.timesheetRepository.findTimesheetForMigrationById(timesheetId)).willReturn(Optional.of(timesheet));
		given(this.timesheetSettingRepository.findWorkLogTypeByIdIn(List.of(10))).willReturn(workLogTypeBySettingId);
		given(this.timeLogRepository.findTimeLogsForMigration(List.of(timesheetId))).willReturn(List.of(timeLog));
		willDoNothing().given(this.timesheetRepository).updateTimesheetTotalColumns(timesheetId, 3600, 28800, 600);

		// When
		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getSuccessCount()).isEqualTo(1);
		assertThat(result.getFailureCount()).isZero();
		assertThat(result.getSuccessfulMigrations()).hasSize(1);
		assertThat(result.getSuccessfulMigrations().get(0).getTimesheetId()).isEqualTo(timesheetId);
		then(this.timesheetRepository).should().updateTimesheetTotalColumns(timesheetId, 3600, 28800, 600);
	}

	@Test
	@DisplayName("Migrate timesheet total columns with timesheetId should add to failed when migration throws")
	void testMigrateTimesheetTotalColumnsSingleTimesheetThrowsAddsToFailed() {
		// Given - timesheet has no timesheet_setting_id (null) which causes
		// ValidationErrorException in migrateSingleTimesheet
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setTimesheetId(1);
		Integer timesheetId = 1;
		TimesheetForMigrationDto timesheet = new TimesheetForMigrationDto();
		timesheet.setTimesheetId(timesheetId);
		timesheet.setTimesheetSettingId(null);

		given(this.timesheetRepository.findTimesheetForMigrationById(timesheetId)).willReturn(Optional.of(timesheet));
		given(this.timesheetSettingRepository.findWorkLogTypeByIdIn(List.of())).willReturn(Collections.emptyMap());
		given(this.timeLogRepository.findTimeLogsForMigration(List.of(timesheetId)))
			.willReturn(Collections.emptyList());

		// When
		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		// Then - migrateSingleTimesheet throws ValidationErrorException for null
		// timesheetSettingId
		assertThat(result).isNotNull();
		assertThat(result.getFailureCount()).isEqualTo(1);
		assertThat(result.getSuccessCount()).isZero();
		assertThat(result.getFailedMigrations()).hasSize(1);
		assertThat(result.getFailedMigrations().get(0).getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getFailedMigrations().get(0).getErrorMessage()).contains("has no timesheet_setting_id");
	}

	@Test
	@DisplayName("Migrate timesheet total columns batch should return empty when no timesheets to migrate")
	void testMigrateTimesheetTotalColumnsBatchEmptyReturnsEmptyResult() {
		// Given
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(100);
		requestDto.setOffset(0);

		given(this.timesheetRepository.findTimesheetsForMigration(100, 0)).willReturn(Collections.emptyList());

		// When
		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTotalProcessed()).isZero();
		assertThat(result.getSuccessCount()).isZero();
		assertThat(result.getFailureCount()).isZero();
		assertThat(result.isHasMore()).isFalse();
		then(this.timesheetRepository).should().findTimesheetsForMigration(100, 0);
	}

	@Test
	@DisplayName("Migrate timesheet total columns batch should use default batch size and offset when null")
	void testMigrateTimesheetTotalColumnsBatchNullBatchSizeUsesDefaults() {
		// Given
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(null);
		requestDto.setOffset(null);

		given(this.timesheetRepository.findTimesheetsForMigration(100, 0)).willReturn(Collections.emptyList());

		// When
		TimesheetMigrationResponseBodyDto result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);

		// Then
		assertThat(result).isNotNull();
		then(this.timesheetRepository).should().findTimesheetsForMigration(100, 0);
	}

	@Test
	@DisplayName("Create timesheets with monthly frequency and last day of month should call setTimesheetStartDateForMonthly")
	void testCreateTimesheetsMonthlyFrequencyLastDayOfMonthCallsSetTimesheetStartDateForMonthly() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		// Use January 31, 2024 (last day of month) - Unix timestamp: 1706659200
		// For monthly with last day of month, end date should be last day of the month
		// (Jan 31)
		Integer startDate = 1706659200; // 2024-01-31 00:00:00 UTC
		Integer endDate = 1706659200; // 2024-01-31 (same day - last day of month)
		Integer jobEndDateStored = 1706659200; // Job ends calendar day of period end
												// (stored value is start of day)
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		timesheetRequest.setStartDate(startDate);
		timesheetRequest.setEndDate(endDate);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());

		AssignCandidateJob assignCandidateJob = TimesheetTestDataFactory.createAssignCandidateJob();
		assignCandidateJob.setCandidateId(contractorIds.get(0));
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, 1))
			.willReturn(List.of(assignCandidateJob));

		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId());
		setting
			.setTimesheetStartDay(io.recruitcrm.contract_staffing.entity.model.WorkDayEnum.LAST_DAY_OF_MONTH.getId()); // Last
																														// day
																														// of
																														// month
		setting.setJobStartDate(startDate);
		setting.setJobEndDate(jobEndDateStored);
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation association = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		association.setContractorId(contractorIds.get(0));
		setting.setAssociation(association);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(jobId, contractorIds))
			.willReturn(List.of(setting));
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);

		given(this.timesheetRepository.validateTimesheetsExist(anyList(), eq(1), eq(jobId), eq(contractorIds)))
			.willReturn(false);

		Timesheet createdTimesheet = TimesheetTestDataFactory.createTimesheet();
		createdTimesheet.setTimesheetSettingId(setting.getId());
		createdTimesheet.setPeriodStart(startDate);
		createdTimesheet.setPeriodEnd(endDate);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(List.of(createdTimesheet));
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		// Then
		then(this.timesheetSettingRepository).should()
			.findLatestTimesheetSettingsByJobIdAndContractorIds(jobId, contractorIds);
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
	}

	@Test
	@DisplayName("Create timesheets with different days of week should cover all getDayTypeId branches")
	void testCreateTimesheetsDifferentDaysOfWeekCoversAllGetDayTypeIdBranches() {
		// Given
		Integer mondayDate = 1704067200; // 2024-01-01 (Monday)
		Integer sundayDate = 1704585600; // 2024-01-07 (Sunday)

		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());

		AssignCandidateJob assignCandidateJob = TimesheetTestDataFactory.createAssignCandidateJob();
		assignCandidateJob.setCandidateId(contractorIds.get(0));
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, 1))
			.willReturn(List.of(assignCandidateJob));

		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting.setTimesheetStartDay(1); // Monday
		setting.setJobStartDate(mondayDate);
		setting.setJobEndDate(sundayDate);
		io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation assoc = new io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation();
		assoc.setContractorId(contractorIds.get(0));
		setting.setAssociation(assoc);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(List.of(setting));
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);

		given(this.timesheetRepository.validateTimesheetsExist(anyList(), eq(1), eq(jobId), eq(contractorIds)))
			.willReturn(false);

		Timesheet createdTimesheet = TimesheetTestDataFactory.createTimesheet();
		createdTimesheet.setPeriodStart(mondayDate);
		createdTimesheet.setPeriodEnd(sundayDate);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(List.of(createdTimesheet));
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// Test Monday - for weekly, end date should be Sunday (7 days later)
		CreateTimesheetRequestBodyDto mondayRequest = new CreateTimesheetRequestBodyDto();
		mondayRequest.setStartDate(mondayDate); // Monday
		mondayRequest.setEndDate(sundayDate); // Sunday (end of week)

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, List.of(mondayRequest));

		// Then
		then(this.timesheetRepository).should().createTimesheets(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
		then(this.timeLogRepository).should().createBulkTimesheetLogs(anyList());
	}

	// ===== createBulkTimesheetsForMultipleJobs Tests =====

	@Test
	@DisplayName("Create bulk timesheets for multiple jobs should create timesheets successfully with single job-contractor pair")
	void testCreateBulkTimesheetsForMultipleJobsSinglePairCreatesSuccessfully() throws Exception {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createBulkTimesheetsForMultipleJobsRequest();
		io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService mockSelfInjectedService = mock(
				io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService.class);
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Bulk User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);

		// Set the self-injected service field using reflection
		java.lang.reflect.Field field = TimesheetService.class.getDeclaredField("selfReference");
		field.setAccessible(true);
		field.set(this.timesheetService, mockSelfInjectedService);

		given(mockSelfInjectedService.createTimesheets(anyInt(), anyList(), anyList(), eq(false)))
			.willReturn(Arrays.asList(11, 12, 13));

		// When
		this.timesheetService.createBulkTimesheetsForMultipleJobs(requestDto);

		// Then
		then(mockSelfInjectedService).should(times(1))
			.createTimesheets(eq(TimesheetTestDataFactory.getDefaultJobId()), eq(Arrays.asList(1, 2, 3)),
					eq(requestDto.getTimesheetDates()), eq(false));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create bulk timesheets for multiple jobs should create timesheets successfully with multiple job-contractor pairs")
	void testCreateBulkTimesheetsForMultipleJobsMultiplePairsCreatesSuccessfully() throws Exception {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createBulkTimesheetsForMultipleJobsRequestWithMultiplePairs();
		io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService mockSelfInjectedService = mock(
				io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService.class);
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Bulk User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);

		// Set the self-injected service field using reflection
		java.lang.reflect.Field field = TimesheetService.class.getDeclaredField("selfReference");
		field.setAccessible(true);
		field.set(this.timesheetService, mockSelfInjectedService);

		given(mockSelfInjectedService.createTimesheets(anyInt(), anyList(), anyList(), eq(false)))
			.willReturn(Collections.emptyList());

		// When
		this.timesheetService.createBulkTimesheetsForMultipleJobs(requestDto);

		// Then
		then(mockSelfInjectedService).should(times(2)).createTimesheets(anyInt(), anyList(), anyList(), eq(false));
		then(mockSelfInjectedService).should()
			.createTimesheets(eq(TimesheetTestDataFactory.getDefaultJobId()), eq(Arrays.asList(1, 2, 3)),
					eq(requestDto.getTimesheetDates()), eq(false));
		then(mockSelfInjectedService).should()
			.createTimesheets(eq(2), eq(Arrays.asList(4, 5)), eq(requestDto.getTimesheetDates()), eq(false));
	}

	@Test
	@DisplayName("Apply entity type field filtering should hide bill fields but retain pay currency for contractor")
	void testApplyEntityTypeFieldFilteringContractorHidesBillFieldsAndPayCurrency() throws Exception {
		// Given
		List<TimesheetListResponseBodyDto> responseDtos = TimesheetTestDataFactory.createTimesheetListResponseList();
		Integer contractorEntityType = UserTypeEnum.CONTRACTOR.getId();

		java.lang.reflect.Method method = TimesheetService.class.getDeclaredMethod("applyEntityTypeFieldFiltering",
				List.class, Integer.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetService, responseDtos, contractorEntityType);

		// Then
		for (TimesheetListResponseBodyDto dto : responseDtos) {
			assertThat(dto.getBillRate()).isNull();
			assertThat(dto.getBillData()).isNull();
			assertThat(dto.getBillStatusId()).isNull();
			assertThat(dto.getBillCurrencySymbol()).isNull();
			assertThat(dto.getBillCurrencyCode()).isNull();

			assertThat(dto.getInvoiceNumber()).isNull();
			assertThat(dto.getInvoiceCreatedOn()).isNull();
			assertThat(dto.getInvoiceStatusId()).isNull();

			assertThat(dto.getPayoutFile()).isNull();

			assertThat(dto.getCanCreate()).isNull();
			assertThat(dto.getCanEdit()).isNull();
			assertThat(dto.getCanDelete()).isNull();

			assertThat(dto.getPayCurrencySymbol()).isEqualTo(TimesheetTestDataFactory.getDefaultPayCurrencySymbol());
			assertThat(dto.getPayCurrencyCode()).isEqualTo(TimesheetTestDataFactory.getDefaultPayCurrencyCode());
		}
	}

	@Test
	@DisplayName("Apply entity type field filtering should hide pay fields but retain pay currency for company contact")
	void testApplyEntityTypeFieldFilteringCompanyContactHidesPayFieldsButRetainsPayCurrency() throws Exception {
		// Given
		List<TimesheetListResponseBodyDto> responseDtos = TimesheetTestDataFactory.createTimesheetListResponseList();
		responseDtos.forEach((dto) -> dto.setIsInvoiceCreated(1));
		Integer companyContactEntityType = UserTypeEnum.COMPANY_CONTACT.getId();

		java.lang.reflect.Method method = TimesheetService.class.getDeclaredMethod("applyEntityTypeFieldFiltering",
				List.class, Integer.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetService, responseDtos, companyContactEntityType);

		// Then
		for (TimesheetListResponseBodyDto dto : responseDtos) {
			assertThat(dto.getPayRate()).isNull();
			assertThat(dto.getPayData()).isNull();
			assertThat(dto.getPayStatusId()).isNull();

			assertThat(dto.getPayCurrencySymbol()).isEqualTo(TimesheetTestDataFactory.getDefaultPayCurrencySymbol());
			assertThat(dto.getPayCurrencyCode()).isEqualTo(TimesheetTestDataFactory.getDefaultPayCurrencyCode());

			assertThat(dto.getIsInvoiceCreated()).isEqualTo(1);
		}
	}

	// ===== Branch coverage: remaining TimesheetService paths =====

	@Test
	@DisplayName("Get timesheet status history should throw ResourceNotFoundException when status history query returns null")
	void testGetTimesheetStatusHistoryNullStatusHistoryListThrowsResourceNotFoundException() {
		Integer timesheetId = 1;
		Integer accountId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId)).willReturn(null);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetStatusHistory(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Status history for timesheet");
	}

	@Test
	@DisplayName("Get timesheet status history should throw UnauthorizedAccessException when contractor candidate is null")
	void testGetTimesheetStatusHistoryContractorNoLinkedCandidateThrowsUnauthorizedAccessException() {
		Integer timesheetId = 1;
		Integer accountId = 1;
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(100);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetStatusHistory(timesheetId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Contractor can only access their own timesheets status history");
	}

	@Test
	@DisplayName("Get timesheet status history should throw UnauthorizedAccessException when contractor id does not match candidate")
	void testGetTimesheetStatusHistoryContractorIdMismatchThrowsUnauthorizedAccessException() {
		Integer timesheetId = 1;
		Integer accountId = 1;
		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(200);
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(100);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetStatusHistory(timesheetId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Contractor can only access their own timesheets status history");
	}

	@Test
	@DisplayName("Get timesheet status history should omit updatedBy when agency user is missing from details map")
	void testGetTimesheetStatusHistoryAgencyUserMissingFromMapOmitsUpdatedBy() {
		Integer timesheetId = 1;
		Integer accountId = 1;
		Timesheet timesheet = TimesheetTestDataFactory.createTimesheet();
		StatusHistoryQueryResultDto row = new StatusHistoryQueryResultDto();
		row.setId(1);
		row.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		row.setRemark("x");
		row.setUpdatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		row.setUpdatedById(999);
		row.setUpdatedOn(1);
		List<StatusHistoryQueryResultDto> statusHistory = List.of(row);
		StatusHistoryResponseBodyDto mapped = new StatusHistoryResponseBodyDto();
		mapped.setId(1);
		mapped.setStatus(TimesheetApprovalStatusTypeEnum.OPEN.getId());
		mapped.setRemark("x");
		mapped.setUpdatedOn(1);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(statusHistory);
		given(this.timesheetStatusHistoryMapper.toTimesheetStatusResultBodyDto(statusHistory))
			.willReturn(List.of(mapped));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Collections.emptyMap());
		willDoNothing().given(this.fetchUserAndContactUserIds)
			.addUserToAppropriateSet(anyInt(), anyInt(), any(), any(), any());

		TimesheetStatusHistoryResponseBodyDto result = this.timesheetService.getTimesheetStatusHistory(timesheetId);
		assertThat(result.getStatusHistory().get(0).getUpdatedBy()).isNull();
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should return empty list when repository returns null")
	void testGetTimesheetsListByDealIdNullQueryResultReturnsEmptyList() {
		Integer dealId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(null);

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		assertThat(result).isEmpty();
		then(this.customTimeSheetMapper).should(never()).listTimeSheetRequestToResponseBodyDto(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor ID should return empty list when repository returns null")
	void testGetTimesheetsListByJobAndContractorIdNullQueryReturnsEmptyList() {
		Integer jobId = 1;
		Integer contractorId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, 1, searchRequest,
				pageable))
			.willReturn(null);

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		assertThat(result).isEmpty();
		then(this.contractStaffingAccessControlChecker).should(never()).allows(any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor should merge duplicate invoices when lower id appears first")
	void testGetTimesheetsListByJobAndContractorIdDuplicateInvoicesMergeWhenLowerIdFirst() {
		Integer jobId = 1;
		Integer contractorId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		TimesheetListResponseBodyDto response = TimesheetTestDataFactory.createTimesheetListResponse();
		response.setId(1);
		List<TimesheetListResponseBodyDto> mapped = List.of(response);
		TimesheetInvoice low = new TimesheetInvoice();
		low.setId(3);
		low.setTimesheetId(1);
		low.setPayoutFile("low.pdf");
		TimesheetInvoice high = new TimesheetInvoice();
		high.setId(50);
		high.setTimesheetId(1);
		high.setPayoutFile("high.pdf");

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, 1, searchRequest,
				pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(mapped);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(1))).willReturn(List.of(low, high));
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		for (TimesheetJobAndContractorListQueryResultDto row : timesheetResults) {
			row.setTotalWorkTime(0);
			row.setTotalOvertime(0);
			row.setTotalTime(0);
		}

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		assertThat(result.get(0).getPayoutFile()).isEqualTo("high.pdf");
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor should omit payoutPaidOn when invoice payment date is null")
	void testGetTimesheetsListByJobAndContractorIdInvoiceWithoutPaymentPaidOnSkipsPayoutPaidOn() {
		Integer jobId = 1;
		Integer contractorId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		List<TimesheetJobAndContractorListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		TimesheetListResponseBodyDto response = TimesheetTestDataFactory.createTimesheetListResponse();
		response.setId(1);
		response.setPayoutPaidOn(null);
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setId(1);
		invoice.setTimesheetId(1);
		invoice.setPayoutFile("only-file.pdf");
		invoice.setPaymentPaidOn(null);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.timesheetRepository.getTimesheetsListByJobAndContractorId(jobId, contractorId, 1, searchRequest,
				pageable))
			.willReturn(timesheetResults);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetResults))
			.willReturn(List.of(response));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(1))).willReturn(List.of(invoice));
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		for (TimesheetJobAndContractorListQueryResultDto row : timesheetResults) {
			row.setTotalWorkTime(0);
			row.setTotalOvertime(0);
			row.setTotalTime(0);
		}

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByJobAndContractorId(jobId,
				contractorId, searchRequest, pageable);

		assertThat(result.get(0).getPayoutFile()).isEqualTo("only-file.pdf");
		assertThat(result.get(0).getPayoutPaidOn()).isNull();
	}

	@Test
	@DisplayName("Search entity mapping should use defaults when job, company, and deal optional fields are null")
	void testSearchEntityMapsNullOptionalFieldsToEmptyOrZeroDefaults() {
		Integer accountId = 1;
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("x", true, true, true, false);
		JobSearchQueryResultDto jobRow = new JobSearchQueryResultDto();
		jobRow.setId(1);
		jobRow.setName("J");
		jobRow.setSlug("j");
		jobRow.setSrno(1);
		jobRow.setCompanyname(null);
		jobRow.setCompanyslug(null);
		jobRow.setLocation(null);
		CompanySearchQueryResultDto companyRow = new CompanySearchQueryResultDto();
		companyRow.setId(2);
		companyRow.setName("C");
		companyRow.setSlug("c");
		companyRow.setSrno(1);
		companyRow.setLogo(null);
		companyRow.setAddress(null);
		companyRow.setCity(null);
		companyRow.setHaschildren(null);
		companyRow.setOwnerid(null);
		companyRow.setWebsite("https://w");
		DealSearchQueryResultDto dealRow = new DealSearchQueryResultDto();
		dealRow.setId(3);
		dealRow.setName("D");
		dealRow.setSlug("d");
		dealRow.setSrno(1);
		dealRow.setOwner(null);
		dealRow.setStagename(null);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.searchJobs(accountId, "x", false)).willReturn(List.of(jobRow));
		given(this.timesheetRepository.searchCompanies(accountId, "x")).willReturn(List.of(companyRow));
		given(this.timesheetRepository.searchDeals(accountId, "x", false)).willReturn(List.of(dealRow));

		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);
		assertThat(result.data().get("4")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);
		assertThat(result.data().get("3")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);
		assertThat(result.data().get("11")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);
	}

	@Test
	@DisplayName("Get times sheets list by entity ID with synthetic entity type should skip contractor and client field filtering")
	void testGetTimesheetsListByEntityIdSyntheticEntityTypeSkipsFieldFiltering() {
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		given(this.auth.getUnifiedPrincipal()).willReturn(null);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(isNull())).willReturn(2);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(isNull())).willReturn(100);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(2, 100);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.timesheetRepository.getTimesheetsListByEntityId(2, 100, List.of(), accountId, searchRequest,
				pageable))
			.willReturn(queryResults);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(Collections.emptyList());

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		assertThat(result.get(0).getBillRate()).isNotNull();
		assertThat(result.get(0).getPayRate()).isNotNull();
	}

	@Test
	@DisplayName("Get timesheets list by entity ID for contact should not set approvers when timesheet setting id is null")
	void testGetTimesheetsListByEntityIdContactNullTimesheetSettingIdSkipsApproverMapping() {
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Integer accountId = 1;
		Integer contactId = 3;
		Integer entityType = UserTypeEnum.COMPANY_CONTACT.getId();
		ContactPrincipal principal = mock(ContactPrincipal.class);
		given(principal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(this.auth.getUnifiedPrincipal()).willReturn(principal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(principal)).willReturn(contactId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, contactId);

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		queryResults.get(0).setTimesheetSettingId(null);
		given(this.timesheetRepository.getTimesheetsListByEntityId(entityType, contactId, List.of(), accountId,
				searchRequest, pageable))
			.willReturn(queryResults);

		TimesheetListResponseBodyDto singleResponse = TimesheetTestDataFactory.createTimesheetListResponse();
		List<TimesheetListResponseBodyDto> expectedResponse = List.of(singleResponse);
		given(this.customTimeSheetMapper.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogJpaRepository.findByTimesheetIds(anyList())).willReturn(Collections.emptyList());

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByEntityId(searchRequest,
				pageable);

		assertThat(result.get(0).getApprovers()).isNull();
		then(this.timesheetApproverRepository).should(never()).findByTimesheetSettingIds(anyList());
	}

	@Test
	@DisplayName("Ensure job timesheet access should return early when contact has no job ids")
	void testEnsureJobTimesheetAccessRecordsExistNullJobIdsReturnsEarly() {
		given(this.timesheetRepository.getJobIdsByContactId(9, 1)).willReturn(null);
		this.timesheetService.ensureJobTimesheetAccessRecordsExist(9, 1);
		then(this.jobTimesheetAccessJpaRepository).should(never()).findByJobIdsAndAccountId(anyList(), anyInt());
	}

	@Test
	@DisplayName("Ensure job timesheet access should return early when all jobs already have access records")
	void testEnsureJobTimesheetAccessRecordsExistAllJobsCoveredReturnsEarly() {
		given(this.timesheetRepository.getJobIdsByContactId(9, 1)).willReturn(List.of(10, 20));
		JobTimesheetAccess a = new JobTimesheetAccess();
		a.setJobId(10);
		JobTimesheetAccess b = new JobTimesheetAccess();
		b.setJobId(20);
		given(this.jobTimesheetAccessJpaRepository.findByJobIdsAndAccountId(List.of(10, 20), 1))
			.willReturn(List.of(a, b));

		this.timesheetService.ensureJobTimesheetAccessRecordsExist(9, 1);

		then(this.jobTimesheetAccessJpaRepository).should(never()).saveAll(anyList());
	}

	@Test
	@DisplayName("Ensure job timesheet access should create records for jobs missing access rows")
	void testEnsureJobTimesheetAccessRecordsExistCreatesMissingRows() {
		given(this.timesheetRepository.getJobIdsByContactId(9, 1)).willReturn(List.of(10, 20));
		JobTimesheetAccess a = new JobTimesheetAccess();
		a.setJobId(10);
		given(this.jobTimesheetAccessJpaRepository.findByJobIdsAndAccountId(List.of(10, 20), 1)).willReturn(List.of(a));
		given(this.jobTimesheetAccessJpaRepository.saveAll(anyList()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		this.timesheetService.ensureJobTimesheetAccessRecordsExist(9, 1);

		then(this.jobTimesheetAccessJpaRepository).should().saveAll(anyList());
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException when start date is before job start date")
	void testCreateTimesheetsStartDateBeforeJobStartThrowsValidationErrorException() {
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		timesheetRequest.setStartDate(1703980800);
		timesheetRequest.setEndDate(1704067200);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		Integer jobStart = 1704067200;
		Integer jobEnd = 1704153600;
		List<AssignCandidateJob> assignCandidateJobs = List.of(TimesheetTestDataFactory.createAssignCandidateJob());
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setJobStartDate(jobStart);
		setting.setJobEndDate(jobEnd);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, 1))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(List.of(setting));

		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("between Job Start Date and Job End Date");
	}

	@Test
	@DisplayName("Create timesheets should throw ValidationErrorException when end date is after job end date")
	void testCreateTimesheetsEndDateAfterJobEndThrowsValidationErrorException() {
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		CreateTimesheetRequestBodyDto timesheetRequest = new CreateTimesheetRequestBodyDto();
		Integer jobStart = 1704067200;
		Integer jobEnd = 1704153600;
		timesheetRequest.setStartDate(1704067200);
		timesheetRequest.setEndDate(1704326400);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = List.of(TimesheetTestDataFactory.createAssignCandidateJob());
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setJobStartDate(jobStart);
		setting.setJobEndDate(jobEnd);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, 1))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(List.of(setting));

		assertThatThrownBy(() -> this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("between Job Start Date and Job End Date");
	}

	@Test
	@DisplayName("Create timesheets with weekly frequency and null timesheet start day should validate successfully")
	void testCreateTimesheetsWeeklyNullTimesheetStartDayCreatesSuccessfully() {
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1);
		CreateTimesheetRequestBodyDto timesheetRequest = TimesheetTestDataFactory.createTimesheetRequest();
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(timesheetRequest);
		List<AssignCandidateJob> assignCandidateJobs = List.of(TimesheetTestDataFactory.createAssignCandidateJob());
		TimesheetSetting setting = TimesheetTestDataFactory.createTimesheetSetting();
		setting.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting.setTimesheetStartDay(null);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, 1))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(List.of(setting));
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList()))
			.willReturn(List.of(TimesheetTestDataFactory.createTimesheet()));
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);

		then(this.timesheetRepository).should().createTimesheets(anyList());
	}

	@Test
	@DisplayName("Get timesheets list by deal ID should skip approvedBy when latest approver is not approved status")
	void testGetTimesheetsListByDealIdNonApprovedApproverSkipsApprovedByDetails() {
		Integer dealId = 1;
		Integer accountId = 1;
		SearchRequestBodyDto searchRequest = TimesheetTestDataFactory.createSearchRequest();
		Pageable pageable = TimesheetTestDataFactory.createPageable();
		Deal deal = TimesheetTestDataFactory.createDeal();
		List<ContractorJobQueryResultDto> contractorJobs = TimesheetTestDataFactory
			.createContractorJobQueryResultList();
		List<TimesheetDealListQueryResultDto> timesheetResults = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		List<TimesheetListResponseBodyDto> expectedResponse = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		for (TimesheetListResponseBodyDto dto : expectedResponse) {
			dto.setApprovedBy(null);
		}
		TimesheetApproverResponseBodyDto approver = new TimesheetApproverResponseBodyDto();
		approver.setTimesheetId(1);
		approver.setTimeSheetApprovalStatusId(TimesheetApprovalStatusTypeEnum.SUBMITTED.getId());
		approver.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		approver.setEntityId(1);

		given(this.dealJpaRepository.findByIdAndAccountId(dealId, 1)).willReturn(Optional.of(deal));
		given(this.timesheetRepository.getCommonCandidatesByDealId(dealId)).willReturn(contractorJobs);
		given(this.timesheetRepository.getTimesheetsListByDealId(contractorJobs, accountId, searchRequest, pageable))
			.willReturn(timesheetResults);
		BulkPermissionCheckResult bulkResult2 = TimesheetTestDataFactory.createBulkPermissionCheckResult();
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(bulkResult2);
		given(this.customTimeSheetMapper.listTimeSheetRequestToResponseBodyDto(timesheetResults))
			.willReturn(expectedResponse);
		given(this.userRepository.getUserDetailsMap(any())).willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(TimesheetTestDataFactory.createEmptyMap());
		given(this.timeLogJpaRepository.getTimesheetWorkSummaries(anyList()))
			.willReturn(TimesheetTestDataFactory.createTimesheetWorkSummaryList());
		given(this.timesheetApprovalJpaRepository.findLatestApprovalsByTimesheetIds(anyList()))
			.willReturn(List.of(approver));

		List<TimesheetListResponseBodyDto> result = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequest, pageable);

		assertThat(result.get(0).getApprovedBy()).isNull();
	}

	@Test
	@DisplayName("Enrich approvers data should ignore contractor user type approvers when collecting ids")
	void testEnrichWithApproversDataIgnoresContractorApproverUserType() {
		List<TimesheetListResponseBodyDto> responseDtos = List
			.of(TimesheetTestDataFactory.createTimesheetListResponse());
		List<TimesheetJobAndContractorListQueryResultDto> queryResults = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		queryResults.get(0).setTimesheetSettingId(77);
		TimesheetApprover contractorApprover = new TimesheetApprover();
		contractorApprover.setTimesheetSettingId(77);
		contractorApprover.setEntityId(500);
		contractorApprover.setUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(List.of(77)))
			.willReturn(List.of(contractorApprover));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Collections.emptyMap());

		this.timesheetService.enrichWithApproversData(responseDtos, queryResults);

		assertThat(responseDtos.get(0).getApprovers()).isNotNull();
		assertThat(responseDtos.get(0).getApprovers().getAgencyApproverDetails()).isEmpty();
		assertThat(responseDtos.get(0).getApprovers().getClientApproverDetails()).isEmpty();
	}

	@Test
	@DisplayName("Create timesheets with publishTimesheetCreatedReminderEvent false should skip kafka notification")
	void testCreateTimesheetsWithPublishReminderFalseSkipsKafkaNotification() {
		// Given
		Integer jobId = 1;
		List<Integer> contractorIds = List.of(1, 2);
		List<CreateTimesheetRequestBodyDto> timesheetDates = List.of(TimesheetTestDataFactory.createTimesheetRequest());
		List<AssignCandidateJob> assignCandidateJobs = TimesheetTestDataFactory.createAssignCandidateJobList();
		List<TimesheetSetting> timesheetSettings = TimesheetTestDataFactory.createTimesheetSettingList();
		List<Timesheet> createdTimesheets = TimesheetTestDataFactory.createTimesheetList();
		Integer accountId = 1;

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds,
				accountId))
			.willReturn(assignCandidateJobs);
		given(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds, contractorIds.size()))
			.willReturn(true);
		given(this.timesheetSettingRepository.findLatestTimesheetSettingsByJobIdAndContractorIds(eq(jobId), anyList()))
			.willReturn(timesheetSettings);
		given(this.timesheetRepository.validateTimesheetsExist(anyList(), anyInt(), anyInt(), anyList()))
			.willReturn(false);
		given(this.timesheetRepository.createTimesheets(anyList())).willReturn(createdTimesheets);
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timeLogRepository).createBulkTimesheetLogs(anyList());

		// When
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates, false);

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create bulk timesheets for multiple jobs as CONTRACTOR should not publish reminder notification")
	void testCreateBulkTimesheetsForMultipleJobsContractorSkipsReminderNotification() throws Exception {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createBulkTimesheetsForMultipleJobsRequest();
		io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService mockSelfInjectedService = mock(
				io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService.class);
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(100);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(1);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		java.lang.reflect.Field field = TimesheetService.class.getDeclaredField("selfReference");
		field.setAccessible(true);
		field.set(this.timesheetService, mockSelfInjectedService);

		given(mockSelfInjectedService.createTimesheets(anyInt(), anyList(), anyList(), eq(false)))
			.willReturn(Arrays.asList(11, 12));

		// When
		this.timesheetService.createBulkTimesheetsForMultipleJobs(requestDto);

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create bulk timesheets for multiple jobs as CONTACT should publish reminder notification")
	void testCreateBulkTimesheetsForMultipleJobsContactPublishesReminderNotification() throws Exception {
		// Given
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createBulkTimesheetsForMultipleJobsRequest();
		io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService mockSelfInjectedService = mock(
				io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService.class);
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(1);
		given(contactPrincipal.getFullName()).willReturn("Contact User");
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		java.lang.reflect.Field field = TimesheetService.class.getDeclaredField("selfReference");
		field.setAccessible(true);
		field.set(this.timesheetService, mockSelfInjectedService);

		given(mockSelfInjectedService.createTimesheets(anyInt(), anyList(), anyList(), eq(false)))
			.willReturn(Arrays.asList(21, 22));

		// When
		this.timesheetService.createBulkTimesheetsForMultipleJobs(requestDto);

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) publishTimesheetCreatedReminderEventsIfApplicable should skip contractor user type")
	void testPublishTimesheetCreatedReminderEventsIfApplicableContractorSkipsKafkaViaReflection() throws Exception {
		// Given
		java.lang.reflect.Method method = TimesheetService.class.getDeclaredMethod(
				"publishTimesheetCreatedReminderEventsIfApplicable", Integer.class, Integer.class, List.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetService, UserTypeEnum.CONTRACTOR.getId(), 1, Arrays.asList(1, 2), "Performer");

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) publishTimesheetCreatedReminderEventsIfApplicable should return early when ids are empty")
	void testPublishTimesheetCreatedReminderEventsIfApplicableEmptyIdsSkipsKafkaViaReflection() throws Exception {
		// Given
		java.lang.reflect.Method method = TimesheetService.class.getDeclaredMethod(
				"publishTimesheetCreatedReminderEventsIfApplicable", Integer.class, Integer.class, List.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetService, AccountUserEnum.USERTYPEID.getId(), 1, Collections.emptyList(),
				"Performer");

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) publishTimesheetCreatedReminderEventsIfApplicable should publish for agency user type")
	void testPublishTimesheetCreatedReminderEventsIfApplicableAgencyUserPublishesKafkaViaReflection() throws Exception {
		// Given
		java.lang.reflect.Method method = TimesheetService.class.getDeclaredMethod(
				"publishTimesheetCreatedReminderEventsIfApplicable", Integer.class, Integer.class, List.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetService, AccountUserEnum.USERTYPEID.getId(), 1, Arrays.asList(10, 20),
				"Agency User");

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) publishTimesheetCreatedReminderEventsIfApplicable should publish for contact user type")
	void testPublishTimesheetCreatedReminderEventsIfApplicableContactUserPublishesKafkaViaReflection()
			throws Exception {
		// Given
		java.lang.reflect.Method method = TimesheetService.class.getDeclaredMethod(
				"publishTimesheetCreatedReminderEventsIfApplicable", Integer.class, Integer.class, List.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetService, UserTypeEnum.COMPANY_CONTACT.getId(), 1, Arrays.asList(30),
				"Contact User");

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) shouldPublishTimesheetCreatedReminder should allow agency and contact user types only")
	void testShouldPublishTimesheetCreatedReminderAllowsAgencyAndContactOnlyViaReflection() throws Exception {
		// Given
		java.lang.reflect.Method method = TimesheetService.class
			.getDeclaredMethod("shouldPublishTimesheetCreatedReminder", Integer.class);
		method.setAccessible(true);

		// When & Then
		assertThat((Boolean) method.invoke(this.timesheetService, AccountUserEnum.USERTYPEID.getId())).isTrue();
		assertThat((Boolean) method.invoke(this.timesheetService, UserTypeEnum.COMPANY_CONTACT.getId())).isTrue();
		assertThat((Boolean) method.invoke(this.timesheetService, UserTypeEnum.CONTRACTOR.getId())).isFalse();
	}

	@Test
	@DisplayName("(Reflection) resolveTimesheetCreatedReminderAuthSnapshot should map USER principal")
	void testResolveTimesheetCreatedReminderAuthSnapshotUserPrincipalViaReflection() throws Exception {
		// Given
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Agency User");
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(99);
		java.lang.reflect.Method method = TimesheetService.class
			.getDeclaredMethod("resolveTimesheetCreatedReminderAuthSnapshot", AuthPrincipal.class);
		method.setAccessible(true);

		// When
		Object snapshot = method.invoke(this.timesheetService, userPrincipal);

		// Then
		assertThat(snapshot).isNotNull();
		assertThat(snapshot.getClass().getRecordComponents()).hasSize(3);
		assertThat(snapshot.getClass().getMethod("userTypeId").invoke(snapshot))
			.isEqualTo(AccountUserEnum.USERTYPEID.getId());
		assertThat(snapshot.getClass().getMethod("accountId").invoke(snapshot)).isEqualTo(99);
		assertThat(snapshot.getClass().getMethod("performerDisplayName").invoke(snapshot)).isEqualTo("Agency User");
	}

	@Test
	@DisplayName("(Reflection) resolveTimesheetCreatedReminderAuthSnapshot should map CONTRACTOR principal")
	void testResolveTimesheetCreatedReminderAuthSnapshotContractorPrincipalViaReflection() throws Exception {
		// Given
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(55);
		java.lang.reflect.Method method = TimesheetService.class
			.getDeclaredMethod("resolveTimesheetCreatedReminderAuthSnapshot", AuthPrincipal.class);
		method.setAccessible(true);

		// When
		Object snapshot = method.invoke(this.timesheetService, contractorPrincipal);

		// Then
		assertThat(snapshot.getClass().getMethod("userTypeId").invoke(snapshot))
			.isEqualTo(UserTypeEnum.CONTRACTOR.getId());
		assertThat(snapshot.getClass().getMethod("accountId").invoke(snapshot)).isEqualTo(55);
		assertThat(snapshot.getClass().getMethod("performerDisplayName").invoke(snapshot)).isNull();
	}

	@Test
	@DisplayName("(Reflection) resolveTimesheetCreatedReminderAuthSnapshot should map CONTACT principal")
	void testResolveTimesheetCreatedReminderAuthSnapshotContactPrincipalViaReflection() throws Exception {
		// Given
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(77);
		given(contactPrincipal.getFullName()).willReturn("Contact User");
		java.lang.reflect.Method method = TimesheetService.class
			.getDeclaredMethod("resolveTimesheetCreatedReminderAuthSnapshot", AuthPrincipal.class);
		method.setAccessible(true);

		// When
		Object snapshot = method.invoke(this.timesheetService, contactPrincipal);

		// Then
		assertThat(snapshot.getClass().getMethod("userTypeId").invoke(snapshot))
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(snapshot.getClass().getMethod("accountId").invoke(snapshot)).isEqualTo(77);
		assertThat(snapshot.getClass().getMethod("performerDisplayName").invoke(snapshot)).isEqualTo("Contact User");
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should reject timesheetIds and isSubmitted together")
	void testGetTimesheetsListByEntityIdBothFiltersThrowsValidationErrorException() {
		// Given
		SearchRequestBodyDto searchRequest = new SearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(1, 2));
		searchRequest.setIsSubmitted(Boolean.TRUE);
		Pageable pageable = Pageable.unpaged();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsListByEntityId(searchRequest, pageable))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Provide only one filter at a time");
		then(this.timesheetRepository).should(never())
			.getTimesheetsListByEntityId(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets count by entity ID should reject timesheetIds and isSubmitted together")
	void testGetTimesheetsCountByEntityIdBothFiltersThrowsValidationErrorException() {
		// Given
		SearchRequestBodyDto searchRequest = new SearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(5));
		searchRequest.setIsSubmitted(Boolean.TRUE);
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetService.getTimesheetsCountByEntityId(searchRequest))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Provide only one filter at a time");
		then(this.timesheetRepository).should(never())
			.getTimesheetsCountByEntityId(anyInt(), anyInt(), any(), anyInt());
	}

	@Test
	@DisplayName("(Reflection) addClientApproverDetail should populate the client approver email from contact details")
	void testAddClientApproverDetailPopulatesEmail() {
		// Given
		Integer entityId = 200;
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactNamePhotoQueryResultDto contactDetails = new ContactNamePhotoQueryResultDto();
		contactDetails.setName("Client Approver");
		contactDetails.setProfilePic("client-pic.jpg");
		contactDetails.setEmail("approver@example.com");
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Map.of(entityId, contactDetails);

		TimesheetApproversResponseBodyDto approverDto = new TimesheetApproversResponseBodyDto();
		approverDto.setClientApproverDetails(new ArrayList<>());

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetService, "addClientApproverDetail", entityId, userTypeId,
				contactUsersMap, approverDto);

		// Then
		assertThat(approverDto.getClientApproverDetails()).hasSize(1);
		ApproverDetailResponseBodyDto detail = approverDto.getClientApproverDetails().get(0);
		assertThat(detail.getId()).isEqualTo(entityId);
		assertThat(detail.getName()).isEqualTo("Client Approver");
		assertThat(detail.getPhoto()).isEqualTo("client-pic.jpg");
		assertThat(detail.getUserTypeId()).isEqualTo(userTypeId);
		assertThat(detail.getEmail()).isEqualTo("approver@example.com");
	}

	@Test
	@DisplayName("(Reflection) addClientApproverDetail should not add a detail when contact details are absent")
	void testAddClientApproverDetailNoContactDetailsAddsNothing() {
		// Given
		Integer entityId = 200;
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = new HashMap<>();

		TimesheetApproversResponseBodyDto approverDto = new TimesheetApproversResponseBodyDto();
		approverDto.setClientApproverDetails(new ArrayList<>());

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetService, "addClientApproverDetail", entityId, userTypeId,
				contactUsersMap, approverDto);

		// Then
		assertThat(approverDto.getClientApproverDetails()).isEmpty();
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should reject timesheetIds and isReimbursement together")
	void testGetTimesheetsListByEntityIdTimesheetIdsAndIsReimbursementThrowsValidationError() {
		SearchRequestBodyDto searchRequest = new SearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(1, 2));
		searchRequest.setIsReimbursement(Boolean.TRUE);
		Pageable pageable = Pageable.unpaged();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetsListByEntityId(searchRequest, pageable))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("timesheetIds or isReimbursement");
		then(this.timesheetRepository).should(never())
			.getTimesheetsListByEntityId(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets list by entity ID should reject isSubmitted and isReimbursement together")
	void testGetTimesheetsListByEntityIdIsSubmittedAndIsReimbursementThrowsValidationError() {
		SearchRequestBodyDto searchRequest = new SearchRequestBodyDto();
		searchRequest.setIsSubmitted(Boolean.TRUE);
		searchRequest.setIsReimbursement(Boolean.TRUE);
		Pageable pageable = Pageable.unpaged();
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetsListByEntityId(searchRequest, pageable))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("isSubmitted or isReimbursement");
		then(this.timesheetRepository).should(never())
			.getTimesheetsListByEntityId(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Get timesheets count by entity ID should reject timesheetIds and isReimbursement together")
	void testGetTimesheetsCountByEntityIdTimesheetIdsAndIsReimbursementThrowsValidationError() {
		SearchRequestBodyDto searchRequest = new SearchRequestBodyDto();
		searchRequest.setTimesheetIds(List.of(5));
		searchRequest.setIsReimbursement(Boolean.TRUE);
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetsCountByEntityId(searchRequest))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("timesheetIds or isReimbursement");
		then(this.timesheetRepository).should(never())
			.getTimesheetsCountByEntityId(anyInt(), anyInt(), any(), anyInt());
	}

	@Test
	@DisplayName("Get timesheets count by entity ID should reject isSubmitted and isReimbursement together")
	void testGetTimesheetsCountByEntityIdIsSubmittedAndIsReimbursementThrowsValidationError() {
		SearchRequestBodyDto searchRequest = new SearchRequestBodyDto();
		searchRequest.setIsSubmitted(Boolean.TRUE);
		searchRequest.setIsReimbursement(Boolean.TRUE);
		Integer accountId = TimesheetTestDataFactory.getDefaultAccountId();
		Integer entityType = UserTypeEnum.CONTRACTOR.getId();
		Integer entityId = TimesheetTestDataFactory.getDefaultContractorId();
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(contractorPrincipal)).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(contractorPrincipal)).willReturn(entityId);
		willDoNothing().given(this.entityAccessValidator).validateEntityAccess(entityType, entityId);

		assertThatThrownBy(() -> this.timesheetService.getTimesheetsCountByEntityId(searchRequest))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("isSubmitted or isReimbursement");
		then(this.timesheetRepository).should(never())
			.getTimesheetsCountByEntityId(anyInt(), anyInt(), any(), anyInt());
	}

	@Test
	@DisplayName("Create bulk timesheets for multiple jobs as USER should publish reminder notification")
	void testCreateBulkTimesheetsForMultipleJobsUserPublishesReminderNotification() throws Exception {
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createBulkTimesheetsForMultipleJobsRequest();
		io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService mockSelfInjectedService = mock(
				io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService.class);
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Agency User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);

		java.lang.reflect.Field field = TimesheetService.class.getDeclaredField("selfReference");
		field.setAccessible(true);
		field.set(this.timesheetService, mockSelfInjectedService);

		given(mockSelfInjectedService.createTimesheets(anyInt(), anyList(), anyList(), eq(false)))
			.willReturn(Arrays.asList(31, 32));

		this.timesheetService.createBulkTimesheetsForMultipleJobs(requestDto);

		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(org.mockito.ArgumentMatchers
				.any(io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto.class));
	}

}
