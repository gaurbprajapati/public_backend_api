package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class DurationBasedDailyOvertimeRuleTimeRangeResolverTests {

	@Mock
	private TimeLog timeLog;

	@Mock
	private CustomRule customRule;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSetting;

	@Mock
	private Logger logger;

	private DurationBasedDailyOvertimeRuleTimeRangeResolver resolver;

	private List<CustomRule> internalSortedCustomRules;

	@BeforeEach
	void setUp() {
		this.resolver = new DurationBasedDailyOvertimeRuleTimeRangeResolver(this.logger);
		this.internalSortedCustomRules = new ArrayList<>();
	}

	@Test
	@DisplayName("Daily overtime should handle breaks excluded from calculation with 8.5 hour threshold - duration-based")
	void testDailyOvertimeWithBreaksExcludedAndEightAndHalfHourThreshold() {
		// Arrange
		// Set up time log: 9:00-18:30 (9.5 hours total) with 30-minute break from
		// 13:00-13:30
		given(this.timeLog.getWorkTime()).willReturn(Duration.ofHours(9).plusMinutes(30)); // Duration-based
																							// time
																							// log
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(LocalTime.of(18, 30));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofMinutes(30)); // 30-minute
																				// break
		given(this.timeLog.getDate()).willReturn(java.time.LocalDate.of(2024, 1, 1)); // Monday

		// Set up daily overtime rule with 8.5-hour threshold
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8).plusMinutes(30)); // 8.5
																									// hours
		given(this.customRule.isApplicableOnDay(any())).willReturn(true);

		// Set up rule template (breaks excluded)

		// Set up internal sorted custom rules (only one rule, so it's the last)
		this.internalSortedCustomRules.add(this.customRule);

		// Set up context with occupied time ranges representing regular hours
		// With the fix, regular hours should claim 9:00-18:00 (9 hours total, 8.5
		// effective)
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(18, 0))); // 9
																						// hours
																						// regular
																						// (8.5
																						// effective)

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(this.timeLog)
			.currentCustomRuleBeingEvaluated(this.customRule)
			.currentTimesheetSetting(this.timesheetSetting)
			.occupiedTimeRanges(occupiedRanges)
			.internalSortedCustomRules(this.internalSortedCustomRules)
			.currentRuleIndex(0)
			.build();

		// Act
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(context);

		// Assert
		// CORRECTED LOGIC:
		// The system now calculates DO threshold time as: 9:00 + (8.5 + 0.5) hours =
		// 18:00
		// This is correct because:
		// - At 18:00, employee has worked 9 hours total, which is 8.5 hours effective
		// (excluding break)
		// - Employee reaches the 8.5-hour threshold at 18:00
		// - DO should start at 18:00

		// Expected calculation:
		// Total time: 9:00-18:30 = 9.5 hours
		// Break time: 30 minutes (13:00-13:30)
		// Effective work time: 9.5 - 0.5 = 9 hours
		// DO threshold: 8.5 hours
		// DO hours: 9 - 8.5 = 0.5 hours (30 minutes)
		// DO should claim 30 minutes from 18:00-18:30 (after reaching 8.5 effective
		// hours)

		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);

		Range<LocalTime> doRange = result.asRanges().iterator().next();
		// Corrected behavior:
		assertThat(doRange.lowerEndpoint()).isEqualTo(LocalTime.of(18, 0)); // Correct! DO
																			// starts at
																			// 18:00
		assertThat(doRange.upperEndpoint()).isEqualTo(LocalTime.of(18, 30)); // Correct!
																				// DO ends
																				// at
																				// 18:30

		// The fix: The system now properly accounts for breaks when calculating
		// when the employee reaches the DO threshold of effective work time
	}

	@Test
	void testGetDailyOvertimeRuleType() {
		// When
		RuleType ruleType = this.resolver.getDailyOvertimeRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_DAILY_OVERTIME);
	}

}