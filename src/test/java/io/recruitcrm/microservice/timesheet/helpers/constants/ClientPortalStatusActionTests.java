/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClientPortalStatusAction}.
 */
class ClientPortalStatusActionTests {

	@Test
	@DisplayName("fromValue returns matching action for valid values")
	void testFromValueValidValuesReturnsMatchingAction() {
		assertThat(ClientPortalStatusAction.fromValue("send_invite")).contains(ClientPortalStatusAction.SEND_INVITE);
		assertThat(ClientPortalStatusAction.fromValue("resend_invite"))
			.contains(ClientPortalStatusAction.RESEND_INVITE);
		assertThat(ClientPortalStatusAction.fromValue("disable")).contains(ClientPortalStatusAction.DISABLE);
		assertThat(ClientPortalStatusAction.fromValue("re_enable")).contains(ClientPortalStatusAction.RE_ENABLE);
	}

	@Test
	@DisplayName("fromValue returns empty for blank or unknown values")
	void testFromValueBlankOrUnknownReturnsEmpty() {
		assertThat(ClientPortalStatusAction.fromValue(null)).isEmpty();
		assertThat(ClientPortalStatusAction.fromValue("")).isEmpty();
		assertThat(ClientPortalStatusAction.fromValue("unknown")).isEmpty();
	}

}
