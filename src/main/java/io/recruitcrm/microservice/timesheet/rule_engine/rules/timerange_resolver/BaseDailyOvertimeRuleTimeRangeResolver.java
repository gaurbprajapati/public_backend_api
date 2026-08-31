/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.VirtualTimeRangeAllocator;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public abstract class BaseDailyOvertimeRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	protected final Logger logger;

	protected final VirtualTimeRangeAllocator virtualTimeRangeAllocator;

	protected BaseDailyOvertimeRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
		this.virtualTimeRangeAllocator = new VirtualTimeRangeAllocator(logger);
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		// Check if current time log is the last interval for this day
		// Daily overtime should only be applied once per day, on the last interval
		if (!isLastIntervalOfDay(timeRangeResolverContext)) {
			this.logger.logDebug("DO not applied on this interval: not the last interval of the day");
			return TreeRangeSet.create();
		}

		// Get effective work hours accumulated so far (aggregated across all same-day
		// intervals)
		Duration effectiveWorkHours = getEffectiveWorkHours(timeRangeResolverContext);

		// Get current DO rule threshold
		CustomRule currentRule = timeRangeResolverContext.getCurrentCustomRuleBeingEvaluated();
		Duration currentThreshold = currentRule.getDailyThreshold();

		// Check if current rule is applicable on this work day
		TimeLog currentTimeLog = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated();
		io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay workDay = io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper
			.getWorkDayFromLocalDate(currentTimeLog.getDate());

		if (!currentRule.isApplicableOnDay(workDay)) {
			this.logger.logInfo("DO not applied: rule is not applicable on this day (" + workDay + ")");
			return TreeRangeSet.create();
		}

		// Calculate DO duration based on effective work hours and thresholds
		Duration doDuration = calculateDODuration(effectiveWorkHours, currentThreshold, timeRangeResolverContext);

		if (doDuration.isZero()) {
			this.logger.logInfo("DO not applied: no overtime hours to claim (effective work hours: "
					+ TimeHelper.convertDurationToApproximateHours(effectiveWorkHours) + ", threshold: "
					+ TimeHelper.convertDurationToApproximateHours(currentThreshold) + ")");
			return TreeRangeSet.create();
		}

		// Allocate virtual time ranges for the DO duration
		RangeSet<LocalTime> allocatedRanges = allocateVirtualTimeRanges(doDuration, timeRangeResolverContext);

		this.logger.logInfo("DO applied: allocated " + TimeHelper.convertDurationToApproximateHours(doDuration)
				+ " hours as virtual time ranges");

		return allocatedRanges;
	}

	/**
	 * Gets the effective work hours using the correct formula: Total Time logged - break
	 * time - adjusted regular hours break threshold. When a day has multiple intervals
	 * (e.g., 9:00-11:00 and 11:00-17:00), this method aggregates all same-day intervals
	 * to calculate total effective work hours for daily overtime threshold comparison.
	 * @param timeRangeResolverContext the context containing evaluation state
	 * @return the effective work hours
	 */
	protected Duration getEffectiveWorkHours(TimeRangeResolverContext timeRangeResolverContext) {
		// Get the aggregated effective working time across all same-day intervals
		// This ensures daily overtime is calculated based on total daily work, not
		// per-interval
		Duration effectiveWorkTime = timeRangeResolverContext.getAdjustedDailyEffectiveWorkingTime();

		this.logger.logDebug("Aggregated daily effective work hours calculation: "
				+ TimeHelper.convertDurationToApproximateHours(effectiveWorkTime) + " hours");

		return effectiveWorkTime;
	}

	/**
	 * Calculates the DO duration based on effective work hours and rule thresholds. This
	 * method handles the logic for determining overtime duration considering next
	 * thresholds.
	 * @param effectiveWorkHours the effective work hours accumulated so far
	 * @param currentThreshold the current rule's threshold
	 * @param timeRangeResolverContext the context containing evaluation state
	 * @return the daily overtime duration to claim
	 */
	protected Duration calculateDODuration(Duration effectiveWorkHours, Duration currentThreshold,
			TimeRangeResolverContext timeRangeResolverContext) {

		// Find next applicable DO rule threshold
		Duration nextThreshold = Duration.ofHours(24); // Default to 24 hours if no next
														// rule

		CustomRule nextApplicableRule = findNextApplicableDailyOvertimeRule(timeRangeResolverContext);
		if (nextApplicableRule != null) {
			nextThreshold = nextApplicableRule.getDailyThreshold();
		}

		// Use TimeHelper to calculate DO hours based on effective work time
		return TimeHelper.calculateDOHours(effectiveWorkHours, currentThreshold, nextThreshold);
	}

	/**
	 * Allocates virtual time ranges for the calculated DO duration. When multiple
	 * same-day intervals exist, uses a merged time log covering the full day range and
	 * marks gaps between intervals as occupied so OT is only placed within actual work
	 * time.
	 */
	protected RangeSet<LocalTime> allocateVirtualTimeRanges(Duration doDuration,
			TimeRangeResolverContext timeRangeResolverContext) {

		RangeSet<LocalTime> occupiedRanges = timeRangeResolverContext.getOccupiedTimeRanges();
		TimeLog currentTimeLog = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated();
		List<TimeLog> sameDayTimeLogs = timeRangeResolverContext.getSameDayTimeLogs();

		// Use merged time log for multi-interval days so OT spans the full day range
		TimeLog allocationTarget = TimeHelper.createMergedTimeLog(currentTimeLog, sameDayTimeLogs);

		// Mark gaps between intervals as occupied so OT is only allocated
		// within actual worked time, not in unworked gaps
		occupiedRanges.addAll(TimeHelper.getGapsBetweenIntervals(sameDayTimeLogs));

		return this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(doDuration, allocationTarget, occupiedRanges);
	}

	/**
	 * Finds the next applicable daily overtime rule that matches both the rule type and
	 * workday criteria. This method searches through the sorted rules list starting from
	 * the current rule index + 1 to find the next rule that is applicable on the given
	 * work day.
	 * @param timeRangeResolverContext the context containing evaluation state
	 * @return the next applicable daily overtime rule, or null if none found
	 */
	private CustomRule findNextApplicableDailyOvertimeRule(TimeRangeResolverContext timeRangeResolverContext) {
		int currentRuleIndex = timeRangeResolverContext.getCurrentRuleIndex();
		List<CustomRule> sortedCustomRules = timeRangeResolverContext.getInternalSortedCustomRules();

		TimeLog currentTimeLog = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated();

		// Get work day for the current time log
		io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay workDay = io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper
			.getWorkDayFromLocalDate(currentTimeLog.getDate());

		// Search for the next applicable daily overtime rule starting from current index
		// + 1
		for (int i = currentRuleIndex + 1; i < sortedCustomRules.size(); i++) {
			CustomRule rule = sortedCustomRules.get(i);
			if (rule.getRuleType() == getDailyOvertimeRuleType() && rule.isApplicableOnDay(workDay)) {
				this.logger.logDebug("Found next applicable daily overtime rule at index " + i + " with threshold: "
						+ rule.getDailyThreshold());
				return rule;
			}
		}

		this.logger.logDebug("No next applicable daily overtime rule found for work day: " + workDay);
		return null;
	}

	/**
	 * Gets the daily overtime rule type for this resolver.
	 * @return the rule type for daily overtime
	 */
	protected abstract RuleType getDailyOvertimeRuleType();

	/**
	 * Checks if the current time log is the last interval for its day. Daily overtime
	 * should only be applied once per day, on the last interval.
	 */
	protected boolean isLastIntervalOfDay(TimeRangeResolverContext timeRangeResolverContext) {
		TimeLog currentTimeLog = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated();
		List<TimeLog> sameDayTimeLogs = timeRangeResolverContext.getSameDayTimeLogs();

		return TimeHelper.isLastIntervalOfDay(currentTimeLog, sameDayTimeLogs);
	}

}