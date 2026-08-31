/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import java.util.Set;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.repositories.user.UserRepository} tests.
 */
public final class UserRepositoryTestDataFactory {

	private UserRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultUserId() {
		return 1001;
	}

	public static Set<Integer> getSampleUserIds() {
		return Set.of(10, 20);
	}

	/**
	 * DTO matching JPQL constructor (display name and profile photo).
	 */
	public static UserDetailsQueryResultDto createUserDetailsQueryResultDto() {
		return new UserDetailsQueryResultDto("Reese Manager", "https://cdn.example/photos/rm.png");
	}

	public static String getSampleTimezoneOffset() {
		return "+05:30";
	}

}
