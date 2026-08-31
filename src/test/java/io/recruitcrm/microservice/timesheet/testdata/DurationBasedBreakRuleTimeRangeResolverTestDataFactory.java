package io.recruitcrm.microservice.timesheet.testdata;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Shared test constants and values for
 * {@link io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based.DurationBasedBreakRuleTimeRangeResolver}
 * tests.
 */
public final class DurationBasedBreakRuleTimeRangeResolverTestDataFactory {

	private DurationBasedBreakRuleTimeRangeResolverTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static final LocalTime NORMALIZED_WORK_DAY_START = LocalTime.MIDNIGHT;

	public static final LocalTime NORMALIZED_WORK_DAY_END_EIGHT_HOURS = LocalTime.of(8, 0);

	public static final Duration DURATION_EIGHT_HOURS = Duration.ofHours(8);

	public static final Duration DURATION_ONE_HOUR = Duration.ofHours(1);

	public static final Duration DURATION_TWO_HOURS = Duration.ofHours(2);

	public static final Duration DURATION_THREE_HOURS = Duration.ofHours(3);

	public static final Duration DURATION_FOUR_HOURS = Duration.ofHours(4);

	public static final Duration DURATION_TEN_HOURS = Duration.ofHours(10);

}
