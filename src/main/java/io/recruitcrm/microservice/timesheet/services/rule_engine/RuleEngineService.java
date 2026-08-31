/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.rule_engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.OvertimeDetail;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.WeeklyOvertimeDetail;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.MoneyDataResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.TimeLogRuleEvaluationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyOvertimeSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyRuleResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.JsonReadException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.mapper.RuleEngineMapper;
import io.recruitcrm.microservice.timesheet.rule_engine.IRuleEngine;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.MoneyData;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogT;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.jooq.CaseValueStep;
import org.jooq.CaseWhenStep;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluatorResult;

/**
 * Service implementation for rule engine operations
 */
@Service
public class RuleEngineService implements IRuleEngineService {

	private final IRuleEngine ruleEngine;

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final AuthHolder authHolder;

	private final RuleEngineMapper ruleEngineMapper;

	private final AccessControlChecker accessControlChecker;

	private final OnDemandEvaluationWorker onDemandEvaluationWorker;

	private final java.util.concurrent.Executor onDemandEvaluationExecutor;

	private final DSLContext dslContext;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final Schema OVERTIME_DETAILS_SCHEMA = loadOvertimeDetailsSchema();

	@Autowired
	public RuleEngineService(IRuleEngine ruleEngine, TimesheetJpaRepository timesheetJpaRepository,
			AuthHolder authHolder, RuleEngineMapper ruleEngineMapper, AccessControlChecker accessControlChecker,
			OnDemandEvaluationWorker onDemandEvaluationWorker,
			@org.springframework.beans.factory.annotation.Qualifier("onDemandEvaluationExecutor") java.util.concurrent.Executor onDemandEvaluationExecutor,
			DSLContext dslContext) {
		this.ruleEngine = ruleEngine;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.authHolder = authHolder;
		this.ruleEngineMapper = ruleEngineMapper;
		this.accessControlChecker = accessControlChecker;
		this.onDemandEvaluationWorker = onDemandEvaluationWorker;
		this.onDemandEvaluationExecutor = onDemandEvaluationExecutor;
		this.dslContext = dslContext;
	}

	@Override
	@Transactional
	@WriterRoute
	public RuleEngineResponseBodyDto evaluateRules(RuleEngineRequestBodyDto requestDto) {
		// Get the principal to determine user type
		AuthPrincipal principal = this.authHolder.getUnifiedPrincipal();

		// Handle access control and account ID based on user type
		Integer accountId;
		Integer userId;
		Integer userTypeId;

		if (principal.getPrincipalType() == PrincipalType.USER) {
			// USER: Check access control and use authHolder for account ID
			this.checkAccessControlForUser(requestDto.getTimesheetId());
			accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();
			userId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();
			userTypeId = AccountUserEnum.USERTYPEID.getId();
		}
		else if (principal instanceof ContactPrincipal contactPrincipal) {
			// CONTACT: No access control check, use contactPrincipal for account ID
			accountId = contactPrincipal.getOrganizationIdentifier();
			userId = contactPrincipal.getContactId();
			userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();
		}
		else if (principal instanceof ContractorPrincipal contractorPrincipal) {
			// CONTRACTOR: No access control check, use contractorPrincipal for account ID
			accountId = contractorPrincipal.getOrganizationIdentifier();
			userId = contractorPrincipal.getCandidateId();
			userTypeId = UserTypeEnum.CONTRACTOR.getId();
		}
		else {
			// Fallback for unknown principal type
			accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();
			userId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();
			userTypeId = AccountUserEnum.USERTYPEID.getId();
		}

		// Fetch the timesheet by ID and account ID
		Optional<Timesheet> timesheetOptional = this.timesheetJpaRepository
			.findByIdAndAccountId(requestDto.getTimesheetId(), accountId);

		if (timesheetOptional.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet not found with ID: " + requestDto.getTimesheetId());
		}

		Timesheet timesheet = timesheetOptional.get();

		// Evaluate rules for the timesheet
		WeeklyRuleEvaluatorResult result = this.ruleEngine.evaluateRules(timesheet);

		RuleEngineMetrics metrics = this.extractRuleEngineMetrics(result);
		this.storeTotalBillPayAndRegularHourData(timesheet, result, userId, userTypeId, metrics);
		this.updateTimeLogDerivedDataViaBatchSql(timesheet.getId(), metrics.regularHoursByTimeLogId(),
				metrics.overtimeDetailsByTimeLogId());

		// Convert to response DTO using MapStruct
		RuleEngineResponseBodyDto response = this.ruleEngineMapper.toRuleEngineResponseBodyDto(result);

		// Filter response based on principal type (contractor/client/agency)
		this.filterResponseBasedOnPrincipalType(response, principal.getPrincipalType());

		return response;
	}

