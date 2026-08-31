package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Focused tests for {@code BaseRuleEvaluator#evaluateDefaultPayRules} and its helpers
 * ({@code computeFreeRangesForDate}, {@code takeFromStart}). Uses a minimal subclass to
 * expose the protected method.
 */
@ExtendWith(MockitoExtension.class)
class BaseRuleEvaluatorDefaultPayTests {

	@Mock
	private IRuleFactory ruleFactory;

	@Mock
	private Logger logger;

	@Mock
	private IRule mockDefaultPayRule;

	private DefaultPayTestEvaluator evaluator;

	@BeforeEach
	void setUp() {
		this.evaluator = new DefaultPayTestEvaluator(this.ruleFactory, this.logger);
	}

	@Test
	@DisplayName("Flag null - default pay is skipped entirely")
	void testFlagNullSkips() {
		Timesheet timesheet = buildTimesheet(null);
		List<TimeLog> weeklyLogs = List
			.of(buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		this.evaluator.evaluateDefaultPayRules(weeklyLogs, timesheet, result);

		assertThat(result.getRuleEvaluationResults()).isEmpty();
		verify(this.ruleFactory, never()).createRule(any(), any());
	}

	@Test
	@DisplayName("Flag 0 - default pay is skipped entirely")
	void testFlagZeroSkips() {
		Timesheet timesheet = buildTimesheet(0);
		List<TimeLog> weeklyLogs = List
			.of(buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		this.evaluator.evaluateDefaultPayRules(weeklyLogs, timesheet, result);

		assertThat(result.getRuleEvaluationResults()).isEmpty();
		verify(this.ruleFactory, never()).createRule(any(), any());
	}

	@Test
	@DisplayName("Flag 1, no unallocated time - no default pay entries created")
	void testNoUnallocatedNoEntries() {
		Timesheet timesheet = buildTimesheet(1);
		TimeLog log = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(17, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		// All time occupied by a prior rule result
		RuleEvaluationResult prior = RuleEvaluationResult.builder()
			.timeRange(rangeSet(LocalTime.of(9, 0), LocalTime.of(17, 0)))
			.build();
		result.addRuleEvaluationResult(log, prior);

		this.evaluator.evaluateDefaultPayRules(List.of(log), timesheet, result);

		// Only the prior rule result — nothing added by default pay
		assertThat(result.getRuleEvaluationResults().get(log)).hasSize(1);
		verify(this.ruleFactory, never()).createRule(any(), any());
	}

	@Test
	@DisplayName("Flag 1, unallocated exists, no WOT - entire unallocated becomes default pay")
	void testUnallocatedNoWotCoversAll() {
		Timesheet timesheet = buildTimesheet(1);
		TimeLog log = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(12, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		// No prior rule results → entire 3-hour range is free

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(log), timesheet, result);

		List<RuleEvaluationResult> attached = result.getRuleEvaluationResults().get(log);
		assertThat(attached).hasSize(1);
		RangeSet<LocalTime> rs = attached.get(0).getTimeRange();
		assertThat(rs.asRanges()).containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0)));
	}

	@Test
	@DisplayName("Flag 1, uniform unallocated, WOT consumes tail of latest day - earliest days get full default pay")
	void testWotConsumesLatestDayTail() {
		Timesheet timesheet = buildTimesheet(1);
		// Mon 09-11 (2h) and Tue 09-11 (2h) — both fully free. WOT = 1h.
		TimeLog mon = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(11, 0));
		TimeLog tue = buildLog(2, LocalDate.of(2026, 4, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setWeeklyOvertimeRuleEvaluationResult(
				RuleEvaluationResult.builder().weeklyOvertimeHours(Duration.ofHours(1)).build());

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(mon, tue), timesheet, result);

		// Tue: 1h default pay (head 09-10); WOT took tail 10-11
		assertThat(result.getRuleEvaluationResults().get(tue)).hasSize(1);
		assertThat(result.getRuleEvaluationResults().get(tue).get(0).getTimeRange().asRanges())
			.containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));

		// Mon: full 2h default pay (WOT already exhausted)
		assertThat(result.getRuleEvaluationResults().get(mon)).hasSize(1);
		assertThat(result.getRuleEvaluationResults().get(mon).get(0).getTimeRange().asRanges())
			.containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(11, 0)));

		verify(this.ruleFactory, times(2)).createRule(RuleType.RANGE_BASED_DEFAULT_PAY, this.logger);
	}

	@Test
	@DisplayName("Flag 1, WOT consumes exactly the latest day - that day has no default pay entry")
	void testWotConsumesLatestDayFully() {
		Timesheet timesheet = buildTimesheet(1);
		TimeLog mon = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(11, 0));
		TimeLog tue = buildLog(2, LocalDate.of(2026, 4, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setWeeklyOvertimeRuleEvaluationResult(
				RuleEvaluationResult.builder().weeklyOvertimeHours(Duration.ofHours(2)).build());

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(mon, tue), timesheet, result);

		// Tue fully consumed by WOT; Mon fully default pay
		assertThat(result.getRuleEvaluationResults().get(tue)).isNull();
		assertThat(result.getRuleEvaluationResults().get(mon)).hasSize(1);
		assertThat(result.getRuleEvaluationResults().get(mon).get(0).getTimeRange().asRanges())
			.containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(11, 0)));
	}

	@Test
	@DisplayName("Flag 1, WOT >= total unallocated - no default pay on any day")
	void testWotConsumesEverything() {
		Timesheet timesheet = buildTimesheet(1);
		TimeLog mon = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(11, 0));
		TimeLog tue = buildLog(2, LocalDate.of(2026, 4, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setWeeklyOvertimeRuleEvaluationResult(
				RuleEvaluationResult.builder().weeklyOvertimeHours(Duration.ofHours(10)).build());

		this.evaluator.evaluateDefaultPayRules(List.of(mon, tue), timesheet, result);

		assertThat(result.getRuleEvaluationResults()).isEmpty();
		verify(this.ruleFactory, never()).createRule(any(), any());
	}

	@Test
	@DisplayName("Flag 1, null WOT result - treats WOT as zero hours")
	void testNullWotResultTreatedAsZero() {
		Timesheet timesheet = buildTimesheet(1);
		TimeLog log = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(10, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		assertThat(result.getWeeklyOvertimeRuleEvaluationResult()).isNull();

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(log), timesheet, result);

		assertThat(result.getRuleEvaluationResults().get(log)).hasSize(1);
	}

	@Test
	@DisplayName("Flag 1, WOT result with null weekly hours - treats as zero")
	void testWotResultWithNullWeeklyHoursTreatedAsZero() {
		Timesheet timesheet = buildTimesheet(1);
		TimeLog log = buildLog(1, LocalDate.of(2026, 4, 13), LocalTime.of(9, 0), LocalTime.of(10, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setWeeklyOvertimeRuleEvaluationResult(RuleEvaluationResult.builder().weeklyOvertimeHours(null).build());

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(log), timesheet, result);

		assertThat(result.getRuleEvaluationResults().get(log)).hasSize(1);
	}

	@Test
	@DisplayName("Multi-interval day - default pay attached to first interval, ranges span both intervals")
	void testMultiIntervalDay() {
		Timesheet timesheet = buildTimesheet(1);
		LocalDate date = LocalDate.of(2026, 4, 13);
		// Two work intervals: morning 09-12, afternoon 13-17 — 7 total free hours
		TimeLog morning = buildLog(1, date, LocalTime.of(9, 0), LocalTime.of(12, 0));
		TimeLog afternoon = buildLog(2, date, LocalTime.of(13, 0), LocalTime.of(17, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(morning, afternoon), timesheet, result);

		// Attached to the first interval of the date (morning)
		assertThat(result.getRuleEvaluationResults().get(morning)).hasSize(1);
		assertThat(result.getRuleEvaluationResults().get(afternoon)).isNull();

		RangeSet<LocalTime> ranges = result.getRuleEvaluationResults().get(morning).get(0).getTimeRange();
		assertThat(ranges.asRanges()).containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0)),
				Range.closedOpen(LocalTime.of(13, 0), LocalTime.of(17, 0)));
	}

	@Test
	@DisplayName("Prior rule results reduce free ranges - default pay only covers gaps")
	void testPriorRulesOccupyRangesCorrectly() {
		Timesheet timesheet = buildTimesheet(1);
		LocalDate date = LocalDate.of(2026, 4, 13);
		TimeLog log = buildLog(1, date, LocalTime.of(9, 0), LocalTime.of(17, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		// Regular Hours claimed 09-11, break claimed 12-13, remainder (11-12, 13-17) is
		// free
		result.addRuleEvaluationResult(log,
				RuleEvaluationResult.builder().timeRange(rangeSet(LocalTime.of(9, 0), LocalTime.of(11, 0))).build());
		result.addRuleEvaluationResult(log,
				RuleEvaluationResult.builder().timeRange(rangeSet(LocalTime.of(12, 0), LocalTime.of(13, 0))).build());

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(log), timesheet, result);

		List<RuleEvaluationResult> attached = result.getRuleEvaluationResults().get(log);
		assertThat(attached).hasSize(3); // 2 prior + 1 default pay
		RuleEvaluationResult defaultPay = attached.get(2);
		assertThat(defaultPay.getTimeRange().asRanges()).containsExactly(
				Range.closedOpen(LocalTime.of(11, 0), LocalTime.of(12, 0)),
				Range.closedOpen(LocalTime.of(13, 0), LocalTime.of(17, 0)));
	}

	@Test
	@DisplayName("WOT takes partial slice from latest-day tail - splits the range")
	void testWotTakesPartialRangeSplit() {
		Timesheet timesheet = buildTimesheet(1);
		LocalDate mon = LocalDate.of(2026, 4, 13);
		LocalDate tue = LocalDate.of(2026, 4, 14);
		// Mon free 09-10 (1h). Tue free 09-12 (3h). WOT=1h → takes last hour of Tue
		// (11-12).
		// Default pay: Mon 09-10 full, Tue 09-11 (head after split)
		TimeLog monLog = buildLog(1, mon, LocalTime.of(9, 0), LocalTime.of(10, 0));
		TimeLog tueLog = buildLog(2, tue, LocalTime.of(9, 0), LocalTime.of(12, 0));
		RuleEvaluatorResult result = new RuleEvaluatorResult();
		result.setWeeklyOvertimeRuleEvaluationResult(
				RuleEvaluationResult.builder().weeklyOvertimeHours(Duration.ofHours(1)).build());

		stubRuleFactoryEcho();

		this.evaluator.evaluateDefaultPayRules(List.of(monLog, tueLog), timesheet, result);

		assertThat(result.getRuleEvaluationResults().get(tueLog).get(0).getTimeRange().asRanges())
			.containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(11, 0)));
		assertThat(result.getRuleEvaluationResults().get(monLog).get(0).getTimeRange().asRanges())
			.containsExactly(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
	}

	@Test
	@DisplayName("Empty weekly time logs list - no-op")
	void testEmptyWeeklyLogs() {
		Timesheet timesheet = buildTimesheet(1);
		RuleEvaluatorResult result = new RuleEvaluatorResult();

		this.evaluator.evaluateDefaultPayRules(List.of(), timesheet, result);

		assertThat(result.getRuleEvaluationResults()).isEmpty();
		verify(this.ruleFactory, never()).createRule(any(), any());
	}

	// Helpers

	private Timesheet buildTimesheet(Integer unplannedPayFlag) {
		TimesheetSetting setting = new TimesheetSetting();
		setting.setIsUnplannedHoursPayEnabled(unplannedPayFlag);
		setting.setPayRate(20.0f);
		setting.setBillRate(30.0f);
		Timesheet ts = new Timesheet();
		ts.setId(100);
		ts.setTimesheetSetting(setting);
		return ts;
	}

	private TimeLog buildLog(int id, LocalDate date, LocalTime start, LocalTime end) {
		TimeLog tl = new TimeLog();
		tl.setId(id);
		tl.setDate(date);
		tl.setWorkStartTime(start);
		tl.setWorkEndTime(end);
		tl.setNormalizedWorkStartTime(start);
		tl.setNormalizedWorkEndTime(end);
		return tl;
	}

	private RangeSet<LocalTime> rangeSet(LocalTime start, LocalTime end) {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(start, end));
		return rs;
	}

	/**
	 * Echoes the context's timeRangesToEvaluate back as the result's timeRange so the
	 * test can inspect what the evaluator passed in.
	 */
	private void stubRuleFactoryEcho() {
		given(this.ruleFactory.createRule(any(), any())).willReturn(this.mockDefaultPayRule);
		given(this.mockDefaultPayRule.evaluate(any())).willAnswer((inv) -> {
			RuleEvaluationContext ctx = inv.getArgument(0);
			return RuleEvaluationResult.builder()
				.timeRange(ctx.getTimeRangesToEvaluate())
				.ruleType(RuleType.RANGE_BASED_DEFAULT_PAY)
				.payAmount(BigDecimal.ZERO)
				.billAmount(BigDecimal.ZERO)
				.build();
		});
	}

	/**
	 * Minimal BaseRuleEvaluator subclass that exposes
	 * {@link BaseRuleEvaluator#evaluateDefaultPayRules} for direct testing.
	 */
	private static class DefaultPayTestEvaluator extends BaseRuleEvaluator {

		DefaultPayTestEvaluator(IRuleFactory ruleFactory, Logger logger) {
			super(ruleFactory, logger);
		}

		@Override
		protected boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet) {
			return false;
		}

		@Override
		protected RuleType getRegularHoursRuleType() {
			return RuleType.RANGE_BASED_REGULAR_HOURS;
		}

		@Override
		protected RuleType getBreakRuleType() {
			return RuleType.RANGE_BASED_BREAK;
		}

		@Override
		protected RuleType getDefaultPayRuleType() {
			return RuleType.RANGE_BASED_DEFAULT_PAY;
		}

		@Override
		public void evaluateDefaultPayRules(List<TimeLog> weeklyTimeLog, Timesheet timesheet,
				RuleEvaluatorResult result) {
			super.evaluateDefaultPayRules(weeklyTimeLog, timesheet, result);
		}

	}

}
