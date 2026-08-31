package io.recruitcrm.microservice.timesheet.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalTimesheetPermissionDto {

	private Integer canCreate;

	private Integer canEdit;

	private Integer canDelete;

}