	/**
	 * Check access control for USER persona (agency users) Uses role-based access control
	 * with candidate permission checks
	 */
	private void checkAccessControlForUser(Integer timesheetId) {
		this.accessControlChecker.allows(Entity.TIMESHEET,
				PermissionCheckContext.builder()
					.permission(Permission.VIEW_TIMESHEET)
					.permissionLevel(PermissionLevel.YES)
					.build(),
				AccessControlCheckMetadataContext.builder().timesheetId(timesheetId).build());
	}

	/**
	 * Single-pass extraction of both regular-hour and overtime metrics from the rule
	 * engine result. Iterates over all weekly results exactly once, classifying each
	 * {@link RuleEvaluationResult} by its {@link RuleType} into either the regular-hours
	 * accumulator or the overtime total.
	 * <p>
	 * Regular-hours types (→ {@code daily_regular_hour} / {@code total_regular_hour}):
	 * {@code RANGE_BASED_REGULAR_HOURS}, {@code DURATION_BASED_REGULAR_HOURS},
	 * {@code RANGE_BASED_DEFAULT_PAY}, {@code DURATION_BASED_DEFAULT_PAY}.
	 * <p>
	 * Overtime types (→ {@code total_overtime}): {@code RANGE_BASED_AFTER_SHIFT},
	 * {@code RANGE_BASED_BEFORE_SHIFT}, {@code RANGE_BASED_SPECIFIC_TIME_RANGE},
	 * {@code RANGE_BASED_DAILY_OVERTIME}, {@code RANGE_BASED_WEEKLY_OVERTIME},
	 * {@code DURATION_BASED_SPECIFIC_HOUR_RANGE}, {@code DURATION_BASED_DAILY_OVERTIME},
	 * {@code DURATION_BASED_WEEKLY_OVERTIME}. Weekly OT duration is read from
	 * {@code weeklyOvertimeHours} (not {@code evaluatedDuration}).
	 */
	RuleEngineMetrics extractRuleEngineMetrics(WeeklyRuleEvaluatorResult result) {
		if (result == null) {
			return RuleEngineMetrics.empty();
		}

		List<WeeklyRuleEvaluatorResult.WeeklyResult> weeklyResults = result.getWeeklyResults();
		if (weeklyResults == null || weeklyResults.isEmpty()) {
			return RuleEngineMetrics.empty();
		}

		MetricsAccumulator accumulator = new MetricsAccumulator();

		for (WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult : weeklyResults) {
			RuleEvaluatorResult evalResult = weeklyResult.getRuleEvaluatorResult();
			if (evalResult == null) {
				continue;
			}
			accumulatePerLogRuleResults(evalResult, accumulator);
			accumulateWeeklyOvertime(evalResult, accumulator);
		}

		return accumulator.toMetrics();
	}

	/**
	 * Single pass over all per-log rule results in {@code evalResult}. Classifies each
	 * {@link RuleEvaluationResult} into the regular-hours accumulator or the overtime
	 * accumulators (seconds, per-log breakdown entries, and pay/bill amounts).
	 */
	private static void accumulatePerLogRuleResults(RuleEvaluatorResult evalResult, MetricsAccumulator accumulator) {
		if (evalResult.getRuleEvaluationResults() == null) {
			return;
		}
		for (Map.Entry<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog, List<RuleEvaluationResult>> entry : evalResult
			.getRuleEvaluationResults()
			.entrySet()) {
			io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog timeLog = entry.getKey();
			if (timeLog == null || timeLog.getId() == null || entry.getValue() == null) {
				continue;
			}
			accumulateRuleResultsForTimeLog(timeLog.getId(), entry.getValue(), accumulator);
		}
	}

	/**
	 * Iterates over the rule results for a single time-log entry, merging regular-hours
	 * and overtime contributions (seconds, breakdown entries, amounts) into the
	 * accumulator.
	 */
	private static void accumulateRuleResultsForTimeLog(Integer timeLogId, List<RuleEvaluationResult> ruleResults,
			MetricsAccumulator accumulator) {
		for (RuleEvaluationResult ruleResult : ruleResults) {
			if (ruleResult == null) {
				continue;
			}
			RuleType ruleType = ruleResult.getRuleType();
			long seconds = resolveEvaluatedDurationSeconds(ruleResult);
			if (isPerTimeLogRegularHoursRuleType(ruleType)) {
				accumulator.addRegularSeconds(timeLogId, Math.max(0L, seconds));
			}
			else if (isOvertimeRuleType(ruleType)) {
				accumulator.addOvertimeSeconds(Math.max(0L, seconds));
				accumulateOvertimeDetail(timeLogId, ruleResult, seconds, accumulator);
			}
		}
	}

