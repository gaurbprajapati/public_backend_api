/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.webhook_kafka_event;

import lombok.Getter;

@Getter
public enum WebhookEvent {

	/**
	 * Fired when one or more timesheets are approved. Payload contains account_id and ids
	 * (array of timesheet IDs).
	 */
	TIMESHEET_APPROVED("timesheet.approved"), TIMESHEET_SUBMITTED("timesheet.submitted");

	private final String eventName;

	WebhookEvent(String eventName) {
		this.eventName = eventName;
	}

}
