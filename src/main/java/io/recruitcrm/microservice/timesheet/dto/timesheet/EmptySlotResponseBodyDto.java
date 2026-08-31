package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmptySlotResponseBodyDto {

	private Integer startDate;

	private Integer endDate;

}