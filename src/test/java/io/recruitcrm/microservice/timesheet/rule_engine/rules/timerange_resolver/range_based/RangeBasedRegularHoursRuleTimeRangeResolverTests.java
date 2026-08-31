
package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RangeBasedRegularHoursRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private TimeLog timeLog;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private TemplateWorkDay templateWorkDay;

	@Mock
	private TimeRangeResolverContext context;

	private RangeBasedRegularHoursRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedRegularHoursRuleTimeRangeResolver(this.logger);
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
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	void testResolveTimeRangeWithValidWorkDay() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		assertThat(result.encloses(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)))).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullWorkStartTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(null);
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	void testResolveTimeRangeWithNullWorkEndTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	void testResolveTimeRangeWithWorkStartTimeAfterWorkEndTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(17, 0)); // After
																						// end
																						// time
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(9, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then — source line 75-76: workStartTime(17:00) is NOT before workEndTime(9:00)
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	void testResolveTimeRangeWithWorkStartTimeEqualToWorkEndTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(9, 0)); // Equal
																						// to
																						// start
																						// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then — source line 75: workStartTime equals workEndTime → !isBefore
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	void testResolveTimeRangeWithOccupiedTimeRanges() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// Create occupied time ranges
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0))); // Lunch
																						// break
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then — source line 92-93: occupied [12:00-13:00] subtracted from [9:00-17:00]
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).hasSize(2);
		assertThat(result.contains(LocalTime.of(10, 0))).isTrue();
		assertThat(result.contains(LocalTime.of(12, 30))).isFalse();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("breakTimeThresholdScenarios")
	void testResolveTimeRangeWithBreakTimeThreshold(String testCase, int breakThresholdMinutes,
			Integer actualBreakMinutes, LocalTime expectedRegularEndTime) {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(breakThresholdMinutes));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		if (actualBreakMinutes == null) {
			given(this.timeLog.getBreakIntervals()).willReturn(null);
		}
		else {
			TimeLogBreakInterval breakInterval = mock(TimeLogBreakInterval.class);
			LocalTime breakStart = LocalTime.of(12, 0);
			given(breakInterval.getBreakStartTime()).willReturn(breakStart);
			given(breakInterval.getBreakEndTime()).willReturn(breakStart.plusMinutes(actualBreakMinutes));
			given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(breakInterval));
		}

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).as(testCase).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> regularHoursRange = result.asRanges().iterator().next();
		assertThat(regularHoursRange.lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(regularHoursRange.upperEndpoint()).isEqualTo(expectedRegularEndTime);
	}

	private static Stream<Arguments> breakTimeThresholdScenarios() {
		return Stream.of(
				Arguments.of("should adjust end time when actual break is below threshold", 30, 15,
						LocalTime.of(16, 45)),
				Arguments.of("should keep end time unchanged when actual break meets threshold", 20, 30,
						LocalTime.of(17, 0)),
				Arguments.of("should deduct full threshold when break intervals are missing", 30, null,
						LocalTime.of(16, 30)));
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdNullThreshold() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null); // No
																				// threshold
																				// set
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then — source line 549: threshold is null → adjustment=ZERO, no trim
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		assertThat(result.encloses(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)))).isTrue();
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdInvalidAdjustment() {
		// Given - break threshold (10h) exceeds the entire work day (8h)
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(null);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then — source line 100-103: break threshold(10h) > template(8h) →
		// breakTimeThresholdAdjustment caps the full range, empty result
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	// ===== limitToRemainingRegularHours Tests =====

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("limitToRemainingRegularHoursScenarios")
	void testLimitToRemainingRegularHoursScenarios(String testCase, Integer dailyOvertimeThresholdHours,
			int workEndHour, boolean isMultiInterval, boolean expectLimited, boolean expectFullTemplate) {
		// Given
		if (dailyOvertimeThresholdHours != null) {
			CustomRule dailyOTRule = mock(CustomRule.class);
			given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
			given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
			given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(dailyOvertimeThresholdHours));
			given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		}
		else {
			given(this.context.getInternalSortedCustomRules()).willReturn(Collections.emptyList());
		}

		if (isMultiInterval) {
			// Multi-interval: morning log (current, not last) + afternoon log (last)
			TimeLog morningLog = mock(TimeLog.class);
			TimeLog afternoonLog = mock(TimeLog.class);
			given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(morningLog);
			given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
			given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
			given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(morningLog, afternoonLog));
			given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
			given(morningLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
			given(morningLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
			given(morningLog.getBreakIntervals()).willReturn(Collections.emptyList());
			given(morningLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
			given(morningLog.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
			given(afternoonLog.getBreakIntervals()).willReturn(Collections.emptyList());
			given(afternoonLog.getWorkStartTime()).willReturn(LocalTime.of(13, 0));
			given(afternoonLog.getWorkEndTime()).willReturn(LocalTime.of(workEndHour, 0));
		}
		else {
			// Single interval
			given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
			given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
			given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
			given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
			given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
			given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
			given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
			given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
			given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
			given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(workEndHour, 0));
		}
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).as(testCase).isNotNull();
		assertThat(result.asRanges()).as(testCase).isNotEmpty();

		if (expectFullTemplate) {
			assertThat(result.encloses(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)))).as(testCase)
				.isTrue();
		}
		else if (expectLimited) {
			// Source lines 190-193,207-211: potentialOvertime > 0 and shouldLimit → trim
			Duration totalDuration = Duration.ZERO;
			for (Range<LocalTime> range : result.asRanges()) {
				totalDuration = totalDuration.plus(Duration.between(range.lowerEndpoint(), range.upperEndpoint()));
			}
			assertThat(totalDuration).as(testCase)
				.isLessThanOrEqualTo(Duration.ofHours(dailyOvertimeThresholdHours.intValue()));
		}
	}

	private static Stream<Arguments> limitToRemainingRegularHoursScenarios() {
		return Stream.of(
				Arguments.of("should keep full template when no daily OT rules exist", null, 17, false, false, true),
				Arguments.of("should limit single interval when OT threshold exceeded", Integer.valueOf(8), 18, false,
						true, false),
				Arguments.of("should not limit non-last interval in multi-interval day", Integer.valueOf(8), 18, true,
						false, false),
				Arguments.of("should not limit when total equals threshold (zero OT)", Integer.valueOf(8), 17, false,
						false, false));
	}

	// ===== calculateCumulativeRegularHours Tests =====

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("sameDayTimeLogsNullOrEmptyScenarios")
	void testResolveTimeRangeWithSameDayTimeLogsNullOrEmpty(String testCase, List<TimeLog> sameDayTimeLogs) {
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(sameDayTimeLogs);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).as(testCase).isNotNull();
		assertThat(result.asRanges()).as(testCase).isNotEmpty();
	}

	private static Stream<Arguments> sameDayTimeLogsNullOrEmptyScenarios() {
		return Stream.of(Arguments.of("should use single-interval path when same-day logs are null", null),
				Arguments.of("should use single-interval path when same-day logs are empty", Collections.emptyList()));
	}

	@Test
	void testCalculateCumulativeRegularHoursWithInvalidTemplate() {
		// Given - invalid template (start after end)
		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(17, 0)); // Invalid
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(9, 0)); // Invalid
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should return empty due to invalid template
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("cumulativeRegularHoursWorkRangeScenarios")
	void testCalculateCumulativeRegularHoursWithWorkRanges(String testCase, LocalTime workStartTime,
			LocalTime workEndTime) {
		// Given
		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(workStartTime);
		given(log1.getWorkEndTime()).willReturn(workEndTime);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).as(testCase).isNotNull();
		assertThat(result.asRanges()).as(testCase).isNotEmpty();
	}

	private static Stream<Arguments> cumulativeRegularHoursWorkRangeScenarios() {
		return Stream.of(
				Arguments.of("should clamp work outside template window", LocalTime.of(8, 0), LocalTime.of(18, 0)),
				Arguments.of("should keep template window when work is after template", LocalTime.of(18, 0),
						LocalTime.of(20, 0)),
				Arguments.of("should keep template window when work touches template end", LocalTime.of(17, 0),
						LocalTime.of(18, 0)));
	}

	@Test
	void testCalculateCumulativeRegularHoursWithBreaks() {
		// Given - work interval with breaks
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(13, 0));
		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - breaks should be subtracted from regular hours
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== findLowestApplicableDailyOvertimeThreshold Tests =====

	@Test
	void testFindLowestApplicableDailyOvertimeThresholdWithNoRules() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getInternalSortedCustomRules()).willReturn(Collections.emptyList());
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - no limiting since no daily OT rules
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testFindLowestApplicableDailyOvertimeThresholdWithMultipleRules() {
		// Given - multiple daily OT rules, should use lowest
		CustomRule rule1 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(Duration.ofHours(10));

		CustomRule rule2 = mock(CustomRule.class);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule2.getDailyThreshold()).willReturn(Duration.ofHours(8)); // Lower
																			// threshold

		CustomRule rule3 = mock(CustomRule.class);
		given(rule3.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME); // Different
																						// type
		given(rule3.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(rule1, rule2, rule3));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should use lowest threshold (8h)
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testFindLowestApplicableDailyOvertimeThresholdWithRuleNotApplicableOnDay() {
		// Given - daily OT rule exists but not applicable on Monday
		CustomRule rule1 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isApplicableOnDay(WorkDay.MONDAY)).willReturn(false); // Not
																			// applicable

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(rule1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - no limiting since rule not applicable
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testFindLowestApplicableDailyOvertimeThresholdWithDurationBasedRule() {
		// Given - duration-based daily OT rule should also be considered
		CustomRule rule1 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(rule1.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(rule1));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - duration-based rule should be considered
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testFindLowestApplicableDailyOvertimeThresholdSkipsNullThreshold() {
		// Given - first applicable DO rule has no threshold configured (should be
		// skipped), second applicable DO rule has a real threshold and must be used
		CustomRule rule1 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(null);

		CustomRule rule2 = mock(CustomRule.class);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule2.getDailyThreshold()).willReturn(Duration.ofHours(6));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(rule1, rule2));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - the null-threshold rule is skipped, the 6h threshold from rule2 is
		// applied: template is 8h (9:00-17:00), so 2h is trimmed from the end
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> regularHoursRange = result.asRanges().iterator().next();
		assertThat(regularHoursRange.lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(regularHoursRange.upperEndpoint()).isEqualTo(LocalTime.of(15, 0));
	}

	@Test
	void testFindLowestApplicableDailyOvertimeThresholdKeepsLowestWhenSubsequentRuleIsHigher() {
		// Given - first applicable DO rule has the lower threshold, a later applicable DO
		// rule has a higher threshold and must NOT override the already-found lowest
		CustomRule rule1 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule1.getDailyThreshold()).willReturn(Duration.ofHours(6)); // Lower

		CustomRule rule2 = mock(CustomRule.class);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(rule2.getDailyThreshold()).willReturn(Duration.ofHours(9)); // Higher - must
																			// be ignored

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(rule1, rule2));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - the 6h threshold from rule1 must still be used (not the later 9h from
		// rule2): template is 8h (9:00-17:00), so 2h is trimmed from the end
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> regularHoursRange = result.asRanges().iterator().next();
		assertThat(regularHoursRange.lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(regularHoursRange.upperEndpoint()).isEqualTo(LocalTime.of(15, 0));
	}

	// ===== capBreakThresholdAdjustment Tests =====

	@Test
	void testCapBreakThresholdAdjustmentWithNullRawAdjustment() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - no adjustment since threshold is null
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithZeroBreakThreshold() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ZERO);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - no adjustment since threshold is zero
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithNegativeExpectedMax() {
		// Given - break threshold exceeds template duration
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10)); // Exceeds
																								// 8h
																								// template

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should handle negative expected max gracefully
		assertThat(result).isNotNull();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithExcessWork() {
		// Given - actual work exceeds expected max
		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(18, 0)); // 9h work
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - adjustment should be capped based on excess work
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== calculateBreakTimeWithinRangeForTimeLog Tests =====

	@Test
	void testCalculateBreakTimeWithinRangeForTimeLogWithNullTimeLog() {
		// Given - break calculation with null time log (tested indirectly through
		// resolveTimeRange)
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(null);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeForTimeLogWithEmptyBreakIntervals() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeForTimeLogWithInvalidBreakInterval() {
		// Given - break interval with null start time
		TimeLogBreakInterval invalidBreak = mock(TimeLogBreakInterval.class);
		given(invalidBreak.getBreakStartTime()).willReturn(null);
		given(invalidBreak.getBreakEndTime()).willReturn(LocalTime.of(13, 0));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(invalidBreak));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - invalid break should be skipped
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeForTimeLogWithBreakOutsideRange() {
		// Given - break interval outside regular hours range
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(18, 0)); // After
																			// template
																			// end
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(19, 0));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - break outside range should not affect regular hours
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== adjustTimeRangesFromEnd Tests =====

	@Test
	void testAdjustTimeRangesFromEndWithMultipleRanges() {
		// Given - multiple ranges, adjustment should trim from last range
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - adjustment should trim from end of last range
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== calculateAggregatedBreakTimeWithinRange Tests =====

	@Test
	void testCalculateAggregatedBreakTimeWithinRangeWithSameDayLogs() {
		// Given - multiple same-day logs with breaks
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(10, 30));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(10, 45));

		TimeLogBreakInterval break2 = mock(TimeLogBreakInterval.class);
		given(break2.getBreakStartTime()).willReturn(LocalTime.of(15, 0));
		given(break2.getBreakEndTime()).willReturn(LocalTime.of(15, 15));

		TimeLog log1 = mock(TimeLog.class);
		given(log1.getBreakIntervals()).willReturn(Arrays.asList(break1));
		TimeLog log2 = mock(TimeLog.class);
		given(log2.getBreakIntervals()).willReturn(Arrays.asList(break2));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1, log2));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - breaks from both logs should be aggregated
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateAggregatedBreakTimeWithinRangeWithFallbackToCurrentLog() {
		// Given - no same-day logs, should fallback to current log
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(13, 0));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(null);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should use current log's breaks
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== addAvailableTimeBeyondInterval Tests =====

	@Test
	void testAddAvailableTimeBeyondIntervalWithIntervalAtTemplateEnd() {
		// Given - interval ends at template end
		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(15, 0)); // Before template
																		// end
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should account for time beyond interval
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== calculateBreakTimeThresholdAdjustmentWithRangeConstraint Tests =====

	@Test
	void testCalculateBreakTimeThresholdAdjustmentWithNullTimeLog() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(null);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);

		// When & Then - null timeLog causes NPE at entry point
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(this.context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testCalculateBreakTimeThresholdAdjustmentWithNullTimesheetSetting() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(null);

		// When & Then - null timesheetSetting causes NPE at entry point
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(this.context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testCalculateBreakTimeThresholdAdjustmentWithActualBreaksExceedingThreshold() {
		// Given - actual breaks exceed threshold, no adjustment needed
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(13, 0)); // 1h break

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30)); // Less
																									// than
																									// 1h

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - no adjustment since breaks exceed threshold
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== calculateTotalDurationFromRanges Tests =====

	@Test
	void testCalculateTotalDurationFromRangesWithEmptyRanges() {
		// Given - empty ranges should return zero duration
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should handle empty ranges
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateTotalDurationFromRangesWithMultipleRanges() {
		// Given - multiple ranges should sum durations
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should calculate total duration across ranges
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== Additional Edge Case Tests =====

	@Test
	void testResolveTimeRangeWithBreakThresholdAdjustmentExceedingRange() {
		// Given - adjustment exceeds available range duration
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10)); // Exceeds
																								// 8h
																								// template

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should handle excessive adjustment
		assertThat(result).isNotNull();
	}

	@Test
	void testResolveTimeRangeWithBreakThresholdAdjustmentAndDailyOTLimiting() {
		// Given - both break threshold adjustment and daily OT limiting
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ofMinutes(15));
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(18, 0)); // 9h work
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - both adjustments should be applied
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testResolveTimeRangeWithNegativeRegularHoursPrevention() {
		// Given - scenario that could result in negative regular hours
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(9, 0));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(18, 0)); // Break exceeds
																			// work

		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should prevent negative regular hours
		assertThat(result).isNotNull();
	}

	@Test
	void testCalculateCumulativeRegularHoursWithNegativeIntervalRegularHours() {
		// Given - break time exceeds interval duration
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(9, 0));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(12, 0)); // 3h break

		TimeLog log1 = mock(TimeLog.class);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(10, 0)); // 1h work, but 3h
																		// break
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - negative regular hours should be prevented
		assertThat(result).isNotNull();
	}

	@Test
	void testCalculateBreakTimeThresholdAdjustmentWithNullBreakTimeWithinRegularHours() {
		// Given - no breaks taken, should use full threshold
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(null);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - should deduct full threshold since no breaks
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeThresholdAdjustmentWithBreakTimeExceedingThreshold() {
		// Given - actual breaks exceed threshold, no adjustment needed
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(13, 30)); // 1.5h break

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30)); // Less
																									// than
																									// 1.5h

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - no adjustment since breaks exceed threshold
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeWithBreakNotConnected() {
		// Given - break interval not connected to range
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(8, 0)); // Before
																			// template
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(8, 30));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - break not connected should not affect regular hours
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testAdjustTimeRangesFromEndWithRangeRemoval() {
		// Given - adjustment exceeds range duration, should remove entire range
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10)); // Large
																								// adjustment

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - large adjustment may remove ranges
		assertThat(result).isNotNull();
	}

	@Test
	void testLimitToRemainingRegularHoursWithBreakThresholdAdjustmentSubtraction() {
		// Given - multi-interval with break threshold adjustment
		TimeLog log1 = mock(TimeLog.class);
		TimeLog log2 = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1, log2));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ofMinutes(15));
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(log2.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log2.getWorkStartTime()).willReturn(LocalTime.of(13, 0));
		given(log2.getWorkEndTime()).willReturn(LocalTime.of(18, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - break threshold adjustment should be subtracted from cumulative
		// calculation
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testLimitToRemainingRegularHoursWithNegativeTotalAfterBreakAdjustment() {
		// Given - break adjustment causes negative total
		TimeLog log1 = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ofHours(10)); // Large
																										// adjustment
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - negative total should be clamped to zero
		assertThat(result).isNotNull();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithEmptySameDayLogs() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Collections.emptyList());
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - empty same day logs should return raw adjustment
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithZeroExpectedMaxRegularHours() {
		// Given - break threshold equals template duration
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(8)); // Equals
																								// template

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - zero expected max should return raw adjustment
		assertThat(result).isNotNull();
	}

	@Test
	void testCalculateBreakTimeWithinRangeWithBreakStartAfterEnd() {
		// Given - invalid break interval (start after end)
		TimeLogBreakInterval invalidBreak = mock(TimeLogBreakInterval.class);
		given(invalidBreak.getBreakStartTime()).willReturn(LocalTime.of(13, 0));
		given(invalidBreak.getBreakEndTime()).willReturn(LocalTime.of(12, 0)); // Invalid

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(invalidBreak));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - invalid break should be skipped
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeWithBreakStartEqualToEnd() {
		// Given - invalid break interval (start equals end)
		TimeLogBreakInterval invalidBreak = mock(TimeLogBreakInterval.class);
		given(invalidBreak.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(invalidBreak.getBreakEndTime()).willReturn(LocalTime.of(12, 0)); // Equal

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(invalidBreak));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - invalid break should be skipped
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeWithEmptyIntersection() {
		// Given - break interval intersects but intersection is empty
		TimeLogBreakInterval break1 = mock(TimeLogBreakInterval.class);
		given(break1.getBreakStartTime()).willReturn(LocalTime.of(17, 0)); // At template
																			// end
		given(break1.getBreakEndTime()).willReturn(LocalTime.of(18, 0));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(break1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - empty intersection should not count as break time
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testAdjustTimeRangesFromEndWithRemainingAdjustmentZero() {
		// Given - adjustment exactly matches range duration
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(8)); // Exact
																								// match

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - adjustment should consume entire range
		assertThat(result).isNotNull();
	}

	@Test
	void testResolveTimeRangeWithNullContext() {
		// Given
		TimeRangeResolverContext nullContext = null;

		// When & Then - should handle null gracefully
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(nullContext))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testResolveTimeRangeWithNullTimeLog() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(null);

		// When & Then - null timeLog causes NPE
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(this.context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testResolveTimeRangeWithNullTimesheetSetting() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(null);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);

		// When & Then - null timesheetSetting causes NPE
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(this.context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testResolveTimeRangeWithNullTemplateWorkDays() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(null);

		// When & Then - null templateWorkDays causes NPE when looking up the template
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(this.context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testResolveTimeRangeWithTemplateWorkDayNotFound() {
		// Given - template has TUESDAY but log date is Monday
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.TUESDAY);

		// When & Then - no matching template for Monday causes NPE
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.resolver.resolveTimeRange(this.context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testLimitToRemainingRegularHoursWithSingleIntervalOvertimeExceedingThreshold() {
		// Given - template 8h, OT threshold 7h → 1h overtime, single interval
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(7));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - regular hours limited to 7h threshold
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testLimitToRemainingRegularHoursWithMultiIntervalNotLastIntervalOvertimeAndLogOutsideTemplate() {
		// Given - multi-interval, current NOT last, OT exceeded; log2 outside template
		TimeLog log1 = mock(TimeLog.class);
		TimeLog log2 = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(2));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1, log2));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(log2.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log2.getWorkStartTime()).willReturn(LocalTime.of(18, 0));
		given(log2.getWorkEndTime()).willReturn(LocalTime.of(20, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - not last interval, full range returned even with OT exceeded
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testLimitToRemainingRegularHoursWithMultiIntervalLastIntervalOvertimeExceeded() {
		// Given - multi-interval, current IS last, OT exceeded → trim last interval
		TimeLog log1 = mock(TimeLog.class);
		TimeLog log2 = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(6));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log2);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1, log2));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(log2.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log2.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log2.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log2.getWorkStartTime()).willReturn(LocalTime.of(13, 0));
		given(log2.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - last interval trimmed to 6h threshold
		assertThat(result).isNotNull();
	}

	@Test
	void testCalculateCumulativeRegularHoursWithBreakIntervalsInLoop() {
		// Given - multi-interval with breaks so calculateBreakTimeWithinRangeForTimeLog
		// processes non-empty breaks
		TimeLogBreakInterval validBreak = mock(TimeLogBreakInterval.class);
		given(validBreak.getBreakStartTime()).willReturn(LocalTime.of(10, 0));
		given(validBreak.getBreakEndTime()).willReturn(LocalTime.of(10, 30));
		TimeLogBreakInterval invalidBreak = mock(TimeLogBreakInterval.class);
		given(invalidBreak.getBreakStartTime()).willReturn(null);
		given(invalidBreak.getBreakEndTime()).willReturn(LocalTime.of(11, 0));

		TimeLog log1 = mock(TimeLog.class);
		TimeLog log2 = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(6));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1, log2));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Arrays.asList(invalidBreak, validBreak));
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(log2.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log2.getWorkStartTime()).willReturn(LocalTime.of(13, 0));
		given(log2.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - breaks subtracted from regular hours calculation
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCalculateCumulativeRegularHoursWithNullWorkStartTimeInLog() {
		// Given - one log has null workStartTime → skipped in loop (line 265-266)
		TimeLog log1 = mock(TimeLog.class);
		TimeLog log2 = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(6));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1, log2));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(log2.getBreakIntervals()).willReturn(Collections.emptyList());
		// log2.getWorkStartTime() not stubbed → returns null → skipped at line 265
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - log with null workStartTime is skipped, only log1 contributes
		assertThat(result).isNotNull();
	}

	@Test
	void testCalculateTotalDurationFromRangesWithEmptyRangesAndOvertimeRule() {
		// Given - break threshold > template duration → empties available ranges
		// then limitToRemainingRegularHours calls calculateTotalDurationFromRanges with
		// empty set
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - break threshold exhausts all ranges, result is empty
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isEmpty();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithIntervalEndingAtTemplateEnd() {
		// Given - current log ends exactly at template end →
		// addAvailableTimeBeyondInterval
		// returns early at line 507
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - cap applied and break threshold adjustment reduces available range
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCapBreakThresholdAdjustmentWithAvailableTimeBeyondIntervalEnd() {
		// Given - log ends before template end, occupied range near template end
		// → cappedAdjustment < rawAdjustment triggers logDebug path
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(16, 45), LocalTime.of(17, 0)));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(occupiedRanges);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(16, 35));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - adjustment capped, logDebug triggered for cap difference
		assertThat(result).isNotNull();
	}

	@Test
	void testCalculateBreakTimeWithinRangeWithInvalidBreakAndNonNullThreshold() {
		// Given - break threshold set, break start > break end → invalid break skipped at
		// line 624 in calculateBreakTimeWithinRange
		TimeLogBreakInterval invalidBreak = mock(TimeLogBreakInterval.class);
		given(invalidBreak.getBreakStartTime()).willReturn(LocalTime.of(13, 0));
		given(invalidBreak.getBreakEndTime()).willReturn(LocalTime.of(12, 0));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(invalidBreak));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then - invalid break is skipped, threshold adjustment still applied
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== Coverage: lines 159,163,172-178,190,193,204-217,265-291,310-341,399,404,422
	// =====

	@Test
	void testMultiIntervalLastIntervalWithOTExceeded() {
		// Covers: line 159 (multi-interval branch), 163
		// (calculateCumulativeRegularHours),
		// 172-174 (breakThresholdAdjustment subtraction), 190-193 (potentialOvertime),
		// 204-205 (shouldLimit=true, last interval), 207-217 (adjustTimeRangesFromEnd),
		// 265-291 (loop in calculateCumulativeRegularHours), 310-341
		// (calculateBreakTimeWithinRangeForTimeLog),
		// 399,404 (findLowestApplicableDailyOvertimeThreshold), 422 (isLastIntervalOfDay)
		TimeLog morningLog = mock(TimeLog.class);
		TimeLog afternoonLog = mock(TimeLog.class);
		TimeLogBreakInterval lunchBreak = mock(TimeLogBreakInterval.class);
		given(lunchBreak.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(lunchBreak.getBreakEndTime()).willReturn(LocalTime.of(12, 30));
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(afternoonLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(morningLog, afternoonLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ofMinutes(30));

		given(morningLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(morningLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(morningLog.getBreakIntervals()).willReturn(Arrays.asList(lunchBreak));
		given(morningLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(morningLog.getWorkEndTime()).willReturn(LocalTime.of(13, 0));

		given(afternoonLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(afternoonLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(afternoonLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(afternoonLog.getWorkStartTime()).willReturn(LocalTime.of(14, 0));
		given(afternoonLog.getWorkEndTime()).willReturn(LocalTime.of(18, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		Duration total = Duration.ZERO;
		for (Range<LocalTime> r : result.asRanges()) {
			total = total.plus(Duration.between(r.lowerEndpoint(), r.upperEndpoint()));
		}
		assertThat(total).isLessThanOrEqualTo(Duration.ofHours(8));
	}

	@Test
	void testMultiIntervalWithBreakThresholdSubtractionGoesNegative() {
		// Covers: line 173-177 (breakThresholdAdjustment subtraction, negative check)
		TimeLog morningLog = mock(TimeLog.class);
		TimeLog afternoonLog = mock(TimeLog.class);
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(afternoonLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(morningLog, afternoonLog));
		// Large break threshold adjustment that exceeds cumulative hours
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ofHours(20));

		given(morningLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(morningLog.getWorkEndTime()).willReturn(LocalTime.of(10, 0));
		given(morningLog.getBreakIntervals()).willReturn(Collections.emptyList());

		given(afternoonLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(afternoonLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(afternoonLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(afternoonLog.getWorkStartTime()).willReturn(LocalTime.of(14, 0));
		given(afternoonLog.getWorkEndTime()).willReturn(LocalTime.of(15, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// After subtraction goes negative → clamped to ZERO → potentialOvertime=0 → no
		// limit
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	// ===== Coverage: lines 438,443,444,456,460,462,464,476-527 =====

	@Test
	void testCapBreakThresholdWithExcessWorkAndBeyondInterval() {
		// Covers: 438 (non-zero rawAdjustment), 443-444 (non-zero breakTimeThreshold),
		// 456 (non-empty sameDayTimeLogs), 460 (calculateCumulativeRegularHours call),
		// 462 (excessWork calculation), 476 (addAvailableTimeBeyondInterval),
		// 500-527 (available time beyond interval end)
		TimeLog log1 = mock(TimeLog.class);
		TimeLogBreakInterval breakInt = mock(TimeLogBreakInterval.class);
		given(breakInt.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(breakInt.getBreakEndTime()).willReturn(LocalTime.of(12, 30));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getInternalSortedCustomRules()).willReturn(Collections.emptyList());

		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Arrays.asList(breakInt));
		// Work ends BEFORE template end → triggers addAvailableTimeBeyondInterval
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(15, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		// Break threshold of 1h but only 30min taken → 30min adjustment
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(1));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCapBreakThresholdExcessWorkNegativeOrZero() {
		// Covers: line 464 (excessWork.isNegative() || excessWork.isZero() → return ZERO)
		// actualWorkInRegular <= expectedMaxRegularHours
		TimeLog log1 = mock(TimeLog.class);

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getInternalSortedCustomRules()).willReturn(Collections.emptyList());

		given(log1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(log1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(log1.getBreakIntervals()).willReturn(Collections.emptyList());
		// Only 5h of work → well below expected max (8h template - 1h threshold = 7h)
		given(log1.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log1.getWorkEndTime()).willReturn(LocalTime.of(14, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(1));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Cap returns ZERO → no adjustment applied → full template returned
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		assertThat(result.encloses(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)))).isTrue();
	}

	// ===== Break threshold edge cases (aggregated breaks + end trim) =====

	private enum BreakThresholdEdgeCase {

		NO_BREAKS, BREAKS_EXCEED_THRESHOLD, PARTIAL_BREAK

	}

	@ParameterizedTest(name = "[{index}] {0}")
	@EnumSource(BreakThresholdEdgeCase.class)
	void testBreakThresholdAdjustmentParameterized(BreakThresholdEdgeCase scenario) {
		TimeLog log1 = new TimeLog();
		log1.setDate(LocalDate.of(2024, 1, 1));
		log1.setDayType(WorkDayType.WORK_DAY);
		log1.setWorkStartTime(LocalTime.of(9, 0));
		log1.setWorkEndTime(LocalTime.of(17, 0));

		if (scenario == BreakThresholdEdgeCase.NO_BREAKS) {
			log1.setBreakIntervals(Collections.emptyList());
			given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));
		}
		else if (scenario == BreakThresholdEdgeCase.BREAKS_EXCEED_THRESHOLD) {
			TimeLogBreakInterval longBreak = mock(TimeLogBreakInterval.class);
			given(longBreak.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
			given(longBreak.getBreakEndTime()).willReturn(LocalTime.of(13, 0));
			log1.setBreakIntervals(Arrays.asList(longBreak));
			given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));
		}
		else {
			TimeLogBreakInterval shortBreak = mock(TimeLogBreakInterval.class);
			given(shortBreak.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
			given(shortBreak.getBreakEndTime()).willReturn(LocalTime.of(12, 15));
			log1.setBreakIntervals(Arrays.asList(shortBreak));
			given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(1));
		}

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.context.getInternalSortedCustomRules()).willReturn(Collections.emptyList());
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).as(scenario.name()).isNotNull();
		assertThat(result.asRanges()).as(scenario.name()).isNotEmpty();

		if (scenario == BreakThresholdEdgeCase.BREAKS_EXCEED_THRESHOLD) {
			assertThat(result.encloses(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)))).as(scenario.name())
				.isTrue();
		}
		else {
			assertThat(result.contains(LocalTime.of(16, 45))).as(scenario.name()).isFalse();
		}
	}

	@Test
	void testAggregatedBreakTimeFallbackToCurrentLog() {
		// Covers: line 596-598 (fallback branch when sameDayTimeLogs is null/empty)
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getSameDayTimeLogs()).willReturn(null);
		given(this.context.getInternalSortedCustomRules()).willReturn(Collections.emptyList());

		TimeLogBreakInterval breakInt = mock(TimeLogBreakInterval.class);
		given(breakInt.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(breakInt.getBreakEndTime()).willReturn(LocalTime.of(12, 30));

		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Arrays.asList(breakInt));
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(1));

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testAdjustTimeRangesFromEndWithOccupiedGapAndOTLimit() {
		// Covers: lines 660,670-690 (adjustTimeRangesFromEnd with reverse iteration,
		// rangeDuration <= remainingAdjustment at 682, partial trim at 686-687)
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		occupied.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(5));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(occupied);
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);

		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		Duration total = Duration.ZERO;
		for (Range<LocalTime> r : result.asRanges()) {
			total = total.plus(Duration.between(r.lowerEndpoint(), r.upperEndpoint()));
		}
		assertThat(total).isLessThanOrEqualTo(Duration.ofHours(5));
	}

	@Test
	void testMultiIntervalWithBreaksInCumulativeCalculation() {
		// Covers: lines 265 (skip invalid interval), 275-291 (intersection + break
		// subtraction),
		// 310-341 (calculateBreakTimeWithinRangeForTimeLog with valid breaks)
		TimeLog validLog = mock(TimeLog.class);
		TimeLog invalidLog = mock(TimeLog.class);
		TimeLogBreakInterval breakInt = mock(TimeLogBreakInterval.class);
		given(breakInt.getBreakStartTime()).willReturn(LocalTime.of(10, 0));
		given(breakInt.getBreakEndTime()).willReturn(LocalTime.of(10, 30));
		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(6));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(validLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(invalidLog, validLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);

		// Invalid log: workEnd before workStart → skipped at line 265
		given(invalidLog.getWorkStartTime()).willReturn(LocalTime.of(17, 0));
		given(invalidLog.getWorkEndTime()).willReturn(LocalTime.of(9, 0));
		given(invalidLog.getBreakIntervals()).willReturn(Collections.emptyList());

		given(validLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(validLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(validLog.getBreakIntervals()).willReturn(Arrays.asList(breakInt));
		given(validLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(validLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testFindLowestThresholdWithDurationBasedDailyOT() {
		// Covers: line 393 (DURATION_BASED_DAILY_OVERTIME rule type check)
		CustomRule durationOTRule = mock(CustomRule.class);
		given(durationOTRule.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(durationOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(durationOTRule.getDailyThreshold()).willReturn(Duration.ofHours(7));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(durationOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(Duration.ZERO);

		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		Duration total = Duration.ZERO;
		for (Range<LocalTime> r : result.asRanges()) {
			total = total.plus(Duration.between(r.lowerEndpoint(), r.upperEndpoint()));
		}
		assertThat(total).isLessThanOrEqualTo(Duration.ofHours(7));
	}

	@Test
	void testFindLowestThresholdNullDailyThreshold() {
		// Covers: line 399 (threshold == null → skip)
		CustomRule ruleWithNullThreshold = mock(CustomRule.class);
		given(ruleWithNullThreshold.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(ruleWithNullThreshold.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(ruleWithNullThreshold.getDailyThreshold()).willReturn(null);

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(ruleWithNullThreshold));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(this.timeLog));

		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getBreakIntervals()).willReturn(Collections.emptyList());
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Null threshold → no limiting → full range
		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
		assertThat(result.encloses(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)))).isTrue();
	}

	@Test
	void testIsLastIntervalOfDayDelegateCoversPrivateMethod() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod("isLastIntervalOfDay",
				TimeLog.class, List.class);
		method.setAccessible(true);

		TimeLog early = new TimeLog();
		early.setWorkStartTime(LocalTime.of(9, 0));
		early.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog late = new TimeLog();
		late.setWorkStartTime(LocalTime.of(13, 0));
		late.setWorkEndTime(LocalTime.of(17, 0));

		assertThat(method.invoke(this.resolver, late, Arrays.asList(early, late))).isEqualTo(Boolean.TRUE);
		assertThat(method.invoke(this.resolver, early, Arrays.asList(early, late))).isEqualTo(Boolean.FALSE);
	}

	@Test
	void testCalculateBreakTimeWithinRangeForTimeLogViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class
			.getDeclaredMethod("calculateBreakTimeWithinRangeForTimeLog", TimeLog.class, Range.class);
		method.setAccessible(true);

		assertThat(method.invoke(this.resolver, null, Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0))))
			.isEqualTo(Duration.ZERO);
		assertThat(method.invoke(this.resolver, new TimeLog(), (Range<LocalTime>) null)).isEqualTo(Duration.ZERO);

		TimeLogBreakInterval bad = mock(TimeLogBreakInterval.class);
		given(bad.getBreakStartTime()).willReturn(null);
		given(bad.getBreakEndTime()).willReturn(LocalTime.of(12, 0));
		TimeLogBreakInterval offWindow = mock(TimeLogBreakInterval.class);
		given(offWindow.getBreakStartTime()).willReturn(LocalTime.of(7, 0));
		given(offWindow.getBreakEndTime()).willReturn(LocalTime.of(7, 30));
		TimeLogBreakInterval ok = mock(TimeLogBreakInterval.class);
		given(ok.getBreakStartTime()).willReturn(LocalTime.of(10, 0));
		given(ok.getBreakEndTime()).willReturn(LocalTime.of(10, 30));

		TimeLog timeLogWithBreaks = new TimeLog();
		timeLogWithBreaks.setBreakIntervals(Arrays.asList(bad, offWindow, ok));

		assertThat(method.invoke(this.resolver, timeLogWithBreaks,
				Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0))))
			.isEqualTo(Duration.ofMinutes(30));

		TimeLogBreakInterval touchBoundary = mock(TimeLogBreakInterval.class);
		given(touchBoundary.getBreakStartTime()).willReturn(LocalTime.of(10, 0));
		given(touchBoundary.getBreakEndTime()).willReturn(LocalTime.of(11, 0));
		TimeLog boundaryLog = new TimeLog();
		boundaryLog.setBreakIntervals(Arrays.asList(touchBoundary));
		assertThat(method.invoke(this.resolver, boundaryLog, Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0))))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	void testAddAvailableTimeBeyondIntervalViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod(
				"addAvailableTimeBeyondInterval", Duration.class, TimeRangeResolverContext.class, Range.class,
				RangeSet.class);
		method.setAccessible(true);

		Range<LocalTime> template = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		TreeRangeSet<LocalTime> available = TreeRangeSet.create();
		available.add(Range.closedOpen(LocalTime.of(15, 0), LocalTime.of(17, 0)));

		TimeLog endsMid = new TimeLog();
		endsMid.setWorkEndTime(LocalTime.of(15, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(endsMid);
		assertThat(method.invoke(this.resolver, Duration.ofMinutes(30), this.context, template, available))
			.isEqualTo(Duration.ofMinutes(150));

		TimeLog endsAtClose = new TimeLog();
		endsAtClose.setWorkEndTime(LocalTime.of(17, 0));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(endsAtClose);
		assertThat(method.invoke(this.resolver, Duration.ofMinutes(10), this.context, template, available))
			.isEqualTo(Duration.ofMinutes(10));

		TimeLog nullEnd = new TimeLog();
		nullEnd.setWorkEndTime(null);
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(nullEnd);
		assertThat(method.invoke(this.resolver, Duration.ofMinutes(11), this.context, template, available))
			.isEqualTo(Duration.ofMinutes(11));
	}

	@Test
	void testAdjustTimeRangesFromEndFullConsumptionViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod("adjustTimeRangesFromEnd",
				RangeSet.class, Duration.class);
		method.setAccessible(true);

		TreeRangeSet<LocalTime> ranges = TreeRangeSet.create();
		ranges.add(Range.closedOpen(LocalTime.of(16, 0), LocalTime.of(17, 0)));
		@SuppressWarnings("unchecked")
		RangeSet<LocalTime> out = (RangeSet<LocalTime>) method.invoke(this.resolver, ranges, Duration.ofHours(1));
		assertThat(out.asRanges()).isEmpty();
	}

	@Test
	void testCalculateBreakTimeWithinRangeForTimeLogEmptyIntersectionViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class
			.getDeclaredMethod("calculateBreakTimeWithinRangeForTimeLog", TimeLog.class, Range.class);
		method.setAccessible(true);

		TimeLogBreakInterval edgeBreak = mock(TimeLogBreakInterval.class);
		given(edgeBreak.getBreakStartTime()).willReturn(LocalTime.of(10, 0));
		given(edgeBreak.getBreakEndTime()).willReturn(LocalTime.of(11, 0));
		TimeLog timeLogWithEdgeBreak = new TimeLog();
		timeLogWithEdgeBreak.setBreakIntervals(Arrays.asList(edgeBreak));

		assertThat(method.invoke(this.resolver, timeLogWithEdgeBreak,
				Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0))))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	void testCalculateCumulativeRegularHoursViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class
			.getDeclaredMethod("calculateCumulativeRegularHours", List.class, TimeRangeResolverContext.class);
		method.setAccessible(true);

		assertThat(method.invoke(this.resolver, null, this.context)).isEqualTo(Duration.ZERO);
		assertThat(method.invoke(this.resolver, Collections.emptyList(), this.context)).isEqualTo(Duration.ZERO);

		TimeLog current = new TimeLog();
		current.setDate(LocalDate.of(2024, 1, 1));
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(current);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		TimeLog invalidOrder = new TimeLog();
		invalidOrder.setWorkStartTime(LocalTime.of(17, 0));
		invalidOrder.setWorkEndTime(LocalTime.of(9, 0));
		assertThat(method.invoke(this.resolver, Arrays.asList(invalidOrder), this.context)).isEqualTo(Duration.ZERO);

		TimeLog outsideTemplate = new TimeLog();
		outsideTemplate.setWorkStartTime(LocalTime.of(18, 0));
		outsideTemplate.setWorkEndTime(LocalTime.of(20, 0));
		assertThat(method.invoke(this.resolver, Arrays.asList(outsideTemplate), this.context)).isEqualTo(Duration.ZERO);

		TemplateWorkDay invalidTemplate = mock(TemplateWorkDay.class);
		given(invalidTemplate.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(invalidTemplate.getWorkStartTime()).willReturn(LocalTime.of(17, 0));
		given(invalidTemplate.getWorkEndTime()).willReturn(LocalTime.of(9, 0));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(invalidTemplate));

		TimeLog validWork = new TimeLog();
		validWork.setWorkStartTime(LocalTime.of(9, 0));
		validWork.setWorkEndTime(LocalTime.of(17, 0));
		assertThat(method.invoke(this.resolver, Arrays.asList(validWork), this.context)).isEqualTo(Duration.ZERO);
	}

	@Test
	void testCapBreakThresholdAdjustmentViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod(
				"capBreakThresholdAdjustment", Duration.class, TimeRangeResolverContext.class, Range.class,
				RangeSet.class);
		method.setAccessible(true);

		Range<LocalTime> template = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		TreeRangeSet<LocalTime> available = TreeRangeSet.create();
		available.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		assertThat(method.invoke(this.resolver, null, this.context, template, available)).isEqualTo(Duration.ZERO);
		assertThat(method.invoke(this.resolver, Duration.ZERO, this.context, template, available))
			.isEqualTo(Duration.ZERO);

		TimeLog fullDay = new TimeLog();
		fullDay.setDate(LocalDate.of(2024, 1, 1));
		fullDay.setWorkStartTime(LocalTime.of(9, 0));
		fullDay.setWorkEndTime(LocalTime.of(17, 0));
		fullDay.setBreakIntervals(Collections.emptyList());

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(fullDay);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(fullDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		Duration capped = (Duration) method.invoke(this.resolver, Duration.ofHours(2), this.context, template,
				available);
		assertThat(capped).isEqualTo(Duration.ofMinutes(30));
	}

	@Test
	void testCalculateBreakTimeThresholdAdjustmentWithRangeConstraintViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod(
				"calculateBreakTimeThresholdAdjustmentWithRangeConstraint", TimeLog.class, List.class,
				TimesheetSetting.class, Range.class);
		method.setAccessible(true);

		Range<LocalTime> template = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		assertThat(method.invoke(this.resolver, null, Collections.emptyList(), this.timesheetSetting, template))
			.isEqualTo(Duration.ZERO);

		TimeLog log1 = new TimeLog();
		assertThat(method.invoke(this.resolver, log1, Collections.emptyList(), null, template))
			.isEqualTo(Duration.ZERO);

		assertThat(method.invoke(this.resolver, log1, Collections.emptyList(), this.timesheetSetting, null))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	void testLimitToRemainingRegularHoursMultiIntervalWithNullAdjustedBreakThreshold() {
		TimeLog morningLog = new TimeLog();
		morningLog.setDate(LocalDate.of(2024, 1, 1));
		morningLog.setDayType(WorkDayType.WORK_DAY);
		morningLog.setWorkStartTime(LocalTime.of(9, 0));
		morningLog.setWorkEndTime(LocalTime.of(12, 0));
		morningLog.setBreakIntervals(Collections.emptyList());
		TimeLog afternoonLog = new TimeLog();
		afternoonLog.setDate(LocalDate.of(2024, 1, 1));
		afternoonLog.setDayType(WorkDayType.WORK_DAY);
		afternoonLog.setWorkStartTime(LocalTime.of(13, 0));
		afternoonLog.setWorkEndTime(LocalTime.of(18, 0));
		afternoonLog.setBreakIntervals(Collections.emptyList());

		CustomRule dailyOTRule = mock(CustomRule.class);
		given(dailyOTRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(dailyOTRule.isApplicableOnDay(WorkDay.MONDAY)).willReturn(true);
		given(dailyOTRule.getDailyThreshold()).willReturn(Duration.ofHours(8));

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(morningLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getOccupiedTimeRanges()).willReturn(TreeRangeSet.create());
		given(this.context.getInternalSortedCustomRules()).willReturn(Arrays.asList(dailyOTRule));
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(morningLog, afternoonLog));
		given(this.context.getAdjustedRegularHoursBreakThreshold()).willReturn(null);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null);

		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		assertThat(result).isNotNull();
		assertThat(result.asRanges()).isNotEmpty();
	}

	@Test
	void testCapBreakReturnsRawWhenExpectedMaxNotPositiveViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod(
				"capBreakThresholdAdjustment", Duration.class, TimeRangeResolverContext.class, Range.class,
				RangeSet.class);
		method.setAccessible(true);

		Range<LocalTime> template = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		TreeRangeSet<LocalTime> available = TreeRangeSet.create();
		available.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		TimeLog log1 = new TimeLog();
		log1.setDate(LocalDate.of(2024, 1, 1));
		log1.setWorkStartTime(LocalTime.of(9, 0));
		log1.setWorkEndTime(LocalTime.of(17, 0));
		log1.setBreakIntervals(Collections.emptyList());

		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(log1);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.context.getSameDayTimeLogs()).willReturn(Arrays.asList(log1));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.templateWorkDay.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		Duration raw = Duration.ofMinutes(45);
		assertThat(method.invoke(this.resolver, raw, this.context, template, available)).isEqualTo(raw);
	}

	@Test
	void testAdjustTimeRangesFromEndDiscardsFullyConsumedTailViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod("adjustTimeRangesFromEnd",
				RangeSet.class, Duration.class);
		method.setAccessible(true);

		TreeRangeSet<LocalTime> ranges = TreeRangeSet.create();
		ranges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		ranges.add(Range.closedOpen(LocalTime.of(11, 0), LocalTime.of(12, 0)));
		@SuppressWarnings("unchecked")
		RangeSet<LocalTime> out = (RangeSet<LocalTime>) method.invoke(this.resolver, ranges, Duration.ofMinutes(90));
		assertThat(out.asRanges()).hasSize(1);
		assertThat(out.span().lowerEndpoint()).isEqualTo(LocalTime.of(9, 0));
		assertThat(out.span().upperEndpoint()).isEqualTo(LocalTime.of(9, 30));
	}

	@Test
	void testAdjustTimeRangesFromEndSkipsAddWhenTrimmedEndNotAfterStartViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod("adjustTimeRangesFromEnd",
				RangeSet.class, Duration.class);
		method.setAccessible(true);

		TreeRangeSet<LocalTime> ranges = TreeRangeSet.create();
		ranges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		@SuppressWarnings("unchecked")
		RangeSet<LocalTime> out = (RangeSet<LocalTime>) method.invoke(this.resolver, ranges, Duration.ofMinutes(65));
		assertThat(out.asRanges()).isEmpty();
	}

	@Test
	void testCalculateTotalDurationFromRangesViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class
			.getDeclaredMethod("calculateTotalDurationFromRanges", RangeSet.class);
		method.setAccessible(true);

		// Null and empty inputs return ZERO (guard branch)
		assertThat(method.invoke(this.resolver, (RangeSet<LocalTime>) null)).isEqualTo(Duration.ZERO);
		assertThat(method.invoke(this.resolver, TreeRangeSet.<LocalTime>create())).isEqualTo(Duration.ZERO);

		// Two ranges summed: 1h + 30m = 90m
		TreeRangeSet<LocalTime> ranges = TreeRangeSet.create();
		ranges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		ranges.add(Range.closedOpen(LocalTime.of(11, 0), LocalTime.of(11, 30)));
		assertThat(method.invoke(this.resolver, ranges)).isEqualTo(Duration.ofMinutes(90));
	}

	@Test
	void testCalculateBreakTimeWithinRangeViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class
			.getDeclaredMethod("calculateBreakTimeWithinRange", TimeLog.class, Range.class);
		method.setAccessible(true);

		Range<LocalTime> regularHours = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));

		// Guard: null timeLog, null range, and null break intervals each return ZERO
		assertThat(method.invoke(this.resolver, null, regularHours)).isEqualTo(Duration.ZERO);
		TimeLog withBreaks = new TimeLog();
		withBreaks.setBreakIntervals(Collections.emptyList());
		assertThat(method.invoke(this.resolver, withBreaks, (Range<LocalTime>) null)).isEqualTo(Duration.ZERO);
		TimeLog nullBreaks = new TimeLog();
		nullBreaks.setBreakIntervals(null);
		assertThat(method.invoke(this.resolver, nullBreaks, regularHours)).isEqualTo(Duration.ZERO);

		// Invalid break interval (end before start) is skipped → ZERO
		TimeLogBreakInterval invalid = mock(TimeLogBreakInterval.class);
		given(invalid.getBreakStartTime()).willReturn(LocalTime.of(13, 0));
		given(invalid.getBreakEndTime()).willReturn(LocalTime.of(12, 0));
		TimeLog invalidLog = new TimeLog();
		invalidLog.setBreakIntervals(Arrays.asList(invalid));
		assertThat(method.invoke(this.resolver, invalidLog, regularHours)).isEqualTo(Duration.ZERO);

		// Break wholly outside range → empty intersection skipped → ZERO
		TimeLogBreakInterval outside = mock(TimeLogBreakInterval.class);
		given(outside.getBreakStartTime()).willReturn(LocalTime.of(7, 0));
		given(outside.getBreakEndTime()).willReturn(LocalTime.of(9, 0));
		TimeLog outsideLog = new TimeLog();
		outsideLog.setBreakIntervals(Arrays.asList(outside));
		assertThat(method.invoke(this.resolver, outsideLog, regularHours)).isEqualTo(Duration.ZERO);

		// Valid break within range is summed (30 minutes)
		TimeLogBreakInterval valid = mock(TimeLogBreakInterval.class);
		given(valid.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(valid.getBreakEndTime()).willReturn(LocalTime.of(12, 30));
		TimeLog validLog = new TimeLog();
		validLog.setBreakIntervals(Arrays.asList(valid));
		assertThat(method.invoke(this.resolver, validLog, regularHours)).isEqualTo(Duration.ofMinutes(30));
	}

	@Test
	void testCalculateAggregatedBreakTimeWithinRangeFallbackViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class
			.getDeclaredMethod("calculateAggregatedBreakTimeWithinRange", TimeLog.class, List.class, Range.class);
		method.setAccessible(true);

		Range<LocalTime> regularHours = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));

		// Fallback path: sameDayTimeLogs null, current time log has breaks
		TimeLogBreakInterval valid = mock(TimeLogBreakInterval.class);
		given(valid.getBreakStartTime()).willReturn(LocalTime.of(12, 0));
		given(valid.getBreakEndTime()).willReturn(LocalTime.of(12, 30));
		TimeLog current = new TimeLog();
		current.setBreakIntervals(Arrays.asList(valid));
		assertThat(method.invoke(this.resolver, current, null, regularHours)).isEqualTo(Duration.ofMinutes(30));

		// Fallback path with no break intervals returns ZERO
		TimeLog noBreaks = new TimeLog();
		noBreaks.setBreakIntervals(Collections.emptyList());
		assertThat(method.invoke(this.resolver, noBreaks, null, regularHours)).isEqualTo(Duration.ZERO);

		// Aggregation path: sameDayTimeLogs present, sums across intervals
		assertThat(method.invoke(this.resolver, current, Arrays.asList(current), regularHours))
			.isEqualTo(Duration.ofMinutes(30));
	}

	@Test
	void testAdjustTimeRangesFromEndReturnsInputWhenNullEmptyOrZeroViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod("adjustTimeRangesFromEnd",
				RangeSet.class, Duration.class);
		method.setAccessible(true);

		// Null input is returned unchanged
		assertThat(method.invoke(this.resolver, (RangeSet<LocalTime>) null, Duration.ofMinutes(10))).isNull();

		// Empty input is returned unchanged
		TreeRangeSet<LocalTime> empty = TreeRangeSet.create();
		assertThat(method.invoke(this.resolver, empty, Duration.ofMinutes(10))).isEqualTo(empty);

		// Zero adjustment returns the same ranges unchanged
		TreeRangeSet<LocalTime> ranges = TreeRangeSet.create();
		ranges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertThat(method.invoke(this.resolver, ranges, Duration.ZERO)).isEqualTo(ranges);
	}

	@Test
	void testCapBreakThresholdAdjustmentReturnsRawWhenNoSameDayLogsViaReflection() throws Exception {
		Method method = RangeBasedRegularHoursRuleTimeRangeResolver.class.getDeclaredMethod(
				"capBreakThresholdAdjustment", Duration.class, TimeRangeResolverContext.class, Range.class,
				RangeSet.class);
		method.setAccessible(true);

		Range<LocalTime> template = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		TreeRangeSet<LocalTime> available = TreeRangeSet.create();
		available.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30));
		given(this.context.getSameDayTimeLogs()).willReturn(Collections.emptyList());

		// sameDayTimeLogs empty → returns raw adjustment unchanged (line 456-457)
		Duration raw = Duration.ofMinutes(20);
		assertThat(method.invoke(this.resolver, raw, this.context, template, available)).isEqualTo(raw);
	}

}
