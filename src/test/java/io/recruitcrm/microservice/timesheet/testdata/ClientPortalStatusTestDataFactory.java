/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkContactDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkSkippedContactDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusBulkSkipReason;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusAction;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientPortalStatusService.ClientPortalStatusUpdateResult;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test data factory for client portal status endpoint tests.
 */
public final class ClientPortalStatusTestDataFactory {

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final String DEFAULT_EMAIL = "jane@example.com";

	private static final Integer DEFAULT_PORTAL_STATUS_ID = 1;

	private static final Integer DEFAULT_VMS_USER_ID = 42;

	private static final Integer DEFAULT_INVITE_COUNT = 1;

	private static final Integer DEFAULT_INVITE_SENT_ON = 1_716_700_000;

	private static final Integer DEFAULT_COMPANY_ID = 78;

	private static final Integer DEFAULT_RECRUITER_USER_ID = 56;

	private static final String DEFAULT_RECRUITER_NAME = "Alice Recruiter";

	private static final String DEFAULT_AGENCY_NAME = "RecruitCRM Pvt Ltd";

	private static final String DEFAULT_COMPANY_NAME = "Acme Corp";

	private static final Integer DEFAULT_RCRM_CONTACT_ID = 11_111;

	private static final String DEFAULT_FIRST_NAME = "Jane";

	private static final String DEFAULT_LAST_NAME = "Doe";

	private static final String DEFAULT_INVITE_TEMPLATE_ID = "d-invite-template";

	private static final String DEFAULT_DISABLE_TEMPLATE_ID = "d-disable-template";

	private static final String DEFAULT_REENABLE_TEMPLATE_ID = "d-reenable-template";

	private static final Integer DEFAULT_UPDATED_ON = 1_716_700_000;

	private static final Integer UNAUTHORIZED_ACCOUNT_ID = 999;

	private static final String INVALID_EMAIL = "not-an-email";

