package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealQueryResultDto {

	private Integer timesheetId;

	private Integer contractorId;

	private Integer jobId;

	private Integer dealId;

	private String dealName;

	private String ownerName;

	private Integer serialNumber;

	private String slug;

	private String status;

}
