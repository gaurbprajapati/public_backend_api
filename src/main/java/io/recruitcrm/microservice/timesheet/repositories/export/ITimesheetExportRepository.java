package io.recruitcrm.microservice.timesheet.repositories.export;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.TimesheetTotalsQueryResultDto;

import java.util.List;

/**
 * Interface for timesheet export repository operations. Handles database queries for
 * export data with dynamic field selection and period grouping.
 */
public interface ITimesheetExportRepository {

	/**
	 * Fetch total_time, total_work_time, total_overtime directly from cst_timesheet_t.
	 * Values are formatted as decimal hours (e.g. "12.00").
	 * @param timesheetIds Timesheet IDs to fetch
	 * @param accountId Account ID for filtering
	 * @return TimesheetTotalsQueryResultDto with totalTime, totalWorkTime, totalOvertime
	 * maps
	 */
	TimesheetTotalsQueryResultDto getTimesheetTotals(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Get export data using dynamic JOOQ query building.
	 * @param request Export request containing selected fields and filters
	 * @param accountId Account ID for filtering
	 * @return List of dynamic export data
	 */
	List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto request, Integer accountId);

	/**
	 * Get export data grouped by timesheet periods (converted to UTC).
	 * @param request Export request containing selected fields and filters
	 * @param accountId Account ID for filtering
	 * @return List of period-grouped export data
	 */
	List<PeriodGroupedExportResponseBodyDto> getExportDataGroupedByPeriods(DynamicExportRequestBodyDto request,
			Integer accountId);

}
