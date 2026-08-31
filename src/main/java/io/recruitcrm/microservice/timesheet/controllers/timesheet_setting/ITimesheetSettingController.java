package io.recruitcrm.microservice.timesheet.controllers.timesheet_setting;

import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.EnableTimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface ITimesheetSettingController {

	ResponseEntity<?> getTimesheetSettingByAssignmentId(@PathVariable Integer jobId,
			@PathVariable Integer contractorId);

	ResponseEntity<?> createBulkTimesheetSettings(TimesheetSettingBulkRequestBodyDto request);

	ResponseEntity<?> getTimesheetSettingDateValidation(@PathVariable Integer jobId, @PathVariable Integer contractorId,
			@RequestParam(required = false) Long startDate, @RequestParam(required = false) Long endDate);

	ResponseEntity<?> getEnabledAssigmentIds(EnableTimesheetSettingRequestBodyDto request);

	ResponseEntity<?> getUserTimesheetSettingPreference();

}