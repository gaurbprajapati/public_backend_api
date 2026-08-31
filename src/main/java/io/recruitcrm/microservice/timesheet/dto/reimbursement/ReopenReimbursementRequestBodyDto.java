package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReopenReimbursementRequestBodyDto {

	@NotBlank(message = "remark: must not be blank")
	@Size(max = 1000, message = "remark: size must be between 1 and 1000")
	private String remark;

}
