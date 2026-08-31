package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloneTemplateResponseBodyDto {

	private Integer workLogType;

	private Boolean calculateBreakTime;

	private Integer customRulesCount;

	private List<Integer> workDayIds;

	@Min(value = 0, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	@Max(value = 1, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	private Integer isUnplannedHoursPayEnabled;

}
