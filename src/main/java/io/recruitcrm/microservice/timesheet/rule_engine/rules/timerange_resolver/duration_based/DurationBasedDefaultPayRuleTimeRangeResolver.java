/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;

import java.time.LocalTime;

/**
 * Duration-based resolver for the Default Pay rule. Registered with the rule factory for
 * completeness; actual Default Pay ranges are computed at week end by
 * {@code BaseRuleEvaluator.evaluateDefaultPayRules} (which sees week-wide state that the
 * per-rule resolver loop cannot), so this resolver is never invoked during normal flow.
 */
public class DurationBasedDefaultPayRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	public DurationBasedDefaultPayRuleTimeRangeResolver(Logger logger) {
		// Logger retained for symmetry with other resolvers; not used.
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		return TreeRangeSet.create();
	}

}
