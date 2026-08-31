/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogMigrationDto {

	private Integer timesheetId;

	private Integer timeLogId;

	private Integer totalTime;

	private Integer overTime;

	private Integer workTime;

	private Integer workStartTime;

	private Integer workEndTime;

}
