/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.constants;

import java.util.Arrays;
import java.util.Optional;

/**
 * Supported actions for {@code POST /v1/portal/client/portal-status}.
 */
public enum ClientPortalStatusAction {

	SEND_INVITE("send_invite"),

	RESEND_INVITE("resend_invite"),

	DISABLE("disable"),

	RE_ENABLE("re_enable");

	private final String value;

	ClientPortalStatusAction(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static Optional<ClientPortalStatusAction> fromValue(String value) {
		if ((value == null) || value.isBlank()) {
			return Optional.empty();
		}
		return Arrays.stream(values()).filter((action) -> action.value.equals(value)).findFirst();
	}

}
