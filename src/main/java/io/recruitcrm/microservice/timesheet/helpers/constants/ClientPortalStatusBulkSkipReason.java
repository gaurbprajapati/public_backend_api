/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Skip reason codes for bulk client portal invite responses.
 */
public final class ClientPortalStatusBulkSkipReason {

	public static final String EMAIL_MISSING = "email_missing";

	public static final String PORTAL_ACTIVE = "portal_active";

	public static final String EMAIL_TAKEN = "email_taken";

	public static final String RATE_LIMIT = "rate_limit";

	public static final String DUPLICATE_EMAIL = "duplicate_email";

	public static final String INVITE_ALREADY_SENT = "invite_already_sent";

	private ClientPortalStatusBulkSkipReason() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

}
