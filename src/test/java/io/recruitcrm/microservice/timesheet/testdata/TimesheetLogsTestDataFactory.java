package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.workTimeEnum;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchBulkContractorTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchContractorBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogWorkSummaryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

/**
 * Test data factory for TimesheetLogs-related test objects.
 */
public final class TimesheetLogsTestDataFactory {

	private TimesheetLogsTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	public static io.recruitcrm.microservice.timesheet.dto.time_log.bulk.BulkTimeLogRequestBodyDto createBulkTimeLogRequest() {
		io.recruitcrm.microservice.timesheet.dto.time_log.bulk.BulkTimeLogRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.time_log.bulk.BulkTimeLogRequestBodyDto();
		request.setTimesheetIds(Arrays.asList(1, 2, 3));
		return request;
	}

	public static BulkUpdateTimeLogsRequestBodyDto createBulkUpdateTimeLogsRequest() {
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setIsApproved(true);
		request.setTimeLogs(createBulkTimeLogRequestList());
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.portal.GetPortalTimeLogsRequestBodyDto createGetPortalTimeLogsRequest() {
		io.recruitcrm.microservice.timesheet.dto.portal.GetPortalTimeLogsRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.portal.GetPortalTimeLogsRequestBodyDto();
		request.setJobId(getDefaultJobId());
		return request;
	}

	public static List<BulkTimeLogRequestBodyDto> createBulkTimeLogRequestList() {
		BulkTimeLogRequestBodyDto timeLog1 = createIndividualBulkTimeLogRequest();
		BulkTimeLogRequestBodyDto timeLog2 = createIndividualBulkTimeLogRequest();
		timeLog2.setId(2);
		timeLog2.setTimesheetId(2);
		return Arrays.asList(timeLog1, timeLog2);
	}

	public static BulkTimeLogRequestBodyDto createIndividualBulkTimeLogRequest() {
		BulkTimeLogRequestBodyDto request = new BulkTimeLogRequestBodyDto();
		request.setId(1);
		request.setTimesheetId(getDefaultTimesheetId());
		request.setWorkTime(8);
		request.setBreakTime(60); // 1 hour
		request.setOverTime(0);
		request.setRemark("Test time log");
		request.setTotalTime(8);
		// Create workTimeDetails with workStartTime, workEndTime, and breakIntervals
		WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
		workTimeDetail.setWorkStartTime(540); // 9:00 AM
		workTimeDetail.setWorkEndTime(1020); // 5:00 PM
		workTimeDetail.setBreakIntervals(createBreakIntervalDtoList());
		request.setWorkTimeDetails(Arrays.asList(workTimeDetail));
		return request;
	}

	private static List<BreakIntervalDto> createBreakIntervalDtoList() {
		BreakIntervalDto interval = new BreakIntervalDto();
		interval.setBreakStartTime(540); // 9:00 AM
		interval.setBreakEndTime(570); // 9:30 AM
		return Arrays.asList(interval);
	}

	public static List<BreakIntervalRequestBodyDto> createBreakIntervalRequestList() {
		BreakIntervalRequestBodyDto interval = new BreakIntervalRequestBodyDto();
		interval.setBreakStartTime(540); // 9:00 AM
		interval.setBreakEndTime(570); // 9:30 AM
		return Arrays.asList(interval);
	}

	// ===== Response DTOs =====

	public static TimesheetResponseBodyDto createTimesheetResponse() {
		TimesheetResponseBodyDto response = new TimesheetResponseBodyDto();
		response.setTimesheetId(getDefaultTimesheetId());
		response.setTimesheetStartDay(1);
		return response;
	}

	public static FetchBulkTimelogValidatedResponseBodyDto createFetchBulkTimelogValidatedResponse() {
		return new FetchBulkTimelogValidatedResponseBodyDto();
	}

	public static FetchBulkTimelogResultBodyDto createFetchBulkTimelogResult() {
		return new FetchBulkTimelogResultBodyDto();
	}

	public static FetchContractorBulkTimelogValidatedResponseBodyDto createFetchContractorBulkTimelogValidatedResponse() {
		return new FetchContractorBulkTimelogValidatedResponseBodyDto();
	}

	public static FetchBulkContractorTimelogResultBodyDto createFetchBulkContractorTimelogResult() {
		return new FetchBulkContractorTimelogResultBodyDto();
	}

	public static TimesheetSettingErrorResponseBodyDto createTimesheetSettingErrorResponse() {
		TimesheetSettingErrorResponseBodyDto errorResponse = new TimesheetSettingErrorResponseBodyDto();
		errorResponse.setTimesheetId(getDefaultTimesheetId());
		errorResponse.setError("Timesheet setting mismatch");
		return errorResponse;
	}

	public static ContractorTimesheetSettingErrorResponseBodyDto createContractorTimesheetSettingErrorResponse() {
		ContractorTimesheetSettingErrorResponseBodyDto errorResponse = new ContractorTimesheetSettingErrorResponseBodyDto();
		errorResponse.setTimesheetId(getDefaultTimesheetId());
		errorResponse.setError("Contractor timesheet setting mismatch");
		return errorResponse;
	}

	// ===== API Response Entities =====

	public static ResponseEntity<APINormalResponse<TimesheetResponseBodyDto>> createTimesheetResponseSuccessResponse(
			TimesheetResponseBodyDto data) {
		APINormalResponse<TimesheetResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<Void>> createVoidSuccessResponse() {
		APINormalResponse<Void> response = new APINormalResponse<>(null);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<FetchBulkTimelogResultBodyDto>> createFetchBulkTimelogResultSuccessResponse(
			FetchBulkTimelogResultBodyDto data) {
		APINormalResponse<FetchBulkTimelogResultBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<FetchBulkContractorTimelogResultBodyDto>> createFetchBulkContractorTimelogResultSuccessResponse(
			FetchBulkContractorTimelogResultBodyDto data) {
		APINormalResponse<FetchBulkContractorTimelogResultBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Test IDs and Constants =====

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultJobId() {
		return 1;
	}

	public static Integer getDefaultAccountId() {
		return 1;
	}

	public static Integer getDefaultUserId() {
		return 1;
	}

	public static Integer getDefaultTimesheetSettingId() {
		return 1;
	}

	// ===== Entity Objects =====

	public static Timesheet createTimesheet() {
		Timesheet timesheet = new Timesheet();
		timesheet.setId(getDefaultTimesheetId());
		timesheet.setTimesheetSettingId(getDefaultTimesheetSettingId());
		timesheet.setPeriodStart(1704067200);
		timesheet.setPeriodEnd(1704153600);
		return timesheet;
	}

	public static TimesheetLogQueryResultDto createTimesheetLogQueryResult() {
		TimesheetLogQueryResultDto queryResult = new TimesheetLogQueryResultDto();
		queryResult.setTimesheetId(getDefaultTimesheetId());
		queryResult.setTimesheetSettingId(getDefaultTimesheetSettingId());
		queryResult.setEntityId(getDefaultUserId());
		queryResult.setUserTypeId(1);
		queryResult.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		queryResult.setTimesheetStartDay(1);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setPayCurrencySymbol("$");
		queryResult.setPayCurrencyCode("USD");
		queryResult.setBillCurrencySymbol("$");
		queryResult.setBillCurrencyCode("USD");
		queryResult.setCustomRules(Arrays.asList());
		return queryResult;
	}

	public static TimesheetLogQueryResultDto createTimesheetLogQueryResultForAgencyRecruiter() {
		TimesheetLogQueryResultDto queryResult = createTimesheetLogQueryResult();
		queryResult.setUserTypeId(2); // Agency recruiter
		return queryResult;
	}

	public static TimesheetLogQueryResultDto createTimesheetLogQueryResultForCompanyContact() {
		TimesheetLogQueryResultDto queryResult = createTimesheetLogQueryResult();
		queryResult.setUserTypeId(1); // Company contact
		return queryResult;
	}

	public static TimesheetLogQueryResultDto createTimesheetLogQueryResultForContractor() {
		TimesheetLogQueryResultDto queryResult = createTimesheetLogQueryResult();
		queryResult.setUserTypeId(3); // Contractor
		return queryResult;
	}

	public static TimesheetLogQueryResultDto createTimesheetLogQueryResultWithBreakTimeThreshold() {
		TimesheetLogQueryResultDto queryResult = createTimesheetLogQueryResult();
		queryResult.setCalculateBreakTime(false);
		queryResult.setBreakTimeThreshold(30);
		return queryResult;
	}

	public static List<TimeLog> createTimeLogList() {
		TimeLog timeLog = createTimeLog();
		return Arrays.asList(timeLog);
	}

	public static TimeLog createTimeLog() {
		TimeLog timeLog = new TimeLog();
		timeLog.setId(1);
		timeLog.setTimesheetId(getDefaultTimesheetId());
		timeLog.setWorkTime(8);
		timeLog.setBreakTime(1);
		timeLog.setOverTime(0);
		timeLog.setTotalTime(8);
		timeLog.setRemark("Test time log");
		return timeLog;
	}

	public static TimesheetApproval createTimesheetApproval() {
		TimesheetApproval approval = new TimesheetApproval();
		approval.setId(1);
		approval.setTimesheetId(getDefaultTimesheetId());
		approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		approval.setUserTypeId(1);
		approval.setEntityId(getDefaultUserId());
		approval.setCreatedOn(1704067200);
		return approval;
	}

	public static TimesheetApproval createTimesheetApprovalWithApprovedStatus() {
		TimesheetApproval approval = createTimesheetApproval();
		approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		return approval;
	}

	public static TimesheetApproval createTimesheetApprovalWithRejectedStatus() {
		TimesheetApproval approval = createTimesheetApproval();
		approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.REJECTED.getId());
		approval.setRemark("Rejected for testing");
		approval.setCreatedOn(1704067200);
		return approval;
	}

	public static TimesheetApproval createTimesheetApprovalWithSubmittedStatus() {
		TimesheetApproval approval = createTimesheetApproval();
		approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		return approval;
	}

	public static TimesheetApproval createTimesheetApprovalWithOpenStatus() {
		TimesheetApproval approval = createTimesheetApproval();
		approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		return approval;
	}

	public static List<TimesheetApprover> createTimesheetApproverList() {
		TimesheetApprover approver = createTimesheetApprover();
		return Arrays.asList(approver);
	}

	public static TimesheetApprover createTimesheetApprover() {
		TimesheetApprover approver = new TimesheetApprover();
		approver.setId(1);
		approver.setTimesheetSettingId(getDefaultTimesheetSettingId());
		approver.setUserTypeId(1);
		approver.setEntityId(getDefaultUserId());
		return approver;
	}

	public static List<CustomRule> createCustomRuleList() {
		CustomRule rule = createCustomRule();
		return Arrays.asList(rule);
	}

	public static CustomRule createCustomRule() {
		CustomRule rule = new CustomRule();
		rule.setId(1);
		rule.setRuleType(5); // RANGE_BASED_WEEKLY_OVERTIME ID
		rule.setRuleName("Test Rule");
		return rule;
	}

	public static List<TimeLogWorkSummaryQueryResultDto> createTimeLogWorkSummaryQueryResult() {
		TimeLogWorkSummaryQueryResultDto summary = new TimeLogWorkSummaryQueryResultDto();
		summary.setTimesheetId(getDefaultTimesheetId());
		summary.setTotalWorkTime(40L);
		summary.setTotalBreakTime(5L);
		summary.setTotalOvertime(0L);
		summary.setTotalTime(40L);
		return Arrays.asList(summary);
	}

	public static TimeLogBreakInterval createTimeLogBreakInterval() {
		TimeLogBreakInterval interval = new TimeLogBreakInterval();
		interval.setId(1);
		interval.setTimeLogId(1);
		interval.setBreakStartTime(540); // 9:00 AM in minutes
		interval.setBreakEndTime(570); // 9:30 AM in minutes
		return interval;
	}

	public static List<TimeLogBreakInterval> createTimeLogBreakIntervalList() {
		TimeLogBreakInterval interval = new TimeLogBreakInterval();
		interval.setId(1);
		interval.setTimeLogId(1);
		interval.setBreakStartTime(540); // 9:00 AM in minutes
		interval.setBreakEndTime(570); // 9:30 AM in minutes
		return Arrays.asList(interval);
	}

	public static TimesheetSetting createTimesheetSetting() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(getDefaultTimesheetSettingId());
		setting.setWorkLogType(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType.WORK_HOUR.getTypeId());
		setting.setCalculateBreakTime(true);
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	public static TimesheetSetting createTimesheetSettingForStartEndTimeEntry() {
		TimesheetSetting setting = createTimesheetSetting();
		setting.setWorkLogType(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType.START_AND_END_TIME.getTypeId());
		return setting;
	}

	public static TimesheetSetting createTimesheetSettingForWorkTimeEntry() {
		TimesheetSetting setting = createTimesheetSetting();
		setting.setWorkLogType(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType.WORK_HOUR.getTypeId());
		return setting;
	}

	// ===== DTO Objects =====

	public static UserDetailsQueryResultDto createUserDetailsQueryResult() {
		UserDetailsQueryResultDto userDetails = new UserDetailsQueryResultDto();
		userDetails.setName("Test User");
		userDetails.setProfilePic("test-profile-pic.jpg");
		return userDetails;
	}

	public static ContactNamePhotoQueryResultDto createContactNamePhotoQueryResult() {
		return new ContactNamePhotoQueryResultDto("Test Contact", "test-contact-photo.jpg", null);
	}

	public static ContractorNamePhotoQueryResultDto createContractorNamePhotoQueryResult() {
		ContractorNamePhotoQueryResultDto contractorDetails = new ContractorNamePhotoQueryResultDto();
		contractorDetails.setName("Test Contractor");
		contractorDetails.setProfilePic("test-contractor-photo.jpg");
		contractorDetails.setSlug("test-contractor");
		return contractorDetails;
	}

	public static TimeLogResponseBodyDto createTimeLogResponse() {
		TimeLogResponseBodyDto response = new TimeLogResponseBodyDto();
		response.setId(1);
		response.setTimesheetId(getDefaultTimesheetId());
		response.setTimesheetPeriod("2024-01-01 to 2024-01-07");
		response.setDate(1704067200);
		response.setDayTypeId(1);
		response.setWorkTime(8);
		response.setWorkStartTime(9);
		response.setWorkEndTime(17);
		response.setBreakTime(1);
		response.setOverTime(0);
		response.setRemark("Test time log");
		response.setTotalTime(8);
		return response;
	}

	public static ApproverRequestResponseBodyDto createApproverResponse() {
		ApproverRequestResponseBodyDto response = new ApproverRequestResponseBodyDto();
		response.setAgencyIds(Arrays.asList(1, 2));
		response.setClientIds(Arrays.asList(3, 4));
		return response;
	}

	public static BulkPermissionCheckResult createSuccessfulBulkPermissionCheckResult() {
		return new BulkPermissionCheckResult();
	}

	public static TimelogWithSettingQueryResultDto createTimelogWithSettingQueryResult() {
		TimelogWithSettingQueryResultDto result = new TimelogWithSettingQueryResultDto();
		result.setTimesheetId(getDefaultTimesheetId());
		result.setTimesheetSettingId(getDefaultTimesheetSettingId());
		result.setCalculateBreakTime(true);
		result.setBreakTimeThreshold(30);
		return result;
	}

	public static TimelogWithSettingQueryResultDto createTimelogWithSettingQueryResultWithBreakTimeThreshold() {
		TimelogWithSettingQueryResultDto result = createTimelogWithSettingQueryResult();
		result.setCalculateBreakTime(false);
		result.setBreakTimeThreshold(30);
		return result;
	}

	public static TimelogQueryResultDto createTimelogQueryResult() {
		TimelogQueryResultDto result = new TimelogQueryResultDto();
		result.setTimesheetId(getDefaultTimesheetId());
		result.setTotalTime(40);
		result.setOverTime(0);
		return result;
	}

	public static TimelogResponseBodyDto createBulkTimeLogResponse() {
		TimelogResponseBodyDto response = new TimelogResponseBodyDto();
		response.setId(1);
		response.setTimesheetId(getDefaultTimesheetId());
		response.setTimesheetPeriod("2024-01-01 to 2024-01-07");
		response.setDate(1704067200);
		response.setDayTypeId(1);
		response.setWorkTime(8);
		response.setWorkStartTime(9);
		response.setWorkEndTime(17);
		response.setBreakTime(1);
		response.setOverTime(0);
		response.setRemark("Test time log");
		response.setTotalTime(8);
		return response;
	}

	public static TemplateWorkDay createTemplateWorkDay() {
		// Constructor: TemplateWorkDay(workDayId, workTimeValue, workStartTimeValue,
		// workEndTimeValue)
		return new TemplateWorkDay(1, 8, 540, 1020); // Monday, 8 hours, 9:00 AM - 17:00
														// PM
	}

	public static List<TemplateWorkDay> createTemplateWorkDayList() {
		TemplateWorkDay templateWorkDay = createTemplateWorkDay();
		return Arrays.asList(templateWorkDay);
	}

	public static ContractorTimesheetAndSettingValidatorResponseBodyDto createContractorTimesheetAndSettingValidatorResponse() {
		ContractorTimesheetAndSettingValidatorResponseBodyDto dto = new ContractorTimesheetAndSettingValidatorResponseBodyDto();
		dto.setTimesheetId(getDefaultTimesheetId());
		dto.setTimesheetSettingId(getDefaultTimesheetSettingId());
		return dto;
	}

	public static TimesheetAndSettingValidatorResponseBodyDto createTimesheetAndSettingValidatorResponse() {
		TimesheetAndSettingValidatorResponseBodyDto dto = new TimesheetAndSettingValidatorResponseBodyDto();
		dto.setTimesheetId(getDefaultTimesheetId());
		dto.setTimesheetSettingId(getDefaultTimesheetSettingId());
		return dto;
	}

	// ===== Contractor Timesheet DTOs =====

	public static PortalTimesheetRequestBodyDto createContractorTimesheetRequest() {
		PortalTimesheetRequestBodyDto request = new PortalTimesheetRequestBodyDto();
		request.setUserTypeId(1);
		return request;
	}

	public static PortalTimesheetResponseBodyDto createContractorTimesheetResponse() {
		PortalTimesheetResponseBodyDto response = new PortalTimesheetResponseBodyDto();
		response.setTimesheetId(getDefaultTimesheetId());
		response.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		response.setTimesheetFrequency(1);
		response.setCalculateBreakTime(true);
		response.setBreakTimeThreshold(30);
		response.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		response.setPayStatusId(1);
		response.setPayoutPaidOn(1704067200);
		response.setPayoutNumber("PAYOUT-001");
		response.setRemark("Test remark");
		response.setCreatedOn(1704067200);
		response.setPayCurrencySymbol("$");
		response.setApprovedBy("Test User");
		response.setApprovedByUserTypeId(2);
		response.setTimeLogs(Arrays.asList(createTimeLogResponse()));
		response.setTemplateWorkDays(createTemplateWorkDayList());
		response.setIsWeeklyEnabled(false);
		return response;
	}

	public static ResponseEntity<APINormalResponse<PortalTimesheetResponseBodyDto>> createContractorTimesheetSuccessResponse(
			PortalTimesheetResponseBodyDto data) {
		APINormalResponse<PortalTimesheetResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Message Constants (Inner Types Must Be Last) =====

	public static final class Messages {

		public static final String TIME_LOGS_FETCHED_SUCCESSFULLY = "Time logs fetched successfully";

		public static final String TIME_LOGS_BULK_UPDATED_SUCCESSFULLY = "Time logs bulk updated successfully";

		private Messages() {
			// Messages class - prevent instantiation
		}

	}

}