/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseBreakRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Time range resolver for range-based break rules.
 *
 * This resolver determines the time ranges for break periods by using actual break
 * intervals from the time log instead of dynamically allocating break time. It creates
 * ranges from the actual break start and end times recorded in the time log.
 *
 * Note: This resolver always identifies break periods for reporting and tracking
 * purposes. The calculateBreakTime setting from RuleTemplate is handled by the BreakRule
 * during evaluation, which determines whether amounts are calculated for these break
 * periods. When calculateBreakTime is FALSE, the BreakRule returns zero amounts but
 * preserves the time ranges for reporting purposes.
 */
public class RangeBasedBreakRuleTimeRangeResolver extends BaseBreakRuleTimeRangeResolver {

	public RangeBasedBreakRuleTimeRangeResolver(Logger logger) {
		super(logger);
	}

	/**
	 * Resolves break time ranges for a single time log. If actual break intervals exist
	 * (from cst_time_log_interval_t), converts them into time ranges. Otherwise falls
	 * back to the parent class's dynamic break allocation which computes break ranges
	 * from the total break duration and available time slots.
	 * @param timeRangeResolverContext the context holding the current time log, occupied
	 * ranges, and timesheet settings
	 * @return a RangeSet of break time ranges clipped to work period boundaries, or empty
	 * if no breaks
	 */
	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {

		// Check if time log has actual break intervals from cst_time_log_interval_t table
		// These are specific break periods recorded by the employee (e.g., 12:00-12:30
		// lunch)
		List<TimeLogBreakInterval> breakIntervals = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated()
			.getBreakIntervals();
		if (breakIntervals == null || breakIntervals.isEmpty()) {
			// Fall back to original behavior if no break intervals are available
			// Parent class (BaseBreakRuleTimeRangeResolver) will use dynamic allocation:
			// - Takes total break duration from time log
			// - Distributes it across available time slots
			// - Creates artificial break ranges to match the total duration
			this.logger.logDebug(MessageFormat.format(
					"No break intervals found for TimeLog ID {0}, falling back to dynamic break allocation",
					timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getId()));
			return super.resolveTimeRange(timeRangeResolverContext);
		}

		// Use actual break intervals to create time ranges
		// This is the preferred path: use real break periods instead of artificial
		// allocation
		// Note: The calculateBreakTime setting is handled by the BreakRule during
		// evaluation, not by this resolver
		// This resolver always identifies break periods for reporting purposes regardless
		// of payment calculation
		this.logger.logDebug(MessageFormat.format("Using {0} actual break intervals for TimeLog ID {1}",
				breakIntervals.size(), timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getId()));
		return createBreakRangesFromIntervals(breakIntervals, timeRangeResolverContext);
	}

	/**
	 * Converts actual break intervals (e.g., 12:00-12:30, 15:00-15:15) into a RangeSet of
	 * time ranges. Each interval is validated (non-null, start before end) and then
	 * clipped to the work period boundaries so no break range extends outside the time
	 * log's actual work start/end time.
	 * @param breakIntervals the list of break intervals from the time log
	 * @param timeRangeResolverContext the context containing the current time log
	 * @return a RangeSet of break time ranges constrained within work period
	 */
	private RangeSet<LocalTime> createBreakRangesFromIntervals(List<TimeLogBreakInterval> breakIntervals,
			TimeRangeResolverContext timeRangeResolverContext) {
		// Create empty RangeSet to accumulate all break ranges
		RangeSet<LocalTime> breakRanges = TreeRangeSet.create();

		// Process each actual break interval recorded by the employee
		for (TimeLogBreakInterval interval : breakIntervals) {
			LocalTime startTime = interval.getBreakStartTime();
			LocalTime endTime = interval.getBreakEndTime();

			// Validate break interval times (skip invalid entries)
			// Invalid cases: null times, end time before start time, or equal times
			if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
				this.logger
					.logWarn(MessageFormat.format("Invalid break interval for TimeLog ID {0}: start={1}, end={2}",
							timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getId(), startTime, endTime));
				continue; // Skip this break and process next one
			}

			// Create range for this break interval
			// Example: Break from 12:00 to 12:30 becomes Range[12:00-12:30]
			Range<LocalTime> breakRange = TimeHelper.toRange(startTime, endTime);
			breakRanges.add(breakRange);

			this.logger.logDebug(MessageFormat.format("Added break range for TimeLog ID {0}: {1} to {2}",
					timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getId(), startTime, endTime));
		}

