package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface IRuleEngineController {

	@PostMapping("/evaluate")
	ResponseEntity<?> evaluateRules(@RequestBody RuleEngineRequestBodyDto requestBodyDto);

	@PostMapping("/validate")
	ResponseEntity<?> validateRules(@RequestBody RuleEngineRequestBodyDto requestBodyDto);

	@PostMapping("/evaluate-overtime")
	ResponseEntity<?> evaluateRulesOnDemand(@RequestBody BulkUpdateTimeLogsRequestBodyDto requestBodyDto);

}
