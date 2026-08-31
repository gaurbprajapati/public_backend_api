package io.recruitcrm.microservice.timesheet.dto.time_log;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimeDetailDto {

	private Integer id;

	@NotNull(message = "Work start time cannot be null")
	private Integer workStartTime;

	@NotNull(message = "Work end time cannot be null")
	private Integer workEndTime;

	private String rangeBasedRemark;

	private Integer rangeBasedBreakTime;

	private List<BreakIntervalDto> breakIntervals;

}
