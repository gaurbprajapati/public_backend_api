package io.recruitcrm.microservice.timesheet.controllers.rule_engine.rule_template;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.CreateRuleTemplateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body.MarkDefaultRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface IRuleTemplateController {

	ResponseEntity<?> createRuleTemplate(@RequestBody CreateRuleTemplateRequestBodyDto requestDto);

	ResponseEntity<?> getRuleTemplate(@PathVariable Integer templateId);

	ResponseEntity<?> updateRuleTemplate(@PathVariable Integer templateId,
			@RequestBody CreateRuleTemplateRequestBodyDto requestDto);

	ResponseEntity<?> getRuleTemplateNames(String search, PaginationRequestBodyDto paginationRequestBodyDto);

	ResponseEntity<?> getAllRuleTemplates(SearchRequestBodyDto searchRequestBodyDto, String search,
			PaginationRequestBodyDto paginationRequestBodyDto);

	ResponseEntity<?> cloneRuleTemplate(@PathVariable Integer templateId);

	ResponseEntity<?> deleteRuleTemplate(@PathVariable Integer templateId);

	ResponseEntity<?> markAsDefault(@PathVariable Integer templateId,
			@RequestBody MarkDefaultRequestBodyDto requestDto);

}
