package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a job ID with its associated contractor IDs for bulk timesheet
 * creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobContractorPairDto {

	@NotNull(message = "Job ID cannot be null")
	private Integer jobId;

	@NotNull(message = "Contractor IDs cannot be null")
	private List<Integer> contractorIds;

}
