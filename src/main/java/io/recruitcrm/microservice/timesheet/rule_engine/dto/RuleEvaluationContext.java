/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import com.google.common.collect.RangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RuleEvaluationContext {

	private Timesheet timesheet;

	private io.recruitcrm.contract_staffing.entity.model.TimesheetSetting timesheetSetting;

	private TimesheetSetting timesheetSettingDto;

	private RangeSet<LocalTime> timeRangesToEvaluate;

	private TimeLog currentTimeLogBeingEvaluated;

	private CustomRule currentRuleBeingEvaluated;

	private Integer currentRuleIndex;

	private List<TimeLog> weeklyTimeLogs;

	private List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges;

}
