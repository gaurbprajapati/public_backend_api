/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for allocating virtual time ranges based on effective work hours.
 *
 * This class provides methods to map effective work hours to actual time ranges within
 * the work day, respecting occupied time slots and break time configurations.
 */
public class VirtualTimeRangeAllocator {

	private final Logger logger;

	public VirtualTimeRangeAllocator(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Allocates virtual time ranges for the specified duration within the work day. This
	 * method creates time ranges that represent the allocated hours without conflicting
	 * with already occupied time slots.
	 * @param durationToAllocate the duration to allocate as virtual time ranges
	 * @param timeLog the time log being evaluated
	 * @param occupiedRanges the already occupied time ranges
	 * @return the allocated virtual time ranges
	 */
	public RangeSet<LocalTime> allocateVirtualTimeRanges(Duration durationToAllocate, TimeLog timeLog,
			RangeSet<LocalTime> occupiedRanges) {

		if (durationToAllocate == null || durationToAllocate.isZero()) {
			return TreeRangeSet.create();
		}

		// Get the work day time range
		Range<LocalTime> workDayRange = getWorkDayTimeRange(timeLog);
		if (workDayRange == null || workDayRange.isEmpty()) {
			this.logger.logWarn("Cannot allocate virtual time ranges: invalid work day range");
			return TreeRangeSet.create();
		}

		// Get available time slots within the work day
		RangeSet<LocalTime> availableRanges = TimeHelper.getAvailableTimeRanges(workDayRange, occupiedRanges);

		// Allocate the duration from available ranges
		RangeSet<LocalTime> allocatedRanges = allocateFromAvailableRanges(durationToAllocate, availableRanges);

		// Add allocated ranges to occupied ranges to prevent double allocation
		occupiedRanges.addAll(allocatedRanges);

		this.logger.logDebug("Allocated " + TimeHelper.convertDurationToApproximateHours(durationToAllocate)
				+ " hours as virtual time ranges: " + allocatedRanges);

		return allocatedRanges;
	}

	/**
	 * Gets the work day time range based on the time log.
	 * @param timeLog the time log being evaluated
	 * @return the work day time range, or null if invalid
	 */
	private Range<LocalTime> getWorkDayTimeRange(TimeLog timeLog) {
		LocalTime effectiveStartTime = TimeHelper.getEffectiveStartTime(timeLog);
		LocalTime effectiveEndTime = TimeHelper.getEffectiveEndTime(timeLog);

		if (effectiveStartTime == null || effectiveEndTime == null || !effectiveStartTime.isBefore(effectiveEndTime)) {
			return null;
		}

		return Range.closedOpen(effectiveStartTime, effectiveEndTime);
	}

	/**
	 * Allocates the specified duration from available time ranges. This method tries to
	 * allocate from the end of the work day first (typical for overtime).
	 * @param durationToAllocate the duration to allocate
	 * @param availableRanges the available time ranges
	 * @return the allocated time ranges
	 */
	private RangeSet<LocalTime> allocateFromAvailableRanges(Duration durationToAllocate,
			RangeSet<LocalTime> availableRanges) {

		RangeSet<LocalTime> allocatedRanges = TreeRangeSet.create();
		Duration remainingToAllocate = durationToAllocate;

		// Get available ranges for allocation
		List<Range<LocalTime>> availableRangesList = new ArrayList<>(availableRanges.asRanges());

		for (Range<LocalTime> range : availableRangesList) {
			if (remainingToAllocate.isZero()) {
				break;
			}

			LocalTime rangeStart = range.lowerEndpoint();
			LocalTime rangeEnd = range.upperEndpoint();
			Duration rangeDuration = Duration.between(rangeStart, rangeEnd);

			if (rangeDuration.compareTo(remainingToAllocate) <= 0) {
				// Allocate the entire range
				allocatedRanges.add(range);
				remainingToAllocate = remainingToAllocate.minus(rangeDuration);
			}
			else {
				// Allocate partial range from the start
				LocalTime partialEnd = rangeStart.plus(remainingToAllocate);
				Range<LocalTime> partialRange = Range.closedOpen(rangeStart, partialEnd);
				allocatedRanges.add(partialRange);
				remainingToAllocate = Duration.ZERO;
			}
		}

		if (!remainingToAllocate.isZero()) {
			this.logger.logWarn("Could not allocate full duration "
					+ TimeHelper.convertDurationToApproximateHours(durationToAllocate) + " hours. Remaining: "
					+ TimeHelper.convertDurationToApproximateHours(remainingToAllocate) + " hours");
		}

		return allocatedRanges;
	}

}
