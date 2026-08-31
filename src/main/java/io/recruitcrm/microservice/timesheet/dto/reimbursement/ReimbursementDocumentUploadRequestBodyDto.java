package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDocumentUploadRequestBodyDto {

	@NotBlank(message = "File name cannot be blank")
	@Size(max = 255, message = "File name must not exceed 255 characters")
	private String fileName;

	@NotNull(message = "Timesheet ID cannot be null")
	private Integer timesheetId;

}
