package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetWorkSummaryQueryResultDto {

	private Integer timesheetId;

	private Long totalWorkingHours;

	private Long totalOverTimeHours;

	private Double totalPayData;

	private Double totalBillData;

	private Long totalTime;

}
