package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a single timesheet's time summary in bulk update payload. Maps to timesheet
 * columns: total_time, total_work_time, total_overtime.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetTimeDetailDto {

	private Integer timesheetId;

	private Integer totalWorkTime;

	private Integer totalOvertime;

	private Integer totalTime;

}
