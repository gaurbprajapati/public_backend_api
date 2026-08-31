/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver;

import com.google.common.collect.RangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeRangeResolverContext {

	private TimeLog currentTimeLogBeingEvaluated;

	private CustomRule currentCustomRuleBeingEvaluated;

	private List<CustomRule> internalSortedCustomRules;

	private TimesheetSetting currentTimesheetSetting;

	private RangeSet<LocalTime> occupiedTimeRanges;

	/**
	 * Tracks effective work hours accumulated during rule evaluation. This includes
	 * regular hours and any previously allocated DO hours. Used by hour-based DO
	 * resolvers to determine overtime eligibility.
	 */
	private Duration workedHoursTillNow;

	/**
	 * Tracks the total effective work hours for the day. This is calculated from the time
	 * log respecting break time flags.
	 */
	private Duration totalEffectiveWorkHours;

	/**
	 * Tracks the adjusted regular hours break threshold. This is calculated by the
	 * Regular Hours resolver and used by DO resolvers to determine effective working
	 * time.
	 */
	private Duration adjustedRegularHoursBreakThreshold;

	private Integer currentRuleIndex;

	/**
	 * List of all time logs for the same day as the current time log being evaluated.
	 * Used for aggregating work hours across multiple intervals when calculating daily
	 * overtime. For example, if a day has 2 intervals (9:00-11:00 and 11:00-17:00), daily
	 * overtime should be calculated based on the total 8 hours, not individually.
	 */
	private List<TimeLog> sameDayTimeLogs;

	/**
	 * Calculates effective working time using the formula: Total Time logged - break time
	 * (only when break is unpaid) - adjusted regular hours break threshold. When
	 * calculateBreakTime is true, break time counts towards the OT threshold.
	 * @param timeLog the time log to calculate effective work time for
	 * @return the effective working time duration
	 */
	public Duration calculateEffectiveWorkingTime(TimeLog timeLog) {
		if (timeLog == null) {
			return Duration.ZERO;
		}

		Duration totalTime = io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper
			.calculateTimeLogDuration(timeLog);

		boolean breakIsPaid = io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper
			.shouldIncludeBreakTimeInCalculation(this.currentTimesheetSetting);

		if (!breakIsPaid) {
			Duration breakTime = timeLog.getBreakTime();
			if (breakTime != null) {
				totalTime = totalTime.minus(breakTime);
			}
		}

		Duration adjustedThreshold = this.adjustedRegularHoursBreakThreshold;
		if (adjustedThreshold == null) {
			adjustedThreshold = Duration.ZERO;
		}

		Duration effectiveWorkingTime = totalTime.minus(adjustedThreshold);

		return effectiveWorkingTime.isNegative() ? Duration.ZERO : effectiveWorkingTime;
	}

	/**
	 * Calculates the aggregated effective working time across all same-day time logs.
	 * This method is used for daily overtime calculation when a day has multiple
	 * intervals. The overtime threshold should be applied to the total daily work, not
	 * individual intervals.
	 * @return the aggregated effective working time duration for the entire day
	 */
	public Duration calculateAggregatedDailyEffectiveWorkingTime() {
		if (this.sameDayTimeLogs == null || this.sameDayTimeLogs.isEmpty()) {
			// Fallback to current time log if no same-day time logs available
			return calculateEffectiveWorkingTime(this.currentTimeLogBeingEvaluated);
		}

		Duration totalEffectiveWork = Duration.ZERO;
		for (TimeLog timeLog : this.sameDayTimeLogs) {
			Duration effectiveWork = calculateSingleTimeLogEffectiveTime(timeLog);
			totalEffectiveWork = totalEffectiveWork.plus(effectiveWork);
		}

		return totalEffectiveWork;
	}

	/**
	 * Calculates effective working time for a single time log without the adjusted
	 * threshold. The adjusted threshold is applied once per day, not per interval. When
	 * calculateBreakTime is true, break time is NOT subtracted (it counts towards OT
	 * threshold).
	 * @param timeLog the time log to calculate effective work time for
	 * @return the effective working time duration
	 */
	private Duration calculateSingleTimeLogEffectiveTime(TimeLog timeLog) {
		if (timeLog == null) {
			return Duration.ZERO;
		}

		Duration totalTime = io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper
			.calculateTimeLogDuration(timeLog);

		boolean breakIsPaid = io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper
			.shouldIncludeBreakTimeInCalculation(this.currentTimesheetSetting);

		if (!breakIsPaid) {
			Duration breakTime = timeLog.getBreakTime();
			if (breakTime != null) {
				totalTime = totalTime.minus(breakTime);
			}
		}

		return totalTime.isNegative() ? Duration.ZERO : totalTime;
	}

	/**
	 * Gets the adjusted daily effective working time by subtracting the adjusted
	 * threshold from the aggregated daily effective time. The threshold is applied once
	 * per day.
	 * @return the adjusted effective working time for the entire day
	 */
	public Duration getAdjustedDailyEffectiveWorkingTime() {
		Duration aggregatedTime = calculateAggregatedDailyEffectiveWorkingTime();

		// Get adjusted regular hours break threshold (set by Regular Hours resolver)
		Duration adjustedThreshold = this.adjustedRegularHoursBreakThreshold;
		if (adjustedThreshold == null) {
			adjustedThreshold = Duration.ZERO;
		}

		// Subtract threshold once for the entire day
		Duration adjustedTime = aggregatedTime.minus(adjustedThreshold);

		return adjustedTime.isNegative() ? Duration.ZERO : adjustedTime;
	}

}
