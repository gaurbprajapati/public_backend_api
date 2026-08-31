/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

public enum PermissionLevel {

	YES("Yes"), NO("No");

	private final String label;

	PermissionLevel(String label) {
		this.label = label;
	}

	public static PermissionLevel fromValue(String value) {
		for (PermissionLevel level : PermissionLevel.values()) {
			if (level.label.equalsIgnoreCase(value)) {
				return level;
			}
		}
		throw new IllegalArgumentException("Unknown permission level: " + value);
	}

	@Override
	public String toString() {
		return this.label;
	}

}
