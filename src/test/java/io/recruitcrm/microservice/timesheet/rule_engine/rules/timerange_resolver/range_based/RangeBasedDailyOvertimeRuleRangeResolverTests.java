/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class RangeBasedDailyOvertimeRuleRangeResolverTests {

	@Mock
	private TimeLog timeLog;

	@Mock
	private CustomRule customRule;

	@Mock
	private CustomRule nextCustomRule;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSetting;

	@Mock
	private Logger logger;

	private RangeBasedDailyOvertimeRuleRangeResolver resolver;

	private List<CustomRule> internalSortedCustomRules;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedDailyOvertimeRuleRangeResolver(this.logger);
		this.internalSortedCustomRules = new ArrayList<>();
	}

	@Test
	@DisplayName("Daily overtime returns empty when not the last interval of the day")
	void testDailyOvertimeSkipsNonLastInterval() {
		// Arrange: two same-day intervals, current is the first
		TimeLog currentTimeLog = new TimeLog();
		currentTimeLog.setWorkStartTime(LocalTime.of(7, 0));
		currentTimeLog.setWorkEndTime(LocalTime.of(12, 0));

		TimeLog laterTimeLog = new TimeLog();
		laterTimeLog.setWorkStartTime(LocalTime.of(13, 0));
		laterTimeLog.setWorkEndTime(LocalTime.of(17, 0));

		List<TimeLog> sameDayLogs = List.of(currentTimeLog, laterTimeLog);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(currentTimeLog)
			.sameDayTimeLogs(sameDayLogs)
			.build();

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(context);

		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Daily overtime proceeds when current interval is the last of day")
	void testDailyOvertimeProceedsOnLastInterval() {
		TimeLog lastTimeLog = new TimeLog();
		lastTimeLog.setWorkStartTime(LocalTime.of(13, 0));
		lastTimeLog.setWorkEndTime(LocalTime.of(17, 0));
		lastTimeLog.setDate(LocalDate.of(2025, 1, 6));

		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting setting = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting();
		setting.setCalculateBreakTime(Boolean.TRUE);

		this.internalSortedCustomRules.add(this.customRule);

		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8));
		given(this.customRule.isApplicableOnDay(any())).willReturn(true);

		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(lastTimeLog)
			.currentCustomRuleBeingEvaluated(this.customRule)
			.currentTimesheetSetting(setting)
			.occupiedTimeRanges(occupiedRanges)
			.internalSortedCustomRules(this.internalSortedCustomRules)
			.currentRuleIndex(0)
			.sameDayTimeLogs(List.of(lastTimeLog))
			.build();

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(context);

		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Daily overtime should not be applied for work time below threshold")
	void testDailyOvertimeNotAppliedWhenWorkTimeBelowThreshold() {
		// Arrange
		// Set up time log: 6 hours work (09:00-15:00) - range-based
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(15, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// Set up daily overtime rule with 8-hour threshold
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		// Set up rule template

		// Set up internal sorted custom rules (only one rule, so it's the last)
		this.internalSortedCustomRules.add(this.customRule);

		// Set up context with occupied time ranges representing regular hours
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(15, 0))); // 6
																						// hours
																						// regular

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
		// Should return empty range set because total work time (6h) <= threshold (8h)
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Daily overtime should not be applied for work time equal to threshold")
	void testDailyOvertimeNotAppliedWhenWorkTimeEqualsThreshold() {
		// Arrange
		// Set up time log: 8 hours work (09:00-17:00) - range-based
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// Set up daily overtime rule with 8-hour threshold
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		// Set up rule template

		// Set up internal sorted custom rules (only one rule, so it's the last)
		this.internalSortedCustomRules.add(this.customRule);

		// Set up context with occupied time ranges representing regular hours
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8
																						// hours
																						// regular

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
		// Should return empty range set because total work time (8h) = threshold (8h)
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Daily overtime should be applied for work time exceeding threshold")
	void testDailyOvertimeAppliedWhenWorkTimeExceedsThreshold() {
		// Arrange
		// Set up time log: 10 hours work (09:00-19:00) - range-based
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(19, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday

		// Set up daily overtime rule with 8-hour threshold
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8));
		given(this.customRule.isApplicableOnDay(any())).willReturn(true);

		// Set up rule template

		// Set up internal sorted custom rules (only one rule, so it's the last)
		this.internalSortedCustomRules.add(this.customRule);

		// Set up context with occupied time ranges representing regular hours (8 hours)
		// This leaves the overtime range (17:00-19:00) available for the DO rule
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8
																						// hours
																						// regular

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
		// Should claim 2 hours of overtime from 17:00-19:00
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);

		Range<LocalTime> overtimeRange = result.asRanges().iterator().next();
		assertThat(overtimeRange.lowerEndpoint()).isEqualTo(LocalTime.of(17, 0)); // Start
																					// after
																					// regular
																					// hours
		assertThat(overtimeRange.upperEndpoint()).isEqualTo(LocalTime.of(19, 0)); // End
																					// at
																					// work
																					// end
																					// time
	}

	@Test
	@DisplayName("Daily overtime should be applied from the end of the day")
	void testDailyOvertimeAppliedFromEndOfDay() {
		// Arrange
		// Set up time log: 16 hours work (06:00-22:00) - range-based, very long day
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(6, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(22, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday

		// Set up daily overtime rule with 6-hour threshold (lower threshold for more
		// overtime)
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(6));
		given(this.customRule.isApplicableOnDay(any())).willReturn(true);

		// Set up rule template

		// Set up internal sorted custom rules (only one rule, so it's the last)
		this.internalSortedCustomRules.add(this.customRule);

		// Set up context with occupied time ranges representing regular hours (6 hours)
		// This leaves the overtime range (12:00-22:00) available for the DO rule
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(6, 0), LocalTime.of(12, 0))); // 6
																						// hours
																						// regular

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
		// Should claim overtime from threshold time to end of day (12:00-22:00)
		// This tests the "end of day" scenario with a much longer work day
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);

		Range<LocalTime> overtimeRange = result.asRanges().iterator().next();
		assertThat(overtimeRange.lowerEndpoint()).isEqualTo(LocalTime.of(12, 0)); // Threshold
																					// time
		assertThat(overtimeRange.upperEndpoint()).isEqualTo(LocalTime.of(22, 0)); // End
																					// of
																					// work
																					// day
		// Verify that overtime extends to the very end of the work day
		assertThat(overtimeRange.upperEndpoint()).isEqualTo(this.timeLog.getWorkEndTime());
	}

	@Test
	@DisplayName("Daily overtime should handle duration-based time logs")
	void testDailyOvertimeWithDurationBasedTimeLog() {
		// Arrange
		// Set up time log: 10 hours work - duration-based
		given(this.timeLog.getWorkTime()).willReturn(Duration.ofHours(10)); // Duration-based
																			// time log
		given(this.timeLog.getNormalizedWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getNormalizedWorkEndTime()).willReturn(LocalTime.of(19, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday

		// Set up daily overtime rule with 8-hour threshold
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8));
		given(this.customRule.isApplicableOnDay(any())).willReturn(true);

		// Set up rule template

		// Set up internal sorted custom rules (only one rule, so it's the last)
		this.internalSortedCustomRules.add(this.customRule);

		// Set up context with occupied time ranges representing regular hours (8 hours)
		// This leaves the overtime range (17:00-19:00) available for the DO rule
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8
																						// hours
																						// regular

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
		// Should claim 2 hours of overtime from 17:00-19:00 since work time (10h) >
		// threshold (8h)
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);

		Range<LocalTime> overtimeRange = result.asRanges().iterator().next();
		assertThat(overtimeRange.lowerEndpoint()).isEqualTo(LocalTime.of(17, 0)); // Start
																					// after
																					// regular
																					// hours
		assertThat(overtimeRange.upperEndpoint()).isEqualTo(LocalTime.of(19, 0)); // End
																					// at
																					// work
																					// end
																					// time
	}

	@Test
	@DisplayName("Daily overtime should handle invalid time log (start time after end time)")
	void testDailyOvertimeWithInvalidTimeLog() {
		// Arrange
		// Set up invalid time log: start time after end time
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(19, 0)); // Start
																				// at
																				// 19:00
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(9, 0)); // End at
																				// 09:00
																				// (invalid)

		// Set up internal sorted custom rules
		this.internalSortedCustomRules.add(this.customRule);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(this.timeLog)
			.currentCustomRuleBeingEvaluated(this.customRule)
			.currentTimesheetSetting(this.timesheetSetting)
			.internalSortedCustomRules(this.internalSortedCustomRules)
			.currentRuleIndex(0)
			.build();

		// Act
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(context);

		// Assert
		// Should return empty range set because time log is invalid
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Daily overtime with multiple rules - should use next threshold")
	void testDailyOvertimeWithMultipleRules() {
		// Arrange
		// Set up time log: 12 hours work (09:00-21:00) - range-based
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(21, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday

		// Set up first daily overtime rule with 8-hour threshold
		given(this.customRule.getDailyThreshold()).willReturn(Duration.ofHours(8));
		given(this.customRule.isApplicableOnDay(any())).willReturn(true);

		// Set up second daily overtime rule with 12-hour threshold
		given(this.nextCustomRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(this.nextCustomRule.getDailyThreshold()).willReturn(Duration.ofHours(12));
		given(this.nextCustomRule.isApplicableOnDay(any())).willReturn(true);

		// Set up rule template

		// Set up internal sorted custom rules
		this.internalSortedCustomRules.add(this.customRule);
		this.internalSortedCustomRules.add(this.nextCustomRule);

		// Set up occupied time ranges representing regular hours (8 hours)
		// This leaves the overtime range (17:00-21:00) available for the first DO rule
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8
																						// hours
																						// regular

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
		// The first DO rule should claim overtime from 17:00 (8-hour threshold) to 21:00
		// (12-hour threshold)
		// This is 4 hours of overtime within the first rule's range
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);

		Range<LocalTime> overtimeRange = result.asRanges().iterator().next();
		assertThat(overtimeRange.lowerEndpoint()).isEqualTo(LocalTime.of(17, 0)); // 8-hour
																					// threshold
		assertThat(overtimeRange.upperEndpoint()).isEqualTo(LocalTime.of(21, 0)); // 12-hour
																					// threshold
																					// (next
																					// rule's
																					// threshold)
	}

	@Test
	@DisplayName("Daily overtime with multiple rules - last rule should claim to end of day")
	void testDailyOvertimeWithMultipleRulesLastRule() {
		// Arrange
		// Set up time log: 14 hours work (09:00-23:00) - range-based
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(23, 0));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday

		// Set up second daily overtime rule with 12-hour threshold (current rule being
		// evaluated)
		given(this.nextCustomRule.getDailyThreshold()).willReturn(Duration.ofHours(12));
		given(this.nextCustomRule.isApplicableOnDay(any())).willReturn(true);

		// Set up rule template

		// Set up internal sorted custom rules
		this.internalSortedCustomRules.add(this.customRule);
		this.internalSortedCustomRules.add(this.nextCustomRule);

		// Set up occupied time ranges representing regular hours (8 hours) + first DO
		// rule (8-12 hours)
		// This leaves the overtime range (21:00-23:00) available for the second DO rule
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8
																						// hours
																						// regular
		occupiedRanges.add(Range.closedOpen(LocalTime.of(17, 0), LocalTime.of(21, 0))); // 4
																						// hours
																						// first
																						// DO
																						// rule

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(this.timeLog)
			.currentCustomRuleBeingEvaluated(this.nextCustomRule) // Second rule
			.currentTimesheetSetting(this.timesheetSetting)
			.occupiedTimeRanges(occupiedRanges)
			.internalSortedCustomRules(this.internalSortedCustomRules)
			.currentRuleIndex(1) // Second rule (last DO rule)
			.build();

		// Act
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(context);

		// Assert
		// The second (last) DO rule should claim overtime from 21:00 (12-hour threshold)
		// to 23:00 (end of work day)
		// This is 2 hours of overtime within the second rule's range
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.asRanges()).hasSize(1);

		Range<LocalTime> overtimeRange = result.asRanges().iterator().next();
		assertThat(overtimeRange.lowerEndpoint()).isEqualTo(LocalTime.of(21, 0)); // 12-hour
																					// threshold
		assertThat(overtimeRange.upperEndpoint()).isEqualTo(LocalTime.of(23, 0)); // End
																					// of
																					// work
																					// day
	}

	@Test
	@DisplayName("Daily overtime should handle breaks excluded from calculation with 8.5 hour threshold")
	void testDailyOvertimeWithBreaksExcludedAndEightAndHalfHourThreshold() {
		// Arrange
		// Set up time log: 9:00-18:30 (9.5 hours total) with 30-minute break from
		// 13:00-13:30
		given(this.timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(18, 30));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofMinutes(30)); // 30-minute
																				// break
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday

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

}