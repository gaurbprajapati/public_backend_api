/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import lombok.Getter;

/**
 * Enum defining different data loading strategies for bulk permission checking. This
 * makes the data loading logic explicit and allows for easy extension when new preloader
 * strategies are needed in the future.
 */
@Getter
public enum BulkDataLoadingStrategy {

	/**
	 * Load both candidate and job data via timesheet relationships. Used when timesheet
	 * IDs are provided and we need both related entities. This strategy uses the existing
	 * join query to fetch all related data.
	 */
	TIMESHEET_WITH_RELATED_ENTITIES("timesheet_with_related_entities",
			"Load both candidate and job data via timesheet relationships"),

	/**
	 * Load only candidate data (no jobs). Used when only candidate IDs are provided for
	 * direct candidate permission checks. This strategy loads candidates independently
	 * without job relationships.
	 */
	CANDIDATES_ONLY("candidates_only", "Load only candidate data for direct candidate permission checks"),

	/**
	 * Load only job data (no candidates). Used when only job IDs are provided for direct
	 * job permission checks. This strategy loads jobs independently without candidate
	 * relationships.
	 */
	JOBS_ONLY("jobs_only", "Load only job data for direct job permission checks"),

	/**
	 * Mixed strategy - loads different data based on the provided entity types. Used when
	 * a single bulk request contains different types of entities (timesheets, candidates,
	 * jobs) and we need to load appropriate data for each.
	 */
	MIXED_STRATEGY("mixed_strategy", "Load appropriate data based on the entity types in the request");

	private final String code;

	private final String description;

	BulkDataLoadingStrategy(String code, String description) {
		this.code = code;
		this.description = description;
	}

	@Override
	public String toString() {
		return String.format("%s (%s): %s", name(), this.code, this.description);
	}

	/**
	 * Determines the appropriate data loading strategy based on the provided entity IDs.
	 * @param hasTimesheetIds Whether timesheet IDs are provided
	 * @param hasCandidateIds Whether candidate IDs are provided
	 * @param hasJobIds Whether job IDs are provided
	 * @return The appropriate data loading strategy
	 */
	public static BulkDataLoadingStrategy determineStrategy(boolean hasTimesheetIds, boolean hasCandidateIds,
			boolean hasJobIds) {
		// Handle multiple types scenario first
		if ((hasTimesheetIds && hasCandidateIds) || (hasTimesheetIds && hasJobIds) || (hasCandidateIds && hasJobIds)) {
			return MIXED_STRATEGY;
		}

		// Handle single type scenarios
		if (hasTimesheetIds) {
			return TIMESHEET_WITH_RELATED_ENTITIES;
		}
		if (hasCandidateIds) {
			return CANDIDATES_ONLY;
		}
		if (hasJobIds) {
			return JOBS_ONLY;
		}

		// Default fallback - no data to load
		return TIMESHEET_WITH_RELATED_ENTITIES;
	}

	/**
	 * Checks if this strategy requires timesheet data loading.
	 */
	public boolean requiresTimesheetData() {
		return this == TIMESHEET_WITH_RELATED_ENTITIES;
	}

	/**
	 * Checks if this strategy requires candidate data loading.
	 */
	public boolean requiresCandidateData() {
		return this == CANDIDATES_ONLY;
	}

	/**
	 * Checks if this strategy requires job data loading.
	 */
	public boolean requiresJobData() {
		return this == JOBS_ONLY;
	}

	/**
	 * Checks if this strategy requires timesheet data loading (for mixed strategy).
	 */
	public boolean requiresTimesheetDataForMixed() {
		return this == TIMESHEET_WITH_RELATED_ENTITIES || this == MIXED_STRATEGY;
	}

	/**
	 * Checks if this strategy requires candidate data loading (for mixed strategy).
	 */
	public boolean requiresCandidateDataForMixed() {
		return this == CANDIDATES_ONLY || this == MIXED_STRATEGY;
	}

	/**
	 * Checks if this strategy requires job data loading (for mixed strategy).
	 */
	public boolean requiresJobDataForMixed() {
		return this == JOBS_ONLY || this == MIXED_STRATEGY;
	}

}