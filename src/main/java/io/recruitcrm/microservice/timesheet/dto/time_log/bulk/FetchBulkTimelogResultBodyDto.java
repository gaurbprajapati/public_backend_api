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
public class FetchBulkTimelogResultBodyDto {

	private List<TimesheetSettingErrorResponseBodyDto> contractorsErrorData;

	private TimesheetAndSettingValidatorResponseBodyDto timesheetSettingsMetaData;

	private List<DayTimelogQueryResultDto> contractorsLogData;

}
