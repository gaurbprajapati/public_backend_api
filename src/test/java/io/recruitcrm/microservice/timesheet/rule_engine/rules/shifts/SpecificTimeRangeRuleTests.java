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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SpecificTimeRangeRuleTests {

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

	private SpecificTimeRangeRule specificTimeRangeRule;

	@BeforeEach
	void setUp() {
		this.specificTimeRangeRule = new SpecificTimeRangeRule(this.logger);
	}

	@Test
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.specificTimeRangeRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);
	}

	@Test
	void testGetName() {
		// When
		String name = this.specificTimeRangeRule.getName();

		// Then
		assertThat(name).isEqualTo("Range-Based Specific Time Range Rule");
	}

	@Test
	void testEvaluateWithNullContext() {
		assertThatThrownBy(() -> this.specificTimeRangeRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
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
		assertThatThrownBy(() -> this.specificTimeRangeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
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
		assertThatThrownBy(() -> this.specificTimeRangeRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimeLog() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);
		given(this.customRule.getRuleName()).willReturn("Test Specific Time Range Rule");
		given(this.customRule.getPayRateMultiplier()).willReturn(1.0f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(null);
		given(this.customRule.getBillRatePerHour()).willReturn(null);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.specificTimeRangeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);
		assertThat(result.getEvaluationDate()).isNull(); // Should be null when time log
															// is null
		assertThat(result.getMetadata()).contains("Date: Weekly Evaluation");
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
		assertThatThrownBy(() -> this.specificTimeRangeRule.evaluate(context))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Current rule being evaluated cannot be null");
	}

	@Test
	void testEvaluateWithZeroDuration() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.0f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create(); // empty

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		RuleEvaluationResult result = this.specificTimeRangeRule.evaluate(context);
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithMultiplierChargeMethod() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getBillRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(20.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(40.0f);
		given(this.customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(12, 0))); // 2
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		RuleEvaluationResult result = this.specificTimeRangeRule.evaluate(context);
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("80.0")); // 20*2*2
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("160.0")); // 40*2*2
	}

	@Test
	void testEvaluateWithFixedRateChargeMethod() {
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(1.0f);
		given(this.customRule.getBillRateMultiplier()).willReturn(1.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.FIXED_RATE);
		given(this.customRule.getPayRatePerHour()).willReturn(30.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(60.0f);
		given(this.customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(8, 0), LocalTime.of(10, 0))); // 2
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		RuleEvaluationResult result = this.specificTimeRangeRule.evaluate(context);
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("60.0")); // 30*2
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("120.0")); // 60*2
	}

	@Test
	void testEvaluateWithPartialHourDuration() {
		given(this.timesheetSetting.getPayRate()).willReturn(15.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(30.0f);
		given(this.customRule.getPayRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getBillRateMultiplier()).willReturn(2.0f);
		given(this.customRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.customRule.getPayRatePerHour()).willReturn(15.0f);
		given(this.customRule.getBillRatePerHour()).willReturn(30.0f);
		given(this.customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 30))); // 1.5
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.customRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		RuleEvaluationResult result = this.specificTimeRangeRule.evaluate(context);
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("45.0")); // 15*2*1.5
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("90.0")); // 30*2*1.5
	}

}