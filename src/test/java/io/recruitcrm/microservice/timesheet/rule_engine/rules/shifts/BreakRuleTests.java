package io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
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
class BreakRuleTests {

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private TimeLog timeLog1;

	@Mock
	private Logger logger;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto;

	private BreakRule breakRule;

	@BeforeEach
	void setUp() {
		this.breakRule = new BreakRule(this.logger);
		// No lenient stubbing; only stub what is needed in each test
	}

	@Test
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.breakRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_BREAK);
	}

	@Test
	void testGetName() {
		// When
		String name = this.breakRule.getName();

		// Then
		assertThat(name).isEqualTo("Range-Based Break Rule");
	}

	@Test
	void testEvaluateWithValidContext() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BREAK);
	}

	@Test
	void testEvaluateWithDayOff() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithNullContext() {
		// When & Then
		assertThatThrownBy(() -> this.breakRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
	void testEvaluateWithNullTimesheet() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(null)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.breakRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimesheetSetting() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(null)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.breakRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testEvaluateWithNullTimeLog() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BREAK);
		assertThat(result.getEvaluationDate()).isNull(); // Should be null when time log
															// is null
		assertThat(result.getMetadata()).contains("Date: Weekly Evaluation");
	}

	@Test
	void testEvaluateWithWorkDay() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(Boolean.TRUE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(14, 0), LocalTime.of(15, 0)));

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BREAK);
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("25.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("50.0"));
	}

	@Test
	void testEvaluateWithCalculateBreakTimeTrueAndNonZeroDuration() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(Boolean.TRUE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(12, 0))); // 2
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("40.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("80.0"));
	}

	@Test
	void testEvaluateWithCalculateBreakTimeFalseAndNonZeroDuration() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(Boolean.FALSE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(12, 0))); // 2
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithCalculateBreakTimeNullAndNonZeroDuration() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(20.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(40.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(null);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(12, 0))); // 2
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithCalculateBreakTimeTrueAndPartialHour() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(30.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(60.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(Boolean.TRUE);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(com.google.common.collect.Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 30))); // 1.5
																												// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.breakRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("45.0"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("90.0"));
	}

}
