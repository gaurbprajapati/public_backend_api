package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetStatusHistoryResponseBodyDto {

	@NotNull(message = "Timesheet id cannot be null")
	private Integer timesheetId;

	private List<StatusHistoryResponseBodyDto> statusHistory;

}