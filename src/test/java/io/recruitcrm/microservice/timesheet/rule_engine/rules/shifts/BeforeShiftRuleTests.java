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

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BeforeShiftRuleTests {

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

	private BeforeShiftRule beforeShiftRule;

	@BeforeEach
	void setUp() {
		this.beforeShiftRule = new BeforeShiftRule(this.logger);
		// No lenient stubbing; only stub what is needed in each test
	}

	@Test
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.beforeShiftRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_BEFORE_SHIFT);
	}

	@Test
	void testGetName() {
		// When
		String name = this.beforeShiftRule.getName();

		// Then
		assertThat(name).isEqualTo("Range-Based Before Shift Rule");
	}

	@Test
	void testEvaluateWithValidContext() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.RANGE_BASED_BEFORE_SHIFT);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Before Shift Rule");
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.0f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.0f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(null);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(null);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.beforeShiftRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BEFORE_SHIFT);
	}

	@Test
	void testEvaluateWithNullContextThrowsException() {
		// When & Then
		assertThatThrownBy(() -> this.beforeShiftRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
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
		assertThatThrownBy(() -> this.beforeShiftRule.evaluate(context)).isInstanceOf(NullPointerException.class);
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
		assertThatThrownBy(() -> this.beforeShiftRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimeLog() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.RANGE_BASED_BEFORE_SHIFT);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Before Shift Rule");
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.0f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.0f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(null);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(null);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.beforeShiftRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BEFORE_SHIFT);
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
		assertThatThrownBy(() -> this.beforeShiftRule.evaluate(context)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Current rule being evaluated cannot be null");
	}

}