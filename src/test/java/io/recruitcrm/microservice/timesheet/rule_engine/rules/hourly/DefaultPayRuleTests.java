package io.recruitcrm.microservice.timesheet.rule_engine.rules.hourly;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
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
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultPayRuleTests {

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private Logger logger;

	@Mock
	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto;

	private DefaultPayRule rule;

	@BeforeEach
	void setUp() {
		this.rule = new DefaultPayRule(this.logger);
	}

	@Test
	@DisplayName("Get name - returns the duration-based label")
	void testGetName() {
		assertThat(this.rule.getName()).isEqualTo("Duration-Based Default Pay Rule");
	}

	@Test
	@DisplayName("Get default rule type - returns DURATION_BASED_DEFAULT_PAY")
	void testGetDefaultRuleType() {
		assertThat(this.rule.getDefaultRuleType()).isEqualTo(RuleType.DURATION_BASED_DEFAULT_PAY);
	}

	@Test
	@DisplayName("Evaluate - non-empty range produces 1x pay and bill at base rate")
	void testEvaluateNonEmptyRange() {
		given(this.timesheetSetting.getPayRate()).willReturn(40.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(60.0f);
		given(this.timesheet.getId()).willReturn(11);

		RangeSet<LocalTime> rangeSet = TreeRangeSet.create();
		rangeSet.add(Range.closedOpen(LocalTime.MIDNIGHT, LocalTime.of(2, 0)));

		TimeLog timeLog = new TimeLog();
		timeLog.setDate(LocalDate.of(2026, 4, 20));

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.timesheetSettingDto(this.timesheetSettingDto)
			.currentTimeLogBeingEvaluated(timeLog)
			.timeRangesToEvaluate(rangeSet)
			.currentRuleIndex(-1)
			.build();

		RuleEvaluationResult result = this.rule.evaluate(context);

		assertThat(result).isNotNull();
		assertThat(result.getRuleType()).isEqualTo(RuleType.DURATION_BASED_DEFAULT_PAY);
		assertThat(result.getRuleName()).isEqualTo("Duration-Based Default Pay Rule");
		assertThat(result.getTimeRange()).isEqualTo(rangeSet);
		assertThat(result.isVirtualRule()).isTrue();
		// 2 hours × $40 = $80 pay; × $60 = $120 bill
		assertThat(result.getPayAmount()).isEqualByComparingTo(new BigDecimal("80"));
		assertThat(result.getBillAmount()).isEqualByComparingTo(new BigDecimal("120"));
	}

	@Test
	@DisplayName("Evaluate - empty range produces zero amounts")
	void testEvaluateEmptyRange() {
		given(this.timesheetSetting.getPayRate()).willReturn(40.0f);
		given(this.timesheetSetting.getBillRate()).willReturn(60.0f);
		given(this.timesheet.getId()).willReturn(2);

		RangeSet<LocalTime> empty = TreeRangeSet.create();

		TimeLog timeLog = new TimeLog();
		timeLog.setDate(LocalDate.of(2026, 4, 20));

		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(this.timesheet)
			.timesheetSetting(this.timesheetSetting)
			.timesheetSettingDto(this.timesheetSettingDto)
			.currentTimeLogBeingEvaluated(timeLog)
			.timeRangesToEvaluate(empty)
			.currentRuleIndex(-1)
			.build();

		RuleEvaluationResult result = this.rule.evaluate(context);

		assertThat(result).isNotNull();
		assertThat(result.getPayAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Evaluate - null context throws IllegalArgumentException")
	void testEvaluateNullContextThrows() {
		assertThatThrownBy(() -> this.rule.evaluate(null)).isInstanceOf(IllegalArgumentException.class);
	}

}
