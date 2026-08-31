package io.recruitcrm.microservice.timesheet.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkInvoiceValidationResponseBodyDto {

	private List<TimesheetInvoicePreviewResponseBodyDto> timesheetInvoicePreviewData;

	private Integer errorCount;

}
