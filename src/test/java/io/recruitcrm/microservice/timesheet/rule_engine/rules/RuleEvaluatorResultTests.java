package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

@DisplayName("RuleEvaluatorResult Tests")
class RuleEvaluatorResultTests {

	private RuleEvaluatorResult ruleEvaluatorResult;

	private Timesheet timesheet;

	private TimeLog timeLog1;

	private TimeLog timeLog2;

	private RuleEvaluationResult ruleResult1;

	private RuleEvaluationResult ruleResult2;

	@BeforeEach
	void setUp() {
		this.timesheet = new Timesheet();
		this.timesheet.setId(1);

		this.timeLog1 = new TimeLog();
		this.timeLog1.setId(1);
		this.timeLog1.setDate(LocalDate.of(2024, 1, 15));

		this.timeLog2 = new TimeLog();
		this.timeLog2.setId(2);
		this.timeLog2.setDate(LocalDate.of(2024, 1, 16));

		this.ruleResult1 = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("50.00"))
			.build();

		this.ruleResult2 = RuleEvaluationResult.builder()
			.payAmount(new BigDecimal("30.00"))
			.billAmount(new BigDecimal("60.00"))
			.build();

		this.ruleEvaluatorResult = RuleEvaluatorResult.builder().timesheet(this.timesheet).build();
	}

	@Test
	@DisplayName("AddRuleEvaluationResult with valid parameters - should add result")
	void testAddRuleEvaluationResultWithValidParameters() {
		// Act
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);

		// Assert
		List<RuleEvaluationResult> results = this.ruleEvaluatorResult.getRuleEvaluationResultsForTimeLog(this.timeLog1);
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).isEqualTo(this.ruleResult1);
	}

	@Test
	@DisplayName("AddRuleEvaluationResult with null timeLog - should throw exception")
	void testAddRuleEvaluationResultWithNullTimeLog() {
		// Act & Assert
		assertThatThrownBy(() -> this.ruleEvaluatorResult.addRuleEvaluationResult(null, this.ruleResult1))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("TimeLog cannot be null");
	}

	@Test
	@DisplayName("AddRuleEvaluationResult with null ruleResult - should throw exception")
	void testAddRuleEvaluationResultWithNullRuleResult() {
		// Act & Assert
		assertThatThrownBy(() -> this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("RuleEvaluationResult cannot be null");
	}

	@Test
	@DisplayName("AddRuleEvaluationResult multiple results for same timeLog - should add all results")
	void testAddRuleEvaluationResultMultipleResultsForSameTimeLog() {
		// Act
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult2);

		// Assert
		List<RuleEvaluationResult> results = this.ruleEvaluatorResult.getRuleEvaluationResultsForTimeLog(this.timeLog1);
		assertThat(results).hasSize(2).contains(this.ruleResult1, this.ruleResult2);
	}

	@Test
	@DisplayName("GetRuleEvaluationResultsForTimeLog with existing results - should return results")
	void testGetRuleEvaluationResultsForTimeLogWithExistingResults() {
		// Arrange - Test with multiple time logs and different result combinations
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog2, this.ruleResult2);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult2);

		// Act
		List<RuleEvaluationResult> resultsForTimeLog1 = this.ruleEvaluatorResult
			.getRuleEvaluationResultsForTimeLog(this.timeLog1);
		List<RuleEvaluationResult> resultsForTimeLog2 = this.ruleEvaluatorResult
			.getRuleEvaluationResultsForTimeLog(this.timeLog2);

		// Assert
		assertThat(resultsForTimeLog1).hasSize(2).contains(this.ruleResult1, this.ruleResult2);
		assertThat(resultsForTimeLog2).hasSize(1).contains(this.ruleResult2);
		assertThat(this.ruleEvaluatorResult.getTotalRuleEvaluations()).isEqualTo(3);
	}

	@Test
	@DisplayName("GetRuleEvaluationResultsForTimeLog with no results - should return empty list")
	void testGetRuleEvaluationResultsForTimeLogWithNoResults() {
		// Act
		List<RuleEvaluationResult> results = this.ruleEvaluatorResult.getRuleEvaluationResultsForTimeLog(this.timeLog1);

		// Assert
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("GetMoneyDataForTimeLog with no money data - should return null")
	void testGetMoneyDataForTimeLogWithNoMoneyData() {
		// Act
		MoneyData result = this.ruleEvaluatorResult.getMoneyDataForTimeLog(this.timeLog1);

		// Assert
		assertThat(result).isNull(); // Initially null since we haven't set it
	}

	@Test
	@DisplayName("HasResults with rule evaluation results - should return true")
	void testHasResultsWithRuleEvaluationResults() {
		// Arrange
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);

		// Act
		boolean hasResults = this.ruleEvaluatorResult.hasResults();

		// Assert
		assertThat(hasResults).isTrue();
	}

	@Test
	@DisplayName("HasResults with weekly overtime result - should return true")
	void testHasResultsWithWeeklyOvertimeResult() {
		// Arrange
		this.ruleEvaluatorResult.setWeeklyOvertimeRuleEvaluationResult(this.ruleResult1);

		// Act
		boolean hasResults = this.ruleEvaluatorResult.hasResults();

		// Assert
		assertThat(hasResults).isTrue();
	}

	@Test
	@DisplayName("HasResults with no results - should return false")
	void testHasResultsWithNoResults() {
		// Act
		boolean hasResults = this.ruleEvaluatorResult.hasResults();

		// Assert
		assertThat(hasResults).isFalse();
	}

	@Test
	@DisplayName("GetTotalRuleEvaluations with rule evaluation results - should return correct count")
	void testGetTotalRuleEvaluationsWithRuleEvaluationResults() {
		// Arrange
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult2);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog2, this.ruleResult1);

		// Act
		int total = this.ruleEvaluatorResult.getTotalRuleEvaluations();

		// Assert
		assertThat(total).isEqualTo(3);
	}

	@Test
	@DisplayName("GetTotalRuleEvaluations with weekly overtime result - should include weekly overtime")
	void testGetTotalRuleEvaluationsWithWeeklyOvertimeResult() {
		// Arrange
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);
		this.ruleEvaluatorResult.setWeeklyOvertimeRuleEvaluationResult(this.ruleResult2);

		// Act
		int total = this.ruleEvaluatorResult.getTotalRuleEvaluations();

		// Assert
		assertThat(total).isEqualTo(2);
	}

	@Test
	@DisplayName("GetTotalRuleEvaluations with no results - should return zero")
	void testGetTotalRuleEvaluationsWithNoResults() {
		// Act
		int total = this.ruleEvaluatorResult.getTotalRuleEvaluations();

		// Assert
		assertThat(total).isZero();
	}

	@Test
	@DisplayName("GetTotalBillAmount with rule evaluation results - should return sum")
	void testGetTotalBillAmountWithRuleEvaluationResults() {
		// Arrange
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult2);

		// Act
		BigDecimal total = this.ruleEvaluatorResult.getTotalBillAmount();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("110.00"));
	}

	@Test
	@DisplayName("GetTotalBillAmount with no results - should return zero")
	void testGetTotalBillAmountWithNoResults() {
		// Act
		BigDecimal total = this.ruleEvaluatorResult.getTotalBillAmount();

		// Assert
		assertThat(total).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("GetTotalPayAmount with rule evaluation results - should return sum")
	void testGetTotalPayAmountWithRuleEvaluationResults() {
		// Arrange
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult2);

		// Act
		BigDecimal total = this.ruleEvaluatorResult.getTotalPayAmount();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("55.00"));
	}

	@Test
	@DisplayName("GetTotalPayAmount with no results - should return zero")
	void testGetTotalPayAmountWithNoResults() {
		// Act
		BigDecimal total = this.ruleEvaluatorResult.getTotalPayAmount();

		// Assert
		assertThat(total).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Builder pattern - should create RuleEvaluatorResult correctly")
	void testBuilderPattern() {
		// Arrange
		MoneyData weeklyOvertimeMoneyData = MoneyData.builder()
			.payAmount(new BigDecimal("100.00"))
			.billAmount(new BigDecimal("200.00"))
			.build();

		// Act
		RuleEvaluatorResult result = RuleEvaluatorResult.builder()
			.timesheet(this.timesheet)
			.weeklyOvertimeRuleEvaluationResult(this.ruleResult1)
			.weeklyOvertimeMoneyData(weeklyOvertimeMoneyData)
			.build();

		// Assert
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
		assertThat(result.getWeeklyOvertimeRuleEvaluationResult()).isEqualTo(this.ruleResult1);
		assertThat(result.getWeeklyOvertimeMoneyData()).isEqualTo(weeklyOvertimeMoneyData);
		assertThat(result.getRuleEvaluationResults()).isEmpty();
		assertThat(result.getMoneyData()).isNotNull();
		assertThat(result.getMoneyData().getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getMoneyData().getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("NoArgsConstructor - should create empty RuleEvaluatorResult")
	void testNoArgsConstructor() {
		// Act
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		// Assert
		assertThat(result.getTimesheet()).isNull();
		assertThat(result.getRuleEvaluationResults()).isEmpty();
		assertThat(result.getWeeklyOvertimeRuleEvaluationResult()).isNull();
		assertThat(result.getMoneyData()).isNotNull();
		assertThat(result.getMoneyData().getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getMoneyData().getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getWeeklyOvertimeMoneyData()).isNull();
		assertThat(result.getWeeklyOvertimeCandidateTimeRanges()).isEmpty();
	}

	@Test
	@DisplayName("AllArgsConstructor - should create RuleEvaluatorResult with all fields")
	void testAllArgsConstructor() {
		// Arrange
		MoneyData weeklyOvertimeMoneyData = MoneyData.builder()
			.payAmount(new BigDecimal("100.00"))
			.billAmount(new BigDecimal("200.00"))
			.build();

		// Act
		RuleEvaluatorResult result = new RuleEvaluatorResult(this.timesheet, null, this.ruleResult1, null,
				weeklyOvertimeMoneyData, null);

		// Assert
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
		assertThat(result.getWeeklyOvertimeRuleEvaluationResult()).isEqualTo(this.ruleResult1);
		assertThat(result.getWeeklyOvertimeMoneyData()).isEqualTo(weeklyOvertimeMoneyData);
		assertThat(result.getWeeklyOvertimeCandidateTimeRanges()).isNull();
	}

	@Test
	@DisplayName("Equals and hashCode - should work correctly")
	void testEqualsAndHashCode() {
		// Arrange
		RuleEvaluatorResult result1 = RuleEvaluatorResult.builder().timesheet(this.timesheet).build();

		RuleEvaluatorResult result2 = RuleEvaluatorResult.builder().timesheet(this.timesheet).build();

		RuleEvaluatorResult result3 = RuleEvaluatorResult.builder().timesheet(new Timesheet()).build();

		// Act & Assert
		assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
		assertThat(result1.hashCode()).isEqualTo(result2.hashCode()).isNotEqualTo(result3.hashCode());
	}

	@Test
	@DisplayName("ToString - should contain result information")
	void testToString() {
		// Arrange
		this.ruleEvaluatorResult.addRuleEvaluationResult(this.timeLog1, this.ruleResult1);

		// Act
		String result = this.ruleEvaluatorResult.toString();

		// Assert
		assertThat(result).contains("timesheet=").contains("ruleEvaluationResults=");
	}

	@Test
	@DisplayName("MoneyData operations - should work correctly")
	void testMoneyDataOperations() {
		// Arrange
		MoneyData moneyData1 = MoneyData.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("50.00"))
			.build();
		MoneyData moneyData2 = MoneyData.builder()
			.payAmount(new BigDecimal("30.00"))
			.billAmount(new BigDecimal("60.00"))
			.build();

		// Act
		MoneyData sum = moneyData1.add(moneyData2);
		MoneyData zero = MoneyData.zero();

		// Assert
		assertThat(sum.getPayAmount()).isEqualTo(new BigDecimal("55.00"));
		assertThat(sum.getBillAmount()).isEqualTo(new BigDecimal("110.00"));
		assertThat(zero.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(zero.getBillAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(moneyData1.hasValues()).isTrue();
		assertThat(zero.hasValues()).isFalse();
	}

	@Test
	@DisplayName("MoneyData with null values - should handle gracefully")
	void testMoneyDataWithNullValues() {
		// Arrange
		MoneyData moneyData = new MoneyData();

		// Act & Assert
		assertThat(moneyData.getPayAmountOrZero()).isEqualTo(BigDecimal.ZERO);
		assertThat(moneyData.getBillAmountOrZero()).isEqualTo(BigDecimal.ZERO);
		assertThat(moneyData.hasValues()).isFalse();
	}

	@Test
	@DisplayName("AddWeeklyOvertimeCandidateTimeRanges with valid ranges - should add ranges")
	void testAddWeeklyOvertimeCandidateTimeRangesWithValidRanges() {
		// Arrange
		RangeSet<LocalTime> rangeSet1 = TreeRangeSet.create();
		rangeSet1.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		RangeSet<LocalTime> rangeSet2 = TreeRangeSet.create();
		rangeSet2.add(Range.closedOpen(LocalTime.of(18, 0), LocalTime.of(20, 0)));

		List<RangeSet<LocalTime>> timeRanges = List.of(rangeSet1, rangeSet2);

		// Act
		this.ruleEvaluatorResult.addWeeklyOvertimeCandidateTimeRanges(timeRanges);

		// Assert
		List<RangeSet<LocalTime>> allRanges = this.ruleEvaluatorResult.getAllWeeklyOvertimeCandidateTimeRanges();
		assertThat(allRanges).hasSize(2).contains(rangeSet1, rangeSet2);
	}

	@Test
	@DisplayName("AddWeeklyOvertimeCandidateTimeRanges with null ranges - should handle gracefully")
	void testAddWeeklyOvertimeCandidateTimeRangesWithNullRanges() {
		// Act
		this.ruleEvaluatorResult.addWeeklyOvertimeCandidateTimeRanges(null);

		// Assert
		List<RangeSet<LocalTime>> allRanges = this.ruleEvaluatorResult.getAllWeeklyOvertimeCandidateTimeRanges();
		assertThat(allRanges).isEmpty();
	}

	@Test
	@DisplayName("AddWeeklyOvertimeCandidateTimeRanges with empty ranges - should handle gracefully")
	void testAddWeeklyOvertimeCandidateTimeRangesWithEmptyRanges() {
		// Act
		this.ruleEvaluatorResult.addWeeklyOvertimeCandidateTimeRanges(List.of());

		// Assert
		List<RangeSet<LocalTime>> allRanges = this.ruleEvaluatorResult.getAllWeeklyOvertimeCandidateTimeRanges();
		assertThat(allRanges).isEmpty();
	}

	@Test
	@DisplayName("GetAllWeeklyOvertimeCandidateTimeRanges with no ranges - should return empty list")
	void testGetAllWeeklyOvertimeCandidateTimeRangesWithNoRanges() {
		// Act
		List<RangeSet<LocalTime>> allRanges = this.ruleEvaluatorResult.getAllWeeklyOvertimeCandidateTimeRanges();

		// Assert
		assertThat(allRanges).isEmpty();
	}

	@Test
	@DisplayName("AddWeeklyOvertimeCandidateTimeRanges multiple calls - should accumulate ranges")
	void testAddWeeklyOvertimeCandidateTimeRangesMultipleCalls() {
		// Arrange
		RangeSet<LocalTime> rangeSet1 = TreeRangeSet.create();
		rangeSet1.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		RangeSet<LocalTime> rangeSet2 = TreeRangeSet.create();
		rangeSet2.add(Range.closedOpen(LocalTime.of(18, 0), LocalTime.of(20, 0)));

		// Act
		this.ruleEvaluatorResult.addWeeklyOvertimeCandidateTimeRanges(List.of(rangeSet1));
		this.ruleEvaluatorResult.addWeeklyOvertimeCandidateTimeRanges(List.of(rangeSet2));

		// Assert
		List<RangeSet<LocalTime>> allRanges = this.ruleEvaluatorResult.getAllWeeklyOvertimeCandidateTimeRanges();
		assertThat(allRanges).hasSize(2).contains(rangeSet1, rangeSet2);
	}

}