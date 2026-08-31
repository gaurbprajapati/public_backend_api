package io.recruitcrm.microservice.timesheet.dto.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetSettingErrorResponseBodyDto {

	private Integer id; // contractorId

	private Integer timesheetId;

	private String timesheetPeriod;

	private String error;

	private String contractorName;

	private String contractorProfilePicUrl;

	private String contractorJobName;

	private String companyProfilePicUrl;

	private Integer contractorSerialNumber;

}
