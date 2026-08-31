package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
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
class DurationBasedSpecificHourRangeRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private TimeLog timeLog;

	@Mock
	private TimeRangeResolverContext context;

	@Mock
	private CustomRule customRule;

	private DurationBasedSpecificHourRangeRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new DurationBasedSpecificHourRangeRuleTimeRangeResolver(this.logger);
	}

	@Test
	void testResolveTimeRangeWithDayOff() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.DAY_OFF);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullStartDuration() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(null);
		given(this.customRule.getEndDuration()).willReturn(Duration.ofHours(8));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullEndDuration() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(Duration.ofHours(0));
		given(this.customRule.getEndDuration()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithStartEqualsEndDuration() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(Duration.ofHours(4));
		given(this.customRule.getEndDuration()).willReturn(Duration.ofHours(4)); // Start
																					// equals
																					// end

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithStartAfterEndDuration() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(Duration.ofHours(8));
		given(this.customRule.getEndDuration()).willReturn(Duration.ofHours(4)); // Start
																					// after
																					// end

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithValidDurationsAndBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(Duration.ofHours(2));
		given(this.customRule.getEndDuration()).willReturn(Duration.ofHours(6));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofHours(1));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();

		// Original range: 02:00-06:00, shifted by 1 hour break: 03:00-07:00
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> specificHourRange = result.asRanges().iterator().next();
		assertThat(specificHourRange.lowerEndpoint()).isEqualTo(LocalTime.of(3, 0));
		assertThat(specificHourRange.upperEndpoint()).isEqualTo(LocalTime.of(7, 0));
	}

	@Test
	void testResolveTimeRangeWithValidDurationsAndNullBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(Duration.ofHours(2));
		given(this.customRule.getEndDuration()).willReturn(Duration.ofHours(6));
		given(this.timeLog.getBreakTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();

		// No break time, so no shift - original range: 02:00-06:00
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> specificHourRange = result.asRanges().iterator().next();
		assertThat(specificHourRange.lowerEndpoint()).isEqualTo(LocalTime.of(2, 0));
		assertThat(specificHourRange.upperEndpoint()).isEqualTo(LocalTime.of(6, 0));
	}

	@Test
	void testResolveTimeRangeWithValidDurationsAndZeroBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.customRule.getStartDuration()).willReturn(Duration.ofHours(2));
		given(this.customRule.getEndDuration()).willReturn(Duration.ofHours(6));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();

		// Zero break time, so no shift - original range: 02:00-06:00
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> specificHourRange = result.asRanges().iterator().next();
		assertThat(specificHourRange.lowerEndpoint()).isEqualTo(LocalTime.of(2, 0));
		assertThat(specificHourRange.upperEndpoint()).isEqualTo(LocalTime.of(6, 0));
	}

	@Test
	void testResolveTimeRangeWithShiftedRangeEqualsAll() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		// Use large durations to try to trigger Range.all()
		given(this.customRule.getStartDuration()).willReturn(Duration.ofDays(10000));
		given(this.customRule.getEndDuration()).willReturn(Duration.ofDays(20000));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofDays(10000));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

}