/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.mapper;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.MoneyDataResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEvaluationSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.TimeLogRuleEvaluationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyOvertimeSummaryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.WeeklyRuleResultResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.BigDecimalFormatter;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.MoneyData;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluatorResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult.WeeklyResult;
import io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MapStruct mapper for converting rule engine results to response DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RuleEngineMapper {

	RuleEngineMapper INSTANCE = Mappers.getMapper(RuleEngineMapper.class);

	@Mapping(source = "timesheet.id", target = "timesheetId")
	@Mapping(source = "weeklyResults", target = "weeklyResults")
	@Mapping(target = "evaluationSummary", expression = "java(createEvaluationSummary(result))")
	RuleEngineResponseBodyDto toRuleEngineResponseBodyDto(WeeklyRuleEvaluatorResult result);

	@Mapping(source = "weekStartDate", target = "weekStartDate")
	@Mapping(source = "weekEndDate", target = "weekEndDate")
	@Mapping(source = "ruleEvaluatorResult", target = "timeLogRuleEvaluations",
			qualifiedByName = "convertToTimeLogRuleEvaluations")
	@Mapping(source = "ruleEvaluatorResult", target = "weeklyMoneyData", qualifiedByName = "convertMoneyData")
	@Mapping(source = "ruleEvaluatorResult", target = "weeklyOvertimeResult",
			qualifiedByName = "convertWeeklyOvertimeSummary")
	WeeklyRuleResultResponseBodyDto toWeeklyRuleResultResponseBodyDto(WeeklyResult weeklyResult);

	@Mapping(source = "timeRange", target = "timeRanges", qualifiedByName = "convertTimeRangeToTimeRanges")
	@Mapping(source = "ruleType", target = "ruleType")
	@Mapping(source = "ruleName", target = "ruleName")
	@Mapping(source = "billAmount", target = "billAmount", qualifiedByName = "formatMonetaryAmount")
	@Mapping(source = "payAmount", target = "payAmount", qualifiedByName = "formatMonetaryAmount")
	@Mapping(source = "evaluatedDuration", target = "evaluatedDurationInSeconds", qualifiedByName = "durationToSeconds")
	@Mapping(source = "evaluatedDuration", target = "evaluatedDurationApproximateHours",
			qualifiedByName = "durationToApproximateHours")
	@Mapping(source = "successful", target = "successful")
	@Mapping(source = "errorMessage", target = "errorMessage")
	@Mapping(source = "metadata", target = "metadata")
	@Mapping(source = "evaluationDate", target = "evaluationDate")
	@Mapping(source = "ruleIndex", target = "ruleIndex")
	RuleEvaluationResultResponseBodyDto toRuleEvaluationResultResponseBodyDto(RuleEvaluationResult result);

	@Mapping(source = "payAmount", target = "payAmount", qualifiedByName = "formatMonetaryAmount")
	@Mapping(source = "billAmount", target = "billAmount", qualifiedByName = "formatMonetaryAmount")
	MoneyDataResponseBodyDto toMoneyDataResponseBodyDto(MoneyData moneyData);

	@Named("formatMonetaryAmount")
	default BigDecimal formatMonetaryAmount(BigDecimal value) {
		return BigDecimalFormatter.formatMonetaryAmount(value);
	}

	@Named("createEvaluationSummary")
	default RuleEvaluationSummaryResponseBodyDto createEvaluationSummary(WeeklyRuleEvaluatorResult result) {
		if (result == null || !result.hasResults()) {
			return RuleEvaluationSummaryResponseBodyDto.builder()
				.totalWeeksEvaluated(0)
				.totalRuleEvaluations(0)
				.totalBillAmount(BigDecimalFormatter.formatMonetaryAmount(BigDecimal.ZERO))
				.totalPayAmount(BigDecimalFormatter.formatMonetaryAmount(BigDecimal.ZERO))
				.build();
		}

		int totalWeeks = result.getWeekCount();
		int totalEvaluations = result.getAllRuleEvaluatorResults()
			.stream()
			.mapToInt(RuleEvaluatorResult::getTotalRuleEvaluations)
			.sum();

		// Calculate total amounts including weekly overtime
		BigDecimal totalBillAmount = result.getAllRuleEvaluatorResults().stream().map((ruleEvaluatorResult) -> {
			BigDecimal baseAmount = ruleEvaluatorResult.getTotalBillAmount();
			BigDecimal weeklyOvertimeAmount = (ruleEvaluatorResult.getWeeklyOvertimeMoneyData() != null)
					? ruleEvaluatorResult.getWeeklyOvertimeMoneyData().getBillAmount() : BigDecimal.ZERO;
			return baseAmount.add(weeklyOvertimeAmount);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalPayAmount = result.getAllRuleEvaluatorResults().stream().map((ruleEvaluatorResult) -> {
			BigDecimal baseAmount = ruleEvaluatorResult.getTotalPayAmount();
			BigDecimal weeklyOvertimeAmount = (ruleEvaluatorResult.getWeeklyOvertimeMoneyData() != null)
					? ruleEvaluatorResult.getWeeklyOvertimeMoneyData().getPayAmount() : BigDecimal.ZERO;
			return baseAmount.add(weeklyOvertimeAmount);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		return RuleEvaluationSummaryResponseBodyDto.builder()
			.totalWeeksEvaluated(totalWeeks)
			.totalRuleEvaluations(totalEvaluations)
			.totalBillAmount(BigDecimalFormatter.formatMonetaryAmount(totalBillAmount))
			.totalPayAmount(BigDecimalFormatter.formatMonetaryAmount(totalPayAmount))
			.build();
	}

	@Named("convertTimeRangeToTimeRanges")
	default List<List<LocalTime>> convertTimeRangeToTimeRanges(RangeSet<LocalTime> timeRange) {
		if (timeRange == null || timeRange.isEmpty()) {
			return List.of();
		}

		return timeRange.asRanges()
			.stream()
			.map((range) -> List.of(range.lowerEndpoint(), range.upperEndpoint()))
			.toList();
	}

	@Named("convertRuleEvaluationResultsToList")
	default List<RuleEvaluationResultResponseBodyDto> convertRuleEvaluationResultsToList(RuleEvaluatorResult result) {
		if (result == null || !result.hasResults()) {
			return List.of();
		}

		List<RuleEvaluationResultResponseBodyDto> results = new java.util.ArrayList<>();

		// Add regular rule evaluation results (excluding weekly overtime)
		for (Map.Entry<TimeLog, List<RuleEvaluationResult>> entry : result.getRuleEvaluationResults().entrySet()) {
			for (RuleEvaluationResult ruleResult : entry.getValue()) {
				// Skip weekly overtime results as they are handled at the weekly level
				if (ruleResult.getRuleType() != RuleType.RANGE_BASED_WEEKLY_OVERTIME
						&& ruleResult.getRuleType() != RuleType.DURATION_BASED_WEEKLY_OVERTIME) {
					results.add(toRuleEvaluationResultResponseBodyDto(ruleResult));
				}
			}
		}

		return results;
	}

	@Named("convertWeeklyOvertimeSummary")
	default WeeklyOvertimeSummaryResponseBodyDto convertWeeklyOvertimeSummary(RuleEvaluatorResult result) {
		if (result == null || result.getWeeklyOvertimeRuleEvaluationResult() == null) {
			// Return empty object instead of null using builder pattern
			return WeeklyOvertimeSummaryResponseBodyDto.builder()
				.weeklyOvertimeHoursInSeconds(0L)
				.weeklyOvertimeHoursInApproximateHours(0.0f)
				.weeklyOvertimeBillAmount(BigDecimalFormatter.formatMonetaryAmount(BigDecimal.ZERO))
				.weeklyOvertimePayAmount(BigDecimalFormatter.formatMonetaryAmount(BigDecimal.ZERO))
				.hasWeeklyOvertime(false)
				.weeklyOvertimeRuleName("Weekly Overtime")
				.virtualRule(false)
				.build();
		}

		RuleEvaluationResult weeklyResult = result.getWeeklyOvertimeRuleEvaluationResult();
		return WeeklyOvertimeSummaryResponseBodyDto.builder()
			.weeklyOvertimeHoursInSeconds((weeklyResult.getWeeklyOvertimeHours() != null)
					? weeklyResult.getWeeklyOvertimeHours().toSeconds() : 0L)
			.weeklyOvertimeHoursInApproximateHours((weeklyResult.getWeeklyOvertimeHours() != null)
					? durationToApproximateHours(weeklyResult.getWeeklyOvertimeHours()) : 0.0f)
			.weeklyOvertimeBillAmount(BigDecimalFormatter.formatMonetaryAmount(
					(weeklyResult.getBillAmount() != null) ? weeklyResult.getBillAmount() : BigDecimal.ZERO))
			.weeklyOvertimePayAmount(BigDecimalFormatter.formatMonetaryAmount(
					(weeklyResult.getPayAmount() != null) ? weeklyResult.getPayAmount() : BigDecimal.ZERO))
			.hasWeeklyOvertime(
					(weeklyResult.getWeeklyOvertimeHours() != null) && !weeklyResult.getWeeklyOvertimeHours().isZero())
			.weeklyOvertimeRuleName(
					(weeklyResult.getRuleName() != null) ? weeklyResult.getRuleName() : "Weekly Overtime")
			.virtualRule(weeklyResult.isVirtualRule())
			.build();
	}

	@Named("convertMoneyData")
	default MoneyDataResponseBodyDto convertMoneyData(RuleEvaluatorResult result) {
		if (result == null) {
			return MoneyDataResponseBodyDto.builder()
				.payAmount(BigDecimalFormatter.formatMonetaryAmount(BigDecimal.ZERO))
				.billAmount(BigDecimalFormatter.formatMonetaryAmount(BigDecimal.ZERO))
				.build();
		}

		MoneyData totalMoneyData = result.getMoneyData();
		if (result.getWeeklyOvertimeMoneyData() != null) {
			totalMoneyData = totalMoneyData.add(result.getWeeklyOvertimeMoneyData());
		}

		return toMoneyDataResponseBodyDto(totalMoneyData);
	}

	@Named("convertToTimeLogRuleEvaluations")
	default List<TimeLogRuleEvaluationResponseBodyDto> convertToTimeLogRuleEvaluations(RuleEvaluatorResult result) {
		if (result == null || !result.hasResults()) {
			return List.of();
		}

		// Group results by timeLogId to aggregate intervals
		Map<Integer, List<Map.Entry<TimeLog, List<RuleEvaluationResult>>>> groupedByTimeLogId = new java.util.LinkedHashMap<>();

		for (Map.Entry<TimeLog, List<RuleEvaluationResult>> entry : result.getRuleEvaluationResults().entrySet()) {
			Integer timeLogId = entry.getKey().getId();
			groupedByTimeLogId.computeIfAbsent(timeLogId, (k) -> new java.util.ArrayList<>()).add(entry);
		}

		List<TimeLogRuleEvaluationResponseBodyDto> timeLogEvaluations = new java.util.ArrayList<>();

		// Process each timeLogId group
		for (Map.Entry<Integer, List<Map.Entry<TimeLog, List<RuleEvaluationResult>>>> groupEntry : groupedByTimeLogId
			.entrySet()) {

			List<Map.Entry<TimeLog, List<RuleEvaluationResult>>> intervalEntries = groupEntry.getValue();

			// Collect all rule evaluation results from all intervals
			List<RuleEvaluationResultResponseBodyDto> allRuleEvaluationDtos = new java.util.ArrayList<>();
			List<RuleEvaluationResult> allRuleResults = new java.util.ArrayList<>();
			List<List<LocalTime>> workIntervals = new java.util.ArrayList<>();
			RangeSet<LocalTime> allOccupiedTimeRanges = TreeRangeSet.create();

			// Use the first interval's time log for common properties (date, etc.)
			TimeLog firstTimeLog = intervalEntries.get(0).getKey();
			LocalTime earliestStartTime = null;
			LocalTime latestEndTime = null;
			float totalActualWorkDuration = 0.0f;

			for (Map.Entry<TimeLog, List<RuleEvaluationResult>> intervalEntry : intervalEntries) {
				TimeLog timeLog = intervalEntry.getKey();
				List<RuleEvaluationResult> ruleResults = intervalEntry.getValue();

				// Track interval time range
				if (timeLog.getWorkStartTime() != null && timeLog.getWorkEndTime() != null) {
					workIntervals.add(List.of(timeLog.getWorkStartTime(), timeLog.getWorkEndTime()));

					// Track earliest start and latest end for overall range
					if (earliestStartTime == null || timeLog.getWorkStartTime().isBefore(earliestStartTime)) {
						earliestStartTime = timeLog.getWorkStartTime();
					}
					if (latestEndTime == null || timeLog.getWorkEndTime().isAfter(latestEndTime)) {
						latestEndTime = timeLog.getWorkEndTime();
					}

					// Sum up work duration
					totalActualWorkDuration += calculateWorkDurationApproximateHours(timeLog.getWorkStartTime(),
							timeLog.getWorkEndTime());
				}

				// Collect rule results
				allRuleResults.addAll(ruleResults);

				// Filter out weekly overtime and convert to DTOs
				List<RuleEvaluationResultResponseBodyDto> ruleEvaluationDtos = ruleResults.stream()
					.filter((ruleResult) -> ruleResult.getRuleType() != RuleType.RANGE_BASED_WEEKLY_OVERTIME
							&& ruleResult.getRuleType() != RuleType.DURATION_BASED_WEEKLY_OVERTIME)
					.map(this::toRuleEvaluationResultResponseBodyDto)
					.toList();
				allRuleEvaluationDtos.addAll(ruleEvaluationDtos);

				// Collect occupied time ranges from this interval
				allOccupiedTimeRanges.addAll(calculateOccupiedTimeRanges(ruleResults));
			}

			// Aggregate rule evaluation results by ruleType
			List<RuleEvaluationResultResponseBodyDto> aggregatedRuleEvaluationDtos = aggregateByRuleType(
					allRuleEvaluationDtos);

			// Calculate total amounts for all intervals
			BigDecimal totalPayAmount = calculateTotalPayAmount(aggregatedRuleEvaluationDtos);
			BigDecimal totalBillAmount = calculateTotalBillAmount(aggregatedRuleEvaluationDtos);

			// Calculate unallocated time ranges across all intervals
			// For multiple intervals, we need to consider gaps between intervals too
			List<List<LocalTime>> unallocatedTimeRanges = calculateUnallocatedTimeRangesForIntervals(workIntervals,
					allOccupiedTimeRanges);
			Float unallocatedTimeApproximateHours = calculateUnallocatedHoursForIntervals(workIntervals,
					allOccupiedTimeRanges);

			// Sort work intervals by start time
			workIntervals.sort((a, b) -> a.get(0).compareTo(b.get(0)));

			// Check if workLogType is 1 (duration-based) to set normalized times
			Integer workLogType = (result.getTimesheet() != null && result.getTimesheet().getTimesheetSetting() != null)
					? result.getTimesheet().getTimesheetSetting().getWorkLogType() : null;
			boolean isDurationBased = (workLogType != null && workLogType == 1);

			TimeLogRuleEvaluationResponseBodyDto timeLogEvaluation = TimeLogRuleEvaluationResponseBodyDto.builder()
				.timeLogId(firstTimeLog.getId())
				.date(firstTimeLog.getDate())
				.startTime(earliestStartTime)
				.endTime(latestEndTime)
				.normalizedStartTime(isDurationBased ? earliestStartTime : null)
				.normalizedEndTime(isDurationBased ? latestEndTime : null)
				.actualWorkDurationApproximateHours(totalActualWorkDuration)
				.normalizedWorkDurationApproximateHours(isDurationBased ? totalActualWorkDuration : 0.0f)
				.ruleEvaluationResults(aggregatedRuleEvaluationDtos)
				.totalPayAmount(BigDecimalFormatter.formatMonetaryAmount(totalPayAmount))
				.totalBillAmount(BigDecimalFormatter.formatMonetaryAmount(totalBillAmount))
				.unallocatedTimeRanges(unallocatedTimeRanges)
				.unallocatedTimeApproximateHours(unallocatedTimeApproximateHours)
				.build();

			timeLogEvaluations.add(timeLogEvaluation);
		}

		// Sort time logs by date in ascending order
		timeLogEvaluations.sort((a, b) -> {
			if (a.getDate() == null && b.getDate() == null) {
				return 0;
			}
			if (a.getDate() == null) {
				return -1;
			}
			if (b.getDate() == null) {
				return 1;
			}
			return a.getDate().compareTo(b.getDate());
		});

		return timeLogEvaluations;
	}

	/**
	 * Aggregates rule evaluation results by ruleType. Combines all entries with the same
	 * ruleType into a single entry with merged timeRanges and summed amounts.
	 */
	default List<RuleEvaluationResultResponseBodyDto> aggregateByRuleType(
			List<RuleEvaluationResultResponseBodyDto> ruleEvaluationDtos) {

		if (ruleEvaluationDtos == null || ruleEvaluationDtos.isEmpty()) {
			return List.of();
		}

		// Group by ruleType
		Map<RuleType, List<RuleEvaluationResultResponseBodyDto>> groupedByRuleType = new java.util.LinkedHashMap<>();
		for (RuleEvaluationResultResponseBodyDto dto : ruleEvaluationDtos) {
			if (dto.getRuleType() != null) {
				groupedByRuleType.computeIfAbsent(dto.getRuleType(), (k) -> new java.util.ArrayList<>()).add(dto);
			}
		}

		List<RuleEvaluationResultResponseBodyDto> aggregatedResults = new java.util.ArrayList<>();

		for (Map.Entry<RuleType, List<RuleEvaluationResultResponseBodyDto>> entry : groupedByRuleType.entrySet()) {
			List<RuleEvaluationResultResponseBodyDto> sameTypeResults = entry.getValue();

			if (sameTypeResults.size() == 1) {
				// Only one result for this type, no aggregation needed
				aggregatedResults.add(sameTypeResults.get(0));
			}
			else {
				// Multiple results for this type, aggregate them
				RuleEvaluationResultResponseBodyDto first = sameTypeResults.get(0);

				// Combine all timeRanges
				List<List<LocalTime>> combinedTimeRanges = new java.util.ArrayList<>();
				prepareAndSortCombinedTimeRangeByStartTime(sameTypeResults, combinedTimeRanges);

				// Sum amounts
				BigDecimal totalPayAmount = sameTypeResults.stream()
					.map(RuleEvaluationResultResponseBodyDto::getPayAmount)
					.filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal totalBillAmount = sameTypeResults.stream()
					.map(RuleEvaluationResultResponseBodyDto::getBillAmount)
					.filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

				// Sum duration
				Long totalDurationSeconds = sameTypeResults.stream()
					.filter((dto) -> dto.getEvaluatedDurationInSeconds() != null)
					.mapToLong(RuleEvaluationResultResponseBodyDto::getEvaluatedDurationInSeconds)
					.sum();

				Float totalApproximateHours = TimeHelper
					.convertDurationToApproximateHours(Duration.ofSeconds(totalDurationSeconds));

				// Build aggregated result
				RuleEvaluationResultResponseBodyDto aggregatedDto = RuleEvaluationResultResponseBodyDto.builder()
					.timeRanges(combinedTimeRanges)
					.ruleType(first.getRuleType())
					.ruleName(first.getRuleName())
					.billAmount(BigDecimalFormatter.formatMonetaryAmount(totalBillAmount))
					.payAmount(BigDecimalFormatter.formatMonetaryAmount(totalPayAmount))
					.evaluatedDurationInSeconds(totalDurationSeconds)
					.evaluatedDurationApproximateHours(totalApproximateHours)
					.successful(first.isSuccessful())
					.errorMessage(first.getErrorMessage())
					.evaluationDate(first.getEvaluationDate())
					.ruleIndex(first.getRuleIndex())
					.virtualRule(first.isVirtualRule())
					.metadata(buildAggregatedMetadata(first.getRuleName(), first.getEvaluationDate(),
							totalDurationSeconds, combinedTimeRanges))
					.build();

				aggregatedResults.add(aggregatedDto);
			}
		}

		// Sort by predefined rule type order
		aggregatedResults.sort((a, b) -> {
			int orderA = getRuleTypeOrder(a.getRuleType());
			int orderB = getRuleTypeOrder(b.getRuleType());
			return Integer.compare(orderA, orderB);
		});

		return aggregatedResults;
	}

	default void prepareAndSortCombinedTimeRangeByStartTime(List<RuleEvaluationResultResponseBodyDto> sameTypeResults,
			List<List<LocalTime>> combinedTimeRanges) {
		for (RuleEvaluationResultResponseBodyDto dto : sameTypeResults) {
			if (dto.getTimeRanges() != null) {
				combinedTimeRanges.addAll(dto.getTimeRanges());
			}
		}
		// Sort time ranges by start time
		combinedTimeRanges.sort((a, b) -> {
			if (a.isEmpty() || b.isEmpty()) {
				return 0;
			}
			return a.getFirst().compareTo(b.getFirst());
		});
	}

	/**
	 * Returns the display order for a rule type. Lower values appear first in the
	 * response. Order: BREAK -> AFTER_SHIFT -> BEFORE_SHIFT -> SPECIFIC_TIME_RANGE ->
	 * REGULAR_HOURS -> DAILY_OVERTIME
	 */
	default int getRuleTypeOrder(RuleType ruleType) {
		if (ruleType == null) {
			return 999;
		}
		return switch (ruleType) {
			case RANGE_BASED_BREAK -> 1;
			case RANGE_BASED_AFTER_SHIFT -> 2;
			case RANGE_BASED_BEFORE_SHIFT -> 3;
			case RANGE_BASED_SPECIFIC_TIME_RANGE -> 4;
			case RANGE_BASED_REGULAR_HOURS -> 5;
			case RANGE_BASED_DAILY_OVERTIME -> 6;
			case RANGE_BASED_WEEKLY_OVERTIME -> 7;
			case RANGE_BASED_DEFAULT_PAY -> 8;
			case DURATION_BASED_BREAK -> 11;
			case DURATION_BASED_SPECIFIC_HOUR_RANGE -> 14;
			case DURATION_BASED_REGULAR_HOURS -> 15;
			case DURATION_BASED_DAILY_OVERTIME -> 16;
			case DURATION_BASED_WEEKLY_OVERTIME -> 17;
			case DURATION_BASED_DEFAULT_PAY -> 18;
		};
	}

	/**
	 * Builds aggregated metadata string for combined rule evaluation results.
	 */
	default String buildAggregatedMetadata(String ruleName, java.time.LocalDate evaluationDate, Long totalSeconds,
			List<List<LocalTime>> timeRanges) {
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		String timeRangesStr = timeRanges.stream()
			.map((range) -> "[" + range.getFirst() + ".." + range.get(1) + ")")
			.collect(java.util.stream.Collectors.joining(", "));
		return String.format("Rule: %s, Date: %s, Duration: %dh %dm, TimeRanges: [%s]", ruleName, evaluationDate, hours,
				minutes, timeRangesStr);
	}

	/**
	 * Calculates unallocated time ranges for multiple work intervals. Unallocated time is
	 * the portion of work intervals not covered by any rule.
	 */
	default List<List<LocalTime>> calculateUnallocatedTimeRangesForIntervals(List<List<LocalTime>> workIntervals,
			RangeSet<LocalTime> occupiedTimeRanges) {

		if (workIntervals == null || workIntervals.isEmpty()) {
			return List.of();
		}

		RangeSet<LocalTime> unallocatedRanges = TreeRangeSet.create();

		// For each work interval, find the unallocated portion
		for (List<LocalTime> interval : workIntervals) {
			if (interval.size() >= 2) {
				LocalTime start = interval.get(0);
				LocalTime end = interval.get(1);
				if (start != null && end != null && start.isBefore(end)) {
					Range<LocalTime> intervalRange = TimeHelper.toRange(start, end);
					RangeSet<LocalTime> intervalUnallocated = TimeHelper.getFreeTimeRanges(intervalRange,
							occupiedTimeRanges);
					unallocatedRanges.addAll(intervalUnallocated);
				}
			}
		}

		if (unallocatedRanges.isEmpty()) {
			return List.of();
		}

		return unallocatedRanges.asRanges()
			.stream()
			.map((range) -> List.of(range.lowerEndpoint(), range.upperEndpoint()))
			.toList();
	}

	/**
	 * Calculates approximate hours of unallocated time for multiple work intervals.
	 */
	default Float calculateUnallocatedHoursForIntervals(List<List<LocalTime>> workIntervals,
			RangeSet<LocalTime> occupiedTimeRanges) {

		if (workIntervals == null || workIntervals.isEmpty()) {
			return 0.0f;
		}

		Duration totalUnallocated = Duration.ZERO;

		// For each work interval, calculate unallocated duration
		for (List<LocalTime> interval : workIntervals) {
			if (interval.size() >= 2) {
				LocalTime start = interval.get(0);
				LocalTime end = interval.get(1);
				if (start != null && end != null && start.isBefore(end)) {
					Range<LocalTime> intervalRange = TimeHelper.toRange(start, end);
					RangeSet<LocalTime> intervalUnallocated = TimeHelper.getFreeTimeRanges(intervalRange,
							occupiedTimeRanges);
					totalUnallocated = totalUnallocated.plus(TimeHelper.convertRangeSetToDuration(intervalUnallocated));
				}
			}
		}

		return TimeHelper.convertDurationToApproximateHours(totalUnallocated);
	}

	/**
	 * Calculates the total pay amount for a list of rule evaluation results.
	 * @param ruleEvaluationDtos the list of rule evaluation results
	 * @return the total pay amount
	 */
	default BigDecimal calculateTotalPayAmount(List<RuleEvaluationResultResponseBodyDto> ruleEvaluationDtos) {
		if (ruleEvaluationDtos == null) {
			return BigDecimal.ZERO;
		}

		return ruleEvaluationDtos.stream()
			.filter((result) -> result.getPayAmount() != null)
			.map(RuleEvaluationResultResponseBodyDto::getPayAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Calculates the total bill amount for a list of rule evaluation results.
	 * @param ruleEvaluationDtos the list of rule evaluation results
	 * @return the total bill amount
	 */
	default BigDecimal calculateTotalBillAmount(List<RuleEvaluationResultResponseBodyDto> ruleEvaluationDtos) {
		if (ruleEvaluationDtos == null) {
			return BigDecimal.ZERO;
		}

		return ruleEvaluationDtos.stream()
			.filter((result) -> result.getBillAmount() != null)
			.map(RuleEvaluationResultResponseBodyDto::getBillAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Calculates the occupied time ranges from rule evaluation results
	 */
	default RangeSet<LocalTime> calculateOccupiedTimeRanges(List<RuleEvaluationResult> ruleResults) {
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();

		for (RuleEvaluationResult ruleResult : ruleResults) {
			if (ruleResult.getTimeRange() != null && !ruleResult.getTimeRange().isEmpty()) {
				occupiedRanges.addAll(ruleResult.getTimeRange());
			}
		}

		return occupiedRanges;
	}

	/**
	 * Calculates unallocated time ranges for a time log
	 */
	default List<List<LocalTime>> calculateUnallocatedTimeRanges(TimeLog timeLog,
			RangeSet<LocalTime> occupiedTimeRanges) {

		LocalTime effectiveStartTime = TimeHelper.getEffectiveStartTime(timeLog);
		LocalTime effectiveEndTime = TimeHelper.getEffectiveEndTime(timeLog);

		// Validate time log has valid start and end times
		if (effectiveStartTime == null || effectiveEndTime == null || !effectiveStartTime.isBefore(effectiveEndTime)) {
			return List.of();
		}

		// Create the full working day range
		Range<LocalTime> fullDayRange = TimeHelper.toRange(effectiveStartTime, effectiveEndTime);

		// Get unallocated time ranges
		RangeSet<LocalTime> unallocatedRanges = TimeHelper.getFreeTimeRanges(fullDayRange, occupiedTimeRanges);

		if (unallocatedRanges.isEmpty()) {
			return List.of();
		}

		// Convert ranges to response format
		return unallocatedRanges.asRanges()
			.stream()
			.map((range) -> List.of(range.lowerEndpoint(), range.upperEndpoint()))
			.toList();
	}

	/**
	 * Calculates approximate hours of unallocated time
	 */
	default Float calculateUnallocatedTimeApproximateHours(TimeLog timeLog, RangeSet<LocalTime> occupiedTimeRanges) {

		LocalTime effectiveStartTime = TimeHelper.getEffectiveStartTime(timeLog);
		LocalTime effectiveEndTime = TimeHelper.getEffectiveEndTime(timeLog);

		// Validate time log has valid start and end times
		if (effectiveStartTime == null || effectiveEndTime == null || !effectiveStartTime.isBefore(effectiveEndTime)) {
			return 0.0f;
		}

		// Create the full working day range
		Range<LocalTime> fullDayRange = TimeHelper.toRange(effectiveStartTime, effectiveEndTime);

		// Get unallocated time ranges
		RangeSet<LocalTime> unallocatedRanges = TimeHelper.getFreeTimeRanges(fullDayRange, occupiedTimeRanges);

		if (unallocatedRanges.isEmpty()) {
			return 0.0f;
		}

		// Calculate total duration
		Duration totalUnallocatedDuration = TimeHelper.convertRangeSetToDuration(unallocatedRanges);
		return TimeHelper.convertDurationToApproximateHours(totalUnallocatedDuration);
	}

	@Named("durationToSeconds")
	default Long durationToSeconds(Duration duration) {
		if (duration == null) {
			return 0L;
		}
		return duration.toSeconds();
	}

	@Named("durationToApproximateHours")
	default Float durationToApproximateHours(Duration duration) {
		return TimeHelper.convertDurationToApproximateHours(duration);
	}

	@Named("calculateWorkDurationApproximateHours")
	default Float calculateWorkDurationApproximateHours(LocalTime startTime, LocalTime endTime) {
		Duration duration = TimeHelper.calculateDuration(startTime, endTime);
		return TimeHelper.convertDurationToApproximateHours(duration);
	}

}