/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import lombok.Getter;

@Getter
public enum TimesheetFrequency {

	CUSTOM(1), WEEKLY(2), BIWEEKLY(3), MONTHLY(4);

	private final int frequencyId;

	TimesheetFrequency(int frequencyId) {
		this.frequencyId = frequencyId;
	}

	public static TimesheetFrequency valueOf(int frequencyId) {
		for (TimesheetFrequency frequency : TimesheetFrequency.values()) {
			if (frequency.frequencyId == frequencyId) {
				return frequency;
			}
		}
		throw new IllegalArgumentException("Invalid TimesheetFrequency id: " + frequencyId);
	}

}
