/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IRuleEvaluator {

	WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet);

	default void populateMoneyData(RuleEvaluatorResult ruleEvaluatorResult) {
		if (ruleEvaluatorResult == null) {
			return;
		}

		Map<TimeLog, MoneyData> moneyDataMap = new HashMap<>();
		MoneyData weeklyOvertimeMoneyData = MoneyData.zero();

		// Process daily money data
		for (Map.Entry<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog, List<RuleEvaluationResult>> entry : ruleEvaluatorResult
			.getRuleEvaluationResults()
			.entrySet()) {

			MoneyData dailyMoneyData = MoneyData.zero();

			for (RuleEvaluationResult ruleEvaluationResult : entry.getValue()) {
				if (ruleEvaluationResult != null && ruleEvaluationResult.hasMonetaryValues()) {
					MoneyData resultMoneyData = MoneyData.builder()
						.payAmount(ruleEvaluationResult.getPayAmount())
						.billAmount(ruleEvaluationResult.getBillAmount())
						.build();
					dailyMoneyData = dailyMoneyData.add(resultMoneyData);
				}
			}
			moneyDataMap.put(entry.getKey(), dailyMoneyData);
		}

		// Process weekly overtime money data
		if (ruleEvaluatorResult.getWeeklyOvertimeRuleEvaluationResult() != null) {
			RuleEvaluationResult weeklyResult = ruleEvaluatorResult.getWeeklyOvertimeRuleEvaluationResult();
			if (weeklyResult.hasMonetaryValues()) {
				weeklyOvertimeMoneyData = MoneyData.builder()
					.payAmount(weeklyResult.getPayAmount())
					.billAmount(weeklyResult.getBillAmount())
					.build();
			}
		}

		ruleEvaluatorResult.setMoneyData(moneyDataMap);
		ruleEvaluatorResult.setWeeklyOvertimeMoneyData(weeklyOvertimeMoneyData);
	}

}
