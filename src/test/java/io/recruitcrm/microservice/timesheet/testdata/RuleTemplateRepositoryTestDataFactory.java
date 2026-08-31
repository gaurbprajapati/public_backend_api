package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template.RuleTemplateRepository}
 * unit tests.
 */
public final class RuleTemplateRepositoryTestDataFactory {

	public static final Integer DEFAULT_TEMPLATE_ID = 1;

	public static final Integer DEFAULT_ACCOUNT_ID = 100;

	public static final String DEFAULT_TEMPLATE_NAME = "Test Template";

	public static final String SEARCH_TERM_STANDARD = "standard";

	/**
	 * JPQL / named query parameter name used by {@code RuleTemplateRepository} queries.
	 */
	public static final String JPQL_PARAMETER_ACCOUNT_ID = "accountId";

	private RuleTemplateRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static RuleTemplate createRuleTemplateEntity() {
		RuleTemplate ruleTemplate = new RuleTemplate();
		ruleTemplate.setId(DEFAULT_TEMPLATE_ID);
		ruleTemplate.setTemplateName(DEFAULT_TEMPLATE_NAME);
		ruleTemplate.setAccountId(DEFAULT_ACCOUNT_ID);
		ruleTemplate.setIsDefault(0);
		return ruleTemplate;
	}

	public static RuleTemplateNameQueryResultDto createRuleTemplateNameQueryResultDto() {
		return new RuleTemplateNameQueryResultDto(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_NAME, 0);
	}

	public static RuleTemplateListQueryResultDto createRuleTemplateListQueryResultDto() {
		return new RuleTemplateListQueryResultDto(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_NAME, 1, 1, 1, 1, 1, 1, 1);
	}

	public static SearchRequestBodyDto createSearchRequestWithEmptySortList() {
		SearchRequestBodyDto dto = new SearchRequestBodyDto();
		dto.setSortPriorityList(new ArrayList<>());
		return dto;
	}

	public static SearchRequestBodyDto createSearchRequestWithOnlyInvalidSortFields() {
		SortPriorityRequestBodyDto blankField = new SortPriorityRequestBodyDto("  ", "asc");
		SortPriorityRequestBodyDto nullField = new SortPriorityRequestBodyDto(null, "desc");
		SearchRequestBodyDto dto = new SearchRequestBodyDto();
		dto.setSortPriorityList(List.of(blankField, nullField));
		return dto;
	}

	public static SearchRequestBodyDto createSearchRequestWithValidSortField() {
		SortPriorityRequestBodyDto sort = new SortPriorityRequestBodyDto("templateName", "asc");
		SearchRequestBodyDto dto = new SearchRequestBodyDto();
		dto.setSortPriorityList(List.of(sort));
		return dto;
	}

}
