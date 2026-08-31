package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for time log migration query result. Contains fields needed to migrate from
 * cst_time_log_t to cst_time_log_interval_t.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogMigrationQueryResultDto {

	private Integer id;

	private Integer workStartTime;

	private Integer workEndTime;

	private String remark;

	private Integer workLogType;

	private Integer workTime;

	private Integer timesheetApprovalStatusTypeId;

}
