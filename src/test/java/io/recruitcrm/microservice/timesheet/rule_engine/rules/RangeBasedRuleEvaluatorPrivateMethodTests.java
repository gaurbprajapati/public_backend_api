package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("RangeBasedRuleEvaluator Private Method Tests")
class RangeBasedRuleEvaluatorPrivateMethodTests {

	@Mock
	private IRuleFactory ruleFactory;

	@Mock
	private Logger logger;

	@Mock
	private ITimeLogIntervalRepository timeLogIntervalRepository;

	private RangeBasedRuleEvaluator evaluator;

	@BeforeEach
	void setUp() {
		this.evaluator = new RangeBasedRuleEvaluator(this.ruleFactory, this.logger, this.timeLogIntervalRepository);
	}

	@SuppressWarnings("unchecked")
	private List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval> invokeParseBreakIntervalsFromJson(
			String json, Integer timeLogId) throws Exception {
		Method method = RangeBasedRuleEvaluator.class.getDeclaredMethod("parseBreakIntervalsFromJson", String.class,
				Integer.class);
		method.setAccessible(true);
		return (List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLogBreakInterval>) method
			.invoke(this.evaluator, json, timeLogId);
	}

	private Duration invokeCalculateTotalBreakDuration(List<TimeLogBreakInterval> breakIntervals) throws Exception {
		Method method = RangeBasedRuleEvaluator.class.getDeclaredMethod("calculateTotalBreakDuration", List.class);
		method.setAccessible(true);
		return (Duration) method.invoke(this.evaluator, breakIntervals);
	}

	private io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog invokeCreateTimeLogFromInterval(
			TimeLog timeLog, TimeLogIntervalDto interval) throws Exception {
		Method method = RangeBasedRuleEvaluator.class.getDeclaredMethod("createTimeLogFromInterval", TimeLog.class,
				TimeLogIntervalDto.class);
		method.setAccessible(true);
		return (io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog) method.invoke(this.evaluator, timeLog,
				interval);
	}

	@SuppressWarnings("unchecked")
	private List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> invokeFetchTimeLogsFromIntervals(
			List<TimeLog> timeLogs) throws Exception {
		Method method = RangeBasedRuleEvaluator.class.getDeclaredMethod("fetchTimeLogsFromIntervals", List.class);
		method.setAccessible(true);
		return (List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog>) method.invoke(this.evaluator,
				timeLogs);
	}

	@Nested
	@DisplayName("parseBreakIntervalsFromJson Tests")
	class ParseBreakIntervalsFromJsonTests {

		static Stream<String> invalidJsonInputs() {
			return Stream.of(null, "   ", "", "not valid json", "{\"key\": \"value\"}");
		}

		static Stream<String> invalidBreakIntervalJsonInputs() {
			return Stream.of("[{\"id\": 1, \"breakStartTime\": null, \"breakEndTime\": 39600}]",
					"[{\"id\": 1, \"breakStartTime\": 36000, \"breakEndTime\": null}]",
					"[{\"id\": 1, \"breakStartTime\": 39600, \"breakEndTime\": 36000}]",
					"[{\"id\": 1, \"breakStartTime\": 36000, \"breakEndTime\": 36000}]",
					"[{\"id\": 1, \"breakEndTime\": 39600}]", "[{\"id\": 1, \"breakStartTime\": 36000}]");
		}

		static Stream<String> invalidJsonOrBreakIntervalInputs() {
			return Stream.concat(invalidJsonInputs(), invalidBreakIntervalJsonInputs());
		}

		@ParameterizedTest
		@MethodSource("invalidJsonOrBreakIntervalInputs")
		@DisplayName("Returns empty list for invalid or empty JSON input or invalid break interval values")
		void testParseBreakIntervalsInvalidInputs(String json) throws Exception {
			List<TimeLogBreakInterval> result = invokeParseBreakIntervalsFromJson(json, Integer.valueOf(1));

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Parses valid JSON with single break interval")
		void testParseBreakIntervalsValidSingleBreak() throws Exception {
			String json = "[{\"id\": 1, \"breakStartTime\": 36000, \"breakEndTime\": 39600}]";

			List<TimeLogBreakInterval> result = invokeParseBreakIntervalsFromJson(json, Integer.valueOf(100));

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getId()).isEqualTo(Integer.valueOf(1));
			assertThat(result.get(0).getTimeLogId()).isEqualTo(Integer.valueOf(100));
			assertThat(result.get(0).getBreakStartTime()).hasHour(10);
			assertThat(result.get(0).getBreakEndTime()).hasHour(11);
		}

