/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.BasePermissionChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.PermissionCheckerFactory;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component(AccessLevelHandler.BEAN_NAME)
public class AccessLevelHandler {

	public static final String BEAN_NAME = "contractStaffingAccessLevelHandler";

	private final PermissionCheckerFactory permissionCheckerFactory;

	public AccessLevelHandler(PermissionCheckerFactory permissionCheckerFactory) {
		this.permissionCheckerFactory = permissionCheckerFactory;
	}

	public void checkAccess(
			@NonNull io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity entity,
			@NonNull PermissionCheckContext permissionCheckContext,
			@NonNull AccessControlCheckMetadataContext accessControlCheckMetadataContext) {
		io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission contractStaffingPermission = permissionCheckContext
			.getPermission();

		/**
		 * Try to use a specific permission checker
		 */
		BasePermissionChecker checker = this.permissionCheckerFactory.getPermissionChecker(contractStaffingPermission);
		if (checker == null) {
			throw new UnauthorizedAccessException(String.format("No permission checker exists for %s on entity %s",
					contractStaffingPermission, entity));
		}

		/**
		 * If we have a checker, use it to verify the permission
		 */
		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(entity)
			.permission(contractStaffingPermission)
			.permissionLevel(permissionCheckContext.getPermissionLevel())
			.timesheetId(accessControlCheckMetadataContext.getTimesheetId())
			.build();
		checker.checkPermission(context);
	}

}
