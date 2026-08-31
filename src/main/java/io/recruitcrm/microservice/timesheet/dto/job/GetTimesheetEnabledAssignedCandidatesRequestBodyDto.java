package io.recruitcrm.microservice.timesheet.dto.job;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTimesheetEnabledAssignedCandidatesRequestBodyDto {

	@NotNull(message = "Job IDs cannot be null")
	@NotEmpty(message = "Job IDs list cannot be empty")
	private List<Integer> jobIds;

}
