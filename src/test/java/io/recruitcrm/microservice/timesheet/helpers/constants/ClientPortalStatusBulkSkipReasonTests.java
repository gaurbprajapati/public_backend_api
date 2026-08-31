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
 * Unit tests for {@link ClientPortalStatusBulkSkipReason}.
 */
class ClientPortalStatusBulkSkipReasonTests {

	@Test
	@DisplayName("Private constructor throws UnsupportedOperationException when invoked via reflection")
	void testPrivateConstructorThrowsUnsupportedOperationException() throws NoSuchMethodException {
		Constructor<ClientPortalStatusBulkSkipReason> constructor = ClientPortalStatusBulkSkipReason.class
			.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("Skip reason constants expose the expected codes")
	void testSkipReasonConstantsExposeExpectedCodes() {
		assertThat(ClientPortalStatusBulkSkipReason.EMAIL_MISSING).isEqualTo("email_missing");
		assertThat(ClientPortalStatusBulkSkipReason.PORTAL_ACTIVE).isEqualTo("portal_active");
		assertThat(ClientPortalStatusBulkSkipReason.EMAIL_TAKEN).isEqualTo("email_taken");
		assertThat(ClientPortalStatusBulkSkipReason.RATE_LIMIT).isEqualTo("rate_limit");
		assertThat(ClientPortalStatusBulkSkipReason.DUPLICATE_EMAIL).isEqualTo("duplicate_email");
		assertThat(ClientPortalStatusBulkSkipReason.INVITE_ALREADY_SENT).isEqualTo("invite_already_sent");
	}

}
