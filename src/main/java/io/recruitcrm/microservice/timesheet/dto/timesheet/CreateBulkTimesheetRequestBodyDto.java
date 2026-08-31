package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBulkTimesheetRequestBodyDto {

	@NotEmpty(message = "Contractor IDs cannot be empty")
	private List<Integer> contractorIds;

	@NotNull(message = "Timesheet dates cannot be null")
	@NotEmpty(message = "Timesheet dates cannot be empty")
	@Valid
	private List<CreateTimesheetRequestBodyDto> timesheetDates;

}
