package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("IRuleEvaluator Tests")
class IRuleEvaluatorTests {

	private TestRuleEvaluator ruleEvaluator;

	private Timesheet timesheet;

	private TimeLog timeLog1;

	private TimeLog timeLog2;

	@BeforeEach
	void setUp() {
		this.ruleEvaluator = new TestRuleEvaluator();
		this.timesheet = new Timesheet();
		this.timesheet.setId(1);

		this.timeLog1 = new TimeLog();
		this.timeLog1.setId(1);
		this.timeLog1.setDate(LocalDate.of(2024, 1, 15));

		this.timeLog2 = new TimeLog();
		this.timeLog2.setId(2);
		this.timeLog2.setDate(LocalDate.of(2024, 1, 16));
	}

	@Test
	@DisplayName("EvaluateRules - should return WeeklyRuleEvaluatorResult")
	void testEvaluateRules() {
		// Act
		WeeklyRuleEvaluatorResult result = this.ruleEvaluator.evaluateRules(this.timesheet);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
	}

	@Test
	@DisplayName("PopulateMoneyData with null result - should not throw exception")
	void testPopulateMoneyDataWithNullResult() {
		// Act & Assert
		assertThatCode(() -> this.ruleEvaluator.populateMoneyData(null)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("populateMoneyData with empty result - should return empty money data map and null for missing key")
	void testPopulateMoneyDataWithEmptyResult() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		assertThat(result.getMoneyData()).isEqualTo(MoneyData.zero());
		assertThat(result.getMoneyDataForTimeLog(this.timeLog1)).isNull();
	}

	@Test
	@DisplayName("PopulateMoneyData with rule evaluation results - should populate money data")
	void testPopulateMoneyDataWithRuleEvaluationResults() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult ruleResult1 = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("50.00"))
			.build();

