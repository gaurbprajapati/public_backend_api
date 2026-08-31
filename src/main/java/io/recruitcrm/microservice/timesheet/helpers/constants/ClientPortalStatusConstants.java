/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Constants for client portal status lookup and lifecycle values.
 */
public final class ClientPortalStatusConstants {

	public static final int PORTAL_STATUS_NOT_SENT = 0;

	public static final int PORTAL_STATUS_INVITATION_SENT = 1;

	public static final int PORTAL_STATUS_PORTAL_ENABLED = 2;

	public static final int PORTAL_STATUS_PORTAL_DISABLED = 3;

	public static final int MAX_DAILY_INVITE_COUNT = 3;

	public static final String FETCH_SUCCESS_MESSAGE = "Portal status fetched successfully";

	public static final String INVITE_SUCCESS_MESSAGE = "Invitation sent successfully";

	public static final String DISABLE_SUCCESS_MESSAGE = "Portal disabled successfully";

	public static final String ACCEPT_SUCCESS_MESSAGE = "Portal enabled successfully";

	public static final String EMAIL_AND_ACCOUNT_ID_REQUIRED_MESSAGE = "email and account_id are required";

	public static final String EMAIL_REQUIRED_MESSAGE = "email is required";

	public static final String EMAIL_VALIDATION_MESSAGE = "email is required and must be a valid email address";

	public static final String ACCOUNT_ID_VALIDATION_MESSAGE = "account_id is required and must be a positive integer";

	public static final String AUTHENTICATED_ACCOUNT_ID_REQUIRED_MESSAGE = "Authenticated account id is required";

	public static final String ACTION_VALIDATION_MESSAGE = "action must be one of: send_invite, resend_invite, disable, re_enable";

	public static final String RECRUITER_USER_ID_REQUIRED_MESSAGE = "recruiterUserId is required";

	public static final String COMPANY_ID_REQUIRED_MESSAGE = "companyId is required to route to the correct HMP account";

	public static final String RCRM_CONTACT_ID_REQUIRED_MESSAGE = "rcrmContactId is required";

	public static final String CONTACT_NOT_ASSIGNED_TO_COMPANY_MESSAGE = "Contact is not assigned to the specified company";

	public static final String INVITE_ALREADY_SENT_MESSAGE = "An invitation has already been sent to this email address.";

	public static final String RESEND_INVITE_INVALID_STATUS_MESSAGE = "Invitation can only be resent when the portal status is invitation sent.";

	public static final String INVITE_RATE_LIMIT_MESSAGE = "Invitation limit reached. Maximum 3 invitations per day.";

	public static final String PORTAL_ACCOUNT_EXISTS_MESSAGE = "A portal account already exists with this email address.";

	public static final String PORTAL_STATUS_NOT_FOUND_MESSAGE = "No portal status record found for the given email and account";

	public static final String UNAUTHORIZED_ACCOUNT_MESSAGE = "Access denied for the requested account";

	public static final String CONTACT_NOT_AUTHORIZED_MESSAGE = "You are not authorised for this contact";

	public static final String BULK_CONTACTS_NOT_AUTHORIZED_MESSAGE = "You do not have authorisation";

	public static final String BULK_INVITE_SUCCESS_MESSAGE = "Bulk invite processed";

	public static final String BULK_ACCOUNT_ID_AND_CONTACTS_REQUIRED_MESSAGE = "contacts must be a non-empty list and account_id is required";

	public static final String BULK_CONTACTS_REQUIRED_MESSAGE = "contacts must be a non-empty list";

	public static final String PORTAL_STATUS_LABEL_NOT_SENT = "Not Sent";

	public static final String PORTAL_STATUS_LABEL_INVITATION_SENT = "Invitation Sent";

	public static final String PORTAL_STATUS_LABEL_PORTAL_ENABLED = "Portal Enabled";

	public static final String PORTAL_STATUS_LABEL_PORTAL_DISABLED = "Portal Disabled";

	private ClientPortalStatusConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

}
