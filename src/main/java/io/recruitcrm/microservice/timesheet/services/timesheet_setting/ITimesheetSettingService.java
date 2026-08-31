package io.recruitcrm.microservice.timesheet.services.timesheet_setting;

import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingPreferenceResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;

public interface ITimesheetSettingService {

	TimesheetSettingResponseBodyDto getTimesheetSettingByAssignmentId(Integer jobId, Integer contractorId);

	void createBulkTimesheetSettings(TimesheetSettingBulkRequestBodyDto request);

	Boolean getTimesheetSettingDateValidation(Integer jobId, Integer contractorId, Long startDate, Long endDate);

	TimesheetSettingPreferenceResponseBodyDto getUserTimesheetSettingPreference();

}