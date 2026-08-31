/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeLog {

	private Integer id;

	private LocalDate date;

	private WorkDayType dayType;

	private Duration workTime;

	private LocalTime workStartTime;

	private LocalTime workEndTime;

	/**
	 * Normalized work start time for rule evaluation This field stores the normalized
	 * start time after applying rule-based adjustments
	 */
	private LocalTime normalizedWorkStartTime;

	/**
	 * Normalized work end time for rule evaluation This field stores the normalized end
	 * time after applying rule-based adjustments
	 */
	private LocalTime normalizedWorkEndTime;

	private Duration breakTime;

	private Duration overTime;

	private String remark;

	private Duration totalTime;

	private Timesheet timesheet;

	private float payData;

	private float billData;

	private List<TimeLogBreakInterval> breakIntervals;

	/**
	 * Work intervals for this time log when workLogType is START_AND_END_TIME (2). Each
	 * time log can have multiple work intervals (up to 10) representing discontinuous
	 * work periods within a single day.
	 */
	private List<TimeLogWorkInterval> workIntervals;

}
