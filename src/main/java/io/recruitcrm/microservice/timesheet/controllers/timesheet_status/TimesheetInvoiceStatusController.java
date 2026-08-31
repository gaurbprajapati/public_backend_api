package io.recruitcrm.microservice.timesheet.controllers.timesheet_status;

import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet_status.TimesheetInvoiceStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/timesheets/invoices")
public class TimesheetInvoiceStatusController implements ITimesheetInvoiceStatusController {

	private final TimesheetInvoiceStatusService timesheetInvoiceStatusService;

	private final APIResponder apiResponder;

	public TimesheetInvoiceStatusController(final TimesheetInvoiceStatusService timesheetInvoiceStatusService,
			final APIResponder apiResponder) {
		this.timesheetInvoiceStatusService = timesheetInvoiceStatusService;
		this.apiResponder = apiResponder;
	}

	@Override
	@PatchMapping("/{id}/pay-bill-status")
	public ResponseEntity<?> updateTimesheetPayBillStatus(@PathVariable("id") Integer timesheetId,
			@Validated @RequestBody UpdateTimesheetPayBillStatusRequestBodyDto requestDto) {
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(timesheetId, requestDto);
		return this.apiResponder.respond(null, "Timesheet updated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
