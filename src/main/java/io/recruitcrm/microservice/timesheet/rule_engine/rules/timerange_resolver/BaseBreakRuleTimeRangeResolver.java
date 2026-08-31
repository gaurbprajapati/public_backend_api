/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Base class for break rule time range resolvers. Provides common functionality for
 * allocating break time within available work periods.
 */
public abstract class BaseBreakRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	protected final Logger logger;

	protected BaseBreakRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {

		Duration breakDuration = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getBreakTime();
		if (breakDuration == null || breakDuration.isZero()) {
			return TreeRangeSet.create();
		}

		Range<LocalTime> workRange = getWorkPeriodRange(timeRangeResolverContext);
		if (workRange == null) {
			return TreeRangeSet.create();
		}

		// Find available time ranges and allocate break time
		RangeSet<LocalTime> availableRanges = TimeHelper.getAvailableTimeRanges(workRange,
				timeRangeResolverContext.getOccupiedTimeRanges());

		return allocateBreakTime(availableRanges, breakDuration, timeRangeResolverContext);
	}

	/**
	 * Gets the work period range for break time allocation.
	 * @param timeRangeResolverContext the context containing evaluation state
	 * @return the work period range, or null if not available
	 */
	protected abstract Range<LocalTime> getWorkPeriodRange(TimeRangeResolverContext timeRangeResolverContext);

	/**
	 * Allocates break time within available time slots using first-come-first-served
	 * approach.
	 * @param availableRanges the available time ranges within the work period
	 * @param breakDuration the required break duration
	 * @param timeRangeResolverContext the context containing evaluation state
	 * @return a RangeSet containing the allocated break time ranges
	 */
	protected RangeSet<LocalTime> allocateBreakTime(RangeSet<LocalTime> availableRanges, Duration breakDuration,
			TimeRangeResolverContext timeRangeResolverContext) {
		RangeSet<LocalTime> allocatedRanges = TreeRangeSet.create();
		long remainingMinutes = breakDuration.toMinutes();

		if (availableRanges.isEmpty() || remainingMinutes <= 0) {
			return allocatedRanges;
		}

		// Allocate break time in available slots (first come, first served)
		for (Range<LocalTime> range : availableRanges.asRanges()) {
			if (remainingMinutes <= 0) {
				break;
			}

			long availableMinutes = Duration.between(range.lowerEndpoint(), range.upperEndpoint()).toMinutes();
			long allocateMinutes = Math.min(remainingMinutes, availableMinutes);

			if (allocateMinutes > 0) {
				LocalTime startTime = range.lowerEndpoint();
				LocalTime endTime = startTime.plusMinutes(allocateMinutes);

				allocatedRanges.add(TimeHelper.toRange(startTime, endTime));
				remainingMinutes -= allocateMinutes;
			}
		}

		// Validate and log if not all break time could be allocated
		if (remainingMinutes > 0) {
			logIncompleteBreakAllocation(breakDuration, remainingMinutes, availableRanges, timeRangeResolverContext);
		}

		return allocatedRanges;
	}

	/**
	 * Logs a warning when not all break time could be allocated.
	 * @param totalBreakDuration the total break duration that was requested
	 * @param unallocatedMinutes the number of minutes that could not be allocated
	 * @param availableRanges the available time ranges that were considered
	 * @param timeRangeResolverContext the context containing evaluation state
	 */
	protected void logIncompleteBreakAllocation(Duration totalBreakDuration, long unallocatedMinutes,
			RangeSet<LocalTime> availableRanges, TimeRangeResolverContext timeRangeResolverContext) {
		// Default implementation using logger to ensure logging always happens
		Integer timeLogId = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getId();

		// Calculate total available time for context
		long totalAvailableMinutes = availableRanges.asRanges()
			.stream()
			.mapToLong((range) -> Duration.between(range.lowerEndpoint(), range.upperEndpoint()).toMinutes())
			.sum();

		this.logger.logWarn(MessageFormat.format(
				"Break time allocation incomplete for TimeLog ID {0}. "
						+ "Requested: {1} minutes, Unallocated: {2} minutes, Total available: {3} minutes. "
						+ "This may indicate insufficient available time slots or overlapping time ranges.",
				timeLogId, totalBreakDuration.toMinutes(), unallocatedMinutes, totalAvailableMinutes));
	}

}