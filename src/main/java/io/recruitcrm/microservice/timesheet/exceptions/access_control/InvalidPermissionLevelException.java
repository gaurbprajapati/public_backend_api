/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.exceptions.access_control;

/**
 * Exception thrown when a permission level is invalid for a given permission type. For
 * example, when a permission level other than YES or NO is used for CAN_ADD, or when YES
 * or NO is used for other permission types.
 */
public class InvalidPermissionLevelException extends RuntimeException {

	public InvalidPermissionLevelException(String message) {
		super(message);
	}

	public InvalidPermissionLevelException(String message, Throwable cause) {
		super(message, cause);
	}

}