package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WeeklyRuleEvaluatorResult Tests")
class WeeklyRuleEvaluatorResultTests {

	private WeeklyRuleEvaluatorResult weeklyResult;

	private Timesheet timesheet;

	private RuleEvaluatorResult week1Result;

	private RuleEvaluatorResult week2Result;

	@BeforeEach
	void setUp() {
		this.timesheet = new Timesheet();
		this.timesheet.setId(1);

		this.week1Result = new RuleEvaluatorResult();
		this.week1Result.setTimesheet(this.timesheet);

		this.week2Result = new RuleEvaluatorResult();
		this.week2Result.setTimesheet(this.timesheet);

		this.weeklyResult = WeeklyRuleEvaluatorResult.builder().timesheet(this.timesheet).build();
	}

	@Test
	@DisplayName("AddWeeklyResult - should add weekly result correctly")
	void testAddWeeklyResult() {
		// Arrange
		LocalDate weekStartDate = LocalDate.of(2024, 1, 15);
		LocalDate weekEndDate = LocalDate.of(2024, 1, 21);

		// Act
		this.weeklyResult.addWeeklyResult(weekStartDate, weekEndDate, this.week1Result);

		// Assert
		assertThat(this.weeklyResult.getWeeklyResults()).hasSize(1);
		WeeklyRuleEvaluatorResult.WeeklyResult addedResult = this.weeklyResult.getWeeklyResults().get(0);
		assertThat(addedResult.getWeekStartDate()).isEqualTo(weekStartDate);
		assertThat(addedResult.getWeekEndDate()).isEqualTo(weekEndDate);
		assertThat(addedResult.getRuleEvaluatorResult()).isEqualTo(this.week1Result);
	}

	@Test
	@DisplayName("AddWeeklyResult multiple results - should add all results")
	void testAddWeeklyResultMultipleResults() {
		// Arrange
		LocalDate week1Start = LocalDate.of(2024, 1, 15);
		LocalDate week1End = LocalDate.of(2024, 1, 21);
		LocalDate week2Start = LocalDate.of(2024, 1, 22);
		LocalDate week2End = LocalDate.of(2024, 1, 28);

		// Act
		this.weeklyResult.addWeeklyResult(week1Start, week1End, this.week1Result);
		this.weeklyResult.addWeeklyResult(week2Start, week2End, this.week2Result);

		// Assert
		assertThat(this.weeklyResult.getWeeklyResults()).hasSize(2);
		assertThat(this.weeklyResult.getWeeklyResults().get(0).getWeekStartDate()).isEqualTo(week1Start);
		assertThat(this.weeklyResult.getWeeklyResults().get(1).getWeekStartDate()).isEqualTo(week2Start);
	}

	@Test
	@DisplayName("GetWeekCount with no results - should return zero")
	void testGetWeekCountWithNoResults() {
		// Act
		int weekCount = this.weeklyResult.getWeekCount();

		// Assert
		assertThat(weekCount).isZero();
	}

