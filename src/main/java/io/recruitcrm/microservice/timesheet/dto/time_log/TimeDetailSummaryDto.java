package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeDetailSummaryDto {

	private Integer timesheetId;

	private Integer totalWorkTime;

	private Integer totalOvertime;

	private Integer totalTime;

}
