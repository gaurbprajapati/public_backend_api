/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TimeHelper {

	private TimeHelper() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	public static boolean isOverlapping(long startTime1, long endTime1, long startTime2, long endTime2) {
		return startTime1 < endTime2 && startTime2 < endTime1;
	}

	public static boolean isWithinRange(long time, long startTime, long endTime) {
		return time >= startTime && time <= endTime;
	}

	public static boolean isOverlapping(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2,
			LocalTime endTime2) {
		if (startTime1 == null || endTime1 == null || startTime2 == null || endTime2 == null) {
			return false;
		}
		return startTime1.isBefore(endTime2) && startTime2.isBefore(endTime1);
	}

	public static boolean isWithinRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
		if (time == null || startTime == null || endTime == null) {
			return false;
		}
		return !time.isBefore(startTime) && !time.isAfter(endTime);
	}

	public static boolean isBefore(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2, LocalTime endTime2) {
		if (startTime1 == null || endTime1 == null || startTime2 == null || endTime2 == null) {
			return false;
		}
		return startTime1.isBefore(startTime2) && endTime1.isBefore(startTime2);
	}

	public static boolean isBefore(long startTime1, long endTime1, long startTime2) {
		return startTime1 < startTime2 && endTime1 < startTime2;
	}

	public static boolean isAfter(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2, LocalTime endTime2) {
		if (startTime1 == null || endTime1 == null || startTime2 == null || endTime2 == null) {
			return false;
		}
		return startTime1.isAfter(endTime2) && endTime1.isAfter(endTime2);
	}

	public static boolean isAfter(long startTime1, long endTime1, long endTime2) {
		return startTime1 > endTime2 && endTime1 > endTime2;
	}

	public static Duration calculateDuration(LocalTime startTime, LocalTime endTime) {
		if (startTime == null || endTime == null) {
			return Duration.ZERO;
		}
		// Validate that startTime is before endTime
		if (!startTime.isBefore(endTime)) {
			return Duration.ZERO; // Return zero duration if startTime is not before
									// endTime
		}
		return Duration.between(startTime, endTime);
	}

	public static Duration calculateDuration(long startTime, long endTime) {
		// Validate that startTime is before endTime
		if (startTime >= endTime) {
			return Duration.ZERO; // Return zero duration if startTime is not before
									// endTime
		}
		return Duration.ofSeconds(endTime - startTime);
	}

	public static LocalTime addDuration(LocalTime time, Duration duration) {
		if (time == null || duration == null) {
			return LocalTime.MIDNIGHT;
		}
		return time.plus(duration);
	}

	public static LocalTime subtractDuration(LocalTime time, Duration duration) {
		if (time == null || duration == null) {
			return LocalTime.MIDNIGHT;
		}
		return time.minus(duration);
	}

	public static long addDuration(long time, Duration duration) {
		return time + duration.toSeconds();
	}

	public static long subtractDuration(long time, Duration duration) {
		return time - duration.toSeconds();
	}

	public static TemplateWorkDay getTemplateWorkDayFromDayType(List<TemplateWorkDay> templateWorkDays,
			WorkDay workDay) {
		if (templateWorkDays == null || workDay == null) {
			return null;
		}
		for (TemplateWorkDay templateWorkDay : templateWorkDays) {
			if (templateWorkDay.getWorkDayType() == workDay) {
				return templateWorkDay;
			}
		}
		return null;
	}

	public static WorkDay getWorkDayFromLocalDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		int dayOfWeek = date.getDayOfWeek().getValue();
		return WorkDay.getWorkDayType(dayOfWeek);
	}

	/**
	 * Gets the WorkDay enum for a given date.
	 *
	 * This method is an alias for getWorkDayFromLocalDate for better readability when
	 * working with TimeLog dates.
	 * @param date the date to get the work day for
	 * @return the WorkDay enum for the given date
	 */
	public static WorkDay getWorkDayFromDate(LocalDate date) {
		return getWorkDayFromLocalDate(date);
	}

	public static boolean isWorkDay(WorkDayType workDayType) {
		return workDayType == WorkDayType.WORK_DAY;
	}

	public static Range<LocalTime> toRange(LocalTime startTime, LocalTime endTime) {
		if (startTime == null || endTime == null) {
			return Range.all();
		}
		// Validate that startTime is before endTime
		if (!startTime.isBefore(endTime)) {
			return Range.all(); // Return empty range if startTime is not before endTime
		}
		return Range.closedOpen(startTime, endTime);
	}

	public static Range<LocalTime> extendContiguously(RangeSet<LocalTime> occupied, RangeSet<LocalTime> workingDay,
			RangeSet<LocalTime> incomingPattern) {

		if (incomingPattern.isEmpty() || workingDay.isEmpty()) {
			return null;
		}

		/* ---------- 1. Where do we start appending? ---------- */
		LocalTime anchor = occupied.isEmpty() ? workingDay.span().lowerEndpoint() // very
																					// first
																					// insert
				: occupied.span().upperEndpoint(); // end of the only block

		LocalTime dayEnd = workingDay.span().upperEndpoint();
		if (!anchor.isBefore(dayEnd)) {
			return null;
		}

		/* ---------- 2. How many minutes does the pattern last? ---------- */
		long patternMinutes = incomingPattern.asRanges()
			.stream()
			.mapToLong((r) -> ChronoUnit.MINUTES.between(r.lowerEndpoint(), r.upperEndpoint()))
			.sum();

		/* ---------- 3. Can we fit it all? Clip if needed ---------- */
		long free = ChronoUnit.MINUTES.between(anchor, dayEnd);
		long slice = Math.min(patternMinutes, free);
		if (slice == 0) {
			return null;
		}

		/* ---------- 4. Grow / create the single block ---------- */
		Range<LocalTime> newBlock = Range.closedOpen(anchor, anchor.plusMinutes(slice));
		occupied.add(newBlock);
		return newBlock;
	}

	public static List<Range<LocalTime>> claimDOTime(Range<LocalTime> fullRange, RangeSet<LocalTime> occupiedRanges,
			Duration doQuota) {
		RangeSet<LocalTime> available = TreeRangeSet.create();
		available.add(fullRange);
		available.removeAll(occupiedRanges);

		List<Range<LocalTime>> claimed = new ArrayList<>();
		Duration totalClaimed = Duration.ZERO;

		for (Range<LocalTime> block : available.asRanges()) {
			LocalTime start = block.lowerEndpoint();
			LocalTime end = block.upperEndpoint();
			Duration blockDuration = Duration.between(start, end);
			Duration remaining = doQuota.minus(totalClaimed);

			if (blockDuration.compareTo(remaining) <= 0) {
				claimed.add(block);
				totalClaimed = totalClaimed.plus(blockDuration);
			}
			else {
				LocalTime partialEnd = start.plus(remaining);
				claimed.add(Range.closedOpen(start, partialEnd));
				break;
			}
		}

		// Now add the claimed DO slices to occupiedRanges
		for (Range<LocalTime> r : claimed) {
			occupiedRanges.add(r);
		}

		return claimed;
	}

	/**
	 * Claims daily overtime time from the end of the day instead of the beginning. This
	 * is more appropriate for daily overtime calculations as overtime typically occurs at
	 * the end of the work day.
	 * @param fullRange the full time range to claim from
	 * @param occupiedRanges the already occupied time ranges
	 * @param doQuota the amount of time to claim
	 * @return list of claimed time ranges
	 */
	public static List<Range<LocalTime>> claimDOTimeFromEnd(Range<LocalTime> fullRange,
			RangeSet<LocalTime> occupiedRanges, Duration doQuota) {
		RangeSet<LocalTime> available = TreeRangeSet.create();
		available.add(fullRange);
		available.removeAll(occupiedRanges);

		List<Range<LocalTime>> claimed = new ArrayList<>();
		Duration totalClaimed = Duration.ZERO;

		// Get available ranges in reverse order (from end to beginning)
		List<Range<LocalTime>> availableRanges = new ArrayList<>(available.asRanges());

		for (Range<LocalTime> block : availableRanges.reversed()) {
			LocalTime start = block.lowerEndpoint();
			LocalTime end = block.upperEndpoint();
			Duration blockDuration = Duration.between(start, end);
			Duration remaining = doQuota.minus(totalClaimed);

			if (blockDuration.compareTo(remaining) <= 0) {
				claimed.add(block);
				totalClaimed = totalClaimed.plus(blockDuration);
			}
			else {
				// Claim from the end of the block
				LocalTime partialStart = end.minus(remaining);
				claimed.add(Range.closedOpen(partialStart, end));
				break;
			}
		}

		// Now add the claimed DO slices to occupiedRanges
		for (Range<LocalTime> r : claimed) {
			occupiedRanges.add(r);
		}

		return claimed;
	}

	/**
	 * Calculates how many hours a DO rule can claim.
	 * @param workedHours Total hours the employee worked (e.g., 14)
	 * @param threshold Current DO threshold (e.g., 8 for DO 8+)
	 * @param nextThreshold Next threshold (e.g., 10 for DO 10+). Use Integer.MAX_VALUE if
	 * there's no next threshold.
	 * @return The number of hours this DO rule can claim.
	 * @deprecated Use {@link #calculateDOHours(Duration, Duration, Duration)} instead
	 */
	@Deprecated(since = "1.28.0", forRemoval = false)
	public static long calculateDOHours(long workedHours, long threshold, long nextThreshold) {
		if (workedHours <= threshold) {
			return 0; // Employee hasn't worked enough for this threshold
		}

		long maxThisTier = nextThreshold - threshold;
		long available = workedHours - threshold;

		return Math.min(maxThisTier, available);
	}

	/**
	 * Calculates how many hours a DO rule can claim using Duration objects.
	 * @param workedDuration Total duration the employee worked
	 * @param currentThreshold Current DO threshold duration
	 * @param nextThreshold Next threshold duration. Use Duration.ofHours(24) if there's
	 * no next threshold.
	 * @return The duration this DO rule can claim.
	 */
	public static Duration calculateDOHours(Duration workedDuration, Duration currentThreshold,
			Duration nextThreshold) {
		if (workedDuration == null || currentThreshold == null || nextThreshold == null) {
			return Duration.ZERO;
		}

		if (workedDuration.compareTo(currentThreshold) <= 0) {
			return Duration.ZERO; // Employee hasn't worked enough for this threshold
		}

		Duration maxThisTier = nextThreshold.minus(currentThreshold);
		Duration available = workedDuration.minus(currentThreshold);

		return (workedDuration.minus(currentThreshold).compareTo(maxThisTier) <= 0) ? available : maxThisTier;
	}

	/**
	 * Calculates total free duration within a working range by subtracting occupied
	 * ranges.
	 * @param workingRange The full range of working hours (e.g. 09:00 to 17:00)
	 * @param occupiedRanges A RangeSet of occupied time blocks within that range
	 * @return Total free time as a Duration
	 */
	public static Duration calculateTotalFreeDuration(Range<LocalTime> workingRange,
			RangeSet<LocalTime> occupiedRanges) {
		RangeSet<LocalTime> free = getFreeTimeRanges(workingRange, occupiedRanges);

		Duration totalFree = Duration.ZERO;

		for (Range<LocalTime> range : free.asRanges()) {
			LocalTime start = range.lowerEndpoint();
			LocalTime end = range.upperEndpoint();
			totalFree = totalFree.plus(Duration.between(start, end));
		}

		return totalFree;
	}

	/**
	 * Returns a RangeSet of free time slots within the working range.
	 * @param workingRange The full range of working hours (e.g. 09:00 to 17:00)
	 * @param occupiedRanges A RangeSet of occupied time blocks
	 * @return A RangeSet of available (free) time blocks
	 */
	public static RangeSet<LocalTime> getFreeTimeRanges(Range<LocalTime> workingRange,
			RangeSet<LocalTime> occupiedRanges) {
		// Validate that working range is not null and not empty
		if (workingRange == null || workingRange.isEmpty()) {
			return TreeRangeSet.create();
		}

		RangeSet<LocalTime> free = TreeRangeSet.create();
		free.add(workingRange);
		free.removeAll(occupiedRanges);
		return free;
	}

	/**
	 * Returns available time ranges from a gross range while considering occupied ranges.
	 *
	 * This method is useful for time range resolvers that need to return only the
	 * unoccupied portions of their gross time ranges. If no occupied ranges exist, the
	 * full gross range is returned.
	 * @param grossRange the gross time range to get available portions from
	 * @param occupiedRanges the occupied time ranges to exclude (can be null or empty)
	 * @return a RangeSet containing only the available (unoccupied) portions of the gross
	 * range
	 */
	public static RangeSet<LocalTime> getAvailableTimeRanges(Range<LocalTime> grossRange,
			RangeSet<LocalTime> occupiedRanges) {
		// Validate that gross range is not null and not empty
		if (grossRange == null || grossRange.isEmpty()) {
			return TreeRangeSet.create();
		}

		// If there are no occupied ranges, return the full gross range
		if (occupiedRanges == null || occupiedRanges.isEmpty()) {
			RangeSet<LocalTime> result = TreeRangeSet.create();
			result.add(grossRange);
			return result;
		}

		// Otherwise, return the free portions of the gross range
		return getFreeTimeRanges(grossRange, occupiedRanges);
	}

	public static Range<LocalTime> normaliseDurationRangeToLocalTime(Duration start, Duration end) {
		if (start == null || end == null) {
			return Range.all();
		}
		// Validate that start duration is before end duration
		if (start.compareTo(end) >= 0) {
			return Range.all(); // Return empty range if start is not before end
		}

		LocalTime anchor = LocalTime.MIDNIGHT;
		LocalTime startTime = TimeHelper.addDuration(anchor, start);
		LocalTime endTime = TimeHelper.addDuration(anchor, end);
		return TimeHelper.toRange(startTime, endTime);
	}

	public static LocalTime normaliseDurationToLocalTime(Duration duration) {
		if (duration == null) {
			return LocalTime.MIDNIGHT;
		}
		LocalTime anchor = LocalTime.MIDNIGHT;
		return TimeHelper.addDuration(anchor, duration);
	}

	public static Float convertDurationToApproximateHours(Duration duration) {
		if (duration == null) {
			return (float) 0.0;
		}
		return (float) (duration.toMinutes() / 60.0);
	}

	public static Duration convertRangeSetToDuration(RangeSet<LocalTime> rangeSet) {
		if (rangeSet == null || rangeSet.isEmpty()) {
			return Duration.ZERO;
		}
		Duration totalDuration = Duration.ZERO;
		for (Range<LocalTime> range : rangeSet.asRanges()) {
			totalDuration = totalDuration.plus(Duration.between(range.lowerEndpoint(), range.upperEndpoint()));
		}
		return totalDuration;
	}

	public static List<List<TimeLog>> splitTimeLogsOnWeeklyBasis(List<TimeLog> timeLogs) {
		List<List<TimeLog>> weeklyTimeLogs = new ArrayList<>();
		Map<String, List<TimeLog>> weekMap = new LinkedHashMap<>();

		WeekFields weekFields = WeekFields.ISO; // ISO standard, week starts on Monday

		for (TimeLog timeLog : timeLogs) {
			LocalDate date = timeLog.getDate();
			int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
			int year = date.get(weekFields.weekBasedYear());

			String key = year + "-" + weekNumber;

			weekMap.computeIfAbsent(key, (k) -> new ArrayList<>()).add(timeLog);
		}

		weeklyTimeLogs.addAll(weekMap.values());

		return weeklyTimeLogs;
	}

	/**
	 * Splits time logs on weekly basis starting from the specified work day.
	 *
	 * This method allows customization of the week start day. For example, if you pass
	 * WorkDay.WEDNESDAY, weeks will start from Wednesday and end on Tuesday.
	 * @param timeLogs the list of time logs to split
	 * @param weekStartDay the day of the week to start weeks from (e.g.,
	 * WorkDay.WEDNESDAY)
	 * @return list of time log lists, each representing a week
	 * @throws IllegalArgumentException if timeLogs is null or weekStartDay is null
	 */
	public static List<List<TimeLog>> splitTimeLogsOnWeeklyBasis(List<TimeLog> timeLogs, WorkDay weekStartDay) {
		if (timeLogs == null) {
			throw new IllegalArgumentException("TimeLogs list cannot be null");
		}
		if (weekStartDay == null) {
			throw new IllegalArgumentException("Week start day cannot be null");
		}

		List<List<TimeLog>> weeklyTimeLogs = new ArrayList<>();
		Map<String, List<TimeLog>> weekMap = new LinkedHashMap<>();

		// Create custom WeekFields based on the specified start day
		// Java's DayOfWeek uses 1=Monday, 2=Tuesday, ..., 7=Sunday
		// We need to convert WorkDay to DayOfWeek
		DayOfWeek dayOfWeek = convertWorkDayToDayOfWeek(weekStartDay);
		WeekFields weekFields = WeekFields.of(dayOfWeek, 1); // 1 means minimum days in
																// first week

		for (TimeLog timeLog : timeLogs) {
			LocalDate date = timeLog.getDate();
			int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
			int year = date.get(weekFields.weekBasedYear());

			String key = year + "-" + weekNumber;

			weekMap.computeIfAbsent(key, (k) -> new ArrayList<>()).add(timeLog);
		}

		weeklyTimeLogs.addAll(weekMap.values());

		return weeklyTimeLogs;
	}

	/**
	 * Converts a WorkDay enum to Java's DayOfWeek enum.
	 * @param workDay the WorkDay to convert
	 * @return the corresponding DayOfWeek
	 */
	private static DayOfWeek convertWorkDayToDayOfWeek(WorkDay workDay) {
		return switch (workDay) {
			case MONDAY -> DayOfWeek.MONDAY;
			case TUESDAY -> DayOfWeek.TUESDAY;
			case WEDNESDAY -> DayOfWeek.WEDNESDAY;
			case THURSDAY -> DayOfWeek.THURSDAY;
			case FRIDAY -> DayOfWeek.FRIDAY;
			case SATURDAY -> DayOfWeek.SATURDAY;
			case SUNDAY -> DayOfWeek.SUNDAY;
		};
	}

	public static boolean isWeekDay(TimeLog timeLog) {
		if (timeLog == null || timeLog.getDayType() == null) {
			return false;
		}
		return timeLog.getDayType() == WorkDayType.WORK_DAY;
	}

	public static Duration getMinimumDuration(Duration duration1, Duration duration2) {
		if (duration1 == null && duration2 == null) {
			return Duration.ZERO;
		}
		else if (duration1 == null) {
			return duration2;
		}
		else if (duration2 == null) {
			return duration1;
		}
		return (duration1.compareTo(duration2) < 0) ? duration1 : duration2;
	}

	/**
	 * Determines if a time log is duration-based by checking if workTime is present.
	 * @param timeLog the time log to check
	 * @return true if the time log is duration-based, false otherwise
	 */
	public static boolean isDurationBasedTimeLog(TimeLog timeLog) {
		if (timeLog == null) {
			return false;
		}
		return timeLog.getWorkTime() != null;
	}

	/**
	 * Calculates the duration for a time log using the appropriate fields. For
	 * duration-based time logs, uses normalizedWorkStartTime and normalizedWorkEndTime.
	 * For range-based time logs, uses workStartTime and workEndTime.
	 * @param timeLog the time log to calculate duration for
	 * @return the calculated duration
	 */
	public static Duration calculateTimeLogDuration(TimeLog timeLog) {
		if (timeLog == null) {
			return Duration.ZERO;
		}

		// For duration-based time logs, use normalized fields
		if (isDurationBasedTimeLog(timeLog)) {
			LocalTime startTime = timeLog.getNormalizedWorkStartTime();
			LocalTime endTime = timeLog.getNormalizedWorkEndTime();

			if (startTime != null && endTime != null) {
				return calculateDuration(startTime, endTime);
			}

			// Fallback to workTime if normalized fields are not available
			if (timeLog.getWorkTime() != null) {
				return timeLog.getWorkTime();
			}
		}

		// For range-based time logs, use original fields
		LocalTime startTime = timeLog.getWorkStartTime();
		LocalTime endTime = timeLog.getWorkEndTime();

		if (startTime != null && endTime != null) {
			return calculateDuration(startTime, endTime);
		}

		return Duration.ZERO;
	}

	/**
	 * Gets the effective start time for a time log. For duration-based time logs, returns
	 * normalizedWorkStartTime. For range-based time logs, returns workStartTime.
	 * @param timeLog the time log to get start time for
	 * @return the effective start time
	 */
	public static LocalTime getEffectiveStartTime(TimeLog timeLog) {
		if (timeLog == null) {
			return null;
		}

		if (isDurationBasedTimeLog(timeLog)) {
			return timeLog.getNormalizedWorkStartTime();
		}

		return timeLog.getWorkStartTime();
	}

	/**
	 * Gets the effective end time for a time log. For duration-based time logs, returns
	 * normalizedWorkEndTime. For range-based time logs, returns workEndTime.
	 * @param timeLog the time log to get end time for
	 * @return the effective end time
	 */
	public static LocalTime getEffectiveEndTime(TimeLog timeLog) {
		if (timeLog == null) {
			return null;
		}

		if (isDurationBasedTimeLog(timeLog)) {
			return timeLog.getNormalizedWorkEndTime();
		}

		return timeLog.getWorkEndTime();
	}

	/**
	 * Checks if a time range falls within regular hours for a specific day.
	 *
	 * This method determines if the given time range overlaps with the regular working
	 * hours defined in the template work day configuration for the specified date.
	 * @param timeRange the time range to check
	 * @param timeLogDate the date to check regular hours for
	 * @param templateWorkDays the template work days configuration
	 * @return true if the time range falls within regular hours, false otherwise
	 */
	public static boolean isTimeRangeWithinRegularHours(RangeSet<LocalTime> timeRange, LocalDate timeLogDate,
			List<TemplateWorkDay> templateWorkDays) {

		if (timeRange == null || timeRange.isEmpty() || timeLogDate == null || templateWorkDays == null) {
			return false;
		}

		// Get the work day for the given date
		WorkDay workDay = getWorkDayFromLocalDate(timeLogDate);
		if (workDay == null) {
			return false;
		}

		// Get the template work day configuration for this day
		TemplateWorkDay templateWorkDay = getTemplateWorkDayFromDayType(templateWorkDays, workDay);

		// If no template work day exists for this day, it's not a work day
		if (templateWorkDay == null) {
			return false;
		}

		// For duration-based timesheets, regular hours are from midnight to workTime
		LocalTime regularStartTime = LocalTime.MIDNIGHT;
		LocalTime regularEndTime = normaliseDurationToLocalTime(templateWorkDay.getWorkTime());

		// Create the regular hours range
		Range<LocalTime> regularHoursRange = toRange(regularStartTime, regularEndTime);

		// Check if any part of the time range overlaps with regular hours
		for (Range<LocalTime> range : timeRange.asRanges()) {
			if (range.isConnected(regularHoursRange) && !range.intersection(regularHoursRange).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a specific date is configured as a work day in the template work days
	 * configuration.
	 *
	 * This method determines if the given date falls on a day that is configured as a
	 * work day in the timesheet settings template work days.
	 * @param date the date to check
	 * @param templateWorkDays the template work days configuration
	 * @return true if the date is a work day, false otherwise
	 */
	public static boolean isWorkDay(LocalDate date, List<TemplateWorkDay> templateWorkDays) {
		if (date == null || templateWorkDays == null) {
			return false;
		}

		// Get the work day for the given date
		WorkDay workDay = getWorkDayFromLocalDate(date);
		if (workDay == null) {
			return false;
		}

		// Check if a template work day exists for this day
		// The presence of a TemplateWorkDay entry indicates it's configured as a work day
		return getTemplateWorkDayFromDayType(templateWorkDays, workDay) != null;
	}

	/**
	 * Calculates the payable amount based on the charge method type.
	 *
	 * This method handles both MULTIPLIER and FIXED_RATE charge methods: - MULTIPLIER:
	 * Uses base pay rate from timesheet settings multiplied by the rule's pay rate
	 * multiplier - FIXED_RATE: Uses the rule's fixed pay rate per hour
	 * @param duration the duration to calculate for
	 * @param chargeMethod the charge method type
	 * @param basePayRate the base pay rate from timesheet settings (for MULTIPLIER
	 * method)
	 * @param payRateMultiplier the pay rate multiplier from the rule (for MULTIPLIER
	 * method)
	 * @param payRatePerHour the fixed pay rate per hour from the rule (for FIXED_RATE
	 * method)
	 * @return the calculated payable amount
	 */
	public static BigDecimal calculatePayAmount(Duration duration, ChargeMethodType chargeMethod, Float basePayRate,
			Float payRateMultiplier, Float payRatePerHour) {

		if (duration == null || duration.isZero()) {
			return BigDecimal.ZERO;
		}

		float hours = convertDurationToApproximateHours(duration);
		float rate = switch (chargeMethod) {
			case MULTIPLIER ->
				((basePayRate != null) ? basePayRate : 0.0f) * ((payRateMultiplier != null) ? payRateMultiplier : 1.0f);
			case FIXED_RATE -> (payRatePerHour != null) ? payRatePerHour : 0.0f;
			default ->
				((basePayRate != null) ? basePayRate : 0.0f) * ((payRateMultiplier != null) ? payRateMultiplier : 1.0f);
		};

		return BigDecimal.valueOf(hours * rate);
	}

	/**
	 * Calculates the billable amount based on the charge method type.
	 *
	 * This method handles both MULTIPLIER and FIXED_RATE charge methods: - MULTIPLIER:
	 * Uses base bill rate from timesheet settings multiplied by the rule's bill rate
	 * multiplier - FIXED_RATE: Uses the rule's fixed bill rate per hour
	 * @param duration the duration to calculate for
	 * @param chargeMethod the charge method type
	 * @param baseBillRate the base bill rate from timesheet settings (for MULTIPLIER
	 * method)
	 * @param billRateMultiplier the bill rate multiplier from the rule (for MULTIPLIER
	 * method)
	 * @param billRatePerHour the fixed bill rate per hour from the rule (for FIXED_RATE
	 * method)
	 * @return the calculated billable amount
	 */
	public static BigDecimal calculateBillAmount(Duration duration, ChargeMethodType chargeMethod, Float baseBillRate,
			Float billRateMultiplier, Float billRatePerHour) {

		if (duration == null || duration.isZero()) {
			return BigDecimal.ZERO;
		}

		float hours = convertDurationToApproximateHours(duration);
		float rate;

		final float rate1 = ((baseBillRate != null) ? baseBillRate : 0.0f)
				* ((billRateMultiplier != null) ? billRateMultiplier : 1.0f);
		if (chargeMethod == ChargeMethodType.FIXED_RATE) {
			// FIXED_RATE method: use fixed rate per hour
			rate = (billRatePerHour != null) ? billRatePerHour : 0.0f;
		}
		else {
			// Default to multiplier method (including MULTIPLIER case)
			rate = rate1;
		}

		return BigDecimal.valueOf(hours * rate);
	}

	/**
	 * Validates and constrains a time range to be within the time log boundaries. If the
	 * time range extends beyond the time log start/end times, it will be clipped to fit
	 * within those boundaries.
	 * @param timeRange the time range to validate and constrain
	 * @param timeLog the time log whose boundaries should be respected
	 * @return a constrained time range that fits within the time log boundaries, or empty
	 * range set if invalid
	 */
	public static RangeSet<LocalTime> constrainTimeRangeToTimeLogBoundaries(RangeSet<LocalTime> timeRange,
			TimeLog timeLog) {
		if (timeRange == null || timeRange.isEmpty() || timeLog == null) {
			return TreeRangeSet.create();
		}

		LocalTime timeLogStartTime = getEffectiveStartTime(timeLog);
		LocalTime timeLogEndTime = getEffectiveEndTime(timeLog);

		// Validate time log has valid start and end times
		if (timeLogStartTime == null || timeLogEndTime == null || !timeLogStartTime.isBefore(timeLogEndTime)) {
			return TreeRangeSet.create();
		}

		// Create the time log boundary range
		Range<LocalTime> timeLogBoundary = toRange(timeLogStartTime, timeLogEndTime);

		// If time log boundary is invalid, return empty range set
		if (timeLogBoundary.equals(Range.all())) {
			return TreeRangeSet.create();
		}

		// Constrain the time range to the time log boundary
		RangeSet<LocalTime> constrainedRange = TreeRangeSet.create();
		for (Range<LocalTime> range : timeRange.asRanges()) {
			// Check if the range intersects with the time log boundary
			if (range.isConnected(timeLogBoundary)) {
				Range<LocalTime> intersection = range.intersection(timeLogBoundary);
				if (!intersection.isEmpty()) {
					constrainedRange.add(intersection);
				}
			}
		}

		return constrainedRange;
	}

	/**
	 * Validates and constrains a single time range to be within the time log boundaries.
	 * If the time range extends beyond the time log start/end times, it will be clipped
	 * to fit within those boundaries.
	 * @param timeRange the time range to validate and constrain
	 * @param timeLog the time log whose boundaries should be respected
	 * @return a constrained time range that fits within the time log boundaries, or null
	 * if invalid
	 */
	public static Range<LocalTime> constrainSingleTimeRangeToTimeLogBoundaries(Range<LocalTime> timeRange,
			TimeLog timeLog) {
		if (timeRange == null || timeLog == null) {
			return null;
		}

		LocalTime timeLogStartTime = getEffectiveStartTime(timeLog);
		LocalTime timeLogEndTime = getEffectiveEndTime(timeLog);

		// Validate time log has valid start and end times
		if (timeLogStartTime == null || timeLogEndTime == null || !timeLogStartTime.isBefore(timeLogEndTime)) {
			return null;
		}

		// Create the time log boundary range
		Range<LocalTime> timeLogBoundary = toRange(timeLogStartTime, timeLogEndTime);

		// If time log boundary is invalid, return null
		if (timeLogBoundary.equals(Range.all())) {
			return null;
		}

		// Check if the range intersects with the time log boundary
		if (timeRange.isConnected(timeLogBoundary)) {
			Range<LocalTime> intersection = timeRange.intersection(timeLogBoundary);
			if (!intersection.isEmpty()) {
				return intersection;
			}
		}

		return null;
	}

	/**
	 * Determines whether break time should be included in overtime calculations based on
	 * the calculateBreakTime flag from timesheet settings.
	 * @param timesheetSetting the timesheet setting containing break time configuration
	 * @return true if break time should be included, false otherwise
	 */
	public static boolean shouldIncludeBreakTimeInCalculation(TimesheetSetting timesheetSetting) {
		if (timesheetSetting != null && timesheetSetting.getCalculateBreakTime() != null) {
			return timesheetSetting.getCalculateBreakTime();
		}

		// If flag is not set, return false (breaks should not be included by default)
		return false;
	}

	/**
	 * Calculates work time for a time log, respecting the calculateBreakTime flag.
	 * @param timeLog the time log to calculate work time for
	 * @param timesheetSetting the timesheet setting containing break time configuration
	 * @return the work time duration
	 */
	public static Duration calculateWorkTimeRespectingBreakFlag(TimeLog timeLog, TimesheetSetting timesheetSetting) {
		Duration totalTime = calculateTimeLogDuration(timeLog);
		Duration breakTime = timeLog.getBreakTime();

		// Check if break time should be included based on calculateBreakTime flag
		boolean shouldIncludeBreakTime = shouldIncludeBreakTimeInCalculation(timesheetSetting);

		if (breakTime != null && !breakTime.isZero() && !shouldIncludeBreakTime) {
			// If calculateBreakTime is false, exclude break time from overtime
			// calculation
			return totalTime.minus(breakTime);
		}

		// If calculateBreakTime is true or no break time, use total duration
		return totalTime;
	}

	/**
	 * Shifts a duration range forward by a specified offset for duration-based rules.
	 * This is used when break time is allocated at the start, and subsequent rules need
	 * to be shifted forward by the break duration.
	 * @param originalStart the original start duration
	 * @param originalEnd the original end duration
	 * @param shiftOffset the duration to shift forward by
	 * @return the shifted duration range as a LocalTime range
	 */
	public static Range<LocalTime> shiftDurationRangeForward(Duration originalStart, Duration originalEnd,
			Duration shiftOffset) {
		if (originalStart == null || originalEnd == null || shiftOffset == null) {
			return Range.all();
		}

		// Validate that start duration is before end duration
		if (originalStart.compareTo(originalEnd) >= 0) {
			return Range.all();
		}

		// Shift both start and end by the offset
		Duration shiftedStart = originalStart.plus(shiftOffset);
		Duration shiftedEnd = originalEnd.plus(shiftOffset);

		// Convert to LocalTime range using midnight anchor
		LocalTime anchor = LocalTime.MIDNIGHT;
		LocalTime startTime = addDuration(anchor, shiftedStart);
		LocalTime endTime = addDuration(anchor, shiftedEnd);

		return toRange(startTime, endTime);
	}

	/**
	 * Shifts a single duration forward by a specified offset for duration-based rules.
	 * @param originalDuration the original duration
	 * @param shiftOffset the duration to shift forward by
	 * @return the shifted duration as a LocalTime
	 */
	public static LocalTime shiftDurationForward(Duration originalDuration, Duration shiftOffset) {
		if (originalDuration == null || shiftOffset == null) {
			return LocalTime.MIDNIGHT;
		}

		Duration shiftedDuration = originalDuration.plus(shiftOffset);
		LocalTime anchor = LocalTime.MIDNIGHT;
		return addDuration(anchor, shiftedDuration);
	}

	/**
	 * Creates a break time range at the start of the work period for duration-based
	 * rules. This allocates break time from 00:00 to the break duration.
	 * @param breakDuration the break duration to allocate
	 * @return the break time range starting from midnight
	 */
	public static Range<LocalTime> createBreakRangeAtStart(Duration breakDuration) {
		if (breakDuration == null || breakDuration.isZero()) {
			return Range.all();
		}

		LocalTime startTime = LocalTime.MIDNIGHT;
		LocalTime endTime = addDuration(startTime, breakDuration);

		return toRange(startTime, endTime);
	}

	/**
	 * Calculates the effective work duration after removing break time for duration-based
	 * rules. This is the duration available for other rules after break is allocated at
	 * the start.
	 * @param totalWorkDuration the total work duration
	 * @param breakDuration the break duration
	 * @return the effective work duration available for other rules
	 */
	public static Duration calculateEffectiveWorkDuration(Duration totalWorkDuration, Duration breakDuration) {
		if (totalWorkDuration == null) {
			return Duration.ZERO;
		}

		if (breakDuration == null || breakDuration.isZero()) {
			return totalWorkDuration;
		}

		// Ensure we don't return negative duration
		if (breakDuration.compareTo(totalWorkDuration) >= 0) {
			return Duration.ZERO;
		}

		return totalWorkDuration.minus(breakDuration);
	}

	/**
	 * Shifts a duration range forward by the break duration for duration-based rules.
	 * This is a convenience wrapper for shiftDurationRangeForward, using breakDuration as
	 * the shift offset.
	 * @param start the original start duration
	 * @param end the original end duration
	 * @param breakDuration the break duration to shift by (may be null or zero)
	 * @return the shifted LocalTime range
	 */
	public static Range<LocalTime> shiftRangeByBreak(Duration start, Duration end, Duration breakDuration) {
		Duration shift = (breakDuration != null && !breakDuration.isZero()) ? breakDuration : Duration.ZERO;
		return shiftDurationRangeForward(start, end, shift);
	}

	/**
	 * Calculates the break time threshold adjustment for regular hours time range. If
	 * breakTimeThreshold is greater than total break time, the difference should be
	 * deducted from the regular hours time range from the end.
	 * @param timeLog the time log containing break time information
	 * @param timesheetSetting the timesheet setting containing break time threshold
	 * @return the duration to be deducted from regular hours (from the end), or zero if
	 * no adjustment needed
	 */
	public static Duration calculateBreakTimeThresholdAdjustment(TimeLog timeLog, TimesheetSetting timesheetSetting) {
		if (timeLog == null || timesheetSetting == null) {
			return Duration.ZERO;
		}

		Duration breakTimeThreshold = timesheetSetting.getBreakTimeThreshold();
		Duration totalBreakTime = timeLog.getBreakTime();

		// If no break time threshold is set, no adjustment needed
		if (breakTimeThreshold == null || breakTimeThreshold.isZero()) {
			return Duration.ZERO;
		}

		// If no break time in time log, deduct the entire break time threshold
		if (totalBreakTime == null || totalBreakTime.isZero()) {
			return breakTimeThreshold;
		}

		// If break time threshold is less than or equal to total break time, no
		// adjustment needed
		if (breakTimeThreshold.compareTo(totalBreakTime) <= 0) {
			return Duration.ZERO;
		}

		// Calculate the difference: breakTimeThreshold - totalBreakTime
		// This amount should be deducted from regular hours from the end
		return breakTimeThreshold.minus(totalBreakTime);
	}

	// ========== Multi-Interval Day Utilities ==========

	/**
	 * Checks if the given time log is the last interval for its day among the provided
	 * same-day time logs. Compares by work end time since expanded intervals from the
	 * same parent TimeLog share IDs.
	 * @param currentTimeLog the time log to check
	 * @param sameDayTimeLogs all time logs for the same day
	 * @return true if this is the last (or only) interval of the day
	 */
	public static boolean isLastIntervalOfDay(TimeLog currentTimeLog, List<TimeLog> sameDayTimeLogs) {
		if (sameDayTimeLogs == null || sameDayTimeLogs.size() <= 1) {
			return true;
		}

		LocalTime currentEnd = getEffectiveEndTime(currentTimeLog);
		if (currentEnd == null) {
			return false;
		}

		return sameDayTimeLogs.stream()
			.map(TimeHelper::getEffectiveEndTime)
			.filter(Objects::nonNull)
			.noneMatch((t) -> t.isAfter(currentEnd));
	}

	/**
	 * Creates a merged time log spanning the full day range (earliest start to latest
	 * end) of all same-day intervals. Returns the fallback if merging is not needed.
	 * @param fallbackTimeLog the time log to return if only one interval or merge fails
	 * @param sameDayTimeLogs all time logs for the same day
	 * @return a merged time log, or the fallback if only one interval
	 */
	public static TimeLog createMergedTimeLog(TimeLog fallbackTimeLog, List<TimeLog> sameDayTimeLogs) {
		if (sameDayTimeLogs == null || sameDayTimeLogs.size() <= 1) {
			return fallbackTimeLog;
		}

		LocalTime mergedStart = null;
		LocalTime mergedEnd = null;

		for (TimeLog tl : sameDayTimeLogs) {
			LocalTime start = getEffectiveStartTime(tl);
			LocalTime end = getEffectiveEndTime(tl);

			if (start != null && (mergedStart == null || start.isBefore(mergedStart))) {
				mergedStart = start;
			}
			if (end != null && (mergedEnd == null || end.isAfter(mergedEnd))) {
				mergedEnd = end;
			}
		}

		if (mergedStart == null || mergedEnd == null || !mergedStart.isBefore(mergedEnd)) {
			return fallbackTimeLog;
		}

		TimeLog merged = new TimeLog();
		merged.setWorkStartTime(mergedStart);
		merged.setWorkEndTime(mergedEnd);
		merged.setNormalizedWorkStartTime(mergedStart);
		merged.setNormalizedWorkEndTime(mergedEnd);
		return merged;
	}

	/**
	 * Filters time logs that share the same date as the given time log.
	 * @param timeLog the reference time log
	 * @param allTimeLogs all time logs to filter from
	 * @return list of time logs on the same date
	 */
	public static List<TimeLog> getSameDayTimeLogs(TimeLog timeLog, List<TimeLog> allTimeLogs) {
		if (allTimeLogs == null || timeLog == null || timeLog.getDate() == null) {
			return List.of();
		}
		return allTimeLogs.stream()
			.filter((tl) -> tl.getDate() != null && tl.getDate().equals(timeLog.getDate()))
			.toList();
	}

	/**
	 * Calculates the gap ranges between work intervals. Gaps are time ranges within the
	 * merged day span where no interval was logged (e.g., between 18:00-19:00 if
	 * intervals are 09:00-18:00 and 19:00-21:00). Returns empty if there is only one
	 * interval or no gaps exist.
	 * @param sameDayTimeLogs all time logs for the same day
	 * @return a RangeSet containing the gap ranges between intervals
	 */
	public static RangeSet<LocalTime> getGapsBetweenIntervals(List<TimeLog> sameDayTimeLogs) {
		RangeSet<LocalTime> gaps = TreeRangeSet.create();

		if (sameDayTimeLogs == null || sameDayTimeLogs.size() <= 1) {
			return gaps;
		}

		// Build the union of all actual work ranges
		RangeSet<LocalTime> workRanges = TreeRangeSet.create();
		LocalTime mergedStart = null;
		LocalTime mergedEnd = null;

		for (TimeLog tl : sameDayTimeLogs) {
			LocalTime start = getEffectiveStartTime(tl);
			LocalTime end = getEffectiveEndTime(tl);
			if (start != null && end != null && start.isBefore(end)) {
				workRanges.add(Range.closedOpen(start, end));
				if (mergedStart == null || start.isBefore(mergedStart)) {
					mergedStart = start;
				}
				if (mergedEnd == null || end.isAfter(mergedEnd)) {
					mergedEnd = end;
				}
			}
		}

		if (mergedStart == null) {
			return gaps;
		}

		// Gaps = full merged span minus actual work ranges
		gaps.add(Range.closedOpen(mergedStart, mergedEnd));
		gaps.removeAll(workRanges);

		return gaps;
	}

}