	/**
	 * Builds one {@code overtime_details} entry for a per-log overtime rule result and
	 * adds its amounts to the overtime bucket. Weekly overtime is excluded from per-day
	 * entries by design — it is summarized at the timesheet level instead.
	 */
	private static void accumulateOvertimeDetail(Integer timeLogId, RuleEvaluationResult ruleResult, long seconds,
			MetricsAccumulator accumulator) {
		if (isWeeklyOvertimeRuleType(ruleResult.getRuleType())) {
			return;
		}
		OvertimeDetail detail = new OvertimeDetail(resolveOvertimeType(ruleResult.getRuleType()),
				ruleResult.getRuleName(), saturateInt(seconds), ruleResult.getPayRateMultiplier(),
				ruleResult.getBillRateMultiplier(), scaleToMoney(ruleResult.getTotalPayAmount()),
				scaleToMoney(ruleResult.getTotalBillAmount()));
		accumulator.addOvertimeDetail(timeLogId, detail);
		accumulator.addOvertimeAmounts(ruleResult.getTotalPayAmount(), ruleResult.getTotalBillAmount());
	}

	/**
	 * Accumulates weekly overtime contributions from one week's evaluation result:
	 * seconds (also counted into total overtime), money data (also counted into the
	 * overtime amount bucket), and the weekly OT rule multipliers for the summary.
	 */
	private static void accumulateWeeklyOvertime(RuleEvaluatorResult evalResult, MetricsAccumulator accumulator) {
		RuleEvaluationResult weeklyOtResult = evalResult.getWeeklyOvertimeRuleEvaluationResult();
		accumulator.addWeeklyOvertimeSeconds(accumulateWeeklyOvertimeSeconds(weeklyOtResult));
		MoneyData weeklyOvertimeMoneyData = evalResult.getWeeklyOvertimeMoneyData();
		if (weeklyOvertimeMoneyData != null) {
			accumulator.addWeeklyOvertimeAmounts(weeklyOvertimeMoneyData.getPayAmountOrZero(),
					weeklyOvertimeMoneyData.getBillAmountOrZero());
		}
		if (weeklyOtResult != null) {
			accumulator.captureWeeklyOvertimeMultipliers(weeklyOtResult.getPayRateMultiplier(),
					weeklyOtResult.getBillRateMultiplier());
		}
	}

	/**
	 * Returns the weekly overtime seconds from a weekly OT result entry. The duration
	 * lives in {@code weeklyOvertimeHours}, not {@code evaluatedDuration}.
	 */
	private static long accumulateWeeklyOvertimeSeconds(RuleEvaluationResult weeklyOtResult) {
		if (weeklyOtResult == null || !isOvertimeRuleType(weeklyOtResult.getRuleType())) {
			return 0L;
		}
		Duration weeklyOvertimeHours = weeklyOtResult.getWeeklyOvertimeHours();
		if (weeklyOvertimeHours == null || weeklyOvertimeHours.isNegative()) {
			return 0L;
		}
		return weeklyOvertimeHours.getSeconds();
	}

	/**
	 * Stores the total bill, pay, regular-hour, and overtime data in the timesheet entity
	 * based on evaluation results. Uses pre-extracted {@link RuleEngineMetrics} to avoid
	 * a second pass over the rule results. Updates audit fields and performs a single
	 * {@link TimesheetJpaRepository#save}.
	 * @param timesheet the timesheet entity to update
	 * @param result the rule evaluation result containing monetary data
	 * @param userId the ID of the user making the update
	 * @param userTypeId the user type ID of the user making the update
	 * @param metrics pre-extracted regular-hours and overtime metrics
	 */
	private void storeTotalBillPayAndRegularHourData(Timesheet timesheet, WeeklyRuleEvaluatorResult result,
			Integer userId, Integer userTypeId, RuleEngineMetrics metrics) {
		if (timesheet == null || result == null) {
			return;
		}

		// Calculate total bill and pay amounts from all weekly results including weekly
		// overtime
		BigDecimal totalBillAmount = BigDecimal.ZERO;
		BigDecimal totalPayAmount = BigDecimal.ZERO;

		for (WeeklyRuleEvaluatorResult.WeeklyResult weeklyResult : result.getWeeklyResults()) {
			if (weeklyResult.getRuleEvaluatorResult() != null) {
				RuleEvaluatorResult ruleEvaluatorResult = weeklyResult.getRuleEvaluatorResult();

				// Add base amounts from regular rules
				totalBillAmount = totalBillAmount.add(ruleEvaluatorResult.getTotalBillAmount());
				totalPayAmount = totalPayAmount.add(ruleEvaluatorResult.getTotalPayAmount());

				// Add weekly overtime amounts if present
				if (ruleEvaluatorResult.getWeeklyOvertimeMoneyData() != null) {
					totalBillAmount = totalBillAmount
						.add(ruleEvaluatorResult.getWeeklyOvertimeMoneyData().getBillAmount());
					totalPayAmount = totalPayAmount
						.add(ruleEvaluatorResult.getWeeklyOvertimeMoneyData().getPayAmount());
				}
			}
		}

		// Update the timesheet entity with calculated totals
		timesheet.setTotalBillData(totalBillAmount.floatValue());
		timesheet.setTotalPayData(totalPayAmount.floatValue());

		long totalRegularSeconds = 0L;
		for (Integer seconds : metrics.regularHoursByTimeLogId().values()) {
			totalRegularSeconds += seconds.longValue();
		}
		timesheet.setTotalRegularHour(saturateInt(totalRegularSeconds));
		timesheet.setTotalOvertime(saturateInt(metrics.totalOvertimeSeconds()));
		timesheet.setTotalWeeklyOvertime(saturateInt(metrics.totalWeeklyOvertimeSeconds()));

		applyBifurcationData(timesheet, totalPayAmount, totalBillAmount, metrics);

		// Update audit fields
		Integer currentUNIXTimestamp = (int) Instant.now().getEpochSecond();
		timesheet.setUpdatedBy(userId);
		timesheet.setUpdatedByUserTypeId(userTypeId);
		timesheet.setUpdatedOn(currentUNIXTimestamp);

		// Save the updated timesheet
		this.timesheetJpaRepository.save(timesheet);
	}

