package io.recruitcrm.microservice.timesheet.search.dto;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZonedDateTimeRangeDto {

	private ZonedDateTime from;

	private ZonedDateTime to;

}
