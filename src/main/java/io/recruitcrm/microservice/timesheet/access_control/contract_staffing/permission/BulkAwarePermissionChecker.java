/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;

/**
 * Interface for permission checkers that can work with bulk context to optimize
 * performance by avoiding repeated database queries
 */
public interface BulkAwarePermissionChecker {

	/**
	 * Checks permission using preloaded bulk context data
	 * @param context The permission check context
	 * @param bulkContext Preloaded data for bulk operations
	 */
	void checkPermissionWithBulkContext(PermissionCheckInternalContext context, BulkPermissionCheckContext bulkContext);

}