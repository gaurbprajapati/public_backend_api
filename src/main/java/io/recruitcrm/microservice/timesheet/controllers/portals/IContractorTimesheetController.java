package io.recruitcrm.microservice.timesheet.controllers.portals;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface IContractorTimesheetController {

	ResponseEntity<?> isTimesheetEnabled(@PathVariable Integer contractorId);

}
