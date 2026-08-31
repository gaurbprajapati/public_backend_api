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

import java.time.LocalTime;

/**
 * Base class for weekly overtime time range resolvers providing common functionality for
 * both range-based and duration-based implementations.
 */
public abstract class BaseWeeklyOvertimeRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	protected final Logger logger;

	protected BaseWeeklyOvertimeRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		// For weekly overtime, if there's no current time log, return the full day range
		// This allows weekly overtime to consider the entire day for candidate time
		// ranges
		if (timeRangeResolverContext.getCurrentTimeLogBeingEvaluated() == null) {
			// Return full day range (00:00 to 23:59) for weekly overtime evaluation
			Range<LocalTime> fullDay = Range.closedOpen(LocalTime.MIDNIGHT, LocalTime.MAX);
			return TimeHelper.getFreeTimeRanges(fullDay, timeRangeResolverContext.getOccupiedTimeRanges());
		}

		LocalTime effectiveStartTime = TimeHelper
			.getEffectiveStartTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());
		LocalTime effectiveEndTime = TimeHelper
			.getEffectiveEndTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());

		// Validate that effective start time is before effective end time
		if (effectiveStartTime == null || effectiveEndTime == null || !effectiveStartTime.isBefore(effectiveEndTime)) {
			return TreeRangeSet.create(); // Return empty range set if validation fails
		}

		Range<LocalTime> fullDay = TimeHelper.toRange(effectiveStartTime, effectiveEndTime);
		return TimeHelper.getFreeTimeRanges(fullDay, timeRangeResolverContext.getOccupiedTimeRanges());
	}

}