package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating timesheets across multiple jobs with their respective contractors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBulkTimesheetsForMultipleJobsRequestBodyDto {

	@NotEmpty(message = "Job contractor pairs cannot be empty")
	@Valid
	private List<JobContractorPairDto> jobContractorPairs;

	@NotNull(message = "Timesheet dates cannot be null")
	@Valid
	private List<CreateTimesheetRequestBodyDto> timesheetDates;

}
