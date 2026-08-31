/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.exceptions.access_control;

public class UnknownAccessLevelException extends RuntimeException {

	public UnknownAccessLevelException(String message) {
		super(message);
	}

}
