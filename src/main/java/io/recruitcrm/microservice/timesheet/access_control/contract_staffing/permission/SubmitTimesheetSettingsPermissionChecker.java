package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SubmitTimesheetSettingsPermissionChecker extends BasePermissionChecker {

	public SubmitTimesheetSettingsPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		// For submitting timesheet settings, we need to check if the user has permission
		// to edit the candidate
		var candidate = getCandidateForTimesheet(context.getTimesheetId());
		checkCandidatePermission(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
				candidate.getOwnerId());
	}

	@Override
	public void checkPermissionWithBulkContext(@NonNull PermissionCheckInternalContext context,
			@NonNull BulkPermissionCheckContext bulkContext) {
		// For submitting timesheet settings, we need to check if the user has permission
		// to edit the candidate using preloaded data
		var candidate = getCandidateForTimesheetFromBulkContext(context.getTimesheetId(), bulkContext);
		checkCandidatePermission(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
				candidate.getOwnerId());
	}

}