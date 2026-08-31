/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

public enum WorkDayType {

	WORK_DAY(1, "Work Day"), DAY_OFF(2, "Day Off");

	private final String value;

	private final int id;

	WorkDayType(int id, String value) {
		this.id = id;
		this.value = value;
	}

	public static WorkDayType fromId(int id) {
		for (WorkDayType type : WorkDayType.values()) {
			if (type.id == id) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid WorkDayType id: " + id);
	}

}
