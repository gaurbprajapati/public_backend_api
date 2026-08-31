package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PermissionCheckerFactory {

	private final Map<Permission, BasePermissionChecker> permissionCheckers;

	public PermissionCheckerFactory(CreateTimesheetPermissionChecker createTimesheetPermissionChecker,
			DeleteTimesheetPermissionChecker deleteTimesheetPermissionChecker,
			ViewTimesheetPermissionChecker viewTimesheetPermissionChecker,
			ModifyTimesheetPayBillStructurePermissionChecker modifyTimesheetPayBillStructurePermissionChecker,
			TimesheetApprovalPermissionChecker timesheetApprovalPermissionChecker,
			SubmitTimesheetSettingsPermissionChecker submitTimesheetSettingsPermissionChecker,
			TimesheetCanEditPermissionChecker timesheetCanEditPermissionChecker) {
		this.permissionCheckers = Map.ofEntries(
				Map.entry(Permission.CREATE_TIMESHEET, createTimesheetPermissionChecker),
				Map.entry(Permission.CREATE_TIMESHEET_SETTINGS, createTimesheetPermissionChecker),
				Map.entry(Permission.DELETE_TIMESHEET, deleteTimesheetPermissionChecker),
				Map.entry(Permission.VIEW_TIMESHEET, viewTimesheetPermissionChecker),
				Map.entry(Permission.VIEW_CONTRACTOR_DETAILS_PAGE, viewTimesheetPermissionChecker),
				Map.entry(Permission.MODIFY_TIMESHEET_PAY_BILL_STRUCTURE,
						modifyTimesheetPayBillStructurePermissionChecker),
				Map.entry(Permission.APPROVE_TIMESHEET, timesheetApprovalPermissionChecker),
				Map.entry(Permission.REJECT_TIMESHEET, timesheetApprovalPermissionChecker),
				Map.entry(Permission.CONFIGURE_TIMESHEET_SETTINGS, submitTimesheetSettingsPermissionChecker),
				Map.entry(Permission.ADD_TIME_IN_TIMESHEET, submitTimesheetSettingsPermissionChecker),
				Map.entry(Permission.SUBMIT_TIMESHEET, submitTimesheetSettingsPermissionChecker),
				Map.entry(Permission.EDIT_TIMESHEET, timesheetCanEditPermissionChecker));
	}

	public BasePermissionChecker getPermissionChecker(Permission permission) {
		var checker = this.permissionCheckers.get(permission);
		if (checker == null) {
			throw new UnauthorizedAccessException(
					String.format("Permission '%s' is not supported in the current implementation", permission));
		}
		return checker;
	}

}