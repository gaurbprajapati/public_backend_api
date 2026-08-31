package io.recruitcrm.microservice.timesheet.services.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.CloneTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.FetchUserAndContactUserIds;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.mapper.rule_engine.rule_template.RuleTemplateMapper;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template.RuleTemplateRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleTemplateConstants;
import io.recruitcrm.microservice.timesheet.services.rule_engine.rule_template.RuleTemplateService;
import io.recruitcrm.microservice.timesheet.testdata.RuleTemplateTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RuleTemplateServiceTests {

	@Mock
	private AuthHolder auth;

	@Mock
	private RuleTemplateRepository ruleTemplateRepository;

	@Mock
	private RuleTemplateMapper ruleTemplateMapper;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ContactRepository contactRepository;

	@Mock
	private FetchUserAndContactUserIds fetchUserAndContactUserIds;

	@InjectMocks
	private RuleTemplateService ruleTemplateService;

	@BeforeEach
	void setUp() {
		this.ruleTemplateService = new RuleTemplateService(this.auth, this.ruleTemplateRepository,
				this.ruleTemplateMapper, this.userRepository, this.contactRepository, this.fetchUserAndContactUserIds);
	}

	@Test
	@DisplayName("createRuleTemplate - success")
	void testCreateRuleTemplateSuccess() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));
		this.ruleTemplateService.createRuleTemplate(dto);
		verify(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("createRuleTemplate - should set isDefault to 0 for new templates")
	void testCreateRuleTemplateSetsIsDefaultZero() {
		// Given
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		this.ruleTemplateService.createRuleTemplate(dto);

		// Then
		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		RuleTemplate saved = captor.getValue();
		assertThat(saved.getIsDefault()).isZero();
	}

	@Test
	@DisplayName("createRuleTemplate - maps isUnplannedHoursPayEnabled onto entity")
	void testCreateRuleTemplateMapsIsUnplannedHoursPayEnabled() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(dto.getIsUnplannedHoursPayEnabled()).willReturn(1);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		this.ruleTemplateService.createRuleTemplate(dto);

		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		assertThat(captor.getValue().getIsUnplannedHoursPayEnabled()).isEqualTo(1);
	}

	@Test
	@DisplayName("createRuleTemplate - null isUnplannedHoursPayEnabled defaults entity field to 0")
	void testCreateRuleTemplateNullIsUnplannedHoursPayEnabledDefaultsToZero() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(dto.getIsUnplannedHoursPayEnabled()).willReturn(null);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		this.ruleTemplateService.createRuleTemplate(dto);

		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		assertThat(captor.getValue().getIsUnplannedHoursPayEnabled()).isZero();
	}

	@Test
	@DisplayName("createRuleTemplate - duplicate name throws ValidationErrorException")
	void testCreateRuleTemplateDuplicateNameThrows() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(RuleTemplateConstants.UNIQUE_TEMPLATE_NAME_CONSTRAINT))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));
		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(ValidationErrorException.class);
	}

	@Test
	@DisplayName("createRuleTemplate - template name too long throws ValidationErrorException")
	void testCreateRuleTemplateTemplateNameTooLongThrowsValidationErrorException() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				"Data truncation: Data too long for column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("createRuleTemplate - null template name throws ValidationErrorException")
	void testCreateRuleTemplateNullTemplateNameThrowsValidationErrorException() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				"Column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "' cannot be null"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(RuleTemplateConstants.TEMPLATE_NAME_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("getRuleTemplate - found")
	void testGetRuleTemplateFound() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		RuleTemplateResponseBodyDto responseDto = mock(RuleTemplateResponseBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(this.ruleTemplateMapper.ruleTemplateToResponseBodyDto(ruleTemplate)).willReturn(responseDto);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(1);
		RuleTemplateResponseBodyDto result = this.ruleTemplateService.getRuleTemplate(1);
		assertThat(result).isEqualTo(responseDto);
		then(responseDto).should().setIsUnplannedHoursPayEnabled(1);
	}

	@Test
	@DisplayName("getRuleTemplate - not found throws ResourceNotFoundException")
	void testGetRuleTemplateNotFoundThrows() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(null);
		assertThatThrownBy(() -> this.ruleTemplateService.getRuleTemplate(1))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("getRuleTemplate - found with template work days and custom rules")
	void testGetRuleTemplateFoundWithDetails() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		RuleTemplateResponseBodyDto responseDto = mock(RuleTemplateResponseBodyDto.class);
		List<TemplateWorkDay> templateWorkDays = List.of(mock(TemplateWorkDay.class));
		List<CustomRule> customRules = List.of(mock(CustomRule.class));

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(this.ruleTemplateMapper.ruleTemplateToResponseBodyDto(ruleTemplate)).willReturn(responseDto);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(templateWorkDays);
		given(ruleTemplate.getCustomRule()).willReturn(customRules);
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(0);

		// When
		RuleTemplateResponseBodyDto result = this.ruleTemplateService.getRuleTemplate(1);

		// Then
		assertThat(result).isEqualTo(responseDto);
		then(responseDto).should().setTemplateWorkDays(templateWorkDays);
		then(responseDto).should().setCustomRules(customRules);
		then(responseDto).should().setIsUnplannedHoursPayEnabled(0);
	}

	@Test
	@DisplayName("getRuleTemplate - validates account access with different account ID")
	void testGetRuleTemplateValidatesAccountAccess() {
		// Given
		Integer templateId = 1;
		Integer accountId = 999;
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		RuleTemplateResponseBodyDto responseDto = mock(RuleTemplateResponseBodyDto.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.ruleTemplateRepository.getRuleTemplate(templateId, accountId)).willReturn(ruleTemplate);
		given(this.ruleTemplateMapper.ruleTemplateToResponseBodyDto(ruleTemplate)).willReturn(responseDto);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(1);

		// When
		RuleTemplateResponseBodyDto result = this.ruleTemplateService.getRuleTemplate(templateId);

		// Then
		assertThat(result).isEqualTo(responseDto);
		then(this.ruleTemplateRepository).should().getRuleTemplate(templateId, accountId);
		then(responseDto).should().setIsUnplannedHoursPayEnabled(1);
	}

	@Test
	@DisplayName("getRuleTemplate - throws ResourceNotFoundException with correct message")
	void testGetRuleTemplateNotFoundThrowsWithCorrectMessage() {
		// Given
		Integer templateId = 999;
		Integer accountId = 1;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.ruleTemplateRepository.getRuleTemplate(templateId, accountId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleTemplateService.getRuleTemplate(templateId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Rule template not found with id: " + templateId);

		then(this.ruleTemplateRepository).should().getRuleTemplate(templateId, accountId);
	}

	@Test
	@DisplayName("getRuleTemplate - handles empty template work days and custom rules")
	void testGetRuleTemplateHandlesEmptyCollections() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		RuleTemplateResponseBodyDto responseDto = mock(RuleTemplateResponseBodyDto.class);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(this.ruleTemplateMapper.ruleTemplateToResponseBodyDto(ruleTemplate)).willReturn(responseDto);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(null);

		// When
		RuleTemplateResponseBodyDto result = this.ruleTemplateService.getRuleTemplate(1);

		// Then
		assertThat(result).isEqualTo(responseDto);
		then(responseDto).should().setTemplateWorkDays(new ArrayList<>());
		then(responseDto).should().setCustomRules(new ArrayList<>());
		then(responseDto).should().setIsUnplannedHoursPayEnabled(0);
	}

	@Test
	@DisplayName("getRuleTemplateNames - found")
	void testGetRuleTemplateNamesFound() {
		List<RuleTemplateNameQueryResultDto> queryResults = List.of(mock(RuleTemplateNameQueryResultDto.class));
		List<RuleTemplateNameResponseBodyDto> responseDtos = List.of(mock(RuleTemplateNameResponseBodyDto.class));
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplateNames(anyString(), any(Pageable.class), eq(1)))
			.willReturn(queryResults);
		given(this.ruleTemplateMapper.ruleTemplateQueryResultToResponseBodyDto(queryResults)).willReturn(responseDtos);
		List<RuleTemplateNameResponseBodyDto> result = this.ruleTemplateService.getRuleTemplateNames("search",
				Pageable.unpaged());
		assertThat(result).isEqualTo(responseDtos);
	}

	@Test
	@DisplayName("getRuleTemplateNames - not found returns empty list")
	void testGetRuleTemplateNamesNotFoundReturnsEmpty() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplateNames(anyString(), any(Pageable.class), eq(1)))
			.willReturn(null);
		List<RuleTemplateNameResponseBodyDto> result = this.ruleTemplateService.getRuleTemplateNames("search",
				Pageable.unpaged());
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getAllRuleTemplates - found")
	void testGetAllRuleTemplatesFound() {
		List<RuleTemplateListQueryResultDto> queryResults = List.of(mock(RuleTemplateListQueryResultDto.class));
		List<RuleTemplateListResponseBodyDto> responseDtos = List.of(mock(RuleTemplateListResponseBodyDto.class));
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getAllRuleTemplates(any(), anyString(), any(Pageable.class), eq(1)))
			.willReturn(queryResults);
		given(this.ruleTemplateMapper.ruleTemplateListQueryResultToResponseBodyDtoList(queryResults))
			.willReturn(responseDtos);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(new HashMap<>());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(new HashMap<>());
		List<RuleTemplateListResponseBodyDto> result = this.ruleTemplateService
			.getAllRuleTemplates(mock(SearchRequestBodyDto.class), "search", Pageable.unpaged());
		assertThat(result).isEqualTo(responseDtos);
	}

	@Test
	@DisplayName("getAllRuleTemplates - not found returns empty list")
	void testGetAllRuleTemplatesNotFoundReturnsEmpty() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getAllRuleTemplates(any(), anyString(), any(Pageable.class), eq(1)))
			.willReturn(null);
		List<RuleTemplateListResponseBodyDto> result = this.ruleTemplateService
			.getAllRuleTemplates(mock(SearchRequestBodyDto.class), "search", Pageable.unpaged());
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("updateRuleTemplate - success")
	void testUpdateRuleTemplateSuccess() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		List<CustomRule> customRules = new ArrayList<>();
		given(dto.getCustomRules()).willReturn(customRules);
		willDoNothing().given(this.ruleTemplateRepository).updateRuleTemplate(any(RuleTemplate.class));
		this.ruleTemplateService.updateRuleTemplate(1, dto);
		verify(this.ruleTemplateRepository).updateRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("updateRuleTemplate - not found throws ValidationErrorException")
	void testUpdateRuleTemplateNotFoundThrows() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(null);
		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(ValidationErrorException.class);
	}

	@Test
	@DisplayName("updateRuleTemplate - duplicate name throws ValidationErrorException")
	void testUpdateRuleTemplateDuplicateNameThrows() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getTemplateName()).willReturn("aaa-662");
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(RuleTemplateConstants.UNIQUE_TEMPLATE_NAME_CONSTRAINT))
			.given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Rule template with name 'aaa-662' already exists.");
	}

	@Test
	@DisplayName("cloneRuleTemplate - success creates template and returns response DTO")
	void testCloneRuleTemplateSuccess() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(2);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(1);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		CloneTemplateResponseBodyDto result = this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		assertThat(result.getWorkLogType()).isEqualTo(2);
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getCustomRulesCount()).isZero();
		assertThat(result.getWorkDayIds()).isEmpty();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(1);
		ArgumentCaptor<RuleTemplate> cloneCaptor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(cloneCaptor.capture());
		assertThat(cloneCaptor.getValue().getIsUnplannedHoursPayEnabled()).isEqualTo(1);
	}

	@Test
	@DisplayName("cloneRuleTemplate - success with work days and custom rules returns populated response")
	void testCloneRuleTemplateSuccessWithWorkDaysAndCustomRules() {
		// Given
		TemplateWorkDay workDay = mock(TemplateWorkDay.class);
		given(workDay.getWorkDayId()).willReturn(1);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		List<CustomRule> customRules = List.of(mock(CustomRule.class), mock(CustomRule.class));
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("My Template");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(false);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(30);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(List.of(workDay));
		given(ruleTemplate.getCustomRule()).willReturn(customRules);
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(0);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		CloneTemplateResponseBodyDto result = this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		assertThat(result.getWorkLogType()).isEqualTo(1);
		assertThat(result.getCalculateBreakTime()).isFalse();
		assertThat(result.getCustomRulesCount()).isEqualTo(2);
		assertThat(result.getWorkDayIds()).containsExactly(1);
		assertThat(result.getIsUnplannedHoursPayEnabled()).isZero();
		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		assertThat(captor.getValue().getTemplateName()).isEqualTo("(Clone) My Template");
		assertThat(captor.getValue().getIsUnplannedHoursPayEnabled()).isZero();
	}

	@Test
	@DisplayName("cloneRuleTemplate - should set isDefault to 0 for cloned template")
	void testCloneRuleTemplateSetsIsDefaultZero() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Template A");
		given(ruleTemplate.getIsUnplannedHoursPayEnabled()).willReturn(null);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		CloneTemplateResponseBodyDto result = this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		assertThat(result.getIsUnplannedHoursPayEnabled()).isZero();
		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		RuleTemplate saved = captor.getValue();
		assertThat(saved.getIsDefault()).isZero();
		assertThat(saved.getIsUnplannedHoursPayEnabled()).isZero();
	}

	@Test
	@DisplayName("cloneRuleTemplate - not found throws ResourceNotFoundException with message")
	void testCloneRuleTemplateNotFoundThrows() {
		// Given
		Integer templateId = 99;
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(templateId, 1)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(templateId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Rule template not found with id: " + templateId);

		then(this.ruleTemplateRepository).should(never()).createRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("cloneRuleTemplate - duplicate name throws ValidationErrorException with message")
	void testCloneRuleTemplateDuplicateNameThrows() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(RuleTemplateConstants.UNIQUE_TEMPLATE_NAME_CONSTRAINT))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		// When & Then
		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Rule template with name '(Clone) Test' already exists.");
	}

	@Test
	@DisplayName("cloneRuleTemplate - template name null uses empty string for clone name")
	void testCloneRuleTemplateWithNullTemplateName() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn(null);
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		CloneTemplateResponseBodyDto result = this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		assertThat(captor.getValue().getTemplateName()).isEqualTo("(Clone) ");
		assertThat(result.getWorkLogType()).isEqualTo(1);
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getCustomRulesCount()).isZero();
		assertThat(result.getWorkDayIds()).isEmpty();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isZero();
	}

	@Test
	@DisplayName("cloneRuleTemplate - long template name truncated with ellipsis")
	void testCloneRuleTemplateWithLongTemplateNameTruncated() {
		// Given - name longer than 187 chars (200 - 13 for "(Clone) ")
		String longName = "A".repeat(200);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn(longName);
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(false);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(30);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		String cloneName = captor.getValue().getTemplateName();
		assertThat(cloneName).startsWith("(Clone) ").endsWith("...");
		assertThat(cloneName.length()).isLessThanOrEqualTo(200);
	}

	@Test
	@DisplayName("cloneRuleTemplate - getCustomRule null returns zero customRulesCount")
	void testCloneRuleTemplateWithNullCustomRule() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Template");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(null);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		CloneTemplateResponseBodyDto result = this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		assertThat(result.getCustomRulesCount()).isZero();
	}

	@Test
	@DisplayName("cloneRuleTemplate - getTemplateWorkDay null returns empty workDayIds")
	void testCloneRuleTemplateWithNullTemplateWorkDay() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Template");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(null);
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		CloneTemplateResponseBodyDto result = this.ruleTemplateService.cloneRuleTemplate(1);

		// Then
		assertThat(result.getWorkDayIds()).isEmpty();
	}

	@Test
	@DisplayName("cloneRuleTemplate - DataIntegrityViolationException with null message rethrown")
	void testCloneRuleTemplateDataIntegrityViolationExceptionWithNullMessageRethrown() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException((String) null)).given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		// When & Then
		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("cloneRuleTemplate - DataIntegrityViolationException without uk_rule_template_name rethrown")
	void testCloneRuleTemplateDataIntegrityViolationExceptionWithoutUkName() {
		// Given
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException("other error")).given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		// When & Then
		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("other error");
	}

	@Test
	@DisplayName("deleteRuleTemplate - success")
	void testDeleteRuleTemplateSuccess() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		willDoNothing().given(this.ruleTemplateRepository).deleteRuleTemplate(1);
		this.ruleTemplateService.deleteRuleTemplate(1);
		verify(this.ruleTemplateRepository).deleteRuleTemplate(1);
	}

	@Test
	@DisplayName("deleteRuleTemplate - not found throws ResourceNotFoundException")
	void testDeleteRuleTemplateNotFoundThrows() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(null);
		assertThatThrownBy(() -> this.ruleTemplateService.deleteRuleTemplate(1))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("createRuleTemplate - DataIntegrityViolationException with null message rethrown")
	void testCreateRuleTemplateDataIntegrityViolationExceptionWithNullMessageRethrown() {
		// Given
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException((String) null)).given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		// When & Then
		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("createRuleTemplate - with custom rules assigns sequential ids")
	void testCreateRuleTemplateWithCustomRulesAssignsSequentialIds() {
		// Given
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		List<CustomRule> customRules = RuleTemplateTestDataFactory.createCustomRulesList();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(customRules);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		this.ruleTemplateService.createRuleTemplate(dto);

		// Then
		assertThat(customRules.get(0).getId()).isEqualTo(1);
		assertThat(customRules.get(1).getId()).isEqualTo(2);
		then(this.ruleTemplateRepository).should().createRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("createRuleTemplate - calculateBreakTime true sets breakTimeThreshold to null")
	void testCreateRuleTemplateWithCalculateBreakTimeTrueSetsBreakTimeThresholdNull() {
		// Given
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(dto.getCalculateBreakTime()).willReturn(true);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));

		// When
		this.ruleTemplateService.createRuleTemplate(dto);

		// Then
		ArgumentCaptor<RuleTemplate> captor = ArgumentCaptor.forClass(RuleTemplate.class);
		then(this.ruleTemplateRepository).should().createRuleTemplate(captor.capture());
		assertThat(captor.getValue().getBreakTimeThreshold()).isNull();
	}

	@Test
	@DisplayName("createRuleTemplate - with null customRules")
	void testCreateRuleTemplateWithNullCustomRules() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(null);
		willDoNothing().given(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));
		this.ruleTemplateService.createRuleTemplate(dto);
		verify(this.ruleTemplateRepository).createRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("createRuleTemplate - DataIntegrityViolationException without uk_rule_template_name")
	void testCreateRuleTemplateDataIntegrityViolationExceptionWithoutUkName() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException("other error")).given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));
		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("other error");
	}

	@Test
	@DisplayName("getAllRuleTemplates - with user details not found")
	void testGetAllRuleTemplatesWithUserDetailsNotFound() {
		List<RuleTemplateListQueryResultDto> queryResults = List.of(createMockQueryResultDto());
		List<RuleTemplateListResponseBodyDto> responseDtos = List.of(mock(RuleTemplateListResponseBodyDto.class));
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getAllRuleTemplates(any(), anyString(), any(Pageable.class), eq(1)))
			.willReturn(queryResults);
		given(this.ruleTemplateMapper.ruleTemplateListQueryResultToResponseBodyDtoList(queryResults))
			.willReturn(responseDtos);
		given(this.userRepository.getUserDetailsMap(anySet())).willReturn(Collections.emptyMap());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(Collections.emptyMap());
		List<RuleTemplateListResponseBodyDto> result = this.ruleTemplateService
			.getAllRuleTemplates(mock(SearchRequestBodyDto.class), "search", Pageable.unpaged());
		assertThat(result).isEqualTo(responseDtos);
	}

	@Test
	@DisplayName("setAddedByUserDetails - agency recruiter with user details")
	void testSetAddedByUserDetailsAgencyRecruiterWithUserDetails() throws Exception {
		// Given
		RuleTemplateListQueryResultDto template = RuleTemplateTestDataFactory.createAgencyAddedByQueryResultDto();
		RuleTemplateListResponseBodyDto response = new RuleTemplateListResponseBodyDto();
		UserDetailsQueryResultDto userDetails = RuleTemplateTestDataFactory.createUserDetailsQueryResult();
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Map.of(template.getAddedBy(), userDetails);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Collections.emptyMap();

		Method method = RuleTemplateService.class.getDeclaredMethod("setAddedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);

		// When
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		// Then
		assertThat(response.getAddedBy()).isNotNull();
		assertThat(response.getAddedBy().getId()).isEqualTo(template.getAddedBy());
		assertThat(response.getAddedBy().getName()).isEqualTo(userDetails.getName());
		assertThat(response.getAddedBy().getPhoto()).isEqualTo(userDetails.getProfilePic());
		assertThat(response.getAddedBy().getUserTypeId()).isEqualTo(template.getAddedByUserTypeId());
	}

	@Test
	@DisplayName("setAddedByUserDetails - agency recruiter with null user details")
	void testSetAddedByUserDetailsAgencyRecruiterWithNullUserDetails() throws Exception {
		RuleTemplateListQueryResultDto template = mock(RuleTemplateListQueryResultDto.class);
		RuleTemplateListResponseBodyDto response = mock(RuleTemplateListResponseBodyDto.class);
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Collections.emptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Collections.emptyMap();

		given(template.getAddedByUserTypeId()).willReturn(UserTypeEnum.AGENCY_RECRUITER.getId());
		given(template.getAddedBy()).willReturn(1);

		Method method = RuleTemplateService.class.getDeclaredMethod("setAddedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		verify(response, never()).setAddedBy(any());
	}

	@Test
	@DisplayName("setAddedByUserDetails - contact user with contact details")
	void testSetAddedByUserDetailsContactUserWithContactDetails() throws Exception {
		// Given
		RuleTemplateListQueryResultDto template = RuleTemplateTestDataFactory.createContactAddedByQueryResultDto();
		RuleTemplateListResponseBodyDto response = new RuleTemplateListResponseBodyDto();
		ContactNamePhotoQueryResultDto contactDetails = RuleTemplateTestDataFactory.createContactNamePhotoQueryResult();
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Collections.emptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Map.of(template.getAddedBy(), contactDetails);

		Method method = RuleTemplateService.class.getDeclaredMethod("setAddedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);

		// When
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		// Then
		assertThat(response.getAddedBy()).isNotNull();
		assertThat(response.getAddedBy().getId()).isEqualTo(template.getAddedBy());
		assertThat(response.getAddedBy().getName()).isEqualTo(contactDetails.getName());
		assertThat(response.getAddedBy().getPhoto()).isEqualTo(contactDetails.getProfilePic());
		assertThat(response.getAddedBy().getUserTypeId()).isEqualTo(template.getAddedByUserTypeId());
	}

	@Test
	@DisplayName("setAddedByUserDetails - contact user with null contact details")
	void testSetAddedByUserDetailsContactUserWithNullContactDetails() throws Exception {
		RuleTemplateListQueryResultDto template = mock(RuleTemplateListQueryResultDto.class);
		RuleTemplateListResponseBodyDto response = mock(RuleTemplateListResponseBodyDto.class);
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Collections.emptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Collections.emptyMap();

		given(template.getAddedByUserTypeId()).willReturn(UserTypeEnum.COMPANY_CONTACT.getId());
		given(template.getAddedBy()).willReturn(1);

		Method method = RuleTemplateService.class.getDeclaredMethod("setAddedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		verify(response, never()).setAddedBy(any());
	}

	@Test
	@DisplayName("setUpdatedByUserDetails - agency recruiter with null user details")
	void testSetUpdatedByUserDetailsAgencyRecruiterWithNullUserDetails() throws Exception {
		// Given
		RuleTemplateListQueryResultDto template = mock(RuleTemplateListQueryResultDto.class);
		RuleTemplateListResponseBodyDto response = mock(RuleTemplateListResponseBodyDto.class);
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Collections.emptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Collections.emptyMap();

		given(template.getUpdatedByUserTypeId()).willReturn(UserTypeEnum.AGENCY_RECRUITER.getId());
		given(template.getUpdatedBy()).willReturn(1);

		Method method = RuleTemplateService.class.getDeclaredMethod("setUpdatedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);

		// When
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		// Then
		then(response).should(never()).setUpdatedBy(any());
	}

	@Test
	@DisplayName("setUpdatedByUserDetails - agency recruiter with user details")
	void testSetUpdatedByUserDetailsAgencyRecruiterWithUserDetails() throws Exception {
		RuleTemplateListQueryResultDto template = mock(RuleTemplateListQueryResultDto.class);
		RuleTemplateListResponseBodyDto response = mock(RuleTemplateListResponseBodyDto.class);
		UserDetailsQueryResultDto userDetails = mock(UserDetailsQueryResultDto.class);
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Map.of(1, userDetails);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Collections.emptyMap();

		given(template.getUpdatedByUserTypeId()).willReturn(UserTypeEnum.AGENCY_RECRUITER.getId());
		given(template.getUpdatedBy()).willReturn(1);
		given(userDetails.getName()).willReturn("Test User");
		given(userDetails.getProfilePic()).willReturn("test.jpg");

		Method method = RuleTemplateService.class.getDeclaredMethod("setUpdatedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		verify(response).setUpdatedBy(any(UpdatedByResponseBodyDto.class));
	}

	@Test
	@DisplayName("setUpdatedByUserDetails - contact user with contact details")
	void testSetUpdatedByUserDetailsContactUserWithContactDetails() throws Exception {
		// Given
		RuleTemplateListQueryResultDto template = RuleTemplateTestDataFactory.createAgencyAddedByQueryResultDto();
		RuleTemplateListResponseBodyDto response = new RuleTemplateListResponseBodyDto();
		ContactNamePhotoQueryResultDto contactDetails = RuleTemplateTestDataFactory.createContactNamePhotoQueryResult();
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Collections.emptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Map.of(template.getUpdatedBy(), contactDetails);

		Method method = RuleTemplateService.class.getDeclaredMethod("setUpdatedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);

		// When
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		// Then
		assertThat(response.getUpdatedBy()).isNotNull();
		assertThat(response.getUpdatedBy().getId()).isEqualTo(template.getUpdatedBy());
		assertThat(response.getUpdatedBy().getName()).isEqualTo(contactDetails.getName());
		assertThat(response.getUpdatedBy().getPhoto()).isEqualTo(contactDetails.getProfilePic());
		assertThat(response.getUpdatedBy().getUserTypeId()).isEqualTo(template.getUpdatedByUserTypeId());
	}

	@Test
	@DisplayName("setUpdatedByUserDetails - contact user with null contact details")
	void testSetUpdatedByUserDetailsContactUserWithNullContactDetails() throws Exception {
		RuleTemplateListQueryResultDto template = mock(RuleTemplateListQueryResultDto.class);
		RuleTemplateListResponseBodyDto response = mock(RuleTemplateListResponseBodyDto.class);
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = Collections.emptyMap();
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = Collections.emptyMap();

		given(template.getUpdatedByUserTypeId()).willReturn(UserTypeEnum.COMPANY_CONTACT.getId());
		given(template.getUpdatedBy()).willReturn(1);

		Method method = RuleTemplateService.class.getDeclaredMethod("setUpdatedByUserDetails",
				RuleTemplateListQueryResultDto.class, RuleTemplateListResponseBodyDto.class, Map.class, Map.class);
		method.setAccessible(true);
		method.invoke(this.ruleTemplateService, template, response, agencyUsersMap, contactUsersMap);

		verify(response, never()).setUpdatedBy(any());
	}

	@Test
	@DisplayName("getTemplateWorkDaysRequestBodyDtos - with null workStartTime and workEndTime")
	void testGetTemplateWorkDaysRequestBodyDtosWithNullWorkStartTimeAndWorkEndTime() throws Exception {
		// Given
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(dto.getWorkDayIds()).willReturn(List.of(1, 2));
		given(dto.getWorkTime()).willReturn(List.of(8, 9));
		given(dto.getWorkStartTime()).willReturn(null);
		given(dto.getWorkEndTime()).willReturn(null);

		Method method = RuleTemplateService.class.getDeclaredMethod("getTemplateWorkDaysRequestBodyDtos",
				CreateRuleTemplateRequestBodyDto.class);
		method.setAccessible(true);

		// When
		@SuppressWarnings("unchecked")
		List<TemplateWorkDay> result = (List<TemplateWorkDay>) method.invoke(this.ruleTemplateService, dto);

		// Then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getWorkStartTime()).isNull();
		assertThat(result.get(0).getWorkEndTime()).isNull();
		assertThat(result.get(1).getWorkStartTime()).isNull();
		assertThat(result.get(1).getWorkEndTime()).isNull();
	}

	@Test
	@DisplayName("getTemplateWorkDaysRequestBodyDtos - with null workTime")
	void testGetTemplateWorkDaysRequestBodyDtosWithNullWorkTime() throws Exception {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(dto.getWorkDayIds()).willReturn(List.of(1, 2));
		given(dto.getWorkTime()).willReturn(null);
		given(dto.getWorkStartTime()).willReturn(List.of(9, 10));
		given(dto.getWorkEndTime()).willReturn(List.of(17, 18));

		Method method = RuleTemplateService.class.getDeclaredMethod("getTemplateWorkDaysRequestBodyDtos",
				CreateRuleTemplateRequestBodyDto.class);
		method.setAccessible(true);
		List<TemplateWorkDay> result = (List<TemplateWorkDay>) method.invoke(this.ruleTemplateService, dto);

		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("getTemplateWorkDaysRequestBodyDtos - with shorter lists")
	void testGetTemplateWorkDaysRequestBodyDtosWithShorterLists() throws Exception {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(dto.getWorkDayIds()).willReturn(List.of(1, 2, 3));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9, 10));
		given(dto.getWorkEndTime()).willReturn(List.of(17));

		Method method = RuleTemplateService.class.getDeclaredMethod("getTemplateWorkDaysRequestBodyDtos",
				CreateRuleTemplateRequestBodyDto.class);
		method.setAccessible(true);
		List<TemplateWorkDay> result = (List<TemplateWorkDay>) method.invoke(this.ruleTemplateService, dto);

		assertThat(result).hasSize(3);
	}

	@Test
	@DisplayName("updateRuleTemplate - with custom rules assigns sequential ids")
	void testUpdateRuleTemplateWithCustomRulesAssignsSequentialIds() {
		// Given
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		List<CustomRule> customRules = RuleTemplateTestDataFactory.createCustomRulesList();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(customRules);
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willDoNothing().given(this.ruleTemplateRepository).updateRuleTemplate(any(RuleTemplate.class));

		// When
		this.ruleTemplateService.updateRuleTemplate(1, dto);

		// Then
		assertThat(customRules.get(0).getId()).isEqualTo(1);
		assertThat(customRules.get(1).getId()).isEqualTo(2);
		then(this.ruleTemplateRepository).should().updateRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("updateRuleTemplate - with null customRules in request")
	void testUpdateRuleTemplateWithNullCustomRulesInRequest() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		willDoNothing().given(this.ruleTemplateRepository).updateRuleTemplate(any(RuleTemplate.class));
		this.ruleTemplateService.updateRuleTemplate(1, dto);
		verify(this.ruleTemplateRepository).updateRuleTemplate(any(RuleTemplate.class));
	}

	@Test
	@DisplayName("getRuleTemplateNames - with empty list result")
	void testGetRuleTemplateNamesWithEmptyList() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getRuleTemplateNames(anyString(), any(Pageable.class), eq(1)))
			.willReturn(Collections.emptyList());
		List<RuleTemplateNameResponseBodyDto> result = this.ruleTemplateService.getRuleTemplateNames("search",
				Pageable.unpaged());
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getAllRuleTemplates - with empty list result")
	void testGetAllRuleTemplatesWithEmptyList() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.ruleTemplateRepository.getAllRuleTemplates(any(), anyString(), any(Pageable.class), eq(1)))
			.willReturn(Collections.emptyList());
		List<RuleTemplateListResponseBodyDto> result = this.ruleTemplateService
			.getAllRuleTemplates(mock(SearchRequestBodyDto.class), "search", Pageable.unpaged());
		assertThat(result).isEmpty();
	}

	private RuleTemplateListQueryResultDto createMockQueryResultDto() {
		RuleTemplateListQueryResultDto dto = mock(RuleTemplateListQueryResultDto.class);
		given(dto.getAddedByUserTypeId()).willReturn(UserTypeEnum.AGENCY_RECRUITER.getId());
		given(dto.getAddedBy()).willReturn(1);
		given(dto.getUpdatedByUserTypeId()).willReturn(UserTypeEnum.COMPANY_CONTACT.getId());
		given(dto.getUpdatedBy()).willReturn(2);
		return dto;
	}

	@ParameterizedTest(name = "isDefault={0}")
	@ValueSource(booleans = { true, false })
	void testMarkAsDefaultValidRequest(boolean isDefault) {
		Integer templateId = 1;
		Integer accountId = 5;
		RuleTemplate existingTemplate = new RuleTemplate();
		existingTemplate.setId(templateId);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.ruleTemplateRepository.getRuleTemplate(templateId, accountId)).willReturn(existingTemplate);
		willDoNothing().given(this.ruleTemplateRepository).markAsDefault(templateId, accountId, isDefault);

		this.ruleTemplateService.markAsDefault(templateId, isDefault);

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.ruleTemplateRepository).should().getRuleTemplate(templateId, accountId);
		then(this.ruleTemplateRepository).should().markAsDefault(templateId, accountId, isDefault);
	}

	@Test
	@DisplayName("Mark as default should throw ResourceNotFoundException when template not found")
	void testMarkAsDefaultTemplateNotFoundThrowsResourceNotFoundException() {
		Integer templateId = 999;
		Boolean isDefault = true;
		Integer accountId = 5;

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.ruleTemplateRepository.getRuleTemplate(templateId, accountId)).willReturn(null);

		assertThatThrownBy(() -> this.ruleTemplateService.markAsDefault(templateId, isDefault))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Rule template not found with id: " + templateId);

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.ruleTemplateRepository).should().getRuleTemplate(templateId, accountId);
		then(this.ruleTemplateRepository).should(never()).markAsDefault(any(), any(), any());
	}

	@Test
	@DisplayName("createRuleTemplate - template name column error without truncation or too long rethrown")
	void testCreateRuleTemplateTemplateNameColumnErrorWithoutTruncationOrTooLongRethrown() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				"Invalid value for column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("createRuleTemplate - Data truncation only throws ValidationErrorException")
	void testCreateRuleTemplateDataIntegrityViolationDataTruncationOnly() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(RuleTemplateConstants.DATA_TRUNCATION + " for column '"
				+ RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("createRuleTemplate - Data too long only throws ValidationErrorException")
	void testCreateRuleTemplateDataIntegrityViolationDataTooLongOnly() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				RuleTemplateConstants.TEMPLATE_NAME_COLUMN + " " + RuleTemplateConstants.DATA_TOO_LONG))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.createRuleTemplate(dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("updateRuleTemplate - null template name throws ValidationErrorException")
	void testUpdateRuleTemplateNullTemplateNameThrowsValidationErrorException() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException("Column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "' "
				+ RuleTemplateConstants.TEMPLATE_NAME_CANNOT_BE_NULL))
			.given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(RuleTemplateConstants.TEMPLATE_NAME_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("updateRuleTemplate - template name too long throws ValidationErrorException")
	void testUpdateRuleTemplateTemplateNameTooLongThrowsValidationErrorException() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException(
				RuleTemplateConstants.DATA_TRUNCATION + ": " + RuleTemplateConstants.DATA_TOO_LONG + " for column '"
						+ RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("updateRuleTemplate - template name column error without truncation or too long rethrown")
	void testUpdateRuleTemplateTemplateNameColumnErrorWithoutTruncationOrTooLongRethrown() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException(
				"Invalid value for column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("updateRuleTemplate - Data truncation only throws ValidationErrorException")
	void testUpdateRuleTemplateDataIntegrityViolationDataTruncationOnly() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException(RuleTemplateConstants.DATA_TRUNCATION + " for column '"
				+ RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("updateRuleTemplate - Data too long only throws ValidationErrorException")
	void testUpdateRuleTemplateDataIntegrityViolationDataTooLongOnly() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException(
				RuleTemplateConstants.TEMPLATE_NAME_COLUMN + " " + RuleTemplateConstants.DATA_TOO_LONG))
			.given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("updateRuleTemplate - DataIntegrityViolationException with null message rethrown")
	void testUpdateRuleTemplateDataIntegrityViolationExceptionWithNullMessageRethrown() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException((String) null)).given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("updateRuleTemplate - DataIntegrityViolationException without uk_rule_template_name rethrown")
	void testUpdateRuleTemplateDataIntegrityViolationExceptionWithoutUkName() {
		CreateRuleTemplateRequestBodyDto dto = mock(CreateRuleTemplateRequestBodyDto.class);
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(dto.getWorkDayIds()).willReturn(List.of(1));
		given(dto.getWorkTime()).willReturn(List.of(8));
		given(dto.getWorkStartTime()).willReturn(List.of(9));
		given(dto.getWorkEndTime()).willReturn(List.of(17));
		given(dto.getCustomRules()).willReturn(new ArrayList<>());
		given(ruleTemplate.getAddedBy()).willReturn(3);
		given(ruleTemplate.getAddedOn()).willReturn(123456789);
		given(ruleTemplate.getAddedByUserTypeId()).willReturn(1);
		willThrow(new DataIntegrityViolationException("other error")).given(this.ruleTemplateRepository)
			.updateRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.updateRuleTemplate(1, dto))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("other error");
	}

	@Test
	@DisplayName("cloneRuleTemplate - null template name throws ValidationErrorException")
	void testCloneRuleTemplateNullTemplateNameThrowsValidationErrorException() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException("Column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "' "
				+ RuleTemplateConstants.TEMPLATE_NAME_CANNOT_BE_NULL))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage(RuleTemplateConstants.TEMPLATE_NAME_REQUIRED_MESSAGE);
	}

	@Test
	@DisplayName("cloneRuleTemplate - template name too long throws ValidationErrorException")
	void testCloneRuleTemplateTemplateNameTooLongThrowsValidationErrorException() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				RuleTemplateConstants.DATA_TRUNCATION + ": " + RuleTemplateConstants.DATA_TOO_LONG + " for column '"
						+ RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("cloneRuleTemplate - template name column error without truncation or too long rethrown")
	void testCloneRuleTemplateTemplateNameColumnErrorWithoutTruncationOrTooLongRethrown() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				"Invalid value for column '" + RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("cloneRuleTemplate - Data truncation only throws ValidationErrorException")
	void testCloneRuleTemplateDataIntegrityViolationDataTruncationOnly() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(RuleTemplateConstants.DATA_TRUNCATION + " for column '"
				+ RuleTemplateConstants.TEMPLATE_NAME_COLUMN + "'"))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

	@Test
	@DisplayName("cloneRuleTemplate - Data too long only throws ValidationErrorException")
	void testCloneRuleTemplateDataIntegrityViolationDataTooLongOnly() {
		RuleTemplate ruleTemplate = mock(RuleTemplate.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(2);
		given(this.ruleTemplateRepository.getRuleTemplate(1, 1)).willReturn(ruleTemplate);
		given(ruleTemplate.getTemplateName()).willReturn("Test");
		given(ruleTemplate.getWorkLogType()).willReturn(1);
		given(ruleTemplate.getCalculateBreakTime()).willReturn(true);
		given(ruleTemplate.getBreakTimeThreshold()).willReturn(null);
		given(ruleTemplate.getTemplateWorkDay()).willReturn(new ArrayList<>());
		given(ruleTemplate.getCustomRule()).willReturn(new ArrayList<>());
		willThrow(new DataIntegrityViolationException(
				RuleTemplateConstants.TEMPLATE_NAME_COLUMN + " " + RuleTemplateConstants.DATA_TOO_LONG))
			.given(this.ruleTemplateRepository)
			.createRuleTemplate(any(RuleTemplate.class));

		assertThatThrownBy(() -> this.ruleTemplateService.cloneRuleTemplate(1))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Template name must not exceed 200 characters");
	}

}