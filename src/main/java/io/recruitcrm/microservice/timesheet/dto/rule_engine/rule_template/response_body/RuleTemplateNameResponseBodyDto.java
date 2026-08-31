package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleTemplateNameResponseBodyDto {

	private Integer id;

	private String templateName;

	private Integer isDefault;

}