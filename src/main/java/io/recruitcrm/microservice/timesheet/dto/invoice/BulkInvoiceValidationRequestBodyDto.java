package io.recruitcrm.microservice.timesheet.dto.invoice;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkInvoiceValidationRequestBodyDto {

	@NotNull(message = "Timesheet ids cannot be null")
	List<Integer> timesheetIds;

}
