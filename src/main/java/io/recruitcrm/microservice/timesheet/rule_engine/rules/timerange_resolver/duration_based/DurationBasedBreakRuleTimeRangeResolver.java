/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseBreakRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Duration-based break rule time range resolver.
 *
 * This resolver handles break time allocation for duration-based time logs. Break time is
 * allocated at the start of the work period (00:00 to break duration) to ensure all
 * subsequent rules are shifted forward by the break duration. This approach maintains
 * consistency with the midnight anchor system used by other duration-based rules.
 */
public class DurationBasedBreakRuleTimeRangeResolver extends BaseBreakRuleTimeRangeResolver {

	public DurationBasedBreakRuleTimeRangeResolver(Logger logger) {
		super(logger);
	}

	@Override
	protected Range<LocalTime> getWorkPeriodRange(TimeRangeResolverContext timeRangeResolverContext) {
		TimeLog currentTimeLog = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated();

		// Get break time from the current time log being evaluated
		Duration breakDuration = currentTimeLog.getBreakTime();
		if (breakDuration == null || breakDuration.isZero()) {
			return null; // No break time
		}

		// Use the normalized start and end times from the time log
		LocalTime timeLogStartTime = currentTimeLog.getNormalizedWorkStartTime();
		LocalTime timeLogEndTime = currentTimeLog.getNormalizedWorkEndTime();

		// Validate that normalized times are available and valid
		if (timeLogStartTime == null || timeLogEndTime == null || !timeLogStartTime.isBefore(timeLogEndTime)) {
			return null;
		}

		// Calculate actual work time from the logged work period
		Duration actualWorkTime = TimeHelper.calculateDuration(timeLogStartTime, timeLogEndTime);
		if (actualWorkTime == null || actualWorkTime.isZero()) {
			return null; // No work time logged
		}

		// Ensure break time never exceeds actual work time (applies to both work days and
		// off days)
		if (breakDuration.compareTo(actualWorkTime) >= 0) {
			return null;
		}

		// Create the full work period range using normalized times
		return TimeHelper.toRange(timeLogStartTime, timeLogEndTime);
	}

	@Override
	protected RangeSet<LocalTime> allocateBreakTime(RangeSet<LocalTime> availableRanges, Duration breakDuration,
			TimeRangeResolverContext timeRangeResolverContext) {
		RangeSet<LocalTime> allocatedRanges = TreeRangeSet.create();

		// For duration-based rules, allocate break time at the start (00:00 to break
		// duration)
		Range<LocalTime> breakRange = TimeHelper.createBreakRangeAtStart(breakDuration);

		if (!breakRange.equals(Range.all())) {
			allocatedRanges.add(breakRange);
			this.logger.logDebug("Allocated break time at start: " + breakRange);
		}

		return allocatedRanges;
	}

}