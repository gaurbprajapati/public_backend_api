package io.recruitcrm.microservice.timesheet.services.timesheet_setting;

import io.jsonwebtoken.io.DeserializationException;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.assigned_candidate.AssignCandidateJobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_setting_association.TimesheetSettingAssociationJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.EnableTimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingPreferenceResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingRequestBodyDto;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingUserPreference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.rule_engine.BreakTimeThresholdValidator;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetSettingMapper;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template.IRuleTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimesheetSettingService implements ITimesheetSettingService {

	private final AuthHolder auth;

	private final TimesheetSettingRepository timesheetSettingRepository;

	private final AssignCandidateJobJpaRepository assignCandidateJobJpaRepository;

	private final TimesheetApproverRepository timesheetApproverRepository;

	private final TimesheetSettingMapper timesheetSettingMapper;

	private final TimeLogRepository timeLogRepository;

	private final TimesheetSettingAssociationJpaRepository timesheetSettingAssociationJpaRepository;

	final AccessControlChecker contractStaffingAccessControlChecker;

	private final ObjectMapper objectMapper;

	private final IRuleTemplateRepository ruleTemplateRepository;

	public TimesheetSettingService(TimesheetSettingRepository timesheetSettingRepository,
			AssignCandidateJobJpaRepository assignCandidateJobJpaRepository,
			TimesheetApproverRepository timesheetApproverRepository, TimesheetSettingMapper timesheetSettingMapper,
			TimeLogRepository timeLogRepository,
			TimesheetSettingAssociationJpaRepository timesheetSettingAssociationJpaRepository, AuthHolder auth,
			AccessControlChecker contractStaffingAccessControlChecker, ObjectMapper objectMapper,
			IRuleTemplateRepository ruleTemplateRepository) {
		this.timesheetSettingRepository = timesheetSettingRepository;
		this.assignCandidateJobJpaRepository = assignCandidateJobJpaRepository;
		this.timesheetApproverRepository = timesheetApproverRepository;
		this.timesheetSettingMapper = timesheetSettingMapper;
		this.timeLogRepository = timeLogRepository;
		this.timesheetSettingAssociationJpaRepository = timesheetSettingAssociationJpaRepository;
		this.auth = auth;
		this.contractStaffingAccessControlChecker = contractStaffingAccessControlChecker;
		this.objectMapper = objectMapper;
		this.ruleTemplateRepository = ruleTemplateRepository;
	}

	@Override
	public TimesheetSettingResponseBodyDto getTimesheetSettingByAssignmentId(Integer jobId, Integer contractorId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		// Step 1: Check if assignment exists and get jobId and candidateId
		AssignCandidateJob assignment = this.assignCandidateJobJpaRepository
			.findByJobIdAndCandidateIdAndAccountId(jobId, contractorId, accountId);
		if (assignment == null) {
			throw new ResourceNotFoundException(
					"Assignment not found for Job Id: " + jobId + " candidate Id : " + contractorId);
		}

		// Step 2: Check if timesheet settings exists with jobId and candidateId
		TimesheetSetting timesheetSetting = this.timesheetSettingRepository.findByJobIdContractorId(jobId, contractorId)
			.orElseThrow(() -> new ResourceNotFoundException(
					"TimesheetSetting not found for Job ID: " + jobId + " and Contractor ID: " + contractorId));

		Integer timesheetSettingId = timesheetSetting.getId();

		TimesheetSetting firstTimesheetSetting = this.timesheetSettingRepository
			.findFirstByJobIdContractorId(jobId, contractorId)
			.orElseThrow(() -> new ResourceNotFoundException(
					"TimesheetSetting not found for Job ID: " + jobId + " and Contractor ID: " + contractorId));

		// Step 4: Get approvers for the timesheet setting
		List<TimesheetApprover> approvers = this.timesheetApproverRepository
			.findByTimesheetSettingId(timesheetSettingId);
		TimesheetSettingResponseBodyDto timesheetSettingResponseBodyDto = this.timesheetSettingMapper
			.timesheetSettingToDtoWithApprovers(timesheetSetting, approvers);

		timesheetSettingResponseBodyDto.setUpdatedOn(timesheetSetting.getCreatedOn());
		timesheetSettingResponseBodyDto.setUpdatedBy(timesheetSetting.getCreatedBy());
		timesheetSettingResponseBodyDto.setUpdatedByUserTypeId(timesheetSetting.getCreatedByUserTypeId());
		timesheetSettingResponseBodyDto.setEnabledOn(firstTimesheetSetting.getCreatedOn());
		timesheetSettingResponseBodyDto.setEnabledBy(firstTimesheetSetting.getCreatedBy());
		timesheetSettingResponseBodyDto.setEnabledByUserTypeId(firstTimesheetSetting.getCreatedByUserTypeId());

		// Set breakTimeThreshold from entity if available
		timesheetSettingResponseBodyDto.setBreakTimeThreshold(timesheetSetting.getBreakTimeThreshold());
		timesheetSettingResponseBodyDto.setIsUnplannedHoursPayEnabled(timesheetSetting.getIsUnplannedHoursPayEnabled());

		try {
			List<TemplateWorkDay> templateWorkDays = timesheetSetting.getTemplateWorkDay();
			List<CustomRule> customRules = timesheetSetting.getCustomRule();
			timesheetSettingResponseBodyDto.setTemplateWorkDays(templateWorkDays);
			timesheetSettingResponseBodyDto.setCustomRules(customRules);
		}
		catch (Exception error) {
			throw new DeserializationException("Failed to deserialize or validate JSON data", error);
		}
		return timesheetSettingResponseBodyDto;
	}

	private void getAssignmentAndValidateTimesheet(Integer jobId, List<Integer> contractorIds, Integer accountId) {
		List<AssignCandidateJob> assignment = this.assignCandidateJobJpaRepository
			.findByJobIdAndCandidateIdsAndAccountId(jobId, contractorIds, accountId);
		if (assignment.size() != contractorIds.size()) {
			throw new ResourceNotFoundException(
					"Assignment not found for Job Id: " + jobId + " candidate Ids : " + contractorIds);
		}
	}

	private TimesheetSetting createTimesheetSettingEntity(Integer jobId, TimesheetSettingRequestBodyDto request,
			Integer accountId) {
		TimesheetSetting timesheetSetting = this.timesheetSettingMapper.toEntity(jobId, request);
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();
		List<TemplateWorkDay> templateWorkDaysList = getTemplateWorkDaysRequestBodyDto(request);
		List<CustomRule> customRules = request.getCustomRules();
		int idCounter = 1;
		for (CustomRule customRule : customRules) {
			customRule.setId(idCounter++);
		}

		timesheetSetting.setAccountId(accountId);
		timesheetSetting.setCreatedOn(currentUNIXTimestamp);
		timesheetSetting.setCreatedBy(userId);
		timesheetSetting.setCreatedByUserTypeId(userTypeId);
		timesheetSetting.setTemplateWorkDay(templateWorkDaysList);
		timesheetSetting.setCustomRule(customRules);
		timesheetSetting.setBreakTimeThreshold(request.getBreakTimeThreshold());
		Integer isRemarkMandatory = request.getIsRemarkMandatory();
		if (isRemarkMandatory == null) {
			isRemarkMandatory = 0;
		}
		timesheetSetting.setIsRemarkMandatory(isRemarkMandatory);
		Integer isReimbursementEnabled = request.getIsReimbursementEnabled();
		if (isReimbursementEnabled == null) {
			isReimbursementEnabled = 0;
		}
		timesheetSetting.setIsReimbursementEnabled(isReimbursementEnabled);
		// Share-with-client default: checked (1) unless the caller explicitly turns it
		// off
		Integer isClientExpenseSharingEnabled = request.getIsClientExpenseSharingEnabled();
		if (isClientExpenseSharingEnabled == null) {
			isClientExpenseSharingEnabled = 1;
		}
		timesheetSetting.setIsClientExpenseSharingEnabled(isClientExpenseSharingEnabled);
		Integer isUnplannedHoursPayEnabled = request.getIsUnplannedHoursPayEnabled();
		if (isUnplannedHoursPayEnabled == null) {
			isUnplannedHoursPayEnabled = 0;
		}
		timesheetSetting.setIsUnplannedHoursPayEnabled(isUnplannedHoursPayEnabled);
		validateAndSetTimesheetStartDate(timesheetSetting, request);
		return timesheetSetting;
	}

	private void validateAndSetTimesheetStartDate(TimesheetSetting timesheetSetting,
			TimesheetSettingRequestBodyDto request) {
		Integer timesheetFrequency = request.getTimesheetFrequency();
		if (timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.WEEKLY.getId())
				|| timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId())
				|| timesheetFrequency.equals(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId())) {
			if (request.getTimesheetStartDay() == null) {
				throw new ValidationErrorException("Timesheet start date cannot be null");
			}
			int timesheetStartDate = request.getTimesheetStartDay();
			if (!((timesheetStartDate > 0 && timesheetStartDate < 29) || timesheetStartDate == 100)) {
				throw new ValidationErrorException("Timesheet start date must be between 1 to 28 OR Last Day(100)");
			}
			timesheetSetting.setTimesheetStartDay(timesheetStartDate);
		}
	}

	private List<TimesheetApprover> createApprovers(Integer timesheetSettingId,
			ApproverRequestResponseBodyDto approverDTO) {
		List<TimesheetApprover> timesheetApproversList = new ArrayList<>();
		List<TimesheetApprover> timesheetApproversAgency = approverDTO.getAgencyIds().stream().map((agencyId) -> {
			TimesheetApprover timesheetApprover = new TimesheetApprover();
			timesheetApprover.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
			timesheetApprover.setTimesheetSettingId(timesheetSettingId);
			timesheetApprover.setEntityId(agencyId);
			return timesheetApprover;
		}).toList();
		timesheetApproversList.addAll(timesheetApproversAgency);

		List<TimesheetApprover> timesheetApproversClients = approverDTO.getClientIds().stream().map((clientId) -> {
			TimesheetApprover timesheetApprover = new TimesheetApprover();
			timesheetApprover.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
			timesheetApprover.setTimesheetSettingId(timesheetSettingId);
			timesheetApprover.setEntityId(clientId);
			return timesheetApprover;
		}).toList();
		timesheetApproversList.addAll(timesheetApproversClients);
		return timesheetApproversList;
	}

	private void updateExistingOrAddTimesheetApprovers(Integer jobId, List<Integer> contractorIds,
			List<Integer> agencyIds, List<Integer> clientIds, Integer accountId) {
		this.timesheetApproverRepository.deleteTimesheetApprovers(jobId, contractorIds, agencyIds, accountId);
		this.timesheetApproverRepository.addTimesheetApprovers(jobId, contractorIds, agencyIds, accountId);
		this.timesheetApproverRepository.deleteTimesheetApproversForClients(jobId, contractorIds, clientIds, accountId);
		this.timesheetApproverRepository.addTimesheetApproversForClients(jobId, contractorIds, clientIds, accountId);
	}

	@Override
	@WriterRoute
	@Transactional
	public void createBulkTimesheetSettings(TimesheetSettingBulkRequestBodyDto request) {

		if (request.getJobEndDate() < request.getJobStartDate()) {
			throw new ValidationErrorException("Job end date should be greater than job start date");
		}

		Integer timesheetFrequency = request.getTimesheetFrequency();
		if (timesheetFrequency == null || timesheetFrequency < 1 || timesheetFrequency > 4) {
			throw new ValidationErrorException("timesheet frequency can be between 1 to 4");
		}

		Integer workLogType = request.getWorkLogType();
		if (workLogType == null || (workLogType != 1 && workLogType != 2)) {
			throw new ValidationErrorException("workLogType can be only 1 or 2");
		}

		// Validate break time threshold based on calculate break time setting
		BreakTimeThresholdValidator.validateBreakTimeThreshold(request.getCalculateBreakTime(),
				request.getBreakTimeThreshold());

		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.CREATE_TIMESHEET_SETTINGS);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(-1);
		// No specific timesheet data needed for create permission

		this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET_SETTINGS, permissionCheckContext,
				metadataContext);

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		List<Integer> contractorIds = request.getContractorIds();
		TimesheetSettingRequestBodyDto settingRequest = this.timesheetSettingMapper.toRequestDto(request);
		Integer jobId = request.getJobId();
		// Not sending isRemarkMandatory in payload means 0; sending 1 means set to 1
		Integer isRemarkMandatory = (request.getIsRemarkMandatory() != null) ? request.getIsRemarkMandatory() : 0;

		// 1. Validate contractors for job
		getAssignmentAndValidateTimesheet(jobId, contractorIds, accountId);

		// 2. Base timesheet setting template
		TimesheetSetting baseSetting = createTimesheetSettingEntity(jobId, settingRequest, accountId);

		// 2a. Fetch all existing associations in bulk
		List<TimesheetSettingAssociation> existingAssociations = this.timesheetSettingAssociationJpaRepository
			.findByJobIdAndContractorIdIn(jobId, contractorIds);

		Map<Integer, TimesheetSettingAssociation> existingMap = existingAssociations.stream()
			.collect(Collectors.toMap(TimesheetSettingAssociation::getContractorId, (a) -> a));

		// 3. Prepare collections
		List<TimesheetApprover> timesheetApproversList = new ArrayList<>();
		Set<Integer> associationIdsUsed = new HashSet<>();

		// 4. Loop through each contractor
		for (Integer contractorId : contractorIds) {
			TimesheetSettingAssociation association;

			// Check if association already exists
			if (existingMap.containsKey(contractorId)) {
				association = existingMap.get(contractorId);
			}
			else {
				association = new TimesheetSettingAssociation();
				association.setJobId(jobId);
				association.setContractorId(contractorId);
				association.setTimesheetSettings(new ArrayList<>());

				// Save the new association BEFORE linking it to the setting
				association = this.timesheetSettingAssociationJpaRepository.save(association);
			}
			associationIdsUsed.add(association.getId());

			// Create a deep copy of the base setting
			TimesheetSetting singleSetting = this.timesheetSettingMapper.copyTimesheetSetting(baseSetting);

			// Set bi-directional relationship
			singleSetting.setAssociation(association);

			// Save single timesheet setting directly to get ID
			TimesheetSetting savedSetting = this.timesheetSettingRepository.createTimesheetSetting(singleSetting);
			Integer timesheetSettingId = savedSetting.getId();

			Integer isPreferencesModified = settingRequest.getIsPreferencesModified();
			if (isPreferencesModified == 1) {
				saveTimesheetSettingPreference(savedSetting, settingRequest);
			}

			// Add to association (optional, useful for tracking)
			if (association.getTimesheetSettings() == null) {
				association.setTimesheetSettings(new ArrayList<>());
			}
			association.getTimesheetSettings().add(savedSetting);

			// Create approvers using the saved timesheetSetting ID
			List<TimesheetApprover> approvers = createApprovers(timesheetSettingId, request.getApprovers());
			timesheetApproversList.addAll(approvers);
		}

		// 5. Save all approvers in bulk
		this.timesheetApproverRepository.createTimesheetApproverInBulk(timesheetApproversList);

		// 6. Final update if needed
		updateExistingOrAddTimesheetApprovers(jobId, contractorIds, request.getApprovers().getAgencyIds(),
				request.getApprovers().getClientIds(), accountId);

		// 7. Update isRemarkMandatory for all versions under the same association IDs
		// (1 = set, not sent = 0). This is intentionally applied across all historical
		// versions because isRemarkMandatory is a global preference.
		// NOTE: isUnplannedHoursPayEnabled is intentionally NOT updated here — it is
		// already set on the newly created entity only (in createTimesheetSettingEntity),
		// so it applies strictly to the new version and does not backfill older ones.
		if (!associationIdsUsed.isEmpty()) {
			this.timesheetSettingRepository.updateIsRemarkMandatoryByAssociationIds(new ArrayList<>(associationIdsUsed),
					isRemarkMandatory);
		}
	}

	@Override
	public TimesheetSettingPreferenceResponseBodyDto getUserTimesheetSettingPreference() {
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		Optional<TimesheetSettingUserPreference> defaultTimesheet = this.timesheetSettingRepository
			.findUserPreferenceByAccountIdAndUserId(accountId, userId);

		Integer defaultTemplateId = this.ruleTemplateRepository.findDefaultTemplateIdByAccountId(accountId);

		if (defaultTimesheet.isEmpty()) {
			TimesheetSettingPreferenceResponseBodyDto responseDto = new TimesheetSettingPreferenceResponseBodyDto();
			responseDto.setTemplateId(defaultTemplateId);
			return responseDto;
		}

		TimesheetSettingUserPreference timesheeetSettingData = defaultTimesheet.get();
		try {
			TimesheetSettingPreferenceResponseBodyDto preferencedTimesheetDto = this.objectMapper.readValue(
					timesheeetSettingData.getTimesheetSettingJson(), TimesheetSettingPreferenceResponseBodyDto.class);

			preferencedTimesheetDto.setTemplateId(defaultTemplateId);

			return preferencedTimesheetDto;
		}
		catch (Exception error) {
			throw new DeserializationException("Error parsing timesheet setting preference", error);
		}
	}

	@Override
	public Boolean getTimesheetSettingDateValidation(Integer jobId, Integer contractorId, Long startDate,
			Long endDate) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		return this.timeLogRepository.validateByDate(jobId, contractorId, startDate, endDate, accountId);
	}

	private static List<TemplateWorkDay> getTemplateWorkDaysRequestBodyDto(TimesheetSettingRequestBodyDto requestDto) {
		List<Integer> workDayIds = requestDto.getWorkDayIds();
		List<Integer> workTime = requestDto.getWorkTime();
		List<Integer> workStartTime = requestDto.getWorkStartTime();
		List<Integer> workEndTime = requestDto.getWorkEndTime();

		List<TemplateWorkDay> templateWorkDaysList = new ArrayList<>();
		for (int i = 0; i < workDayIds.size(); i++) {
			Integer workDayId = workDayIds.get(i);
			Integer workTimeValue = (workTime != null && i < workTime.size()) ? workTime.get(i) : null;
			Integer workStartTimeValue = (workStartTime != null && i < workStartTime.size()) ? workStartTime.get(i)
					: null;
			Integer workEndTimeValue = (workEndTime != null && i < workEndTime.size()) ? workEndTime.get(i) : null;
			TemplateWorkDay templateWorkDays = new TemplateWorkDay(workDayId, workTimeValue, workStartTimeValue,
					workEndTimeValue);
			templateWorkDaysList.add(templateWorkDays);
		}
		return templateWorkDaysList;
	}

	public List<Integer> getEnabledAssigmentIds(EnableTimesheetSettingRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		return this.timesheetSettingRepository.fetchEnabledAssignmentIds(request.getAssignmentIds(), accountId);
	}

	/**
	 * Creates and saves a user preference from a timesheet setting
	 * @param savedSetting The timesheet setting to create preference from
	 * @param settingRequest The request body to create preference from
	 * @throws DeserializationException if conversion fails
	 */
	private void saveTimesheetSettingPreference(TimesheetSetting savedSetting,
			TimesheetSettingRequestBodyDto settingRequest) {
		// Create preference DTO from savedSetting
		TimesheetSettingPreferenceResponseBodyDto preferenceDto = new TimesheetSettingPreferenceResponseBodyDto(
				settingRequest.getTimesheetFrequency(), settingRequest.getTimesheetStartDay(),
				settingRequest.getApprovers(), savedSetting.getCreatedBy(), null);

		try {
			String timesheetSettingJson = this.objectMapper.writeValueAsString(preferenceDto);
			Integer addedBy = savedSetting.getCreatedBy();
			Integer addedByUserTypeId = savedSetting.getCreatedByUserTypeId();
			Integer settingAccountId = savedSetting.getAccountId();

			// Save user preference
			this.timesheetSettingRepository.saveUserPreference(timesheetSettingJson, addedBy, addedByUserTypeId,
					settingAccountId);
		}
		catch (Exception error) {
			throw new DeserializationException("Failed to deserialize or validate JSON data", error);
		}
	}

}