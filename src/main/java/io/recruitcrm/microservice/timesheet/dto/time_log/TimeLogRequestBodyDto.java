package io.recruitcrm.microservice.timesheet.dto.time_log;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogRequestBodyDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	private Integer workTime;

	private Integer workStartTime;

	private Integer workEndTime;

	private Integer breakTime;

	private Integer overTime;

	private String remark;

	private Integer totalTime;

	private List<BreakIntervalRequestBodyDto> breakIntervals;

}