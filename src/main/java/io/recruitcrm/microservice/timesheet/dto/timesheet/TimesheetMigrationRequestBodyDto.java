/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetMigrationRequestBodyDto {

	/**
	 * Number of timesheets to process in this batch. Default: 100
	 */
	@Min(value = 1, message = "Batch size must be at least 1")
	@Max(value = 500, message = "Batch size must not exceed 500")
	private Integer batchSize = 100;

	/**
	 * Offset for pagination. Use 0 for first batch, then nextOffset from previous
	 * response for subsequent calls. Ignored when timesheetId is provided.
	 */
	@Min(value = 0, message = "Offset must be at least 0")
	private Integer offset = 0;

	/**
	 * When provided, migrate only this timesheet. Batch mode (batchSize, offset) is
	 * skipped. When not provided, batch migration runs as per batchSize and offset.
	 */
	private Integer timesheetId;

}
