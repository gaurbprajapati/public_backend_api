/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.TimesheetFrequency;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetSetting {

	private Integer id;

	private Integer jobStartDate;

	private Integer jobEndDate;

	private TimesheetFrequency timesheetFrequency;

	private Integer timesheetStartDay;

	private Integer payCurrencyId;

	private Integer billCurrencyId;

	private float billRate;

	private float payRate;

	private Integer isRemarkMandatory;

	private WorkLogType workLogType;

	private Boolean calculateBreakTime;

	private Duration breakTimeThreshold;

	private List<TemplateWorkDay> templateWorkDays;

	private List<CustomRule> customRules;

	private Integer accountId;

	private Integer createdOn;

	private Integer createdBy;

	private Integer createdByUserTypeId;

}
