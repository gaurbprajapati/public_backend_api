/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;

/**
 * DTO representing a single work interval within a range-based time log. Supports the new
 * multiple intervals per day feature where each day can have up to 10 time log intervals.
 *
 * Each interval represents a continuous period of work (e.g., 9:00 AM - 10:00 AM) with
 * work_start_time and work_end_time fetched from cst_time_log_interval_t table. Each
 * interval is evaluated separately by the rule engine.
 *
 * Note: This is used only for range-based time logs. Duration-based time logs still use a
 * single work_time value from the TimeLog entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogWorkInterval {

	/**
	 * Interval ID from cst_time_log_interval_t
	 */
	private Integer id;

	/**
	 * Reference to the parent time log
	 */
	private Integer timeLogId;

	/**
	 * Back reference to the parent TimeLog DTO
	 */
	private TimeLog timeLog;

	/**
	 * Start time of this work interval (fetched from
	 * cst_time_log_interval_t.work_start_time)
	 */
	private LocalTime workStartTime;

	/**
	 * End time of this work interval (fetched from cst_time_log_interval_t.work_end_time)
	 */
	private LocalTime workEndTime;

	/**
	 * Duration of work for this interval (calculated as end - start)
	 */
	private Duration workTime;

	/**
	 * Normalized start time for rule evaluation. Typically the same as workStartTime but
	 * may be adjusted during rule evaluation.
	 */
	private LocalTime normalizedWorkStartTime;

	/**
	 * Normalized end time for rule evaluation. Typically the same as workEndTime but may
	 * be adjusted during rule evaluation.
	 */
	private LocalTime normalizedWorkEndTime;

	/**
	 * Remarks for the time log entry
	 */
	private String rangeBasedRemark;

}
