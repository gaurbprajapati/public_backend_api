package io.recruitcrm.microservice.timesheet.helpers.enums;

import lombok.Getter;

@Getter
public enum AccountUserEnum {

	USERTYPEID(2);

	private final Integer id;

	AccountUserEnum(Integer id) {
		this.id = id;
	}

}
