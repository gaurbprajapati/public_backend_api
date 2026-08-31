package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;

import java.util.List;

/**
 * Test data factory for rule template mapper DTOs.
 */
public final class RuleTemplateMapperTestDataFactory {

	public static final Integer DEFAULT_TEMPLATE_ID = Integer.valueOf(101);

	public static final String DEFAULT_TEMPLATE_NAME = "Weekday Template";

	public static final Integer DEFAULT_IS_DEFAULT = Integer.valueOf(1);

	public static final Integer DEFAULT_ADDED_ON = Integer.valueOf(1710000000);

	public static final Integer DEFAULT_ADDED_BY = Integer.valueOf(501);

	public static final Integer DEFAULT_ADDED_BY_USER_TYPE_ID = Integer.valueOf(2);

	public static final Integer DEFAULT_UPDATED_ON = Integer.valueOf(1711000000);

	public static final Integer DEFAULT_UPDATED_BY = Integer.valueOf(601);

	public static final Integer DEFAULT_UPDATED_BY_USER_TYPE_ID = Integer.valueOf(3);

	private RuleTemplateMapperTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Creates a default RuleTemplateNameQueryResultDto.
	 * @return default rule template name query result
	 */
	public static RuleTemplateNameQueryResultDto createRuleTemplateNameQueryResultDto() {
		return new RuleTemplateNameQueryResultDto(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_NAME, DEFAULT_IS_DEFAULT);
	}

	/**
	 * Creates a list of RuleTemplateNameQueryResultDto objects.
	 * @return list with two deterministic items
	 */
	public static List<RuleTemplateNameQueryResultDto> createRuleTemplateNameQueryResultDtoList() {
		return List.of(createRuleTemplateNameQueryResultDto(),
				new RuleTemplateNameQueryResultDto(Integer.valueOf(102), "Weekend Template", Integer.valueOf(0)));
	}

	/**
	 * Creates a default RuleTemplateListQueryResultDto.
	 * @return default rule template list query result
	 */
	public static RuleTemplateListQueryResultDto createRuleTemplateListQueryResultDto() {
		return new RuleTemplateListQueryResultDto(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_NAME, DEFAULT_IS_DEFAULT,
				DEFAULT_ADDED_ON, DEFAULT_ADDED_BY, DEFAULT_ADDED_BY_USER_TYPE_ID, DEFAULT_UPDATED_ON,
				DEFAULT_UPDATED_BY, DEFAULT_UPDATED_BY_USER_TYPE_ID);
	}

	/**
	 * Creates a list of RuleTemplateListQueryResultDto objects.
	 * @return list with two deterministic items
	 */
	public static List<RuleTemplateListQueryResultDto> createRuleTemplateListQueryResultDtoList() {
		return List.of(createRuleTemplateListQueryResultDto(),
				new RuleTemplateListQueryResultDto(Integer.valueOf(202), "Night Shift Template", Integer.valueOf(0),
						Integer.valueOf(1712000000), Integer.valueOf(701), Integer.valueOf(2),
						Integer.valueOf(1713000000), Integer.valueOf(801), Integer.valueOf(3)));
	}

}
