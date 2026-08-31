/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.constants;

/**
 * Message constants for access control API responses.
 */
public final class AccessControlMessageConstants {

	private AccessControlMessageConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Prefix for successful bulk permission check responses (append count and entity
	 * type).
	 */
	public static final String BULK_PERMISSION_CHECK_COMPLETED_PREFIX = "Bulk permission check completed for ";

}
