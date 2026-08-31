/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import org.springframework.stereotype.Component;

@Component(AccessControlChecker.BEAN_NAME)
public class AccessControlChecker {

	public static final String BEAN_NAME = "contractStaffingAccessControlChecker";

	private final AccessLevelHandler accessLevelHandler;

	private final BulkPermissionChecker bulkPermissionChecker;

	public AccessControlChecker(AccessLevelHandler accessLevelHandler, BulkPermissionChecker bulkPermissionChecker) {
		this.accessLevelHandler = accessLevelHandler;
		this.bulkPermissionChecker = bulkPermissionChecker;
	}

	public void allows(Entity entity, PermissionCheckContext permissionCheckContext,
			AccessControlCheckMetadataContext accessControlCheckMetadataContext) {
		this.accessLevelHandler.checkAccess(entity, permissionCheckContext, accessControlCheckMetadataContext);
	}

	/**
	 * Performs bulk permission checking for multiple entities with optimized data loading
	 * @param request The bulk permission check request
	 * @return Bulk permission check results
	 */
	public BulkPermissionCheckResult allowsBulk(BulkPermissionCheckRequest request) {
		return this.bulkPermissionChecker.checkBulkPermissions(request);
	}

}
