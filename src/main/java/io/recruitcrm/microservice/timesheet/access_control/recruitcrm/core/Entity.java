/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

public enum Entity {

	CANDIDATES("candidates"), CONTACTS("contacts"), JOBS("jobs"), COMPANIES("companies"), DEALS("deals"),
	PLACEMENT_BILLING("placementbilling"), TASK_MEETINGS("taskmeetings"), NOTES("notes"), CALL_LOG("calllog"),
	FILES("files"), GLOBAL("global");

	private final String label;

	Entity(String label) {
		this.label = label;
	}

	public static Entity fromValue(String value) {
		for (Entity entity : Entity.values()) {
			if (entity.label.equalsIgnoreCase(value)) {
				return entity;
			}
		}
		throw new IllegalArgumentException("No enum constant for value: " + value);
	}

	@Override
	public String toString() {
		return this.label;
	}

}
