package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.CustomRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.VirtualRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("UnifiedRuleManager Tests")
class UnifiedRuleManagerTests {

	@Test
	@DisplayName("createUnifiedRuleList - with custom rules and virtual rules")
	void testCreateUnifiedRuleListWithCustomRulesAndVirtualRules() {
		// Arrange
		CustomRule customRule1 = mock(CustomRule.class);
		CustomRule customRule2 = mock(CustomRule.class);
		VirtualRule virtualRule1 = mock(VirtualRule.class);
		VirtualRule virtualRule2 = mock(VirtualRule.class);

		// Set up rule types for the mocked rules
		given(customRule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(customRule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(virtualRule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(virtualRule2.getRuleType()).willReturn(RuleType.RANGE_BASED_WEEKLY_OVERTIME);

		List<CustomRule> customRules = List.of(customRule1, customRule2);
		List<VirtualRule> virtualRules = List.of(virtualRule1, virtualRule2);

		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList(customRules, virtualRules);

		// Assert
		assertThat(result).isNotNull().hasSize(4).containsAll(customRules).containsAll(virtualRules);
	}

	@Test
	@DisplayName("createUnifiedRuleList - with custom rules and single virtual rule")
	void testCreateUnifiedRuleListWithCustomRulesAndSingleVirtualRule() {
		// Arrange
		CustomRule customRule1 = mock(CustomRule.class);
		CustomRule customRule2 = mock(CustomRule.class);
		VirtualRule virtualRule = mock(VirtualRule.class);

		// Set up rule types for the mocked rules
		given(customRule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(customRule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(virtualRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);

		List<CustomRule> customRules = List.of(customRule1, customRule2);
		List<VirtualRule> virtualRules = List.of(virtualRule);

		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList(customRules, virtualRules);

		// Assert
		assertThat(result).isNotNull().hasSize(3).containsAll(customRules).containsAll(virtualRules);
	}

	@Test
	@DisplayName("createUnifiedRuleList - with null custom rules")
	void testCreateUnifiedRuleListWithNullCustomRules() {
		// Arrange
		VirtualRule virtualRule = mock(VirtualRule.class);
		given(virtualRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		List<VirtualRule> virtualRules = List.of(virtualRule);

		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList(null, virtualRules);

		// Assert
		assertThat(result).isNotNull().hasSize(1).containsAll(virtualRules);
	}

	@Test
	@DisplayName("createUnifiedRuleList - with null virtual rules")
	void testCreateUnifiedRuleListWithNullVirtualRules() {
		// Arrange
		CustomRule customRule = mock(CustomRule.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		List<CustomRule> customRules = List.of(customRule);
		List<VirtualRule> virtualRules = null;

		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList(customRules, virtualRules);

		// Assert
		assertThat(result).isNotNull().hasSize(1).containsAll(customRules);
	}

	@Test
	@DisplayName("createUnifiedRuleList - with both null")
	void testCreateUnifiedRuleListWithBothNull() {
		// Arrange
		List<CustomRule> customRules = null;
		List<VirtualRule> virtualRules = null;

		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList(customRules, virtualRules);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("createRegularHoursVirtualRule - with range-based rule type")
	void testCreateRegularHoursVirtualRuleWithRangeBasedRuleType() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List.of(
				new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200), // 8
																					// hours,
																					// 9:00-17:00
				new TemplateWorkDay(WorkDay.TUESDAY.getId(), 28800, 32400, 61200) // 8
																					// hours,
																					// 9:00-17:00
		);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);

		// Act
		VirtualRule result = UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS,
				timesheet);

		// Assert
		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.RANGE_BASED_REGULAR_HOURS));
	}

	@Test
	@DisplayName("createRegularHoursVirtualRule - with duration-based rule type")
	void testCreateRegularHoursVirtualRuleWithDurationBasedRuleType() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200) // 8
																					// hours,
																					// 9:00-17:00
			);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);

		// Act
		VirtualRule result = UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.DURATION_BASED_REGULAR_HOURS,
				timesheet);

		// Assert
		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.DURATION_BASED_REGULAR_HOURS));
	}

	@Test
	@DisplayName("createRegularHoursVirtualRule - with null timesheet")
	void testCreateRegularHoursVirtualRuleWithNullTimesheet() {
		// Act & Assert
		assertThatThrownBy(
				() -> UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS, null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("createBreakVirtualRule - with range-based rule type")
	void testCreateBreakVirtualRuleWithRangeBasedRuleType() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200) // 8
																					// hours,
																					// 9:00-17:00
			);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);

		// Act
		VirtualRule result = UnifiedRuleManager.createBreakVirtualRule(RuleType.RANGE_BASED_BREAK, timesheet);

		// Assert
		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BREAK))
			.satisfies((rule) -> assertThat(rule.getWorkDays()).isEmpty())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.MONDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.TUESDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SUNDAY)).isTrue());
	}

	@Test
	@DisplayName("createBreakVirtualRule - with duration-based rule type")
	void testCreateBreakVirtualRuleWithDurationBasedRuleType() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List.of(
				new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200), // 8
																					// hours,
																					// 9:00-17:00
				new TemplateWorkDay(WorkDay.TUESDAY.getId(), 28800, 32400, 61200) // 8
																					// hours,
																					// 9:00-17:00
		);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);

		// Act
		VirtualRule result = UnifiedRuleManager.createBreakVirtualRule(RuleType.DURATION_BASED_BREAK, timesheet);

		// Assert
		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK))
			.satisfies((rule) -> assertThat(rule.getWorkDays()).isEmpty())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.MONDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.TUESDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SUNDAY)).isTrue());
	}

	@Test
	@DisplayName("createBreakVirtualRule - with null timesheet")
	void testCreateBreakVirtualRuleWithNullTimesheet() {
		// Act
		VirtualRule result = UnifiedRuleManager.createBreakVirtualRule(RuleType.RANGE_BASED_BREAK, null);

		// Assert
		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BREAK))
			.satisfies((rule) -> assertThat(rule.getWorkDays()).isEmpty())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.MONDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SUNDAY)).isTrue());
	}

	@Test
	@DisplayName("createBreakVirtualRule - with timesheet having no settings")
	void testCreateBreakVirtualRuleWithTimesheetNoSettings() {
		// Arrange
		var timesheet = mock(io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		given(timesheet.getTimesheetSetting()).willReturn(null);

		// Act
		VirtualRule result = UnifiedRuleManager.createBreakVirtualRule(RuleType.DURATION_BASED_BREAK, timesheet);

		// Assert
		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.DURATION_BASED_BREAK))
			.satisfies((rule) -> assertThat(rule.getWorkDays()).isEmpty())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.MONDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SUNDAY)).isTrue());
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - with valid timesheet")
	void testGetWorkDaysFromTimesheetWithValidTimesheet() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List.of(
				new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200), // 8
																					// hours,
																					// 9:00-17:00
				new TemplateWorkDay(WorkDay.TUESDAY.getId(), 28800, 32400, 61200) // 8
																					// hours,
																					// 9:00-17:00
		);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);

		// Act - This is a private method, so we test it indirectly through
		// createRegularHoursVirtualRule
		VirtualRule result = UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS,
				timesheet);

		// Assert
		assertThat(result).isNotNull().satisfies((rule) -> assertThat(rule.getWorkDays()).isNotNull().hasSize(2));
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - with null work days")
	void testGetWorkDaysFromTimesheetWithNullWorkDays() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(null);

		// Act
		VirtualRule result = UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS,
				timesheet);

		// Assert
		assertThat(result).isNull(); // No work days configured, so no virtual rule
										// created
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - with empty work days")
	void testGetWorkDaysFromTimesheetWithEmptyWorkDays() {
		// Arrange
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>());

		// Act
		VirtualRule result = UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS,
				timesheet);

		// Assert
		assertThat(result).isNull(); // No work days configured, so no virtual rule
										// created
	}

	@Test
	@DisplayName("sortRulesByPrecedence - throws IllegalStateException when precedence order is invalid")
	void testSortRulesByPrecedenceThrowsWhenOrderInvalid() {
		// Arrange
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		// Simulate both as range-based rules with same precedence
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		List<IEvaluatableRule> rules = List.of(rule1, rule2);
		// Mock RulePrecedenceConfig to return false for validation
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(rule1.getRuleType()))
				.thenReturn(true);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(rule2.getRuleType()))
				.thenReturn(true);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule1.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule2.getRuleType()))
				.thenReturn(2);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.validateRulePrecedenceOrder(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(false);
			// Act & Assert
			assertThatThrownBy(() -> UnifiedRuleManager.sortRulesByPrecedence(rules))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Rules are not in correct precedence order after sorting");
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - sorts by precedence and threshold for daily overtime rules")
	void testSortRangeBasedRulesSortsByPrecedenceAndThreshold() {
		// Arrange
		CustomRule rule1 = mock(CustomRule.class);
		CustomRule rule2 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(true);
		Duration threshold1 = Duration.ofHours(8);
		Duration threshold2 = Duration.ofHours(10);
		given(rule1.getDailyThreshold()).willReturn(threshold1);
		given(rule2.getDailyThreshold()).willReturn(threshold2);
		List<IEvaluatableRule> rules = List.of(rule2, rule1); // Out of order
		// Use reflection to call private method
		try {
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, rules);
			assertThat(sorted.get(0)).isEqualTo(rule1); // Lower threshold first
			assertThat(sorted.get(1)).isEqualTo(rule2);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	@DisplayName("sortDurationBasedRules - sorts by precedence and threshold for daily overtime rules")
	void testSortDurationBasedRulesSortsByPrecedenceAndThreshold() {
		// Arrange
		CustomRule rule1 = mock(CustomRule.class);
		CustomRule rule2 = mock(CustomRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.DURATION_BASED_DAILY_OVERTIME);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(true);
		Duration threshold1 = Duration.ofHours(8);
		Duration threshold2 = Duration.ofHours(10);
		given(rule1.getDailyThreshold()).willReturn(threshold1);
		given(rule2.getDailyThreshold()).willReturn(threshold2);
		List<IEvaluatableRule> rules = List.of(rule2, rule1); // Out of order
		// Use reflection to call private method
		try {
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortDurationBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, rules);
			assertThat(sorted.get(0)).isEqualTo(rule1); // Lower threshold first
			assertThat(sorted.get(1)).isEqualTo(rule2);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	@DisplayName("getDailyThreshold - covers all branches")
	void testGetDailyThresholdCoversAllBranches() {
		// CustomRule with threshold
		CustomRule customRule = mock(CustomRule.class);
		given(customRule.getDailyThreshold()).willReturn(Duration.ofHours(7));
		long threshold = invokeGetDailyThreshold(customRule);
		assertThat(threshold).isEqualTo(7);
		// CustomRule with null threshold
		CustomRule customRuleNull = mock(CustomRule.class);
		given(customRuleNull.getDailyThreshold()).willReturn(null);
		threshold = invokeGetDailyThreshold(customRuleNull);
		assertThat(threshold).isZero();
		// Non-CustomRule
		IEvaluatableRule nonCustomRule = mock(IEvaluatableRule.class);
		threshold = invokeGetDailyThreshold(nonCustomRule);
		assertThat(threshold).isZero();
	}

	// Helper to invoke private getDailyThreshold
	private long invokeGetDailyThreshold(IEvaluatableRule rule) {
		try {
			var method = UnifiedRuleManager.class.getDeclaredMethod("getDailyThreshold", IEvaluatableRule.class);
			method.setAccessible(true);
			return (long) method.invoke(null, rule);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	@DisplayName("createUnifiedRuleList (overload) - with custom rules and single virtual rule")
	void testCreateUnifiedRuleListOverloadWithCustomRulesAndSingleVirtualRule() {
		// Arrange
		CustomRule customRule1 = mock(CustomRule.class);
		CustomRule customRule2 = mock(CustomRule.class);
		VirtualRule virtualRule = mock(VirtualRule.class);
		given(customRule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(customRule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(virtualRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		List<CustomRule> customRules = List.of(customRule1, customRule2);
		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList(customRules, virtualRule);
		// Assert
		assertThat(result).isNotNull().hasSize(3).contains(customRule1, customRule2, virtualRule);
	}

	@Test
	@DisplayName("createUnifiedRuleList (overload) - with null custom rules and null virtual rule")
	void testCreateUnifiedRuleListOverloadWithNulls() {
		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleList((List<CustomRule>) null,
				(VirtualRule) null);
		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("createUnifiedRuleListWithSystemRules - both virtual rules present")
	void testCreateUnifiedRuleListWithSystemRulesBothPresent() {
		// Arrange
		CustomRule customRule = mock(CustomRule.class);
		given(customRule.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		List<CustomRule> customRules = List.of(customRule);
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200));
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);
		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
				RuleType.RANGE_BASED_REGULAR_HOURS, RuleType.RANGE_BASED_BREAK, timesheet);
		// Assert
		assertThat(result).isNotNull().hasSize(3); // custom + 2 virtual
	}

	@Test
	@DisplayName("createUnifiedRuleListWithSystemRules - only regularHoursRule present")
	void testCreateUnifiedRuleListWithSystemRulesOnlyRegularHours() {
		// Arrange
		List<CustomRule> customRules = new ArrayList<>();
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200));
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);
		// Patch createBreakVirtualRule to return null
		try (var mocked = org.mockito.Mockito.mockStatic(UnifiedRuleManager.class,
				org.mockito.Mockito.CALLS_REAL_METHODS)) {
			mocked
				.when(() -> UnifiedRuleManager.createBreakVirtualRule(org.mockito.ArgumentMatchers.any(),
						org.mockito.ArgumentMatchers.any()))
				.thenReturn(null);
			List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
					RuleType.RANGE_BASED_REGULAR_HOURS, RuleType.RANGE_BASED_BREAK, timesheet);
			assertThat(result).isNotNull().hasSize(1); // only regularHoursRule
		}
	}

	@Test
	@DisplayName("createUnifiedRuleListWithSystemRules - only breakRule present")
	void testCreateUnifiedRuleListWithSystemRulesOnlyBreakRule() {
		// Arrange
		List<CustomRule> customRules = new ArrayList<>();
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200));
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);
		// Patch createRegularHoursVirtualRule to return null
		try (var mocked = org.mockito.Mockito.mockStatic(UnifiedRuleManager.class,
				org.mockito.Mockito.CALLS_REAL_METHODS)) {
			mocked
				.when(() -> UnifiedRuleManager.createRegularHoursVirtualRule(org.mockito.ArgumentMatchers.any(),
						org.mockito.ArgumentMatchers.any()))
				.thenReturn(null);
			List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
					RuleType.RANGE_BASED_REGULAR_HOURS, RuleType.RANGE_BASED_BREAK, timesheet);
			assertThat(result).isNotNull().hasSize(1); // only breakRule
		}
	}

	@Test
	@DisplayName("createUnifiedRuleListWithSystemRules - both virtual rules null")
	void testCreateUnifiedRuleListWithSystemRulesBothVirtualNull() {
		// Arrange
		List<CustomRule> customRules = new ArrayList<>();
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY.getId(), 28800, 32400, 61200));
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);
		// Patch both virtual rule creators to return null
		try (var mocked = org.mockito.Mockito.mockStatic(UnifiedRuleManager.class,
				org.mockito.Mockito.CALLS_REAL_METHODS)) {
			mocked
				.when(() -> UnifiedRuleManager.createRegularHoursVirtualRule(org.mockito.ArgumentMatchers.any(),
						org.mockito.ArgumentMatchers.any()))
				.thenReturn(null);
			mocked
				.when(() -> UnifiedRuleManager.createBreakVirtualRule(org.mockito.ArgumentMatchers.any(),
						org.mockito.ArgumentMatchers.any()))
				.thenReturn(null);
			List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
					RuleType.RANGE_BASED_REGULAR_HOURS, RuleType.RANGE_BASED_BREAK, timesheet);
			assertThat(result).isNotNull().isEmpty();
		}
	}

	@Test
	@DisplayName("createUnifiedRuleListWithSystemRules - timesheet with no work days")
	void testCreateUnifiedRuleListWithSystemRulesNoWorkDays() {
		// Arrange
		List<CustomRule> customRules = new ArrayList<>();
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(new ArrayList<>()); // no
																					// work
																					// days

		// Act
		List<IEvaluatableRule> result = UnifiedRuleManager.createUnifiedRuleListWithSystemRules(customRules,
				RuleType.RANGE_BASED_REGULAR_HOURS, RuleType.RANGE_BASED_BREAK, timesheet);

		// Assert - Break rule should be created and applicable on all days, but Regular
		// Hours rule should not be created
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0)).satisfies((rule) -> assertThat(rule.isBreakRule()).isTrue())
			.satisfies((rule) -> assertThat(rule.getWorkDays()).isEmpty())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.MONDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SUNDAY)).isTrue());
	}

	@Test
	@DisplayName("sortRulesByPrecedence - null rules returns empty list")
	void testSortRulesByPrecedenceNullRules() {
		List<IEvaluatableRule> result = UnifiedRuleManager.sortRulesByPrecedence(null);
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("sortRulesByPrecedence - empty rules returns empty list")
	void testSortRulesByPrecedenceEmptyRules() {
		List<IEvaluatableRule> result = UnifiedRuleManager.sortRulesByPrecedence(new ArrayList<>());
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("sortRulesByPrecedence - only rangeBasedRules present")
	void testSortRulesByPrecedenceOnlyRangeBased() {
		IEvaluatableRule rangeRule = mock(IEvaluatableRule.class);
		given(rangeRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(rangeRule.getRuleType()))
				.thenReturn(true);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isDurationBasedRule(rangeRule.getRuleType()))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rangeRule.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.validateRulePrecedenceOrder(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(true);
			List<IEvaluatableRule> result = UnifiedRuleManager.sortRulesByPrecedence(List.of(rangeRule));
			assertThat(result).containsExactly(rangeRule);
		}
	}

	@Test
	@DisplayName("sortRulesByPrecedence - only durationBasedRules present")
	void testSortRulesByPrecedenceOnlyDurationBased() {
		IEvaluatableRule durationRule = mock(IEvaluatableRule.class);
		given(durationRule.getRuleType()).willReturn(RuleType.DURATION_BASED_BREAK);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(durationRule.getRuleType()))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isDurationBasedRule(durationRule.getRuleType()))
				.thenReturn(true);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getDurationBasedPrecedence(durationRule.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.validateRulePrecedenceOrder(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(true);
			List<IEvaluatableRule> result = UnifiedRuleManager.sortRulesByPrecedence(List.of(durationRule));
			assertThat(result).containsExactly(durationRule);
		}
	}

	@Test
	@DisplayName("sortRulesByPrecedence - both rangeBasedRules and durationBasedRules present")
	void testSortRulesByPrecedenceBothTypesPresent() {
		IEvaluatableRule rangeRule = mock(IEvaluatableRule.class);
		IEvaluatableRule durationRule = mock(IEvaluatableRule.class);
		given(rangeRule.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(durationRule.getRuleType()).willReturn(RuleType.DURATION_BASED_BREAK);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(rangeRule.getRuleType()))
				.thenReturn(true);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(durationRule.getRuleType()))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isDurationBasedRule(rangeRule.getRuleType()))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isDurationBasedRule(durationRule.getRuleType()))
				.thenReturn(true);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rangeRule.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getDurationBasedPrecedence(durationRule.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.validateRulePrecedenceOrder(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(true);
			List<IEvaluatableRule> result = UnifiedRuleManager.sortRulesByPrecedence(List.of(rangeRule, durationRule));
			// Only rangeBasedRules should be sorted and returned
			assertThat(result).containsExactly(rangeRule);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - different precedence")
	void testSortRangeBasedRulesDifferentPrecedence() throws Exception {
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule1.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule2.getRuleType()))
				.thenReturn(2);
			List<IEvaluatableRule> rules = List.of(rule2, rule1); // Out of order
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, rules);
			assertThat(sorted.get(0)).isEqualTo(rule1);
			assertThat(sorted.get(1)).isEqualTo(rule2);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - same precedence, both daily overtime rules")
	void testSortRangeBasedRulesSamePrecedenceBothDailyOvertime() throws Exception {
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(true);
		// Use reflection to set thresholds
		CustomRule customRule1 = mock(CustomRule.class);
		CustomRule customRule2 = mock(CustomRule.class);
		given(customRule1.getDailyThreshold()).willReturn(Duration.ofHours(8));
		given(customRule2.getDailyThreshold()).willReturn(Duration.ofHours(10));
		// Use getDailyThreshold via UnifiedRuleManager
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule1.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule2.getRuleType()))
				.thenReturn(1);
			// Patch getDailyThreshold to use our custom rules
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			// Patch getDailyThreshold to use customRule1 and customRule2 for rule1 and
			// rule2
			// But since getDailyThreshold uses instanceof, we need to pass customRule1
			// and customRule2 directly
			List<IEvaluatableRule> rules = List.of(customRule2, customRule1); // Out of
																				// order
			given(customRule1.isDailyOvertimeRule()).willReturn(true);
			given(customRule2.isDailyOvertimeRule()).willReturn(true);
			given(customRule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
			given(customRule2.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(customRule1.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(customRule2.getRuleType()))
				.thenReturn(1);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, rules);
			assertThat(sorted.get(0)).isEqualTo(customRule1); // Lower threshold first
			assertThat(sorted.get(1)).isEqualTo(customRule2);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - same precedence, neither daily overtime rule")
	void testSortRangeBasedRulesSamePrecedenceNeitherDailyOvertime() throws Exception {
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(rule1.isDailyOvertimeRule()).willReturn(false);
		given(rule2.isDailyOvertimeRule()).willReturn(false);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule1.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule2.getRuleType()))
				.thenReturn(1);
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, List.of(rule2, rule1));
			// Order should be preserved since neither is daily overtime
			assertThat(sorted.get(0)).isEqualTo(rule2);
			assertThat(sorted.get(1)).isEqualTo(rule1);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - one daily overtime, one not")
	void testSortRangeBasedRulesOneDailyOvertimeOneNot() throws Exception {
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(rule1.isDailyOvertimeRule()).willReturn(true);
		given(rule2.isDailyOvertimeRule()).willReturn(false);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule1.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule2.getRuleType()))
				.thenReturn(1);
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, List.of(rule2, rule1));
			// Order should be preserved since only one is daily overtime
			assertThat(sorted.get(0)).isEqualTo(rule2);
			assertThat(sorted.get(1)).isEqualTo(rule1);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - a is daily overtime, b is not, same precedence")
	void testSortRangeBasedRulesADailyOvertimeBNot() throws Exception {
		IEvaluatableRule ruleA = mock(IEvaluatableRule.class);
		IEvaluatableRule ruleB = mock(IEvaluatableRule.class);
		given(ruleA.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(ruleB.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(ruleA.isDailyOvertimeRule()).willReturn(true);
		given(ruleB.isDailyOvertimeRule()).willReturn(false);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleA.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleB.getRuleType()))
				.thenReturn(1);
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, List.of(ruleA, ruleB));
			// Order should be preserved since only one is daily overtime
			assertThat(sorted.get(0)).isEqualTo(ruleA);
			assertThat(sorted.get(1)).isEqualTo(ruleB);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - a is not daily overtime, b is, same precedence (false branch left)")
	void testSortRangeBasedRulesFalseBranchLeft() throws Exception {
		IEvaluatableRule ruleA = mock(IEvaluatableRule.class);
		IEvaluatableRule ruleB = mock(IEvaluatableRule.class);
		given(ruleA.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(ruleB.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(ruleA.isDailyOvertimeRule()).willReturn(false);
		given(ruleB.isDailyOvertimeRule()).willReturn(true);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleA.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleB.getRuleType()))
				.thenReturn(1);
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, List.of(ruleA, ruleB));
			// Order should be preserved since only one is daily overtime
			assertThat(sorted.get(0)).isEqualTo(ruleA);
			assertThat(sorted.get(1)).isEqualTo(ruleB);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - a is daily overtime, b is not, different precedence (false branch right)")
	void testSortRangeBasedRulesFalseBranchRight() throws Exception {
		IEvaluatableRule ruleA = mock(IEvaluatableRule.class);
		IEvaluatableRule ruleB = mock(IEvaluatableRule.class);
		given(ruleA.getRuleType()).willReturn(RuleType.RANGE_BASED_DAILY_OVERTIME);
		given(ruleB.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(ruleA.isDailyOvertimeRule()).willReturn(true);
		given(ruleB.isDailyOvertimeRule()).willReturn(false);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleA.getRuleType()))
				.thenReturn(2); // Higher precedence
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleB.getRuleType()))
				.thenReturn(1); // Lower precedence
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, List.of(ruleA, ruleB));
			// When precedence is different, rules should be sorted by precedence (lower
			// precedence first)
			// ruleB has precedence 1 (lower), ruleA has precedence 2 (higher)
			assertThat(sorted.get(0)).isEqualTo(ruleB); // Lower precedence comes first
			assertThat(sorted.get(1)).isEqualTo(ruleA); // Higher precedence comes second
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - different precedence, reverse order")
	void testSortRangeBasedRulesDifferentPrecedenceReverse() throws Exception {
		IEvaluatableRule rule1 = mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = mock(IEvaluatableRule.class);
		given(rule1.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(rule2.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule1.getRuleType()))
				.thenReturn(2);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(rule2.getRuleType()))
				.thenReturn(1);
			List<IEvaluatableRule> rules = List.of(rule1, rule2); // rule1 should now come
																	// after rule2
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, rules);
			assertThat(sorted.get(0)).isEqualTo(rule2);
			assertThat(sorted.get(1)).isEqualTo(rule1);
		}
	}

	@Test
	@DisplayName("sortRangeBasedRules - neither a nor b is daily overtime, same precedence (false branch both)")
	void testSortRangeBasedRulesFalseBranchBoth() throws Exception {
		IEvaluatableRule ruleA = mock(IEvaluatableRule.class);
		IEvaluatableRule ruleB = mock(IEvaluatableRule.class);
		given(ruleA.getRuleType()).willReturn(RuleType.RANGE_BASED_BREAK);
		given(ruleB.getRuleType()).willReturn(RuleType.RANGE_BASED_REGULAR_HOURS);
		given(ruleA.isDailyOvertimeRule()).willReturn(false);
		given(ruleB.isDailyOvertimeRule()).willReturn(false);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleA.getRuleType()))
				.thenReturn(1);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.getRangeBasedPrecedence(ruleB.getRuleType()))
				.thenReturn(1);
			var method = UnifiedRuleManager.class.getDeclaredMethod("sortRangeBasedRules", List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IEvaluatableRule> sorted = (List<IEvaluatableRule>) method.invoke(null, List.of(ruleA, ruleB));
			// Order should be preserved since neither is daily overtime
			assertThat(sorted.get(0)).isEqualTo(ruleA);
			assertThat(sorted.get(1)).isEqualTo(ruleB);
		}
	}

	@Test
	@DisplayName("sortRulesByPrecedence - rule is neither range-based nor duration-based")
	void testSortRulesByPrecedenceRuleNeitherType() {
		IEvaluatableRule unknownRule = mock(IEvaluatableRule.class);
		given(unknownRule.getRuleType()).willReturn(null); // Simulate unknown type
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(null))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isDurationBasedRule(null))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.validateRulePrecedenceOrder(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(true);
			List<IEvaluatableRule> result = UnifiedRuleManager.sortRulesByPrecedence(List.of(unknownRule));
			assertThat(result).isEmpty();
		}
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - timesheetSetting is null")
	void testGetWorkDaysFromTimesheetSettingNull() throws Exception {
		Timesheet timesheet = mock(Timesheet.class);
		given(timesheet.getTimesheetSetting()).willReturn(null);
		var method = UnifiedRuleManager.class.getDeclaredMethod("getWorkDaysFromTimesheet",
				io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay> result = (java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay>) method
			.invoke(null, timesheet);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - templateWorkDay is null")
	void testGetWorkDaysFromTimesheetTemplateWorkDayNull() throws Exception {
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(null);
		var method = UnifiedRuleManager.class.getDeclaredMethod("getWorkDaysFromTimesheet",
				io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay> result = (java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay>) method
			.invoke(null, timesheet);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - templateWorkDay is empty")
	void testGetWorkDaysFromTimesheetTemplateWorkDayEmpty() throws Exception {
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(new java.util.ArrayList<>());
		var method = UnifiedRuleManager.class.getDeclaredMethod("getWorkDaysFromTimesheet",
				io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay> result = (java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay>) method
			.invoke(null, timesheet);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - templateWorkDay is non-empty")
	void testGetWorkDaysFromTimesheetTemplateWorkDayNonEmpty() throws Exception {
		Timesheet timesheet = mock(Timesheet.class);
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		java.util.List<io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay> templateWorkDays = java.util.List
			.of(new io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay(
					io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY.getId(), 28800, 32400,
					61200));
		given(timesheet.getTimesheetSetting()).willReturn(timesheetSetting);
		given(timesheetSetting.getTemplateWorkDay()).willReturn(templateWorkDays);
		var method = UnifiedRuleManager.class.getDeclaredMethod("getWorkDaysFromTimesheet",
				io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay> result = (java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay>) method
			.invoke(null, timesheet);
		assertThat(result).containsExactly(io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay.MONDAY);
	}

	@Test
	@DisplayName("createRegularHoursVirtualRule - returns null if workDays is empty")
	void testCreateRegularHoursVirtualRuleNoWorkDays() {
		var timesheet = mock(io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		var setting = mock(io.recruitcrm.contract_staffing.entity.model.TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(setting);
		given(setting.getTemplateWorkDay()).willReturn(new java.util.ArrayList<>());
		assertThat(UnifiedRuleManager.createRegularHoursVirtualRule(RuleType.RANGE_BASED_REGULAR_HOURS, timesheet))
			.isNull();
	}

	@Test
	@DisplayName("createBreakVirtualRule - returns break rule applicable on all days when workDays is empty")
	void testCreateBreakVirtualRuleNoWorkDays() {
		var timesheet = mock(io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		var setting = mock(io.recruitcrm.contract_staffing.entity.model.TimesheetSetting.class);
		given(timesheet.getTimesheetSetting()).willReturn(setting);
		given(setting.getTemplateWorkDay()).willReturn(new java.util.ArrayList<>());

		VirtualRule result = UnifiedRuleManager.createBreakVirtualRule(RuleType.RANGE_BASED_BREAK, timesheet);

		assertThat(result).isNotNull()
			.satisfies((rule) -> assertThat(rule.getRuleType()).isEqualTo(RuleType.RANGE_BASED_BREAK))
			.satisfies((rule) -> assertThat(rule.getWorkDays()).isEmpty())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.MONDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.TUESDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.WEDNESDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.THURSDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.FRIDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SATURDAY)).isTrue())
			.satisfies((rule) -> assertThat(rule.isApplicableOnDay(WorkDay.SUNDAY)).isTrue());
	}

	@Test
	@DisplayName("createUnifiedRuleList - both null lists")
	void testCreateUnifiedRuleListBothNull() {
		assertThat(UnifiedRuleManager.createUnifiedRuleList((List<CustomRule>) null, (VirtualRule) null)).isEmpty();
	}

	@Test
	@DisplayName("createUnifiedRuleList - both empty lists")
	void testCreateUnifiedRuleListBothEmpty() {
		List<CustomRule> customRules = new ArrayList<>();
		assertThat(UnifiedRuleManager.createUnifiedRuleList(customRules, (VirtualRule) null)).isEmpty();
	}

	@Test
	@DisplayName("createUnifiedRuleListWithSystemRules - all null inputs")
	void testCreateUnifiedRuleListWithSystemRulesAllNull() {
		org.assertj.core.api.Assertions
			.assertThatThrownBy(() -> UnifiedRuleManager.createUnifiedRuleListWithSystemRules(null, null, null, null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("sortRulesByPrecedence - null input")
	void testSortRulesByPrecedenceNullInput() {
		assertThat(UnifiedRuleManager.sortRulesByPrecedence(null)).isEmpty();
	}

	@Test
	@DisplayName("sortRulesByPrecedence - empty input")
	void testSortRulesByPrecedenceEmptyInput() {
		assertThat(UnifiedRuleManager.sortRulesByPrecedence(new java.util.ArrayList<>())).isEmpty();
	}

	@Test
	@DisplayName("sortRulesByPrecedence - unknown rule type")
	void testSortRulesByPrecedenceUnknownType() {
		var rule = mock(IEvaluatableRule.class);
		given(rule.getRuleType()).willReturn(null);
		try (var mocked = org.mockito.Mockito
			.mockStatic(io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig.class)) {
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isRangeBasedRule(null))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.isDurationBasedRule(null))
				.thenReturn(false);
			mocked
				.when(() -> io.recruitcrm.microservice.timesheet.rule_engine.constants.RulePrecedenceConfig
					.validateRulePrecedenceOrder(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(true);
			assertThat(UnifiedRuleManager.sortRulesByPrecedence(java.util.List.of(rule))).isEmpty();
		}
	}

	@Test
	@DisplayName("UnifiedRuleManager private constructor coverage")
	void testPrivateConstructorCoverage() throws Exception {
		var ctor = UnifiedRuleManager.class.getDeclaredConstructor();
		ctor.setAccessible(true);
		Object instance = ctor.newInstance(); // should not throw

		// Assert that the constructor successfully created an instance
		assertThat(instance).isNotNull().isInstanceOf(UnifiedRuleManager.class);

		// Assert that the constructor is private (modifiers check)
		assertThat(ctor.getModifiers() & java.lang.reflect.Modifier.PRIVATE).isNotZero();
	}

	@Test
	@DisplayName("getWorkDaysFromTimesheet - null timesheet throws NPE")
	void testGetWorkDaysFromTimesheetNullTimesheet() throws Exception {
		var method = UnifiedRuleManager.class.getDeclaredMethod("getWorkDaysFromTimesheet",
				io.recruitcrm.contract_staffing.entity.model.Timesheet.class);
		method.setAccessible(true);
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> method.invoke(null, (Object) null))
			.hasCauseInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("sortDurationBasedRules - neither rule is daily overtime (return 0 branch)")
	void testSortDurationBasedRulesNeitherDailyOvertime() throws Exception {
		IEvaluatableRule rule1 = org.mockito.Mockito.mock(IEvaluatableRule.class);
		IEvaluatableRule rule2 = org.mockito.Mockito.mock(IEvaluatableRule.class);
		org.mockito.BDDMockito.given(rule1.getRuleType()).willReturn(RuleType.DURATION_BASED_BREAK);
		org.mockito.BDDMockito.given(rule2.getRuleType()).willReturn(RuleType.DURATION_BASED_BREAK);
		org.mockito.BDDMockito.given(rule1.isDailyOvertimeRule()).willReturn(false);
		org.mockito.BDDMockito.given(rule2.isDailyOvertimeRule()).willReturn(false);
		var method = UnifiedRuleManager.class.getDeclaredMethod("sortDurationBasedRules", java.util.List.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.List<IEvaluatableRule> sorted = (java.util.List<IEvaluatableRule>) method.invoke(null,
				java.util.List.of(rule2, rule1));
		// Order should be preserved since neither is daily overtime
		org.assertj.core.api.Assertions.assertThat(sorted.get(0)).isEqualTo(rule2);
		org.assertj.core.api.Assertions.assertThat(sorted.get(1)).isEqualTo(rule1);
	}

}