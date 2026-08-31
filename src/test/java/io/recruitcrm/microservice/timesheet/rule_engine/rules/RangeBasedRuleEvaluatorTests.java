package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogIntervalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RangeBasedRuleEvaluatorTests {

	@Mock
	private IRuleFactory ruleFactory;

	@Mock
	private Logger logger;

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private IEvaluatableRule mockRule;

	@Mock
	private ITimeLogIntervalRepository timeLogIntervalRepository;

	private RangeBasedRuleEvaluator evaluator;

	@BeforeEach
	void setUp() {
		this.evaluator = new RangeBasedRuleEvaluator(this.ruleFactory, this.logger, this.timeLogIntervalRepository);
	}

	@Test
	void testIsWeeklyOvertimeRuleWithWeeklyOvertimeRuleReturnsTrue() {
		given(this.mockRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		Timesheet weeklyTimesheet = createMockTimesheetWithFrequency(TimesheetFrequency.WEEKLY);

		boolean result = this.evaluator.isWeeklyOvertimeRule(this.mockRule, weeklyTimesheet);

		assertThat(result).isTrue();
	}

	@Test
	void testIsWeeklyOvertimeRuleWithNonWeeklyOvertimeRuleReturnsFalse() {
		given(this.mockRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		Timesheet weeklyTimesheet = createMockTimesheetWithFrequency(TimesheetFrequency.WEEKLY);

		boolean result = this.evaluator.isWeeklyOvertimeRule(this.mockRule, weeklyTimesheet);

		assertThat(result).isFalse();
	}

	@Test
	void testIsWeeklyOvertimeRuleWithMonthlyFrequencyReturnsFalse() {
		given(this.mockRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		Timesheet monthlyTimesheet = createMockTimesheetWithFrequency(TimesheetFrequency.MONTHLY);

		boolean result = this.evaluator.isWeeklyOvertimeRule(this.mockRule, monthlyTimesheet);

		assertThat(result).isFalse();
	}

	@Test
	void testIsWeeklyOvertimeRuleWithBiweeklyFrequencyReturnsTrue() {
		given(this.mockRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		Timesheet biweeklyTimesheet = createMockTimesheetWithFrequency(TimesheetFrequency.BIWEEKLY);

		boolean result = this.evaluator.isWeeklyOvertimeRule(this.mockRule, biweeklyTimesheet);

		assertThat(result).isTrue();
	}

	@Test
	void testIsWeeklyOvertimeRuleWithNullTimesheetFrequencyReturnsTrue() {
		given(this.mockRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		Timesheet nullFrequencyTimesheet = createMockTimesheetWithNullableFrequency(null);

		boolean result = this.evaluator.isWeeklyOvertimeRule(this.mockRule, nullFrequencyTimesheet);

		assertThat(result).isTrue();
	}

	@Test
	void testIsWeeklyOvertimeRuleWithZeroTimesheetFrequencyReturnsTrue() {
		given(this.mockRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		Timesheet zeroFrequencyTimesheet = createMockTimesheetWithNullableFrequency(0);

		boolean result = this.evaluator.isWeeklyOvertimeRule(this.mockRule, zeroFrequencyTimesheet);

		assertThat(result).isTrue();
	}

	@Test
	void testGetRegularHoursRuleTypeReturnsRangeBasedRegularHours() {
		RuleType result = this.evaluator.getRegularHoursRuleType();

		assertThat(result).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
	}

	@Test
	void testGetBreakRuleTypeReturnsRangeBasedBreak() {
		RuleType result = this.evaluator.getBreakRuleType();

		assertThat(result).isEqualTo(RuleType.RANGE_BASED_BREAK);
	}

	@Test
	void testGetDefaultPayRuleTypeReturnsRangeBasedDefaultPay() {
		RuleType result = this.evaluator.getDefaultPayRuleType();

		assertThat(result).isEqualTo(RuleType.RANGE_BASED_DEFAULT_PAY);
	}

	@Test
	void testEvaluateRulesWithValidTimesheetReturnsWeeklyResult() {
		// Mock a valid time log with only the necessary methods
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDate()).willReturn(1703116800); // 2023-12-21 in seconds
															// (Thursday)
		given(timeLog.getDayTypeId()).willReturn(1); // WORK_DAY
		given(timeLog.getWorkTime()).willReturn(null); // Range-based time log
		given(timeLog.getWorkStartTime()).willReturn(32400); // 9:00 AM in seconds since
																// midnight
		given(timeLog.getWorkEndTime()).willReturn(61200); // 5:00 PM in seconds since
															// midnight
		given(timeLog.getTimesheet()).willReturn(this.timesheet); // Reference to
																	// timesheet

		List<TimeLog> timeLogs = List.of(timeLog);

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(timeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(1); // Monday
		given(this.timesheetSetting.getWorkLogType()).willReturn(2); // START_AND_END_TIME
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>()); // No
																					// custom
																					// rules
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>()); // No
																							// template
																							// work
																							// days

		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		// Mock time range resolver for Break rule (Regular Hours won't be created due to
		// empty work days)
		ICustomRuleTimeRangeResolver breakResolver = mock(ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();

		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);

		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	void testEvaluateRulesMonthlyStartAndEndCalendarStartDayTenDoesNotThrow() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDate()).willReturn(1703116800);
		given(timeLog.getDayTypeId()).willReturn(1);
		given(timeLog.getWorkTime()).willReturn(null);
		given(timeLog.getWorkStartTime()).willReturn(32400);
		given(timeLog.getWorkEndTime()).willReturn(61200);
		given(timeLog.getTimesheet()).willReturn(this.timesheet);

		List<TimeLog> timeLogs = List.of(timeLog);

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(timeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(10);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>());

		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		ICustomRuleTimeRangeResolver breakResolver = mock(ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	void testEvaluateRulesMonthlyFrequencySplitsLogsIntoMultipleWeeksWhenSpanningIsoWeeks() {
		TimeLog timeLogWeek1 = mock(TimeLog.class);
		given(timeLogWeek1.getId()).willReturn(1);
		given(timeLogWeek1.getDate()).willReturn(1705296000);
		given(timeLogWeek1.getDayTypeId()).willReturn(1);
		given(timeLogWeek1.getWorkTime()).willReturn(null);
		given(timeLogWeek1.getWorkStartTime()).willReturn(32400);
		given(timeLogWeek1.getWorkEndTime()).willReturn(61200);
		given(timeLogWeek1.getTimesheet()).willReturn(this.timesheet);

		TimeLog timeLogWeek2 = mock(TimeLog.class);
		given(timeLogWeek2.getId()).willReturn(2);
		given(timeLogWeek2.getDate()).willReturn(1705977600);
		given(timeLogWeek2.getDayTypeId()).willReturn(1);
		given(timeLogWeek2.getWorkTime()).willReturn(null);
		given(timeLogWeek2.getWorkStartTime()).willReturn(32400);
		given(timeLogWeek2.getWorkEndTime()).willReturn(61200);
		given(timeLogWeek2.getTimesheet()).willReturn(this.timesheet);

		List<TimeLog> timeLogs = List.of(timeLogWeek1, timeLogWeek2);

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(timeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(15);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>());

		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		ICustomRuleTimeRangeResolver breakResolver = mock(ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);

		assertThat(result).isNotNull();
		assertThat(result.getWeekCount()).isEqualTo(2);
	}

	@Test
	void testEvaluateRulesWeeklyStartAndEndUsesWeekStartFromSettingsWhenNotMonthly() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDate()).willReturn(1703116800);
		given(timeLog.getDayTypeId()).willReturn(1);
		given(timeLog.getWorkTime()).willReturn(null);
		given(timeLog.getWorkStartTime()).willReturn(32400);
		given(timeLog.getWorkEndTime()).willReturn(61200);
		given(timeLog.getTimesheet()).willReturn(this.timesheet);

		List<TimeLog> timeLogs = List.of(timeLog);

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(timeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(3);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.WEEKLY.getFrequencyId()));
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>());

		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		ICustomRuleTimeRangeResolver breakResolver = mock(ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	void testEvaluateRulesWorkHourDelegatesToSuperEvaluateRules() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDate()).willReturn(1703116800);
		given(timeLog.getDayTypeId()).willReturn(1);
		given(timeLog.getWorkTime()).willReturn(28800);
		List<TimeLog> timeLogs = List.of(timeLog);

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(timeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>());

		ICustomRuleTimeRangeResolver breakResolver = mock(ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	void testEvaluateRulesWithEmptyTimeLogsThrowsException() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(new ArrayList<>());

		assertThatThrownBy(() -> this.evaluator.evaluateRules(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testEvaluateRulesWithNullTimesheetThrowsException() {
		assertThatThrownBy(() -> this.evaluator.evaluateRules(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testEvaluateRulesWithNullTimesheetSettingThrowsException() {
		// No stubbing needed since exception is thrown before any stubs are accessed
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		assertThatThrownBy(() -> this.evaluator.evaluateRules(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class);
	}

	private Timesheet createMockTimesheetWithFrequency(TimesheetFrequency frequency) {
		TimesheetSetting localTimesheetSetting = new TimesheetSetting();
		localTimesheetSetting.setTimesheetFrequency(frequency.getFrequencyId());

		Timesheet localTimesheet = new Timesheet();
		localTimesheet.setTimesheetSetting(localTimesheetSetting);

		return localTimesheet;
	}

	private Timesheet createMockTimesheetWithNullableFrequency(Integer frequencyId) {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setTimesheetFrequency(frequencyId);
		Timesheet ts = new Timesheet();
		ts.setTimesheetSetting(setting);
		return ts;
	}

}
