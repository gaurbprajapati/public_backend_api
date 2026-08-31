package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ModifyTimesheetPayBillStructurePermissionChecker extends BasePermissionChecker {

	public ModifyTimesheetPayBillStructurePermissionChecker(AuthHolder authHolder,
			TimesheetRepository timesheetRepository, AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		// For modifying pay/bill structure, we need to check if the user has permission
		// to edit the job. If job has been deleted (null), bypass job permission check.
		var job = getJobForTimesheetOrNull(context.getTimesheetId());
		if (job != null) {
			checkJobPermission(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					job.getOwnerId());
		}
	}

	@Override
	public void checkPermissionWithBulkContext(@NonNull PermissionCheckInternalContext context,
			@NonNull BulkPermissionCheckContext bulkContext) {
		// For modifying pay/bill structure, we need to check if the user has permission
		// to edit the job using preloaded data. If job has been deleted (null), bypass
		// job permission check.
		var job = getJobForTimesheetFromBulkContextOrNull(context.getTimesheetId(), bulkContext);
		if (job != null) {
			checkJobPermission(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					job.getOwnerId());
		}
	}

}