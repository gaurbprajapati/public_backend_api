package io.recruitcrm.microservice.timesheet.services.rule_engine.rule_template;

import io.recruitcrm.aws.aurora.annotation.ReaderRouteGlobalConsistency;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.CloneTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.FetchUserAndContactUserIds;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.helpers.rule_engine.BreakTimeThresholdValidator;
import io.recruitcrm.microservice.timesheet.mapper.rule_engine.rule_template.RuleTemplateMapper;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template.RuleTemplateRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleTemplateConstants;

@Service
public class RuleTemplateService implements IRuleTemplateService {

	private final AuthHolder auth;

	private final RuleTemplateRepository ruleTemplateRepository;

	private final RuleTemplateMapper ruleTemplateMapper;

	private final UserRepository userRepository;

	private final ContactRepository contactRepository;

	private final FetchUserAndContactUserIds fetchUserAndContactUserIds;

	public RuleTemplateService(AuthHolder auth, RuleTemplateRepository ruleTemplateRepository,
			RuleTemplateMapper ruleTemplateMapper, UserRepository userRepository, ContactRepository contactRepository,
			FetchUserAndContactUserIds fetchUserAndContactUserIds) {
		this.auth = auth;
		this.ruleTemplateRepository = ruleTemplateRepository;
		this.ruleTemplateMapper = ruleTemplateMapper;
		this.userRepository = userRepository;
		this.contactRepository = contactRepository;
		this.fetchUserAndContactUserIds = fetchUserAndContactUserIds;
	}

