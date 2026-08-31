package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleTemplateListQueryResultDto {

	private Integer id;

	private String templateName;

	private Integer isDefault;

	private Integer addedOn;

	private Integer addedBy;

	private Integer addedByUserTypeId;

	private Integer updatedOn;

	private Integer updatedBy;

	private Integer updatedByUserTypeId;

}
