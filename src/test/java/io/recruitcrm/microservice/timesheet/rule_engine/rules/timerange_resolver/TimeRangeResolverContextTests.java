package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TimeRangeResolverContextTests {

	@Test
	void builderWithValidParameters() {
		// Arrange
		TimeLog timeLog = createMockTimeLog();
		CustomRule customRule = mock(CustomRule.class);
		List<CustomRule> internalRules = new ArrayList<>();
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		Duration workedHours = Duration.ofHours(8);
		Integer ruleIndex = 0;

		// Act
		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(timeLog)
			.currentCustomRuleBeingEvaluated(customRule)
			.internalSortedCustomRules(internalRules)
			.currentTimesheetSetting(timesheetSetting)
			.occupiedTimeRanges(occupiedRanges)
			.workedHoursTillNow(workedHours)
			.currentRuleIndex(ruleIndex)
			.build();

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isEqualTo(timeLog);
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isEqualTo(customRule);
		assertThat(context.getInternalSortedCustomRules()).isEqualTo(internalRules);
		assertThat(context.getCurrentTimesheetSetting()).isEqualTo(timesheetSetting);
		assertThat(context.getOccupiedTimeRanges()).isEqualTo(occupiedRanges);
		assertThat(context.getWorkedHoursTillNow()).isEqualTo(workedHours);
		assertThat(context.getCurrentRuleIndex()).isEqualTo(ruleIndex);
	}

	@Test
	void builderWithNullValues() {
		// Act
		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(null)
			.currentCustomRuleBeingEvaluated(null)
			.internalSortedCustomRules(null)
			.currentTimesheetSetting(null)
			.occupiedTimeRanges(null)
			.workedHoursTillNow(null)
			.currentRuleIndex(null)
			.build();

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isNull();
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isNull();
		assertThat(context.getInternalSortedCustomRules()).isNull();
		assertThat(context.getCurrentTimesheetSetting()).isNull();
		assertThat(context.getOccupiedTimeRanges()).isNull();
		assertThat(context.getWorkedHoursTillNow()).isNull();
		assertThat(context.getCurrentRuleIndex()).isNull();
	}

	@Test
	void testNoArgsConstructor() {
		// Act
		TimeRangeResolverContext context = new TimeRangeResolverContext();

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isNull();
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isNull();
		assertThat(context.getInternalSortedCustomRules()).isNull();
		assertThat(context.getCurrentTimesheetSetting()).isNull();
		assertThat(context.getOccupiedTimeRanges()).isNull();
		assertThat(context.getWorkedHoursTillNow()).isNull();
		assertThat(context.getCurrentRuleIndex()).isNull();
	}

	@Test
	void testAllArgsConstructor() {
		// Arrange
		TimeLog timeLog = createMockTimeLog();
		CustomRule customRule = mock(CustomRule.class);
		List<CustomRule> internalRules = new ArrayList<>();
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		Duration workedHours = Duration.ofHours(8);
		Integer ruleIndex = 1;

		// Act
		TimeRangeResolverContext context = new TimeRangeResolverContext(timeLog, customRule, internalRules,
				timesheetSetting, occupiedRanges, workedHours, null, null, ruleIndex, null);

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isEqualTo(timeLog);
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isEqualTo(customRule);
		assertThat(context.getInternalSortedCustomRules()).isEqualTo(internalRules);
		assertThat(context.getCurrentTimesheetSetting()).isEqualTo(timesheetSetting);
		assertThat(context.getOccupiedTimeRanges()).isEqualTo(occupiedRanges);
		assertThat(context.getWorkedHoursTillNow()).isEqualTo(workedHours);
		assertThat(context.getCurrentRuleIndex()).isEqualTo(ruleIndex);
	}

	@Test
	void testSettersAndGetters() {
		// Arrange
		TimeRangeResolverContext context = new TimeRangeResolverContext();
		TimeLog timeLog = createMockTimeLog();
		CustomRule customRule = mock(CustomRule.class);
		List<CustomRule> internalRules = new ArrayList<>();
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		Duration workedHours = Duration.ofHours(6);
		Integer ruleIndex = 2;

		// Act
		context.setCurrentTimeLogBeingEvaluated(timeLog);
		context.setCurrentCustomRuleBeingEvaluated(customRule);
		context.setInternalSortedCustomRules(internalRules);
		context.setCurrentTimesheetSetting(timesheetSetting);
		context.setOccupiedTimeRanges(occupiedRanges);
		context.setWorkedHoursTillNow(workedHours);
		context.setCurrentRuleIndex(ruleIndex);

		// Assert
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isEqualTo(timeLog);
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isEqualTo(customRule);
		assertThat(context.getInternalSortedCustomRules()).isEqualTo(internalRules);
		assertThat(context.getCurrentTimesheetSetting()).isEqualTo(timesheetSetting);
		assertThat(context.getOccupiedTimeRanges()).isEqualTo(occupiedRanges);
		assertThat(context.getWorkedHoursTillNow()).isEqualTo(workedHours);
		assertThat(context.getCurrentRuleIndex()).isEqualTo(ruleIndex);
	}

	@Test
	void testEqualsAndHashCode() {
		// Arrange
		TimeLog timeLog = createMockTimeLog();
		CustomRule customRule = mock(CustomRule.class);
		List<CustomRule> internalRules = new ArrayList<>();
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		Duration workedHours = Duration.ofHours(8);
		Integer ruleIndex = 0;

		TimeRangeResolverContext context1 = new TimeRangeResolverContext(timeLog, customRule, internalRules,
				timesheetSetting, occupiedRanges, workedHours, null, null, ruleIndex, null);

		TimeRangeResolverContext context2 = new TimeRangeResolverContext(timeLog, customRule, internalRules,
				timesheetSetting, occupiedRanges, workedHours, null, null, ruleIndex, null);

		// Assert
		assertThat(context1).isEqualTo(context2).hasSameHashCodeAs(context2);
	}

	@Test
	void testToString() {
		// Arrange
		TimeRangeResolverContext context = TimeRangeResolverContext.builder().currentRuleIndex(1).build();

		// Act
		String result = context.toString();

		// Assert
		assertThat(result).isNotNull().contains("currentRuleIndex=1");
	}

	@Test
	void testCalculateEffectiveWorkingTimeWithNullTimeLog() {
		TimeRangeResolverContext context = new TimeRangeResolverContext();

		Duration result = context.calculateEffectiveWorkingTime(null);

		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	void testCalculateEffectiveWorkingTimeWithBreakUnpaid() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));
		timeLog.setBreakTime(Duration.ofHours(1));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(false);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder().currentTimesheetSetting(setting).build();

		Duration result = context.calculateEffectiveWorkingTime(timeLog);

		assertThat(result).isEqualTo(Duration.ofHours(7));
	}

	@Test
	void testCalculateEffectiveWorkingTimeWithBreakPaid() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));
		timeLog.setBreakTime(Duration.ofHours(1));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder().currentTimesheetSetting(setting).build();

		Duration result = context.calculateEffectiveWorkingTime(timeLog);

		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	void testCalculateEffectiveWorkingTimeWithNullBreakTime() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));
		timeLog.setBreakTime(null);

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(false);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder().currentTimesheetSetting(setting).build();

		Duration result = context.calculateEffectiveWorkingTime(timeLog);

		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	void testCalculateEffectiveWorkingTimeWithAdjustedThreshold() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));
		timeLog.setBreakTime(Duration.ZERO);

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(false);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.adjustedRegularHoursBreakThreshold(Duration.ofHours(2))
			.build();

		Duration result = context.calculateEffectiveWorkingTime(timeLog);

		assertThat(result).isEqualTo(Duration.ofHours(6));
	}

	@Test
	void testCalculateEffectiveWorkingTimeReturnsZeroForNegative() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(10, 0));
		timeLog.setBreakTime(Duration.ZERO);

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(false);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.adjustedRegularHoursBreakThreshold(Duration.ofHours(5))
			.build();

		Duration result = context.calculateEffectiveWorkingTime(timeLog);

		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	void testCalculateAggregatedDailyEffectiveWorkingTimeNullSameDayLogs() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(timeLog)
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(null)
			.build();

		Duration result = context.calculateAggregatedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	void testCalculateAggregatedDailyEffectiveWorkingTimeEmptySameDayLogs() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimeLogBeingEvaluated(timeLog)
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(new ArrayList<>())
			.build();

		Duration result = context.calculateAggregatedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	void testCalculateAggregatedDailyEffectiveWorkingTimeMultipleIntervals() {
		TimeLog tl1 = new TimeLog();
		tl1.setWorkStartTime(LocalTime.of(7, 0));
		tl1.setWorkEndTime(LocalTime.of(12, 0));

		TimeLog tl2 = new TimeLog();
		tl2.setWorkStartTime(LocalTime.of(13, 0));
		tl2.setWorkEndTime(LocalTime.of(17, 0));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(List.of(tl1, tl2))
			.build();

		Duration result = context.calculateAggregatedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ofHours(9));
	}

	@Test
	void testCalculateAggregatedDailyEffectiveBreakUnpaid() {
		TimeLog tl1 = new TimeLog();
		tl1.setWorkStartTime(LocalTime.of(9, 0));
		tl1.setWorkEndTime(LocalTime.of(12, 0));
		tl1.setBreakTime(Duration.ofMinutes(30));

		TimeLog tl2 = new TimeLog();
		tl2.setWorkStartTime(LocalTime.of(13, 0));
		tl2.setWorkEndTime(LocalTime.of(17, 0));
		tl2.setBreakTime(Duration.ofMinutes(30));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(false);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(List.of(tl1, tl2))
			.build();

		Duration result = context.calculateAggregatedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ofHours(6));
	}

	@Test
	void testGetAdjustedDailyEffectiveWorkingTime() {
		TimeLog tl1 = new TimeLog();
		tl1.setWorkStartTime(LocalTime.of(9, 0));
		tl1.setWorkEndTime(LocalTime.of(17, 0));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(List.of(tl1))
			.adjustedRegularHoursBreakThreshold(Duration.ofHours(1))
			.build();

		Duration result = context.getAdjustedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ofHours(7));
	}

	@Test
	void testGetAdjustedDailyEffectiveWorkingTimeNullThreshold() {
		TimeLog tl1 = new TimeLog();
		tl1.setWorkStartTime(LocalTime.of(9, 0));
		tl1.setWorkEndTime(LocalTime.of(17, 0));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(List.of(tl1))
			.adjustedRegularHoursBreakThreshold(null)
			.build();

		Duration result = context.getAdjustedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	void testGetAdjustedDailyEffectiveWorkingTimeReturnsZeroForNegative() {
		TimeLog tl1 = new TimeLog();
		tl1.setWorkStartTime(LocalTime.of(9, 0));
		tl1.setWorkEndTime(LocalTime.of(10, 0));

		TimesheetSetting setting = new TimesheetSetting();
		setting.setCalculateBreakTime(true);

		TimeRangeResolverContext context = TimeRangeResolverContext.builder()
			.currentTimesheetSetting(setting)
			.sameDayTimeLogs(List.of(tl1))
			.adjustedRegularHoursBreakThreshold(Duration.ofHours(5))
			.build();

		Duration result = context.getAdjustedDailyEffectiveWorkingTime();

		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	void testSameDayTimeLogsSetterGetter() {
		TimeLog tl1 = new TimeLog();
		TimeLog tl2 = new TimeLog();
		List<TimeLog> sameDayLogs = List.of(tl1, tl2);

		TimeRangeResolverContext context = new TimeRangeResolverContext();
		context.setSameDayTimeLogs(sameDayLogs);

		assertThat(context.getSameDayTimeLogs()).isEqualTo(sameDayLogs);
		assertThat(context.getSameDayTimeLogs()).hasSize(2);
	}

	// Helper methods
	private TimeLog createMockTimeLog() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDate()).willReturn(LocalDate.of(2023, 12, 21));
		given(timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		return timeLog;
	}

}
