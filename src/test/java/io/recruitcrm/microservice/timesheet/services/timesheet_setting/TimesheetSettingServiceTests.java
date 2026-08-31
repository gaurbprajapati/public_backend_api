package io.recruitcrm.microservice.timesheet.services.timesheet_setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.DeserializationException;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingUserPreference;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.assigned_candidate.AssignCandidateJobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_setting_association.TimesheetSettingAssociationJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.EnableTimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingPreferenceResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetSettingMapper;
import io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template.IRuleTemplateRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetSettingTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TimesheetSettingServiceTests {

	@InjectMocks
	private TimesheetSettingService timesheetSettingService;

	@Mock
	private TimesheetSettingRepository timesheetSettingRepository;

	@Mock
	private AssignCandidateJobJpaRepository assignCandidateJobJpaRepository;

	@Mock
	private TimesheetApproverRepository timesheetApproverRepository;

	@Mock
	private TimesheetSettingMapper timesheetSettingMapper;

	@Mock
	private TimeLogRepository timeLogRepository;

	@Mock
	private TimesheetSettingAssociationJpaRepository timesheetSettingAssociationJpaRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private AccessControlChecker contractStaffingAccessControlChecker;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private IRuleTemplateRepository ruleTemplateRepository;

	private static final Integer JOB_ID = TimesheetSettingTestDataFactory.getDefaultJobId();

	private static final Integer CONTRACTOR_ID = TimesheetSettingTestDataFactory.getDefaultContractorId();

	private static final Integer ACCOUNT_ID = TimesheetSettingTestDataFactory.getDefaultAccountId();

	private static final Integer USER_ID = TimesheetSettingTestDataFactory.getDefaultUserId();

	private static final Integer TIMESHEET_SETTING_ID = TimesheetSettingTestDataFactory.getDefaultTimesheetSettingId();

	@BeforeEach
	void setUp() {
		// No global stubs to avoid unnecessary stubbing errors
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success")
	void testGetTimesheetSettingByAssignmentIdSuccessfully() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(expectedResponse.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(Integer.valueOf(0));
		assertThat(result.getUpdatedByUserTypeId()).isEqualTo(timesheetSetting.getCreatedByUserTypeId());
		assertThat(result.getEnabledByUserTypeId()).isEqualTo(timesheetSetting.getCreatedByUserTypeId());
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID, ACCOUNT_ID);
		then(this.timesheetSettingRepository).should().findByJobIdContractorId(JOB_ID, CONTRACTOR_ID);
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Assignment not found")
	void testGetTimesheetSettingByAssignmentIdAssignmentNotFound() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID, CONTRACTOR_ID))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Assignment not found for Job Id: " + JOB_ID + " candidate Id : " + CONTRACTOR_ID);

		then(this.timesheetSettingRepository).should(never()).findByJobIdContractorId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Timesheet setting not found")
	void testGetTimesheetSettingByAssignmentIdTimesheetSettingNotFound() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID, CONTRACTOR_ID))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(
					"TimesheetSetting not found for Job ID: " + JOB_ID + " and Contractor ID: " + CONTRACTOR_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - First timesheet setting not found")
	void testGetTimesheetSettingByAssignmentIdFirstTimesheetSettingNotFound() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID, CONTRACTOR_ID))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(
					"TimesheetSetting not found for Job ID: " + JOB_ID + " and Contractor ID: " + CONTRACTOR_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Deserialization exception")
	void testGetTimesheetSettingByAssignmentIdDeserializationException() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = Mockito.mock(TimesheetSetting.class);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto responseDto = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(timesheetSetting.getId()).willReturn(TIMESHEET_SETTING_ID);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(responseDto);

		// Mock the getTemplateWorkDay() method to throw an exception
		given(timesheetSetting.getTemplateWorkDay()).willThrow(new RuntimeException("Deserialization error"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID, CONTRACTOR_ID))
			.isInstanceOf(DeserializationException.class)
			.hasMessageContaining("Failed to deserialize or validate JSON data");
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with isRemarkMandatory 1")
	void testGetTimesheetSettingByAssignmentIdWithIsRemarkMandatoryOne() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		timesheetSetting.setIsRemarkMandatory(1);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();
		expectedResponse.setIsRemarkMandatory(1);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getIsRemarkMandatory()).isEqualTo(expectedResponse.getIsRemarkMandatory());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with isRemarkMandatory null")
	void testGetTimesheetSettingByAssignmentIdWithIsRemarkMandatoryNull() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		timesheetSetting.setIsRemarkMandatory(null);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();
		expectedResponse.setIsRemarkMandatory(null);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(expectedResponse.getIsRemarkMandatory());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with isUnplannedHoursPayEnabled set to 1")
	void testGetTimesheetSettingByAssignmentIdWithIsUnplannedHoursPayEnabledOne() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		timesheetSetting.setIsUnplannedHoursPayEnabled(1);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();
		expectedResponse.setIsUnplannedHoursPayEnabled(1);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(expectedResponse.getIsUnplannedHoursPayEnabled());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with isUnplannedHoursPayEnabled set to 0")
	void testGetTimesheetSettingByAssignmentIdWithIsUnplannedHoursPayEnabledZero() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		timesheetSetting.setIsUnplannedHoursPayEnabled(0);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();
		expectedResponse.setIsUnplannedHoursPayEnabled(0);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(Integer.valueOf(0));
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(expectedResponse.getIsUnplannedHoursPayEnabled());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with empty approvers list")
	void testGetTimesheetSettingByAssignmentIdWithEmptyApprovers() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetApprover> emptyApprovers = List.of();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();
		expectedResponse.setApprovers(null);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(emptyApprovers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, emptyApprovers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovers()).isNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(Integer.valueOf(0));
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with all fields mapped correctly")
	void testGetTimesheetSettingByAssignmentIdAllFieldsMappedCorrectly() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		timesheetSetting.setIsRemarkMandatory(1);
		timesheetSetting.setCalculateBreakTime(false);
		timesheetSetting.setBreakTimeThreshold(60);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();
		expectedResponse.setIsRemarkMandatory(1);
		expectedResponse.setCalculateBreakTime(false);
		expectedResponse.setBreakTimeThreshold(60);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(expectedResponse.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getCalculateBreakTime()).isFalse();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(Integer.valueOf(60));
		assertThat(result.getJobStartDate()).isEqualTo(timesheetSetting.getJobStartDate());
		assertThat(result.getJobEndDate()).isEqualTo(timesheetSetting.getJobEndDate());
		assertThat(result.getTimesheetFrequency()).isEqualTo(timesheetSetting.getTimesheetFrequency());
		assertThat(result.getPayRate()).isEqualTo(timesheetSetting.getPayRate());
		assertThat(result.getBillRate()).isEqualTo(timesheetSetting.getBillRate());
		assertThat(result.getUpdatedOn()).isEqualTo(timesheetSetting.getCreatedOn());
		assertThat(result.getUpdatedBy()).isEqualTo(timesheetSetting.getCreatedBy());
		assertThat(result.getUpdatedByUserTypeId()).isEqualTo(timesheetSetting.getCreatedByUserTypeId());
		assertThat(result.getEnabledOn()).isEqualTo(timesheetSetting.getCreatedOn());
		assertThat(result.getEnabledBy()).isEqualTo(timesheetSetting.getCreatedBy());
		assertThat(result.getEnabledByUserTypeId()).isEqualTo(timesheetSetting.getCreatedByUserTypeId());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Get timesheet setting by assignment ID - Success with different first timesheet setting")
	void testGetTimesheetSettingByAssignmentIdWithDifferentFirstTimesheetSetting() {
		// Given
		AssignCandidateJob assignment = TimesheetSettingTestDataFactory.createAssignCandidateJob();
		TimesheetSetting timesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting firstTimesheetSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		firstTimesheetSetting.setId(999);
		firstTimesheetSetting.setCreatedOn(1000000);
		firstTimesheetSetting.setCreatedBy(999);
		firstTimesheetSetting.setCreatedByUserTypeId(999);
		List<TimesheetApprover> approvers = TimesheetSettingTestDataFactory.createTimesheetApprovers();
		TimesheetSettingResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponseBodyDto();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdAndAccountId(JOB_ID, CONTRACTOR_ID,
				ACCOUNT_ID))
			.willReturn(assignment);
		given(this.timesheetSettingRepository.findByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetSettingRepository.findFirstByJobIdContractorId(JOB_ID, CONTRACTOR_ID))
			.willReturn(Optional.of(firstTimesheetSetting));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID)).willReturn(approvers);
		given(this.timesheetSettingMapper.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingResponseBodyDto result = this.timesheetSettingService.getTimesheetSettingByAssignmentId(JOB_ID,
				CONTRACTOR_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getEnabledOn()).isEqualTo(firstTimesheetSetting.getCreatedOn());
		assertThat(result.getEnabledBy()).isEqualTo(firstTimesheetSetting.getCreatedBy());
		assertThat(result.getEnabledByUserTypeId()).isEqualTo(firstTimesheetSetting.getCreatedByUserTypeId());
		assertThat(result.getUpdatedOn()).isEqualTo(timesheetSetting.getCreatedOn());
		assertThat(result.getUpdatedBy()).isEqualTo(timesheetSetting.getCreatedBy());
		assertThat(result.getUpdatedByUserTypeId()).isEqualTo(timesheetSetting.getCreatedByUserTypeId());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Success")
	void testCreateBulkTimesheetSettingsSuccessfully() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.contractStaffingAccessControlChecker).should()
			.allows(eq(Entity.TIMESHEET_SETTINGS), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
		then(this.assignCandidateJobJpaRepository).should()
			.findByJobIdAndCandidateIdsAndAccountId(JOB_ID, request.getContractorIds(), ACCOUNT_ID);
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@ParameterizedTest
	@CsvSource({ "1782777600, 1769990400, 'Job end date before job start date'",
			"1633046400, 1633046399, 'Job end date one second before start date'",
			"1, 0, 'Minimum epoch values job dates'", "2147483647, 2147483646, 'Large epoch values job dates'" })
	@DisplayName("Create bulk timesheet settings - Job end date validation errors")
	void testCreateBulkTimesheetSettingsJobEndDateValidationErrors(Integer jobStartDate, Integer jobEndDate,
			String scenario) {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setJobStartDate(jobStartDate);
		request.setJobEndDate(jobEndDate);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Job end date should be greater than job start date");
	}

	@ParameterizedTest
	@CsvSource({ "0", "5", "-1", "10" })
	@DisplayName("Create bulk timesheet settings - Timesheet frequency must be between 1 and 4")
	void testCreateBulkTimesheetSettingsInvalidTimesheetFrequency(Integer invalidFrequency) {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setTimesheetFrequency(invalidFrequency);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("timesheet frequency can be between 1 to 4");
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Timesheet frequency null")
	void testCreateBulkTimesheetSettingsNullTimesheetFrequency() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setTimesheetFrequency(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("timesheet frequency can be between 1 to 4");
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Timesheet frequency 4 is valid")
	void testCreateBulkTimesheetSettingsTimesheetFrequencyFourIsValid() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory.createAssignCandidateJobs();
		AssignCandidateJob contractor2Assignment = assignments.get(0);
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();
		request.setTimesheetFrequency(4);
		request.setTimesheetStartDay(100); // Last day for monthly
		settingRequest.setTimesheetFrequency(4);
		settingRequest.setTimesheetStartDay(100);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(List.of(contractor2Assignment));
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - timesheet frequency 4 is accepted
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
	}

	@ParameterizedTest
	@CsvSource({ "0", "3", "4", "-1" })
	@DisplayName("Create bulk timesheet settings - workLogType must be 1 or 2")
	void testCreateBulkTimesheetSettingsInvalidWorkLogType(Integer invalidWorkLogType) {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setWorkLogType(invalidWorkLogType);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("workLogType can be only 1 or 2");
	}

	@Test
	@DisplayName("Create bulk timesheet settings - workLogType null")
	void testCreateBulkTimesheetSettingsNullWorkLogType() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setWorkLogType(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("workLogType can be only 1 or 2");
	}

	@Test
	@DisplayName("Create bulk timesheet settings - workLogType 2 is valid")
	void testCreateBulkTimesheetSettingsWorkLogTypeTwoIsValid() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory.createAssignCandidateJobs();
		AssignCandidateJob contractor2Assignment = assignments.get(0);
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();
		request.setWorkLogType(2);
		settingRequest.setWorkLogType(2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(List.of(contractor2Assignment));
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - workLogType 2 is accepted
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Job start and end dates equal")
	void testCreateBulkTimesheetSettingsJobStartAndEndDatesEqual() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory.createAssignCandidateJobs();
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		request.setJobStartDate(1633046400);
		request.setJobEndDate(1633046400); // Same as start date
		settingRequest.setJobStartDate(1633046400);
		settingRequest.setJobEndDate(1633046400);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Should not throw exception, equal dates are valid
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Job end date after start date valid scenario")
	void testCreateBulkTimesheetSettingsJobEndDateAfterStartDateValid() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory.createAssignCandidateJobs();
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		request.setJobStartDate(1633046400);
		request.setJobEndDate(1635724800); // 30 days later
		settingRequest.setJobStartDate(1633046400);
		settingRequest.setJobEndDate(1635724800);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Should complete successfully
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Job end date one second after start date")
	void testCreateBulkTimesheetSettingsJobEndDateOneSecondAfterStartDate() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory.createAssignCandidateJobs();
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		request.setJobStartDate(1633046400);
		request.setJobEndDate(1633046401); // One second after start date
		settingRequest.setJobStartDate(1633046400);
		settingRequest.setJobEndDate(1633046401);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Should complete successfully
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Zero epoch values for both dates")
	void testCreateBulkTimesheetSettingsZeroEpochValues() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory.createAssignCandidateJobs();
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		request.setJobStartDate(0);
		request.setJobEndDate(0); // Same as start date (both zero)
		settingRequest.setJobStartDate(0);
		settingRequest.setJobEndDate(0);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Should complete successfully (equal dates are valid)
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Assignment validation failure")
	void testCreateBulkTimesheetSettingsAssignmentValidationFailure() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(Collections.emptyList()); // Return empty list to trigger
													// exception

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining(
					"Assignment not found for Job Id: " + JOB_ID + " candidate Ids : " + request.getContractorIds());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Timesheet start date validation failure")
	void testCreateBulkTimesheetSettingsTimesheetStartDateValidationFailure() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		request.setTimesheetStartDay(null); // This should trigger validation error
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		settingRequest.setTimesheetStartDay(null);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet start date cannot be null");
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Invalid timesheet start date")
	void testCreateBulkTimesheetSettingsInvalidTimesheetStartDate() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		request.setTimesheetStartDay(50); // Invalid date
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		settingRequest.setTimesheetStartDay(50);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet start date must be between 1 to 28 OR Last Day(100)");
	}

	@Test
	@DisplayName("Get user timesheet setting preference - Success with existing preference")
	void testGetUserTimesheetSettingPreferenceSuccessfullyWithExistingPreference() throws Exception {
		// Given
		TimesheetSettingUserPreference preference = Mockito.mock(TimesheetSettingUserPreference.class);
		TimesheetSettingPreferenceResponseBodyDto expectedResponse = TimesheetSettingTestDataFactory
			.createTimesheetSettingPreferenceResponseBodyDto();
		String preferenceJson = "{\"timesheetFrequency\":1,\"timesheetStartDay\":1}";

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);
		given(this.timesheetSettingRepository.findUserPreferenceByAccountIdAndUserId(ACCOUNT_ID, USER_ID))
			.willReturn(Optional.of(preference));
		given(this.ruleTemplateRepository.findDefaultTemplateIdByAccountId(ACCOUNT_ID)).willReturn(1);
		given(preference.getTimesheetSettingJson()).willReturn(preferenceJson);
		given(this.objectMapper.readValue(preferenceJson, TimesheetSettingPreferenceResponseBodyDto.class))
			.willReturn(expectedResponse);

		// When
		TimesheetSettingPreferenceResponseBodyDto result = this.timesheetSettingService
			.getUserTimesheetSettingPreference();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTemplateId()).isEqualTo(1);
		then(this.timesheetSettingRepository).should().findUserPreferenceByAccountIdAndUserId(ACCOUNT_ID, USER_ID);
		then(this.objectMapper).should().readValue(preferenceJson, TimesheetSettingPreferenceResponseBodyDto.class);
	}

	@Test
	@DisplayName("Get user timesheet setting preference - Success with no existing preference")
	void testGetUserTimesheetSettingPreferenceSuccessfullyWithNoExistingPreference() throws Exception {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);
		given(this.timesheetSettingRepository.findUserPreferenceByAccountIdAndUserId(ACCOUNT_ID, USER_ID))
			.willReturn(Optional.empty());
		given(this.ruleTemplateRepository.findDefaultTemplateIdByAccountId(ACCOUNT_ID)).willReturn(1);

		// When
		TimesheetSettingPreferenceResponseBodyDto result = this.timesheetSettingService
			.getUserTimesheetSettingPreference();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTemplateId()).isEqualTo(1);
		then(this.timesheetSettingRepository).should().findUserPreferenceByAccountIdAndUserId(ACCOUNT_ID, USER_ID);
		then(this.objectMapper).should(never()).readValue(anyString(), any(Class.class));
	}

	@Test
	@DisplayName("Get user timesheet setting preference - Deserialization exception")
	void testGetUserTimesheetSettingPreferenceDeserializationException() throws Exception {
		// Given
		TimesheetSettingUserPreference preference = Mockito.mock(TimesheetSettingUserPreference.class);
		String preferenceJson = "{\"timesheetFrequency\":1,\"timesheetStartDay\":1}";

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);
		given(this.timesheetSettingRepository.findUserPreferenceByAccountIdAndUserId(ACCOUNT_ID, USER_ID))
			.willReturn(Optional.of(preference));
		given(this.ruleTemplateRepository.findDefaultTemplateIdByAccountId(ACCOUNT_ID)).willReturn(1);
		given(preference.getTimesheetSettingJson()).willReturn(preferenceJson);
		given(this.objectMapper.readValue(preferenceJson, TimesheetSettingPreferenceResponseBodyDto.class))
			.willThrow(new RuntimeException("JSON parsing error"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.getUserTimesheetSettingPreference())
			.isInstanceOf(DeserializationException.class)
			.hasMessageContaining("Error parsing timesheet setting preference");
	}

	@Test
	@DisplayName("Get timesheet setting date validation - Success")
	void testGetTimesheetSettingDateValidationSuccessfully() {
		// Given
		Long startDate = 1633046400L;
		Long endDate = 1635724800L;
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timeLogRepository.validateByDate(JOB_ID, CONTRACTOR_ID, startDate, endDate, ACCOUNT_ID))
			.willReturn(true);

		// When
		Boolean result = this.timesheetSettingService.getTimesheetSettingDateValidation(JOB_ID, CONTRACTOR_ID,
				startDate, endDate);

		// Then
		assertThat(result).isTrue();
		then(this.timeLogRepository).should().validateByDate(JOB_ID, CONTRACTOR_ID, startDate, endDate, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Get timesheet setting date validation - Failure")
	void testGetTimesheetSettingDateValidationFailure() {
		// Given
		Long startDate = 1633046400L;
		Long endDate = 1635724800L;
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timeLogRepository.validateByDate(JOB_ID, CONTRACTOR_ID, startDate, endDate, ACCOUNT_ID))
			.willReturn(false);

		// When
		Boolean result = this.timesheetSettingService.getTimesheetSettingDateValidation(JOB_ID, CONTRACTOR_ID,
				startDate, endDate);

		// Then
		assertThat(result).isFalse();
		then(this.timeLogRepository).should().validateByDate(JOB_ID, CONTRACTOR_ID, startDate, endDate, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Get enabled assignment IDs - Success")
	void testGetEnabledAssignmentIdsSuccessfully() {
		// Given
		EnableTimesheetSettingRequestBodyDto request = TimesheetSettingTestDataFactory
			.createEnableTimesheetSettingRequestBodyDto();
		List<Integer> expectedIds = Arrays.asList(1, 2, 3);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingRepository.fetchEnabledAssignmentIds(request.getAssignmentIds(), ACCOUNT_ID))
			.willReturn(expectedIds);

		// When
		List<Integer> result = this.timesheetSettingService.getEnabledAssigmentIds(request);

		// Then
		assertThat(result).isEqualTo(expectedIds);
		then(this.timesheetSettingRepository).should()
			.fetchEnabledAssignmentIds(request.getAssignmentIds(), ACCOUNT_ID);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should update both agency and client approvers")
	void testCreateBulkTimesheetSettingsShouldUpdateBothAgencyAndClientApprovers() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetApproverRepository).should()
			.deleteTimesheetApprovers(JOB_ID, request.getContractorIds(), request.getApprovers().getAgencyIds(),
					ACCOUNT_ID);
		then(this.timesheetApproverRepository).should()
			.addTimesheetApprovers(JOB_ID, request.getContractorIds(), request.getApprovers().getAgencyIds(),
					ACCOUNT_ID);
		then(this.timesheetApproverRepository).should()
			.deleteTimesheetApproversForClients(JOB_ID, request.getContractorIds(),
					request.getApprovers().getClientIds(), ACCOUNT_ID);
		then(this.timesheetApproverRepository).should()
			.addTimesheetApproversForClients(JOB_ID, request.getContractorIds(), request.getApprovers().getClientIds(),
					ACCOUNT_ID);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should handle single contractor update for agency approvers")
	void testCreateBulkTimesheetSettingsShouldHandleSingleContractorUpdateForAgencyApprovers() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Arrays.asList(CONTRACTOR_ID)); // Single contractor
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = Arrays
			.asList(TimesheetSettingTestDataFactory.createAssignCandidateJob());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Verify old agency approvers are deleted and new ones are added
		then(this.timesheetApproverRepository).should()
			.deleteTimesheetApprovers(JOB_ID, request.getContractorIds(), request.getApprovers().getAgencyIds(),
					ACCOUNT_ID);
		then(this.timesheetApproverRepository).should()
			.addTimesheetApprovers(JOB_ID, request.getContractorIds(), request.getApprovers().getAgencyIds(),
					ACCOUNT_ID);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should handle single contractor update for client approvers")
	void testCreateBulkTimesheetSettingsShouldHandleSingleContractorUpdateForClientApprovers() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Arrays.asList(CONTRACTOR_ID)); // Single contractor
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = Arrays
			.asList(TimesheetSettingTestDataFactory.createAssignCandidateJob());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Verify old client approvers are deleted and new ones are added
		then(this.timesheetApproverRepository).should()
			.deleteTimesheetApproversForClients(JOB_ID, request.getContractorIds(),
					request.getApprovers().getClientIds(), ACCOUNT_ID);
		then(this.timesheetApproverRepository).should()
			.addTimesheetApproversForClients(JOB_ID, request.getContractorIds(), request.getApprovers().getClientIds(),
					ACCOUNT_ID);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should maintain separate ID sequences for agency and client approvers")
	void testCreateBulkTimesheetSettingsShouldMaintainSeparateIdSequencesForAgencyAndClientApprovers() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Arrays.asList(CONTRACTOR_ID)); // Single contractor
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		List<AssignCandidateJob> assignments = Arrays
			.asList(TimesheetSettingTestDataFactory.createAssignCandidateJob());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then - Verify both approver types are processed independently
		then(this.timesheetApproverRepository).should()
			.deleteTimesheetApprovers(eq(JOB_ID), anyList(), anyList(), eq(ACCOUNT_ID));
		then(this.timesheetApproverRepository).should()
			.addTimesheetApprovers(eq(JOB_ID), anyList(), anyList(), eq(ACCOUNT_ID));
		then(this.timesheetApproverRepository).should()
			.deleteTimesheetApproversForClients(eq(JOB_ID), anyList(), anyList(), eq(ACCOUNT_ID));
		then(this.timesheetApproverRepository).should()
			.addTimesheetApproversForClients(eq(JOB_ID), anyList(), anyList(), eq(ACCOUNT_ID));
		// Verify all four operations are called exactly once
		then(this.timesheetApproverRepository).should(times(1))
			.deleteTimesheetApprovers(anyInt(), anyList(), anyList(), anyInt());
		then(this.timesheetApproverRepository).should(times(1))
			.addTimesheetApprovers(anyInt(), anyList(), anyList(), anyInt());
		then(this.timesheetApproverRepository).should(times(1))
			.deleteTimesheetApproversForClients(anyInt(), anyList(), anyList(), anyInt());
		then(this.timesheetApproverRepository).should(times(1))
			.addTimesheetApproversForClients(anyInt(), anyList(), anyList(), anyInt());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should set isRemarkMandatory to 0 when null")
	void testCreateBulkTimesheetSettingsShouldSetIsRemarkMandatoryToZeroWhenNull() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(null); // Explicitly set to null
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(null);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsRemarkMandatory(null); // Ensure mapper doesn't provide a default
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				// Verify isRemarkMandatory is set to 0 when null (defaults to 0)
				assertThat(setting.getIsRemarkMandatory()).isEqualTo(Integer.valueOf(0));
				return setting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should default isClientExpenseSharingEnabled to 1 when not provided")
	void testCreateBulkTimesheetSettingsShouldSetIsClientExpenseSharingEnabledToOneWhenNull() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsClientExpenseSharingEnabled(null); // Explicitly set to null
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsClientExpenseSharingEnabled(null);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsClientExpenseSharingEnabled(null); // Ensure mapper doesn't
															// provide a default
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				// Verify isClientExpenseSharingEnabled defaults to 1 (checked by default,
				// BR-001)
				assertThat(setting.getIsClientExpenseSharingEnabled()).isEqualTo(Integer.valueOf(1));
				return setting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update isRemarkMandatory to 1 for all same association when provided")
	void testCreateBulkTimesheetSettingsShouldUpdateIsRemarkMandatoryToOneWhenProvided() {
		// Given: isRemarkMandatory=1 should create new version then update all settings
		// with same association ID
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(20);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = existingAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: new version created and then all settings with same association ID
		// updated
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update isRemarkMandatory to 0 for all same association when explicitly provided")
	void testCreateBulkTimesheetSettingsShouldUpdateIsRemarkMandatoryToZeroWhenExplicitlyProvided() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(0);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(0);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(21);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = existingAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 0);
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update isRemarkMandatory when no existing associations (new associations created)")
	void testCreateBulkTimesheetSettingsShouldNotUpdateWhenNoAssociationsExist() {
		// Given: no existing associations; full flow creates new associations and new
		// settings, then updates them
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(22);
		TimesheetSettingAssociation newAssociation = new TimesheetSettingAssociation();
		newAssociation.setId(99);
		newAssociation.setJobId(JOB_ID);
		newAssociation.setContractorId(request.getContractorIds().get(0));

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(Collections.emptyList());
		given(this.timesheetSettingAssociationJpaRepository.save(any(TimesheetSettingAssociation.class)))
			.willReturn(newAssociation);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: new settings created, then update isRemarkMandatory for new association
		// IDs
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should()
			.updateIsRemarkMandatoryByAssociationIds(eq(Collections.singletonList(99)), eq(1));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - When isRemarkMandatory not sent (null) should set 0 for all same association")
	void testCreateBulkTimesheetSettingsShouldCreateNewSettingsWhenIsRemarkMandatoryIsNull() {
		// Given: not sending isRemarkMandatory means 0
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(null);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(null);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();
		// Request has default contractorIds [2], so only association for contractor 2 is
		// used
		List<Integer> associationIds = existingAssociations.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.map(TimesheetSettingAssociation::getId)
			.toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: new settings created and update isRemarkMandatory to 0 (not sent = 0)
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
		then(this.timesheetSettingRepository).should()
			.updateIsRemarkMandatoryByAssociationIds(eq(associationIds), eq(0));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new versions and update multiple associations when isRemarkMandatory provided")
	void testCreateBulkTimesheetSettingsShouldUpdateMultipleAssociationsWhenIsRemarkMandatoryProvided() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Arrays.asList(1, 2, 3));
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(30);

		TimesheetSettingAssociation association1 = new TimesheetSettingAssociation();
		association1.setId(1);
		association1.setJobId(JOB_ID);
		association1.setContractorId(1);
		TimesheetSettingAssociation association2 = new TimesheetSettingAssociation();
		association2.setId(2);
		association2.setJobId(JOB_ID);
		association2.setContractorId(2);
		TimesheetSettingAssociation association3 = new TimesheetSettingAssociation();
		association3.setId(3);
		association3.setJobId(JOB_ID);
		association3.setContractorId(3);
		List<TimesheetSettingAssociation> existingAssociations = Arrays.asList(association1, association2,
				association3);
		List<Integer> associationIds = Arrays.asList(1, 2, 3);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: new version per contractor and update all association IDs
		then(this.timesheetSettingRepository).should(times(3)).createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new versions for all and update when some contractors have no existing associations")
	void testCreateBulkTimesheetSettingsShouldUpdateOnlyExistingAssociationsWhenSomeContractorsHaveNoAssociations() {
		// Given: contractors 1,2 have existing associations; contractor 3 gets new
		// association
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Arrays.asList(1, 2, 3));
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(31);
		TimesheetSettingAssociation association1 = new TimesheetSettingAssociation();
		association1.setId(1);
		association1.setJobId(JOB_ID);
		association1.setContractorId(1);
		TimesheetSettingAssociation association2 = new TimesheetSettingAssociation();
		association2.setId(2);
		association2.setJobId(JOB_ID);
		association2.setContractorId(2);
		TimesheetSettingAssociation newAssociation3 = new TimesheetSettingAssociation();
		newAssociation3.setId(10);
		newAssociation3.setJobId(JOB_ID);
		newAssociation3.setContractorId(3);
		List<TimesheetSettingAssociation> existingAssociations = Arrays.asList(association1, association2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingAssociationJpaRepository.save(any(TimesheetSettingAssociation.class)))
			.willReturn(newAssociation3);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: 3 new settings created; update called with association IDs 1, 2, 10 (Set
		// order may vary)
		then(this.timesheetSettingRepository).should(times(3)).createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(anyList(), eq(1));
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should validate break time threshold even when isRemarkMandatory provided")
	void testCreateBulkTimesheetSettingsShouldValidateBreakTimeThresholdWhenUpdatingIsRemarkMandatory() {
		// Given: break time validation runs first and throws before any DB or auth use
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		request.setCalculateBreakTime(false);
		request.setBreakTimeThreshold(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetSettingService.createBulkTimesheetSettings(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Break time threshold is required when calculate break time is disabled");

		// No DB operations (validation throws before getAssignmentAndValidateTimesheet)
		then(this.timesheetSettingAssociationJpaRepository).should(never())
			.findByJobIdAndContractorIdIn(anyInt(), anyList());
		then(this.timesheetSettingRepository).should(never())
			.updateIsRemarkMandatoryByAssociationIds(anyList(), anyInt());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should check permissions when creating new version with isRemarkMandatory")
	void testCreateBulkTimesheetSettingsShouldCheckPermissionsWhenUpdatingIsRemarkMandatory() {
		// Given: permission check runs first; then full create flow + update
		// isRemarkMandatory
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(24);
		List<TimesheetSettingAssociation> filteredAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = filteredAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(filteredAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.contractStaffingAccessControlChecker).should()
			.allows(eq(Entity.TIMESHEET_SETTINGS), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should call mapper and create new version then update isRemarkMandatory when provided")
	void testCreateBulkTimesheetSettingsShouldNotCallMapperWhenUpdatingIsRemarkMandatory() {
		// Given: when isRemarkMandatory provided we still create new version (mapper
		// called) then update all same association ID
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(25);
		List<TimesheetSettingAssociation> filteredAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = filteredAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(filteredAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: mapper called, new version created, update isRemarkMandatory for
		// association IDs
		then(this.timesheetSettingMapper).should().toRequestDto(request);
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should handle empty contractor list when isRemarkMandatory provided (no create, no update)")
	void testCreateBulkTimesheetSettingsShouldHandleEmptyContractorListWhenUpdatingIsRemarkMandatory() {
		// Given: empty contractor list; loop runs 0 times, associationIdsUsed stays
		// empty, so update not called
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Collections.emptyList());
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				Collections.emptyList(), ACCOUNT_ID))
			.willReturn(Collections.emptyList());
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				Collections.emptyList()))
			.willReturn(Collections.emptyList());
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: no settings created (loop 0 times), update not called (associationIdsUsed
		// empty)
		then(this.timesheetSettingRepository).should(never()).createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should(never())
			.updateIsRemarkMandatoryByAssociationIds(anyList(), anyInt());
		// Service still calls createTimesheetApproverInBulk with empty list when no
		// contractors
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update all versions of settings with same association_id")
	void testCreateBulkTimesheetSettingsShouldUpdateAllVersionsOfSettingsWithSameAssociationId() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(26);
		TimesheetSettingAssociation association = new TimesheetSettingAssociation();
		association.setId(100);
		association.setJobId(JOB_ID);
		association.setContractorId(CONTRACTOR_ID);
		List<TimesheetSettingAssociation> existingAssociations = Arrays.asList(association);
		List<Integer> associationIds = Arrays.asList(100);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: new version created, then updateIsRemarkMandatoryByAssociationIds updates
		// ALL settings (all versions) with same association_id
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should()
			.updateIsRemarkMandatoryByAssociationIds(eq(associationIds), eq(1));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update isRemarkMandatory when associations exist")
	void testCreateBulkTimesheetSettingsShouldUpdateIsRemarkMandatoryWhenAssociationsExist() {
		// Given: existing associations
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(27);
		List<TimesheetSettingAssociation> filteredAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = filteredAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(filteredAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update isRemarkMandatory with single contractor")
	void testCreateBulkTimesheetSettingsShouldHandleIsRemarkMandatoryUpdateWithSingleContractor() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setContractorIds(Arrays.asList(CONTRACTOR_ID));
		request.setIsRemarkMandatory(0);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(0);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(28);
		TimesheetSettingAssociation association = new TimesheetSettingAssociation();
		association.setId(50);
		association.setJobId(JOB_ID);
		association.setContractorId(CONTRACTOR_ID);
		List<TimesheetSettingAssociation> existingAssociations = Arrays.asList(association);
		List<Integer> associationIds = Arrays.asList(50);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 0);
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should set isUnplannedHoursPayEnabled to 0 on entity when null in request and NOT bulk-update old versions")
	void testCreateBulkTimesheetSettingsShouldSetIsUnplannedHoursPayEnabledToZeroWhenNull() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsUnplannedHoursPayEnabled(null);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsUnplannedHoursPayEnabled(null);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsUnplannedHoursPayEnabled(null);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				assertThat(setting.getIsUnplannedHoursPayEnabled()).isEqualTo(Integer.valueOf(0));
				return setting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: value is set on the new entity (asserted above via willAnswer),
		// and the bulk-update that would have overwritten old versions is never called.
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should(never())
			.updateIsUnplannedHoursPayEnabledByAssociationIds(anyList(), anyInt());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should set isUnplannedHoursPayEnabled to 1 on new entity only and NOT bulk-update old versions")
	void testCreateBulkTimesheetSettingsShouldUpdateIsUnplannedHoursPayEnabledToOneWhenProvided() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsUnplannedHoursPayEnabled(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsUnplannedHoursPayEnabled(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsUnplannedHoursPayEnabled(1);
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(30);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				assertThat(setting.getIsUnplannedHoursPayEnabled()).isEqualTo(1);
				return savedSetting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should(never())
			.updateIsUnplannedHoursPayEnabledByAssociationIds(anyList(), anyInt());
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should set isUnplannedHoursPayEnabled to 0 on new entity only when explicitly provided as 0")
	void testCreateBulkTimesheetSettingsShouldUpdateIsUnplannedHoursPayEnabledToZeroWhenExplicitlyProvided() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsUnplannedHoursPayEnabled(0);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsUnplannedHoursPayEnabled(0);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsUnplannedHoursPayEnabled(0);
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(31);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				assertThat(setting.getIsUnplannedHoursPayEnabled()).isZero();
				return savedSetting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should(never())
			.updateIsUnplannedHoursPayEnabledByAssociationIds(anyList(), anyInt());
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - isRemarkMandatory bulk-updates all versions; isUnplannedHoursPayEnabled is set on new entity only")
	void testCreateBulkTimesheetSettingsShouldCallBothBulkUpdatesInSameRequest() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		request.setIsUnplannedHoursPayEnabled(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		settingRequest.setIsUnplannedHoursPayEnabled(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsUnplannedHoursPayEnabled(1);
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(32);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = existingAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				assertThat(setting.getIsUnplannedHoursPayEnabled()).isEqualTo(1);
				return savedSetting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: isRemarkMandatory still bulk-updates all historical versions (intended).
		// isUnplannedHoursPayEnabled is set on the new entity only (asserted via
		// willAnswer)
		// and the bulk UPDATE for it must NOT be called.
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
		then(this.timesheetSettingRepository).should(never())
			.updateIsUnplannedHoursPayEnabledByAssociationIds(anyList(), anyInt());
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should NOT call isUnplannedHoursPayEnabled update when no association IDs collected")
	void testCreateBulkTimesheetSettingsShouldNotCallIsUnplannedHoursPayEnabledUpdateWhenNoAssociationsCollected() {
		// Given: empty contractor list → loop never runs → associationIdsUsed stays empty
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsUnplannedHoursPayEnabled(1);
		request.setContractorIds(Collections.emptyList());
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsUnplannedHoursPayEnabled(1);
		List<AssignCandidateJob> assignments = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(Collections.emptyList());
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(TimesheetSettingTestDataFactory.createTimesheetSetting());

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should(never())
			.updateIsUnplannedHoursPayEnabledByAssociationIds(anyList(), anyInt());
		then(this.timesheetApproverRepository).should().createTimesheetApproverInBulk(anyList());
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should set isUnplannedHoursPayEnabled to 1 on entity when provided in request")
	void testCreateBulkTimesheetSettingsShouldSetIsUnplannedHoursPayEnabledToOneOnEntityWhenProvided() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsUnplannedHoursPayEnabled(1);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsUnplannedHoursPayEnabled(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		baseSetting.setIsUnplannedHoursPayEnabled(null);
		List<TimesheetSettingAssociation> existingAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(existingAssociations);
		given(this.timesheetSettingMapper.toEntity(JOB_ID, settingRequest)).willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(baseSetting)).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willAnswer((invocation) -> {
				TimesheetSetting setting = invocation.getArgument(0);
				assertThat(setting.getIsUnplannedHoursPayEnabled()).isEqualTo(Integer.valueOf(1));
				return setting;
			});

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then
		then(this.timesheetSettingRepository).should(times(request.getContractorIds().size()))
			.createTimesheetSetting(any(TimesheetSetting.class));
	}

	@Test
	@DisplayName("Create bulk timesheet settings - Should create new version and update only isRemarkMandatory for all same association (other fields preserved in bulk update)")
	void testCreateBulkTimesheetSettingsShouldPreserveOtherFieldsWhenUpdatingIsRemarkMandatory() {
		// Given
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		request.setIsRemarkMandatory(1);
		request.setCalculateBreakTime(true);
		request.setBreakTimeThreshold(30);
		TimesheetSettingRequestBodyDto settingRequest = TimesheetSettingTestDataFactory
			.createTimesheetSettingRequestBodyDto();
		settingRequest.setIsRemarkMandatory(1);
		List<AssignCandidateJob> assignments = TimesheetSettingTestDataFactory
			.createAssignCandidateJobs(request.getContractorIds().size());
		TimesheetSetting baseSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		TimesheetSetting savedSetting = TimesheetSettingTestDataFactory.createTimesheetSetting();
		savedSetting.setId(29);
		List<TimesheetSettingAssociation> filteredAssociations = TimesheetSettingTestDataFactory
			.createTimesheetSettingAssociations()
			.stream()
			.filter((a) -> request.getContractorIds().contains(a.getContractorId()))
			.toList();
		List<Integer> associationIds = filteredAssociations.stream().map(TimesheetSettingAssociation::getId).toList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.timesheetSettingMapper.toRequestDto(request)).willReturn(settingRequest);
		given(this.assignCandidateJobJpaRepository.findByJobIdAndCandidateIdsAndAccountId(JOB_ID,
				request.getContractorIds(), ACCOUNT_ID))
			.willReturn(assignments);
		given(this.timesheetSettingAssociationJpaRepository.findByJobIdAndContractorIdIn(JOB_ID,
				request.getContractorIds()))
			.willReturn(filteredAssociations);
		given(this.timesheetSettingMapper.toEntity(eq(JOB_ID), any(TimesheetSettingRequestBodyDto.class)))
			.willReturn(baseSetting);
		given(this.timesheetSettingMapper.copyTimesheetSetting(any(TimesheetSetting.class))).willReturn(baseSetting);
		given(this.timesheetSettingRepository.createTimesheetSetting(any(TimesheetSetting.class)))
			.willReturn(savedSetting);

		// When
		this.timesheetSettingService.createBulkTimesheetSettings(request);

		// Then: new version created; updateIsRemarkMandatoryByAssociationIds only updates
		// isRemarkMandatory (other fields preserved)
		then(this.timesheetSettingRepository).should().createTimesheetSetting(any(TimesheetSetting.class));
		then(this.timesheetSettingRepository).should().updateIsRemarkMandatoryByAssociationIds(associationIds, 1);
	}

}
