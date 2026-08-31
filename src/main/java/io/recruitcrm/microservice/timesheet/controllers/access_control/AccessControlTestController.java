/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.access_control;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.constants.AccessControlMessageConstants;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/access-control/test")
@RequiredArgsConstructor
public class AccessControlTestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlTestController.class);

	private final AccessControlChecker accessControlChecker;

	private final APIResponder apiResponder;

	/**
	 * Test bulk permission checking for timesheet IDs
	 * @param request Request containing list of timesheet IDs
	 * @return Bulk permission check results
	 */
	@PostMapping("/timesheets")
	@Transactional
	public ResponseEntity<?> testTimesheetBulkPermissions(@RequestBody TimesheetBulkTestRequest request) {
		try {
			// Create bulk permission check request items for timesheets
			List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = request.timesheetIds()
				.stream()
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

			// Create the bulk request
			BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();

			// Perform bulk permission check
			BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(bulkRequest);

			return this.apiResponder
				.respond(result,
						AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX
								+ request.timesheetIds().size() + " timesheets",
						APIResponseType.SUCCESS, HttpStatus.OK);
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside testTimesheetBulkPermissions() method :: {}", sw);
			return this.apiResponder.respondWithError(ex, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Test bulk permission checking for candidate IDs
	 * @param request Request containing list of candidate IDs
	 * @return Bulk permission check results
	 */
	@PostMapping("/candidates")
	@Transactional
	public ResponseEntity<?> testCandidateBulkPermissions(@RequestBody CandidateBulkTestRequest request) {
		try {
			// Create bulk permission check request items for candidates
			List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = request.candidateIds()
				.stream()
				.map((candidateId) -> {
					PermissionCheckContext permissionContext = new PermissionCheckContext();
					permissionContext.setPermission(Permission.VIEW_TIMESHEET);
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

			// Create the bulk request
			BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();

			// Perform bulk permission check
			BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(bulkRequest);

			return this.apiResponder
				.respond(result,
						AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX
								+ request.candidateIds().size() + " candidates",
						APIResponseType.SUCCESS, HttpStatus.OK);
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside testCandidateBulkPermissions() method :: {}", sw);
			return this.apiResponder.respondWithError(ex, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Test bulk permission checking for job IDs
	 * @param request Request containing list of job IDs
	 * @return Bulk permission check results
	 */
	@PostMapping("/jobs")
	@Transactional
	public ResponseEntity<?> testJobBulkPermissions(@RequestBody JobBulkTestRequest request) {
		try {
			// Create bulk permission check request items for jobs
			List<BulkPermissionCheckRequest.BulkPermissionCheckItem> items = request.jobIds().stream().map((jobId) -> {
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

			// Create the bulk request
			BulkPermissionCheckRequest bulkRequest = BulkPermissionCheckRequest.builder().items(items).build();

			// Perform bulk permission check
			BulkPermissionCheckResult result = this.accessControlChecker.allowsBulk(bulkRequest);

			return this.apiResponder.respond(result,
					AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + request.jobIds().size()
							+ " jobs",
					APIResponseType.SUCCESS, HttpStatus.OK);

		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside testJobBulkPermissions() method :: {}", sw);
			return this.apiResponder.respondWithError(ex, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Request DTOs for the three test methods
	public record TimesheetBulkTestRequest(List<Integer> timesheetIds) {
		public TimesheetBulkTestRequest {
			if (timesheetIds == null || timesheetIds.isEmpty()) {
				throw new IllegalArgumentException("Timesheet IDs list cannot be null or empty");
			}
		}
	}

	public record CandidateBulkTestRequest(List<Integer> candidateIds) {
		public CandidateBulkTestRequest {
			if (candidateIds == null || candidateIds.isEmpty()) {
				throw new IllegalArgumentException("Candidate IDs list cannot be null or empty");
			}
		}
	}

	public record JobBulkTestRequest(List<Integer> jobIds) {
		public JobBulkTestRequest {
			if (jobIds == null || jobIds.isEmpty()) {
				throw new IllegalArgumentException("Job IDs list cannot be null or empty");
			}
		}
	}

}