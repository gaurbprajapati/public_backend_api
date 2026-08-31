package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for time log upsert operations Contains values for batch upsert: [id, date,
 * day_type_id, timesheet_id, remark, break_time, over_time, total_time, work_time]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogUpsertDto {

	private Integer id;

	private Integer date;

	private Integer dayTypeId;

	private Integer timesheetId;

	private String remark;

	private Integer breakTime;

	private Integer overTime;

	private Integer totalTime;

	private Integer workTime;

}
