/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetMigrationResponseBodyDto {

	private List<SuccessfulMigrationDto> successfulMigrations;

	private List<FailedMigrationDto> failedMigrations;

	private int totalProcessed;

	private int successCount;

	private int failureCount;

	/**
	 * Whether more records exist. If true, call again with nextOffset.
	 */
	private boolean hasMore;

	/**
	 * Offset to use for next batch. Always present in response.
	 */
	private Integer nextOffset;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SuccessfulMigrationDto {

		private Integer timesheetId;

		private List<Integer> timeLogIds;

		private Integer totalTime;

		private Integer totalWorkTime;

		private Integer totalOvertime;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FailedMigrationDto {

		private Integer timesheetId;

		private String errorMessage;

	}

}
