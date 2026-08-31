package io.recruitcrm.microservice.timesheet.dto.time_log.bulk;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelogWithSettingQueryResultDto {

	// Time log fields
	private Integer id; // timeLogId

	private Integer date;

	private Integer dayTypeId;

	private Integer workTime;

	private Integer workStartTime;

	private Integer workEndTime;

	private Integer breakTime;

	private Integer overTime;

	private String remark;

	private Integer totalTime;

	private Integer timesheetId;

	private Integer periodStart;

	private Integer periodEnd;

	// Timesheet setting fields
	private Integer timesheetSettingId;

	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	private List<TemplateWorkDay> templateWorkDays;

	private Integer isRemarkMandatory;

}