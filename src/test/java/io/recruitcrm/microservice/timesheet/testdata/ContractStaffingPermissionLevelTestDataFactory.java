/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel}.
 */
public final class ContractStaffingPermissionLevelTestDataFactory {

	/** String that does not match {@code YES} or {@code NO} labels. */
	public static final String INVALID_PERMISSION_LEVEL_LABEL = "unspecified_level";

	private ContractStaffingPermissionLevelTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
