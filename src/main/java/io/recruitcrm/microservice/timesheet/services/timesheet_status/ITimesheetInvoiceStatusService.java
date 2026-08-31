package io.recruitcrm.microservice.timesheet.services.timesheet_status;

import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;

public interface ITimesheetInvoiceStatusService {

	void updateTimesheetStatus(Integer timesheetId, UpdateTimesheetStatusRequestBodyDto requestDto);

	void updateTimesheetPayBillStatus(Integer timesheetId, UpdateTimesheetPayBillStatusRequestBodyDto requestDto);

}
