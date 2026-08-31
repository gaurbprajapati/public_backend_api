/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException}
 * tests.
 */
public final class UnknownAccessLevelExceptionTestDataFactory {

	private UnknownAccessLevelExceptionTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Representative error detail when persisting or resolving an access level string is
	 * not mapped.
	 */
	public static String getSampleUnknownAccessLevelMessage() {
		return "Unknown access level: PORTAL_FINANCE for permission bulkCheck";
	}

	/**
	 * Message including characters that commonly appear in interpolation or log output.
	 */
	public static String getSampleMessageWithSpecialCharacters() {
		return "Unknown access level '99' (accountId=1001, entity=TIMESHEET)";
	}

}