	private ClientPortalStatusTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return DEFAULT_ACCOUNT_ID;
	}

	public static Integer getUnauthorizedAccountId() {
		return UNAUTHORIZED_ACCOUNT_ID;
	}

	public static String getDefaultEmail() {
		return DEFAULT_EMAIL;
	}

	public static String getInvalidEmail() {
		return INVALID_EMAIL;
	}

	public static ClientPortalStatus createClientPortalStatusEntity() {
		return new ClientPortalStatus(null, DEFAULT_PORTAL_STATUS_ID, DEFAULT_ACCOUNT_ID, DEFAULT_COMPANY_ID,
				DEFAULT_INVITE_COUNT, DEFAULT_VMS_USER_ID, DEFAULT_EMAIL, DEFAULT_RECRUITER_USER_ID,
				DEFAULT_INVITE_SENT_ON, DEFAULT_UPDATED_ON);
	}

	public static ClientPortalStatus createDisabledPortalStatusEntity() {
		return new ClientPortalStatus(1, ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED, DEFAULT_ACCOUNT_ID,
				DEFAULT_COMPANY_ID, DEFAULT_INVITE_COUNT, DEFAULT_VMS_USER_ID, DEFAULT_EMAIL, DEFAULT_RECRUITER_USER_ID,
				DEFAULT_INVITE_SENT_ON, DEFAULT_UPDATED_ON);
	}

	public static ClientPortalStatus createPortalEnabledEntityUnderDifferentAccount() {
		return new ClientPortalStatus(2, ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED,
				UNAUTHORIZED_ACCOUNT_ID, DEFAULT_COMPANY_ID, DEFAULT_INVITE_COUNT, DEFAULT_VMS_USER_ID, DEFAULT_EMAIL,
				DEFAULT_RECRUITER_USER_ID, DEFAULT_INVITE_SENT_ON, DEFAULT_UPDATED_ON);
	}

	public static ClientPortalStatusUpdateRequestBodyDto createSendInviteRequest() {
		return new ClientPortalStatusUpdateRequestBodyDto(ClientPortalStatusAction.SEND_INVITE.getValue(),
				DEFAULT_EMAIL, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, DEFAULT_RECRUITER_USER_ID, DEFAULT_RECRUITER_NAME,
				DEFAULT_AGENCY_NAME, DEFAULT_COMPANY_NAME, DEFAULT_COMPANY_ID, DEFAULT_RCRM_CONTACT_ID);
	}

	public static ClientPortalStatusUpdateRequestBodyDto createResendInviteRequest() {
		return new ClientPortalStatusUpdateRequestBodyDto(ClientPortalStatusAction.RESEND_INVITE.getValue(),
				DEFAULT_EMAIL, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, DEFAULT_RECRUITER_USER_ID, DEFAULT_RECRUITER_NAME,
				DEFAULT_AGENCY_NAME, DEFAULT_COMPANY_NAME, DEFAULT_COMPANY_ID, DEFAULT_RCRM_CONTACT_ID);
	}

	public static ClientPortalStatusUpdateRequestBodyDto createDisableRequest() {
		return new ClientPortalStatusUpdateRequestBodyDto(ClientPortalStatusAction.DISABLE.getValue(), DEFAULT_EMAIL,
				DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, DEFAULT_RECRUITER_USER_ID, null, DEFAULT_AGENCY_NAME, null, null,
				null);
	}

	public static ClientPortalStatusUpdateRequestBodyDto createReEnableRequest() {
		return new ClientPortalStatusUpdateRequestBodyDto(ClientPortalStatusAction.RE_ENABLE.getValue(), DEFAULT_EMAIL,
				DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, null, null, DEFAULT_AGENCY_NAME, null, null, null);
	}

	public static ClientPortalStatusUpdateResponseBodyDto createUpdateResponseBody() {
		return new ClientPortalStatusUpdateResponseBodyDto(DEFAULT_PORTAL_STATUS_ID,
				ClientPortalStatusConstants.PORTAL_STATUS_LABEL_INVITATION_SENT, DEFAULT_EMAIL);
	}

	public static ClientPortalStatusUpdateResult createUpdateResult() {
		return new ClientPortalStatusUpdateResult(createUpdateResponseBody(),
				ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE);
	}

	public static String getDefaultInviteTemplateId() {
		return DEFAULT_INVITE_TEMPLATE_ID;
	}

	public static String getDefaultDisableTemplateId() {
		return DEFAULT_DISABLE_TEMPLATE_ID;
	}

	public static String getDefaultReenableTemplateId() {
		return DEFAULT_REENABLE_TEMPLATE_ID;
	}

	public static String getDefaultAgencyName() {
		return DEFAULT_AGENCY_NAME;
	}

	public static Integer getDefaultRecruiterUserId() {
		return DEFAULT_RECRUITER_USER_ID;
	}

	public static Integer getDefaultCompanyId() {
		return DEFAULT_COMPANY_ID;
	}

	public static Integer getDefaultRcrmContactId() {
		return DEFAULT_RCRM_CONTACT_ID;
	}

	public static ClientPortalStatusQueryResultDto createQueryResult() {
		return new ClientPortalStatusQueryResultDto(DEFAULT_PORTAL_STATUS_ID, DEFAULT_COMPANY_ID, DEFAULT_EMAIL,
				DEFAULT_VMS_USER_ID, DEFAULT_INVITE_COUNT, DEFAULT_INVITE_SENT_ON, DEFAULT_RECRUITER_USER_ID,
				DEFAULT_UPDATED_ON);
	}

	public static ClientPortalStatusResponseBodyDto createResponseBody() {
		return new ClientPortalStatusResponseBodyDto(DEFAULT_PORTAL_STATUS_ID, DEFAULT_COMPANY_ID, DEFAULT_EMAIL,
				DEFAULT_VMS_USER_ID, DEFAULT_INVITE_COUNT, DEFAULT_INVITE_SENT_ON, DEFAULT_RECRUITER_USER_ID,
				DEFAULT_UPDATED_ON, false);
	}

	public static ClientPortalStatusResponseBodyDto createDefaultNotSentResponseBody() {
		return new ClientPortalStatusResponseBodyDto(0, null, null, null, 0, 0, 0, 0, false);
	}

	public static ClientPortalStatusResponseBodyDto createCrossAgencyResponseBody() {
		return new ClientPortalStatusResponseBodyDto(0, null, null, null, 0, 0, 0, 0, true);
	}

	public static ResponseEntity<APINormalResponse<ClientPortalStatusResponseBodyDto>> createSuccessResponseEntity(
			ClientPortalStatusResponseBodyDto responseBody) {
		return new ResponseEntity<>(new APINormalResponse<>(responseBody), HttpStatus.OK);
	}

	public static String getDefaultSignupPortalUrl() {
		return "https://portal.recruitcrm.io/signup?email=" + DEFAULT_EMAIL + "&companyName=Acme%20Corp" + "&accountId="
				+ DEFAULT_ACCOUNT_ID + "&rcrmCompanyId=" + DEFAULT_COMPANY_ID + "&rcrmContactId="
				+ DEFAULT_RCRM_CONTACT_ID;
	}

	public static ClientPortalStatusBulkRequestBodyDto createBulkInviteRequest() {
		return new ClientPortalStatusBulkRequestBodyDto(DEFAULT_ACCOUNT_ID, DEFAULT_RECRUITER_USER_ID,
				DEFAULT_RECRUITER_NAME, DEFAULT_AGENCY_NAME, DEFAULT_COMPANY_NAME, DEFAULT_COMPANY_ID,
				List.of(new ClientPortalStatusBulkContactDto(DEFAULT_EMAIL, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
						DEFAULT_RCRM_CONTACT_ID), new ClientPortalStatusBulkContactDto(null, "Bob", "Lee", null),
						new ClientPortalStatusBulkContactDto("carol@example.com", "Carol", "Kim", null)));
	}

	public static ClientPortalStatusBulkResponseBodyDto createBulkInviteResponse() {
		return new ClientPortalStatusBulkResponseBodyDto(List.of(DEFAULT_EMAIL),
				List.of(new ClientPortalStatusBulkSkippedContactDto(null, "Bob",
						ClientPortalStatusBulkSkipReason.EMAIL_MISSING),
						new ClientPortalStatusBulkSkippedContactDto("carol@example.com", "Carol",
								ClientPortalStatusBulkSkipReason.PORTAL_ACTIVE)),
				1, 2);
	}

	public static final class Messages {

		public static final String FETCH_SUCCESS = "Portal status fetched successfully";

		public static final String INVITE_SUCCESS = ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE;

		private Messages() {
			throw new UnsupportedOperationException("Utility class");
		}

	}

}
