package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseRule Tests")
class BaseRuleTests {

	@Mock
	private Logger logger;

	private TestBaseRule testRule;

	@BeforeEach
	void setUp() {
		this.testRule = new TestBaseRule(this.logger);
	}

	@Test
	@DisplayName("Constructor - should initialize logger")
	void testConstructor() {
		// Act & Assert
		assertThat(this.testRule.logger).isEqualTo(this.logger);
	}

	@Test
	@DisplayName("Validate - should log debug message")
	void testValidate() {
		// Act
		this.testRule.validate();

		// Assert
		verify(this.logger).logDebug("Validating rule: Test Rule");
	}

	@Test
	@DisplayName("CreateResult with timeRange - should create basic result")
	void testCreateResultWithTimeRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		RuleEvaluationResult result = this.testRule.createResult(timeRange);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getPayAmount()).isNull();
		assertThat(result.getBillAmount()).isNull();
	}

	@Test
	@DisplayName("CreateResult with monetary amounts - should create result with amounts")
	void testCreateResultWithMonetaryAmounts() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		BigDecimal payAmount = new BigDecimal("25.00");
		BigDecimal billAmount = new BigDecimal("50.00");

		// Act
		RuleEvaluationResult result = this.testRule.createResult(timeRange, payAmount, billAmount);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getPayAmount()).isEqualTo(payAmount);
		assertThat(result.getBillAmount()).isEqualTo(billAmount);
	}

	@Test
	@DisplayName("CreateResult with weekly overtime hours - should create result with overtime")
	void testCreateResultWithWeeklyOvertimeHours() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		Duration weeklyOvertimeHours = Duration.ofHours(5);

		// Act
		RuleEvaluationResult result = this.testRule.createResult(timeRange, weeklyOvertimeHours);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(weeklyOvertimeHours);
		assertThat(result.getPayAmount()).isNull();
		assertThat(result.getBillAmount()).isNull();
	}

	@Test
	@DisplayName("CreateCompleteResult - should create complete result with all fields")
	void testCreateCompleteResult() {
		// Arrange
		RuleEvaluationContext context = createTestContext();
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		BigDecimal payAmount = new BigDecimal("25.00");
		BigDecimal billAmount = new BigDecimal("50.00");

		// Act
		RuleEvaluationResult result = this.testRule.createCompleteResult(context, timeRange, payAmount, billAmount);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getRuleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
		assertThat(result.getRuleName()).isEqualTo("Test Custom Rule");
		assertThat(result.getPayAmount()).isEqualTo(payAmount);
		assertThat(result.getBillAmount()).isEqualTo(billAmount);
		assertThat(result.getEvaluatedDuration()).isEqualTo(Duration.ofHours(8));
		assertThat(result.getEvaluationDate()).isEqualTo(LocalDate.of(2024, 1, 15));
		assertThat(result.getRuleIndex()).isEqualTo(1);
		assertThat(result.isVirtualRule()).isTrue();
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.getMetadata()).contains("Rule: Test Custom Rule");
		assertThat(result.getMetadata()).contains("Date: 2024-01-15");
		assertThat(result.getMetadata()).contains("Duration: 8h 0m");
	}

	@Test
	@DisplayName("CreateCompleteResult with weekly overtime - should create complete result with overtime")
	void testCreateCompleteResultWithWeeklyOvertime() {
		// Arrange
		RuleEvaluationContext context = createTestContext();
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		Duration weeklyOvertimeHours = Duration.ofHours(5);
		BigDecimal payAmount = new BigDecimal("25.00");
		BigDecimal billAmount = new BigDecimal("50.00");

		// Act
		RuleEvaluationResult result = this.testRule.createCompleteResult(context, timeRange, weeklyOvertimeHours,
				payAmount, billAmount);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimeRange()).isEqualTo(timeRange);
		assertThat(result.getWeeklyOvertimeHours()).isEqualTo(weeklyOvertimeHours);
		assertThat(result.getPayAmount()).isEqualTo(payAmount);
		assertThat(result.getBillAmount()).isEqualTo(billAmount);
		assertThat(result.getEvaluatedDuration()).isEqualTo(Duration.ofHours(8));
		assertThat(result.isVirtualRule()).isTrue();
		assertThat(result.isSuccessful()).isTrue();
	}

	@Test
	@DisplayName("GetRuleType with custom rule - should return custom rule type")
	void testGetRuleTypeWithCustomRule() {
		// Arrange
		RuleEvaluationContext context = createTestContext();

		// Act
		RuleType ruleType = this.testRule.getRuleType(context);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("GetRuleType without custom rule - should return default rule type")
	void testGetRuleTypeWithoutCustomRule() {
		// Arrange
		RuleEvaluationContext context = createTestContext();
		context.setCurrentRuleBeingEvaluated(null);

		// Act
		RuleType ruleType = this.testRule.getRuleType(context);

		// Assert
		assertThat(ruleType).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS);
	}

	@Test
	@DisplayName("GetRuleName with custom rule - should return custom rule name")
	void testGetRuleNameWithCustomRule() {
		// Arrange
		RuleEvaluationContext context = createTestContext();

		// Act
		String ruleName = this.testRule.getRuleName(context);

		// Assert
		assertThat(ruleName).isEqualTo("Test Custom Rule");
	}

	@Test
	@DisplayName("GetRuleName without custom rule - should return rule name")
	void testGetRuleNameWithoutCustomRule() {
		// Arrange
		RuleEvaluationContext context = createTestContext();
		context.setCurrentRuleBeingEvaluated(null);

		// Act
		String ruleName = this.testRule.getRuleName(context);

		// Assert
		assertThat(ruleName).isEqualTo("Test Rule");
	}

	@Test
	@DisplayName("CreateMetadata - should create metadata string")
	void testCreateMetadata() {
		// Arrange
		RuleEvaluationContext context = createTestContext();
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		Duration evaluatedDuration = Duration.ofHours(8);

		// Act
		String metadata = this.testRule.createMetadata(context, timeRange, evaluatedDuration);

		// Assert
		assertThat(metadata).contains("Rule: Test Custom Rule")
			.contains("Date: 2024-01-15")
			.contains("Duration: 8h 0m");
	}

	@Test
	@DisplayName("FormatDuration - should format duration correctly")
	void testFormatDuration() {
		// Arrange
		Duration duration = Duration.ofHours(8).plusMinutes(30);

		// Act
		String formatted = this.testRule.formatDuration(duration);

		// Assert
		assertThat(formatted).isEqualTo("8h 30m");
	}

	@Test
	@DisplayName("FormatDuration with zero duration - should format as zero")
	void testFormatDurationZero() {
		// Arrange
		Duration duration = Duration.ZERO;

		// Act
		String formatted = this.testRule.formatDuration(duration);

		// Assert
		assertThat(formatted).isEqualTo("0h 0m");
	}

	@Test
	@DisplayName("IsVirtualRule with regular hours - should return true")
	void testIsVirtualRuleWithRegularHours() {
		// Act & Assert
		assertThat(this.testRule.isVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS)).isTrue();
		assertThat(this.testRule.isVirtualRule(RuleType.DURATION_BASED_REGULAR_HOURS)).isTrue();
	}

	@Test
	@DisplayName("IsVirtualRule with break - should return true")
	void testIsVirtualRuleWithBreak() {
		// Act & Assert
		assertThat(this.testRule.isVirtualRule(RuleType.RANGE_BASED_BREAK)).isTrue();
		assertThat(this.testRule.isVirtualRule(RuleType.DURATION_BASED_BREAK)).isTrue();
	}

	@Test
	@DisplayName("IsVirtualRule with default pay - should return true")
	void testIsVirtualRuleWithDefaultPay() {
		assertThat(this.testRule.isVirtualRule(RuleType.RANGE_BASED_DEFAULT_PAY)).isTrue();
		assertThat(this.testRule.isVirtualRule(RuleType.DURATION_BASED_DEFAULT_PAY)).isTrue();
	}

	@Test
	@DisplayName("IsVirtualRule with other rule types - should return false")
	void testIsVirtualRuleWithOtherRuleTypes() {
		// Act & Assert
		assertThat(this.testRule.isVirtualRule(RuleType.RANGE_BASED_DAILY_OVERTIME)).isFalse();
		assertThat(this.testRule.isVirtualRule(RuleType.RANGE_BASED_WEEKLY_OVERTIME)).isFalse();
		assertThat(this.testRule.isVirtualRule(null)).isFalse();
	}

	@Test
	@DisplayName("ValidateContext with null context - should throw exception")
	void testValidateContextWithNullContext() {
		// Act & Assert
		assertThatThrownBy(() -> this.testRule.validateContext(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Rule evaluation context cannot be null");
	}

	@Test
	@DisplayName("ValidateContext with valid context - should not throw exception")
	void testValidateContextWithValidContext() {
		// Arrange
		RuleEvaluationContext context = createTestContext();

		// Act & Assert
		assertThatCode(() -> this.testRule.validateContext(context)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("LogEvaluationStart - should log debug message")
	void testLogEvaluationStart() {
		// Arrange
		RuleEvaluationContext context = createTestContext();

		// Act
		this.testRule.logEvaluationStart(context);

		// Assert
		verify(this.logger).logDebug("Starting evaluation of rule: Test Rule for timesheet: 1");
	}

	@Test
	@DisplayName("LogEvaluationComplete - should log debug message")
	void testLogEvaluationComplete() {
		// Arrange
		RuleEvaluationResult result = RuleEvaluationResult.builder().ruleName("Test Rule").successful(true).build();

		// Act
		this.testRule.logEvaluationComplete(result);

		// Assert
		verify(this.logger).logDebug("Completed evaluation of rule: Test Rule with result: " + result);
	}

	@Test
	@DisplayName("InitRatesAndMethods - should not throw exception")
	void testInitRatesAndMethods() {
		// Arrange
		RuleEvaluationContext context = createTestContext();

		// Act & Assert
		assertThatCode(() -> this.testRule.initRatesAndMethods(context)).doesNotThrowAnyException();
	}

	private RuleEvaluationContext createTestContext() {
		CustomRule customRule = new CustomRule();
		customRule.setRuleName("Test Custom Rule");
		customRule.setRuleType(RuleType.RANGE_BASED_REGULAR_HOURS);

		TimeLog timeLog = new TimeLog();
		timeLog.setDate(LocalDate.of(2024, 1, 15));

		Timesheet timesheet = new Timesheet();
		timesheet.setId(1);

		RuleEvaluationContext context = new RuleEvaluationContext();
		context.setCurrentRuleBeingEvaluated(customRule);
		context.setCurrentTimeLogBeingEvaluated(timeLog);
		context.setCurrentRuleIndex(1);
		context.setTimesheet(timesheet);

		return context;
	}

	// Test implementation of BaseRule
	private static class TestBaseRule extends BaseRule {

		TestBaseRule(Logger logger) {
			super(logger);
		}

		@Override
		public RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext) {
			return new RuleEvaluationResult();
		}

		@Override
		public String getName() {
			return "Test Rule";
		}

		@Override
		protected RuleType getDefaultRuleType() {
			return RuleType.RANGE_BASED_REGULAR_HOURS;
		}

		// Expose protected methods for testing
		@Override
		public RuleType getRuleType(RuleEvaluationContext context) {
			return super.getRuleType(context);
		}

		@Override
		public String getRuleName(RuleEvaluationContext context) {
			return super.getRuleName(context);
		}

		@Override
		public String createMetadata(RuleEvaluationContext context, RangeSet<LocalTime> timeRange,
				Duration evaluatedDuration) {
			return super.createMetadata(context, timeRange, evaluatedDuration);
		}

		@Override
		public String formatDuration(Duration duration) {
			return super.formatDuration(duration);
		}

		@Override
		public boolean isVirtualRule(RuleType ruleType) {
			return super.isVirtualRule(ruleType);
		}

		@Override
		public void validateContext(RuleEvaluationContext context) {
			super.validateContext(context);
		}

		@Override
		public void logEvaluationStart(RuleEvaluationContext context) {
			super.logEvaluationStart(context);
		}

		@Override
		public void logEvaluationComplete(RuleEvaluationResult result) {
			super.logEvaluationComplete(result);
		}

		@Override
		public void initRatesAndMethods(RuleEvaluationContext ctx) {
			super.initRatesAndMethods(ctx);
		}

	}

}