package io.recruitcrm.microservice.timesheet.dto.validator;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetAndSettingValidatorQueryResultDto {

	private Integer contractorId;

	private Integer jobId;

	private Integer workLogType;

	private Integer periodStart;

	private Integer periodEnd;

	private String contractorName;

	private String contractorPhoto;

	private String jobName;

	private Integer timesheetSettingId;

	private Integer timesheetId;

	private String companyProfilePicUrl;

	private Integer timesheetApprovalStatusTypeId;

	private Boolean calculateBreakTime;

	private List<TemplateWorkDay> templateWorkDays;

	private Integer contractorSerialNumber;

}
