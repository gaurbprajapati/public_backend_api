package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body;

import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleTemplateListResponseBodyDto {

	private Integer id;

	private String templateName;

	private Integer isDefault;

	private Integer addedOn;

	private AddedByResponseBodyDto addedBy;

	private Integer updatedOn;

	private UpdatedByResponseBodyDto updatedBy;

}