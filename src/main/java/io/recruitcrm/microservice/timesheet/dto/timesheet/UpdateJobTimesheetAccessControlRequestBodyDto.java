package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateJobTimesheetAccessControlRequestBodyDto {

	private Integer edit;

	private Integer create;

	private Integer delete;

}
