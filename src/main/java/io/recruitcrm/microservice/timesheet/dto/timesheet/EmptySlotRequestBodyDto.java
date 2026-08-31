package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmptySlotRequestBodyDto {

	@NotEmpty(message = "Contractor IDs cannot be empty")
	private List<Integer> contractorIds;

	@NotNull(message = "Start date cannot be null")
	private Integer startDate;

	@NotNull(message = "End date cannot be null")
	private Integer endDate;

	@NotNull(message = "Timesheet frequency cannot be null")
	private Integer timesheetFrequencyId;

	@NotNull(message = "Timesheet start day cannot be null")
	private Integer timesheetStartDay;

	@NotNull(message = "Job Id cannot be null")
	private Integer jobId;

}