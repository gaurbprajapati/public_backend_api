/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

/**
 * Interface for DTOs that have extended access level properties. This interface extends
 * BasicAccessLevel to include additional permissions like file access and owner change
 * capabilities.
 */
public interface ExtendedAccessLevel extends BasicAccessLevel {

	/**
	 * Gets the permission level for file access.
	 * @return The access level string
	 */
	String getFileAccess();

	/**
	 * Gets the permission level for changing ownership.
	 * @return The access level string
	 */
	String getOwnerChange();

}