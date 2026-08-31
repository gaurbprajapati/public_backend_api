package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IEvaluatableRule Tests")
class IEvaluatableRuleTests {

	@Test
	@DisplayName("isSystemRule - with Regular Hours rule returns true")
	void testIsSystemRuleWithRegularHoursRuleReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_REGULAR_HOURS);

		// Act
		boolean result = rule.isSystemRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isSystemRule - with Break rule returns true")
	void testIsSystemRuleWithBreakRuleReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_BREAK);

		// Act
		boolean result = rule.isSystemRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isSystemRule - with Daily Overtime rule returns false")
	void testIsSystemRuleWithDailyOvertimeRuleReturnsFalse() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_DAILY_OVERTIME);

		// Act
		boolean result = rule.isSystemRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isRegularHoursRule - with Range-based Regular Hours returns true")
	void testIsRegularHoursRuleWithRangeBasedRegularHoursReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_REGULAR_HOURS);

		// Act
		boolean result = rule.isRegularHoursRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isRegularHoursRule - with Duration-based Regular Hours returns true")
	void testIsRegularHoursRuleWithDurationBasedRegularHoursReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_REGULAR_HOURS);

		// Act
		boolean result = rule.isRegularHoursRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isRegularHoursRule - with non-Regular Hours rule returns false")
	void testIsRegularHoursRuleWithNonRegularHoursRuleReturnsFalse() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_DAILY_OVERTIME);

		// Act
		boolean result = rule.isRegularHoursRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isBreakRule - with Range-based Break returns true")
	void testIsBreakRuleWithRangeBasedBreakReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_BREAK);

		// Act
		boolean result = rule.isBreakRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isBreakRule - with Duration-based Break returns true")
	void testIsBreakRuleWithDurationBasedBreakReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_BREAK);

		// Act
		boolean result = rule.isBreakRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isBreakRule - with non-Break rule returns false")
	void testIsBreakRuleWithNonBreakRuleReturnsFalse() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_DAILY_OVERTIME);

		// Act
		boolean result = rule.isBreakRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isDailyOvertimeRule - with Range-based Daily Overtime returns true")
	void testIsDailyOvertimeRuleWithRangeBasedDailyOvertimeReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_DAILY_OVERTIME);

		// Act
		boolean result = rule.isDailyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isDailyOvertimeRule - with Duration-based Daily Overtime returns true")
	void testIsDailyOvertimeRuleWithDurationBasedDailyOvertimeReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_DAILY_OVERTIME);

		// Act
		boolean result = rule.isDailyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isDailyOvertimeRule - with non-Daily Overtime rule returns false")
	void testIsDailyOvertimeRuleWithNonDailyOvertimeRuleReturnsFalse() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_REGULAR_HOURS);

		// Act
		boolean result = rule.isDailyOvertimeRule();

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isWeeklyOvertimeRule - with Range-based Weekly Overtime returns true")
	void testIsWeeklyOvertimeRuleWithRangeBasedWeeklyOvertimeReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_WEEKLY_OVERTIME);

		// Act
		boolean result = rule.isWeeklyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWeeklyOvertimeRule - with Duration-based Weekly Overtime returns true")
	void testIsWeeklyOvertimeRuleWithDurationBasedWeeklyOvertimeReturnsTrue() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.DURATION_BASED_WEEKLY_OVERTIME);

		// Act
		boolean result = rule.isWeeklyOvertimeRule();

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWeeklyOvertimeRule - with non-Weekly Overtime rule returns false")
	void testIsWeeklyOvertimeRuleWithNonWeeklyOvertimeRuleReturnsFalse() {
		// Arrange
		IEvaluatableRule rule = createMockRule(RuleType.RANGE_BASED_REGULAR_HOURS);

		// Act
		boolean result = rule.isWeeklyOvertimeRule();

		// Assert
		assertThat(result).isFalse();
	}

	private IEvaluatableRule createMockRule(RuleType ruleType) {
		return new IEvaluatableRule() {
			@Override
			public String getRuleName() {
				return "Test Rule";
			}

			@Override
			public RuleType getRuleType() {
				return ruleType;
			}

			@Override
			public List<WorkDay> getWorkDays() {
				return List.of(WorkDay.MONDAY);
			}

			@Override
			public boolean isApplicableOnDay(WorkDay timeLogDay) {
				return true;
			}
		};
	}

}