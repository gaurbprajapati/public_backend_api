/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.time.LocalTime;

public class RangeBasedSpecificTimeRangeRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	private final Logger logger;

	public RangeBasedSpecificTimeRangeRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		LocalTime startTime = timeRangeResolverContext.getCurrentCustomRuleBeingEvaluated().getStartTime();
		LocalTime endTime = timeRangeResolverContext.getCurrentCustomRuleBeingEvaluated().getEndTime();

		// Validate that startTime is before endTime
		if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
			return TreeRangeSet.create(); // Return empty range set if validation fails
		}

		Range<LocalTime> grossTimeRange = TimeHelper.toRange(startTime, endTime);
		return TimeHelper.getAvailableTimeRanges(grossTimeRange, timeRangeResolverContext.getOccupiedTimeRanges());
	}

}
