/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.RangeSet;

/**
 * Result container for rule evaluation across a timesheet Contains aggregated results
 * from all rule evaluations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluatorResult {

	private Timesheet timesheet;

	@Builder.Default
	private Map<TimeLog, List<RuleEvaluationResult>> ruleEvaluationResults = new HashMap<>();

	private RuleEvaluationResult weeklyOvertimeRuleEvaluationResult;

	@Builder.Default
	private Map<TimeLog, MoneyData> moneyData = new HashMap<>();

	private MoneyData weeklyOvertimeMoneyData;

	@Builder.Default
	private List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

	/**
	 * Adds a rule evaluation result for a specific time log
	 * @param timeLog the time log associated with the evaluation
	 * @param ruleEvaluationResult the result to add
	 */
	public void addRuleEvaluationResult(TimeLog timeLog, RuleEvaluationResult ruleEvaluationResult) {
		if (timeLog == null) {
			throw new IllegalArgumentException("TimeLog cannot be null");
		}
		if (ruleEvaluationResult == null) {
			throw new IllegalArgumentException("RuleEvaluationResult cannot be null");
		}

		this.ruleEvaluationResults.computeIfAbsent(timeLog, (k) -> new ArrayList<>()).add(ruleEvaluationResult);
	}

	/**
	 * Gets all rule evaluation results for a specific time log
	 * @param timeLog the time log to get results for
	 * @return list of rule evaluation results, or empty list if none found
	 */
	public List<RuleEvaluationResult> getRuleEvaluationResultsForTimeLog(TimeLog timeLog) {
		return this.ruleEvaluationResults.getOrDefault(timeLog, new ArrayList<>());
	}

	/**
	 * Gets money data for a specific time log
	 * @param timeLog the time log to get money data for
	 * @return money data, or null if not found
	 */
	public MoneyData getMoneyDataForTimeLog(TimeLog timeLog) {
		return this.moneyData.get(timeLog);
	}

	/**
	 * Checks if there are any rule evaluation results
	 * @return true if there are results, false otherwise
	 */
	public boolean hasResults() {
		return !this.ruleEvaluationResults.isEmpty() || this.weeklyOvertimeRuleEvaluationResult != null;
	}

	/**
	 * Gets the total number of rule evaluations performed
	 * @return total count of rule evaluations
	 */
	public int getTotalRuleEvaluations() {
		int count = this.ruleEvaluationResults.values().stream().mapToInt(List::size).sum();

		if (this.weeklyOvertimeRuleEvaluationResult != null) {
			count++;
		}

		return count;
	}

	public BigDecimal getTotalBillAmount() {
		return this.ruleEvaluationResults.values()
			.stream()
			.flatMap(Collection::stream)
			.map(RuleEvaluationResult::getTotalBillAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getTotalPayAmount() {
		return this.ruleEvaluationResults.values()
			.stream()
			.flatMap(Collection::stream)
			.map(RuleEvaluationResult::getTotalPayAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public MoneyData getMoneyData() {
		return this.moneyData.values().stream().reduce(MoneyData.zero(), MoneyData::add);
	}

	/**
	 * Adds weekly overtime candidate time ranges
	 * @param timeRanges the time ranges to add
	 */
	public void addWeeklyOvertimeCandidateTimeRanges(List<RangeSet<LocalTime>> timeRanges) {
		if (timeRanges != null && !timeRanges.isEmpty()) {
			this.weeklyOvertimeCandidateTimeRanges.addAll(timeRanges);
		}
	}

	/**
	 * Gets all weekly overtime candidate time ranges
	 * @return list of all weekly overtime candidate time ranges
	 */
	public List<RangeSet<LocalTime>> getAllWeeklyOvertimeCandidateTimeRanges() {
		return this.weeklyOvertimeCandidateTimeRanges;
	}

}
