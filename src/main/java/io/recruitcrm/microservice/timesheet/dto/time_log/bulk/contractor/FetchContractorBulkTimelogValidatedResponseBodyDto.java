package io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor;

import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchContractorBulkTimelogValidatedResponseBodyDto {

	List<ContractorTimesheetSettingErrorResponseBodyDto> errorData;

	List<ContractorTimesheetAndSettingValidatorResponseBodyDto> timesheetAndSettingValidatorResponseBodyDtos;

	Integer primaryTimesheetSettingId;

}
