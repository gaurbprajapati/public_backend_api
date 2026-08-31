/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.responses.IAPIResponder;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.services.rule_engine.IRuleEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for rule engine operations.
 */
@Validated
@RestController
@RequestMapping("/v1/rule-engine")
public class RuleEngineController implements IRuleEngineController {

	private final IRuleEngineService ruleEngineService;

	private final IAPIResponder apiResponder;

	@Autowired
	public RuleEngineController(IRuleEngineService ruleEngineService, IAPIResponder apiResponder) {
		this.ruleEngineService = ruleEngineService;
		this.apiResponder = apiResponder;
	}

	/**
	 * Evaluates rules for a given timesheet. Response is filtered based on user type: -
	 * CONTRACTOR: Bill amounts are hidden (set to null) - CONTACT (Client): All pay and
	 * bill amounts are hidden (set to null) - USER (Agency): Full response with all
	 * amounts
	 */
	@PostMapping("/evaluate")
	@Override
	public ResponseEntity<?> evaluateRules(@Validated @RequestBody RuleEngineRequestBodyDto requestBodyDto) {
		try {
			RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(requestBodyDto);
			return this.apiResponder.respond(result, "Rules evaluated successfully", APIResponseType.SUCCESS,
					HttpStatus.OK);
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationErrorException(ex.getMessage());
		}
	}

	/**
	 * Evaluates rules on-demand using time logs from the request body. Dry-run/preview —
	 * no timesheet totals are persisted. Unlike {@code /evaluate}, agency users do not go
	 * through per-timesheet VIEW access checks; timesheets are still loaded only for the
	 * authenticated account.
	 */
	@PostMapping("/evaluate-overtime")
	@Override
	public ResponseEntity<?> evaluateRulesOnDemand(
			@Validated @RequestBody BulkUpdateTimeLogsRequestBodyDto requestBodyDto) {
		try {
			java.util.List<io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto> result = this.ruleEngineService
				.evaluateRulesOnDemand(requestBodyDto);
			return this.apiResponder.respond(result, "Rules evaluated successfully (on-demand)",
					APIResponseType.SUCCESS, HttpStatus.OK);
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationErrorException(ex.getMessage());
		}
	}

	/**
	 * Validates all rule configurations and system readiness.
	 */
	@PostMapping("/validate")
	@Override
	public ResponseEntity<?> validateRules(@Validated @RequestBody RuleEngineRequestBodyDto requestBodyDto) {
		try {
			String result = this.ruleEngineService.validateRules(requestBodyDto);
			return this.apiResponder.respond(result, "All rules are valid");
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationErrorException(ex.getMessage());
		}
	}

}