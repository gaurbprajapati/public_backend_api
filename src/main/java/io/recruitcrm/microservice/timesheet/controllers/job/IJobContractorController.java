package io.recruitcrm.microservice.timesheet.controllers.job;

import io.recruitcrm.microservice.timesheet.dto.job.GetTimesheetEnabledAssignedCandidatesRequestBodyDto;
import org.springframework.http.ResponseEntity;

public interface IJobContractorController {

	ResponseEntity<?> getTimesheetEnabledAssignedCandidates(
			GetTimesheetEnabledAssignedCandidatesRequestBodyDto request);

}
