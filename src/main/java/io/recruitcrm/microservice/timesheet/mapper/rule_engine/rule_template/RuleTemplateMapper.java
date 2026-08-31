package io.recruitcrm.microservice.timesheet.mapper.rule_engine.rule_template;

import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RuleTemplateMapper {

	RuleTemplateMapper INSTANCE = Mappers.getMapper(RuleTemplateMapper.class);

	@Mapping(target = "templateWorkDays", ignore = true) // Skip setting the 'addedBy'
															// field
	@Mapping(target = "customRules", ignore = true) // Skip setting the 'addedByUse
	RuleTemplateResponseBodyDto ruleTemplateToResponseBodyDto(RuleTemplate projection);

	List<RuleTemplateNameResponseBodyDto> ruleTemplateQueryResultToResponseBodyDto(
			List<RuleTemplateNameQueryResultDto> projection);

	@Mapping(target = "addedBy", ignore = true) // Will be set separately with user
												// details
	@Mapping(target = "updatedBy", ignore = true) // Will be set separately with user
													// details
	@Mapping(source = "id", target = "id")
	@Mapping(source = "templateName", target = "templateName")
	@Mapping(source = "addedOn", target = "addedOn")
	@Mapping(source = "updatedOn", target = "updatedOn")
	RuleTemplateListResponseBodyDto ruleTemplateListQueryResultToResponseBodyDto(
			RuleTemplateListQueryResultDto projection);

	List<RuleTemplateListResponseBodyDto> ruleTemplateListQueryResultToResponseBodyDtoList(
			List<RuleTemplateListQueryResultDto> projection);

}
