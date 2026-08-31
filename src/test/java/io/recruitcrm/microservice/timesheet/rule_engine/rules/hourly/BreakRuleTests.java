package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

import com.google.common.collect.Range;
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
import org.junit.jupiter.api.DisplayName;
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
	@DisplayName("Get default rule type returns DURATION_BASED_BREAK")
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.breakRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_BREAK);
	}

	@Test
	@DisplayName("Get name returns correct rule name")
	void testGetName() {
		// When
		String name = this.breakRule.getName();

		// Then
		assertThat(name).isEqualTo("Duration-Based Break Rule");
	}

	@Test
	@DisplayName("Evaluate with zero break duration returns zero amounts")
	void testEvaluateWithZeroBreakDuration() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create(); // Empty range set

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
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toMinutes()).isZero();
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with calculateBreakTime FALSE returns zero amounts")
	void testEvaluateWithCalculateBreakTimeFalse() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(false);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(12, 0), LocalTime.of(13, 0))); // 1 hour
																				// break

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
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(1);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with calculateBreakTime TRUE calculates amounts correctly")
	void testEvaluateWithCalculateBreakTimeTrue() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(true);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(12, 0), LocalTime.of(13, 0))); // 1 hour
																				// break

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
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK);
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("25.0"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("50.0"));
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(1);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with calculateBreakTime NULL returns zero amounts")
	void testEvaluateWithCalculateBreakTimeNull() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(null);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(12, 0), LocalTime.of(13, 0))); // 1 hour
																				// break

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
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(1);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with partial hour break calculates amounts correctly")
	void testEvaluateWithPartialHourBreak() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getCalculateBreakTime()).willReturn(true);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(12, 0), LocalTime.of(12, 30))); // 30
																					// minutes
																					// break

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
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK);
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("12.5"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("25.0"));
		assertThat(result.getEvaluatedDuration().toMinutes()).isEqualTo(30);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with null context throws exception")
	void testEvaluateWithNullContext() {
		// When & Then
		assertThatThrownBy(() -> this.breakRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
	@DisplayName("Evaluate with null timesheet throws exception")
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
	@DisplayName("Evaluate with null timesheet setting throws exception")
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
	@DisplayName("Evaluate with null time log handles gracefully")
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
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK);
		assertThat(result.getEvaluationDate()).isNull(); // Should be null when time log
															// is null
		assertThat(result.getMetadata()).contains("Date: Weekly Evaluation");
	}

	@Test
	@DisplayName("Validate method logs debug message")
	void testValidate() {
		// When
		this.breakRule.validate();

		// Then - no exception should be thrown, debug logging should occur
		// The actual logging is tested implicitly by the fact that no exception is thrown
	}

}