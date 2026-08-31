/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEvaluationContextTests {

	@Test
	@DisplayName("Default constructor - Success")
	void testDefaultConstructorSuccess() {
		// Act
		RuleEvaluationContext context = new RuleEvaluationContext();

		// Assert
		assertThat(context).isNotNull()
			.satisfies((ctx) -> assertThat(ctx.getTimesheet()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSetting()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSettingDto()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getTimeRangesToEvaluate()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getCurrentTimeLogBeingEvaluated()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleBeingEvaluated()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleIndex()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getWeeklyTimeLogs()).isNull())
			.satisfies((ctx) -> assertThat(ctx.getWeeklyOvertimeCandidateTimeRanges()).isNull());
	}

	@Test
	@DisplayName("All args constructor - Success")
	void testAllArgsConstructorSuccess() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		io.recruitcrm.contract_staffing.entity.model.TimesheetSetting timesheetSetting = new io.recruitcrm.contract_staffing.entity.model.TimesheetSetting();
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting();
		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		TimeLog currentTimeLog = new TimeLog();
		CustomRule currentRule = new CustomRule();
		Integer currentRuleIndex = 1;
		List<TimeLog> weeklyTimeLogs = Arrays.asList(new TimeLog(), new TimeLog());
		List<RangeSet<LocalTime>> weeklyOvertimeRanges = Arrays.asList(TreeRangeSet.create(), TreeRangeSet.create());

		// Act
		RuleEvaluationContext context = new RuleEvaluationContext(timesheet, timesheetSetting, timesheetSettingDto,
				timeRanges, currentTimeLog, currentRule, currentRuleIndex, weeklyTimeLogs, weeklyOvertimeRanges);

		// Assert
		assertThat(context).satisfies((ctx) -> assertThat(ctx.getTimesheet()).isEqualTo(timesheet))
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSetting()).isEqualTo(timesheetSetting))
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSettingDto()).isEqualTo(timesheetSettingDto))
			.satisfies((ctx) -> assertThat(ctx.getTimeRangesToEvaluate()).isEqualTo(timeRanges))
			.satisfies((ctx) -> assertThat(ctx.getCurrentTimeLogBeingEvaluated()).isEqualTo(currentTimeLog))
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleBeingEvaluated()).isEqualTo(currentRule))
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleIndex()).isEqualTo(currentRuleIndex))
			.satisfies((ctx) -> assertThat(ctx.getWeeklyTimeLogs()).isEqualTo(weeklyTimeLogs))
			.satisfies((ctx) -> assertThat(ctx.getWeeklyOvertimeCandidateTimeRanges()).isEqualTo(weeklyOvertimeRanges));
	}

	@Test
	@DisplayName("Builder pattern - Success")
	void testBuilderPatternSuccess() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		io.recruitcrm.contract_staffing.entity.model.TimesheetSetting timesheetSetting = new io.recruitcrm.contract_staffing.entity.model.TimesheetSetting();
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting();
		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		TimeLog currentTimeLog = new TimeLog();
		CustomRule currentRule = new CustomRule();
		Integer currentRuleIndex = 2;
		List<TimeLog> weeklyTimeLogs = Collections.singletonList(new TimeLog());
		List<RangeSet<LocalTime>> weeklyOvertimeRanges = Collections.singletonList(TreeRangeSet.create());

		// Act
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(timesheet)
			.timesheetSetting(timesheetSetting)
			.timesheetSettingDto(timesheetSettingDto)
			.timeRangesToEvaluate(timeRanges)
			.currentTimeLogBeingEvaluated(currentTimeLog)
			.currentRuleBeingEvaluated(currentRule)
			.currentRuleIndex(currentRuleIndex)
			.weeklyTimeLogs(weeklyTimeLogs)
			.weeklyOvertimeCandidateTimeRanges(weeklyOvertimeRanges)
			.build();

		// Assert
		assertThat(context).satisfies((ctx) -> assertThat(ctx.getTimesheet()).isEqualTo(timesheet))
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSetting()).isEqualTo(timesheetSetting))
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSettingDto()).isEqualTo(timesheetSettingDto))
			.satisfies((ctx) -> assertThat(ctx.getTimeRangesToEvaluate()).isEqualTo(timeRanges))
			.satisfies((ctx) -> assertThat(ctx.getCurrentTimeLogBeingEvaluated()).isEqualTo(currentTimeLog))
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleBeingEvaluated()).isEqualTo(currentRule))
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleIndex()).isEqualTo(currentRuleIndex))
			.satisfies((ctx) -> assertThat(ctx.getWeeklyTimeLogs()).isEqualTo(weeklyTimeLogs))
			.satisfies((ctx) -> assertThat(ctx.getWeeklyOvertimeCandidateTimeRanges()).isEqualTo(weeklyOvertimeRanges));
	}

	@Test
	@DisplayName("Setters and getters - All fields")
	void testSettersAndGettersAllFields() {
		// Arrange
		RuleEvaluationContext context = new RuleEvaluationContext();
		Timesheet timesheet = new Timesheet();
		io.recruitcrm.contract_staffing.entity.model.TimesheetSetting timesheetSetting = new io.recruitcrm.contract_staffing.entity.model.TimesheetSetting();
		io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting timesheetSettingDto = new io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting();
		RangeSet<LocalTime> timeRanges = TreeRangeSet.create();
		TimeLog currentTimeLog = new TimeLog();
		CustomRule currentRule = new CustomRule();
		Integer currentRuleIndex = 3;
		List<TimeLog> weeklyTimeLogs = Arrays.asList(new TimeLog(), new TimeLog(), new TimeLog());
		List<RangeSet<LocalTime>> weeklyOvertimeRanges = Arrays.asList(TreeRangeSet.create(), TreeRangeSet.create());

		// Act
		context.setTimesheet(timesheet);
		context.setTimesheetSetting(timesheetSetting);
		context.setTimesheetSettingDto(timesheetSettingDto);
		context.setTimeRangesToEvaluate(timeRanges);
		context.setCurrentTimeLogBeingEvaluated(currentTimeLog);
		context.setCurrentRuleBeingEvaluated(currentRule);
		context.setCurrentRuleIndex(currentRuleIndex);
		context.setWeeklyTimeLogs(weeklyTimeLogs);
		context.setWeeklyOvertimeCandidateTimeRanges(weeklyOvertimeRanges);

		// Assert
		assertThat(context).satisfies((ctx) -> assertThat(ctx.getTimesheet()).isEqualTo(timesheet))
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSetting()).isEqualTo(timesheetSetting))
			.satisfies((ctx) -> assertThat(ctx.getTimesheetSettingDto()).isEqualTo(timesheetSettingDto))
			.satisfies((ctx) -> assertThat(ctx.getTimeRangesToEvaluate()).isEqualTo(timeRanges))
			.satisfies((ctx) -> assertThat(ctx.getCurrentTimeLogBeingEvaluated()).isEqualTo(currentTimeLog))
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleBeingEvaluated()).isEqualTo(currentRule))
			.satisfies((ctx) -> assertThat(ctx.getCurrentRuleIndex()).isEqualTo(currentRuleIndex))
			.satisfies((ctx) -> assertThat(ctx.getWeeklyTimeLogs()).isEqualTo(weeklyTimeLogs))
			.satisfies((ctx) -> assertThat(ctx.getWeeklyOvertimeCandidateTimeRanges()).isEqualTo(weeklyOvertimeRanges));
	}

	@Test
	@DisplayName("Equals and hashCode - Same objects")
	void testEqualsAndHashCodeSameObjects() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		RuleEvaluationContext context1 = RuleEvaluationContext.builder()
			.timesheet(timesheet)
			.currentRuleIndex(1)
			.build();

		RuleEvaluationContext context2 = RuleEvaluationContext.builder()
			.timesheet(timesheet)
			.currentRuleIndex(1)
			.build();

		// Act & Assert
		assertThat(context1).isEqualTo(context2).hasSameHashCodeAs(context2);
	}

	@Test
	@DisplayName("Equals and hashCode - Different objects")
	void testEqualsAndHashCodeDifferentObjects() {
		// Arrange
		Timesheet timesheet1 = new Timesheet();
		Timesheet timesheet2 = new Timesheet();
		RuleEvaluationContext context1 = RuleEvaluationContext.builder()
			.timesheet(timesheet1)
			.currentRuleIndex(1)
			.build();

		RuleEvaluationContext context2 = RuleEvaluationContext.builder()
			.timesheet(timesheet2)
			.currentRuleIndex(1)
			.build();

		// Act & Assert
		assertThat(context1).isNotEqualTo(context2).doesNotHaveSameHashCodeAs(context2);
	}

	@Test
	@DisplayName("ToString - Contains all fields")
	void testToStringContainsAllFields() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		RuleEvaluationContext context = RuleEvaluationContext.builder()
			.timesheet(timesheet)
			.currentRuleIndex(1)
			.build();

		// Act
		String result = context.toString();

		// Assert
		assertThat(result).contains("timesheet=")
			.contains("timesheetSetting=null")
			.contains("timesheetSettingDto=null")
			.contains("timeRangesToEvaluate=null")
			.contains("currentTimeLogBeingEvaluated=null")
			.contains("currentRuleBeingEvaluated=null")
			.contains("currentRuleIndex=1")
			.contains("weeklyTimeLogs=null")
			.contains("weeklyOvertimeCandidateTimeRanges=null");
	}

	@Test
	@DisplayName("ToString - Null fields")
	void testToStringNullFields() {
		// Arrange
		RuleEvaluationContext context = new RuleEvaluationContext();

		// Act
		String result = context.toString();

		// Assert
		assertThat(result).contains("timesheet=null")
			.contains("timesheetSetting=null")
			.contains("timesheetSettingDto=null")
			.contains("timeRangesToEvaluate=null")
			.contains("currentTimeLogBeingEvaluated=null")
			.contains("currentRuleBeingEvaluated=null")
			.contains("currentRuleIndex=null")
			.contains("weeklyTimeLogs=null")
			.contains("weeklyOvertimeCandidateTimeRanges=null");
	}

}