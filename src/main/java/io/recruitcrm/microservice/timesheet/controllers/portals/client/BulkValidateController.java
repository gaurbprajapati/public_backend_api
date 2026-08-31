package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IBulkValidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/portal/client")
public class BulkValidateController implements IBulkValidateController {

	private final IBulkValidateService bulkValidateService;

	private final APIResponder apiResponder;

	public BulkValidateController(IBulkValidateService bulkValidateService, APIResponder apiResponder) {
		this.bulkValidateService = bulkValidateService;
		this.apiResponder = apiResponder;
	}

	@Override
	@PostMapping("/bulk-validate")
	public ResponseEntity<?> bulkValidate(@Valid @RequestBody BulkValidateRequestBodyDto request) {
		BulkValidateResponseBodyDto response = this.bulkValidateService.bulkValidate(request);
		return this.apiResponder.respond(response, "Bulk validation completed", APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
