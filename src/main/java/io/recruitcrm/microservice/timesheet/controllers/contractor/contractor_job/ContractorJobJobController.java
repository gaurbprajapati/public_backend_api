package io.recruitcrm.microservice.timesheet.controllers.contractor.contractor_job;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.contractor.IContractorJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/contractors")
public class ContractorJobJobController implements IContractorJobController {

	private final IContractorJobService contractorJobService;

	private final APIResponder apiResponder;

	public ContractorJobJobController(IContractorJobService contractorJobService, APIResponder apiResponder) {
		this.contractorJobService = contractorJobService;
		this.apiResponder = apiResponder;
	}

	@GetMapping("/jobs")
	@Override
	public ResponseEntity<?> getContractorJobs() {
		List<ContractorJobResponseBodyDto> jobs = this.contractorJobService.getContractorJobs();
		return this.apiResponder.respond(jobs, "Contractor jobs fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
