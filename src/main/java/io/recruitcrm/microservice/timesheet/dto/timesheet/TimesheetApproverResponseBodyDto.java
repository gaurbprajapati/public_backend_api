package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetApproverResponseBodyDto {

	@NotNull(message = "timeSheetApprovalStatusId cannot be null")
	private Integer timeSheetApprovalStatusId;

	@NotNull(message = "entityId cannot be null")
	private Integer entityId;

	@NotNull(message = "userTypeId cannot be null")
	private Integer userTypeId;

	@NotNull(message = "timesheetId cannot be null")
	private Integer timesheetId;

}
