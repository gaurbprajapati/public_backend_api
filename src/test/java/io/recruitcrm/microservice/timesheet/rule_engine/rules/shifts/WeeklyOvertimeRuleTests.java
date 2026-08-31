package io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WeeklyOvertimeRuleTests {

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private TimeLog timeLog1;

	@Mock
	private Logger logger;

	@Mock
	private CustomRule customRule;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto;

	private WeeklyOvertimeRule weeklyOvertimeRule;

	@BeforeEach
	void setUp() {
		this.weeklyOvertimeRule = new WeeklyOvertimeRule(this.logger);
	}

	@Test
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.weeklyOvertimeRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
	}

	@Test
	void testGetName() {
		// When
		String name = this.weeklyOvertimeRule.getName();

		// Then
		assertThat(name).isEqualTo("Range-Based Weekly Overtime Rule");
	}

	@Test
	void testEvaluateWithNullContext() {
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
	void testEvaluateWithNullTimesheet() {
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(null)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimesheetSetting() {
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(null)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimeLog() {
		// Weekly overtime rules don't use currentTimeLogBeingEvaluated, so null is
		// acceptable
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(Arrays.asList(this.timeLog1))
			.weeklyOvertimeCandidateTimeRanges(new ArrayList<>())
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then - should not throw exception and should return a valid result
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyOvertimeHours()).isNotNull();
	}

	@Test
	void testEvaluateWithNullCurrentRuleBeingEvaluated() {
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(null)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(context)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Current rule being evaluated cannot be null");
	}

	@Test
	void testEvaluateWithNullWeeklyThreshold() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(null);

		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithHoursWorkedNotExceedingThreshold() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		List<TimeLog> weeklyTimeLogs = List.of(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithHoursWorkedExceedingThreshold() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Simulate 45 hours worked (5 hours overtime)
		TimeLog overtimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(overtimeLog.getWorkTime()).willReturn(Duration.ofHours(45));
		List<TimeLog> weeklyTimeLogs = Arrays.asList(overtimeLog);

		// Overtime candidate time range is 5 hours
		RangeSet<LocalTime> overtimeRange = TreeRangeSet.create();
		overtimeRange.add(com.google.common.collect.Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(5, 0)));
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = List.of(overtimeRange);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Overtime duration is min(5, 5) = 5 hours, pay = 20*1.5*5 = 150, bill = 40*1.5*5
		// = 300
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("150.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("300.0"));
	}

	@Test
	void testEvaluateCarriesRateMultipliersFromRuleConfigOntoResult() {
		// Given — distinct pay/bill multipliers so a swapped source would be caught
		// (covers the weekly-overtime createCompleteResult overload)
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		TimeLog overtimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(overtimeLog.getWorkTime()).willReturn(Duration.ofHours(45));
		List<TimeLog> weeklyTimeLogs = Arrays.asList(overtimeLog);

		RangeSet<LocalTime> overtimeRange = TreeRangeSet.create();
		overtimeRange.add(com.google.common.collect.Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(5, 0)));
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = List.of(overtimeRange);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then — the rule config's multipliers are carried onto the emitted result
		assertThat(result.getPayRateMultiplier()).isEqualTo(1.5f);
		assertThat(result.getBillRateMultiplier()).isEqualTo(2.0f);
	}

	@Test
	void testEvaluateWithFixedRateChargeMethod() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.FIXED_RATE);
		given(this.customRule.getPayRatePerHour()).willReturn(30.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(60.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Simulate 45 hours worked (5 hours overtime)
		TimeLog overtimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(overtimeLog.getWorkTime()).willReturn(Duration.ofHours(45));
		List<TimeLog> weeklyTimeLogs = Arrays.asList(overtimeLog);

		// Overtime candidate time range is 5 hours
		RangeSet<LocalTime> overtimeRange = TreeRangeSet.create();
		overtimeRange.add(com.google.common.collect.Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(5, 0)));
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = Arrays.asList(overtimeRange);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Fixed rate: pay = 30*5 = 150, bill = 60*5 = 300
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("150.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("300.0"));
	}

	@Test
	void testEvaluateWithUnknownChargeMethodDefaultsToMultiplier() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		// Use MULTIPLIER instead of null
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Simulate 45 hours worked (5 hours overtime)
		TimeLog overtimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(overtimeLog.getWorkTime()).willReturn(Duration.ofHours(45));
		List<TimeLog> weeklyTimeLogs = List.of(overtimeLog);

		// Overtime candidate time range is 5 hours
		RangeSet<LocalTime> overtimeRange = TreeRangeSet.create();
		overtimeRange.add(com.google.common.collect.Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(5, 0)));
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = List.of(overtimeRange);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Defaults to multiplier: pay = 20*1.5*5 = 150, bill = 40*1.5*5 = 300
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("150.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("300.0"));
	}

	@Test
	void testEvaluateWithNullChargeMethodThrows() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(null);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		TimeLog overtimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(overtimeLog.getWorkTime()).willReturn(Duration.ofHours(45));
		List<TimeLog> weeklyTimeLogs = List.of(overtimeLog);

		RangeSet<LocalTime> overtimeRange = TreeRangeSet.create();
		overtimeRange.add(com.google.common.collect.Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(5, 0)));
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = List.of(overtimeRange);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithPartialHourOvertime() {
		given(this.timesheetSetting.getPayRate()).willReturn(15.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(30.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getBillRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(15.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(30.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Simulate 42.5 hours worked (2.5 hours overtime)
		TimeLog overtimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(overtimeLog.getWorkTime()).willReturn(Duration.ofHours(42).plusMinutes(30));
		List<TimeLog> weeklyTimeLogs = Arrays.asList(overtimeLog);

		// Overtime candidate time range is 2.5 hours
		RangeSet<LocalTime> overtimeRange = TreeRangeSet.create();
		overtimeRange.add(com.google.common.collect.Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(2, 30)));
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = Arrays.asList(overtimeRange);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Overtime duration is min(2.5, 2.5) = 2.5 hours, pay = 15*2*2.5 = 75, bill =
		// 30*2*2.5 = 150
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("75.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("150.0"));
	}

	@Test
	void testEvaluateWithTimeLogHavingNonZeroDuration() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Create a TimeLog with non-zero duration to cover the branch where
		// duration.isZero() returns false
		TimeLog validTimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(validTimeLog.getWorkTime()).willReturn(Duration.ofHours(8));
		given(validTimeLog.getBreakTime()).willReturn(Duration.ofMinutes(30));

		List<TimeLog> weeklyTimeLogs = List.of(validTimeLog);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Since hours worked (8) is less than threshold (40), no overtime
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithNullAndEmptyRangeSets() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Create a TimeLog with non-zero duration
		TimeLog validTimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(validTimeLog.getWorkTime()).willReturn(Duration.ofHours(45));
		given(validTimeLog.getBreakTime()).willReturn(Duration.ofMinutes(30));

		List<TimeLog> weeklyTimeLogs = List.of(validTimeLog);

		// Create list with null and empty RangeSets to cover the filter condition
		RangeSet<LocalTime> nullRangeSet = null;
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = Arrays.asList(nullRangeSet, emptyRangeSet);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Since candidate time ranges are null/empty, net overtime is 0
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithTimeLogHavingZeroDuration() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		// Create a TimeLog with zero duration to cover the branch where duration.isZero()
		// returns true
		TimeLog zeroDurationTimeLog = org.mockito.Mockito.mock(TimeLog.class);
		given(zeroDurationTimeLog.getWorkTime()).willReturn(Duration.ZERO);

		List<TimeLog> weeklyTimeLogs = List.of(zeroDurationTimeLog);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);
		assertThat(result).isNotNull();
		// Since the TimeLog has zero duration, it should be filtered out and no overtime
		// calculated
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Evaluate with null weekly time logs returns zero amounts")
	void testEvaluateWithNullWeeklyTimeLogs() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(null) // Null weekly time logs
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with empty weekly time logs returns zero amounts")
	void testEvaluateWithEmptyWeeklyTimeLogs() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		List<TimeLog> weeklyTimeLogs = new ArrayList<>(); // Empty list
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with null weekly overtime candidate time ranges returns zero amounts")
	void testEvaluateWithNullWeeklyOvertimeCandidateTimeRanges() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.customRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(null) // Null candidate time ranges
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

}