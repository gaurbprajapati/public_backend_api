package io.recruitcrm.microservice.timesheet.controllers.invoice;

import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ITimesheetInvoiceController {

	ResponseEntity<?> getTimesheetPayBillHistory(@PathVariable("timesheet_id") Integer timesheetId);

	ResponseEntity<?> getBillDetails(@PathVariable("id") Integer timesheetId);

	ResponseEntity<?> validateTimesheetsForInvoice(@RequestBody BulkInvoiceValidationRequestBodyDto requestBodyDto);

}
