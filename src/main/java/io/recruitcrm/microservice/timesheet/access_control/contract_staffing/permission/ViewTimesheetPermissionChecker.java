package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ViewTimesheetPermissionChecker extends BasePermissionChecker {

	public ViewTimesheetPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		/**
		 * For viewing timesheets, check if user has permission to view the candidate. If
		 * candidate has been deleted (null), bypass access control check to allow viewing
		 * timesheet data with graceful handling of deleted candidate information.
		 */
		var candidate = getCandidateForTimesheetOrNull(context.getTimesheetId());
		if (candidate != null) {
			checkCandidatePermission(
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_VIEW,
					candidate.getOwnerId());
		}
		// If candidate is null (deleted), bypass access control and allow view operation
	}

	@Override
	public void checkPermissionWithBulkContext(@NonNull PermissionCheckInternalContext context,
			@NonNull BulkPermissionCheckContext bulkContext) {
		/**
		 * For viewing timesheets, check if user has permission to view the candidate
		 * using preloaded data. If candidate has been deleted (null), bypass access
		 * control check.
		 */
		var candidate = bulkContext.getCandidatesByTimesheetId().get(context.getTimesheetId());
		if (candidate != null) {
			checkCandidatePermission(
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_VIEW,
					candidate.getOwnerId());
		}
		// If candidate is null (deleted), bypass access control and allow view operation
	}

}