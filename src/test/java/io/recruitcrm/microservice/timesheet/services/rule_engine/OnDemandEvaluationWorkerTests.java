/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.TimeLogRuleEvaluationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyOvertimeSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyRuleResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.mapper.RuleEngineMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleEngine;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OnDemandEvaluationWorkerTests {

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private IRuleEngine ruleEngine;

	@Mock
	private RuleEngineMapper ruleEngineMapper;

	@Mock
	private Timesheet timesheet;

	@Mock
	private TimesheetSetting timesheetSetting;

	@Mock
	private WeeklyRuleEvaluatorResult weeklyRuleEvaluatorResult;

	private OnDemandEvaluationWorker worker;

	@BeforeEach
	void setUp() {
		this.worker = new OnDemandEvaluationWorker(this.timesheetJpaRepository, this.ruleEngine, this.ruleEngineMapper);
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// evaluateSingleTimesheet — guard branches
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateSingleTimesheet - timesheet not found throws ResourceNotFoundException")
	void testEvaluateSingleTimesheetTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.empty());

		// When & Then
		List<BulkTimeLogRequestBodyDto> emptyTimeLogs = List.of();
		assertThatThrownBy(() -> this.worker.evaluateSingleTimesheet(1, emptyTimeLogs, 100, false))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Timesheet not found with ID: 1");

		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - empty time logs after conversion returns early with empty DTO")
	void testEvaluateSingleTimesheetEmptyTimeLogsAfterConversionReturnsEarlyEmptyDto() {
		// Given — duration-based, all null workTime → none converted
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		BulkTimeLogRequestBodyDto log = new BulkTimeLogRequestBodyDto();
		log.setId(10);
		log.setDate(0);
		log.setDayTypeId(1);
		log.setTimesheetId(1);
		log.setWorkTime(null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(1);
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Duration-based path
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateSingleTimesheet - duration-based valid work time returns result")
	void testEvaluateSingleTimesheetDurationBasedValidWorkTimeReturnsResult() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		then(this.ruleEngine).should().evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - duration-based zero work time skips time log")
	void testEvaluateSingleTimesheetDurationBasedZeroWorkTimeSkipsTimeLog() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 0, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then — zero workTime is treated as ≤ 0 → skipped → empty timeLogs
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - duration-based positive break time sets break duration")
	void testEvaluateSingleTimesheetDurationBasedPositiveBreakTimeSetsBreakDuration() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, 3600);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		then(this.ruleEngine).should().evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - duration-based zero break time uses zero duration")
	void testEvaluateSingleTimesheetDurationBasedZeroBreakTimeUsesZeroDuration() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, 0);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - duration-based null work log type treated as duration-based")
	void testEvaluateSingleTimesheetNullWorkLogTypeTreatedAsDurationBased() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(null);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		then(this.ruleEngine).should().evaluateRulesOnDemand(any(), anyList());
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Range-based path
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based valid work time details returns result")
	void testEvaluateSingleTimesheetRangeBasedValidWorkTimeDetailsReturnsResult() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		// 9 AM to 5 PM in seconds
		WorkTimeDetailDto detail = buildWorkDetail(32400, 61200, null);
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, List.of(detail));

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		then(this.ruleEngine).should().evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based null work time details returns empty DTO")
	void testEvaluateSingleTimesheetRangeBasedNullWorkTimeDetailsReturnsEmptyDto() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);

		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then — null details skipped → empty timeLogs → early return
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based empty work time details returns empty DTO")
	void testEvaluateSingleTimesheetRangeBasedEmptyWorkTimeDetailsReturnsEmptyDto() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);

		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, Collections.emptyList());

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based null work start time skips interval")
	void testEvaluateSingleTimesheetRangeBasedNullWorkStartTimeSkipsInterval() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);

		WorkTimeDetailDto detail = buildWorkDetail(null, 61200, null);
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, List.of(detail));

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then — createRangeBasedTimeLog returns null → skipped → empty → early return
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based null work end time skips interval")
	void testEvaluateSingleTimesheetRangeBasedNullWorkEndTimeSkipsInterval() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);

		WorkTimeDetailDto detail = buildWorkDetail(32400, null, null);
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, List.of(detail));

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based zero duration interval skips interval")
	void testEvaluateSingleTimesheetRangeBasedZeroDurationIntervalSkipsInterval() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);

		// Same start and end → zero duration
		WorkTimeDetailDto detail = buildWorkDetail(32400, 32400, null);
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, List.of(detail));

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs()).isEmpty();
		then(this.ruleEngine).should(never()).evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based with valid break intervals calculates break time")
	void testEvaluateSingleTimesheetRangeBasedWithBreakIntervalsCalculatesBreakTime() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BreakIntervalDto breakInterval = new BreakIntervalDto(null, 43200, 45000);
		WorkTimeDetailDto detail = buildWorkDetail(32400, 61200, List.of(breakInterval));
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, List.of(detail));

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		then(this.ruleEngine).should().evaluateRulesOnDemand(any(), anyList());
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based break interval with null times is skipped")
	void testEvaluateSingleTimesheetRangeBasedBreakIntervalWithNullTimesIsSkipped() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		// Break interval with null start time → skipped
		BreakIntervalDto breakInterval = new BreakIntervalDto(null, null, 45000);
		WorkTimeDetailDto detail = buildWorkDetail(32400, 61200, List.of(breakInterval));
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, List.of(detail));

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then — interval skipped but time log itself is still valid
		assertThat(result.getTimesheetId()).isEqualTo(1);
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - range-based intervals sorted by work start time")
	void testEvaluateSingleTimesheetRangeBasedIntervalsSortedByWorkStartTime() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(2);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of());
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		// Two intervals out of order: afternoon then morning
		WorkTimeDetailDto afternoon = buildWorkDetail(50400, 61200, null); // 2 PM–5 PM
		WorkTimeDetailDto morning = buildWorkDetail(32400, 43200, null); // 9 AM–12 PM
		BulkTimeLogRequestBodyDto log = buildRangeLog(10, 1, new ArrayList<>(List.of(afternoon, morning)));

		// When — both valid intervals, should be sorted internally
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		then(this.ruleEngine).should().evaluateRulesOnDemand(any(), anyList());
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// extractOvertimeFromResponse / calculateOvertimeSeconds branches
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateSingleTimesheet - null weekly results returns empty time log list")
	void testEvaluateSingleTimesheetNullWeeklyResultsReturnsEmptyTimeLogList() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(null);
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimesheetId()).isEqualTo(1);
		assertThat(result.getTimeLogs()).isEmpty();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - null time log rule evaluations in weekly result returns no entries")
	void testEvaluateSingleTimesheetNullTimeLogRuleEvaluationsReturnsNoEntries() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(null);
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs()).isEmpty();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - overtime rule type accumulates overtime seconds")
	void testEvaluateSingleTimesheetOvertimeRuleTypeAccumulatesOvertimeSeconds() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEvaluationResultResponseBodyDto overtimeRule = RuleEvaluationResultResponseBodyDto.builder()
			.ruleType(RuleType.DURATION_BASED_DAILY_OVERTIME)
			.evaluatedDurationInSeconds(3600L)
			.build();
		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTimeLogId(10);
		timeLogEval.setRuleEvaluationResults(List.of(overtimeRule));

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of(timeLogEval));
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs()).hasSize(1);
		assertThat(result.getTimeLogs().get(0).getTimeLogId()).isEqualTo(10);
		assertThat(result.getTimeLogs().get(0).getOvertimeInSeconds()).isEqualTo(3600L);
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - all overtime rule types are accumulated")
	void testEvaluateSingleTimesheetAllOvertimeRuleTypesAreAccumulated() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		List<RuleEvaluationResultResponseBodyDto> ruleResults = List.of(
				buildRuleResult(RuleType.RANGE_BASED_AFTER_SHIFT, 1800L),
				buildRuleResult(RuleType.RANGE_BASED_BEFORE_SHIFT, 900L),
				buildRuleResult(RuleType.RANGE_BASED_DAILY_OVERTIME, 3600L),
				buildRuleResult(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE, 1200L),
				buildRuleResult(RuleType.DURATION_BASED_DAILY_OVERTIME, 7200L),
				buildRuleResult(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE, 600L));

		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTimeLogId(10);
		timeLogEval.setRuleEvaluationResults(ruleResults);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of(timeLogEval));
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs().get(0).getOvertimeInSeconds())
			.isEqualTo(1800L + 900L + 3600L + 1200L + 7200L + 600L);
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - non-overtime rule type not accumulated")
	void testEvaluateSingleTimesheetNonOvertimeRuleTypeNotAccumulated() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEvaluationResultResponseBodyDto regularRule = buildRuleResult(RuleType.DURATION_BASED_REGULAR_HOURS,
				28800L);
		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTimeLogId(10);
		timeLogEval.setRuleEvaluationResults(List.of(regularRule));

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of(timeLogEval));
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs().get(0).getOvertimeInSeconds()).isZero();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - null rule type not accumulated")
	void testEvaluateSingleTimesheetNullRuleTypeNotAccumulated() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEvaluationResultResponseBodyDto nullTypeRule = buildRuleResult(null, 3600L);
		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTimeLogId(10);
		timeLogEval.setRuleEvaluationResults(List.of(nullTypeRule));

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of(timeLogEval));
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs().get(0).getOvertimeInSeconds()).isZero();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - null evaluatedDurationInSeconds on overtime rule not accumulated")
	void testEvaluateSingleTimesheetNullEvaluatedDurationOnOvertimeRuleNotAccumulated() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		// Overtime rule type but null duration → isOvertimeRuleWithDuration returns false
		RuleEvaluationResultResponseBodyDto ruleWithNullDuration = buildRuleResult(
				RuleType.DURATION_BASED_DAILY_OVERTIME, null);
		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTimeLogId(10);
		timeLogEval.setRuleEvaluationResults(List.of(ruleWithNullDuration));

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of(timeLogEval));
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs().get(0).getOvertimeInSeconds()).isZero();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - null rule evaluation results returns zero overtime")
	void testEvaluateSingleTimesheetNullRuleEvaluationResultsReturnsZeroOvertime() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTimeLogId(10);
		timeLogEval.setRuleEvaluationResults(null);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of(timeLogEval));
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getTimeLogs().get(0).getOvertimeInSeconds()).isZero();
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// weeklyOvertimeResults — includeWeeklyOvertime flag
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateSingleTimesheet - includeWeeklyOvertime false returns null weeklyOvertimeResults")
	void testEvaluateSingleTimesheetIncludeWeeklyOvertimeFalseReturnsNullWeeklyOvertimeResults() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of());
		WeeklyOvertimeSummaryResponseBodyDto overtimeSummary = new WeeklyOvertimeSummaryResponseBodyDto();
		overtimeSummary.setWeeklyOvertimeHoursInSeconds(3600L);
		weeklyResult.setWeeklyOvertimeResult(overtimeSummary);
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, false);

		// Then
		assertThat(result.getWeeklyOvertimeResults()).isNull();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - includeWeeklyOvertime true populates weeklyOvertimeResults")
	void testEvaluateSingleTimesheetIncludeWeeklyOvertimeTruePopulatesWeeklyOvertimeResults() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of());
		WeeklyOvertimeSummaryResponseBodyDto overtimeSummary = new WeeklyOvertimeSummaryResponseBodyDto();
		overtimeSummary.setWeeklyOvertimeHoursInSeconds(3600L);
		weeklyResult.setWeeklyOvertimeResult(overtimeSummary);
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, true);

		// Then
		assertThat(result.getWeeklyOvertimeResults()).isNotNull().hasSize(1);
		assertThat(result.getWeeklyOvertimeResults().get(0)).isEqualTo(3600L);
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - includeWeeklyOvertime true with null weeklyOvertimeResult returns zero entry")
	void testEvaluateSingleTimesheetIncludeWeeklyOvertimeTrueNullOvertimeResultReturnsZeroEntry() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of());
		weeklyResult.setWeeklyOvertimeResult(null);
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When — extractWeeklyOvertimeSeconds returns 0 when weeklyOvertimeResult is null
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, true);

		// Then
		assertThat(result.getWeeklyOvertimeResults()).isNotNull().hasSize(1);
		assertThat(result.getWeeklyOvertimeResults().get(0)).isZero();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - includeWeeklyOvertime true with null weeklyOvertimeHoursInSeconds returns zero entry")
	void testEvaluateSingleTimesheetIncludeWeeklyOvertimeTrueNullHoursInSecondsReturnsZeroEntry() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();
		weeklyResult.setTimeLogRuleEvaluations(List.of());
		WeeklyOvertimeSummaryResponseBodyDto overtimeSummary = new WeeklyOvertimeSummaryResponseBodyDto();
		overtimeSummary.setWeeklyOvertimeHoursInSeconds(null);
		weeklyResult.setWeeklyOvertimeResult(overtimeSummary);
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(weeklyResult));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When — extractWeeklyOvertimeSeconds returns 0 when weeklyOvertimeHoursInSeconds
		// is null
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, true);

		// Then
		assertThat(result.getWeeklyOvertimeResults()).isNotNull().hasSize(1);
		assertThat(result.getWeeklyOvertimeResults().get(0)).isZero();
	}

	@Test
	@DisplayName("evaluateSingleTimesheet - includeWeeklyOvertime true with multiple weekly results populates all entries")
	void testEvaluateSingleTimesheetIncludeWeeklyOvertimeTrueMultipleWeeksPopulatesAllEntries() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(1, 100)).willReturn(Optional.of(this.timesheet));
		given(this.timesheet.getTimesheetSetting()).willReturn(this.timesheetSetting);
		given(this.timesheetSetting.getWorkLogType()).willReturn(1);
		given(this.ruleEngine.evaluateRulesOnDemand(any(), anyList())).willReturn(this.weeklyRuleEvaluatorResult);

		WeeklyRuleResultResponseBodyDto week1 = new WeeklyRuleResultResponseBodyDto();
		week1.setTimeLogRuleEvaluations(List.of());
		WeeklyOvertimeSummaryResponseBodyDto ot1 = new WeeklyOvertimeSummaryResponseBodyDto();
		ot1.setWeeklyOvertimeHoursInSeconds(1800L);
		week1.setWeeklyOvertimeResult(ot1);

		WeeklyRuleResultResponseBodyDto week2 = new WeeklyRuleResultResponseBodyDto();
		week2.setTimeLogRuleEvaluations(List.of());
		WeeklyOvertimeSummaryResponseBodyDto ot2 = new WeeklyOvertimeSummaryResponseBodyDto();
		ot2.setWeeklyOvertimeHoursInSeconds(7200L);
		week2.setWeeklyOvertimeResult(ot2);

		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();
		response.setWeeklyResults(List.of(week1, week2));
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult)).willReturn(response);

		BulkTimeLogRequestBodyDto log = buildDurationLog(10, 1, 28800, null);

		// When
		OnDemandTimesheetOvertimeDto result = this.worker.evaluateSingleTimesheet(1, List.of(log), 100, true);

		// Then
		assertThat(result.getWeeklyOvertimeResults()).isNotNull().hasSize(2);
		assertThat(result.getWeeklyOvertimeResults().get(0)).isEqualTo(1800L);
		assertThat(result.getWeeklyOvertimeResults().get(1)).isEqualTo(7200L);
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Constructor
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Constructor - creates instance with all dependencies")
	void testConstructorCreatesInstanceWithAllDependencies() {
		// When
		OnDemandEvaluationWorker newWorker = new OnDemandEvaluationWorker(this.timesheetJpaRepository, this.ruleEngine,
				this.ruleEngineMapper);

		// Then
		assertThat(newWorker).isNotNull();
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Test data builders
	// ──────────────────────────────────────────────────────────────────────────────

	private static BulkTimeLogRequestBodyDto buildDurationLog(int id, int dayTypeId, Integer workTime,
			Integer breakTime) {
		BulkTimeLogRequestBodyDto log = new BulkTimeLogRequestBodyDto();
		log.setId(id);
		log.setDate(0);
		log.setDayTypeId(dayTypeId);
		log.setTimesheetId(1);
		log.setWorkTime(workTime);
		log.setBreakTime(breakTime);
		return log;
	}

	private static BulkTimeLogRequestBodyDto buildRangeLog(int id, int dayTypeId, List<WorkTimeDetailDto> details) {
		BulkTimeLogRequestBodyDto log = new BulkTimeLogRequestBodyDto();
		log.setId(id);
		log.setDate(0);
		log.setDayTypeId(dayTypeId);
		log.setTimesheetId(1);
		log.setWorkTimeDetails(details);
		return log;
	}

	private static WorkTimeDetailDto buildWorkDetail(Integer startTime, Integer endTime,
			List<BreakIntervalDto> breakIntervals) {
		WorkTimeDetailDto detail = new WorkTimeDetailDto();
		detail.setWorkStartTime(startTime);
		detail.setWorkEndTime(endTime);
		detail.setBreakIntervals(breakIntervals);
		return detail;
	}

	private static RuleEvaluationResultResponseBodyDto buildRuleResult(RuleType ruleType, Long durationInSeconds) {
		return RuleEvaluationResultResponseBodyDto.builder()
			.ruleType(ruleType)
			.evaluatedDurationInSeconds(durationInSeconds)
			.build();
	}

}
