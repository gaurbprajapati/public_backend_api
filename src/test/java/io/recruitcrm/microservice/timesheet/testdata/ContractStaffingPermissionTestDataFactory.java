/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission}.
 */
public final class ContractStaffingPermissionTestDataFactory {

	/**
	 * Value that does not match any
	 * {@link io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission}
	 * label.
	 */
	public static final String INVALID_PERMISSION_ACTION_VALUE = "unknown_contract_action";

	/**
	 * Matches {@code Permission.fromValue} failure messages
	 * ({@code Unknown action: ...}).
	 */
	public static final String UNKNOWN_ACTION_MESSAGE_PREFIX = "Unknown action:";

	/**
	 * Expected
	 * {@link io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission#values()}
	 * length.
	 */
	public static final int PERMISSION_ENUM_CONSTANT_COUNT = 15;

	private ContractStaffingPermissionTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
