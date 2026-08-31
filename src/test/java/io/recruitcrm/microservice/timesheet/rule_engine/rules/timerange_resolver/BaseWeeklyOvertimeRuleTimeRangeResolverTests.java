/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

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

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Base test class for weekly overtime time range resolver tests. Provides common test
 * functionality for both range-based and duration-based implementations.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseWeeklyOvertimeRuleTimeRangeResolverTests {

	@Mock
	protected Logger logger;

	@Mock
	protected TimeLog timeLog;

	@Mock
	protected TimeRangeResolverContext context;

	protected BaseWeeklyOvertimeRuleTimeRangeResolver resolver;

	/**
	 * Creates the specific resolver implementation to test.
	 * @param logger the logger to use
	 * @return the resolver instance
	 */
	protected abstract BaseWeeklyOvertimeRuleTimeRangeResolver createResolver(Logger logger);

	@BeforeEach
	void setUp() {
		this.resolver = createResolver(this.logger);
	}

	@Test
	void testResolveTimeRangeWithValidTimeRange() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> range = result.asRanges().iterator().next();
		assertThat(range.lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(range.upperEndpoint()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	void testResolveTimeRangeWithNullEffectiveStartTime() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(null);
		realTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullEffectiveEndTime() {
		// Given
		TimeLog realTimeLog = new TimeLog();
		realTimeLog.setWorkStartTime(LocalTime.of(9, 0));
		realTimeLog.setWorkEndTime(null);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(realTimeLog);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullTimeLog() {
		// Given - Test the case where there's no current time log (weekly overtime
		// evaluation)
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(null);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> range = result.asRanges().iterator().next();
		assertThat(range.lowerEndpoint()).isEqualTo(LocalTime.MIDNIGHT);
		assertThat(range.upperEndpoint()).isEqualTo(LocalTime.MAX);
	}

}