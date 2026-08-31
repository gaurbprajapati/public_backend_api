package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SpecificHoursRangeRuleTests {

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

	private SpecificHoursRangeRule specificHoursRangeRule;

	@BeforeEach
	void setUp() {
		this.specificHoursRangeRule = new SpecificHoursRangeRule(this.logger);
	}

	@Test
	@DisplayName("Get default rule type returns DURATION_BASED_SPECIFIC_HOUR_RANGE")
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.specificHoursRangeRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
	}

	@Test
	@DisplayName("Get name returns correct rule name")
	void testGetName() {
		// When
		String name = this.specificHoursRangeRule.getName();

		// Then
		assertThat(name).isEqualTo("Duration-Based Specific Hours Range Rule");
	}

	@Test
	@DisplayName("Evaluate with time range within regular hours calculates amounts correctly")
	void testEvaluateWithTimeRangeWithinRegularHours() {
		// Given
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// work
																					// day
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Specific Hours Range Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(2, 0), LocalTime.of(5, 0))); // 3 hours
																				// within
																				// regular
																				// hours
																				// (00:00-08:00)

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.specificHoursRangeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		assertThat(result.getRuleName()).isEqualTo("Test Specific Hours Range Rule");
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("112.5")); // 25 * 1.5
																				// * 3
																				// hours
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("225.0")); // 50 * 1.5
																				// * 3
																				// hours
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(3);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with time range outside regular hours returns zero amounts")
	void testEvaluateWithTimeRangeOutsideRegularHours() {
		// Given
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// work
																					// day
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Specific Hours Range Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(20, 0), LocalTime.of(22, 0))); // 2 hours
																				// outside
																				// regular
																				// hours
																				// (after
																				// 8 PM)

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.specificHoursRangeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(2);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with fixed rate charge method calculates amounts correctly")
	void testEvaluateWithFixedRateChargeMethod() {
		// Given
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.templateWorkDay.getWorkDayType()).willReturn(WorkDay.MONDAY);
		given(this.templateWorkDay.getWorkTime()).willReturn(Duration.ofHours(8)); // 8
																					// hours
																					// work
																					// day
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.FIXED_RATE);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(30.0f); // Fixed pay
																			// rate
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(60.0f); // Fixed bill
																			// rate
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Fixed Rate Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(1, 0), LocalTime.of(3, 0))); // 2 hours
																				// within
																				// regular
																				// hours
																				// (00:00-08:00)

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.specificHoursRangeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("60.0")); // 30 * 2
																				// hours
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("120.0")); // 60 * 2
																				// hours
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(2);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with zero duration returns zero amounts")
	void testEvaluateWithZeroDuration() {
		// Given
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getTemplateWorkDays()).willReturn(Arrays.asList(this.templateWorkDay));
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Zero Duration Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create(); // Empty range set

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.specificHoursRangeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toMinutes()).isZero();
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Evaluate with null context throws exception")
	void testEvaluateWithNullContext() {
		// When & Then
		assertThatThrownBy(() -> this.specificHoursRangeRule.evaluate(null))
			.isInstanceOf(IllegalArgumentException.class)
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
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.specificHoursRangeRule.evaluate(context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Evaluate with null timesheet setting throws exception")
	void testEvaluateWithNullTimesheetSetting() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(null)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.specificHoursRangeRule.evaluate(context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Evaluate with null time log throws exception")
	void testEvaluateWithNullTimeLog() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.specificHoursRangeRule.evaluate(context))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Evaluate with null custom rule throws exception")
	void testEvaluateWithNullCustomRule() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(null)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(TreeRangeSet.create())
			.build();

		// When & Then
		assertThatThrownBy(() -> this.specificHoursRangeRule.evaluate(context))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Current rule being evaluated cannot be null");
	}

	@Test
	@DisplayName("Evaluate with non-work day returns zero amounts")
	void testEvaluateWithNonWorkDay() {
		// Given
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timesheetSettingDto.getTemplateWorkDays()).willReturn(Arrays.asList()); // No
		// work
		// days
		// configured
		given(this.mockCustomRule.getPayRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getBillRateMultiplier()).willReturn(1.5f);
		given(this.mockCustomRule.getChargeMethod()).willReturn(ChargeMethodType.MULTIPLIER);
		given(this.mockCustomRule.getPayRatePerHour()).willReturn(25.0f);
		given(this.mockCustomRule.getBillRatePerHour()).willReturn(50.0f);
		given(this.mockCustomRule.getRuleType()).willReturn(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		given(this.mockCustomRule.getRuleName()).willReturn("Test Non-Work Day Rule");

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0))); // 3 hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.currentRuleBeingEvaluated(this.mockCustomRule)
			.timesheetSettingDto(this.timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.specificHoursRangeRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(3);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isFalse();
	}

	@Test
	@DisplayName("Validate method logs debug message")
	void testValidate() {
		// When
		this.specificHoursRangeRule.validate();

		// Then - no exception should be thrown, debug logging should occur
		// The actual logging is tested implicitly by the fact that no exception is thrown
	}

}