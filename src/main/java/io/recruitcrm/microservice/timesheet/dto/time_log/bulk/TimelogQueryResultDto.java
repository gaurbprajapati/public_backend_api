package io.recruitcrm.microservice.timesheet.dto.time_log.bulk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelogQueryResultDto {

	private Integer id;

	private Integer date;

	private Integer dayTypeId;

	private Integer workTime;

	private Integer workStartTime;

	private Integer workEndTime;

	private Integer breakTime;

	private Integer overTime;

	private String remark;

	private Integer totalTime;

	private Integer timesheetId;

	private Integer periodStart;

	private Integer periodEnd;

}
