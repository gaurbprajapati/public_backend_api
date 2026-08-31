/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.entity.model.UserMfaSettings;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository.UserMfaSettingsRepository}
 * tests.
 */
public final class UserMfaSettingsRepositoryTestDataFactory {

	private UserMfaSettingsRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Default recruiter user id used in MFA settings lookups.
	 */
	public static Integer getDefaultUserId() {
		return 123;
	}

	/**
	 * Populated {@link UserMfaSettings} aligned with service-layer tests for consistent
	 * delegation assertions.
	 */
	public static UserMfaSettings createUserMfaSettings() {
		UserMfaSettings settings = new UserMfaSettings();
		settings.setUserId(123);
		settings.setAccountId(456);
		settings.setWebMfaLogin(true);
		settings.setMobileMfaLogin(true);
		settings.setSecretKey("test-secret-key");
		settings.setMfaEnforceBy("admin");
		settings.setMfaEnforcedDate(1_625_097_600);
		return settings;
	}

}
