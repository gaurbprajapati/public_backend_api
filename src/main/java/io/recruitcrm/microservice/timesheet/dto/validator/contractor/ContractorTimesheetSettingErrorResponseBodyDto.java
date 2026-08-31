package io.recruitcrm.microservice.timesheet.dto.validator.contractor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractorTimesheetSettingErrorResponseBodyDto {

	private Integer id; // contractorId

	private Integer timesheetId;

	private String timesheetPeriod;

	private String error;

	private Integer contractorSerialNumber;

}