		// Constrain break ranges to work period boundaries
		// This ensures break ranges don't extend outside the actual work time
		// Example: If work is 9:00-17:00 but break is recorded as 17:30-18:00, clip it
		// out
		Range<LocalTime> workRange = getWorkPeriodRange(timeRangeResolverContext);
		if (workRange != null) {
			RangeSet<LocalTime> constrainedRanges = TreeRangeSet.create();
			// Process each break range and clip it to work boundaries
			for (Range<LocalTime> range : breakRanges.asRanges()) {
				// Check if break range overlaps with work period
				if (range.isConnected(workRange)) {
					// Calculate intersection (overlapping portion only)
					// Example: Break[12:00-13:00] ∩ Work[9:00-12:30] = [12:00-12:30]
					Range<LocalTime> intersection = range.intersection(workRange);
					if (!intersection.isEmpty()) {
						constrainedRanges.add(intersection);
					}
				}
				// If break doesn't overlap with work period, it's completely excluded
			}
			breakRanges = constrainedRanges;
		}

		return breakRanges;
	}

	/**
	 * Returns the work period range (effective start to effective end) of the current
	 * time log. This range is used by {@link #createBreakRangesFromIntervals} to clip
	 * break ranges so they don't extend outside the actual work time. Returns null if the
	 * time log has invalid or missing start/end times.
	 * @param timeRangeResolverContext the context holding the current time log
	 * @return the work period as a closed-open Range, or null if invalid
	 */
	@Override
	protected Range<LocalTime> getWorkPeriodRange(TimeRangeResolverContext timeRangeResolverContext) {
		// Use the actual time log start and end times (not template times)
		// getEffectiveStartTime() handles both workStartTime and normalizedWorkStartTime
		// This ensures we use the actual work period, not the configured template
		// schedule
		LocalTime actualWorkStartTime = TimeHelper
			.getEffectiveStartTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());
		LocalTime actualWorkEndTime = TimeHelper
			.getEffectiveEndTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());

		// Validate that actual work start time is before actual work end time
		// This prevents invalid ranges and handles edge cases like missing time data
		if (actualWorkStartTime == null || actualWorkEndTime == null
				|| !actualWorkStartTime.isBefore(actualWorkEndTime)) {
			return null; // Return null if validation fails - caller should handle
							// gracefully
		}

		// Create the full work period range
		// Example: If employee worked 9:15-17:30, create Range[9:15-17:30]
		// This range will be used to clip break intervals to actual work boundaries
		return TimeHelper.toRange(actualWorkStartTime, actualWorkEndTime);
	}

	/**
	 * Logs a warning when the parent's dynamic break allocation could not place all break
	 * time within available time slots. This happens when the work period is too short
	 * for the requested break duration. Includes context: time log ID, work period,
	 * requested vs unallocated minutes, and total available minutes.
	 * @param totalBreakDuration the total break duration that was requested
	 * @param unallocatedMinutes how many minutes could not be placed
	 * @param availableRanges the time ranges that were available for break allocation
	 * @param timeRangeResolverContext the context holding the current time log
	 */
	@Override
	protected void logIncompleteBreakAllocation(Duration totalBreakDuration, long unallocatedMinutes,
			RangeSet<LocalTime> availableRanges, TimeRangeResolverContext timeRangeResolverContext) {
		// Extract context information for detailed error reporting
		Integer timeLogId = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getId();
		LocalTime workStartTime = TimeHelper
			.getEffectiveStartTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());
		LocalTime workEndTime = TimeHelper
			.getEffectiveEndTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());

		// Calculate total available time for context
		// This helps diagnose whether the issue is insufficient time or overlapping
		// ranges
		// Stream through all available ranges and sum their durations
		long totalAvailableMinutes = availableRanges.asRanges()
			.stream()
			.mapToLong((range) -> Duration.between(range.lowerEndpoint(), range.upperEndpoint()).toMinutes())
			.sum();

		// Log comprehensive warning with all relevant context
		// This helps troubleshoot break allocation issues in production
		// Common causes: work period too short, breaks already occupy available time,
		// invalid data
		this.logger.logWarn(MessageFormat.format(
				"Range-based break time allocation incomplete for TimeLog ID {0} (work period: {1}-{2}). "
						+ "Requested: {3} minutes, Unallocated: {4} minutes, Total available: {5} minutes. "
						+ "This may indicate insufficient available time slots or overlapping time ranges.",
				timeLogId, workStartTime, workEndTime, totalBreakDuration.toMinutes(), unallocatedMinutes,
				totalAvailableMinutes));
	}

}