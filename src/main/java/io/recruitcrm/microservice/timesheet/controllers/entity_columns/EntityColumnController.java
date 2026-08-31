package io.recruitcrm.microservice.timesheet.controllers.entity_columns;

import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.entity_columns.EntityColumnService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/v1")
public class EntityColumnController implements IEntityColumnController {

	private final EntityColumnService entityColumnService;

	private final APIResponder apiResponder;

	public EntityColumnController(final EntityColumnService entityColumnService, final APIResponder apiResponder) {
		this.entityColumnService = entityColumnService;
		this.apiResponder = apiResponder;
	}

	@GetMapping("/entity-columns")
	public ResponseEntity<?> getEntityColumns(@RequestParam(name = "entity", required = true) String entity) {
		AccountViewColumnResponseBodyDto entityColumns = this.entityColumnService.getEntityColumns(entity);
		Map<String, AccountViewColumnResponseBodyDto> response = Map.of("columns", entityColumns);
		return this.apiResponder.respond(List.of(response), "Entity columns fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@GetMapping("/account-view-columns")
	public ResponseEntity<?> getAccountViewColumns(@RequestParam(name = "entity", required = true) String entity) {
		AccountViewColumnResponseBodyDto accountViewColumns = this.entityColumnService
			.getAccountViewColumnsByEntity(entity);
		Map<String, AccountViewColumnResponseBodyDto> response = Map.of("accountViewColumns", accountViewColumns);
		return this.apiResponder.respond(List.of(response), "Account view columns fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
