/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import lombok.Getter;

@Getter
public enum ChargeMethodType {

	MULTIPLIER(1), FIXED_RATE(2);

	private final int id;

	ChargeMethodType(int id) {
		this.id = id;
	}

	public static ChargeMethodType fromId(int id) {
		for (ChargeMethodType type : ChargeMethodType.values()) {
			if (type.id == id) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid ChargeType id: " + id);
	}

}
