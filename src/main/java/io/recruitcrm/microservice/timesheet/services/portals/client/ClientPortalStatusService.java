/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkContactDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkSkippedContactDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.ClientPortalUrlHelper;
import io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusAction;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusBulkSkipReason;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.SendGridSettingsConstants;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IBulkValidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IClientPortalStatusRepository;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IInvitableContactsRepository;
import io.recruitcrm.microservice.timesheet.repositories.settings.ISettingsRepository;
import io.recruitcrm.microservice.timesheet.services.sendgrid.ISendGridEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation for fetching and updating client portal status by email and
 * account.
 */
@Service
public class ClientPortalStatusService implements IClientPortalStatusService {

	private static final String TEMPLATE_KEY_CONTACT_FIRST_NAME = "contactFirstName";

	private static final String TEMPLATE_KEY_AGENCY_NAME = "agencyName";

	private static final String TEMPLATE_KEY_AGENCY_USER_NAME = "agencyUserName";

	private static final String TEMPLATE_KEY_PORTAL_URL = "portalURL";

	private final AuthHolder auth;

	private final IClientPortalStatusRepository clientPortalStatusRepository;

	private final IInvitableContactsRepository invitableContactsRepository;

	private final EmailValidationErrorHelper emailValidationErrorHelper;

	private final ISendGridEmailService sendGridEmailService;

	private final ISettingsRepository settingsRepository;

	private final AccessControlHelper accessControlHelper;

	private final IBulkValidateRepository bulkValidateRepository;

	private final String applicationEnv;

	public ClientPortalStatusService(AuthHolder auth, IClientPortalStatusRepository clientPortalStatusRepository,
			IInvitableContactsRepository invitableContactsRepository,
			EmailValidationErrorHelper emailValidationErrorHelper, ISendGridEmailService sendGridEmailService,
			ISettingsRepository settingsRepository, AccessControlHelper accessControlHelper,
			IBulkValidateRepository bulkValidateRepository, @Value("${application.env}") String applicationEnv) {
		this.auth = auth;
		this.clientPortalStatusRepository = clientPortalStatusRepository;
		this.invitableContactsRepository = invitableContactsRepository;
		this.emailValidationErrorHelper = emailValidationErrorHelper;
		this.sendGridEmailService = sendGridEmailService;
		this.settingsRepository = settingsRepository;
		this.accessControlHelper = accessControlHelper;
		this.bulkValidateRepository = bulkValidateRepository;
		this.applicationEnv = applicationEnv;
	}

	@Override
	@Transactional(readOnly = true)
	public ClientPortalStatusResponseBodyDto getPortalStatus(String email, Integer rcrmContactId) {
		this.validateGetPortalStatusRequest(email, rcrmContactId);
		this.validateContactAccessForView(rcrmContactId);
		Integer accountId = this.resolveAuthenticatedAccountId();

		String normalizedEmail = email.trim();
		Optional<ClientPortalStatusQueryResultDto> portalStatusOptional = this.clientPortalStatusRepository
			.findByVmsUserEmailAndAccountId(normalizedEmail, accountId);

		if (portalStatusOptional.isPresent()) {
			return this.toResponseBodyDto(portalStatusOptional.get(), false);
		}

		boolean existsUnderDifferentAccount = this.clientPortalStatusRepository
			.existsPortalStatusUnderDifferentAccount(normalizedEmail, accountId);
		return this.buildDefaultResponse(existsUnderDifferentAccount);
	}

