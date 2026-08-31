package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface IInvitableContactsController {

	ResponseEntity<?> getInvitableContacts(@RequestParam @NotNull @Positive Integer companyId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) @Positive @Max(100) Integer limit);

}
