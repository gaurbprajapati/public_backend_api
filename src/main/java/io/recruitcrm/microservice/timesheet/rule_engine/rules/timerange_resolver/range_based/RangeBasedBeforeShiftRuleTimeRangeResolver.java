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

public class RangeBasedBeforeShiftRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	private final Logger logger;

	public RangeBasedBeforeShiftRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		LocalTime effectiveStartTime = TimeHelper
			.getEffectiveStartTime(timeRangeResolverContext.getCurrentTimeLogBeingEvaluated());
		LocalTime ruleStartTime = timeRangeResolverContext.getCurrentCustomRuleBeingEvaluated().getStartTime();

		// Validate that effective start time is before rule start time
		if (effectiveStartTime == null || ruleStartTime == null || !effectiveStartTime.isBefore(ruleStartTime)) {
			return TreeRangeSet.create(); // Return empty range set if validation fails
		}

		// Create the gross time range for before shift period
		Range<LocalTime> grossTimeRange = TimeHelper.toRange(effectiveStartTime, ruleStartTime);

		// Get available time ranges, respecting already occupied time ranges
		// This will automatically exclude any time ranges that have been occupied by
		// other rules (like break periods)
		return TimeHelper.getAvailableTimeRanges(grossTimeRange, timeRangeResolverContext.getOccupiedTimeRanges());
	}

}
