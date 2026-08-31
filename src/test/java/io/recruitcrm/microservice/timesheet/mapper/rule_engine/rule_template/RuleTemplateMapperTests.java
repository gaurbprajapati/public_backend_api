package io.recruitcrm.microservice.timesheet.mapper.rule_engine.rule_template;

import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.testdata.RuleTemplateMapperTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("RuleTemplateMapper Tests")
class RuleTemplateMapperTests {

	private static final String WEEKDAY_TEMPLATE_NAME = "Weekday Template";

	private static final String WEEKEND_TEMPLATE_NAME = "Weekend Template";

	private static final String NIGHT_SHIFT_TEMPLATE_NAME = "Night Shift Template";

	@Test
	@DisplayName("ruleTemplateToResponseBodyDto should map scalar fields and ignore collections")
	void testRuleTemplateToResponseBodyDtoMapsFieldsAndIgnoresCollections() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(ruleTemplate.getId()).willReturn(Integer.valueOf(300));
		given(ruleTemplate.getTemplateName()).willReturn("Core Template");
		given(ruleTemplate.getWorkLogType()).willReturn(Integer.valueOf(1));
		given(ruleTemplate.getCalculateBreakTime()).willReturn(Boolean.TRUE);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(Integer.valueOf(30));
		given(ruleTemplate.getAccountId()).willReturn(Integer.valueOf(400));
		given(ruleTemplate.getAddedOn()).willReturn(Integer.valueOf(1710001111));
		given(ruleTemplate.getAddedBy()).willReturn(Integer.valueOf(500));
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(Integer.valueOf(2));
		given(ruleTemplate.getUpdatedOn()).willReturn(Integer.valueOf(1710002222));
		given(ruleTemplate.getUpdatedBy()).willReturn(Integer.valueOf(600));
		given(ruleTemplate.getUpdatedByUserTypeId()).willReturn(Integer.valueOf(3));
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(Integer.valueOf(1));
		given(ruleTemplate.getIsDefault()).willReturn(Integer.valueOf(0));

