package io.recruitcrm.microservice.timesheet.repositories.invoice;

import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;

import java.util.List;

public interface ITimesheetInvoiceRepository {

	TimesheetInvoice findByTimesheetId(Integer timesheetId, Integer accountId);

	List<TimesheetInvoice> findByTimesheetIdIn(List<Integer> timesheetIds, Integer accountId);

	TimesheetInvoice saveInvoice(TimesheetInvoice invoice);

	TimesheetInvoice findInvoiceWithStatusHistoryByTimesheetId(Integer timesheetId, Integer accountId);

	TimesheetInvoice findBillDetailsByTimesheetId(Integer timesheetId, Integer accountId);

	List<InvoiceValidationQueryResultDto> validateTimesheetsForInvoice(List<Integer> timesheetIds, Integer accountId);

}