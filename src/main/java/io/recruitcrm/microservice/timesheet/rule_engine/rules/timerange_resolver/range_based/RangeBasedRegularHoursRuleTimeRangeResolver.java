/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Time range resolver for range-based regular hours rules.
 *
 * This resolver determines the time ranges for regular hours based on timesheet settings.
 * Regular hours ranges are calculated as the full work time range. Break time is handled
 * separately by the Break rule through the occupied time ranges mechanism.
 */
public class RangeBasedRegularHoursRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	private final Logger logger;

	public RangeBasedRegularHoursRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Main entry point — calculates the time ranges that qualify as "Regular Hours".
	 * Skips DAY_OFF, gets template work hours, subtracts occupied ranges and break
	 * threshold, then caps at the daily overtime threshold on the last interval.
	 * @param timeRangeResolverContext the context holding the current time log, occupied
	 * ranges, same-day time logs, custom rules, and timesheet settings
	 * @return a RangeSet of time ranges qualifying as regular hours, or empty if DAY_OFF
	 * or invalid
	 */
	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		// Skip processing if this is a day off (no regular hours on non-working days)
		if (timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getDayType() == WorkDayType.DAY_OFF) {
			return TreeRangeSet.create(); // Return empty set - no regular hours on
											// DAY_OFF
		}

		// Extract the date from current time log to determine day of week (Monday,
		// Tuesday, etc.)
		LocalDate timeLogDate = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getDate();
		// Convert LocalDate to WorkDay enum (MONDAY=1, TUESDAY=2, etc.)
		WorkDay currentTimeLogWorkDay = TimeHelper.getWorkDayFromLocalDate(timeLogDate);
		// Lookup the template work schedule for this day of week from timesheet settings
		// Example: Monday template might be 9:00 AM - 5:00 PM with 30-min break threshold
		TemplateWorkDay currentTimeLogTemplateWorkDay = TimeHelper.getTemplateWorkDayFromDayType(
				timeRangeResolverContext.getCurrentTimesheetSetting().getTemplateWorkDays(), currentTimeLogWorkDay);

		// Validate that work start time is before work end time
		LocalTime workStartTime = currentTimeLogTemplateWorkDay.getWorkStartTime();
		LocalTime workEndTime = currentTimeLogTemplateWorkDay.getWorkEndTime();
		// Prevent invalid configurations like start=17:00, end=9:00 or null values
		if (workStartTime == null || workEndTime == null || !workStartTime.isBefore(workEndTime)) {
			return TreeRangeSet.create(); // Return empty range set if validation fails
		}

		// Create the full regular hours range (work start time to work end time)
		// This represents the maximum possible regular hours for this day (e.g.,
		// 9:00-17:00)
		// Break time will be handled separately by the Break rule through occupied time
		// ranges
		Range<LocalTime> fullRegularHoursRange = TimeHelper.toRange(workStartTime, workEndTime);

		// Get occupied time ranges from context and return available ranges
		// occupiedTimeRanges contains time already claimed by previous rules in the
		// sequence
		// getAvailableTimeRanges subtracts occupied ranges from the full range
		// Example: If Break rule already claimed 12:00-12:30, this removes that from
		// available time
		RangeSet<LocalTime> availableTimeRanges = TimeHelper.getAvailableTimeRanges(fullRegularHoursRange,
				timeRangeResolverContext.getOccupiedTimeRanges());

		// Apply break time threshold adjustment if needed
		// Break threshold = minimum break time employee is entitled to (e.g., 1 hour)
		// If actual breaks < threshold, deduct the difference — but only if the
		// employee's actual work exceeds expected hours (template - threshold).
		// Uses ALL same-day intervals' breaks so the threshold is applied per-day.
		Duration breakTimeThresholdAdjustment = calculateBreakTimeThresholdAdjustmentWithRangeConstraint(
				timeRangeResolverContext.getCurrentTimeLogBeingEvaluated(),
				timeRangeResolverContext.getSameDayTimeLogs(), timeRangeResolverContext.getCurrentTimesheetSetting(),
				fullRegularHoursRange);

		// Cap the adjustment so we never penalize below expected hours.
		// Expected = template - threshold. If actual work ≤ expected, no trim needed.
		breakTimeThresholdAdjustment = capBreakThresholdAdjustment(breakTimeThresholdAdjustment,
				timeRangeResolverContext, fullRegularHoursRange, availableTimeRanges);

		// Set the adjusted break threshold in context for Daily Overtime (DO) resolvers
		// to use
		timeRangeResolverContext.setAdjustedRegularHoursBreakThreshold(breakTimeThresholdAdjustment);

		if (!breakTimeThresholdAdjustment.isZero()) {
			availableTimeRanges = adjustTimeRangesFromEnd(availableTimeRanges, breakTimeThresholdAdjustment);
			this.logger.logDebug(
					String.format("Applied break time threshold adjustment to available time ranges - Adjustment: %s",
							breakTimeThresholdAdjustment));
		}

		// Apply daily overtime threshold limit for same-day intervals
		// This ensures regular hours across all same-day intervals don't exceed the daily
		// overtime threshold
		// Example: If daily OT threshold = 8h and total regular hours = 9h, limit to 8h
		// regular + 1h for OT
		availableTimeRanges = limitToRemainingRegularHours(availableTimeRanges, timeRangeResolverContext);

		return availableTimeRanges;
	}

	/**
	 * Caps regular hours at the daily overtime threshold. For multi-interval days, sums
	 * regular hours across all intervals and only trims the last interval if total
	 * exceeds the threshold.
	 * @param availableTimeRanges the time ranges available for regular hours before
	 * capping
	 * @param context the context holding same-day time logs, custom rules, and current
	 * time log
	 * @return the capped time ranges, or original ranges if no daily overtime threshold
	 * exists or total is within the threshold
	 */
	private RangeSet<LocalTime> limitToRemainingRegularHours(RangeSet<LocalTime> availableTimeRanges,
			TimeRangeResolverContext context) {
		// Find the lowest applicable daily overtime threshold from custom rules
		// Example: If multiple Daily OT rules exist (8h, 10h), use the lowest (8h)
		Duration dailyOvertimeThreshold = findLowestApplicableDailyOvertimeThreshold(context);

		// If no daily overtime threshold found, no limiting needed
		// This happens when no Daily OT rules are configured for this timesheet
		if (dailyOvertimeThreshold == null) {
			return availableTimeRanges; // Return original ranges unchanged
		}

		List<TimeLog> sameDayTimeLogs = context.getSameDayTimeLogs();
		TimeLog currentTimeLog = context.getCurrentTimeLogBeingEvaluated();

		// Calculate total regular hours duration across all same-day intervals
		Duration totalRegularHoursWorkDuration;
		if (sameDayTimeLogs != null && sameDayTimeLogs.size() > 1) {
			// Multiple intervals: calculate cumulative regular hours across all intervals
			// Example: Morning 3h + Afternoon 5h = 8h total
			// This ensures we see the full day's regular hours, not just current interval
			totalRegularHoursWorkDuration = calculateCumulativeRegularHours(sameDayTimeLogs, context);

			// Subtract the break threshold adjustment from cumulative regular hours.
			// calculateCumulativeRegularHours computes the raw intersection of work
			// intervals with the template range (minus actual breaks), but does not
			// account for the break threshold adjustment already applied to
			// availableTimeRanges. Without this subtraction, the threshold adjustment
			// is double-counted: once when reducing availableTimeRanges from the end,
			// and again here when comparing against the daily overtime threshold.
			Duration breakThresholdAdjustment = context.getAdjustedRegularHoursBreakThreshold();
			if (breakThresholdAdjustment != null && !breakThresholdAdjustment.isZero()) {
				totalRegularHoursWorkDuration = totalRegularHoursWorkDuration.minus(breakThresholdAdjustment);
				if (totalRegularHoursWorkDuration.isNegative()) {
					totalRegularHoursWorkDuration = Duration.ZERO;
				}
			}
		}
		else {
			// Single time log: use current available ranges duration
			// No need for cross-interval calculation when only one interval exists
			// Note: availableTimeRanges already has break threshold adjustment applied,
			// so no additional subtraction is needed here.
			totalRegularHoursWorkDuration = calculateTotalDurationFromRanges(availableTimeRanges);
		}

		// Calculate how much overtime would occur if we don't limit regular hours
		Duration potentialOvertime = Duration.ZERO;
		if (totalRegularHoursWorkDuration.compareTo(dailyOvertimeThreshold) > 0) {
			// Total regular hours exceed threshold - calculate excess
			// Example: 9h total - 8h threshold = 1h potential overtime
			potentialOvertime = totalRegularHoursWorkDuration.minus(dailyOvertimeThreshold);
		}

		// If no overtime would occur, no limiting needed
		if (potentialOvertime.isZero()) {
			return availableTimeRanges; // Total is within threshold, no changes needed
		}

		// For single time log or last interval, apply the limit
		// Multi-interval logic: Only limit on the LAST interval to avoid double-limiting
		// Single interval logic: Always limit since there's only one interval
		boolean shouldLimit = (sameDayTimeLogs == null || sameDayTimeLogs.size() <= 1)
				|| isLastIntervalOfDay(currentTimeLog, sameDayTimeLogs);

		if (shouldLimit) {
			// Limit the available time ranges by removing overtime from the end
			// Example: Remove 1h from end of [13:00-18:00] to get [13:00-17:00]
			// This makes 1h available for Daily OT rule to claim
			RangeSet<LocalTime> limitedRanges = adjustTimeRangesFromEnd(availableTimeRanges, potentialOvertime);

			this.logger.logDebug(String.format(
					"Limited regular hours: total regular hours=%s, threshold=%s, overtime=%s, removed=%s",
					totalRegularHoursWorkDuration, dailyOvertimeThreshold, potentialOvertime, potentialOvertime));

			return limitedRanges;
		}

		// Not the last interval, return full regular hours
		// Example: Morning interval gets full 3h, afternoon interval will be limited
		return availableTimeRanges;
	}

	/**
	 * Sums regular hours across all same-day intervals by intersecting each interval's
	 * work range with the template Regular Hours range and subtracting breaks.
	 * @param sameDayTimeLogs all time logs for the same day (multiple intervals)
	 * @param context the context holding the template work day settings for the current
	 * date
	 * @return the total duration of regular hours across all intervals, excluding breaks
	 */
	private Duration calculateCumulativeRegularHours(List<TimeLog> sameDayTimeLogs, TimeRangeResolverContext context) {
		if (sameDayTimeLogs == null || sameDayTimeLogs.isEmpty()) {
			return Duration.ZERO;
		}

		// Get the template Regular Hours range for this day of week
		// This represents the "standard" work schedule (e.g., Monday 9:00-17:00)
		LocalDate timeLogDate = context.getCurrentTimeLogBeingEvaluated().getDate();
		WorkDay currentTimeLogWorkDay = TimeHelper.getWorkDayFromLocalDate(timeLogDate);
		TemplateWorkDay currentTimeLogTemplateWorkDay = TimeHelper.getTemplateWorkDayFromDayType(
				context.getCurrentTimesheetSetting().getTemplateWorkDays(), currentTimeLogWorkDay);

		LocalTime templateStartTime = currentTimeLogTemplateWorkDay.getWorkStartTime();
		LocalTime templateEndTime = currentTimeLogTemplateWorkDay.getWorkEndTime();

		// Validate template times before proceeding
		if (templateStartTime == null || templateEndTime == null || !templateStartTime.isBefore(templateEndTime)) {
			return Duration.ZERO; // Invalid template configuration
		}

		// Create template range - this is the boundary for regular hours
		// Example: [9:00-17:00] means only work within this range counts as regular hours
		Range<LocalTime> templateRange = TimeHelper.toRange(templateStartTime, templateEndTime);

		Duration totalRegularHours = Duration.ZERO;

		// Process each interval for the same day
		for (TimeLog timeLog : sameDayTimeLogs) {
			LocalTime workStart = TimeHelper.getEffectiveStartTime(timeLog);
			LocalTime workEnd = TimeHelper.getEffectiveEndTime(timeLog);

			// Skip intervals with invalid time data
			if (workStart == null || workEnd == null || !workStart.isBefore(workEnd)) {
				continue; // Skip this interval and process next one
			}

			// Create the work range for this interval
			// Example: Morning interval [9:00-12:00], Afternoon interval [13:00-18:00]
			Range<LocalTime> workRange = TimeHelper.toRange(workStart, workEnd);

			// Intersect with template range to get the regular hours portion
			// This clips the work range to only the part that falls within template hours
			if (workRange.isConnected(templateRange)) {
				Range<LocalTime> intersection = workRange.intersection(templateRange);
				if (!intersection.isEmpty()) {
					// Calculate duration, excluding breaks within this range
					// Example: [9:00-12:00] ∩ [9:00-17:00] = [9:00-12:00] = 3 hours
					Duration intervalRegularHours = Duration.between(intersection.lowerEndpoint(),
							intersection.upperEndpoint());

					// Subtract any break time that falls within this intersection
					// Example: If 15-min break at 10:30-10:45, subtract 15 min from 3h =
					// 2h 45m
					Duration breakTimeInRange = calculateBreakTimeWithinRangeForTimeLog(timeLog, intersection);
					intervalRegularHours = intervalRegularHours.minus(breakTimeInRange);

					// Only add positive durations (prevent negative regular hours)
					if (!intervalRegularHours.isNegative()) {
						totalRegularHours = totalRegularHours.plus(intervalRegularHours);
					}
				}
			}
		}

		return totalRegularHours;
	}

	/**
	 * Returns total break duration that falls within the given time range for one time
	 * log. Iterates over the time log's break intervals, intersects each with the given
	 * range, and sums the overlapping durations.
	 * @param timeLog the time log containing break intervals to check
	 * @param range the time range to constrain break intervals to
	 * @return the total break duration within the range, or ZERO if none
	 */
	private Duration calculateBreakTimeWithinRangeForTimeLog(TimeLog timeLog, Range<LocalTime> range) {
		// Guard clause: return zero if any required data is missing
		if (timeLog == null || range == null || timeLog.getBreakIntervals() == null
				|| timeLog.getBreakIntervals().isEmpty()) {
			return Duration.ZERO; // No breaks to process
		}

		Duration totalBreakTime = Duration.ZERO;

		// Process each break interval for this time log
		for (TimeLogBreakInterval breakInterval : timeLog.getBreakIntervals()) {
			LocalTime breakStart = breakInterval.getBreakStartTime();
			LocalTime breakEnd = breakInterval.getBreakEndTime();

			// Skip invalid break intervals (null times or end before start)
			if (breakStart == null || breakEnd == null || !breakStart.isBefore(breakEnd)) {
				continue; // Skip this break and process next one
			}

			// Create range for this break interval
			// Example: Break from 10:30 to 10:45 becomes Range[10:30-10:45]
			Range<LocalTime> breakRange = TimeHelper.toRange(breakStart, breakEnd);

			// Check if break range overlaps with the constraint range
			// isConnected() returns true if ranges touch or overlap
			if (breakRange.isConnected(range)) {
				// Calculate the intersection (overlapping portion)
				// Example: Break[10:30-10:45] ∩ WorkRange[9:00-12:00] = [10:30-10:45]
				Range<LocalTime> intersection = breakRange.intersection(range);
				if (!intersection.isEmpty()) {
					// Add the duration of the overlapping portion to total break time
					// Only count break time that falls within the constraint range
					totalBreakTime = totalBreakTime
						.plus(Duration.between(intersection.lowerEndpoint(), intersection.upperEndpoint()));
				}
			}
		}

		return totalBreakTime;
	}

	/**
	 * Sums the durations of all ranges in the given RangeSet.
	 * @param timeRanges the set of time ranges to sum
	 * @return the total duration of all ranges combined, or ZERO if empty/null
	 */
	private Duration calculateTotalDurationFromRanges(RangeSet<LocalTime> timeRanges) {
		if (timeRanges == null || timeRanges.isEmpty()) {
			return Duration.ZERO;
		}

		Duration totalDuration = Duration.ZERO;
		for (Range<LocalTime> range : timeRanges.asRanges()) {
			Duration rangeDuration = Duration.between(range.lowerEndpoint(), range.upperEndpoint());
			totalDuration = totalDuration.plus(rangeDuration);
		}

		return totalDuration;
	}

	/**
	 * Scans custom rules to find the smallest daily overtime threshold applicable to the
	 * current work day. Returns null if no DO rule exists.
	 * @param context the context holding the custom rules list and current time log date
	 * @return the lowest daily overtime threshold duration, or null if no applicable DO
	 * rule found
	 */
	private Duration findLowestApplicableDailyOvertimeThreshold(TimeRangeResolverContext context) {
		// Get the list of custom rules configured for this timesheet
		List<CustomRule> customRules = context.getInternalSortedCustomRules();
		if (customRules == null || customRules.isEmpty()) {
			return null; // No custom rules configured, no daily OT threshold exists
		}

		TimeLog currentTimeLog = context.getCurrentTimeLogBeingEvaluated();
		// Convert date to day of week (MONDAY=1, TUESDAY=2, etc.)
		WorkDay currentWorkDay = TimeHelper.getWorkDayFromLocalDate(currentTimeLog.getDate());

		Duration lowestThreshold = null;

		// Scan through all custom rules to find Daily OT rules
		for (CustomRule rule : customRules) {
			// Check if this is a daily overtime rule type (range-based or duration-based)
			// Both types can have daily thresholds that we need to respect
			// Also check if rule is applicable on this work day
			// Example: Rule might only apply Mon-Fri, skip if today is Saturday
			if ((rule.getRuleType() == RuleType.RANGE_BASED_DAILY_OVERTIME
					|| rule.getRuleType() == RuleType.DURATION_BASED_DAILY_OVERTIME)
					&& rule.isApplicableOnDay(currentWorkDay)) {
				Duration threshold = rule.getDailyThreshold();
				// Track the lowest threshold found
				// Example: If Rule1=8h and Rule2=10h, use 8h (more restrictive)
				// This ensures regular hours don't exceed the most restrictive
				// threshold
				if (threshold != null && (lowestThreshold == null || threshold.compareTo(lowestThreshold) < 0)) {
					lowestThreshold = threshold;
				}
			}
		}

		return lowestThreshold;
	}

	/**
	 * Returns true if this time log is the chronologically last interval of its day.
	 * @param currentTimeLog the time log to check
	 * @param sameDayTimeLogs all time logs for the same day
	 * @return true if currentTimeLog has the latest end time among same-day logs
	 */
	private boolean isLastIntervalOfDay(TimeLog currentTimeLog, List<TimeLog> sameDayTimeLogs) {
		return TimeHelper.isLastIntervalOfDay(currentTimeLog, sameDayTimeLogs);
	}

	/**
	 * Caps the break threshold adjustment so that regular hours are never reduced below
	 * (template hours - break threshold). If the employee's actual work within regular
	 * hours is already at or below expected hours, no adjustment is needed. If actual
	 * work exceeds expected hours, the adjustment is capped to the excess amount.
	 * @param rawAdjustment the raw break threshold adjustment
	 * @param context the resolver context
	 * @param regularHoursRange the template regular hours range
	 * @param availableTimeRanges the available time ranges (template minus occupied)
	 * @return the capped adjustment duration
	 */
	private Duration capBreakThresholdAdjustment(Duration rawAdjustment, TimeRangeResolverContext context,
			Range<LocalTime> regularHoursRange, RangeSet<LocalTime> availableTimeRanges) {
		if (rawAdjustment == null || rawAdjustment.isZero()) {
			return Duration.ZERO;
		}

		Duration breakTimeThreshold = context.getCurrentTimesheetSetting().getBreakTimeThreshold();
		if (breakTimeThreshold == null || breakTimeThreshold.isZero()) {
			return rawAdjustment;
		}

		Duration templateDuration = Duration.between(regularHoursRange.lowerEndpoint(),
				regularHoursRange.upperEndpoint());
		Duration expectedMaxRegularHours = templateDuration.minus(breakTimeThreshold);

		if (expectedMaxRegularHours.isNegative() || expectedMaxRegularHours.isZero()) {
			return rawAdjustment;
		}

		List<TimeLog> sameDayTimeLogs = context.getSameDayTimeLogs();
		if (sameDayTimeLogs == null || sameDayTimeLogs.isEmpty()) {
			return rawAdjustment;
		}

		Duration actualWorkInRegular = calculateCumulativeRegularHours(sameDayTimeLogs, context);

		Duration excessWork = actualWorkInRegular.minus(expectedMaxRegularHours);

		if (excessWork.isNegative() || excessWork.isZero()) {
			this.logger.logDebug(String.format(
					"Break threshold adjustment capped to ZERO: actualWork=%s <= expectedMax=%s (template=%s, threshold=%s)",
					actualWorkInRegular, expectedMaxRegularHours, templateDuration, breakTimeThreshold));
			return Duration.ZERO;
		}

		// The trim is applied to availableTimeRanges (full template minus occupied) via
		// adjustTimeRangesFromEnd, but BaseRuleEvaluator later constrains the result to
		// the interval boundaries. If the interval ends before the template end, the
		// trim is "wasted" on available time beyond the interval. Account for this so
		// the trim is large enough to be visible after constraining.
		excessWork = addAvailableTimeBeyondInterval(excessWork, context, regularHoursRange, availableTimeRanges);

		Duration cappedAdjustment = (excessWork.compareTo(rawAdjustment) < 0) ? excessWork : rawAdjustment;

		if (!cappedAdjustment.equals(rawAdjustment)) {
			this.logger.logDebug(
					String.format("Break threshold adjustment capped: raw=%s, capped=%s, actualWork=%s, expectedMax=%s",
							rawAdjustment, cappedAdjustment, actualWorkInRegular, expectedMaxRegularHours));
		}

		return cappedAdjustment;
	}

	/**
	 * Calculates the available time in the template range that lies beyond the current
	 * interval's end. This time will absorb part of the trim without producing any
	 * visible effect (since BaseRuleEvaluator constrains to the interval), so the excess
	 * must be increased by this amount.
	 * @param excessWork the excess work that needs to be trimmed
	 * @param context the resolver context
	 * @param regularHoursRange the template regular hours range
	 * @param availableTimeRanges the available time ranges
	 * @return the excess increased by the available time beyond the interval
	 */
	private Duration addAvailableTimeBeyondInterval(Duration excessWork, TimeRangeResolverContext context,
			Range<LocalTime> regularHoursRange, RangeSet<LocalTime> availableTimeRanges) {
		TimeLog currentTimeLog = context.getCurrentTimeLogBeingEvaluated();
		LocalTime intervalEnd = TimeHelper.getEffectiveEndTime(currentTimeLog);
		LocalTime templateEnd = regularHoursRange.upperEndpoint();

		if (intervalEnd == null || !intervalEnd.isBefore(templateEnd)) {
			return excessWork;
		}

		Range<LocalTime> beyondRange = TimeHelper.toRange(intervalEnd, templateEnd);
		Duration availableBeyondInterval = Duration.ZERO;
		for (Range<LocalTime> range : availableTimeRanges.asRanges()) {
			if (range.isConnected(beyondRange)) {
				Range<LocalTime> intersection = range.intersection(beyondRange);
				if (!intersection.isEmpty()) {
					availableBeyondInterval = availableBeyondInterval
						.plus(Duration.between(intersection.lowerEndpoint(), intersection.upperEndpoint()));
				}
			}
		}

		if (!availableBeyondInterval.isZero()) {
			this.logger.logDebug(
					String.format("Available time beyond interval end (%s): %s", intervalEnd, availableBeyondInterval));
		}

		return excessWork.plus(availableBeyondInterval);
	}

	/**
	 * Calculates how much to deduct from regular hours due to the break threshold being
	 * larger than actual breaks. Uses ALL same-day intervals' breaks so the threshold is
	 * applied once per day, not per interval.
	 * @param timeLog the current time log being evaluated
	 * @param sameDayTimeLogs all time logs for the same day (for aggregating breaks)
	 * @param timesheetSetting the settings containing the break time threshold
	 * @param regularHoursRange the template regular hours range to constrain breaks to
	 * @return the duration to deduct from regular hours end, or ZERO if no adjustment
	 * needed
	 */
	private Duration calculateBreakTimeThresholdAdjustmentWithRangeConstraint(TimeLog timeLog,
			List<TimeLog> sameDayTimeLogs, TimesheetSetting timesheetSetting, Range<LocalTime> regularHoursRange) {
		if (timeLog == null || timesheetSetting == null || regularHoursRange == null) {
			return Duration.ZERO;
		}

		Duration breakTimeThreshold = timesheetSetting.getBreakTimeThreshold();

		if (breakTimeThreshold == null || breakTimeThreshold.isZero()) {
			return Duration.ZERO;
		}

		// Aggregate break time within regular hours across ALL same-day intervals
		// so the threshold is applied per-day, not per-interval
		Duration breakTimeWithinRegularHours = calculateAggregatedBreakTimeWithinRange(timeLog, sameDayTimeLogs,
				regularHoursRange);

		Duration adjustedThreshold;
		if (breakTimeWithinRegularHours == null || breakTimeWithinRegularHours.isZero()) {
			adjustedThreshold = breakTimeThreshold;
		}
		else {
			if (breakTimeThreshold.compareTo(breakTimeWithinRegularHours) <= 0) {
				adjustedThreshold = Duration.ZERO;
			}
			else {
				adjustedThreshold = breakTimeThreshold.minus(breakTimeWithinRegularHours);
			}
		}

		return adjustedThreshold;
	}

	/**
	 * Calculates aggregated break time within the given range across all same-day time
	 * logs. Falls back to the current time log's breaks if sameDayTimeLogs is
	 * unavailable.
	 * @param currentTimeLog the current time log being evaluated
	 * @param sameDayTimeLogs all time logs for the same day
	 * @param regularHoursRange the range to constrain breaks to
	 * @return total break duration within the range across all same-day intervals
	 */
	private Duration calculateAggregatedBreakTimeWithinRange(TimeLog currentTimeLog, List<TimeLog> sameDayTimeLogs,
			Range<LocalTime> regularHoursRange) {
		// If sameDayTimeLogs is available, aggregate breaks from all intervals
		if (sameDayTimeLogs != null && !sameDayTimeLogs.isEmpty()) {
			Duration totalBreak = Duration.ZERO;
			for (TimeLog sameDayTimeLog : sameDayTimeLogs) {
				Duration breakForInterval = calculateBreakTimeWithinRange(sameDayTimeLog, regularHoursRange);
				totalBreak = totalBreak.plus(breakForInterval);
			}
			return totalBreak;
		}

		// Fallback: use only the current time log's breaks
		if (currentTimeLog.getBreakIntervals() != null && !currentTimeLog.getBreakIntervals().isEmpty()) {
			return calculateBreakTimeWithinRange(currentTimeLog, regularHoursRange);
		}
		return Duration.ZERO;
	}

	/**
	 * Returns total break duration that overlaps with the given time range (constrains
	 * break intervals to range boundaries).
	 * @param timeLog the time log containing break intervals
	 * @param timeRange the time range to constrain break intervals to
	 * @return the total break duration within the range, or ZERO if none
	 */
	private Duration calculateBreakTimeWithinRange(TimeLog timeLog, Range<LocalTime> timeRange) {
		// Guard clause: return zero if any required data is missing
		if (timeLog == null || timeRange == null || timeLog.getBreakIntervals() == null) {
			return Duration.ZERO;
		}

		Duration totalBreakTimeWithinRange = Duration.ZERO;

		// Process each break interval for this time log
		for (TimeLogBreakInterval interval : timeLog.getBreakIntervals()) {
			LocalTime breakStartTime = interval.getBreakStartTime();
			LocalTime breakEndTime = interval.getBreakEndTime();

			// Validate break interval times (skip invalid breaks)
			if (breakStartTime == null || breakEndTime == null || !breakStartTime.isBefore(breakEndTime)) {
				continue; // Skip this break and process next one
			}

			// Create range for this break interval
			// Example: Break 10:30-10:45 becomes Range[10:30-10:45]
			Range<LocalTime> breakRange = TimeHelper.toRange(breakStartTime, breakEndTime);

			// Check if the break range intersects with the constraint time range
			// isConnected() returns true if ranges touch or overlap
			if (breakRange.isConnected(timeRange)) {
				// Calculate intersection (overlapping portion only)
				// Example: Break[10:30-10:45] ∩ RegularHours[9:00-17:00] = [10:30-10:45]
				Range<LocalTime> intersection = breakRange.intersection(timeRange);
				if (!intersection.isEmpty()) {
					// Calculate the duration of the intersection
					// Only count break time that falls within the constraint range
					Duration intersectionDuration = Duration.between(intersection.lowerEndpoint(),
							intersection.upperEndpoint());
					totalBreakTimeWithinRange = totalBreakTimeWithinRange.plus(intersectionDuration);
				}
			}
		}

		return totalBreakTimeWithinRange;
	}

	/**
	 * Trims the given time ranges from the end by the specified duration. Processes
	 * ranges in reverse chronological order so the deduction starts from the latest time
	 * range.
	 * @param availableTimeRanges the original time ranges to trim
	 * @param adjustment the duration to remove from the end of the ranges
	 * @return a new RangeSet with the adjustment deducted, or original if adjustment is
	 * zero
	 */
	private RangeSet<LocalTime> adjustTimeRangesFromEnd(RangeSet<LocalTime> availableTimeRanges, Duration adjustment) {
		if (availableTimeRanges == null || availableTimeRanges.isEmpty() || adjustment.isZero()) {
			return availableTimeRanges;
		}

		// Convert to list for reverse iteration
		List<Range<LocalTime>> rangeList = new java.util.ArrayList<>(availableTimeRanges.asRanges());
		RangeSet<LocalTime> adjustedRanges = TreeRangeSet.create();
		Duration remainingAdjustment = adjustment;

		// Process ranges in reverse order so we trim from the LAST (latest) range first
		for (int i = rangeList.size() - 1; i >= 0; i--) {
			Range<LocalTime> range = rangeList.get(i);

			if (remainingAdjustment.isZero()) {
				adjustedRanges.add(range);
				continue;
			}

			LocalTime rangeStart = range.lowerEndpoint();
			LocalTime rangeEnd = range.upperEndpoint();
			Duration rangeDuration = Duration.between(rangeStart, rangeEnd);

			if (rangeDuration.compareTo(remainingAdjustment) <= 0) {
				remainingAdjustment = remainingAdjustment.minus(rangeDuration);
			}
			else {
				LocalTime adjustedEnd = rangeEnd.minus(remainingAdjustment);
				if (adjustedEnd.isAfter(rangeStart)) {
					adjustedRanges.add(TimeHelper.toRange(rangeStart, adjustedEnd));
				}
				remainingAdjustment = Duration.ZERO;
			}
		}

		return adjustedRanges;
	}

}
