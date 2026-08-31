/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.time_log_interval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for TimeLogInterval data from cst_time_log_interval_t table. Holds raw interval
 * data with work start and end times as seconds of day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogIntervalDto {

	/**
	 * Interval ID from cst_time_log_interval_t
	 */
	private Integer id;

	/**
	 * Reference to cst_time_log_t.id
	 */
	private Integer timeLogId;

	/**
	 * Work start time as seconds of day (0-86399)
	 */
	private Integer workStartTime;

	/**
	 * Work end time as seconds of day (0-86399)
	 */
	private Integer workEndTime;

	/**
	 * Remarks for the time log entry
	 */
	private String rangeBasedRemark;

	/**
	 * Break interval data in JSON format
	 */
	private String breakInterval;

}
