/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

/**
 * Interface for DTOs that have basic access level properties. This interface defines the
 * contract for entities that support basic CRUD operations.
 */
public interface BasicAccessLevel {

	/**
	 * Gets the permission level for adding new records.
	 * @return The access level string
	 */
	String getCanAdd();

	/**
	 * Gets the permission level for editing existing records.
	 * @return The access level string
	 */
	String getCanEdit();

	/**
	 * Gets the permission level for viewing records.
	 * @return The access level string
	 */
	String getCanView();

	/**
	 * Gets the permission level for deleting records.
	 * @return The access level string
	 */
	String getCanDelete();

}