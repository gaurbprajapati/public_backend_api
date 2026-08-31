package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.entity.model.Deal;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DeleteTimesheetsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetWorkSummaryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPeriodResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobDurationQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ApproverResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprovalStatusTypeEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.workTimeEnum;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test data factory for Timesheet-related test objects.
 */
public final class TimesheetTestDataFactory {

	private TimesheetTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	public static CreateBulkTimesheetRequestBodyDto createBulkTimesheetRequest() {
		CreateBulkTimesheetRequestBodyDto request = new CreateBulkTimesheetRequestBodyDto();
		request.setContractorIds(Arrays.asList(1, 2, 3));
		request.setTimesheetDates(Arrays.asList(createTimesheetRequest(), createTimesheetRequest()));
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto createBulkTimesheetsForMultipleJobsRequest() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto();
		request.setJobContractorPairs(Arrays.asList(createJobContractorPair()));
		request.setTimesheetDates(Arrays.asList(createTimesheetRequest()));
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto createBulkTimesheetsForMultipleJobsRequestWithMultiplePairs() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto();
		request
			.setJobContractorPairs(Arrays.asList(createJobContractorPair(), createJobContractorPairWithDifferentJob()));
		request.setTimesheetDates(Arrays.asList(createTimesheetRequest()));
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto createJobContractorPair() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto pair = new io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto();
		pair.setJobId(getDefaultJobId());
		pair.setContractorIds(Arrays.asList(1, 2, 3));
		return pair;
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto createJobContractorPairWithDifferentJob() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto pair = new io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto();
		pair.setJobId(2);
		pair.setContractorIds(Arrays.asList(4, 5));
		return pair;
	}

	public static CreateTimesheetRequestBodyDto createTimesheetRequest() {
		CreateTimesheetRequestBodyDto request = new CreateTimesheetRequestBodyDto();
		request.setStartDate(getDefaultStartDate());
		request.setEndDate(getDefaultEndDate());
		return request;
	}

