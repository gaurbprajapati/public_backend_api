/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.RangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Base class for rule implementations providing common functionality
 */
public abstract class BaseRule implements IRule {

	protected final Logger logger;

	protected BaseRule(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void validate() {
		// Default validation - subclasses can override if needed
		this.logger.logDebug(MessageFormat.format("Validating rule: {0}", getName()));
	}

	/**
	 * Creates a basic rule evaluation result with the given time range
	 * @param timeRange the time range for this rule
	 * @return a new RuleEvaluationResult
	 */
	protected RuleEvaluationResult createResult(RangeSet<LocalTime> timeRange) {
		return RuleEvaluationResult.builder().timeRange(timeRange).build();
	}

	/**
	 * Creates a rule evaluation result with monetary amounts
	 * @param timeRange the time range for this rule
	 * @param payAmount the pay amount
	 * @param billAmount the bill amount
	 * @return a new RuleEvaluationResult
	 */
	protected RuleEvaluationResult createResult(RangeSet<LocalTime> timeRange, BigDecimal payAmount,
			BigDecimal billAmount) {
		return RuleEvaluationResult.builder().timeRange(timeRange).payAmount(payAmount).billAmount(billAmount).build();
	}

	/**
	 * Creates a rule evaluation result with weekly overtime hours
	 * @param timeRange the time range for this rule
	 * @param weeklyOvertimeHours the weekly overtime hours
	 * @return a new RuleEvaluationResult
	 */
	protected RuleEvaluationResult createResult(RangeSet<LocalTime> timeRange, Duration weeklyOvertimeHours) {
		return RuleEvaluationResult.builder().timeRange(timeRange).weeklyOvertimeHours(weeklyOvertimeHours).build();
	}

	/**
	 * Creates a complete rule evaluation result with all fields populated
	 * @param context the evaluation context containing rule information
	 * @param timeRange the time range for this rule
	 * @param payAmount the pay amount
	 * @param billAmount the bill amount
	 * @return a new RuleEvaluationResult with all fields populated
	 */
	protected RuleEvaluationResult createCompleteResult(RuleEvaluationContext context, RangeSet<LocalTime> timeRange,
			BigDecimal payAmount, BigDecimal billAmount) {

		// Determine rule type and name
		RuleType ruleType = getRuleType(context);
		String ruleName = getRuleName(context);

		// Calculate evaluated duration
		Duration evaluatedDuration = TimeHelper.convertRangeSetToDuration(timeRange);

		// Create metadata
		String metadata = createMetadata(context, timeRange, evaluatedDuration);

		// Extract evaluation date and rule index
		LocalDate evaluationDate = null;
		if (context.getCurrentTimeLogBeingEvaluated() != null) {
			evaluationDate = context.getCurrentTimeLogBeingEvaluated().getDate();
		}
		Integer ruleIndex = context.getCurrentRuleIndex();

		// Determine if this is a virtual rule (Regular Hours or Break)
		boolean virtualRule = isVirtualRule(ruleType);

		return RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(ruleType)
			.ruleName(ruleName)
			.payAmount(payAmount)
			.billAmount(billAmount)
			.payRateMultiplier(getPayRateMultiplier(context))
			.billRateMultiplier(getBillRateMultiplier(context))
			.evaluatedDuration(evaluatedDuration)
			.metadata(metadata)
			.evaluationDate(evaluationDate)
			.ruleIndex(ruleIndex)
			.virtualRule(virtualRule)
			.successful(true)
			.build();
	}

	/**
	 * Creates a complete rule evaluation result with weekly overtime hours
	 * @param context the evaluation context containing rule information
	 * @param timeRange the time range for this rule
	 * @param weeklyOvertimeHours the weekly overtime hours
	 * @param payAmount the pay amount
	 * @param billAmount the bill amount
	 * @return a new RuleEvaluationResult with all fields populated
	 */
	protected RuleEvaluationResult createCompleteResult(RuleEvaluationContext context, RangeSet<LocalTime> timeRange,
			Duration weeklyOvertimeHours, BigDecimal payAmount, BigDecimal billAmount) {

		// Determine rule type and name
		RuleType ruleType = getRuleType(context);
		String ruleName = getRuleName(context);

		// Calculate evaluated duration
		Duration evaluatedDuration = TimeHelper.convertRangeSetToDuration(timeRange);

		// Create metadata
		String metadata = createMetadata(context, timeRange, evaluatedDuration);

		// Extract evaluation date and rule index
		LocalDate evaluationDate = null;
		if (context.getCurrentTimeLogBeingEvaluated() != null) {
			evaluationDate = context.getCurrentTimeLogBeingEvaluated().getDate();
		}
		Integer ruleIndex = context.getCurrentRuleIndex();

		// Determine if this is a virtual rule (Regular Hours or Break)
		boolean virtualRule = isVirtualRule(ruleType);

		return RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(ruleType)
			.ruleName(ruleName)
			.weeklyOvertimeHours(weeklyOvertimeHours)
			.payAmount(payAmount)
			.billAmount(billAmount)
			.payRateMultiplier(getPayRateMultiplier(context))
			.billRateMultiplier(getBillRateMultiplier(context))
			.evaluatedDuration(evaluatedDuration)
			.metadata(metadata)
			.evaluationDate(evaluationDate)
			.ruleIndex(ruleIndex)
			.virtualRule(virtualRule)
			.successful(true)
			.build();
	}

	/**
	 * Determines the rule type from the context
	 * @param context the evaluation context
	 * @return the rule type
	 */
	protected RuleType getRuleType(RuleEvaluationContext context) {
		CustomRule currentRule = context.getCurrentRuleBeingEvaluated();
		if (currentRule != null) {
			return currentRule.getRuleType();
		}
		// For virtual rules (Regular Hours and Break), determine based on the rule
		// implementation
		return getDefaultRuleType();
	}

	/**
	 * Gets the rule name from the context
	 * @param context the evaluation context
	 * @return the rule name
	 */
	protected String getRuleName(RuleEvaluationContext context) {
		CustomRule currentRule = context.getCurrentRuleBeingEvaluated();
		if (currentRule != null) {
			return currentRule.getRuleName();
		}
		// For virtual rules, use the rule's getName() method
		return getName();
	}

	/**
	 * Gets the pay rate multiplier from the rule config being evaluated. Null for virtual
	 * rules (no custom rule in context) and for rules whose config carries no multiplier
	 * (e.g. FIXED_RATE charge method).
	 * @param context the evaluation context
	 * @return the pay rate multiplier, or null
	 */
	protected Float getPayRateMultiplier(RuleEvaluationContext context) {
		CustomRule currentRule = context.getCurrentRuleBeingEvaluated();
		return (currentRule != null) ? currentRule.getPayRateMultiplier() : null;
	}

	/**
	 * Gets the bill rate multiplier from the rule config being evaluated. Null for
	 * virtual rules (no custom rule in context) and for rules whose config carries no
	 * multiplier (e.g. FIXED_RATE charge method).
	 * @param context the evaluation context
	 * @return the bill rate multiplier, or null
	 */
	protected Float getBillRateMultiplier(RuleEvaluationContext context) {
		CustomRule currentRule = context.getCurrentRuleBeingEvaluated();
		return (currentRule != null) ? currentRule.getBillRateMultiplier() : null;
	}

	/**
	 * Creates metadata for the rule evaluation result
	 * @param context the evaluation context
	 * @param timeRange the time range evaluated
	 * @param evaluatedDuration the duration evaluated
	 * @return metadata string
	 */
	protected String createMetadata(RuleEvaluationContext context, RangeSet<LocalTime> timeRange,
			Duration evaluatedDuration) {

		StringBuilder metadata = new StringBuilder();
		metadata.append("Rule: ").append(getRuleName(context));

		// Handle case where there's no current time log (e.g., weekly overtime rules)
		if (context.getCurrentTimeLogBeingEvaluated() != null) {
			metadata.append(", Date: ").append(context.getCurrentTimeLogBeingEvaluated().getDate());
		}
		else {
			metadata.append(", Date: Weekly Evaluation");
		}

		metadata.append(", Duration: ").append(formatDuration(evaluatedDuration));

		if (timeRange != null && !timeRange.isEmpty()) {
			metadata.append(", TimeRanges: ");
			metadata.append(timeRange.asRanges().toString());
		}

		CustomRule currentRule = context.getCurrentRuleBeingEvaluated();
		if (currentRule != null) {
			metadata.append(", RuleIndex: ").append(context.getCurrentRuleIndex());
		}

		return metadata.toString();
	}

	/**
	 * Formats duration in a readable format
	 * @param duration the duration to format
	 * @return formatted duration string
	 */
	protected String formatDuration(Duration duration) {
		if (duration == null) {
			return "0h 0m";
		}

		long hours = duration.toHours();
		long minutes = duration.toMinutesPart();
		return String.format("%dh %dm", hours, minutes);
	}

	/**
	 * Gets the default rule type for this rule implementation
	 * @return the default rule type
	 */
	protected abstract RuleType getDefaultRuleType();

	/**
	 * Determines if a rule type is a virtual/system rule
	 * @param ruleType the rule type to check
	 * @return true if the rule type is a virtual rule, false otherwise
	 */
	protected boolean isVirtualRule(RuleType ruleType) {
		if (ruleType == null) {
			return false;
		}

		// Virtual rules are Regular Hours, Break, and Default Pay rules
		return ruleType == RuleType.RANGE_BASED_REGULAR_HOURS || ruleType == RuleType.DURATION_BASED_REGULAR_HOURS
				|| ruleType == RuleType.RANGE_BASED_BREAK || ruleType == RuleType.DURATION_BASED_BREAK
				|| ruleType == RuleType.RANGE_BASED_DEFAULT_PAY || ruleType == RuleType.DURATION_BASED_DEFAULT_PAY;
	}

	/**
	 * Validates that the evaluation context is not null
	 * @param context the evaluation context to validate
	 * @throws IllegalArgumentException if context is null
	 */
	protected void validateContext(RuleEvaluationContext context) {
		if (context == null) {
			throw new IllegalArgumentException("Rule evaluation context cannot be null");
		}
	}

	/**
	 * Logs rule evaluation start
	 * @param context the evaluation context
	 */
	protected void logEvaluationStart(RuleEvaluationContext context) {
		this.logger.logDebug(MessageFormat.format("Starting evaluation of rule: {0} for timesheet: {1}", getName(),
				context.getTimesheet().getId()));
	}

	/**
	 * Logs rule evaluation completion
	 * @param result the evaluation result
	 */
	protected void logEvaluationComplete(RuleEvaluationResult result) {
		this.logger
			.logDebug(MessageFormat.format("Completed evaluation of rule: {0} with result: {1}", getName(), result));
	}

	/**
	 * Initializes rule fields. Subclasses should implement their own field assignments.
	 * RegularHoursRule should override for the 1.0f multiplier edge case.
	 */
	protected void initRatesAndMethods(RuleEvaluationContext ctx) {
		// Subclasses should override this method to assign their specific fields
	}

}