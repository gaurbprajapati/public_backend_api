/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkContactDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusBulkSkipReason;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.SendGridSettingsConstants;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IBulkValidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IClientPortalStatusRepository;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IInvitableContactsRepository;
import io.recruitcrm.microservice.timesheet.repositories.settings.ISettingsRepository;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientPortalStatusService.ClientPortalStatusUpdateResult;
import io.recruitcrm.microservice.timesheet.services.sendgrid.ISendGridEmailService;
import io.recruitcrm.microservice.timesheet.testdata.ClientPortalStatusTestDataFactory;
import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link ClientPortalStatusService}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientPortalStatusServiceTests {

	@Mock
	private AuthHolder auth;

	@Mock
	private IClientPortalStatusRepository clientPortalStatusRepository;

	@Mock
	private IInvitableContactsRepository invitableContactsRepository;

	@Mock
	private ISendGridEmailService sendGridEmailService;

	@Mock
	private ISettingsRepository settingsRepository;

	@Mock
	private AccessControlHelper accessControlHelper;

	@Mock
	private IBulkValidateRepository bulkValidateRepository;

	private final EmailValidationErrorHelper emailValidationErrorHelper = new EmailValidationErrorHelper();

	private ClientPortalStatusService clientPortalStatusService;

	private static final String DEFAULT_APPLICATION_ENV = "production";

	private static final Integer DEFAULT_CURRENT_USER_ID = 56;

	private static final Integer DEFAULT_CONTACT_OWNER_ID = 56;

	@BeforeEach
	void setUp() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ClientPortalStatusTestDataFactory.getDefaultAccountId());
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(DEFAULT_CURRENT_USER_ID);
		Contacts contactsAcl = new Contacts();
		contactsAcl.setCanView("Everything");
		contactsAcl.setCanEdit("Everything");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(contactsAcl);
		given(this.accessControlHelper.requiresTeamLookup(any())).willReturn(false);
		given(this.accessControlHelper.resolvePermission(any(), any(), any(), any())).willReturn(true);
		given(this.bulkValidateRepository.findOwnerIdsByContactIds(anyList()))
			.willReturn(Map.of(ClientPortalStatusTestDataFactory.getDefaultRcrmContactId(), DEFAULT_CONTACT_OWNER_ID));
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_INVITE_TEMPLATE_ID))
			.willReturn(ClientPortalStatusTestDataFactory.getDefaultInviteTemplateId());
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_DISABLE_TEMPLATE_ID))
			.willReturn(ClientPortalStatusTestDataFactory.getDefaultDisableTemplateId());
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.CLIENT_PORTAL_REENABLE_TEMPLATE_ID))
			.willReturn(ClientPortalStatusTestDataFactory.getDefaultReenableTemplateId());
		willDoNothing().given(this.sendGridEmailService).sendEmailWithTemplate(any(), any(), any());
		given(this.invitableContactsRepository.existsContactAssignedToCompany(any(), any(), any())).willReturn(true);
		this.clientPortalStatusService = new ClientPortalStatusService(this.auth, this.clientPortalStatusRepository,
				this.invitableContactsRepository, this.emailValidationErrorHelper, this.sendGridEmailService,
				this.settingsRepository, this.accessControlHelper, this.bulkValidateRepository,
				DEFAULT_APPLICATION_ENV);
	}

	@Test
	@DisplayName("Get portal status returns mapped response when record exists")
	void testGetPortalStatusExistingRecordReturnsMappedResponse() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatusQueryResultDto queryResult = ClientPortalStatusTestDataFactory.createQueryResult();
		given(this.clientPortalStatusRepository.findByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(queryResult));

		// When
		ClientPortalStatusResponseBodyDto result = this.clientPortalStatusService.getPortalStatus(email,
				ClientPortalStatusTestDataFactory.getDefaultRcrmContactId());

		// Then
		assertThat(result).isEqualTo(ClientPortalStatusTestDataFactory.createResponseBody());
		then(this.clientPortalStatusRepository).should().findByVmsUserEmailAndAccountId(email, accountId);
		then(this.clientPortalStatusRepository).should(never())
			.existsPortalStatusUnderDifferentAccount(email, accountId);
	}

	@Test
	@DisplayName("Get portal status returns default not-sent response when record missing")
	void testGetPortalStatusMissingRecordReturnsDefaultResponse() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);

		// When
		ClientPortalStatusResponseBodyDto result = this.clientPortalStatusService.getPortalStatus(email,
				ClientPortalStatusTestDataFactory.getDefaultRcrmContactId());

		// Then
		assertThat(result).isEqualTo(ClientPortalStatusTestDataFactory.createDefaultNotSentResponseBody());
	}

	@Test
	@DisplayName("Get portal status returns cross-agency flag when record exists under another account")
	void testGetPortalStatusCrossAgencyRecordReturnsCrossAgencyFlag() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(true);

		// When
		ClientPortalStatusResponseBodyDto result = this.clientPortalStatusService.getPortalStatus(email,
				ClientPortalStatusTestDataFactory.getDefaultRcrmContactId());

		// Then
		assertThat(result).isEqualTo(ClientPortalStatusTestDataFactory.createCrossAgencyResponseBody());
	}

	@Test
	@DisplayName("Get portal status throws validation error when email is missing")
	void testGetPortalStatusMissingEmailThrowsValidationError() {
		// Given
		Integer rcrmContactId = ClientPortalStatusTestDataFactory.getDefaultRcrmContactId();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.getPortalStatus(null, rcrmContactId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.EMAIL_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Get portal status throws validation error when email is invalid")
	void testGetPortalStatusInvalidEmailThrowsValidationError() {
		// Given
		String invalidEmail = ClientPortalStatusTestDataFactory.getInvalidEmail();
		Integer rcrmContactId = ClientPortalStatusTestDataFactory.getDefaultRcrmContactId();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.getPortalStatus(invalidEmail, rcrmContactId))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.EMAIL_VALIDATION_MESSAGE);
	}

	@Test
	@DisplayName("Get portal status throws unauthorized when authenticated account id is missing")
	void testGetPortalStatusMissingAuthenticatedAccountIdThrowsUnauthorizedAccess() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(null);
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer rcrmContactId = ClientPortalStatusTestDataFactory.getDefaultRcrmContactId();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.getPortalStatus(email, rcrmContactId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.AUTHENTICATED_ACCOUNT_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Get portal status throws validation error when contact id is missing")
	void testGetPortalStatusMissingContactIdThrowsValidationError() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.getPortalStatus(email, null))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RCRM_CONTACT_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Get portal status throws unauthorized when user lacks view access for contact")
	void testGetPortalStatusMissingContactViewAccessThrowsUnauthorizedAccess() {
		// Given
		given(this.accessControlHelper.resolvePermission(any(), any(), any(), any())).willReturn(false);
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer rcrmContactId = ClientPortalStatusTestDataFactory.getDefaultRcrmContactId();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.getPortalStatus(email, rcrmContactId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.CONTACT_NOT_AUTHORIZED_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).findByVmsUserEmailAndAccountId(any(), any());
	}

	@Test
	@DisplayName("Update portal status send invite creates new record and sends email")
	void testUpdatePortalStatusSendInviteCreatesRecordAndSendsEmail() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusUpdateResult result = this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(result.successMessage()).isEqualTo(ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE);
		assertThat(result.responseBody().getPortalStatusId())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT);
		assertThat(result.responseBody().getPortalStatusLabel())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_INVITATION_SENT);
		ArgumentCaptor<Map> templateDataCaptor = ArgumentCaptor.forClass(Map.class);
		then(this.sendGridEmailService).should()
			.sendEmailWithTemplate(eq(ClientPortalStatusTestDataFactory.getDefaultInviteTemplateId()), eq(email),
					templateDataCaptor.capture());
		assertThat(templateDataCaptor.getValue()).containsEntry("contactFirstName", "Jane")
			.containsEntry("agencyUserName", "Alice Recruiter")
			.containsEntry("agencyName", ClientPortalStatusTestDataFactory.getDefaultAgencyName())
			.containsEntry("portalURL", ClientPortalStatusTestDataFactory.getDefaultSignupPortalUrl());
	}

	@Test
	@DisplayName("Update portal status resend invite increments invite count for same day record")
	void testUpdatePortalStatusResendInviteIncrementsInviteCount() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		int currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 1, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), currentTimestamp, currentTimestamp);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusUpdateResult result = this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(existingEntity.getInviteCount()).isEqualTo(2);
		assertThat(result.responseBody().getPortalStatusId())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT);
		ArgumentCaptor<Map> templateDataCaptor = ArgumentCaptor.forClass(Map.class);
		then(this.sendGridEmailService).should()
			.sendEmailWithTemplate(eq(ClientPortalStatusTestDataFactory.getDefaultInviteTemplateId()), eq(email),
					templateDataCaptor.capture());
		assertThat(templateDataCaptor.getValue()).containsEntry("contactFirstName", "Jane")
			.containsEntry("agencyUserName", "Alice Recruiter")
			.containsEntry("agencyName", ClientPortalStatusTestDataFactory.getDefaultAgencyName())
			.containsEntry("portalURL", ClientPortalStatusTestDataFactory.getDefaultSignupPortalUrl());
	}

	@Test
	@DisplayName("Update portal status send invite throws when email already has portal record regardless of status")
	void testUpdatePortalStatusSendInviteExistingRecordThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus disabledEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 1, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), 1_716_000_000, 1_716_000_000);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(disabledEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.INVITE_ALREADY_SENT_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status send invite throws when invitation already sent")
	void testUpdatePortalStatusSendInviteAlreadySentThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		int currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 1, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), currentTimestamp, currentTimestamp);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.INVITE_ALREADY_SENT_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status resend invite throws when daily invite limit reached")
	void testUpdatePortalStatusResendInviteRateLimitThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		int currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 3, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), currentTimestamp, currentTimestamp);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.INVITE_RATE_LIMIT_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status resend invite increments invite count when last invite was on previous day")
	void testUpdatePortalStatusResendInviteIncrementsInviteCountForPreviousDay() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 3, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), 1_716_000_000, 1_716_000_000);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(existingEntity.getInviteCount()).isEqualTo(4);
	}

	@Test
	@DisplayName("Update portal status send invite throws when email active under another agency")
	void testUpdatePortalStatusSendInviteExistingPortalUnderAnotherAgencyThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(true);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.PORTAL_ACCOUNT_EXISTS_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status resend invite is not blocked by the record under the same account")
	void testUpdatePortalStatusResendInviteSameAccountRecordDoesNotThrowPortalAccountExists() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 1, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), 1_716_000_000, 1_716_000_000);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusUpdateResult result = this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(result.successMessage()).isEqualTo(ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE);
		then(this.clientPortalStatusRepository).should().existsPortalStatusUnderDifferentAccount(email, accountId);
		then(this.clientPortalStatusRepository).should().save(existingEntity);
	}

	@Test
	@DisplayName("Update portal status resend invite throws when email has a portal record under another agency")
	void testUpdatePortalStatusResendInviteExistingPortalUnderAnotherAgencyThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(true);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.PORTAL_ACCOUNT_EXISTS_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status send invite throws when rcrmContactId is missing")
	void testUpdatePortalStatusSendInviteMissingRcrmContactIdThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setRcrmContactId(null);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RCRM_CONTACT_ID_REQUIRED_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status send invite throws when contact is not assigned to company")
	void testUpdatePortalStatusSendInviteContactNotAssignedToCompanyThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.invitableContactsRepository.existsContactAssignedToCompany(
				ClientPortalStatusTestDataFactory.getDefaultRcrmContactId(),
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), accountId))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.CONTACT_NOT_ASSIGNED_TO_COMPANY_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status disable updates record and sends disable email")
	void testUpdatePortalStatusDisableUpdatesRecordAndSendsEmail() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createDisableRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus existingEntity = ClientPortalStatusTestDataFactory.createClientPortalStatusEntity();
		existingEntity.setId(1);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusUpdateResult result = this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(existingEntity.getPortalStatusId())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED);
		assertThat(existingEntity.getInviteSentByUserId())
			.isEqualTo(ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId());
		assertThat(result.successMessage()).isEqualTo(ClientPortalStatusConstants.DISABLE_SUCCESS_MESSAGE);
		assertThat(result.responseBody().getPortalStatusLabel())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_PORTAL_DISABLED);
		ArgumentCaptor<Map> templateDataCaptor = ArgumentCaptor.forClass(Map.class);
		then(this.sendGridEmailService).should()
			.sendEmailWithTemplate(eq(ClientPortalStatusTestDataFactory.getDefaultDisableTemplateId()), eq(email),
					templateDataCaptor.capture());
		assertThat(templateDataCaptor.getValue()).containsEntry("contactFirstName", "Jane Doe")
			.containsEntry("agencyName", ClientPortalStatusTestDataFactory.getDefaultAgencyName());
	}

	@Test
	@DisplayName("Update portal status disable throws not found when record missing")
	void testUpdatePortalStatusDisableMissingRecordThrowsNotFound() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createDisableRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage(ClientPortalStatusConstants.PORTAL_STATUS_NOT_FOUND_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status re-enable updates existing record and sends re-enable email")
	void testUpdatePortalStatusReEnableUpdatesExistingRecord() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createReEnableRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus existingEntity = ClientPortalStatusTestDataFactory.createDisabledPortalStatusEntity();
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusUpdateResult result = this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(existingEntity.getPortalStatusId())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED);
		assertThat(result.successMessage()).isEqualTo(ClientPortalStatusConstants.ACCEPT_SUCCESS_MESSAGE);
		assertThat(result.responseBody().getPortalStatusLabel())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_PORTAL_ENABLED);
		ArgumentCaptor<Map> templateDataCaptor = ArgumentCaptor.forClass(Map.class);
		then(this.sendGridEmailService).should()
			.sendEmailWithTemplate(eq(ClientPortalStatusTestDataFactory.getDefaultReenableTemplateId()), eq(email),
					templateDataCaptor.capture());
		assertThat(templateDataCaptor.getValue()).containsEntry("contactFirstName", "Jane")
			.containsEntry("agencyName", ClientPortalStatusTestDataFactory.getDefaultAgencyName())
			.containsEntry("portalURL", "https://portal.recruitcrm.io/client/login");
	}

	@Test
	@DisplayName("Update portal status re-enable throws not found when record missing")
	void testUpdatePortalStatusReEnableMissingRecordThrowsNotFound() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createReEnableRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage(ClientPortalStatusConstants.PORTAL_STATUS_NOT_FOUND_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status throws validation error for invalid action")
	void testUpdatePortalStatusInvalidActionThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setAction("invalid_action");

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.ACTION_VALIDATION_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status send invite throws when recruiter user id missing")
	void testUpdatePortalStatusSendInviteMissingRecruiterUserIdThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setRecruiterUserId(null);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RECRUITER_USER_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status send invite throws when company id missing")
	void testUpdatePortalStatusSendInviteMissingCompanyIdThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setCompanyId(null);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.COMPANY_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status throws unauthorized when authenticated account id is missing")
	void testUpdatePortalStatusMissingAuthenticatedAccountIdThrowsUnauthorizedAccess() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(null);
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.AUTHENTICATED_ACCOUNT_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status send invite persists invite metadata on saved entity")
	void testUpdatePortalStatusSendInvitePersistsInviteMetadata() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		ArgumentCaptor<ClientPortalStatus> entityCaptor = ArgumentCaptor.forClass(ClientPortalStatus.class);
		given(this.clientPortalStatusRepository.save(entityCaptor.capture()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		ClientPortalStatus savedEntity = entityCaptor.getValue();
		assertThat(savedEntity.getVmsUserEmail()).isEqualTo(email);
		assertThat(savedEntity.getCompanyId()).isEqualTo(ClientPortalStatusTestDataFactory.getDefaultCompanyId());
		assertThat(savedEntity.getInviteSentByUserId())
			.isEqualTo(ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId());
		assertThat(savedEntity.getInviteCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("Bulk enable portal invites valid contacts and skips invalid ones")
	void testBulkEnablePortalInvitesValidContactsAndSkipsInvalidOnes() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		String invitedEmail = ClientPortalStatusTestDataFactory.getDefaultEmail();
		String skippedActiveEmail = "carol@example.com";
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus portalEnabledEntity = new ClientPortalStatus(2,
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 0, null, skippedActiveEmail, 0, 0, 0);
		given(this.clientPortalStatusRepository
			.findEntitiesByVmsUserEmailInAndAccountId(List.of(invitedEmail, skippedActiveEmail), accountId))
			.willReturn(List.of(portalEnabledEntity));
		given(this.clientPortalStatusRepository
			.findEmailsPortalEnabledUnderDifferentAccount(List.of(invitedEmail, skippedActiveEmail), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.saveAll(anyList()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).containsExactly(invitedEmail);
		assertThat(result.getInvitedCount()).isEqualTo(1);
		assertThat(result.getSkippedCount()).isEqualTo(2);
		assertThat(result.getSkipped()).extracting("reason")
			.contains(ClientPortalStatusBulkSkipReason.EMAIL_MISSING, ClientPortalStatusBulkSkipReason.PORTAL_ACTIVE);
		then(this.clientPortalStatusRepository).should().saveAll(anyList());
		then(this.sendGridEmailService).should()
			.sendEmailWithTemplate(eq(ClientPortalStatusTestDataFactory.getDefaultInviteTemplateId()), eq(invitedEmail),
					any(Map.class));
	}

	@Test
	@DisplayName("Bulk enable portal skips email taken under another agency")
	void testBulkEnablePortalSkipsEmailTakenUnderAnotherAgency() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(email), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(email), accountId))
			.willReturn(List.of(email));

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).isEmpty();
		assertThat(result.getSkipped()).hasSize(1);
		assertThat(result.getSkipped().get(0).getReason()).isEqualTo(ClientPortalStatusBulkSkipReason.EMAIL_TAKEN);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Bulk enable portal skips contacts that already have an invitation sent")
	void testBulkEnablePortalSkipsContactsWithInvitationAlreadySent() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		int currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		ClientPortalStatus invitationSentEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 1, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), currentTimestamp, currentTimestamp);
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(email), accountId))
			.willReturn(List.of(invitationSentEntity));
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(email), accountId))
			.willReturn(List.of());

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).isEmpty();
		assertThat(result.getSkipped().get(0).getReason())
			.isEqualTo(ClientPortalStatusBulkSkipReason.INVITE_ALREADY_SENT);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Bulk enable portal skips contacts that hit daily rate limit")
	void testBulkEnablePortalSkipsContactsAtDailyRateLimit() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		int currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		ClientPortalStatus rateLimitedEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 3, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), currentTimestamp, currentTimestamp);
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(email), accountId))
			.willReturn(List.of(rateLimitedEntity));
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(email), accountId))
			.willReturn(List.of());

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).isEmpty();
		assertThat(result.getSkipped().get(0).getReason()).isEqualTo(ClientPortalStatusBulkSkipReason.RATE_LIMIT);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
	}

	@Test
	@DisplayName("Bulk enable portal persists all invited contacts in a single saveAll call")
	void testBulkEnablePortalPersistsInvitedContactsInSingleSaveAllCall() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(email), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(email), accountId))
			.willReturn(List.of());
		ArgumentCaptor<List<ClientPortalStatus>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
		given(this.clientPortalStatusRepository.saveAll(saveAllCaptor.capture()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(saveAllCaptor.getValue()).hasSize(1);
		assertThat(saveAllCaptor.getValue().get(0).getVmsUserEmail()).isEqualTo(email);
		assertThat(saveAllCaptor.getValue().get(0).getCompanyId())
			.isEqualTo(ClientPortalStatusTestDataFactory.getDefaultCompanyId());
		assertThat(saveAllCaptor.getValue().get(0).getInviteSentByUserId())
			.isEqualTo(ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId());
		then(this.clientPortalStatusRepository).should().saveAll(anyList());
	}

	@Test
	@DisplayName("Bulk enable portal sends only one invitation when contacts share the same email")
	void testBulkEnablePortalSendsSingleInvitationForDuplicateEmails() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		ClientPortalStatusBulkContactDto duplicateContact = new ClientPortalStatusBulkContactDto(email, "Jane", "Doe",
				99);
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0),
				duplicateContact));
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(email, email),
				accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(email, email),
				accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.saveAll(anyList()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).containsExactly(email);
		assertThat(result.getInvitedCount()).isEqualTo(1);
		assertThat(result.getSkipped()).hasSize(1);
		assertThat(result.getSkipped().get(0).getReason()).isEqualTo(ClientPortalStatusBulkSkipReason.DUPLICATE_EMAIL);
		then(this.sendGridEmailService).should()
			.sendEmailWithTemplate(eq(ClientPortalStatusTestDataFactory.getDefaultInviteTemplateId()), eq(email),
					any(Map.class));
		then(this.sendGridEmailService).shouldHaveNoMoreInteractions();
	}

	@Test
	@DisplayName("Bulk enable portal throws when contacts list is missing")
	void testBulkEnablePortalMissingContactsThrowsValidationError() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = new ClientPortalStatusBulkRequestBodyDto(null, 56,
				ClientPortalStatusTestDataFactory.createSendInviteRequest().getRecruiterName(),
				ClientPortalStatusTestDataFactory.getDefaultAgencyName(), null, 78, null);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.BULK_CONTACTS_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Bulk enable portal throws when contacts list is empty")
	void testBulkEnablePortalEmptyContactsThrowsValidationError() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of());

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.BULK_CONTACTS_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Bulk enable portal throws unauthorized when request account id differs from authenticated account")
	void testBulkEnablePortalMismatchedRequestAccountIdThrowsUnauthorizedAccess() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setAccountId(ClientPortalStatusTestDataFactory.getUnauthorizedAccountId());
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.UNAUTHORIZED_ACCOUNT_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
	}

	@Test
	@DisplayName("Bulk enable portal throws validation error when contact is not assigned to company")
	void testBulkEnablePortalContactNotAssignedToCompanyThrowsValidationError() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		given(this.invitableContactsRepository.existsContactAssignedToCompany(
				ClientPortalStatusTestDataFactory.getDefaultRcrmContactId(),
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(),
				ClientPortalStatusTestDataFactory.getDefaultAccountId()))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.CONTACT_NOT_ASSIGNED_TO_COMPANY_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
	}

	@Test
	@DisplayName("Bulk enable portal skips company validation for contacts without rcrm contact id")
	void testBulkEnablePortalSkipsCompanyValidationWhenRcrmContactIdMissing() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setAccountId(null);
		request.setContacts(List.of(new ClientPortalStatusBulkContactDto(null, "Bob", "Lee", null)));
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(), accountId))
			.willReturn(List.of());

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).isEmpty();
		assertThat(result.getSkipped()).hasSize(1);
		then(this.invitableContactsRepository).should(never()).existsContactAssignedToCompany(any(), any(), any());
	}

	@Test
	@DisplayName("Update portal status throws unauthorized when user lacks view and edit access for contact")
	void testUpdatePortalStatusMissingContactAccessThrowsUnauthorizedAccess() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		given(this.accessControlHelper.resolvePermission(any(), any(), any(), any())).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.CONTACT_NOT_AUTHORIZED_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
	}

	@Test
	@DisplayName("Bulk enable portal throws unauthorized when user lacks view and edit access for a contact")
	void testBulkEnablePortalMissingContactAccessThrowsUnauthorizedAccess() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		given(this.accessControlHelper.resolvePermission(any(), any(), any(), any())).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.BULK_CONTACTS_NOT_AUTHORIZED_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
	}

	@Test
	@DisplayName("Bulk enable portal collects only valid emails when batch mixes null invalid and valid emails")
	void testBulkEnablePortalCollectsOnlyValidEmailsForBatchLookup() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		String validEmail = ClientPortalStatusTestDataFactory.getDefaultEmail();
		request.setContacts(List.of(new ClientPortalStatusBulkContactDto(validEmail, "Jane", "Doe", 11_111),
				new ClientPortalStatusBulkContactDto(null, "Bob", "Lee", null),
				new ClientPortalStatusBulkContactDto("not-an-email", "Carol", "Kim", 22_222)));
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(validEmail),
				accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(validEmail),
				accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.saveAll(anyList()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).containsExactly(validEmail);
		assertThat(result.getSkipped()).hasSize(2);
		assertThat(result.getSkipped()).extracting("reason")
			.containsOnly(ClientPortalStatusBulkSkipReason.EMAIL_MISSING);
		then(this.clientPortalStatusRepository).should()
			.findEntitiesByVmsUserEmailInAndAccountId(List.of(validEmail), accountId);
	}

	@Test
	@DisplayName("Bulk enable portal invites existing contact whose last invite was on a previous day")
	void testBulkEnablePortalInvitesExistingContactFromPreviousDay() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus previousDayEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 3, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), 1_716_000_000, 1_716_000_000);
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(email), accountId))
			.willReturn(List.of(previousDayEntity));
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(email), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.saveAll(anyList()))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).containsExactly(email);
		assertThat(previousDayEntity.getInviteCount()).isEqualTo(1);
		assertThat(previousDayEntity.getPortalStatusId())
			.isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT);
	}

	@Test
	@DisplayName("Bulk enable portal throws when company id is not positive")
	void testBulkEnablePortalNonPositiveCompanyIdThrowsValidationError() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		request.setCompanyId(-1);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.COMPANY_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Bulk enable portal throws when recruiter user id is not positive")
	void testBulkEnablePortalNonPositiveRecruiterUserIdThrowsValidationError() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(ClientPortalStatusTestDataFactory.createBulkInviteRequest().getContacts().get(0)));
		request.setRecruiterUserId(0);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.bulkEnablePortal(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RECRUITER_USER_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Bulk enable portal trims blank email to null in skipped contact dto")
	void testBulkEnablePortalSkippedContactDtoReportsBlankEmailAsTrimmed() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(new ClientPortalStatusBulkContactDto("   ", "Bob", "Lee", null)));
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(), accountId))
			.willReturn(List.of());

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getSkipped()).hasSize(1);
		assertThat(result.getSkipped().get(0).getReason()).isEqualTo(ClientPortalStatusBulkSkipReason.EMAIL_MISSING);
		assertThat(result.getSkipped().get(0).getEmail()).isEqualTo("   ");
	}

	@Test
	@DisplayName("Bulk enable portal skips contact whose email format is invalid")
	void testBulkEnablePortalSkipsContactWithInvalidEmailFormat() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		request.setContacts(List.of(new ClientPortalStatusBulkContactDto("not-an-email", "Jane", "Doe", 11_111)));
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(List.of(), accountId))
			.willReturn(List.of());
		given(this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(), accountId))
			.willReturn(List.of());

		// When
		ClientPortalStatusBulkResponseBodyDto result = this.clientPortalStatusService.bulkEnablePortal(request);

		// Then
		assertThat(result.getInvited()).isEmpty();
		assertThat(result.getSkipped()).hasSize(1);
		assertThat(result.getSkipped().get(0).getReason()).isEqualTo(ClientPortalStatusBulkSkipReason.EMAIL_MISSING);
		then(this.clientPortalStatusRepository).should(never()).saveAll(anyList());
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Resolve portal status label maps invitation sent status id to its label")
	void testResolvePortalStatusLabelInvitationSentReturnsInvitationSentLabel() {
		// When
		String label = ReflectionTestUtils.invokeMethod(this.clientPortalStatusService, "resolvePortalStatusLabel",
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT);

		// Then
		assertThat(label).isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_INVITATION_SENT);
	}

	@Test
	@DisplayName("Resolve portal status label maps null status id to the not-sent label")
	void testResolvePortalStatusLabelNullStatusReturnsNotSentLabel() {
		// When
		String label = ReflectionTestUtils.invokeMethod(this.clientPortalStatusService, "resolvePortalStatusLabel",
				(Object) null);

		// Then
		assertThat(label).isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_NOT_SENT);
	}

	@Test
	@DisplayName("Resolve portal status label maps portal disabled status id to its label")
	void testResolvePortalStatusLabelPortalDisabledReturnsPortalDisabledLabel() {
		// When
		String label = ReflectionTestUtils.invokeMethod(this.clientPortalStatusService, "resolvePortalStatusLabel",
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED);

		// Then
		assertThat(label).isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_PORTAL_DISABLED);
	}

	@Test
	@DisplayName("Resolve portal status label maps portal enabled status id to its label")
	void testResolvePortalStatusLabelPortalEnabledReturnsPortalEnabledLabel() {
		// When
		String label = ReflectionTestUtils.invokeMethod(this.clientPortalStatusService, "resolvePortalStatusLabel",
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED);

		// Then
		assertThat(label).isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_PORTAL_ENABLED);
	}

	@Test
	@DisplayName("Resolve portal status label maps an unknown status id to the not-sent label")
	void testResolvePortalStatusLabelUnknownStatusReturnsNotSentLabel() {
		// When
		String label = ReflectionTestUtils.invokeMethod(this.clientPortalStatusService, "resolvePortalStatusLabel",
				999);

		// Then
		assertThat(label).isEqualTo(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_NOT_SENT);
	}

	@Test
	@DisplayName("Update portal status throws unauthorized when authenticated account id is not positive")
	void testUpdatePortalStatusNonPositiveAuthenticatedAccountIdThrowsUnauthorizedAccess() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(0);
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessage(ClientPortalStatusConstants.AUTHENTICATED_ACCOUNT_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status resend invite increments invite count when last invite has no timestamp")
	void testUpdatePortalStatusResendInviteIncrementsWhenInviteSentOnMissing() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 3, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), null, null);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(existingEntity.getInviteCount()).isEqualTo(4);
	}

	@Test
	@DisplayName("Update portal status resend invite throws when portal status is not invitation sent")
	void testUpdatePortalStatusResendInviteNonInvitationSentStatusThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus disabledEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 0, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), 0, 0);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(disabledEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RESEND_INVITE_INVALID_STATUS_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status resend invite throws when no portal status record exists")
	void testUpdatePortalStatusResendInviteMissingRecordThrowsResourceNotFound() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage(ClientPortalStatusConstants.PORTAL_STATUS_NOT_FOUND_MESSAGE);
		then(this.clientPortalStatusRepository).should(never()).save(any(ClientPortalStatus.class));
		then(this.sendGridEmailService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Update portal status resend invite allows same-day resend below the daily limit boundary")
	void testUpdatePortalStatusResendInviteSameDayBelowLimitIncrementsInviteCount() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createResendInviteRequest();
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		int currentTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		ClientPortalStatus existingEntity = new ClientPortalStatus(1,
				ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT, accountId,
				ClientPortalStatusTestDataFactory.getDefaultCompanyId(), 1, null, email,
				ClientPortalStatusTestDataFactory.getDefaultRecruiterUserId(), currentTimestamp, currentTimestamp);
		given(this.clientPortalStatusRepository.findEntityByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(existingEntity));
		given(this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId))
			.willReturn(false);
		given(this.clientPortalStatusRepository.save(any(ClientPortalStatus.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.clientPortalStatusService.updatePortalStatus(request);

		// Then
		assertThat(existingEntity.getInviteCount()).isEqualTo(2);
	}

	@Test
	@DisplayName("Update portal status send invite throws when company id is not positive")
	void testUpdatePortalStatusSendInviteNonPositiveCompanyIdThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setCompanyId(-5);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.COMPANY_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status send invite throws when rcrmContactId is not positive")
	void testUpdatePortalStatusSendInviteNonPositiveRcrmContactIdThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setRcrmContactId(0);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RCRM_CONTACT_ID_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("Update portal status send invite throws when recruiter user id is not positive")
	void testUpdatePortalStatusSendInviteNonPositiveRecruiterUserIdThrowsValidationError() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		request.setRecruiterUserId(0);

		// When & Then
		assertThatThrownBy(() -> this.clientPortalStatusService.updatePortalStatus(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(ClientPortalStatusConstants.RECRUITER_USER_ID_REQUIRED_MESSAGE);
	}

}
