package io.recruitcrm.microservice.timesheet.dto.contractor_setting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OccupiedSlotsQueryResultDto {

	private Integer id;

	private Integer periodStart;

	private Integer periodEnd;

	private Integer contractorId;

	private Integer jobId;

	private Integer timesheetStartDay;

}