		// When
		RuleTemplateResponseBodyDto result = RuleTemplateMapper.INSTANCE.ruleTemplateToResponseBodyDto(ruleTemplate);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(Integer.valueOf(300));
		assertThat(result.getTemplateName()).isEqualTo("Core Template");
		assertThat(result.getWorkLogType()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(Integer.valueOf(30));
		assertThat(result.getAccountId()).isEqualTo(Integer.valueOf(400));
		assertThat(result.getAddedOn()).isEqualTo(Integer.valueOf(1710001111));
		assertThat(result.getAddedBy()).isEqualTo(Integer.valueOf(500));
		assertThat(result.getAddedByUserTypeId()).isEqualTo(Integer.valueOf(2));
		assertThat(result.getUpdatedOn()).isEqualTo(Integer.valueOf(1710002222));
		assertThat(result.getUpdatedBy()).isEqualTo(Integer.valueOf(600));
		assertThat(result.getUpdatedByUserTypeId()).isEqualTo(Integer.valueOf(3));
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getIsDefault()).isEqualTo(Integer.valueOf(0));
		assertThat(result.getTemplateWorkDays()).isNull();
		assertThat(result.getCustomRules()).isNull();
	}

	@Test
	@DisplayName("ruleTemplateToResponseBodyDto should return null for null source")
	void testRuleTemplateToResponseBodyDtoNullSourceReturnsNull() {
		// Given

		// When
		RuleTemplateResponseBodyDto result = RuleTemplateMapper.INSTANCE.ruleTemplateToResponseBodyDto(null);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("ruleTemplateQueryResultToResponseBodyDto should map name query result list")
	void testRuleTemplateQueryResultToResponseBodyDtoMapsList() {
		// Given
		List<RuleTemplateNameQueryResultDto> projection = RuleTemplateMapperTestDataFactory
			.createRuleTemplateNameQueryResultDtoList();

		// When
		List<RuleTemplateNameResponseBodyDto> result = RuleTemplateMapper.INSTANCE
			.ruleTemplateQueryResultToResponseBodyDto(projection);

		// Then
		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(Integer.valueOf(101));
		assertThat(result.get(0).getTemplateName()).isEqualTo(WEEKDAY_TEMPLATE_NAME);
		assertThat(result.get(0).getIsDefault()).isEqualTo(Integer.valueOf(1));
		assertThat(result.get(1).getId()).isEqualTo(Integer.valueOf(102));
		assertThat(result.get(1).getTemplateName()).isEqualTo(WEEKEND_TEMPLATE_NAME);
		assertThat(result.get(1).getIsDefault()).isEqualTo(Integer.valueOf(0));
	}

	@Test
	@DisplayName("ruleTemplateQueryResultToResponseBodyDto should return null for null source list")
	void testRuleTemplateQueryResultToResponseBodyDtoNullListReturnsNull() {
		// Given

		// When
		List<RuleTemplateNameResponseBodyDto> result = RuleTemplateMapper.INSTANCE
			.ruleTemplateQueryResultToResponseBodyDto(null);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("ruleTemplateQueryResultToResponseBodyDto should keep null element when source list contains null")
	void testRuleTemplateQueryResultToResponseBodyDtoWithNullElement() {
		// Given
		List<RuleTemplateNameQueryResultDto> projection = new ArrayList<>();
		projection.add(RuleTemplateMapperTestDataFactory.createRuleTemplateNameQueryResultDto());
		projection.add(null);

		// When
		List<RuleTemplateNameResponseBodyDto> result = RuleTemplateMapper.INSTANCE
			.ruleTemplateQueryResultToResponseBodyDto(projection);

		// Then
		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.get(0)).isNotNull();
		assertThat(result.get(0).getTemplateName()).isEqualTo(WEEKDAY_TEMPLATE_NAME);
		assertThat(result.get(1)).isNull();
	}

	@Test
	@DisplayName("ruleTemplateListQueryResultToResponseBodyDto should map and ignore addedBy updatedBy")
	void testRuleTemplateListQueryResultToResponseBodyDtoMapsAndIgnoresUserDtos() {
		// Given
		RuleTemplateListQueryResultDto projection = RuleTemplateMapperTestDataFactory
			.createRuleTemplateListQueryResultDto();

		// When
		RuleTemplateListResponseBodyDto result = RuleTemplateMapper.INSTANCE
			.ruleTemplateListQueryResultToResponseBodyDto(projection);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(Integer.valueOf(101));
		assertThat(result.getTemplateName()).isEqualTo(WEEKDAY_TEMPLATE_NAME);
		assertThat(result.getIsDefault()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getAddedOn()).isEqualTo(Integer.valueOf(1710000000));
		assertThat(result.getUpdatedOn()).isEqualTo(Integer.valueOf(1711000000));
		assertThat(result.getAddedBy()).isNull();
		assertThat(result.getUpdatedBy()).isNull();
	}

	@Test
	@DisplayName("ruleTemplateListQueryResultToResponseBodyDto should return null for null source")
	void testRuleTemplateListQueryResultToResponseBodyDtoNullSourceReturnsNull() {
		// Given

		// When
		RuleTemplateListResponseBodyDto result = RuleTemplateMapper.INSTANCE
			.ruleTemplateListQueryResultToResponseBodyDto(null);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("ruleTemplateListQueryResultToResponseBodyDtoList should map list")
	void testRuleTemplateListQueryResultToResponseBodyDtoListMapsList() {
		// Given
		List<RuleTemplateListQueryResultDto> projection = RuleTemplateMapperTestDataFactory
			.createRuleTemplateListQueryResultDtoList();

		// When
		List<RuleTemplateListResponseBodyDto> result = RuleTemplateMapper.INSTANCE
			.ruleTemplateListQueryResultToResponseBodyDtoList(projection);

		// Then
		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(Integer.valueOf(101));
		assertThat(result.get(0).getTemplateName()).isEqualTo(WEEKDAY_TEMPLATE_NAME);
		assertThat(result.get(0).getAddedBy()).isNull();
		assertThat(result.get(0).getUpdatedBy()).isNull();
		assertThat(result.get(1).getId()).isEqualTo(Integer.valueOf(202));
		assertThat(result.get(1).getTemplateName()).isEqualTo(NIGHT_SHIFT_TEMPLATE_NAME);
		assertThat(result.get(1).getAddedBy()).isNull();
		assertThat(result.get(1).getUpdatedBy()).isNull();
	}

	@Test
	@DisplayName("ruleTemplateListQueryResultToResponseBodyDtoList should return null for null source list")
	void testRuleTemplateListQueryResultToResponseBodyDtoListNullSourceReturnsNull() {
		// Given

		// When
		List<RuleTemplateListResponseBodyDto> result = RuleTemplateMapper.INSTANCE
			.ruleTemplateListQueryResultToResponseBodyDtoList(null);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("ruleTemplateListQueryResultToResponseBodyDtoList should keep null element when source list contains null")
	void testRuleTemplateListQueryResultToResponseBodyDtoListWithNullElement() {
		// Given
		List<RuleTemplateListQueryResultDto> projection = new ArrayList<>();
		projection.add(RuleTemplateMapperTestDataFactory.createRuleTemplateListQueryResultDto());
		projection.add(null);

		// When
		List<RuleTemplateListResponseBodyDto> result = RuleTemplateMapper.INSTANCE
			.ruleTemplateListQueryResultToResponseBodyDtoList(projection);

		// Then
		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.get(0)).isNotNull();
		assertThat(result.get(0).getTemplateName()).isEqualTo(WEEKDAY_TEMPLATE_NAME);
		assertThat(result.get(1)).isNull();
	}

}
