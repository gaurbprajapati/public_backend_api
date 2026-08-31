package io.recruitcrm.microservice.timesheet.dto.validator.contractor;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractorTimesheetValidatorQueryResultDto {

	private Integer timesheetId;

	private Integer timesheetSettingId;

	private Integer workTimeType;

	private Boolean calculateBreakTime;

	private List<TemplateWorkDay> templateWorkDays;

}