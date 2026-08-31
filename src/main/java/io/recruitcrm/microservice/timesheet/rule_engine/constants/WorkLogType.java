/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import lombok.Getter;

@Getter
public enum WorkLogType {

	START_AND_END_TIME(2), WORK_HOUR(1);

	private final int typeId;

	WorkLogType(int typeId) {
		this.typeId = typeId;
	}

	public static WorkLogType valueOf(int typeId) {
		for (WorkLogType type : WorkLogType.values()) {
			if (type.typeId == typeId) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid WorkLogType id: " + typeId);
	}

}
