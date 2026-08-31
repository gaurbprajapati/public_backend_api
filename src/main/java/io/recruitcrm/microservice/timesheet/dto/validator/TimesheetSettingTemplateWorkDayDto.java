package io.recruitcrm.microservice.timesheet.dto.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for holding timesheet setting ID and its template work day data for work day
 * comparison.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetSettingTemplateWorkDayDto {

	private Integer timesheetSettingId;

	private List<Integer> workDayIds;

}