	public static DeleteTimesheetsRequestBodyDto createDeleteTimesheetsRequest() {
		DeleteTimesheetsRequestBodyDto request = new DeleteTimesheetsRequestBodyDto();
		request.setTimesheetIds(Arrays.asList(1, 2, 3));
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.portal.DeletePortalTimesheetsRequestBodyDto createDeletePortalTimesheetsRequest() {
		io.recruitcrm.microservice.timesheet.dto.portal.DeletePortalTimesheetsRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.portal.DeletePortalTimesheetsRequestBodyDto();
		request.setTimesheetId(getDefaultTimesheetId());
		request.setJobId(getDefaultJobId());
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto createEmptySlotRequest() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto();
		request.setContractorIds(Arrays.asList(1, 2, 3));
		request.setStartDate(1704067200); // 2024-01-01
		request.setEndDate(1706745600); // 2024-02-01
		request.setTimesheetFrequencyId(1); // Weekly
		request.setTimesheetStartDay(1); // Monday
		request.setJobId(getDefaultJobId());
		return request;
	}

	public static SearchRequestBodyDto createSearchRequest() {
		return new SearchRequestBodyDto();
	}

	public static PaginationRequestBodyDto createPaginationRequest() {
		return new PaginationRequestBodyDto(0, 20);
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto createTimesheetSearchRequest() {
		return new io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto();
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto createTimesheetSearchRequestWithFilters() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto();
		request.setAdvancedSearchContext("timesheet search");
		return request;
	}

	public static io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto createTimesheetSearchRequestWithSorting() {
		io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto request = new io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto();
		request.setSortPriorityList(java.util.Arrays.asList());
		return request;
	}

	// ===== Response DTOs =====

	public static TimesheetStatusHistoryResponseBodyDto createTimesheetStatusHistoryResponse() {
		TimesheetStatusHistoryResponseBodyDto response = new TimesheetStatusHistoryResponseBodyDto();
		response.setTimesheetId(getDefaultTimesheetId());
		response.setStatusHistory(Arrays.asList());
		return response;
	}

	public static io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto createTimeSlotsResult() {
		return new io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto(1704067200,
				1704672000);
	}

	public static java.util.List<io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto> createTimeSlotsResultList() {
		return Arrays.asList(createTimeSlotsResult(),
				new io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto(1705276800,
						1705881600));
	}

	public static TimesheetListResponseBodyDto createTimesheetListResponse() {
		TimesheetListResponseBodyDto dto = new TimesheetListResponseBodyDto();
		dto.setId(1);
		dto.setTimesheetPeriod(createTimesheetPeriodResponse());
		dto.setTimesheetStatusId(1);
		dto.setJobDuration(createJobDurationQueryResult());
		dto.setPayCurrencySymbol(getDefaultPayCurrencySymbol());
		dto.setBillCurrencySymbol(getDefaultBillCurrencySymbol());
		dto.setPayCurrencyCode(getDefaultPayCurrencyCode());
		dto.setBillCurrencyCode(getDefaultBillCurrencyCode());
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setPayData(200.0f);
		dto.setBillData(240.0f);
		dto.setApprovedBy(createApproverResultBody());
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setContractor(createContractorQueryResult());
		dto.setJob(createJobResultBody());
		dto.setPayStatusId(1);
		dto.setPayoutPaidOn(20240115);
		dto.setPayoutNumber("PAY-001");
		dto.setPayoutFile("payout.pdf");
		dto.setBillStatusId(1);
		dto.setInvoiceNumber("INV-001");
		dto.setInvoiceCreatedOn(20240115);
		dto.setAddedBy(createAddedByResponseBody());
		dto.setUpdatedBy(createUpdatedByResponseBody());
		dto.setTotalWorkTime(40L);
		dto.setTotalOvertime(8L);
		dto.setTotalTime(48L);
		dto.setSerialNumber("SN-001");
		dto.setCompanyName("Test Company");
		return dto;
	}

	public static List<TimesheetListResponseBodyDto> createTimesheetListResponseList() {
		return Arrays.asList(createTimesheetListResponse(), createTimesheetListResponse());
	}

	public static List<TimesheetListResponseBodyDto> createEmptyTimesheetListResponseList() {
		return Arrays.asList();
	}

	// Additional factory methods for TimesheetListResponseBodyDto components
	public static TimesheetPeriodResponseBodyDto createTimesheetPeriodResponse() {
		TimesheetPeriodResponseBodyDto dto = new TimesheetPeriodResponseBodyDto();
		dto.setTimesheetStartDate(20240101);
		dto.setTimesheetEndDate(20240107);
		return dto;
	}

	public static JobDurationQueryResultDto createJobDurationQueryResult() {
		JobDurationQueryResultDto dto = new JobDurationQueryResultDto();
		dto.setJobStartDate(20240101);
		dto.setJobEndDate(20240131);
		return dto;
	}

	public static ApproverResultBodyDto createApproverResultBody() {
		ApproverResultBodyDto dto = new ApproverResultBodyDto();
		dto.setId(1);
		dto.setName("Test Approver");
		dto.setPhoto("photo.jpg");
		dto.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		return dto;
	}

	public static ContractorQueryResultDto createContractorQueryResult() {
		ContractorQueryResultDto dto = new ContractorQueryResultDto();
		dto.setId(1);
		dto.setName("Test Contractor");
		dto.setPhoto("contractor.jpg");
		dto.setSlug("test-contractor");
		dto.setPosition("Developer");
		dto.setOwnerId("owner123");
		return dto;
	}

	public static JobResultBodyDto createJobResultBody() {
		JobResultBodyDto dto = new JobResultBodyDto();
		dto.setId(1);
		dto.setName("Test Job");
		dto.setSlug("test-job");
		return dto;
	}

	public static AddedByResponseBodyDto createAddedByResponseBody() {
		AddedByResponseBodyDto dto = new AddedByResponseBodyDto();
		dto.setId(1);
		dto.setName("Test User");
		dto.setPhoto("user.jpg");
		dto.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		return dto;
	}

	public static UpdatedByResponseBodyDto createUpdatedByResponseBody() {
		UpdatedByResponseBodyDto dto = new UpdatedByResponseBodyDto();
		dto.setId(1);
		dto.setName("Test User");
		dto.setPhoto("user.jpg");
		dto.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		return dto;
	}

	// ===== API Response Entities =====

	public static ResponseEntity<APINormalResponse<Void>> createVoidSuccessResponse() {
		APINormalResponse<Void> response = new APINormalResponse<>(null);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<java.util.List<io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto>>> createFreeSlotsSuccessResponse() {
		APINormalResponse<java.util.List<io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto>> response = new APINormalResponse<>(
				createTimeSlotsResultList());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<TimesheetStatusHistoryResponseBodyDto>> createTimesheetStatusHistorySuccessResponse(
			TimesheetStatusHistoryResponseBodyDto data) {
		APINormalResponse<TimesheetStatusHistoryResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<List<TimesheetListResponseBodyDto>>> createTimesheetListSuccessResponse(
			List<TimesheetListResponseBodyDto> data) {
		APINormalResponse<List<TimesheetListResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Creates a successful ResponseEntity for Long count.
	 * @param count The Long count value
	 * @return ResponseEntity with APINormalResponse containing count
	 */
	public static ResponseEntity<APINormalResponse<Long>> createLongCountSuccessResponse(Long count) {
		APINormalResponse<Long> response = new APINormalResponse<>(count);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Entity Objects =====
	// Note: Entity objects are created using mocks in test classes

	// ===== Test IDs and Constants =====

	public static Integer getDefaultJobId() {
		return 1;
	}

	public static String getDefaultPayCurrencySymbol() {
		return "$";
	}

	public static String getDefaultPayCurrencyCode() {
		return "USD";
	}

	public static String getDefaultBillCurrencySymbol() {
		return "$";
	}

	public static String getDefaultBillCurrencyCode() {
		return "USD";
	}

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultDealId() {
		return 1;
	}

	public static Integer getDefaultContractorId() {
		return 2;
	}

	public static Integer getDefaultStartDate() {
		return 1704067200; // 2024-01-01 00:00:00 UTC
	}

	public static Integer getDefaultEndDate() {
		return 1704153600; // 2024-01-02 00:00:00 UTC
	}

	public static Long getDefaultTimesheetCount() {
		return 10L;
	}

	public static Long getZeroTimesheetCount() {
		return 0L;
	}

	public static Long getLargeTimesheetCount() {
		return 1000L;
	}

	public static Long getSingleTimesheetCount() {
		return 1L;
	}

	public static Long getSmallTimesheetCount() {
		return 5L;
	}

	// ===== Additional Entity Methods =====

	public static AssignCandidateJob createAssignCandidateJob() {
		AssignCandidateJob assignment = new AssignCandidateJob();
		assignment.setJobId(getDefaultJobId());
		assignment.setCandidateId(getDefaultContractorId());
		assignment.setAccountId(1);
		return assignment;
	}

	public static List<AssignCandidateJob> createAssignCandidateJobList() {
		AssignCandidateJob assignment1 = createAssignCandidateJob();
		AssignCandidateJob assignment2 = createAssignCandidateJob();
		assignment2.setCandidateId(2);
		return Arrays.asList(assignment1, assignment2);
	}

	public static TimesheetSetting createTimesheetSetting() {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setId(1);
		setting.setJobStartDate(getDefaultStartDate());
		setting.setJobEndDate(getDefaultEndDate());
		setting.setAccountId(1);
		setting.setCreatedBy(1);
		setting.setCreatedOn(getDefaultStartDate());
		setting.setTimesheetFrequency(1); // Weekly
		setting.setTimesheetStartDay(1); // Monday
		// Set template work days - Monday to Friday (workDayId 1-5)
		List<TemplateWorkDay> templateWorkDays = Arrays.asList(createTemplateWorkDay(1), createTemplateWorkDay(2),
				createTemplateWorkDay(3), createTemplateWorkDay(4), createTemplateWorkDay(5));
		setting.setTemplateWorkDay(templateWorkDays);
		setting.setIsRemarkMandatory(0);
		return setting;
	}

	public static TemplateWorkDay createTemplateWorkDay(Integer workDayId) {
		TemplateWorkDay templateWorkDay = new TemplateWorkDay();
		templateWorkDay.setWorkDayId(workDayId);
		templateWorkDay.setWorkTime(8); // 8 hours
		return templateWorkDay;
	}

	public static List<TimesheetSetting> createTimesheetSettingList() {
		TimesheetSetting setting1 = createTimesheetSetting();
		TimesheetSetting setting2 = createTimesheetSetting();
		setting2.setId(2);
		return Arrays.asList(setting1, setting2);
	}

	public static Timesheet createTimesheet() {
		Timesheet timesheet = new Timesheet();
		timesheet.setId(getDefaultTimesheetId());
		timesheet.setPeriodStart(getDefaultStartDate());
		timesheet.setPeriodEnd(getDefaultEndDate());
		timesheet.setTimesheetSettingId(1);
		timesheet.setAccountId(1);
		timesheet.setAddedBy(1);
		timesheet.setAddedOn(getDefaultStartDate());
		return timesheet;
	}

	public static List<Timesheet> createTimesheetList() {
		Timesheet timesheet1 = createTimesheet();
		Timesheet timesheet2 = createTimesheet();
		timesheet2.setId(2);
		return Arrays.asList(timesheet1, timesheet2);
	}

	public static TimesheetApproval createTimesheetApproval() {
		TimesheetApproval approval = new TimesheetApproval();
		approval.setId(1);
		approval.setTimesheetId(getDefaultTimesheetId());
		approval.setTimesheetApprovalStatusTypeId(1);
		approval.setEntityId(1);
		approval.setCreatedOn(getDefaultStartDate());
		return approval;
	}

	public static TimesheetApproval createApprovedTimesheetApproval() {
		TimesheetApproval approval = createTimesheetApproval();
		approval.setTimesheetApprovalStatusTypeId(TimesheetApprovalStatusTypeEnum.APPROVED.getId()); // Approved
																										// status
																										// (4)
		return approval;
	}

	public static BulkPermissionCheckResult createBulkPermissionCheckResult() {
		return new BulkPermissionCheckResult();
	}

	public static Deal createDeal() {
		return new Deal();
	}

	public static Pageable createPageable() {
		return org.springframework.data.domain.PageRequest.of(0, 20);
	}

	// Additional methods for complex test scenarios
	public static List<StatusHistoryQueryResultDto> createStatusHistoryList() {
		StatusHistoryQueryResultDto dto = new StatusHistoryQueryResultDto();
		dto.setId(1);
		dto.setStatus(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		dto.setRemark("Approved by test approver");
		dto.setUpdatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setUpdatedOn(20240115);
		dto.setUpdatedById(1);
		return Arrays.asList(dto);
	}

	public static <K, V> java.util.Map<K, V> createEmptyMap() {
		return new java.util.HashMap<>();
	}

	public static Integer getDefaultAccountId() {
		return 1;
	}

	public static List<StatusHistoryResponseBodyDto> createStatusHistoryResponseList() {
		StatusHistoryResponseBodyDto dto = new StatusHistoryResponseBodyDto();
		dto.setId(1);
		dto.setStatus(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		dto.setRemark("Approved by test approver");
		dto.setUpdatedOn(20240115);
		UpdatedByResponseBodyDto updatedBy = new UpdatedByResponseBodyDto();
		updatedBy.setId(1);
		updatedBy.setName("Test User");
		updatedBy.setPhoto(null);
		updatedBy.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setUpdatedBy(updatedBy);
		return Arrays.asList(dto);
	}

	public static List<ContractorJobQueryResultDto> createContractorJobQueryResultList() {
		ContractorJobQueryResultDto dto = new ContractorJobQueryResultDto();
		dto.setContractorId(1);
		dto.setJobId(1);
		return Arrays.asList(dto);
	}

	public static List<TimesheetDealListQueryResultDto> createTimesheetDealListQueryResultList() {
		TimesheetDealListQueryResultDto dto = new TimesheetDealListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setContractorId(1);
		dto.setContractorName("Test Contractor");
		dto.setJobId(1);
		dto.setJobName("Test Job");
		dto.setJobSlug("test-job");
		dto.setInvoiceStatusId(1);
		// Contractor off-limit fields
		dto.setContractorOffLimitStatusId(20);
		dto.setContractorStatusLabel("Do Not Contact");
		dto.setContractorBackgroundColorHex("#00FF00");
		dto.setContractorTextColorHex("#000000");
		dto.setContractorOffLimitReason("Non-compete agreement");
		dto.setContractorMarkedByName("John Doe");
		dto.setContractorOffLimitStartDate(1717200000);
		dto.setContractorOffLimitEndDate(1719792000);
		return Arrays.asList(dto);
	}

	public static List<TimesheetJobAndContractorListQueryResultDto> createTimesheetJobAndContractorListQueryResultList() {
		TimesheetJobAndContractorListQueryResultDto dto = new TimesheetJobAndContractorListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setPayData(200.0f);
		dto.setBillData(240.0f);
		dto.setRateTypeId(1);
		dto.setPayStatusId(1);
		dto.setPayoutPaidOn(20240115);
		dto.setPayoutNumber("PAY-001");
		dto.setPayoutFile("payout.pdf");
		dto.setBillStatusId(1);
		dto.setInvoiceCreatedOn(20240115);
		dto.setInvoiceNumber("INV-001");
		dto.setInvoiceStatusId(1);
		dto.setSerialNumber("SN-001");
		return Arrays.asList(dto);
	}

	public static List<TimesheetWorkSummaryQueryResultDto> createTimesheetWorkSummaryList() {
		TimesheetWorkSummaryQueryResultDto dto = new TimesheetWorkSummaryQueryResultDto();
		dto.setTimesheetId(1);
		dto.setTotalWorkingHours(40L);
		dto.setTotalOverTimeHours(8L);
		dto.setTotalTime(48L);
		dto.setTotalPayData(200.0);
		dto.setTotalBillData(240.0);
		return Arrays.asList(dto);
	}

	public static List<TimesheetApproverResponseBodyDto> createTimesheetApproverResponseList() {
		TimesheetApproverResponseBodyDto dto = new TimesheetApproverResponseBodyDto();
		dto.setTimeSheetApprovalStatusId(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		dto.setEntityId(1);
		dto.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setTimesheetId(1);
		return Arrays.asList(dto);
	}

	public static List<TimeLog> createTimeLogList() {
		TimeLog timeLog = new TimeLog();
		timeLog.setId(1);
		timeLog.setTimesheetId(1);
		timeLog.setDate(getDefaultStartDate());
		timeLog.setWorkStartTime(getDefaultStartDate());
		timeLog.setWorkEndTime(getDefaultEndDate());
		timeLog.setWorkTime(8);
		timeLog.setOverTime(0);
		timeLog.setTotalTime(8);
		timeLog.setPayData(200.0f);
		timeLog.setBillData(240.0f);
		return Arrays.asList(timeLog);
	}

	public static List<TimesheetDealListQueryResultDto> createTimesheetDealListQueryResultWithAgencyRecruiterAddedByList() {
		TimesheetDealListQueryResultDto dto = new TimesheetDealListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setContractorId(1);
		dto.setContractorName("Test Contractor");
		dto.setJobId(1);
		dto.setJobName("Test Job");
		dto.setJobSlug("test-job");
		return Arrays.asList(dto);
	}

	public static List<TimesheetDealListQueryResultDto> createTimesheetDealListQueryResultWithContactAddedByList() {
		TimesheetDealListQueryResultDto dto = new TimesheetDealListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setContractorId(1);
		dto.setContractorName("Test Contractor");
		dto.setJobId(1);
		dto.setJobName("Test Job");
		dto.setJobSlug("test-job");
		return Arrays.asList(dto);
	}

	public static List<TimesheetDealListQueryResultDto> createTimesheetDealListQueryResultWithContractorAddedByList() {
		TimesheetDealListQueryResultDto dto = new TimesheetDealListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setContractorId(1);
		dto.setContractorName("Test Contractor");
		dto.setJobId(1);
		dto.setJobName("Test Job");
		dto.setJobSlug("test-job");
		return Arrays.asList(dto);
	}

	public static List<TimesheetJobAndContractorListQueryResultDto> createTimesheetJobAndContractorListQueryResultWithAgencyRecruiterAddedByList() {
		TimesheetJobAndContractorListQueryResultDto dto = new TimesheetJobAndContractorListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setPayData(200.0f);
		dto.setBillData(240.0f);
		dto.setRateTypeId(1);
		dto.setPayStatusId(1);
		dto.setPayoutPaidOn(20240115);
		dto.setPayoutNumber("PAY-001");
		dto.setPayoutFile("payout.pdf");
		dto.setBillStatusId(1);
		dto.setInvoiceCreatedOn(20240115);
		dto.setInvoiceNumber("INV-001");
		dto.setInvoiceStatusId(1);
		dto.setSerialNumber("SN-001");
		return Arrays.asList(dto);
	}

	public static List<TimesheetJobAndContractorListQueryResultDto> createTimesheetJobAndContractorListQueryResultWithContactAddedByList() {
		TimesheetJobAndContractorListQueryResultDto dto = new TimesheetJobAndContractorListQueryResultDto();
		dto.setId(1);
		dto.setAddedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		dto.setAddedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		dto.setUpdatedById(1);
		dto.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		dto.setTimesheetPeriodStartDate(20240101);
		dto.setTimesheetPeriodEndDate(20240107);
		dto.setAddedOn(20240101);
		dto.setUpdatedOn(20240101);
		dto.setJobDurationStartDate(20240101);
		dto.setJobDurationEndDate(20240131);
		dto.setPayRate(25.0f);
		dto.setBillRate(30.0f);
		dto.setPayData(200.0f);
		dto.setBillData(240.0f);
		dto.setRateTypeId(1);
		dto.setPayStatusId(1);
		dto.setPayoutPaidOn(20240115);
		dto.setPayoutNumber("PAY-001");
		dto.setPayoutFile("payout.pdf");
		dto.setBillStatusId(1);
		dto.setInvoiceCreatedOn(20240115);
		dto.setInvoiceNumber("INV-001");
		dto.setInvoiceStatusId(1);
		dto.setSerialNumber("SN-001");
		return Arrays.asList(dto);
	}

	public static List<TimesheetJobAndContractorListQueryResultDto> createTimesheetJobAndContractorListQueryResultWithContractorAddedByList() {
		// Reuse the base method to avoid code duplication
		// Both methods create the same data structure with contractor as the creator
		return createTimesheetJobAndContractorListQueryResultList();
	}

	public static List<TimesheetApproverResponseBodyDto> createApprovedTimesheetApproverResponseList() {
		TimesheetApproverResponseBodyDto dto = new TimesheetApproverResponseBodyDto();
		dto.setTimesheetId(1);
		dto.setEntityId(1);
		dto.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setTimeSheetApprovalStatusId(TimesheetApprovalStatusTypeEnum.APPROVED.getId());
		return Arrays.asList(dto);
	}

	public static List<io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice> createTimesheetInvoiceList() {
		io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice invoice = new io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice();
		invoice.setId(1);
		invoice.setTimesheetId(1);
		invoice.setInvoiceId(100);
		invoice.setAccountId(123);
		invoice.setUpdatedBy(1);
		invoice.setUpdatedOn(20240101);
		invoice.setUserTypeId(1);
		invoice.setPaymentStatusId(1);
		invoice.setBillingStatusId(1);
		return Arrays.asList(invoice);
	}

	/**
	 * Creates a TimesheetCountResponseBodyDto with default test data.
	 * @return TimesheetCountResponseBodyDto with totalCount=10, filteredCount=5
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto createTimesheetCountResponse() {
		return io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto.builder()
			.totalCount(10L)
			.filteredCount(5L)
			.build();
	}

	/**
	 * Creates a TimesheetCountResponseBodyDto with custom counts.
	 * @param totalCount The total count
	 * @param filteredCount The filtered count
	 * @return TimesheetCountResponseBodyDto with specified counts
	 */
	public static io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto createTimesheetCountResponse(
			Long totalCount, Long filteredCount) {
		return io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto.builder()
			.totalCount(totalCount)
			.filteredCount(filteredCount)
			.build();
	}

	/**
	 * Creates a successful ResponseEntity for timesheet count.
	 * @param count The TimesheetCountResponseBodyDto
	 * @return ResponseEntity with APINormalResponse containing count
	 */
	public static ResponseEntity<APINormalResponse<io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto>> createTimesheetCountSuccessResponse(
			io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto count) {
		APINormalResponse<io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto> response = new APINormalResponse<>(
				count);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Additional Factory Methods for Coverage Tests =====

	public static CreateBulkTimesheetRequestBodyDto createBulkTimesheetRequestWithEmptyContractors() {
		CreateBulkTimesheetRequestBodyDto request = new CreateBulkTimesheetRequestBodyDto();
		request.setContractorIds(Collections.emptyList());
		request.setTimesheetDates(Arrays.asList(createTimesheetRequest()));
		return request;
	}

	public static List<TimesheetDealListQueryResultDto> createTimesheetDealListQueryResultListWithNullUserType() {
		TimesheetDealListQueryResultDto dto = new TimesheetDealListQueryResultDto();
		dto.setId(getDefaultTimesheetId());
		dto.setAddedById(1);
		dto.setAddedByUserTypeId(null);
		dto.setUpdatedById(1);
		dto.setUpdatedByUserTypeId(null);
		return Arrays.asList(dto);
	}

	public static List<TimesheetJobAndContractorListQueryResultDto> createTimesheetJobAndContractorListQueryResultListWithNullContractor() {
		TimesheetJobAndContractorListQueryResultDto dto = new TimesheetJobAndContractorListQueryResultDto();
		dto.setId(getDefaultTimesheetId());
		dto.setContractorId(null);
		dto.setAddedById(1);
		dto.setAddedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		dto.setUpdatedById(1);
		dto.setUpdatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		return Arrays.asList(dto);
	}

	public static List<TimesheetListResponseBodyDto> createTimesheetListResponseBodyDtoListWithBillAndPayFields() {
		TimesheetListResponseBodyDto dto = new TimesheetListResponseBodyDto();
		dto.setId(getDefaultTimesheetId());
		dto.setBillRate(100.0f);
		dto.setPayRate(80.0f);
		dto.setPayData(200.0f);
		dto.setBillData(240.0f);
		dto.setPayStatusId(1);
		dto.setPayCurrencySymbol(getDefaultPayCurrencySymbol());
		dto.setPayCurrencyCode(getDefaultPayCurrencyCode());
		return Arrays.asList(dto);
	}

	// ===== Message Constants (Inner Types Must Be Last) =====

	public static final class Messages {

		public static final String TIMESHEETS_CREATED_SUCCESSFULLY = "Timesheets created successfully";

		public static final String TIMESHEET_DELETED_SUCCESSFULLY = "Timesheet deleted successfully";

		public static final String TIMESHEETS_DELETED_SUCCESSFULLY = "Timesheets deleted successfully";

		public static final String TIMESHEET_STATUS_HISTORY_FETCHED_SUCCESSFULLY = "Timesheet status history fetched successfully";

		public static final String TIMESHEETS_FETCHED_SUCCESSFULLY = "Timesheets fetched successfully";

		public static final String TIMESHEET_COUNT_FETCHED_SUCCESSFULLY = "Timesheet count fetched successfully";

		public static final String TIMESHEETS_CREATED_SUCCESSFULLY_FOR_MULTIPLE_JOBS = "Timesheets created successfully for multiple jobs";

		private Messages() {
			// Messages class - prevent instantiation
		}

	}

}
