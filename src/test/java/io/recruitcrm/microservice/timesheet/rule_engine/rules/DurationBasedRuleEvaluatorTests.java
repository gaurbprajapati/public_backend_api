package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("DurationBasedRuleEvaluator Tests")
class DurationBasedRuleEvaluatorTests {

	@Mock
	private IRuleFactory ruleFactory;

	@Mock
	private Logger logger;

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	private TimeLog timeLog1;

	private TimeLog timeLog2;

	private DurationBasedRuleEvaluator evaluator;

	@BeforeEach
	void setUp() {
		this.evaluator = new DurationBasedRuleEvaluator(this.ruleFactory, this.logger);
		this.timeLog1 = new TimeLog();
		this.timeLog2 = new TimeLog();
		// Set up with integer/epoch values as required by the entity model
		this.timeLog1.setWorkTime(3600); // 1 hour in seconds
		this.timeLog1.setWorkStartTime(28800); // 8:00 AM in seconds
		this.timeLog1.setWorkEndTime(32400); // 9:00 AM in seconds
		this.timeLog1.setDate(1705296000); // Example epoch for 2024-01-15

		this.timeLog2.setWorkTime(7200); // 2 hours in seconds
		this.timeLog2.setWorkStartTime(32400); // 9:00 AM in seconds
		this.timeLog2.setWorkEndTime(39600); // 11:00 AM in seconds
		this.timeLog2.setDate(1705382400); // Example epoch for 2024-01-16
	}

	@Test
	@DisplayName("Constructor - should initialize correctly")
	void testConstructor() {
		// Act
		this.evaluator = new DurationBasedRuleEvaluator(this.ruleFactory, this.logger);

		// Assert
		assertThat(this.evaluator).isNotNull();
	}

	@Test
	@DisplayName("IsWeeklyOvertimeRule with duration based weekly overtime - should return true")
	void testIsWeeklyOvertimeRuleWithDurationBasedWeeklyOvertime() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		Timesheet timesheetUnderTest = createMockTimesheetWithFrequency(TimesheetFrequency.WEEKLY);

