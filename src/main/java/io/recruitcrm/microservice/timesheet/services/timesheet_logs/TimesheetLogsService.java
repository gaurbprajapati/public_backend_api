package io.recruitcrm.microservice.timesheet.services.timesheet_logs;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.workTimeEnum;
import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Invoice;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoicesJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_setting.TimesheetSettingJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.time_log.BatchOperationData;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto;
import io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeDetailSummaryDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.ReminderNotificationEventType;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationChannelsDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogTotalPayBillResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogWorkSummaryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.DayTimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimesheetJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchBulkContractorTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.TimelogsMetaDataDto;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ExternalServiceException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.TimesheetUpdateHelper;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.helpers.rule_engine.CustomRuleTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.candidate.CandidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.mapper.TimeLogMapper;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogBreakIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.services.invoice.ITimesheetInvoiceService;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.TimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.services.rule_engine.RuleEngineService;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookEvent;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookKafkaEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.recruitcrm.microservice.timesheet.helpers.GenericHelper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimesheetLogsService implements ITimesheetLogsService {

	private static final int MAX_INTERVALS_PER_TIME_LOG = 10;

	private static final int MAX_BREAKS_PER_INTERVAL = 5;

	final TimesheetJpaRepository timesheetJpaRepository;

	final TimesheetSettingJpaRepository timesheetSettingJpaRepository;

	final TimeLogMapper timeLogMapper;

	final TimesheetInvoiceRepository timesheetInvoiceRepository;

	final TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	final TimeLogJpaRepository timeLogJpaRepository;

	final TimesheetApprovalRepository timesheetApprovalRepository;

	final TimeLogRepository timeLogRepository;

	final UserRepository userRepository;

	final ContactRepository contactRepository;

	final TimesheetApproverRepository timesheetApproverRepository;

	final TimesheetRepository timesheetRepository;

	final TimeLogBreakIntervalRepository timeLogBreakIntervalRepository;

	final TimeLogIntervalRepository timeLogIntervalRepository;

	final AuthHolder auth;

	final TimesheetUpdateHelper timesheetUpdateHelper;

	final AccessControlChecker contractStaffingAccessControlChecker;

	final TimesheetSettingRepository timesheetSettingRepository;

	final CandidateRepository candidateRepository;

	private final RuleEngineService ruleEngineService;

	final ITimesheetInvoiceService timesheetInvoiceService;

	final InvoicesJpaRepository invoicesJpaRepository;

	final PortalAccessControlService portalAccessControlService;

	final WebhookKafkaEventService webhookKafkaEventService;

	private final ObjectMapper objectMapper;

	private final KafkaProducerHelper kafkaProducerHelper;

	@PersistenceContext
	private EntityManager entityManager;

	public TimesheetLogsService(TimesheetJpaRepository timesheetJpaRepository,
			TimesheetSettingJpaRepository timesheetSettingJpaRepository,
			TimesheetApprovalJpaRepository timesheetApprovalJpaRepository, TimeLogMapper timeLogMapper,
			TimesheetInvoiceRepository timesheetInvoiceRepository, TimeLogJpaRepository timeLogJpaRepository,
			TimesheetApprovalRepository timesheetApprovalRepository, TimeLogRepository timeLogRepository,
			UserRepository userRepository, ContactRepository contactRepository,
			TimesheetApproverRepository timesheetApproverRepository, TimesheetRepository timesheetRepository,
			AuthHolder auth, TimeLogBreakIntervalRepository timeLogBreakIntervalRepository,
			TimeLogIntervalRepository timeLogIntervalRepository,
			AccessControlChecker contractStaffingAccessControlChecker, TimesheetUpdateHelper timesheetUpdateHelper,
			TimesheetSettingRepository timesheetSettingRepository, CandidateRepository candidateRepository,
			RuleEngineService ruleEngineService, ITimesheetInvoiceService timesheetInvoiceService,
			InvoicesJpaRepository invoicesJpaRepository, PortalAccessControlService portalAccessControlService,
			WebhookKafkaEventService webhookKafkaEventService, ObjectMapper objectMapper,
			KafkaProducerHelper kafkaProducerHelper) {
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.timesheetSettingJpaRepository = timesheetSettingJpaRepository;
		this.timesheetApprovalJpaRepository = timesheetApprovalJpaRepository;
		this.timeLogMapper = timeLogMapper;
		this.timesheetInvoiceRepository = timesheetInvoiceRepository;
		this.timeLogJpaRepository = timeLogJpaRepository;
		this.timesheetApprovalRepository = timesheetApprovalRepository;
		this.timeLogRepository = timeLogRepository;
		this.userRepository = userRepository;
		this.contactRepository = contactRepository;
		this.timesheetApproverRepository = timesheetApproverRepository;
		this.timesheetRepository = timesheetRepository;
		this.auth = auth;
		this.timeLogBreakIntervalRepository = timeLogBreakIntervalRepository;
		this.timeLogIntervalRepository = timeLogIntervalRepository;
		this.timesheetUpdateHelper = timesheetUpdateHelper;
		this.contractStaffingAccessControlChecker = contractStaffingAccessControlChecker;
		this.timesheetSettingRepository = timesheetSettingRepository;
		this.candidateRepository = candidateRepository;
		this.ruleEngineService = ruleEngineService;
		this.timesheetInvoiceService = timesheetInvoiceService;
		this.invoicesJpaRepository = invoicesJpaRepository;
		this.portalAccessControlService = portalAccessControlService;
		this.webhookKafkaEventService = webhookKafkaEventService;
		this.objectMapper = objectMapper;
		this.kafkaProducerHelper = kafkaProducerHelper;
	}

	@Override
	@Transactional(readOnly = true)
	public TimesheetResponseBodyDto getTimeLogsByTimesheetId(Integer timesheetId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// Validate timesheet exists
		Timesheet timesheet = validateTimesheetExists(timesheetId, accountId);

		// Get timesheet log query result
		TimesheetLogQueryResultDto timesheetLogQueryResultDto = this.timeLogRepository
			.getTimeLogByTimesheetId(timesheetId);

		// Get time logs
		List<TimeLog> timeLogs = getTimeLogs(timesheetId);

		// Get work time details (for multiple time entries)
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap = getWorkTimeDetailsMap(
				timesheetLogQueryResultDto, timeLogs);

		// Get timesheet approval
		TimesheetApproval timesheetApproval = getTimesheetApproval(timesheetId);

		// Create base response with timesheet approver
		TimesheetResponseBodyDto responseDto = createBaseResponse(timesheet, timesheetApproval,
				timesheetLogQueryResultDto);

		// Map payment/billing fields from query result for all statuses
		setInvoiceFieldsFromQuery(responseDto, timesheetLogQueryResultDto);

		// Get approval status type ID
		Integer approvalStatusTypeId = timesheetApproval.getTimesheetApprovalStatusTypeId();

		// Set approver name if needed
		setApproverName(responseDto, timesheetLogQueryResultDto, approvalStatusTypeId);

		// Process approval status specific logic
		processApprovalStatusLogic(responseDto, timesheetApproval, timesheet, timesheetLogQueryResultDto);

		// Set time logs with proper filtering and mapping
		setFilteredTimeLogs(responseDto, timeLogs, timesheet, workTimeDetailsMap);

		responseDto.setTemplateWorkDays(timesheetLogQueryResultDto.getTemplateWorkDays());

		return responseDto;
	}

	private Timesheet validateTimesheetExists(Integer timesheetId, Integer accountId) {
		Optional<Timesheet> timesheet = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId);
		if (timesheet.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet", timesheetId);
		}
		return timesheet.get();
	}

	private List<TimeLog> getTimeLogs(Integer timesheetId) {
		List<TimeLog> timeLogs = this.timeLogJpaRepository.findByTimesheetId(timesheetId);
		if (timeLogs == null || timeLogs.isEmpty()) {
			throw new ResourceNotFoundException("Time logs for timesheet", timesheetId);
		}
		return timeLogs;
	}

	private Map<Integer, List<WorkTimeDetailResponseBodyDto>> getWorkTimeDetailsMap(
			TimesheetLogQueryResultDto timesheetLogQueryResultDto, List<TimeLog> timeLogs) {

		if (Objects.equals(timesheetLogQueryResultDto.getWorkLogType(), workTimeEnum.ENTER_START_END_TIME.getId())) {
			List<Integer> timeLogIds = timeLogs.stream().map(TimeLog::getId).toList();
			// Fetch TimeLogInterval records which contain break intervals as JSON
			List<TimeLogInterval> timeLogIntervals = this.timeLogIntervalRepository.findByTimeLogIdIn(timeLogIds);

			// Group TimeLogIntervals by timeLogId and create
			// WorkTimeDetailResponseBodyDto
			Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap = new HashMap<>();

			for (TimeLogInterval interval : timeLogIntervals) {
				Integer timeLogId = interval.getTimeLogId();

				// Create break intervals list for this interval
				List<BreakIntervalResponseBodyDto> breakIntervalDtos = new ArrayList<>();
				Integer rangeBasedBreakTime = 0;
				if (interval.getBreakInterval() != null && !interval.getBreakInterval().isEmpty()) {
					breakIntervalDtos = interval.getBreakInterval()
						.stream()
						.map((breakInterval) -> new BreakIntervalResponseBodyDto(breakInterval.getId(), timeLogId,
								breakInterval.getBreakStartTime(), breakInterval.getBreakEndTime()))
						.toList();

					// Calculate total break time for this interval
					rangeBasedBreakTime = interval.getBreakInterval()
						.stream()
						.mapToInt(
								(breakInterval) -> breakInterval.getBreakEndTime() - breakInterval.getBreakStartTime())
						.sum();
				}

				// Create WorkTimeDetailResponseBodyDto for this interval
				WorkTimeDetailResponseBodyDto workTimeDetail = new WorkTimeDetailResponseBodyDto(interval.getId(),
						interval.getWorkStartTime(), interval.getWorkEndTime(), interval.getRangeBasedRemark(),
						rangeBasedBreakTime, breakIntervalDtos);

				workTimeDetailsMap.computeIfAbsent(timeLogId, (k) -> new ArrayList<>()).add(workTimeDetail);
			}

			return workTimeDetailsMap;
		}
		return new HashMap<>();
	}

	private Map<Integer, List<WorkTimeDetailResponseBodyDto>> getWorkTimeDetailsMapForBulk(
			List<TimeLogInterval> timeLogIntervals, Map<Integer, Integer> timeLogIdToTimesheetIdMap,
			Map<Integer, Integer> timesheetIdToWorkLogTypeMap) {

		Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap = new HashMap<>();

		for (TimeLogInterval interval : timeLogIntervals) {
			Integer timeLogId = interval.getTimeLogId();
			Integer timesheetId = timeLogIdToTimesheetIdMap.get(timeLogId);
			Integer workLogType = (timesheetId != null) ? timesheetIdToWorkLogTypeMap.get(timesheetId) : null;
			if (timesheetId == null || !Objects.equals(workLogType, workTimeEnum.ENTER_START_END_TIME.getId())) {
				continue;
			}

			List<BreakIntervalResponseBodyDto> breakIntervalDtos = new ArrayList<>();
			Integer rangeBasedBreakTime = 0;
			if (interval.getBreakInterval() != null && !interval.getBreakInterval().isEmpty()) {
				breakIntervalDtos = interval.getBreakInterval()
					.stream()
					.map((breakInterval) -> new BreakIntervalResponseBodyDto(breakInterval.getId(), timeLogId,
							breakInterval.getBreakStartTime(), breakInterval.getBreakEndTime()))
					.toList();

				rangeBasedBreakTime = interval.getBreakInterval()
					.stream()
					.mapToInt((breakInterval) -> breakInterval.getBreakEndTime() - breakInterval.getBreakStartTime())
					.sum();
			}

			WorkTimeDetailResponseBodyDto workTimeDetail = new WorkTimeDetailResponseBodyDto(interval.getId(),
					interval.getWorkStartTime(), interval.getWorkEndTime(), interval.getRangeBasedRemark(),
					rangeBasedBreakTime, breakIntervalDtos);

			workTimeDetailsMap.computeIfAbsent(timeLogId, (k) -> new ArrayList<>()).add(workTimeDetail);
		}

		return workTimeDetailsMap;
	}

	private void populateWorkTimeDetailsOrBreakIntervals(List<TimelogResponseBodyDto> timelogResponseBodyDtos,
			Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap,
			Map<Integer, List<BreakIntervalResponseBodyDto>> breakIntervalsMap,
			Map<Integer, Integer> timeLogIdToTimesheetIdMap, Map<Integer, Integer> timesheetIdToWorkLogTypeMap) {
		for (TimelogResponseBodyDto timelogDto : timelogResponseBodyDtos) {
			Integer timesheetId = timeLogIdToTimesheetIdMap.get(timelogDto.getId());
			if (timesheetId == null) {
				continue;
			}
			Integer workLogType = timesheetIdToWorkLogTypeMap.get(timesheetId);
			if (Objects.equals(workLogType, workTimeEnum.ENTER_START_END_TIME.getId())) {
				List<WorkTimeDetailResponseBodyDto> workTimeDetails = workTimeDetailsMap.get(timelogDto.getId());
				if (workTimeDetails != null && !workTimeDetails.isEmpty()) {
					timelogDto.setWorkTimeDetails(workTimeDetails);
				}
			}
			else {
				List<BreakIntervalResponseBodyDto> breakIntervalDtos = breakIntervalsMap.get(timelogDto.getId());
				if (breakIntervalDtos != null && !breakIntervalDtos.isEmpty()) {
					timelogDto.setBreakIntervals(breakIntervalDtos);
				}
			}
		}
	}

	private TimesheetApproval getTimesheetApproval(Integer timesheetId) {
		TimesheetApproval timesheetApproval = this.timesheetApprovalJpaRepository
			.findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		if (timesheetApproval == null) {
			throw new ResourceNotFoundException("TimesheetApproval for timesheet", timesheetId);
		}
		return timesheetApproval;
	}

	private TimesheetResponseBodyDto createBaseResponse(Timesheet timesheet, TimesheetApproval timesheetApproval,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		TimesheetResponseBodyDto responseDto = new TimesheetResponseBodyDto();
		responseDto.setTimesheetId(timesheet.getId());
		responseDto.setApprovalStatusId(timesheetApproval.getTimesheetApprovalStatusTypeId());
		responseDto.setApprovedByUserTypeId(timesheetApproval.getUserTypeId());

		// Set timesheet setting details
		responseDto.setCalculateBreakTime(timesheetLogQueryResultDto.getCalculateBreakTime());
		responseDto.setBreakTimeThreshold(timesheetLogQueryResultDto.getBreakTimeThreshold());
		responseDto.setIsRemarkMandatory(timesheetLogQueryResultDto.getIsRemarkMandatory());
		responseDto.setWorkLogType(timesheetLogQueryResultDto.getWorkLogType());
		responseDto.setTimesheetFrequency(timesheetLogQueryResultDto.getTimesheetFrequency());
		responseDto.setTimesheetStartDay(timesheetLogQueryResultDto.getTimesheetStartDay());
		responseDto.setIsUnplannedHoursPayEnabled(timesheetLogQueryResultDto.getIsUnplannedHoursPayEnabled());

		// Get and set approvers
		List<TimesheetApprover> approvers = this.timesheetApproverRepository
			.findByTimesheetSettingId(timesheetLogQueryResultDto.getTimesheetSettingId());
		responseDto.setApprovers(this.timeLogMapper.mapApprovers(approvers));

		return responseDto;
	}

	private void setApproverName(TimesheetResponseBodyDto responseDto,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto, Integer approvalStatusTypeId) {

		if (timesheetLogQueryResultDto.getUserTypeId().equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto approverDetail = this.userRepository
				.getUserDetails(timesheetLogQueryResultDto.getEntityId());
			if (approverDetail != null) {
				responseDto.setApprovedBy(approverDetail.getName());
			}
		}
		else if (timesheetLogQueryResultDto.getUserTypeId().equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			Map<Integer, ContactNamePhotoQueryResultDto> contactMap = this.contactRepository
				.getContactNamePhotoMap(Set.of(timesheetLogQueryResultDto.getEntityId()));
			ContactNamePhotoQueryResultDto approverDetail = contactMap.get(timesheetLogQueryResultDto.getEntityId());
			if (approverDetail != null) {
				responseDto.setApprovedBy(approverDetail.getName());
			}
		}
		else if (timesheetLogQueryResultDto.getUserTypeId().equals(UserTypeEnum.CONTRACTOR.getId())) {
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorMap = this.candidateRepository
				.getContractorQueryResultMap(Set.of(timesheetLogQueryResultDto.getEntityId()));
			ContractorNamePhotoQueryResultDto approverDetail = contractorMap
				.get(timesheetLogQueryResultDto.getEntityId());
			if (approverDetail != null) {
				responseDto.setApprovedBy(approverDetail.getName());
			}
		}

		// Set approvedByUserId when approval status is SUBMITTED, REJECTED, or APPROVED
		// (2, 3, or 4)
		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.SUBMITTED.getId())
				|| Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.REJECTED.getId())
				|| Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.APPROVED.getId())) {
			responseDto.setApprovedByUserId(timesheetLogQueryResultDto.getEntityId());
		}
	}

	private void processApprovalStatusLogic(TimesheetResponseBodyDto responseDto, TimesheetApproval timesheetApproval,
			Timesheet timesheet, TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		Integer approvalStatusTypeId = timesheetApproval.getTimesheetApprovalStatusTypeId();

		// Always reflect whether a weekly overtime rule is configured, regardless of
		// approval status (open timesheets need this for the frontend to render
		// correctly)
		setWeeklyOvertimeEnabled(responseDto, timesheetLogQueryResultDto.getCustomRules());

		if (isSubmittedApprovedOrRejected(approvalStatusTypeId)) {
			setTimesheetTotalPayBill(responseDto, timesheet.getId(), timesheetLogQueryResultDto);
			setCurrencySymbols(responseDto, timesheetLogQueryResultDto);
			responseDto.setCreatedOn(timesheetApproval.getCreatedOn());
		}

		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.APPROVED.getId())) {
			setApprovedTimesheetDetails();
		}
		else {
			setNonApprovedDefaults();
		}

		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.REJECTED.getId())) {
			responseDto.setRemark(timesheetApproval.getRemark());
		}
	}

	private boolean isSubmittedApprovedOrRejected(Integer approvalStatusTypeId) {
		return Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.SUBMITTED.getId())
				|| Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.APPROVED.getId())
				|| Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.REJECTED.getId());
	}

	private void setTimesheetTotalPayBill(TimesheetResponseBodyDto responseDto, Integer timesheetId,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		List<Integer> timesheetIds = List.of(timesheetId);
		List<TimeLogWorkSummaryQueryResultDto> workSummaryResults = getWorkSummaryResults(timesheetIds,
				timesheetLogQueryResultDto.getWorkLogType());

		if (!workSummaryResults.isEmpty()) {
			TimeLogWorkSummaryQueryResultDto workSummary = workSummaryResults.getFirst();
			TimeLogTotalPayBillResponseBodyDto totalPayBill = createTotalPayBillDto(workSummary);
			responseDto.setTimesheetTotalPayBill(totalPayBill);
		}
	}

	private List<TimeLogWorkSummaryQueryResultDto> getWorkSummaryResults(List<Integer> timesheetIds,
			Integer workLogType) {

		if (workLogType != null && Objects.equals(workLogType, workTimeEnum.ENTER_START_END_TIME.getId())) {
			return this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(timesheetIds);
		}
		return this.timeLogJpaRepository.getTimeLogWorkSummaries(timesheetIds);
	}

	private TimeLogTotalPayBillResponseBodyDto createTotalPayBillDto(TimeLogWorkSummaryQueryResultDto workSummary) {
		TimeLogTotalPayBillResponseBodyDto totalPayBill = new TimeLogTotalPayBillResponseBodyDto();
		totalPayBill.setTotalWorkTime(safeConvertToInt(workSummary.getTotalWorkTime()));
		totalPayBill.setTotalBreakTime(safeConvertToInt(workSummary.getTotalBreakTime()));
		totalPayBill.setTotalOvertime(safeConvertToInt(workSummary.getTotalOvertime()));
		totalPayBill.setTotalTime(safeConvertToInt(workSummary.getTotalTime()));
		return totalPayBill;
	}

	private Integer safeConvertToInt(Long value) {
		return (value != null) ? value.intValue() : 0;
	}

	private void setCurrencySymbols(TimesheetResponseBodyDto responseDto,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {
		responseDto.setPayCurrencySymbol(timesheetLogQueryResultDto.getPayCurrencySymbol());
		responseDto.setPayCurrencyCode(timesheetLogQueryResultDto.getPayCurrencyCode());
		responseDto.setBillCurrencySymbol(timesheetLogQueryResultDto.getBillCurrencySymbol());
		responseDto.setBillCurrencyCode(timesheetLogQueryResultDto.getBillCurrencyCode());
	}

	private void setApprovedTimesheetDetails() {
		// Approved timesheet details - fields removed from DTO
	}

	private void setInvoiceFieldsFromQuery(TimesheetResponseBodyDto responseDto,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {
		responseDto.setPayStatusId(timesheetLogQueryResultDto.getPaymentStatusId());
		responseDto.setPayoutPaidOn(timesheetLogQueryResultDto.getPaymentPaidOn());
		responseDto.setBillStatusId(timesheetLogQueryResultDto.getBillingStatusId());
		responseDto.setInvoiceCreatedOn(timesheetLogQueryResultDto.getBillingDate());
		responseDto.setPayoutNumber(timesheetLogQueryResultDto.getPayoutNumber());
		responseDto.setInvoiceNumber(timesheetLogQueryResultDto.getInvoiceNumber());
		if (timesheetLogQueryResultDto.getInvoiceStatus() != null) {
			responseDto.setInvoiceStatusId(timesheetLogQueryResultDto.getInvoiceStatus().getId());
		}
		else {
			responseDto.setInvoiceStatusId(null);
		}
	}

	private boolean hasWeeklyOvertimeRule(List<CustomRule> customRules) {
		if (customRules != null && !customRules.isEmpty()) {
			for (CustomRule customRule : customRules) {
				if (isWeeklyOvertimeRule(customRule)) {
					return true;
				}
			}
		}
		return false;
	}

	private void setWeeklyOvertimeEnabled(TimesheetResponseBodyDto responseDto, List<CustomRule> customRules) {
		responseDto.setIsWeeklyEnabled(hasWeeklyOvertimeRule(customRules));
	}

	private boolean isWeeklyOvertimeRule(CustomRule customRule) {
		return customRule != null && (customRule.getRuleType()
			.equals(CustomRuleTypeEnum.RANGE_BASED_WEEKLY_OVERTIME.getValue())
				|| customRule.getRuleType().equals(CustomRuleTypeEnum.DURATION_BASED_WEEKLY_OVERTIME.getValue()));
	}

	private void setNonApprovedDefaults() {
		// Weekly pay/bill data is now set for all states (submitted, approved, rejected)
		// in the processApprovalStatusLogic method, so no need to reset it here
	}

	private void setFilteredTimeLogs(TimesheetResponseBodyDto responseDto, List<TimeLog> timeLogs, Timesheet timesheet,
			Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap) {

		String formattedPeriod = formatTimesheetPeriod(timesheet.getPeriodStart(), timesheet.getPeriodEnd());

		List<TimeLogResponseBodyDto> mappedTimeLogs = timeLogs.stream()
			.map((log) -> mapTimeLogWithBreakIntervals(log, formattedPeriod, workTimeDetailsMap))
			.toList();

		responseDto.setTimeLogs(mappedTimeLogs);
	}

	private TimeLogResponseBodyDto mapTimeLogWithBreakIntervals(TimeLog log, String formattedPeriod,
			Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap) {

		TimeLogResponseBodyDto dto = this.timeLogMapper.toDto(log);
		dto.setTimesheetPeriod(formattedPeriod);
		setWorkTimeDetails(dto, log.getId(), workTimeDetailsMap);
		return dto;
	}

	private void setWorkTimeDetails(TimeLogResponseBodyDto dto, Integer timeLogId,
			Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap) {
		if (workTimeDetailsMap.containsKey(timeLogId)) {
			List<WorkTimeDetailResponseBodyDto> workTimeDetails = workTimeDetailsMap.get(timeLogId);
			if (workTimeDetails != null && !workTimeDetails.isEmpty()) {
				dto.setWorkTimeDetails(workTimeDetails);
				dto.setWorkHoursDisplay(this.formatWorkTimeDetailsAsCommaSeparated(workTimeDetails));
			}
		}
	}

	/**
	 * Format work time details as comma-separated "HH:MM-HH:MM" (e.g. "07:00-19:00,
	 * 20:00-21:00").
	 */
	private String formatWorkTimeDetailsAsCommaSeparated(List<WorkTimeDetailResponseBodyDto> workTimeDetails) {
		if (workTimeDetails == null || workTimeDetails.isEmpty()) {
			return null;
		}
		return workTimeDetails.stream()
			.filter((d) -> d != null && d.getWorkStartTime() != null && d.getWorkEndTime() != null
					&& d.getWorkStartTime() >= 0 && d.getWorkEndTime() >= 0)
			.map((d) -> this.formatSecondsToTime(d.getWorkStartTime()) + "-"
					+ this.formatSecondsToTime(d.getWorkEndTime()))
			.filter((s) -> !s.endsWith("-"))
			.collect(Collectors.joining(", "));
	}

	private String formatSecondsToTime(Integer seconds) {
		if (seconds == null || seconds < 0) {
			return "";
		}
		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		return String.format("%02d:%02d", hours, minutes);
	}

	@Override
	@WriterRoute
	@Transactional(rollbackFor = Exception.class)
	public void bulkUpdateTimeLogs(BulkUpdateTimeLogsRequestBodyDto requestDto) {

		// No-op when nothing to process: no time log changes and no metadata-only
		// timesheets
		if (isNullOrEmpty(requestDto.getTimeLogs()) && isNullOrEmpty(requestDto.getTimesheetIdNoLogChanges())) {
			return;
		}

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String performerDisplayName = principal.getFullName();

		switch (principal.getPrincipalType()) {
			case USER -> this.bulkUpdateTimeLogsForUser(requestDto, performerDisplayName);
			case CONTRACTOR -> this.bulkUpdateTimeLogsForContractor(requestDto, principal, performerDisplayName);
			case CONTACT -> this.bulkUpdateTimeLogsForContact(requestDto, principal, performerDisplayName);
			default -> throw new io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException(
					"Unknown persona type");
		}
	}

	/**
	 * Bulk update time logs for USER persona (agency users) Performs role-based access
	 * control
	 */
	private void bulkUpdateTimeLogsForUser(BulkUpdateTimeLogsRequestBodyDto requestDto, String performerDisplayName) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		List<Integer> timesheetIdsFromLogs = getTimesheetIdsFromLogs(requestDto);
		List<Integer> timesheetIdsNoLogChanges = getTimesheetIdNoLogChanges(requestDto);
		List<Integer> allTimesheetIds = unionDistinct(timesheetIdsFromLogs, timesheetIdsNoLogChanges);

		if (Boolean.TRUE.equals(requestDto.getSave())) {
			allTimesheetIds = this.filterToOpenTimesheetIds(allTimesheetIds);
			Set<Integer> openSet = new HashSet<>(allTimesheetIds);
			timesheetIdsFromLogs = timesheetIdsFromLogs.stream().filter(openSet::contains).toList();
			timesheetIdsNoLogChanges = timesheetIdsNoLogChanges.stream().filter(openSet::contains).toList();
		}

		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = allTimesheetIds.stream().map((timesheetId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.ADD_TIME_IN_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);
			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);
			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();
		this.contractStaffingAccessControlChecker.allowsBulk(bulkRequest);

		if (!timesheetIdsFromLogs.isEmpty()) {
			performBulkTimeLogUpdate(requestDto, timesheetIdsFromLogs, accountId, userId, userTypeId);
		}
		if (!timesheetIdsNoLogChanges.isEmpty()) {
			performTimesheetMetadataOnlyUpdate(requestDto, timesheetIdsNoLogChanges, userId, userTypeId);
		}
		this.triggerBulkTimesheetNotifications(requestDto, timesheetIdsFromLogs, timesheetIdsNoLogChanges, accountId,
				userTypeId, performerDisplayName);
	}

	/**
	 * Bulk update time logs for CONTRACTOR persona Validates contractor owns the
	 * timesheets. Contractors cannot approve timesheets, so isApproved is always set to
	 * false regardless of request value.
	 */
	private void bulkUpdateTimeLogsForContractor(BulkUpdateTimeLogsRequestBodyDto requestDto, AuthPrincipal principal,
			String performerDisplayName) {

		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
		Integer contractorId = contractorPrincipal.getCandidateId();
		Integer accountId = contractorPrincipal.getOrganizationIdentifier();
		Integer userId = contractorId;
		Integer userTypeId = UserTypeEnum.CONTRACTOR.getId();

		requestDto.setIsApproved(false);

		List<Integer> timesheetIdsFromLogs = getTimesheetIdsFromLogs(requestDto);
		List<Integer> timesheetIdsNoLogChanges = getTimesheetIdNoLogChanges(requestDto);
		List<Integer> allTimesheetIds = unionDistinct(timesheetIdsFromLogs, timesheetIdsNoLogChanges);

		if (Boolean.TRUE.equals(requestDto.getSave())) {
			allTimesheetIds = this.filterToOpenTimesheetIds(allTimesheetIds);
			Set<Integer> openSet = new HashSet<>(allTimesheetIds);
			timesheetIdsFromLogs = timesheetIdsFromLogs.stream().filter(openSet::contains).toList();
			timesheetIdsNoLogChanges = timesheetIdsNoLogChanges.stream().filter(openSet::contains).toList();
		}

		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = allTimesheetIds.stream().map((timesheetId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.ADD_TIME_IN_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);
			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);
			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();
		this.contractStaffingAccessControlChecker.allowsBulk(bulkRequest);

		if (!timesheetIdsFromLogs.isEmpty()) {
			performBulkTimeLogUpdate(requestDto, timesheetIdsFromLogs, accountId, userId, userTypeId);
		}
		if (!timesheetIdsNoLogChanges.isEmpty()) {
			performTimesheetMetadataOnlyUpdate(requestDto, timesheetIdsNoLogChanges, userId, userTypeId);
		}
		this.triggerBulkTimesheetNotifications(requestDto, timesheetIdsFromLogs, timesheetIdsNoLogChanges, accountId,
				userTypeId, performerDisplayName);
	}

	/**
	 * Bulk update time logs for CONTACT persona Validates portal access control: 1.
	 * Validates job exists and portal is enabled 2. Validates clientId matches job's
	 * primary contactId or secondary contact 3. Validates JobTimesheetAccess record
	 * exists and checks EDIT_TIMESHEET permission
	 */
	private void bulkUpdateTimeLogsForContact(BulkUpdateTimeLogsRequestBodyDto requestDto, AuthPrincipal principal,
			String performerDisplayName) {

		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer clientId = contactPrincipal.getContactId();
		Integer jobId = requestDto.getJobId();
		Integer accountId = contactPrincipal.getOrganizationIdentifier();
		Integer userId = contactPrincipal.getContactId();
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		PortalTimesheetPermissionDto permissions = this.portalAccessControlService.validatePortalAccessControl(jobId,
				clientId);
		if (permissions.getCanEdit() == null || permissions.getCanEdit() != 1) {
			throw new UnauthorizedAccessException("Unauthorized access for edit timesheet");
		}

		List<Integer> timesheetIdsFromLogs = getTimesheetIdsFromLogs(requestDto);
		List<Integer> timesheetIdsNoLogChanges = getTimesheetIdNoLogChanges(requestDto);

		if (Boolean.TRUE.equals(requestDto.getSave())) {
			List<Integer> allTimesheetIds = unionDistinct(timesheetIdsFromLogs, timesheetIdsNoLogChanges);
			allTimesheetIds = this.filterToOpenTimesheetIds(allTimesheetIds);
			Set<Integer> openSet = new HashSet<>(allTimesheetIds);
			timesheetIdsFromLogs = timesheetIdsFromLogs.stream().filter(openSet::contains).toList();
			timesheetIdsNoLogChanges = timesheetIdsNoLogChanges.stream().filter(openSet::contains).toList();
		}

		if (!timesheetIdsFromLogs.isEmpty()) {
			performBulkTimeLogUpdate(requestDto, timesheetIdsFromLogs, accountId, userId, userTypeId);
		}
		if (!timesheetIdsNoLogChanges.isEmpty()) {
			performTimesheetMetadataOnlyUpdate(requestDto, timesheetIdsNoLogChanges, userId, userTypeId);
		}
		this.triggerBulkTimesheetNotifications(requestDto, timesheetIdsFromLogs, timesheetIdsNoLogChanges, accountId,
				userTypeId, performerDisplayName);
	}

	private static boolean isNullOrEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	private List<Integer> getTimesheetIdsFromLogs(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		if (requestDto == null || isNullOrEmpty(requestDto.getTimeLogs())) {
			return new ArrayList<>();
		}
		return extractTimesheetIds(requestDto);
	}

	private List<Integer> getTimesheetIdNoLogChanges(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		if (requestDto == null || isNullOrEmpty(requestDto.getTimesheetIdNoLogChanges())) {
			return new ArrayList<>();
		}
		return requestDto.getTimesheetIdNoLogChanges().stream().filter(Objects::nonNull).distinct().toList();
	}

	private List<Integer> unionDistinct(List<Integer> a, List<Integer> b) {
		Set<Integer> set = new LinkedHashSet<>(a);
		set.addAll(b);
		return new ArrayList<>(set);
	}

	private List<Integer> extractTimesheetIds(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		if (requestDto == null) {
			throw new IllegalArgumentException("Request DTO cannot be null");
		}
		if (requestDto.getTimeLogs() == null || requestDto.getTimeLogs().isEmpty()) {
			throw new IllegalArgumentException("Time logs list cannot be null or empty");
		}

		List<Integer> timesheetIds = new ArrayList<>();
		for (BulkTimeLogRequestBodyDto timeLog : requestDto.getTimeLogs()) {
			if (timeLog == null) {
				continue; // Skip null entries
			}
			Integer timesheetId = timeLog.getTimesheetId();
			if (timesheetId != null && !timesheetIds.contains(timesheetId)) {
				timesheetIds.add(timesheetId);
			}
		}

		if (timesheetIds.isEmpty()) {
			throw new IllegalArgumentException("No valid timesheet IDs found in request");
		}

		return timesheetIds;
	}

	/**
	 * Filters the given timesheet IDs to only those in open state. A timesheet is
	 * considered open when its latest approval has status OPEN. Uses a single batch query
	 * instead of N individual queries.
	 * @param timesheetIds list of timesheet IDs to filter
	 * @return list containing only timesheet IDs that are in open state; never null
	 */
	private List<Integer> filterToOpenTimesheetIds(List<Integer> timesheetIds) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return new ArrayList<>();
		}
		List<TimesheetApproval> latestApprovals = this.timesheetApprovalRepository
			.findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		Set<Integer> openIds = latestApprovals.stream()
			.filter((approval) -> Objects.equals(approval.getTimesheetApprovalStatusTypeId(),
					ApprovalStatusEnum.OPEN.getId()))
			.map(TimesheetApproval::getTimesheetId)
			.collect(Collectors.toSet());
		return timesheetIds.stream().filter(openIds::contains).toList();
	}

	/**
	 * Metadata-only update for timesheets where no logs were changed. Updates status
	 * history, last modified (updated_on/updated_by), and optionally invokes rule engine
	 * on submit.
	 */
	private void performTimesheetMetadataOnlyUpdate(BulkUpdateTimeLogsRequestBodyDto requestDto,
			List<Integer> timesheetIdsNoLogChanges, Integer userId, Integer userTypeId) {

		if (!Boolean.TRUE.equals(requestDto.getSave())) {
			validateTimesheetsNotApproved(timesheetIdsNoLogChanges, requestDto.getSave());
		}

		for (Integer timesheetId : timesheetIdsNoLogChanges) {
			if (timesheetId == null) {
				continue;
			}
			updateTimesheetApprovalStatus(timesheetId, requestDto.getIsApproved(), userId, userTypeId,
					requestDto.getSave());
			updateTimesheetLastModified(timesheetId, userId, userTypeId);
		}

		updateTimesheetTimeDetailsFromRequest(requestDto);
		invokeRuleEngineIfNeeded(requestDto, timesheetIdsNoLogChanges);

		if (Boolean.TRUE.equals(requestDto.getIsApproved())) {
			GenericHelper.runAfterCommitOrNow(() -> this.webhookKafkaEventService
				.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, timesheetIdsNoLogChanges));
		}
	}

	/**
	 * OPTIMIZED VERSION: Batch operations for bulk time log updates
	 *
	 * Handles: - Time log interval creation (id = null) - Time log interval updation (id
	 * != null) - Mixed payloads (some null IDs, some non-null IDs) - Interval deletions
	 * (intervals not in payload are deleted) - Batching of 1000 records (handled by
	 * repository)
	 *
	 * Performance improvement: - Before: ~5N queries (N for fetch, N for settings, N for
	 * updates, 2N for intervals) - After: ~3-5 queries total (batch fetch TimeLogs, batch
	 * fetch Settings, batch update TimeLogs, batch upsert Intervals)
	 *
	 * For 100 time logs: ~500 queries → ~3-5 queries (100x+ reduction)
	 *
	 * Common bulk time log update logic Called after persona-specific access control All
	 * database operations are within a single transaction - if any fails, entire
	 * transaction rolls back
	 */
	private void performBulkTimeLogUpdate(BulkUpdateTimeLogsRequestBodyDto requestDto, List<Integer> timesheetIds,
			Integer accountId, Integer userId, Integer userTypeId) {

		if (!Boolean.TRUE.equals(requestDto.getSave())) {
			validateTimesheetsNotApproved(timesheetIds, requestDto.getSave());
		}

		// Step 1: Validate and fetch time logs
		Map<Integer, TimeLog> timeLogMap = validateAndFetchTimeLogs(requestDto);

		// Step 2: Fetch timesheet settings
		Map<Integer, TimesheetSetting> timesheetSettingMap = fetchTimesheetSettings(timesheetIds, accountId);

		// Step 3: Validate time log intervals (count limit and overlap detection)
		validateTimeLogIntervalConflicts(requestDto, timesheetSettingMap);

		// Step 4: Group time logs by timesheet ID (filter out null timesheet IDs)
		Map<Integer, List<BulkTimeLogRequestBodyDto>> timeLogsByTimesheetId = requestDto.getTimeLogs()
			.stream()
			.filter((logDto) -> logDto != null && logDto.getTimesheetId() != null)
			.collect(Collectors.groupingBy(BulkTimeLogRequestBodyDto::getTimesheetId));

		// Step 5: Prepare batch operation data
		BatchOperationData batchData = prepareBatchOperationData(requestDto, timeLogMap, timesheetSettingMap);

		// Step 6: Execute database operations in transaction
		executeBatchDatabaseOperations(batchData, requestDto, timeLogsByTimesheetId, userId, userTypeId);

		// Clear persistence context so rule engine loads fresh TimeLogs from DB.
		// Native SQL batch upsert does not update managed entities; without this, the
		// rule
		// engine sees stale workTime (null) for duration-based logs and skips them.
		this.entityManager.clear();

		// Step 7: Invoke rule engine (outside transaction scope)
		invokeRuleEngineIfNeeded(requestDto, timesheetIds);

		if (Boolean.TRUE.equals(requestDto.getIsApproved())) {
			GenericHelper.runAfterCommitOrNow(() -> this.webhookKafkaEventService
				.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, timesheetIds));
		}
	}

	/**
	 * After bulk log and/or metadata-only updates: one merged
	 * {@link WebhookEvent#TIMESHEET_APPROVED} plus one approved reminder when
	 * {@code isApproved}, otherwise one merged submitted reminder when not a draft save.
	 * Branches are mutually exclusive.
	 */
	private void triggerBulkTimesheetNotifications(final BulkUpdateTimeLogsRequestBodyDto requestDto,
			final List<Integer> timesheetIdsFromLogs, final List<Integer> timesheetIdsNoLogChanges,
			final Integer accountId, final Integer userTypeId, final String performerDisplayName) {
		final List<Integer> mergedIds = this.unionDistinct(
				(timesheetIdsFromLogs != null) ? timesheetIdsFromLogs : Collections.emptyList(),
				(timesheetIdsNoLogChanges != null) ? timesheetIdsNoLogChanges : Collections.emptyList());
		if (mergedIds.isEmpty()) {
			return;
		}
		if (Boolean.TRUE.equals(requestDto.getIsApproved())) {
			GenericHelper.runAfterCommitOrNow(() -> this.publishTimesheetNotification(mergedIds, accountId, userTypeId,
					TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_APPROVED, performerDisplayName,
					TimesheetReminderNotificationChannelsDto.APPROVED));
			return;
		}
		if (!Boolean.TRUE.equals(requestDto.getSave())) {
			GenericHelper.runAfterCommitOrNow(() -> this.publishTimesheetNotification(mergedIds, accountId, userTypeId,
					TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_SUBMITTED, performerDisplayName,
					TimesheetReminderNotificationChannelsDto.SUBMITTED));
		}
	}

	/**
	 * Validates and fetches time logs in batch
	 * @param requestDto Request DTO containing time log data
	 * @return Map of time log ID to TimeLog entity
	 */
	private Map<Integer, TimeLog> validateAndFetchTimeLogs(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		if (requestDto == null || requestDto.getTimeLogs() == null || requestDto.getTimeLogs().isEmpty()) {
			throw new IllegalArgumentException("Request DTO or time logs list cannot be null or empty");
		}

		// Extract all time log IDs from request
		List<Integer> timeLogIds = requestDto.getTimeLogs()
			.stream()
			.filter((logDto) -> logDto != null && logDto.getId() != null)
			.map(BulkTimeLogRequestBodyDto::getId)
			.toList();

		if (timeLogIds.isEmpty()) {
			throw new IllegalArgumentException("No valid time log IDs found in request");
		}

		// Batch fetch all TimeLogs upfront (1 query instead of N)
		Map<Integer, TimeLog> timeLogMap = this.timeLogJpaRepository.findAllById(timeLogIds)
			.stream()
			.filter((timeLog) -> timeLog != null && timeLog.getId() != null)
			.collect(Collectors.toMap(TimeLog::getId, (timeLog) -> timeLog));

		// Validate all time logs exist and timesheet IDs match
		for (BulkTimeLogRequestBodyDto logDto : requestDto.getTimeLogs()) {
			if (logDto == null) {
				throw new ValidationErrorException("Time log entry cannot be null");
			}
			if (logDto.getId() == null) {
				throw new ValidationErrorException("Time log ID cannot be null");
			}
			if (logDto.getTimesheetId() == null) {
				throw new ValidationErrorException("Timesheet ID cannot be null for time log ID: " + logDto.getId());
			}

			if (!timeLogMap.containsKey(logDto.getId())) {
				throw new ResourceNotFoundException("TimeLog", logDto.getId());
			}

			TimeLog timeLog = timeLogMap.get(logDto.getId());
			if (timeLog == null) {
				throw new ResourceNotFoundException("TimeLog", logDto.getId());
			}

			if (!logDto.getTimesheetId().equals(timeLog.getTimesheetId())) {
				throw new IllegalArgumentException("Timesheet ID mismatch for time log ID: " + logDto.getId());
			}
		}

		return timeLogMap;
	}

	/**
	 * Validates that incoming time log intervals do not violate the per-time-log limit,
	 * do not overlap with already-persisted intervals, respect the per-interval break
	 * limit, and satisfy mandatory-remark requirements. This prevents concurrent
	 * submissions from silently creating conflicting time ranges.
	 */
	private void validateTimeLogIntervalConflicts(BulkUpdateTimeLogsRequestBodyDto requestDto,
			Map<Integer, TimesheetSetting> timesheetSettingMap) {

		Map<Integer, List<WorkTimeDetailDto>> workDetailsByTimeLogId = collectRangeBasedWorkDetails(requestDto,
				timesheetSettingMap);

		if (workDetailsByTimeLogId.isEmpty()) {
			return;
		}

		List<Integer> timeLogIds = new ArrayList<>(workDetailsByTimeLogId.keySet());
		Map<Integer, List<TimeLogIntervalDto>> existingByTimeLogId = this.timeLogIntervalRepository
			.findIntervalsByTimeLogIds(timeLogIds);

		Set<Integer> remarkRequiredTimeLogIds = resolveRemarkRequiredTimeLogIds(requestDto, timesheetSettingMap);

		for (Map.Entry<Integer, List<WorkTimeDetailDto>> entry : workDetailsByTimeLogId.entrySet()) {
			validateSingleTimeLogIntervals(entry.getKey(), entry.getValue(),
					existingByTimeLogId.getOrDefault(entry.getKey(), Collections.emptyList()),
					remarkRequiredTimeLogIds.contains(entry.getKey()));
		}
	}

	private Set<Integer> resolveRemarkRequiredTimeLogIds(BulkUpdateTimeLogsRequestBodyDto requestDto,
			Map<Integer, TimesheetSetting> timesheetSettingMap) {

		if (Boolean.TRUE.equals(requestDto.getSave())) {
			return Collections.emptySet();
		}

		return requestDto.getTimeLogs()
			.stream()
			.filter((logDto) -> logDto != null && logDto.getId() != null && logDto.getTimesheetId() != null)
			.filter((logDto) -> {
				TimesheetSetting setting = timesheetSettingMap.get(logDto.getTimesheetId());
				return setting != null && Integer.valueOf(1).equals(setting.getIsRemarkMandatory());
			})
			.map(BulkTimeLogRequestBodyDto::getId)
			.collect(Collectors.toSet());
	}

	private Map<Integer, List<WorkTimeDetailDto>> collectRangeBasedWorkDetails(
			BulkUpdateTimeLogsRequestBodyDto requestDto, Map<Integer, TimesheetSetting> timesheetSettingMap) {

		Map<Integer, List<WorkTimeDetailDto>> result = new HashMap<>();

		for (BulkTimeLogRequestBodyDto logDto : requestDto.getTimeLogs()) {
			if (isNullOrEmpty(logDto.getWorkTimeDetails())
					|| isEnterWorkTimeType(timesheetSettingMap, logDto.getTimesheetId())) {
				continue;
			}
			result.computeIfAbsent(logDto.getId(), (k) -> new ArrayList<>()).addAll(logDto.getWorkTimeDetails());
		}

		return result;
	}

	private boolean isEnterWorkTimeType(Map<Integer, TimesheetSetting> settingMap, Integer timesheetId) {
		TimesheetSetting setting = settingMap.get(timesheetId);
		return setting != null && setting.getWorkLogType() != null
				&& Objects.equals(setting.getWorkLogType(), workTimeEnum.ENTER_WORK_TIME.getId());
	}

	private void validateSingleTimeLogIntervals(Integer timeLogId, List<WorkTimeDetailDto> payloadDetails,
			List<TimeLogIntervalDto> existing, boolean isRemarkRequired) {

		List<WorkTimeDetailDto> validDetails = payloadDetails.stream().filter(Objects::nonNull).toList();

		Set<Integer> deletionIds = validDetails.stream()
			.filter((d) -> isDeletionMarker(d) && d.getId() != null)
			.map(WorkTimeDetailDto::getId)
			.collect(Collectors.toSet());

		Map<Integer, WorkTimeDetailDto> updates = validDetails.stream()
			.filter((d) -> !isDeletionMarker(d) && d.getId() != null)
			.collect(Collectors.toMap(WorkTimeDetailDto::getId, (d) -> d));

		List<WorkTimeDetailDto> newIntervals = validDetails.stream()
			.filter((d) -> !isDeletionMarker(d) && d.getId() == null)
			.toList();

		List<WorkTimeDetailDto> activeDetails = validDetails.stream().filter((d) -> !isDeletionMarker(d)).toList();

		validateIntervalCount(timeLogId, existing, deletionIds, newIntervals);
		validateBreakIntervalCount(timeLogId, activeDetails);
		validateBreaksWithinParentBounds(timeLogId, activeDetails);

		if (isRemarkRequired) {
			validateMandatoryRemarks(timeLogId, activeDetails);
		}

		List<int[]> effectiveIntervals = buildEffectiveIntervals(existing, deletionIds, updates, newIntervals);

		effectiveIntervals.sort((a, b) -> Integer.compare(a[0], b[0]));

		checkForOverlaps(timeLogId, effectiveIntervals);
	}

	private void validateIntervalCount(Integer timeLogId, List<TimeLogIntervalDto> existing, Set<Integer> deletionIds,
			List<WorkTimeDetailDto> newIntervals) {

		long survivingCount = existing.stream().filter((i) -> !deletionIds.contains(i.getId())).count();
		int totalAfterOperation = (int) survivingCount + newIntervals.size();

		if (totalAfterOperation > MAX_INTERVALS_PER_TIME_LOG) {
			throw new ValidationErrorException("Time log ID " + timeLogId + " would have " + totalAfterOperation
					+ " intervals after this operation, which exceeds the maximum of " + MAX_INTERVALS_PER_TIME_LOG);
		}
	}

	private void validateBreakIntervalCount(Integer timeLogId, List<WorkTimeDetailDto> activeDetails) {
		for (WorkTimeDetailDto detail : activeDetails) {
			if (detail.getBreakIntervals() != null && detail.getBreakIntervals().size() > MAX_BREAKS_PER_INTERVAL) {
				throw new ValidationErrorException(
						"Time log ID " + timeLogId + " has an interval with " + detail.getBreakIntervals().size()
								+ " break intervals, which exceeds the maximum of " + MAX_BREAKS_PER_INTERVAL);
			}
		}
	}

	private void validateBreaksWithinParentBounds(Integer timeLogId, List<WorkTimeDetailDto> activeDetails) {
		for (WorkTimeDetailDto detail : activeDetails) {
			if (isNullOrEmpty(detail.getBreakIntervals())) {
				continue;
			}
			validateSingleIntervalBreakBounds(timeLogId, detail);
		}
	}

	private void validateSingleIntervalBreakBounds(Integer timeLogId, WorkTimeDetailDto detail) {
		Integer workStart = detail.getWorkStartTime();
		Integer workEnd = detail.getWorkEndTime();

		if (workStart == null || workEnd == null) {
			return;
		}

		for (BreakIntervalDto breakInterval : detail.getBreakIntervals()) {
			if (breakInterval.getBreakStartTime() == null || breakInterval.getBreakEndTime() == null) {
				continue;
			}
			if (breakInterval.getBreakStartTime() < workStart || breakInterval.getBreakEndTime() > workEnd) {
				throw new ValidationErrorException("Time log ID " + timeLogId + " has a break interval "
						+ formatSecondsToTime(breakInterval.getBreakStartTime()) + "-"
						+ formatSecondsToTime(breakInterval.getBreakEndTime())
						+ " that falls outside its parent work interval " + formatSecondsToTime(workStart) + "-"
						+ formatSecondsToTime(workEnd));
			}
		}
	}

	private void validateMandatoryRemarks(Integer timeLogId, List<WorkTimeDetailDto> activeDetails) {
		boolean missingRemark = activeDetails.stream()
			.filter((d) -> d.getWorkStartTime() != null && d.getWorkEndTime() != null)
			.anyMatch((d) -> d.getRangeBasedRemark() == null || d.getRangeBasedRemark().isBlank());

		if (missingRemark) {
			throw new ValidationErrorException(
					"Time log ID " + timeLogId + " requires a remark for all time intervals when remark is mandatory");
		}
	}

	private List<int[]> buildEffectiveIntervals(List<TimeLogIntervalDto> existing, Set<Integer> deletionIds,
			Map<Integer, WorkTimeDetailDto> updates, List<WorkTimeDetailDto> newIntervals) {

		List<int[]> effectiveIntervals = new ArrayList<>();

		existing.stream()
			.filter((interval) -> !deletionIds.contains(interval.getId()))
			.map((interval) -> resolveIntervalTimeRange(interval, updates))
			.filter(Objects::nonNull)
			.forEach(effectiveIntervals::add);

		newIntervals.stream()
			.filter((i) -> i.getWorkStartTime() != null && i.getWorkEndTime() != null)
			.map((i) -> new int[] { i.getWorkStartTime(), i.getWorkEndTime() })
			.forEach(effectiveIntervals::add);

		return effectiveIntervals;
	}

	private int[] resolveIntervalTimeRange(TimeLogIntervalDto interval, Map<Integer, WorkTimeDetailDto> updates) {
		if (updates.containsKey(interval.getId())) {
			WorkTimeDetailDto updated = updates.get(interval.getId());
			return (updated.getWorkStartTime() != null && updated.getWorkEndTime() != null)
					? new int[] { updated.getWorkStartTime(), updated.getWorkEndTime() } : null;
		}
		return (interval.getWorkStartTime() != null && interval.getWorkEndTime() != null)
				? new int[] { interval.getWorkStartTime(), interval.getWorkEndTime() } : null;
	}

	private void checkForOverlaps(Integer timeLogId, List<int[]> effectiveIntervals) {
		for (int i = 1; i < effectiveIntervals.size(); i++) {
			int[] prev = effectiveIntervals.get(i - 1);
			int[] curr = effectiveIntervals.get(i);
			if (curr[0] < prev[1]) {
				throw new ValidationErrorException("Time log ID " + timeLogId + " has overlapping intervals: "
						+ formatSecondsToTime(prev[0]) + "-" + formatSecondsToTime(prev[1]) + " overlaps with "
						+ formatSecondsToTime(curr[0]) + "-" + formatSecondsToTime(curr[1]));
			}
		}
	}

	private String formatSecondsToTime(int totalSeconds) {
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		return String.format("%02d:%02d", hours, minutes);
	}

	/**
	 * Fetches TimesheetSetting for each timesheet ID using batch queries. Replaces the
	 * previous N-query loop with 2 batch queries (one for timesheets, one for settings).
	 */
	private Map<Integer, TimesheetSetting> fetchTimesheetSettings(List<Integer> timesheetIds, Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			throw new IllegalArgumentException("Timesheet IDs list cannot be null or empty");
		}
		if (accountId == null) {
			throw new IllegalArgumentException("Account ID cannot be null");
		}
		if (timesheetIds.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("Timesheet ID cannot be null");
		}

		List<Timesheet> timesheets = this.timesheetJpaRepository.findByIdInAndAccountId(timesheetIds, accountId);
		Map<Integer, Timesheet> timesheetById = timesheets.stream()
			.collect(Collectors.toMap(Timesheet::getId, (ts) -> ts));

		for (Integer timesheetId : timesheetIds) {
			if (!timesheetById.containsKey(timesheetId)) {
				throw new ResourceNotFoundException("Timesheet", timesheetId);
			}
		}

		List<Integer> settingIds = timesheets.stream().map(Timesheet::getTimesheetSettingId).distinct().toList();

		List<TimesheetSetting> settings = this.timesheetSettingJpaRepository.findByIdInAndAccountId(settingIds,
				accountId);
		Map<Integer, TimesheetSetting> settingById = settings.stream()
			.collect(Collectors.toMap(TimesheetSetting::getId, (s) -> s));

		Map<Integer, TimesheetSetting> timesheetSettingMap = new HashMap<>();
		for (Integer timesheetId : timesheetIds) {
			Timesheet ts = timesheetById.get(timesheetId);
			TimesheetSetting setting = settingById.get(ts.getTimesheetSettingId());
			if (setting == null) {
				throw new ResourceNotFoundException("TimesheetSetting", ts.getTimesheetSettingId());
			}
			timesheetSettingMap.put(timesheetId, setting);
		}
		return timesheetSettingMap;
	}

	/**
	 * Prepares batch operation data from request DTOs
	 * @param requestDto Request DTO
	 * @param timeLogMap Map of time log ID to TimeLog entity
	 * @param timesheetSettingMap Map of timesheet ID to TimesheetSetting
	 * @return BatchOperationData containing prepared values
	 */
	private BatchOperationData prepareBatchOperationData(BulkUpdateTimeLogsRequestBodyDto requestDto,
			Map<Integer, TimeLog> timeLogMap, Map<Integer, TimesheetSetting> timesheetSettingMap) {

		BatchOperationData batchData = new BatchOperationData();

		// Process each time log and prepare batch operations
		for (BulkTimeLogRequestBodyDto logDto : requestDto.getTimeLogs()) {

			Integer timesheetId = logDto.getTimesheetId();
			TimeLog timeLog = timeLogMap.get(logDto.getId());
			if (timeLog == null) {
				throw new ResourceNotFoundException("TimeLog", logDto.getId());
			}

			TimesheetSetting timesheetSetting = timesheetSettingMap.get(timesheetId);
			if (timesheetSetting == null) {
				throw new ResourceNotFoundException("TimesheetSetting", timesheetId);
			}

			// Prepare time log upsert values
			TimeLogUpsertDto timeLogValues = prepareTimeLogUpsertValues(logDto, timeLog, timesheetSetting);
			batchData.getTimeLogUpsertValues().add(timeLogValues);

			// Process work time details if applicable
			processWorkTimeDetails(logDto, timeLog, timesheetSetting, batchData);
		}

		return batchData;
	}

	/**
	 * Prepares upsert values for a single time log
	 * @param logDto Time log DTO
	 * @param timeLog Time log entity
	 * @param timesheetSetting Timesheet setting
	 * @return Object array with upsert values
	 */
	private TimeLogUpsertDto prepareTimeLogUpsertValues(BulkTimeLogRequestBodyDto logDto, TimeLog timeLog,
			TimesheetSetting timesheetSetting) {

		// Handle remark: empty string becomes null
		String remark = (logDto.getRemark() != null && !logDto.getRemark().isEmpty()) ? logDto.getRemark() : null;

		// Handle break_time: -1 becomes null
		Integer breakTime = (logDto.getBreakTime() != null && logDto.getBreakTime() != -1) ? logDto.getBreakTime()
				: null;

		// Handle over_time: -1 becomes null
		Integer overTime = (logDto.getOverTime() != null && logDto.getOverTime() != -1) ? logDto.getOverTime() : null;

		// Handle total_time: -1 becomes null
		Integer totalTime = (logDto.getTotalTime() != null && logDto.getTotalTime() != -1) ? logDto.getTotalTime()
				: null;

		// Handle work_time: -1 becomes null (only for ENTER_WORK_TIME type)
		Integer workTime = timeLog.getWorkTime(); // Default to existing value
		if (timesheetSetting.getWorkLogType() != null
				&& Objects.equals(timesheetSetting.getWorkLogType(), workTimeEnum.ENTER_WORK_TIME.getId())
				&& logDto.getWorkTime() != null) {
			workTime = (logDto.getWorkTime() == -1) ? null : logDto.getWorkTime();
		}

		return new TimeLogUpsertDto(timeLog.getId(), timeLog.getDate(), timeLog.getDayTypeId(),
				timeLog.getTimesheetId(), remark, breakTime, overTime, totalTime, workTime);
	}

	/**
	 * Processes work time details and prepares interval upsert values
	 * @param logDto Time log DTO
	 * @param timeLog Time log entity
	 * @param timesheetSetting Timesheet setting
	 * @param batchData Batch operation data to populate
	 */
	private void processWorkTimeDetails(BulkTimeLogRequestBodyDto logDto, TimeLog timeLog,
			TimesheetSetting timesheetSetting, BatchOperationData batchData) {

		// Only process for START_END_TIME entry type
		if (timesheetSetting.getWorkLogType() != null
				&& Objects.equals(timesheetSetting.getWorkLogType(), workTimeEnum.ENTER_WORK_TIME.getId())) {
			return;
		}

		// Process work time details if present
		if (logDto.getWorkTimeDetails() == null || logDto.getWorkTimeDetails().isEmpty()) {
			return;
		}

		Integer timeLogId = timeLog.getId();

		batchData.getTimeLogIdsWithIntervals().add(timeLogId);

		// Convert workTimeDetails to upsert format
		for (WorkTimeDetailDto workTimeDetail : logDto.getWorkTimeDetails()) {
			if (workTimeDetail == null || isDeletionMarker(workTimeDetail)) {
				continue;
			}

			// Convert break intervals to JSON string
			String breakIntervalJson = serializeBreakIntervals(workTimeDetail, timeLogId);

			// Prepare upsert DTO
			TimeLogIntervalUpsertDto intervalDto = new TimeLogIntervalUpsertDto(workTimeDetail.getId(), timeLogId,
					workTimeDetail.getWorkStartTime(), workTimeDetail.getWorkEndTime(),
					workTimeDetail.getRangeBasedRemark(), breakIntervalJson);
			batchData.getIntervalUpsertValues().add(intervalDto);
		}
	}

	/**
	 * Checks if a work time detail is a deletion marker
	 * @param workTimeDetail Work time detail DTO
	 * @return true if it's a deletion marker
	 */
	private boolean isDeletionMarker(WorkTimeDetailDto workTimeDetail) {
		return workTimeDetail != null && workTimeDetail.getId() != null && workTimeDetail.getWorkStartTime() != null
				&& workTimeDetail.getWorkStartTime() == -1 && workTimeDetail.getWorkEndTime() != null
				&& workTimeDetail.getWorkEndTime() == -1;
	}

	/**
	 * Serializes break intervals to JSON string
	 * @param workTimeDetail Work time detail DTO
	 * @param timeLogId Time log ID for error messages
	 * @return JSON string or null
	 */
	private String serializeBreakIntervals(WorkTimeDetailDto workTimeDetail, Integer timeLogId) {
		if (workTimeDetail == null) {
			return null;
		}

		if (workTimeDetail.getBreakIntervals() == null || workTimeDetail.getBreakIntervals().isEmpty()) {
			return null;
		}

		try {
			return this.objectMapper.writeValueAsString(workTimeDetail.getBreakIntervals());
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to serialize break intervals for time log " + timeLogId, ex);
		}
	}

	/**
	 * Collects deletion marker IDs from request DTO
	 * @param requestDto Request DTO
	 * @return Set of deletion marker IDs
	 */
	private Set<Integer> collectDeletionMarkers(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		Set<Integer> deletionMarkerIds = new HashSet<>();

		if (requestDto == null || requestDto.getTimeLogs() == null) {
			return deletionMarkerIds;
		}

		for (BulkTimeLogRequestBodyDto logDto : requestDto.getTimeLogs()) {
			// Work hours payload type does not have workTimeDetails - skip
			if (logDto.getWorkTimeDetails() == null || logDto.getWorkTimeDetails().isEmpty()) {
				continue;
			}
			for (WorkTimeDetailDto workTimeDetail : logDto.getWorkTimeDetails()) {
				if (isDeletionMarker(workTimeDetail) && workTimeDetail.getId() != null) {
					deletionMarkerIds.add(workTimeDetail.getId());
				}
			}
		}

		return deletionMarkerIds;
	}

	/**
	 * Handles interval deletions efficiently
	 *
	 * OPTIMIZATION: This method assumes that the UI ALWAYS sends deletion markers
	 * (workStartTime = -1, workEndTime = -1) when intervals are deleted. Therefore, we
	 * don't need to fetch all existing intervals from the database to compare with the
	 * payload. We can directly delete the intervals marked for deletion.
	 *
	 * Performance benefit: For 100 timesheets × 30 days × 10 intervals = 30,000
	 * intervals, this avoids fetching 30k IDs from the database, saving significant query
	 * time and memory.
	 * @param deletionMarkerIds Set of deletion marker IDs (intervals with workStartTime =
	 * -1, workEndTime = -1)
	 */
	private void deleteIntervals(Set<Integer> deletionMarkerIds) {
		Set<Integer> effectiveDeletionMarkerIds = (deletionMarkerIds != null) ? deletionMarkerIds : new HashSet<>();

		// Early exit if no deletion markers
		if (effectiveDeletionMarkerIds.isEmpty()) {
			return;
		}

		try {
			// Convert to list for batch deletion
			List<Integer> intervalIdsToDelete = new ArrayList<>(effectiveDeletionMarkerIds);

			this.timeLogIntervalRepository.deleteByIdIn(intervalIdsToDelete);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to delete time log intervals. Transaction will be rolled back.",
					ex);
		}
	}

	/**
	 * Executes all batch database operations within transaction
	 * @param batchData Batch operation data
	 * @param requestDto Request DTO
	 * @param timeLogsByTimesheetId Map of timesheet ID to time log DTOs
	 * @param userId User ID
	 * @param userTypeId User type ID
	 */
	private void executeBatchDatabaseOperations(BatchOperationData batchData,
			BulkUpdateTimeLogsRequestBodyDto requestDto,
			Map<Integer, List<BulkTimeLogRequestBodyDto>> timeLogsByTimesheetId, Integer userId, Integer userTypeId) {

		// Step 1: Upsert time logs
		upsertTimeLogs(batchData.getTimeLogUpsertValues());

		// Step 2: Collect deletion markers
		Set<Integer> deletionMarkerIds = collectDeletionMarkers(requestDto);

		// Step 3: Delete intervals
		deleteIntervals(deletionMarkerIds);

		// Step 4: Upsert intervals
		upsertIntervals(batchData.getIntervalUpsertValues());

		// Step 5: Update timesheet metadata (includes time details update in same query)
		updateTimesheetMetadata(requestDto, timeLogsByTimesheetId, userId, userTypeId);
	}

	/**
	 * Upserts time logs in batch
	 * @param timeLogUpsertValues List of time log upsert DTOs
	 */
	private void upsertTimeLogs(List<TimeLogUpsertDto> timeLogUpsertValues) {
		if (timeLogUpsertValues == null || timeLogUpsertValues.isEmpty()) {
			return;
		}

		try {
			this.timeLogRepository.batchUpsert(timeLogUpsertValues);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to batch upsert time logs. Transaction will be rolled back.", ex);
		}
	}

	/**
	 * Upserts intervals in batch
	 * @param intervalUpsertValues List of interval upsert DTOs
	 */
	private void upsertIntervals(List<TimeLogIntervalUpsertDto> intervalUpsertValues) {
		if (intervalUpsertValues == null || intervalUpsertValues.isEmpty()) {
			return;
		}

		try {
			this.timeLogIntervalRepository.batchUpsert(intervalUpsertValues);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to batch upsert time log intervals. Transaction will be rolled back.", ex);
		}
	}

	/**
	 * Updates timesheet approval status, last modified, and time details in batch.
	 * Collects all approval entries and saves them in one go, then combines last modified
	 * + time details into a single UPDATE on cst_timesheet_t.
	 * @param requestDto Request DTO
	 * @param timeLogsByTimesheetId Map of timesheet ID to time log DTOs
	 * @param userId User ID
	 * @param userTypeId User type ID
	 */
	private void updateTimesheetMetadata(BulkUpdateTimeLogsRequestBodyDto requestDto,
			Map<Integer, List<BulkTimeLogRequestBodyDto>> timeLogsByTimesheetId, Integer userId, Integer userTypeId) {

		try {
			List<Integer> validTimesheetIds = timeLogsByTimesheetId.entrySet()
				.stream()
				.filter((entry) -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty())
				.map(Map.Entry::getKey)
				.toList();

			if (validTimesheetIds.isEmpty()) {
				return;
			}

			Integer approvalStatus = Boolean.TRUE.equals(requestDto.getSave()) ? ApprovalStatusEnum.OPEN.getId()
					: ApprovalStatusEnum.SUBMITTED.getId();
			Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());

			List<TimesheetApproval> approvalsToSave = new ArrayList<>();

			for (Integer timesheetId : validTimesheetIds) {
				TimesheetApproval approval = new TimesheetApproval();
				approval.setTimesheetId(timesheetId);
				approval.setEntityId(userId);
				approval.setUserTypeId(userTypeId);
				approval.setTimesheetApprovalStatusTypeId(approvalStatus);
				approval.setCreatedOn(currentUNIXTimestamp);
				approval.setRemark(null);
				approvalsToSave.add(approval);
			}

			if (Boolean.TRUE.equals(requestDto.getIsApproved())) {
				this.appendApprovalEntriesForApprovedTimesheets(approvalsToSave, validTimesheetIds, userId, userTypeId,
						currentUNIXTimestamp);
				this.timesheetApprovalRepository.createBulkTimesheetApprovals(approvalsToSave);
				createInvoicesForTimesheets(validTimesheetIds, userId, userTypeId);
			}
			else {
				this.timesheetApprovalRepository.createBulkTimesheetApprovals(approvalsToSave);
			}

			List<TimeDetailSummaryDto> timeDetails = (requestDto.getTimeDetails() != null) ? requestDto.getTimeDetails()
					: List.of();

			this.timesheetUpdateHelper.batchUpdateTimesheetLastModifiedWithTimeDetails(validTimesheetIds, userId,
					userTypeId, timeDetails);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to update timesheet metadata or time details. Transaction will be rolled back.", ex);
		}
	}

	private void appendApprovalEntriesForApprovedTimesheets(List<TimesheetApproval> approvalsToSave,
			List<Integer> validTimesheetIds, Integer userId, Integer userTypeId, int currentUNIXTimestamp) {
		List<Timesheet> timesheets = this.timesheetJpaRepository.findAllById(validTimesheetIds);
		List<Integer> timesheetSettingIds = timesheets.stream()
			.map(Timesheet::getTimesheetSettingId)
			.distinct()
			.toList();

		List<TimesheetApprover> allApprovers = this.timesheetApproverRepository
			.findByTimesheetSettingIds(timesheetSettingIds);
		Map<Integer, List<TimesheetApprover>> approversBySettingId = allApprovers.stream()
			.collect(Collectors.groupingBy(TimesheetApprover::getTimesheetSettingId));

		// For client (COMPANY_CONTACT), resolve all contact IDs sharing the JWT email
		// once before the loop — a different contact record may be the registered
		// approver. For other personas the original single-userId check is unchanged.
		List<Integer> approverContactIds = UserTypeEnum.COMPANY_CONTACT.getId().equals(userTypeId)
				? this.portalAccessControlService
					.resolveContactIds(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
				: null;

		for (Timesheet timesheet : timesheets) {
			List<TimesheetApprover> approvers = approversBySettingId.getOrDefault(timesheet.getTimesheetSettingId(),
					List.of());
			if (!this.isAuthorizedApprover(approvers, approverContactIds, userId, userTypeId)) {
				throw new ValidationErrorException(
						"User is not authorized to approve this timesheet. Timesheet ID: " + timesheet.getId());
			}

			TimesheetApproval approvedEntry = new TimesheetApproval();
			approvedEntry.setTimesheetId(timesheet.getId());
			approvedEntry.setEntityId(userId);
			approvedEntry.setUserTypeId(userTypeId);
			approvedEntry.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
			approvedEntry.setCreatedOn(currentUNIXTimestamp);
			approvedEntry.setRemark(null);
			approvalsToSave.add(approvedEntry);
		}
	}

	private boolean isAuthorizedApprover(List<TimesheetApprover> approvers, List<Integer> approverContactIds,
			Integer userId, Integer userTypeId) {
		if (approverContactIds != null) {
			return approvers.stream()
				.anyMatch((approver) -> approverContactIds.contains(approver.getEntityId())
						&& approver.getUserTypeId().equals(userTypeId));
		}
		return approvers.stream()
			.anyMatch(
					(approver) -> approver.getEntityId().equals(userId) && approver.getUserTypeId().equals(userTypeId));
	}

	private void createInvoicesForTimesheets(List<Integer> timesheetIds, Integer userId, Integer userTypeId) {
		for (Integer timesheetId : timesheetIds) {
			this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);
		}
	}

	private void publishTimesheetNotification(final List<Integer> timesheetIds, final Integer accountId,
			final Integer createdByUserTypeId, final String eventName, final String performerDisplayName,
			final TimesheetReminderNotificationChannelsDto channels) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}
		final TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(timesheetIds), eventName, accountId,
				createdByUserTypeId, UserTypeEnum.CONTRACTOR.getId(), ReminderNotificationEventType.REALTIME,
				channels.sendInappNotification(), channels.sendEmailNotification(), channels.sendPortalNotification(),
				performerDisplayName, null);
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload);
	}

	/**
	 * Invokes rule engine if needed (outside transaction scope)
	 * @param requestDto Request DTO
	 * @param timesheetIds List of timesheet IDs
	 */
	private void invokeRuleEngineIfNeeded(BulkUpdateTimeLogsRequestBodyDto requestDto, List<Integer> timesheetIds) {
		if (requestDto == null || timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}

		// Skip if save is true
		if (Boolean.TRUE.equals(requestDto.getSave())) {
			return;
		}

		// Invoke rule engine for all timesheets (outside transaction scope)
		for (Integer timesheetId : timesheetIds) {
			if (timesheetId == null) {
				continue; // Skip null timesheet IDs
			}

			try {
				this.invokeAndCacheRuleEngineEvaluationResult(timesheetId);
			}
			catch (Exception ex) {
				throw new ExternalServiceException("Failed to invoke rule engine for timesheet " + timesheetId
						+ ". Transaction completed successfully.", ex);
			}
		}
	}

	/**
	 * Updates timesheet time details (totalTime, totalWorkTime, totalOvertime) from the
	 * request payload. Runs AFTER the rule engine to prevent the rule engine's JPA save
	 * from overwriting these values.
	 * @param requestDto the bulk update request containing timeDetails
	 */
	private void updateTimesheetTimeDetailsFromRequest(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		if (requestDto == null || requestDto.getTimeDetails() == null || requestDto.getTimeDetails().isEmpty()) {
			return;
		}

		for (TimeDetailSummaryDto totalTimeDetail : requestDto.getTimeDetails()) {
			if (totalTimeDetail == null || totalTimeDetail.getTimesheetId() == null) {
				continue;
			}

			this.timesheetUpdateHelper.updateTimesheetTimeDetails(totalTimeDetail.getTimesheetId(),
					totalTimeDetail.getTotalTime(), totalTimeDetail.getTotalWorkTime());
		}
	}

	/**
	 * Validates that none of the provided timesheets are in approved status. If save is
	 * true, also validates that all timesheets are in open state. Uses a single batch
	 * query instead of N individual queries.
	 * @param timesheetIds list of timesheet IDs to validate
	 * @param save if true, validates that all timesheets are in open state
	 * @throws ValidationErrorException if any timesheet is approved or (if save is true)
	 * not in open state
	 */
	private void validateTimesheetsNotApproved(List<Integer> timesheetIds, Boolean save) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}
		List<TimesheetApproval> latestApprovals = this.timesheetApprovalRepository
			.findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		Map<Integer, TimesheetApproval> approvalByTimesheetId = latestApprovals.stream()
			.collect(Collectors.toMap(TimesheetApproval::getTimesheetId, (approval) -> approval));

		for (Integer timesheetId : timesheetIds) {
			TimesheetApproval latestApproval = approvalByTimesheetId.get(timesheetId);

			if (Boolean.TRUE.equals(save)) {
				if (latestApproval == null || !Objects.equals(latestApproval.getTimesheetApprovalStatusTypeId(),
						ApprovalStatusEnum.OPEN.getId())) {
					throw new ValidationErrorException("Timesheet is not in open state. Timesheet ID: " + timesheetId);
				}
			}
			else {
				if (latestApproval != null && Objects.equals(latestApproval.getTimesheetApprovalStatusTypeId(),
						ApprovalStatusEnum.APPROVED.getId())) {
					throw new ValidationErrorException(
							"Cannot edit time logs for approved timesheet. Timesheet ID: " + timesheetId);
				}
			}
		}
	}

	private void invokeAndCacheRuleEngineEvaluationResult(Integer timesheetId) {
		RuleEngineRequestBodyDto ruleEngineRequestBodyDto = RuleEngineRequestBodyDto.builder()
			.timesheetId(timesheetId)
			.refreshStoredData(true)
			.build();
		this.ruleEngineService.evaluateRules(ruleEngineRequestBodyDto);
	}

	private void updateTimesheetApprovalStatus(Integer timesheetId, Boolean isApproved, Integer userId,
			Integer userTypeId, Boolean save) {
		Integer approvalStatus = Boolean.TRUE.equals(save) ? ApprovalStatusEnum.OPEN.getId()
				: ApprovalStatusEnum.SUBMITTED.getId();
		this.timesheetApprovalRepository.createTimesheetApproval(timesheetId, userId, userTypeId, approvalStatus, null);

		if (Boolean.TRUE.equals(isApproved)) {
			if (UserTypeEnum.COMPANY_CONTACT.getId().equals(userTypeId)) {
				// Client portal: expand to all contacts sharing the JWT email so that a
				// different contact record with the same email is also a valid approver.
				Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
				this.portalAccessControlService.validateApproverAccess(timesheetId, userId, userTypeId, accountId);
			}
			else {
				// Original unchanged check for agency users and contractors.
				Timesheet timesheet = this.timesheetJpaRepository.findById(timesheetId)
					.orElseThrow(() -> new ResourceNotFoundException("Timesheet", timesheetId));

				Integer timesheetSettingId = timesheet.getTimesheetSettingId();

				List<TimesheetApprover> approvers = this.timesheetApproverRepository
					.findByTimesheetSettingId(timesheetSettingId);

				boolean isApprover = approvers.stream()
					.anyMatch((approver) -> approver.getEntityId().equals(userId)
							&& approver.getUserTypeId().equals(userTypeId));

				if (!isApprover) {
					throw new ValidationErrorException("User is not authorized to approve this timesheet");
				}
			}

			this.timesheetApprovalRepository.createTimesheetApproval(timesheetId, userId, userTypeId,
					ApprovalStatusEnum.APPROVED.getId(), null);

			// Create timesheet invoice when approving timesheet
			this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);
		}

	}

	/**
	 * Formats epoch timestamps into a readable date range string Uses UTC timezone for
	 * consistent date formatting across different server timezones.
	 * @param startDateEpoch Start date in epoch seconds
	 * @param endDateEpoch End date in epoch seconds
	 * @return Formatted date range string (e.g. "Jan 01, 2023 - Jan 31, 2023")
	 */
	private String formatTimesheetPeriod(Integer startDateEpoch, Integer endDateEpoch) {
		if (startDateEpoch == null || endDateEpoch == null) {
			return "";
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

		// Use UTC timezone consistently for period formatting
		LocalDate startDate = convertEpochToUtcLocalDate(startDateEpoch);
		LocalDate endDate = convertEpochToUtcLocalDate(endDateEpoch);

		return String.format("%s - %s", startDate.format(formatter), endDate.format(formatter));
	}

	/**
	 * Converts epoch timestamp to UTC LocalDate for consistent date comparison. This
	 * ensures that period validation is based on actual dates rather than
	 * timezone-sensitive epoch values.
	 */
	private LocalDate convertEpochToUtcLocalDate(Integer epochSeconds) {
		if (epochSeconds == null) {
			return null;
		}
		return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
	}

	private void updateTimesheetLastModified(Integer timesheetId, Integer userId, Integer userTypeId) {
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);
	}

	public FetchBulkTimelogResultBodyDto getAllTimeLogs(List<Integer> timesheetIds,
			List<TimesheetAndSettingValidatorResponseBodyDto> timesheetAndSettingValidatorResponseBodyDtos,
			List<TimesheetSettingErrorResponseBodyDto> errorData) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		// Convert the list to a HashMap with timesheetId as the key
		// Use merge function to handle potential duplicate timesheetId values
		Map<Integer, TimesheetAndSettingValidatorResponseBodyDto> timesheetMap = timesheetAndSettingValidatorResponseBodyDtos
			.stream()
			.collect(Collectors.toMap(TimesheetAndSettingValidatorResponseBodyDto::getTimesheetId, (dto) -> dto,
					(existing, replacement) -> existing));

		// Filter out timesheet IDs that have errors before querying time logs
		Set<Integer> timesheetIdsWithErrors = errorData.stream()
			.map(TimesheetSettingErrorResponseBodyDto::getTimesheetId)
			.collect(Collectors.toSet());

		// Only fetch time logs for timesheets that don't have errors
		List<Integer> validTimesheetIds = timesheetIds.stream()
			.filter((timesheetId) -> !timesheetIdsWithErrors.contains(timesheetId))
			.toList();

		List<TimelogQueryResultDto> timeLogs = this.timeLogRepository.findTimeLogsWithDetails(validTimesheetIds,
				accountId);

		// Get timesheet setting data for each valid timesheet
		List<Object[]> timesheetSettingsData = this.timeLogRepository
			.findTimesheetSettingsForTimesheets(validTimesheetIds, accountId);
		Map<Integer, Object[]> timesheetToSettingMap = timesheetSettingsData.stream()
			.collect(Collectors.toMap((row) -> (Integer) row[0], // timesheetId
					(row) -> row // [timesheetId, calculateBreakTime, breakTimeThreshold,
			// templateWorkDay, isRemarkMandatory]
			));

		// Get break intervals and work time details for all time logs
		List<Integer> allTimeLogIds = timeLogs.stream().map(TimelogQueryResultDto::getId).toList();

		// Fetch TimeLogInterval records which contain break intervals and work time
		// details
		List<TimeLogInterval> timeLogIntervals = this.timeLogIntervalRepository.findByTimeLogIdIn(allTimeLogIds);

		Map<Integer, Integer> timeLogIdToTimesheetIdMap = timeLogs.stream()
			.collect(Collectors.toMap(TimelogQueryResultDto::getId, TimelogQueryResultDto::getTimesheetId,
					(existing, replacement) -> existing));

		Map<Integer, Integer> timesheetIdToWorkLogTypeMap = timesheetMap.entrySet()
			.stream()
			.filter((e) -> e.getValue() != null && e.getValue().getWorkLogType() != null)
			.collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().getWorkLogType(), (a, b) -> a));

		// Build work time details map for ENTER_START_END_TIME timesheets (multiple
		// intervals per time log)
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap = getWorkTimeDetailsMapForBulk(
				timeLogIntervals, timeLogIdToTimesheetIdMap, timesheetIdToWorkLogTypeMap);

		// Extract break intervals from JSON and group by timeLogId (for
		// ENTER_WORK_TIME timesheets)
		Map<Integer, List<BreakIntervalResponseBodyDto>> breakIntervalsMap = new HashMap<>();
		prepareBreakIntervalsMap(timeLogIntervals, timeLogIdToTimesheetIdMap, timesheetIdToWorkLogTypeMap,
				breakIntervalsMap);

		// Get contractor details for contractorName and contractorProfilePicUrl
		Set<Integer> contractorIds = timesheetAndSettingValidatorResponseBodyDtos.stream()
			.map(TimesheetAndSettingValidatorResponseBodyDto::getContractorId)
			.collect(Collectors.toSet());
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = this.candidateRepository
			.getContractorQueryResultMap(contractorIds);

		// Get job details (jobName, jobSlug) and assignment ID for each timesheet
		Map<Integer, TimesheetJobQueryResultDto> jobDetailsMap = this.timeLogRepository
			.findCompanyByTimesheetIds(validTimesheetIds, accountId);

		// Batch-fetch latest approval status for all valid timesheets (1 query)
		Map<Integer, Integer> timesheetStatusMap = this.timesheetApprovalRepository
			.findLatestApprovalEntitiesByTimesheetIds(validTimesheetIds)
			.stream()
			.collect(Collectors.toMap(TimesheetApproval::getTimesheetId,
					TimesheetApproval::getTimesheetApprovalStatusTypeId, (existing, replacement) -> existing));

		// Get unique timesheet setting IDs for fetching approvers
		Set<Integer> timesheetSettingIds = timesheetAndSettingValidatorResponseBodyDtos.stream()
			.map(TimesheetAndSettingValidatorResponseBodyDto::getTimesheetSettingId)
			.collect(Collectors.toSet());

		// Fetch all approvers for the unique timesheet setting IDs
		Map<Integer, List<TimesheetApprover>> approversMap = new HashMap<>();
		for (Integer timesheetSettingId : timesheetSettingIds) {
			List<TimesheetApprover> approvers = this.timesheetApproverRepository
				.findByTimesheetSettingId(timesheetSettingId);
			approversMap.put(timesheetSettingId, approvers);
		}

		List<DayTimelogQueryResultDto> dayTimelogQueryResultDtos = new ArrayList<>();

		for (Integer timesheetId : validTimesheetIds) {
			List<TimelogQueryResultDto> timeLogsForTimesheet = new ArrayList<>();
			prepareTimeLogsListForTimeSheet(timeLogs, timesheetId, timeLogsForTimesheet);
			List<TimelogResponseBodyDto> timelogResponseBodyDtos = this.timeLogMapper
				.timeLogQueryResultDtoToResponseBodyDto(timeLogsForTimesheet);

			// Populate work time details (for ENTER_START_END_TIME) or break intervals
			// (for ENTER_WORK_TIME)
			populateWorkTimeDetailsOrBreakIntervals(timelogResponseBodyDtos, workTimeDetailsMap, breakIntervalsMap,
					timeLogIdToTimesheetIdMap, timesheetIdToWorkLogTypeMap);

			DayTimelogQueryResultDto dayTimelogQueryResultDto = new DayTimelogQueryResultDto();
			TimesheetAndSettingValidatorResponseBodyDto timesheetSettingDto = timesheetMap.get(timesheetId);
			Integer contractorId = getContractorIdFromTimeSheetSettings(timesheetSettingDto);
			dayTimelogQueryResultDto.setId(contractorId);
			dayTimelogQueryResultDto.setTimesheetId(timesheetId);
			dayTimelogQueryResultDto.setTimesheetStatus(timesheetStatusMap.get(timesheetId));

			// Set contractor name, profile picture and slug
			ContractorNamePhotoQueryResultDto contractorDetails = contractorDetailsMap.get(contractorId);
			setContractorDetailsInResultDTOifPresent(dayTimelogQueryResultDto, contractorDetails);

			// Set job ID, name, slug and assignment ID
			fetchAndSetCompanyDetailsInfoIntoResultList(jobDetailsMap, timesheetId, dayTimelogQueryResultDto);

			dayTimelogQueryResultDto.setTimeLogs(timelogResponseBodyDtos);
			dayTimelogQueryResultDto.setTotalTime(timeLogsForTimesheet.stream()
				.mapToInt((t) -> (t.getTotalTime() != null) ? t.getTotalTime() : 0)
				.sum());

			dayTimelogQueryResultDto.setTotalOvertime(timeLogsForTimesheet.stream()
				.mapToInt((t) -> (t.getOverTime() != null) ? t.getOverTime() : 0)
				.sum());

			// Populate calculateBreakTime, breakTimeThreshold, templateWorkDays and
			// isRemarkMandatory for
			// this timesheet
			Object[] settingData = timesheetToSettingMap.get(timesheetId);
			if (settingData != null) {
				dayTimelogQueryResultDto.setCalculateBreakTime((Boolean) settingData[1]);
				dayTimelogQueryResultDto.setBreakTimeThreshold((Integer) settingData[2]);
				@SuppressWarnings("unchecked")
				List<TemplateWorkDay> templateWorkDays = (List<TemplateWorkDay>) settingData[3];
				dayTimelogQueryResultDto.setTemplateWorkDays(templateWorkDays);
				dayTimelogQueryResultDto.setIsRemarkMandatory((Integer) settingData[4]);
			}

			// Get and set approvers for this contractor based on timesheet setting ID
			Integer timesheetSettingId = (timesheetSettingDto != null) ? timesheetSettingDto.getTimesheetSettingId()
					: null;
			List<TimesheetApprover> approvers = (timesheetSettingId != null) ? approversMap.get(timesheetSettingId)
					: Collections.emptyList();
			dayTimelogQueryResultDto.setApprovers(this.timeLogMapper.mapApprovers(approvers));

			dayTimelogQueryResultDtos.add(dayTimelogQueryResultDto);
		}
		return getFetchBulkTimelogResultBodyDto(timesheetAndSettingValidatorResponseBodyDtos, timesheetIds.getFirst(),
				dayTimelogQueryResultDtos);
	}

	private void setContractorDetailsInResultDTOifPresent(DayTimelogQueryResultDto dayTimelogQueryResultDto,
			ContractorNamePhotoQueryResultDto contractorDetails) {
		if (contractorDetails != null) {
			dayTimelogQueryResultDto.setContractorName(contractorDetails.getName());
			dayTimelogQueryResultDto.setContractorProfilePicUrl(contractorDetails.getProfilePic());
			dayTimelogQueryResultDto.setContractorSlug(contractorDetails.getSlug());
		}
	}

	private Integer getContractorIdFromTimeSheetSettings(
			TimesheetAndSettingValidatorResponseBodyDto timesheetSettingDto) {
		return (timesheetSettingDto != null) ? timesheetSettingDto.getContractorId() : null;
	}

	private void fetchAndSetCompanyDetailsInfoIntoResultList(Map<Integer, TimesheetJobQueryResultDto> jobDetailsMap,
			Integer timesheetId, DayTimelogQueryResultDto dayTimelogQueryResultDto) {
		if (jobDetailsMap != null) {
			TimesheetJobQueryResultDto companyDetails = jobDetailsMap.get(timesheetId);
			if (companyDetails != null) {
				dayTimelogQueryResultDto.setJobId(companyDetails.getJobId());
				dayTimelogQueryResultDto.setJobName(companyDetails.getJobName());
				dayTimelogQueryResultDto.setJobSlug(companyDetails.getJobSlug());
				dayTimelogQueryResultDto.setAssignmentId(companyDetails.getAssignmentId());
			}
		}
	}

	private void prepareTimeLogsListForTimeSheet(List<TimelogQueryResultDto> timeLogs, Integer timesheetId,
			List<TimelogQueryResultDto> timeLogsForTimesheet) {
		for (TimelogQueryResultDto timeLog : timeLogs) {
			if (timeLog.getTimesheetId().equals(timesheetId)) {
				timeLogsForTimesheet.add(timeLog);
			}
		}
	}

	private void prepareBreakIntervalsMap(List<TimeLogInterval> timeLogIntervals,
			Map<Integer, Integer> timeLogIdToTimesheetIdMap, Map<Integer, Integer> timesheetIdToWorkLogTypeMap,
			Map<Integer, List<BreakIntervalResponseBodyDto>> breakIntervalsMap) {
		for (TimeLogInterval interval : timeLogIntervals) {
			if (interval.getBreakInterval() != null && !interval.getBreakInterval().isEmpty()) {
				Integer timeLogId = interval.getTimeLogId();
				Integer timesheetId = timeLogIdToTimesheetIdMap.get(timeLogId);
				if (timesheetId != null) {
					Integer workLogType = timesheetIdToWorkLogTypeMap.get(timesheetId);
					if (!Objects.equals(workLogType, workTimeEnum.ENTER_START_END_TIME.getId())) {
						List<BreakIntervalResponseBodyDto> breakIntervalDtos = interval.getBreakInterval()
							.stream()
							.map((breakInterval) -> new BreakIntervalResponseBodyDto(breakInterval.getId(), timeLogId,
									breakInterval.getBreakStartTime(), breakInterval.getBreakEndTime()))
							.toList();

						breakIntervalsMap.computeIfAbsent(timeLogId, (k) -> new ArrayList<>())
							.addAll(breakIntervalDtos);
					}
				}
			}
		}
	}

	public FetchBulkContractorTimelogResultBodyDto getContractorAllTimeLogs(List<Integer> timesheetIds,
			List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorTimesheetAndSettingValidatorResponseBodyDtos,
			List<ContractorTimesheetSettingErrorResponseBodyDto> errorData) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// Filter out timesheet IDs that have errors before querying time logs
		Set<Integer> timesheetIdsWithErrors = errorData.stream()
			.map(ContractorTimesheetSettingErrorResponseBodyDto::getTimesheetId)
			.collect(Collectors.toSet());

		// Only fetch time logs for timesheets that don't have errors
		List<Integer> validTimesheetIds = timesheetIds.stream()
			.filter((timesheetId) -> !timesheetIdsWithErrors.contains(timesheetId))
			.toList();

		// Use enhanced query to get both time logs and timesheet setting metadata in one
		// query
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = this.timeLogRepository
			.findTimeLogsWithSettingDetails(validTimesheetIds, accountId);

		// Extract time logs for existing logic
		List<TimelogQueryResultDto> timeLogs = timeLogsWithSettings.stream()
			.map((combined) -> new TimelogQueryResultDto(combined.getId(), combined.getDate(), combined.getDayTypeId(),
					combined.getWorkTime(), combined.getWorkStartTime(), combined.getWorkEndTime(),
					combined.getBreakTime(), combined.getOverTime(), combined.getRemark(), combined.getTotalTime(),
					combined.getTimesheetId(), combined.getPeriodStart(), combined.getPeriodEnd()))
			.toList();

		// Get unique timesheet setting IDs for fetching approvers
		Set<Integer> timesheetSettingIds = timeLogsWithSettings.stream()
			.map(TimelogWithSettingQueryResultDto::getTimesheetSettingId)
			.collect(Collectors.toSet());

		// Fetch all approvers for the unique timesheet setting IDs
		Map<Integer, List<TimesheetApprover>> approversMap = new HashMap<>();
		for (Integer timesheetSettingId : timesheetSettingIds) {
			List<TimesheetApprover> approvers = this.timesheetApproverRepository
				.findByTimesheetSettingId(timesheetSettingId);
			approversMap.put(timesheetSettingId, approvers);
		}

		// Batch-fetch latest approval status for all valid timesheets (1 query)
		Map<Integer, Integer> timesheetStatusMap = this.timesheetApprovalRepository
			.findLatestApprovalEntitiesByTimesheetIds(validTimesheetIds)
			.stream()
			.collect(Collectors.toMap(TimesheetApproval::getTimesheetId,
					TimesheetApproval::getTimesheetApprovalStatusTypeId, (existing, replacement) -> existing));

		// Extract unique timesheet metadata for timelogsMetaData (without individual
		// approvers)
		List<TimelogsMetaDataDto> timelogsMetaData = timeLogsWithSettings.stream()
			.collect(Collectors.groupingBy(TimelogWithSettingQueryResultDto::getTimesheetId))
			.values()
			.stream()
			.map((group) -> {
				TimelogWithSettingQueryResultDto first = group.getFirst();
				TimelogsMetaDataDto metaData = new TimelogsMetaDataDto();
				metaData.setTimesheetId(first.getTimesheetId());
				metaData.setCalculateBreakTime(first.getCalculateBreakTime());
				metaData.setBreakTimeThreshold(first.getBreakTimeThreshold());
				metaData.setTemplateWorkDays(first.getTemplateWorkDays());
				metaData.setIsRemarkMandatory(first.getIsRemarkMandatory());
				metaData.setTimesheetStatus(timesheetStatusMap.get(first.getTimesheetId()));
				return metaData;
			})
			.toList();

		// Get break intervals and work time details for all time logs
		List<Integer> allTimeLogIds = timeLogs.stream().map(TimelogQueryResultDto::getId).toList();

		// Fetch TimeLogInterval records which contain break intervals and work time
		// details
		List<TimeLogInterval> timeLogIntervals = this.timeLogIntervalRepository.findByTimeLogIdIn(allTimeLogIds);

		Map<Integer, Integer> timeLogIdToTimesheetIdMap = timeLogs.stream()
			.collect(Collectors.toMap(TimelogQueryResultDto::getId, TimelogQueryResultDto::getTimesheetId,
					(existing, replacement) -> existing));

		Map<Integer, Integer> timesheetIdToWorkLogTypeMap = contractorTimesheetAndSettingValidatorResponseBodyDtos
			.stream()
			.filter((dto) -> dto.getTimesheetId() != null && dto.getWorkLogType() != null)
			.collect(Collectors.toMap(ContractorTimesheetAndSettingValidatorResponseBodyDto::getTimesheetId,
					ContractorTimesheetAndSettingValidatorResponseBodyDto::getWorkLogType, (a, b) -> a));

		// Build work time details map for ENTER_START_END_TIME timesheets (multiple
		// intervals per time log)
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap = getWorkTimeDetailsMapForBulk(
				timeLogIntervals, timeLogIdToTimesheetIdMap, timesheetIdToWorkLogTypeMap);

		// Extract break intervals from JSON and group by timeLogId (for
		// ENTER_WORK_TIME timesheets)
		Map<Integer, List<BreakIntervalResponseBodyDto>> breakIntervalsMap = new HashMap<>();
		prepareBreakIntervalsMap(timeLogIntervals, timeLogIdToTimesheetIdMap, timesheetIdToWorkLogTypeMap,
				breakIntervalsMap);

		List<TimelogResponseBodyDto> allTimelogResponseBodyDtos = new ArrayList<>();

		for (Integer timesheetId : validTimesheetIds) {
			List<TimelogQueryResultDto> timeLogsForTimesheet = new ArrayList<>();
			prepareTimeLogsListForTimeSheet(timeLogs, timesheetId, timeLogsForTimesheet);
			List<TimelogResponseBodyDto> timelogResponseBodyDtos = this.timeLogMapper
				.timeLogQueryResultDtoToResponseBodyDto(timeLogsForTimesheet);

			// Populate work time details (for ENTER_START_END_TIME) or break intervals
			// (for ENTER_WORK_TIME)
			populateWorkTimeDetailsOrBreakIntervals(timelogResponseBodyDtos, workTimeDetailsMap, breakIntervalsMap,
					timeLogIdToTimesheetIdMap, timesheetIdToWorkLogTypeMap);

			// Set timesheet period for each time log using data from query results
			for (int i = 0; i < timelogResponseBodyDtos.size(); i++) {
				TimelogResponseBodyDto timelogDto = timelogResponseBodyDtos.get(i);
				TimelogQueryResultDto queryResult = timeLogsForTimesheet.get(i);

				// Format timesheet period from query result data
				String timesheetPeriod = formatTimesheetPeriod(queryResult.getPeriodStart(),
						queryResult.getPeriodEnd());
				timelogDto.setTimesheetPeriod(timesheetPeriod);
			}

			allTimelogResponseBodyDtos.addAll(timelogResponseBodyDtos);
		}
		FetchBulkContractorTimelogResultBodyDto result = getContractorFetchBulkTimelogResultBodyDto(
				contractorTimesheetAndSettingValidatorResponseBodyDtos, timesheetIds.getFirst(),
				allTimelogResponseBodyDtos);

		// Populate timeslotsMetaData in the contractor DTOs
		populateTimelogsMetaDataInResult(result, timelogsMetaData);

		// Find and set common approvers across all valid timesheets
		ApproverRequestResponseBodyDto commonApprovers = findCommonApprovers(approversMap, timesheetSettingIds);
		if (result.getTimesheetSettingsMetaData() != null) {
			result.getTimesheetSettingsMetaData().setApprovers(commonApprovers);
		}

		return result;
	}

	private static FetchBulkTimelogResultBodyDto getFetchBulkTimelogResultBodyDto(
			List<TimesheetAndSettingValidatorResponseBodyDto> timesheetAndSettingValidatorResponseBodyDtos,
			Integer firstTimesheetId, List<DayTimelogQueryResultDto> dayTimelogQueryResultDtos) {
		TimesheetAndSettingValidatorResponseBodyDto primaryContractorTimesheetAndSettingValidatorResponseBodyDto = new TimesheetAndSettingValidatorResponseBodyDto();
		for (TimesheetAndSettingValidatorResponseBodyDto dto : timesheetAndSettingValidatorResponseBodyDtos) {
			if (dto.getTimesheetId().equals(firstTimesheetId)) {
				primaryContractorTimesheetAndSettingValidatorResponseBodyDto = dto;
				break;
			}
		}
		FetchBulkTimelogResultBodyDto fetchBulkTimelogResultBodyDto = new FetchBulkTimelogResultBodyDto();
		fetchBulkTimelogResultBodyDto
			.setTimesheetSettingsMetaData(primaryContractorTimesheetAndSettingValidatorResponseBodyDto);
		fetchBulkTimelogResultBodyDto.setContractorsLogData(dayTimelogQueryResultDtos);
		return fetchBulkTimelogResultBodyDto;
	}

	private static FetchBulkContractorTimelogResultBodyDto getContractorFetchBulkTimelogResultBodyDto(
			List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorTimesheetAndSettingValidatorResponseBodyDto,
			Integer firstTimesheetId, List<TimelogResponseBodyDto> timeLogs) {
		// Find the DTO that matches the first timesheet ID (same logic as deal endpoint)
		ContractorTimesheetAndSettingValidatorResponseBodyDto primaryContractorTimesheetAndSettingValidatorResponseBodyDto = new ContractorTimesheetAndSettingValidatorResponseBodyDto();
		for (ContractorTimesheetAndSettingValidatorResponseBodyDto dto : contractorTimesheetAndSettingValidatorResponseBodyDto) {
			if (dto.getTimesheetId().equals(firstTimesheetId)) {
				primaryContractorTimesheetAndSettingValidatorResponseBodyDto = dto;
				break;
			}
		}
		FetchBulkContractorTimelogResultBodyDto fetchBulkContractorTimelogResultBodyDto = new FetchBulkContractorTimelogResultBodyDto();
		fetchBulkContractorTimelogResultBodyDto
			.setTimesheetSettingsMetaData(primaryContractorTimesheetAndSettingValidatorResponseBodyDto);
		fetchBulkContractorTimelogResultBodyDto.setTimeLogs(timeLogs);
		return fetchBulkContractorTimelogResultBodyDto;
	}

	private void populateTimelogsMetaDataInResult(FetchBulkContractorTimelogResultBodyDto result,
			List<TimelogsMetaDataDto> timelogsMetaData) {
		// Set timeslotsMetaData in the timesheetSettingsMetaData
		if (result.getTimesheetSettingsMetaData() != null) {
			result.getTimesheetSettingsMetaData().setTimelogsMetaData(timelogsMetaData);
		}
	}

	private ApproverRequestResponseBodyDto findCommonApprovers(Map<Integer, List<TimesheetApprover>> approversMap,
			Set<Integer> timesheetSettingIds) {
		if (timesheetSettingIds.isEmpty()) {
			return null;
		}

		// Convert each timesheet setting's approvers to a set of unique identifiers
		// Using entityId + userTypeId as the unique identifier for an approver
		Set<String> commonApproverKeys = null;
		Map<String, TimesheetApprover> approverKeyToEntity = new HashMap<>();

		for (Integer timesheetSettingId : timesheetSettingIds) {
			List<TimesheetApprover> currentApprovers = approversMap.get(timesheetSettingId);
			if (currentApprovers == null || currentApprovers.isEmpty()) {
				// If any timesheet setting has no approvers, there are no common
				// approvers
				return null;
			}

			// Create set of approver keys for current timesheet setting
			Set<String> currentApproverKeys = new HashSet<>();
			for (TimesheetApprover approver : currentApprovers) {
				String key = approver.getEntityId() + "_" + approver.getUserTypeId();
				currentApproverKeys.add(key);
				approverKeyToEntity.put(key, approver);
			}

			if (commonApproverKeys == null) {
				// First timesheet setting - initialize common approvers
				commonApproverKeys = new HashSet<>(currentApproverKeys);
			}
			else {
				// Find intersection with current timesheet setting
				commonApproverKeys.retainAll(currentApproverKeys);
			}

			// If no common approvers remain, return null early
			if (commonApproverKeys.isEmpty()) {
				return null;
			}
		}

		// Convert common approver keys back to TimesheetApprover entities
		if (commonApproverKeys == null || commonApproverKeys.isEmpty()) {
			return null;
		}

		List<TimesheetApprover> commonApprovers = commonApproverKeys.stream().map(approverKeyToEntity::get).toList();

		// Convert common approvers to DTO using existing mapper
		return this.timeLogMapper.mapApprovers(commonApprovers);
	}

	@Override
	@Transactional(readOnly = true)
	public PortalTimesheetResponseBodyDto getPortalTimeLogs(Integer timesheetId) {

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		return switch (principal.getPrincipalType()) {
			case CONTRACTOR -> this.getPortalTimeLogsForContractor(timesheetId, principal);
			case CONTACT -> this.getPortalTimeLogsForContact(timesheetId, principal);
			default -> throw new io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException(
					"Only contractors and contacts can access portal timesheets");
		};
	}

	/**
	 * Get timesheet with time logs for CONTRACTOR persona Returns contractor-specific
	 * data
	 */
	private PortalTimesheetResponseBodyDto getPortalTimeLogsForContractor(Integer timesheetId,
			AuthPrincipal principal) {

		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
		Integer contractorId = contractorPrincipal.getCandidateId();
		Integer accountId = contractorPrincipal.getOrganizationIdentifier();

		// Validate timesheet belongs to this contractor portal
		Candidate candidate = this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId);
		if (candidate == null || !Objects.equals(candidate.getId(), contractorId)) {
			throw new UnauthorizedAccessException("Contractor can only access their own timesheets");
		}

		return this.fetchContractorTimesheetWithTimeLogs(timesheetId, accountId, UserTypeEnum.CONTRACTOR.getId());
	}

	/**
	 * Get timesheet with time logs for CONTACT persona Validates portal access control:
	 * 1. Validates job exists and portal is enabled 2. Validates clientId matches job's
	 * primary contactId or secondary contact 3. Validates JobTimesheetAccess record
	 * exists and checks EDIT_TIMESHEET permission
	 */
	private PortalTimesheetResponseBodyDto getPortalTimeLogsForContact(Integer timesheetId, AuthPrincipal principal) {

		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer accountId = contactPrincipal.getOrganizationIdentifier();

		return this.fetchContractorTimesheetWithTimeLogs(timesheetId, accountId, UserTypeEnum.COMPANY_CONTACT.getId());
	}

	/**
	 * Fetches timesheet with time logs Called after persona-specific access validation
	 */
	private PortalTimesheetResponseBodyDto fetchContractorTimesheetWithTimeLogs(Integer timesheetId, Integer accountId,
			Integer userTypeId) {

		// Validate timesheet exists
		Timesheet timesheet = validateTimesheetExists(timesheetId, accountId);

		// Get timesheet log query result
		TimesheetLogQueryResultDto timesheetLogQueryResultDto = this.timeLogRepository
			.getTimeLogByTimesheetId(timesheetId);

		// Get time logs
		List<TimeLog> timeLogs = getTimeLogs(timesheetId);

		// Get work time details (for multiple time entries)
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap = getWorkTimeDetailsMap(
				timesheetLogQueryResultDto, timeLogs);

		// Get timesheet approval
		TimesheetApproval timesheetApproval = getTimesheetApproval(timesheetId);

		// Create base response for contractor
		PortalTimesheetResponseBodyDto responseDto = createContractorBaseResponse(timesheet, timesheetApproval,
				timesheetLogQueryResultDto);

		// Get approval status type ID
		Integer approvalStatusTypeId = timesheetApproval.getTimesheetApprovalStatusTypeId();

		// Set approver name if needed
		setContractorApproverName(responseDto, timesheetLogQueryResultDto, approvalStatusTypeId);

		// Process approval status specific logic based on user type
		if (Objects.equals(userTypeId, UserTypeEnum.COMPANY_CONTACT.getId())) {
			// Client-specific logic (userTypeId = 1)
			processClientApprovalStatusLogic(responseDto, timesheetApproval, timesheet, timesheetLogQueryResultDto);
		}
		else {
			// Contractor-specific logic (userTypeId = 3)
			processContractorApprovalStatusLogic(responseDto, timesheetApproval, timesheet, timesheetLogQueryResultDto);
		}

		// Set time logs with proper filtering and mapping
		setContractorFilteredTimeLogs(responseDto, timeLogs, timesheet, workTimeDetailsMap);

		responseDto.setTemplateWorkDays(timesheetLogQueryResultDto.getTemplateWorkDays());

		return responseDto;
	}

	private PortalTimesheetResponseBodyDto createContractorBaseResponse(Timesheet timesheet,
			TimesheetApproval timesheetApproval, TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		PortalTimesheetResponseBodyDto responseDto = new PortalTimesheetResponseBodyDto();
		responseDto.setTimesheetId(timesheet.getId());
		responseDto.setApprovalStatusId(timesheetApproval.getTimesheetApprovalStatusTypeId());
		responseDto.setApprovedByUserTypeId(timesheetApproval.getUserTypeId());

		// Set timesheet setting details
		responseDto.setCalculateBreakTime(timesheetLogQueryResultDto.getCalculateBreakTime());
		responseDto.setBreakTimeThreshold(timesheetLogQueryResultDto.getBreakTimeThreshold());
		responseDto.setWorkLogType(timesheetLogQueryResultDto.getWorkLogType());
		responseDto.setTimesheetFrequency(timesheetLogQueryResultDto.getTimesheetFrequency());
		responseDto.setTimesheetStartDay(timesheetLogQueryResultDto.getTimesheetStartDay());
		responseDto.setIsRemarkMandatory(timesheetLogQueryResultDto.getIsRemarkMandatory());
		responseDto.setIsUnplannedHoursPayEnabled(timesheetLogQueryResultDto.getIsUnplannedHoursPayEnabled());

		return responseDto;
	}

	private void setContractorApproverName(PortalTimesheetResponseBodyDto responseDto,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto, Integer approvalStatusTypeId) {

		if (timesheetLogQueryResultDto.getUserTypeId().equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto approverDetail = this.userRepository
				.getUserDetails(timesheetLogQueryResultDto.getEntityId());
			if (approverDetail != null) {
				responseDto.setApprovedBy(approverDetail.getName());
			}
		}
		else if (timesheetLogQueryResultDto.getUserTypeId().equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			Map<Integer, ContactNamePhotoQueryResultDto> contactMap = this.contactRepository
				.getContactNamePhotoMap(Set.of(timesheetLogQueryResultDto.getEntityId()));
			ContactNamePhotoQueryResultDto approverDetail = contactMap.get(timesheetLogQueryResultDto.getEntityId());
			if (approverDetail != null) {
				responseDto.setApprovedBy(approverDetail.getName());
			}
		}
		else if (timesheetLogQueryResultDto.getUserTypeId().equals(UserTypeEnum.CONTRACTOR.getId())) {
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorMap = this.candidateRepository
				.getContractorQueryResultMap(Set.of(timesheetLogQueryResultDto.getEntityId()));
			ContractorNamePhotoQueryResultDto approverDetail = contractorMap
				.get(timesheetLogQueryResultDto.getEntityId());
			if (approverDetail != null) {
				responseDto.setApprovedBy(approverDetail.getName());
			}
		}

		// Set approvedByUserId when approval status is SUBMITTED, REJECTED, or APPROVED
		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.SUBMITTED.getId())
				|| Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.REJECTED.getId())
				|| Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.APPROVED.getId())) {
			responseDto.setApprovedByUserId(timesheetLogQueryResultDto.getEntityId());
		}
	}

	private void processContractorApprovalStatusLogic(PortalTimesheetResponseBodyDto responseDto,
			TimesheetApproval timesheetApproval, Timesheet timesheet,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		Integer approvalStatusTypeId = timesheetApproval.getTimesheetApprovalStatusTypeId();

		setWeeklyOvertimeEnabled(responseDto, timesheetLogQueryResultDto.getCustomRules());

		if (isSubmittedApprovedOrRejected(approvalStatusTypeId)) {
			setTimesheetTotalPayBill(responseDto, timesheet.getId(), timesheetLogQueryResultDto);
			setContractorCurrencySymbols(responseDto, timesheetLogQueryResultDto);
			responseDto.setCreatedOn(timesheetApproval.getCreatedOn());
		}

		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.APPROVED.getId())) {
			setContractorApprovedTimesheetDetails(responseDto, timesheetLogQueryResultDto);
		}

		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.REJECTED.getId())) {
			responseDto.setRemark(timesheetApproval.getRemark());
		}
	}

	private void setTimesheetTotalPayBill(PortalTimesheetResponseBodyDto responseDto, Integer timesheetId,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		List<Integer> timesheetIds = List.of(timesheetId);
		List<TimeLogWorkSummaryQueryResultDto> workSummaryResults = getWorkSummaryResults(timesheetIds,
				timesheetLogQueryResultDto.getWorkLogType());

		if (!workSummaryResults.isEmpty()) {
			TimeLogWorkSummaryQueryResultDto workSummary = workSummaryResults.getFirst();
			TimeLogTotalPayBillResponseBodyDto totalPayBill = createTotalPayBillDto(workSummary);
			responseDto.setTimesheetTotalPayBill(totalPayBill);
		}
	}

	private void setContractorCurrencySymbols(PortalTimesheetResponseBodyDto responseDto,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {
		responseDto.setPayCurrencySymbol(timesheetLogQueryResultDto.getPayCurrencySymbol());
		responseDto.setPayCurrencyCode(timesheetLogQueryResultDto.getPayCurrencyCode());
	}

	private void setContractorApprovedTimesheetDetails(PortalTimesheetResponseBodyDto responseDto,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		responseDto.setPayStatusId(timesheetLogQueryResultDto.getPaymentStatusId());
		responseDto.setPayoutPaidOn(timesheetLogQueryResultDto.getPaymentPaidOn());
		responseDto.setPayoutNumber(timesheetLogQueryResultDto.getPayoutNumber());
	}

	private void setWeeklyOvertimeEnabled(PortalTimesheetResponseBodyDto responseDto, List<CustomRule> customRules) {
		responseDto.setIsWeeklyEnabled(hasWeeklyOvertimeRule(customRules));
	}

	private void setContractorFilteredTimeLogs(PortalTimesheetResponseBodyDto responseDto, List<TimeLog> timeLogs,
			Timesheet timesheet, Map<Integer, List<WorkTimeDetailResponseBodyDto>> workTimeDetailsMap) {

		String formattedPeriod = formatTimesheetPeriod(timesheet.getPeriodStart(), timesheet.getPeriodEnd());

		List<TimeLogResponseBodyDto> mappedTimeLogs = timeLogs.stream()
			.map((log) -> mapTimeLogWithBreakIntervals(log, formattedPeriod, workTimeDetailsMap))
			.toList();

		responseDto.setTimeLogs(mappedTimeLogs);
	}

	private void processClientApprovalStatusLogic(PortalTimesheetResponseBodyDto responseDto,
			TimesheetApproval timesheetApproval, Timesheet timesheet,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		Integer approvalStatusTypeId = timesheetApproval.getTimesheetApprovalStatusTypeId();

		// Set approvers for all client requests regardless of status
		setClientApprovers(responseDto, timesheetLogQueryResultDto.getTimesheetSettingId());

		// Set bill currency symbol and code for client
		responseDto.setBillCurrencySymbol(timesheetLogQueryResultDto.getBillCurrencySymbol());
		responseDto.setBillCurrencyCode(timesheetLogQueryResultDto.getBillCurrencyCode());

		setWeeklyOvertimeEnabled(responseDto, timesheetLogQueryResultDto.getCustomRules());

		if (isSubmittedApprovedOrRejected(approvalStatusTypeId)) {
			setTimesheetTotalPayBill(responseDto, timesheet.getId(), timesheetLogQueryResultDto);
			responseDto.setCreatedOn(timesheetApproval.getCreatedOn());
		}

		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.APPROVED.getId())) {
			setClientApprovedTimesheetDetails(responseDto, timesheet, timesheetLogQueryResultDto);
		}

		if (Objects.equals(approvalStatusTypeId, ApprovalStatusEnum.REJECTED.getId())) {
			responseDto.setRemark(timesheetApproval.getRemark());
		}
	}

	private void setClientApprovedTimesheetDetails(PortalTimesheetResponseBodyDto responseDto, Timesheet timesheet,
			TimesheetLogQueryResultDto timesheetLogQueryResultDto) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// Get invoice data
		TimesheetInvoice timesheetInvoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheet.getId(),
				accountId);

		if (timesheetInvoice != null) {
			// Set bill status
			responseDto.setBillStatusId(timesheetInvoice.getBillingStatusId());

			// Set invoice details if invoice exists
			if (timesheetInvoice.getInvoiceId() != null) {
				// Get invoice details
				Optional<Invoice> invoice = this.invoicesJpaRepository.findById(timesheetInvoice.getInvoiceId());
				if (invoice.isPresent()) {
					responseDto.setInvoiceNumber(invoice.get().getInvoiceIdNumber());
					responseDto.setInvoiceCreatedOn(invoice.get().getCreatedOn());
				}
			}
		}

		// Set invoice status ID from query result
		if (timesheetLogQueryResultDto.getInvoiceStatus() != null) {
			responseDto.setInvoiceStatusId(timesheetLogQueryResultDto.getInvoiceStatus().getId());
		}
		else {
			responseDto.setInvoiceStatusId(null);
		}

	}

	/**
	 * Set client approvers (agencyIds and clientIds) using existing mapper
	 */
	private void setClientApprovers(PortalTimesheetResponseBodyDto responseDto, Integer timesheetSettingId) {
		List<TimesheetApprover> approvers = this.timesheetApproverRepository
			.findByTimesheetSettingId(timesheetSettingId);
		responseDto.setApprovers(this.timeLogMapper.mapApprovers(approvers));
	}

}
