package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRuleTemplateRequestBodyDto {

	private Integer id;

	String templateName;

	Integer workLogType;

	@AssertFalse(message = "calculateBreakTime must be false (0)")
	Boolean calculateBreakTime;

	@NotNull(message = "Work days cannot be null")
	List<Integer> workDayIds;

	List<Integer> workTime;

	List<Integer> workStartTime;

	List<Integer> workEndTime;

	List<UpdateCustomRuleRequestBodyDto> customRules;

}