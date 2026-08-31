package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetCandidateEmailQueryResultDto {

	private Integer timesheetId;

	private Integer candidateId;

	private String firstName;

	private String lastName;

	private Integer srno;

	private String slug;

	private String emailId;

	private Byte emailOptOut;

	private Byte deleted;

	private Integer latestApprovalStatusId;

	private Integer assignmentId;

	private Integer approverTypeId;

	private Integer portalStatusId;

	private Integer ownerId;

}
