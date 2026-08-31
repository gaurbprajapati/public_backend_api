package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RangeBasedAfterShiftRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private CustomRule customRule;

	@Mock
	private TimeRangeResolverContext context;

	private RangeBasedAfterShiftRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedAfterShiftRuleTimeRangeResolver(this.logger);
	}

	@Test
	void testResolveTimeRangeWithValidAfterShiftPeriod() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(16, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
	}

	@Test
	void testResolveTimeRangeWithNullStartTime() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullEndTime() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkEndTime(null);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(16, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("invalidStartAndEndTimeCombinations")
	void testResolveTimeRangeWithInvalidStartAndEndTimes(LocalTime endTime, LocalTime startTime) {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkEndTime(endTime);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(startTime);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithOccupiedTimeRanges() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(16, 0));

		// Create occupied time ranges that overlap with the after shift period
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(16, 0), LocalTime.of(16, 30)));
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// Should only contain the available time from 16:30-17:00
	}

	private static Stream<Arguments> invalidStartAndEndTimeCombinations() {
		return Stream.of(Arguments.of(LocalTime.of(15, 0), LocalTime.of(16, 0)),
				Arguments.of(LocalTime.of(16, 0), LocalTime.of(16, 0)),
				Arguments.of(LocalTime.of(14, 0), LocalTime.of(15, 0)),
				Arguments.of(LocalTime.of(15, 0), LocalTime.of(15, 0)));
	}

}
