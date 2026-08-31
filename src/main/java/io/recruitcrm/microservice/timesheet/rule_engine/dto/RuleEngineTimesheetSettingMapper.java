/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.contract_staffing.entity.configuration.Generated;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Generated
public interface RuleEngineTimesheetSettingMapper {

	RuleEngineTimesheetSettingMapper INSTANCE = Mappers.getMapper(RuleEngineTimesheetSettingMapper.class);

	@Mapping(source = "timesheetFrequency", target = "timesheetFrequency",
			qualifiedByName = "fromIdToTimesheetFrequency")
	@Mapping(source = "workLogType", target = "workLogType", qualifiedByName = "fromIdToWorkLogType")
	@Mapping(source = "calculateBreakTime", target = "calculateBreakTime")
	@Mapping(source = "breakTimeThreshold", target = "breakTimeThreshold", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "accountId", target = "accountId")
	@Mapping(source = "templateWorkDay", target = "templateWorkDays")
	@Mapping(source = "customRule", target = "customRules")
	io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting toTimesheetSetting(
			TimesheetSetting timesheetSetting);

	@Mapping(source = "workDayId", target = "workDayType", qualifiedByName = "fromIdToWorkDayType")
	@Mapping(source = "workTime", target = "workTime", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "workStartTime", target = "workStartTime", qualifiedByName = "fromSecondsToLocalTime")
	@Mapping(source = "workEndTime", target = "workEndTime", qualifiedByName = "fromSecondsToLocalTime")
	io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay toTemplateWorkDay(
			TemplateWorkDay templateWorkDay);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "ruleName", target = "ruleName")
	@Mapping(source = "workDayId", target = "workDays", qualifiedByName = "fromIdToWorkDayType")
	@Mapping(source = "ruleType", target = "ruleType", qualifiedByName = "fromIdToRuleType")
	@Mapping(source = "startTime", target = "startTime", qualifiedByName = "fromSecondsToLocalTime")
	@Mapping(source = "endTime", target = "endTime", qualifiedByName = "fromSecondsToLocalTime")
	@Mapping(source = "dailyThreshold", target = "dailyThreshold", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "weeklyThreshold", target = "weeklyThreshold", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "chargeMethod", target = "chargeMethod", qualifiedByName = "fromIdToChargeMethodType")
	@Mapping(source = "payRateMultiplier", target = "payRateMultiplier")
	@Mapping(source = "billRateMultiplier", target = "billRateMultiplier")
	@Mapping(source = "payRatePerHour", target = "payRatePerHour")
	@Mapping(source = "billRatePerHour", target = "billRatePerHour")
	@Mapping(source = "startDuration", target = "startDuration", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "endDuration", target = "endDuration", qualifiedByName = "fromSecondsToDuration")
	CustomRule toCustomRule(io.recruitcrm.contract_staffing.entity.model.CustomRule customRule);

	List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay> toTemplateWorkDayList(
			List<TemplateWorkDay> templateWorkDays);

	List<CustomRule> toCustomRuleList(List<io.recruitcrm.contract_staffing.entity.model.CustomRule> customRules);

	@Named("fromSecondsToDuration")
	default Duration fromSecondsToDuration(Long seconds) {
		return (seconds != null) ? Duration.ofSeconds(seconds) : null;
	}

	@Named("fromSecondsToLocalTime")
	default LocalTime fromSecondsToLocalTime(Long seconds) {
		return (seconds != null) ? LocalTime.ofSecondOfDay(seconds) : null;
	}

	@Named("fromIdToWorkDayType")
	default WorkDay fromIdToWorkDayType(Integer id) {
		return (id != null) ? WorkDay.getWorkDayType(id) : null;
	}

	@Named("fromIdToTimesheetFrequency")
	default TimesheetFrequency fromIdToTimesheetFrequency(Integer id) {
		if (id == null || id == 0) {
			return null;
		}
		try {
			return TimesheetFrequency.valueOf(id);
		}
		catch (IllegalArgumentException ex) {
			// Return null for invalid frequency IDs instead of throwing exception
			return null;
		}
	}

	@Named("fromIdToWorkLogType")
	default WorkLogType fromIdToWorkLogType(Integer id) {
		return (id != null) ? WorkLogType.valueOf(id) : null;
	}

	@Named("fromIdToRuleType")
	default RuleType fromIdToRuleType(Integer id) {
		return (id != null) ? RuleType.fromId(id) : null;
	}

	@Named("fromIdToChargeMethodType")
	default ChargeMethodType fromIdToChargeMethodType(Integer id) {
		return (id != null) ? ChargeMethodType.fromId(id) : null;
	}

	@Named("fromEpochSecondsToInstant")
	default Instant fromEpochSecondsToInstant(Integer epochSeconds) {
		return (epochSeconds != null) ? Instant.ofEpochSecond(epochSeconds) : null;
	}

}
