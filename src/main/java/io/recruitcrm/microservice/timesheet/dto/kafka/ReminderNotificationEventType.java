package io.recruitcrm.microservice.timesheet.dto.kafka;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Distinguishes user-initiated reminder notifications from scheduled (CRON) digests.
 */
public enum ReminderNotificationEventType {

	REALTIME("timesheet.realtime"), CRON("timesheet.cron");

	private final String displayName;

	ReminderNotificationEventType(final String displayName) {
		this.displayName = displayName;
	}

	@JsonValue
	public String getDisplayName() {
		return this.displayName;
	}

}
