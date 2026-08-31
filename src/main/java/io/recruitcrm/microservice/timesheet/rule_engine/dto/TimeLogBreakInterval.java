/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogBreakInterval {

	private Integer id;

	private Integer timeLogId;

	private TimeLog timeLog;

	private LocalTime breakStartTime;

	private LocalTime breakEndTime;

}
