/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import java.time.Duration;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VirtualTimeRangeAllocator Tests")
class VirtualTimeRangeAllocatorTests {

	@Mock
	private Logger logger;

	@InjectMocks
	private VirtualTimeRangeAllocator virtualTimeRangeAllocator;

	private static TimeLog rangeBasedTimeLog(LocalTime start, LocalTime end) {
		// workTime null => range-based, so effective start/end use
		// workStartTime/workEndTime
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(start);
		timeLog.setWorkEndTime(end);
		return timeLog;
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should return empty set when duration is null")
	void testAllocateVirtualTimeRangesNullDurationReturnsEmpty() {
		// Given
		TimeLog timeLog = rangeBasedTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		// When
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(null, timeLog,
				occupiedRanges);

		// Then
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should return empty set when duration is zero")
	void testAllocateVirtualTimeRangesZeroDurationReturnsEmpty() {
		// Given
		TimeLog timeLog = rangeBasedTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		// When
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(Duration.ZERO, timeLog,
				occupiedRanges);

		// Then
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should return empty set when work day range is invalid")
	void testAllocateVirtualTimeRangesInvalidWorkDayRangeReturnsEmpty() {
		// Given - start equals end so the work day range is invalid (not before)
		TimeLog timeLog = rangeBasedTimeLog(LocalTime.of(9, 0), LocalTime.of(9, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		// When
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(Duration.ofHours(1),
				timeLog, occupiedRanges);

		// Then
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should return empty set when start or end time is null")
	void testAllocateVirtualTimeRangesNullTimesReturnsEmpty() {
		// Given - null work times so effective times are null
		TimeLog timeLog = rangeBasedTimeLog(null, null);
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		// When
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(Duration.ofHours(1),
				timeLog, occupiedRanges);

		// Then
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should allocate the full duration from an available range")
	void testAllocateVirtualTimeRangesAllocatesFullDuration() {
		// Given - 8 hour work day, no occupied ranges, allocate 2 hours
		TimeLog timeLog = rangeBasedTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		// When
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(Duration.ofHours(2),
				timeLog, occupiedRanges);

		// Then
		assertThat(result.isEmpty()).isFalse();
		assertThat(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)).encloses(result.span())).isTrue();
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should stop allocating once remaining duration is zero")
	void testAllocateVirtualTimeRangesStopsWhenRemainingIsZero() {
		// Given two available slots split by an occupied middle range, where the
		// requested duration fits entirely within the first slot so the loop must break
		// before consuming the second slot
		TimeLog timeLog = rangeBasedTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(11, 0), LocalTime.of(12, 0)));

		// When - allocate exactly the first slot (09:00-11:00 = 2 hours)
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(Duration.ofHours(2),
				timeLog, occupiedRanges);

		// Then - only the first slot should be allocated, second slot untouched
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.span().lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(result.span().upperEndpoint()).isEqualTo(LocalTime.of(11, 0));
	}

	@Test
	@DisplayName("allocateVirtualTimeRanges should warn when duration cannot be fully allocated")
	void testAllocateVirtualTimeRangesCannotAllocateFullDurationLogsWarning() {
		// Given - work day is only 2 hours but we request 5 hours
		TimeLog timeLog = rangeBasedTimeLog(LocalTime.of(9, 0), LocalTime.of(11, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		// When
		RangeSet<LocalTime> result = this.virtualTimeRangeAllocator.allocateVirtualTimeRanges(Duration.ofHours(5),
				timeLog, occupiedRanges);

		// Then - the available 2 hours are allocated and a warning is logged
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.span().lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(result.span().upperEndpoint()).isEqualTo(LocalTime.of(11, 0));
	}

}