		@Test
		@DisplayName("Parses valid JSON with multiple break intervals")
		void testParseBreakIntervalsMultipleBreaks() throws Exception {
			String json = "[{\"id\": 1, \"breakStartTime\": 36000, \"breakEndTime\": 39600},"
					+ "{\"id\": 2, \"breakStartTime\": 50400, \"breakEndTime\": 54000}]";

			List<TimeLogBreakInterval> result = invokeParseBreakIntervalsFromJson(json, Integer.valueOf(1));

			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Handles JSON with missing id field")
		void testParseBreakIntervalsMissingId() throws Exception {
			String json = "[{\"breakStartTime\": 36000, \"breakEndTime\": 39600}]";

			List<TimeLogBreakInterval> result = invokeParseBreakIntervalsFromJson(json, Integer.valueOf(1));

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getId()).isNull();
		}

	}

	@Nested
	@DisplayName("calculateTotalBreakDuration Tests")
	class CalculateTotalBreakDurationTests {

		@Test
		@DisplayName("Returns zero for null list")
		void testCalculateTotalBreakDurationNullList() throws Exception {
			Duration result = invokeCalculateTotalBreakDuration(null);

			assertThat(result).isEqualTo(Duration.ZERO);
		}

		@Test
		@DisplayName("Returns zero for empty list")
		void testCalculateTotalBreakDurationEmptyList() throws Exception {
			Duration result = invokeCalculateTotalBreakDuration(new ArrayList<>());

			assertThat(result).isEqualTo(Duration.ZERO);
		}

		@Test
		@DisplayName("Calculates total for single break interval")
		void testCalculateTotalBreakDurationSingleInterval() throws Exception {
			TimeLogBreakInterval breakInterval = new TimeLogBreakInterval();
			breakInterval.setBreakStartTime(java.time.LocalTime.of(10, 0));
			breakInterval.setBreakEndTime(java.time.LocalTime.of(11, 0));

			Duration result = invokeCalculateTotalBreakDuration(List.of(breakInterval));

			assertThat(result).isEqualTo(Duration.ofHours(1));
		}

		@Test
		@DisplayName("Calculates total for multiple break intervals")
		void testCalculateTotalBreakDurationMultipleIntervals() throws Exception {
			TimeLogBreakInterval break1 = new TimeLogBreakInterval();
			break1.setBreakStartTime(java.time.LocalTime.of(10, 0));
			break1.setBreakEndTime(java.time.LocalTime.of(10, 30));

			TimeLogBreakInterval break2 = new TimeLogBreakInterval();
			break2.setBreakStartTime(java.time.LocalTime.of(14, 0));
			break2.setBreakEndTime(java.time.LocalTime.of(14, 30));

			Duration result = invokeCalculateTotalBreakDuration(List.of(break1, break2));

			assertThat(result).isEqualTo(Duration.ofHours(1));
		}

		@Test
		@DisplayName("Skips break interval with null start time")
		void testCalculateTotalBreakDurationNullStartTime() throws Exception {
			TimeLogBreakInterval breakInterval = new TimeLogBreakInterval();
			breakInterval.setBreakStartTime(null);
			breakInterval.setBreakEndTime(java.time.LocalTime.of(11, 0));

			Duration result = invokeCalculateTotalBreakDuration(List.of(breakInterval));

			assertThat(result).isEqualTo(Duration.ZERO);
		}

		@Test
		@DisplayName("Skips break interval with null end time")
		void testCalculateTotalBreakDurationNullEndTime() throws Exception {
			TimeLogBreakInterval breakInterval = new TimeLogBreakInterval();
			breakInterval.setBreakStartTime(java.time.LocalTime.of(10, 0));
			breakInterval.setBreakEndTime(null);

			Duration result = invokeCalculateTotalBreakDuration(List.of(breakInterval));

			assertThat(result).isEqualTo(Duration.ZERO);
		}

		@Test
		@DisplayName("Skips negative duration break interval")
		void testCalculateTotalBreakDurationNegativeDuration() throws Exception {
			TimeLogBreakInterval breakInterval = new TimeLogBreakInterval();
			breakInterval.setBreakStartTime(java.time.LocalTime.of(11, 0));
			breakInterval.setBreakEndTime(java.time.LocalTime.of(10, 0));

			Duration result = invokeCalculateTotalBreakDuration(List.of(breakInterval));

			assertThat(result).isEqualTo(Duration.ZERO);
		}

	}

	@Nested
	@DisplayName("createTimeLogFromInterval Tests")
	class CreateTimeLogFromIntervalTests {

