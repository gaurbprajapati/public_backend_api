/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.examples;

import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.helpers.constants.AccessControlCheckerExampleConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Example class demonstrating how to use the AccessControlChecker for both individual and
 * bulk permission checks.
 *
 * This example shows: 1. Individual permission checks for single entities 2. Bulk
 * permission checks for multiple entities 3. Different entity types (timesheets,
 * candidates, jobs) 4. Different permission types (view, edit, delete, etc.)
 */
@Component
public class AccessControlCheckerExample {

	private final AccessControlChecker accessControlChecker;

	private final Logger logger;

	public AccessControlCheckerExample(AccessControlChecker accessControlChecker,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.accessControlChecker = accessControlChecker;
		this.logger = logger;
	}

	/**
	 * Example 1: Individual permission check for a single timesheet
	 *
	 * This demonstrates how to check if a user has permission to view a specific
	 * timesheet. The AccessControlChecker will automatically load the required candidate
	 * and job data and perform the permission validation.
	 */
	public void checkIndividualTimesheetPermission(Integer timesheetId) {
		try {
			/**
			 * Create permission context for viewing timesheet
			 */
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.VIEW_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			/**
			 * Create metadata context with timesheet ID
			 */
			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);

			/**
			 * Perform individual permission check
			 */
			this.accessControlChecker.allows(Entity.TIMESHEET, permissionContext, metadataContext);

			this.logger.logInfo("User has permission to view timesheet: " + timesheetId);

		}
		catch (Exception ex) {
			this.logger.logError("Permission denied for timesheet " + timesheetId + ": " + ex.getMessage());
		}
	}

	/**
	 * Example 2: Individual permission check for a single candidate
	 *
	 * This demonstrates how to check if a user has permission to edit timesheets for a
	 * specific candidate.
	 */
	public void checkIndividualCandidatePermission(Integer candidateId) {
		try {
			/**
			 * Create permission context for editing timesheet
			 */
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.EDIT_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			/**
			 * Create metadata context with candidate ID
			 */
			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setCandidateId(candidateId);

			/**
			 * Perform individual permission check
			 */
			this.accessControlChecker.allows(Entity.TIMESHEET, permissionContext, metadataContext);

			this.logger.logInfo("User has permission to edit timesheets for candidate: " + candidateId);

		}
		catch (Exception ex) {
			this.logger.logError("Permission denied for candidate " + candidateId + ": " + ex.getMessage());
		}
	}

	/**
	 * Example 3: Individual permission check for a single job
	 *
	 * This demonstrates how to check if a user has permission to delete timesheets for a
	 * specific job.
	 */
	public void checkIndividualJobPermission(Integer jobId) {
		try {
			/**
			 * Create permission context for deleting timesheet
			 */
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.DELETE_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			/**
			 * Create metadata context with job ID
			 */
			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setJobId(jobId);

			/**
			 * Perform individual permission check
			 */
			this.accessControlChecker.allows(Entity.TIMESHEET, permissionContext, metadataContext);

			this.logger.logInfo("User has permission to delete timesheets for job: " + jobId);

		}
		catch (Exception ex) {
			this.logger.logError("Permission denied for job " + jobId + ": " + ex.getMessage());
		}
	}

	/**
	 * Example 4: Bulk permission check for multiple timesheets
	 *
	 * This demonstrates how to efficiently check permissions for multiple timesheets in a
	 * single request. The AccessControlChecker will optimize data loading and perform all
	 * permission checks in bulk.
	 */
	public BulkPermissionCheckResult checkBulkTimesheetPermissions(List<Integer> timesheetIds) {
		this.logger.logInfo(AccessControlCheckerExampleConstants.CHECKING_BULK_PERMISSIONS_FOR_PREFIX
				+ timesheetIds.size() + " timesheets");

		/**
		 * Create bulk permission check request items
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = timesheetIds.stream().map((timesheetId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.VIEW_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);

			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		/**
		 * Create the bulk request
		 */
		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder().items(items).build();

		/**
		 * Perform bulk permission check
		 */
		BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(request);

		/**
		 * Log results summary
		 */
		long successCount = result.getResults().stream().filter((item) -> item.isAllowed()).count();

		this.logger.logInfo(AccessControlCheckerExampleConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + successCount
				+ "/" + timesheetIds.size() + AccessControlCheckerExampleConstants.SUCCESSFUL_SUFFIX);

		return result;
	}

	/**
	 * Example 5: Bulk permission check for multiple candidates
	 *
	 * This demonstrates how to check permissions for multiple candidates efficiently.
	 */
	public BulkPermissionCheckResult checkBulkCandidatePermissions(List<Integer> candidateIds) {
		this.logger.logInfo(AccessControlCheckerExampleConstants.CHECKING_BULK_PERMISSIONS_FOR_PREFIX
				+ candidateIds.size() + " candidates");

		/**
		 * Create bulk permission check request items
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = candidateIds.stream().map((candidateId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.EDIT_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setCandidateId(candidateId);

			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		/**
		 * Create the bulk request
		 */
		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder().items(items).build();

		/**
		 * Perform bulk permission check
		 */
		BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(request);

		/**
		 * Log results summary
		 */
		long successCount = result.getResults().stream().filter((item) -> item.isAllowed()).count();

		this.logger.logInfo(AccessControlCheckerExampleConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + successCount
				+ "/" + candidateIds.size() + AccessControlCheckerExampleConstants.SUCCESSFUL_SUFFIX);

		return result;
	}

	/**
	 * Example 6: Bulk permission check for multiple jobs
	 *
	 * This demonstrates how to check permissions for multiple jobs efficiently.
	 */
	public BulkPermissionCheckResult checkBulkJobPermissions(List<Integer> jobIds) {
		this.logger.logInfo(
				AccessControlCheckerExampleConstants.CHECKING_BULK_PERMISSIONS_FOR_PREFIX + jobIds.size() + " jobs");

		/**
		 * Create bulk permission check request items
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = jobIds.stream().map((jobId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.DELETE_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setJobId(jobId);

			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		/**
		 * Create the bulk request
		 */
		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder().items(items).build();

		/**
		 * Perform bulk permission check
		 */
		BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(request);

		/**
		 * Log results summary
		 */
		long successCount = result.getResults().stream().filter((item) -> item.isAllowed()).count();

		this.logger.logInfo(AccessControlCheckerExampleConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + successCount
				+ "/" + jobIds.size() + AccessControlCheckerExampleConstants.SUCCESSFUL_SUFFIX);

		return result;
	}

	/**
	 * Example 7: Mixed bulk permission check for different entity types
	 *
	 * This demonstrates how to check permissions for different entity types (timesheets,
	 * candidates, jobs) in a single bulk request.
	 */
	public BulkPermissionCheckResult checkMixedBulkPermissions(List<Integer> timesheetIds, List<Integer> candidateIds,
			List<Integer> jobIds) {
		this.logger.logInfo("Checking mixed bulk permissions - Timesheets: " + timesheetIds.size() + ", Candidates: "
				+ candidateIds.size() + ", Jobs: " + jobIds.size());

		/**
		 * Create items for timesheet permissions
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> timesheetItems = timesheetIds.stream()
			.map((timesheetId) -> {
				PermissionCheckContext permissionContext = new PermissionCheckContext();
				permissionContext.setPermission(Permission.VIEW_TIMESHEET);
				permissionContext.setPermissionLevel(PermissionLevel.YES);

				AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
				metadataContext.setTimesheetId(timesheetId);

				return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
					.entity(Entity.TIMESHEET)
					.permissionCheckContext(permissionContext)
					.accessControlCheckMetadataContext(metadataContext)
					.build();
			})
			.toList();

		/**
		 * Create items for candidate permissions
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> candidateItems = candidateIds.stream()
			.map((candidateId) -> {
				PermissionCheckContext permissionContext = new PermissionCheckContext();
				permissionContext.setPermission(Permission.EDIT_TIMESHEET);
				permissionContext.setPermissionLevel(PermissionLevel.YES);

				AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
				metadataContext.setCandidateId(candidateId);

				return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
					.entity(Entity.TIMESHEET)
					.permissionCheckContext(permissionContext)
					.accessControlCheckMetadataContext(metadataContext)
					.build();
			})
			.toList();

		/**
		 * Create items for job permissions
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> jobItems = jobIds.stream().map((jobId) -> {
			PermissionCheckContext permissionContext = new PermissionCheckContext();
			permissionContext.setPermission(Permission.DELETE_TIMESHEET);
			permissionContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setJobId(jobId);

			return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
				.entity(Entity.TIMESHEET)
				.permissionCheckContext(permissionContext)
				.accessControlCheckMetadataContext(metadataContext)
				.build();
		}).toList();

		/**
		 * Combine all items
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> allItems = new java.util.ArrayList<>();
		allItems.addAll(timesheetItems);
		allItems.addAll(candidateItems);
		allItems.addAll(jobItems);

		/**
		 * Create the bulk request
		 */
		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder().items(allItems).build();

		/**
		 * Perform bulk permission check
		 */
		BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(request);

		/**
		 * Log results summary
		 */
		long successCount = result.getResults().stream().filter((item) -> item.isAllowed()).count();

		this.logger.logInfo("Mixed bulk permission check completed: " + successCount + "/" + allItems.size()
				+ AccessControlCheckerExampleConstants.SUCCESSFUL_SUFFIX);

		return result;
	}

	/**
	 * Example 8: Check multiple different permissions for the same timesheet
	 *
	 * This demonstrates how to check multiple different permissions (view, edit, delete,
	 * etc.) for the same timesheet in a single bulk request.
	 */
	public BulkPermissionCheckResult checkMultiplePermissionsForTimesheet(Integer timesheetId) {
		this.logger.logInfo("Checking multiple permissions for timesheet: " + timesheetId);

		/**
		 * Create permission contexts for different operations
		 */
		PermissionCheckContext viewContext = new PermissionCheckContext();
		viewContext.setPermission(Permission.VIEW_TIMESHEET);
		viewContext.setPermissionLevel(PermissionLevel.YES);

		PermissionCheckContext editContext = new PermissionCheckContext();
		editContext.setPermission(Permission.EDIT_TIMESHEET);
		editContext.setPermissionLevel(PermissionLevel.YES);

		PermissionCheckContext deleteContext = new PermissionCheckContext();
		deleteContext.setPermission(Permission.DELETE_TIMESHEET);
		deleteContext.setPermissionLevel(PermissionLevel.YES);

		PermissionCheckContext approveContext = new PermissionCheckContext();
		approveContext.setPermission(Permission.APPROVE_TIMESHEET);
		approveContext.setPermissionLevel(PermissionLevel.YES);

		/**
		 * Create metadata context
		 */
		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(timesheetId);

		/**
		 * Create items for different permissions
		 */
		List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = List.of(
				/**
				 * Check view permission
				 */
				BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
					.entity(Entity.TIMESHEET)
					.permissionCheckContext(viewContext)
					.accessControlCheckMetadataContext(metadataContext)
					.build(),

				/**
				 * Check edit permission
				 */
				BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
					.entity(Entity.TIMESHEET)
					.permissionCheckContext(editContext)
					.accessControlCheckMetadataContext(metadataContext)
					.build(),

				/**
				 * Check delete permission
				 */
				BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
					.entity(Entity.TIMESHEET)
					.permissionCheckContext(deleteContext)
					.accessControlCheckMetadataContext(metadataContext)
					.build(),

				/**
				 * Check approve permission
				 */
				BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
					.entity(Entity.TIMESHEET)
					.permissionCheckContext(approveContext)
					.accessControlCheckMetadataContext(metadataContext)
					.build());

		/**
		 * Create the bulk request
		 */
		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder().items(items).build();

		/**
		 * Perform bulk permission check
		 */
		BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(request);

		/**
		 * Log results summary
		 */
		long successCount = result.getResults().stream().filter((item) -> item.isAllowed()).count();

		this.logger.logInfo("Multiple permissions check completed: " + successCount + "/" + items.size()
				+ AccessControlCheckerExampleConstants.SUCCESSFUL_SUFFIX);

		return result;
	}

}