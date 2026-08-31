package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import org.springframework.http.ResponseEntity;

public interface IClientJobController {

	ResponseEntity<?> fetchClientJobs();

}
