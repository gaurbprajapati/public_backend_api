/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.access_control.contract_staffing.constants.AccessControlMessageConstants}.
 */
public final class AccessControlMessageConstantsTestDataFactory {

	public static final String BULK_PERMISSION_CHECK_COMPLETED_PREFIX = "Bulk permission check completed for ";

	public static final String UTILITY_CLASS_CANNOT_BE_INSTANTIATED_MESSAGE = "This is a utility class and cannot be instantiated";

	private AccessControlMessageConstantsTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String getBulkPermissionCheckCompletedPrefix() {
		return BULK_PERMISSION_CHECK_COMPLETED_PREFIX;
	}

	public static String getUtilityClassCannotBeInstantiatedMessage() {
		return UTILITY_CLASS_CANNOT_BE_INSTANTIATED_MESSAGE;
	}

}
