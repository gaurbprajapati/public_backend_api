package io.recruitcrm.microservice.timesheet.controllers.contractor.contractor_job;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface IContractorController {

	ResponseEntity<?> getContractorJobs(@PathVariable Integer contractorId);

}
