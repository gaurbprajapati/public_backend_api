package io.recruitcrm.microservice.timesheet.dto.time_log.bulk;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayTimelogQueryResultDto {

	private Integer id; // contractorId

	private String contractorName;

	private String contractorProfilePicUrl;

	private String contractorSlug;

	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	private List<TemplateWorkDay> templateWorkDays;

	private Integer isRemarkMandatory;

	List<TimelogResponseBodyDto> timeLogs;

	private Integer totalTime;

	private Integer totalOvertime;

	private ApproverRequestResponseBodyDto approvers;

	private Integer timesheetId;

	private Integer timesheetStatus;

	private Integer jobId;

	private String jobName;

	private String jobSlug;

	private Integer assignmentId;

}
