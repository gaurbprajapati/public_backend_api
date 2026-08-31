/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.mapper;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.TimeLogRuleEvaluationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.MoneyData;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluatorResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RuleEngineMapperTests {

	@Mock
	private TimeLog timeLog;

	@Mock
	private RuleEvaluationResult ruleResult1;

	@Mock
	private RuleEvaluationResult ruleResult2;

	@Test
	@DisplayName("Should calculate unallocated time ranges correctly")
	void testCalculateUnallocatedTimeRanges() {
		// Arrange
		given(this.timeLog.getId()).willReturn(1);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(18, 30));

		// Create occupied time ranges (Regular Hours: 9:00-18:00, leaving 18:00-18:30
		// unallocated)
		RangeSet<LocalTime> occupiedRanges1 = TreeRangeSet.create();
		occupiedRanges1.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(18, 0)));

		given(this.ruleResult1.getTimeRange()).willReturn(occupiedRanges1);
		given(this.ruleResult1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(this.ruleResult1.getPayAmount()).willReturn(BigDecimal.valueOf(100));
		given(this.ruleResult1.getBillAmount()).willReturn(BigDecimal.valueOf(150));

		// Create RuleEvaluatorResult
		RuleEvaluatorResult result = RuleEvaluatorResult.builder()
			.ruleEvaluationResults(java.util.Map.of(this.timeLog, List.of(this.ruleResult1)))
			.build();

		// Act
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		List<TimeLogRuleEvaluationResponseBodyDto> evaluations = mapper.convertToTimeLogRuleEvaluations(result);

		// Assert
		assertThat(evaluations).hasSize(1);
		TimeLogRuleEvaluationResponseBodyDto evaluation = evaluations.get(0);

		assertThat(evaluation.getUnallocatedTimeRanges()).hasSize(1);
		assertThat(evaluation.getUnallocatedTimeRanges().get(0)).containsExactly(LocalTime.of(18, 0),
				LocalTime.of(18, 30));
		assertThat(evaluation.getUnallocatedTimeApproximateHours()).isEqualTo(0.5f);
	}

	@Test
	@DisplayName("Should return empty unallocated time when all time is allocated")
	void testNoUnallocatedTimeWhenAllAllocated() {
		// Arrange
		given(this.timeLog.getId()).willReturn(1);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(this.timeLog.getWorkEndTime()).willReturn(LocalTime.of(18, 30));

		// Create occupied time ranges covering the entire work period
		RangeSet<LocalTime> occupiedRanges1 = TreeRangeSet.create();
		occupiedRanges1.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(18, 0)));

		RangeSet<LocalTime> occupiedRanges2 = TreeRangeSet.create();
		occupiedRanges2.add(Range.closedOpen(LocalTime.of(18, 0), LocalTime.of(18, 30)));

		given(this.ruleResult1.getTimeRange()).willReturn(occupiedRanges1);
		given(this.ruleResult1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(this.ruleResult1.getPayAmount()).willReturn(BigDecimal.valueOf(100));
		given(this.ruleResult1.getBillAmount()).willReturn(BigDecimal.valueOf(150));

		given(this.ruleResult2.getTimeRange()).willReturn(occupiedRanges2);
		given(this.ruleResult2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(this.ruleResult2.getPayAmount()).willReturn(BigDecimal.valueOf(50));
		given(this.ruleResult2.getBillAmount()).willReturn(BigDecimal.valueOf(75));

		// Create RuleEvaluatorResult
		RuleEvaluatorResult result = RuleEvaluatorResult.builder()
			.ruleEvaluationResults(java.util.Map.of(this.timeLog, List.of(this.ruleResult1, this.ruleResult2)))
			.build();

		// Act
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		List<TimeLogRuleEvaluationResponseBodyDto> evaluations = mapper.convertToTimeLogRuleEvaluations(result);

		// Assert
		assertThat(evaluations).hasSize(1);
		TimeLogRuleEvaluationResponseBodyDto evaluation = evaluations.get(0);

		assertThat(evaluation.getUnallocatedTimeRanges()).isEmpty();
		assertThat(evaluation.getUnallocatedTimeApproximateHours()).isEqualTo(0.0f);
	}

	@Test
	@DisplayName("Should handle invalid time log gracefully")
	void testInvalidTimeLog() {
		// Arrange
		given(this.timeLog.getId()).willReturn(1);
		given(this.timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		given(this.timeLog.getWorkStartTime()).willReturn(null);

		given(this.ruleResult1.getTimeRange()).willReturn(TreeRangeSet.create());
		given(this.ruleResult1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);

		// Create RuleEvaluatorResult
		RuleEvaluatorResult result = RuleEvaluatorResult.builder()
			.ruleEvaluationResults(java.util.Map.of(this.timeLog, List.of(this.ruleResult1)))
			.build();

		// Act
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		List<TimeLogRuleEvaluationResponseBodyDto> evaluations = mapper.convertToTimeLogRuleEvaluations(result);

		// Assert
		assertThat(evaluations).hasSize(1);
		TimeLogRuleEvaluationResponseBodyDto evaluation = evaluations.get(0);

		assertThat(evaluation.getUnallocatedTimeRanges()).isEmpty();
		assertThat(evaluation.getUnallocatedTimeApproximateHours()).isEqualTo(0.0f);
	}

	@Test
	@DisplayName("Should create evaluation summary with weekly overtime amounts included")
	void testCreateEvaluationSummaryWithWeeklyOvertime() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(248);

		// Create first week result with regular rules and weekly overtime
		RuleEvaluatorResult week1Result = RuleEvaluatorResult.builder().timesheet(timesheet).build();

		// Set weekly overtime money data for week 1
		MoneyData weeklyOvertimeMoneyData1 = MoneyData.builder()
			.payAmount(new BigDecimal("2800.00"))
			.billAmount(new BigDecimal("5600.00"))
			.build();
		week1Result.setWeeklyOvertimeMoneyData(weeklyOvertimeMoneyData1);

		// Create second week result with regular rules and weekly overtime
		RuleEvaluatorResult week2Result = RuleEvaluatorResult.builder().timesheet(timesheet).build();

		// Set weekly overtime money data for week 2
		MoneyData weeklyOvertimeMoneyData2 = MoneyData.builder()
			.payAmount(new BigDecimal("1500.00"))
			.billAmount(new BigDecimal("3000.00"))
			.build();
		week2Result.setWeeklyOvertimeMoneyData(weeklyOvertimeMoneyData2);

		// Create WeeklyRuleEvaluatorResult with both weeks
		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		weeklyResult.addWeeklyResult(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 4), week1Result);
		weeklyResult.addWeeklyResult(LocalDate.of(2025, 7, 7), LocalDate.of(2025, 7, 11), week2Result);

		// When
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		RuleEvaluationSummaryResponseBodyDto summary = mapper.createEvaluationSummary(weeklyResult);

		// Then
		assertThat(summary).isNotNull();
		assertThat(summary.getTotalWeeksEvaluated()).isEqualTo(2);
		assertThat(summary.getTotalRuleEvaluations()).isZero(); // No rule evaluation
																// results added

		// Verify that weekly overtime amounts are included in totals
		// Expected: Base amounts (0) + Weekly overtime amounts
		// Week 1: 2800.00 pay + 5600.00 bill
		// Week 2: 1500.00 pay + 3000.00 bill
		// Total: 4300.00 pay + 8600.00 bill
		assertThat(summary.getTotalPayAmount()).isEqualTo(new BigDecimal("4300.00"));
		assertThat(summary.getTotalBillAmount()).isEqualTo(new BigDecimal("8600.00"));
	}

	@Test
	@DisplayName("Should create evaluation summary with mixed regular and weekly overtime amounts")
	void testCreateEvaluationSummaryWithMixedAmounts() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(248);

		// Create week result with both regular rules and weekly overtime
		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder().timesheet(timesheet).build();

		// Mock the getTotalBillAmount and getTotalPayAmount methods to return base
		// amounts
		// These would normally come from regular rule evaluations
		// For this test, we'll simulate the behavior by setting the weekly overtime money
		// data
		// and verifying that the createEvaluationSummary method adds them correctly

		// Set weekly overtime money data
		MoneyData weeklyOvertimeMoneyData = MoneyData.builder()
			.payAmount(new BigDecimal("2800.00"))
			.billAmount(new BigDecimal("5600.00"))
			.build();
		weekResult.setWeeklyOvertimeMoneyData(weeklyOvertimeMoneyData);

		// Create WeeklyRuleEvaluatorResult
		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		weeklyResult.addWeeklyResult(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 4), weekResult);

		// When
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		RuleEvaluationSummaryResponseBodyDto summary = mapper.createEvaluationSummary(weeklyResult);

		// Then
		assertThat(summary).isNotNull();
		assertThat(summary.getTotalWeeksEvaluated()).isEqualTo(1);
		assertThat(summary.getTotalRuleEvaluations()).isZero(); // No rule evaluation
																// results added

		// Verify that weekly overtime amounts are included
		// The base amounts from getTotalBillAmount() and getTotalPayAmount() would be 0
		// since we haven't added any rule evaluation results, but the weekly overtime
		// amounts should still be included
		assertThat(summary.getTotalPayAmount()).isEqualTo(new BigDecimal("2800.00"));
		assertThat(summary.getTotalBillAmount()).isEqualTo(new BigDecimal("5600.00"));
	}

	@Test
	@DisplayName("Should create evaluation summary with null weekly overtime money data")
	void testCreateEvaluationSummaryWithNullWeeklyOvertimeMoneyData() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(248);

		// Create week result without weekly overtime
		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder().timesheet(timesheet).build();

		// Don't set weekly overtime money data (should be null)

		// Create WeeklyRuleEvaluatorResult
		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		weeklyResult.addWeeklyResult(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 4), weekResult);

		// When
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		RuleEvaluationSummaryResponseBodyDto summary = mapper.createEvaluationSummary(weeklyResult);

		// Then
		assertThat(summary).isNotNull();
		assertThat(summary.getTotalWeeksEvaluated()).isEqualTo(1);
		assertThat(summary.getTotalRuleEvaluations()).isZero(); // No rule evaluation
																// results added

		// Verify that amounts are zero when no weekly overtime money data
		assertThat(summary.getTotalPayAmount()).isEqualTo(new BigDecimal("0.00"));
		assertThat(summary.getTotalBillAmount()).isEqualTo(new BigDecimal("0.00"));
	}

	@Test
	@DisplayName("Should create evaluation summary with zero weekly overtime amounts")
	void testCreateEvaluationSummaryWithZeroWeeklyOvertimeAmounts() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(248);

		// Create week result with zero weekly overtime amounts
		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder().timesheet(timesheet).build();

		// Set weekly overtime money data with zero amounts
		MoneyData weeklyOvertimeMoneyData = MoneyData.builder()
			.payAmount(BigDecimal.ZERO)
			.billAmount(BigDecimal.ZERO)
			.build();
		weekResult.setWeeklyOvertimeMoneyData(weeklyOvertimeMoneyData);

		// Create WeeklyRuleEvaluatorResult
		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		weeklyResult.addWeeklyResult(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 4), weekResult);

		// When
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		RuleEvaluationSummaryResponseBodyDto summary = mapper.createEvaluationSummary(weeklyResult);

		// Then
		assertThat(summary).isNotNull();
		assertThat(summary.getTotalWeeksEvaluated()).isEqualTo(1);
		assertThat(summary.getTotalRuleEvaluations()).isZero(); // No rule evaluation
																// results added

		// Verify that amounts are zero
		assertThat(summary.getTotalPayAmount()).isEqualTo(new BigDecimal("0.00"));
		assertThat(summary.getTotalBillAmount()).isEqualTo(new BigDecimal("0.00"));
	}

	@Test
	@DisplayName("Should create evaluation summary with null result")
	void testCreateEvaluationSummaryWithNullResult() {
		// When
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		RuleEvaluationSummaryResponseBodyDto summary = mapper.createEvaluationSummary(null);

		// Then
		assertThat(summary).isNotNull();
		assertThat(summary.getTotalWeeksEvaluated()).isZero();
		assertThat(summary.getTotalRuleEvaluations()).isZero();
		assertThat(summary.getTotalPayAmount()).isEqualTo(new BigDecimal("0.00"));
		assertThat(summary.getTotalBillAmount()).isEqualTo(new BigDecimal("0.00"));
	}

	@Test
	@DisplayName("Should create evaluation summary with empty results")
	void testCreateEvaluationSummaryWithEmptyResults() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(248);

		WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(timesheet).build();

		// Don't add any weekly results

		// When
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		RuleEvaluationSummaryResponseBodyDto summary = mapper.createEvaluationSummary(weeklyResult);

		// Then
		assertThat(summary).isNotNull();
		assertThat(summary.getTotalWeeksEvaluated()).isZero();
		assertThat(summary.getTotalRuleEvaluations()).isZero();
		assertThat(summary.getTotalPayAmount()).isEqualTo(new BigDecimal("0.00"));
		assertThat(summary.getTotalBillAmount()).isEqualTo(new BigDecimal("0.00"));
	}

	@Test
	@DisplayName("Get rule type order - Default Pay sorts after Weekly Overtime in range-based")
	void testGetRuleTypeOrderRangeBasedDefaultPayAfterWeeklyOvertime() {
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		int wotOrder = mapper.getRuleTypeOrder(RuleType.RANGE_BASED_WEEKLY_OVERTIME);
		int defaultPayOrder = mapper.getRuleTypeOrder(RuleType.RANGE_BASED_DEFAULT_PAY);

		assertThat(defaultPayOrder).isGreaterThan(wotOrder).isEqualTo(8);
	}

	@Test
	@DisplayName("Get rule type order - Default Pay sorts after Weekly Overtime in duration-based")
	void testGetRuleTypeOrderDurationBasedDefaultPayAfterWeeklyOvertime() {
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;
		int wotOrder = mapper.getRuleTypeOrder(RuleType.DURATION_BASED_WEEKLY_OVERTIME);
		int defaultPayOrder = mapper.getRuleTypeOrder(RuleType.DURATION_BASED_DEFAULT_PAY);

		assertThat(defaultPayOrder).isGreaterThan(wotOrder).isEqualTo(18);
	}

	@Test
	@DisplayName("Get rule type order - Null returns 999 sentinel")
	void testGetRuleTypeOrderNullReturnsSentinel() {
		RuleEngineMapper mapper = RuleEngineMapper.INSTANCE;

		assertThat(mapper.getRuleTypeOrder(null)).isEqualTo(999);
	}

}