package io.recruitcrm.microservice.timesheet.dto.time_log;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogResponseBodyDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	@NotNull(message = "Timesheet id cannot be null")
	private Integer timesheetId;

	@NotNull(message = "Timesheet period cannot be null")
	private String timesheetPeriod;

	@NotNull(message = "Date cannot be null")
	private Integer date;

	@NotNull(message = "Day type cannot be null")
	private Integer dayTypeId;

	private Integer workTime;

	private Integer workStartTime;

	private Integer workEndTime;

	/**
	 * Comma-separated work hours display (e.g. "07:00-19:00, 20:00-21:00") when multiple
	 * intervals exist per day. Derived from workTimeDetails.
	 */
	private String workHoursDisplay;

	private Integer breakTime;

	private List<BreakIntervalResponseBodyDto> breakIntervals;

	private List<WorkTimeDetailResponseBodyDto> workTimeDetails;

	private Integer overTime;

	private String remark;

	private Integer totalTime;

}
