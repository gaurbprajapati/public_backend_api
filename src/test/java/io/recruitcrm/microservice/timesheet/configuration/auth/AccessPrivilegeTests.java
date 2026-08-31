/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessPrivilege Tests")
class AccessPrivilegeTests {

	@Test
	@DisplayName("AccessPrivilege class should be loadable and define no enum constants")
	void testAccessPrivilegeClassIsLoadableAndEmpty() {
		// Given

		// When
		AccessPrivilege[] values = AccessPrivilege.values();

		// Then
		assertThat(AccessPrivilege.class).isNotNull();
		assertThat(values).isEmpty();
	}

}
