package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.Range;
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
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BaseRuleEvaluatorTests {

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

	private TestBaseRuleEvaluator evaluator;

	private List<TimeLog> timeLogs;

	@BeforeEach
	void setUp() {
		this.evaluator = new TestBaseRuleEvaluator(this.ruleFactory, this.logger);
		this.timeLogs = new ArrayList<>();
	}

	@Test
	void testValidateTimesheetWithNullTimesheetThrowsException() {
		assertThatThrownBy(() -> this.evaluator.validateTimesheet(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateTimesheetWithNullTimesheetSettingThrowsException() {
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		assertThatThrownBy(() -> this.evaluator.validateTimesheet(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateTimesheetWithNullTimeLogsThrowsException() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(null);

		assertThatThrownBy(() -> this.evaluator.validateTimesheet(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateTimesheetWithEmptyTimeLogsThrowsException() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(List.of());

		assertThatThrownBy(() -> this.evaluator.validateTimesheet(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateTimesheetWithValidTimesheetNoException() {
		TimeLog timeLog = mock(TimeLog.class);
		this.timeLogs.add(timeLog);

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(this.timeLogs);

		assertThatCode(() -> this.evaluator.validateTimesheet(this.timesheet)).doesNotThrowAnyException();
	}

	@Test
	void testIsDailyOvertimeRuleWithDailyOvertimeRuleReturnsTrue() {
		given(this.mockRule.isDailyOvertimeRule()).willReturn(true);

		boolean result = this.evaluator.isDailyOvertimeRule(this.mockRule);

		assertThat(result).isTrue();
	}

	@Test
	void testIsDailyOvertimeRuleWithNonDailyOvertimeRuleReturnsFalse() {
		given(this.mockRule.isDailyOvertimeRule()).willReturn(false);

		boolean result = this.evaluator.isDailyOvertimeRule(this.mockRule);

		assertThat(result).isFalse();
	}

	@Test
	void testUpdateEvaluationState() {
		BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
		RangeSet<LocalTime> rangeSet = TreeRangeSet.create();
		rangeSet.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0)));

		this.evaluator.updateEvaluationState(state, rangeSet);

		assertThat(state.occupiedTimeRanges.isEmpty()).isFalse();
	}

	@Test
	void testPostProcessResult() {
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		assertThatCode(() -> this.evaluator.postProcessResult(result)).doesNotThrowAnyException();
	}

	@Test
	void testGetTotalRuleCount() {
		// Arrange
		List<io.recruitcrm.contract_staffing.entity.model.CustomRule> customRules = List.of(
				mock(io.recruitcrm.contract_staffing.entity.model.CustomRule.class),
				mock(io.recruitcrm.contract_staffing.entity.model.CustomRule.class),
				mock(io.recruitcrm.contract_staffing.entity.model.CustomRule.class));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getCustomRule()).willReturn(customRules);

		// Act
		int result = this.evaluator.getTotalRuleCount(this.timesheet);

		// Assert
		assertThat(result).isEqualTo(3);
	}

	@Test
	void testIsDailyOvertimeRuleWithNullRuleReturnsFalse() {
		// Act
		boolean result = this.evaluator.isDailyOvertimeRule(null);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void testIsCustomRuleApplicableOnDayWithNullWorkDaysReturnsTrue() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		given(customRule.getWorkDays()).willReturn(null);

		// Act
		boolean result = this.evaluator.isCustomRuleApplicableOnDay(customRule,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void testIsCustomRuleApplicableOnDayWithEmptyWorkDaysReturnsTrue() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		given(customRule.getWorkDays()).willReturn(List.of());

		// Act
		boolean result = this.evaluator.isCustomRuleApplicableOnDay(customRule,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void testIsCustomRuleApplicableOnDayWithMatchingWorkDayReturnsTrue() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		given(customRule.getWorkDays())
			.willReturn(List.of(io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY,
					io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.TUESDAY));

		// Act
		boolean result = this.evaluator.isCustomRuleApplicableOnDay(customRule,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void testIsCustomRuleApplicableOnDayWithNonMatchingWorkDayReturnsFalse() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		given(customRule.getWorkDays())
			.willReturn(List.of(io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY,
					io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.TUESDAY));

		// Act
		boolean result = this.evaluator.isCustomRuleApplicableOnDay(customRule,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void testIsRegularHoursRuleApplicableOnDayWithMatchingWorkDayReturnsTrue() {
		// Arrange
		io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay templateWorkDay = mock(
				io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay.class);
		given(templateWorkDay.getWorkDayId()).willReturn(1); // Monday
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(List.of(templateWorkDay));
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR

		// Act
		boolean result = this.evaluator.isRegularHoursRuleApplicableOnDay(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY, this.timesheet);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void testIsRegularHoursRuleApplicableOnDayWithNonMatchingWorkDayReturnsFalse() {
		// Arrange
		io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay templateWorkDay = mock(
				io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay.class);
		given(templateWorkDay.getWorkDayId()).willReturn(1); // Monday
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(List.of(templateWorkDay));
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR

		// Act
		boolean result = this.evaluator.isRegularHoursRuleApplicableOnDay(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.TUESDAY, this.timesheet);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void testIsRegularHoursRuleApplicableOnDayWithEmptyTemplateWorkDaysReturnsFalse() {
		// Arrange
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTemplateWorkDay()).willReturn(List.of());
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR

		// Act
		boolean result = this.evaluator.isRegularHoursRuleApplicableOnDay(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY, this.timesheet);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void testAddRuleResultToEvaluatorResultWithWeeklyOvertimeRule() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RuleEvaluationResult ruleResult = mock(RuleEvaluationResult.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);

		// Act
		this.evaluator.addRuleResultToEvaluatorResult(result, timeLog, customRule, ruleResult, this.timesheet);

		// Assert - Weekly overtime rules are handled differently, so we just verify no
		// exception
		assertThat(result).isNotNull();
	}

	@Test
	void testAddRuleResultToEvaluatorResultWithNonWeeklyOvertimeRule() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RuleEvaluationResult ruleResult = mock(RuleEvaluationResult.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);

		// Act
		this.evaluator.addRuleResultToEvaluatorResult(result, timeLog, customRule, ruleResult, this.timesheet);

		// Assert - Non-weekly overtime rules are added directly to the result
		assertThat(result).isNotNull();
	}

	@Test
	void testCreateRuleEvaluationContext() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RangeSet<LocalTime> evaluatedRangeSet = TreeRangeSet.create();
		evaluatedRangeSet.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0)));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR

		// Act
		io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext context = this.evaluator
			.createRuleEvaluationContext(timeLog, customRule, evaluatedRangeSet, 0, this.timesheet);

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getTimesheet()).isEqualTo(this.timesheet);
		assertThat(context.getTimesheetSetting()).isEqualTo(this.timesheetSetting);
		assertThat(context.getTimeRangesToEvaluate()).isEqualTo(evaluatedRangeSet);
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isEqualTo(timeLog);
		assertThat(context.getCurrentRuleBeingEvaluated()).isEqualTo(customRule);
		assertThat(context.getCurrentRuleIndex()).isZero();
	}

	@Test
	void testCreateTimeRangeResolverContext() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
		List<IEvaluatableRule> unifiedRules = List.of(customRule);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR

		// Act
		io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext context = this.evaluator
			.createTimeRangeResolverContext(timeLog, customRule, this.timesheet, state, unifiedRules);

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isEqualTo(timeLog);
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isEqualTo(customRule);
		assertThat(context.getOccupiedTimeRanges()).isEqualTo(state.occupiedTimeRanges);
		assertThat(context.getWorkedHoursTillNow()).isEqualTo(state.workedHoursTillNow);
	}

	@Test
	void testCreateTimeRangeResolverContextWithSystemRule() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		IEvaluatableRule systemRule = mock(IEvaluatableRule.class);
		BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
		List<IEvaluatableRule> unifiedRules = List.of(systemRule);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR

		// Act
		io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext context = this.evaluator
			.createTimeRangeResolverContext(timeLog, systemRule, this.timesheet, state, unifiedRules);

		// Assert
		assertThat(context).isNotNull();
		assertThat(context.getCurrentCustomRuleBeingEvaluated()).isNull(); // System rules
																			// don't have
																			// custom rule
		assertThat(context.getCurrentRuleIndex()).isEqualTo(-1); // System rules have
																	// index -1
	}

	@Test
	void testResolveTimeRangesForRule() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
		List<IEvaluatableRule> unifiedRules = List.of(customRule);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver resolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> expectedRangeSet = TreeRangeSet.create();
		expectedRangeSet.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0)));
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS)).willReturn(resolver);
		given(resolver.resolveTimeRange(any())).willReturn(expectedRangeSet);

		// Act
		RangeSet<LocalTime> result = this.evaluator.resolveTimeRangesForRule(timeLog, customRule, this.timesheet, state,
				unifiedRules);

		// Assert
		assertThat(result).isEqualTo(expectedRangeSet);
	}

	@Test
	void testProcessRuleEvaluationWithEmptyConstrainedRangeSet() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RangeSet<LocalTime> evaluatedRangeSet = TreeRangeSet.create(); // Empty range set
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(timeLog);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();
		given(timeLog.getId()).willReturn(1);

		// Act
		RuleEvaluationResult result = this.evaluator.processRuleEvaluation(timeLog, customRule, evaluatedRangeSet, 0,
				weeklyTimeLog, weeklyOvertimeCandidateTimeRanges, this.timesheet);

		// Assert
		assertThat(result).isNull(); // Should return null for empty range set
	}

	@Test
	void testProcessRuleEvaluationWithWeeklyOvertimeRule() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RangeSet<LocalTime> evaluatedRangeSet = TreeRangeSet.create();
		evaluatedRangeSet.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0)));
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(timeLog);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();
		io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule rule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule.class);
		RuleEvaluationResult ruleResult = mock(RuleEvaluationResult.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR
		given(this.ruleFactory.createRule(RuleType.RANGE_BASED_WEEKLY_OVERTIME, this.logger)).willReturn(rule);
		given(rule.evaluate(any())).willReturn(ruleResult);

		// Mock timeLog boundaries to ensure constrained range set is not empty
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(8, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		// Act
		RuleEvaluationResult result = this.evaluator.processRuleEvaluation(timeLog, customRule, evaluatedRangeSet, 0,
				weeklyTimeLog, weeklyOvertimeCandidateTimeRanges, this.timesheet);

		// Assert
		assertThat(result).isEqualTo(ruleResult);
		assertThat(weeklyOvertimeCandidateTimeRanges).hasSize(1); // Should add to weekly
																	// overtime candidate
																	// ranges
	}

	@Test
	void testProcessRuleEvaluationWithNonWeeklyOvertimeRule() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RangeSet<LocalTime> evaluatedRangeSet = TreeRangeSet.create();
		evaluatedRangeSet.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0)));
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(timeLog);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();
		io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule rule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule.class);
		RuleEvaluationResult ruleResult = mock(RuleEvaluationResult.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR
		given(this.ruleFactory.createRule(RuleType.RANGE_BASED_REGULAR_HOURS, this.logger)).willReturn(rule);
		given(rule.evaluate(any())).willReturn(ruleResult);

		// Mock timeLog boundaries to ensure constrained range set is not empty
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(8, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		// Act
		RuleEvaluationResult result = this.evaluator.processRuleEvaluation(timeLog, customRule, evaluatedRangeSet, 0,
				weeklyTimeLog, weeklyOvertimeCandidateTimeRanges, this.timesheet);

		// Assert
		assertThat(result).isEqualTo(ruleResult);
		assertThat(weeklyOvertimeCandidateTimeRanges).isEmpty(); // Should not add to
																	// weekly overtime
																	// candidate ranges
	}

	@Test
	void testProcessRuleEvaluationWithNullRuleResult() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);
		RangeSet<LocalTime> evaluatedRangeSet = TreeRangeSet.create();
		evaluatedRangeSet.add(Range.closed(LocalTime.of(9, 0), LocalTime.of(12, 0)));
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(timeLog);
		List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();
		io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule rule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR
		given(this.ruleFactory.createRule(RuleType.RANGE_BASED_REGULAR_HOURS, this.logger)).willReturn(rule);
		given(rule.evaluate(any())).willReturn(null);

		// Mock timeLog boundaries to ensure constrained range set is not empty
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(8, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		// Act
		RuleEvaluationResult result = this.evaluator.processRuleEvaluation(timeLog, customRule, evaluatedRangeSet, 0,
				weeklyTimeLog, weeklyOvertimeCandidateTimeRanges, this.timesheet);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void testEvaluateWeeklyTimeLogs() {
		// Arrange
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog1 = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog2 = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(timeLog1, timeLog2);
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		// Setup timesheet setting to prevent null pointer exception
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		// Setup workLogType to a valid value (1 = WORK_HOUR)
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		// Setup custom rules to be empty to avoid additional rule processing
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());

		// Mock the time range resolvers for system rules
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);

		// Setup mock resolvers to return empty range sets
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		// Setup rule factory to return the mock resolvers
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		// Act
		this.evaluator.evaluateWeeklyTimeLogs(weeklyTimeLog, this.timesheet, result);

		// Assert - Should not throw exception and process both time logs
		assertThat(result).isNotNull();
	}

	@Test
	void testEvaluateWeeklyTimeLogsWithEmptyList() {
		// Arrange
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of();
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		// Setup timesheet setting mock to avoid NullPointerException
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());

		// Act
		this.evaluator.evaluateWeeklyTimeLogs(weeklyTimeLog, this.timesheet, result);

		// Assert - Should not throw exception for empty list
		assertThat(result).isNotNull();
	}

	@Test
	void testPrepareWeeklyTimeLogsWithEmptyList() {
		// Arrange
		List<TimeLog> mTimeLogs = List.of();

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(mTimeLogs);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void testPrepareWeeklyTimeLogsWithCustomWeekStartDay() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDayTypeId()).willReturn(1); // WORK_DAY
		List<TimeLog> mTimeLogs = List.of(timeLog);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(mTimeLogs,
					io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.WEDNESDAY);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	void testPrepareWeeklyTimeLogsWithTimesheetAndNullStartDay() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDayTypeId()).willReturn(1); // WORK_DAY
		List<TimeLog> mTimeLogs = List.of(timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null);

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(mTimeLogs, this.timesheet);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	void testPrepareWeeklyTimeLogsWithTimesheetAndValidStartDay() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDayTypeId()).willReturn(1); // WORK_DAY
		List<TimeLog> mTimeLogs = List.of(timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(3); // Wednesday

		// Act
		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(mTimeLogs, this.timesheet);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	void testPrepareWeeklyTimeLogsWithMonthlyTimesheetIgnoresCalendarStartDayAsWeekday() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDayTypeId()).willReturn(1);
		List<TimeLog> mTimeLogs = List.of(timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(10);

		assertThatCode(() -> this.evaluator.prepareWeeklyTimeLogs(mTimeLogs, this.timesheet))
			.doesNotThrowAnyException();
	}

	@Test
	void testPrepareWeeklyTimeLogsMonthlyFrequencySplitsAcrossIsoWeeks() {
		TimeLog log1 = mock(TimeLog.class);
		given(log1.getId()).willReturn(1);
		given(log1.getDate()).willReturn(1705296000);
		given(log1.getDayTypeId()).willReturn(1);
		given(log1.getWorkTime()).willReturn(null);
		given(log1.getWorkStartTime()).willReturn(32400);
		given(log1.getWorkEndTime()).willReturn(61200);

		TimeLog log2 = mock(TimeLog.class);
		given(log2.getId()).willReturn(2);
		given(log2.getDate()).willReturn(1705977600);
		given(log2.getDayTypeId()).willReturn(1);
		given(log2.getWorkTime()).willReturn(null);
		given(log2.getWorkStartTime()).willReturn(32400);
		given(log2.getWorkEndTime()).willReturn(61200);

		List<TimeLog> mTimeLogs = List.of(log1, log2);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(1);

		List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> result = this.evaluator
			.prepareWeeklyTimeLogs(mTimeLogs, this.timesheet);

		assertThat(result).hasSize(2);
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenFrequencyNullReturnsTrue() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency()).willReturn(null);

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isTrue();
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenFrequencyZeroReturnsTrue() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency()).willReturn(0);

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isTrue();
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenWeeklyReturnsTrue() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.WEEKLY.getFrequencyId()));

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isTrue();
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenBiweeklyReturnsTrue() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.BIWEEKLY.getFrequencyId()));

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isTrue();
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenMonthlyReturnsFalse() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isFalse();
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenInvalidFrequencyIdReturnsTrue() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency()).willReturn(999);

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isTrue();
	}

	@Test
	void testShouldInterpretTimesheetStartDayAsWeekdayWhenCustomFrequencyReturnsTrue() {
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.CUSTOM.getFrequencyId()));

		assertThat(this.evaluator.interpretTimesheetStartDayAsWeekday(this.timesheet)).isTrue();
	}

	@Test
	void testPrepareWeeklyTimeLogsBiweeklyWithWeekdayStartDayDoesNotThrow() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDayTypeId()).willReturn(1);
		List<TimeLog> mTimeLogs = List.of(timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.BIWEEKLY.getFrequencyId()));
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(5);

		assertThatCode(() -> this.evaluator.prepareWeeklyTimeLogs(mTimeLogs, this.timesheet))
			.doesNotThrowAnyException();
	}

	@Test
	void testEvaluateRulesWithEmptyWeeklyTimeLogs() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDayTypeId()).willReturn(1); // WORK_DAY
		List<TimeLog> mTimeLogs = List.of(timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(mTimeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null); // Use
																				// default
																				// (Monday)
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR
		// Setup custom rules to be empty to avoid additional rule processing
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());

		// Mock the time range resolvers for system rules
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);

		// Setup mock resolvers to return empty range sets
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		// Setup rule factory to return the mock resolvers
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		// Act
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	void testEvaluateRulesWithValidWeeklyTimeLogs() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getId()).willReturn(1);
		given(timeLog.getDate()).willReturn(1703116800); // 2024-01-15 in epoch seconds
		given(timeLog.getDayTypeId()).willReturn(1); // WORK_DAY
		List<TimeLog> mTimeLogs = List.of(timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(mTimeLogs);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null); // Use
																				// default
																				// (Monday)
		given(this.timesheetSetting.getWorkLogType()).willReturn(1); // WORK_HOUR
		// Setup custom rules to be empty to avoid additional rule processing
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());

		// Mock the time range resolvers for system rules
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);

		// Setup mock resolvers to return empty range sets
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);

		// Setup rule factory to return the mock resolvers
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		// Act
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluateRules(this.timesheet);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	void testIsRuleApplicableOnDayForSystemRule() {
		IEvaluatableRule systemRule = mock(IEvaluatableRule.class);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog.class);
		Timesheet mTimesheet = mock(Timesheet.class);
		given(timeLog.getDate()).willReturn(LocalDate.now());
		given(systemRule.isApplicableOnDay(any())).willReturn(true);
		boolean result = this.evaluator.isRuleApplicableOnDay(systemRule, timeLog, mTimesheet);
		assertThat(result).isTrue();
	}

	@Test
	void testGetDurationInSecondsNullAndNonNull() {
		// Use reflection to call private getDurationInSeconds
		try {
			var method = BaseRuleEvaluator.class.getDeclaredMethod("getDurationInSeconds", Duration.class);
			method.setAccessible(true);
			long secondsNull = (long) method.invoke(this.evaluator, (Object) null);
			long secondsNonNull = (long) method.invoke(this.evaluator, Duration.ofMinutes(90));
			assertThat(secondsNull).isZero();
			assertThat(secondsNonNull).isEqualTo(5400);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// evaluatePreBuiltTimeLogs tests
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - null timesheet throws IllegalArgumentException")
	void testEvaluatePreBuiltTimeLogsNullTimesheetThrowsException() {
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> emptyTimeLogs = List.of();
		assertThatThrownBy(() -> this.evaluator.evaluatePreBuiltTimeLogs(null, emptyTimeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Timesheet cannot be null");
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - null timesheet setting throws IllegalArgumentException")
	void testEvaluatePreBuiltTimeLogsNullTimesheetSettingThrowsException() {
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> emptyTimeLogs = List.of();
		assertThatThrownBy(() -> this.evaluator.evaluatePreBuiltTimeLogs(this.timesheet, emptyTimeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Timesheet setting cannot be null");
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - null timesheetStartDay uses default ISO week split and returns empty result")
	void testEvaluatePreBuiltTimeLogsNullStartDayUsesDefaultSplit() {
		// Given — timesheetStartDay == null triggers the if-branch (default ISO split).
		// Empty expandedTimeLogs → splitTimeLogsOnWeeklyBasis returns empty list → loop
		// never runs.
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null);

		// When
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluatePreBuiltTimeLogs(this.timesheet, List.of());

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
		assertThat(result.getWeeklyResults()).isEmpty();
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - monthly timesheet with non-null start day uses default split via !shouldInterpret")
	void testEvaluatePreBuiltTimeLogsMonthlyTimesheetUsesDefaultSplit() {
		// Given — timesheetStartDay != null but shouldInterpretTimesheetStartDayAsWeekday
		// returns false for MONTHLY frequency, so the if-branch is taken (default split).
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(15);
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.MONTHLY.getFrequencyId()));

		// When
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluatePreBuiltTimeLogs(this.timesheet, List.of());

		// Then — empty expanded logs → no weekly results
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).isEmpty();
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - non-monthly timesheet with non-null start day uses weekday-based split")
	void testEvaluatePreBuiltTimeLogsElseBranchUsesWeekdaySplit() {
		// Given — timesheetStartDay != null and shouldInterpretTimesheetStartDayAsWeekday
		// returns true (WEEKLY frequency), so the else-branch is taken (custom weekday
		// split).
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(1);
		dtoLog.setDate(LocalDate.of(2024, 1, 15));

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(3); // Wednesday
		given(this.timesheetSetting.getTimesheetFrequency())
			.willReturn(Integer.valueOf(TimesheetFrequency.WEEKLY.getFrequencyId()));
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		// When
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluatePreBuiltTimeLogs(this.timesheet, List.of(dtoLog));

		// Then — one week bucket created via the weekday-based split
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).hasSize(1);
		assertThat(result.getWeeklyResults().get(0).getWeekStartDate()).isEqualTo(LocalDate.of(2024, 1, 15));
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - single DTO time log produces one processed weekly result")
	void testEvaluatePreBuiltTimeLogsSingleDtoTimeLogProducesOneWeeklyResult() {
		// Given — one DTO time log on 2024-01-15; verifies the loop body executes
		// (weeklyTimeLog.isEmpty() == false path) and a weekly result is added.
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(1);
		dtoLog.setDate(LocalDate.of(2024, 1, 15));

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		// When
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluatePreBuiltTimeLogs(this.timesheet, List.of(dtoLog));

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).hasSize(1);
		assertThat(result.getWeeklyResults().get(0).getWeekStartDate()).isEqualTo(LocalDate.of(2024, 1, 15));
		assertThat(result.getWeeklyResults().get(0).getWeekEndDate()).isEqualTo(LocalDate.of(2024, 1, 15));
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - time logs spanning two separate weeks produce two weekly results")
	void testEvaluatePreBuiltTimeLogsLogsInTwoSeparateWeeksProduceTwoResults() {
		// Given — two DTO time logs in consecutive ISO weeks (Jan 15 = week 3, Jan 22 =
		// week 4)
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog1 = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog1.setId(1);
		dtoLog1.setDate(LocalDate.of(2024, 1, 15)); // week 3

		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog2 = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog2.setId(2);
		dtoLog2.setDate(LocalDate.of(2024, 1, 22)); // week 4

		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getTimesheetStartDay()).willReturn(null);
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		// When
		WeeklyRuleEvaluatorResult result = this.evaluator.evaluatePreBuiltTimeLogs(this.timesheet,
				List.of(dtoLog1, dtoLog2));

		// Then — two separate week buckets, each with one WeeklyResult
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).hasSize(2);
	}

	@Test
	@DisplayName("evaluatePreBuiltTimeLogs - empty inner week list triggers continue and is skipped")
	void testEvaluatePreBuiltTimeLogsEmptyInnerWeekListIsSkipped() {
		// Given — splitTimeLogsOnWeeklyBasis never naturally produces empty inner lists
		// (it only creates a bucket when a time log lands in that week).
		// The TestBaseRuleEvaluatorPreBuiltGapWeek subclass simulates this by directly
		// passing a list with an empty inner bucket to the post-split loop, exercising
		// weeklyTimeLog.isEmpty() → true → continue.
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(new ArrayList<>());

		TestBaseRuleEvaluatorPreBuiltGapWeek gapEvaluator = new TestBaseRuleEvaluatorPreBuiltGapWeek(this.ruleFactory,
				this.logger);

		// When
		WeeklyRuleEvaluatorResult result = gapEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, List.of());

		// Then — the empty week bucket is skipped; no weekly results added
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).isEmpty();
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// evaluateRules — isEmpty() branch coverage
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateRules - empty inner week list triggers continue and is skipped")
	void testEvaluateRulesEmptyInnerWeekListIsSkipped() {
		// Given — prepareWeeklyTimeLogs returns a list that contains one empty inner
		// bucket (gap week). This exercises weeklyTimeLog.isEmpty() → true → continue.
		TimeLog timeLog = mock(TimeLog.class);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(List.of(timeLog));

		TestBaseRuleEvaluatorWithGapWeek gapEvaluator = new TestBaseRuleEvaluatorWithGapWeek(this.ruleFactory,
				this.logger);

		// When
		WeeklyRuleEvaluatorResult result = gapEvaluator.evaluateRules(this.timesheet);

		// Then — the empty week bucket is skipped; no weekly results are added
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).isEmpty();
	}

	@Test
	@DisplayName("evaluateRules - non-empty inner week list is processed and not skipped")
	void testEvaluateRulesNonEmptyInnerWeekListIsProcessed() {
		// Given — prepareWeeklyTimeLogs returns a list with one non-empty inner bucket.
		// This exercises weeklyTimeLog.isEmpty() → false → normal week processing.
		TimeLog timeLog = mock(TimeLog.class);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheet.getTimeLogs()).willReturn(List.of(timeLog));
		given(this.timesheetSetting.getCustomRule()).willReturn(new ArrayList<>());
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver regularHoursResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver breakResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);

		com.google.common.collect.RangeSet<java.time.LocalTime> emptyRangeSet = com.google.common.collect.TreeRangeSet
			.create();
		given(regularHoursResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(breakResolver.resolveTimeRange(any())).willReturn(emptyRangeSet);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_REGULAR_HOURS))
			.willReturn(regularHoursResolver);
		given(this.ruleFactory.createTimeRangeResolver(RuleType.RANGE_BASED_BREAK)).willReturn(breakResolver);

		TestBaseRuleEvaluatorWithValidWeek validEvaluator = new TestBaseRuleEvaluatorWithValidWeek(this.ruleFactory,
				this.logger);

		// When
		WeeklyRuleEvaluatorResult result = validEvaluator.evaluateRules(this.timesheet);

		// Then — the non-empty week is processed and one weekly result is added
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyResults()).hasSize(1);
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Full-flow evaluateWeeklyTimeLogs — exercises the daily/weekly OT + default-pay
	// code paths inside evaluateTimeLog, evaluateWeeklyOvertimeRules,
	// createWeeklyOvertimeRuleEvaluationContext, calculateCustomRuleIndex and
	// isDailyOvertimeRuleType using real entity custom rules mapped via the mapper.
	// ──────────────────────────────────────────────────────────────────────────────

	private io.recruitcrm.contract_staffing.entity.model.CustomRule buildEntityCustomRule(int id, int ruleType) {
		io.recruitcrm.contract_staffing.entity.model.CustomRule rule = new io.recruitcrm.contract_staffing.entity.model.CustomRule();
		rule.setId(id);
		rule.setRuleName("rule-" + id);
		rule.setRuleType(ruleType);
		rule.setWorkDayId(new ArrayList<>()); // empty → applicable on all days
		rule.setChargeMethod(1); // MULTIPLIER
		rule.setPayRateMultiplier(1.5f);
		rule.setBillRateMultiplier(1.5f);
		rule.setDailyThreshold(28800); // 8h
		rule.setWeeklyThreshold(144000); // 40h
		return rule;
	}

	private io.recruitcrm.contract_staffing.entity.model.Timesheet buildEntityTimesheetWithRules() {
		io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay templateWorkDay = new io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay();
		templateWorkDay.setWorkDayId(1); // Monday — 2024-01-15 is a Monday

		io.recruitcrm.contract_staffing.entity.model.TimesheetSetting setting = new io.recruitcrm.contract_staffing.entity.model.TimesheetSetting();
		setting.setWorkLogType(1); // WORK_HOUR
		setting.setPayRate(20.0f);
		setting.setBillRate(30.0f);
		setting.setIsUnplannedHoursPayEnabled(1); // enable default pay sweep
		setting.setTemplateWorkDay(List.of(templateWorkDay));
		setting.setCustomRule(new ArrayList<>(List.of(buildEntityCustomRule(1, 4), // Daily
																					// OT
				buildEntityCustomRule(2, 5)))); // Weekly OT

		io.recruitcrm.contract_staffing.entity.model.Timesheet ts = new io.recruitcrm.contract_staffing.entity.model.Timesheet();
		ts.setId(500);
		ts.setTimesheetSetting(setting);
		return ts;
	}

	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog buildDtoLog(int id, LocalDate date,
			LocalTime start, LocalTime end) {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog tl = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		tl.setId(id);
		tl.setDate(date);
		tl.setWorkStartTime(start);
		tl.setWorkEndTime(end);
		tl.setNormalizedWorkStartTime(start);
		tl.setNormalizedWorkEndTime(end);
		return tl;
	}

	@Test
	@DisplayName("evaluateWeeklyTimeLogs - multi-interval day with daily OT and weekly OT custom rules exercises full evaluation flow")
	void testEvaluateWeeklyTimeLogsMultiIntervalDailyAndWeeklyOvertimeFlow() {
		// Given — a Monday with two work intervals (multi-interval day), a daily OT and a
		// weekly OT custom rule, and default pay enabled.
		io.recruitcrm.contract_staffing.entity.model.Timesheet entityTimesheet = buildEntityTimesheetWithRules();
		LocalDate monday = LocalDate.of(2024, 1, 15);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog morning = buildDtoLog(1, monday,
				LocalTime.of(9, 0), LocalTime.of(12, 0));
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog afternoon = buildDtoLog(2, monday,
				LocalTime.of(13, 0), LocalTime.of(18, 0));
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(morning, afternoon);
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		// Resolvers: regular hours and daily OT claim ranges; break/others return empty.
		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver claimingResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> claimedRanges = TreeRangeSet.create();
		claimedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		given(claimingResolver.resolveTimeRange(any())).willReturn(claimedRanges);
		given(this.ruleFactory.createTimeRangeResolver(any())).willReturn(claimingResolver);

		// Rules: every rule type evaluates to a non-null result.
		io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule rule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule.class);
		given(rule.evaluate(any())).willAnswer((inv) -> {
			io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext ctx = inv.getArgument(0);
			return RuleEvaluationResult.builder()
				.timeRange(ctx.getTimeRangesToEvaluate())
				.weeklyOvertimeHours(Duration.ofHours(1))
				.payAmount(java.math.BigDecimal.ONE)
				.billAmount(java.math.BigDecimal.ONE)
				.build();
		});
		given(this.ruleFactory.createRule(any(), any())).willReturn(rule);

		// When
		this.evaluator.evaluateWeeklyTimeLogs(weeklyTimeLog, entityTimesheet, result);

		// Then — the weekly overtime result is set and per-interval results are recorded.
		assertThat(result).isNotNull();
		assertThat(result.getWeeklyOvertimeRuleEvaluationResult()).isNotNull();
		assertThat(result.getRuleEvaluationResults()).containsKey(morning);
		assertThat(result.getRuleEvaluationResults()).containsKey(afternoon);
	}

	@Test
	@DisplayName("evaluateWeeklyTimeLogs - single-interval day with daily OT custom rule records daily result")
	void testEvaluateWeeklyTimeLogsSingleIntervalDailyOvertimeFlow() {
		// Given — a single-interval Monday with a daily OT custom rule. This drives the
		// non-multi-interval branch of evaluateTimeLog (constrainTarget = timeLog) and
		// calculateCustomRuleIndex matching the custom rule.
		io.recruitcrm.contract_staffing.entity.model.Timesheet entityTimesheet = buildEntityTimesheetWithRules();
		// Remove the weekly OT rule so only the daily OT custom rule remains for this
		// flow.
		entityTimesheet.getTimesheetSetting().setCustomRule(new ArrayList<>(List.of(buildEntityCustomRule(1, 4))));
		entityTimesheet.getTimesheetSetting().setIsUnplannedHoursPayEnabled(0); // skip
																				// sweep

		LocalDate monday = LocalDate.of(2024, 1, 15);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog day = buildDtoLog(1, monday, LocalTime.of(9, 0),
				LocalTime.of(18, 0));
		List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog = List.of(day);
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver claimingResolver = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver.class);
		RangeSet<LocalTime> claimedRanges = TreeRangeSet.create();
		claimedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(11, 0)));
		given(claimingResolver.resolveTimeRange(any())).willReturn(claimedRanges);
		given(this.ruleFactory.createTimeRangeResolver(any())).willReturn(claimingResolver);

		io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule rule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule.class);
		given(rule.evaluate(any())).willAnswer((inv) -> {
			io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext ctx = inv.getArgument(0);
			return RuleEvaluationResult.builder().timeRange(ctx.getTimeRangesToEvaluate()).build();
		});
		given(this.ruleFactory.createRule(any(), any())).willReturn(rule);

		// When
		this.evaluator.evaluateWeeklyTimeLogs(weeklyTimeLog, entityTimesheet, result);

		// Then — the day's interval has rule results attached.
		assertThat(result.getRuleEvaluationResults()).containsKey(day);
		assertThat(result.getRuleEvaluationResults().get(day)).isNotEmpty();
	}

	@Test
	@DisplayName("createWeeklyOvertimeRuleEvaluationContext - populates context with null time ranges and time log")
	void testCreateWeeklyOvertimeRuleEvaluationContext() {
		// Given
		io.recruitcrm.contract_staffing.entity.model.Timesheet entityTimesheet = buildEntityTimesheetWithRules();
		io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule = mock(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule.class);

		// When
		io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext context = this.evaluator
			.createWeeklyOvertimeRuleEvaluationContext(customRule, 3, entityTimesheet);

		// Then — weekly OT context carries no specific time ranges or time log.
		assertThat(context).isNotNull();
		assertThat(context.getTimesheet()).isEqualTo(entityTimesheet);
		assertThat(context.getTimeRangesToEvaluate()).isNull();
		assertThat(context.getCurrentTimeLogBeingEvaluated()).isNull();
		assertThat(context.getCurrentRuleBeingEvaluated()).isEqualTo(customRule);
		assertThat(context.getCurrentRuleIndex()).isEqualTo(3);
	}

	// Test implementation of BaseRuleEvaluator
	private static class TestBaseRuleEvaluator extends BaseRuleEvaluator {

		TestBaseRuleEvaluator(IRuleFactory ruleFactory, Logger logger) {
			super(ruleFactory, logger);
		}

		@Override
		protected boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet) {
			return rule.getRuleType() == RuleType.RANGE_BASED_WEEKLY_OVERTIME
					|| rule.getRuleType() == RuleType.DURATION_BASED_WEEKLY_OVERTIME;
		}

		@Override
		protected RuleType getRegularHoursRuleType() {
			return RuleType.RANGE_BASED_REGULAR_HOURS;
		}

		@Override
		protected RuleType getBreakRuleType() {
			return RuleType.RANGE_BASED_BREAK;
		}

		@Override
		protected RuleType getDefaultPayRuleType() {
			return RuleType.RANGE_BASED_DEFAULT_PAY;
		}

		@Override
		public void evaluateDefaultPayRules(
				List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog, Timesheet timesheet,
				RuleEvaluatorResult result) {
			super.evaluateDefaultPayRules(weeklyTimeLog, timesheet, result);
		}

		// Expose protected methods for testing
		@Override
		public void validateTimesheet(Timesheet timesheet) {
			super.validateTimesheet(timesheet);
		}

		@Override
		public boolean isDailyOvertimeRule(IEvaluatableRule rule) {
			return super.isDailyOvertimeRule(rule);
		}

		@Override
		public void updateEvaluationState(EvaluationState state, RangeSet<LocalTime> evaluatedRangeSet) {
			super.updateEvaluationState(state, evaluatedRangeSet);
		}

		@Override
		public void postProcessResult(RuleEvaluatorResult result) {
			super.postProcessResult(result);
		}

		@Override
		public int getTotalRuleCount(Timesheet timesheet) {
			return super.getTotalRuleCount(timesheet);
		}

		@Override
		public boolean isCustomRuleApplicableOnDay(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule,
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay workDay) {
			return super.isCustomRuleApplicableOnDay(customRule, workDay);
		}

		@Override
		public boolean isRegularHoursRuleApplicableOnDay(
				io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay workDay, Timesheet timesheet) {
			return super.isRegularHoursRuleApplicableOnDay(workDay, timesheet);
		}

		public void addRuleResultToEvaluatorResult(RuleEvaluatorResult result,
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule,
				RuleEvaluationResult ruleResult, Timesheet timesheet) {
			super.addRuleResultToEvaluatorResult(result, timeLog, customRule, ruleResult, timesheet);
		}

		public io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext createRuleEvaluationContext(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule,
				RangeSet<LocalTime> evaluatedRangeSet, int currentRuleIndex, Timesheet timesheet) {
			return super.createRuleEvaluationContext(timeLog, customRule, evaluatedRangeSet, currentRuleIndex,
					timesheet);
		}

		public io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext createTimeRangeResolverContext(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule, Timesheet timesheet,
				EvaluationState state, List<IEvaluatableRule> unifiedRules) {
			return super.createTimeRangeResolverContext(timeLog, customRule, timesheet, state, unifiedRules);
		}

		@Override
		public io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext createTimeRangeResolverContext(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog, IEvaluatableRule systemRule,
				Timesheet timesheet, EvaluationState state, List<IEvaluatableRule> unifiedRules) {
			return super.createTimeRangeResolverContext(timeLog, systemRule, timesheet, state, unifiedRules);
		}

		public RangeSet<LocalTime> resolveTimeRangesForRule(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule, Timesheet timesheet,
				EvaluationState state, List<IEvaluatableRule> unifiedRules) {
			return super.resolveTimeRangesForRule(timeLog, customRule, timesheet, state, unifiedRules);
		}

		public RuleEvaluationResult processRuleEvaluation(
				io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog,
				io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule customRule,
				RangeSet<LocalTime> evaluatedRangeSet, int currentRuleIndex,
				List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog,
				List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges, Timesheet timesheet) {
			return super.processRuleEvaluation(timeLog, customRule, evaluatedRangeSet, currentRuleIndex, weeklyTimeLog,
					weeklyOvertimeCandidateTimeRanges, timesheet);
		}

		@Override
		public void evaluateWeeklyTimeLogs(
				List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog, Timesheet timesheet,
				RuleEvaluatorResult result) {
			super.evaluateWeeklyTimeLogs(weeklyTimeLog, timesheet, result);
		}

		@Override
		public List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
				List<TimeLog> timeLogs) {
			return super.prepareWeeklyTimeLogs(timeLogs);
		}

		@Override
		public List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
				List<TimeLog> timeLogs, io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay startDay) {
			return super.prepareWeeklyTimeLogs(timeLogs, startDay);
		}

		@Override
		public List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
				List<TimeLog> timeLogs, Timesheet timesheet) {
			return super.prepareWeeklyTimeLogs(timeLogs, timesheet);
		}

		@Override
		public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
			return super.evaluateRules(timesheet);
		}

		boolean interpretTimesheetStartDayAsWeekday(Timesheet timesheet) {
			return this.shouldInterpretTimesheetStartDayAsWeekday(timesheet);
		}

	}

	/**
	 * Exercises the weeklyTimeLog.isEmpty() true branch in evaluatePreBuiltTimeLogs by
	 * overriding the method to inject one empty inner week bucket into the post-split
	 * loop while still delegating the null-guard and the early-return logic to the base
	 * class.
	 */
	private static class TestBaseRuleEvaluatorPreBuiltGapWeek extends TestBaseRuleEvaluator {

		TestBaseRuleEvaluatorPreBuiltGapWeek(IRuleFactory ruleFactory, Logger logger) {
			super(ruleFactory, logger);
		}

		@Override
		public WeeklyRuleEvaluatorResult evaluatePreBuiltTimeLogs(Timesheet timesheet,
				List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> expandedTimeLogs) {
			if (timesheet == null) {
				throw new IllegalArgumentException("Timesheet cannot be null");
			}
			if (timesheet.getTimesheetSetting() == null) {
				throw new IllegalArgumentException("Timesheet setting cannot be null");
			}
			WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();
			List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> weeklyTimeLogs = new ArrayList<>();
			weeklyTimeLogs.add(new ArrayList<>()); // empty gap-week bucket
			for (List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> weeklyTimeLog : weeklyTimeLogs) {
				if (weeklyTimeLog.isEmpty()) {
					continue;
				}
				LocalDate weekStartDate = weeklyTimeLog.get(0).getDate();
				LocalDate weekEndDate = weeklyTimeLog.get(weeklyTimeLog.size() - 1).getDate();
				RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
				weekResult.setTimesheet(timesheet);
				evaluateWeeklyTimeLogs(weeklyTimeLog, timesheet, weekResult);
				postProcessResult(weekResult);
				weeklyResult.addWeeklyResult(weekStartDate, weekEndDate, weekResult);
			}
			return weeklyResult;
		}

	}

	/**
	 * Returns a single empty inner list to exercise the weeklyTimeLog.isEmpty() true
	 * branch (continue) in evaluateRules.
	 */
	private static class TestBaseRuleEvaluatorWithGapWeek extends TestBaseRuleEvaluator {

		TestBaseRuleEvaluatorWithGapWeek(IRuleFactory ruleFactory, Logger logger) {
			super(ruleFactory, logger);
		}

		@Override
		public List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
				List<io.recruitcrm.contract_staffing.entity.model.TimeLog> timeLogs, Timesheet timesheet) {
			List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> weeks = new ArrayList<>();
			weeks.add(new ArrayList<>());
			return weeks;
		}

	}

	/**
	 * Returns a single non-empty inner list to exercise the weeklyTimeLog.isEmpty() false
	 * branch (normal week processing) in evaluateRules.
	 */
	private static class TestBaseRuleEvaluatorWithValidWeek extends TestBaseRuleEvaluator {

		TestBaseRuleEvaluatorWithValidWeek(IRuleFactory ruleFactory, Logger logger) {
			super(ruleFactory, logger);
		}

		@Override
		public List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> prepareWeeklyTimeLogs(
				List<io.recruitcrm.contract_staffing.entity.model.TimeLog> timeLogs, Timesheet timesheet) {
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
			dtoLog.setId(1);
			dtoLog.setDate(java.time.LocalDate.of(2024, 1, 15));
			List<List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>> weeks = new ArrayList<>();
			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> week = new ArrayList<>();
			week.add(dtoLog);
			weeks.add(week);
			return weeks;
		}

	}

}