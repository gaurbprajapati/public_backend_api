package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEmailValidationDetailDto {

	private Integer timesheetId;

	private Integer contractorEntityId;

	private Integer approverTypeId;

	private Integer approverEntityId;

	private String name;

	private Integer serialNumber;

	private String slug;

	private String email;

	private Integer ownerId;

	private String error;

	private boolean valid;

}
