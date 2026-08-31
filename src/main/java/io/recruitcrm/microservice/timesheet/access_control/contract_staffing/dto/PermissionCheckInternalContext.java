package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Context object containing all parameters needed for permission checking. This makes the
 * permission checking system more flexible as new parameters can be added without
 * changing method signatures.
 */
@Getter
@Builder
public class PermissionCheckInternalContext {

	@NonNull
	private final Entity entity;

	@NonNull
	private final Permission permission;

	@NonNull
	private final PermissionLevel permissionLevel;

	private final Integer timesheetId;

	private final Integer candidateId;

	private final Integer jobId;

	/**
	 * Additional fields can be added here in the future without breaking existing code
	 */

}