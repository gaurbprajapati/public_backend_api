package io.recruitcrm.microservice.timesheet.dto.time_log.bulk;

import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelogResponseBodyDto {

	private Integer id;

	private Integer date;

	private Integer dayTypeId;

	private Integer workTime;

	private Integer workStartTime;

	private Integer workEndTime;

	private Integer breakTime;

	private List<BreakIntervalResponseBodyDto> breakIntervals;

	private List<WorkTimeDetailResponseBodyDto> workTimeDetails;

	private Integer overTime;

	private String remark;

	private Integer totalTime;

	private Integer timesheetId;

	private String timesheetPeriod;

}