	/**
	 * Sets the regular/overtime amount split and the weekly overtime summary on the
	 * timesheet entity. The regular bucket is derived as {@code total - overtime} so the
	 * invariant {@code regular + overtime = pay_data/bill_data} holds by construction
	 * (default-pay/unplanned hours therefore land in the regular bucket).
	 */
	private static void applyBifurcationData(Timesheet timesheet, BigDecimal totalPayAmount, BigDecimal totalBillAmount,
			RuleEngineMetrics metrics) {
		BigDecimal overtimePayAmount = scaleToMoney(metrics.overtimePayAmount());
		BigDecimal overtimeBillAmount = scaleToMoney(metrics.overtimeBillAmount());
		timesheet.setOvertimePayAmount(overtimePayAmount);
		timesheet.setOvertimeBillAmount(overtimeBillAmount);
		timesheet.setRegularPayAmount(scaleToMoney(totalPayAmount.subtract(overtimePayAmount)));
		timesheet.setRegularBillAmount(scaleToMoney(totalBillAmount.subtract(overtimeBillAmount)));
		timesheet.setWeeklyOvertimeDetails(metrics.weeklyOvertimeDetails());
	}

	/**
	 * Returns true for rule types whose evaluated duration contributes to
	 * {@code total_overtime}. Covers all daily and weekly shift/overtime rules for both
	 * range-based and duration-based evaluators.
	 */
	private static boolean isOvertimeRuleType(RuleType ruleType) {
		if (ruleType == null) {
			return false;
		}
		return ruleType == RuleType.RANGE_BASED_AFTER_SHIFT || ruleType == RuleType.RANGE_BASED_BEFORE_SHIFT
				|| ruleType == RuleType.RANGE_BASED_SPECIFIC_TIME_RANGE
				|| ruleType == RuleType.RANGE_BASED_DAILY_OVERTIME || ruleType == RuleType.RANGE_BASED_WEEKLY_OVERTIME
				|| ruleType == RuleType.DURATION_BASED_SPECIFIC_HOUR_RANGE
				|| ruleType == RuleType.DURATION_BASED_DAILY_OVERTIME
				|| ruleType == RuleType.DURATION_BASED_WEEKLY_OVERTIME;
	}

	/**
	 * Persists {@code TimeLog.dailyRegularHour} ({@code daily_regular_hour}, seconds —
	 * same unit as {@code work_time}) and {@code overtime_details} (JSON breakdown) via a
	 * single JOOQ UPDATE with CASE-WHEN expressions. Evaluated rows get their computed
	 * values; all other rows for this timesheet are set to {@code NULL} to clear stale
	 * values. Does not load TimeLog entities.
	 * <p>
	 * SQL shape (single statement):
	 *
	 * <pre>
	 * UPDATE cst_time_log_t
	 *    SET daily_regular_hour = CASE id
	 *           WHEN 101 THEN 3600
	 *           WHEN 102 THEN 7200
	 *           ELSE NULL END,
	 *        overtime_details = CASE id
	 *           WHEN 101 THEN '[{"overtime_type": ...}]'
	 *           ELSE NULL END
	 *  WHERE timesheet_id = ?
	 * </pre>
	 */
	private void updateTimeLogDerivedDataViaBatchSql(Integer timesheetId, Map<Integer, Integer> regularHoursByTimeLogId,
			Map<Integer, List<OvertimeDetail>> overtimeDetailsByTimeLogId) {
		if (timesheetId == null) {
			return;
		}

		var tl = CstTimeLogT.CST_TIME_LOG_T;
		Field<String> overtimeDetailsField = DSL.field(DSL.name("overtime_detail"), String.class);

		if (regularHoursByTimeLogId.isEmpty() && overtimeDetailsByTimeLogId.isEmpty()) {
			this.dslContext.update(tl)
				.setNull(tl.DAILY_REGULAR_HOUR)
				.setNull(overtimeDetailsField)
				.where(tl.TIMESHEET_ID.eq(timesheetId))
				.execute();
			return;
		}

		this.dslContext.update(tl)
			.set(tl.DAILY_REGULAR_HOUR, buildCaseExpression(tl.ID, regularHoursByTimeLogId, Integer.class))
			.set(overtimeDetailsField,
					buildCaseExpression(tl.ID, this.serializeOvertimeDetailsByTimeLogId(overtimeDetailsByTimeLogId),
							String.class))
			.where(tl.TIMESHEET_ID.eq(timesheetId))
			.execute();
	}

