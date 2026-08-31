/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Duration-based regular hours range rule time range resolver.
 *
 * This resolver handles regular hours rules for duration-based time logs. It uses the
 * time shifting approach where the work period is shifted forward by the break duration
 * to maintain consistency with the midnight anchor system and break allocation at the
 * start.
 */
public class DurationBasedRegularHoursRangeRuleTimeRangeResolver implements ICustomRuleTimeRangeResolver {

	private final Logger logger;

	public DurationBasedRegularHoursRangeRuleTimeRangeResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
		if (timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getDayType() == WorkDayType.DAY_OFF) {
			return TreeRangeSet.create();
		}

		LocalDate timeLogDate = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getDate();
		WorkDay currentTimeLogWorkDay = TimeHelper.getWorkDayFromLocalDate(timeLogDate);
		TemplateWorkDay currentTimeLogTemplateWorkDay = TimeHelper.getTemplateWorkDayFromDayType(
				timeRangeResolverContext.getCurrentTimesheetSetting().getTemplateWorkDays(), currentTimeLogWorkDay);

		if (currentTimeLogTemplateWorkDay == null) {
			this.logger.logWarn("No template work day found for date: " + timeLogDate);
			return TreeRangeSet.create();
		}

		// Get the total work time from template
		Duration totalWorkTime = currentTimeLogTemplateWorkDay.getWorkTime();
		if (totalWorkTime == null || totalWorkTime.isZero()) {
			this.logger.logWarn("No work time configured for template work day");
			return TreeRangeSet.create();
		}

		// Get break duration to calculate effective work time
		Duration breakDuration = timeRangeResolverContext.getCurrentTimeLogBeingEvaluated().getBreakTime();
		Duration effectiveWorkTime = TimeHelper.calculateEffectiveWorkDuration(totalWorkTime, breakDuration);

		if (effectiveWorkTime.isZero()) {
			this.logger.logWarn("No effective work time available after break allocation");
			return TreeRangeSet.create();
		}

		// Apply break time threshold adjustment if needed
		// If breakTimeThreshold < totalBreakTime, deduct the difference from the end
		Duration breakTimeThresholdAdjustment = TimeHelper.calculateBreakTimeThresholdAdjustment(
				timeRangeResolverContext.getCurrentTimeLogBeingEvaluated(),
				timeRangeResolverContext.getCurrentTimesheetSetting());

		// Set the adjusted break threshold in context for DO resolvers to use
		timeRangeResolverContext.setAdjustedRegularHoursBreakThreshold(breakTimeThresholdAdjustment);

		if (!breakTimeThresholdAdjustment.isZero()) {
			// Adjust the effective work time by reducing it by the threshold adjustment
			Duration adjustedEffectiveWorkTime = effectiveWorkTime.minus(breakTimeThresholdAdjustment);

			// Ensure the adjusted work time is still positive
			if (adjustedEffectiveWorkTime.isPositive()) {
				effectiveWorkTime = adjustedEffectiveWorkTime;
				this.logger.logDebug(String.format(
						"Applied break time threshold adjustment - Original effective work time: %s, Adjustment: %s, New effective work time: %s",
						effectiveWorkTime.plus(breakTimeThresholdAdjustment), breakTimeThresholdAdjustment,
						effectiveWorkTime));
			}
			else {
				this.logger.logWarn(String.format(
						"Break time threshold adjustment would result in zero or negative work time - Original: %s, Adjustment: %s",
						effectiveWorkTime, breakTimeThresholdAdjustment));
				return TreeRangeSet.create(); // Return empty range if adjustment would
												// make work time invalid
			}
		}

		// Create the regular hours range starting from break duration (shifted by break)
		Range<LocalTime> regularHoursRange = TimeHelper.shiftRangeByBreak(Duration.ZERO, effectiveWorkTime,
				breakDuration);

		if (regularHoursRange.equals(Range.all())) {
			this.logger.logWarn("Failed to create regular hours range");
			return TreeRangeSet.create();
		}

		// Get available time ranges, respecting already occupied time ranges
		RangeSet<LocalTime> result = TimeHelper.getAvailableTimeRanges(regularHoursRange,
				timeRangeResolverContext.getOccupiedTimeRanges());

		this.logger.logDebug("Regular hours range: total work=" + totalWorkTime + ", break=" + breakDuration
				+ ", effective=" + effectiveWorkTime + ", range=" + regularHoursRange);

		return result;
	}

}
