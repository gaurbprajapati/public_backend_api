package io.recruitcrm.microservice.timesheet.dto.validator.contractor;

import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractorTimesheetAndSettingValidatorResponseBodyDto {

	private Integer timesheetSettingId;

	private Integer timesheetId;

	private Integer workLogType;

	private List<TimelogsMetaDataDto> timelogsMetaData;

	private ApproverRequestResponseBodyDto approvers;

}
