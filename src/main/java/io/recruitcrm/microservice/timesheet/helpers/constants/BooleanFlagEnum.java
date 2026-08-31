package io.recruitcrm.microservice.timesheet.helpers.constants;

import lombok.Getter;

@Getter
public enum BooleanFlagEnum {

	FALSE(0), TRUE(1);

	private final Integer value;

	BooleanFlagEnum(Integer value) {
		this.value = value;
	}

}
