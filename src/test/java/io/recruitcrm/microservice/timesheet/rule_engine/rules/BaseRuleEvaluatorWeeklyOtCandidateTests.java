package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleFactory;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.IEvaluatableRule;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseRuleEvaluator Weekly OT Candidate Tests")
class BaseRuleEvaluatorWeeklyOtCandidateTests {

	@Mock
	private IRuleFactory ruleFactory;

	@Mock
	private Logger logger;

	private RangeBasedRuleEvaluator evaluator;

	@BeforeEach
	void setUp() {
		this.evaluator = new RangeBasedRuleEvaluator(this.ruleFactory, this.logger, null);
	}

	private TimeLog createTimeLog(LocalTime start, LocalTime end) {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(start);
		timeLog.setWorkEndTime(end);
		return timeLog;
	}

	@SuppressWarnings("unchecked")
	private RangeSet<LocalTime> invokeBuildActualWorkRanges(List<TimeLog> sameDayLogs) throws Exception {
		Method method = BaseRuleEvaluator.class.getDeclaredMethod("buildActualWorkRanges", List.class);
		method.setAccessible(true);
		return (RangeSet<LocalTime>) method.invoke(this.evaluator, sameDayLogs);
	}

	@SuppressWarnings("unchecked")
	private RangeSet<LocalTime> invokeIntersectRangeSets(RangeSet<LocalTime> rangeSetA, RangeSet<LocalTime> rangeSetB)
			throws Exception {
		Method method = BaseRuleEvaluator.class.getDeclaredMethod("intersectRangeSets", RangeSet.class, RangeSet.class);
		method.setAccessible(true);
		return (RangeSet<LocalTime>) method.invoke(this.evaluator, rangeSetA, rangeSetB);
	}

