/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for multiple weekly rule evaluation results. Each week gets its own
 * RuleEvaluatorResult since calculations are performed on a weekly basis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyRuleEvaluatorResult {

	private Timesheet timesheet;

	@Builder.Default
	private List<WeeklyResult> weeklyResults = new ArrayList<>();

	/**
	 * Adds a weekly result to the collection.
	 */
	public void addWeeklyResult(LocalDate weekStartDate, LocalDate weekEndDate, RuleEvaluatorResult result) {
		WeeklyResult weeklyResult = WeeklyResult.builder()
			.weekStartDate(weekStartDate)
			.weekEndDate(weekEndDate)
			.ruleEvaluatorResult(result)
			.build();
		this.weeklyResults.add(weeklyResult);
	}

	/**
	 * Gets the total number of weeks evaluated.
	 */
	public int getWeekCount() {
		return this.weeklyResults.size();
	}

	/**
	 * Checks if there are any weekly results.
	 */
	public boolean hasResults() {
		return !this.weeklyResults.isEmpty();
	}

	/**
	 * Gets all rule evaluator results from all weeks.
	 */
	public List<RuleEvaluatorResult> getAllRuleEvaluatorResults() {
		return this.weeklyResults.stream().map(WeeklyResult::getRuleEvaluatorResult).toList();
	}

	/**
	 * Represents a single week's evaluation result with its date range.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WeeklyResult {

		private LocalDate weekStartDate;

		private LocalDate weekEndDate;

		private RuleEvaluatorResult ruleEvaluatorResult;

		/**
		 * Gets a descriptive week identifier.
		 */
		public String getWeekIdentifier() {
			return String.format("Week of %s to %s", this.weekStartDate, this.weekEndDate);
		}

	}

}