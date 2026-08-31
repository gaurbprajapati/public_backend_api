/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import lombok.Getter;

@Getter
public enum Permission {

	CAN_ADD("canadd"), CAN_EDIT("canedit"), CAN_VIEW("canview"), CAN_DELETE("candelete"), OWNER_CHANGE("ownerchange"),
	FILE_ACCESS("fileaccess"), ALLOWED("allowed");

	private final String value;

	Permission(String value) {
		this.value = value;
	}

	public static Permission fromValue(String value) {
		for (Permission permission : Permission.values()) {
			if (permission.value.equals(value)) {
				return permission;
			}
		}
		throw new IllegalArgumentException("Unknown action: " + value);
	}

	@Override
	public String toString() {
		return this.value;
	}

}