	@Override
	@Transactional
	@WriterRoute
	public ClientPortalStatusBulkResponseBodyDto bulkEnablePortal(ClientPortalStatusBulkRequestBodyDto request) {
		this.validateBulkRequest(request);
		Integer accountId = this.resolveAuthenticatedAccountId();
		this.validateRequestAccountId(request.getAccountId(), accountId);
		this.validateRecruiterUserId(request.getRecruiterUserId());
		this.validateCompanyId(request.getCompanyId());
		this.validateBulkContactsAccessControl(request.getContacts());
		this.validateBulkContactsAssignedToCompany(request.getContacts(), request.getCompanyId(), accountId);

		List<String> invited = new ArrayList<>();
		List<ClientPortalStatusBulkSkippedContactDto> skipped = new ArrayList<>();
		List<ClientPortalStatus> entitiesToPersist = new ArrayList<>();
		List<BulkInviteToSend> invitesToSend = new ArrayList<>();
		List<String> normalizedEmailsForBatch = this.collectNormalizedEmailsForBatchLookup(request.getContacts());
		Map<String, ClientPortalStatus> existingByEmail = this.loadExistingEntitiesByEmail(normalizedEmailsForBatch,
				accountId);
		Set<String> emailsWithPriorInvitationSent = this.collectEmailsWithPriorInvitationSent(existingByEmail);
		Set<String> emailsTakenByOtherAgencies = new HashSet<>(this.clientPortalStatusRepository
			.findEmailsPortalEnabledUnderDifferentAccount(normalizedEmailsForBatch, accountId));
		Set<String> emailsInvitedInBatch = new HashSet<>();
		int currentTimestamp = this.getCurrentUnixTimestamp();

		for (ClientPortalStatusBulkContactDto contact : request.getContacts()) {
			String skipReason = this.resolveBulkSkipReason(contact, existingByEmail, emailsWithPriorInvitationSent,
					emailsTakenByOtherAgencies, currentTimestamp);
			String normalizedEmail = (skipReason != null) ? null : contact.getEmail().trim();
			if ((skipReason == null) && emailsInvitedInBatch.contains(normalizedEmail)) {
				skipReason = ClientPortalStatusBulkSkipReason.DUPLICATE_EMAIL;
			}
			if (skipReason != null) {
				skipped.add(this.toSkippedContactDto(contact, skipReason));
				continue;
			}

			emailsInvitedInBatch.add(normalizedEmail);
			ClientPortalStatus entity = existingByEmail.getOrDefault(normalizedEmail, new ClientPortalStatus());
			int effectiveInviteCount = this.resolveEffectiveInviteCount(entity, currentTimestamp);
			entity.setPortalStatusId(ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT);
			entity.setAccountId(accountId);
			entity.setCompanyId(request.getCompanyId());
			entity.setInviteCount(effectiveInviteCount + 1);
			entity.setVmsUserEmail(normalizedEmail);
			entity.setVmsUserId(null);
			entity.setInviteSentByUserId(request.getRecruiterUserId());
			entity.setInviteSentOn(currentTimestamp);
			entity.setUpdatedOn(currentTimestamp);

			existingByEmail.put(normalizedEmail, entity);
			entitiesToPersist.add(entity);
			invitesToSend.add(new BulkInviteToSend(contact, normalizedEmail));
			invited.add(normalizedEmail);
		}

		if (!entitiesToPersist.isEmpty()) {
			this.clientPortalStatusRepository.saveAll(entitiesToPersist);
		}

		for (BulkInviteToSend inviteToSend : invitesToSend) {
			this.sendBulkInviteEmail(request, inviteToSend.contact(), inviteToSend.normalizedEmail(), accountId);
		}

		return new ClientPortalStatusBulkResponseBodyDto(invited, skipped, invited.size(), skipped.size());
	}

	@Override
	@Transactional
	@WriterRoute
	public ClientPortalStatusUpdateResult updatePortalStatus(ClientPortalStatusUpdateRequestBodyDto request) {
		ClientPortalStatusAction action = this.resolveAction(request.getAction());
		String normalizedEmail = this.validateAndNormalizeEmail(request.getEmail());
		this.validateContactAccessForUpdate(request.getRcrmContactId());
		Integer accountId = this.resolveAuthenticatedAccountId();

		return switch (action) {
			case SEND_INVITE, RESEND_INVITE -> this.handleSendInvite(request, normalizedEmail, accountId, action);
			case DISABLE -> this.handleDisable(request, normalizedEmail, accountId);
			case RE_ENABLE -> this.handleReEnable(request, normalizedEmail, accountId);
		};
	}

	private ClientPortalStatusUpdateResult handleSendInvite(ClientPortalStatusUpdateRequestBodyDto request,
			String normalizedEmail, Integer accountId, ClientPortalStatusAction action) {
		this.validateRecruiterUserId(request.getRecruiterUserId());
		this.validateCompanyId(request.getCompanyId());
		this.validateRcrmContactId(request.getRcrmContactId());
		this.validateContactAssignedToCompany(request.getRcrmContactId(), request.getCompanyId(), accountId);
		this.validateEmailNotActiveUnderAnotherAgency(normalizedEmail, accountId);

		int currentTimestamp = this.getCurrentUnixTimestamp();
		Optional<ClientPortalStatus> existingOptional = this.clientPortalStatusRepository
			.findEntityByVmsUserEmailAndAccountId(normalizedEmail, accountId);
		ClientPortalStatus existingEntity = existingOptional.orElse(null);
		boolean isResendInvite = (action == ClientPortalStatusAction.RESEND_INVITE);

		if (!isResendInvite && (existingEntity != null)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.INVITE_ALREADY_SENT_MESSAGE);
		}

