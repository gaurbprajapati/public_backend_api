package io.recruitcrm.microservice.timesheet.controllers.entity_columns;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface IEntityColumnController {

	ResponseEntity<?> getEntityColumns(@RequestParam(name = "entity", required = true) String entity);

	ResponseEntity<?> getAccountViewColumns(@RequestParam(name = "entity", required = true) String entity);

}
