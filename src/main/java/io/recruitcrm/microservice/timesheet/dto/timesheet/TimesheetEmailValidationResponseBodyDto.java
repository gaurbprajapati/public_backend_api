package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEmailValidationResponseBodyDto {

	private Integer receiverType;

	private List<TimesheetEmailValidationDetailDto> timesheetDetails;

}
