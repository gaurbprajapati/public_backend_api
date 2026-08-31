/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.BasePermissionChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.PermissionCheckerFactory;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component(BulkPermissionChecker.BEAN_NAME)
public class BulkPermissionChecker {

	public static final String BEAN_NAME = "contractStaffingBulkPermissionChecker";

	private final PermissionCheckerFactory permissionCheckerFactory;

	private final AuthHolder authHolder;

	private final TimesheetRepository timesheetRepository;

	private final Logger logger;

	public BulkPermissionChecker(PermissionCheckerFactory permissionCheckerFactory, AuthHolder authHolder,
			TimesheetRepository timesheetRepository,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.permissionCheckerFactory = permissionCheckerFactory;
		this.authHolder = authHolder;
		this.timesheetRepository = timesheetRepository;
		this.logger = logger;
	}

	/**
	 * Performs bulk permission checking for multiple entities with optimized data loading
	 * @param request The bulk permission check request
	 * @return Bulk permission check results
	 */
	public BulkPermissionCheckResult checkBulkPermissions(@NonNull BulkPermissionCheckRequest request) {
		if (request.getItems() == null || request.getItems().isEmpty()) {
			return BulkPermissionCheckResult.builder().results(Collections.emptyList()).build();
		}

		/**
		 * Preload all required data
		 */
		BulkPermissionCheckContext bulkContext = preloadBulkData(request);

		/**
		 * Process each permission check
		 */
		List<BulkPermissionCheckResult.BulkPermissionCheckResultItem> results = new ArrayList<>();

		for (BulkPermissionCheckRequest.BulkPermissionCheckItem item : request.getItems()) {
			BulkPermissionCheckResult.BulkPermissionCheckResultItem result = processPermissionCheck(item, bulkContext);
			results.add(result);
		}

		return BulkPermissionCheckResult.builder().results(results).build();
	}

	/**
	 * Preloads all candidate and job data needed for bulk permission checking using
	 * optimized queries based on the determined data loading strategy
	 */
	private BulkPermissionCheckContext preloadBulkData(BulkPermissionCheckRequest request) {
		/**
		 * Extract unique IDs for different entity types
		 */
		Set<Integer> timesheetIds = request.getItems()
			.stream()
			.map((item) -> item.getAccessControlCheckMetadataContext().getTimesheetId())
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<Integer> candidateIds = request.getItems()
			.stream()
			.map((item) -> item.getAccessControlCheckMetadataContext().getCandidateId())
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<Integer> jobIds = request.getItems()
			.stream()
			.map((item) -> item.getAccessControlCheckMetadataContext().getJobId())
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		/**
		 * Determine the appropriate data loading strategy
		 */
		BulkDataLoadingStrategy strategy = request.getExplicitStrategy();
		if (strategy == null) {
			strategy = BulkDataLoadingStrategy.determineStrategy(!timesheetIds.isEmpty(), !candidateIds.isEmpty(),
					!jobIds.isEmpty());
		}

		this.logger.logDebug("Using data loading strategy: " + strategy
				+ ((request.getExplicitStrategy() != null) ? " (explicitly specified)" : " (auto-determined)"));

		Integer accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();

		try {
			/**
			 * Initialize maps
			 */
			Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
			Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
			Map<Integer, Candidate> candidatesById = new HashMap<>();
			Map<Integer, Job> jobsById = new HashMap<>();

			/**
			 * Load data based on the determined strategy
			 */
			loadDataByStrategy(LoadDataRequest.builder()
				.strategy(strategy)
				.timesheetIds(timesheetIds)
				.candidateIds(candidateIds)
				.jobIds(jobIds)
				.accountId(accountId)
				.candidatesByTimesheetId(candidatesByTimesheetId)
				.jobsByTimesheetId(jobsByTimesheetId)
				.candidatesById(candidatesById)
				.jobsById(jobsById)
				.build());

			return BulkPermissionCheckContext.builder()
				.candidatesByTimesheetId(candidatesByTimesheetId)
				.jobsByTimesheetId(jobsByTimesheetId)
				.candidatesById(candidatesById)
				.jobsById(jobsById)
				.organizationIdentifier(String.valueOf(accountId))
				.build();

		}
		catch (Exception ex) {
			this.logger.logError("Failed to load bulk permission data using strategy: " + strategy
					+ " - Timesheet IDs: " + timesheetIds + ", Candidate IDs: " + candidateIds + ", Job IDs: " + jobIds
					+ " - " + ex.getMessage());
			/**
			 * Fallback to empty context - individual permission checks will handle
			 * missing data
			 */
			return BulkPermissionCheckContext.builder()
				.candidatesByTimesheetId(Collections.emptyMap())
				.jobsByTimesheetId(Collections.emptyMap())
				.candidatesById(Collections.emptyMap())
				.jobsById(Collections.emptyMap())
				.organizationIdentifier(String.valueOf(accountId))
				.build();
		}
	}