	private void invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluator target, TimeLog timeLog,
			IEvaluatableRule currentRule, Timesheet timesheet, BaseRuleEvaluator.EvaluationState state,
			List<IEvaluatableRule> unifiedRules, List<TimeLog> weeklyTimeLog, List<TimeLog> sameDayLogs,
			boolean isMultiInterval, List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges) throws Exception {
		Method method = BaseRuleEvaluator.class.getDeclaredMethod("collectWeeklyOvertimeCandidates", TimeLog.class,
				IEvaluatableRule.class, Timesheet.class, BaseRuleEvaluator.EvaluationState.class, List.class,
				List.class, List.class, boolean.class, List.class);
		method.setAccessible(true);
		method.invoke(target, timeLog, currentRule, timesheet, state, unifiedRules, weeklyTimeLog, sameDayLogs,
				isMultiInterval, weeklyOvertimeCandidateTimeRanges);
	}

	@Nested
	@DisplayName("buildActualWorkRanges Tests")
	class BuildActualWorkRangesTests {

		@Test
		@DisplayName("Returns work ranges for multiple valid intervals")
		void testBuildActualWorkRangesWithMultipleValidIntervals() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(7, 0), LocalTime.of(12, 0));
			TimeLog timeLog2 = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.asRanges()).hasSize(2);
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(7, 0), LocalTime.of(12, 0)))).isTrue();
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(13, 0), LocalTime.of(17, 0)))).isTrue();
		}

		@Test
		@DisplayName("Returns single range for one interval")
		void testBuildActualWorkRangesWithSingleInterval() throws Exception {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.asRanges()).hasSize(1);
		}

		@Test
		@DisplayName("Returns empty for empty list")
		void testBuildActualWorkRangesWithEmptyList() throws Exception {
			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(new ArrayList<>());

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Skips time logs with null start time")
		void testBuildActualWorkRangesSkipsNullStartTime() throws Exception {
			TimeLog timeLog = createTimeLog(null, LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Skips time logs with null end time")
		void testBuildActualWorkRangesSkipsNullEndTime() throws Exception {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), null);
			List<TimeLog> sameDayLogs = List.of(timeLog);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Skips time logs where start equals end")
		void testBuildActualWorkRangesSkipsStartEqualsEnd() throws Exception {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(9, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Skips time logs where start is after end")
		void testBuildActualWorkRangesSkipsStartAfterEnd() throws Exception {
			TimeLog timeLog = createTimeLog(LocalTime.of(17, 0), LocalTime.of(9, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Includes valid intervals and skips invalid ones")
		void testBuildActualWorkRangesMixedValidAndInvalid() throws Exception {
			TimeLog validLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog invalidLog = createTimeLog(null, null);
			List<TimeLog> sameDayLogs = List.of(validLog, invalidLog);

			RangeSet<LocalTime> result = invokeBuildActualWorkRanges(sameDayLogs);

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.asRanges()).hasSize(1);
		}

	}

	@Nested
	@DisplayName("intersectRangeSets Tests")
	class IntersectRangeSetsTests {

		@Test
		@DisplayName("Returns intersection of overlapping range sets")
		void testIntersectRangeSetsWithOverlap() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			rangeSetA.add(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(12, 0)));

			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();
			rangeSetB.add(TimeHelper.toRange(LocalTime.of(7, 0), LocalTime.of(10, 0)));

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(10, 0)))).isTrue();
		}

		@Test
		@DisplayName("Returns empty for non-overlapping range sets")
		void testIntersectRangeSetsNoOverlap() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			rangeSetA.add(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(9, 0)));

			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();
			rangeSetB.add(TimeHelper.toRange(LocalTime.of(10, 0), LocalTime.of(11, 0)));

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns empty when first set is empty")
		void testIntersectRangeSetsEmptyFirstSet() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();
			rangeSetB.add(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(9, 0)));

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns empty when second set is empty")
		void testIntersectRangeSetsEmptySecondSet() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			rangeSetA.add(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(9, 0)));
			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns empty when both sets are empty")
		void testIntersectRangeSetsBothEmpty() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Handles multiple ranges in both sets")
		void testIntersectRangeSetsMultipleRanges() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			rangeSetA.add(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(10, 0)));
			rangeSetA.add(TimeHelper.toRange(LocalTime.of(14, 0), LocalTime.of(16, 0)));

			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();
			rangeSetB.add(TimeHelper.toRange(LocalTime.of(7, 0), LocalTime.of(9, 0)));
			rangeSetB.add(TimeHelper.toRange(LocalTime.of(15, 0), LocalTime.of(17, 0)));

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.asRanges()).hasSize(2);
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(9, 0)))).isTrue();
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(15, 0), LocalTime.of(16, 0)))).isTrue();
		}

		@Test
		@DisplayName("Returns full range when A is subset of B")
		void testIntersectRangeSetsASubsetOfB() throws Exception {
			RangeSet<LocalTime> rangeSetA = TreeRangeSet.create();
			rangeSetA.add(TimeHelper.toRange(LocalTime.of(9, 0), LocalTime.of(12, 0)));

			RangeSet<LocalTime> rangeSetB = TreeRangeSet.create();
			rangeSetB.add(TimeHelper.toRange(LocalTime.of(7, 0), LocalTime.of(17, 0)));

			RangeSet<LocalTime> result = invokeIntersectRangeSets(rangeSetA, rangeSetB);

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(9, 0), LocalTime.of(12, 0)))).isTrue();
		}

	}

	@Nested
	@DisplayName("collectWeeklyOvertimeCandidates Multi-Interval Tests")
	class CollectWeeklyOvertimeCandidatesMultiIntervalTests {

		@Mock
		private IEvaluatableRule mockRule;

		@Mock
		private Timesheet mockTimesheet;

		@Test
		@DisplayName("Multi-interval: all time occupied produces no candidates")
		void testCollectMultiIntervalAllOccupiedNoCandidate() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(5, 0), LocalTime.of(7, 0));
			TimeLog timeLog2 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(5, 0), LocalTime.of(7, 0)));
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(9, 0), LocalTime.of(17, 0)));

			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog2,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Multi-interval: finds unallocated worked time as candidates")
		void testCollectMultiIntervalFindsUnallocatedWorkedTime() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(7, 0), LocalTime.of(12, 0));
			TimeLog timeLog2 = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(7, 0), LocalTime.of(8, 0)));
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(9, 0), LocalTime.of(17, 0)));

			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog2,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).hasSize(1);
			RangeSet<LocalTime> candidateRanges = candidates.get(0);
			assertThat(candidateRanges.encloses(TimeHelper.toRange(LocalTime.of(8, 0), LocalTime.of(9, 0)))).isTrue();
		}

		@Test
		@DisplayName("Multi-interval: free ranges fully occupied by gap produces no candidates")
		void testCollectMultiIntervalGapFullyOccupiedNoCandidate() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog timeLog2 = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(9, 0), LocalTime.of(12, 0)));
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(12, 0), LocalTime.of(13, 0)));
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(13, 0), LocalTime.of(17, 0)));

			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog2,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Multi-interval: null merged times produces no candidates")
		void testCollectMultiIntervalNullMergedTimesNoCandidate() throws Exception {
			TimeLog timeLog1 = createTimeLog(null, null);
			TimeLog timeLog2 = createTimeLog(null, null);
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog2,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Multi-interval: free ranges not within worked intervals")
		void testCollectMultiIntervalFreeRangesNotInWorkedIntervals() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(5, 0), LocalTime.of(7, 0));
			TimeLog timeLog2 = createTimeLog(LocalTime.of(10, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(5, 0), LocalTime.of(7, 0)));
			state.occupiedTimeRanges.add(TimeHelper.toRange(LocalTime.of(10, 0), LocalTime.of(17, 0)));

			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog2,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Multi-interval: mergedStart equals mergedEnd produces no candidates")
		void testCollectMultiIntervalMergedStartEqualsEndNoCandidate() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(9, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog1,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Multi-interval: mergedEnd null produces no candidates")
		void testCollectMultiIntervalMergedEndNullNoCandidate() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(9, 0), null);
			TimeLog timeLog2 = createTimeLog(LocalTime.of(13, 0), null);
			List<TimeLog> sameDayLogs = List.of(timeLog1, timeLog2);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog1,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Multi-interval: mergedStart after mergedEnd produces no candidates")
		void testCollectMultiIntervalMergedStartAfterEndNoCandidate() throws Exception {
			TimeLog timeLog1 = createTimeLog(LocalTime.of(17, 0), LocalTime.of(9, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog1);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			invokeCollectWeeklyOvertimeCandidates(BaseRuleEvaluatorWeeklyOtCandidateTests.this.evaluator, timeLog1,
					this.mockRule, this.mockTimesheet, state, new ArrayList<>(), sameDayLogs, sameDayLogs, true,
					candidates);

			assertThat(candidates).isEmpty();
		}

	}

	@Nested
	@DisplayName("collectWeeklyOvertimeCandidates Single-Interval Tests")
	class CollectWeeklyOvertimeCandidatesSingleIntervalTests {

		@Mock
		private IEvaluatableRule mockRule;

		@Mock
		private Timesheet mockTimesheet;

		@Test
		@DisplayName("Single-interval: resolver returns empty ranges produces no candidates")
		void testCollectSingleIntervalEmptyResolverNoCandidate() throws Exception {
			RangeBasedRuleEvaluator spyEvaluator = Mockito
				.spy(new RangeBasedRuleEvaluator(BaseRuleEvaluatorWeeklyOtCandidateTests.this.ruleFactory,
						BaseRuleEvaluatorWeeklyOtCandidateTests.this.logger, null));

			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			RangeSet<LocalTime> emptyRangeSet = TreeRangeSet.create();
			doReturn(emptyRangeSet).when(spyEvaluator)
				.resolveTimeRangesForRule(any(TimeLog.class), any(IEvaluatableRule.class), any(Timesheet.class),
						any(BaseRuleEvaluator.EvaluationState.class), any(List.class), any(List.class));

			invokeCollectWeeklyOvertimeCandidates(spyEvaluator, timeLog, this.mockRule, this.mockTimesheet, state,
					new ArrayList<>(), sameDayLogs, sameDayLogs, false, candidates);

			assertThat(candidates).isEmpty();
		}

		@Test
		@DisplayName("Single-interval: resolver returns valid ranges adds candidates")
		void testCollectSingleIntervalValidResolverAddsCandidate() throws Exception {
			RangeBasedRuleEvaluator spyEvaluator = Mockito
				.spy(new RangeBasedRuleEvaluator(BaseRuleEvaluatorWeeklyOtCandidateTests.this.ruleFactory,
						BaseRuleEvaluatorWeeklyOtCandidateTests.this.logger, null));

			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			RangeSet<LocalTime> resolvedRanges = TreeRangeSet.create();
			resolvedRanges.add(TimeHelper.toRange(LocalTime.of(10, 0), LocalTime.of(12, 0)));
			doReturn(resolvedRanges).when(spyEvaluator)
				.resolveTimeRangesForRule(any(TimeLog.class), any(IEvaluatableRule.class), any(Timesheet.class),
						any(BaseRuleEvaluator.EvaluationState.class), any(List.class), any(List.class));

			invokeCollectWeeklyOvertimeCandidates(spyEvaluator, timeLog, this.mockRule, this.mockTimesheet, state,
					new ArrayList<>(), sameDayLogs, sameDayLogs, false, candidates);

			assertThat(candidates).hasSize(1);
			assertThat(candidates.get(0).encloses(TimeHelper.toRange(LocalTime.of(10, 0), LocalTime.of(12, 0))))
				.isTrue();
		}

		@Test
		@DisplayName("Single-interval: constrained ranges become empty produces no candidates")
		void testCollectSingleIntervalConstrainedEmptyNoCandidate() throws Exception {
			RangeBasedRuleEvaluator spyEvaluator = Mockito
				.spy(new RangeBasedRuleEvaluator(BaseRuleEvaluatorWeeklyOtCandidateTests.this.ruleFactory,
						BaseRuleEvaluatorWeeklyOtCandidateTests.this.logger, null));

			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog);

			BaseRuleEvaluator.EvaluationState state = new BaseRuleEvaluator.EvaluationState();
			List<RangeSet<LocalTime>> candidates = new ArrayList<>();

			RangeSet<LocalTime> resolvedRanges = TreeRangeSet.create();
			resolvedRanges.add(TimeHelper.toRange(LocalTime.of(13, 0), LocalTime.of(17, 0)));
			doReturn(resolvedRanges).when(spyEvaluator)
				.resolveTimeRangesForRule(any(TimeLog.class), any(IEvaluatableRule.class), any(Timesheet.class),
						any(BaseRuleEvaluator.EvaluationState.class), any(List.class), any(List.class));

			invokeCollectWeeklyOvertimeCandidates(spyEvaluator, timeLog, this.mockRule, this.mockTimesheet, state,
					new ArrayList<>(), sameDayLogs, sameDayLogs, false, candidates);

			assertThat(candidates).isEmpty();
		}

	}

}
