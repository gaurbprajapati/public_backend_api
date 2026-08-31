/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseWeeklyOvertimeRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.BaseWeeklyOvertimeRuleTimeRangeResolverTests;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for range-based weekly overtime time range resolver. Extends the base test class
 * to inherit common test functionality.
 */
class RangeBasedWeeklyOvertimeRuleTimeRangeResolverTests extends BaseWeeklyOvertimeRuleTimeRangeResolverTests {

	@Override
	protected BaseWeeklyOvertimeRuleTimeRangeResolver createResolver(Logger logger) {
		return new RangeBasedWeeklyOvertimeRuleTimeRangeResolver(logger);
	}

	@Test
	void testResolveTimeRangeWithStartTimeAfterEndTime() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(17, 0));
		realTimeLog.setWorkEndTime(LocalTime.of(9, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithStartTimeEqualToEndTime() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		realTimeLog.setWorkEndTime(LocalTime.of(9, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);

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
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);

		// Occupied ranges: 10:00-12:00, 14:00-16:00
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(12, 0)));
		occupiedRanges.add(Range.closedOpen(LocalTime.of(14, 0), LocalTime.of(16, 0)));
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		List<Range<LocalTime>> freeRanges = List.copyOf(result.asRanges());
		assertThat(freeRanges).hasSize(3);
		assertThat(freeRanges.get(0).lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(freeRanges.get(0).upperEndpoint()).isEqualTo(LocalTime.of(10, 0));
		assertThat(freeRanges.get(1).lowerEndpoint()).isEqualTo(LocalTime.of(12, 0));
		assertThat(freeRanges.get(1).upperEndpoint()).isEqualTo(LocalTime.of(14, 0));
		assertThat(freeRanges.get(2).lowerEndpoint()).isEqualTo(LocalTime.of(16, 0));
		assertThat(freeRanges.get(2).upperEndpoint()).isEqualTo(LocalTime.of(17, 0));
	}

}