package io.recruitcrm.microservice.timesheet.controllers.rule_engine.rule_template;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.CloneTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.MarkDefaultRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateNameResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.response_body.RuleTemplateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.rule_engine.rule_template.RuleTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/rule-engine/rule-template")
public class RuleTemplateController implements IRuleTemplateController {

	private final APIResponder apiResponder;

	private final RuleTemplateService ruleTemplateService;

	public RuleTemplateController(APIResponder apiResponder, RuleTemplateService ruleTemplateService) {
		this.apiResponder = apiResponder;
		this.ruleTemplateService = ruleTemplateService;
	}

	@Override
	@PostMapping
	public ResponseEntity<?> createRuleTemplate(@Validated @RequestBody CreateRuleTemplateRequestBodyDto requestDto) {
		this.ruleTemplateService.createRuleTemplate(requestDto);
		return this.apiResponder.respond(null, "Rule template created successfully.", APIResponseType.SUCCESS,
				HttpStatus.CREATED);
	}

	@Override
	@GetMapping("/{templateId}")
	public ResponseEntity<?> getRuleTemplate(@PathVariable Integer templateId) {
		RuleTemplateResponseBodyDto ruleTemplateResponseBodyDto = this.ruleTemplateService.getRuleTemplate(templateId);
		return this.apiResponder.respond(ruleTemplateResponseBodyDto, "Rule template fetched successfully.",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@DeleteMapping("/{templateId}")
	public ResponseEntity<?> deleteRuleTemplate(@PathVariable Integer templateId) {
		this.ruleTemplateService.deleteRuleTemplate(templateId);
		return this.apiResponder.respond(null, "Rule template deleted successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/{templateId}/clone")
	public ResponseEntity<?> cloneRuleTemplate(@PathVariable Integer templateId) {
		CloneTemplateResponseBodyDto cloneTemplateResponseBodyDto = this.ruleTemplateService
			.cloneRuleTemplate(templateId);
		return this.apiResponder.respond(cloneTemplateResponseBodyDto, "Rule template cloned successfully",
				APIResponseType.SUCCESS, HttpStatus.CREATED);
	}

	@Override
	@GetMapping("/list")
	public ResponseEntity<?> getRuleTemplateNames(@RequestParam(required = false) String search,
			PaginationRequestBodyDto paginationRequestBodyDto) {
		List<RuleTemplateNameResponseBodyDto> ruleTemplateNames = this.ruleTemplateService.getRuleTemplateNames(search,
				paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(ruleTemplateNames, "Rule template names fetched successfully.",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/get")
	public ResponseEntity<?> getAllRuleTemplates(@RequestBody SearchRequestBodyDto searchRequestBodyDto,
			@RequestParam(required = false) String search, PaginationRequestBodyDto paginationRequestBodyDto) {
		List<RuleTemplateListResponseBodyDto> ruleTemplates = this.ruleTemplateService
			.getAllRuleTemplates(searchRequestBodyDto, search, paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(ruleTemplates, "Rule template fetched successfully.", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PatchMapping("/{templateId}")
	public ResponseEntity<?> updateRuleTemplate(@PathVariable Integer templateId,
			@Validated @RequestBody CreateRuleTemplateRequestBodyDto requestDto) {
		this.ruleTemplateService.updateRuleTemplate(templateId, requestDto);
		return this.apiResponder.respond(null, "Rule template updated successfully.", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/{templateId}/mark-default")
	public ResponseEntity<?> markAsDefault(@PathVariable Integer templateId,
			@RequestBody MarkDefaultRequestBodyDto requestDto) {
		boolean isDefault = Boolean.TRUE.equals(requestDto.getIsDefault());
		this.ruleTemplateService.markAsDefault(templateId, isDefault);
		String message = isDefault ? "Rule template marked as default successfully."
				: "Rule template unmarked as default successfully.";
		return this.apiResponder.respond(null, message, APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
