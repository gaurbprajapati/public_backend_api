package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPayBillResponseBodyDto {

	private Integer totalWeeklyOvertime;

	private float totalWeeklyPayData;

	private float totalWeeklyBillData;

}
