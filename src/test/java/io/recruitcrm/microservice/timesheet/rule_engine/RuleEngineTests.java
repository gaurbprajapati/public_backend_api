/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
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
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class RuleEngineTests {

	@Mock
	private RuleEvaluator ruleEvaluator;

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private WeeklyRuleEvaluatorResult expectedResult;

	@Mock
	private TimeLog timeLog;

	private RuleEngine ruleEngine;

	@BeforeEach
	void setUp() {
		this.ruleEngine = new RuleEngine(this.ruleEvaluator);
	}

	@Test
	@DisplayName("Evaluate rules - Success with valid timesheet")
	void testEvaluateRulesValidTimesheetReturnsResult() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRules(this.timesheet)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.ruleEvaluator).evaluateRules(this.timesheet);
	}

	@Test
	@DisplayName("Evaluate rules - Null timesheet throws IllegalArgumentException")
	void testEvaluateRulesNullTimesheetThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRules(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet cannot be null");
	}

	@Test
	@DisplayName("Evaluate rules - Null timesheet setting throws IllegalArgumentException")
	void testEvaluateRulesNullTimesheetSettingThrowsIllegalArgumentException() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRules(this.timesheet))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet setting cannot be null");
	}

	@Test
	@DisplayName("Evaluate rules - Empty timesheet setting is allowed")
	void testEvaluateRulesEmptyTimesheetSettingIsAllowed() {
		// Given
		TimesheetSetting emptyTimesheetSetting = new TimesheetSetting();
		given(this.timesheet.getTimesheetSetting()).willReturn(emptyTimesheetSetting);
		given(this.ruleEvaluator.evaluateRules(this.timesheet)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.ruleEvaluator).evaluateRules(this.timesheet);
	}

	@Test
	@DisplayName("Evaluate rules - Delegates to rule evaluator correctly")
	void testEvaluateRulesDelegatesToRuleEvaluator() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRules(this.timesheet)).willReturn(this.expectedResult);

		// When
		this.ruleEngine.evaluateRules(this.timesheet);

		// Then
		verify(this.ruleEvaluator).evaluateRules(this.timesheet);
	}

	@Test
	@DisplayName("Validate rules - Throws UnsupportedOperationException")
	void testValidateRulesThrowsUnsupportedOperationException() {
		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.validateRules()).isInstanceOf(UnsupportedOperationException.class)
			.hasMessage("Rule validation not implemented yet");
	}

	@Test
	@DisplayName("Constructor - Creates instance with rule evaluator")
	void testConstructorCreatesInstanceWithRuleEvaluator() {
		// When
		RuleEngine newRuleEngine = new RuleEngine(this.ruleEvaluator);

		// Then
		assertThat(newRuleEngine).isNotNull();
	}

	@Test
	@DisplayName("Evaluate rules - Null result from evaluator")
	void testEvaluateRulesNullResultFromEvaluator() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRules(this.timesheet)).willReturn(null);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isNull();
		verify(this.ruleEvaluator).evaluateRules(this.timesheet);
	}

	@Test
	@DisplayName("Evaluate rules - Exception from evaluator propagates")
	void testEvaluateRulesExceptionFromEvaluatorPropagates() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRules(this.timesheet)).willThrow(new RuntimeException("Evaluator error"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRules(this.timesheet)).isInstanceOf(RuntimeException.class)
			.hasMessage("Evaluator error");
	}

	@Test
	@DisplayName("Constructor - Null rule evaluator handled")
	void testConstructorNullRuleEvaluatorHandled() {
		// When
		RuleEngine newRuleEngine = new RuleEngine(null);

		// Then
		assertThat(newRuleEngine).isNotNull();
	}

	@Test
	@DisplayName("Evaluate rules on demand - Success returns result")
	void testEvaluateRulesOnDemandValidInputReturnsResult() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRulesOnDemand(this.timesheet, timeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.ruleEvaluator).evaluateRulesWithTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules on demand - Null timesheet throws IllegalArgumentException")
	void testEvaluateRulesOnDemandNullTimesheetThrowsIllegalArgumentException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);

		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRulesOnDemand(null, timeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet cannot be null");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Null timesheet setting throws IllegalArgumentException")
	void testEvaluateRulesOnDemandNullTimesheetSettingThrowsIllegalArgumentException() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRulesOnDemand(this.timesheet, timeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet setting cannot be null");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Null time logs throws IllegalArgumentException")
	void testEvaluateRulesOnDemandNullTimeLogsThrowsIllegalArgumentException() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);

		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRulesOnDemand(this.timesheet, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Time logs cannot be null or empty");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Empty time logs throws IllegalArgumentException")
	void testEvaluateRulesOnDemandEmptyTimeLogsThrowsIllegalArgumentException() {
		// Given
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);

		// When & Then
		List<TimeLog> emptyTimeLogs = Collections.emptyList();
		assertThatThrownBy(() -> this.ruleEngine.evaluateRulesOnDemand(this.timesheet, emptyTimeLogs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Time logs cannot be null or empty");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Delegates to evaluator correctly")
	void testEvaluateRulesOnDemandDelegatesToEvaluatorCorrectly() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs)).willReturn(this.expectedResult);

		// When
		this.ruleEngine.evaluateRulesOnDemand(this.timesheet, timeLogs);

		// Then
		verify(this.ruleEvaluator).evaluateRulesWithTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules on demand - Null result from evaluator")
	void testEvaluateRulesOnDemandNullResultFromEvaluator() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs)).willReturn(null);

		// When
		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRulesOnDemand(this.timesheet, timeLogs);

		// Then
		assertThat(result).isNull();
		verify(this.ruleEvaluator).evaluateRulesWithTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules on demand - Exception from evaluator propagates")
	void testEvaluateRulesOnDemandExceptionFromEvaluatorPropagates() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.ruleEvaluator.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.willThrow(new RuntimeException("Evaluator error"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngine.evaluateRulesOnDemand(this.timesheet, timeLogs))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Evaluator error");
	}

}