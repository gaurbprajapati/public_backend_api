package io.recruitcrm.microservice.timesheet.services.rule_engine.rule_template;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.CloneTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRuleTemplateService {

	void createRuleTemplate(CreateRuleTemplateRequestBodyDto requestDto);

	RuleTemplateResponseBodyDto getRuleTemplate(Integer templateId);

	void deleteRuleTemplate(Integer templateId);

	CloneTemplateResponseBodyDto cloneRuleTemplate(Integer templateId);

	List<RuleTemplateNameResponseBodyDto> getRuleTemplateNames(String search, Pageable pageable);

	List<RuleTemplateListResponseBodyDto> getAllRuleTemplates(SearchRequestBodyDto searchRequestBodyDto, String search,
			Pageable pageable);

	void updateRuleTemplate(Integer templateId, CreateRuleTemplateRequestBodyDto requestDto);

	void markAsDefault(Integer templateId, Boolean isDefault);

}
