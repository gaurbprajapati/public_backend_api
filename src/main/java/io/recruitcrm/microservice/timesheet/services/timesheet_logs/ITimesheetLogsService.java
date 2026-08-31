package io.recruitcrm.microservice.timesheet.services.timesheet_logs;

import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;

public interface ITimesheetLogsService {

	TimesheetResponseBodyDto getTimeLogsByTimesheetId(Integer timesheetId);

	void bulkUpdateTimeLogs(BulkUpdateTimeLogsRequestBodyDto requestDto);

	PortalTimesheetResponseBodyDto getPortalTimeLogs(Integer timesheetId);

}
