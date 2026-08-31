package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleTemplateResponseBodyDto {

	private Integer id;

	private String templateName;

	private Integer workLogType;

	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	private Integer accountId;

	private Integer addedOn;

	private Integer addedBy;

	private Integer addedByUserTypeId;

	private Integer updatedOn;

	private Integer updatedBy;

	private Integer updatedByUserTypeId;

	@Min(value = 0, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	@Max(value = 1, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	private Integer isUnplannedHoursPayEnabled;

	private List<TemplateWorkDay> templateWorkDays;

	private List<CustomRule> customRules;

	private Integer isDefault;

}
