package io.recruitcrm.microservice.timesheet.dto.time_log.bulk;

import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchBulkTimelogValidatedResponseBodyDto {

	List<TimesheetSettingErrorResponseBodyDto> timesheetSettingErrorResponseBodyDtos;

	List<TimesheetAndSettingValidatorResponseBodyDto> timesheetAndSettingValidatorResponseBodyDtos;

	Integer primaryTimesheetSettingId;

}
