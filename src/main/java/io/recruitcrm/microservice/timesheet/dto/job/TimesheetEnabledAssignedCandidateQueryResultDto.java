package io.recruitcrm.microservice.timesheet.dto.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEnabledAssignedCandidateQueryResultDto {

	private Integer id;

	private Integer srno;

	private String firstName;

	private String lastName;

	private String emailId;

	private String slug;

	private Integer ownerId;

	private String position;

	private String lastOrganisation;

	private String contactNumber;

	private String profilePic;

	private Integer jobId;

	private Integer jobStartDate;

	private Integer jobEndDate;

	private Integer timesheetFrequency;

	private Integer timesheetStartDay;

}
