package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DeleteTimesheetPermissionChecker extends BasePermissionChecker {

	public DeleteTimesheetPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		var candidate = getCandidateForTimesheetOrNull(context.getTimesheetId());
		if (candidate != null) {
			checkCandidatePermission(
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_DELETE,
					candidate.getOwnerId());
		}
		// If candidate is null (deleted), bypass access control and allow delete
		// operation
	}

	@Override
	public void checkPermissionWithBulkContext(@NonNull PermissionCheckInternalContext context,
			@NonNull BulkPermissionCheckContext bulkContext) {
		var candidate = bulkContext.getCandidatesByTimesheetId().get(context.getTimesheetId());
		if (candidate != null) {
			checkCandidatePermission(
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_DELETE,
					candidate.getOwnerId());
		}
		// If candidate is null (deleted), bypass access control and allow delete
		// operation
	}

}