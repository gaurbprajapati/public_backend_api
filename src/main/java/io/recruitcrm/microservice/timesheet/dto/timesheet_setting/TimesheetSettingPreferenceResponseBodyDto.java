package io.recruitcrm.microservice.timesheet.dto.timesheet_setting;

import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for storing timesheet setting preferences in JSON format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetSettingPreferenceResponseBodyDto {

	private Integer timesheetFrequency;

	private Integer timesheetStartDay;

	private ApproverRequestResponseBodyDto approvers;

	private Integer enabledBy;

	private Integer templateId;

}
