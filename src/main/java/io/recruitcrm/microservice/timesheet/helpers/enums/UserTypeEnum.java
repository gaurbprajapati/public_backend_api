package io.recruitcrm.microservice.timesheet.helpers.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {

	COMPANY_CONTACT(1), AGENCY_RECRUITER(2), CONTRACTOR(3);

	private final Integer id;

	UserTypeEnum(Integer id) {
		this.id = id;
	}

}