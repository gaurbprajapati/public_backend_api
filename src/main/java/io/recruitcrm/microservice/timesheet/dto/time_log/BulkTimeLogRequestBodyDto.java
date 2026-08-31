package io.recruitcrm.microservice.timesheet.dto.time_log;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkTimeLogRequestBodyDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	@NotNull(message = "Date cannot be null")
	private Integer date;

	@NotNull(message = "Day type ID cannot be null")
	private Integer dayTypeId;

	@NotNull(message = "Timesheet ID cannot be null")
	private Integer timesheetId;

	private String timesheetPeriod;

	private Integer workTime;

	private Integer breakTime;

	private List<WorkTimeDetailDto> workTimeDetails;

	private Integer overTime;

	private String remark;

	private Integer totalTime;

}