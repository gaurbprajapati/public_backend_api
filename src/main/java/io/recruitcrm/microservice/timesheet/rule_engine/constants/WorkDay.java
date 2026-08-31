/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import lombok.Getter;

@Getter
public enum WorkDay {

	MONDAY(1, "Monday"), TUESDAY(2, "Tuesday"), WEDNESDAY(3, "Wednesday"), THURSDAY(4, "Thursday"), FRIDAY(5, "Friday"),
	SATURDAY(6, "Saturday"), SUNDAY(7, "Sunday");

	private final String day;

	private final Integer id;

	WorkDay(Integer id, String day) {
		this.id = id;
		this.day = day;
	}

	public static WorkDay getWorkDayType(String day) {
		if (day == null || day.isEmpty()) {
			throw new IllegalArgumentException("Day cannot be null or empty");
		}

		for (WorkDay workDayType : WorkDay.values()) {
			if (workDayType.day.equalsIgnoreCase(day)) {
				return workDayType;
			}
		}

		throw new IllegalArgumentException("Invalid day: " + day);
	}

	public static WorkDay getWorkDayType(Integer day) {
		if (day == null) {
			throw new IllegalArgumentException("Day cannot be null");
		}

		for (WorkDay workDayType : WorkDay.values()) {
			if (workDayType.id.equals(day)) {
				return workDayType;
			}
		}

		throw new IllegalArgumentException("Invalid day id: " + day);
	}

}
