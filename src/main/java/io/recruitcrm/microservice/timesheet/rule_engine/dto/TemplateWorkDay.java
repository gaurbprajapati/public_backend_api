/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TemplateWorkDay {

	WorkDay workDayType;

	Duration workTime;

	LocalTime workStartTime;

	LocalTime workEndTime;

}
