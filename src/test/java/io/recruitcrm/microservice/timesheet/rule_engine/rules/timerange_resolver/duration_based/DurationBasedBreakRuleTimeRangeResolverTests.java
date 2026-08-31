/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import io.recruitcrm.microservice.timesheet.testdata.DurationBasedBreakRuleTimeRangeResolverTestDataFactory;

@ExtendWith(MockitoExtension.class)
@DisplayName("DurationBasedBreakRuleTimeRangeResolver Tests")
class DurationBasedBreakRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private TimeLog timeLog;

	@Mock
	private TimeRangeResolverContext context;

	private DurationBasedBreakRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new DurationBasedBreakRuleTimeRangeResolver(this.logger);
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when break time is null")
	void testGetWorkPeriodRangeBreakTimeNullReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(null);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when break time is zero")
	void testGetWorkPeriodRangeBreakTimeZeroReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when normalized work start is null")
	void testGetWorkPeriodRangeNormalizedStartNullReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(null);
		given(this.timeLog.getNormalizedWorkEndTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when normalized work end is null")
	void testGetWorkPeriodRangeNormalizedEndNullReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(null);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when normalized end is not after start")
	void testGetWorkPeriodRangeInvalidNormalizedOrderReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(LocalTime.of(8, 0));
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(LocalTime.MIDNIGHT);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when normalized start equals end")
	void testGetWorkPeriodRangeEqualNormalizedEndpointsReturnsNull() {
		// Given
		LocalTime same = LocalTime.of(9, 0);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(same);
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(same);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when break duration is not less than actual work duration")
	void testGetWorkPeriodRangeBreakNotShorterThanWorkReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_TEN_HOURS);
		given(this.timeLog.getNormalizedWorkStartTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		given(this.timeLog.getNormalizedWorkEndTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when calculateDuration returns null")
	void testGetWorkPeriodRangeActualWorkTimeNullReturnsNull() {
		// Given
		LocalTime startTime = DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START;
		LocalTime endTime = DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS;
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(startTime);
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(endTime);

		// When
		Range<LocalTime> result;
		try (var mocked = Mockito.mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked.when(() -> TimeHelper.calculateDuration(startTime, endTime)).thenReturn(null);
			result = this.resolver.getWorkPeriodRange(this.context);
		}

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when calculateDuration returns zero")
	void testGetWorkPeriodRangeActualWorkTimeZeroReturnsNull() {
		// Given
		LocalTime startTime = DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START;
		LocalTime endTime = DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS;
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(startTime);
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(endTime);

		// When
		Range<LocalTime> result;
		try (var mocked = Mockito.mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked.when(() -> TimeHelper.calculateDuration(startTime, endTime)).thenReturn(Duration.ZERO);
			result = this.resolver.getWorkPeriodRange(this.context);
		}

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns null when break duration equals actual work duration")
	void testGetWorkPeriodRangeBreakEqualsWorkReturnsNull() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_EIGHT_HOURS);
		given(this.timeLog.getNormalizedWorkStartTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		given(this.timeLog.getNormalizedWorkEndTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkPeriodRange returns normalized work range when break is shorter than work")
	void testGetWorkPeriodRangeValidReturnsNormalizedRange() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		given(this.timeLog.getNormalizedWorkEndTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);

		// When
		Range<LocalTime> result = this.resolver.getWorkPeriodRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.lowerEndpoint())
			.isEqualTo(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		assertThat(result.upperEndpoint())
			.isEqualTo(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideBreakAllocationScenarios")
	@DisplayName("resolveTimeRange allocates break from midnight for duration-based logs")
	void testResolveTimeRangeBreakAllocatedAtStart(String scenario, Duration breakDuration, LocalTime expectedEnd) {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(breakDuration);
		given(this.timeLog.getNormalizedWorkStartTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		given(this.timeLog.getNormalizedWorkEndTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty().hasSize(1);
		Range<LocalTime> breakRange = result.asRanges().iterator().next();
		assertThat(breakRange.lowerEndpoint())
			.isEqualTo(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		assertThat(breakRange.upperEndpoint()).isEqualTo(expectedEnd);
		then(this.logger).should()
			.logDebug(
					argThat((String message) -> message != null && message.contains("Allocated break time at start")));
	}

	static Stream<Arguments> provideBreakAllocationScenarios() {
		return Stream.of(
				Arguments.of("Two hour break from midnight",
						DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_TWO_HOURS, LocalTime.of(2, 0)),
				Arguments.of("Three hour break from midnight",
						DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_THREE_HOURS,
						LocalTime.of(3, 0)),
				Arguments.of("Four hour break from midnight",
						DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_FOUR_HOURS,
						LocalTime.of(4, 0)));
	}

	@Test
	@DisplayName("resolveTimeRange returns empty when break time is null")
	void testResolveTimeRangeBreakNullReturnsEmpty() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	@DisplayName("resolveTimeRange returns empty when break time is zero")
	void testResolveTimeRangeBreakZeroReturnsEmpty() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	@DisplayName("resolveTimeRange returns empty when time log is the only stub and break defaults to null")
	void testResolveTimeRangeNoBreakConfiguredReturnsEmpty() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	@DisplayName("resolveTimeRange returns empty when work period cannot be resolved")
	void testResolveTimeRangeNoWorkPeriodReturnsEmpty() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	@DisplayName("resolveTimeRange returns empty when start break range resolves to all-day")
	void testResolveTimeRangeAllDayBreakRangeReturnsEmpty() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getBreakTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR);
		given(this.timeLog.getNormalizedWorkStartTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_START);
		given(this.timeLog.getNormalizedWorkEndTime())
			.willReturn(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.NORMALIZED_WORK_DAY_END_EIGHT_HOURS);

		// When
		RangeSet<LocalTime> result;
		try (var mocked = Mockito.mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked
				.when(() -> TimeHelper
					.createBreakRangeAtStart(DurationBasedBreakRuleTimeRangeResolverTestDataFactory.DURATION_ONE_HOUR))
				.thenReturn(Range.all());
			result = this.resolver.resolveTimeRange(this.context);
		}

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
		then(this.logger).should(Mockito.never()).logDebug(argThat(Objects::nonNull));
	}

}
