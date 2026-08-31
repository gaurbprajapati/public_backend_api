package io.recruitcrm.microservice.search.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetPeriodRequestBodyDto {

	Integer startDate;

	Integer endDate;

}
