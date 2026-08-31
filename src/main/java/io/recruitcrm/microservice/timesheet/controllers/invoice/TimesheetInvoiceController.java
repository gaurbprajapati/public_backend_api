package io.recruitcrm.microservice.timesheet.controllers.invoice;

import io.recruitcrm.microservice.timesheet.dto.invoice.BillDetailsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPayBillHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.invoice.TimesheetInvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/timesheets/invoices")
public class TimesheetInvoiceController implements ITimesheetInvoiceController {

	private final TimesheetInvoiceService timesheetInvoiceService;

	private final APIResponder apiResponder;

	public TimesheetInvoiceController(final TimesheetInvoiceService timesheetInvoiceService,
			final APIResponder apiResponder) {
		this.timesheetInvoiceService = timesheetInvoiceService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("/{timesheet_id}/pay-bill-history")
	public ResponseEntity<?> getTimesheetPayBillHistory(@PathVariable("timesheet_id") Integer timesheetId) {
		TimesheetPayBillHistoryResponseBodyDto payBillHistory = this.timesheetInvoiceService
			.getTimesheetPayBillHistory(timesheetId);
		return this.apiResponder.respond(payBillHistory, "Timesheet pay bill history fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@GetMapping("/{id}/bill-details")
	public ResponseEntity<?> getBillDetails(@PathVariable("id") Integer timesheetId) {
		BillDetailsResponseBodyDto billDetails = this.timesheetInvoiceService.getBillDetailsByTimesheetId(timesheetId);
		return this.apiResponder.respond(billDetails, "Timesheet bill details fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/validate")
	public ResponseEntity<?> validateTimesheetsForInvoice(
			@RequestBody BulkInvoiceValidationRequestBodyDto requestBodyDto) {
		BulkInvoiceValidationResponseBodyDto validationResult = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(requestBodyDto.getTimesheetIds());
		return this.apiResponder.respond(validationResult, "Timesheet validation completed successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
