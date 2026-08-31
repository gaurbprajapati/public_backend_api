package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/portal/client")
public class ClientJobController implements IClientJobController {

	private final IClientJobService clientJobService;

	private final APIResponder apiResponder;

	public ClientJobController(IClientJobService clientJobService, APIResponder apiResponder) {
		this.clientJobService = clientJobService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("/jobs")
	public ResponseEntity<?> fetchClientJobs() {
		List<ClientJobResponseBodyDto> response = this.clientJobService.fetchClientJobs();
		return this.apiResponder.respond(response, "Jobs fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
