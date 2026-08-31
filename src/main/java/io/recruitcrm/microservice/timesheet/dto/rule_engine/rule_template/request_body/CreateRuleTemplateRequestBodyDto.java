package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleTemplateConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRuleTemplateRequestBodyDto {

	@NotBlank(message = "Template name is required")
	@Size(max = RuleTemplateConstants.MAX_TEMPLATE_NAME_LENGTH,
			message = "Template name must not exceed 200 characters")
	String templateName;

	Integer workLogType;

	@AssertFalse(message = "calculateBreakTime must be false (0)")
	Boolean calculateBreakTime;

	Integer breakTimeThreshold;

	@Min(value = 0, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	@Max(value = 1, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	Integer isUnplannedHoursPayEnabled;

	@NotNull(message = "Work days cannot be null")
	List<Integer> workDayIds;

	List<Integer> workTime;

	List<Integer> workStartTime;

	List<Integer> workEndTime;

	List<CustomRule> customRules;

}
