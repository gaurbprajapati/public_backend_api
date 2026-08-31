package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreakIntervalResponseBodyDto {

	private Integer id;

	private Integer timeLogId;

	private Integer breakStartTime;

	private Integer breakEndTime;

}
