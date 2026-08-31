/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker}
 * unit tests.
 */
public final class AccessControlCheckerTestDataFactory {

	public static final Integer CURRENT_USER_ID = 123;

	public static final Integer OWNER_ID = 456;

	public static final Integer OTHER_OWNER_ID = 999;

	public static final String MSG_GLOBAL_NO_OWNER_ID = "Global permissions do not support owner ID";

	public static final String MSG_GLOBAL_ONLY_ALLOWED_PERMISSION = "Global permissions only support ALLOWED permission type";

	public static final String MSG_GLOBAL_PERMISSION_REQUIRED = "Global permission must be specified for Entity.GLOBAL";

	public static final String MSG_NON_GLOBAL_NO_GLOBAL_PERMISSION = "Global permission should not be specified for non-global entities";

	public static final String MSG_USE_GLOBAL_FOUR_ARG_SIMPLIFIED = "For global permissions, use allows(Entity.GLOBAL, Permission.ALLOWED, null, GlobalPermission)";

	public static final String MSG_USE_GLOBAL_FIVE_ARG_WITH_LEVEL = "For global permissions, use allows(Entity.GLOBAL, Permission.ALLOWED, requestedLevel, null, GlobalPermission)";

	public static final String MSG_GLOBAL_LEVELS_YES_OR_NO_ONLY = "Global permissions only support YES or NO permission levels";

	private AccessControlCheckerTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
