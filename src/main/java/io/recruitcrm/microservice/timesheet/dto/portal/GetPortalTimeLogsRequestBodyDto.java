package io.recruitcrm.microservice.timesheet.dto.portal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPortalTimeLogsRequestBodyDto {

	@NotNull(message = "Job ID cannot be null")
	private Integer jobId;

}
