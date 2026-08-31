/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.contract_staffing.entity.configuration.Generated;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Generated
public interface RuleEngineTimeLogMapper {

	RuleEngineTimeLogMapper INSTANCE = Mappers.getMapper(RuleEngineTimeLogMapper.class);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "date", target = "date", qualifiedByName = "fromSecondsToLocalDate")
	@Mapping(source = "dayTypeId", target = "dayType", qualifiedByName = "fromIdToWorkDayType")
	@Mapping(source = "workTime", target = "workTime", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "workStartTime", target = "workStartTime", qualifiedByName = "fromSecondsToLocalTime")
	@Mapping(source = "workEndTime", target = "workEndTime", qualifiedByName = "fromSecondsToLocalTime")
	@Mapping(source = "breakTime", target = "breakTime", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "overTime", target = "overTime", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "remark", target = "remark")
	@Mapping(source = "totalTime", target = "totalTime", qualifiedByName = "fromSecondsToDuration")
	@Mapping(source = "timesheet", target = "timesheet")
	@Mapping(source = "payData", target = "payData")
	@Mapping(source = "billData", target = "billData")
	@Mapping(target = "normalizedWorkStartTime", ignore = true)
	@Mapping(target = "normalizedWorkEndTime", ignore = true)
	@Mapping(target = "breakIntervals", ignore = true) // Break intervals now fetched from
														// cst_time_log_interval_t.break_interval
														// JSON
	TimeLog toTimeLog(io.recruitcrm.contract_staffing.entity.model.TimeLog timeLog);

	@Named("fromSecondsToLocalDate")
	default LocalDate fromSecondsToLocalDate(Integer seconds) {
		if (seconds == null) {
			return null;
		}
		return LocalDate.ofEpochDay(seconds / 86400L);
	}

	@Named("fromIdToWorkDayType")
	default WorkDayType fromIdToWorkDayType(Integer id) {
		if (id == null) {
			return null;
		}
		return WorkDayType.fromId(id);
	}

	@Named("fromSecondsToDuration")
	default Duration fromSecondsToDuration(Integer seconds) {
		if (seconds == null) {
			return null;
		}
		return Duration.ofSeconds(seconds);
	}

	@Named("fromSecondsToLocalTime")
	default LocalTime fromSecondsToLocalTime(Integer seconds) {
		if (seconds == null) {
			return null;
		}
		return LocalTime.ofSecondOfDay(seconds);
	}

}
