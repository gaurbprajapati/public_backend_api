package io.recruitcrm.microservice.timesheet.controllers.timesheet_logs;

import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ITimesheetLogsController {

	ResponseEntity<?> getTimeLogsByTimesheetId(@PathVariable("id") Integer id);

	ResponseEntity<?> bulkUpdateTimeLogs(@RequestBody BulkUpdateTimeLogsRequestBodyDto requestDto);

	ResponseEntity<?> getBulkTimeLogs(@RequestBody BulkTimeLogRequestBodyDto requestBodyDto);

	ResponseEntity<?> getContractorBulkTimeLogs(@RequestBody BulkTimeLogRequestBodyDto requestBodyDto);

	ResponseEntity<?> updateTimesheetStatus(@PathVariable("id") Integer timesheetId,
			@Validated @RequestBody UpdateTimesheetStatusRequestBodyDto requestDto);

}
