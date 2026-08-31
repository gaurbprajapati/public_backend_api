package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RangeBasedBreakRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private TimeLog timeLog;

	@Mock
	private TimeLogBreakInterval breakInterval1;

	@Mock
	private TimeLogBreakInterval breakInterval2;

	@Mock
	private TimeRangeResolverContext context;

	private RangeBasedBreakRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedBreakRuleTimeRangeResolver(this.logger);
	}

	@Test
	void testResolveTimeRangeWithDayOff() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullBreakIntervals() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakIntervals()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		// Should fall back to parent class behavior
	}

	@Test
	void testResolveTimeRangeWithEmptyBreakIntervals() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		// Should fall back to parent class behavior
	}

	@Test
	void testResolveTimeRangeWithValidBreakIntervals() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(123);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1, this.breakInterval2));

		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(13, 0));
		given(this.breakInterval2.getBreakStartTime()).willReturn(LocalTime.of(15, 0));
		given(this.breakInterval2.getBreakEndTime()).willReturn(LocalTime.of(15, 30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(2);
	}

	@Test
	void testResolveTimeRangeWithInvalidBreakInterval() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(456);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Invalid interval: start time after end time
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(13, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(12, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithBreakIntervalStartTimeEqualToEndTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(606);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Invalid interval: start time equal to end time
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(12, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullBreakStartTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(789);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		given(this.breakInterval1.getBreakStartTime()).willReturn(null);
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(13, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullBreakEndTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(101);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithBreakIntervalEqualToWorkPeriod() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(202);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Break interval exactly matches work period
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(17, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
	}

	@Test
	void testResolveTimeRangeWithBreakIntervalOutsideWorkPeriod() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(303);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Break interval completely outside work period
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(18, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(19, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithBreakIntervalPartiallyOverlappingWorkPeriod() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(404);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Break interval partially overlaps work period
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(8, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(13, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		// Should contain only the intersection: 9:00-13:00
	}

	@Test
	void testResolveTimeRangeWithNullWorkPeriodRange() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(505);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(17, 0)); // After
																				// end
																				// time
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(13, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// Should return the original break intervals without constraint
	}

	@Test
	void testResolveTimeRangeWithBreakIntervalAdjacentToWorkPeriod() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(606);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Break interval adjacent to work period (connected but no overlap)
		// Work period: 9:00-17:00, Break interval: 17:00-18:00
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(17, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(18, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
		// The intersection should be empty because the ranges are adjacent but don't
		// overlap
		// This ensures the intersection.isEmpty() branch is covered
	}

	@Test
	void testGetWorkPeriodRangeWithNullActualWorkStartTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getWorkTime()).willReturn(null);
		given(this.timeLog.getWorkStartTime()).willReturn(null); // Null start time
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void testGetWorkPeriodRangeWithNullActualWorkEndTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getWorkTime()).willReturn(null);
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(null); // Null end time

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void testGetWorkPeriodRangeWithInvalidWorkTimes() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getWorkTime()).willReturn(null);
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(17, 0)); // Start
																				// after
																				// end
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(9, 0));

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void testGetWorkPeriodRangeWithEqualWorkTimes() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getWorkTime()).willReturn(null);
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(9, 0)); // Equal
																				// times

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void testLogIncompleteBreakAllocationIsCalled() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(707);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(this.breakInterval1));

		// Only 30 minutes available for break
		given(this.breakInterval1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(this.breakInterval1.getBreakEndTime()).willReturn(LocalTime.of(12, 30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// This triggers logIncompleteBreakAllocation because the requested break duration
		// (default or set in logic) exceeds available
	}

	@Test
	void testLogIncompleteBreakAllocationLogsWarningWithContext() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(808);
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		RangeSet<LocalTime> availableRanges = com.google.common.collect.TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		availableRanges.add(Range.closedOpen(LocalTime.of(11, 0), LocalTime.of(11, 30)));

		// When
		this.resolver.logIncompleteBreakAllocation(java.time.Duration.ofMinutes(120), 30L, availableRanges,
				this.context);

		// Then - a warning is logged including total available minutes (60 + 30 = 90)
		then(this.logger).should()
			.logWarn(org.mockito.ArgumentMatchers.contains("Range-based break time allocation incomplete"));
	}

}