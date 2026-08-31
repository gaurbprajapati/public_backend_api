package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class TimesheetApprovalPermissionChecker extends BasePermissionChecker {

	public TimesheetApprovalPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		super(authHolder, timesheetRepository, accessControlChecker);
	}

	@Override
	public void checkPermission(@NonNull PermissionCheckInternalContext context) {
		// First check if the user is an approver
		Boolean isApprover = timesheetRepository.validateIsApprover(context.getTimesheetId(),
				authHolder.getAuthenticationPrincipalUniqueIdentifier(), UserTypeEnum.AGENCY_RECRUITER.getId());

		if (Boolean.FALSE.equals(isApprover)) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("User with ID: {0} is not an approver for timesheet ID: {1}",
							authHolder.getAuthenticationPrincipalUniqueIdentifier(), context.getTimesheetId()));
		}

		/**
		 * Check if the user has permission to edit both the candidate and job. If
		 * candidate or job has been deleted (null), bypass their respective permission
		 * checks to allow updating timesheet status with graceful handling of deleted
		 * entities.
		 */
		var candidate = getCandidateForTimesheetOrNull(context.getTimesheetId());
		if (candidate != null) {
			checkCandidatePermission(
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					candidate.getOwnerId());
		}

		var job = getJobForTimesheetOrNull(context.getTimesheetId());
		if (job != null) {
			checkJobPermission(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					job.getOwnerId());
		}
	}

	@Override
	public void checkPermissionWithBulkContext(@NonNull PermissionCheckInternalContext context,
			@NonNull BulkPermissionCheckContext bulkContext) {
		// First check if the user is an approver
		Boolean isApprover = timesheetRepository.validateIsApprover(context.getTimesheetId(),
				authHolder.getAuthenticationPrincipalUniqueIdentifier(), UserTypeEnum.AGENCY_RECRUITER.getId());

		if (Boolean.FALSE.equals(isApprover)) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("User with ID: {0} is not an approver for timesheet ID: {1}",
							authHolder.getAuthenticationPrincipalUniqueIdentifier(), context.getTimesheetId()));
		}

		/**
		 * Check if the user has permission to edit both the candidate and job using
		 * preloaded data. If candidate or job has been deleted (null), bypass their
		 * respective permission checks.
		 */
		var candidate = bulkContext.getCandidatesByTimesheetId().get(context.getTimesheetId());
		if (candidate != null) {
			checkCandidatePermission(
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					candidate.getOwnerId());
		}

		var job = getJobForTimesheetFromBulkContextOrNull(context.getTimesheetId(), bulkContext);
		if (job != null) {
			checkJobPermission(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					job.getOwnerId());
		}
	}

}