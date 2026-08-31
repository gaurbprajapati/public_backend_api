package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimesheetStatusRequestBodyDto {

	@NotNull(message = "Approval status id cannot be null")
	private Integer approvalStatus;

	private String remark;

}