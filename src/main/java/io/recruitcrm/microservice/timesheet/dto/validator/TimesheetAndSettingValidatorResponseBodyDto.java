package io.recruitcrm.microservice.timesheet.dto.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetAndSettingValidatorResponseBodyDto {

	private Integer contractorId;

	private Integer workLogType;

	private Integer periodStart;

	private Integer periodEnd;

	private Integer timesheetId;

	private Integer timesheetSettingId;

}
