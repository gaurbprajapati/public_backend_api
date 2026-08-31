package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetPeriodResponseBodyDto {

	@NotNull(message = "Start date cannot be null")
	private Integer timesheetStartDate;

	@NotNull(message = "End date cannot be null")
	private Integer timesheetEndDate;

}