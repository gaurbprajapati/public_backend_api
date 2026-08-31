/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseWeeklyOvertimeRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseWeeklyOvertimeRuleTimeRangeResolverTests;

/**
 * Tests for duration-based weekly overtime time range resolver. Extends the base test
 * class to inherit common test functionality.
 */
class DurationBasedWeeklyOvertimeRuleRangeResolverTests extends BaseWeeklyOvertimeRuleTimeRangeResolverTests {

	@Override
	protected BaseWeeklyOvertimeRuleTimeRangeResolver createResolver(Logger logger) {
		return new DurationBasedWeeklyOvertimeRuleRangeResolver(logger);
	}

}