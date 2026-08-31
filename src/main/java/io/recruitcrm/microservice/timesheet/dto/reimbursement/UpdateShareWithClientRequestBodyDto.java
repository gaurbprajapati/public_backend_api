package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShareWithClientRequestBodyDto {

	@NotNull(message = "isSharedWithClient must be provided and must be 0 or 1")
	@Min(value = 0, message = "isSharedWithClient must be provided and must be 0 or 1")
	@Max(value = 1, message = "isSharedWithClient must be provided and must be 0 or 1")
	private Integer isSharedWithClient;

}
