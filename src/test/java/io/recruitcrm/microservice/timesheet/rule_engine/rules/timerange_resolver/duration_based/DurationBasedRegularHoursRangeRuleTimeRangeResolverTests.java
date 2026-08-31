package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.duration_based;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DurationBasedRegularHoursRangeRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	@Mock
	private TimeLog timeLog;

	@Mock
	private TimeRangeResolverContext context;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSetting;

	@Mock
	private TemplateWorkDay templateWorkDay;

	private DurationBasedRegularHoursRangeRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new DurationBasedRegularHoursRangeRuleTimeRangeResolver(this.logger);
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
	void testResolveTimeRangeWithNullTemplateWorkDay() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Collections.emptyList());

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithNullWorkTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithZeroWorkTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ZERO);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithBreakExceedsWorkTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofHours(10)); // Break
																				// longer
																				// than
																				// work
																				// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithBreakEqualsWorkTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofHours(8)); // Break
																			// equals work
																			// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void testResolveTimeRangeWithValidTimes() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofHours(1));

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();

		// Regular hours should be shifted by break duration (1 hour)
		// Effective work time: 8 - 1 = 7 hours
		// Regular hours range: 01:00 to 08:00 (shifted by 1 hour break)
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> regularHoursRange = result.asRanges().iterator().next();
		assertThat(regularHoursRange.lowerEndpoint()).isEqualTo(LocalTime.of(1, 0));
		assertThat(regularHoursRange.upperEndpoint()).isEqualTo(LocalTime.of(8, 0));
	}

	@Test
	void testResolveTimeRangeWithNullBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);

		// Use a real TemplateWorkDay, not a mock
		TemplateWorkDay realTemplateWorkDay = new TemplateWorkDay();
		realTemplateWorkDay.setWorkDayType(WorkDay.MONDAY);
		realTemplateWorkDay.setWorkTime(Duration.ofHours(8));
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Collections.singletonList(realTemplateWorkDay));
		given(this.timeLog.getBreakTime()).willReturn(null);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();

		// Null break time means no break - all work time is regular hours
		// Regular hours range: 00:00 to 08:00 (no shift since no break)
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> regularHoursRange = result.asRanges().iterator().next();
		assertThat(regularHoursRange.lowerEndpoint()).isEqualTo(LocalTime.of(0, 0));
		assertThat(regularHoursRange.upperEndpoint()).isEqualTo(LocalTime.of(8, 0));
	}

	@Test
	void testResolveTimeRangeWithZeroBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8));
		given(this.timeLog.getBreakTime()).willReturn(Duration.ZERO);

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();

		// Zero break time, so no shift - regular hours from 00:00 to 08:00
		assertThat(result.asRanges()).hasSize(1);
		Range<LocalTime> regularHoursRange = result.asRanges().iterator().next();
		assertThat(regularHoursRange.lowerEndpoint()).isEqualTo(LocalTime.of(0, 0));
		assertThat(regularHoursRange.upperEndpoint()).isEqualTo(LocalTime.of(8, 0));
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdAdjustment() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofMinutes(15)); // 15
																				// minutes
																				// break
																				// time
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30)); // 30
																									// minutes
																									// threshold
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// total
																					// work
																					// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// Effective work time should be 8 hours - 15 minutes = 7 hours 45 minutes
		// Then adjusted by threshold: 7:45 - 15 minutes = 7:30
		// Regular hours range should be from 00:15 to 07:45 (shifted by 15 min break)
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdNoAdjustment() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofMinutes(30)); // 30
																				// minutes
																				// break
																				// time
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(20)); // 20
																									// minutes
																									// threshold
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// total
																					// work
																					// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// No adjustment needed since threshold (20 min) <= break time (30 min)
		// Effective work time: 8 hours - 30 minutes = 7 hours 30 minutes
		// Regular hours range should be from 00:30 to 08:00 (shifted by 30 min break)
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdNullBreakTime() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getBreakTime()).willReturn(null); // No break time
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofMinutes(30)); // 30
																									// minutes
																									// threshold
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// total
																					// work
																					// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// Should deduct entire threshold (30 minutes) since no break time
		// Effective work time: 8 hours - 30 minutes = 7 hours 30 minutes
		// Regular hours range should be from 00:00 to 07:30 (no shift, but reduced by
		// threshold)
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdNullThreshold() {
		// Given
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofMinutes(15)); // 15
																				// minutes
																				// break
																				// time
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(null); // No
																				// threshold
																				// set
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// total
																					// work
																					// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
		// No adjustment needed since no threshold set
		// Effective work time: 8 hours - 15 minutes = 7 hours 45 minutes
		// Regular hours range should be from 00:15 to 08:00 (shifted by 15 min break)
	}

	@Test
	void testResolveTimeRangeWithBreakTimeThresholdInvalidAdjustment() {
		// Given - adjustment would make effective work time zero or negative
		given(this.context.getCurrentTimeLogBeingEvaluated()).willReturn(this.timeLog);
		given(this.context.getCurrentTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timeLog.getBreakTime()).willReturn(Duration.ofMinutes(5)); // 5 minutes
																				// break
																				// time
		given(this.timesheetSetting.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.timesheetSetting.getBreakTimeThreshold()).willReturn(Duration.ofHours(10)); // 10
																								// hours
																								// threshold
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// total
																					// work
																					// time

		// When
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(this.context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue(); // Should return empty range due to invalid
												// adjustment
	}

}