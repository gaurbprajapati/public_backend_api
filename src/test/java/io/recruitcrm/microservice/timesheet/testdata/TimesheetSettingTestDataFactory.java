package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingUserPreference;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.EnableTimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingPreferenceResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;

import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.responses.APIResponseKeyMeta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public final class TimesheetSettingTestDataFactory {

	private TimesheetSettingTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// Default values
	public static Integer getDefaultJobId() {
		return 1;
	}

	public static Integer getDefaultContractorId() {
		return 2;
	}

	public static Integer getDefaultAccountId() {
		return 123;
	}

	public static Integer getDefaultUserId() {
		return 456;
	}

	public static Integer getDefaultUserTypeId() {
		return AccountUserEnum.USERTYPEID.getId();
	}

	public static Integer getDefaultCurrentUnixTimestamp() {
		return Math.toIntExact(Instant.now().getEpochSecond());
	}

	public static Integer getDefaultTimesheetSettingId() {
		return 10;
	}

	// DTOs
	public static TimesheetSettingBulkRequestBodyDto createTimesheetSettingBulkRequestBodyDto() {
		TimesheetSettingBulkRequestBodyDto dto = new TimesheetSettingBulkRequestBodyDto();
		dto.setContractorIds(Arrays.asList(getDefaultContractorId()));
		dto.setJobId(getDefaultJobId());
		dto.setJobStartDate(1633046400);
		dto.setJobEndDate(1635724800);
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(1);
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setPayCurrencyId(1);
		dto.setPayRate(25.0f);
		dto.setBillCurrencyId(1);
		dto.setBillRate(30.0f);
		dto.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5));
		dto.setWorkLogType(1);
		dto.setIsPreferencesModified(1);
		dto.setCalculateBreakTime(false);
		dto.setBreakTimeThreshold(30);
		dto.setIsRemarkMandatory(0);
		dto.setIsUnplannedHoursPayEnabled(0);
		dto.setWorkTime(Arrays.asList(8, 8, 8, 8, 8));
		dto.setWorkStartTime(Arrays.asList(9, 9, 9, 9, 9));
		dto.setWorkEndTime(Arrays.asList(17, 17, 17, 17, 17));
		dto.setCustomRules(createCustomRules());
		return dto;
	}

	public static TimesheetSettingRequestBodyDto createTimesheetSettingRequestBodyDto() {
		TimesheetSettingRequestBodyDto dto = new TimesheetSettingRequestBodyDto();
		dto.setJobStartDate(1633046400);
		dto.setJobEndDate(1635724800);
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(1);
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setPayCurrencyId(1);
		dto.setPayRate(25.0f);
		dto.setBillCurrencyId(1);
		dto.setBillRate(30.0f);
		dto.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5));
		dto.setWorkLogType(1);
		dto.setIsPreferencesModified(1);
		dto.setCalculateBreakTime(false);
		dto.setBreakTimeThreshold(30);
		dto.setIsRemarkMandatory(0);
		dto.setIsUnplannedHoursPayEnabled(0);
		dto.setWorkTime(Arrays.asList(8, 8, 8, 8, 8));
		dto.setWorkStartTime(Arrays.asList(9, 9, 9, 9, 9));
		dto.setWorkEndTime(Arrays.asList(17, 17, 17, 17, 17));
		dto.setCustomRules(createCustomRules());
		return dto;
	}

	public static ApproverRequestResponseBodyDto createApproverRequestResponseBodyDto() {
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Arrays.asList(1, 2));
		dto.setClientIds(Arrays.asList(3, 4));
		return dto;
	}

	public static TimesheetSettingResponseBodyDto createTimesheetSettingResponseBodyDto() {
		TimesheetSettingResponseBodyDto dto = new TimesheetSettingResponseBodyDto();
		dto.setId(getDefaultTimesheetSettingId());
		dto.setJobStartDate(1633046400);
		dto.setJobEndDate(1635724800);
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(1);
		dto.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5));
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setPayCurrencyId(1);
		dto.setBillCurrencyId(1);
		dto.setBillRate(30.0f);
		dto.setPayRate(25.0f);
		dto.setWorkLogType(1);
		dto.setCalculateBreakTime(false);
		dto.setBreakTimeThreshold(30);
		dto.setIsRemarkMandatory(0);
		dto.setIsUnplannedHoursPayEnabled(1);
		dto.setTemplateWorkDays(createTemplateWorkDays());
		dto.setCustomRules(createCustomRules());
		dto.setUpdatedOn(getDefaultCurrentUnixTimestamp());
		dto.setUpdatedBy(getDefaultUserId());
		dto.setUpdatedByUserTypeId(getDefaultUserTypeId());
		dto.setEnabledOn(getDefaultCurrentUnixTimestamp());
		dto.setEnabledBy(getDefaultUserId());
		dto.setEnabledByUserTypeId(getDefaultUserTypeId());
		return dto;
	}

	public static TimesheetSettingPreferenceResponseBodyDto createTimesheetSettingPreferenceResponseBodyDto() {
		TimesheetSettingPreferenceResponseBodyDto dto = new TimesheetSettingPreferenceResponseBodyDto();
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(1);
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setEnabledBy(getDefaultUserId());
		dto.setTemplateId(1);
		return dto;
	}

	public static EnableTimesheetSettingRequestBodyDto createEnableTimesheetSettingRequestBodyDto() {
		EnableTimesheetSettingRequestBodyDto dto = new EnableTimesheetSettingRequestBodyDto();
		dto.setAssignmentIds(Arrays.asList(1, 2, 3));
		return dto;
	}

	// Entities
	/**
	 * Timesheet setting with no persisted id (for persist-path repository tests).
	 */
	public static TimesheetSetting createTimesheetSettingWithoutId() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(null);
		setting.setAccountId(getDefaultAccountId());
		setting.setWorkLogType(1);
		return setting;
	}

	public static List<ContractorJobPairDto> createContractorJobPairDtos() {
		return Arrays.asList(new ContractorJobPairDto(getDefaultContractorId(), getDefaultJobId()),
				new ContractorJobPairDto(3, getDefaultJobId()));
	}

	public static List<ContractorJobPairDto> createSingleContractorJobPairDto() {
		return List.of(new ContractorJobPairDto(getDefaultContractorId(), getDefaultJobId()));
	}

	public static TimesheetSetting createTimesheetSetting() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setAccountId(getDefaultAccountId());
		setting.setCreatedOn(getDefaultCurrentUnixTimestamp());
		setting.setCreatedBy(getDefaultUserId());
		setting.setCreatedByUserTypeId(getDefaultUserTypeId());
		setting.setJobStartDate(1633046400);
		setting.setJobEndDate(1635724800);
		setting.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		setting.setTimesheetStartDay(1);
		setting.setPayCurrencyId(1);
		setting.setPayRate(25.0f);
		setting.setBillCurrencyId(1);
		setting.setBillRate(30.0f);
		setting.setWorkLogType(1);
		setting.setCalculateBreakTime(true);
		setting.setBreakTimeThreshold(30);
		setting.setIsRemarkMandatory(1);
		setting.setIsUnplannedHoursPayEnabled(0);
		setting.setIsUnplannedHoursPayEnabled(1);
		setting.setTemplateWorkDay(createTemplateWorkDays());
		setting.setCustomRule(createCustomRules());
		return setting;
	}

	public static AssignCandidateJob createAssignCandidateJob() {
		AssignCandidateJob assignment = new AssignCandidateJob();
		assignment.setJobId(getDefaultJobId());
		assignment.setCandidateId(getDefaultContractorId());
		assignment.setAccountId(getDefaultAccountId());
		return assignment;
	}

	public static TimesheetApprover createTimesheetApprover() {
		TimesheetApprover approver = new TimesheetApprover();
		approver.setId(1);
		approver.setTimesheetSettingId(getDefaultTimesheetSettingId());
		approver.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		approver.setEntityId(1);
		return approver;
	}

	public static TimesheetSettingAssociation createTimesheetSettingAssociation() {
		TimesheetSettingAssociation association = new TimesheetSettingAssociation();
		association.setId(1);
		association.setJobId(getDefaultJobId());
		association.setContractorId(getDefaultContractorId());
		association.setTimesheetSettings(Arrays.asList(createTimesheetSetting()));
		return association;
	}

	public static TimesheetSettingUserPreference createTimesheetSettingUserPreference() {
		TimesheetSettingUserPreference preference = new TimesheetSettingUserPreference();
		preference.setId(1);
		preference.setAccountId(getDefaultAccountId());
		preference.setTimesheetSettingJson("{\"timesheetFrequency\":1,\"timesheetStartDay\":1}");
		return preference;
	}

	// Helper methods
	public static List<TemplateWorkDay> createTemplateWorkDays() {
		return Arrays.asList(new TemplateWorkDay(1, 8, 9, 17), new TemplateWorkDay(2, 8, 9, 17),
				new TemplateWorkDay(3, 8, 9, 17), new TemplateWorkDay(4, 8, 9, 17), new TemplateWorkDay(5, 8, 9, 17));
	}

	public static List<CustomRule> createCustomRules() {
		CustomRule rule1 = new CustomRule();
		rule1.setId(1);
		rule1.setRuleType(1);
		rule1.setRuleName("Daily Overtime");

		CustomRule rule2 = new CustomRule();
		rule2.setId(2);
		rule2.setRuleType(2);
		rule2.setRuleName("Break Time");

		return Arrays.asList(rule1, rule2);
	}

	public static List<TimesheetApprover> createTimesheetApprovers() {
		TimesheetApprover approver1 = new TimesheetApprover();
		approver1.setId(1);
		approver1.setTimesheetSettingId(getDefaultTimesheetSettingId());
		approver1.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		approver1.setEntityId(1);

		TimesheetApprover approver2 = new TimesheetApprover();
		approver2.setId(2);
		approver2.setTimesheetSettingId(getDefaultTimesheetSettingId());
		approver2.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		approver2.setEntityId(2);

		return Arrays.asList(approver1, approver2);
	}

	public static List<AssignCandidateJob> createAssignCandidateJobs() {
		return createAssignCandidateJobsMatchingContractors(List.of(getDefaultContractorId()));
	}

	/**
	 * Assignments aligned with
	 * {@link TimesheetSettingBulkRequestBodyDto#getContractorIds()}.
	 */
	public static List<AssignCandidateJob> createAssignCandidateJobsMatchingContractors(List<Integer> contractorIds) {
		List<AssignCandidateJob> list = new java.util.ArrayList<>();
		for (Integer contractorId : contractorIds) {
			AssignCandidateJob a = new AssignCandidateJob();
			a.setJobId(getDefaultJobId());
			a.setCandidateId(contractorId);
			a.setAccountId(getDefaultAccountId());
			list.add(a);
		}
		return list;
	}

	/**
	 * Returns a list of AssignCandidateJob of the given size (for validation: size must
	 * match contractorIds.size()).
	 */
	public static List<AssignCandidateJob> createAssignCandidateJobs(int count) {
		if (count <= 0) {
			return new java.util.ArrayList<>();
		}
		if (count == 1) {
			return createAssignCandidateJobsMatchingContractors(List.of(getDefaultContractorId()));
		}
		List<Integer> ids = new java.util.ArrayList<>();
		for (int i = 1; i <= count; i++) {
			ids.add(i);
		}
		return createAssignCandidateJobsMatchingContractors(ids);
	}

	public static List<TimesheetSettingAssociation> createTimesheetSettingAssociations() {
		TimesheetSettingAssociation association1 = new TimesheetSettingAssociation();
		association1.setId(1);
		association1.setJobId(getDefaultJobId());
		association1.setContractorId(1);

		TimesheetSettingAssociation association2 = new TimesheetSettingAssociation();
		association2.setId(2);
		association2.setJobId(getDefaultJobId());
		association2.setContractorId(2);

		return Arrays.asList(association1, association2);
	}

	// Additional methods for controller tests
	public static Integer getDefaultStartDate() {
		return 1633046400;
	}

	public static Integer getDefaultEndDate() {
		return 1635724800;
	}

	public static Integer getDefaultJobStartDate() {
		return 1633046400;
	}

	public static Integer getDefaultJobEndDate() {
		return 1635724800;
	}

	public static Integer getDefaultTimesheetStartDay() {
		return 1; // Monday
	}

	public static Integer getDefaultCurrencyId() {
		return 1;
	}

	public static List<Integer> createEnabledAssignmentIds() {
		return Arrays.asList(1, 2, 3);
	}

	// Missing methods for controller tests
	public static TimesheetSettingResponseBodyDto createTimesheetSettingResponse() {
		TimesheetSettingResponseBodyDto dto = new TimesheetSettingResponseBodyDto();
		dto.setId(getDefaultTimesheetSettingId());
		dto.setJobStartDate(getDefaultJobStartDate());
		dto.setJobEndDate(getDefaultJobEndDate());
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5));
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setPayCurrencyId(getDefaultCurrencyId());
		dto.setBillCurrencyId(getDefaultCurrencyId());
		dto.setBillRate(100.0f);
		dto.setPayRate(80.0f);
		dto.setWorkLogType(1);
		dto.setCalculateBreakTime(false);
		dto.setBreakTimeThreshold(30);
		dto.setIsRemarkMandatory(0);
		dto.setIsUnplannedHoursPayEnabled(1);
		dto.setTemplateWorkDays(createTemplateWorkDays());
		dto.setCustomRules(createCustomRules());
		dto.setUpdatedOn(getDefaultCurrentUnixTimestamp());
		dto.setUpdatedBy(getDefaultUserId());
		dto.setUpdatedByUserTypeId(getDefaultUserTypeId());
		dto.setEnabledOn(getDefaultCurrentUnixTimestamp());
		dto.setEnabledBy(getDefaultUserId());
		dto.setEnabledByUserTypeId(getDefaultUserTypeId());
		return dto;
	}

	public static TimesheetSettingBulkRequestBodyDto createTimesheetSettingBulkRequest() {
		TimesheetSettingBulkRequestBodyDto dto = new TimesheetSettingBulkRequestBodyDto();
		dto.setContractorIds(Arrays.asList(1, 2, 3));
		dto.setJobId(getDefaultJobId());
		dto.setJobStartDate(getDefaultJobStartDate());
		dto.setJobEndDate(getDefaultJobEndDate());
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5));
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setPayCurrencyId(getDefaultCurrencyId());
		dto.setBillCurrencyId(getDefaultCurrencyId());
		dto.setBillRate(100.0f);
		dto.setPayRate(80.0f);
		dto.setWorkLogType(1);
		dto.setCalculateBreakTime(false);
		dto.setBreakTimeThreshold(30);
		dto.setIsRemarkMandatory(0);
		dto.setIsUnplannedHoursPayEnabled(0);
		dto.setCustomRules(createCustomRules());
		return dto;
	}

	public static TimesheetSettingPreferenceResponseBodyDto createTimesheetSettingPreferenceResponse() {
		TimesheetSettingPreferenceResponseBodyDto dto = new TimesheetSettingPreferenceResponseBodyDto();
		dto.setTimesheetFrequency(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId());
		dto.setTimesheetStartDay(getDefaultTimesheetStartDay());
		dto.setApprovers(createApproverRequestResponseBodyDto());
		dto.setEnabledBy(getDefaultUserId());
		dto.setTemplateId(1);
		return dto;
	}

	// Response creation methods
	public static ResponseEntity<APINormalResponse<TimesheetSettingResponseBodyDto>> createTimesheetSettingSuccessResponse(
			TimesheetSettingResponseBodyDto data) {
		APINormalResponse<TimesheetSettingResponseBodyDto> response = new APINormalResponse<>(data,
				new APIResponseKeyMeta("Timesheet setting fetched successfully"));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<Void>> createVoidSuccessResponse() {
		APINormalResponse<Void> response = new APINormalResponse<>(
				new APIResponseKeyMeta("Timesheet setting created successfully"), (Void) null);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	public static ResponseEntity<APINormalResponse<Boolean>> createBooleanSuccessResponse(Boolean data) {
		APINormalResponse<Boolean> response = new APINormalResponse<>(data,
				new APIResponseKeyMeta("Timesheet setting validation successfully"));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APIErrorResponse> createErrorResponse() {
		APIErrorResponse response = new APIErrorResponse("Timesheets exist for the given date",
				"Timesheets exist for the given date", APIResponseType.ERROR, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	public static ResponseEntity<APINormalResponse<List<Integer>>> createEnabledAssignmentIdsSuccessResponse(
			List<Integer> data) {
		APINormalResponse<List<Integer>> response = new APINormalResponse<>(data,
				new APIResponseKeyMeta("Timesheet setting enabled assignment ids successfully fetched"));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<TimesheetSettingPreferenceResponseBodyDto>> createTimesheetSettingPreferenceSuccessResponse(
			TimesheetSettingPreferenceResponseBodyDto data) {
		APINormalResponse<TimesheetSettingPreferenceResponseBodyDto> response = new APINormalResponse<>(data,
				new APIResponseKeyMeta("Timesheet setting preference fetched successfully"));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// Messages class for controller tests
	public static final class Messages {

		public static final String TIMESHEET_SETTING_FETCHED_SUCCESSFULLY = "Timesheet setting fetched successfully";

		public static final String TIMESHEET_SETTING_CREATED_SUCCESSFULLY = "Timesheet setting created successfully";

		public static final String TIMESHEET_SETTING_VALIDATION_SUCCESSFULLY = "Timesheet setting validation successfully";

		public static final String TIMESHEET_SETTING_ENABLED_ASSIGNMENT_IDS_SUCCESSFULLY_FETCHED = "Timesheet setting enabled assignment ids successfully fetched";

		public static final String TIMESHEET_SETTING_PREFERENCE_FETCHED_SUCCESSFULLY = "Timesheet setting preference fetched successfully";

		private Messages() {
			throw new UnsupportedOperationException("Utility class");
		}

	}

}