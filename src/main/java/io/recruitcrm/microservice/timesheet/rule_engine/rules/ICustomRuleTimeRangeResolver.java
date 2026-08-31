/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.RangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;

import java.time.LocalTime;

public interface ICustomRuleTimeRangeResolver {

	RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext);

}
