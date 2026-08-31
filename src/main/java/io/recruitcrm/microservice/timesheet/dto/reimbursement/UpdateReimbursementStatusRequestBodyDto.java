package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReimbursementStatusRequestBodyDto {

	@NotNull(message = "Status ID cannot be null")
	private Integer status;

	@Size(max = 1000, message = "Remark must not exceed 1000 characters")
	private String remark;

}
