package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

import com.google.common.collect.Range;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DailyOvertimeRuleTests {

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

	private DailyOvertimeRule dailyOvertimeRule;

	@BeforeEach
	void setUp() {
		this.dailyOvertimeRule = new DailyOvertimeRule(this.logger);
	}

	@Test
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.dailyOvertimeRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_DAILY_OVERTIME);
	}

	@Test
	void testGetName() {
		// When
		String name = this.dailyOvertimeRule.getName();

		// Then
		assertThat(name).isEqualTo("Duration-Based Daily Overtime Rule");
	}

	@Test
	void testEvaluateWithValidContext() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Daily Overtime Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.dailyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_DAILY_OVERTIME);
	}

	@Test
	void testEvaluateCarriesRateMultipliersFromRuleConfigOntoResult() {
		// Given — distinct pay/bill multipliers so a swapped source would be caught
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(2.0f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(this.mockCustomRule.getRuleName()).willReturn("Multiplier Plumbing Daily Overtime");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.dailyOvertimeRule.evaluate(context);

		// Then — the rule config's multipliers are carried onto the emitted result
		assertThat(result.getPayRateMultiplier()).isEqualTo(1.5f);
		assertThat(result.getBillRateMultiplier()).isEqualTo(2.0f);
	}

	@Test
	void testEvaluateWithDayOff() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Daily Overtime Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.dailyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithNullContext() {
		// When & Then
		assertThatThrownBy(() -> this.dailyOvertimeRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
	void testEvaluateWithNullTimesheet() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(null)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.dailyOvertimeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimesheetSetting() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(null)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.dailyOvertimeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimeLog() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Daily Overtime Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.dailyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_DAILY_OVERTIME);
		assertThat(result.getEvaluationDate()).isNull(); // Should be null when time log
															// is null
		assertThat(result.getMetadata()).contains("Date: Weekly Evaluation");
	}

	@Test
	void testEvaluateWithNullCustomRule() {
		// Given
		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(null)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.dailyOvertimeRule.evaluate(context)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Current rule being evaluated cannot be null");
	}

	@Test
	void testEvaluateWithWorkDay() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 2));
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.0f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.0f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.FIXED_RATE);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(30.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(60.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Daily Overtime Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.dailyOvertimeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_DAILY_OVERTIME);
		assertThat(result.getEvaluationDate()).isEqualTo(LocalDate.of(2024, 1, 2));
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.valueOf(60.0));
	}

}