	/**
	 * Loads data based on the specified strategy
	 */
	private void loadDataByStrategy(LoadDataRequest loadDataRequest) {

		/**
		 * Load data based on strategy type
		 */
		if (loadDataRequest.getStrategy() == BulkDataLoadingStrategy.MIXED_STRATEGY) {
			loadDataForMixedStrategy(loadDataRequest);
		}
		else {
			loadDataForFixedStrategy(loadDataRequest);
		}
	}

	/**
	 * For specific strategies, use the dedicated methods
	 */
	private void loadDataForFixedStrategy(LoadDataRequest loadDataRequest) {
		if (loadDataRequest.getStrategy().requiresTimesheetData() && !loadDataRequest.getTimesheetIds().isEmpty()) {
			loadTimesheetData(loadDataRequest.getTimesheetIds(), loadDataRequest.getAccountId(),
					loadDataRequest.getCandidatesByTimesheetId(), loadDataRequest.getJobsByTimesheetId());
		}
		if (loadDataRequest.getStrategy().requiresCandidateData() && !loadDataRequest.getCandidateIds().isEmpty()) {
			loadCandidateData(loadDataRequest.getCandidateIds(), loadDataRequest.getAccountId(),
					loadDataRequest.getCandidatesById());
		}
		if (loadDataRequest.getStrategy().requiresJobData() && !loadDataRequest.getJobIds().isEmpty()) {
			loadJobData(loadDataRequest.getJobIds(), loadDataRequest.getAccountId(), loadDataRequest.getJobsById());
		}
	}

	/**
	 * For mixed strategy, load only what's actually needed
	 */
	private void loadDataForMixedStrategy(LoadDataRequest loadDataRequest) {
		if (!loadDataRequest.getTimesheetIds().isEmpty()) {
			loadTimesheetData(loadDataRequest.getTimesheetIds(), loadDataRequest.getAccountId(),
					loadDataRequest.getCandidatesByTimesheetId(), loadDataRequest.getJobsByTimesheetId());
		}
		if (!loadDataRequest.getCandidateIds().isEmpty()) {
			loadCandidateData(loadDataRequest.getCandidateIds(), loadDataRequest.getAccountId(),
					loadDataRequest.getCandidatesById());
		}
		if (!loadDataRequest.getJobIds().isEmpty()) {
			loadJobData(loadDataRequest.getJobIds(), loadDataRequest.getAccountId(), loadDataRequest.getJobsById());
		}
	}

	/**
	 * Loads timesheet data with related entities
	 */
	private void loadTimesheetData(Set<Integer> timesheetIds, Integer accountId,
			Map<Integer, Candidate> candidatesByTimesheetId, Map<Integer, Job> jobsByTimesheetId) {
		List<TimesheetPermissionDataDto> timesheetPermissionDataList = this.timesheetRepository
			.getTimesheetPermissionDataBulk(new ArrayList<>(timesheetIds), accountId);

		for (TimesheetPermissionDataDto permissionData : timesheetPermissionDataList) {
			if (permissionData.getCandidate() != null) {
				candidatesByTimesheetId.put(permissionData.getTimesheetId(), permissionData.getCandidate());
			}
			if (permissionData.getJob() != null) {
				jobsByTimesheetId.put(permissionData.getTimesheetId(), permissionData.getJob());
			}
		}

		this.logger.logDebug("Successfully loaded timesheet permission data for " + timesheetPermissionDataList.size()
				+ " timesheets using TIMESHEET_WITH_RELATED_ENTITIES strategy");
	}

