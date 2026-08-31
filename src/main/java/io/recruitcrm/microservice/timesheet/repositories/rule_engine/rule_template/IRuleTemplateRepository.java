package io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template;

import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRuleTemplateRepository {

	void createRuleTemplate(RuleTemplate ruleTemplate);

	RuleTemplate getRuleTemplate(Integer templateId, Integer accountId);

	void deleteRuleTemplate(Integer templateId);

	List<RuleTemplateNameQueryResultDto> getRuleTemplateNames(String search, Pageable pageable, Integer accountId);

	List<RuleTemplateListQueryResultDto> getAllRuleTemplates(SearchRequestBodyDto searchRequestBodyDto, String search,
			Pageable pageable, Integer accountId);

	void updateRuleTemplate(RuleTemplate ruleTemplate);

	void markAsDefault(Integer templateId, Integer accountId, Boolean isDefault);

	Integer findDefaultTemplateIdByAccountId(Integer accountId);

}
