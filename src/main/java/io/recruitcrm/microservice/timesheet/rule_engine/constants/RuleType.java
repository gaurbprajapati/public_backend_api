/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

public enum RuleType {

	RANGE_BASED_AFTER_SHIFT(1, "After Shift"), RANGE_BASED_BEFORE_SHIFT(2, "Before Shift"),
	RANGE_BASED_SPECIFIC_TIME_RANGE(3, "Specific Time Range"), RANGE_BASED_DAILY_OVERTIME(4, "Daily Overtime"),
	RANGE_BASED_WEEKLY_OVERTIME(5, "Weekly Overtime"),

	DURATION_BASED_SPECIFIC_HOUR_RANGE(6, "Specific Hour Range"), DURATION_BASED_DAILY_OVERTIME(7, "Daily Overtime"),
	DURATION_BASED_WEEKLY_OVERTIME(8, "Weekly Overtime"),

	RANGE_BASED_REGULAR_HOURS(11, "Regular Hours"), DURATION_BASED_REGULAR_HOURS(12, "Regular Hours"),
	RANGE_BASED_BREAK(9, "Break"), DURATION_BASED_BREAK(10, "Break"),

	RANGE_BASED_DEFAULT_PAY(13, "Default Pay"), DURATION_BASED_DEFAULT_PAY(14, "Default Pay");

	private final String label;

	private final int id;

	RuleType(int id, String label) {
		this.id = id;
		this.label = label;
	}

	public static RuleType fromId(int id) {
		for (RuleType type : RuleType.values()) {
			if (type.id == id) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid RuleType id: " + id);
	}

}
