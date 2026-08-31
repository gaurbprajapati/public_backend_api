package io.recruitcrm.microservice.timesheet.services.timesheet;

import io.recruitcrm.aws.aurora.annotation.ReaderRouteGlobalConsistency;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.DayTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprovalStatusTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.WorkDayEnum;
import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.entity.model.Deal;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
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
import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ApproverResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobContractorPairDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAccessControlResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetWorkSummaryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateJobTimesheetAccessControlRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetForMigrationDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobSearchResponseDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CompanySearchResponseDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealSearchResponseDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CompanySearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto;
import io.recruitcrm.microservice.timesheet.helpers.auth.EntityAccessValidator;
import io.recruitcrm.microservice.timesheet.helpers.auth.PrincipalEntityExtractor;
import io.recruitcrm.microservice.timesheet.dao.assigned_candidate.AssignCandidateJobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.deal.DealJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job.JobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job_timesheet_access.JobTimesheetAccessJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverDetailResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.approver.TimesheetApproversResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.ReminderNotificationEventType;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsParameterDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import io.recruitcrm.microservice.timesheet.helpers.FetchUserAndContactUserIds;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.mapper.CustomTimeSheetMapper;
import io.recruitcrm.microservice.timesheet.mapper.JobTimesheetAccessMapper;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetMapper;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetStatusHistoryMapper;
import io.recruitcrm.microservice.timesheet.repositories.candidate.CandidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogBreakIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.TimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService;
import io.recruitcrm.microservice.timesheet.services.user.IUserTimezoneService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimesheetService implements ITimesheetService {

	final AuthHolder auth;

	final TimesheetRepository timesheetRepository;

	final TimesheetJpaRepository timesheetJpaRepository;

	final TimeLogRepository timeLogRepository;

	final TimeLogBreakIntervalRepository timeLogBreakIntervalRepository;

	final TimeLogIntervalRepository timeLogIntervalRepository;

	final DealJpaRepository dealJpaRepository;

	final TimesheetSettingRepository timesheetSettingRepository;

	final AssignCandidateJobJpaRepository assignCandidateJobJpaRepository;

	final JobJpaRepository jobJpaRepository;

	final TimesheetApprovalRepository timesheetApprovalRepository;

	final TimesheetApproverRepository timesheetApproverRepository;

	private static final Integer DAY_IN_SECONDS = 86400;

	final TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	final TimeLogJpaRepository timeLogJpaRepository;

	final TimesheetMapper timesheetMapper;

	final TimesheetStatusHistoryMapper timesheetStatusHistoryMapper;

	final TimesheetInvoiceRepository timesheetInvoiceRepository;

	final UserRepository userRepository;

	final ContactRepository contactRepository;

	final CandidateRepository candidateRepository;

	final FetchUserAndContactUserIds fetchUserAndContactUserIds;

	final CustomTimeSheetMapper customTimeSheetMapper;

	final AccessControlChecker contractStaffingAccessControlChecker;

	final EntityAccessValidator entityAccessValidator;

	final PortalAccessControlService portalAccessControlService;

	final JobTimesheetAccessJpaRepository jobTimesheetAccessJpaRepository;

	final JobTimesheetAccessMapper jobTimesheetAccessMapper;

	final PrincipalEntityExtractor principalEntityExtractor;

	final io.recruitcrm.microservice.timesheet.services.search.TimesheetSearchService timesheetSearchService;

	final IUserTimezoneService userTimezoneService;

	final KafkaProducerHelper kafkaProducerHelper;

	final ITimesheetService selfReference;

	public TimesheetService(TimesheetRepository timesheetRepository, TimesheetJpaRepository timesheetJpaRepository,
			TimeLogRepository timeLogRepository, TimeLogBreakIntervalRepository timeLogBreakIntervalRepository,
			TimeLogIntervalRepository timeLogIntervalRepository, DealJpaRepository dealJpaRepository,
			TimesheetSettingRepository timesheetSettingRepository,
			AssignCandidateJobJpaRepository assignCandidateJobJpaRepository, JobJpaRepository jobJpaRepository,
			TimesheetApprovalRepository timesheetApprovalRepository,
			TimesheetApproverRepository timesheetApproverRepository,
			TimesheetApprovalJpaRepository timesheetApprovalJpaRepository, TimeLogJpaRepository timeLogJpaRepository,
			TimesheetMapper timesheetMapper, TimesheetStatusHistoryMapper timesheetStatusHistoryMapper,
			TimesheetInvoiceRepository timesheetInvoiceRepository, UserRepository userRepository,
			ContactRepository contactRepository, FetchUserAndContactUserIds fetchUserAndContactUserIds,
			CandidateRepository candidateRepository, CustomTimeSheetMapper customTimeSheetMapper, AuthHolder auth,
			AccessControlChecker contractStaffingAccessControlChecker,
			JobTimesheetAccessJpaRepository jobTimesheetAccessJpaRepository,
			JobTimesheetAccessMapper jobTimesheetAccessMapper, EntityAccessValidator entityAccessValidator,
			PortalAccessControlService portalAccessControlService, PrincipalEntityExtractor principalEntityExtractor,
			io.recruitcrm.microservice.timesheet.services.search.TimesheetSearchService timesheetSearchService,
			IUserTimezoneService userTimezoneService, KafkaProducerHelper kafkaProducerHelper,
			@Lazy ITimesheetService selfReference) {
		this.timesheetRepository = timesheetRepository;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.timeLogRepository = timeLogRepository;
		this.timeLogBreakIntervalRepository = timeLogBreakIntervalRepository;
		this.timeLogIntervalRepository = timeLogIntervalRepository;
		this.timesheetSettingRepository = timesheetSettingRepository;
		this.assignCandidateJobJpaRepository = assignCandidateJobJpaRepository;
		this.jobJpaRepository = jobJpaRepository;
		this.timesheetApprovalRepository = timesheetApprovalRepository;
		this.timesheetApproverRepository = timesheetApproverRepository;
		this.timesheetApprovalJpaRepository = timesheetApprovalJpaRepository;
		this.timeLogJpaRepository = timeLogJpaRepository;
		this.dealJpaRepository = dealJpaRepository;
		this.timesheetMapper = timesheetMapper;
		this.timesheetStatusHistoryMapper = timesheetStatusHistoryMapper;
		this.timesheetInvoiceRepository = timesheetInvoiceRepository;
		this.userRepository = userRepository;
		this.contactRepository = contactRepository;
		this.fetchUserAndContactUserIds = fetchUserAndContactUserIds;
		this.candidateRepository = candidateRepository;
		this.customTimeSheetMapper = customTimeSheetMapper;
		this.auth = auth;
		this.contractStaffingAccessControlChecker = contractStaffingAccessControlChecker;
		this.jobTimesheetAccessMapper = jobTimesheetAccessMapper;
		this.entityAccessValidator = entityAccessValidator;
		this.portalAccessControlService = portalAccessControlService;
		this.jobTimesheetAccessJpaRepository = jobTimesheetAccessJpaRepository;
		this.principalEntityExtractor = principalEntityExtractor;
		this.timesheetSearchService = timesheetSearchService;
		this.userTimezoneService = userTimezoneService;
		this.kafkaProducerHelper = kafkaProducerHelper;
		this.selfReference = selfReference;
	}

	@Override
	@Transactional
	@WriterRoute
	public void createTimesheets(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates) {
		this.selfReference.createTimesheets(jobId, contractorIds, timesheetDates, true);
	}

	@Override
	@Transactional
	@WriterRoute
	public List<Integer> createTimesheets(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates, boolean publishTimesheetCreatedReminderEvent) {

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String performerDisplayName = principal.getFullName();

		return switch (principal.getPrincipalType()) {
			case USER -> this.createTimesheetsForUser(jobId, contractorIds, timesheetDates, performerDisplayName,
					publishTimesheetCreatedReminderEvent);
			case CONTRACTOR -> this.createTimesheetsForContractor(jobId, contractorIds, timesheetDates, principal,
					publishTimesheetCreatedReminderEvent);
			case CONTACT -> this.createTimesheetsForContact(jobId, contractorIds, timesheetDates, principal,
					performerDisplayName, publishTimesheetCreatedReminderEvent);
			default -> throw new UnauthorizedAccessException("Unknown persona type");
		};
	}

	@Override
	@Transactional
	@WriterRoute
	public void createBulkTimesheetsForMultipleJobs(CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto) {
		List<JobContractorPairDto> jobContractorPairs = requestDto.getJobContractorPairs();
		List<CreateTimesheetRequestBodyDto> timesheetDates = requestDto.getTimesheetDates();

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		TimesheetCreatedReminderAuthSnapshot reminderAuth = this.resolveTimesheetCreatedReminderAuthSnapshot(principal);

		List<Integer> aggregatedCreatedTimesheetIds = new ArrayList<>();
		for (JobContractorPairDto jobContractorPair : jobContractorPairs) {
			Integer jobId = jobContractorPair.getJobId();
			List<Integer> contractorIds = jobContractorPair.getContractorIds();
			aggregatedCreatedTimesheetIds
				.addAll(this.selfReference.createTimesheets(jobId, contractorIds, timesheetDates, false));
		}
		this.publishTimesheetCreatedReminderEventsIfApplicable(reminderAuth.userTypeId(), reminderAuth.accountId(),
				aggregatedCreatedTimesheetIds, reminderAuth.performerDisplayName());
	}

	private TimesheetCreatedReminderAuthSnapshot resolveTimesheetCreatedReminderAuthSnapshot(AuthPrincipal principal) {
		return switch (principal.getPrincipalType()) {
			case USER -> new TimesheetCreatedReminderAuthSnapshot(AccountUserEnum.USERTYPEID.getId(),
					this.auth.getAuthenticationPrincipalOrganizationIdentifier(), principal.getFullName());
			case CONTRACTOR -> new TimesheetCreatedReminderAuthSnapshot(UserTypeEnum.CONTRACTOR.getId(),
					((ContractorPrincipal) principal).getOrganizationIdentifier(), null);
			case CONTACT -> new TimesheetCreatedReminderAuthSnapshot(UserTypeEnum.COMPANY_CONTACT.getId(),
					((ContactPrincipal) principal).getOrganizationIdentifier(), principal.getFullName());
			default -> throw new UnauthorizedAccessException("Unknown persona type");
		};
	}

	private List<Integer> createTimesheetsForUser(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates, String performerDisplayName,
			boolean publishTimesheetCreatedReminderEvent) {

		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.CREATE_TIMESHEET);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(-1);
		// No specific timesheet data needed for create permission

		this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();
		UserDetailsParameterDto userDetails = new UserDetailsParameterDto(userId, accountId, userTypeId);

		// Perform common timesheet creation logic
		return this.performTimesheetCreation(jobId, contractorIds, timesheetDates, userDetails, performerDisplayName,
				publishTimesheetCreatedReminderEvent);
	}

	/**
	 * Create timesheets for CONTRACTOR persona Validates contractor can only create
	 * timesheets for themselves
	 */
	private List<Integer> createTimesheetsForContractor(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates, AuthPrincipal principal,
			boolean publishTimesheetCreatedReminderEvent) {

		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
		Integer contractorId = contractorPrincipal.getCandidateId();
		Integer accountId = contractorPrincipal.getOrganizationIdentifier();
		Integer userId = contractorId;
		Integer userTypeId = UserTypeEnum.CONTRACTOR.getId();
		UserDetailsParameterDto userDetails = new UserDetailsParameterDto(userId, accountId, userTypeId);

		// Perform common timesheet creation logic (contractor-created reminders omit
		// performer in payload path via shouldPublishTimesheetCreatedReminder)
		return this.performTimesheetCreation(jobId, contractorIds, timesheetDates, userDetails, null,
				publishTimesheetCreatedReminderEvent);
	}

	/**
	 * Create timesheets for CONTACT persona Validates portal access control: 1. Validates
	 * job exists and portal is enabled 2. Validates clientId matches job's primary
	 * contactId or secondary contact 3. Validates JobTimesheetAccess record exists and
	 * checks CREATE_TIMESHEET permission
	 */
	private List<Integer> createTimesheetsForContact(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates, AuthPrincipal principal, String performerDisplayName,
			boolean publishTimesheetCreatedReminderEvent) {

		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer clientId = contactPrincipal.getContactId();
		Integer accountId = contactPrincipal.getOrganizationIdentifier();
		Integer userId = contactPrincipal.getContactId();
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		// Step 1: Validate portal access control with provided jobId and get permissions
		// This will throw exceptions if validation fails (job not found, portal not
		// enabled,
		// clientId mismatch, or access record not found)
		PortalTimesheetPermissionDto permissions = this.portalAccessControlService.validatePortalAccessControl(jobId,
				clientId);

		// Step 2: Check CREATE_TIMESHEET permission specifically
		if (permissions.getCanCreate() == null || permissions.getCanCreate() != 1) {
			throw new UnauthorizedAccessException("Unauthorized access for create timesheet");
		}

		UserDetailsParameterDto userDetails = new UserDetailsParameterDto(userId, accountId, userTypeId);

		// Perform common timesheet creation logic
		return this.performTimesheetCreation(jobId, contractorIds, timesheetDates, userDetails, performerDisplayName,
				publishTimesheetCreatedReminderEvent);
	}

	/**
	 * Common timesheet creation logic Called after persona-specific access control
	 * validation
	 */
	private List<Integer> performTimesheetCreation(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates, UserDetailsParameterDto userDetails,
			String performerDisplayName, boolean publishTimesheetCreatedReminderEvent) {

		Integer accountId = userDetails.getAccountId();
		Integer userTypeId = userDetails.getUserTypeId();

		// Validate assignment and get timesheet settings
		List<TimesheetSetting> timesheetSettings = validateAssignmentAndGetSettings(jobId, contractorIds, accountId);
		if (Boolean.FALSE.equals(this.timesheetSettingRepository.validateTimesheetSettingsConsistency(contractorIds,
				contractorIds.size()))) {
			throw new ValidationErrorException("Timesheet settings are inconsistent for the provided contractor IDs.");
		}

		TimesheetSetting timesheetSetting = timesheetSettings.getFirst();
		Integer jobStartDate = timesheetSetting.getJobStartDate();
		Integer jobEndDate = timesheetSetting.getJobEndDate() + (DAY_IN_SECONDS - 1);
		Integer timesheetStartDate = timesheetSetting.getTimesheetStartDay();
		Integer timesheetFrequency = timesheetSetting.getTimesheetFrequency();

		// Build a map of timesheetSettingId -> workDays for each contractor's timesheet
		// setting
		Map<Integer, List<Integer>> timesheetSettingIdToWorkDaysMap = timesheetSettings.stream()
			.collect(Collectors.toMap(TimesheetSetting::getId,
					(setting) -> getWorkDaysforTimsheetSettings(setting.getTemplateWorkDay())));

		List<Timesheet> allCreatedTimesheetsForReminder = new ArrayList<>();
		for (CreateTimesheetRequestBodyDto dto : timesheetDates) {
			Integer startDate = dto.getStartDate();
			Integer endDate = dto.getEndDate();

			// Validate date ranges
			validateDateRanges(startDate, endDate, jobStartDate, jobEndDate, timesheetStartDate, timesheetFrequency);

			// Generate and validate time log dates
			List<Integer> timeLogDates = generateDateRangeWithStartAndEndDates(startDate, endDate);
			validateTimeLogDates(timeLogDates, timesheetFrequency);

			// Validate all time log dates in a single query
			validateNoDuplicateTimesheets(timeLogDates, accountId, jobId, contractorIds);

			// Create timesheets and related records in bulk
			allCreatedTimesheetsForReminder.addAll(this.createTimesheetAndRelatedRecords(userDetails, timeLogDates,
					timesheetSettings, timesheetSettingIdToWorkDaysMap, startDate, endDate));
		}
		List<Integer> createdTimesheetIds = allCreatedTimesheetsForReminder.stream()
			.map(Timesheet::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(ArrayList::new));
		if (publishTimesheetCreatedReminderEvent) {
			this.publishTimesheetCreatedReminderEventsIfApplicable(userTypeId, accountId, createdTimesheetIds,
					performerDisplayName);
		}
		return createdTimesheetIds;
	}

	private List<TimesheetSetting> validateAssignmentAndGetSettings(Integer jobId, List<Integer> contractorIds,
			Integer accountId) {
		List<AssignCandidateJob> assignCandidateJobs = this.assignCandidateJobJpaRepository
			.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, accountId);
		// Check if all contractorIds have corresponding records
		if (assignCandidateJobs.size() != contractorIds.size()) {
			throw new ResourceNotFoundException(
					"Some assignments are missing for Job Id: " + jobId + " and contractor Ids: " + contractorIds);
		}

		// Extract jobId and contractorId pairs
		List<Integer> contractorIdsFromJobs = assignCandidateJobs.stream()
			.map(AssignCandidateJob::getCandidateId)
			.toList();

		// Bulk fetch TimesheetSettings
		List<TimesheetSetting> timesheetSettings = this.timesheetSettingRepository
			.findLatestTimesheetSettingsByJobIdAndContractorIds(jobId, contractorIdsFromJobs);

		// Validate that all TimesheetSettings are fetched
		if (timesheetSettings.size() != contractorIds.size()) {
			throw new ResourceNotFoundException("Some TimesheetSettings are missing for Job Id: " + jobId
					+ " and contractor Ids: " + contractorIds);
		}

		return timesheetSettings;
	}

	private void validateDateRanges(Integer startDate, Integer endDate, Integer jobStartDate, Integer jobEndDate,
			Integer timesheetStartDate, Integer timesheetFrequency) {
		Integer timesheetEndDate = 0;
		// Basic date range validation
		if (startDate > endDate) {
			throw new ValidationErrorException("Start date must be before end date");
		}

		// Job date range validation
		if (endDate < jobStartDate || startDate < jobStartDate || startDate > jobEndDate || endDate > jobEndDate) {
			throw new ValidationErrorException(
					"Start date and end date must be between Job Start Date and Job End Date");
		}

		// Check if the frequency is Monthly
		if (timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId())) {
			// Check if Timesheet Start Date is Last Day of the Month
			if (timesheetStartDate.equals(WorkDayEnum.LAST_DAY_OF_MONTH.getId())) {
				// Set the Timesheet Start Date according to the given Month
				timesheetStartDate = setTimesheetStartDateForMonthly(startDate);
			}
			// Set the Timesheet End Date according to the given Month
			timesheetEndDate = setTimesheetEndDateForMonthly(endDate);
		}

		// Timesheet end day validation
		Integer endDay = timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId())
				? getDateTypeId(endDate) : getDayTypeId(endDate);

		LocalDate endDateWithOutTime = Instant.ofEpochSecond(endDate).atZone(ZoneOffset.UTC).toLocalDate();

		LocalDate jobEndDateWithOutTime = Instant.ofEpochSecond(jobEndDate).atZone(ZoneOffset.UTC).toLocalDate();

		boolean isValidEndDate = timesheetStartDate == null
				|| (timesheetStartDate != 1
						&& (endDay.equals(timesheetStartDate - 1) || endDay.equals(timesheetEndDate - 1)))
				|| (timesheetStartDate == 1 && isWeeklyOrBiweekly(timesheetFrequency)
						&& endDay.equals(WorkDayEnum.SUNDAY.getId()))
				|| (timesheetStartDate == 1 && isMonthly(timesheetFrequency) && endDay.equals(timesheetEndDate))
				|| endDateWithOutTime.equals(jobEndDateWithOutTime);

		if (!isValidEndDate) {
			throw new ValidationErrorException(
					"End date must be One Day Before the Timesheet Start Day or Job End Date");
		}
	}

	private static Integer setTimesheetStartDateForMonthly(Integer startDate) {
		// Convert Unix timestamp to LocalDateTime
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startDate), ZoneOffset.UTC);

		// Extract YearMonth
		YearMonth yearMonth = YearMonth.from(dateTime);

		// Get last day of the month
		return yearMonth.lengthOfMonth();
	}

	private static Integer setTimesheetEndDateForMonthly(Integer endDate) {
		// Get same day next month (or clipped to last valid day of that month)
		LocalDateTime endDayMonth = LocalDateTime.ofInstant(Instant.ofEpochSecond(endDate), ZoneOffset.UTC);

		// Extract YearMonth
		YearMonth endDayYearMonth = YearMonth.from(endDayMonth);

		return endDayYearMonth.lengthOfMonth();
	}

	private boolean isWeeklyOrBiweekly(Integer timesheetFrequency) {
		return timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId())
				|| timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId());
	}

	private boolean isMonthly(Integer timesheetFrequency) {
		return timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId());
	}

	private void validateTimeLogDates(List<Integer> timeLogDates, Integer timesheetFrequency) {
		if (timeLogDates.size() > 90) {
			throw new ValidationErrorException("Time Log Dates are more than 90 Days");
		}

		if (isWeeklyOrBiweekly(timesheetFrequency)) {
			int maxDays = timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId()) ? 7 : 14;
			if (timeLogDates.size() > maxDays) {
				throw new ValidationErrorException(String.format("%s Time Logs must be %d or less",
						timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId()) ? "Weekly"
								: "BiWeekly",
						maxDays));
			}
		}
		else if (isMonthly(timesheetFrequency) && timeLogDates.size() > 31) {
			throw new ValidationErrorException("Monthly Time Logs must be 31 or less");
		}
	}

	private void validateNoDuplicateTimesheets(List<Integer> timeLogDates, Integer accountId, Integer jobId,
			List<Integer> contractorIds) {
		Boolean timesheetsExist = this.timesheetRepository.validateTimesheetsExist(timeLogDates, accountId, jobId,
				contractorIds);
		if (Boolean.TRUE.equals(timesheetsExist)) {
			throw new ValidationErrorException(
					"Timesheet already exists for one or more of the provided time periods.");
		}
	}

	private List<Timesheet> createTimesheetAndRelatedRecords(UserDetailsParameterDto userDetailsParameterDto,
			List<Integer> timeLogDates, List<TimesheetSetting> timesheetSettings,
			Map<Integer, List<Integer>> timesheetSettingIdToWorkDaysMap, Integer startDate, Integer endDate) {

		Integer userId = userDetailsParameterDto.getUserId();
		Integer userTypeId = userDetailsParameterDto.getUserTypeId();
		Integer accountId = userDetailsParameterDto.getAccountId();
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());

		// Create timesheets in bulk
		List<Timesheet> timesheets = generateAndCreateTimesheets(startDate, endDate, userId, userTypeId, accountId,
				currentUNIXTimestamp, timesheetSettings);

		// Create timesheet approvals in bulk
		createTimesheetApprovals(timesheets, userId, userTypeId, currentUNIXTimestamp);

		// Create time logs in bulk with correct workDays for each timesheet
		createTimeLogs(timesheets, timeLogDates, timesheetSettingIdToWorkDaysMap);

		return timesheets;
	}

	private void publishTimesheetCreatedReminderEventsIfApplicable(final Integer createdByUserTypeId,
			final Integer accountId, final List<Integer> timesheetIds, final String performerDisplayName) {
		if (!this.shouldPublishTimesheetCreatedReminder(createdByUserTypeId)) {
			return;
		}
		final ArrayList<Integer> ids = timesheetIds.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(ArrayList::new));
		if (ids.isEmpty()) {
			return;
		}
		final TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), ids,
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED, accountId, createdByUserTypeId,
				UserTypeEnum.CONTRACTOR.getId(), ReminderNotificationEventType.REALTIME, false, true, true,
				performerDisplayName, null);
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload);
	}

	private boolean shouldPublishTimesheetCreatedReminder(final Integer createdByUserTypeId) {
		return Objects.equals(createdByUserTypeId, AccountUserEnum.USERTYPEID.getId())
				|| Objects.equals(createdByUserTypeId, UserTypeEnum.COMPANY_CONTACT.getId());
	}

	private List<Timesheet> generateAndCreateTimesheets(Integer startDate, Integer endDate, Integer userId,
			Integer userTypeId, Integer accountId, Integer currentUNIXTimestamp,
			List<TimesheetSetting> timesheetSettings) {

		List<Timesheet> timesheetList = new ArrayList<>();

		for (TimesheetSetting timesheetSetting : timesheetSettings) {
			Timesheet timesheet = new Timesheet();
			timesheet.setPeriodStart(startDate);
			timesheet.setPeriodEnd(endDate);
			timesheet.setAddedBy(userId);
			timesheet.setAddedOn(currentUNIXTimestamp);
			timesheet.setUpdatedBy(userId);
			timesheet.setAddedByUserTypeId(userTypeId);
			timesheet.setUpdatedByUserTypeId(userTypeId);
			timesheet.setUpdatedOn(currentUNIXTimestamp);
			timesheet.setAccountId(accountId);
			timesheet.setTimesheetSettingId(timesheetSetting.getId());
			timesheetList.add(timesheet);
		}
		return this.timesheetRepository.createTimesheets(timesheetList);
	}

	private void createTimesheetApprovals(List<Timesheet> timesheets, Integer userId, Integer userTypeId,
			Integer currentUNIXTimestamp) {
		List<TimesheetApproval> timesheetApprovals = timesheets.stream().map((timesheet) -> {
			TimesheetApproval approval = new TimesheetApproval();
			approval.setTimesheetId(timesheet.getId());
			approval.setUserTypeId(userTypeId);
			approval.setEntityId(userId);
			approval.setTimesheetApprovalStatusTypeId(TimesheetApprovalStatusTypeEnum.OPEN.getId());
			approval.setCreatedOn(currentUNIXTimestamp);
			approval.setRemark(null);
			return approval;
		}).toList();
		this.timesheetApprovalRepository.createBulkTimesheetApprovals(timesheetApprovals);
	}

	private void createTimeLogs(List<Timesheet> timesheets, List<Integer> allTimeLogDates,
			Map<Integer, List<Integer>> timesheetSettingIdToWorkDaysMap) {
		Set<Integer> validDates = new HashSet<>(allTimeLogDates); // For fast lookup
		List<TimeLog> timeLogs = new ArrayList<>();
		for (Timesheet timesheet : timesheets) {
			Integer timesheetSettingId = timesheet.getTimesheetSettingId();
			List<Integer> workDays = timesheetSettingIdToWorkDaysMap.get(timesheetSettingId);
			if (workDays == null) {
				throw new ResourceNotFoundException("WorkDays not found for timesheetSettingId: " + timesheetSettingId);
			}

			Integer startDate = timesheet.getPeriodStart();
			Integer endDate = timesheet.getPeriodEnd();
			List<Integer> dateRange = generateDateRangeWithStartAndEndDates(startDate, endDate);
			for (Integer date : dateRange) {
				if (validDates.contains(date)) {
					Integer dateTypeId = getDayTypeId(date);
					Integer workDayId = workDays.contains(dateTypeId) ? DayTypeEnum.WORKDAY.getId()
							: DayTypeEnum.DAY_OFF.getId();

					TimeLog timeLog = new TimeLog();
					timeLog.setTimesheetId(timesheet.getId());
					timeLog.setDate(date);
					timeLog.setDayTypeId(workDayId);
					timeLogs.add(timeLog);
				}
			}
		}
		this.timeLogRepository.createBulkTimesheetLogs(timeLogs);
	}

	public List<Integer> generateDateRangeWithStartAndEndDates(Integer startDate, Integer endDate) {
		List<Integer> dateList = new ArrayList<>();
		int current = startDate;

		while (current <= endDate) {
			dateList.add(current);
			current += DAY_IN_SECONDS;
		}

		return dateList;
	}

	@NotNull
	private static Integer getDayTypeId(Integer date) {
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneOffset.UTC);
		DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
		return switch (dayOfWeek) {
			case MONDAY -> WorkDayEnum.MONDAY.getId();
			case TUESDAY -> WorkDayEnum.TUESDAY.getId();
			case WEDNESDAY -> WorkDayEnum.WEDNESDAY.getId();
			case THURSDAY -> WorkDayEnum.THURSDAY.getId();
			case FRIDAY -> WorkDayEnum.FRIDAY.getId();
			case SATURDAY -> WorkDayEnum.SATURDAY.getId();
			case SUNDAY -> WorkDayEnum.SUNDAY.getId();
		};
	}

	@NotNull
	private static Integer getDateTypeId(Integer date) {
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneOffset.UTC);
		return dateTime.getDayOfMonth();
	}

	private List<Integer> getWorkDaysforTimsheetSettings(List<TemplateWorkDay> templateWorkDays) {
		List<Integer> workDays = new ArrayList<>();
		for (TemplateWorkDay templateWorkDay : templateWorkDays) {
			workDays.add(templateWorkDay.getWorkDayId());
		}
		return workDays;
	}

	@Override
	@WriterRoute
	@Transactional
	public void deleteTimesheet(Integer timesheetId) {
		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.DELETE_TIMESHEET);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(timesheetId);
		this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Optional<Timesheet> timesheet = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId);
		if (timesheet.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet", timesheetId);
		}

		TimesheetApproval timesheetApproval = this.timesheetApprovalJpaRepository
			.findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		if (Objects.equals(timesheetApproval.getTimesheetApprovalStatusTypeId(),
				TimesheetApprovalStatusTypeEnum.APPROVED.getId())) {
			throw new ValidationErrorException("Timesheet Status is Approved for the given Timesheet : " + timesheetId);
		}

		List<Integer> timeLogIds = this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(List.of(timesheetId));
		if (!timeLogIds.isEmpty()) {
			this.timeLogIntervalRepository.deleteByTimeLogIntervalIdIn(timeLogIds);
		}

		this.timeLogJpaRepository.deleteByTimesheetId(timesheetId);
		this.timesheetApprovalJpaRepository.deleteByTimesheetId(timesheetId);
		this.timesheetJpaRepository.deleteByIdAndAccountId(timesheetId, accountId);
	}

	@Override
	@Transactional
	@WriterRoute
	public void deleteTimesheets(List<Integer> timesheetIds) {

		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = timesheetIds.stream().map((timesheetId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.DELETE_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);

			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		// Create the bulk request
		BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();

		// Perform bulk permission check
		this.contractStaffingAccessControlChecker.allowsBulk(bulkRequest);

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		this.performTimesheetDeletion(timesheetIds, accountId);
	}

	@Override
	@Transactional
	@WriterRoute
	public void deletePortalTimesheets(Integer timesheetId, Integer jobId) {
		if (timesheetId == null) {
			throw new ValidationErrorException("Timesheet id is required");
		}
		if (jobId == null) {
			throw new ValidationErrorException("Job id is required");
		}

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		switch (principal.getPrincipalType()) {
			case CONTRACTOR -> this.deletePortalTimesheetsForContractor(timesheetId, principal);
			case CONTACT -> this.deletePortalTimesheetsForContact(timesheetId, jobId, principal);
			default ->
				throw new UnauthorizedAccessException("Only contractors and contacts can delete portal timesheets");
		}
	}

	/**
	 * Delete timesheet for CONTRACTOR persona Validates contractor owns the timesheet
	 * before deletion
	 */
	private void deletePortalTimesheetsForContractor(Integer timesheetId, AuthPrincipal principal) {

		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
		Integer accountId = contractorPrincipal.getOrganizationIdentifier();

		// Perform deletion after validation
		this.performTimesheetDeletion(List.of(timesheetId), accountId);
	}

	/**
	 * Delete timesheet for CONTACT persona Validates portal access control: 1. Validates
	 * job exists and portal is enabled 2. Validates clientId matches job's contactId 3.
	 * Validates JobTimesheetAccess record exists and checks DELETE_TIMESHEET permission
	 */
	private void deletePortalTimesheetsForContact(Integer timesheetId, Integer jobId, AuthPrincipal principal) {

		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer clientId = contactPrincipal.getContactId();
		Integer accountId = contactPrincipal.getOrganizationIdentifier();

		// Step 1: Validate portal access control with provided jobId and get permissions
		// This will throw exceptions if validation fails (job not found, portal not
		// enabled,
		// clientId mismatch, or access record not found)
		PortalTimesheetPermissionDto permissions = this.portalAccessControlService.validatePortalAccessControl(jobId,
				clientId);

		// Step 2: Check DELETE_TIMESHEET permission specifically
		if (permissions.getCanDelete() == null || permissions.getCanDelete() != 1) {
			throw new UnauthorizedAccessException(
					"Unauthorized access: DELETE_TIMESHEET permission not granted for job ID: " + jobId
							+ " and client ID: " + clientId);
		}

		// Perform deletion after validation
		this.performTimesheetDeletion(List.of(timesheetId), accountId);
	}

	/**
	 * Performs the actual deletion of timesheets Called after access control validation
	 * (permission checks for USER persona or ownership/company validation for portal
	 * personas)
	 */
	private void performTimesheetDeletion(List<Integer> timesheetIds, Integer accountId) {

		// Bulk fetch all timesheets in a single query instead of N individual queries
		// Using custom repository with EntityManager JPQL instead of JPA repository
		List<Timesheet> timesheets = this.timesheetRepository.findByIdInAndAccountId(timesheetIds, accountId);
		Map<Integer, Timesheet> timesheetMap = timesheets.stream()
			.collect(Collectors.toMap(Timesheet::getId, (timesheet) -> timesheet));

		// Validate all timesheets exist
		for (Integer timesheetId : timesheetIds) {
			if (!timesheetMap.containsKey(timesheetId)) {
				throw new ResourceNotFoundException("Timesheet", timesheetId);
			}
		}

		// Bulk fetch latest approvals for all timesheets in a single query instead of N
		// individual queries
		// Using custom repository with EntityManager JPQL instead of JPA repository
		List<TimesheetApproval> latestApprovals = this.timesheetApprovalRepository
			.findLatestApprovalEntitiesByTimesheetIds(timesheetIds);
		Map<Integer, TimesheetApproval> approvalMap = latestApprovals.stream()
			.collect(Collectors.toMap(TimesheetApproval::getTimesheetId, (approval) -> approval));

		// Collect valid timesheet IDs that can be deleted (not approved)
		List<Integer> validTimesheetIds = new ArrayList<>();
		for (Integer timesheetId : timesheetIds) {
			TimesheetApproval timesheetApproval = approvalMap.get(timesheetId);
			if (timesheetApproval == null) {
				throw new ResourceNotFoundException("TimesheetApproval for timesheet", timesheetId);
			}
			// Skip approved timesheets (status 4)
			if (!Objects.equals(timesheetApproval.getTimesheetApprovalStatusTypeId(),
					TimesheetApprovalStatusTypeEnum.APPROVED.getId())) {
				validTimesheetIds.add(timesheetId);
			}
		}

		if (validTimesheetIds.isEmpty()) {
			throw new ValidationErrorException(
					"Timesheet Status is Approved for the given Timesheets : " + timesheetIds);
		}
		else {
			// Step 1: Fetch all time log IDs for the given timesheet IDs
			List<Integer> timeLogIds = this.timeLogRepository.findTimeLogIdsByTimesheetIdIn(validTimesheetIds);

			// Step 2: Delete time log intervals for those time log IDs (JOOQ bulk delete)
			// This is necessary because JOOQ bulk delete doesn't handle JPA cascading
			if (!timeLogIds.isEmpty()) {
				this.timeLogIntervalRepository.deleteByTimeLogIntervalIdIn(timeLogIds);
			}

			// Step 3: Delete time logs, approvals, and timesheets
			this.timeLogRepository.deleteByTimesheetIdIn(validTimesheetIds);
			this.timesheetApprovalRepository.deleteByTimesheetIdIn(validTimesheetIds);
			this.timesheetRepository.deleteByIdInAndAccountId(validTimesheetIds, accountId);
		}
	}

	@Override
	@Transactional
	@ReaderRouteGlobalConsistency
	public TimesheetStatusHistoryResponseBodyDto getTimesheetStatusHistory(Integer timesheetId) {

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		return switch (principal.getPrincipalType()) {
			case USER -> this.getTimesheetStatusHistoryForUser(timesheetId);
			case CONTRACTOR -> this.getTimesheetStatusHistoryForContractor(timesheetId, principal);
			case CONTACT -> this.getTimesheetStatusHistoryForContact(timesheetId, principal);
			default -> throw new UnauthorizedAccessException("Unknown persona type");
		};
	}

	/**
	 * Get timesheet status history for USER persona Validates role-based permissions and
	 * returns status history
	 */
	private TimesheetStatusHistoryResponseBodyDto getTimesheetStatusHistoryForUser(Integer timesheetId) {

		// need to check access control - vivek

		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.VIEW_TIMESHEET);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(timesheetId);

		this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		return this.fetchAndBuildTimesheetStatusHistory(timesheetId, accountId);
	}

	/**
	 * Get timesheet status history for CONTRACTOR persona Validates contractor owns the
	 * timesheet and returns status history
	 */
	private TimesheetStatusHistoryResponseBodyDto getTimesheetStatusHistoryForContractor(Integer timesheetId,
			AuthPrincipal principal) {

		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
		Integer contractorId = contractorPrincipal.getCandidateId();
		Integer accountId = contractorPrincipal.getOrganizationIdentifier();

		// access control

		io.recruitcrm.entity.model.Candidate candidate = this.timesheetRepository
			.getCandidateLinkedToTimesheet(timesheetId, accountId);
		if (candidate == null || !Objects.equals(candidate.getId(), contractorId)) {
			throw new UnauthorizedAccessException("Contractor can only access their own timesheets status history");
		}

		return this.fetchAndBuildTimesheetStatusHistory(timesheetId, accountId);
	}

	/**
	 * Get timesheet status history for CONTACT persona Validates timesheet belongs to
	 * contact's company and returns status history
	 */
	private TimesheetStatusHistoryResponseBodyDto getTimesheetStatusHistoryForContact(Integer timesheetId,
			AuthPrincipal principal) {

		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer accountId = contactPrincipal.getOrganizationIdentifier();

		return this.fetchAndBuildTimesheetStatusHistory(timesheetId, accountId);
	}

	/**
	 * Fetches and builds timesheet status history Called after persona-specific access
	 * validation
	 */
	private TimesheetStatusHistoryResponseBodyDto fetchAndBuildTimesheetStatusHistory(Integer timesheetId,
			Integer accountId) {
		// Verify timesheet exists
		Optional<Timesheet> timesheet = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId);
		if (timesheet.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet", timesheetId);
		}

		// Get status history
		List<StatusHistoryQueryResultDto> statusHistory = this.timesheetApprovalJpaRepository
			.findByTimesheetIdOrderByIdDesc(timesheetId);
		if (statusHistory == null || statusHistory.isEmpty()) {
			throw new ResourceNotFoundException("Status history for timesheet", timesheetId);
		}
		TimesheetStatusHistoryResponseBodyDto timesheetStatusHistoryResponseBodyDto = new TimesheetStatusHistoryResponseBodyDto();

		List<StatusHistoryResponseBodyDto> statusHistoryResponseBodyDtos = this.timesheetStatusHistoryMapper
			.toTimesheetStatusResultBodyDto(statusHistory);

		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();
		Set<Integer> contractorUserIds = new HashSet<>();
		for (StatusHistoryQueryResultDto dto : statusHistory) {
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getUpdatedByUserTypeId(), dto.getUpdatedById(),
					agencyUserIds, contactUserIds, contractorUserIds);
		}

		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = this.userRepository.getUserDetailsMap(agencyUserIds);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = this.contactRepository
			.getContactNamePhotoMap(contactUserIds);
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = this.candidateRepository
			.getContractorQueryResultMap(contractorUserIds);

		for (int i = 0; i < statusHistoryResponseBodyDtos.size(); i++) {
			StatusHistoryQueryResultDto statusHistoryQueryResultDto = statusHistory.get(i);
			StatusHistoryResponseBodyDto statusHistoryResponseBodyDto = statusHistoryResponseBodyDtos.get(i);
			UpdatedByResponseBodyDto updatedBy = this.buildUpdatedByForStatusHistory(statusHistoryQueryResultDto,
					contractorUsersMap, agencyUsersMap, contactUsersMap);
			if (updatedBy != null) {
				statusHistoryResponseBodyDto.setUpdatedBy(updatedBy);
			}
		}

		timesheetStatusHistoryResponseBodyDto.setTimesheetId(timesheetId);
		timesheetStatusHistoryResponseBodyDto.setStatusHistory(statusHistoryResponseBodyDtos);

		return timesheetStatusHistoryResponseBodyDto;
	}

	@Override
	@Transactional
	@ReaderRouteGlobalConsistency
	public List<TimesheetListResponseBodyDto> getTimesheetsListByDealId(Integer dealId,
			SearchRequestBodyDto searchRequestBodyDto, Pageable pageable) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		isDealValid(dealId, accountId);
		List<ContractorJobQueryResultDto> contractorJobs = this.timesheetRepository.getCommonCandidatesByDealId(dealId);
		List<TimesheetDealListQueryResultDto> timesheetDealListQueryResultDtos = this.timesheetRepository
			.getTimesheetsListByDealId(contractorJobs, accountId, searchRequestBodyDto, pageable);

		if (timesheetDealListQueryResultDtos == null || timesheetDealListQueryResultDtos.isEmpty()) {
			return Collections.emptyList();
		}

		List<Integer> timesheetIds = new ArrayList<>();

		for (TimesheetDealListQueryResultDto timesheetDealListQueryResultDto : timesheetDealListQueryResultDtos) {
			timesheetIds.add(timesheetDealListQueryResultDto.getId());
		}
		// Create bulk permission check request items for timesheets
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = timesheetIds.stream().map((timesheetId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.VIEW_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);

			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		// Create the bulk request
		BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();

		this.contractStaffingAccessControlChecker.allowsBulk(bulkRequest);

		List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos = this.customTimeSheetMapper
			.listTimeSheetRequestToResponseBodyDto(timesheetDealListQueryResultDtos);

		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();
		for (TimesheetDealListQueryResultDto dto : timesheetDealListQueryResultDtos) {
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getAddedByUserTypeId(), dto.getAddedById(),
					agencyUserIds, contactUserIds, new HashSet<>());
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getUpdatedByUserTypeId(), dto.getUpdatedById(),
					agencyUserIds, contactUserIds, new HashSet<>());
		}
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = this.userRepository.getUserDetailsMap(agencyUserIds);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = this.contactRepository
			.getContactNamePhotoMap(contactUserIds);

		timesheetIds = new ArrayList<>();

		setAddedByDeal(timesheetDealListQueryResultDtos, timesheetListResponseBodyDtos, agencyUsersMap, contactUsersMap,
				timesheetIds);

		// Build work summary map from query result (totalTime, totalWorkTime,
		// totalOvertime already from DB)
		Map<Integer, TimesheetWorkSummaryQueryResultDto> workSummaryMap = timesheetDealListQueryResultDtos.stream()
			.collect(Collectors.toMap(TimesheetDealListQueryResultDto::getId,
					(dto) -> new TimesheetWorkSummaryQueryResultDto(dto.getId(),
							(dto.getTotalWorkTime() != null) ? dto.getTotalWorkTime().longValue() : null,
							(dto.getTotalOvertime() != null) ? dto.getTotalOvertime().longValue() : null, null, null,
							(dto.getTotalTime() != null) ? dto.getTotalTime().longValue() : null)));

		List<TimesheetApproverResponseBodyDto> timesheetApproverResponseBodyDtos = this.timesheetApprovalJpaRepository
			.findLatestApprovalsByTimesheetIds(timesheetIds);

		Map<Integer, TimesheetApproverResponseBodyDto> approverMap = timesheetApproverResponseBodyDtos.stream()
			.collect(Collectors.toMap(TimesheetApproverResponseBodyDto::getTimesheetId, (dto) -> dto));

		fillCurrentTimesheetDetailsDeal(timesheetListResponseBodyDtos, workSummaryMap, approverMap, agencyUsersMap,
				contactUsersMap);

		return timesheetListResponseBodyDtos;
	}

	private void fillCurrentTimesheetDetailsDeal(List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos,
			Map<Integer, TimesheetWorkSummaryQueryResultDto> workSummaryMap,
			Map<Integer, TimesheetApproverResponseBodyDto> approverMap,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		for (TimesheetListResponseBodyDto currentTimesheet : timesheetListResponseBodyDtos) {
			Integer timesheetId = currentTimesheet.getId();

			// Set work summary details
			setWorkSummaryDetails(currentTimesheet, workSummaryMap.get(timesheetId));

			// Set approver details
			setApproverDetails(currentTimesheet, approverMap.get(timesheetId), agencyUsersMap, contactUsersMap);
		}
	}

	private void setWorkSummaryDetails(TimesheetListResponseBodyDto currentTimesheet,
			TimesheetWorkSummaryQueryResultDto workSummary) {
		if (workSummary != null) {
			currentTimesheet.setTotalWorkTime(workSummary.getTotalWorkingHours());
			currentTimesheet.setTotalOvertime(workSummary.getTotalOverTimeHours());
			currentTimesheet.setTotalTime(workSummary.getTotalTime());
		}
	}

	private void setApproverDetails(TimesheetListResponseBodyDto currentTimesheet,
			TimesheetApproverResponseBodyDto approver, Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		if (approver != null) {
			currentTimesheet.setTimesheetStatusId(approver.getTimeSheetApprovalStatusId());

			if (approver.getTimeSheetApprovalStatusId() == 4) {
				Integer approverUserTypeId = approver.getUserTypeId();
				Integer approverId = approver.getEntityId();

				ApproverResultBodyDto approverDetails = getApproverDetails(approverUserTypeId, approverId,
						agencyUsersMap, contactUsersMap);
				if (approverDetails != null) {
					currentTimesheet.setApprovedBy(approverDetails);
				}
			}
		}
	}

	private ApproverResultBodyDto getApproverDetails(Integer userTypeId, Integer userId,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(userId);
			if (userDetails != null) {
				return new ApproverResultBodyDto(userId, userDetails.getName(), userDetails.getProfilePic(),
						userTypeId);
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(userId);
			if (contactDetails != null) {
				return new ApproverResultBodyDto(userId, contactDetails.getName(), contactDetails.getProfilePic(),
						userTypeId);
			}
		}
		return null;
	}

	private void setAddedByDeal(List<TimesheetDealListQueryResultDto> timesheetDealListQueryResultDtos,
			List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap, List<Integer> timesheetIds) {
		for (int i = 0; i < timesheetDealListQueryResultDtos.size(); i++) {
			TimesheetDealListQueryResultDto timesheetDealListQueryResultDto = timesheetDealListQueryResultDtos.get(i);
			TimesheetListResponseBodyDto timesheetListResponseBodyDto = timesheetListResponseBodyDtos.get(i);

			AddedByResponseBodyDto addedBy = this.buildAddedByForDeal(timesheetDealListQueryResultDto,
					timesheetListResponseBodyDtos.get(i), agencyUsersMap, contactUsersMap);
			if (addedBy != null) {
				timesheetListResponseBodyDto.setAddedBy(addedBy);
			}

			UpdatedByResponseBodyDto updatedBy = this.buildUpdatedByForDeal(timesheetDealListQueryResultDto,
					timesheetListResponseBodyDtos.get(i), agencyUsersMap, contactUsersMap);
			if (updatedBy != null) {
				timesheetListResponseBodyDto.setUpdatedBy(updatedBy);
			}

			timesheetIds.add(timesheetDealListQueryResultDto.getId());
		}
	}

	public void isDealValid(Integer dealId, Integer accountId) {
		Optional<Deal> deal = this.dealJpaRepository.findByIdAndAccountId(dealId, accountId);
		if (deal.isEmpty()) {
			throw new ResourceNotFoundException("Deal", dealId);
		}
	}

	@Override
	@Transactional
	@ReaderRouteGlobalConsistency
	public List<TimesheetListResponseBodyDto> getTimesheetsListByJobAndContractorId(Integer jobId, Integer contractorId,
			SearchRequestBodyDto searchRequestBodyDto, Pageable pageable) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.VIEW_TIMESHEET);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

		List<TimesheetJobAndContractorListQueryResultDto> timesheetJobAndContractorListQueryResultBodyDtoList = this.timesheetRepository
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, accountId, searchRequestBodyDto, pageable);

		if (timesheetJobAndContractorListQueryResultBodyDtoList == null
				|| timesheetJobAndContractorListQueryResultBodyDtoList.isEmpty()) {
			return Collections.emptyList();
		}

		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(timesheetJobAndContractorListQueryResultBodyDtoList.getFirst().getId());
		// No specific timesheet data needed for create permission

		this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);
		List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos = this.customTimeSheetMapper
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(timesheetJobAndContractorListQueryResultBodyDtoList);

		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();
		for (TimesheetJobAndContractorListQueryResultDto dto : timesheetJobAndContractorListQueryResultBodyDtoList) {
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getAddedByUserTypeId(), dto.getAddedById(),
					agencyUserIds, contactUserIds, new HashSet<>());
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getUpdatedByUserTypeId(), dto.getUpdatedById(),
					agencyUserIds, contactUserIds, new HashSet<>());
		}

		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = this.userRepository.getUserDetailsMap(agencyUserIds);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = this.contactRepository
			.getContactNamePhotoMap(contactUserIds);

		// Contractor and job details are now set by the mapper from query results

		List<Integer> timesheetIds = new ArrayList<>();

		setAddedByJobAndContractor(timesheetJobAndContractorListQueryResultBodyDtoList, timesheetListResponseBodyDtos,
				agencyUsersMap, contactUsersMap, timesheetIds);

		enrichWithInvoiceDetails(timesheetListResponseBodyDtos, timesheetIds, accountId);

		List<TimesheetApproverResponseBodyDto> timesheetApproverResponseBodyDtos = this.timesheetApprovalJpaRepository
			.findLatestApprovalsByTimesheetIds(timesheetIds);

		// Prepare a map of TimesheetApproverResponseBodyDto by timesheetId
		Map<Integer, TimesheetApproverResponseBodyDto> approverMap = timesheetApproverResponseBodyDtos.stream()
			.collect(Collectors.toMap(TimesheetApproverResponseBodyDto::getTimesheetId, (dto) -> dto));

		// Build work summary map from query result (totalTime, totalWorkTime,
		// totalOvertime already from DB)
		Map<Integer, TimesheetWorkSummaryQueryResultDto> workSummaryMap = timesheetJobAndContractorListQueryResultBodyDtoList
			.stream()
			.collect(Collectors.toMap(TimesheetJobAndContractorListQueryResultDto::getId,
					(dto) -> new TimesheetWorkSummaryQueryResultDto(dto.getId(),
							(dto.getTotalWorkTime() != null) ? dto.getTotalWorkTime().longValue() : null,
							(dto.getTotalOvertime() != null) ? dto.getTotalOvertime().longValue() : null, null, null,
							(dto.getTotalTime() != null) ? dto.getTotalTime().longValue() : null)));

		fillCurrentTimesheetDetailsJobAndContractor(timesheetListResponseBodyDtos, workSummaryMap, approverMap,
				agencyUsersMap, contactUsersMap);

		return timesheetListResponseBodyDtos;
	}

	public void fillCurrentTimesheetDetailsJobAndContractor(
			List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos,
			Map<Integer, TimesheetWorkSummaryQueryResultDto> workSummaryMap,
			Map<Integer, TimesheetApproverResponseBodyDto> approverMap,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		for (TimesheetListResponseBodyDto currentTimesheet : timesheetListResponseBodyDtos) {
			Integer timesheetId = currentTimesheet.getId();

			// Set work summary details
			setWorkSummaryDetails(currentTimesheet, workSummaryMap.get(timesheetId));

			// Set approver details
			setApproverDetails(currentTimesheet, approverMap.get(timesheetId), agencyUsersMap, contactUsersMap);
		}
	}

	public void setAddedByJobAndContractor(
			List<TimesheetJobAndContractorListQueryResultDto> timesheetJobAndContractorListQueryResultBodyDtoList,
			List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap, List<Integer> timesheetIds) {

		for (int i = 0; i < timesheetJobAndContractorListQueryResultBodyDtoList.size(); i++) {
			TimesheetJobAndContractorListQueryResultDto queryResultDto = timesheetJobAndContractorListQueryResultBodyDtoList
				.get(i);
			TimesheetListResponseBodyDto responseDto = timesheetListResponseBodyDtos.get(i);

			AddedByResponseBodyDto addedBy = this.buildAddedByForJobAndContractor(queryResultDto, responseDto,
					agencyUsersMap, contactUsersMap);
			if (addedBy != null) {
				responseDto.setAddedBy(addedBy);
			}

			UpdatedByResponseBodyDto updatedBy = this.buildUpdatedByForJobAndContractor(queryResultDto, responseDto,
					agencyUsersMap, contactUsersMap);
			if (updatedBy != null) {
				responseDto.setUpdatedBy(updatedBy);
			}

			timesheetIds.add(queryResultDto.getId());
		}
	}

	@Override
	@Transactional
	@WriterRoute
	public void updateJobTimesheetAccessControl(Integer jobId,
			UpdateJobTimesheetAccessControlRequestBodyDto updateJobTimesheetAccessControlRequestBodyDto) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());

		Optional<JobTimesheetAccess> existingJobTimesheetAccess = this.jobTimesheetAccessJpaRepository
			.findByJobIdAndAccountId(jobId, accountId);

		if (existingJobTimesheetAccess.isPresent()) {
			JobTimesheetAccess jobTimesheetAccess = existingJobTimesheetAccess.get();
			jobTimesheetAccess.setCanCreate(updateJobTimesheetAccessControlRequestBodyDto.getCreate());
			jobTimesheetAccess.setCanEdit(updateJobTimesheetAccessControlRequestBodyDto.getEdit());
			jobTimesheetAccess.setCanDelete(updateJobTimesheetAccessControlRequestBodyDto.getDelete());
			jobTimesheetAccess.setUpdatedBy(userId);
			jobTimesheetAccess.setUpdatedOn(currentTimestamp);

			this.jobTimesheetAccessJpaRepository.save(jobTimesheetAccess);
		}
		else {
			throw new ResourceNotFoundException("Job with", jobId);
		}
	}

	@Override
	@Transactional
	@WriterRoute
	public TimesheetJobAccessControlResponseBodyDto getTimesheetJobAccessInfo(Integer jobId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

		Optional<JobTimesheetAccess> jobTimesheetAccess = this.jobTimesheetAccessJpaRepository
			.findByJobIdAndAccountId(jobId, accountId);
		if (jobTimesheetAccess.isPresent()) {
			return this.jobTimesheetAccessMapper.toResponseDto(jobTimesheetAccess.get());
		}
		else {
			JobTimesheetAccess newAccess = new JobTimesheetAccess();
			newAccess.setJobId(jobId);
			newAccess.setCanCreate(1);
			newAccess.setCanEdit(1);
			newAccess.setCanDelete(1);
			newAccess.setAccountId(accountId);
			newAccess.setUpdatedBy(userId);
			newAccess.setUpdatedOn(Math.toIntExact(Instant.now().getEpochSecond()));
			JobTimesheetAccess savedAccess = this.jobTimesheetAccessJpaRepository.save(newAccess);
			return this.jobTimesheetAccessMapper.toResponseDto(savedAccess);
		}
	}

	@Override
	@Transactional
	@ReaderRouteGlobalConsistency
	public List<TimesheetListResponseBodyDto> getTimesheetsListByEntityId(SearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		// Extract entityType and entityId from the authenticated principal (derived from
		// JWT token)
		Integer entityType = this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal);
		Integer entityId = this.principalEntityExtractor.extractEntityIdFromPrincipal(principal);

		if (entityType == null || entityId == null) {
			throw new ValidationErrorException(
					"Entity type and entity ID must be available in the access token. This endpoint is only available for contractors and contacts.");
		}

		this.entityAccessValidator.validateEntityAccess(entityType, entityId);

		List<Integer> requestTimesheetIds = searchRequestBodyDto.getTimesheetIds();
		boolean hasTimesheetIds = requestTimesheetIds != null && !requestTimesheetIds.isEmpty();
		boolean isSubmittedTrue = Boolean.TRUE.equals(searchRequestBodyDto.getIsSubmitted());
		boolean isReimbursementTrue = Boolean.TRUE.equals(searchRequestBodyDto.getIsReimbursement());

		if (hasTimesheetIds && isSubmittedTrue) {
			throw new ValidationErrorException(
					"Provide only one filter at a time: either timesheetIds or isSubmitted, not both.");
		}

		List<Integer> contactIds = resolveContactIds(entityType, principal, accountId);
		if (hasTimesheetIds && isReimbursementTrue) {
			throw new ValidationErrorException(
					"Provide only one filter at a time: either timesheetIds or isReimbursement, not both.");
		}

		if (isSubmittedTrue && isReimbursementTrue) {
			throw new ValidationErrorException(
					"Provide only one filter at a time: either isSubmitted or isReimbursement, not both.");
		}

		List<TimesheetJobAndContractorListQueryResultDto> queryResults = this.timesheetRepository
			.getTimesheetsListByEntityId(entityType, entityId, contactIds, accountId, searchRequestBodyDto, pageable);

		if (queryResults == null || queryResults.isEmpty()) {
			return Collections.emptyList();
		}

		List<TimesheetListResponseBodyDto> responseDtos = this.customTimeSheetMapper
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults);

		UserDetailsMaps userDetailsMaps = buildUserDetailsMaps(queryResults);
		List<Integer> timesheetIds = enrichWithUserDetails(responseDtos, queryResults, userDetailsMaps);

		enrichWithInvoiceDetails(responseDtos, timesheetIds, accountId);
		enrichWithApprovalOnly(responseDtos, timesheetIds, userDetailsMaps);

		// If entity is a client/contact (entityType == 1), enrich with approvers data
		if (entityType.equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			enrichWithApproversData(responseDtos, queryResults);
		}

		applyEntityTypeFieldFiltering(responseDtos, entityType);

		return responseDtos;
	}

	@Override
	public List<TimesheetListResponseBodyDto> searchTimesheets(TimesheetSearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		String gmtDifference = this.userTimezoneService.getCurrentUserGmtDifference();

		FilterSearchListDto filterSearchListDto = searchRequestBodyDto.getFilterSearchList();
		List<SortPriorityRequestBodyDto> sortPriorityList = searchRequestBodyDto.getSortPriorityList();
		List<Integer> requestTimesheetIds = searchRequestBodyDto.getTimesheetIds();

		List<TimesheetJobAndContractorListQueryResultDto> queryResults;

		// If specific timesheet IDs are provided, keep only those for this account that
		// pass the same candidate access control as the account-wide timesheet list
		if (requestTimesheetIds != null && !requestTimesheetIds.isEmpty()) {
			List<Integer> visibleIds = this.timesheetRepository
				.filterTimesheetIdsByAccountAndCandidateAccess(requestTimesheetIds, accountId);
			if (visibleIds.isEmpty()) {
				return Collections.emptyList();
			}
			queryResults = this.timesheetRepository.getTimesheetsListByIds(visibleIds, sortPriorityList, pageable);
		}
		// If isSubmitted is true, fetch timesheets where the login user is a configured
		// approver
		else if (Boolean.TRUE.equals(searchRequestBodyDto.getIsSubmitted())) {
			Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
			List<Integer> approverTimesheetIds = this.timesheetRepository.getTimesheetIdsByApproverUserId(userId,
					accountId, pageable);
			if (approverTimesheetIds.isEmpty()) {
				return Collections.emptyList();
			}
			queryResults = this.timesheetRepository.getTimesheetsListByIds(approverTimesheetIds, sortPriorityList,
					pageable);
		}
		// If isReimbursement is true, fetch timesheets that have pending reimbursements
		else if (Boolean.TRUE.equals(searchRequestBodyDto.getIsReimbursement())) {
			List<Integer> pendingReimbursementTimesheetIds = this.timesheetRepository
				.getTimesheetIdsWithPendingReimbursements(accountId, pageable);
			if (pendingReimbursementTimesheetIds.isEmpty()) {
				return Collections.emptyList();
			}
			queryResults = this.timesheetRepository.getTimesheetsListByIds(pendingReimbursementTimesheetIds,
					sortPriorityList, pageable);
		}
		// If filterSearchListDto is null, return all timesheets with sorting applied if
		// any, otherwise return timesheets without filters (sorted by updated_on desc by
		// default)
		else if (filterSearchListDto == null) {
			queryResults = this.timesheetRepository.getAllTimesheetsByAccountId(accountId, sortPriorityList, pageable);
		}
		else {
			queryResults = this.timesheetSearchService.searchTimesheets(filterSearchListDto, sortPriorityList,
					accountId, gmtDifference, pageable);
		}

		if (queryResults == null || queryResults.isEmpty()) {
			return Collections.emptyList();
		}

		List<TimesheetListResponseBodyDto> responseDtos = this.customTimeSheetMapper
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(queryResults);

		UserDetailsMaps userDetailsMaps = buildUserDetailsMaps(queryResults);
		List<Integer> timesheetIds = enrichWithUserDetails(responseDtos, queryResults, userDetailsMaps);

		enrichWithInvoiceDetails(responseDtos, timesheetIds, accountId);
		enrichWithApprovalOnly(responseDtos, timesheetIds, userDetailsMaps);
		enrichWithDealDetails(responseDtos, timesheetIds, accountId);

		return responseDtos;
	}

	@Override
	public Long searchTimesheetsCount(TimesheetSearchRequestBodyDto searchRequestBodyDto) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		String gmtDifference = this.userTimezoneService.getCurrentUserGmtDifference();

		FilterSearchListDto filterSearchListDto = searchRequestBodyDto.getFilterSearchList();
		List<Integer> requestTimesheetIds = searchRequestBodyDto.getTimesheetIds();

		if (requestTimesheetIds != null && !requestTimesheetIds.isEmpty()) {
			List<Integer> visibleIds = this.timesheetRepository
				.filterTimesheetIdsByAccountAndCandidateAccess(requestTimesheetIds, accountId);
			return (long) visibleIds.size();
		}
		else if (Boolean.TRUE.equals(searchRequestBodyDto.getIsSubmitted())) {
			Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
			return this.timesheetRepository.getTimesheetsCountByApproverUserId(userId, accountId);
		}
		else if (Boolean.TRUE.equals(searchRequestBodyDto.getIsReimbursement())) {
			return this.timesheetRepository.getTimesheetsCountWithPendingReimbursements(accountId);
		}
		else if (filterSearchListDto == null) {
			return this.timesheetRepository.getAllTimesheetsCountByAccountId(accountId);
		}
		else {
			return this.timesheetSearchService.getTimesheetsCount(filterSearchListDto, accountId, gmtDifference);
		}
	}

	@Override
	@Transactional
	@ReaderRouteGlobalConsistency
	public TimesheetCountResponseBodyDto getTimesheetsCountByEntityId(SearchRequestBodyDto searchRequestBodyDto) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		// Extract entityType and entityId from the authenticated principal (derived from
		// JWT token)
		Integer entityType = this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal);
		Integer entityId = this.principalEntityExtractor.extractEntityIdFromPrincipal(principal);

		if (entityType == null || entityId == null) {
			throw new ValidationErrorException(
					"Entity type and entity ID must be available in the access token. This endpoint is only available for contractors and contacts.");
		}

		this.entityAccessValidator.validateEntityAccess(entityType, entityId);

		// If entity is a client/contact (entityType == 1), ensure JobTimesheetAccess
		// records exist
		if (entityType.equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			this.ensureJobTimesheetAccessRecordsExist(entityId, accountId);
		}

		List<Integer> timesheetIds = searchRequestBodyDto.getTimesheetIds();
		boolean hasTimesheetIds = timesheetIds != null && !timesheetIds.isEmpty();
		boolean isSubmittedTrue = Boolean.TRUE.equals(searchRequestBodyDto.getIsSubmitted());
		boolean isReimbursementTrue = Boolean.TRUE.equals(searchRequestBodyDto.getIsReimbursement());

		if (hasTimesheetIds && isSubmittedTrue) {
			throw new ValidationErrorException(
					"Provide only one filter at a time: either timesheetIds or isSubmitted, not both.");
		}

		if (hasTimesheetIds && isReimbursementTrue) {
			throw new ValidationErrorException(
					"Provide only one filter at a time: either timesheetIds or isReimbursement, not both.");
		}

		if (isSubmittedTrue && isReimbursementTrue) {
			throw new ValidationErrorException(
					"Provide only one filter at a time: either isSubmitted or isReimbursement, not both.");
		}

		// Get total count without any filters
		List<Integer> contactIds = resolveContactIds(entityType, principal, accountId);
		Long totalCount = this.timesheetRepository.getTimesheetsCountByEntityId(entityType, entityId, contactIds,
				accountId);

		// Get filtered count with all entity-scoped filters applied (timesheetIds,
		// period,
		// isSubmitted, etc.) to ensure count matches the list endpoint
		Long filteredCount = this.timesheetRepository.getTimesheetsCountByEntityIdWithFilters(entityType, entityId,
				contactIds, accountId, searchRequestBodyDto);

		return TimesheetCountResponseBodyDto.builder().totalCount(totalCount).filteredCount(filteredCount).build();
	}

	/**
	 * For COMPANY_CONTACT entity type, resolve all contact IDs sharing the same email
	 * within the account. Returns empty list for all other entity types, preserving the
	 * existing single-entityId lookup path for contractors.
	 */
	private List<Integer> resolveContactIds(Integer entityType, AuthPrincipal principal, Integer accountId) {
		if (!entityType.equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			return List.of();
		}
		String email = principal.getEmail();
		if (email == null || email.isEmpty()) {
			return List.of();
		}
		return this.timesheetRepository.findContactIdsByEmail(email, accountId);
	}

	/**
	 * Build user details maps for agency users and contacts. Contractor details are now
	 * fetched directly in the query and set by the mapper.
	 */
	private UserDetailsMaps buildUserDetailsMaps(List<TimesheetJobAndContractorListQueryResultDto> queryResults) {
		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();

		collectUserIds(queryResults, agencyUserIds, contactUserIds);

		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = this.userRepository.getUserDetailsMap(agencyUserIds);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = this.contactRepository
			.getContactNamePhotoMap(contactUserIds);

		// Contractor details are now fetched in the JOOQ query and set by mapper
		return new UserDetailsMaps(agencyUsersMap, contactUsersMap, Map.of());
	}

	/**
	 * Enrich timesheet DTOs with user, contact, and contractor details. Returns list of
	 * timesheet IDs. Note: Contractor and job details are now set by the mapper from
	 * query results, so no need to manually set them here.
	 */
	private List<Integer> enrichWithUserDetails(List<TimesheetListResponseBodyDto> responseDtos,
			List<TimesheetJobAndContractorListQueryResultDto> queryResults, UserDetailsMaps userDetailsMaps) {
		List<Integer> timesheetIds = new ArrayList<>();
		setAddedByJobAndContractor(queryResults, responseDtos, userDetailsMaps.agencyUsersMap(),
				userDetailsMaps.contactUsersMap(), timesheetIds);
		return timesheetIds;
	}

	/**
	 * Collect user IDs from query results for fetching user details.
	 */
	private void collectUserIds(List<TimesheetJobAndContractorListQueryResultDto> queryResults,
			Set<Integer> agencyUserIds, Set<Integer> contactUserIds) {
		for (TimesheetJobAndContractorListQueryResultDto dto : queryResults) {
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getAddedByUserTypeId(), dto.getAddedById(),
					agencyUserIds, contactUserIds, new HashSet<>());
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getUpdatedByUserTypeId(), dto.getUpdatedById(),
					agencyUserIds, contactUserIds, new HashSet<>());
		}
	}

	/**
	 * Enrich timesheet DTOs with invoice details. {@code isInvoiceCreated} is 1 only when
	 * a {@link TimesheetInvoice} row exists and {@code invoiceId} is non-null (linked
	 * invoice).
	 */
	private void enrichWithInvoiceDetails(List<TimesheetListResponseBodyDto> responseDtos, List<Integer> timesheetIds,
			Integer accountId) {
		List<TimesheetInvoice> invoices = this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId);
		// Handle duplicates by keeping the invoice with the highest ID (most recent)
		Map<Integer, TimesheetInvoice> invoiceMap = invoices.stream()
			.collect(Collectors.toMap(TimesheetInvoice::getTimesheetId, (inv) -> inv,
					(existing, replacement) -> (replacement.getId() > existing.getId()) ? replacement : existing));

		for (TimesheetListResponseBodyDto dto : responseDtos) {
			TimesheetInvoice invoice = invoiceMap.get(dto.getId());
			dto.setIsInvoiceCreated((invoice != null && invoice.getInvoiceId() != null) ? 1 : 0);
			if (invoice != null) {
				dto.setPayoutFile(invoice.getPayoutFile());
				if (invoice.getPaymentPaidOn() != null) {
					dto.setPayoutPaidOn(invoice.getPaymentPaidOn());
				}
			}
		}
	}

	/**
	 * Enrich timesheet DTOs with approval details only. Total time fields (totalTime,
	 * totalWorkTime, totalOvertime) are expected to be already set from DB columns via
	 * the mapper. Used for search/list endpoint.
	 */
	private void enrichWithApprovalOnly(List<TimesheetListResponseBodyDto> responseDtos, List<Integer> timesheetIds,
			UserDetailsMaps userDetailsMaps) {
		List<TimesheetApproverResponseBodyDto> approverDtos = this.timesheetApprovalJpaRepository
			.findLatestApprovalsByTimesheetIds(timesheetIds);
		Map<Integer, TimesheetApproverResponseBodyDto> approverMap = approverDtos.stream()
			.collect(Collectors.toMap(TimesheetApproverResponseBodyDto::getTimesheetId, (dto) -> dto));

		fillCurrentTimesheetDetailsJobAndContractor(responseDtos, Map.of(), approverMap,
				userDetailsMaps.agencyUsersMap(), userDetailsMaps.contactUsersMap());
	}

	/**
	 * Enrich timesheet DTOs with deal details. Returns all deals associated with each
	 * timesheet.
	 */
	private void enrichWithDealDetails(List<TimesheetListResponseBodyDto> responseDtos, List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}

		// Fetch all deals for timesheets
		List<DealQueryResultDto> dealQueryResults = this.timesheetRepository.getDealsByTimesheetIds(timesheetIds,
				accountId);

		if (dealQueryResults == null || dealQueryResults.isEmpty()) {
			// Set empty lists for all timesheets
			for (TimesheetListResponseBodyDto dto : responseDtos) {
				dto.setDeals(List.of());
			}
			return;
		}

		// Group deals by timesheet ID
		Map<Integer, List<DealQueryResultDto>> dealsByTimesheetId = dealQueryResults.stream()
			.collect(Collectors.groupingBy(DealQueryResultDto::getTimesheetId));

		// Map deals to response DTOs
		for (TimesheetListResponseBodyDto dto : responseDtos) {
			List<DealQueryResultDto> timesheetDeals = dealsByTimesheetId.get(dto.getId());

			if (timesheetDeals != null && !timesheetDeals.isEmpty()) {
				List<DealResponseBodyDto> dealDtos = timesheetDeals.stream()
					.map((deal) -> new DealResponseBodyDto(deal.getDealId(), deal.getDealName(), deal.getOwnerName(),
							deal.getSerialNumber(), deal.getSlug(), deal.getStatus()))
					.toList();
				dto.setDeals(dealDtos);
			}
			else {
				dto.setDeals(List.of());
			}
		}
	}

	/**
	 * Enriches timesheet response DTOs with approvers data by fetching all approvers for
	 * the timesheet settings associated with the returned timesheets. Includes detailed
	 * information for each approver (id, name, photo, userTypeId).
	 * @param responseDtos List of timesheet response DTOs to enrich
	 * @param queryResults Query results containing timesheet setting IDs
	 */
	protected void enrichWithApproversData(List<TimesheetListResponseBodyDto> responseDtos,
			List<TimesheetJobAndContractorListQueryResultDto> queryResults) {
		// Extract unique timesheet setting IDs from query results
		List<Integer> timesheetSettingIds = queryResults.stream()
			.map(TimesheetJobAndContractorListQueryResultDto::getTimesheetSettingId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		if (timesheetSettingIds.isEmpty()) {
			return; // No timesheet settings to fetch approvers for
		}

		// Fetch all approvers for these timesheet settings
		List<TimesheetApprover> approvers = this.timesheetApproverRepository
			.findByTimesheetSettingIds(timesheetSettingIds);

		if (approvers.isEmpty()) {
			return; // No approvers found
		}

		// Collect agency and client IDs from approvers
		ApproverUserIds approverUserIds = collectApproverUserIds(approvers);

		// Fetch user details for agency users and clients
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = this.userRepository
			.getUserDetailsMap(approverUserIds.agencyUserIds());
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = this.contactRepository
			.getContactNamePhotoMap(approverUserIds.clientContactIds());

		// Group approvers by timesheet setting ID with detailed information
		Map<Integer, TimesheetApproversResponseBodyDto> approversBySettingId = buildApproversBySettingIdMap(approvers,
				agencyUsersMap, contactUsersMap);

		// Map approvers to each timesheet response DTO
		mapApproversToResponseDtos(responseDtos, queryResults, approversBySettingId);
	}

	/**
	 * Collects agency user IDs and client contact IDs from approvers list.
	 * @param approvers List of timesheet approvers
	 * @return Record containing sets of agency and client user IDs
	 */
	private ApproverUserIds collectApproverUserIds(List<TimesheetApprover> approvers) {
		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> clientContactIds = new HashSet<>();

		for (TimesheetApprover approver : approvers) {
			if (approver.getUserTypeId().equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
				agencyUserIds.add(approver.getEntityId());
			}
			else if (approver.getUserTypeId().equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
				clientContactIds.add(approver.getEntityId());
			}
		}

		return new ApproverUserIds(agencyUserIds, clientContactIds);
	}

	/**
	 * Builds a map of approvers grouped by timesheet setting ID.
	 * @param approvers List of timesheet approvers
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 * @return Map of approvers grouped by setting ID
	 */
	private Map<Integer, TimesheetApproversResponseBodyDto> buildApproversBySettingIdMap(
			List<TimesheetApprover> approvers, Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Map<Integer, TimesheetApproversResponseBodyDto> approversBySettingId = new HashMap<>();

		for (TimesheetApprover approver : approvers) {
			Integer settingId = approver.getTimesheetSettingId();
			TimesheetApproversResponseBodyDto approverDto = approversBySettingId.computeIfAbsent(settingId,
					(k) -> new TimesheetApproversResponseBodyDto(new ArrayList<>(), new ArrayList<>()));

			addApproverDetailToDto(approver, approverDto, agencyUsersMap, contactUsersMap);
		}

		return approversBySettingId;
	}

	/**
	 * Adds approver detail to the approver response DTO based on user type.
	 * @param approver Timesheet approver
	 * @param approverDto Approver response DTO to populate
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 */
	private void addApproverDetailToDto(TimesheetApprover approver, TimesheetApproversResponseBodyDto approverDto,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Integer userTypeId = approver.getUserTypeId();
		Integer entityId = approver.getEntityId();

		if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			addAgencyApproverDetail(entityId, userTypeId, agencyUsersMap, approverDto);
		}
		else if (userTypeId.equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			addClientApproverDetail(entityId, userTypeId, contactUsersMap, approverDto);
		}
	}

	/**
	 * Adds agency approver detail to the approver response DTO.
	 * @param entityId Entity ID of the approver
	 * @param userTypeId User type ID
	 * @param agencyUsersMap Map of agency user details
	 * @param approverDto Approver response DTO to populate
	 */
	private void addAgencyApproverDetail(Integer entityId, Integer userTypeId,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap, TimesheetApproversResponseBodyDto approverDto) {
		UserDetailsQueryResultDto userDetails = agencyUsersMap.get(entityId);
		if (userDetails != null) {
			ApproverDetailResponseBodyDto detailDto = ApproverDetailResponseBodyDto.builder()
				.id(entityId)
				.name(userDetails.getName())
				.photo(userDetails.getProfilePic())
				.userTypeId(userTypeId)
				.build();
			approverDto.getAgencyApproverDetails().add(detailDto);
		}
	}

	/**
	 * Adds client approver detail to the approver response DTO.
	 * @param entityId Entity ID of the approver
	 * @param userTypeId User type ID
	 * @param contactUsersMap Map of contact user details
	 * @param approverDto Approver response DTO to populate
	 */
	private void addClientApproverDetail(Integer entityId, Integer userTypeId,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap,
			TimesheetApproversResponseBodyDto approverDto) {
		ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(entityId);
		if (contactDetails != null) {
			ApproverDetailResponseBodyDto detailDto = ApproverDetailResponseBodyDto.builder()
				.id(entityId)
				.name(contactDetails.getName())
				.photo(contactDetails.getProfilePic())
				.userTypeId(userTypeId)
				.email(contactDetails.getEmail())
				.build();
			approverDto.getClientApproverDetails().add(detailDto);
		}
	}

	/**
	 * Maps approvers to each timesheet response DTO.
	 * @param responseDtos List of timesheet response DTOs
	 * @param queryResults Query results containing timesheet setting IDs
	 * @param approversBySettingId Map of approvers grouped by setting ID
	 */
	private void mapApproversToResponseDtos(List<TimesheetListResponseBodyDto> responseDtos,
			List<TimesheetJobAndContractorListQueryResultDto> queryResults,
			Map<Integer, TimesheetApproversResponseBodyDto> approversBySettingId) {
		for (int i = 0; i < responseDtos.size(); i++) {
			TimesheetListResponseBodyDto responseDto = responseDtos.get(i);
			TimesheetJobAndContractorListQueryResultDto queryResult = queryResults.get(i);

			Integer timesheetSettingId = queryResult.getTimesheetSettingId();
			if (timesheetSettingId != null) {
				TimesheetApproversResponseBodyDto approverDto = approversBySettingId.get(timesheetSettingId);
				responseDto.setApprovers(approverDto);
			}
		}
	}

	/**
	 * Ensures that JobTimesheetAccess records exist for all jobs associated with the
	 * given contact/client. Creates records with default permissions (can_create=1,
	 * can_edit=1, can_delete=1) for any jobs that don't have access records.
	 * @param contactId The contact/client ID
	 * @param accountId The account ID
	 */
	protected void ensureJobTimesheetAccessRecordsExist(Integer contactId, Integer accountId) {
		// Get all job IDs associated with this contact/client
		List<Integer> jobIds = this.timesheetRepository.getJobIdsByContactId(contactId, accountId);

		if (jobIds == null || jobIds.isEmpty()) {
			return; // No jobs found, nothing to do
		}

		// Find existing JobTimesheetAccess records for these jobs
		List<JobTimesheetAccess> existingRecords = this.jobTimesheetAccessJpaRepository.findByJobIdsAndAccountId(jobIds,
				accountId);

		// Extract job IDs that already have access records
		Set<Integer> existingJobIds = existingRecords.stream()
			.map(JobTimesheetAccess::getJobId)
			.collect(Collectors.toSet());

		// Find job IDs that don't have access records
		List<Integer> missingJobIds = jobIds.stream().filter((jobId) -> !existingJobIds.contains(jobId)).toList();

		if (missingJobIds.isEmpty()) {
			return; // All jobs already have access records
		}

		// Create JobTimesheetAccess records for missing job IDs
		Integer currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		List<JobTimesheetAccess> newRecords = missingJobIds.stream().map((jobId) -> {
			JobTimesheetAccess access = new JobTimesheetAccess();
			access.setJobId(jobId);
			access.setCanCreate(1);
			access.setCanEdit(1);
			access.setCanDelete(1);
			access.setAccountId(accountId);
			access.setUpdatedBy(contactId);
			access.setUpdatedOn(currentTimestamp);
			return access;
		}).toList();

		// Save all new records in bulk
		this.jobTimesheetAccessJpaRepository.saveAll(newRecords);
	}

	/**
	 * Apply field filtering based on entity type.
	 */
	private void applyEntityTypeFieldFiltering(List<TimesheetListResponseBodyDto> responseDtos, Integer entityType) {
		if (entityType == 3) {
			filterContractorFields(responseDtos);
		}
		else if (entityType == 1) {
			filterClientFields(responseDtos);
		}
	}

	/**
	 * Filter response fields for contractors. Contractors should only see pay-related
	 * fields, not bill-related or invoice-related fields.
	 * @param timesheetListResponseBodyDtos List of timesheet DTOs to filter
	 */
	private void filterContractorFields(List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos) {
		for (TimesheetListResponseBodyDto dto : timesheetListResponseBodyDtos) {
			// Hide bill-related fields
			dto.setBillRate(null);
			dto.setBillData(null);
			dto.setBillStatusId(null);
			dto.setBillCurrencySymbol(null);
			dto.setBillCurrencyCode(null);

			// Hide invoice-related fields
			dto.setInvoiceNumber(null);
			dto.setInvoiceCreatedOn(null);
			dto.setInvoiceStatusId(null);

			// Hide payout file (sensitive information)
			dto.setPayoutFile(null);

			// Hide access control fields (contractors should not see these)
			dto.setCanCreate(null);
			dto.setCanEdit(null);
			dto.setCanDelete(null);
		}
	}

	/**
	 * Filter response fields for clients/contacts. Clients should only see bill-related
	 * fields, not pay-related or payout-related fields. Exceptions: payCurrencySymbol and
	 * payCurrencyCode are retained for client/contact portal use;
	 * {@code isInvoiceCreated} is retained so the portal can show whether an invoice
	 * exists.
	 * @param timesheetListResponseBodyDtos List of timesheet DTOs to filter
	 */
	private void filterClientFields(List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos) {
		for (TimesheetListResponseBodyDto dto : timesheetListResponseBodyDtos) {
			// Hide pay-related fields (except payCurrencySymbol and payCurrencyCode
			// which are needed for client/contact portal)
			dto.setPayRate(null);
			dto.setPayData(null);
			dto.setPayStatusId(null);

			// Hide payout-related fields
			dto.setPayoutNumber(null);
			dto.setPayoutPaidOn(null);
			dto.setPayoutFile(null);

			// Hide invoice detail fields (number, dates, status); keep isInvoiceCreated
			// for portal
			dto.setInvoiceNumber(null);
			dto.setInvoiceCreatedOn(null);
			dto.setInvoiceStatusId(null);
		}
	}

	@Override
	@ReaderRouteGlobalConsistency
	public SearchEntityResponseBodyDto searchEntity(SearchEntityRequestBodyDto requestDto) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		String searchKeyword = (requestDto.search() != null && !requestDto.search().trim().isEmpty())
				? requestDto.search().trim() : null;

		Map<String, List<?>> dataMap = new HashMap<>();

		if (Boolean.TRUE.equals(requestDto.jobs())) {
			List<JobSearchResponseDto> jobs = this.searchJobs(accountId, searchKeyword,
					requestDto.fromContractorsListPage());
			dataMap.put(EntityTypeEnum.JOB.getIdAsString(), jobs);
		}

		if (Boolean.TRUE.equals(requestDto.companies())) {
			List<CompanySearchResponseDto> companies = this.searchCompanies(accountId, searchKeyword);
			dataMap.put(EntityTypeEnum.COMPANY.getIdAsString(), companies);
		}

		if (Boolean.TRUE.equals(requestDto.deals())) {
			List<DealSearchResponseDto> deals = this.searchDeals(accountId, searchKeyword,
					requestDto.fromContractorsListPage());
			dataMap.put(EntityTypeEnum.DEAL.getIdAsString(), deals);
		}

		return new SearchEntityResponseBodyDto(dataMap);
	}

	/**
	 * Searches for jobs and maps them to response DTOs.
	 * @param accountId The account ID to search within
	 * @param searchKeyword The search keyword (can be null)
	 * @param fromContractorsListPage Flag to conditionally apply job type filter
	 * @return List of job search response DTOs
	 */
	private List<JobSearchResponseDto> searchJobs(Integer accountId, String searchKeyword,
			Boolean fromContractorsListPage) {
		List<JobSearchQueryResultDto> jobResults = this.timesheetRepository.searchJobs(accountId, searchKeyword,
				fromContractorsListPage);
		return jobResults.stream().map((job) -> {
			JobSearchResponseDto dto = new JobSearchResponseDto();
			dto.setTitle(job.getName());
			dto.setSlug(job.getSlug());
			dto.setId(job.getId());
			dto.setSrno(job.getSrno());
			dto.setEntitytype(EntityTypeEnum.JOB.getIdAsString());
			dto.setCompanynameforjob((job.getCompanyname() != null) ? job.getCompanyname() : "");
			dto.setCompanyslug((job.getCompanyslug() != null) ? job.getCompanyslug() : "");
			dto.setLocation((job.getLocation() != null) ? job.getLocation() : "");
			return dto;
		}).toList();
	}

	/**
	 * Searches for companies and maps them to response DTOs.
	 * @param accountId The account ID to search within
	 * @param searchKeyword The search keyword (can be null)
	 * @return List of company search response DTOs
	 */
	private List<CompanySearchResponseDto> searchCompanies(Integer accountId, String searchKeyword) {
		List<CompanySearchQueryResultDto> companyResults = this.timesheetRepository.searchCompanies(accountId,
				searchKeyword);
		return companyResults.stream().map((company) -> {
			CompanySearchResponseDto dto = new CompanySearchResponseDto();
			dto.setTitle(company.getName());
			dto.setSlug(company.getSlug());
			dto.setId(company.getId());
			dto.setSrno(company.getSrno());
			dto.setPhoto((company.getLogo() != null) ? company.getLogo() : "");
			dto.setEntitytype(EntityTypeEnum.COMPANY.getIdAsString());
			dto.setAddress((company.getAddress() != null) ? company.getAddress() : "");
			dto.setCity((company.getCity() != null) ? company.getCity() : "");
			dto.setHaschildren((company.getHaschildren() != null) ? company.getHaschildren() : 0);
			dto.setIndustryname("None");
			dto.setLink("/company/" + company.getSlug());
			dto.setMlink("/company/" + company.getSlug());
			dto.setOwner((company.getOwnerid() != null) ? company.getOwnerid() : 0);
			dto.setWebsite(company.getWebsite());
			return dto;
		}).toList();
	}

	/**
	 * Searches for deals and maps them to response DTOs.
	 * @param accountId The account ID to search within
	 * @param searchKeyword The search keyword (can be null)
	 * @param fromContractorsListPage Whether the search is from contractors list page
	 * @return List of deal search response DTOs
	 */
	private List<DealSearchResponseDto> searchDeals(Integer accountId, String searchKeyword,
			Boolean fromContractorsListPage) {
		List<DealSearchQueryResultDto> dealResults = this.timesheetRepository.searchDeals(accountId, searchKeyword,
				fromContractorsListPage);
		return dealResults.stream().map((deal) -> {
			DealSearchResponseDto dto = new DealSearchResponseDto();
			dto.setTitle(deal.getName());
			dto.setSlug(deal.getSlug());
			dto.setId(deal.getId());
			dto.setSrno(deal.getSrno());
			dto.setEntitytype(EntityTypeEnum.DEAL.getIdAsString());
			dto.setOwner((deal.getOwner() != null) ? deal.getOwner() : 0);
			dto.setStagename((deal.getStagename() != null) ? deal.getStagename() : "");
			return dto;
		}).toList();
	}

	/**
	 * Builds UpdatedByResponseBodyDto for status history based on user type.
	 * @param statusHistoryQueryResultDto The status history query result DTO
	 * @param contractorUsersMap Map of contractor user details
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 * @return UpdatedByResponseBodyDto or null if user details not found
	 */
	private UpdatedByResponseBodyDto buildUpdatedByForStatusHistory(
			StatusHistoryQueryResultDto statusHistoryQueryResultDto,
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Integer userTypeId = statusHistoryQueryResultDto.getUpdatedByUserTypeId();
		Integer userId = statusHistoryQueryResultDto.getUpdatedById();

		if (userTypeId.equals(UserTypeEnum.CONTRACTOR.getId())) {
			ContractorNamePhotoQueryResultDto contractorDetails = contractorUsersMap.get(userId);
			if (contractorDetails != null) {
				return new UpdatedByResponseBodyDto(userId, contractorDetails.getName(),
						contractorDetails.getProfilePic(), userTypeId);
			}
		}
		else if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(userId);
			if (userDetails != null) {
				return new UpdatedByResponseBodyDto(userId, userDetails.getName(), userDetails.getProfilePic(),
						userTypeId);
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(userId);
			if (contactDetails != null) {
				return new UpdatedByResponseBodyDto(userId, contactDetails.getName(), contactDetails.getProfilePic(),
						userTypeId);
			}
		}
		return null;
	}

	/**
	 * Builds AddedByResponseBodyDto for deal-based timesheet list.
	 * @param timesheetDealListQueryResultDto The timesheet deal list query result DTO
	 * @param timesheetListResponseBodyDto The timesheet list response body DTO
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 * @return AddedByResponseBodyDto or null if user details not found
	 */
	private AddedByResponseBodyDto buildAddedByForDeal(TimesheetDealListQueryResultDto timesheetDealListQueryResultDto,
			TimesheetListResponseBodyDto timesheetListResponseBodyDto,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Integer userTypeId = timesheetDealListQueryResultDto.getAddedByUserTypeId();
		Integer userId = timesheetDealListQueryResultDto.getAddedById();

		if (userTypeId.equals(UserTypeEnum.CONTRACTOR.getId())) {
			ContractorQueryResultDto contractor = timesheetListResponseBodyDto.getContractor();
			if (contractor != null) {
				return new AddedByResponseBodyDto(userId, contractor.getName(), contractor.getPhoto(), userTypeId);
			}
		}
		else if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(userId);
			if (userDetails != null) {
				return new AddedByResponseBodyDto(userId, userDetails.getName(), userDetails.getProfilePic(),
						userTypeId);
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(userId);
			if (contactDetails != null) {
				return new AddedByResponseBodyDto(userId, contactDetails.getName(), contactDetails.getProfilePic(),
						userTypeId);
			}
		}
		return null;
	}

	/**
	 * Builds UpdatedByResponseBodyDto for deal-based timesheet list.
	 * @param timesheetDealListQueryResultDto The timesheet deal list query result DTO
	 * @param timesheetListResponseBodyDto The timesheet list response body DTO
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 * @return UpdatedByResponseBodyDto or null if user details not found
	 */
	private UpdatedByResponseBodyDto buildUpdatedByForDeal(
			TimesheetDealListQueryResultDto timesheetDealListQueryResultDto,
			TimesheetListResponseBodyDto timesheetListResponseBodyDto,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Integer userTypeId = timesheetDealListQueryResultDto.getUpdatedByUserTypeId();
		Integer userId = timesheetDealListQueryResultDto.getUpdatedById();

		if (userTypeId.equals(UserTypeEnum.CONTRACTOR.getId())) {
			ContractorQueryResultDto contractor = timesheetListResponseBodyDto.getContractor();
			if (contractor != null) {
				return new UpdatedByResponseBodyDto(userId, contractor.getName(), contractor.getPhoto(), userTypeId);
			}
		}
		else if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(userId);
			if (userDetails != null) {
				return new UpdatedByResponseBodyDto(userId, userDetails.getName(), userDetails.getProfilePic(),
						userTypeId);
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(userId);
			if (contactDetails != null) {
				return new UpdatedByResponseBodyDto(userId, contactDetails.getName(), contactDetails.getProfilePic(),
						userTypeId);
			}
		}
		return null;
	}

	/**
	 * Builds AddedByResponseBodyDto for job and contractor-based timesheet list.
	 * @param queryResultDto The timesheet job and contractor list query result DTO
	 * @param responseDto The timesheet list response body DTO
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 * @return AddedByResponseBodyDto or null if user details not found
	 */
	private AddedByResponseBodyDto buildAddedByForJobAndContractor(
			TimesheetJobAndContractorListQueryResultDto queryResultDto, TimesheetListResponseBodyDto responseDto,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Integer userTypeId = queryResultDto.getAddedByUserTypeId();
		Integer userId = queryResultDto.getAddedById();

		if (userTypeId.equals(UserTypeEnum.CONTRACTOR.getId())) {
			ContractorQueryResultDto contractor = responseDto.getContractor();
			if (contractor != null) {
				return new AddedByResponseBodyDto(userId, contractor.getName(), contractor.getPhoto(), userTypeId);
			}
		}
		else if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(userId);
			if (userDetails != null) {
				return new AddedByResponseBodyDto(userId, userDetails.getName(), userDetails.getProfilePic(),
						userTypeId);
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(userId);
			if (contactDetails != null) {
				return new AddedByResponseBodyDto(userId, contactDetails.getName(), contactDetails.getProfilePic(),
						userTypeId);
			}
		}
		return null;
	}

	/**
	 * Builds UpdatedByResponseBodyDto for job and contractor-based timesheet list.
	 * @param queryResultDto The timesheet job and contractor list query result DTO
	 * @param responseDto The timesheet list response body DTO
	 * @param agencyUsersMap Map of agency user details
	 * @param contactUsersMap Map of contact user details
	 * @return UpdatedByResponseBodyDto or null if user details not found
	 */
	private UpdatedByResponseBodyDto buildUpdatedByForJobAndContractor(
			TimesheetJobAndContractorListQueryResultDto queryResultDto, TimesheetListResponseBodyDto responseDto,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {
		Integer userTypeId = queryResultDto.getUpdatedByUserTypeId();
		Integer userId = queryResultDto.getUpdatedById();

		if (userTypeId.equals(UserTypeEnum.CONTRACTOR.getId())) {
			ContractorQueryResultDto contractor = responseDto.getContractor();
			if (contractor != null) {
				return new UpdatedByResponseBodyDto(userId, contractor.getName(), contractor.getPhoto(), userTypeId);
			}
		}
		else if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(userId);
			if (userDetails != null) {
				return new UpdatedByResponseBodyDto(userId, userDetails.getName(), userDetails.getProfilePic(),
						userTypeId);
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(userId);
			if (contactDetails != null) {
				return new UpdatedByResponseBodyDto(userId, contactDetails.getName(), contactDetails.getProfilePic(),
						userTypeId);
			}
		}
		return null;
	}

	@Override
	@WriterRoute
	@Transactional
	public TimesheetMigrationResponseBodyDto migrateTimesheetTotalColumns(TimesheetMigrationRequestBodyDto requestDto) {
		if (requestDto.getTimesheetId() != null) {
			return this.migrateSingleTimesheetById(requestDto.getTimesheetId());
		}
		return this.migrateTimesheetBatch(requestDto);
	}

	/**
	 * Migrates a single timesheet by ID. No batch processing.
	 */
	private TimesheetMigrationResponseBodyDto migrateSingleTimesheetById(Integer timesheetId) {
		List<TimesheetMigrationResponseBodyDto.SuccessfulMigrationDto> successfulMigrations = new ArrayList<>();
		List<TimesheetMigrationResponseBodyDto.FailedMigrationDto> failedMigrations = new ArrayList<>();

		Optional<TimesheetForMigrationDto> timesheetOpt = this.timesheetRepository
			.findTimesheetForMigrationById(timesheetId);

		if (timesheetOpt.isEmpty()) {
			failedMigrations.add(TimesheetMigrationResponseBodyDto.FailedMigrationDto.builder()
				.timesheetId(timesheetId)
				.errorMessage("Timesheet not found")
				.build());
			return TimesheetMigrationResponseBodyDto.builder()
				.successfulMigrations(successfulMigrations)
				.failedMigrations(failedMigrations)
				.totalProcessed(1)
				.successCount(0)
				.failureCount(1)
				.hasMore(false)
				.nextOffset(timesheetId)
				.build();
		}

		TimesheetForMigrationDto timesheet = timesheetOpt.get();
		List<Integer> timesheetSettingIds = (timesheet.getTimesheetSettingId() != null)
				? List.of(timesheet.getTimesheetSettingId()) : List.of();
		Map<Integer, Integer> workLogTypeBySettingId = this.timesheetSettingRepository
			.findWorkLogTypeByIdIn(timesheetSettingIds);
		List<TimeLogMigrationDto> timeLogs = this.timeLogRepository.findTimeLogsForMigration(List.of(timesheetId));
		Map<Integer, List<TimeLogMigrationDto>> timeLogsByTimesheetId = timeLogs.stream()
			.collect(Collectors.groupingBy(TimeLogMigrationDto::getTimesheetId));

		try {
			TimesheetMigrationResponseBodyDto.SuccessfulMigrationDto result = this.migrateSingleTimesheet(timesheet,
					workLogTypeBySettingId, timeLogsByTimesheetId);
			if (result != null) {
				successfulMigrations.add(result);
			}
		}
		catch (Exception ex) {
			failedMigrations.add(TimesheetMigrationResponseBodyDto.FailedMigrationDto.builder()
				.timesheetId(timesheetId)
				.errorMessage((ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getSimpleName())
				.build());
		}

		return TimesheetMigrationResponseBodyDto.builder()
			.successfulMigrations(successfulMigrations)
			.failedMigrations(failedMigrations)
			.totalProcessed(1)
			.successCount(successfulMigrations.size())
			.failureCount(failedMigrations.size())
			.hasMore(false)
			.nextOffset(timesheetId)
			.build();
	}

	/**
	 * Migrates timesheets in batch mode (batchSize, offset).
	 */
	private TimesheetMigrationResponseBodyDto migrateTimesheetBatch(TimesheetMigrationRequestBodyDto requestDto) {
		int batchSize = (requestDto.getBatchSize() != null) ? requestDto.getBatchSize() : 100;
		int offset = (requestDto.getOffset() != null) ? requestDto.getOffset() : 0;

		List<TimesheetMigrationResponseBodyDto.SuccessfulMigrationDto> successfulMigrations = new ArrayList<>();
		List<TimesheetMigrationResponseBodyDto.FailedMigrationDto> failedMigrations = new ArrayList<>();

		List<TimesheetForMigrationDto> timesheets = this.timesheetRepository.findTimesheetsForMigration(batchSize,
				offset);

		if (!timesheets.isEmpty()) {
			List<Integer> timesheetIds = timesheets.stream().map(TimesheetForMigrationDto::getTimesheetId).toList();
			List<Integer> timesheetSettingIds = timesheets.stream()
				.map(TimesheetForMigrationDto::getTimesheetSettingId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

			Map<Integer, Integer> workLogTypeBySettingId = this.timesheetSettingRepository
				.findWorkLogTypeByIdIn(timesheetSettingIds);
			List<TimeLogMigrationDto> timeLogs = this.timeLogRepository.findTimeLogsForMigration(timesheetIds);

			Map<Integer, List<TimeLogMigrationDto>> timeLogsByTimesheetId = timeLogs.stream()
				.collect(Collectors.groupingBy(TimeLogMigrationDto::getTimesheetId));

			for (TimesheetForMigrationDto timesheet : timesheets) {
				try {
					TimesheetMigrationResponseBodyDto.SuccessfulMigrationDto result = this
						.migrateSingleTimesheet(timesheet, workLogTypeBySettingId, timeLogsByTimesheetId);
					if (result != null) {
						successfulMigrations.add(result);
					}
				}
				catch (Exception ex) {
					failedMigrations.add(TimesheetMigrationResponseBodyDto.FailedMigrationDto.builder()
						.timesheetId(timesheet.getTimesheetId())
						.errorMessage((ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getSimpleName())
						.build());
				}
			}
		}

		boolean hasMore = timesheets.size() == batchSize;
		Integer nextOffset = offset + timesheets.size();

		return TimesheetMigrationResponseBodyDto.builder()
			.successfulMigrations(successfulMigrations)
			.failedMigrations(failedMigrations)
			.totalProcessed(successfulMigrations.size() + failedMigrations.size())
			.successCount(successfulMigrations.size())
			.failureCount(failedMigrations.size())
			.hasMore(hasMore)
			.nextOffset(nextOffset)
			.build();
	}

	/**
	 * Migrates a single timesheet's total columns.
	 * @param timesheet Timesheet data
	 * @param workLogTypeBySettingId Map of setting ID to work_log_type
	 * @param timeLogsByTimesheetId Map of timesheet ID to time logs
	 * @return SuccessfulMigrationDto or null if timesheet has no timelogs (still updated
	 * with 0s)
	 */
	private TimesheetMigrationResponseBodyDto.SuccessfulMigrationDto migrateSingleTimesheet(
			TimesheetForMigrationDto timesheet, Map<Integer, Integer> workLogTypeBySettingId,
			Map<Integer, List<TimeLogMigrationDto>> timeLogsByTimesheetId) {
		Integer timesheetId = timesheet.getTimesheetId();
		Integer timesheetSettingId = timesheet.getTimesheetSettingId();

		if (timesheetSettingId == null) {
			throw new ValidationErrorException("Timesheet " + timesheetId + " has no timesheet_setting_id");
		}

		Integer workLogType = workLogTypeBySettingId.get(timesheetSettingId);
		if (workLogType == null) {
			throw new ResourceNotFoundException(
					"Timesheet setting " + timesheetSettingId + " not found for timesheet " + timesheetId);
		}

		List<TimeLogMigrationDto> timelogs = timeLogsByTimesheetId.getOrDefault(timesheetId, List.of());

		int totalTime = timelogs.stream().mapToInt((tl) -> (tl.getTotalTime() != null) ? tl.getTotalTime() : 0).sum();
		int totalOvertime = timelogs.stream().mapToInt((tl) -> (tl.getOverTime() != null) ? tl.getOverTime() : 0).sum();

		int totalWorkTime;
		if (workLogType == WorkLogType.START_AND_END_TIME.getTypeId()) {
			totalWorkTime = timelogs.stream().mapToInt((tl) -> {
				if (tl.getWorkStartTime() != null && tl.getWorkEndTime() != null) {
					return Math.max(0, tl.getWorkEndTime() - tl.getWorkStartTime());
				}
				return 0;
			}).sum();
		}
		else {
			totalWorkTime = timelogs.stream().mapToInt((tl) -> (tl.getWorkTime() != null) ? tl.getWorkTime() : 0).sum();
		}

		this.timesheetRepository.updateTimesheetTotalColumns(timesheetId, totalTime, totalWorkTime, totalOvertime);

		List<Integer> timeLogIds = timelogs.stream().map(TimeLogMigrationDto::getTimeLogId).toList();
		return TimesheetMigrationResponseBodyDto.SuccessfulMigrationDto.builder()
			.timesheetId(timesheetId)
			.timeLogIds(timeLogIds)
			.totalTime(totalTime)
			.totalWorkTime(totalWorkTime)
			.totalOvertime(totalOvertime)
			.build();
	}

	private record TimesheetCreatedReminderAuthSnapshot(Integer userTypeId, Integer accountId,
			String performerDisplayName) {
	}

	/**
	 * Record to hold approver user IDs separated by type.
	 *
	 * @param agencyUserIds Set of agency user IDs
	 * @param clientContactIds Set of client contact IDs
	 */
	private record ApproverUserIds(Set<Integer> agencyUserIds, Set<Integer> clientContactIds) {
	}

}
