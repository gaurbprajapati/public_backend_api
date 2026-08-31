package io.recruitcrm.microservice.timesheet.controllers.portals;

import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.IContractorTimesheetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/contractor")

public class ContractorTimesheetController implements IContractorTimesheetController {

	private final IContractorTimesheetService contractorTimesheetService;

	private final APIResponder apiResponder;

	public ContractorTimesheetController(IContractorTimesheetService contractorTimesheetService,
			APIResponder apiResponder) {
		this.contractorTimesheetService = contractorTimesheetService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("/{contractorId}/timesheet-enabled")
	public ResponseEntity<?> isTimesheetEnabled(@PathVariable Integer contractorId) {
		Integer response = this.contractorTimesheetService.isTimesheetEnabled(contractorId);
		return this.apiResponder.respond(response, "Timesheet enabled status fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