	/**
	 * Builds a {@code CASE id WHEN ... THEN ... ELSE NULL END} expression for the given
	 * per-id values, or a plain NULL value when the map is empty (all rows cleared).
	 */
	private static <T> Field<T> buildCaseExpression(Field<Integer> idField, Map<Integer, T> valuesById,
			Class<T> valueType) {
		if (valuesById.isEmpty()) {
			return DSL.val(null, valueType);
		}

		CaseValueStep<Integer> caseStep = DSL.case_(idField);
		CaseWhenStep<Integer, T> whenChain = null;

		for (Map.Entry<Integer, T> entry : valuesById.entrySet()) {
			if (whenChain == null) {
				whenChain = caseStep.when(entry.getKey(), entry.getValue());
			}
			else {
				whenChain = whenChain.when(entry.getKey(), entry.getValue());
			}
		}

		return whenChain.else_((T) null);
	}

	/**
	 * Serializes each time log's overtime breakdown to its JSON column value. Logs with
	 * no entries are omitted (the CASE expression's ELSE NULL clears them).
	 */
	private Map<Integer, String> serializeOvertimeDetailsByTimeLogId(
			Map<Integer, List<OvertimeDetail>> overtimeDetailsByTimeLogId) {
		Map<Integer, String> jsonByTimeLogId = new LinkedHashMap<>();
		for (Map.Entry<Integer, List<OvertimeDetail>> entry : overtimeDetailsByTimeLogId.entrySet()) {
			if (entry.getValue() == null || entry.getValue().isEmpty()) {
				continue;
			}
			jsonByTimeLogId.put(entry.getKey(), this.serializeOvertimeDetails(entry.getValue()));
		}
		return jsonByTimeLogId;
	}

	/**
	 * Serializes one overtime breakdown list to its JSON string and validates it against
	 * the {@code overtime_details_json.json} schema before it is written — the same
	 * write-side guarantee the entity AttributeConverter provides, which the batch UPDATE
	 * path bypasses. A validation failure throws and rolls back the transaction.
	 */
	private String serializeOvertimeDetails(List<OvertimeDetail> overtimeDetails) {
		try {
			String jsonString = this.objectMapper.writeValueAsString(overtimeDetails);
			OVERTIME_DETAILS_SCHEMA.validate(new JSONArray(jsonString));
			return jsonString;
		}
		catch (JsonProcessingException error) {
			throw new JsonReadException("Failed to serialize overtime details", error);
		}
		catch (JSONException | ValidationException error) {
			throw new JsonReadException("overtime_details JSON failed schema validation", error);
		}
	}

	/**
	 * Loads the {@code overtime_details_json.json} schema from the classpath at
	 * class-initialization time (same pattern as the entity-models AttributeConverters —
	 * a missing schema file fails fast at startup).
	 */
	private static Schema loadOvertimeDetailsSchema() {
		try (InputStream inputStream = new ClassPathResource("overtime_details_json.json").getInputStream()) {
			JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			return SchemaLoader.load(rawSchema);
		}
		catch (IOException | JSONException | SchemaException error) {
			throw new JsonReadException("Failed to load overtime_details_json.json schema", error);
		}
	}

	/**
	 * Rule types whose evaluated duration contributes to {@code daily_regular_hour} /
	 * {@code total_regular_hour}. Includes both the regular-hours virtual rule and the
	 * default-pay rule so that time not covered by any custom rule is counted as regular
	 * time. Range-based variants apply to {@code START_AND_END_TIME} timesheets;
	 * duration-based variants apply to {@code WORK_HOUR} timesheets.
	 */
	private static boolean isPerTimeLogRegularHoursRuleType(RuleType ruleType) {
		if (ruleType == null) {
			return false;
		}
		return ruleType == RuleType.RANGE_BASED_REGULAR_HOURS || ruleType == RuleType.DURATION_BASED_REGULAR_HOURS
				|| ruleType == RuleType.RANGE_BASED_DEFAULT_PAY || ruleType == RuleType.DURATION_BASED_DEFAULT_PAY;
	}

	/**
	 * Returns true for the weekly overtime rule types (both range-based and
	 * duration-based). Weekly overtime is summarized at the timesheet level and excluded
	 * from per-day {@code overtime_details} entries.
	 */
	private static boolean isWeeklyOvertimeRuleType(RuleType ruleType) {
		return ruleType == RuleType.RANGE_BASED_WEEKLY_OVERTIME || ruleType == RuleType.DURATION_BASED_WEEKLY_OVERTIME;
	}

	/**
	 * Resolves the {@code overtime_type} JSON value for a rule type: the enum name with
	 * the evaluator-strategy prefix stripped (e.g. {@code RANGE_BASED_DAILY_OVERTIME} and
	 * {@code DURATION_BASED_DAILY_OVERTIME} both become {@code DAILY_OVERTIME}), since
	 * the range/duration distinction is an internal evaluator detail.
	 */
	private static String resolveOvertimeType(RuleType ruleType) {
		return ruleType.name().replace("RANGE_BASED_", "").replace("DURATION_BASED_", "");
	}

