/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.microservice.timesheet.rule_engine.rules.DurationBasedRuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RangeBasedRuleEvaluator;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RuleEngineConfigurationTests {

	@Mock
	private DurationBasedRuleEvaluator durationBasedRuleEvaluator;

	@Mock
	private RangeBasedRuleEvaluator rangeBasedRuleEvaluator;

	private RuleEngineConfiguration configuration;

	@BeforeEach
	void setUp() {
		this.configuration = new RuleEngineConfiguration();
	}

	@Test
	@DisplayName("Rule evaluator bean - Creates instance with strategies")
	void testRuleEvaluatorBeanCreatesInstanceWithStrategies() {
		// Arrange
		List<RuleEvaluationStrategy> strategies = List.of(
				new DurationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator),
				new RangeBasedRuleEvaluationStrategy(this.rangeBasedRuleEvaluator));

		// Act
		RuleEvaluator result = this.configuration.ruleEvaluator(strategies);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RuleEvaluator.class);
	}

	@Test
	@DisplayName("Rule evaluator bean - Creates instance with empty strategies")
	void testRuleEvaluatorBeanCreatesInstanceWithEmptyStrategies() {
		// Arrange
		List<RuleEvaluationStrategy> strategies = List.of();

		// Act
		RuleEvaluator result = this.configuration.ruleEvaluator(strategies);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RuleEvaluator.class);
	}

	@Test
	@DisplayName("Rule evaluator bean - Creates instance with single strategy")
	void testRuleEvaluatorBeanCreatesInstanceWithSingleStrategy() {
		// Arrange
		List<RuleEvaluationStrategy> strategies = List
			.of(new DurationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator));

		// Act
		RuleEvaluator result = this.configuration.ruleEvaluator(strategies);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RuleEvaluator.class);
	}

	@Test
	@DisplayName("Duration based rule evaluation strategy bean - Creates instance with evaluator")
	void testDurationBasedRuleEvaluationStrategyBeanCreatesInstanceWithEvaluator() {
		// Act
		DurationBasedRuleEvaluationStrategy result = this.configuration
			.durationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(DurationBasedRuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Duration based rule evaluation strategy bean - Creates instance with null evaluator")
	void testDurationBasedRuleEvaluationStrategyBeanCreatesInstanceWithNullEvaluator() {
		// Act
		DurationBasedRuleEvaluationStrategy result = this.configuration.durationBasedRuleEvaluationStrategy(null);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(DurationBasedRuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Range based rule evaluation strategy bean - Creates instance with evaluator")
	void testRangeBasedRuleEvaluationStrategyBeanCreatesInstanceWithEvaluator() {
		// Act
		RangeBasedRuleEvaluationStrategy result = this.configuration
			.rangeBasedRuleEvaluationStrategy(this.rangeBasedRuleEvaluator);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RangeBasedRuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Range based rule evaluation strategy bean - Creates instance with null evaluator")
	void testRangeBasedRuleEvaluationStrategyBeanCreatesInstanceWithNullEvaluator() {
		// Act
		RangeBasedRuleEvaluationStrategy result = this.configuration.rangeBasedRuleEvaluationStrategy(null);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RangeBasedRuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Constructor - Creates instance")
	void testConstructorCreatesInstance() {
		// Act
		RuleEngineConfiguration newConfiguration = new RuleEngineConfiguration();

		// Assert
		assertThat(newConfiguration).isNotNull();
	}

	@Test
	@DisplayName("Rule evaluator bean - Creates instance with multiple strategies")
	void testRuleEvaluatorBeanCreatesInstanceWithMultipleStrategies() {
		// Arrange
		List<RuleEvaluationStrategy> strategies = List.of(
				new DurationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator),
				new RangeBasedRuleEvaluationStrategy(this.rangeBasedRuleEvaluator),
				new DurationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator));

		// Act
		RuleEvaluator result = this.configuration.ruleEvaluator(strategies);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RuleEvaluator.class);
	}

	@Test
	@DisplayName("Duration based rule evaluation strategy bean - Returns correct type")
	void testDurationBasedRuleEvaluationStrategyBeanReturnsCorrectType() {
		// Act
		DurationBasedRuleEvaluationStrategy result = this.configuration
			.durationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator);

		// Assert
		assertThat(result).isInstanceOf(RuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Range based rule evaluation strategy bean - Returns correct type")
	void testRangeBasedRuleEvaluationStrategyBeanReturnsCorrectType() {
		// Act
		RangeBasedRuleEvaluationStrategy result = this.configuration
			.rangeBasedRuleEvaluationStrategy(this.rangeBasedRuleEvaluator);

		// Assert
		assertThat(result).isInstanceOf(RuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Rule evaluator bean - Creates instance with null strategies")
	void testRuleEvaluatorBeanCreatesInstanceWithNullStrategies() {
		// Arrange
		List<RuleEvaluationStrategy> strategies = null;

		// Act
		RuleEvaluator result = this.configuration.ruleEvaluator(strategies);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RuleEvaluator.class);
	}

	@Test
	@DisplayName("Rule evaluator bean - Creates instance with mixed null strategies")
	void testRuleEvaluatorBeanCreatesInstanceWithMixedNullStrategies() {
		// Arrange
		List<RuleEvaluationStrategy> strategies = Arrays.asList(
				new DurationBasedRuleEvaluationStrategy(this.durationBasedRuleEvaluator), null,
				new RangeBasedRuleEvaluationStrategy(this.rangeBasedRuleEvaluator));

		// Act
		RuleEvaluator result = this.configuration.ruleEvaluator(strategies);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(RuleEvaluator.class);
	}

}