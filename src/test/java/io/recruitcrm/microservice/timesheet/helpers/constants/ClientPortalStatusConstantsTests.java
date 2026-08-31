/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClientPortalStatusConstants}.
 */
class ClientPortalStatusConstantsTests {

	@Test
	@DisplayName("Private constructor throws UnsupportedOperationException when invoked via reflection")
	void testPrivateConstructorThrowsUnsupportedOperationException() throws NoSuchMethodException {
		Constructor<ClientPortalStatusConstants> constructor = ClientPortalStatusConstants.class
			.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("Status constants expose the expected lifecycle values")
	void testStatusConstantsExposeExpectedValues() {
		assertThat(ClientPortalStatusConstants.PORTAL_STATUS_NOT_SENT).isZero();
		assertThat(ClientPortalStatusConstants.PORTAL_STATUS_INVITATION_SENT).isEqualTo(1);
		assertThat(ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED).isEqualTo(2);
		assertThat(ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_DISABLED).isEqualTo(3);
		assertThat(ClientPortalStatusConstants.PORTAL_STATUS_LABEL_NOT_SENT).isEqualTo("Not Sent");
	}

}
