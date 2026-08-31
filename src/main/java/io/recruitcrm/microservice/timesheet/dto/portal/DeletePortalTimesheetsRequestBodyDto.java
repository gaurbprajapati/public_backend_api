package io.recruitcrm.microservice.timesheet.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletePortalTimesheetsRequestBodyDto {

	private Integer timesheetId;

	private Integer jobId;

}