		if (isResendInvite) {
			this.validateResendInviteEligibility(existingEntity);
		}

		int nextInviteCount = this.resolveNextInviteCount(existingEntity);
		if (isResendInvite) {
			this.validateResendInviteRateLimit(existingEntity, nextInviteCount, currentTimestamp);
		}

		ClientPortalStatus entity = existingOptional.orElseGet(ClientPortalStatus::new);
		entity.setPortalStatusId(ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT);
		entity.setAccountId(accountId);
		entity.setCompanyId(request.getCompanyId());
		entity.setInviteCount(nextInviteCount);
		entity.setVmsUserEmail(normalizedEmail);
		entity.setVmsUserId(null);
		entity.setInviteSentByUserId(request.getRecruiterUserId());
		entity.setInviteSentOn(currentTimestamp);
		entity.setUpdatedOn(currentTimestamp);

		ClientPortalStatus savedEntity = this.clientPortalStatusRepository.save(entity);
		this.sendInviteEmail(request, normalizedEmail, accountId);

		return new ClientPortalStatusUpdateResult(this.toUpdateResponseBodyDto(savedEntity, normalizedEmail),
				ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE);
	}

	private ClientPortalStatusUpdateResult handleDisable(ClientPortalStatusUpdateRequestBodyDto request,
			String normalizedEmail, Integer accountId) {
		this.validateRecruiterUserId(request.getRecruiterUserId());

		ClientPortalStatus entity = this.clientPortalStatusRepository
			.findEntityByVmsUserEmailAndAccountId(normalizedEmail, accountId)
			.orElseThrow(
					() -> new ResourceNotFoundException(ClientPortalStatusConstants.PORTAL_STATUS_NOT_FOUND_MESSAGE));

		int currentTimestamp = this.getCurrentUnixTimestamp();
		entity.setPortalStatusId(ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED);
		entity.setInviteSentByUserId(request.getRecruiterUserId());
		entity.setUpdatedOn(currentTimestamp);

		ClientPortalStatus savedEntity = this.clientPortalStatusRepository.save(entity);
		this.sendDisableEmail(request, normalizedEmail);

		return new ClientPortalStatusUpdateResult(this.toUpdateResponseBodyDto(savedEntity, normalizedEmail),
				ClientPortalStatusConstants.DISABLE_SUCCESS_MESSAGE);
	}

	private ClientPortalStatusUpdateResult handleReEnable(ClientPortalStatusUpdateRequestBodyDto request,
			String normalizedEmail, Integer accountId) {
		ClientPortalStatus entity = this.clientPortalStatusRepository
			.findEntityByVmsUserEmailAndAccountId(normalizedEmail, accountId)
			.orElseThrow(
					() -> new ResourceNotFoundException(ClientPortalStatusConstants.PORTAL_STATUS_NOT_FOUND_MESSAGE));

		int currentTimestamp = this.getCurrentUnixTimestamp();
		entity.setPortalStatusId(ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED);
		entity.setUpdatedOn(currentTimestamp);

		ClientPortalStatus savedEntity = this.clientPortalStatusRepository.save(entity);
		this.sendReEnableEmail(request, normalizedEmail);

		return new ClientPortalStatusUpdateResult(this.toUpdateResponseBodyDto(savedEntity, normalizedEmail),
				ClientPortalStatusConstants.ACCEPT_SUCCESS_MESSAGE);
	}

	private void sendBulkInviteEmail(ClientPortalStatusBulkRequestBodyDto request,
			ClientPortalStatusBulkContactDto contact, String normalizedEmail, Integer accountId) {
		String templateId = this.settingsRepository
			.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_INVITE_TEMPLATE_ID);
		Map<String, Object> templateData = this.buildInviteTemplateData(new InviteTemplateContext(
				contact.getFirstName(), request.getRecruiterName(), request.getAgencyName(), normalizedEmail,
				request.getCompanyName(), accountId, request.getCompanyId(), contact.getRcrmContactId()));
		this.sendGridEmailService.sendEmailWithTemplate(templateId, normalizedEmail, templateData);
	}

	private List<String> collectNormalizedEmailsForBatchLookup(List<ClientPortalStatusBulkContactDto> contacts) {
		List<String> normalizedEmails = new ArrayList<>();
		for (ClientPortalStatusBulkContactDto contact : contacts) {
			String email = contact.getEmail();
			if ((email != null) && !email.isBlank()
					&& (this.emailValidationErrorHelper.validateClientEmailFormat(email.trim()) == null)) {
				normalizedEmails.add(email.trim());
			}
		}
		return normalizedEmails;
	}

	private Map<String, ClientPortalStatus> loadExistingEntitiesByEmail(List<String> normalizedEmails,
			Integer accountId) {
		if (normalizedEmails.isEmpty()) {
			return Map.of();
		}
		return this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(normalizedEmails, accountId)
			.stream()
			.filter((entity) -> (entity.getVmsUserEmail() != null) && !entity.getVmsUserEmail().isBlank())
			.collect(Collectors.toMap((entity) -> entity.getVmsUserEmail().trim(), Function.identity(),
					(existing, replacement) -> existing));
	}

	private Set<String> collectEmailsWithPriorInvitationSent(Map<String, ClientPortalStatus> existingByEmail) {
		Set<String> emailsWithPriorInvitationSent = new HashSet<>();
		for (Map.Entry<String, ClientPortalStatus> entry : existingByEmail.entrySet()) {
			if (Objects.equals(entry.getValue().getPortalStatusId(),
					ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT)) {
				emailsWithPriorInvitationSent.add(entry.getKey());
			}
		}
		return emailsWithPriorInvitationSent;
	}

	private String resolveBulkSkipReason(ClientPortalStatusBulkContactDto contact,
			Map<String, ClientPortalStatus> existingByEmail, Set<String> emailsWithPriorInvitationSent,
			Set<String> emailsTakenByOtherAgencies, int currentTimestamp) {
		if ((contact.getEmail() == null) || contact.getEmail().isBlank()) {
			return ClientPortalStatusBulkSkipReason.EMAIL_MISSING;
		}
		String normalizedEmail = contact.getEmail().trim();
		if (this.emailValidationErrorHelper.validateClientEmailFormat(normalizedEmail) != null) {
			return ClientPortalStatusBulkSkipReason.EMAIL_MISSING;
		}
		ClientPortalStatus existingEntity = existingByEmail.get(normalizedEmail);
		if ((existingEntity != null) && Objects.equals(existingEntity.getPortalStatusId(),
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED)) {
			return ClientPortalStatusBulkSkipReason.PORTAL_ACTIVE;
		}
		if (emailsTakenByOtherAgencies.contains(normalizedEmail)) {
			return ClientPortalStatusBulkSkipReason.EMAIL_TAKEN;
		}
		if (emailsWithPriorInvitationSent.contains(normalizedEmail)) {
			return ClientPortalStatusBulkSkipReason.INVITE_ALREADY_SENT;
		}
		int effectiveInviteCount = this.resolveEffectiveInviteCount(existingEntity, currentTimestamp);
		if (effectiveInviteCount >= ClientPortalStatusConstants.MAX_DAILY_INVITE_COUNT) {
			return ClientPortalStatusBulkSkipReason.RATE_LIMIT;
		}
		return null;
	}

	private ClientPortalStatusBulkSkippedContactDto toSkippedContactDto(ClientPortalStatusBulkContactDto contact,
			String reason) {
		String email = contact.getEmail();
		if ((email != null) && !email.isBlank()) {
			email = email.trim();
		}
		return new ClientPortalStatusBulkSkippedContactDto(email, contact.getFirstName(), reason);
	}

	private void validateContactAccessForView(Integer rcrmContactId) {
		if (!this.hasContactViewAccess(rcrmContactId)) {
			throw new UnauthorizedAccessException(ClientPortalStatusConstants.CONTACT_NOT_AUTHORIZED_MESSAGE);
		}
	}

	private void validateContactAccessForUpdate(Integer rcrmContactId) {
		if ((rcrmContactId == null) || (rcrmContactId <= 0)) {
			return;
		}
		if (!this.hasContactViewAndEditAccess(rcrmContactId)) {
			throw new UnauthorizedAccessException(ClientPortalStatusConstants.CONTACT_NOT_AUTHORIZED_MESSAGE);
		}
	}

	private void validateRequestAccountId(Integer requestAccountId, Integer authenticatedAccountId) {
		if ((requestAccountId != null) && !Objects.equals(requestAccountId, authenticatedAccountId)) {
			throw new UnauthorizedAccessException(ClientPortalStatusConstants.UNAUTHORIZED_ACCOUNT_MESSAGE);
		}
	}

	private void validateBulkContactsAssignedToCompany(List<ClientPortalStatusBulkContactDto> contacts,
			Integer companyId, Integer accountId) {
		List<Integer> contactIds = contacts.stream()
			.map(ClientPortalStatusBulkContactDto::getRcrmContactId)
			.filter((contactId) -> (contactId != null) && (contactId > 0))
			.distinct()
			.toList();
		for (Integer contactId : contactIds) {
			this.validateContactAssignedToCompany(contactId, companyId, accountId);
		}
	}

	private void validateBulkContactsAccessControl(List<ClientPortalStatusBulkContactDto> contacts) {
		List<Integer> contactIds = contacts.stream()
			.map(ClientPortalStatusBulkContactDto::getRcrmContactId)
			.filter((contactId) -> (contactId != null) && (contactId > 0))
			.distinct()
			.toList();
		if (contactIds.isEmpty()) {
			return;
		}
		Contacts contactsAcl = this.accessControlHelper.getContactsAccessControl();
		if (contactsAcl == null) {
			throw new UnauthorizedAccessException(ClientPortalStatusConstants.BULK_CONTACTS_NOT_AUTHORIZED_MESSAGE);
		}
		Integer currentUserId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		List<Integer> teamUserIds = this.resolveTeamUserIdsForContactsAccess(contactsAcl, currentUserId);
		Map<Integer, Integer> ownerIdByContactId = this.bulkValidateRepository.findOwnerIdsByContactIds(contactIds);
		for (Integer contactId : contactIds) {
			Integer ownerId = ownerIdByContactId.get(contactId);
			boolean canView = this.accessControlHelper.resolvePermission(contactsAcl.getCanView(), ownerId,
					currentUserId, teamUserIds);
			boolean canEdit = this.accessControlHelper.resolvePermission(contactsAcl.getCanEdit(), ownerId,
					currentUserId, teamUserIds);
			if (!canView || !canEdit) {
				throw new UnauthorizedAccessException(ClientPortalStatusConstants.BULK_CONTACTS_NOT_AUTHORIZED_MESSAGE);
			}
		}
	}

	private boolean hasContactViewAccess(Integer rcrmContactId) {
		Contacts contactsAcl = this.accessControlHelper.getContactsAccessControl();
		if (contactsAcl == null) {
			return false;
		}
		Integer currentUserId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		List<Integer> teamUserIds = this.resolveTeamUserIdsForContactsViewAccess(contactsAcl, currentUserId);
		Map<Integer, Integer> ownerIdByContactId = this.bulkValidateRepository
			.findOwnerIdsByContactIds(List.of(rcrmContactId));
		Integer ownerId = ownerIdByContactId.get(rcrmContactId);
		return this.accessControlHelper.resolvePermission(contactsAcl.getCanView(), ownerId, currentUserId,
				teamUserIds);
	}

	private boolean hasContactViewAndEditAccess(Integer rcrmContactId) {
		Contacts contactsAcl = this.accessControlHelper.getContactsAccessControl();
		if (contactsAcl == null) {
			return false;
		}
		Integer currentUserId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		List<Integer> teamUserIds = this.resolveTeamUserIdsForContactsAccess(contactsAcl, currentUserId);
		Map<Integer, Integer> ownerIdByContactId = this.bulkValidateRepository
			.findOwnerIdsByContactIds(List.of(rcrmContactId));
		Integer ownerId = ownerIdByContactId.get(rcrmContactId);
		boolean canView = this.accessControlHelper.resolvePermission(contactsAcl.getCanView(), ownerId, currentUserId,
				teamUserIds);
		boolean canEdit = this.accessControlHelper.resolvePermission(contactsAcl.getCanEdit(), ownerId, currentUserId,
				teamUserIds);
		return canView && canEdit;
	}

	private List<Integer> resolveTeamUserIdsForContactsViewAccess(Contacts contactsAcl, Integer currentUserId) {
		if (!this.accessControlHelper.requiresTeamLookup(contactsAcl.getCanView())) {
			return List.of();
		}
		List<Integer> teamUserIds = this.accessControlHelper.getTeamUserIds(currentUserId);
		if (!teamUserIds.contains(currentUserId)) {
			teamUserIds.add(currentUserId);
		}
		return teamUserIds;
	}

	private List<Integer> resolveTeamUserIdsForContactsAccess(Contacts contactsAcl, Integer currentUserId) {
		if (!this.accessControlHelper.requiresTeamLookup(contactsAcl.getCanView())
				&& !this.accessControlHelper.requiresTeamLookup(contactsAcl.getCanEdit())) {
			return List.of();
		}
		List<Integer> teamUserIds = this.accessControlHelper.getTeamUserIds(currentUserId);
		if (!teamUserIds.contains(currentUserId)) {
			teamUserIds.add(currentUserId);
		}
		return teamUserIds;
	}

	private void validateBulkRequest(ClientPortalStatusBulkRequestBodyDto request) {
		boolean contactsMissing = (request.getContacts() == null) || request.getContacts().isEmpty();

		if (contactsMissing) {
			throw new ValidationErrorException(ClientPortalStatusConstants.BULK_CONTACTS_REQUIRED_MESSAGE);
		}
		if ((request.getRecruiterUserId() == null) || (request.getRecruiterUserId() <= 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.RECRUITER_USER_ID_REQUIRED_MESSAGE);
		}
		if ((request.getCompanyId() == null) || (request.getCompanyId() <= 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.COMPANY_ID_REQUIRED_MESSAGE);
		}
	}

	private void sendInviteEmail(ClientPortalStatusUpdateRequestBodyDto request, String normalizedEmail,
			Integer accountId) {
		String templateId = this.settingsRepository
			.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_INVITE_TEMPLATE_ID);
		Map<String, Object> templateData = this.buildInviteTemplateData(new InviteTemplateContext(
				request.getFirstName(), request.getRecruiterName(), request.getAgencyName(), normalizedEmail,
				request.getCompanyName(), accountId, request.getCompanyId(), request.getRcrmContactId()));
		this.sendGridEmailService.sendEmailWithTemplate(templateId, normalizedEmail, templateData);
	}

	private void sendDisableEmail(ClientPortalStatusUpdateRequestBodyDto request, String normalizedEmail) {
		String templateId = this.settingsRepository
			.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_DISABLE_TEMPLATE_ID);
		Map<String, Object> templateData = new HashMap<>();
		templateData.put(TEMPLATE_KEY_CONTACT_FIRST_NAME,
				ClientPortalUrlHelper.resolveHiringManagerName(request.getFirstName(), request.getLastName()));
		templateData.put(TEMPLATE_KEY_AGENCY_NAME, ClientPortalUrlHelper.resolveAgencyName(request.getAgencyName()));
		this.sendGridEmailService.sendEmailWithTemplate(templateId, normalizedEmail, templateData);
	}

	private void sendReEnableEmail(ClientPortalStatusUpdateRequestBodyDto request, String normalizedEmail) {
		String templateId = this.settingsRepository
			.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_REENABLE_TEMPLATE_ID);
		Map<String, Object> templateData = new HashMap<>();
		templateData.put(TEMPLATE_KEY_CONTACT_FIRST_NAME,
				ClientPortalUrlHelper.resolveContactFirstName(request.getFirstName()));
		templateData.put(TEMPLATE_KEY_AGENCY_NAME, ClientPortalUrlHelper.resolveAgencyName(request.getAgencyName()));
		templateData.put(TEMPLATE_KEY_PORTAL_URL,
				ClientPortalUrlHelper.resolveClientLoginPortalUrl(this.applicationEnv));
		this.sendGridEmailService.sendEmailWithTemplate(templateId, normalizedEmail, templateData);
	}

	private Map<String, Object> buildInviteTemplateData(InviteTemplateContext context) {
		Map<String, Object> templateData = new HashMap<>();
		templateData.put(TEMPLATE_KEY_CONTACT_FIRST_NAME,
				ClientPortalUrlHelper.resolveContactFirstName(context.firstName()));
		templateData.put(TEMPLATE_KEY_AGENCY_USER_NAME,
				ClientPortalUrlHelper.resolveAgencyUserName(context.recruiterName()));
		templateData.put(TEMPLATE_KEY_AGENCY_NAME, ClientPortalUrlHelper.resolveAgencyName(context.agencyName()));
		templateData.put(TEMPLATE_KEY_PORTAL_URL,
				ClientPortalUrlHelper.resolveSignupPortalUrl(this.applicationEnv, context.email(),
						context.companyName(), context.accountId(), context.rcrmCompanyId(), context.rcrmContactId()));
		return templateData;
	}

	private ClientPortalStatusAction resolveAction(String action) {
		return ClientPortalStatusAction.fromValue(action)
			.orElseThrow(() -> new ValidationErrorException(ClientPortalStatusConstants.ACTION_VALIDATION_MESSAGE));
	}

	private String validateAndNormalizeEmail(String email) {
		if ((email == null) || email.isBlank()) {
			throw new ValidationErrorException(ClientPortalStatusUpdateRequestBodyDto.EMAIL_REQUIRED_MESSAGE);
		}
		String normalizedEmail = email.trim();
		if (this.emailValidationErrorHelper.validateClientEmailFormat(normalizedEmail) != null) {
			throw new ValidationErrorException(ClientPortalStatusUpdateRequestBodyDto.EMAIL_REQUIRED_MESSAGE);
		}
		return normalizedEmail;
	}

	private void validateRecruiterUserId(Integer recruiterUserId) {
		if ((recruiterUserId == null) || (recruiterUserId <= 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.RECRUITER_USER_ID_REQUIRED_MESSAGE);
		}
	}

	private void validateCompanyId(Integer companyId) {
		if ((companyId == null) || (companyId <= 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.COMPANY_ID_REQUIRED_MESSAGE);
		}
	}

	private void validateRcrmContactId(Integer rcrmContactId) {
		if ((rcrmContactId == null) || (rcrmContactId <= 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.RCRM_CONTACT_ID_REQUIRED_MESSAGE);
		}
	}

	private void validateContactAssignedToCompany(Integer rcrmContactId, Integer companyId, Integer accountId) {
		if (!this.invitableContactsRepository.existsContactAssignedToCompany(rcrmContactId, companyId, accountId)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.CONTACT_NOT_ASSIGNED_TO_COMPANY_MESSAGE);
		}
	}

	private void validateEmailNotActiveUnderAnotherAgency(String normalizedEmail, Integer accountId) {
		if (this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(normalizedEmail, accountId)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.PORTAL_ACCOUNT_EXISTS_MESSAGE);
		}
	}

	private void validateResendInviteEligibility(ClientPortalStatus existingEntity) {
		if (existingEntity == null) {
			throw new ResourceNotFoundException(ClientPortalStatusConstants.PORTAL_STATUS_NOT_FOUND_MESSAGE);
		}
		if (!Objects.equals(existingEntity.getPortalStatusId(),
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.RESEND_INVITE_INVALID_STATUS_MESSAGE);
		}
	}

	private int resolveNextInviteCount(ClientPortalStatus existingEntity) {
		if (existingEntity == null) {
			return 1;
		}
		Integer inviteCount = existingEntity.getInviteCount();
		if ((inviteCount == null) || (inviteCount <= 0)) {
			return 1;
		}
		return inviteCount + 1;
	}

	private void validateResendInviteRateLimit(ClientPortalStatus existingEntity, int nextInviteCount,
			int currentTimestamp) {
		Integer inviteSentOn = existingEntity.getInviteSentOn();
		boolean lastInviteSentOnIsToday = (inviteSentOn != null) && (inviteSentOn > 0)
				&& this.isSameUtcDay(inviteSentOn, currentTimestamp);
		if (lastInviteSentOnIsToday
				&& (((nextInviteCount - 1) % ClientPortalStatusConstants.MAX_DAILY_INVITE_COUNT) == 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.INVITE_RATE_LIMIT_MESSAGE);
		}
	}

	private int resolveEffectiveInviteCount(ClientPortalStatus existingEntity, int currentTimestamp) {
		if (existingEntity == null) {
			return 0;
		}
		Integer inviteSentOn = existingEntity.getInviteSentOn();
		if ((inviteSentOn == null) || (inviteSentOn <= 0) || !this.isSameUtcDay(inviteSentOn, currentTimestamp)) {
			return 0;
		}
		Integer inviteCount = existingEntity.getInviteCount();
		return (inviteCount != null) ? inviteCount : 0;
	}

	private boolean isSameUtcDay(int firstTimestamp, int secondTimestamp) {
		LocalDate firstDate = Instant.ofEpochSecond(firstTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
		LocalDate secondDate = Instant.ofEpochSecond(secondTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
		return firstDate.equals(secondDate);
	}

	private int getCurrentUnixTimestamp() {
		return Math.toIntExact(Instant.now().getEpochSecond());
	}

	private void validateGetPortalStatusRequest(String email, Integer rcrmContactId) {
		if ((email == null) || email.isBlank()) {
			throw new ValidationErrorException(ClientPortalStatusConstants.EMAIL_REQUIRED_MESSAGE);
		}

		if ((rcrmContactId == null) || (rcrmContactId <= 0)) {
			throw new ValidationErrorException(ClientPortalStatusConstants.RCRM_CONTACT_ID_REQUIRED_MESSAGE);
		}

		if (this.emailValidationErrorHelper.validateClientEmailFormat(email.trim()) != null) {
			throw new ValidationErrorException(ClientPortalStatusConstants.EMAIL_VALIDATION_MESSAGE);
		}
	}

	private Integer resolveAuthenticatedAccountId() {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		if ((accountId == null) || (accountId <= 0)) {
			throw new UnauthorizedAccessException(
					ClientPortalStatusConstants.AUTHENTICATED_ACCOUNT_ID_REQUIRED_MESSAGE);
		}
		return accountId;
	}

	private ClientPortalStatusResponseBodyDto buildDefaultResponse(boolean portalExistsOnCrossAgency) {
		return new ClientPortalStatusResponseBodyDto(ClientPortalStatusConstants.PORTAL_STATUS_NOT_SENT, null, null,
				null, 0, 0, 0, 0, portalExistsOnCrossAgency);
	}

	private ClientPortalStatusResponseBodyDto toResponseBodyDto(ClientPortalStatusQueryResultDto queryResult,
			boolean portalExistsOnCrossAgency) {
		return new ClientPortalStatusResponseBodyDto(queryResult.getPortalStatusId(), queryResult.getCompanyId(),
				queryResult.getVmsUserEmail(), queryResult.getVmsUserId(), queryResult.getInviteCount(),
				queryResult.getInviteSentOn(), queryResult.getInviteSentByUserId(), queryResult.getUpdatedOn(),
				portalExistsOnCrossAgency);
	}

	private ClientPortalStatusUpdateResponseBodyDto toUpdateResponseBodyDto(ClientPortalStatus entity,
			String normalizedEmail) {
		return new ClientPortalStatusUpdateResponseBodyDto(entity.getPortalStatusId(),
				this.resolvePortalStatusLabel(entity.getPortalStatusId()),
				(StringUtils.hasText(entity.getVmsUserEmail())) ? entity.getVmsUserEmail() : normalizedEmail);
	}

	private String resolvePortalStatusLabel(Integer portalStatusId) {
		if (portalStatusId == null) {
			return ClientPortalStatusConstants.PORTAL_STATUS_LABEL_NOT_SENT;
		}
		return switch (portalStatusId) {
			case ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT ->
				ClientPortalStatusConstants.PORTAL_STATUS_LABEL_INVITATION_SENT;
			case ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED ->
				ClientPortalStatusConstants.PORTAL_STATUS_LABEL_PORTAL_ENABLED;
			case ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED ->
				ClientPortalStatusConstants.PORTAL_STATUS_LABEL_PORTAL_DISABLED;
			default -> ClientPortalStatusConstants.PORTAL_STATUS_LABEL_NOT_SENT;
		};
	}

	private record BulkInviteToSend(ClientPortalStatusBulkContactDto contact, String normalizedEmail) {
	}

	private record InviteTemplateContext(String firstName, String recruiterName, String agencyName, String email,
			String companyName, Integer accountId, Integer rcrmCompanyId, Integer rcrmContactId) {
	}

}
