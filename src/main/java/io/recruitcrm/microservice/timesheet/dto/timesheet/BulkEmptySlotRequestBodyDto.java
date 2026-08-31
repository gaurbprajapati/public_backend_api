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
public class BulkEmptySlotRequestBodyDto {

	@NotEmpty(message = "Contractor-Job pairs cannot be empty")
	@Valid
	private List<ContractorJobPairDto> contractorJobPairs;

	@NotNull(message = "Start date cannot be null")
	private Integer maxJobStartDate;

	@NotNull(message = "End date cannot be null")
	private Integer minJobEndDate;

}
