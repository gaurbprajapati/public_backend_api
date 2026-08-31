package io.recruitcrm.microservice.timesheet.rule_engine.rules.shifts;

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
		// No lenient stubbing; only stub what is needed in each test
	}

	@Test
	void testGetDefaultRuleType() {
		// When
		RuleType ruleType = this.regularHoursRule.getDefaultRuleType();

		// Then
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
	}

	@Test
	void testGetName() {
		// When
		String name = this.regularHoursRule.getName();

		// Then
		assertThat(name).isEqualTo("Range-Based Regular Hours Rule");
	}

	@Test
	void testEvaluateWithValidContext() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.WORK_DAY);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.regularHoursRule.evaluate(context);

		// Then — virtual rule: no custom rule in context, so multipliers stay null
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
		assertThat(result.getPayRateMultiplier()).isNull();
		assertThat(result.getBillRateMultiplier()).isNull();
	}

	@Test
	void testEvaluateWithDayOff() {
		// Given
		given(this.timesheetSetting.getPayRate()).willReturn(25.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(50.0f);
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.DAY_OFF);

		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();

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
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	void testEvaluateWithNullContext() {
		// When & Then
		assertThatThrownBy(() -> this.regularHoursRule.evaluate(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
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
	void testEvaluateWithWorkDay() {
		// Given - Test with a work day that has specific work hours and different rates
		given(this.timesheetSetting.getPayRate()).willReturn(30.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(60.0f);
		given(this.timeLog1.getDayType()).willReturn(WorkDayType.WORK_DAY);

		// Create a time range that represents a full work day
		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		timeRanges.add(Range.closed(LocalTime.of(8, 0), LocalTime.of(18, 0))); // 10 hours

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.currentTimeLogBeingEvaluated(this.timeLog1)
			.timeRangesToEvaluate(timeRanges)
			.build();

		// When
		RuleEvaluationResult result = this.regularHoursRule.evaluate(context);

		// Then - Verify work day specific behavior with different rates and hours
		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.isVirtualRule()).isTrue();
		// Additional assertions specific to work day scenario
		assertThat(result.getPayAmount()).isNotNull();
		assertThat(result.getBillAmount()).isNotNull();
		assertThat(result.getEvaluatedDuration()).isNotNull();
	}

}