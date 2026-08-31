/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

public enum Entity {

	TIMESHEET("timesheet"), TIMESHEET_SETTINGS("timesheet_settings"), CANDIDATE("candidate"), JOB("job");

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
