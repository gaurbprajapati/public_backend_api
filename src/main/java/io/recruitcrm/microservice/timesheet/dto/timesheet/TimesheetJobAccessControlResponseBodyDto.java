package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetJobAccessControlResponseBodyDto {

	private Integer canEdit;

	private Integer canCreate;

	private Integer canDelete;

}
