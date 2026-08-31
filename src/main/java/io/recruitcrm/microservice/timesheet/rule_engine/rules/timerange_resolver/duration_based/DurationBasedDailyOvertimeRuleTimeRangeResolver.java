/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseDailyOvertimeRuleTimeRangeResolver;

public class DurationBasedDailyOvertimeRuleTimeRangeResolver extends BaseDailyOvertimeRuleTimeRangeResolver {

	public DurationBasedDailyOvertimeRuleTimeRangeResolver(Logger logger) {
		super(logger);
	}

	@Override
	protected RuleType getDailyOvertimeRuleType() {
		return RuleType.DURATION_BASED_DAILY_OVERTIME;
	}

}
