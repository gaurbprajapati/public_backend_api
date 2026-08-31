package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTimesheetsRequestBodyDto {

	@NotEmpty(message = "Timesheet ids are required")
	private List<Integer> timesheetIds;

}