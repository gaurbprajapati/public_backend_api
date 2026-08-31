/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import lombok.Getter;

@Getter
public enum PermissionLevel {

	YES("Yes"), NO("No"), NOTHING("Nothing"), EVERYTHING("Everything"), TEAM_ONLY("Team Only"),
	OWNED_ONLY("Owned Only");

	private final String label;

	PermissionLevel(String label) {
		this.label = label;
	}

	public static PermissionLevel fromLabel(String label) {
		for (PermissionLevel level : PermissionLevel.values()) {
			if (level.label.equalsIgnoreCase(label)) {
				return level;
			}
		}
		throw new IllegalArgumentException("Unknown permission level: " + label);
	}

	@Override
	public String toString() {
		return this.label;
	}

}
