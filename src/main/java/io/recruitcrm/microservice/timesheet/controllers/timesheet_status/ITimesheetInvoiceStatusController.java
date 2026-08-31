package io.recruitcrm.microservice.timesheet.controllers.timesheet_status;

import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ITimesheetInvoiceStatusController {

	ResponseEntity<?> updateTimesheetPayBillStatus(@PathVariable("id") Integer timesheetId,
			@Valid @RequestBody UpdateTimesheetPayBillStatusRequestBodyDto requestDto);

}
