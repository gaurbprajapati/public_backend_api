package io.recruitcrm.microservice.timesheet.dto.validator.contractor;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelogsMetaDataDto {

	private Integer timesheetId;

	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	private List<TemplateWorkDay> templateWorkDays;

	private Integer isRemarkMandatory;

	private Integer timesheetStatus;

}