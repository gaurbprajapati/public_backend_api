package io.recruitcrm.microservice.timesheet.dto.time_log.bulk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetJobQueryResultDto {

	private Integer timesheetId;

	private Integer jobId;

	private String jobName;

	private String jobSlug;

	private Integer assignmentId;

}
