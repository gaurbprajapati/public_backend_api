package io.recruitcrm.microservice.timesheet.dto.time_log;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreakIntervalDto {

	private Integer id;

	@NotNull(message = "Break start time cannot be null")
	private Integer breakStartTime;

	@NotNull(message = "Break end time cannot be null")
	private Integer breakEndTime;

}
