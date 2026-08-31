/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity}.
 */
public final class RecruitCrmCoreEntityTestDataFactory {

	/** Value that does not match any {@code Entity} label in {@code fromValue}. */
	public static final String INVALID_ENTITY_LABEL = "unknown_recruitcrm_core_entity";

	/**
	 * Prefix of the {@link IllegalArgumentException} message from
	 * {@code Entity.fromValue}.
	 */
	public static final String FROM_VALUE_NOT_FOUND_MESSAGE_PREFIX = "No enum constant for value:";

	private RecruitCrmCoreEntityTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
