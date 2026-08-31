package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.RuleEvaluationStrategy;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleEvaluator Tests")
class RuleEvaluatorTests {

	@Mock
	private RuleEvaluationStrategy strategy1;

	@Mock
	private RuleEvaluationStrategy strategy2;

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private TimeLog timeLog;

	@Mock
	private WeeklyRuleEvaluatorResult expectedResult;

	private RuleEvaluator ruleEvaluator;

	@BeforeEach
	void setUp() {
		this.ruleEvaluator = new RuleEvaluator(List.of(this.strategy1, this.strategy2));
	}

	@Test
	@DisplayName("EvaluateRules with supported work log type - should use correct strategy")
	void testEvaluateRulesWithSupportedWorkLogType() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.strategy1.canHandle(1)).willReturn(false);
		given(this.strategy2.canHandle(1)).willReturn(true);
		given(this.strategy2.evaluateRules(this.timesheet))
			.willReturn(WeeklyRuleEvaluatorResult.builder().timesheet(this.timesheet).build());

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEvaluator.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	@DisplayName("EvaluateRules with unsupported work log type - should throw exception")
	void testEvaluateRulesWithUnsupportedWorkLogType() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(999);
		given(this.strategy1.canHandle(999)).willReturn(false);
		given(this.strategy2.canHandle(999)).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRules(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported WorkLogType: 999");
	}

	@Test
	@DisplayName("EvaluateRules with null timesheet - should throw exception")
	void testEvaluateRulesWithNullTimesheet() {
		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRules(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("EvaluateRules with null timesheet setting - should throw exception")
	void testEvaluateRulesWithNullTimesheetSetting() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRules(this.timesheet))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("EvaluateRules with null work log type - should throw exception")
	void testEvaluateRulesWithNullWorkLogType() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRules(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported WorkLogType: null");
	}

	@Test
	@DisplayName("EvaluateRules with first strategy matching - should use first strategy")
	void testEvaluateRulesWithFirstStrategyMatching() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.strategy1.canHandle(1)).willReturn(true);
		given(this.strategy1.evaluateRules(this.timesheet))
			.willReturn(WeeklyRuleEvaluatorResult.builder().timesheet(this.timesheet).build());

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEvaluator.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	@DisplayName("EvaluateRules with empty strategies list - should throw exception")
	void testEvaluateRulesWithEmptyStrategiesList() {
		// Given
		RuleEvaluator emptyEvaluator = new RuleEvaluator(List.of());
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		// When & Then
		assertThatThrownBy(() -> emptyEvaluator.evaluateRules(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported WorkLogType: 1");
	}

	@Test
	@DisplayName("Constructor with strategies - should initialize correctly")
	void testConstructorWithStrategies() {
		// When
		RuleEvaluator evaluator = new RuleEvaluator(List.of(this.strategy1, this.strategy2));

		// Then
		assertThat(evaluator).isNotNull();
	}

	@Test
	@DisplayName("Constructor with null strategies - should initialize correctly")
	void testConstructorWithNullStrategies() {
		// When
		RuleEvaluator evaluator = new RuleEvaluator(null);

		// Then
		assertThat(evaluator).isNotNull();
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with second strategy matching - should use correct strategy")
	void testEvaluateRulesWithTimeLogsSecondStrategyMatchingReturnsResult() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.strategy1.canHandle(2)).willReturn(false);
		given(this.strategy2.canHandle(2)).willReturn(true);
		given(this.strategy2.evaluateRulesWithTimeLogs(this.timesheet, timeLogs)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		then(this.strategy2).should().evaluateRulesWithTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with first strategy matching - should use first strategy")
	void testEvaluateRulesWithTimeLogsFirstStrategyMatchingReturnsResult() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.strategy1.canHandle(1)).willReturn(true);
		given(this.strategy1.evaluateRulesWithTimeLogs(this.timesheet, timeLogs)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		then(this.strategy1).should().evaluateRulesWithTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with unsupported work log type - should throw IllegalArgumentException")
	void testEvaluateRulesWithTimeLogsUnsupportedWorkLogTypeThrowsIllegalArgumentException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(999);
		given(this.strategy1.canHandle(999)).willReturn(false);
		given(this.strategy2.canHandle(999)).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported WorkLogType: 999");
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with null work log type - should throw IllegalArgumentException")
	void testEvaluateRulesWithTimeLogsNullWorkLogTypeThrowsIllegalArgumentException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported WorkLogType: null");
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with null timesheet - should throw NullPointerException")
	void testEvaluateRulesWithTimeLogsNullTimesheetThrowsNullPointerException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRulesWithTimeLogs(null, timeLogs))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with null timesheet setting - should throw NullPointerException")
	void testEvaluateRulesWithTimeLogsNullTimesheetSettingThrowsNullPointerException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with empty strategies list - should throw IllegalArgumentException")
	void testEvaluateRulesWithTimeLogsEmptyStrategiesListThrowsIllegalArgumentException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		RuleEvaluator emptyEvaluator = new RuleEvaluator(List.of());
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		// When & Then
		assertThatThrownBy(() -> emptyEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported WorkLogType: 1");
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs with empty time logs list - should delegate to strategy")
	void testEvaluateRulesWithTimeLogsEmptyTimeLogsListDelegatesToStrategy() {
		// Given
		List<TimeLog> emptyTimeLogs = Collections.emptyList();
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.strategy1.canHandle(1)).willReturn(true);
		given(this.strategy1.evaluateRulesWithTimeLogs(this.timesheet, emptyTimeLogs)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, emptyTimeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		then(this.strategy1).should().evaluateRulesWithTimeLogs(this.timesheet, emptyTimeLogs);
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs - exception from strategy propagates")
	void testEvaluateRulesWithTimeLogsExceptionFromStrategyPropagates() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.strategy1.canHandle(1)).willReturn(true);
		given(this.strategy1.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.willThrow(new RuntimeException("Strategy error"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Strategy error");
	}

	@Test
	@DisplayName("EvaluateRulesWithTimeLogs - non-matching strategy is not invoked")
	void testEvaluateRulesWithTimeLogsNonMatchingStrategyNotInvoked() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.strategy1.canHandle(1)).willReturn(true);
		given(this.strategy1.evaluateRulesWithTimeLogs(this.timesheet, timeLogs)).willReturn(this.expectedResult);

		// When
		this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		then(this.strategy2).should(never()).evaluateRulesWithTimeLogs(this.timesheet, timeLogs);
	}

}