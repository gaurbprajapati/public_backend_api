package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

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

import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
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
	private CustomRule mockCustomRule;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto;

	@Mock
	private TemplateWorkDay templateWorkDay;

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
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
	}

	@Test
	void testGetName() {
		// When
		String name = this.weeklyOvertimeRule.getName();

		// Then
		assertThat(name).isEqualTo("Duration-Based Weekly Overtime Rule");
	}

	@Test
	void testEvaluateWithValidContext() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
	}

	@Test
	void testEvaluateWithDayOff() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithNullContext() {
		// No stubbing needed
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
	void testEvaluateWithNullTimesheet() {
		// No stubbing needed since exception is thrown before any stubs are accessed
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(null)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimesheetSetting() {
		// No stubbing needed since exception is thrown before any stubs are accessed
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(null)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
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
			.currentRuleBeingEvaluated(this.mockCustomRule)
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
	void testEvaluateWithNullCustomRule() {
		// No stubbing needed since exception is thrown before any stubs are accessed
		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(null)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.weeklyOvertimeRule.evaluate(context)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Current rule being evaluated cannot be null");
	}

	@Test
	void testEvaluateWithWorkDay() {
		// Given - Test with a work day that has specific work hours and exceeds weekly
		// threshold
		given(this.timesheetSetting.getPayRate()).willReturn(30.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(60.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(2.0f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(2.0f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(30.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(60.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(35)); // Lower
																							// threshold
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		// Mock time log with specific work hours that would trigger overtime
		given(this.timeLog1.getWorkTime()).willReturn(Duration.ofHours(10)); // 10 hours
																				// worked

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then - Verify work day specific behavior
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
		// Additional assertions specific to work day scenario
		assertThat(result.getPayAmount()).isNotNull();
		assertThat(result.getBillAmount()).isNotNull();
		assertThat(result.getWeeklyOvertimeHours()).isNotNull();
	}

	@Test
	@DisplayName("Evaluate with null weekly threshold returns zero amounts")
	void testEvaluateWithNullWeeklyThreshold() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(null); // Null
																			// threshold
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with hours worked not exceeding threshold returns zero amounts")
	void testEvaluateWithHoursWorkedNotExceedingThreshold() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40)); // 40
																							// hour
																							// threshold
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with fixed rate charge method calculates amounts correctly")
	void testEvaluateWithFixedRateChargeMethod() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.FIXED_RATE);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(30.0f); // Fixed pay
																			// rate
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(60.0f); // Fixed bill
																			// rate
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with unknown charge method defaults to multiplier")
	void testEvaluateWithUnknownChargeMethod() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(null); // Unknown charge
																		// method
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with hours worked exceeding threshold calculates overtime amounts")
	void testEvaluateWithHoursWorkedExceedingThreshold() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

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
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(TreeRangeSet.create())
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		// Overtime duration is min(5, 5) = 5 hours, pay = 25*1.5*5 = 187.5, bill =
		// 50*1.5*5 = 375
		assertThat(result.getPayAmount()).isEqualTo(new java.math.BigDecimal("187.5"));
		assertThat(result.getBillAmount()).isEqualTo(new java.math.BigDecimal("375.0"));
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ofHours(5));
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with null weekly time logs returns zero amounts")
	void testEvaluateWithNullWeeklyTimeLogs() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(null) // Null weekly time logs
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
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
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = new ArrayList<>(); // Empty list
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeCandidateTimeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
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
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getWeeklyThreshold()).willReturn(Duration.ofHours(40));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		List<TimeLog> weeklyTimeLogs = Arrays.asList(this.timeLog1);

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(null) // Null candidate time ranges
			.build();

		// When
		RuleEvaluationResult result = this.weeklyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(Duration.ZERO);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

}