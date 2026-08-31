package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TimesheetCanEditPermissionChecker extends BasePermissionChecker {

	public TimesheetCanEditPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	@Transactional
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		// Then check if the user has permission to edit both the candidate and job
		var candidate = getCandidateForTimesheet(context.getTimesheetId());
		checkCandidatePermission(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
				candidate.getOwnerId());
	}

}
