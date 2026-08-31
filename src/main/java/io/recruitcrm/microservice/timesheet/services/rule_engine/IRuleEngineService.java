/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.rule_engine;

import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;

import java.util.List;

/**
 * Service interface for rule engine operations
 */
public interface IRuleEngineService {

	/**
	 * Evaluates rules for a given timesheet
	 * @param requestDto the rule engine request containing timesheet ID
	 * @return RuleEngineResponseBodyDto containing evaluation results
	 */
	RuleEngineResponseBodyDto evaluateRules(RuleEngineRequestBodyDto requestDto);

	/**
	 * Evaluates rules on-demand using time logs from the request body (dry-run, no DB
	 * write). This is used for preview/calculation while the user is filling the
	 * timesheet. Supports multiple timesheets in a single request. Returns simplified
	 * overtime per timeLogId.
	 * @param requestDto the bulk update request containing time log data
	 * @return list of OnDemandTimesheetOvertimeDto, one per timesheet
	 */
	List<OnDemandTimesheetOvertimeDto> evaluateRulesOnDemand(BulkUpdateTimeLogsRequestBodyDto requestDto);

	/**
	 * Validates all rule configurations and system readiness
	 * @return validation result message
	 */
	String validateRules(RuleEngineRequestBodyDto requestDto);

}