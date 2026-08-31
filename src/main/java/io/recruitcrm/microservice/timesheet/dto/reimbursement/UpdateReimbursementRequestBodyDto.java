package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReimbursementRequestBodyDto {

	@Size(min = 1, max = 100, message = "Description must be between 1 and 100 characters")
	private String description;

	@Positive(message = "Amount must be greater than 0")
	@DecimalMax(value = "9999999999", message = "Amount must not exceed 9999999999")
	private BigDecimal amount;

	@Size(max = 1000, message = "Document Token must not exceed 1000 characters")
	private String documentToken;

	@Size(max = 255, message = "File name must not exceed 255 characters")
	private String fileName;

	/**
	 * When true, do not insert a status history row. Use only when history was already
	 * recorded for this user action (e.g. immediately after reopen in the same flow).
	 */
	private Boolean skipStatusHistory;

}
