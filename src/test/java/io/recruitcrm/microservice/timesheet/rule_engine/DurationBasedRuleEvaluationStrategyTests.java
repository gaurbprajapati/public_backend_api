/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.DurationBasedRuleEvaluator;
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
class DurationBasedRuleEvaluationStrategyTests {

	@Mock
	private DurationBasedRuleEvaluator durationBasedRuleEvaluator;

	@Mock
	private Timesheet timesheet;

	@Mock
	private WeeklyRuleEvaluatorResult expectedResult;

	@Mock
	private TimeLog timeLog;

	private DurationBasedRuleEvaluationStrategy strategy;

	@BeforeEach
	void setUp() {
		this.strategy = new DurationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator);
	}

	@Test
	@DisplayName("Evaluate rules - Success")
	void testEvaluateRulesSuccessReturnsResult() {
		// Given
		given(this.durationBasedRuleEvaluator.evaluateRules(this.timesheet)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.durationBasedRuleEvaluator).evaluateRules(this.timesheet);
	}

	@Test
	@DisplayName("Can handle - Work hour type returns true")
	void testCanHandleWorkHourTypeReturnsTrue() {
		// When
		boolean result = this.strategy.canHandle(WorkLogType.WORK_HOUR.getTypeId());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Can handle - Start and end time type returns false")
	void testCanHandleStartAndEndTimeTypeReturnsFalse() {
		// When
		boolean result = this.strategy.canHandle(WorkLogType.START_AND_END_TIME.getTypeId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can handle - Null work log type throws NullPointerException")
	void testCanHandleNullWorkLogTypeThrowsNullPointerException() {
		// When & Then
		assertThatThrownBy(() -> this.strategy.canHandle(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Can handle - Different work log type returns false")
	void testCanHandleDifferentWorkLogTypeReturnsFalse() {
		// When
		boolean result = this.strategy.canHandle(999);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Evaluate rules - Delegates to evaluator correctly")
	void testEvaluateRulesDelegatesToEvaluatorCorrectly() {
		// Given
		given(this.durationBasedRuleEvaluator.evaluateRules(this.timesheet)).willReturn(this.expectedResult);

		// When
		this.strategy.evaluateRules(this.timesheet);

		// Then
		verify(this.durationBasedRuleEvaluator).evaluateRules(this.timesheet);
	}

	@Test
	@DisplayName("Evaluate rules - Returns correct result type")
	void testEvaluateRulesReturnsCorrectResultType() {
		// Given
		given(this.durationBasedRuleEvaluator.evaluateRules(this.timesheet)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRules(this.timesheet);

		// Then
		assertThat(result).isInstanceOf(WeeklyRuleEvaluatorResult.class);
	}

	@Test
	@DisplayName("Constructor - Creates instance with evaluator")
	void testConstructorCreatesInstanceWithEvaluator() {
		// When
		DurationBasedRuleEvaluationStrategy newStrategy = new DurationBasedRuleEvaluationStrategy(
				this.durationBasedRuleEvaluator);

		// Then
		assertThat(newStrategy).isNotNull();
	}

	@Test
	@DisplayName("Can handle - Zero work log type returns false")
	void testCanHandleZeroWorkLogTypeReturnsFalse() {
		// When
		boolean result = this.strategy.canHandle(0);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can handle - Negative work log type returns false")
	void testCanHandleNegativeWorkLogTypeReturnsFalse() {
		// When
		boolean result = this.strategy.canHandle(-1);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Evaluate rules - Null timesheet handled gracefully")
	void testEvaluateRulesNullTimesheetHandledGracefully() {
		// Given
		given(this.durationBasedRuleEvaluator.evaluateRules(null)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRules(null);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.durationBasedRuleEvaluator).evaluateRules(null);
	}

	@Test
	@DisplayName("Evaluate rules - Exception from evaluator propagates")
	void testEvaluateRulesExceptionFromEvaluatorPropagates() {
		// Given
		given(this.durationBasedRuleEvaluator.evaluateRules(this.timesheet))
			.willThrow(new RuntimeException("Evaluator error"));

		// When & Then
		assertThatThrownBy(() -> this.strategy.evaluateRules(this.timesheet)).isInstanceOf(RuntimeException.class)
			.hasMessage("Evaluator error");
	}

	@Test
	@DisplayName("Constructor - Null evaluator handled")
	void testConstructorNullEvaluatorHandled() {
		// When
		DurationBasedRuleEvaluationStrategy newStrategy = new DurationBasedRuleEvaluationStrategy(null);

		// Then
		assertThat(newStrategy).isNotNull();
	}

	@Test
	@DisplayName("Can handle - Edge case work log types")
	void testCanHandleEdgeCaseWorkLogTypes() {
		// Test various edge cases
		assertThat(this.strategy.canHandle(Integer.MAX_VALUE)).isFalse();
		assertThat(this.strategy.canHandle(Integer.MIN_VALUE)).isFalse();
		assertThat(this.strategy.canHandle(0)).isFalse();
		assertThat(this.strategy.canHandle(1)).isTrue(); // WORK_HOUR = 1
		assertThat(this.strategy.canHandle(2)).isFalse(); // START_AND_END_TIME = 2
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Success returns result")
	void testEvaluateRulesWithTimeLogsSuccessReturnsResult() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, timeLogs))
			.willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.durationBasedRuleEvaluator).evaluatePreBuiltTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Delegates to evaluator correctly")
	void testEvaluateRulesWithTimeLogsDelegatesToEvaluatorCorrectly() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, timeLogs))
			.willReturn(this.expectedResult);

		// When
		this.strategy.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		verify(this.durationBasedRuleEvaluator).evaluatePreBuiltTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Returns correct result type")
	void testEvaluateRulesWithTimeLogsReturnsCorrectResultType() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, timeLogs))
			.willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		assertThat(result).isInstanceOf(WeeklyRuleEvaluatorResult.class);
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Empty time logs list returns result")
	void testEvaluateRulesWithTimeLogsEmptyTimeLogsListReturnsResult() {
		// Given
		List<TimeLog> timeLogs = Collections.emptyList();
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, timeLogs))
			.willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRulesWithTimeLogs(this.timesheet, timeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.durationBasedRuleEvaluator).evaluatePreBuiltTimeLogs(this.timesheet, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Null timesheet handled gracefully")
	void testEvaluateRulesWithTimeLogsNullTimesheetHandledGracefully() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(null, timeLogs)).willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRulesWithTimeLogs(null, timeLogs);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.durationBasedRuleEvaluator).evaluatePreBuiltTimeLogs(null, timeLogs);
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Null time logs handled gracefully")
	void testEvaluateRulesWithTimeLogsNullTimeLogsHandledGracefully() {
		// Given
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, null))
			.willReturn(this.expectedResult);

		// When
		WeeklyRuleEvaluatorResult result = this.strategy.evaluateRulesWithTimeLogs(this.timesheet, null);

		// Then
		assertThat(result).isEqualTo(this.expectedResult);
		verify(this.durationBasedRuleEvaluator).evaluatePreBuiltTimeLogs(this.timesheet, null);
	}

	@Test
	@DisplayName("Evaluate rules with time logs - Exception from evaluator propagates")
	void testEvaluateRulesWithTimeLogsExceptionFromEvaluatorPropagates() {
		// Given
		List<TimeLog> timeLogs = List.of(this.timeLog);
		given(this.durationBasedRuleEvaluator.evaluatePreBuiltTimeLogs(this.timesheet, timeLogs))
			.willThrow(new RuntimeException("Evaluator error"));

		// When & Then
		assertThatThrownBy(() -> this.strategy.evaluateRulesWithTimeLogs(this.timesheet, timeLogs))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Evaluator error");
	}

}