/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.services.user.UserTimezoneService} tests.
 */
public final class UserTimezoneServiceTestDataFactory {

	private UserTimezoneServiceTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Default GMT offset returned when no user-specific timezone is available (UTC).
	 */
	public static String getDefaultGmtDifference() {
		return "+00:00";
	}

	/**
	 * Representative authenticated user id used when exercising repository lookups.
	 */
	public static Integer getSampleUserId() {
		return 42;
	}

	/**
	 * A non-blank timezone offset as stored for a user row.
	 */
	public static String getNonBlankTimezoneOffset() {
		return "+05:30";
	}

}
