/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Duration-based specific hour range rule time range resolver.
 *
 * This resolver handles specific hour range rules for duration-based time logs. It uses
 * the time shifting approach where rules are shifted forward by the break duration to
 * maintain consistency with the midnight anchor system and break allocation at the start.
 */
public class DurationBasedSpecificHourRangeRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	private final Logger logger;

	public DurationBasedSpecificHourRangeRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		if (timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getDayType() == WorkDayType.DAY_OFF) {
			return TreeRangeSet.create();
		}

		// Get the specific hour range from the custom rule
		Duration startDuration = timeRangeResolverContext.getCurrentCustomRuleBeingEvaluated().getStartDuration();
		Duration endDuration = timeRangeResolverContext.getCurrentCustomRuleBeingEvaluated().getEndDuration();

		if (startDuration == null || endDuration == null || startDuration.compareTo(endDuration) >= 0) {
			this.logger.logWarn("Invalid duration range: start=" + startDuration + ", end=" + endDuration);
			return TreeRangeSet.create();
		}

		// Get break duration to calculate shift offset
		Duration breakDuration = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getBreakTime();
		// Shift the duration range forward by the break duration
		Range<LocalTime> shiftedRange = TimeHelper.shiftRangeByBreak(startDuration, endDuration, breakDuration);

		if (shiftedRange.equals(Range.all())) {
			this.logger
				.logWarn("Failed to create shifted range for durations: " + startDuration + " to " + endDuration);
			return TreeRangeSet.create();
		}

		// Get available time ranges, respecting already occupied time ranges
		RangeSet<LocalTime> result = TimeHelper.getAvailableTimeRanges(shiftedRange,
				timeRangeResolverContext.getOccupiedTimeRanges());

		this.logger.logDebug("Specific hour range rule: original=" + startDuration + "-" + endDuration + ", shifted="
				+ shiftedRange + ", break shift=" + breakDuration);

		return result;
	}

}