	/**
	 * Loads candidate data only
	 */
	private void loadCandidateData(Set<Integer> candidateIds, Integer accountId,
			Map<Integer, Candidate> candidatesById) {
		List<TimesheetPermissionDataDto> candidatePermissionDataList = this.timesheetRepository
			.getCandidatePermissionDataBulk(new ArrayList<>(candidateIds), accountId);

		for (TimesheetPermissionDataDto permissionData : candidatePermissionDataList) {
			if (permissionData.getCandidate() != null) {
				candidatesById.put(permissionData.getCandidateId(), permissionData.getCandidate());
			}
		}

		this.logger.logDebug("Successfully loaded candidate permission data for " + candidatePermissionDataList.size()
				+ " candidates using CANDIDATES_ONLY strategy");
	}

	/**
	 * Loads job data only
	 */
	private void loadJobData(Set<Integer> jobIds, Integer accountId, Map<Integer, Job> jobsById) {
		List<TimesheetPermissionDataDto> jobPermissionDataList = this.timesheetRepository
			.getJobPermissionDataBulk(new ArrayList<>(jobIds), accountId);

		for (TimesheetPermissionDataDto permissionData : jobPermissionDataList) {
			if (permissionData.getJob() != null) {
				jobsById.put(permissionData.getJobId(), permissionData.getJob());
			}
		}

		this.logger.logDebug("Successfully loaded job permission data for " + jobPermissionDataList.size()
				+ " jobs using JOBS_ONLY strategy");
	}

	/**
	 * Processes a single permission check using preloaded data
	 */
	private BulkPermissionCheckResult.BulkPermissionCheckResultItem processPermissionCheck(
			BulkPermissionCheckRequest.BulkPermissionCheckItem item, BulkPermissionCheckContext bulkContext) {

		try {
			/**
			 * Get the appropriate permission checker
			 */
			BasePermissionChecker checker = this.permissionCheckerFactory
				.getPermissionChecker(item.getPermissionCheckContext().getPermission());

			if (checker == null) {
				return BulkPermissionCheckResult.BulkPermissionCheckResultItem.builder()
					.allowed(false)
					.errorMessage(
							"No permission checker exists for " + item.getPermissionCheckContext().getPermission())
					.build();
			}

			/**
			 * Create internal context with preloaded data
			 */
			PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
				.entity(item.getEntity())
				.permission(item.getPermissionCheckContext().getPermission())
				.permissionLevel(item.getPermissionCheckContext().getPermissionLevel())
				.timesheetId(item.getAccessControlCheckMetadataContext().getTimesheetId())
				.candidateId(item.getAccessControlCheckMetadataContext().getCandidateId())
				.jobId(item.getAccessControlCheckMetadataContext().getJobId())
				.build();

			/**
			 * Use the enhanced base permission checker with bulk context Since
			 * BasePermissionChecker implements BulkAwarePermissionChecker, we can call
			 * directly
			 */
			checker.checkPermissionWithBulkContext(context, bulkContext);

			return BulkPermissionCheckResult.BulkPermissionCheckResultItem.builder().allowed(true).build();

		}
		catch (UnauthorizedAccessException ex) {
			return BulkPermissionCheckResult.BulkPermissionCheckResultItem.builder()
				.allowed(false)
				.errorMessage(ex.getMessage())
				.build();
		}
		catch (Exception ex) {
			this.logger.logError("Unexpected error during permission check - " + ex.getMessage());
			return BulkPermissionCheckResult.BulkPermissionCheckResultItem.builder()
				.allowed(false)
				.errorMessage("Unexpected error during permission check: " + ex.getMessage())
				.build();
		}
	}

}