		RuleEvaluationResult ruleResult2 = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("30.00"))
			.billAmount(new BigDecimal("60.00"))
			.build();

		result.addRuleEvaluationResult(this.timeLog1, ruleResult1);
		result.addRuleEvaluationResult(this.timeLog1, ruleResult2);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData timeLogMoneyData = result.getMoneyDataForTimeLog(this.timeLog1);
		assertThat(timeLogMoneyData).isNotNull();
		assertThat(timeLogMoneyData.getPayAmount()).isEqualTo(new BigDecimal("55.00"));
		assertThat(timeLogMoneyData.getBillAmount()).isEqualTo(new BigDecimal("110.00"));
	}

	@Test
	@DisplayName("PopulateMoneyData with weekly overtime result - should populate weekly overtime money data")
	void testPopulateMoneyDataWithWeeklyOvertimeResult() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult weeklyOvertimeResult = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("100.00"))
			.billAmount(new BigDecimal("200.00"))
			.build();

		result.setWeeklyOvertimeRuleEvaluationResult(weeklyOvertimeResult);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData weeklyOvertimeMoneyData = result.getWeeklyOvertimeMoneyData();
		assertThat(weeklyOvertimeMoneyData).isNotNull();
		assertThat(weeklyOvertimeMoneyData.getPayAmount()).isEqualTo(new BigDecimal("100.00"));
		assertThat(weeklyOvertimeMoneyData.getBillAmount()).isEqualTo(new BigDecimal("200.00"));
	}

	@Test
	@DisplayName("PopulateMoneyData with rule results without monetary values - should handle gracefully")
	void testPopulateMoneyDataWithRuleResultsWithoutMonetaryValues() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult ruleResultWithoutMoney = RuleEvaluationResult.builder().timeRange(null).build();

		result.addRuleEvaluationResult(this.timeLog1, ruleResultWithoutMoney);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData timeLogMoneyData = result.getMoneyDataForTimeLog(this.timeLog1);
		assertThat(timeLogMoneyData).isNotNull();
		assertThat(timeLogMoneyData.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(timeLogMoneyData.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("PopulateMoneyData with weekly overtime result without monetary values - should handle gracefully")
	void testPopulateMoneyDataWithWeeklyOvertimeResultWithoutMonetaryValues() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult weeklyOvertimeResultWithoutMoney = RuleEvaluationResult.builder().timeRange(null).build();

		result.setWeeklyOvertimeRuleEvaluationResult(weeklyOvertimeResultWithoutMoney);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData weeklyOvertimeMoneyData = result.getWeeklyOvertimeMoneyData();
		assertThat(weeklyOvertimeMoneyData).isNotNull();
		assertThat(weeklyOvertimeMoneyData.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(weeklyOvertimeMoneyData.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("PopulateMoneyData with mixed monetary and non-monetary results - should handle correctly")
	void testPopulateMoneyDataWithMixedResults() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult ruleResultWithMoney = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("50.00"))
			.build();

		RuleEvaluationResult ruleResultWithoutMoney = RuleEvaluationResult.builder().timeRange(null).build();

		result.addRuleEvaluationResult(this.timeLog1, ruleResultWithMoney);
		result.addRuleEvaluationResult(this.timeLog1, ruleResultWithoutMoney);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData timeLogMoneyData = result.getMoneyDataForTimeLog(this.timeLog1);
		assertThat(timeLogMoneyData).isNotNull();
		assertThat(timeLogMoneyData.getPayAmount()).isEqualTo(new BigDecimal("25.00"));
		assertThat(timeLogMoneyData.getBillAmount()).isEqualTo(new BigDecimal("50.00"));
	}

	@Test
	@DisplayName("PopulateMoneyData with multiple time logs - should populate money data for each")
	void testPopulateMoneyDataWithMultipleTimeLogs() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult ruleResult1 = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("50.00"))
			.build();

		RuleEvaluationResult ruleResult2 = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("30.00"))
			.billAmount(new BigDecimal("60.00"))
			.build();

		result.addRuleEvaluationResult(this.timeLog1, ruleResult1);
		result.addRuleEvaluationResult(this.timeLog2, ruleResult2);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData timeLog1MoneyData = result.getMoneyDataForTimeLog(this.timeLog1);
		assertThat(timeLog1MoneyData).isNotNull();
		assertThat(timeLog1MoneyData.getPayAmount()).isEqualTo(new BigDecimal("25.00"));
		assertThat(timeLog1MoneyData.getBillAmount()).isEqualTo(new BigDecimal("50.00"));

		MoneyData timeLog2MoneyData = result.getMoneyDataForTimeLog(this.timeLog2);
		assertThat(timeLog2MoneyData).isNotNull();
		assertThat(timeLog2MoneyData.getPayAmount()).isEqualTo(new BigDecimal("30.00"));
		assertThat(timeLog2MoneyData.getBillAmount()).isEqualTo(new BigDecimal("60.00"));
	}

	@Test
	@DisplayName("PopulateMoneyData with null amounts in rule results - should handle gracefully")
	void testPopulateMoneyDataWithNullAmounts() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult ruleResultWithNullAmounts = RuleEvaluationResult.builder()
			.payAmount(null)
			.billAmount(null)
			.build();

		result.addRuleEvaluationResult(this.timeLog1, ruleResultWithNullAmounts);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData timeLogMoneyData = result.getMoneyDataForTimeLog(this.timeLog1);
		assertThat(timeLogMoneyData).isNotNull();
		assertThat(timeLogMoneyData.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(timeLogMoneyData.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("PopulateMoneyData with zero amounts in rule results - should handle correctly")
	void testPopulateMoneyDataWithZeroAmounts() {
		// Arrange
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setTimesheet(this.timesheet);

		RuleEvaluationResult ruleResultWithZeroAmounts = RuleEvaluationResult.builder()
			.payAmount(BigDecimal.ZERO)
			.billAmount(BigDecimal.ZERO)
			.build();

		result.addRuleEvaluationResult(this.timeLog1, ruleResultWithZeroAmounts);

		// Act
		this.ruleEvaluator.populateMoneyData(result);

		// Assert
		MoneyData timeLogMoneyData = result.getMoneyDataForTimeLog(this.timeLog1);
		assertThat(timeLogMoneyData).isNotNull();
		assertThat(timeLogMoneyData.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(timeLogMoneyData.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	// Test implementation of IRuleEvaluator
	private static class TestRuleEvaluator implements IRuleEvaluator {

		@Override
		public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
			return WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();
		}

	}

}