package io.recruitcrm.microservice.timesheet.controllers.rule_engine.rule_template;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.MarkDefaultRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.CloneTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.rule_engine.rule_template.RuleTemplateService;
import io.recruitcrm.microservice.timesheet.testdata.RuleTemplateTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RuleTemplateControllerTests {

	@Mock
	private RuleTemplateService ruleTemplateService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private RuleTemplateController ruleTemplateController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Create rule template successfully")
	void testCreateRuleTemplateValidRequestCreatesTemplate() {
		// Arrange
		CreateRuleTemplateRequestBodyDto requestDto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.ruleTemplateService).createRuleTemplate(requestDto);
		Mockito
			.when(this.apiResponder.respond(null,
					RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_CREATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.CREATED))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.createRuleTemplate(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).createRuleTemplate(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(null, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_CREATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.CREATED);
	}

	@Test
	@DisplayName("Get rule template successfully")
	void testGetRuleTemplateValidIdReturnsTemplate() {
		// Arrange
		Integer templateId = RuleTemplateTestDataFactory.getDefaultTemplateId();
		RuleTemplateResponseBodyDto expectedTemplate = RuleTemplateTestDataFactory.createRuleTemplateResponse();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory
			.createRuleTemplateSuccessResponse(expectedTemplate);

		Mockito.when(this.ruleTemplateService.getRuleTemplate(templateId)).thenReturn(expectedTemplate);
		Mockito
			.when(this.apiResponder.respond(expectedTemplate,
					RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.getRuleTemplate(templateId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).getRuleTemplate(templateId);
		Mockito.verify(this.apiResponder)
			.respond(expectedTemplate, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Delete rule template successfully")
	void testDeleteRuleTemplateValidIdDeletesTemplate() {
		// Arrange
		Integer templateId = RuleTemplateTestDataFactory.getDefaultTemplateId();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.ruleTemplateService).deleteRuleTemplate(templateId);
		Mockito
			.when(this.apiResponder.respond(null,
					RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_DELETED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.deleteRuleTemplate(templateId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).deleteRuleTemplate(templateId);
		Mockito.verify(this.apiResponder)
			.respond(null, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_DELETED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Clone rule template successfully")
	void testCloneRuleTemplateValidIdReturnsClone() {
		// Arrange
		Integer templateId = RuleTemplateTestDataFactory.getDefaultTemplateId();
		CloneTemplateResponseBodyDto expectedClone = RuleTemplateTestDataFactory.createCloneTemplateResponse();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory
			.createCloneTemplateSuccessResponse(expectedClone);

		Mockito.when(this.ruleTemplateService.cloneRuleTemplate(templateId)).thenReturn(expectedClone);
		Mockito
			.when(this.apiResponder.respond(expectedClone,
					RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_CLONED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.CREATED))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.cloneRuleTemplate(templateId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).cloneRuleTemplate(templateId);
		Mockito.verify(this.apiResponder)
			.respond(expectedClone, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_CLONED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.CREATED);
	}

	@Test
	@DisplayName("Get rule template names successfully")
	void testGetRuleTemplateNamesValidRequestReturnsNames() {
		// Arrange
		String search = "test";
		PaginationRequestBodyDto paginationDto = RuleTemplateTestDataFactory.createPaginationRequest();
		List<RuleTemplateNameResponseBodyDto> expectedNames = RuleTemplateTestDataFactory
			.createRuleTemplateNameResponseList();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory
			.createRuleTemplateNameListSuccessResponse(expectedNames);

		Mockito.when(this.ruleTemplateService.getRuleTemplateNames(search, paginationDto.toPageable()))
			.thenReturn(expectedNames);
		Mockito.when(this.apiResponder.respond(expectedNames,
				RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_NAMES_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.getRuleTemplateNames(search, paginationDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).getRuleTemplateNames(search, paginationDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedNames, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_NAMES_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get all rule templates successfully")
	void testGetAllRuleTemplatesValidRequestReturnsTemplates() {
		// Arrange
		SearchRequestBodyDto searchDto = RuleTemplateTestDataFactory.createSearchRequest();
		String search = "test";
		PaginationRequestBodyDto paginationDto = RuleTemplateTestDataFactory.createPaginationRequest();
		List<RuleTemplateListResponseBodyDto> expectedTemplates = RuleTemplateTestDataFactory
			.createRuleTemplateListResponseList();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory
			.createRuleTemplateListSuccessResponse(expectedTemplates);

		Mockito.when(this.ruleTemplateService.getAllRuleTemplates(searchDto, search, paginationDto.toPageable()))
			.thenReturn(expectedTemplates);
		Mockito.when(this.apiResponder.respond(expectedTemplates,
				RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_FETCHED_SUCCESSFULLY_LIST, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.getAllRuleTemplates(searchDto, search, paginationDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).getAllRuleTemplates(searchDto, search, paginationDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedTemplates, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_FETCHED_SUCCESSFULLY_LIST,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Update rule template successfully")
	void testUpdateRuleTemplateValidRequestUpdatesTemplate() {
		// Arrange
		Integer templateId = RuleTemplateTestDataFactory.getDefaultTemplateId();
		CreateRuleTemplateRequestBodyDto requestDto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.ruleTemplateService).updateRuleTemplate(templateId, requestDto);
		Mockito
			.when(this.apiResponder.respond(null,
					RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.updateRuleTemplate(templateId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).updateRuleTemplate(templateId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(null, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Mark as default successfully")
	void testMarkAsDefaultValidRequestMarksAsDefault() {
		// Arrange
		Integer templateId = RuleTemplateTestDataFactory.getDefaultTemplateId();
		MarkDefaultRequestBodyDto requestDto = RuleTemplateTestDataFactory.createMarkDefaultRequest();
		ResponseEntity<?> expectedResponseEntity = RuleTemplateTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.ruleTemplateService).markAsDefault(templateId, requestDto.getIsDefault());
		Mockito.when(this.apiResponder.respond(null,
				RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_MARKED_AS_DEFAULT_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.ruleTemplateController.markAsDefault(templateId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.ruleTemplateService).markAsDefault(templateId, requestDto.getIsDefault());
		Mockito.verify(this.apiResponder)
			.respond(null, RuleTemplateTestDataFactory.Messages.RULE_TEMPLATE_MARKED_AS_DEFAULT_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}