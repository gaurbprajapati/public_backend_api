package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetApproverEmailQueryRowDto {

	private Integer timesheetId;

	private Integer timesheetApproverId;

	private Integer userTypeId;

	private Integer entityId;

	private String firstName;

	private String lastName;

	private String emailId;

	private String slug;

	private Byte emailOptOut;

	private Byte deleted;

	private Integer latestApprovalStatusId;

	private Integer jobId;

	private Byte sharedWithContact;

	private Byte sharedWithClient;

	private Integer ownerId;

}
