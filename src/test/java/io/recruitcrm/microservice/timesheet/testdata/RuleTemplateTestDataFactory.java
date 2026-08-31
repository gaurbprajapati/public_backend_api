package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.MarkDefaultRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.CloneTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test data factory for RuleTemplate-related test objects.
 */
public final class RuleTemplateTestDataFactory {

	private RuleTemplateTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	public static CreateRuleTemplateRequestBodyDto createRuleTemplateRequest() {
		CreateRuleTemplateRequestBodyDto dto = new CreateRuleTemplateRequestBodyDto();
		dto.setTemplateName("Test Template");
		dto.setCalculateBreakTime(false);
		dto.setIsUnplannedHoursPayEnabled(0);
		dto.setWorkDayIds(Arrays.asList(1));
		return dto;
	}

	public static CreateRuleTemplateRequestBodyDto createRuleTemplateRequestWithWorkDays() {
		CreateRuleTemplateRequestBodyDto dto = createRuleTemplateRequest();
		dto.setWorkDayIds(Arrays.asList(1));
		dto.setWorkTime(Arrays.asList(8));
		dto.setWorkStartTime(Arrays.asList(9));
		dto.setWorkEndTime(Arrays.asList(17));
		dto.setCustomRules(new ArrayList<>());
		return dto;
	}

	public static List<CustomRule> createCustomRulesList() {
		return Arrays.asList(new CustomRule(), new CustomRule());
	}

	public static RuleTemplateListQueryResultDto createAgencyAddedByQueryResultDto() {
		return new RuleTemplateListQueryResultDto(getDefaultTemplateId(), "Test Template", 0, 1, 10,
				UserTypeEnum.AGENCY_RECRUITER.getId(), 2, 20, UserTypeEnum.COMPANY_CONTACT.getId());
	}

	public static RuleTemplateListQueryResultDto createContactAddedByQueryResultDto() {
		return new RuleTemplateListQueryResultDto(getDefaultTemplateId(), "Test Template", 0, 1, 10,
				UserTypeEnum.COMPANY_CONTACT.getId(), 2, 20, UserTypeEnum.AGENCY_RECRUITER.getId());
	}

	public static UserDetailsQueryResultDto createUserDetailsQueryResult() {
		return new UserDetailsQueryResultDto("Agency User", "agency.jpg");
	}

	public static ContactNamePhotoQueryResultDto createContactNamePhotoQueryResult() {
		return new ContactNamePhotoQueryResultDto("Contact User", "contact.jpg", null);
	}

	public static MarkDefaultRequestBodyDto createMarkDefaultRequest() {
		MarkDefaultRequestBodyDto request = new MarkDefaultRequestBodyDto();
		request.setIsDefault(true);
		return request;
	}

	public static SearchRequestBodyDto createSearchRequest() {
		return new SearchRequestBodyDto();
	}

	public static PaginationRequestBodyDto createPaginationRequest() {
		return new PaginationRequestBodyDto(0, 20);
	}

	// ===== Response DTOs =====

	public static RuleTemplateResponseBodyDto createRuleTemplateResponse() {
		RuleTemplateResponseBodyDto response = new RuleTemplateResponseBodyDto();
		response.setId(getDefaultTemplateId());
		response.setIsUnplannedHoursPayEnabled(0);
		return response;
	}

	public static CloneTemplateResponseBodyDto createCloneTemplateResponse() {
		CloneTemplateResponseBodyDto response = new CloneTemplateResponseBodyDto();
		response.setWorkLogType(1);
		response.setCalculateBreakTime(true);
		response.setCustomRulesCount(2);
		response.setWorkDayIds(Arrays.asList(1, 2, 3));
		response.setIsUnplannedHoursPayEnabled(0);
		return response;
	}

	public static RuleTemplateNameResponseBodyDto createRuleTemplateNameResponse() {
		RuleTemplateNameResponseBodyDto response = new RuleTemplateNameResponseBodyDto();
		response.setId(getDefaultTemplateId());
		response.setTemplateName("Test Template");
		return response;
	}

	public static List<RuleTemplateNameResponseBodyDto> createRuleTemplateNameResponseList() {
		return Arrays.asList(createRuleTemplateNameResponse(), createRuleTemplateNameResponse());
	}

	public static RuleTemplateListResponseBodyDto createRuleTemplateListResponse() {
		RuleTemplateListResponseBodyDto response = new RuleTemplateListResponseBodyDto();
		response.setId(getDefaultTemplateId());
		response.setTemplateName("Test Template");
		return response;
	}

	public static List<RuleTemplateListResponseBodyDto> createRuleTemplateListResponseList() {
		return Arrays.asList(createRuleTemplateListResponse(), createRuleTemplateListResponse());
	}

	// ===== API Response Entities =====

	public static ResponseEntity<APINormalResponse<Void>> createVoidSuccessResponse() {
		APINormalResponse<Void> response = new APINormalResponse<>(null);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	public static ResponseEntity<APINormalResponse<RuleTemplateResponseBodyDto>> createRuleTemplateSuccessResponse(
			RuleTemplateResponseBodyDto data) {
		APINormalResponse<RuleTemplateResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<CloneTemplateResponseBodyDto>> createCloneTemplateSuccessResponse(
			CloneTemplateResponseBodyDto data) {
		APINormalResponse<CloneTemplateResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	public static ResponseEntity<APINormalResponse<List<RuleTemplateNameResponseBodyDto>>> createRuleTemplateNameListSuccessResponse(
			List<RuleTemplateNameResponseBodyDto> data) {
		APINormalResponse<List<RuleTemplateNameResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<List<RuleTemplateListResponseBodyDto>>> createRuleTemplateListSuccessResponse(
			List<RuleTemplateListResponseBodyDto> data) {
		APINormalResponse<List<RuleTemplateListResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Test IDs and Constants =====

	public static Integer getDefaultTemplateId() {
		return 1;
	}

	public static final class Messages {

		public static final String RULE_TEMPLATE_CREATED_SUCCESSFULLY = "Rule template created successfully.";

		public static final String RULE_TEMPLATE_FETCHED_SUCCESSFULLY = "Rule template fetched successfully.";

		public static final String RULE_TEMPLATE_DELETED_SUCCESSFULLY = "Rule template deleted successfully";

		public static final String RULE_TEMPLATE_CLONED_SUCCESSFULLY = "Rule template cloned successfully";

		public static final String RULE_TEMPLATE_NAMES_FETCHED_SUCCESSFULLY = "Rule template names fetched successfully.";

		public static final String RULE_TEMPLATE_FETCHED_SUCCESSFULLY_LIST = "Rule template fetched successfully.";

		public static final String RULE_TEMPLATE_UPDATED_SUCCESSFULLY = "Rule template updated successfully.";

		public static final String RULE_TEMPLATE_MARKED_AS_DEFAULT_SUCCESSFULLY = "Rule template marked as default successfully.";

		public static final String RULE_TEMPLATE_UNMARKED_AS_DEFAULT_SUCCESSFULLY = "Rule template unmarked as default successfully.";

	}

}