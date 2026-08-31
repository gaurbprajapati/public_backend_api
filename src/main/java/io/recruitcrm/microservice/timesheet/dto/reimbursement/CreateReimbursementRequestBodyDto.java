package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReimbursementRequestBodyDto {

	@NotBlank(message = "Description cannot be blank")
	@Size(max = 100, message = "Description must not exceed 100 characters")
	private String description;

	@NotNull(message = "Amount cannot be null")
	@Positive(message = "Amount must be greater than 0")
	@DecimalMax(value = "9999999999", message = "Amount must not exceed 9999999999")
	private BigDecimal amount;

	@Size(max = 1000, message = "Document Token must not exceed 1000 characters")
	private String documentToken;

	@Size(max = 255, message = "File name must not exceed 255 characters")
	private String fileName;

}