		@Test
		@DisplayName("Creates valid time log from interval with start and end time")
		void testCreateTimeLogFromIntervalValid() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			TimeLogIntervalDto interval = TimeLogIntervalDto.builder()
				.workStartTime(Integer.valueOf(32400))
				.workEndTime(Integer.valueOf(43200))
				.breakInterval(null)
				.build();

			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog result = invokeCreateTimeLogFromInterval(
					timeLog, interval);

			assertThat(result).isNotNull();
			assertThat(result.getWorkStartTime()).hasHour(9);
			assertThat(result.getWorkEndTime()).hasHour(12);
		}

		@Test
		@DisplayName("Creates time log with break intervals from JSON")
		void testCreateTimeLogFromIntervalWithBreaks() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			String breakJson = "[{\"id\": 1, \"breakStartTime\": 36000, \"breakEndTime\": 37800}]";
			TimeLogIntervalDto interval = TimeLogIntervalDto.builder()
				.workStartTime(Integer.valueOf(32400))
				.workEndTime(Integer.valueOf(43200))
				.breakInterval(breakJson)
				.build();

			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog result = invokeCreateTimeLogFromInterval(
					timeLog, interval);

			assertThat(result).isNotNull();
			assertThat(result.getBreakIntervals()).isNotNull().hasSize(1);
			assertThat(result.getBreakTime()).isEqualTo(Duration.ofMinutes(30));
		}

		@Test
		@DisplayName("Returns null for interval with zero duration")
		void testCreateTimeLogFromIntervalZeroDuration() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			TimeLogIntervalDto interval = TimeLogIntervalDto.builder()
				.workStartTime(null)
				.workEndTime(null)
				.breakInterval(null)
				.build();

			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog result = invokeCreateTimeLogFromInterval(
					timeLog, interval);

			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Sets break time to zero when no break intervals")
		void testCreateTimeLogFromIntervalNoBreakIntervals() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			TimeLogIntervalDto interval = TimeLogIntervalDto.builder()
				.workStartTime(Integer.valueOf(32400))
				.workEndTime(Integer.valueOf(43200))
				.breakInterval("")
				.build();

			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog result = invokeCreateTimeLogFromInterval(
					timeLog, interval);

			assertThat(result).isNotNull();
			assertThat(result.getBreakTime()).isEqualTo(Duration.ZERO);
		}

	}

	@Nested
	@DisplayName("fetchTimeLogsFromIntervals Tests")
	class FetchTimeLogsFromIntervalsTests {

		@Test
		@DisplayName("Expands time logs with intervals")
		void testFetchTimeLogsWithIntervals() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			TimeLogIntervalDto interval1 = TimeLogIntervalDto.builder()
				.workStartTime(Integer.valueOf(32400))
				.workEndTime(Integer.valueOf(43200))
				.build();
			TimeLogIntervalDto interval2 = TimeLogIntervalDto.builder()
				.workStartTime(Integer.valueOf(46800))
				.workEndTime(Integer.valueOf(61200))
				.build();

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			intervalsMap.put(Integer.valueOf(1), List.of(interval1, interval2));
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = invokeFetchTimeLogsFromIntervals(
					List.of(timeLog));

			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Falls back to original time log when no intervals")
		void testFetchTimeLogsFallbackNoIntervals() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = invokeFetchTimeLogsFromIntervals(
					List.of(timeLog));

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Skips original time log with zero duration when no intervals")
		void testFetchTimeLogsSkipsZeroDurationOriginal() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(null);
			given(timeLog.getWorkEndTime()).willReturn(null);

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = invokeFetchTimeLogsFromIntervals(
					List.of(timeLog));

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Skips invalid intervals with zero duration")
		void testFetchTimeLogsSkipsInvalidIntervals() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			TimeLogIntervalDto validInterval = TimeLogIntervalDto.builder()
				.workStartTime(Integer.valueOf(32400))
				.workEndTime(Integer.valueOf(43200))
				.build();
			TimeLogIntervalDto invalidInterval = TimeLogIntervalDto.builder()
				.workStartTime(null)
				.workEndTime(null)
				.build();

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			intervalsMap.put(Integer.valueOf(1), List.of(validInterval, invalidInterval));
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = invokeFetchTimeLogsFromIntervals(
					List.of(timeLog));

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Falls back to original for null interval list")
		void testFetchTimeLogsFallbackNullIntervalList() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			intervalsMap.put(Integer.valueOf(1), null);
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = invokeFetchTimeLogsFromIntervals(
					List.of(timeLog));

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Falls back to original for empty interval list")
		void testFetchTimeLogsFallbackEmptyIntervalList() throws Exception {
			TimeLog timeLog = mock(TimeLog.class);
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			intervalsMap.put(Integer.valueOf(1), Collections.emptyList());
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> result = invokeFetchTimeLogsFromIntervals(
					List.of(timeLog));

			assertThat(result).hasSize(1);
		}

	}

	@Nested
	@DisplayName("evaluateRules workLogType routing Tests")
	class EvaluateRulesRoutingTests {

		@Test
		@DisplayName("Routes to interval evaluation for workLogType 2")
		void testEvaluateRulesWorkLogTypeTwo() {
			Timesheet timesheet = mock(Timesheet.class);
			TimesheetSetting setting = mock(TimesheetSetting.class);
			TimeLog timeLog = mock(TimeLog.class);

			given(timesheet.getTimesheetSetting()).willReturn(setting);
			given(timesheet.getTimeLogs()).willReturn(List.of(timeLog));
			given(timesheet.getId()).willReturn(Integer.valueOf(1));
			given(setting.getWorkLogType()).willReturn(Integer.valueOf(2));
			given(setting.getTimesheetStartDay()).willReturn(Integer.valueOf(1));
			given(setting.getCustomRule()).willReturn(new ArrayList<>());
			given(setting.getTemplateWorkDay()).willReturn(new ArrayList<>());
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));

			Map<Integer, List<TimeLogIntervalDto>> intervalsMap = new HashMap<>();
			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.timeLogIntervalRepository
				.findIntervalsByTimeLogIds(anyList())).willReturn(intervalsMap);

			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.ruleFactory.createTimeRangeResolver(
					io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType.RANGE_BASED_BREAK))
				.willReturn((ctx) -> com.google.common.collect.TreeRangeSet.create());

			WeeklyRuleEvaluatorResult result = RangeBasedRuleEvaluatorPrivateMethodTests.this.evaluator
				.evaluateRules(timesheet);

			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Routes to super.evaluateRules for non-workLogType 2")
		void testEvaluateRulesWorkLogTypeOne() {
			Timesheet timesheet = mock(Timesheet.class);
			TimesheetSetting setting = mock(TimesheetSetting.class);
			TimeLog timeLog = mock(TimeLog.class);

			given(timesheet.getTimesheetSetting()).willReturn(setting);
			given(timesheet.getTimeLogs()).willReturn(List.of(timeLog));
			given(setting.getWorkLogType()).willReturn(Integer.valueOf(1));
			given(setting.getTimesheetStartDay()).willReturn(Integer.valueOf(1));
			given(setting.getCustomRule()).willReturn(new ArrayList<>());
			given(setting.getTemplateWorkDay()).willReturn(new ArrayList<>());
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));
			given(timeLog.getTimesheet()).willReturn(timesheet);

			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.ruleFactory.createTimeRangeResolver(
					io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType.RANGE_BASED_BREAK))
				.willReturn((ctx) -> com.google.common.collect.TreeRangeSet.create());

			WeeklyRuleEvaluatorResult result = RangeBasedRuleEvaluatorPrivateMethodTests.this.evaluator
				.evaluateRules(timesheet);

			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Routes to super.evaluateRules for null workLogType")
		void testEvaluateRulesWorkLogTypeNull() {
			Timesheet timesheet = mock(Timesheet.class);
			TimesheetSetting setting = mock(TimesheetSetting.class);
			TimeLog timeLog = mock(TimeLog.class);

			given(timesheet.getTimesheetSetting()).willReturn(setting);
			given(timesheet.getTimeLogs()).willReturn(List.of(timeLog));
			given(setting.getWorkLogType()).willReturn(null);
			given(setting.getTimesheetStartDay()).willReturn(Integer.valueOf(1));
			given(setting.getCustomRule()).willReturn(new ArrayList<>());
			given(setting.getTemplateWorkDay()).willReturn(new ArrayList<>());
			given(timeLog.getId()).willReturn(Integer.valueOf(1));
			given(timeLog.getDate()).willReturn(Integer.valueOf(1703116800));
			given(timeLog.getDayTypeId()).willReturn(Integer.valueOf(1));
			given(timeLog.getWorkTime()).willReturn(null);
			given(timeLog.getWorkStartTime()).willReturn(Integer.valueOf(32400));
			given(timeLog.getWorkEndTime()).willReturn(Integer.valueOf(61200));
			given(timeLog.getTimesheet()).willReturn(timesheet);

			given(RangeBasedRuleEvaluatorPrivateMethodTests.this.ruleFactory.createTimeRangeResolver(
					io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType.RANGE_BASED_BREAK))
				.willReturn((ctx) -> com.google.common.collect.TreeRangeSet.create());

			WeeklyRuleEvaluatorResult result = RangeBasedRuleEvaluatorPrivateMethodTests.this.evaluator
				.evaluateRules(timesheet);

			assertThat(result).isNotNull();
		}

	}

}
