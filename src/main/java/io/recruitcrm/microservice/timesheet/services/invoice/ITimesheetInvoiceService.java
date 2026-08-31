package io.recruitcrm.microservice.timesheet.services.invoice;

import io.recruitcrm.entity.model.Invoice;
import io.recruitcrm.microservice.timesheet.dto.invoice.BillDetailsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPayBillHistoryResponseBodyDto;

import java.util.List;

public interface ITimesheetInvoiceService {

	TimesheetPayBillHistoryResponseBodyDto getTimesheetPayBillHistory(Integer timesheetId);

	BillDetailsResponseBodyDto getBillDetailsByTimesheetId(Integer timesheetId);

	Invoice createInvoice(Invoice invoice);

	BulkInvoiceValidationResponseBodyDto validateTimesheetsForInvoice(List<Integer> timesheetIds);

	void createTimesheetInvoice(Integer timesheetId, Integer userId, Integer userTypeId);

}
