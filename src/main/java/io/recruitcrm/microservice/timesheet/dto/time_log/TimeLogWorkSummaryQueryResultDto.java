package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogWorkSummaryQueryResultDto {

	private Integer timesheetId;

	private Long totalWorkTime;

	private Long totalBreakTime;

	private Long totalOvertime;

	private Long totalTime;

	private Double totalPayData;

	private Double totalBillData;

}