	/**
	 * Resolves evaluated duration in seconds from a rule evaluation result.
	 */
	private static long resolveEvaluatedDurationSeconds(RuleEvaluationResult ruleEvaluationResult) {
		Duration evaluated = ruleEvaluationResult.getEvaluatedDuration();
		if (evaluated != null) {
			return evaluated.getSeconds();
		}
		return ruleEvaluationResult.calculateDuration().getSeconds();
	}

	/**
	 * Scales a monetary amount to 2 decimal places (HALF_UP) — the same rounding MySQL
	 * applies when storing into the DECIMAL(10,2) amount columns. Applied before amounts
	 * enter the JSON columns (which MySQL does not round) so JSON and DECIMAL values stay
	 * consistent.
	 */
	static BigDecimal scaleToMoney(BigDecimal amount) {
		return amount.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Clamps a non-negative long into {@code int} range for DB integer columns.
	 */
	static int saturateInt(long value) {
		if (value <= 0L) {
			return 0;
		}
		if (value >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) value;
	}

	@Override
	public String validateRules(RuleEngineRequestBodyDto requestDto) {
		this.ruleEngine.validateRules();
		return "Validation successful";
	}

	/**
	 * On-demand preview: no per-timesheet VIEW_TIMESHEET access check (unlike
	 * {@link #evaluateRules}). Callers are still limited to timesheets in their
	 * {@code accountId} via {@link OnDemandEvaluationWorker#evaluateSingleTimesheet}.
	 */
	@Override
	public List<OnDemandTimesheetOvertimeDto> evaluateRulesOnDemand(BulkUpdateTimeLogsRequestBodyDto requestDto) {
		// Get the principal to determine user type
		AuthPrincipal principal = this.authHolder.getUnifiedPrincipal();

		// Handle access control and account ID based on user type
		Integer accountId;

		List<BulkTimeLogRequestBodyDto> bulkTimeLogs = requestDto.getTimeLogs();
		if (bulkTimeLogs == null || bulkTimeLogs.isEmpty()) {
			throw new IllegalArgumentException("Time logs cannot be null or empty");
		}

		if (principal.getPrincipalType() == PrincipalType.USER) {
			accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();
		}
		else if (principal instanceof ContactPrincipal contactPrincipal) {
			accountId = contactPrincipal.getOrganizationIdentifier();
		}
		else if (principal instanceof ContractorPrincipal contractorPrincipal) {
			accountId = contractorPrincipal.getOrganizationIdentifier();
		}
		else {
			accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();
		}

		// Group time logs by timesheetId
		Map<Integer, List<BulkTimeLogRequestBodyDto>> timeLogsByTimesheetId = new LinkedHashMap<>();
		for (BulkTimeLogRequestBodyDto bulkTimeLog : bulkTimeLogs) {
			if (bulkTimeLog.getTimesheetId() == null) {
				throw new IllegalArgumentException("Timesheet ID cannot be null on time log");
			}
			timeLogsByTimesheetId.computeIfAbsent(bulkTimeLog.getTimesheetId(), (k) -> new ArrayList<>())
				.add(bulkTimeLog);
		}

		// Fan out evaluation to worker threads -- each gets its own @Transactional
		final Integer finalAccountId = accountId;
		final boolean isSingleTimesheet = timeLogsByTimesheetId.size() == 1;
		List<java.util.concurrent.CompletableFuture<OnDemandTimesheetOvertimeDto>> futures = new ArrayList<>();

		for (Map.Entry<Integer, List<BulkTimeLogRequestBodyDto>> entry : timeLogsByTimesheetId.entrySet()) {
			final Integer timesheetId = entry.getKey();
			final List<BulkTimeLogRequestBodyDto> logsForTimesheet = entry.getValue();
			futures.add(
					java.util.concurrent.CompletableFuture
						.supplyAsync(
								() -> this.onDemandEvaluationWorker.evaluateSingleTimesheet(timesheetId,
										logsForTimesheet, finalAccountId, isSingleTimesheet),
								this.onDemandEvaluationExecutor));
		}

		// Collect results
		return futures.stream().map(java.util.concurrent.CompletableFuture::join).toList();
	}

	/**
	 * Filters the rule engine response based on principal type: - CONTRACTOR: Hides all
	 * bill amounts (set to null) - CONTACT (Client): Hides all pay and bill amounts (set
	 * to null) - USER (Agency): No changes - full response
	 * @param response the response to filter
	 * @param principalType the type of principal making the request
	 */
	private void filterResponseBasedOnPrincipalType(RuleEngineResponseBodyDto response, PrincipalType principalType) {
		if (response == null || principalType == null) {
			return;
		}

		// Agency users (USER) - no filtering, show everything
		if (principalType == PrincipalType.USER) {
			return;
		}

		// Determine what to hide based on principal type
		boolean hideBillAmounts = (principalType == PrincipalType.CONTRACTOR || principalType == PrincipalType.CONTACT);
		boolean hidePayAmounts = (principalType == PrincipalType.CONTACT);

		// Filter weekly results
		if (response.getWeeklyResults() != null) {
			for (WeeklyRuleResultResponseBodyDto weeklyResult : response.getWeeklyResults()) {
				this.filterWeeklyResult(weeklyResult, hideBillAmounts, hidePayAmounts);
			}
		}

		// Filter evaluation summary
		if (response.getEvaluationSummary() != null) {
			this.filterEvaluationSummary(response.getEvaluationSummary(), hideBillAmounts, hidePayAmounts);
		}
	}

	/**
	 * Filters a weekly result based on principal type
	 */
	private void filterWeeklyResult(WeeklyRuleResultResponseBodyDto weeklyResult, boolean hideBillAmounts,
			boolean hidePayAmounts) {
		if (weeklyResult == null) {
			return;
		}

		// Filter time log rule evaluations
		if (weeklyResult.getTimeLogRuleEvaluations() != null) {
			for (TimeLogRuleEvaluationResponseBodyDto timeLogEvaluation : weeklyResult.getTimeLogRuleEvaluations()) {
				this.filterTimeLogEvaluation(timeLogEvaluation, hideBillAmounts, hidePayAmounts);
			}
		}

		// Filter weekly money data
		if (weeklyResult.getWeeklyMoneyData() != null) {
			this.filterMoneyData(weeklyResult.getWeeklyMoneyData(), hideBillAmounts, hidePayAmounts);
		}

		// Filter weekly overtime result
		if (weeklyResult.getWeeklyOvertimeResult() != null) {
			this.filterWeeklyOvertimeSummary(weeklyResult.getWeeklyOvertimeResult(), hideBillAmounts, hidePayAmounts);
		}
	}

	/**
	 * Filters a time log evaluation based on principal type
	 */
	private void filterTimeLogEvaluation(TimeLogRuleEvaluationResponseBodyDto timeLogEvaluation,
			boolean hideBillAmounts, boolean hidePayAmounts) {
		if (timeLogEvaluation == null) {
			return;
		}

		// Filter total amounts
		if (hideBillAmounts) {
			timeLogEvaluation.setTotalBillAmount(null);
		}
		if (hidePayAmounts) {
			timeLogEvaluation.setTotalPayAmount(null);
		}

		// Filter individual rule evaluation results
		if (timeLogEvaluation.getRuleEvaluationResults() != null) {
			for (RuleEvaluationResultResponseBodyDto ruleResult : timeLogEvaluation.getRuleEvaluationResults()) {
				this.filterRuleEvaluationResult(ruleResult, hideBillAmounts, hidePayAmounts);
			}
		}
	}

	/**
	 * Filters a rule evaluation result based on principal type
	 */
	private void filterRuleEvaluationResult(RuleEvaluationResultResponseBodyDto ruleResult, boolean hideBillAmounts,
			boolean hidePayAmounts) {
		if (ruleResult == null) {
			return;
		}

		if (hideBillAmounts) {
			ruleResult.setBillAmount(null);
		}
		if (hidePayAmounts) {
			ruleResult.setPayAmount(null);
		}
	}

	/**
	 * Filters money data based on principal type
	 */
	private void filterMoneyData(MoneyDataResponseBodyDto moneyData, boolean hideBillAmounts, boolean hidePayAmounts) {
		if (moneyData == null) {
			return;
		}

		if (hideBillAmounts) {
			moneyData.setBillAmount(null);
		}
		if (hidePayAmounts) {
			moneyData.setPayAmount(null);
		}
	}

	/**
	 * Filters weekly overtime summary based on principal type
	 */
	private void filterWeeklyOvertimeSummary(WeeklyOvertimeSummaryResponseBodyDto overtimeSummary,
			boolean hideBillAmounts, boolean hidePayAmounts) {
		if (overtimeSummary == null) {
			return;
		}

		if (hideBillAmounts) {
			overtimeSummary.setWeeklyOvertimeBillAmount(null);
		}
		if (hidePayAmounts) {
			overtimeSummary.setWeeklyOvertimePayAmount(null);
		}
	}

	/**
	 * Filters evaluation summary based on principal type
	 */
	private void filterEvaluationSummary(RuleEvaluationSummaryResponseBodyDto evaluationSummary,
			boolean hideBillAmounts, boolean hidePayAmounts) {
		if (evaluationSummary == null) {
			return;
		}

		if (hideBillAmounts) {
			evaluationSummary.setTotalBillAmount(null);
		}
		if (hidePayAmounts) {
			evaluationSummary.setTotalPayAmount(null);
		}
	}

	/**
	 * Holds the metrics extracted from the rule engine result in a single pass: per-log
	 * regular-hour seconds (for {@code daily_regular_hour} / {@code total_regular_hour}),
	 * total overtime seconds (for {@code total_overtime}), weekly-only overtime seconds
	 * (for {@code total_weekly_overtime}), the per-log overtime breakdown (for
	 * {@code overtime_details}), the weekly overtime summary (for
	 * {@code weekly_overtime_details}; null when no weekly OT accrued), and the overtime
	 * pay/bill amount bucket (for the regular/overtime amount split).
	 */
	record RuleEngineMetrics(Map<Integer, Integer> regularHoursByTimeLogId, long totalOvertimeSeconds,
			long totalWeeklyOvertimeSeconds, Map<Integer, List<OvertimeDetail>> overtimeDetailsByTimeLogId,
			WeeklyOvertimeDetail weeklyOvertimeDetails, BigDecimal overtimePayAmount, BigDecimal overtimeBillAmount) {

		static RuleEngineMetrics empty() {
			return new RuleEngineMetrics(Map.of(), 0L, 0L, Map.of(), null, BigDecimal.ZERO, BigDecimal.ZERO);
		}

	}

	/**
	 * Mutable accumulator used during the single-pass metric extraction. Collects per-log
	 * regular seconds, per-log overtime breakdown entries, overtime totals (seconds and
	 * pay/bill amounts), and the weekly overtime summary components.
	 */
	private static final class MetricsAccumulator {

		private final Map<Integer, Long> regularSecondsByTimeLogId = new HashMap<>();

		private final Map<Integer, List<OvertimeDetail>> overtimeDetailsByTimeLogId = new LinkedHashMap<>();

		private long overtimeSeconds;

		private long weeklyOvertimeSeconds;

		private BigDecimal overtimePayAmount = BigDecimal.ZERO;

		private BigDecimal overtimeBillAmount = BigDecimal.ZERO;

		private BigDecimal weeklyOvertimePayAmount = BigDecimal.ZERO;

		private BigDecimal weeklyOvertimeBillAmount = BigDecimal.ZERO;

		private Float weeklyOvertimePayRateMultiplier;

		private Float weeklyOvertimeBillRateMultiplier;

		void addRegularSeconds(Integer timeLogId, long seconds) {
			this.regularSecondsByTimeLogId.merge(timeLogId, seconds, Long::sum);
		}

		void addOvertimeSeconds(long seconds) {
			this.overtimeSeconds += seconds;
		}

		void addOvertimeDetail(Integer timeLogId, OvertimeDetail detail) {
			this.overtimeDetailsByTimeLogId.computeIfAbsent(timeLogId, (k) -> new ArrayList<>()).add(detail);
		}

		void addOvertimeAmounts(BigDecimal payAmount, BigDecimal billAmount) {
			this.overtimePayAmount = this.overtimePayAmount.add(payAmount);
			this.overtimeBillAmount = this.overtimeBillAmount.add(billAmount);
		}

		void addWeeklyOvertimeSeconds(long seconds) {
			this.overtimeSeconds += seconds;
			this.weeklyOvertimeSeconds += seconds;
		}

		void addWeeklyOvertimeAmounts(BigDecimal payAmount, BigDecimal billAmount) {
			this.weeklyOvertimePayAmount = this.weeklyOvertimePayAmount.add(payAmount);
			this.weeklyOvertimeBillAmount = this.weeklyOvertimeBillAmount.add(billAmount);
			this.addOvertimeAmounts(payAmount, billAmount);
		}

		void captureWeeklyOvertimeMultipliers(Float payRateMultiplier, Float billRateMultiplier) {
			if (this.weeklyOvertimePayRateMultiplier == null) {
				this.weeklyOvertimePayRateMultiplier = payRateMultiplier;
			}
			if (this.weeklyOvertimeBillRateMultiplier == null) {
				this.weeklyOvertimeBillRateMultiplier = billRateMultiplier;
			}
		}

		RuleEngineMetrics toMetrics() {
			Map<Integer, Integer> regularHours = HashMap.newHashMap(this.regularSecondsByTimeLogId.size());
			for (Map.Entry<Integer, Long> entry : this.regularSecondsByTimeLogId.entrySet()) {
				regularHours.put(entry.getKey(), saturateInt(entry.getValue()));
			}
			return new RuleEngineMetrics(regularHours, this.overtimeSeconds, this.weeklyOvertimeSeconds,
					this.overtimeDetailsByTimeLogId, this.buildWeeklyOvertimeDetails(), this.overtimePayAmount,
					this.overtimeBillAmount);
		}

		private WeeklyOvertimeDetail buildWeeklyOvertimeDetails() {
			if (this.weeklyOvertimeSeconds <= 0L) {
				return null;
			}
			return new WeeklyOvertimeDetail(saturateInt(this.weeklyOvertimeSeconds),
					this.weeklyOvertimePayRateMultiplier, this.weeklyOvertimeBillRateMultiplier,
					scaleToMoney(this.weeklyOvertimePayAmount), scaleToMoney(this.weeklyOvertimeBillAmount));
		}

	}

}
