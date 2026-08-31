/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseWeeklyOvertimeRuleTimeRangeResolver;

/**
 * Duration-based implementation of weekly overtime time range resolver. Extends the base
 * class to inherit common functionality.
 */
public class DurationBasedWeeklyOvertimeRuleRangeResolver extends BaseWeeklyOvertimeRuleTimeRangeResolver {

	public DurationBasedWeeklyOvertimeRuleRangeResolver(Logger logger) {
		super(logger);
	}

}
