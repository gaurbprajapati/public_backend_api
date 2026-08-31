package io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor;

import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchBulkContractorTimelogResultBodyDto {

	private List<ContractorTimesheetSettingErrorResponseBodyDto> errorData;

	private ContractorTimesheetAndSettingValidatorResponseBodyDto timesheetSettingsMetaData;

	private List<TimelogResponseBodyDto> timeLogs;

}