		// Act
		boolean result = this.evaluator.isWeeklyOvertimeRule(rule, timesheetUnderTest);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("IsWeeklyOvertimeRule with other rule type - should return false")
	void testIsWeeklyOvertimeRuleWithOtherRuleType() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_REGULAR_HOURS);
		Timesheet timesheetUnderTest = createMockTimesheetWithFrequency(TimesheetFrequency.WEEKLY);

		// Act
		boolean result = this.evaluator.isWeeklyOvertimeRule(rule, timesheetUnderTest);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("IsWeeklyOvertimeRule with monthly frequency - should return false")
	void testIsWeeklyOvertimeRuleWithMonthlyFrequency() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		Timesheet timesheetUnderTest = createMockTimesheetWithFrequency(TimesheetFrequency.MONTHLY);

		// Act
		boolean result = this.evaluator.isWeeklyOvertimeRule(rule, timesheetUnderTest);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("IsWeeklyOvertimeRule with biweekly frequency - should return true")
	void testIsWeeklyOvertimeRuleWithBiweeklyFrequency() {
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		Timesheet timesheetUnderTest = createMockTimesheetWithFrequency(TimesheetFrequency.BIWEEKLY);

		boolean result = this.evaluator.isWeeklyOvertimeRule(rule, timesheetUnderTest);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("IsWeeklyOvertimeRule with null timesheet frequency - should return true")
	void testIsWeeklyOvertimeRuleWithNullTimesheetFrequency() {
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		Timesheet timesheetUnderTest = createMockTimesheetWithNullableFrequency(null);

		boolean result = this.evaluator.isWeeklyOvertimeRule(rule, timesheetUnderTest);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("IsWeeklyOvertimeRule with zero timesheet frequency - should return true")
	void testIsWeeklyOvertimeRuleWithZeroTimesheetFrequency() {
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		Timesheet timesheetUnderTest = createMockTimesheetWithNullableFrequency(0);

		boolean result = this.evaluator.isWeeklyOvertimeRule(rule, timesheetUnderTest);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("GetRegularHoursRuleType - should return duration based regular hours")
	void testGetRegularHoursRuleType() {
		// Act
		RuleType result = this.evaluator.getRegularHoursRuleType();

		// Assert
		assertThat(result).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("GetBreakRuleType - should return duration based break")
	void testGetBreakRuleType() {
		// Act
		RuleType result = this.evaluator.getBreakRuleType();

		// Assert
		assertThat(result).isEqualTo(RuleType.DURATION_BASED_BREAK);
	}

	@Test
	@DisplayName("GetDefaultPayRuleType - should return duration based default pay")
	void testGetDefaultPayRuleType() {
		RuleType result = this.evaluator.getDefaultPayRuleType();

		assertThat(result).isEqualTo(RuleType.DURATION_BASED_DEFAULT_PAY);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with valid time logs - should filter and normalize")
	void testPrepareWeeklyTimeLogsWithValidTimeLogs() {
		// Arrange
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600); // 8 hours in seconds
		this.timeLog1.setWorkStartTime(28800); // 8:00 AM in seconds
		this.timeLog1.setWorkEndTime(32400); // 9:00 AM in seconds
		this.timeLog1.setDate(1705296000); // 2024-01-15 epoch

		this.timeLog2.setId(2);
		this.timeLog2.setWorkTime(43200); // 12 hours in seconds
		this.timeLog2.setWorkStartTime(32400); // 9:00 AM in seconds
		this.timeLog2.setWorkEndTime(39600); // 11:00 AM in seconds
		this.timeLog2.setDate(1705382400); // 2024-01-16 epoch

		List<TimeLog> timeLogs = List.of(this.timeLog1, this.timeLog2);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0)).hasSize(2);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with time log without work time - should filter out")
	void testPrepareWeeklyTimeLogsWithTimeLogWithoutWorkTime() {
		// Arrange
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(null);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with custom week start day - should use custom day")
	void testPrepareWeeklyTimeLogsWithCustomWeekStartDay() {
		// Arrange
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600); // 8 hours in seconds
		this.timeLog1.setWorkStartTime(28800); // 8:00 AM in seconds
		this.timeLog1.setWorkEndTime(32400); // 9:00 AM in seconds
		this.timeLog1.setDate(1705296000); // 2024-01-15 epoch

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with timesheet and null start day - should use default")
	void testPrepareWeeklyTimeLogsWithTimesheetAndNullStartDay() {
		// Arrange
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null);
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600); // 8 hours in seconds
		this.timeLog1.setWorkStartTime(28800); // 8:00 AM in seconds
		this.timeLog1.setWorkEndTime(32400); // 9:00 AM in seconds
		this.timeLog1.setDate(1705296000); // 2024-01-15 epoch

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, this.timesheet);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with timesheet and valid start day - should use custom day")
	void testPrepareWeeklyTimeLogsWithTimesheetAndValidStartDay() {
		// Arrange
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(3); // Wednesday
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600); // 8 hours in seconds
		this.timeLog1.setWorkStartTime(28800); // 8:00 AM in seconds
		this.timeLog1.setWorkEndTime(32400); // 9:00 AM in seconds
		this.timeLog1.setDate(1705296000); // 2024-01-15 epoch

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, this.timesheet);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with monthly timesheet - calendar start day ignored, default week boundaries")
	void testPrepareWeeklyTimeLogsWithMonthlyTimesheetIgnoresCalendarStartDay() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(31);

		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600);
		this.timeLog1.setWorkStartTime(28800);
		this.timeLog1.setWorkEndTime(32400);
		this.timeLog1.setDate(1705296000);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, this.timesheet);

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with monthly timesheet spanning two ISO weeks - two week buckets")
	void testPrepareWeeklyTimeLogsWithMonthlyTimesheetSplitsAcrossWeeks() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(1);

		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600);
		this.timeLog1.setWorkStartTime(28800);
		this.timeLog1.setWorkEndTime(32400);
		this.timeLog1.setDate(1705296000);

		this.timeLog2.setId(2);
		this.timeLog2.setWorkTime(3600);
		this.timeLog2.setWorkStartTime(28800);
		this.timeLog2.setWorkEndTime(32400);
		this.timeLog2.setDate(1705977600);

		List<TimeLog> timeLogs = List.of(this.timeLog1, this.timeLog2);

		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, this.timesheet);

		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with time log having null work time - should filter out and log warning")
	void testPrepareWeeklyTimeLogsWithTimeLogHavingNullWorkTime() {
		// Arrange
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(null); // Null work time
		this.timeLog1.setWorkStartTime(28800);
		this.timeLog1.setWorkEndTime(32400);
		this.timeLog1.setDate(1705296000);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with custom week start day and null work time - should filter out")
	void testPrepareWeeklyTimeLogsWithCustomWeekStartDayAndNullWorkTime() {
		// Arrange
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(null); // Null work time
		this.timeLog1.setWorkStartTime(28800);
		this.timeLog1.setWorkEndTime(32400);
		this.timeLog1.setDate(1705296000);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with timesheet and null work time - should filter out")
	void testPrepareWeeklyTimeLogsWithTimesheetAndNullWorkTime() {
		// Arrange
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(3); // Wednesday
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(null); // Null work time
		this.timeLog1.setWorkStartTime(28800);
		this.timeLog1.setWorkEndTime(32400);
		this.timeLog1.setDate(1705296000);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, this.timesheet);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with mixed valid and null work time logs - should filter correctly")
	void testPrepareWeeklyTimeLogsWithMixedValidAndNullWorkTimeLogs() {
		// Arrange
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600); // Valid work time
		this.timeLog1.setWorkStartTime(28800);
		this.timeLog1.setWorkEndTime(32400);
		this.timeLog1.setDate(1705296000);

		this.timeLog2.setId(2);
		this.timeLog2.setWorkTime(null); // Null work time - should be filtered out
		this.timeLog2.setWorkStartTime(32400);
		this.timeLog2.setWorkEndTime(39600);
		this.timeLog2.setDate(1705382400);

		List<TimeLog> timeLogs = List.of(this.timeLog1, this.timeLog2);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1); // Only the valid time log should remain
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with time log having both work start and end times - should use existing times")
	void testPrepareWeeklyTimeLogsWithTimeLogHavingBothWorkStartAndEndTimes() {
		// Arrange - This tests the normalizeDurationBasedTimeLog method when both
		// start/end times are set
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(3600); // 1 hour in seconds
		this.timeLog1.setWorkStartTime(28800); // 8:00 AM in seconds
		this.timeLog1.setWorkEndTime(32400); // 9:00 AM in seconds
		this.timeLog1.setDate(1705296000);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1);

		// Verify the normalized time log has the correct normalized times
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog normalizedTimeLog = result.get(0).get(0);
		assertThat(normalizedTimeLog.getNormalizedWorkStartTime()).isEqualTo(normalizedTimeLog.getWorkStartTime());
		assertThat(normalizedTimeLog.getNormalizedWorkEndTime()).isEqualTo(normalizedTimeLog.getWorkEndTime());
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with time log having only work time - should normalize using midnight anchor")
	void testPrepareWeeklyTimeLogsWithTimeLogHavingOnlyWorkTime() {
		// Arrange - This tests the normalizeDurationBasedTimeLog method when only work
		// time is set
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(7200); // 2 hours in seconds
		this.timeLog1.setWorkStartTime(null); // No start time
		this.timeLog1.setWorkEndTime(null); // No end time
		this.timeLog1.setDate(1705296000);

		List<TimeLog> timeLogs = List.of(this.timeLog1);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasSize(1);

		// Verify the normalized time log has midnight as start and 2 hours later as end
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog normalizedTimeLog = result.get(0).get(0);
		assertThat(normalizedTimeLog.getNormalizedWorkStartTime()).isEqualTo(LocalTime.MIDNIGHT);
		assertThat(normalizedTimeLog.getNormalizedWorkEndTime()).isEqualTo(LocalTime.of(2, 0)); // 2
																								// hours
																								// from
																								// midnight
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with time log having null work time but valid start/end times - should filter out")
	void testPrepareWeeklyTimeLogsWithTimeLogHavingNullWorkTimeButValidStartEndTimes() {
		// Arrange - This tests that even with valid start/end times, null work time
		// causes filtering, but with different time values and multiple time logs
		this.timeLog1.setId(1);
		this.timeLog1.setWorkTime(null); // Null work time - should be filtered out
		this.timeLog1.setWorkStartTime(36000); // 10:00 AM in seconds
		this.timeLog1.setWorkEndTime(43200); // 12:00 PM in seconds
		this.timeLog1.setDate(1705382400); // Different date

		this.timeLog2.setId(2);
		this.timeLog2.setWorkTime(7200); // Valid work time - 2 hours
		this.timeLog2.setWorkStartTime(43200); // 12:00 PM in seconds
		this.timeLog2.setWorkEndTime(50400); // 2:00 PM in seconds
		this.timeLog2.setDate(1705468800); // Different date

		List<TimeLog> timeLogs = List.of(this.timeLog1, this.timeLog2);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().hasSize(1); // Should have one week
		assertThat(result.getFirst()).hasSize(1); // Should have only the valid time log
		assertThat(result.getFirst().getFirst().getWorkTime()).isEqualTo(Duration.ofSeconds(7200)); // Verify
																									// the
																									// valid
																									// time
																									// log
																									// remains
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with empty time logs list - should return empty result")
	void testPrepareWeeklyTimeLogsWithEmptyTimeLogsList() {
		// Arrange
		List<TimeLog> timeLogs = List.of();

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with empty time logs list and custom week start day - should return empty result")
	void testPrepareWeeklyTimeLogsWithEmptyTimeLogsListAndCustomWeekStartDay() {
		// Arrange
		List<TimeLog> timeLogs = List.of();

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("PrepareWeeklyTimeLogs with empty time logs list and timesheet - should return empty result")
	void testPrepareWeeklyTimeLogsWithEmptyTimeLogsListAndTimesheet() {
		// Arrange
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(3); // Wednesday
		List<TimeLog> timeLogs = List.of();

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(timeLogs, this.timesheet);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	private IEvaluatableRule createMockRule(RuleType ruleType) {
		return new IEvaluatableRule() {
			@Override
			public String getRuleName() {
				return "Test Rule";
			}

			@Override
			public RuleType getRuleType() {
				return ruleType;
			}

			@Override
			public List<WorkDay> getWorkDays() {
				return List.of(WorkDay.MONDAY, WorkDay.TUESDAY, WorkDay.WEDNESDAY, WorkDay.THURSDAY, WorkDay.FRIDAY);
			}

			@Override
			public boolean isApplicableOnDay(WorkDay timeLogDay) {
				return getWorkDays().contains(timeLogDay);
			}

			@Override
			public boolean isDailyOvertimeRule() {
				return false;
			}
		};
	}

	private Timesheet createMockTimesheetWithFrequency(TimesheetFrequency frequency) {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setTimesheetFrequency(frequency.getFrequencyId());

		Timesheet createdTimesheet = new Timesheet();
		createdTimesheet.setTimesheetSetting(setting);

		return createdTimesheet;
	}

	private Timesheet createMockTimesheetWithNullableFrequency(Integer frequencyId) {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setTimesheetFrequency(frequencyId);
		Timesheet ts = new Timesheet();
		ts.setTimesheetSetting(setting);
		return ts;
	}

}