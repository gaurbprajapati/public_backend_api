package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
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
class RangeBasedSpecificTimeRangeRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private CustomRule customRule;

	@Mock
	private TimeRangeResolverContext context;

	private RangeBasedSpecificTimeRangeRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedSpecificTimeRangeRuleTimeRangeResolver(this.logger);
	}

	@Test
	void testResolveTimeRangeWithValidTimeRange() {
		// Given
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));
		given(this.customRule.getEndTime()).willReturn(LocalTime.of(14, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
	}

	@Test
	void testResolveTimeRangeWithNullStartTime() {
		// Given
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(null);
		given(this.customRule.getEndTime()).willReturn(LocalTime.of(14, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullEndTime() {
		// Given
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));
		given(this.customRule.getEndTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithStartTimeAfterEndTime() {
		// Given
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(14, 0)); // After
																				// end
																				// time
		given(this.customRule.getEndTime()).willReturn(LocalTime.of(10, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithStartTimeEqualToEndTime() {
		// Given
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));
		given(this.customRule.getEndTime()).willReturn(LocalTime.of(10, 0)); // Equal to
																				// start
																				// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithOccupiedTimeRanges() {
		// Given
		given(this.context.getCurrentCustomRuleBeingEvaluated()).willReturn(this.customRule);
		given(this.customRule.getStartTime()).willReturn(LocalTime.of(10, 0));
		given(this.customRule.getEndTime()).willReturn(LocalTime.of(14, 0));

		// Create occupied time ranges that overlap with the specific time range
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(11, 0), LocalTime.of(12, 0)));
		occupiedRanges.add(Range.closedOpen(LocalTime.of(13, 0), LocalTime.of(14, 0)));
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// Should only contain the available time from 10:00-11:00 and 12:00-13:00
	}

}