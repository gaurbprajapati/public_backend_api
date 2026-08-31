package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public abstract class BasePermissionChecker implements BulkAwarePermissionChecker {

	protected final AuthHolder authHolder;

	protected final TimesheetRepository timesheetRepository;

	protected final AccessControlChecker accessControlChecker;

	protected BasePermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
			AccessControlChecker accessControlChecker) {
		this.authHolder = authHolder;
		this.timesheetRepository = timesheetRepository;
		this.accessControlChecker = accessControlChecker;
	}

	/**
	 * Checks if the user has the required permission based on the provided context. This
	 * method is abstract and must be implemented by concrete permission checkers.
	 * @param context The context containing all parameters needed for permission checking
	 */
	public abstract void checkPermission(@NonNull PermissionCheckInternalContext context);

	/**
	 * Convenience method that creates a PermissionCheckContext from individual
	 * parameters. This method delegates to checkPermission(PermissionCheckContext) after
	 * building the context.
	 */
	public void checkPermission(@NonNull Entity entity, @NonNull Permission permission,
			@NonNull PermissionLevel permissionLevel, @NonNull Integer timesheetId) {
		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(entity)
			.permission(permission)
			.permissionLevel(permissionLevel)
			.timesheetId(timesheetId)
			.build();
		checkPermission(context);
	}

	protected Candidate getCandidateForTimesheet(Integer timesheetId) {
		Candidate candidate = this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId,
				this.authHolder.getAuthenticationPrincipalOrganizationIdentifier());
		if (candidate == null) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("Candidate not found for timesheet ID: {0}", timesheetId));
		}
		return candidate;
	}

	/**
	 * Gets candidate for timesheet without throwing exception if not found. Returns null
	 * if candidate is deleted or not found. Used for graceful handling in view
	 * operations.
	 */
	protected Candidate getCandidateForTimesheetOrNull(Integer timesheetId) {
		return this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId,
				this.authHolder.getAuthenticationPrincipalOrganizationIdentifier());
	}

	protected Job getJobForTimesheet(Integer timesheetId) {
		Job job = this.timesheetRepository.getJobLinkedToTimesheet(timesheetId,
				this.authHolder.getAuthenticationPrincipalOrganizationIdentifier());
		if (job == null) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("Job not found for timesheet ID: {0}", timesheetId));
		}
		return job;
	}

	/**
	 * Gets job for timesheet without throwing exception if not found. Returns null if job
	 * is deleted or not found. Used for graceful handling in view operations.
	 */
	protected Job getJobForTimesheetOrNull(Integer timesheetId) {
		return this.timesheetRepository.getJobLinkedToTimesheet(timesheetId,
				this.authHolder.getAuthenticationPrincipalOrganizationIdentifier());
	}

	protected void checkCandidatePermission(
			io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission permission,
			Integer ownerId) {
		this.accessControlChecker.allows(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES, permission,
				ownerId);
	}

	protected void checkJobPermission(
			io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission permission,
			Integer ownerId) {
		this.accessControlChecker.allows(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.JOBS, permission, ownerId);
	}

	/**
	 * Default implementation of bulk-aware permission checking. Falls back to regular
	 * permission checking if not overridden.
	 */
	@Override
	public void checkPermissionWithBulkContext(PermissionCheckInternalContext context,
			BulkPermissionCheckContext bulkContext) {
		/**
		 * Default implementation falls back to regular permission checking
		 */
		checkPermission(context);
	}

	/**
	 * Gets candidate for timesheet using preloaded bulk context data
	 */
	protected Candidate getCandidateForTimesheetFromBulkContext(Integer timesheetId,
			BulkPermissionCheckContext bulkContext) {
		Candidate candidate = bulkContext.getCandidatesByTimesheetId().get(timesheetId);
		if (candidate == null) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("Candidate not found for timesheet ID: {0}", timesheetId));
		}
		return candidate;
	}

	/**
	 * Gets job for timesheet using preloaded bulk context data
	 */
	protected Job getJobForTimesheetFromBulkContext(Integer timesheetId, BulkPermissionCheckContext bulkContext) {
		Job job = bulkContext.getJobsByTimesheetId().get(timesheetId);
		if (job == null) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("Job not found for timesheet ID: {0}", timesheetId));
		}
		return job;
	}

	/**
	 * Gets job for timesheet using preloaded bulk context data without throwing exception
	 * if not found. Returns null if job is deleted or not found. Used for graceful
	 * handling in operations.
	 */
	protected Job getJobForTimesheetFromBulkContextOrNull(Integer timesheetId, BulkPermissionCheckContext bulkContext) {
		return bulkContext.getJobsByTimesheetId().get(timesheetId);
	}

}