	@Override
	@Transactional
	@WriterRoute
	public void createRuleTemplate(CreateRuleTemplateRequestBodyDto requestDto) {
		validateBreakTimeThreshold(requestDto);

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();
		List<TemplateWorkDay> templateWorkDaysList = getTemplateWorkDaysRequestBodyDtos(requestDto);

		List<CustomRule> customRules = requestDto.getCustomRules();
		if (customRules == null) {
			customRules = new ArrayList<>();
		}

		int idCounter = 1;
		for (CustomRule customRule : customRules) {
			customRule.setId(idCounter++);
		}
		RuleTemplate ruleTemplate = new RuleTemplate();
		setRuleTemplate(ruleTemplate, requestDto,
				new TemplateAuditContext(accountId, userId, currentUNIXTimestamp, userTypeId), templateWorkDaysList,
				customRules, true);

		try {
			this.ruleTemplateRepository.createRuleTemplate(ruleTemplate);
		}
		catch (DataIntegrityViolationException ex) {
			if (ex.getMessage() != null
					&& ex.getMessage().contains(RuleTemplateConstants.UNIQUE_TEMPLATE_NAME_CONSTRAINT)) {
				throw new ValidationErrorException(
						"Rule template with name '" + ruleTemplate.getTemplateName() + "' already exists.");
			}
			if (ex.getMessage() != null && ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_COLUMN)
					&& ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_CANNOT_BE_NULL)) {
				throw new ValidationErrorException(RuleTemplateConstants.TEMPLATE_NAME_REQUIRED_MESSAGE);
			}
			if (ex.getMessage() != null && ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_COLUMN)
					&& (ex.getMessage().contains(RuleTemplateConstants.DATA_TRUNCATION)
							|| ex.getMessage().contains(RuleTemplateConstants.DATA_TOO_LONG))) {
				throw new ValidationErrorException("Template name must not exceed "
						+ RuleTemplateConstants.MAX_TEMPLATE_NAME_LENGTH + " characters.");
			}
			throw ex;
		}
	}

	private static List<TemplateWorkDay> getTemplateWorkDaysRequestBodyDtos(
			CreateRuleTemplateRequestBodyDto requestDto) {
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

	private void setRuleTemplate(RuleTemplate ruleTemplate, CreateRuleTemplateRequestBodyDto requestDto,
			TemplateAuditContext auditContext, List<TemplateWorkDay> templateWorkDays, List<CustomRule> customRules,
			boolean isNewTemplate) {
		ruleTemplate.setTemplateName(requestDto.getTemplateName());
		ruleTemplate.setWorkLogType(requestDto.getWorkLogType());
		ruleTemplate.setCalculateBreakTime(requestDto.getCalculateBreakTime());
		Integer isUnplannedHoursPayEnabled = requestDto.getIsUnplannedHoursPayEnabled();
		if (isUnplannedHoursPayEnabled == null) {
			isUnplannedHoursPayEnabled = 0;
		}
		ruleTemplate.setIsUnplannedHoursPayEnabled(isUnplannedHoursPayEnabled);

		if (Boolean.FALSE.equals(requestDto.getCalculateBreakTime())) {
			ruleTemplate.setBreakTimeThreshold(requestDto.getBreakTimeThreshold());
		}
		else {
			ruleTemplate.setBreakTimeThreshold(null);
		}

		ruleTemplate.setAccountId(auditContext.accountId);
		ruleTemplate.setAddedBy(auditContext.userId);
		ruleTemplate.setUpdatedBy(auditContext.userId);
		ruleTemplate.setTemplateWorkDay(templateWorkDays);
		ruleTemplate.setCustomRule(customRules);
		ruleTemplate.setUpdatedByUserTypeId(auditContext.userTypeId);
		ruleTemplate.setAddedByUserTypeId(auditContext.userTypeId);
		ruleTemplate.setAddedOn(auditContext.timestamp);
		ruleTemplate.setUpdatedOn(auditContext.timestamp);
		if (isNewTemplate) {
			ruleTemplate.setIsDefault(0);
		}
	}

	@Override
	public RuleTemplateResponseBodyDto getRuleTemplate(Integer templateId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		RuleTemplate ruleTemplate = this.ruleTemplateRepository.getRuleTemplate(templateId, accountId);

		if (ruleTemplate == null) {
			throw new ResourceNotFoundException("Rule template not found with id: " + templateId);
		}

		RuleTemplateResponseBodyDto ruleTemplateResponseBodyDto = this.ruleTemplateMapper
			.ruleTemplateToResponseBodyDto(ruleTemplate);

		List<TemplateWorkDay> templateWorkDays = ruleTemplate.getTemplateWorkDay();
		List<CustomRule> customRules = ruleTemplate.getCustomRule();

		ruleTemplateResponseBodyDto.setTemplateWorkDays(templateWorkDays);
		ruleTemplateResponseBodyDto.setCustomRules(customRules);

		// Set breakTimeThreshold from entity once the field is available
		ruleTemplateResponseBodyDto.setBreakTimeThreshold(ruleTemplate.getBreakTimeThreshold());
		Integer unplannedHoursPayEnabled = ruleTemplate.getIsUnplannedHoursPayEnabled();
		ruleTemplateResponseBodyDto
			.setIsUnplannedHoursPayEnabled((unplannedHoursPayEnabled != null) ? unplannedHoursPayEnabled : 0);

		return ruleTemplateResponseBodyDto;
	}

	@Override
	public List<RuleTemplateNameResponseBodyDto> getRuleTemplateNames(String search, Pageable pageable) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		List<RuleTemplateNameQueryResultDto> ruleTemplateNameQueryResultDtos = this.ruleTemplateRepository
			.getRuleTemplateNames(search, pageable, accountId);

		// if no rule template found, throw ResourceNotFoundException
		if (ruleTemplateNameQueryResultDtos == null || ruleTemplateNameQueryResultDtos.isEmpty()) {
			return new ArrayList<>();
		}
		return this.ruleTemplateMapper.ruleTemplateQueryResultToResponseBodyDto(ruleTemplateNameQueryResultDtos);
	}

	@Override
	@Transactional
	@ReaderRouteGlobalConsistency
	public List<RuleTemplateListResponseBodyDto> getAllRuleTemplates(SearchRequestBodyDto searchRequestBodyDto,
			String search, Pageable pageable) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		List<RuleTemplateListQueryResultDto> ruleTemplates = this.ruleTemplateRepository
			.getAllRuleTemplates(searchRequestBodyDto, search, pageable, accountId);

		if (ruleTemplates == null || ruleTemplates.isEmpty()) {
			return new ArrayList<>();
		}

		List<RuleTemplateListResponseBodyDto> ruleTemplateListResponseBodyDtos = this.ruleTemplateMapper
			.ruleTemplateListQueryResultToResponseBodyDtoList(ruleTemplates);

		// Collect user IDs
		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();
		for (RuleTemplateListQueryResultDto dto : ruleTemplates) {
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getAddedByUserTypeId(), dto.getAddedBy(),
					agencyUserIds, contactUserIds, new HashSet<>());
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(dto.getUpdatedByUserTypeId(), dto.getUpdatedBy(),
					agencyUserIds, contactUserIds, new HashSet<>());
		}

		// Fetch user details
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = this.userRepository.getUserDetailsMap(agencyUserIds);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = this.contactRepository
			.getContactNamePhotoMap(contactUserIds);

		// Set user details for addedBy and updatedBy
		setRuleTemplateUserDetails(ruleTemplates, ruleTemplateListResponseBodyDtos, agencyUsersMap, contactUsersMap);

		return ruleTemplateListResponseBodyDtos;
	}

	private void setRuleTemplateUserDetails(List<RuleTemplateListQueryResultDto> ruleTemplates,
			List<RuleTemplateListResponseBodyDto> ruleTemplateListResponseBodyDtos,
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		for (int i = 0; i < ruleTemplates.size(); i++) {
			RuleTemplateListQueryResultDto template = ruleTemplates.get(i);
			RuleTemplateListResponseBodyDto response = ruleTemplateListResponseBodyDtos.get(i);

			setAddedByUserDetails(template, response, agencyUsersMap, contactUsersMap);
			setUpdatedByUserDetails(template, response, agencyUsersMap, contactUsersMap);
		}
	}

	private void setAddedByUserDetails(RuleTemplateListQueryResultDto template,
			RuleTemplateListResponseBodyDto response, Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		if (template.getAddedByUserTypeId().equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(template.getAddedBy());
			if (userDetails != null) {
				response.setAddedBy(new AddedByResponseBodyDto(template.getAddedBy(), userDetails.getName(),
						userDetails.getProfilePic(), template.getAddedByUserTypeId()));
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(template.getAddedBy());
			if (contactDetails != null) {
				response.setAddedBy(new AddedByResponseBodyDto(template.getAddedBy(), contactDetails.getName(),
						contactDetails.getProfilePic(), template.getAddedByUserTypeId()));
			}
		}
	}

	private void setUpdatedByUserDetails(RuleTemplateListQueryResultDto template,
			RuleTemplateListResponseBodyDto response, Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap) {

		if (template.getUpdatedByUserTypeId().equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			UserDetailsQueryResultDto userDetails = agencyUsersMap.get(template.getUpdatedBy());
			if (userDetails != null) {
				response.setUpdatedBy(new UpdatedByResponseBodyDto(template.getUpdatedBy(), userDetails.getName(),
						userDetails.getProfilePic(), template.getUpdatedByUserTypeId()));
			}
		}
		else {
			ContactNamePhotoQueryResultDto contactDetails = contactUsersMap.get(template.getUpdatedBy());
			if (contactDetails != null) {
				response.setUpdatedBy(new UpdatedByResponseBodyDto(template.getUpdatedBy(), contactDetails.getName(),
						contactDetails.getProfilePic(), template.getUpdatedByUserTypeId()));
			}
		}
	}

	@Override
	public void updateRuleTemplate(Integer templateId, CreateRuleTemplateRequestBodyDto requestDto) {
		validateBreakTimeThreshold(requestDto);
		// Get the existing rule template
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		RuleTemplate existingRuleTemplate = this.ruleTemplateRepository.getRuleTemplate(templateId, accountId);

		if (existingRuleTemplate == null) {
			throw new ValidationErrorException("Rule template not found with id: " + templateId);
		}

		// Store original values to preserve
		Integer originalAddedBy = existingRuleTemplate.getAddedBy();
		Integer originalAddedOn = existingRuleTemplate.getAddedOn();
		Integer originalAddedByUserTypeId = existingRuleTemplate.getAddedByUserTypeId();

		// Use the same logic as createRuleTemplate
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();
		List<TemplateWorkDay> templateWorkDaysList = getTemplateWorkDaysRequestBodyDtos(requestDto);

		List<CustomRule> customRules = requestDto.getCustomRules();
		int idCounter = 1;
		for (CustomRule customRule : customRules) {
			customRule.setId(idCounter++);
		}

		// Use the same setRuleTemplate method as createRuleTemplate
		setRuleTemplate(existingRuleTemplate, requestDto,
				new TemplateAuditContext(accountId, userId, currentUNIXTimestamp, userTypeId), templateWorkDaysList,
				customRules, false);

		// Keep the original ID and original creation audit fields
		existingRuleTemplate.setId(templateId);
		existingRuleTemplate.setAddedBy(originalAddedBy);
		existingRuleTemplate.setAddedOn(originalAddedOn);
		existingRuleTemplate.setAddedByUserTypeId(originalAddedByUserTypeId);

		try {
			this.ruleTemplateRepository.updateRuleTemplate(existingRuleTemplate);
		}
		catch (DataIntegrityViolationException ex) {
			if (ex.getMessage() != null
					&& ex.getMessage().contains(RuleTemplateConstants.UNIQUE_TEMPLATE_NAME_CONSTRAINT)) {
				throw new ValidationErrorException(
						"Rule template with name '" + requestDto.getTemplateName() + "' already exists.");
			}
			if (ex.getMessage() != null && ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_COLUMN)
					&& ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_CANNOT_BE_NULL)) {
				throw new ValidationErrorException(RuleTemplateConstants.TEMPLATE_NAME_REQUIRED_MESSAGE);
			}
			if (ex.getMessage() != null && ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_COLUMN)
					&& (ex.getMessage().contains(RuleTemplateConstants.DATA_TRUNCATION)
							|| ex.getMessage().contains(RuleTemplateConstants.DATA_TOO_LONG))) {
				throw new ValidationErrorException("Template name must not exceed "
						+ RuleTemplateConstants.MAX_TEMPLATE_NAME_LENGTH + " characters.");
			}
			throw ex;
		}
	}

	@Override
	public CloneTemplateResponseBodyDto cloneRuleTemplate(Integer templateId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		RuleTemplate existingRuleTemplate = this.ruleTemplateRepository.getRuleTemplate(templateId, accountId);
		if (existingRuleTemplate == null) {
			throw new ResourceNotFoundException("Rule template not found with id: " + templateId);
		}

		// Collect required data for response
		Integer workLogType = existingRuleTemplate.getWorkLogType();
		Boolean calculateBreakTime = existingRuleTemplate.getCalculateBreakTime();
		Integer existingUnplannedHoursPayEnabled = existingRuleTemplate.getIsUnplannedHoursPayEnabled();
		Integer isUnplannedHoursPayEnabled = (existingUnplannedHoursPayEnabled != null)
				? existingUnplannedHoursPayEnabled : 0;
		Integer customRulesCount = (existingRuleTemplate.getCustomRule() != null)
				? existingRuleTemplate.getCustomRule().size() : 0;
		List<Integer> workDayIds = new ArrayList<>();
		if (existingRuleTemplate.getTemplateWorkDay() != null) {
			for (TemplateWorkDay templateWorkDay : existingRuleTemplate.getTemplateWorkDay()) {
				workDayIds.add(templateWorkDay.getWorkDayId());
			}
		}

		String originalName = existingRuleTemplate.getTemplateName();

		if (originalName == null) {
			originalName = "";
		}

		int maxOriginalLength = RuleTemplateConstants.MAX_TEMPLATE_NAME_LENGTH
				- RuleTemplateConstants.CLONE_PREFIX.length();

		if (originalName.length() > maxOriginalLength) {
			int keepChars = Math.max(0, maxOriginalLength - RuleTemplateConstants.ELLIPSIS.length());
			originalName = originalName.substring(0, keepChars) + RuleTemplateConstants.ELLIPSIS;
		}

		String newRuleTemplateName = RuleTemplateConstants.CLONE_PREFIX + originalName;

		// Create a new RuleTemplate entity instead of reusing the existing one
		RuleTemplate newRuleTemplate = new RuleTemplate();
		newRuleTemplate.setTemplateName(newRuleTemplateName);
		newRuleTemplate.setWorkLogType(existingRuleTemplate.getWorkLogType());
		newRuleTemplate.setCalculateBreakTime(existingRuleTemplate.getCalculateBreakTime());
		newRuleTemplate.setBreakTimeThreshold(existingRuleTemplate.getBreakTimeThreshold());
		newRuleTemplate.setIsUnplannedHoursPayEnabled(isUnplannedHoursPayEnabled);
		newRuleTemplate.setAccountId(accountId);
		newRuleTemplate.setAddedBy(userId);
		newRuleTemplate.setUpdatedBy(userId);
		newRuleTemplate.setTemplateWorkDay(existingRuleTemplate.getTemplateWorkDay());
		newRuleTemplate.setCustomRule(existingRuleTemplate.getCustomRule());
		newRuleTemplate.setUpdatedByUserTypeId(userTypeId);
		newRuleTemplate.setAddedByUserTypeId(userTypeId);
		newRuleTemplate.setAddedOn(currentUNIXTimestamp);
		newRuleTemplate.setUpdatedOn(currentUNIXTimestamp);
		newRuleTemplate.setIsDefault(0);

		try {
			this.ruleTemplateRepository.createRuleTemplate(newRuleTemplate);
		}
		catch (DataIntegrityViolationException ex) {
			if (ex.getMessage() != null
					&& ex.getMessage().contains(RuleTemplateConstants.UNIQUE_TEMPLATE_NAME_CONSTRAINT)) {
				throw new ValidationErrorException(
						"Rule template with name '" + newRuleTemplateName + "' already exists.");
			}
			if (ex.getMessage() != null && ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_COLUMN)
					&& ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_CANNOT_BE_NULL)) {
				throw new ValidationErrorException(RuleTemplateConstants.TEMPLATE_NAME_REQUIRED_MESSAGE);
			}
			if (ex.getMessage() != null && ex.getMessage().contains(RuleTemplateConstants.TEMPLATE_NAME_COLUMN)
					&& (ex.getMessage().contains(RuleTemplateConstants.DATA_TRUNCATION)
							|| ex.getMessage().contains(RuleTemplateConstants.DATA_TOO_LONG))) {
				throw new ValidationErrorException("Template name must not exceed "
						+ RuleTemplateConstants.MAX_TEMPLATE_NAME_LENGTH + " characters.");
			}
			throw ex;
		}

		// Return the response DTO with the collected data
		return new CloneTemplateResponseBodyDto(workLogType, calculateBreakTime, customRulesCount, workDayIds,
				isUnplannedHoursPayEnabled);
	}

	@Override
	public void deleteRuleTemplate(Integer templateId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// first check template exist or not and validate account access
		RuleTemplate existingRuleTemplate = this.ruleTemplateRepository.getRuleTemplate(templateId, accountId);
		if (existingRuleTemplate == null) {
			throw new ResourceNotFoundException("Rule template not found with id: " + templateId);
		}

		this.ruleTemplateRepository.deleteRuleTemplate(templateId);
	}

	@Override
	@Transactional
	@WriterRoute
	public void markAsDefault(Integer templateId, Boolean isDefault) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// first check template exist or not and validate account access
		RuleTemplate existingRuleTemplate = this.ruleTemplateRepository.getRuleTemplate(templateId, accountId);
		if (existingRuleTemplate == null) {
			throw new ResourceNotFoundException("Rule template not found with id: " + templateId);
		}

		this.ruleTemplateRepository.markAsDefault(templateId, accountId, isDefault);
	}

	private void validateBreakTimeThreshold(CreateRuleTemplateRequestBodyDto requestDto) {
		BreakTimeThresholdValidator.validateBreakTimeThreshold(requestDto.getCalculateBreakTime(),
				requestDto.getBreakTimeThreshold());
	}

	private static final class TemplateAuditContext {

		final Integer accountId;

		final Integer userId;

		final Integer timestamp;

		final Integer userTypeId;

		TemplateAuditContext(Integer accountId, Integer userId, Integer timestamp, Integer userTypeId) {
			this.accountId = accountId;
			this.userId = userId;
			this.timestamp = timestamp;
			this.userTypeId = userTypeId;
		}

	}

}