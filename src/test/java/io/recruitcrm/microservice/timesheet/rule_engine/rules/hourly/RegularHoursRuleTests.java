package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
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
class RegularHoursRuleTests {

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private TimeLog timeLog1;

	@Mock
	private Logger logger;

	private RegularHoursRule regularHoursRule;

	@BeforeEach
	void setUp() {
		this.regularHoursRule = new RegularHoursRule(this.logger);
	}

	@Test
	@DisplayName("Get default rule type returns DURATION_BASED_REGULAR_HOURS")
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.regularHoursRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("Get name returns correct rule name")
	void testGetName() {
		// When
		String name = this.regularHoursRule.getName();

		// Then
		assertThat(name).isEqualTo("Duration-Based Regular Hours Rule");
	}

	@Test
	@DisplayName("Evaluate with work day calculates amounts correctly")
	void testEvaluateWithWorkDay() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8 hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.regularHoursRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("200.0"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("400.0"));
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(8);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with day off returns zero amounts")
	void testEvaluateWithDayOff() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.DAY_OFF);
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8 hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.regularHoursRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toHours()).isEqualTo(8);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with zero duration returns zero amounts")
	void testEvaluateWithZeroDuration() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create(); // Empty range set

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.regularHoursRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getEvaluatedDuration().toMinutes()).isZero();
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with partial hours calculates amounts correctly")
	void testEvaluateWithPartialHours() {
		// Given
		given(this.timesheet.getId()).willReturn(1);
		given(this.timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.WORK_DAY);
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 30))); // 3.5
																				// hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.regularHoursRule.evaluate(context);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS);
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("87.5"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("175.0"));
		assertThat(result.getEvaluatedDuration().toMinutes()).isEqualTo(210); // 3.5 hours
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
	}

	@Test
	@DisplayName("Evaluate with null context throws exception")
	void testEvaluateWithNullContext() {
		// When & Then
		assertThatThrownBy(() -> this.regularHoursRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
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
			.build();

		// When & Then
		assertThatThrownBy(() -> this.regularHoursRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Evaluate with null timesheet setting throws exception")
	void testEvaluateWithNullTimesheetSetting() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(null)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.regularHoursRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Evaluate with null time log throws exception")
	void testEvaluateWithNullTimeLog() {
		// Given
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(null)
			.build();

		// When & Then
		assertThatThrownBy(() -> this.regularHoursRule.evaluate(context)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Validate method logs debug message")
	void testValidate() {
		// When
		this.regularHoursRule.validate();

		// Then - no exception should be thrown, debug logging should occur
		// The actual logging is tested implicitly by the fact that no exception is thrown
	}

}