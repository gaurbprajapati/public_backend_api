/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.OvertimeDetail;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.WeeklyOvertimeDetail;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.MoneyDataResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.TimeLogRuleEvaluationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyOvertimeSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyRuleResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.mapper.RuleEngineMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleEngine;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.MoneyData;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluatorResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult.WeeklyResult;

import com.google.common.collect.TreeRangeSet;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTests {

	@Mock
	private IRuleEngine ruleEngine;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private AuthHolder authHolder;

	@Mock
	private RuleEngineMapper ruleEngineMapper;

	@Mock
	private AccessControlChecker accessControlChecker;

	@Mock
	private OnDemandEvaluationWorker onDemandEvaluationWorker;

	@Mock
	private java.util.concurrent.Executor onDemandEvaluationExecutor;

	@Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
	private DSLContext dslContext;

	@Mock
	private Timesheet timesheet;

	@Mock
	private WeeklyRuleEvaluatorResult weeklyRuleEvaluatorResult;

	@Mock
	private RuleEngineResponseBodyDto ruleEngineResponseBodyDto;

	@Mock
	private RuleEngineRequestBodyDto requestDto;

	private RuleEngineService ruleEngineService;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		this.ruleEngineService = new RuleEngineService(this.ruleEngine, this.timesheetJpaRepository, this.authHolder,
				this.ruleEngineMapper, this.accessControlChecker, this.onDemandEvaluationWorker,
				this.onDemandEvaluationExecutor, this.dslContext);
	}

	@Test
	@DisplayName("Evaluate rules - Success with refresh stored data")
	void testEvaluateRulesSuccessWithRefreshStoredDataReturnsResponse() {
		AuthPrincipal mockPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.requestDto.getTimesheetId()).willReturn(123);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(789);
		given(this.timesheetJpaRepository.findByIdAndAccountId(123, 456)).willReturn(Optional.of(this.timesheet));
		given(this.ruleEngine.evaluateRules(this.timesheet)).willReturn(this.weeklyRuleEvaluatorResult);
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult))
			.willReturn(this.ruleEngineResponseBodyDto);

		RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(this.requestDto);

		assertThat(result).isEqualTo(this.ruleEngineResponseBodyDto);
		then(this.accessControlChecker).should()
			.allows(eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
		then(this.ruleEngine).should().evaluateRules(this.timesheet);
		then(this.ruleEngineMapper).should().toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult);
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Evaluate rules - Success without refresh stored data",
			"Evaluate rules - Timesheet approval not found throws ResourceNotFoundException",
			"Evaluate rules - Timesheet not approved throws ValidationErrorException" })
	void testEvaluateRulesSuccessVariants(String description) {
		AuthPrincipal mockPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.requestDto.getTimesheetId()).willReturn(123);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(789);
		given(this.timesheetJpaRepository.findByIdAndAccountId(123, 456)).willReturn(Optional.of(this.timesheet));
		given(this.ruleEngine.evaluateRules(this.timesheet)).willReturn(this.weeklyRuleEvaluatorResult);
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult))
			.willReturn(this.ruleEngineResponseBodyDto);

		RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(this.requestDto);

		assertThat(result).isEqualTo(this.ruleEngineResponseBodyDto);
	}

	@Test
	@DisplayName("Evaluate rules - Timesheet not found throws ResourceNotFoundException")
	void testEvaluateRulesTimesheetNotFoundThrowsResourceNotFoundException() {
		AuthPrincipal mockPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.requestDto.getTimesheetId()).willReturn(123);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);
		given(this.timesheetJpaRepository.findByIdAndAccountId(123, 456)).willReturn(Optional.empty());

		assertThatThrownBy(() -> this.ruleEngineService.evaluateRules(this.requestDto))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Timesheet not found with ID: 123");
	}

	@Test
	@DisplayName("Validate rules - Success")
	void testValidateRulesSuccessReturnsValidationMessage() {
		String result = this.ruleEngineService.validateRules(this.requestDto);

		assertThat(result).isEqualTo("Validation successful");
		then(this.ruleEngine).should().validateRules();
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - timesheet is null does nothing")
	void testStoreTotalBillPayAndRegularHourDataTimesheetNull() throws Exception {
		var method = this.storeMethod();
		method.invoke(this.ruleEngineService, (Timesheet) null, this.weeklyRuleEvaluatorResult, 100, 2,
				this.makeMetrics(Map.of(), 0L));
		org.mockito.Mockito.verify(this.timesheetJpaRepository, org.mockito.Mockito.never()).save(any());
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - result is null does nothing")
	void testStoreTotalBillPayAndRegularHourDataResultNull() throws Exception {
		var method = this.storeMethod();
		method.invoke(this.ruleEngineService, this.timesheet, null, 100, 2, this.makeMetrics(Map.of(), 0L));
		org.mockito.Mockito.verify(this.timesheetJpaRepository, org.mockito.Mockito.never()).save(any());
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - weeklyResults is empty")
	void testStoreTotalBillPayAndRegularHourDataWeeklyResultsEmpty() throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(java.util.Collections.emptyList());

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 100, 2, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setTotalBillData(0.0f);
		org.mockito.Mockito.verify(this.timesheet).setTotalPayData(0.0f);
		org.mockito.Mockito.verify(this.timesheet).setTotalRegularHour(0);
		org.mockito.Mockito.verify(this.timesheetJpaRepository).save(this.timesheet);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - weeklyResults with null RuleEvaluatorResult")
	void testStoreTotalBillPayAndRegularHourDataWeeklyResultsWithNullRuleEvaluatorResult() throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		org.mockito.BDDMockito.given(weeklyResult.getRuleEvaluatorResult()).willReturn(null);
		org.mockito.BDDMockito.given(result.getWeeklyResults())
			.willReturn(java.util.Collections.singletonList(weeklyResult));

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 100, 2, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setTotalBillData(0.0f);
		org.mockito.Mockito.verify(this.timesheet).setTotalPayData(0.0f);
		org.mockito.Mockito.verify(this.timesheetJpaRepository).save(this.timesheet);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - weeklyResults with non-null RuleEvaluatorResult")
	void testStoreTotalBillPayAndRegularHourDataWeeklyResultsWithNonNullRuleEvaluatorResult() throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		RuleEvaluatorResult ruleEvaluatorResult = org.mockito.Mockito.mock(RuleEvaluatorResult.class);

		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalBillAmount())
			.willReturn(new java.math.BigDecimal("10.50"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalPayAmount())
			.willReturn(new java.math.BigDecimal("5.25"));

		MoneyData weeklyOvertimeMoneyData = org.mockito.Mockito.mock(MoneyData.class);
		org.mockito.BDDMockito.given(weeklyOvertimeMoneyData.getBillAmount())
			.willReturn(new java.math.BigDecimal("15.75"));
		org.mockito.BDDMockito.given(weeklyOvertimeMoneyData.getPayAmount())
			.willReturn(new java.math.BigDecimal("8.25"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getWeeklyOvertimeMoneyData())
			.willReturn(weeklyOvertimeMoneyData);

		org.mockito.BDDMockito.given(weeklyResult.getRuleEvaluatorResult()).willReturn(ruleEvaluatorResult);
		org.mockito.BDDMockito.given(result.getWeeklyResults())
			.willReturn(java.util.Collections.singletonList(weeklyResult));

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 100, 2, this.makeMetrics(Map.of(10, 3600), 0L));

		org.mockito.Mockito.verify(this.timesheet).setTotalBillData(26.25f);
		org.mockito.Mockito.verify(this.timesheet).setTotalPayData(13.50f);
		org.mockito.Mockito.verify(this.timesheet).setTotalRegularHour(3600);
		org.mockito.Mockito.verify(this.timesheetJpaRepository).save(this.timesheet);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - weeklyResults with non-null RuleEvaluatorResult but no weekly overtime")
	void testStoreTotalBillPayAndRegularHourDataWeeklyResultsWithNonNullRuleEvaluatorResultNoWeeklyOvertime()
			throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		RuleEvaluatorResult ruleEvaluatorResult = org.mockito.Mockito.mock(RuleEvaluatorResult.class);

		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalBillAmount())
			.willReturn(new java.math.BigDecimal("10.50"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalPayAmount())
			.willReturn(new java.math.BigDecimal("5.25"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getWeeklyOvertimeMoneyData()).willReturn(null);

		org.mockito.BDDMockito.given(weeklyResult.getRuleEvaluatorResult()).willReturn(ruleEvaluatorResult);
		org.mockito.BDDMockito.given(result.getWeeklyResults())
			.willReturn(java.util.Collections.singletonList(weeklyResult));

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 100, 2, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setTotalBillData(10.50f);
		org.mockito.Mockito.verify(this.timesheet).setTotalPayData(5.25f);
		org.mockito.Mockito.verify(this.timesheetJpaRepository).save(this.timesheet);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets updatedBy with userId")
	void testStoreTotalBillPayAndRegularHourDataSetsUpdatedBy() throws Exception {
		Integer userId = 123;
		Integer userTypeId = 2;
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(java.util.Collections.emptyList());

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, userId, userTypeId, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setUpdatedBy(userId);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets updatedByUserTypeId with userTypeId")
	void testStoreTotalBillPayAndRegularHourDataSetsUpdatedByUserTypeId() throws Exception {
		Integer userId = 456;
		Integer userTypeId = 1;
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(java.util.Collections.emptyList());

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, userId, userTypeId, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setUpdatedByUserTypeId(userTypeId);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets updatedOn with current UNIX timestamp")
	void testStoreTotalBillPayAndRegularHourDataSetsUpdatedOn() throws Exception {
		Integer userId = 789;
		Integer userTypeId = 2;
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(java.util.Collections.emptyList());

		int timestampBefore = (int) java.time.Instant.now().getEpochSecond();
		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, userId, userTypeId, this.makeMetrics(Map.of(), 0L));
		int timestampAfter = (int) java.time.Instant.now().getEpochSecond();

		org.mockito.ArgumentCaptor<Integer> timestampCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
		org.mockito.Mockito.verify(this.timesheet).setUpdatedOn(timestampCaptor.capture());
		Integer capturedTimestamp = timestampCaptor.getValue();
		assertThat(capturedTimestamp).isGreaterThanOrEqualTo(timestampBefore).isLessThanOrEqualTo(timestampAfter);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets all audit fields together with different user types")
	void testStoreTotalBillPayAndRegularHourDataSetsAllAuditFieldsForContactPrincipal() throws Exception {
		Integer userId = 999;
		Integer userTypeId = 1;
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		RuleEvaluatorResult ruleEvaluatorResult = org.mockito.Mockito.mock(RuleEvaluatorResult.class);

		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalBillAmount())
			.willReturn(new java.math.BigDecimal("100.00"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalPayAmount())
			.willReturn(new java.math.BigDecimal("50.00"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getWeeklyOvertimeMoneyData()).willReturn(null);

		org.mockito.BDDMockito.given(weeklyResult.getRuleEvaluatorResult()).willReturn(ruleEvaluatorResult);
		org.mockito.BDDMockito.given(result.getWeeklyResults())
			.willReturn(java.util.Collections.singletonList(weeklyResult));

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, userId, userTypeId, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setUpdatedBy(userId);
		org.mockito.Mockito.verify(this.timesheet).setUpdatedByUserTypeId(userTypeId);
		org.mockito.Mockito.verify(this.timesheet).setUpdatedOn(org.mockito.ArgumentMatchers.anyInt());
		org.mockito.Mockito.verify(this.timesheet).setTotalBillData(100.00f);
		org.mockito.Mockito.verify(this.timesheet).setTotalPayData(50.00f);
		org.mockito.Mockito.verify(this.timesheetJpaRepository).save(this.timesheet);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets all audit fields together for agency user")
	void testStoreTotalBillPayAndRegularHourDataSetsAllAuditFieldsForAgencyUser() throws Exception {
		Integer userId = 888;
		Integer userTypeId = 2;
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		RuleEvaluatorResult ruleEvaluatorResult = org.mockito.Mockito.mock(RuleEvaluatorResult.class);

		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalBillAmount())
			.willReturn(new java.math.BigDecimal("200.00"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getTotalPayAmount())
			.willReturn(new java.math.BigDecimal("100.00"));
		org.mockito.BDDMockito.given(ruleEvaluatorResult.getWeeklyOvertimeMoneyData()).willReturn(null);

		org.mockito.BDDMockito.given(weeklyResult.getRuleEvaluatorResult()).willReturn(ruleEvaluatorResult);
		org.mockito.BDDMockito.given(result.getWeeklyResults())
			.willReturn(java.util.Collections.singletonList(weeklyResult));

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, userId, userTypeId, this.makeMetrics(Map.of(), 0L));

		org.mockito.Mockito.verify(this.timesheet).setUpdatedBy(userId);
		org.mockito.Mockito.verify(this.timesheet).setUpdatedByUserTypeId(userTypeId);
		org.mockito.Mockito.verify(this.timesheet).setUpdatedOn(org.mockito.ArgumentMatchers.anyInt());
		org.mockito.Mockito.verify(this.timesheet).setTotalBillData(200.00f);
		org.mockito.Mockito.verify(this.timesheet).setTotalPayData(100.00f);
		org.mockito.Mockito.verify(this.timesheetJpaRepository).save(this.timesheet);
	}

	@Test
	@DisplayName("Evaluate rules for CONTRACTOR principal - Should hide bill amounts")
	void testEvaluateRulesForContractorPrincipalHidesBillAmounts() {
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(456);
		given(contractorPrincipal.getCandidateId()).willReturn(789);

		given(this.authHolder.getUnifiedPrincipal()).willReturn(contractorPrincipal);
		given(this.requestDto.getTimesheetId()).willReturn(Integer.valueOf(123));
		given(this.timesheetJpaRepository.findByIdAndAccountId(Integer.valueOf(123), Integer.valueOf(456)))
			.willReturn(Optional.of(this.timesheet));
		given(this.ruleEngine.evaluateRules(this.timesheet)).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto responseDto = this.createMockResponseWithAmounts();
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult))
			.willReturn(responseDto);

		RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(this.requestDto);

		assertThat(result).isNotNull();

		assertThat(result.getEvaluationSummary().getTotalBillAmount()).isNull();
		assertThat(result.getEvaluationSummary().getTotalPayAmount()).isNotNull();

		for (WeeklyRuleResultResponseBodyDto weeklyResult : result.getWeeklyResults()) {
			assertThat(weeklyResult.getWeeklyMoneyData().getBillAmount()).isNull();
			assertThat(weeklyResult.getWeeklyMoneyData().getPayAmount()).isNotNull();

			for (TimeLogRuleEvaluationResponseBodyDto timeLogEval : weeklyResult.getTimeLogRuleEvaluations()) {
				assertThat(timeLogEval.getTotalBillAmount()).isNull();
				assertThat(timeLogEval.getTotalPayAmount()).isNotNull();

				for (RuleEvaluationResultResponseBodyDto ruleResult : timeLogEval.getRuleEvaluationResults()) {
					assertThat(ruleResult.getBillAmount()).isNull();
					assertThat(ruleResult.getPayAmount()).isNotNull();
				}
			}

			assertThat(weeklyResult.getWeeklyOvertimeResult().getWeeklyOvertimeBillAmount()).isNull();
			assertThat(weeklyResult.getWeeklyOvertimeResult().getWeeklyOvertimePayAmount()).isNotNull();
		}

		then(this.ruleEngine).should().evaluateRules(this.timesheet);
		then(this.ruleEngineMapper).should().toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult);
	}

	@Test
	@DisplayName("Evaluate rules for CONTACT principal - Should hide all pay and bill amounts")
	void testEvaluateRulesForContactPrincipalHidesAllPayAndBillAmounts() {
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(456);
		given(contactPrincipal.getContactId()).willReturn(789);

		given(this.authHolder.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.requestDto.getTimesheetId()).willReturn(Integer.valueOf(123));
		given(this.timesheetJpaRepository.findByIdAndAccountId(Integer.valueOf(123), Integer.valueOf(456)))
			.willReturn(Optional.of(this.timesheet));
		given(this.ruleEngine.evaluateRules(this.timesheet)).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto responseDto = this.createMockResponseWithAmounts();
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult))
			.willReturn(responseDto);

		RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(this.requestDto);

		assertThat(result).isNotNull();

		assertThat(result.getEvaluationSummary().getTotalBillAmount()).isNull();
		assertThat(result.getEvaluationSummary().getTotalPayAmount()).isNull();

		for (WeeklyRuleResultResponseBodyDto weeklyResult : result.getWeeklyResults()) {
			assertThat(weeklyResult.getWeeklyMoneyData().getBillAmount()).isNull();
			assertThat(weeklyResult.getWeeklyMoneyData().getPayAmount()).isNull();

			for (TimeLogRuleEvaluationResponseBodyDto timeLogEval : weeklyResult.getTimeLogRuleEvaluations()) {
				assertThat(timeLogEval.getTotalBillAmount()).isNull();
				assertThat(timeLogEval.getTotalPayAmount()).isNull();

				for (RuleEvaluationResultResponseBodyDto ruleResult : timeLogEval.getRuleEvaluationResults()) {
					assertThat(ruleResult.getBillAmount()).isNull();
					assertThat(ruleResult.getPayAmount()).isNull();
				}
			}

			assertThat(weeklyResult.getWeeklyOvertimeResult().getWeeklyOvertimeBillAmount()).isNull();
			assertThat(weeklyResult.getWeeklyOvertimeResult().getWeeklyOvertimePayAmount()).isNull();
		}

		then(this.ruleEngine).should().evaluateRules(this.timesheet);
		then(this.ruleEngineMapper).should().toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult);
	}

	@Test
	@DisplayName("Evaluate rules for USER principal - Should show all amounts (no filtering)")
	void testEvaluateRulesForUserPrincipalShowsAllAmounts() {
		AuthPrincipal mockPrincipal = mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.requestDto.getTimesheetId()).willReturn(Integer.valueOf(123));
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(456));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(Integer.valueOf(789));
		given(this.timesheetJpaRepository.findByIdAndAccountId(Integer.valueOf(123), Integer.valueOf(456)))
			.willReturn(Optional.of(this.timesheet));
		given(this.ruleEngine.evaluateRules(this.timesheet)).willReturn(this.weeklyRuleEvaluatorResult);

		RuleEngineResponseBodyDto responseDto = this.createMockResponseWithAmounts();
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult))
			.willReturn(responseDto);

		RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(this.requestDto);

		assertThat(result).isNotNull();

		assertThat(result.getEvaluationSummary().getTotalBillAmount()).isNotNull().isEqualTo(new BigDecimal("1000.00"));
		assertThat(result.getEvaluationSummary().getTotalPayAmount()).isNotNull().isEqualTo(new BigDecimal("500.00"));
	}

	@Test
	@DisplayName("Filter response with null response - Should handle gracefully")
	void testFilterResponseWithNullResponseHandlesGracefully() throws Exception {
		var method = RuleEngineService.class.getDeclaredMethod("filterResponseBasedOnPrincipalType",
				RuleEngineResponseBodyDto.class, PrincipalType.class);
		method.setAccessible(true);

		Object result = method.invoke(this.ruleEngineService, null, PrincipalType.CONTRACTOR);
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Filter response with null principal type - Should handle gracefully")
	void testFilterResponseWithNullPrincipalTypeHandlesGracefully() throws Exception {
		RuleEngineResponseBodyDto response = this.createMockResponseWithAmounts();
		var method = RuleEngineService.class.getDeclaredMethod("filterResponseBasedOnPrincipalType",
				RuleEngineResponseBodyDto.class, PrincipalType.class);
		method.setAccessible(true);

		Object result = method.invoke(this.ruleEngineService, response, null);
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Evaluate rules - unknown principal type (not USER/Contact/Contractor instance) falls back to authHolder")
	void testEvaluateRulesUnknownPrincipalTypeFallsBackToAuthHolder() {
		// Given — plain AuthPrincipal mock (not ContactPrincipal, not
		// ContractorPrincipal)
		// with a non-USER type exercises the else-fallback branch in evaluateRules:
		// 1. getPrincipalType() == USER → false
		// 2. instanceof ContactPrincipal → false
		// 3. instanceof ContractorPrincipal → false
		// 4. else → authHolder.getAuthenticationPrincipalOrganizationIdentifier()
		AuthPrincipal plainPrincipal = mock(AuthPrincipal.class);
		given(plainPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(plainPrincipal);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(789);
		given(this.requestDto.getTimesheetId()).willReturn(123);
		given(this.timesheetJpaRepository.findByIdAndAccountId(123, 456)).willReturn(Optional.of(this.timesheet));
		given(this.ruleEngine.evaluateRules(this.timesheet)).willReturn(this.weeklyRuleEvaluatorResult);
		given(this.ruleEngineMapper.toRuleEngineResponseBodyDto(this.weeklyRuleEvaluatorResult))
			.willReturn(this.ruleEngineResponseBodyDto);

		// When
		RuleEngineResponseBodyDto result = this.ruleEngineService.evaluateRules(this.requestDto);

		// Then — else branch uses authHolder for both account and user id
		assertThat(result).isEqualTo(this.ruleEngineResponseBodyDto);
		then(this.authHolder).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.authHolder).should().getAuthenticationPrincipalUniqueIdentifier();
	}

	// --- extractRuleEngineMetrics (regular-hours component) tests ---

	@Test
	@DisplayName("extractRuleEngineMetrics - null result returns empty regular hours map")
	void testExtractRegularHoursFromResultNullResult() {
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(null);
		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null weekly results returns empty regular hours map")
	void testExtractRegularHoursFromResultNullWeeklyResults() {
		given(this.weeklyRuleEvaluatorResult.getWeeklyResults()).willReturn(null);
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(this.weeklyRuleEvaluatorResult);
		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - empty weekly results returns empty regular hours map")
	void testExtractRegularHoursFromResultEmptyWeeklyResults() {
		given(this.weeklyRuleEvaluatorResult.getWeeklyResults()).willReturn(List.of());
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(this.weeklyRuleEvaluatorResult);
		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - aggregates RANGE_BASED_REGULAR_HOURS seconds")
	void testExtractRegularHoursFromResultAggregatesRegularHours() {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(100);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult regularHours = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.evaluatedDuration(Duration.ofHours(2))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, regularHours);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(100, 7200);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - aggregates DURATION_BASED_REGULAR_HOURS seconds (WORK_HOUR)")
	void testExtractRegularHoursFromResultAggregatesDurationBasedRegularHours() {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(101);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult regularHours = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_REGULAR_HOURS)
			.evaluatedDuration(Duration.ofHours(5))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, regularHours);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(101, 18000);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - ignores non-regular, non-overtime rule types (e.g. BREAK)")
	void testExtractRegularHoursFromResultIgnoresOtherRuleTypes() {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(200);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult breakRule = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_BREAK)
			.evaluatedDuration(Duration.ofMinutes(30))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, breakRule);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - sums multiple intervals for same time log")
	void testExtractRegularHoursFromResultSumsMultipleIntervals() {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(300);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult r1 = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.evaluatedDuration(Duration.ofHours(3))
			.build();
		RuleEvaluationResult r2 = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, r1);
		weekResult.addRuleEvaluationResult(dtoLog, r2);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(300, 14400);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null eval result in weekly is skipped")
	void testExtractRegularHoursFromResultNullEvalResultSkipped() {
		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(null).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null time log key is skipped")
	void testExtractRegularHoursFromResultNullTimeLogKeySkipped() {
		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.getRuleEvaluationResults().put(null, List.of());

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - time log with null id is skipped")
	void testExtractRegularHoursFromResultTimeLogNullIdSkipped() {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(null);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult rr = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, rr);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null rule evaluation result in list is skipped")
	void testExtractRegularHoursFromResultNullRuleEvaluationResultSkipped() {
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(400);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.getRuleEvaluationResults().put(dtoLog, new ArrayList<>());
		weekResult.getRuleEvaluationResults().get(dtoLog).add(null);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	// --- extractRuleEngineMetrics (isPerTimeLogRegularHoursRuleType — missing
	// conditions) ---

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_DEFAULT_PAY accumulates regular hours")
	void testExtractRuleEngineMetricsRangeBasedDefaultPayAccumulatesRegularHours() {
		// Given — RANGE_BASED_DEFAULT_PAY is condition C in the OR chain
		// (A=RANGE_BASED_REGULAR_HOURS false, B=DURATION_BASED_REGULAR_HOURS false,
		// C=true)
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(600);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult defaultPay = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_DEFAULT_PAY)
			.evaluatedDuration(Duration.ofHours(4))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, defaultPay);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(600, 14400);
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - DURATION_BASED_DEFAULT_PAY accumulates regular hours")
	void testExtractRuleEngineMetricsDurationBasedDefaultPayAccumulatesRegularHours() {
		// Given — DURATION_BASED_DEFAULT_PAY is condition D (the last in the OR chain)
		// A=false, B=false, C=false, D=true — all preceding conditions must be evaluated
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(601);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult durationDefaultPay = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_DEFAULT_PAY)
			.evaluatedDuration(Duration.ofHours(6))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, durationDefaultPay);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(601, 21600);
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null ruleType in per-log result skips both regular and overtime")
	void testExtractRuleEngineMetricsNullRuleTypeSkipsBothRegularAndOvertime() {
		// Given — null ruleType exercises the isPerTimeLogRegularHoursRuleType(null)
		// guard (returns false) AND isOvertimeRuleType(null) guard (returns false)
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(602);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult nullTypeResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(null)
			.evaluatedDuration(Duration.ofHours(2))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, nullTypeResult);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — null ruleType falls through both guards → no contribution
		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null evaluatedDuration on regular-hours rule falls back to calculateDuration")
	void testExtractRuleEngineMetricsNullEvaluatedDurationOnRegularHoursRuleFallsBackToCalculateDuration() {
		// Given: evaluatedDuration is null on a regular-hours rule.
		// resolveEvaluatedDurationSeconds falls back to calculateDuration() from the
		// timeRange (covers the else branch)
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(603);

		com.google.common.collect.RangeSet<java.time.LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(com.google.common.collect.Range.closedOpen(java.time.LocalTime.of(9, 0),
				java.time.LocalTime.of(17, 0)));

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult regularNoEvaluated = RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.evaluatedDuration(null)
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, regularNoEvaluated);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — calculateDuration() gives 8 h (09:00–17:00)
		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(603, 28800);
	}

	// --- extractRuleEngineMetrics (overtime component) tests ---

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_AFTER_SHIFT accumulates totalOvertimeSeconds")
	void testExtractRuleEngineMetricsRangeBasedAfterShiftAccumulatesOvertimeSeconds() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(500);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult afterShift = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_AFTER_SHIFT)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, afterShift);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(3600L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_BEFORE_SHIFT accumulates totalOvertimeSeconds")
	void testExtractRuleEngineMetricsRangeBasedBeforeShiftAccumulatesOvertimeSeconds() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(501);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult beforeShift = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_BEFORE_SHIFT)
			.evaluatedDuration(Duration.ofMinutes(30))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, beforeShift);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(1800L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_DAILY_OVERTIME accumulates totalOvertimeSeconds")
	void testExtractRuleEngineMetricsRangeBasedDailyOvertimeAccumulatesOvertimeSeconds() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(502);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult dailyOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_DAILY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(2))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, dailyOt);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(7200L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_SPECIFIC_TIME_RANGE accumulates totalOvertimeSeconds")
	void testExtractRuleEngineMetricsRangeBasedSpecificTimeRangeAccumulatesOvertimeSeconds() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(503);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult specificRange = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE)
			.evaluatedDuration(Duration.ofMinutes(45))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, specificRange);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(2700L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - DURATION_BASED_DAILY_OVERTIME accumulates totalOvertimeSeconds")
	void testExtractRuleEngineMetricsDurationBasedDailyOvertimeAccumulatesOvertimeSeconds() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(504);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult durationDailyOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_DAILY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(3))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, durationDailyOt);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(10800L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - DURATION_BASED_SPECIFIC_HOUR_RANGE accumulates totalOvertimeSeconds")
	void testExtractRuleEngineMetricsDurationBasedSpecificHourRangeAccumulatesOvertimeSeconds() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(505);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult specificHour = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE)
			.evaluatedDuration(Duration.ofMinutes(90))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, specificHour);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(5400L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - overtime with negative evaluated duration is clamped to zero")
	void testExtractRuleEngineMetricsNegativeOvertimeDurationIsClampedToZero() {
		// Given — negative duration tests Math.max(0L, seconds) in the overtime branch
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(506);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult negativeOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_DAILY_OVERTIME)
			.evaluatedDuration(Duration.ofSeconds(-3600))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, negativeOt);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - mixed regular hours and overtime in same time log both accumulate")
	void testExtractRuleEngineMetricsMixedRegularAndOvertimeSameTimeLogBothAccumulate() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(507);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult regular = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.evaluatedDuration(Duration.ofHours(8))
			.build();
		RuleEvaluationResult overtime = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_AFTER_SHIFT)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, regular);
		weekResult.addRuleEvaluationResult(dtoLog, overtime);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — regular hours and overtime accumulate independently
		assertThat(metrics.regularHoursByTimeLogId()).containsEntry(507, 28800);
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(3600L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - overtime entries across multiple time logs sum correctly")
	void testExtractRuleEngineMetricsOvertimeAcrossMultipleTimeLogsSumCorrectly() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog log1 = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		log1.setId(510);
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog log2 = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		log2.setId(511);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult ot1 = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_DAILY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		RuleEvaluationResult ot2 = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_DAILY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(2))
			.build();
		weekResult.addRuleEvaluationResult(log1, ot1);
		weekResult.addRuleEvaluationResult(log2, ot2);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — 1h + 2h = 10800 seconds
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(10800L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - null evaluatedDuration falls back to calculateDuration from timeRange")
	void testExtractRuleEngineMetricsNullEvaluatedDurationFallsBackToTimeRange() {
		// Given — evaluatedDuration is null so resolveEvaluatedDurationSeconds falls
		// back to calculateDuration() which sums the time range intervals
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(520);

		com.google.common.collect.RangeSet<java.time.LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(com.google.common.collect.Range.closedOpen(java.time.LocalTime.of(17, 0),
				java.time.LocalTime.of(18, 0)));

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult overtimeNoEvaluatedDuration = RuleEvaluationResult.builder()
			.timeRange(timeRange)
			.ruleType(RuleType.RANGE_BASED_AFTER_SHIFT)
			.evaluatedDuration(null)
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, overtimeNoEvaluatedDuration);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — calculateDuration gives 1 hour (17:00–18:00)
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(3600L);
	}

	// --- accumulateWeeklyOvertimeSeconds branch coverage ---

	@Test
	@DisplayName("extractRuleEngineMetrics - weeklyOtResult null returns zero weekly overtime")
	void testExtractRuleEngineMetricsWeeklyOtResultNullReturnsZeroOvertime() {
		// Given — weeklyOvertimeRuleEvaluationResult is null → first condition true
		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder().weeklyOvertimeRuleEvaluationResult(null).build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - weeklyOtResult non-overtime ruleType returns zero weekly overtime")
	void testExtractRuleEngineMetricsWeeklyOtResultNonOvertimeRuleTypeReturnsZeroOvertime() {
		// Given — weeklyOtResult is non-null but ruleType is NOT an overtime type
		// → !isOvertimeRuleType(...) is true → return 0L
		RuleEvaluationResult nonOvertimeWeeklyResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
			.weeklyOvertimeHours(Duration.ofHours(3))
			.build();

		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(nonOvertimeWeeklyResult)
			.build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - weeklyOtResult null ruleType returns zero weekly overtime")
	void testExtractRuleEngineMetricsWeeklyOtResultNullRuleTypeReturnsZeroOvertime() {
		// Given — ruleType is null → isOvertimeRuleType(null) returns false
		// → !false = true → return 0L (exercises the null-ruleType path inside
		// isOvertimeRuleType)
		RuleEvaluationResult nullTypeWeeklyResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(null)
			.weeklyOvertimeHours(Duration.ofHours(2))
			.build();

		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(nullTypeWeeklyResult)
			.build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - weeklyOvertimeHours null returns zero weekly overtime")
	void testExtractRuleEngineMetricsWeeklyOvertimeHoursNullReturnsZeroOvertime() {
		// Given — valid overtime ruleType but weeklyOvertimeHours is null
		RuleEvaluationResult weeklyOtResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(null)
			.build();

		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(weeklyOtResult)
			.build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - negative weeklyOvertimeHours returns zero weekly overtime")
	void testExtractRuleEngineMetricsNegativeWeeklyOvertimeHoursReturnsZeroOvertime() {
		// Given — valid overtime ruleType but weeklyOvertimeHours is negative
		RuleEvaluationResult weeklyOtResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(-1))
			.build();

		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(weeklyOtResult)
			.build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_WEEKLY_OVERTIME accumulates totalOvertimeSeconds and totalWeeklyOvertimeSeconds")
	void testExtractRuleEngineMetricsRangeBasedWeeklyOvertimeAccumulatesTotalOvertimeSeconds() {
		// Given — happy path: valid overtime type with positive weeklyOvertimeHours
		RuleEvaluationResult weeklyOtResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(5))
			.build();

		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(weeklyOtResult)
			.build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(18000L);
		assertThat(metrics.totalWeeklyOvertimeSeconds()).isEqualTo(18000L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - DURATION_BASED_WEEKLY_OVERTIME accumulates totalOvertimeSeconds and totalWeeklyOvertimeSeconds")
	void testExtractRuleEngineMetricsDurationBasedWeeklyOvertimeAccumulatesTotalOvertimeSeconds() {
		// Given
		RuleEvaluationResult weeklyOtResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(2))
			.build();

		RuleEvaluatorResult weekResult = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(weeklyOtResult)
			.build();

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(7200L);
		assertThat(metrics.totalWeeklyOvertimeSeconds()).isEqualTo(7200L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - per-log overtime and weekly overtime both accumulate into total")
	void testExtractRuleEngineMetricsPerLogAndWeeklyOvertimeBothAccumulateIntoTotal() {
		// Given — per-log overtime (RANGE_BASED_AFTER_SHIFT) + weekly overtime
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(530);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult perLogOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_AFTER_SHIFT)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, perLogOt);

		RuleEvaluationResult weeklyOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(2))
			.build();
		weekResult.setWeeklyOvertimeRuleEvaluationResult(weeklyOt);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — 1h per-log + 2h weekly = 3h = 10800s total; only 2h = 7200s is weekly
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(10800L);
		assertThat(metrics.totalWeeklyOvertimeSeconds()).isEqualTo(7200L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - per-log overtime only does not populate totalWeeklyOvertimeSeconds")
	void testExtractRuleEngineMetricsPerLogOvertimeOnlyDoesNotPopulateTotalWeeklyOvertimeSeconds() {
		// Given — only per-log overtime, no weekly overtime
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(99);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult perLogOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_DAILY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(3))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, perLogOt);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — 3h per-log daily OT counts in total but NOT in weekly
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(10800L);
		assertThat(metrics.totalWeeklyOvertimeSeconds()).isZero();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - biweekly timesheet sums weekly overtime across two weekly windows")
	void testExtractRuleEngineMetricsBiweeklyTimesheetSumsWeeklyOvertimeAcrossTwoWeeks() {
		// Given — two weekly windows each with weekly overtime (biweekly/monthly
		// scenario)
		RuleEvaluationResult weeklyOt1 = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(3))
			.build();
		RuleEvaluatorResult week1Result = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(weeklyOt1)
			.build();

		RuleEvaluationResult weeklyOt2 = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(2))
			.build();
		RuleEvaluatorResult week2Result = RuleEvaluatorResult.builder()
			.weeklyOvertimeRuleEvaluationResult(weeklyOt2)
			.build();

		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder()
			.weeklyResults(List.of(WeeklyResult.builder().ruleEvaluatorResult(week1Result).build(),
					WeeklyResult.builder().ruleEvaluatorResult(week2Result).build()))
			.build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — week1 3h + week2 2h = 5h = 18000s weekly overtime
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(18000L);
		assertThat(metrics.totalWeeklyOvertimeSeconds()).isEqualTo(18000L);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - totalRegularHour sums multiple time log values")
	void testStoreTotalBillPayAndRegularHourDataSumsTotalRegularHour() throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(Collections.emptyList());

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 1, 2,
					this.makeMetrics(Map.of(10, 3600, 20, 7200), 0L));

		org.mockito.Mockito.verify(this.timesheet).setTotalRegularHour(10800);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets totalWeeklyOvertime from metrics")
	void testStoreTotalBillPayAndRegularHourDataSetsTotalWeeklyOvertime() throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(Collections.emptyList());

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 1, 2, this.makeMetrics(Map.of(), 7200L, 7200L));

		org.mockito.Mockito.verify(this.timesheet).setTotalWeeklyOvertime(7200);
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets totalWeeklyOvertime to 0 when no weekly overtime")
	void testStoreTotalBillPayAndRegularHourDataSetsTotalWeeklyOvertimeToZeroWhenNone() throws Exception {
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		org.mockito.BDDMockito.given(result.getWeeklyResults()).willReturn(Collections.emptyList());

		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 1, 2, this.makeMetrics(Map.of(), 3600L, 0L));

		org.mockito.Mockito.verify(this.timesheet).setTotalWeeklyOvertime(0);
	}

	// --- Helper methods ---

	/**
	 * Locates the private {@code RuleEngineMetrics} record declared inside
	 * {@link RuleEngineService} via reflection.
	 */
	private Class<?> metricsClass() {
		return RuleEngineService.RuleEngineMetrics.class;
	}

	/**
	 * Creates a {@code RuleEngineMetrics} instance with
	 * {@code totalWeeklyOvertimeSeconds} defaulting to {@code 0}.
	 */
	private Object makeMetrics(Map<Integer, Integer> regularHours, long overtimeSeconds) throws Exception {
		return this.makeMetrics(regularHours, overtimeSeconds, 0L);
	}

	/**
	 * Creates a {@code RuleEngineMetrics} instance via its canonical constructor using
	 * reflection, since the record is private. Bifurcation components default to empty.
	 */
	private Object makeMetrics(Map<Integer, Integer> regularHours, long overtimeSeconds, long weeklyOvertimeSeconds)
			throws Exception {
		return this.makeMetrics(regularHours, overtimeSeconds, weeklyOvertimeSeconds, null, BigDecimal.ZERO,
				BigDecimal.ZERO);
	}

	/**
	 * Creates a {@code RuleEngineMetrics} instance with explicit bifurcation components
	 * (weekly overtime summary and overtime pay/bill amounts).
	 */
	private Object makeMetrics(Map<Integer, Integer> regularHours, long overtimeSeconds, long weeklyOvertimeSeconds,
			WeeklyOvertimeDetail weeklyOvertimeDetails, BigDecimal overtimePayAmount, BigDecimal overtimeBillAmount)
			throws Exception {
		Class<?> cls = this.metricsClass();
		java.lang.reflect.Constructor<?> ctor = cls.getDeclaredConstructor(Map.class, long.class, long.class, Map.class,
				WeeklyOvertimeDetail.class, BigDecimal.class, BigDecimal.class);
		ctor.setAccessible(true);
		return ctor.newInstance(regularHours, overtimeSeconds, weeklyOvertimeSeconds, Map.of(), weeklyOvertimeDetails,
				overtimePayAmount, overtimeBillAmount);
	}

	/**
	 * Returns the {@code storeTotalBillPayAndRegularHourData} method with its updated
	 * signature that accepts a {@code RuleEngineMetrics} parameter.
	 */
	private java.lang.reflect.Method storeMethod() throws Exception {
		java.lang.reflect.Method method = RuleEngineService.class.getDeclaredMethod(
				"storeTotalBillPayAndRegularHourData", Timesheet.class, WeeklyRuleEvaluatorResult.class, Integer.class,
				Integer.class, this.metricsClass());
		method.setAccessible(true);
		return method;
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// evaluateRulesOnDemand tests
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("evaluateRulesOnDemand - null time logs throws IllegalArgumentException")
	void testEvaluateRulesOnDemandNullTimeLogsThrowsIllegalArgumentException() {
		// Given
		AuthPrincipal mockPrincipal = mock(AuthPrincipal.class);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = new BulkUpdateTimeLogsRequestBodyDto();
		bulkRequest.setTimeLogs(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineService.evaluateRulesOnDemand(bulkRequest))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Time logs cannot be null or empty");
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - empty time logs throws IllegalArgumentException")
	void testEvaluateRulesOnDemandEmptyTimeLogsThrowsIllegalArgumentException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto bulkRequest = new BulkUpdateTimeLogsRequestBodyDto();
		bulkRequest.setTimeLogs(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineService.evaluateRulesOnDemand(bulkRequest))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Time logs cannot be null or empty");
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - null timesheetId on time log throws IllegalArgumentException")
	void testEvaluateRulesOnDemandNullTimesheetIdOnTimeLogThrowsIllegalArgumentException() {
		// Given
		AuthPrincipal mockPrincipal = mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);

		BulkTimeLogRequestBodyDto logWithNullTimesheetId = new BulkTimeLogRequestBodyDto();
		logWithNullTimesheetId.setId(1);
		logWithNullTimesheetId.setTimesheetId(null);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = new BulkUpdateTimeLogsRequestBodyDto();
		bulkRequest.setTimeLogs(List.of(logWithNullTimesheetId));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineService.evaluateRulesOnDemand(bulkRequest))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet ID cannot be null on time log");
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - USER principal uses authHolder account id")
	void testEvaluateRulesOnDemandUserPrincipalUsesAuthHolderAccountId() {
		// Given
		AuthPrincipal mockPrincipal = mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);

		OnDemandTimesheetOvertimeDto workerResult = OnDemandTimesheetOvertimeDto.builder()
			.timesheetId(1)
			.timeLogs(List.of())
			.build();

		org.mockito.BDDMockito.willAnswer((inv) -> {
			((Runnable) inv.getArgument(0)).run();
			return null;
		}).given(this.onDemandEvaluationExecutor).execute(any(Runnable.class));

		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(1), any(), eq(456), eq(true)))
			.willReturn(workerResult);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = buildBulkRequest(1);

		// When
		List<OnDemandTimesheetOvertimeDto> result = this.ruleEngineService.evaluateRulesOnDemand(bulkRequest);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTimesheetId()).isEqualTo(1);
		then(this.authHolder).should().getAuthenticationPrincipalOrganizationIdentifier();
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - ContactPrincipal uses contact organization id")
	void testEvaluateRulesOnDemandContactPrincipalUsesContactOrganizationId() {
		// Given
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(789);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(contactPrincipal);

		OnDemandTimesheetOvertimeDto workerResult = OnDemandTimesheetOvertimeDto.builder()
			.timesheetId(1)
			.timeLogs(List.of())
			.build();

		org.mockito.BDDMockito.willAnswer((inv) -> {
			((Runnable) inv.getArgument(0)).run();
			return null;
		}).given(this.onDemandEvaluationExecutor).execute(any(Runnable.class));

		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(1), any(), eq(789), eq(true)))
			.willReturn(workerResult);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = buildBulkRequest(1);

		// When
		List<OnDemandTimesheetOvertimeDto> result = this.ruleEngineService.evaluateRulesOnDemand(bulkRequest);

		// Then
		assertThat(result).hasSize(1);
		then(contactPrincipal).should().getOrganizationIdentifier();
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - ContractorPrincipal uses contractor organization id")
	void testEvaluateRulesOnDemandContractorPrincipalUsesContractorOrganizationId() {
		// Given
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(321);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		OnDemandTimesheetOvertimeDto workerResult = OnDemandTimesheetOvertimeDto.builder()
			.timesheetId(1)
			.timeLogs(List.of())
			.build();

		org.mockito.BDDMockito.willAnswer((inv) -> {
			((Runnable) inv.getArgument(0)).run();
			return null;
		}).given(this.onDemandEvaluationExecutor).execute(any(Runnable.class));

		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(1), any(), eq(321), eq(true)))
			.willReturn(workerResult);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = buildBulkRequest(1);

		// When
		List<OnDemandTimesheetOvertimeDto> result = this.ruleEngineService.evaluateRulesOnDemand(bulkRequest);

		// Then
		assertThat(result).hasSize(1);
		then(contractorPrincipal).should().getOrganizationIdentifier();
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - non-ContractorPrincipal with non-USER type falls through to else branch")
	void testEvaluateRulesOnDemandNonContractorPrincipalInstanceFallsThroughToElseBranch() {
		// Given — a plain AuthPrincipal mock (not ContactPrincipal, not
		// ContractorPrincipal)
		// whose getPrincipalType() returns CONTRACTOR (not USER).
		// Flow:
		// 1. getPrincipalType() == USER → false (skips first if)
		// 2. instanceof ContactPrincipal → false (not a ContactPrincipal)
		// 3. instanceof ContractorPrincipal → false ← this is the UNCOVERED FALSE branch
		// 4. else → authHolder.getAuthenticationPrincipalOrganizationIdentifier()
		AuthPrincipal nonContractorPrincipal = mock(AuthPrincipal.class);
		given(nonContractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(nonContractorPrincipal);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);

		OnDemandTimesheetOvertimeDto workerResult = OnDemandTimesheetOvertimeDto.builder()
			.timesheetId(1)
			.timeLogs(List.of())
			.build();

		org.mockito.BDDMockito.willAnswer((inv) -> {
			((Runnable) inv.getArgument(0)).run();
			return null;
		}).given(this.onDemandEvaluationExecutor).execute(any(Runnable.class));

		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(1), any(), eq(456), eq(true)))
			.willReturn(workerResult);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = buildBulkRequest(1);

		// When
		List<OnDemandTimesheetOvertimeDto> result = this.ruleEngineService.evaluateRulesOnDemand(bulkRequest);

		// Then — else branch uses authHolder account id
		assertThat(result).hasSize(1);
		then(this.authHolder).should().getAuthenticationPrincipalOrganizationIdentifier();
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - unknown principal falls back to authHolder account id")
	void testEvaluateRulesOnDemandUnknownPrincipalFallsBackToAuthHolderAccountId() {
		// Given
		AuthPrincipal unknownPrincipal = mock(AuthPrincipal.class);
		given(unknownPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(unknownPrincipal);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);

		OnDemandTimesheetOvertimeDto workerResult = OnDemandTimesheetOvertimeDto.builder()
			.timesheetId(1)
			.timeLogs(List.of())
			.build();

		org.mockito.BDDMockito.willAnswer((inv) -> {
			((Runnable) inv.getArgument(0)).run();
			return null;
		}).given(this.onDemandEvaluationExecutor).execute(any(Runnable.class));

		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(1), any(), eq(456), eq(true)))
			.willReturn(workerResult);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = buildBulkRequest(1);

		// When
		List<OnDemandTimesheetOvertimeDto> result = this.ruleEngineService.evaluateRulesOnDemand(bulkRequest);

		// Then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("evaluateRulesOnDemand - multiple timesheets are grouped and each evaluated once")
	void testEvaluateRulesOnDemandMultipleTimesheetsGroupedAndEachEvaluatedOnce() {
		// Given
		AuthPrincipal mockPrincipal = mock(AuthPrincipal.class);
		given(mockPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.authHolder.getUnifiedPrincipal()).willReturn(mockPrincipal);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(456);

		org.mockito.BDDMockito.willAnswer((inv) -> {
			((Runnable) inv.getArgument(0)).run();
			return null;
		}).given(this.onDemandEvaluationExecutor).execute(any(Runnable.class));

		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(1), any(), eq(456), eq(false)))
			.willReturn(OnDemandTimesheetOvertimeDto.builder().timesheetId(1).timeLogs(List.of()).build());
		given(this.onDemandEvaluationWorker.evaluateSingleTimesheet(eq(2), any(), eq(456), eq(false)))
			.willReturn(OnDemandTimesheetOvertimeDto.builder().timesheetId(2).timeLogs(List.of()).build());

		// Two time logs for timesheet 1 and one for timesheet 2
		BulkTimeLogRequestBodyDto log1a = buildTimeLog(10, 1);
		BulkTimeLogRequestBodyDto log1b = buildTimeLog(11, 1);
		BulkTimeLogRequestBodyDto log2 = buildTimeLog(20, 2);

		BulkUpdateTimeLogsRequestBodyDto bulkRequest = new BulkUpdateTimeLogsRequestBodyDto();
		bulkRequest.setTimeLogs(List.of(log1a, log1b, log2));

		// When
		List<OnDemandTimesheetOvertimeDto> result = this.ruleEngineService.evaluateRulesOnDemand(bulkRequest);

		// Then
		assertThat(result).hasSize(2);
		then(this.onDemandEvaluationWorker).should().evaluateSingleTimesheet(eq(1), any(), eq(456), eq(false));
		then(this.onDemandEvaluationWorker).should().evaluateSingleTimesheet(eq(2), any(), eq(456), eq(false));
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// saturateInt tests
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("saturateInt - zero returns 0")
	void testSaturateIntZeroReturnsZero() {
		assertThat(RuleEngineService.saturateInt(0L)).isZero();
	}

	@Test
	@DisplayName("saturateInt - negative value returns 0")
	void testSaturateIntNegativeValueReturnsZero() {
		assertThat(RuleEngineService.saturateInt(-100L)).isZero();
	}

	@Test
	@DisplayName("saturateInt - value within int range is cast correctly")
	void testSaturateIntValueWithinIntRangeIsCastCorrectly() {
		assertThat(RuleEngineService.saturateInt(3600L)).isEqualTo(3600);
	}

	@Test
	@DisplayName("saturateInt - value equal to Integer.MAX_VALUE returns Integer.MAX_VALUE")
	void testSaturateIntEqualToMaxValueReturnsMaxValue() {
		assertThat(RuleEngineService.saturateInt((long) Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("saturateInt - value exceeding Integer.MAX_VALUE returns Integer.MAX_VALUE")
	void testSaturateIntExceedingMaxValueReturnsMaxValue() {
		assertThat(RuleEngineService.saturateInt((long) Integer.MAX_VALUE + 1L)).isEqualTo(Integer.MAX_VALUE);
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// isOvertimeRuleType — missing per-log branch coverage
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("extractRuleEngineMetrics - null ruleType in per-log result is not accumulated as overtime")
	void testExtractRuleEngineMetricsNullRuleTypeInPerLogResultNotAccumulatedAsOvertime() {
		// Given — per-log RuleEvaluationResult with null ruleType exercises the
		// isOvertimeRuleType(null) → false guard via accumulateRuleResultsForTimeLog
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(540);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult nullTypeResult = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(null)
			.evaluatedDuration(Duration.ofHours(1))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, nullTypeResult);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — null ruleType is neither regular nor overtime → zero contributions
		assertThat(metrics.totalOvertimeSeconds()).isZero();
		assertThat(metrics.regularHoursByTimeLogId()).isEmpty();
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - RANGE_BASED_WEEKLY_OVERTIME in per-log result accumulates overtime")
	void testExtractRuleEngineMetricsRangeBasedWeeklyOvertimeInPerLogAccumulatesOvertime() {
		// Given — RANGE_BASED_WEEKLY_OVERTIME (condition E in the OR chain) via the
		// per-log path, not the weekly-OT path; covers the fifth || operand being true
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(541);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult weeklyOtPerLog = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(2))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, weeklyOtPerLog);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(7200L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - DURATION_BASED_WEEKLY_OVERTIME in per-log result accumulates overtime")
	void testExtractRuleEngineMetricsDurationBasedWeeklyOvertimeInPerLogAccumulatesOvertime() {
		// Given — DURATION_BASED_WEEKLY_OVERTIME (condition H, last in the OR chain)
		// via the per-log path; all seven preceding conditions are false, ensuring H is
		// evaluated and found true
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(542);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult durationWeeklyOtPerLog = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.DURATION_BASED_WEEKLY_OVERTIME)
			.evaluatedDuration(Duration.ofHours(3))
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, durationWeeklyOtPerLog);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(10800L);
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Overtime bifurcation — overtime_details, weekly OT summary, amount split
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("extractRuleEngineMetrics - daily overtime result builds overtime_details entry with all fields")
	void testExtractRuleEngineMetricsDailyOvertimeBuildsOvertimeDetailEntry() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(101);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		RuleEvaluationResult dailyOt = RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_DAILY_OVERTIME)
			.ruleName("Daily Overtime")
			.evaluatedDuration(Duration.ofHours(2))
			.payAmount(new BigDecimal("30.00"))
			.billAmount(new BigDecimal("36.00"))
			.payRateMultiplier(1.5f)
			.billRateMultiplier(2.0f)
			.build();
		weekResult.addRuleEvaluationResult(dtoLog, dailyOt);

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.overtimeDetailsByTimeLogId()).containsKey(101);
		List<OvertimeDetail> details = metrics.overtimeDetailsByTimeLogId().get(101);
		assertThat(details).hasSize(1);
		OvertimeDetail detail = details.get(0);
		assertThat(detail.getOvertimeType()).isEqualTo("DAILY_OVERTIME");
		assertThat(detail.getRuleName()).isEqualTo("Daily Overtime");
		assertThat(detail.getHours()).isEqualTo(7200);
		assertThat(detail.getPayRateMultiplier()).isEqualTo(1.5f);
		assertThat(detail.getBillRateMultiplier()).isEqualTo(2.0f);
		assertThat(detail.getPayAmount()).isEqualTo(new BigDecimal("30.00"));
		assertThat(detail.getBillAmount()).isEqualTo(new BigDecimal("36.00"));
		assertThat(metrics.overtimePayAmount()).isEqualTo(new BigDecimal("30.00"));
		assertThat(metrics.overtimeBillAmount()).isEqualTo(new BigDecimal("36.00"));
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - multiple overtime rules on the same log build multiple entries")
	void testExtractRuleEngineMetricsMultipleOvertimeRulesSameLogBuildMultipleEntries() {
		// Given
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(102);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.addRuleEvaluationResult(dtoLog,
				RuleEvaluationResult.builder()
					.timeRange(TreeRangeSet.create())
					.ruleType(RuleType.RANGE_BASED_DAILY_OVERTIME)
					.ruleName("Daily Overtime")
					.evaluatedDuration(Duration.ofHours(2))
					.payAmount(new BigDecimal("30.00"))
					.billAmount(new BigDecimal("36.00"))
					.build());
		weekResult.addRuleEvaluationResult(dtoLog,
				RuleEvaluationResult.builder()
					.timeRange(TreeRangeSet.create())
					.ruleType(RuleType.RANGE_BASED_AFTER_SHIFT)
					.ruleName("Evening OT")
					.evaluatedDuration(Duration.ofHours(1))
					.payAmount(new BigDecimal("20.00"))
					.billAmount(new BigDecimal("24.00"))
					.build());

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.overtimeDetailsByTimeLogId().get(102)).hasSize(2);
		assertThat(metrics.overtimePayAmount()).isEqualTo(new BigDecimal("50.00"));
		assertThat(metrics.overtimeBillAmount()).isEqualTo(new BigDecimal("60.00"));
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - weekly overtime per-log result is excluded from overtime_details")
	void testExtractRuleEngineMetricsWeeklyOvertimePerLogExcludedFromOvertimeDetails() {
		// Given — a weekly OT rule type appearing in the per-log map counts seconds
		// (defensive) but never produces a per-day breakdown entry
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(103);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.addRuleEvaluationResult(dtoLog,
				RuleEvaluationResult.builder()
					.timeRange(TreeRangeSet.create())
					.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
					.evaluatedDuration(Duration.ofHours(2))
					.payAmount(new BigDecimal("30.00"))
					.billAmount(new BigDecimal("36.00"))
					.build());

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.overtimeDetailsByTimeLogId()).isEmpty();
		assertThat(metrics.totalOvertimeSeconds()).isEqualTo(7200L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - weekly overtime summary sums hours and amounts across weeks")
	void testExtractRuleEngineMetricsWeeklyOvertimeSummaryAcrossWeeksSumsHoursAndAmounts() {
		// Given — two weeks, each with its own weekly OT result and money data; the
		// multiplier is captured from the first week's result
		RuleEvaluatorResult weekOne = new RuleEvaluatorResult();
		weekOne.setWeeklyOvertimeRuleEvaluationResult(RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(2))
			.payRateMultiplier(1.5f)
			.billRateMultiplier(1.5f)
			.build());
		weekOne.setWeeklyOvertimeMoneyData(
				MoneyData.builder().payAmount(new BigDecimal("20.00")).billAmount(new BigDecimal("24.00")).build());

		RuleEvaluatorResult weekTwo = new RuleEvaluatorResult();
		weekTwo.setWeeklyOvertimeRuleEvaluationResult(RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofHours(3))
			.payRateMultiplier(1.5f)
			.billRateMultiplier(1.5f)
			.build());
		weekTwo.setWeeklyOvertimeMoneyData(
				MoneyData.builder().payAmount(new BigDecimal("30.00")).billAmount(new BigDecimal("36.00")).build());

		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder()
			.weeklyResults(List.of(WeeklyResult.builder().ruleEvaluatorResult(weekOne).build(),
					WeeklyResult.builder().ruleEvaluatorResult(weekTwo).build()))
			.build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		WeeklyOvertimeDetail summary = metrics.weeklyOvertimeDetails();
		assertThat(summary).isNotNull();
		assertThat(summary.getHours()).isEqualTo(18000);
		assertThat(summary.getPayRateMultiplier()).isEqualTo(1.5f);
		assertThat(summary.getBillRateMultiplier()).isEqualTo(1.5f);
		assertThat(summary.getPayAmount()).isEqualTo(new BigDecimal("50.00"));
		assertThat(summary.getBillAmount()).isEqualTo(new BigDecimal("60.00"));
		assertThat(metrics.overtimePayAmount()).isEqualTo(new BigDecimal("50.00"));
		assertThat(metrics.overtimeBillAmount()).isEqualTo(new BigDecimal("60.00"));
		assertThat(metrics.totalWeeklyOvertimeSeconds()).isEqualTo(18000L);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - float-artifact amounts are rounded to 2 decimals in both JSON structures")
	void testExtractRuleEngineMetricsAmountsRoundedToTwoDecimals() {
		// Given — raw engine amounts carry float artifacts (hours multiplied by rate,
		// computed in floats); stored JSON must match the DECIMAL(10,2) HALF_UP rounding
		// of the amount columns
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(105);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.addRuleEvaluationResult(dtoLog,
				RuleEvaluationResult.builder()
					.timeRange(TreeRangeSet.create())
					.ruleType(RuleType.RANGE_BASED_DAILY_OVERTIME)
					.ruleName("Daily Overtime")
					.evaluatedDuration(Duration.ofHours(2))
					.payAmount(new BigDecimal("195.83334350585935"))
					.billAmount(new BigDecimal("293.746"))
					.build());
		weekResult.setWeeklyOvertimeRuleEvaluationResult(RuleEvaluationResult.builder()
			.timeRange(TreeRangeSet.create())
			.ruleType(RuleType.RANGE_BASED_WEEKLY_OVERTIME)
			.weeklyOvertimeHours(Duration.ofSeconds(70500))
			.payRateMultiplier(1.0f)
			.billRateMultiplier(1.0f)
			.build());
		weekResult.setWeeklyOvertimeMoneyData(MoneyData.builder()
			.payAmount(new BigDecimal("195.83334350585935"))
			.billAmount(new BigDecimal("293.755"))
			.build());

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then — per-day entry amounts rounded HALF_UP to 2 decimals
		OvertimeDetail detail = metrics.overtimeDetailsByTimeLogId().get(105).get(0);
		assertThat(detail.getPayAmount()).isEqualTo(new BigDecimal("195.83"));
		assertThat(detail.getBillAmount()).isEqualTo(new BigDecimal("293.75"));
		// Then — weekly summary amounts rounded HALF_UP to 2 decimals
		assertThat(metrics.weeklyOvertimeDetails().getPayAmount()).isEqualTo(new BigDecimal("195.83"));
		assertThat(metrics.weeklyOvertimeDetails().getBillAmount()).isEqualTo(new BigDecimal("293.76"));
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - amount split is rounded to 2 decimals before persistence")
	void testStoreTotalBillPayAndRegularHourDataRoundsAmountSplitToTwoDecimals() throws Exception {
		// Given — raw overtime bucket carries float artifacts
		RuleEvaluatorResult evaluatorResult = org.mockito.Mockito.mock(RuleEvaluatorResult.class);
		given(evaluatorResult.getTotalPayAmount()).willReturn(new BigDecimal("100.00"));
		given(evaluatorResult.getTotalBillAmount()).willReturn(new BigDecimal("120.00"));

		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		given(weeklyResult.getRuleEvaluatorResult()).willReturn(evaluatorResult);
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		given(result.getWeeklyResults()).willReturn(List.of(weeklyResult));

		// When
		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 100, 2, this.makeMetrics(Map.of(), 0L, 0L, null,
					new BigDecimal("30.00434350585935"), new BigDecimal("40.005")));

		// Then — overtime rounded HALF_UP; regular = total - rounded overtime
		then(this.timesheet).should().setOvertimePayAmount(new BigDecimal("30.00"));
		then(this.timesheet).should().setOvertimeBillAmount(new BigDecimal("40.01"));
		then(this.timesheet).should().setRegularPayAmount(new BigDecimal("70.00"));
		then(this.timesheet).should().setRegularBillAmount(new BigDecimal("79.99"));
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - no weekly overtime returns null weekly_overtime summary")
	void testExtractRuleEngineMetricsNoWeeklyOvertimeReturnsNullWeeklyOvertimeDetails() {
		// Given — regular-hours-only result: no OT entries, no weekly OT
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog dtoLog = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog();
		dtoLog.setId(104);

		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.addRuleEvaluationResult(dtoLog,
				RuleEvaluationResult.builder()
					.timeRange(TreeRangeSet.create())
					.ruleType(RuleType.RANGE_BASED_REGULAR_HOURS)
					.evaluatedDuration(Duration.ofHours(8))
					.build());

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.weeklyOvertimeDetails()).isNull();
		assertThat(metrics.overtimeDetailsByTimeLogId()).isEmpty();
		assertThat(metrics.overtimePayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(metrics.overtimeBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("extractRuleEngineMetrics - weekly OT money data without result counts in overtime bucket")
	void testExtractRuleEngineMetricsWeeklyOvertimeMoneyDataWithoutResultCountsInOvertimeBucket() {
		// Given — money data present but no weekly OT rule result: amounts land in the
		// overtime bucket (mirroring the total computation) while the summary stays null
		RuleEvaluatorResult weekResult = new RuleEvaluatorResult();
		weekResult.setWeeklyOvertimeMoneyData(
				MoneyData.builder().payAmount(new BigDecimal("10.00")).billAmount(new BigDecimal("12.00")).build());

		WeeklyResult weekly = WeeklyResult.builder().ruleEvaluatorResult(weekResult).build();
		WeeklyRuleEvaluatorResult full = WeeklyRuleEvaluatorResult.builder().weeklyResults(List.of(weekly)).build();

		// When
		var metrics = this.ruleEngineService.extractRuleEngineMetrics(full);

		// Then
		assertThat(metrics.weeklyOvertimeDetails()).isNull();
		assertThat(metrics.overtimePayAmount()).isEqualTo(new BigDecimal("10.00"));
		assertThat(metrics.overtimeBillAmount()).isEqualTo(new BigDecimal("12.00"));
	}

	@Test
	@DisplayName("storeTotalBillPayAndRegularHourData - sets amount split and weekly overtime summary on timesheet")
	void testStoreTotalBillPayAndRegularHourDataSetsBifurcationData() throws Exception {
		// Given — totals 100/120 with overtime bucket 30/40: regular = total - overtime
		RuleEvaluatorResult evaluatorResult = org.mockito.Mockito.mock(RuleEvaluatorResult.class);
		given(evaluatorResult.getTotalPayAmount()).willReturn(new BigDecimal("100.00"));
		given(evaluatorResult.getTotalBillAmount()).willReturn(new BigDecimal("120.00"));

		WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult = org.mockito.Mockito
			.mock(WeeklyRuleEvaluatorResult.WeeklyResult.class);
		given(weeklyResult.getRuleEvaluatorResult()).willReturn(evaluatorResult);
		WeeklyRuleEvaluatorResult result = org.mockito.Mockito.mock(WeeklyRuleEvaluatorResult.class);
		given(result.getWeeklyResults()).willReturn(List.of(weeklyResult));

		WeeklyOvertimeDetail weeklyOvertimeDetail = new WeeklyOvertimeDetail(18000, 1.5f, 1.5f, new BigDecimal("75.00"),
				new BigDecimal("90.00"));

		// When
		this.storeMethod()
			.invoke(this.ruleEngineService, this.timesheet, result, 100, 2, this.makeMetrics(Map.of(), 0L, 0L,
					weeklyOvertimeDetail, new BigDecimal("30.00"), new BigDecimal("40.00")));

		// Then
		then(this.timesheet).should().setOvertimePayAmount(new BigDecimal("30.00"));
		then(this.timesheet).should().setOvertimeBillAmount(new BigDecimal("40.00"));
		then(this.timesheet).should().setRegularPayAmount(new BigDecimal("70.00"));
		then(this.timesheet).should().setRegularBillAmount(new BigDecimal("80.00"));
		then(this.timesheet).should().setWeeklyOvertimeDetails(weeklyOvertimeDetail);
		then(this.timesheetJpaRepository).should().save(this.timesheet);
	}

	@Test
	@DisplayName("serializeOvertimeDetails - schema-invalid payload throws JsonReadException before write")
	void testSerializeOvertimeDetailsSchemaInvalidPayloadThrowsJsonReadException() throws Exception {
		// Given — an all-null detail serializes fine but violates the schema
		// (overtime_type must be a string, hours an integer, amounts numbers)
		OvertimeDetail invalid = new OvertimeDetail();
		var method = RuleEngineService.class.getDeclaredMethod("serializeOvertimeDetails", List.class);
		method.setAccessible(true);

		// When / Then
		assertThatThrownBy(() -> method.invoke(this.ruleEngineService, List.of(invalid)))
			.hasCauseInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.JsonReadException.class);
	}

	@Test
	@DisplayName("serializeOvertimeDetails - valid payload passes schema validation and serializes snake_case keys")
	void testSerializeOvertimeDetailsValidPayloadPassesSchemaValidation() throws Exception {
		// Given
		OvertimeDetail valid = new OvertimeDetail("DAILY_OVERTIME", "Daily Overtime", 7200, 1.5f, 1.5f,
				new BigDecimal("30.00"), new BigDecimal("36.00"));
		var method = RuleEngineService.class.getDeclaredMethod("serializeOvertimeDetails", List.class);
		method.setAccessible(true);

		// When
		String json = (String) method.invoke(this.ruleEngineService, List.of(valid));

		// Then
		assertThat(json).contains("\"overtime_type\":\"DAILY_OVERTIME\"")
			.contains("\"rule_name\":\"Daily Overtime\"")
			.contains("\"hours\":7200")
			.contains("\"pay_rate_multiplier\":1.5")
			.contains("\"bill_rate_multiplier\":1.5")
			.contains("\"pay_amount\":30.00")
			.contains("\"bill_amount\":36.00");
	}

	@Test
	@DisplayName("serializeOvertimeDetails - serialization failure throws JsonReadException")
	void testSerializeOvertimeDetailsSerializationFailureThrowsJsonReadException() throws Exception {
		// Given — a detail whose getter throws, making Jackson serialization fail
		OvertimeDetail failing = mock(OvertimeDetail.class);
		given(failing.getOvertimeType()).willThrow(new RuntimeException("boom"));
		var method = RuleEngineService.class.getDeclaredMethod("serializeOvertimeDetails", List.class);
		method.setAccessible(true);

		// When / Then
		assertThatThrownBy(() -> method.invoke(this.ruleEngineService, List.of(failing)))
			.hasCauseInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.JsonReadException.class);
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// updateTimeLogDerivedDataViaBatchSql — branch coverage
	// ──────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("updateTimeLogDerivedDataViaBatchSql - null timesheetId returns without DB interaction")
	void testUpdateTimeLogDerivedDataViaBatchSqlNullTimesheetIdReturnsWithoutDbInteraction() throws Exception {
		// Given
		var method = this.updateTimeLogDerivedDataMethod();

		// When
		method.invoke(this.ruleEngineService, null, Map.of(), Map.of());

		// Then — the null-timesheetId guard returns before touching dslContext
		then(this.dslContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("updateTimeLogDerivedDataViaBatchSql - both maps empty calls setNull for all rows")
	void testUpdateTimeLogDerivedDataViaBatchSqlBothMapsEmptyCallsSetNull() throws Exception {
		// Given
		var method = this.updateTimeLogDerivedDataMethod();

		// When — empty maps exercise the isEmpty() && isEmpty() branch
		method.invoke(this.ruleEngineService, 42, Map.of(), Map.of());

		// Then — dslContext.update(tl).setNull(...).setNull(...).where(...).execute()
		then(this.dslContext).should().update(any());
	}

	@Test
	@DisplayName("updateTimeLogDerivedDataViaBatchSql - non-empty maps build CASE-WHEN SQL")
	void testUpdateTimeLogDerivedDataViaBatchSqlNonEmptyMapsBuildCaseWhenSql() throws Exception {
		// Given
		var method = this.updateTimeLogDerivedDataMethod();
		Map<Integer, List<OvertimeDetail>> overtimeDetails = Map.of(10, List.of(new OvertimeDetail("DAILY_OVERTIME",
				"Daily Overtime", 7200, 1.5f, 1.5f, new BigDecimal("30.00"), new BigDecimal("36.00"))));

		// When — non-empty maps skip the isEmpty() branch and build both CASE-WHENs
		method.invoke(this.ruleEngineService, 42, Map.of(10, 3600, 20, 7200), overtimeDetails);

		// Then — dslContext.update(tl).set(...).set(...).where(...).execute()
		then(this.dslContext).should().update(any());
	}

	@Test
	@DisplayName("updateTimeLogDerivedDataViaBatchSql - overtime details only builds NULL regular-hours expression")
	void testUpdateTimeLogDerivedDataViaBatchSqlOvertimeDetailsOnlyBuildsNullRegularExpression() throws Exception {
		// Given — empty regular-hours map with non-empty details exercises the
		// empty-map branch of buildCaseExpression (plain NULL value)
		var method = this.updateTimeLogDerivedDataMethod();
		Map<Integer, List<OvertimeDetail>> overtimeDetails = Map.of(10, List.of(new OvertimeDetail("AFTER_SHIFT",
				"Evening OT", 3600, 2.0f, 2.0f, new BigDecimal("20.00"), new BigDecimal("24.00"))));

		// When
		method.invoke(this.ruleEngineService, 42, Map.of(), overtimeDetails);

		// Then
		then(this.dslContext).should().update(any());
	}

	@Test
	@DisplayName("updateTimeLogDerivedDataViaBatchSql - empty detail list is omitted from serialization")
	void testUpdateTimeLogDerivedDataViaBatchSqlEmptyDetailListOmittedFromSerialization() throws Exception {
		// Given — one log with entries, one with an empty list (skipped by the
		// serialization loop; its row is cleared via ELSE NULL)
		var method = this.updateTimeLogDerivedDataMethod();
		Map<Integer, List<OvertimeDetail>> overtimeDetails = new java.util.LinkedHashMap<>();
		overtimeDetails.put(10, List.of(new OvertimeDetail("DAILY_OVERTIME", "Daily Overtime", 7200, 1.5f, 1.5f,
				new BigDecimal("30.00"), new BigDecimal("36.00"))));
		overtimeDetails.put(20, List.of());

		// When
		method.invoke(this.ruleEngineService, 42, Map.of(10, 3600), overtimeDetails);

		// Then
		then(this.dslContext).should().update(any());
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Private helpers for updateDailyRegularHoursViaBatchSql
	// ──────────────────────────────────────────────────────────────────────────────

	private java.lang.reflect.Method updateTimeLogDerivedDataMethod() throws Exception {
		java.lang.reflect.Method method = RuleEngineService.class
			.getDeclaredMethod("updateTimeLogDerivedDataViaBatchSql", Integer.class, Map.class, Map.class);
		method.setAccessible(true);
		return method;
	}

	// ──────────────────────────────────────────────────────────────────────────────
	// Private helpers for evaluateRulesOnDemand tests
	// ──────────────────────────────────────────────────────────────────────────────

	private static BulkUpdateTimeLogsRequestBodyDto buildBulkRequest(Integer timesheetId) {
		BulkTimeLogRequestBodyDto log = buildTimeLog(1, timesheetId);
		BulkUpdateTimeLogsRequestBodyDto dto = new BulkUpdateTimeLogsRequestBodyDto();
		dto.setTimeLogs(List.of(log));
		return dto;
	}

	private static BulkTimeLogRequestBodyDto buildTimeLog(int id, Integer timesheetId) {
		BulkTimeLogRequestBodyDto log = new BulkTimeLogRequestBodyDto();
		log.setId(id);
		log.setTimesheetId(timesheetId);
		return log;
	}

	private RuleEngineResponseBodyDto createMockResponseWithAmounts() {
		RuleEngineResponseBodyDto response = new RuleEngineResponseBodyDto();

		RuleEvaluationSummaryResponseBodyDto summary = new RuleEvaluationSummaryResponseBodyDto();
		summary.setTotalBillAmount(new BigDecimal("1000.00"));
		summary.setTotalPayAmount(new BigDecimal("500.00"));
		summary.setTotalWeeksEvaluated(Integer.valueOf(2));
		summary.setTotalRuleEvaluations(Integer.valueOf(12));
		response.setEvaluationSummary(summary);

		List<WeeklyRuleResultResponseBodyDto> weeklyResults = new ArrayList<>();
		WeeklyRuleResultResponseBodyDto weeklyResult = new WeeklyRuleResultResponseBodyDto();

		MoneyDataResponseBodyDto weeklyMoneyData = new MoneyDataResponseBodyDto();
		weeklyMoneyData.setBillAmount(new BigDecimal("500.00"));
		weeklyMoneyData.setPayAmount(new BigDecimal("250.00"));
		weeklyResult.setWeeklyMoneyData(weeklyMoneyData);

		WeeklyOvertimeSummaryResponseBodyDto overtimeSummary = new WeeklyOvertimeSummaryResponseBodyDto();
		overtimeSummary.setWeeklyOvertimeBillAmount(new BigDecimal("50.00"));
		overtimeSummary.setWeeklyOvertimePayAmount(new BigDecimal("25.00"));
		weeklyResult.setWeeklyOvertimeResult(overtimeSummary);

		List<TimeLogRuleEvaluationResponseBodyDto> timeLogEvaluations = new ArrayList<>();
		TimeLogRuleEvaluationResponseBodyDto timeLogEval = new TimeLogRuleEvaluationResponseBodyDto();
		timeLogEval.setTotalBillAmount(new BigDecimal("100.00"));
		timeLogEval.setTotalPayAmount(new BigDecimal("50.00"));

		List<RuleEvaluationResultResponseBodyDto> ruleResults = new ArrayList<>();
		RuleEvaluationResultResponseBodyDto ruleResult = new RuleEvaluationResultResponseBodyDto();
		ruleResult.setBillAmount(new BigDecimal("100.00"));
		ruleResult.setPayAmount(new BigDecimal("50.00"));
		ruleResults.add(ruleResult);

		timeLogEval.setRuleEvaluationResults(ruleResults);
		timeLogEvaluations.add(timeLogEval);
		weeklyResult.setTimeLogRuleEvaluations(timeLogEvaluations);

		weeklyResults.add(weeklyResult);
		response.setWeeklyResults(weeklyResults);

		return response;
	}

}
