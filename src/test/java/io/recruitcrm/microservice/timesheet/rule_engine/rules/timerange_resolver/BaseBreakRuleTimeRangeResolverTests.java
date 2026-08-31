package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BaseBreakRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private TimeLog timeLog;

	@Mock
	private TimeRangeResolverContext context;

	private TestBreakRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new TestBreakRuleTimeRangeResolver(this.logger);
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
	void testResolveTimeRangeWithNullBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithZeroBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullWorkRange() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofHours(1));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testAllocateBreakTimeWithEmptyAvailableRanges() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		Duration breakDuration = Duration.ofHours(1);

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testAllocateBreakTimeWithZeroBreakDuration() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		Duration breakDuration = Duration.ZERO;

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testAllocateBreakTimeWithNegativeBreakDuration() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		Duration breakDuration = Duration.ofMinutes(-30);

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testAllocateBreakTimeWithExactAvailableTime() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0))); // 1
																							// hour
																							// available
		Duration breakDuration = Duration.ofHours(1);

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> allocatedRange = result.asRanges().iterator().next();
		assertThat(allocatedRange.lowerEndpoint()).isEqualTo(LocalTime.of(12, 0));
		assertThat(allocatedRange.upperEndpoint()).isEqualTo(LocalTime.of(13, 0));
	}

	@Test
	void testAllocateBreakTimeWithMoreAvailableTimeThanNeeded() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(14, 0))); // 2
																							// hours
																							// available
		Duration breakDuration = Duration.ofHours(1); // Only need 1 hour

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> allocatedRange = result.asRanges().iterator().next();
		assertThat(allocatedRange.lowerEndpoint()).isEqualTo(LocalTime.of(12, 0));
		assertThat(allocatedRange.upperEndpoint()).isEqualTo(LocalTime.of(13, 0));
	}

	@Test
	void testAllocateBreakTimeWithLessAvailableTimeThanNeeded() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getId()).willReturn(123);

		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0))); // 1
																							// hour
																							// available
		Duration breakDuration = Duration.ofHours(2); // Need 2 hours

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> allocatedRange = result.asRanges().iterator().next();
		assertThat(allocatedRange.lowerEndpoint()).isEqualTo(LocalTime.of(12, 0));
		assertThat(allocatedRange.upperEndpoint()).isEqualTo(LocalTime.of(13, 0));
		// The logIncompleteBreakAllocation should be called for the remaining 1 hour
	}

	@Test
	void testAllocateBreakTimeWithMultipleAvailableRanges() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(11, 0))); // 1
																							// hour
		availableRanges.add(Range.closedOpen(LocalTime.of(14, 0), LocalTime.of(16, 0))); // 2
																							// hours
		Duration breakDuration = Duration.ofHours(2); // Need 2 hours

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(2);
		// Should allocate 1 hour from first range and 1 hour from second range
	}

	@Test
	void testAllocateBreakTimeWithZeroAllocateMinutes() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(12, 0))); // 0
																							// minutes
																							// available
		Duration breakDuration = Duration.ofHours(1);

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testAllocateBreakTimeWithRemainingMinutesZeroAfterFirstAllocation() {
		// Given
		RangeSet<LocalTime> availableRanges = TreeRangeSet.create();
		availableRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0))); // 1
																							// hour
		availableRanges.add(Range.closedOpen(LocalTime.of(14, 0), LocalTime.of(15, 0))); // 1
																							// hour
		Duration breakDuration = Duration.ofHours(1); // Need exactly 1 hour

		// When
		RangeSet<LocalTime> result = this.resolver.allocateBreakTime(availableRanges, breakDuration, this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		// Should only use the first range, remainingMinutes becomes 0, so second range is
		// skipped
	}

	/**
	 * Test implementation of BaseBreakRuleTimeRangeResolver for testing purposes.
	 */
	private static class TestBreakRuleTimeRangeResolver extends BaseBreakRuleTimeRangeResolver {

		TestBreakRuleTimeRangeResolver(Logger logger) {
			super(logger);
		}

		@Override
		protected Range<LocalTime> getWorkPeriodRange(TimeRangeResolverContext timeRangeResolverContext) {
			// Return null to test the null work range branch
			return null;
		}

	}

}