package io.recruitcrm.microservice.timesheet.dto.contractor_setting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractorTimesheetSettingResponseBodyDto {

	private Integer timesheetSettingId;

	private Integer startDate;

	private Integer endDate;

	private Integer timesheetStartDate;

	private Integer timesheetFrequency;

}
