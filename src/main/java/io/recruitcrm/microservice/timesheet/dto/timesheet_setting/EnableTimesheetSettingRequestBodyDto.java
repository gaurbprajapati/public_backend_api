package io.recruitcrm.microservice.timesheet.dto.timesheet_setting;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class EnableTimesheetSettingRequestBodyDto {

	@NotEmpty
	private List<Integer> assignmentIds;

}
