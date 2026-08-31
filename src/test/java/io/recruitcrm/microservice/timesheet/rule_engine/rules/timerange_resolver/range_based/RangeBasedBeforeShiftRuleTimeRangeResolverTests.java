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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RangeBasedBeforeShiftRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private CustomRule customRule;

	@Mock
	private TimeRangeResolverContext context;

	private RangeBasedBeforeShiftRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedBeforeShiftRuleTimeRangeResolver(this.logger);
	}

	@Test
	void testResolveTimeRangeWithValidBeforeShiftPeriod() {
		// Use a real TimeLog object to avoid static mocking issues
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
	}

	@Test
	void testResolveTimeRangeWithNullEffectiveStartTime() {
		// Only stub what's needed for this path
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(null);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullRuleStartTime() {
		// Only stub what's needed for this path
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithEffectiveStartTimeAfterRuleStartTime() {
		// Only stub what's needed for this path
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(11, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithEffectiveStartTimeEqualToRuleStartTime() {
		// Only stub what's needed for this path
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(10, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithOccupiedTimeRanges() {
		// Use a real TimeLog object and set up a before-shift period with partial
		// occupation
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));

		// Occupied range only covers 9:30-10:00, so 9:00-9:30 should be available
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 30), LocalTime.of(10, 0)));
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);
		assertThat(result).isNotNull();
		// Only 9:00-9:30 should be available
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> available = result.asRanges().iterator().next();
		assertThat(available.lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(available.upperEndpoint()).isEqualTo(LocalTime.of(9, 30));
	}

}