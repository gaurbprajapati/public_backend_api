/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.RuleEvaluationStrategy;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RuleEvaluator implements IRuleEvaluator {

	private final List<RuleEvaluationStrategy> evaluationStrategies;

	@Autowired
	public RuleEvaluator(List<RuleEvaluationStrategy> evaluationStrategies) {
		this.evaluationStrategies = evaluationStrategies;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
		Integer workLogType = timesheet.getTimesheetSetting().getWorkLogType();

		RuleEvaluationStrategy strategy = this.evaluationStrategies.stream()
			.filter((s) -> s.canHandle(workLogType))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unsupported WorkLogType: " + workLogType));

		return strategy.evaluateRules(timesheet);
	}

	/**
	 * Evaluates rules using pre-built time log DTOs (for on-demand evaluation). Selects
	 * the appropriate strategy based on work log type, then delegates to the strategy's
	 * on-demand evaluation method.
	 * @param timesheet the timesheet containing rule configuration and settings
	 * @param timeLogs the pre-built time log DTOs to evaluate
	 * @return WeeklyRuleEvaluatorResult containing weekly evaluation results
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public WeeklyRuleEvaluatorResult evaluateRulesWithTimeLogs(Timesheet timesheet, List<TimeLog> timeLogs) {
		Integer workLogType = timesheet.getTimesheetSetting().getWorkLogType();

		RuleEvaluationStrategy strategy = this.evaluationStrategies.stream()
			.filter((s) -> s.canHandle(workLogType))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unsupported WorkLogType: " + workLogType));

		return strategy.evaluateRulesWithTimeLogs(timesheet, timeLogs);
	}

}
