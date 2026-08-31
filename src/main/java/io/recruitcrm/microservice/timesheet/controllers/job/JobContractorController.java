package io.recruitcrm.microservice.timesheet.controllers.job;

import io.recruitcrm.microservice.timesheet.dto.job.GetTimesheetEnabledAssignedCandidatesRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.job.IJobContractorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/jobs")
public class JobContractorController implements IJobContractorController {

	private final IJobContractorService jobContractorService;

	private final APIResponder apiResponder;

	public JobContractorController(IJobContractorService jobContractorService, APIResponder apiResponder) {
		this.jobContractorService = jobContractorService;
		this.apiResponder = apiResponder;
	}

	@PostMapping("/get-timesheet-enabled-assigned-candidates")
	@Override
	public ResponseEntity<?> getTimesheetEnabledAssignedCandidates(
			@Validated @RequestBody GetTimesheetEnabledAssignedCandidatesRequestBodyDto request) {
		List<TimesheetEnabledAssignedCandidateResponseBodyDto> candidates = this.jobContractorService
			.getTimesheetEnabledAssignedCandidates(request.getJobIds());
		return this.apiResponder.respond(candidates, "Timesheet enabled assigned candidates fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
