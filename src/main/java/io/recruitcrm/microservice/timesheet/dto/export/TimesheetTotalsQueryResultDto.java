package io.recruitcrm.microservice.timesheet.dto.export;

import java.util.Map;

/**
 * Query result DTO for totals fetched from cst_timesheet_t (total_time, total_work_time,
 * total_regular_hour, total_overtime). Values are formatted as decimal hours (e.g.
 * "12.00").
 */
public record TimesheetTotalsQueryResultDto(Map<Integer, String> totalTime, Map<Integer, String> totalWorkTime,
		Map<Integer, String> totalOvertime, Map<Integer, String> totalRegularHours) {
}
