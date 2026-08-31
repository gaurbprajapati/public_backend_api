package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for assembling reimbursement data for the export "Reimbursements"
 * sheet.
 */
public interface IReimbursementExportService {

	/**
	 * Build reimbursement export rows for the given timesheet IDs, enriching each row
	 * with timesheet-level context (period, contractor, job, company, duration) from the
	 * already-fetched export data.
	 * @param timesheetIds IDs of the timesheets being exported
	 * @param accountId current account
	 * @param timesheetContextMap map of timesheetId to its export row for context lookups
	 * @return ordered list of reimbursement rows ready for the spreadsheet
	 */
	List<ReimbursementExportRowDto> buildReimbursementExportRows(List<Integer> timesheetIds, Integer accountId,
			Map<Integer, DynamicExportResponseBodyDto> timesheetContextMap);

}
