package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimeLogsRequestBodyDto {

	private Boolean isApproved;

	private List<TimeLogRequestBodyDto> logs;

}