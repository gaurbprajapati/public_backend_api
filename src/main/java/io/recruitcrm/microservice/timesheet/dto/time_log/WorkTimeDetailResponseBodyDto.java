package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimeDetailResponseBodyDto {

	private Integer id;

	private Integer workStartTime;

	private Integer workEndTime;

	private String rangeBasedRemark;

	private Integer rangeBasedBreakTime;

	private List<BreakIntervalResponseBodyDto> breakIntervals;

}
