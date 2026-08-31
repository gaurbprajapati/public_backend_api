package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CreateTimesheetPermissionChecker extends BasePermissionChecker {

	public CreateTimesheetPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		// For creating timesheets, we only need to check if the user has permission to
		// add candidates
		checkCandidatePermission(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_ADD,
				null);
	}

	@Override
	public void checkPermissionWithBulkContext(@NonNull PermissionCheckInternalContext context,
			@NonNull BulkPermissionCheckContext bulkContext) {
		// For creating timesheets, we only need to check if the user has permission to
		// add candidates (no timesheet-specific data needed)
		checkCandidatePermission(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_ADD,
				null);
	}

}