	@Test
	@DisplayName("GetWeekCount with results - should return correct count")
	void testGetWeekCountWithResults() {
		// Arrange
		this.weeklyResult.addWeeklyResult(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 21), this.week1Result);
		this.weeklyResult.addWeeklyResult(LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 28), this.week2Result);

		// Act
		int weekCount = this.weeklyResult.getWeekCount();

		// Assert
		assertThat(weekCount).isEqualTo(2);
	}

	@Test
	@DisplayName("HasResults with no results - should return false")
	void testHasResultsWithNoResults() {
		// Act
		boolean hasResults = this.weeklyResult.hasResults();

		// Assert
		assertThat(hasResults).isFalse();
	}

	@Test
	@DisplayName("HasResults with results - should return true")
	void testHasResultsWithResults() {
		// Arrange
		this.weeklyResult.addWeeklyResult(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 21), this.week1Result);

		// Act
		boolean hasResults = this.weeklyResult.hasResults();

		// Assert
		assertThat(hasResults).isTrue();
	}

	@Test
	@DisplayName("GetAllRuleEvaluatorResults with no results - should return empty list")
	void testGetAllRuleEvaluatorResultsWithNoResults() {
		// Act
		List<RuleEvaluatorResult> results = this.weeklyResult.getAllRuleEvaluatorResults();

		// Assert
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("GetAllRuleEvaluatorResults with results - should return all results")
	void testGetAllRuleEvaluatorResultsWithResults() {
		// Arrange
		this.weeklyResult.addWeeklyResult(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 21), this.week1Result);
		this.weeklyResult.addWeeklyResult(LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 28), this.week2Result);

		// Act
		List<RuleEvaluatorResult> results = this.weeklyResult.getAllRuleEvaluatorResults();

		// Assert
		assertThat(results).hasSize(2).contains(this.week1Result, this.week2Result);
	}

	@Test
	@DisplayName("Builder pattern - should create WeeklyRuleEvaluatorResult correctly")
	void testBuilderPattern() {
		// Act
		WeeklyRuleEvaluatorResult result = WeeklyRuleEvaluatorResult.builder().timesheet(this.timesheet).build();

		// Assert
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
		assertThat(result.getWeeklyResults()).isEmpty();
	}

	@Test
	@DisplayName("NoArgsConstructor - should create empty WeeklyRuleEvaluatorResult")
	void testNoArgsConstructor() {
		// Act
		WeeklyRuleEvaluatorResult result = new WeeklyRuleEvaluatorResult();

		// Assert
		assertThat(result.getTimesheet()).isNull();
		assertThat(result.getWeeklyResults()).isEmpty();
	}

	@Test
	@DisplayName("AllArgsConstructor - should create WeeklyRuleEvaluatorResult with all fields")
	void testAllArgsConstructor() {
		// Arrange
		List<WeeklyRuleEvaluatorResult.WeeklyResult> weeklyResults = List
			.of(WeeklyRuleEvaluatorResult.WeeklyResult.builder()
				.weekStartDate(LocalDate.of(2024, 1, 15))
				.weekEndDate(LocalDate.of(2024, 1, 21))
				.ruleEvaluatorResult(this.week1Result)
				.build());

		// Act
		WeeklyRuleEvaluatorResult result = new WeeklyRuleEvaluatorResult(this.timesheet, weeklyResults);

		// Assert
		assertThat(result.getTimesheet()).isEqualTo(this.timesheet);
		assertThat(result.getWeeklyResults()).isEqualTo(weeklyResults);
	}

	@Test
	@DisplayName("Equals and hashCode - should work correctly")
	void testEqualsAndHashCode() {
		// Arrange
		WeeklyRuleEvaluatorResult result1 = WeeklyRuleEvaluatorResult.builder().timesheet(this.timesheet).build();

		WeeklyRuleEvaluatorResult result2 = WeeklyRuleEvaluatorResult.builder().timesheet(this.timesheet).build();

		WeeklyRuleEvaluatorResult result3 = WeeklyRuleEvaluatorResult.builder().timesheet(new Timesheet()).build();

		// Act & Assert
		assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
		assertThat(result1.hashCode()).isEqualTo(result2.hashCode()).isNotEqualTo(result3.hashCode());
	}

	@Test
	@DisplayName("ToString - should contain result information")
	void testToString() {
		// Arrange
		this.weeklyResult.addWeeklyResult(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 21), this.week1Result);

		// Act
		String result = this.weeklyResult.toString();

		// Assert
		assertThat(result).contains("timesheet=").contains("weeklyResults=");
	}

	@Test
	@DisplayName("WeeklyResult getWeekIdentifier - should return descriptive identifier")
	void testWeeklyResultGetWeekIdentifier() {
		// Arrange
		LocalDate weekStartDate = LocalDate.of(2024, 1, 15);
		LocalDate weekEndDate = LocalDate.of(2024, 1, 21);

		WeeklyRuleEvaluatorResult.WeeklyResult mWeeklyResult = WeeklyRuleEvaluatorResult.WeeklyResult.builder()
			.weekStartDate(weekStartDate)
			.weekEndDate(weekEndDate)
			.ruleEvaluatorResult(this.week1Result)
			.build();

		// Act
		String identifier = mWeeklyResult.getWeekIdentifier();

		// Assert
		assertThat(identifier).isEqualTo("Week of 2024-01-15 to 2024-01-21");
	}

	@Test
	@DisplayName("WeeklyResult builder pattern - should create WeeklyResult correctly")
	void testWeeklyResultBuilderPattern() {
		// Arrange
		LocalDate weekStartDate = LocalDate.of(2024, 1, 15);
		LocalDate weekEndDate = LocalDate.of(2024, 1, 21);

		// Act
		WeeklyRuleEvaluatorResult.WeeklyResult result = WeeklyRuleEvaluatorResult.WeeklyResult.builder()
			.weekStartDate(weekStartDate)
			.weekEndDate(weekEndDate)
			.ruleEvaluatorResult(this.week1Result)
			.build();

		// Assert
		assertThat(result.getWeekStartDate()).isEqualTo(weekStartDate);
		assertThat(result.getWeekEndDate()).isEqualTo(weekEndDate);
		assertThat(result.getRuleEvaluatorResult()).isEqualTo(this.week1Result);
	}

	@Test
	@DisplayName("WeeklyResult NoArgsConstructor - should create empty WeeklyResult")
	void testWeeklyResultNoArgsConstructor() {
		// Act
		WeeklyRuleEvaluatorResult.WeeklyResult result = new WeeklyRuleEvaluatorResult.WeeklyResult();

		// Assert
		assertThat(result.getWeekStartDate()).isNull();
		assertThat(result.getWeekEndDate()).isNull();
		assertThat(result.getRuleEvaluatorResult()).isNull();
	}

	@Test
	@DisplayName("WeeklyResult AllArgsConstructor - should create WeeklyResult with all fields")
	void testWeeklyResultAllArgsConstructor() {
		// Arrange
		LocalDate weekStartDate = LocalDate.of(2024, 1, 15);
		LocalDate weekEndDate = LocalDate.of(2024, 1, 21);

		// Act
		WeeklyRuleEvaluatorResult.WeeklyResult result = new WeeklyRuleEvaluatorResult.WeeklyResult(weekStartDate,
				weekEndDate, this.week1Result);

		// Assert
		assertThat(result.getWeekStartDate()).isEqualTo(weekStartDate);
		assertThat(result.getWeekEndDate()).isEqualTo(weekEndDate);
		assertThat(result.getRuleEvaluatorResult()).isEqualTo(this.week1Result);
	}

	@Test
	@DisplayName("WeeklyResult equals and hashCode - should work correctly")
	void testWeeklyResultEqualsAndHashCode() {
		// Arrange
		LocalDate weekStartDate = LocalDate.of(2024, 1, 15);
		LocalDate weekEndDate = LocalDate.of(2024, 1, 21);

		WeeklyRuleEvaluatorResult.WeeklyResult result1 = WeeklyRuleEvaluatorResult.WeeklyResult.builder()
			.weekStartDate(weekStartDate)
			.weekEndDate(weekEndDate)
			.ruleEvaluatorResult(this.week1Result)
			.build();

		WeeklyRuleEvaluatorResult.WeeklyResult result2 = WeeklyRuleEvaluatorResult.WeeklyResult.builder()
			.weekStartDate(weekStartDate)
			.weekEndDate(weekEndDate)
			.ruleEvaluatorResult(this.week1Result)
			.build();

		WeeklyRuleEvaluatorResult.WeeklyResult result3 = WeeklyRuleEvaluatorResult.WeeklyResult.builder()
			.weekStartDate(LocalDate.of(2024, 1, 22))
			.weekEndDate(LocalDate.of(2024, 1, 28))
			.ruleEvaluatorResult(this.week2Result)
			.build();

		// Act & Assert
		assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
		assertThat(result1.hashCode()).isEqualTo(result2.hashCode()).isNotEqualTo(result3.hashCode());
	}

	@Test
	@DisplayName("WeeklyResult toString - should contain result information")
	void testWeeklyResultToString() {
		// Arrange
		LocalDate weekStartDate = LocalDate.of(2024, 1, 15);
		LocalDate weekEndDate = LocalDate.of(2024, 1, 21);

		WeeklyRuleEvaluatorResult.WeeklyResult result = WeeklyRuleEvaluatorResult.WeeklyResult.builder()
			.weekStartDate(weekStartDate)
			.weekEndDate(weekEndDate)
			.ruleEvaluatorResult(this.week1Result)
			.build();

		// Act
		String resultString = result.toString();

		// Assert
		assertThat(resultString).contains("weekStartDate=2024-01-15")
			.contains("weekEndDate=2024-01-21")
			.contains("ruleEvaluatorResult=");
	}

}