package io.recruitcrm.microservice.timesheet.dto.contractor_setting;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetContractorListRequestBodyDto {

	@NotNull(message = "Job ID is required")
	private Integer jobId;

	@NotEmpty(message = "Contractor IDs list cannot be empty")
	private List<Integer> contractorIds;

}
